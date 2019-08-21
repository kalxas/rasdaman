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
#include "blobfile.hh"
#include "blobfscommon.hh"
#include "mymalloc/mymalloc.h"
#include "raslib/error.hh"          // for FAILEDWRITINGTODISK, r_Error, BLO...
#include "logging.hh"               // for LERROR, LWARNING

#include <errno.h>                  // for errno, ENOENT
#include <fcntl.h>                  // for open, O_CREAT, O_WRONLY, O_RDONLY
#include <stdio.h>                  // for rename, size_t
#include <string.h>                 // for strerror
#include <sys/stat.h>               // for stat
#include <unistd.h>                 // for write, access, close, read, unlink
#include <assert.h>

using namespace std;

BlobFile::BlobFile(const string &filePathArg)
    : filePath(filePathArg), fd{INVALID_FILE_DESCRIPTOR}
{
}

BlobFile::~BlobFile()
{
    closeFileDescriptor();
}

void BlobFile::insertData(BlobData &blob)
{
    prepareForInserting();
    writeFile(blob.data, blob.size);
}
void BlobFile::updateData(BlobData &blob)
{
    prepareForUpdating();
    writeFile(blob.data, blob.size);
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

void BlobFile::prepareForInserting()
{
    assert(!filePath.empty());
    fd = open(filePath.c_str(), O_CREAT | O_WRONLY | O_SYNC, 0660);
    if (fd == INVALID_FILE_DESCRIPTOR)
        generateError("failed opening blob file for inserting", FAILEDOPENFORWRITING);
}
void BlobFile::prepareForUpdating()
{
    assert(!filePath.empty());
    fd = open(filePath.c_str(), O_CREAT | O_WRONLY | O_SYNC | O_TRUNC, 0660);
    if (fd == INVALID_FILE_DESCRIPTOR)
        generateError("failed opening blob file for updating", FAILEDOPENFORUPDATING);
}
void BlobFile::prepareForReading()
{
    assert(!filePath.empty());
    fd = open(filePath.c_str(), O_RDONLY);
    if (fd == INVALID_FILE_DESCRIPTOR)
        generateError("failed opening blob file for reading", FAILEDOPENFORREADING);
}

void BlobFile::readFile(char *dst, size_t size)
{
    ssize_t count = read(fd, dst, size);
    if (count == IO_ERROR_RC)
        generateError("failed reading data from blob file", FAILEDREADINGFROMDISK);
    if (count < static_cast<ssize_t>(size))
        generateError("failed reading all data from blob file", FAILEDREADINGFROMDISK);
}
void BlobFile::writeFile(char *data, size_t size)
{
    ssize_t written = write(fd, data, size);
    if (written == IO_ERROR_RC)
        generateError("failed writing data to blob file", FAILEDWRITINGTODISK);
    
    if (written < static_cast<ssize_t>(size))
    {
        LERROR << "written only " << written  << " out of " << size 
               << " bytes to blob file; not enough disk space?";
        clearFileDescriptor();
        generateError("failed writing all data to blob file", FAILEDWRITINGTODISK);
    }
    closeFileDescriptor();
}

void BlobFile::clearFileDescriptor()
{
    if (fd != INVALID_FILE_DESCRIPTOR)
        ftruncate(fd, 0);
}

void BlobFile::closeFileDescriptor()
{
    if (fd != INVALID_FILE_DESCRIPTOR)
    {
        if (close(fd) == IO_ERROR_RC)
            generateError("could not close blob file descriptor.", FAILEDIOOPERATION);
        fd = INVALID_FILE_DESCRIPTOR;
    }
}

off_t BlobFile::getSize()
{
    struct stat status;
    if (stat(filePath.c_str(), &status) == IO_ERROR_RC)
        generateError("blob file not found", BLOBFILENOTFOUND);
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
        LERROR << "failed moving file from " << fromFilePath << " to " << toFilePath
               << ", reason: " << strerror(errno);
        unsigned int errorCode = BLOBFILENOTFOUND;
        if (errno != ENOENT)
            errorCode = FAILEDIOOPERATION;
        errno = 0;
        throw r_Error(static_cast<unsigned int>(errorCode));
    }
}

void BlobFile::removeFile(const std::string &filePath)
{
    if (unlink(filePath.c_str()) == IO_ERROR_RC)
    {
        LWARNING << "failed deleting file from disk: " << filePath
                 << ", reason: " << strerror(errno);
    }
}

long long BlobFile::getBlobId()
{
    long long ret = INVALID_BLOB_ID;

    if (!filePath.empty() && !isdigit(filePath.back()))
        return ret; // probably a .lock file, not a blob

    size_t lastIndex = filePath.find_last_of('/');
    if (lastIndex != string::npos)
    {
        auto fileName = filePath.substr(lastIndex + 1);
        try
        {
            ret = std::stoll(fileName);
        }
        catch (std::invalid_argument &ex)
        {
            generateError("could not parse long long blob id", FILENAMETOBLOBIDFAILED);
        }
        catch (std::out_of_range &ex)
        {
            generateError("blob id out of long long range", FILENAMETOBLOBIDFAILED);
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
    errno = 0;
    throw r_Error(static_cast<unsigned int>(errorCode));
}
