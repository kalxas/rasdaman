/*
 * This file is part of rasdaman community.
 *
 * Rasdaman community is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Rasdaman community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2014 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */

#ifndef FILEUTILS_HH
#define FILEUTILS_HH

#include <string>
#include <cstdio>
#include <memory>
#include <vector>
#include <dirent.h>

namespace common
{
class FileUtils
{
public:
    /// Check if a file exists.
    /// @return A boolean value of whether the file exists.
    static bool fileExists(const std::string &path);
    /// Check if a directory exists.
    /// @return A boolean value of whether a directory at the given path exists.
    static bool dirExists(const std::string &path);

    /// Copies a file.
    /// @param srcFile The source file.
    /// @param destFile The destination file.
    static void copyFile(const std::string &srcFile, const std::string &destFile);

    /// @return contents of file fp
    static std::string readFile(FILE *fp);
    /// @return contents of file at a given path if it exists, otherwise empty string
    static std::string readFileToString(const char *filePath);
    /// @return contents of file at a given path if it exists, otherwise nullptr
    static std::unique_ptr<char[]> readFile(const char *filePath);
    /// @return true if filePath is readable, false otherwise
    static bool isReadable(const char *filePath);

    /// write data to a file at filePath
    static bool writeFile(const char *filePath, const char *data, size_t size);
    static bool writeFile(const char *filePath, const std::string &data);

    /// @return available space on the filesystem at the given path, or -1
    /// in case of error (e.g. path does not exist)
    static long getAvailableFilesystemSpace(const char *path);

    /// Try to create directory at path.
    /// @return true if successful, false otherwise
    static bool createDirectory(const std::string &path);
    /// Try to create directory at path, along with any parent directories as necessary.
    /// @return true if successful, false otherwise
    static bool createDirectoryRecursive(const std::string &path);

    /// Try to remove file at path.
    /// @return true if successful, false otherwise
    static bool removeFile(const std::string &path);
    /// try to remove the directory at path;
    /// return the number of files/directories removed.
    static int removeDirRecursive(const std::string &path);

    /// filePath must not be an empty string.
    /// @return true if the filePath is absolute, false otherwise.
    static bool isAbsolutePath(const std::string &filePath);

    /// filePath must not be an empty string.
    /// @return true if the filePath is a GDAL subdataset, false otherwise
    static bool isGdalSubdataset(const std::string &filePath);

    /// @return true if path is an empty directory, false otherwise.
    static bool isDirEmpty(const std::string &path);

    /// @return a list of files in the given directory path, optionally filtered
    /// by the given extension if not empty.
    /// Note: assumes that dirPath is a directory and exists on the filesystem.
    static std::vector<std::string> listFiles(const std::string &dirPath,
                                              std::string extension = "");

    /// @return path with any leading directory removed, e.g.
    /// `/path/to/something.txt` -> `something.txt`. If the last character of
    /// path is `/` then it is first removed, and then the basename is computed.
    static std::string getBasename(const std::string &path);

    /**
    * Retrieves the directory PATH of file
    * @param fname File name.
    * @return a string containing the
    */
    static std::string dirnameOf(const std::string &fname);
};

/**
 * Iterate through the files in a directory; directories are skipped.
 */
class FileDirIterator
{
public:
    explicit FileDirIterator(const std::string &dirPath);
    ~FileDirIterator() noexcept;

    /// Open directory stream.
    /// @return true if successful, false otherwise; inspect errno in case of false.
    bool open();

    /// @return the next file in the directory; returns an empty string if there
    /// are no more files.
    std::string nextFile();

    /// @return true if the directory stream is open
    bool isOpen() const;

private:
    std::string dirPath;
    DIR *dirStream{nullptr};
};

}  // namespace common
#endif /* FILEUTILS_HH */
