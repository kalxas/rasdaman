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
#include "ecwmemfs.hh"
#include <fstream>
#include <algorithm>
#include <easylogging++.h>

const char*
MemoryFileSystem::memorySrc = NULL;

r_Bytes
MemoryFileSystem::memorySrcLength = 0;

const char*
MemoryFileSystem::memorySrcName = "memory.src";

MemoryFileSystem::MemoryFileSystem()
    :   current(NULL),
        source(NULL),
        length(0),
        closed(false),
        owner(true)
{
    LTRACE << "MemoryFileSystem()";
}

MemoryFileSystem::m_Error
MemoryFileSystem::open(const char* memorySource, r_Bytes mSize)
{
    LTRACE << "open(source, " << mSize << ")";
    owner = false;
    length = mSize;
    source = (char*)memorySource;
    current = source;
    return No_Error;
}

MemoryFileSystem::m_Error
MemoryFileSystem::open(const char* fileName)
{
    owner = true;
    if (fileName == memorySrcName)
    {
        MemoryFileSystem::m_Error err = open(memorySrc, memorySrcLength);
        return err;
    }
    else
    {
        std::ifstream f;
        f.open(fileName);
        if (!f.is_open())
        {
            LFATAL << "MemoryFileSystem::open(" << fileName << ") could not open file";
            return Error;
        }
        f.seekg(0, std::ios::end);
        std::ios::pos_type end = f.tellg();
        LTRACE << "size ";
        length = end;
        source = new char[end];
        current = source;
        memset(source, 0, end);
        f.seekg(0, std::ios::beg);
        f.read(source, end);
        f.close();
        return No_Error;
    }
}

MemoryFileSystem::m_Error
MemoryFileSystem::close()
{
    if (!closed)
    {
        if (owner)
        {
            delete [] source;
        }
        source = NULL;
        current = NULL;
        length = 0;
        return No_Error;
    }
    else
    {
        return Error;
    }
}

MemoryFileSystem::~MemoryFileSystem()
{
    LTRACE << "~MemoryFileSystem()";
    close();
}

MemoryFileSystem::m_Error
MemoryFileSystem::read(void* buffer, r_Bytes bSize)
{
    bSize = std::min(bSize, length - (current - source));
    LTRACE << "reading " << bSize;
    memcpy(buffer, current, bSize);
    current += bSize;
    return No_Error;
}

unsigned long long
MemoryFileSystem::tell()
{
    LTRACE << "tell() " << (current - source);
    return current - source;
}

MemoryFileSystem::m_Error
MemoryFileSystem::seek(unsigned long long offset)
{
    if (offset > length)
    {
        return Error;
    }
    else
    {
        current = offset + source;
        return No_Error;
    }
}

