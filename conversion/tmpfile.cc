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

#include "config.h"
#include "conversion/tmpfile.hh"
#include <easylogging++.h>
#include <unistd.h>
#include <errno.h>
#include <stdlib.h>

#define TMP_FILENAME_TEMPLATE "/tmp/rasdaman.XXXXXX\0"

using namespace std;

const int r_TmpFile::INVALID_FILE_DESCRIPTOR = -1;

r_TmpFile::r_TmpFile() throw (r_Error)
{
    char tmpFileName[] = TMP_FILENAME_TEMPLATE;
    if((fd = mkstemp(tmpFileName)) == INVALID_FILE_DESCRIPTOR)
    {
        LERROR << "failed creating a temporary file.";
        LERROR << "reason: " << strerror(errno);
        throw r_Error(r_Error::r_Error_General);
    }
    fileName = string(tmpFileName);
    unlink(tmpFileName);
}

r_TmpFile::~r_TmpFile(void)
{
    if (fd != INVALID_FILE_DESCRIPTOR)
    {
        close(fd);
        fd = INVALID_FILE_DESCRIPTOR;
        remove(fileName.c_str());
    }
}

std::string r_TmpFile::getFileName() const
{
    return fileName;
}

int r_TmpFile::getFileDescriptor() const
{
    return fd;
}