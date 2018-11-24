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

#include "fileutils.hh"
#include <fstream>
#include <iostream>
#include <stdio.h>
#include <string>
#include <sys/stat.h>
namespace common
{
bool FileUtils::fileExists(const std::string& fileName) 
{
    struct stat buffer;   
    return (stat(fileName.c_str(), &buffer) == 0); 
}

void FileUtils::copyFile(const std::string& srcFile, const std::string& destFile )
{
    std::ifstream src(srcFile.c_str(), std::ios::binary);
    std::ofstream dest(destFile.c_str(), std::ios::binary);
    dest << src.rdbuf();
    src.close();
    dest.close();
}
}
