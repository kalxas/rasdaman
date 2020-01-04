/*
* This file is part of rasdaman community.
*
* Rasdaman community is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Rasdaman community is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
*
* Copyright 2003 - 2015 Peter Baumann / rasdaman GmbH.
*
* For more information please see <http://www.rasdaman.org>
* or contact Peter Baumann via <baumann@rasdaman.com>.
*/

#include "blobfile.hh"               // for BlobFile
#include "blobfscommon.hh"           // for BlobFSConfig, INVALID_BLOB_ID
#include "blobfstransaction.hh"
#include "blobfstransactionlock.hh"  // for BlobFSTransactionLock
#include "dirwrapper.hh"             // for DirEntryIterator, DirWrapper
#include "reladminif/sqlitewrapper.hh"
#include "raslib/error.hh"           // for r_Error, BLOBFILENOTFOUND, FAILE...
#include "common/string/stringutil.hh"
#include "logging.hh"                // for LINFO, LERROR, LDEBUG

#include <errno.h>                   // for errno
#include <stdlib.h>                  // for mkdtemp
#include <string.h>                  // for strerror
#include <ostream>                   // for stringstream, basic_ostream, ope...
#include <climits>                   // for CHAR_BIT
#include <cassert>

using std::string;
using std::stringstream;

const string BlobFSTransaction::INSERT_TRANSACTIONS_SUBDIR = "insert";
const string BlobFSTransaction::UPDATE_TRANSACTIONS_SUBDIR = "update";
const string BlobFSTransaction::REMOVE_TRANSACTIONS_SUBDIR = "remove";

const long BlobFSTransaction::tileDirsPerDir = 16384;
const long BlobFSTransaction::tilesPerDir = 16384;

BlobFSTransaction::BlobFSTransaction(
    BlobFSConfig &cfg, const std::string &trDir, const std::string &trPath)
    : config(cfg), transactionPath(DirWrapper::toCanonicalPath(trPath))
{
    if (transactionPath.empty() && !trDir.empty())
    {
        initTransactionDirectory(trDir);
    }
    else
    {
        LDEBUG << "Locking pre-existing transaction dir: " << transactionPath;
        transactionLock.reset(new BlobFSTransactionLock(transactionPath));
        transactionLock->lock(TransactionLockType::General);
    }
}

BlobFSTransaction::BlobFSTransaction(BlobFSConfig &cfg) : config(cfg)
{
}

BlobFSTransaction::~BlobFSTransaction()
{
    if (!transactionPath.empty())
        DirWrapper::removeDirectory(transactionPath);
}

void BlobFSTransaction::preRasbaseCommit() {}
void BlobFSTransaction::postRasbaseCommit() {}
void BlobFSTransaction::postRasbaseAbort() {}

string BlobFSTransaction::getTmpBlobPath(long long blobId)
{
    if (blobId > 0)
    {
        return transactionPath + std::to_string(blobId);
    }
    else
    {
        LERROR << "invalid blob id " << blobId;
        throw r_Error(BLOBFILENOTFOUND);
    }
}

string BlobFSTransaction::getFinalBlobPath(long long blobId)
{
    if (blobId <= 0)
    {
        LERROR << "invalid blob id " << blobId;
        throw r_Error(BLOBFILENOTFOUND);
    }

    // string length of a long long
    static const size_t idSize = ((CHAR_BIT * sizeof(long long) / 3) + 2);
    // string length of dir1Index, dir2Index and blobId + 2 slashes
    static const size_t allIdsSize = (idSize * 3) + 2;

    std::string ret;
    ret.reserve(config.tilesPath.size() + allIdsSize + 1);
    ret.append(config.tilesPath);

    long long dir2Index = blobId / tilesPerDir;
    long long dir1Index = dir2Index / tileDirsPerDir;

    ret.append(std::to_string(dir1Index));
    ret.append("/");
    DirWrapper::createDirectory(ret);

    ret.append(std::to_string(dir2Index));
    ret.append("/");
    DirWrapper::createDirectory(ret);

    ret.append(std::to_string(blobId));

    return ret;
}

