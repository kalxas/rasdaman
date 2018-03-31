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

#include "config.h"
#include <fcntl.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>
#include <limits.h>
#include <string.h>
#include <errno.h>
#include <stdlib.h>
#include <limits.h>
#include <stdio.h>
#include "blobfile.hh"
#include "dirwrapper.hh"
#include "blobfstransaction.hh"
#include <logging.hh>
#include "blobfstransactionlock.hh"
#include "reladminif/sqlitewrapper.hh"

using namespace std;
using namespace blobfs;

#ifndef FILESTORAGE_TILES_PER_DIR
#define FILESTORAGE_TILES_PER_DIR 16384
#endif

#ifndef FILESTORAGE_DIRS_PER_DIR
#define FILESTORAGE_DIRS_PER_DIR  16384
#endif

const string BlobFSTransaction::FILESTORAGE_INSERT_TRANSACTIONS_SUBDIR = "insert";
const string BlobFSTransaction::FILESTORAGE_UPDATE_TRANSACTIONS_SUBDIR = "update";
const string BlobFSTransaction::FILESTORAGE_REMOVE_TRANSACTIONS_SUBDIR = "remove";

BlobFSTransaction::~BlobFSTransaction()
{
    if (transactionLock != NULL)
    {
        delete transactionLock;
        transactionLock = NULL;
    }
    if (!transactionPath.empty())
    {
        DirWrapper::removeDirectory(transactionPath);
    }
}

BlobFSTransaction::BlobFSTransaction(BlobFSConfig& configArg,
                                     const std::string& transactionDir,
                                     const std::string& fileStorageTransactionPathArg) throw (r_Error)
    : config(configArg), transactionPath(DirWrapper::convertToCanonicalPath(fileStorageTransactionPathArg)),
      transactionLock(NULL), dir1IndexCache(INVALID_DIR_INDEX), dir2IndexCache(INVALID_DIR_INDEX)
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

BlobFSTransaction::BlobFSTransaction(BlobFSConfig& configArg)
    : config(configArg), transactionLock(NULL), dir1IndexCache(INVALID_DIR_INDEX), dir2IndexCache(INVALID_DIR_INDEX)
{
}

void BlobFSTransaction::preRasbaseCommit() throw (r_Error)
{
}

void BlobFSTransaction::postRasbaseCommit() throw (r_Error)
{
}

void BlobFSTransaction::postRasbaseAbort() throw (r_Error)
{
}

const string BlobFSTransaction::getTmpBlobPath(long long blobId) throw (r_Error)
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
        LFATAL << "invalid blob id " << blobId;
        throw r_Error(BLOBFILENOTFOUND);
    }
}

const string BlobFSTransaction::getFinalBlobPath(long long blobId) throw (r_Error)
{
    if (blobId > 0)
    {
        if (config.nested)
        {
            blobPathStream.clear();
            blobPathStream.str("");
            blobPathStream << config.tilesPath;

            long long dir2Index = blobId / FILESTORAGE_TILES_PER_DIR;
            long long dir1Index = dir2Index / FILESTORAGE_DIRS_PER_DIR;

            blobPathStream << dir1Index << '/';
            if (dir1IndexCache != dir1Index)
            {
                DirWrapper::createDirectory(blobPathStream.str());
                dir1IndexCache = dir1Index;
                dir2IndexCache = INVALID_DIR_INDEX;
            }
            blobPathStream << dir2Index << '/';
            if (dir2IndexCache != dir2Index)
            {
                DirWrapper::createDirectory(blobPathStream.str());
                dir2IndexCache = dir2Index;
            }
            blobPathStream << blobId;
            return blobPathStream.str();
        }
        else
        {
            stringstream blobPath;
            blobPath << config.tilesPath;
            blobPath << blobId;
            return blobPath.str();
        }
    }
    else
    {
        LFATAL << "invalid blob id " << blobId;
        throw r_Error(BLOBFILENOTFOUND);
    }
}

void BlobFSTransaction::finalizeUncompleted() throw (r_Error)
{
    collectBlobIds();
    if (!blobIds.empty())
    {
        if (!validCommitState())
        {
            LINFO << "invalid transaction commit state; finalizing commit procedure...";
            postRasbaseCommit();
            LINFO << "completed recovery of invalid transaction commit.";
        }
        else if (!validAbortState())
        {
            LINFO << "invalid transaction abort state; finalizing abort procedure...";
            postRasbaseAbort();
            LINFO << "completed recovery of invalid transaction abort.";
        }
        else
        {
            LINFO << "invalid transaction state; running recovery procedure...";
            finalizeRasbaseCrash();
            LINFO << "completed recovery of invalid transaction.";
        }
    }
}

