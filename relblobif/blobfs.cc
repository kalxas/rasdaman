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

#include "config.h"
#include <fcntl.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <limits.h>
#include <string.h>
#include <stdio.h>
#include <easylogging++.h>
#include "blobfscommon.hh"
#include "blobtile.hh"
#include "dirwrapper.hh"
#include "blobfs.hh"
#include "blobfstransactionlock.hh"

using namespace std;
using namespace blobfs;

#ifndef FILESTORAGE_TILES_SUBDIR
#define FILESTORAGE_TILES_SUBDIR "TILES"
#endif

#ifndef FILESTORAGE_TRANSACTIONS_SUBDIR
#define FILESTORAGE_TRANSACTIONS_SUBDIR "TRANSACTIONS"
#endif

BlobFS& BlobFS::getInstance()
{
    static BlobFS instance;
    return instance;
}

BlobFS::BlobFS() throw (r_Error)
    : config(BlobFS::getFileStorageRootPath(), string(""), string(""), true),
      insertTransaction(NULL), updateTransaction(NULL), removeTransaction(NULL), selectTransaction(NULL)
{
    init();
}

BlobFS::BlobFS(const string& rasdataPathParam) throw (r_Error)
    : config(DirWrapper::convertToCanonicalPath(rasdataPathParam), string(""), string(""), true),
      insertTransaction(NULL), updateTransaction(NULL), removeTransaction(NULL), selectTransaction(NULL)
{
    init();
}

void BlobFS::init() throw (r_Error)
{
    LDEBUG << "initializing file storage on directory " << config.rootPath;
    if (config.rootPath.empty())
    {
        LFATAL << "blob file storage data directory has not been specified.";
        LFATAL << "please set the environment variable RASDATA, or --with-filedatadir when configuring rasdaman.";
        throw r_Error(static_cast<unsigned int>(FILEDATADIR_NOTFOUND));
    }

    validateFileStorageRootPath();
    config.nested = isNestedStorage();
    if (config.nested)
    {
        config.tilesPath = getNestedStorageRootPath();
    }
    else
    {
        config.tilesPath = config.rootPath;
    }

    config.transactionsPath = getTransactionsRootPath();
    DirWrapper::createDirectory(config.transactionsPath);

    insertTransaction = new BlobFSInsertTransaction(config);
    updateTransaction = new BlobFSUpdateTransaction(config);
    removeTransaction = new BlobFSRemoveTransaction(config);
    selectTransaction = new BlobFSSelectTransaction(config);

    finalizeUncompletedTransactions();

    LINFO << "initialized blob file storage handler with root data directory " << config.tilesPath;
    LDEBUG << "(using " << (config.nested ? "new" : "old") << " storage organization).";
}

