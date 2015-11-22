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
#include <unistd.h>
#include <limits.h>
#include <string.h>
#include <errno.h>
#include <stdlib.h>
#include <limits.h>
#include <stdio.h>
#include "blobtile.hh"
#include "blobfilestorage.hh"
#include "../common/src/logging/easylogging++.hh"

using namespace std;

#ifndef FILESTORAGE_TILES_SUBDIR
#define FILESTORAGE_TILES_SUBDIR "TILES"
#endif

#ifndef FILESTORAGE_TILES_PER_DIR
#define FILESTORAGE_TILES_PER_DIR 16384
#endif

#ifndef FILESTORAGE_DIRS_PER_DIR
#define FILESTORAGE_DIRS_PER_DIR  16384
#endif

BlobFileStorage::~BlobFileStorage()
{
}

BlobFileStorage::BlobFileStorage() throw (r_Error)
: fileStorageRootPath(BlobFileStorage::getFileStorageRootPath())
{
    init();
}

BlobFileStorage::BlobFileStorage(const string& rasdataPathParam) throw (r_Error)
: fileStorageRootPath(rasdataPathParam)
{
    init();
}

void BlobFileStorage::init() throw (r_Error)
{
    LDEBUG << "initializing file storage on directory " << fileStorageRootPath;
    if (fileStorageRootPath.empty())
    {
        LFATAL << "blob file storage data directory has not been specified.";
        LFATAL << "please set the environment variable RASDATA, or --with-filedatadir when configuring rasdaman.";
        throw r_Error(static_cast<unsigned int> (FILEDATADIR_NOTFOUND));
    }
    if (fileStorageRootPath[fileStorageRootPath.size() - 1] != '/')
    {
        fileStorageRootPath += '/';
    }

    validateFileStorageRootPath();

    fileStorageTilesPath = fileStorageRootPath;
    nestedStorage = isNestedStorage();
    if (nestedStorage)
    {
        fileStorageTilesPath = getNestedStorageRootPath();
    }

    LINFO << "initialized blob file storage handler with root data directory " << fileStorageTilesPath <<
            " (using " << (nestedStorage ? "new" : "old") << " storage organization).";
}

void BlobFileStorage::validateFileStorageRootPath() throw (r_Error)
{
    struct stat status;
    if (stat(fileStorageRootPath.c_str(), &status) == -1)
    {
        generateError("blob file storage data directory not found", fileStorageRootPath, FILEDATADIR_NOTFOUND);
    }
    if (!S_ISDIR(status.st_mode))
    {
        generateError("path to blob file storage is not a directory", fileStorageRootPath, FILEDATADIR_NOTFOUND);
    }
    if (access(fileStorageRootPath.c_str(), W_OK | X_OK) == -1)
    {
        generateError("blob file storage data directory is not writable", fileStorageRootPath, FILEDATADIR_NOTWRITABLE);
    }
}

void BlobFileStorage::insert(const char* data, r_Bytes dataSize, long long blobId) throw (r_Error)
{
    const string blobPath = getPath(blobId);
    int fd = openFileForInserting(blobPath);
    try
    {
        writeDataToFileDescriptor(fd, blobPath, dataSize, data);
    }
    catch (r_Error& err)
    {
        closeFileDescriptor(fd, blobPath);
        throw err;
    }
    closeFileDescriptor(fd, blobPath);
}

void BlobFileStorage::update(const char* data, r_Bytes dataSize, long long blobId) throw (r_Error)
{
    const string blobPath = getPath(blobId);
    int fd = openFileForUpdating(blobPath);
    try
    {
        writeDataToFileDescriptor(fd, blobPath, dataSize, data);
    }
    catch (r_Error& err)
    {
        closeFileDescriptor(fd, blobPath);
        throw err;
    }
    closeFileDescriptor(fd, blobPath);
}

void BlobFileStorage::retrieve(long long blobId, char** data, r_Bytes* dataSize) throw (r_Error)
{
    const string blobPath = getPath(blobId);
    if (dataSize == NULL)
    {
        LFATAL << "got invalid data size argument while retrieving blob file " << blobPath;
        throw r_Error(r_Error::r_Error_General);
    }
    if (data == NULL)
    {
        LFATAL << "got invalid data argument while retrieving blob file " << blobPath;
        throw r_Error(r_Error::r_Error_General);
    }
    *dataSize = getFileSize(blobPath);
    if (*dataSize <= 0)
    {
        generateError("cannot read empty blob file", blobPath, EMPTYBLOBFILE);
    }
    *data = static_cast<char*> (malloc(static_cast<size_t> (*dataSize)));
    if (*data == NULL)
    {
        generateError("failed allocating memory for blob file", blobPath, MEMMORYALLOCATIONERROR);
    }

    int fd = openFileForReading(blobPath);
    try
    {
        readDataFromFileDescriptor(fd, blobPath, dataSize, data);
    }
    catch (r_Error& err)
    {
        closeFileDescriptor(fd, blobPath);
        throw err;
    }
    closeFileDescriptor(fd, blobPath);
}

void BlobFileStorage::remove(long long blobId) throw (r_Error)
{
    const string blobPath = getPath(blobId);
    if (unlink(blobPath.c_str()) < 0)
    {
        LWARNING << "failed deleting blob file from disk - " << blobPath;
        LWARNING << strerror(errno);
    }
}

