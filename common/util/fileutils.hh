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

namespace common
{
class FileUtils {
public:
    /// Check if a file exists.
    /// @return A boolean value of whether the file exists.
    static bool fileExists(const std::string& path);
    /// Check if a directory exists.
    /// @return A boolean value of whether a directory at the given path exists.
    static bool dirExists(const std::string& path);

    /// Copies a file.
    /// @param srcFile The source file.
    /// @param destFile The destination file.
    static void copyFile(const std::string& srcFile, const std::string& destFile);
    
    /// @return contents of file fp
    static std::string readFile(FILE *fp);
    
private:

};
}
#endif /* FILEUTILS_HH */

