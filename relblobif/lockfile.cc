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

#include "blobfscommon.hh"  // for INVALID_FILE_DESCRIPTOR, IO_ERROR_RC, IO_...
#include "lockfile.hh"
#include "logging.hh"       // for LWARNING

#include <errno.h>          // for errno
#include <fcntl.h>          // for open, O_WRONLY, O_CREAT
#include <logging.hh>       // for Writer, CWARNING
#include <string.h>         // for strerror
#include <sys/file.h>       // for flock, LOCK_EX, LOCK_NB, LOCK_UN
#include <unistd.h>         // for close, unlink

using namespace std;


LockFile::LockFile(const std::string &path)
    : lockFilePath(path), fd(INVALID_FILE_DESCRIPTOR) {}

LockFile::~LockFile()
{
    unlock();
}

bool LockFile::lock()
{
    bool ret = false;
    if (fd == INVALID_FILE_DESCRIPTOR)
    {
        fd = open(lockFilePath.c_str(), O_CREAT | O_WRONLY | O_SYNC, 0660);
        if (fd == INVALID_FILE_DESCRIPTOR)
            logWarning("Failed opening lock file");
        else if (!(ret = (flock(fd, LOCK_EX | LOCK_NB) == IO_SUCCESS_RC)))
            logWarning("Failed locking file");
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
        if (!ret && flock(checkfd, LOCK_UN) == IO_ERROR_RC)
            logWarning("Failed reverting lock during lock check");
        if (close(checkfd) == IO_ERROR_RC)
            logWarning("Failed closing check file descriptor");
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
        if (!ret && flock(checkfd, LOCK_UN) == IO_ERROR_RC)
            logWarning("Failed reverting lock during lock check");
        if (close(checkfd) == IO_ERROR_RC)
            logWarning("Failed closing check file descriptor");
    }
    return ret;
}

bool LockFile::unlock()
{
    bool ret = false;
    if (fd != INVALID_FILE_DESCRIPTOR)
    {
        ret = close(fd) == IO_SUCCESS_RC;
        if (!ret)
            logWarning("Failed closing lock file descriptor");
        remove(lockFilePath.c_str());
        fd = INVALID_FILE_DESCRIPTOR;
    }
    return ret;
}

void LockFile::logWarning(const char *msg) const
{
    LWARNING << msg << " " << lockFilePath << ": " << strerror(errno);
    errno = 0;
}