int BlobFileStorage::openFileForInserting(const string& blobPath) throw (r_Error)
{
    int fd = open(blobPath.c_str(), O_CREAT | O_WRONLY, 0770);
    if (fd < 0)
    {
        generateError("failed opening blob file for inserting", blobPath, FAILEDOPENFORWRITING);
    }
    return fd;
}

int BlobFileStorage::openFileForUpdating(const string& blobPath) throw (r_Error)
{
    int fd = open(blobPath.c_str(), O_WRONLY | O_CREAT | O_TRUNC, 0770);
    if (fd < 0)
    {
        generateError("failed opening blob file for updating", blobPath, FAILEDOPENFORUPDATING);
    }
    return fd;
}

int BlobFileStorage::openFileForReading(const string& blobPath) throw (r_Error)
{
    int fd = open(blobPath.c_str(), O_RDONLY);
    if (fd < 0)
    {
        generateError("failed opening blob file for reading", blobPath, FAILEDOPENFORREADING);
    }
    return fd;
}

void BlobFileStorage::writeDataToFileDescriptor(int fd, const string& blobPath, r_Bytes dataSize, const char* data) throw (r_Error)
{
    ssize_t count = write(fd, data, dataSize);
    if (count == -1)
    {
        generateError("failed writing data to blob file", blobPath, FAILEDWRITINGTODISK);
    }
    if (count < dataSize)
    {
        LFATAL << "written only " << count << " out of " << dataSize << " bytes to blob file.";
        generateError("failed writing all data to blob file", blobPath, FAILEDWRITINGTODISK);
    }
}

void BlobFileStorage::readDataFromFileDescriptor(int fd, const string& blobPath, r_Bytes* dataSize, char** data) throw (r_Error)
{
    ssize_t count = read(fd, *data, *dataSize);
    if (count == -1)
    {
        generateError("failed reading data from blob file", blobPath, FAILEDREADINGFROMDISK);
    }
    if (count < *dataSize)
    {
        LFATAL << "read only " << count << " out of " << dataSize << " bytes from blob file.";
        generateError("failed reading all data from blob file", blobPath, FAILEDREADINGFROMDISK);
    }
}

void BlobFileStorage::closeFileDescriptor(int fd, const string& blobPath) throw (r_Error)
{
    if (close(fd) < 0)
    {
        LFATAL << "could not close blob file descriptor.";
        generateError("failed I/O operation on blob file", blobPath, FAILEDIOOPERATION);
    }
}

r_Bytes BlobFileStorage::getFileSize(const string& blobPath) throw (r_Error)
{
    struct stat status;
    if (stat(blobPath.c_str(), &status) < 0)
    {
        generateError("blob file not found", blobPath, BLOBFILENOTFOUND);
    }
    return status.st_size;
}

void BlobFileStorage::createDirectory(const string& dirPath) throw (r_Error)
{
    struct stat status;
    if (stat(dirPath.c_str(), &status) < 0)
    {
        if (mkdir(dirPath.c_str(), 0770) == -1)
        {
            generateError("failed creating subdirectory for blob files",
                          dirPath.c_str(), FILEDATADIR_NOTWRITABLE);
        }
    }
}

const string BlobFileStorage::getPath(long long blobId)
{
    if (blobId > 0)
    {
        if (nestedStorage)
        {
            static long long dir2IndexCache = -1;
            static long long dir1IndexCache = -1;
            static stringstream blobPath;
            blobPath.clear();
            blobPath.str("");
            blobPath << fileStorageTilesPath;

            long long dir2Index = blobId / FILESTORAGE_TILES_PER_DIR;
            long long dir1Index = dir2Index / FILESTORAGE_DIRS_PER_DIR;

            blobPath << dir1Index << '/';
            if (dir1IndexCache != dir1Index)
            {
                createDirectory(blobPath.str());
                dir1IndexCache = dir1Index;
                dir2IndexCache = -1;
            }
            blobPath << dir2Index << '/';
            if (dir2IndexCache != dir2Index)
            {
                createDirectory(blobPath.str());
                dir2IndexCache = dir2Index;
            }
            blobPath << blobId;
            return blobPath.str();
        }
        else
        {
            stringstream blobPath;
            blobPath << fileStorageTilesPath;
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

bool BlobFileStorage::isNestedStorage()
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
            createDirectory(nestedStorageRootPath);
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

const string BlobFileStorage::getNestedStorageRootPath()
{
    const string ret = fileStorageRootPath + FILESTORAGE_TILES_SUBDIR + '/';
    return ret;
}

const string BlobFileStorage::getFileStorageRootPath()
{
    char* ret = getenv("RASDATA");
    if (ret == NULL || strcmp(ret, "") == 0)
    {
#ifdef FILEDATADIR
        ret = const_cast<char*> (FILEDATADIR);
#endif
        if (ret == NULL)
        {
            LFATAL << "blob file storage data directory has not been specified.";
            LFATAL << "please set the environment variable RASDATA, or --with-filedatadir when configuring rasdaman.";
            throw r_Error(static_cast<unsigned int> (FILEDATADIR_NOTFOUND));
        }
    }
    return string(ret);
}

const string BlobFileStorage::getFileStorageTilesPath()
{
    return fileStorageTilesPath;
}

void BlobFileStorage::generateError(const char* message, const string& path, int errorCode) throw (r_Error)
{
    LFATAL << "Error: " << message << " - " << path;
    LFATAL << "Reason: " << strerror(errno);
    throw r_Error(static_cast<unsigned int> (errorCode));
}
