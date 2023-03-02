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

#include "dirwrapper.hh"
#include "blobfscommon.hh"  // for IO_ERROR_RC, IO_SUCCESS_RC
#include "raslib/error.hh"  // for r_Error, FILEDATADIR_NOTWRITABLE
#include <logging.hh>

#include <errno.h>     // for errno, ENOENT
#include <ftw.h>       // for nftw, FTW_DEPTH, FTW_PHYS
#include <stdio.h>     // for remove
#include <string.h>    // for strerror, strcmp
#include <sys/stat.h>  // for stat, fstatat, mkdir, S_ISDIR
#include <dirent.h>    // for DIR
#include <cassert>

using std::string;

// -----------------------------------------------------------------------------
//                               DirWrapper
// -----------------------------------------------------------------------------

void DirWrapper::createDirectory(const string &dirPath)
{
    createDirectory(dirPath.c_str());
}
void DirWrapper::createDirectory(const char *dirPath)
{
    if (!directoryExists(dirPath))
    {
        if (mkdir(dirPath, 0770) == IO_ERROR_RC)
        {
            LERROR << "failed creating directory - " << dirPath
                   << ", reason: " << strerror(errno);
            throw r_Error(static_cast<unsigned int>(FILEDATADIR_NOTWRITABLE));
        }
    }
}

int removePath(const char *fpath, const struct stat *, int, struct FTW *)
{
    int ret = remove(fpath);
    if (ret == IO_ERROR_RC)
    {
        LWARNING << "failed deleting path from disk - " << fpath
                 << ", reason: " << strerror(errno);
    }
    return ret;
}

void DirWrapper::removeDirectory(const string &dirPath)
{
    if (nftw(dirPath.c_str(), removePath, 64, FTW_DEPTH | FTW_PHYS) == IO_ERROR_RC)
    {
        if (errno != ENOENT)
        {
            LWARNING << "failed deleting directory from disk - " << dirPath
                     << ", reason: " << strerror(errno);
        }
    }
}

bool DirWrapper::directoryExists(const char *dirPath)
{
    struct stat status;
    if (stat(dirPath, &status) == IO_ERROR_RC)
        return false;
    else
    {
        if (!S_ISDIR(status.st_mode))
            LWARNING << "found a non-directory while checking "
                        "if a directory exists: "
                     << dirPath;
        return true;
    }
}

string DirWrapper::toCanonicalPath(const string &dirPath)
{
    return !dirPath.empty() && dirPath.back() != '/'
               ? dirPath + '/'
               : dirPath;
}

string DirWrapper::fromCanonicalPath(const string &dirPath)
{
    return !dirPath.empty() && dirPath.back() == '/'
               ? dirPath.substr(0, dirPath.size() - 1)
               : dirPath;
}

string DirWrapper::getDirname(const std::string &filePath)
{
    assert(!filePath.empty());
    auto index = filePath.find_last_of("/");
    return index != string::npos
               ? filePath.substr(0, index)
               : "";
}

string DirWrapper::getBasename(const std::string &path)
{
    if (path.empty())
        return "";

    auto endPos = path.size() - 1;
    while (endPos >= 1 && path[endPos] == '/')
        --endPos;
    auto startPos = path.find_last_of("/", endPos);
    startPos = startPos == string::npos ? 0 : startPos + 1;
    auto len = endPos - startPos + 1;
    return path.substr(startPos, len);
}

// -----------------------------------------------------------------------------
//                          DirEntryIterator
// -----------------------------------------------------------------------------

DirEntryIterator::DirEntryIterator(const string &dirPathArg, bool files)
    : dirPath(DirWrapper::toCanonicalPath(dirPathArg)), filesOnly(files)
{
}

DirEntryIterator::~DirEntryIterator()
{
    close();
}

bool DirEntryIterator::open()
{
    if ((dirStream = opendir(dirPath.c_str())) != nullptr)
    {
        return true;
    }
    else
    {
        //        LWARNING << "error opening directory " << dirPath << ": " << strerror(errno);
        LDEBUG << "error opening directory " << dirPath << ": " << strerror(errno);
        errno = 0;
        return false;
    }
}

bool DirEntryIterator::done()
{
    return dirEntry == nullptr;
}

string DirEntryIterator::next()
{
    string ret;
    if (dirStream != nullptr && (dirEntry = readdir(dirStream)) != nullptr)
    {
        const char *d_name = dirEntry->d_name;
        if (strcmp(d_name, ".") != 0 && strcmp(d_name, "..") != 0)
        {
            struct stat st;
            if (fstatat(dirfd(dirStream), d_name, &st, 0) == IO_ERROR_RC)
            {
                if (errno == ENOENT)
                {
                    return next();  // skip if curr dir name is not found
                }
                else
                {
                    LWARNING << "failed reading directory " << d_name
                             << ": " << strerror(errno);
                    errno = 0;
                }
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
    if (dirStream != nullptr)
    {
        bool ret = closedir(dirStream) == IO_SUCCESS_RC;
        dirStream = nullptr;
        dirEntry = nullptr;
        return ret;
    }
    else
    {
        return true;
    }
}
