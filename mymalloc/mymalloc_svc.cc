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

//
//  wrapper for malloc on server side - throw bad_alloc if malloc fails
//
#include "config.h"
#include "mymalloc/mymalloc.h"
#include "reladminif/objectbroker.hh"
#include <stdlib.h>
#include <logging.hh>

using namespace std;

// try to free memory from the ObjectBroker at most MAX_FREE_ATTEMPTS times;
// this is used to guard against any fault in ObjectBroker::freeMemory()
// that could lead to an infinite loop
#define MAX_RETRIES 10000

// try to allocate requested memory;
// if impossible, try to free some, then retry allocation (by recursion)
// if nothing can be freed & allocated, give up & throw exception
void *mymalloc(size_t size)
{
    void *ret = aligned_alloc(RAS_MEMORY_ALIGNMENT, size);
    if (ret != (void *)NULL)
    {
        // success, return
        return ret;
    }
    else
    {
        // failed, retry
        auto retriesSoFar = MAX_RETRIES;   // while all of these are true:
        while (ret == (void *)NULL         //  - p is null == malloc failed
                && retriesSoFar != 0           //  - retried less than MAX_RETRIES times so far
                && ObjectBroker::freeMemory()) //  - it's possible to free some cached memory
        {
            ret = malloc(size);
            --retriesSoFar;
        }
    }

    if (ret != (void *)NULL)
    {
        // success, return
        return ret;
    }
    else
    {
        // failed, give up
        const auto sizeGB = size / 1000000000.0;
        LERROR << "Failed allocating " << size << " bytes (" << sizeGB << " GB) of memory.";
        throw std::bad_alloc();
    }
}
