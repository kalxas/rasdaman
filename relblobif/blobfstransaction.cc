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
#include "reladminif/sqlitewrapper.hh"          // for SQLiteQuery
#include "raslib/error.hh"       // for r_Error, BLOBFILENOTFOUND, FAILE...
#include "logging.hh"                // for LINFO, LERROR, LDEBUG

#include <errno.h>                   // for errno
#include <stdlib.h>                  // for mkdtemp
#include <string.h>                  // for strerror
#include <ostream>                   // for stringstream, basic_ostream, ope...

using std::string;
using std::stringstream;

#ifndef FILESTORAGE_TILES_PER_DIR
#define FILESTORAGE_TILES_PER_DIR 16384
#endif

#ifndef FILESTORAGE_DIRS_PER_DIR
#define FILESTORAGE_DIRS_PER_DIR 16384
#endif

const string BlobFSTransaction::FILESTORAGE_INSERT_TRANSACTIONS_SUBDIR = "insert";
const string BlobFSTransaction::FILESTORAGE_UPDATE_TRANSACTIONS_SUBDIR = "update";
const string BlobFSTransaction::FILESTORAGE_REMOVE_TRANSACTIONS_SUBDIR = "remove";

BlobFSTransaction::~BlobFSTransaction()
{
    delete transactionLock;
    transactionLock = nullptr;
    if (!transactionPath.empty())
    {
        DirWrapper::removeDirectory(transactionPath);
    }
}

BlobFSTransaction::BlobFSTransaction(
    BlobFSConfig &configArg, const std::string &transactionDir,
    const std::string &fileStorageTransactionPathArg)
    : config(configArg),
      transactionPath(DirWrapper::convertToCanonicalPath(fileStorageTransactionPathArg)),
      transactionLock(nullptr)
{
    if (fileStorageTransactionPathArg.empty() && !transactionDir.empty())
    {
        initTransactionDirectory(transactionDir);
    }
    else
    {
        transactionLock = new BlobFSTransactionLock(transactionPath);
        transactionLock->lockForTransaction();
    }
}

BlobFSTransaction::BlobFSTransaction(BlobFSConfig &configArg)
    : config(configArg), transactionLock(nullptr) {}

void BlobFSTransaction::preRasbaseCommit() {}

void BlobFSTransaction::postRasbaseCommit() {}

void BlobFSTransaction::postRasbaseAbort() {}

const string BlobFSTransaction::getTmpBlobPath(long long blobId)
{
    if (blobId > 0)
    {
        static stringstream ret;
        ret.clear();
        ret.str("");
        ret << transactionPath << blobId;
        return ret.str();
    }
    else
    {
        LERROR << "invalid blob id " << blobId;
        throw r_Error(BLOBFILENOTFOUND);
    }
}

const string BlobFSTransaction::getFinalBlobPath(long long blobId)
{
    if (blobId > 0)
    {
        std::stringstream blobPathStream;
        blobPathStream << config.tilesPath;

        long long dir2Index = blobId / FILESTORAGE_TILES_PER_DIR;
        long long dir1Index = dir2Index / FILESTORAGE_DIRS_PER_DIR;

        blobPathStream << dir1Index << '/';
        DirWrapper::createDirectory(blobPathStream.str());
        blobPathStream << dir2Index << '/';
        DirWrapper::createDirectory(blobPathStream.str());
        blobPathStream << blobId;
        return blobPathStream.str();
    }
    else
    {
        LERROR << "invalid blob id " << blobId;
        throw r_Error(BLOBFILENOTFOUND);
    }
}

