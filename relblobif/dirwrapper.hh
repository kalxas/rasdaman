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

#ifndef _DIRWRAPPER_HH_
#define _DIRWRAPPER_HH_

#include "raslib/error.hh"

#include <dirent.h>  // for DIR
#include <ftw.h>
#include <sys/stat.h>
#include <string>    // for string

// used by DirWrapper::removeDirectory
int removePath(const char *fpath, const struct stat *sb, int typeflag,
               struct FTW *ftwbuf);

namespace blobfs
{

/**
 * Encapsulate common directory operations.
 */
class DirWrapper
{
public:
    // Create a directory at dirPath; ignore if directory already exists
    static void createDirectory(const std::string &dirPath);

    // Remove a directory at dirPath recursively, including all content
    // files/subdirs; print warning in case of error
    static void removeDirectory(const std::string &dirPath);

    // Append final '/' to dirPath if necessary
    static std::string convertToCanonicalPath(const std::string &dirPath);

    // Remove final '/' from dirPath if necessary
    static std::string convertFromCanonicalPath(const std::string &dirPath);

    // /path/to/dir/file -> /path/to/dir
    static std::string getBasename(const std::string &filePath);
};

/**
 * Encapsulates iteration on the entries of a directory.
 */
class DirEntryIterator
{
public:
    /**
     * Initialize with root directory path; if filesOnly is true then next()
     * will return only files, otherwise only directories.
     */
    DirEntryIterator(const std::string &dirPath, bool filesOnly = false);

    /**
     * Makes sure to close() if necessary
     */
    ~DirEntryIterator();

    bool open();

    /**
     * @return true if there are more entries
     */
    bool done();

    /**
     * Get the next entry of dirPath; if no next entry is found or
     * in case of an error, empty string is returned.
     */
    std::string next();

    bool close();

private:
    std::string dirPath;
    bool filesOnly;
    DIR *dirStream;
    struct dirent *dirEntry;
};

}

#endif  // _DIRWRAPPER_HH_