void BlobFSTransaction::finalizeUncompleted()
{
    if (!transactionLock)
    {
        LWARNING << "Transaction lock not initialized for " << transactionPath
                 << ", cannot finalize interrupted transaction.";
        return;
    }
    
    // collect blob ids from the blob files in the transaction directory;
    collectBlobIds();
    // nothing to do if no blob files are found
    if (blobIds.empty())
        return;
    
    if (!transactionLock->isValid(TransactionLockType::Commit))
    {
        NNLINFO << "invalid transaction commit state; finalizing commit procedure...";
        postRasbaseCommit();
        BLINFO << "ok.\n";
    }
    else if (!transactionLock->isValid(TransactionLockType::Abort))
    {
        NNLINFO << "invalid transaction abort state; finalizing abort procedure...";
        postRasbaseAbort();
        BLINFO << "ok.\n";
    }
    else
    {
        NNLINFO << "invalid transaction state; running recovery procedure...";
        finalizeRasbaseCrash();
        BLINFO << "ok.\n";
    }
}

void BlobFSTransaction::finalizeRasbaseCrash()
{
    // it was already checked in finalizeUncompleted that blobIds is not empty
    assert(!blobIds.empty());
    
    bool tilesTableExists = SQLiteQuery::returnsRows(
        "SELECT name FROM sqlite_master WHERE type='table' AND name='RAS_TILES'");
    if (tilesTableExists)
    {
        BLINFO << "\n";

        for (auto blobId : blobIds)
        {
            const auto tmpBlobPath = getTmpBlobPath(blobId);

            auto checkQuery = "SELECT BlobId FROM RAS_TILES WHERE BlobId = " + 
                              std::to_string(blobId);
            if (SQLiteQuery::returnsRows(checkQuery))
            {
                // blob still in RASBASE, needs to be restored to the TILES dir
                LINFO << " restoring blob file " << getFinalBlobPath(blobId) 
                      << " from " << tmpBlobPath;
                try
                {
                    BlobFile::moveFile(tmpBlobPath, getFinalBlobPath(blobId));
                }
                catch (r_Error &ex)
                {
                    if (ex.get_errorno() != BLOBFILENOTFOUND)
                        throw;
                    // else, blob not found: try to still restore remaining ones
                }
            }
            else
            {
                // blob not found in RASBASE, remove from transaction dir
                LINFO << " removing blob file " << tmpBlobPath;
                BlobFile::removeFile(tmpBlobPath);
            }
        }
    }
    blobIds.clear();
}

void BlobFSTransaction::collectBlobIds()
{
    if (!blobIds.empty())
        return;

    DirEntryIterator blobFileIter(transactionPath, true);
    if (!blobFileIter.open())
        return;

    for (string blobPath = blobFileIter.next();
         !blobFileIter.done(); blobPath = blobFileIter.next())
    {
        if (blobPath.empty())
            continue;
        if (addBlobId(blobPath))
            LINFO << "blob file queued for finalizing an interrupted transaction: " << blobPath;
    }
    blobFileIter.close();
}

bool BlobFSTransaction::addBlobId(const std::string &blobPath)
{
    BlobFile blobFile(blobPath);
    long long blobId = blobFile.getBlobId();
    if (blobId != INVALID_BLOB_ID)
    {
        blobIds.push_back(blobId);
        return true;
    }
    else
    {
        return false;
    }
}

void BlobFSTransaction::initTransactionDirectory(const string &trSubdir)
{
    bool first = transactionPath.empty();
    createTransactionDir(trSubdir);
    createGeneralLock();
    validateTransactionDir(trSubdir, first);
}

