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
 * Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Peter Baumann /
 rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.

 * SOURCE: fileutils.hh
 *
 * MODULE: util
 * CLASS:  fileutils
 *
 *
 * COMMENTS:
 *        The fileutils class is used to make a library with which we can 
 * check if a file exists and copy a file.
 *
 */

#include "common/string/stringutil.hh"
#include "fileutils.hh"
#include "logging.hh"
#include <fstream>
#include <iostream>
#include <stdio.h>
#include <string>
#include <sys/stat.h>
#include <sys/statvfs.h>
#include <string.h>
#include <assert.h>
#include <unistd.h>
#define HAVE_BOOST_FILESYSTEM
#ifdef HAVE_BOOST_FILESYSTEM
#include <boost/filesystem.hpp>
#endif

namespace common
{
bool FileUtils::fileExists(const std::string &path)
{
    struct stat info;
    return (stat(path.c_str(), &info) == 0);
}

bool FileUtils::dirExists(const std::string &path)
{
    struct stat info;
    if (stat(path.c_str(), &info) != 0)
        return false;
    return (info.st_mode & S_IFDIR) != 0;
}

void FileUtils::copyFile(const std::string &srcFile, const std::string &destFile)
{
    std::ifstream src(srcFile.c_str(), std::ios::binary);
    std::ofstream dest(destFile.c_str(), std::ios::binary);
    dest << src.rdbuf();
    src.close();
    dest.close();
}

std::string FileUtils::readFile(FILE *fp)
{
    static const size_t BUFFER_SIZE = 1000;
    char buffer[BUFFER_SIZE];
    std::string ret;
    while (fgets(buffer, BUFFER_SIZE, fp) != nullptr)
    {
        ret += buffer;
    }
    return ret;
}

std::string FileUtils::readFileToString(const char *filePath)
{
    FILE *fp = fopen(filePath, "r");
    std::string ret;
    if (fp != nullptr)
    {
        ret = readFile(fp);
        fclose(fp);
    }
    return ret;
}

std::unique_ptr<char[]> FileUtils::readFile(const char *filePath)
{
    struct stat st;
    bool exists = (stat(filePath, &st) == 0);
    if (exists)
    {
        auto fileSize = st.st_size;
        if (fileSize <= 0)
            return nullptr;
        const auto size = static_cast<size_t>(fileSize);
        std::unique_ptr<char[]> ret;
        ret.reset(new char[size]);
        FILE *f = fopen(filePath, "rb");
        if (!f)
            return nullptr;
        auto bytesRead = fread(ret.get(), sizeof(char), size, f);
        fclose(f);
        if (bytesRead != size)
            return nullptr;
        return ret;
    }
    else
    {
        return nullptr;
    }
}

bool FileUtils::isReadable(const char *filePath)
{
#define CHECK_WITH_RETRIES
#ifdef CHECK_WITH_RETRIES
    // cf. https://projects.rasdaman.com/ticket/600#comment:4
    // Random I/O error on network filesystem can cause the check to fail.
    // For robustness we retry the check a couple of times.
    static const size_t maxRetries = 3;
    static const size_t delayBetweenRetries = 1;  // 1 second
    for (size_t retryNo = 0; retryNo < maxRetries; ++retryNo)
    {
        std::ifstream f(filePath, std::ios::in);
        if (f.good())
        {
            return true;
        }
        else
        {
            sleep(delayBetweenRetries);
        }
    }
    return false;
#else
    std::ifstream ifile(filePath, std::ios::in);
    return ifile.good();
#endif
}

bool FileUtils::writeFile(const char *filePath, const char *data, size_t size)
{
    FILE *f = fopen(filePath, "wb");
    if (!f)
        return false;
    auto writtenBytes = fwrite(data, 1, size, f);
    fclose(f);
    return writtenBytes == size;
}

bool FileUtils::writeFile(const char *filePath, const std::string &data)
{
    return writeFile(filePath, data.c_str(), data.size());
}

long FileUtils::getAvailableFilesystemSpace(const char *path)
{
    struct statvfs stat;
    if (statvfs(path, &stat) != 0)
        return -1;  // error
    else
        return static_cast<long>(stat.f_bsize) * static_cast<long>(stat.f_bavail);
}

bool FileUtils::createDirectory(const std::string &path)
{
    int ret = mkdir(path.c_str(), S_IRWXU | S_IRWXG);
    return ret != -1;
}

// adapted from https://stackoverflow.com/a/29828907
bool FileUtils::createDirectoryRecursive(const std::string &path)
{
    if (createDirectory(path))
        return true;

    // failed creating path
    switch (errno)
    {
    case ENOENT:
        // parent didn't exist, try to create it
        {
            auto pos = path.find_last_of('/');
            if (pos == std::string::npos)
                return false;
            if (!createDirectoryRecursive(path.substr(0, pos)))
                return false;
        }
        // parent was created, try again to create child
        return createDirectory(path);

    case EEXIST:
        // make sure it's a directory at path, and not a file or link
        return dirExists(path);

    default:
        return false;
    }
}

bool FileUtils::removeFile(const std::string &path)
{
    int ret = remove(path.c_str());
    return ret == 0;
}

int FileUtils::removeDirRecursive(const std::string &path)
{
#ifdef HAVE_BOOST_FILESYSTEM
    return int(boost::filesystem::remove_all(path));
#else
    // Adapted from https://stackoverflow.com/a/2256974
    DIR *d = opendir(path.c_str());
    size_t path_len = path.size();
    int ret = -1;

    if (d)
    {
        struct dirent *p;

        ret = 0;
        while (!ret && (p = readdir(d)))
        {
            int subRet = -1;

            /* Skip the names "." and ".." as we don't want to recurse on them. */
            if (!strcmp(p->d_name, ".") || !strcmp(p->d_name, ".."))
                continue;

            std::string subPath = path;
            subPath.reserve(path_len + strlen(p->d_name) + 2);
            subPath += "/";
            subPath += p->d_name;

            struct stat statbuf;
            if (!stat(subPath.c_str(), &statbuf))
            {
                if (S_ISDIR(statbuf.st_mode))
                    subRet = removeDirRecursive(subPath);
                else
                    subRet = unlink(subPath.c_str());
            }
            ret = subRet;
        }
        closedir(d);
    }

    if (!ret)
        ret = rmdir(path.c_str());

    return ret;
#endif
}

bool FileUtils::isAbsolutePath(const std::string &filePath)
{
    assert(!filePath.empty());
    return filePath[0] == '/';
}

bool FileUtils::isGdalSubdataset(const std::string &filePath)
{
    assert(!filePath.empty());
    if (filePath[0] != '/')
    {
        // check if it's a GDAL subdataset of the form scheme:path:..
        for (auto c: filePath)
        {
            if (c == ':')
                return true;
            if (c == '/')
                break;  // any '/' before a ':' in this case = not a subdataset
        }
    }
    else if (StringUtil::startsWithExactCase(filePath.c_str(), "/vsi"))
    {
        // https://gdal.org/user/virtual_file_systems.html
        return true;
    }
    return false;
}

bool FileUtils::isDirEmpty(const std::string &path)
{
#ifdef HAVE_BOOST_FILESYSTEM
    return boost::filesystem::is_empty(path);
#else
    FileDirIterator dirIter(path);
    return dirIter.open() && dirIter.nextFile() == "";
#endif
}

std::vector<std::string> FileUtils::listFiles(const std::string &dirPath,
                                              std::string extension)
{
    std::vector<std::string> ret;
    assert(dirExists(dirPath));
    FileDirIterator dirIter(dirPath);
    dirIter.open();
    while (dirIter.isOpen())
    {
        auto f = dirIter.nextFile();
        LTRACE << "next file '" << f << "'";
        if (f.empty())
            break;
        if (extension.empty())
        {
            ret.push_back(f);
        }
        else
        {
            const auto sn = f.size();
            const auto suffn = extension.size();
            if (suffn <= sn)
            {
                bool add = true;
                for (size_t i = sn - suffn, j = 0; i < sn; ++i, ++j)
                {
                    if (f[i] != extension[j])
                    {
                        add = false;
                        break;
                    }
                }
                if (add)
                {
                    LTRACE << "adding file " << f;
                    ret.push_back(f);
                }
            }
        }
    }
    return ret;
}

std::string FileUtils::getBasename(const std::string &path)
{
    if (!path.empty())
    {
        size_t end = path.size() - 1;
        if (path.back() == '/')
            --end;
        auto pos = path.find_last_of('/', end);
        if (pos != std::string::npos)
        {
            return path.substr(pos + 1, end - pos);
        }
    }
    return path;
}

std::string FileUtils::dirnameOf(const std::string &fname)
{
    size_t pos = fname.find_last_of("/");
    return (std::string::npos == pos)
               ? ""
               : fname.substr(0, pos);
}

FileDirIterator::FileDirIterator(const std::string &dirPathArg)
    : dirPath{dirPathArg}
{
}

FileDirIterator::~FileDirIterator() noexcept
{
    if (dirStream != nullptr)
    {
        closedir(dirStream);
        dirStream = nullptr;
    }
}

bool FileDirIterator::open()
{
    dirStream = opendir(dirPath.c_str());
    return dirStream != nullptr;
}

std::string FileDirIterator::nextFile()
{
    if (!isOpen())
        return "";

    struct dirent *dirEntry;
    while (dirStream != nullptr && (dirEntry = readdir(dirStream)) != nullptr)
    {
        const char *d_name = dirEntry->d_name;
        if (strcmp(d_name, ".") != 0 && strcmp(d_name, "..") != 0)
        {
            struct stat st;
            if (fstatat(dirfd(dirStream), d_name, &st, 0) == -1)
            {
                LWARNING << "failed stat on path " << dirPath << "/" << d_name;
                return "";  // failed reading
            }

            if (!S_ISDIR(st.st_mode))
            {
                return dirPath + "/" + std::string(d_name);
            }
        }
    }
    return "";
}

bool FileDirIterator::isOpen() const
{
    return dirStream != nullptr;
}

}  // namespace common
