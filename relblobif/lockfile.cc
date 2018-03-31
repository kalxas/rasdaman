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
#include <sys/file.h>
#include <sys/stat.h>
#include <fcntl.h>
#include "lockfile.hh"
#include "blobfscommon.hh"
#include <logging.hh>

using namespace std;
using namespace blobfs;

LockFile::LockFile(const std::string& path)
    : lockFilePath(path), fd(INVALID_FILE_DESCRIPTOR)
{
}

LockFile::~LockFile()
{
    unlock();
}

bool LockFile::lock()
{
    bool ret = false;
    if (fd == INVALID_FILE_DESCRIPTOR)
    {
        fd = open(lockFilePath.c_str(), O_CREAT | O_WRONLY, 0660);
        if (fd != INVALID_FILE_DESCRIPTOR)
        {
            ret = flock(fd, LOCK_EX | LOCK_NB) == IO_SUCCESS_RC;
        }
    }
    return ret;
}

bool LockFile::isLocked()
{
    bool ret = false;
    int checkfd = open(lockFilePath.c_str(), O_WRONLY, 0660);
    if (checkfd != INVALID_FILE_DESCRIPTOR)
    {
        ret = flock(checkfd, LOCK_EX | LOCK_NB) == IO_ERROR_RC;
        if (!ret)
        {
            flock(checkfd, LOCK_UN);
        }
        if (close(checkfd) == IO_ERROR_RC)
        {
            LWARNING << "failed closing check file descriptor: " << strerror(errno);
        }
    }
    return ret;
}

bool LockFile::isValid()
{
    bool ret = true;
    int checkfd = open(lockFilePath.c_str(), O_WRONLY, 0660);
    if (checkfd != INVALID_FILE_DESCRIPTOR)
    {
        ret = flock(checkfd, LOCK_EX | LOCK_NB) == IO_ERROR_RC;
        if (!ret)
        {
            flock(checkfd, LOCK_UN);
        }
        if (close(checkfd) == IO_ERROR_RC)
        {
            LWARNING << "failed closing check file descriptor: " << strerror(errno);
        }
    }
    return ret;
}

bool LockFile::unlock()
{
    bool ret = false;
    if (fd != INVALID_FILE_DESCRIPTOR)
    {
        ret = flock(fd, LOCK_UN) == IO_SUCCESS_RC;
        if (close(fd) == IO_ERROR_RC)
        {
            LWARNING << "failed closing lock file descriptor (" << lockFilePath << "): " << strerror(errno);
        }
        if (unlink(lockFilePath.c_str()) == IO_ERROR_RC)
        {
            if (access(lockFilePath.c_str(), F_OK) != IO_ERROR_RC)
            {
                // lock file still exists, but cannot be removed; perhaps it was locked by another process?
                LWARNING << "failed deleting lock file (" << lockFilePath << "): " << strerror(errno);
            }
            else
            {
                // it was already deleted by another process probably, nothing to do
            }
        }
        fd = INVALID_FILE_DESCRIPTOR;
    }
    return ret;
}