void BlobFSTransaction::finalizeUncompleted()
{
    collectBlobIds();
    if (!blobIds.empty())
    {
        if (!validCommitState())
        {
            NNLINFO << "invalid transaction commit state; finalizing commit procedure...";
            postRasbaseCommit();
            BLINFO << "ok.\n";
        }
        else if (!validAbortState())
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
}

void BlobFSTransaction::finalizeRasbaseCrash()
{
    if (!blobIds.empty())
    {
        SQLiteQuery checkTable("SELECT name FROM sqlite_master WHERE type='table' AND name='RAS_TILES'");
        if (checkTable.nextRow())
        {
            if (!blobIds.empty())
            {
                BLINFO << "\n";
            }
            for (auto blobId : blobIds)
            {
                const string tmpBlobPath = getTmpBlobPath(blobId);

                SQLiteQuery checkQuery("SELECT BlobId FROM RAS_TILES WHERE BlobId = %lld", blobId);
                if (checkQuery.nextRow())
                {
                    LINFO << " restoring blob file " << getFinalBlobPath(blobId) << " to " << tmpBlobPath;
                    BlobFile::moveFile(tmpBlobPath, getFinalBlobPath(blobId));
                }
                else
                {
                    LINFO << " removing blob file " << tmpBlobPath;
                    BlobFile::removeFile(tmpBlobPath);
                }
            }
        }
        blobIds.clear();
    }
}

void BlobFSTransaction::collectBlobIds()
{
    if (blobIds.empty())
    {
        DirEntryIterator blobFileIterator(transactionPath, true);
        if (blobFileIterator.open())
        {
            for (string blobFilePath = blobFileIterator.next();
                    !blobFileIterator.done(); blobFilePath = blobFileIterator.next())
            {
                if (!blobFilePath.empty())
                {
                    if (addBlobId(blobFilePath))
                    {
                        LINFO << "blob file queued for finalizing an interrupted transaction: " << blobFilePath;
                    }
                }
            }
            blobFileIterator.close();
        }
    }
}

bool BlobFSTransaction::addBlobId(const std::string blobPath)
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

void BlobFSTransaction::initTransactionDirectory(const string &transactionSubdir)
{
    string tempDirPath = config.transactionsPath + transactionSubdir + ".XXXXXX";
    if (mkdtemp(const_cast<char *>(tempDirPath.c_str())) == nullptr)
    {
        LERROR << "failed creating transaction directory: " << tempDirPath;
        LERROR << "reason: " << strerror(errno);
        throw r_Error(static_cast<unsigned int>(FAILEDCREATINGDIR));
    }
    else
    {
        transactionPath = tempDirPath + '/';
        if (transactionLock == nullptr)
        {
            transactionLock = new BlobFSTransactionLock(transactionPath);
            transactionLock->lockForTransaction();
        }
        LDEBUG << "file storage " << transactionSubdir << " transaction path: " << transactionPath;
    }
}

bool BlobFSTransaction::validState()
{
    return transactionLock->transactionLockValid();
}

bool BlobFSTransaction::validCommitState()
{
    return transactionLock->commitLockValid();
}

bool BlobFSTransaction::validAbortState()
{
    return transactionLock->abortLockValid();
}

BlobFSTransaction *
BlobFSTransaction::getBlobFSTransaction(const string &transactionPath,
                                        BlobFSConfig &config)
{
    BlobFSTransaction *ret = nullptr;
    if (!transactionPath.empty())
    {
        string transactionDir =
            transactionPath.substr(config.transactionsPath.size(),
                                   FILESTORAGE_INSERT_TRANSACTIONS_SUBDIR.size());
        if (transactionDir == FILESTORAGE_INSERT_TRANSACTIONS_SUBDIR)
        {
            ret = new BlobFSInsertTransaction(config, transactionPath);
        }
        else if (transactionDir == FILESTORAGE_UPDATE_TRANSACTIONS_SUBDIR)
        {
            ret = new BlobFSUpdateTransaction(config, transactionPath);
        }
        else if (transactionDir == FILESTORAGE_REMOVE_TRANSACTIONS_SUBDIR)
        {
            ret = new BlobFSRemoveTransaction(config, transactionPath);
        }
    }
    return ret;
}

// -- insert

BlobFSInsertTransaction::BlobFSInsertTransaction(
    BlobFSConfig &configArg, const string &fileStorageTransactionPathArg)
    : BlobFSTransaction(configArg, FILESTORAGE_INSERT_TRANSACTIONS_SUBDIR, fileStorageTransactionPathArg) {}

void BlobFSInsertTransaction::add(BlobData &blob)
{
    const string blobPath = getTmpBlobPath(blob.blobId);
    BlobFile blobFile(blobPath);
    blobFile.insertData(blob);
    blobIds.push_back(blob.blobId);
}

void BlobFSInsertTransaction::postRasbaseCommit()
{
    if (!blobIds.empty())
    {
        transactionLock->lockForCommit();
        for (auto blobId : blobIds)
        {
            BlobFile::moveFile(getTmpBlobPath(blobId), getFinalBlobPath(blobId));
        }
        blobIds.clear();
        transactionLock->clearCommitLock();
    }
}

void BlobFSInsertTransaction::postRasbaseAbort()
{
    if (!blobIds.empty())
    {
        transactionLock->lockForAbort();
        for (auto blobId : blobIds)
        {
            BlobFile::removeFile(getTmpBlobPath(blobId));
        }
        blobIds.clear();
        transactionLock->clearAbortLock();
    }
}

// -- update

BlobFSUpdateTransaction::BlobFSUpdateTransaction(
    BlobFSConfig &configArg, const string &fileStorageTransactionPathArg)
    : BlobFSTransaction(configArg, FILESTORAGE_UPDATE_TRANSACTIONS_SUBDIR, fileStorageTransactionPathArg) {}

void BlobFSUpdateTransaction::add(BlobData &blob)
{
    const string blobPath = getTmpBlobPath(blob.blobId);
    BlobFile blobFile(blobPath);
    blobFile.updateData(blob);
    blobIds.push_back(blob.blobId);
}

void BlobFSUpdateTransaction::postRasbaseCommit()
{
    if (!blobIds.empty())
    {
        transactionLock->lockForCommit();
        for (auto blobId : blobIds)
        {
            BlobFile::moveFile(getTmpBlobPath(blobId), getFinalBlobPath(blobId));
        }
        blobIds.clear();
        transactionLock->clearCommitLock();
    }
}

void BlobFSUpdateTransaction::postRasbaseAbort()
{
    if (!blobIds.empty())
    {
        transactionLock->lockForAbort();
        for (auto blobId : blobIds)
        {
            BlobFile::removeFile(getTmpBlobPath(blobId));
        }
        blobIds.clear();
        transactionLock->clearAbortLock();
    }
}

// -- remove

BlobFSRemoveTransaction::BlobFSRemoveTransaction(
    BlobFSConfig &configArg, const string &fileStorageTransactionPathArg)
    : BlobFSTransaction(configArg, FILESTORAGE_REMOVE_TRANSACTIONS_SUBDIR, fileStorageTransactionPathArg) {}

void BlobFSRemoveTransaction::add(BlobData &blob)
{
    if (blob.blobId > 0)
    {
        blobIds.push_back(blob.blobId);
    }
    else
    {
        LERROR << "invalid blob id " << blob.blobId;
        throw r_Error(BLOBFILENOTFOUND);
    }
}

void BlobFSRemoveTransaction::preRasbaseCommit()
{
    if (!blobIds.empty())
    {
        transactionLock->lockForAbort();
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
                {
                    throw ex;
                }
            }
        }
        transactionLock->clearAbortLock();
    }
}

