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
*/

#ifndef _R_TMPFILE_HH_
#define _R_TMPFILE_HH_

#include <string>

//@ManMemo: Module {\bf conversion}

/*@Doc:
 * Manages a temporary file.
 */
class r_TmpFile
{
public:

    /// constructor creates a temporary file
    r_TmpFile();
    /// destructor removes the temporary file
    ~r_TmpFile(void);
    /// return temporary file name
    std::string getFileName() const;
    /// return temporary file descriptor
    int getFileDescriptor() const;
    /// write the given data with dataSize (bytes)
    void writeData(const char *data, size_t dataSize);
    /// write the given data with dataSize (bytes)
    char *readData(long &dataSize);

private:

    void initTmpFile();
    void removeAuxXmlFile() const;

    std::string fileName;
    int fd;
    static const int INVALID_FILE_DESCRIPTOR;
};

#endif
