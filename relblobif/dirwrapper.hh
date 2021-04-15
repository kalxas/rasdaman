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

#pragma once

#include "raslib/error.hh"
#include <string>    // for string
#include <dirent.h>  // for DIR

struct stat;
struct dirent;
struct FTW;

// used by DirWrapper::removeDirectory
int removePath(const char *fpath, const struct stat *sb, int typeflag,
               struct FTW *ftwbuf);

/**
 * Encapsulate common directory operations.
 */
class DirWrapper
{
public:
    /// Create a directory at dirPath; ignore if directory already exists
    static void createDirectory(const std::string &dirPath);
    /// Create a directory at dirPath; ignore if directory already exists
    static void createDirectory(const char *dirPath);

    /// Remove a directory at dirPath recursively, including all content
    /// files/subdirs; print warning in case of error
    static void removeDirectory(const std::string &dirPath);

    /// Return true if dirPath exists, false otherwise
    static bool directoryExists(const char *dirPath);

    /// Append final '/' to dirPath if necessary
    static std::string toCanonicalPath(const std::string &dirPath);

    /// Remove final '/' from dirPath if necessary
    static std::string fromCanonicalPath(const std::string &dirPath);

    /// /path/to/dir/file -> /path/to/dir
    static std::string getDirname(const std::string &filePath);
    
    /// /path/to/dir/file -> file
    /// /path/to/dir/ -> dir
    /// /path/to/dir/// -> dir
    /// dir/ -> dir
    /// file -> file
    /// "" -> ""
    static std::string getBasename(const std::string &path);
};

/**
 * Encapsulates iteration on the entries of a directory.
 */
class DirEntryIterator
{
public:
    /// Initialize with root directory path; if filesOnly is true then next()
    /// will return only files, otherwise only directories.
    DirEntryIterator(const std::string &dirPath, bool filesOnly = false);

    /// Makes sure to close() if necessary
    ~DirEntryIterator();

    /// @return true on success, false otherwise
    bool open();

    /// Get the next entry of dirPath; if no next entry is found or
    /// in case of an error, empty string is returned.
    std::string next();

    /// @return true if there are more entries
    bool done();

    /// @return the next entry of dirPath; if no next entry is found or in case
    /// of an error, empty string is returned. std::string next();
    bool close();

private:
    std::string dirPath;
    bool filesOnly;
    DIR *dirStream;
    struct dirent *dirEntry;
};
