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
#include <fcntl.h>
#include <sys/stat.h>
#include <limits.h>
#include <string.h>
#include <stdlib.h>
#include <stdio.h>
#include "blobfile.hh"
#include "blobfscommon.hh"
#include <logging.hh>

using namespace std;
using namespace blobfs;

BlobFile::BlobFile(const string& filePathArg)
    : filePath(filePathArg), fd(INVALID_FILE_DESCRIPTOR)
{
}

BlobFile::~BlobFile()
{
    if (fd != INVALID_FILE_DESCRIPTOR)
    {
        closeFileDescriptor();
    }
}

void BlobFile::insertData(BlobData& blob) throw (r_Error)
{
    prepareForInserting();
    ssize_t count = write(fd, blob.data, blob.size);
    if (count == IO_ERROR_RC)
    {
        generateError("failed writing data to blob file", FAILEDWRITINGTODISK);
    }
    if (count < blob.size)
    {
        LFATAL << "written only " << count << " out of " << blob.size << " bytes to blob file.";
        generateError("failed writing all data to blob file", FAILEDWRITINGTODISK);
    }
    closeFileDescriptor();
}

void BlobFile::updateData(BlobData& blob) throw (r_Error)
{
    prepareForUpdating();
    ssize_t count = write(fd, blob.data, blob.size);
    if (count == IO_ERROR_RC)
    {
        generateError("failed writing data to blob file", FAILEDWRITINGTODISK);
    }
    if (count < blob.size)
    {
        LFATAL << "written only " << count << " out of " << blob.size << " bytes to blob file.";
        generateError("failed writing all data to blob file", FAILEDWRITINGTODISK);
    }
    closeFileDescriptor();
}

void BlobFile::readData(BlobData& blob) throw (r_Error)
{
    blob.size = static_cast<r_Bytes>(getSize());
    if (blob.size == 0)
    {
        generateError("cannot read empty blob file", EMPTYBLOBFILE);
    }
    blob.data = static_cast<char*>(malloc(static_cast<size_t>(blob.size)));
    if (blob.data == NULL)
    {
        generateError("failed allocating memory for blob file", MEMMORYALLOCATIONERROR);
    }

    prepareForReading();
    ssize_t count = read(fd, blob.data, blob.size);
    if (count == IO_ERROR_RC)
    {
        generateError("failed reading data from blob file", FAILEDREADINGFROMDISK);
    }
    if (count < blob.size)
    {
        LFATAL << "read only " << count << " out of " << blob.size << " bytes from blob file.";
        generateError("failed reading all data from blob file", FAILEDREADINGFROMDISK);
    }
    closeFileDescriptor();
}

void BlobFile::prepareForInserting() throw (r_Error)
{
    fd = open(filePath.c_str(), O_CREAT | O_WRONLY, 0770);
    if (fd == INVALID_FILE_DESCRIPTOR)
    {
        generateError("failed opening blob file for inserting", FAILEDOPENFORWRITING);
    }
}

void BlobFile::prepareForUpdating() throw (r_Error)
{
    fd = open(filePath.c_str(), O_WRONLY | O_CREAT | O_TRUNC, 0770);
    if (fd == INVALID_FILE_DESCRIPTOR)
    {
        generateError("failed opening blob file for updating", FAILEDOPENFORUPDATING);
    }
}

void BlobFile::prepareForReading() throw (r_Error)
{
    fd = open(filePath.c_str(), O_RDONLY);
    if (fd == INVALID_FILE_DESCRIPTOR)
    {
        generateError("failed opening blob file for reading", FAILEDOPENFORREADING);
    }
}

void BlobFile::closeFileDescriptor() throw (r_Error)
{
    if (close(fd) == IO_ERROR_RC)
    {
        LFATAL << "could not close blob file descriptor.";
        generateError("failed I/O operation on blob file", FAILEDIOOPERATION);
    }
    fd = INVALID_FILE_DESCRIPTOR;
}

off_t BlobFile::getSize() throw (r_Error)
{
    struct stat status;
    if (stat(filePath.c_str(), &status) == IO_ERROR_RC)
    {
        generateError("blob file not found", BLOBFILENOTFOUND);
    }
    return status.st_size;
}

bool BlobFile::fileExists(const string& filePath) throw (r_Error)
{
    return access(filePath.c_str(), F_OK) != IO_ERROR_RC;
}

void BlobFile::moveFile(const std::string& fromFilePath, const std::string& toFilePath) throw (r_Error)
{
    if (rename(fromFilePath.c_str(), toFilePath.c_str()) == IO_ERROR_RC)
    {
        LFATAL << "failed moving file from " << fromFilePath << " to " << toFilePath << ".";
        LFATAL << "reason: " << strerror(errno);
        unsigned int errorCode = BLOBFILENOTFOUND;
        if (errno != ENOENT)
        {
            errorCode = FAILEDIOOPERATION;
        }
        throw r_Error(static_cast<unsigned int>(errorCode));
    }
}

void BlobFile::removeFile(const std::string& filePath)
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

void BlobFile::generateError(const char* message, int errorCode) throw (r_Error)
{
    LFATAL << message << " - " << filePath;
    LFATAL << "reason: " << strerror(errno);
    throw r_Error(static_cast<unsigned int>(errorCode));
}