void BlobFSTransaction::createTransactionDir(const string &trSubdir)
{
    static const size_t suffixSize = 7; // ".XXXXXX"
    const auto tempDirPathSize = 
            config.transactionsPath.size() + trSubdir.size() + suffixSize;
    transactionPath.clear();
    transactionPath.reserve(tempDirPathSize + 1);
    
    auto tempDirPath = std::unique_ptr<char[]>(new char[tempDirPathSize + 1]);
    sprintf(tempDirPath.get(), "%s%s.XXXXXX", 
            config.transactionsPath.c_str(), trSubdir.c_str());
    
    char *tempDirPathRes = mkdtemp(tempDirPath.get());
    if (tempDirPathRes == NULL)
    {
        LERROR << "failed creating transaction directory " << tempDirPath.get()
               << ", reason: " << strerror(errno);
        throw r_Error(static_cast<unsigned int>(FAILEDCREATINGDIR));
    }
    transactionPath.append(tempDirPath.get());
    transactionPath.append("/");
}

void BlobFSTransaction::createGeneralLock()
{
    if (transactionLock == nullptr)
    {
        transactionLock.reset(new BlobFSTransactionLock(transactionPath));
        transactionLock->lock(TransactionLockType::General);
    }
}

void BlobFSTransaction::validateTransactionDir(const string &transactionSubdir, bool first)
{
    if (DirWrapper::directoryExists(transactionPath.c_str()))
    {
        LDEBUG << "Created " << transactionSubdir 
               << " transaction path: " << transactionPath;
    }
    else if (first)
    {
        LWARNING << "Successfully created " << transactionSubdir 
                 << " transaction path: " << transactionPath
                 << ", however, now it cannot be found on the filesystem; retrying...";
        transactionLock.reset();
        initTransactionDirectory(transactionSubdir);
    }
    else
    {
        LERROR << "Successfully created " << transactionSubdir 
               << " transaction path: " << transactionPath
               << ", however, it still cannot be found on the filesystem.";
        throw r_Error(static_cast<unsigned int>(FAILEDCREATINGDIR));
    }
}

BlobFSTransaction *
BlobFSTransaction::getBlobFSTransaction(const string &trPath, BlobFSConfig &config)
{
    if (trPath.empty())
        return nullptr;

    char transactionType = trPath[config.transactionsPath.size()];
    switch (transactionType)
    {
        case 'i': return new BlobFSInsertTransaction(config, trPath);
        case 'u': return new BlobFSUpdateTransaction(config, trPath);
        case 'r': return new BlobFSRemoveTransaction(config, trPath);
        default:
        {
            LWARNING << "invalid transaction path: " << trPath;
            return nullptr;
        }
    }
}

// -------------------------------------------------------------------------- //
//                                   INSERT                                   //
// -------------------------------------------------------------------------- //

BlobFSInsertTransaction::BlobFSInsertTransaction(
    BlobFSConfig &configArg, const string &transactionPathArg)
    : BlobFSTransaction(configArg, INSERT_TRANSACTIONS_SUBDIR, transactionPathArg)
{
}

void BlobFSInsertTransaction::add(BlobData &blob)
{
    const string blobPath = getTmpBlobPath(blob.blobId);
    BlobFile blobFile(blobPath);
    blobFile.insertData(blob);
    blobIds.push_back(blob.blobId);
}

void BlobFSInsertTransaction::postRasbaseCommit()
{
    if (blobIds.empty())
        return;

    transactionLock->lock(TransactionLockType::Commit);
    for (auto blobId : blobIds)
    {
        BlobFile::moveFile(getTmpBlobPath(blobId), getFinalBlobPath(blobId));
    }
    blobIds.clear();
    transactionLock->clear(TransactionLockType::Commit);
}

void BlobFSInsertTransaction::postRasbaseAbort()
{
    if (blobIds.empty())
        return;

    transactionLock->lock(TransactionLockType::Abort);
    for (auto blobId : blobIds)
    {
        BlobFile::removeFile(getTmpBlobPath(blobId));
    }
    blobIds.clear();
    transactionLock->clear(TransactionLockType::Abort);
}

// -------------------------------------------------------------------------- //
//                                   UPDATE                                   //
// -------------------------------------------------------------------------- //

BlobFSUpdateTransaction::BlobFSUpdateTransaction(
    BlobFSConfig &configArg, const string &transactionPathArg)
    : BlobFSTransaction(configArg, UPDATE_TRANSACTIONS_SUBDIR, transactionPathArg) {}

