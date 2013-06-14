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
#ifndef _TILECACHETEST_HH_
#define _TILECACHETEST_HH_

#include <vector>
#include "reladminif/oidif.hh"

/**
 * Test tile caching.
 * 
 * @author Dimitar Misev
 */
class TileCacheTest {
public:
    /// constructor
    TileCacheTest();

    /// insert a number of blobs
    void insertBlobs();
    
    /// insert more blobs (over cache limit) so that cache readjustments kicks in
    void insertBlobsReadjust();
    
    /// remove all blobs from the cache
    void removeBlobs();
    
    /// permanently delete all blobs created in this test
    void deleteBlobs();
    
    /// get the blobs created in this test
    void getBlobs();
    
    /// test cache clearing
    void clearCache();

private:
    
    /// a list of the blob oids that are created during the test
    std::vector<OId> oids;

    /// helper for creating a new blob
    BLOBTile* createBlob();
};

#endif
