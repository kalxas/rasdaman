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

#include <string>

static const int INVALID_FILE_DESCRIPTOR = -1;
static const int INVALID_BLOB_ID = -1;

/// common error return code of C I/O functions
static const int IO_ERROR_RC = -1;
/// common success return code of C I/O functions
static const int IO_SUCCESS_RC = 0;

class BlobFSConfig
{
public:
    /// Path where tiles are stored = $RASDATA
    std::string rootPath;
    
    /// Path where tiles are stored = $RASDATA/TILES
    std::string tilesPath;
    
    /// Path where transactions are stored = $RASDATA/TRANSACTIONS
    std::string transactionsPath;

    inline BlobFSConfig(const std::string &rootPathArg,
                        const std::string &tilesPathArg,
                        const std::string &transactionsPathArg)
        : rootPath(rootPathArg), tilesPath(tilesPathArg),
          transactionsPath(transactionsPathArg) {}
};