void BlobFSTransaction::finalizeRasbaseCrash() throw (r_Error)
{
    if (!blobIds.empty())
    {
        SQLiteQuery checkTable("SELECT name FROM sqlite_master WHERE type='table' AND name='RAS_TILES'");
        if (checkTable.nextRow())
        {
            for (long unsigned int i = 0; i < blobIds.size(); i++)
            {
                long long blobId = blobIds[i];
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

void BlobFSTransaction::initTransactionDirectory(const string& transactionSubdir) throw (r_Error)
{
    string tempDirPath = config.transactionsPath + transactionSubdir + ".XXXXXX";
    if (mkdtemp(const_cast<char*>(tempDirPath.c_str())) == NULL)
    {
        LFATAL << "failed creating transaction directory: " << tempDirPath;
        LFATAL << "reason: " << strerror(errno);
        throw r_Error(static_cast<unsigned int>(FAILEDCREATINGDIR));
    }
    else
    {
        transactionPath = tempDirPath + '/';
        if (transactionLock == NULL)
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

BlobFSTransaction*
BlobFSTransaction::getBlobFSTransaction(const string& transactionPath,
                                        BlobFSConfig& config)
{
    BlobFSTransaction* ret = NULL;
    if (!transactionPath.empty())
    {
        string transactionDir = transactionPath.substr(config.transactionsPath.size(),
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

BlobFSInsertTransaction::BlobFSInsertTransaction(BlobFSConfig& configArg,
        const string& fileStorageTransactionPathArg) throw (r_Error)
    : BlobFSTransaction(configArg, FILESTORAGE_INSERT_TRANSACTIONS_SUBDIR, fileStorageTransactionPathArg)
{
}

void BlobFSInsertTransaction::add(BlobData& blob) throw (r_Error)
{
    const string blobPath = getTmpBlobPath(blob.blobId);
    BlobFile blobFile(blobPath);
    blobFile.insertData(blob);
    blobIds.push_back(blob.blobId);
}

void BlobFSInsertTransaction::postRasbaseCommit() throw (r_Error)
{
    if (!blobIds.empty())
    {
        transactionLock->lockForCommit();
        for (long unsigned int i = 0; i < blobIds.size(); i++)
        {
            BlobFile::moveFile(getTmpBlobPath(blobIds[i]), getFinalBlobPath(blobIds[i]));
        }
        blobIds.clear();
        transactionLock->clearCommitLock();
    }
}

void BlobFSInsertTransaction::postRasbaseAbort() throw (r_Error)
{
    if (!blobIds.empty())
    {
        transactionLock->lockForAbort();
        for (long unsigned int i = 0; i < blobIds.size(); i++)
        {
            BlobFile::removeFile(getTmpBlobPath(blobIds[i]));
        }
        blobIds.clear();
        transactionLock->clearAbortLock();
    }
}

// -- update

BlobFSUpdateTransaction::BlobFSUpdateTransaction(BlobFSConfig& configArg,
        const string& fileStorageTransactionPathArg) throw (r_Error)
    : BlobFSTransaction(configArg, FILESTORAGE_UPDATE_TRANSACTIONS_SUBDIR, fileStorageTransactionPathArg)
{
}

void BlobFSUpdateTransaction::add(BlobData& blob) throw (r_Error)
{
    const string blobPath = getTmpBlobPath(blob.blobId);
    BlobFile blobFile(blobPath);
    blobFile.updateData(blob);
    blobIds.push_back(blob.blobId);
}

void BlobFSUpdateTransaction::postRasbaseCommit() throw (r_Error)
{
    if (!blobIds.empty())
    {
        transactionLock->lockForCommit();
        for (long unsigned int i = 0; i < blobIds.size(); i++)
        {
            BlobFile::moveFile(getTmpBlobPath(blobIds[i]), getFinalBlobPath(blobIds[i]));
        }
        blobIds.clear();
        transactionLock->clearCommitLock();
    }
}

void BlobFSUpdateTransaction::postRasbaseAbort() throw (r_Error)
{
    if (!blobIds.empty())
    {
        transactionLock->lockForAbort();
        for (long unsigned int i = 0; i < blobIds.size(); i++)
        {
            BlobFile::removeFile(getTmpBlobPath(blobIds[i]));
        }
        blobIds.clear();
        transactionLock->clearAbortLock();
    }
}

// -- remove

BlobFSRemoveTransaction::BlobFSRemoveTransaction(BlobFSConfig& configArg,
        const string& fileStorageTransactionPathArg) throw (r_Error)
    : BlobFSTransaction(configArg, FILESTORAGE_REMOVE_TRANSACTIONS_SUBDIR, fileStorageTransactionPathArg)
{
}

void BlobFSRemoveTransaction::add(BlobData& blob) throw (r_Error)
{
    if (blob.blobId > 0)
    {
        blobIds.push_back(blob.blobId);
    }
    else
    {
        LFATAL << "invalid blob id " << blob.blobId;
        throw r_Error(BLOBFILENOTFOUND);
    }
}

void BlobFSRemoveTransaction::preRasbaseCommit() throw (r_Error)
{
    if (!blobIds.empty())
    {
        transactionLock->lockForAbort();
        for (long unsigned int i = 0; i < blobIds.size(); i++)
        {
            BlobFile::moveFile(getFinalBlobPath(blobIds[i]), getTmpBlobPath(blobIds[i]));
        }
        transactionLock->clearAbortLock();
    }
}

void BlobFSRemoveTransaction::postRasbaseCommit() throw (r_Error)
{
    if (!blobIds.empty())
    {
        transactionLock->lockForCommit();
        for (long unsigned int i = 0; i < blobIds.size(); i++)
        {
            BlobFile::removeFile(getTmpBlobPath(blobIds[i]));
        }
        blobIds.clear();
        transactionLock->clearCommitLock();
    }
}

void BlobFSRemoveTransaction::postRasbaseAbort() throw (r_Error)
{
    if (!blobIds.empty())
    {
        transactionLock->lockForAbort();
        for (long unsigned int i = 0; i < blobIds.size(); i++)
        {
            const string tmpBlobPath = getTmpBlobPath(blobIds[i]);
            if (BlobFile::fileExists(tmpBlobPath))
            {
                BlobFile::moveFile(tmpBlobPath, getFinalBlobPath(blobIds[i]));
            }
        }
        blobIds.clear();
        transactionLock->clearAbortLock();
    }
}

// -- select/retrieve

BlobFSSelectTransaction::BlobFSSelectTransaction(BlobFSConfig& configArg) throw (r_Error)
    : BlobFSTransaction(configArg)
{
}

void BlobFSSelectTransaction::add(BlobData& blob) throw (r_Error)
{
    const string blobPath = getFinalBlobPath(blob.blobId);
    BlobFile blobFile(blobPath);
    blobFile.readData(blob);
}
