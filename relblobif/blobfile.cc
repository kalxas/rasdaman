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
 * COMMENTS:
 *
 ************************************************************/

#include "config.h"
#include "blobfile.hh"
#include "blobfscommon.hh"
#include "mymalloc/mymalloc.h"
#include "raslib/error.hh"      // for FAILEDWRITINGTODISK, r_Error, BLO...
#include "logging.hh"               // for LERROR, LWARNING

#include <errno.h>                  // for errno, ENOENT
#include <fcntl.h>                  // for open, O_CREAT, O_WRONLY, O_RDONLY
#include <stdio.h>                  // for rename, size_t
#include <string.h>                 // for strerror
#include <sys/stat.h>               // for stat
#include <unistd.h>                 // for write, access, close, read, unlink
#include <istream>                  // for stringstream, basic_istream, basi...

using namespace std;

BlobFile::BlobFile(const string &filePathArg)
    : filePath(filePathArg), fd(INVALID_FILE_DESCRIPTOR) {}

BlobFile::~BlobFile()
{
    if (fd != INVALID_FILE_DESCRIPTOR)
    {
        closeFileDescriptor();
    }
}

void BlobFile::insertData(BlobData &blob)
{
    prepareForInserting();
    ssize_t count = write(fd, blob.data, blob.size);
    if (count == IO_ERROR_RC)
    {
        generateError("failed writing data to blob file", FAILEDWRITINGTODISK);
    }
    if (count < static_cast<ssize_t>(blob.size))
    {
        LERROR << "written only " << count << " out of " << blob.size << " bytes to blob file; not enough disk space?";
        clearFileDescriptor();
        generateError("failed writing all data to blob file", FAILEDWRITINGTODISK);
    }
    closeFileDescriptor();
}

void BlobFile::updateData(BlobData &blob)
{
    prepareForUpdating();
    ssize_t count = write(fd, blob.data, blob.size);
    if (count == IO_ERROR_RC)
    {
        generateError("failed writing data to blob file", FAILEDWRITINGTODISK);
    }
    if (count < static_cast<ssize_t>(blob.size))
    {
        LERROR << "written only " << count << " out of " << blob.size << " bytes to blob file; not enough disk space?";
        clearFileDescriptor();
        generateError("failed writing all data to blob file", FAILEDWRITINGTODISK);
    }
    closeFileDescriptor();
}

void BlobFile::readData(BlobData &blob)
{
    blob.size = static_cast<r_Bytes>(getSize());
    if (blob.size == 0)
        generateError("cannot read empty blob file", EMPTYBLOBFILE);
    blob.data = static_cast<char *>(mymalloc(static_cast<size_t>(blob.size)));
    prepareForReading();
    readFile(blob.data, blob.size);
    closeFileDescriptor();
}

void BlobFile::readFile(char *dst, size_t size)
{
//  LDEBUG << "reading blob file of size " << size;
    ssize_t count = read(fd, dst, size);
    if (count == IO_ERROR_RC)
        generateError("failed reading data from blob file", FAILEDREADINGFROMDISK);
    if (count < static_cast<ssize_t>(size))
        generateError("failed reading all data from blob file", FAILEDREADINGFROMDISK);
}

void BlobFile::prepareForInserting()
{
    fd = open(filePath.c_str(), O_CREAT | O_WRONLY, 0660);
    if (fd == INVALID_FILE_DESCRIPTOR)
    {
        generateError("failed opening blob file for inserting", FAILEDOPENFORWRITING);
    }
}

void BlobFile::prepareForUpdating()
{
    fd = open(filePath.c_str(), O_WRONLY | O_CREAT | O_TRUNC, 0660);
    if (fd == INVALID_FILE_DESCRIPTOR)
    {
        generateError("failed opening blob file for updating", FAILEDOPENFORUPDATING);
    }
}

void BlobFile::prepareForReading()
{
    fd = open(filePath.c_str(), O_RDONLY);
    if (fd == INVALID_FILE_DESCRIPTOR)
    {
        generateError("failed opening blob file for reading", FAILEDOPENFORREADING);
    }
}

void BlobFile::clearFileDescriptor()
{
    if (fd != INVALID_FILE_DESCRIPTOR)
    {
        ftruncate(fd, 0);
    }
}

void BlobFile::closeFileDescriptor()
{
    if (fd != INVALID_FILE_DESCRIPTOR)
    {
        if (close(fd) == IO_ERROR_RC)
        {
            LERROR << "could not close blob file descriptor.";
            generateError("failed I/O operation on blob file", FAILEDIOOPERATION);
        }
        fd = INVALID_FILE_DESCRIPTOR;
    }
}

off_t BlobFile::getSize()
{
    struct stat status;
    if (stat(filePath.c_str(), &status) == IO_ERROR_RC)
    {
        generateError("blob file not found", BLOBFILENOTFOUND);
    }
    return status.st_size;
}

bool BlobFile::fileExists(const string &filePath)
{
    return access(filePath.c_str(), F_OK) != IO_ERROR_RC;
}

void BlobFile::moveFile(const std::string &fromFilePath, const std::string &toFilePath)
{
    if (rename(fromFilePath.c_str(), toFilePath.c_str()) == IO_ERROR_RC)
    {
        LERROR << "failed moving file from " << fromFilePath << " to " << toFilePath << ".";
        LERROR << "reason: " << strerror(errno);
        unsigned int errorCode = BLOBFILENOTFOUND;
        if (errno != ENOENT)
        {
            errorCode = FAILEDIOOPERATION;
        }
        throw r_Error(static_cast<unsigned int>(errorCode));
    }
}

void BlobFile::removeFile(const std::string &filePath)
{
    if (unlink(filePath.c_str()) == IO_ERROR_RC)
    {
        LWARNING << "failed deleting file from disk: " << filePath;
        LWARNING << "reason: " << strerror(errno);
    }
}

long long BlobFile::getBlobId()
{
    long long ret = INVALID_BLOB_ID;
    size_t lastIndex = filePath.find_last_of('/');
    if (lastIndex != string::npos)
    {
        string blobId = filePath.substr(lastIndex + 1);
        stringstream str;
        str << blobId;
        long long tmp;
        if (str >> tmp)
        {
            ret = tmp;
        }
    }
    return ret;
}

void BlobFile::generateError(const char *message, int errorCode)
{
    closeFileDescriptor();
    if (errno != 0)
        LERROR << message << " - " << filePath << ", reason: " << strerror(errno);
    else
        LERROR << message << " - " << filePath;

    {
        auto dirPath = filePath;
        if (dirPath.size() > 1)
        {
            dirPath.erase(std::find(dirPath.rbegin(), dirPath.rend(), '/').base(), dirPath.end());
            struct stat sb;
            if (stat(dirPath.c_str(), &sb) == 0 && S_ISDIR(sb.st_mode))
                LERROR << "Directory " << dirPath << " exists.";
            else
                LERROR << "Directory " << dirPath << " does not exist.";
        }
    }
    throw r_Error(static_cast<unsigned int>(errorCode));
}
