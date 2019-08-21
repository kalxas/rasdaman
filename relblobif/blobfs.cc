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
#include <limits.h>                  // for PATH_MAX
#include <chrono>
#include <thread>

using namespace std;

extern char globalConnectId[PATH_MAX];

const std::string BlobFS::tilesSubdir        = "TILES";
const std::string BlobFS::transactionsSubdir = "TRANSACTIONS";

BlobFS &BlobFS::getInstance()
{
    static BlobFS instance;
    return instance;
}

BlobFS::BlobFS() : BlobFS(BlobFS::getFileStorageRootPath())
{}

BlobFS::BlobFS(const string &rasdataPathParam)
    : config(DirWrapper::toCanonicalPath(rasdataPathParam), "", "")
{
    LDEBUG << "initializing file storage on directory " << config.rootPath;

    validateFileStorageRootPath();
    
    config.tilesPath = config.rootPath + tilesSubdir + '/';
    DirWrapper::createDirectory(config.tilesPath);
    config.transactionsPath = config.rootPath + transactionsSubdir + '/';
    DirWrapper::createDirectory(config.transactionsPath);

    insertTransaction.reset(new BlobFSInsertTransaction(config));
    updateTransaction.reset(new BlobFSUpdateTransaction(config));
    removeTransaction.reset(new BlobFSRemoveTransaction(config));
    selectTransaction.reset(new BlobFSSelectTransaction(config));

    finalizeUncompletedTransactions();

    LDEBUG << "initialized blob file storage handler with tiles directory "
           << config.tilesPath;
}

string BlobFS::getFileStorageRootPath()
{
    auto rootPath = DirWrapper::getDirname(globalConnectId);
    if (rootPath.empty())
        generateError("blob file storage data directory has not been set ; "
                      "please set the -connect value in rasmgr.conf.",
                      rootPath, FILEDATADIR_NOTFOUND);
    return DirWrapper::toCanonicalPath(rootPath);
}
void BlobFS::validateFileStorageRootPath()
{    
    if (config.rootPath.empty())
        generateError("blob file storage data directory has not been set "
                      "(-connect setting in rasmgr.conf).",
                      config.rootPath, FILEDATADIR_NOTFOUND);
    
    struct stat st;
    if (stat(config.rootPath.c_str(), &st) == -1)
        generateError("blob file storage data directory not found", 
                      config.rootPath, FILEDATADIR_NOTFOUND);
    if (!S_ISDIR(st.st_mode))
        generateError("path to blob file storage is not a directory",
                      config.rootPath, FILEDATADIR_NOTFOUND);
    if (access(config.rootPath.c_str(), W_OK | X_OK) == -1)
        generateError("blob file storage data directory is not writable",
                      config.rootPath, FILEDATADIR_NOTWRITABLE);
}

void BlobFS::insert(BlobData &blob) { insertTransaction->add(blob); }
void BlobFS::update(BlobData &blob) { updateTransaction->add(blob); }
void BlobFS::select(BlobData &blob) { selectTransaction->add(blob); }
void BlobFS::remove(BlobData &blob) { removeTransaction->add(blob); }

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
    DirEntryIterator trDirIter(config.transactionsPath);
    if (!trDirIter.open())
        return;

    for (string trDir = trDirIter.next(); !trDirIter.done(); trDir = trDirIter.next())
    {
        if (trDir.empty())
            continue;

        // lock transaction dir for checking; if locking fails, another
        // rasserver is already checking this trDir so nothing to do
        LockFile checkTrLock(DirWrapper::fromCanonicalPath(trDir) + ".lock");
        if (!checkTrLock.lock())
            continue;
        
        // Wait for 10ms before checking the transaction lock. This is necessary
        // to account for the possibility that another rasserver is currently
        // in the process of initializing this transaction in
        // BlobFSTransaction::initTransactionDirectory, where it has created the 
        // trDir but has not managed to create the General lock yet.
        //
        // 10ms is not practically verified, intuitively it should be more than 
        // enough time for a simple file to be created and locked by another 
        // rasserver.
        this_thread::sleep_for(chrono::milliseconds(10));
        
        // if a transaction lock is already in place, it means that another
        // rasserver is currently running this transaction so nothing to do
        BlobFSTransactionLock trLock(trDir, true);
        if (trLock.isLocked(TransactionLockType::General))
            continue;
        trLock.clear(TransactionLockType::General);
        
        // get the correct transaction object (insert/update/remove)
        auto transaction = std::unique_ptr<BlobFSTransaction>(
            BlobFSTransaction::getBlobFSTransaction(trDir, config));
        if (!transaction)
            continue;

        NNLDEBUG << "transaction in invalid state discovered, recovering...";
        transaction->finalizeUncompleted();
        BLDEBUG << "ok.\n";
    }
    trDirIter.close();
}

std::string BlobFS::getBlobFilePath(long long blobId) const
{
    return selectTransaction->getFinalBlobPath(blobId);
}
const BlobFSConfig &BlobFS::getConfig() const
{
    return config;
}

void BlobFS::generateError(const char *msg, const string &path, int errCode)
{
    LERROR << msg << " - " << path << ", reason: " << strerror(errno);
    throw r_Error(static_cast<unsigned int>(errCode));
}
