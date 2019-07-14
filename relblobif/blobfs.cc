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
/*************************************************************
 *
 * PURPOSE:
 * The interface used by the file storage modules.
 *
 *
 * COMMENTS:
 *
 ************************************************************/

#include "blobfs.hh"
#include "blobfscommon.hh"           // for BlobFSConfig
#include "blobfstransaction.hh"      // for BlobFSTransaction, BlobFSInsertT...
#include "blobfstransactionlock.hh"  // for BlobFSTransactionLock
#include "blobtile.hh"               // for BLOBTile, BLOBTile::NO_TILE_FOUND
#include "dirwrapper.hh"             // for DirEntryIterator, DirWrapper
#include "lockfile.hh"               // for LockFile
#include "raslib/error.hh"           // for r_Error, FILEDATADIR_NOTFOUND
#include "logging.hh"                // for LERROR, LDEBUG, LINFO

#include <errno.h>                   // for errno
#include <stdlib.h>                  // for getenv
#include <string.h>                  // for strcmp, strerror
#include <sys/stat.h>                // for stat, S_ISDIR
#include <unistd.h>                  // for access, W_OK, X_OK

using namespace std;

extern char globalConnectId[PATH_MAX];

#ifndef FILESTORAGE_TILES_SUBDIR
#define FILESTORAGE_TILES_SUBDIR "TILES"
#endif

#ifndef FILESTORAGE_TRANSACTIONS_SUBDIR
#define FILESTORAGE_TRANSACTIONS_SUBDIR "TRANSACTIONS"
#endif

BlobFS &BlobFS::getInstance()
{
    static BlobFS instance;
    return instance;
}

BlobFS::BlobFS() : BlobFS(BlobFS::getFileStorageRootPath())
{
}

BlobFS::BlobFS(const string &rasdataPathParam)
    : config(DirWrapper::convertToCanonicalPath(rasdataPathParam), string(""), string(""))
{
    LDEBUG << "initializing file storage on directory " << config.rootPath;

    validateFileStorageRootPath();
    config.tilesPath = config.rootPath + FILESTORAGE_TILES_SUBDIR + '/';
    DirWrapper::createDirectory(config.tilesPath);
    config.transactionsPath = config.rootPath + FILESTORAGE_TRANSACTIONS_SUBDIR + '/';
    DirWrapper::createDirectory(config.transactionsPath);

    insertTransaction = new BlobFSInsertTransaction(config);
    updateTransaction = new BlobFSUpdateTransaction(config);
    removeTransaction = new BlobFSRemoveTransaction(config);
    selectTransaction = new BlobFSSelectTransaction(config);

    finalizeUncompletedTransactions();

    LDEBUG << "initialized blob file storage handler with root data directory "
           << config.tilesPath;
}

void BlobFS::validateFileStorageRootPath()
{    
    if (config.rootPath.empty())
    {
        generateError("blob file storage data directory has not been set (-connect setting in rasmgr.conf).",
                      config.rootPath, FILEDATADIR_NOTFOUND);
    }
    struct stat status;
    if (stat(config.rootPath.c_str(), &status) == -1)
    {
        generateError("blob file storage data directory not found", config.rootPath, FILEDATADIR_NOTFOUND);
    }
    if (!S_ISDIR(status.st_mode))
    {
        generateError("path to blob file storage is not a directory", config.rootPath, FILEDATADIR_NOTFOUND);
    }
    if (access(config.rootPath.c_str(), W_OK | X_OK) == -1)
    {
        generateError("blob file storage data directory is not writable", config.rootPath, FILEDATADIR_NOTWRITABLE);
    }
}

void BlobFS::insert(BlobData &blob)
{
    insertTransaction->add(blob);
}

void BlobFS::update(BlobData &blob)
{
    updateTransaction->add(blob);
}

void BlobFS::select(BlobData &blob)
{
    selectTransaction->add(blob);
}

void BlobFS::remove(BlobData &blob)
{
    removeTransaction->add(blob);
}

string BlobFS::getFileStorageRootPath()
{
    auto rootPath = DirWrapper::getDirname(globalConnectId);
    if (rootPath.empty())
    {
        LERROR << "blob file storage data directory has not been set; "
               << "please set the -connect value in rasmgr.conf.";
        throw r_Error(static_cast<unsigned int>(FILEDATADIR_NOTABSOLUTE));
    }
    return DirWrapper::convertToCanonicalPath(rootPath);
}

void BlobFS::generateError(const char *message, const string &path, int errorCode)
{
    LERROR << "Error: " << message << " - " << path;
    LERROR << "Reason: " << strerror(errno);
    throw r_Error(static_cast<unsigned int>(errorCode));
}

void BlobFS::preRasbaseCommit()
{
    insertTransaction->preRasbaseCommit();
    updateTransaction->preRasbaseCommit();
    selectTransaction->preRasbaseCommit();
    removeTransaction->preRasbaseCommit();
}

void BlobFS::postRasbaseCommit()
{
    insertTransaction->postRasbaseCommit();
    updateTransaction->postRasbaseCommit();
    selectTransaction->postRasbaseCommit();
    removeTransaction->postRasbaseCommit();
}

void BlobFS::postRasbaseAbort()
{
    insertTransaction->postRasbaseAbort();
    updateTransaction->postRasbaseAbort();
    selectTransaction->postRasbaseAbort();
    removeTransaction->postRasbaseAbort();
}

void BlobFS::finalizeUncompletedTransactions()
{
    DirEntryIterator subdirIter(config.transactionsPath);
    if (!subdirIter.open())
        return;

    for (string subdir = subdirIter.next(); !subdirIter.done(); subdir = subdirIter.next())
    {
        if (subdir.empty())
            continue;

        LockFile checkTransactionLock(DirWrapper::convertFromCanonicalPath(subdir) + ".lock");
        if (!checkTransactionLock.lock())
            continue;

        BlobFSTransactionLock transactionLock(subdir, true);
        if (transactionLock.lockedForTransaction())
            continue;

        transactionLock.clearTransactionLock();
        auto transaction = std::unique_ptr<BlobFSTransaction>(
            BlobFSTransaction::getBlobFSTransaction(subdir, config));
        if (!transaction)
            continue;

        NNLDEBUG << "transaction in invalid state discovered, recovering...";
        transaction->finalizeUncompleted();
        BLDEBUG << "ok.\n";
    }
    subdirIter.close();
}

BlobFS::~BlobFS()
{
    delete insertTransaction, insertTransaction = nullptr;
    delete updateTransaction, updateTransaction = nullptr;
    delete removeTransaction, removeTransaction = nullptr;
    delete selectTransaction, selectTransaction = nullptr;
}

std::string BlobFS::getBlobFilePath(long long blobId) const
{
    return selectTransaction->getFinalBlobPath(blobId);
}

const BlobFSConfig &BlobFS::getConfig() const
{
    return config;
}
