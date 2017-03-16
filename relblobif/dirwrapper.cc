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
#include <ftw.h>
#include <errno.h>
#include "dirwrapper.hh"
#include "blobfscommon.hh"
#include <easylogging++.h>

using namespace std;
using namespace blobfs;

void DirWrapper::createDirectory(const string& dirPath) throw (r_Error)
{
    struct stat status;
    if (stat(dirPath.c_str(), &status) == IO_ERROR_RC)
    {
        if (mkdir(dirPath.c_str(), 0770) == IO_ERROR_RC)
        {
            LFATAL << "error: failed creating directory - " << dirPath;
            LFATAL << "reason: " << strerror(errno);
            throw r_Error(static_cast<unsigned int>(FILEDATADIR_NOTWRITABLE));
        }
    }
}

int removePath(const char* fpath, __attribute__ ((unused)) const struct stat* sb, 
        __attribute__ ((unused)) int typeflag, __attribute__ ((unused)) struct FTW* ftwbuf)
{
    int ret = remove(fpath);
    if (ret == IO_ERROR_RC)
    {
        LWARNING << "failed deleting path from disk - " << fpath;
        LWARNING << strerror(errno);
    }

    return ret;
}

void DirWrapper::removeDirectory(const string& dirPath) throw (r_Error)
{
    if (nftw(dirPath.c_str(), removePath, 64, FTW_DEPTH | FTW_PHYS) == IO_ERROR_RC)
    {
        if (errno != ENOENT)
        {
            LWARNING << "failed deleting directory from disk - " << dirPath;
            LWARNING << strerror(errno);
        }
    }
}

string DirWrapper::convertToCanonicalPath(const string& dirPath)
{
    if (!dirPath.empty() && dirPath[dirPath.size() - 1] != '/')
    {
        return dirPath + '/';
    }
    else
    {
        return dirPath;
    }
}

string DirWrapper::convertFromCanonicalPath(const string& dirPath)
{
    if (!dirPath.empty() && dirPath[dirPath.size() - 1] == '/')
    {
        return dirPath.substr(0, dirPath.size() - 1);
    }
    else
    {
        return dirPath;
    }
}

DirEntryIterator::DirEntryIterator(const string& dirPathArg, bool filesArg)
    : dirPath(DirWrapper::convertToCanonicalPath(dirPathArg)), filesOnly(filesArg)
{
}

DirEntryIterator::~DirEntryIterator()
{
    close();
}

bool DirEntryIterator::open()
{
    bool ret = true;
    dirStream = opendir(dirPath.c_str());
    if (dirStream == NULL)
    {
        LWARNING << "error opening directory: " << dirPath;
        LWARNING << strerror(errno);
        ret = false;
    }
    return ret;
}

bool DirEntryIterator::done()
{
    return dirEntry == NULL;
}

string DirEntryIterator::next()
{
    string ret("");
    if (dirStream != NULL && (dirEntry = readdir(dirStream)) != NULL)
    {
        if (strcmp(dirEntry->d_name, ".") != 0 && strcmp(dirEntry->d_name, "..") != 0)
        {
            struct stat st;
            if (fstatat(dirfd(dirStream), dirEntry->d_name, &st, 0) == IO_ERROR_RC)
            {
                LWARNING << "failed reading directory: " << dirEntry->d_name;
                LWARNING << strerror(errno);
            }
            else if (!filesOnly && S_ISDIR(st.st_mode))
            {
                ret = dirPath + string(dirEntry->d_name) + '/';
            }
            else if (filesOnly && !S_ISDIR(st.st_mode))
            {
                ret = dirPath + string(dirEntry->d_name);
            }
        }
    }
    return ret;
}

bool DirEntryIterator::close()
{
    bool ret = true;
    if (dirStream != NULL)
    {
        ret = closedir(dirStream) == IO_SUCCESS_RC;
        dirStream = NULL;
        dirEntry = NULL;
    }
    return true;
}