void BlobFS::validateFileStorageRootPath() throw (r_Error)
{
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

bool BlobFS::isNestedStorage()
{
    struct stat status;
    const string nestedStorageRootPath = getNestedStorageRootPath();
    if (stat(nestedStorageRootPath.c_str(), &status) == 0 && S_ISDIR(status.st_mode))
    {
        // RASDATA contains a TILES subdir, so this is the nested storage organization
        return true;
    }
    else
    {
        long long blobId = BLOBTile::getAnyTileOid();
        if (blobId == BLOBTile::NO_TILE_FOUND)
        {
            // no tiles have been found in RASBASE, so we continue using
            // the new nested storage organization
            DirWrapper::createDirectory(nestedStorageRootPath);
            return true;
        }
        else
        {
            // tiles are found in RAS_TILES but no TILES subdir exists,
            // so this is flat file organization and not nested
            return false;
        }
    }
}

void BlobFS::insert(BlobData& blob) throw (r_Error)
{
    insertTransaction->add(blob);
}

void BlobFS::update(BlobData& blob) throw (r_Error)
{
    updateTransaction->add(blob);
}

void BlobFS::select(BlobData& blob) throw (r_Error)
{
    selectTransaction->add(blob);
}

void BlobFS::remove(BlobData& blob) throw (r_Error)
{
    removeTransaction->add(blob);
}

const string BlobFS::getNestedStorageRootPath()
{
    const string ret = config.rootPath + FILESTORAGE_TILES_SUBDIR + '/';
    return ret;
}

const string BlobFS::getTransactionsRootPath()
{
    const string ret = config.rootPath + FILESTORAGE_TRANSACTIONS_SUBDIR + '/';
    return ret;
}

const string BlobFS::getFileStorageRootPath()
{
    char* ret = getenv("RASDATA");
    if (ret == NULL || strcmp(ret, "") == 0)
    {
#ifdef FILEDATADIR
        ret = const_cast<char*>(FILEDATADIR);
#endif
        if (ret == NULL)
        {
            LFATAL << "blob file storage data directory has not been specified.";
            LFATAL << "please set the environment variable RASDATA, or --with-filedatadir when configuring rasdaman.";
            throw r_Error(static_cast<unsigned int>(FILEDATADIR_NOTFOUND));
        }
    }
    return DirWrapper::convertToCanonicalPath(string(ret));
}

void BlobFS::generateError(const char* message, const string& path, int errorCode) throw (r_Error)
{
    LFATAL << "Error: " << message << " - " << path;
    LFATAL << "Reason: " << strerror(errno);
    throw r_Error(static_cast<unsigned int>(errorCode));
}

void BlobFS::preRasbaseCommit() throw (r_Error)
{
    insertTransaction->preRasbaseCommit();
    updateTransaction->preRasbaseCommit();
    selectTransaction->preRasbaseCommit();
    removeTransaction->preRasbaseCommit();
}

void BlobFS::postRasbaseCommit() throw (r_Error)
{
    insertTransaction->postRasbaseCommit();
    updateTransaction->postRasbaseCommit();
    selectTransaction->postRasbaseCommit();
    removeTransaction->postRasbaseCommit();
}

void BlobFS::postRasbaseAbort() throw (r_Error)
{
    insertTransaction->postRasbaseAbort();
    updateTransaction->postRasbaseAbort();
    selectTransaction->postRasbaseAbort();
    removeTransaction->postRasbaseAbort();
}

void BlobFS::finalizeUncompletedTransactions() throw (r_Error)
{
    DirEntryIterator subdirIterator(config.transactionsPath);
    if (subdirIterator.open())
    {
        for (string subdir = subdirIterator.next(); !subdirIterator.done(); subdir = subdirIterator.next())
        {
            if (subdir.empty())
            {
                continue;
            }
            LockFile checkTransactionLock(DirWrapper::convertFromCanonicalPath(subdir) + ".lock");
            if (checkTransactionLock.lock())
            {
                BlobFSTransactionLock transactionLock(subdir, true);
                if (!transactionLock.lockedForTransaction())
                {
                    transactionLock.clearTransactionLock();
                    BlobFSTransaction* transaction = BlobFSTransaction::getBlobFSTransaction(subdir, config);
                    if (transaction != NULL)
                    {
                        LINFO << "transaction in invalid state discovered, recovering...";
                        transaction->finalizeUncompleted();
                        delete transaction;
                        transaction = NULL;
                        LINFO << "invalid transaction successfully recovered.";
                    }
                }
                checkTransactionLock.unlock();
            }
        }
        subdirIterator.close();
    }
}

BlobFS::~BlobFS()
{
    if (insertTransaction != NULL)
    {
        delete insertTransaction;
        insertTransaction = NULL;
    }
    if (updateTransaction != NULL)
    {
        delete updateTransaction;
        updateTransaction = NULL;
    }
    if (removeTransaction != NULL)
    {
        delete removeTransaction;
        removeTransaction = NULL;
    }
    if (selectTransaction != NULL)
    {
        delete selectTransaction;
        selectTransaction = NULL;
    }
}