void BlobFSUpdateTransaction::add(BlobData &blob)
{
    const string blobPath = getTmpBlobPath(blob.blobId);
    BlobFile blobFile(blobPath);
    blobFile.updateData(blob);
    blobIds.push_back(blob.blobId);
}

void BlobFSUpdateTransaction::postRasbaseCommit()
{
    if (blobIds.empty())
        return;

    transactionLock->lock(TransactionLockType::Commit);
    for (auto blobId : blobIds)
    {
        BlobFile::moveFile(getTmpBlobPath(blobId), getFinalBlobPath(blobId));
    }
    blobIds.clear();
    transactionLock->clear(TransactionLockType::Commit);
}

void BlobFSUpdateTransaction::postRasbaseAbort()
{
    if (blobIds.empty())
        return;

    transactionLock->lock(TransactionLockType::Abort);
    for (auto blobId : blobIds)
    {
        BlobFile::removeFile(getTmpBlobPath(blobId));
    }
    blobIds.clear();
    transactionLock->clear(TransactionLockType::Abort);
}

// -------------------------------------------------------------------------- //
//                                   REMOVE                                   //
// -------------------------------------------------------------------------- //

BlobFSRemoveTransaction::BlobFSRemoveTransaction(
    BlobFSConfig &configArg, const string &transactionPathArg)
    : BlobFSTransaction(configArg, REMOVE_TRANSACTIONS_SUBDIR, transactionPathArg) {}

void BlobFSRemoveTransaction::add(BlobData &blob)
{
    if (blob.blobId > 0)
    {
        blobIds.push_back(blob.blobId);
    }
    else
    {
        LERROR << "cannot remove invalid blob id " << blob.blobId;
        throw r_Error(BLOBFILENOTFOUND);
    }
}

void BlobFSRemoveTransaction::preRasbaseCommit()
{
    if (blobIds.empty())
        return;

    transactionLock->lock(TransactionLockType::Abort);
    for (auto blobId : blobIds)
    {
        try
        {
            BlobFile::moveFile(getFinalBlobPath(blobId), getTmpBlobPath(blobId));
        }
        catch (const r_Error &ex)
        {
            // in a remove transaction ignore a blob file not found error,
            // otherwise the array gets locked and cannot be removed.
            if (ex.get_errorno() != BLOBFILENOTFOUND)
                throw;
        }
    }
    transactionLock->clear(TransactionLockType::Abort);
}

void BlobFSRemoveTransaction::postRasbaseCommit()
{
    if (blobIds.empty())
        return;

    transactionLock->lock(TransactionLockType::Commit);
    for (auto blobId : blobIds)
    {
        BlobFile::removeFile(getTmpBlobPath(blobId));
    }
    blobIds.clear();
    transactionLock->clear(TransactionLockType::Commit);
}

void BlobFSRemoveTransaction::postRasbaseAbort()
{
    if (blobIds.empty())
        return;

    transactionLock->lock(TransactionLockType::Abort);
    for (auto blobId : blobIds)
    {
        const string tmpBlobPath = getTmpBlobPath(blobId);
        if (!BlobFile::fileExists(tmpBlobPath))
            continue;

        try
        {
            BlobFile::moveFile(tmpBlobPath, getFinalBlobPath(blobId));
        }
        catch (const r_Error &ex)
        {
            // in a remove transaction ignore a blob file not found error,
            // otherwise the array gets locked and cannot be removed.
            if (ex.get_errorno() != BLOBFILENOTFOUND)
                throw;
        }
    }
    blobIds.clear();
    transactionLock->clear(TransactionLockType::Abort);
}

// -------------------------------------------------------------------------- //
//                                   SELECT                                   //
// -------------------------------------------------------------------------- //

BlobFSSelectTransaction::BlobFSSelectTransaction(BlobFSConfig &configArg)
    : BlobFSTransaction(configArg) {}

void BlobFSSelectTransaction::add(BlobData &blob)
{
    const string blobPath = getFinalBlobPath(blob.blobId);
    BlobFile blobFile(blobPath);
    blobFile.readData(blob);
}