void BlobFSRemoveTransaction::postRasbaseCommit()
{
    if (!blobIds.empty())
    {
        transactionLock->lockForCommit();
        for (auto blobId : blobIds)
        {
            BlobFile::removeFile(getTmpBlobPath(blobId));
        }
        blobIds.clear();
        transactionLock->clearCommitLock();
    }
}

void BlobFSRemoveTransaction::postRasbaseAbort()
{
    if (!blobIds.empty())
    {
        transactionLock->lockForAbort();
        for (auto blobId : blobIds)
        {
            const string tmpBlobPath = getTmpBlobPath(blobId);
            if (BlobFile::fileExists(tmpBlobPath))
            {
                try
                {
                    BlobFile::moveFile(tmpBlobPath, getFinalBlobPath(blobId));
                }
                catch (const r_Error &ex)
                {
                    // in a remove transaction ignore a blob file not found error,
                    // otherwise the array gets locked and cannot be removed.
                    if (ex.get_errorno() != BLOBFILENOTFOUND)
                    {
                        throw ex;
                    }
                }
            }
        }
        blobIds.clear();
        transactionLock->clearAbortLock();
    }
}

// -- select/retrieve

BlobFSSelectTransaction::BlobFSSelectTransaction(BlobFSConfig &configArg)
    : BlobFSTransaction(configArg) {}

void BlobFSSelectTransaction::add(BlobData &blob)
{
    const string blobPath = getFinalBlobPath(blob.blobId);
    BlobFile blobFile(blobPath);
    blobFile.readData(blob);
}
