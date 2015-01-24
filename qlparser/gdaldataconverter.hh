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
 * MERCHANTrABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
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

#ifndef GDALDATACONVERTER_HH
#define	GDALDATACONVERTER_HH

#include "raslib/error.hh"
#include "raslib/primitivetype.hh"
#include "raslib/structuretype.hh"

// GDAL headers
#include "ogr_spatialref.h"
#include "cpl_conv.h"
#include "cpl_string.h"
#include "vrtdataset.h"

/**
 * Helper class which converts data from GDAL to rasdaman format.
 */
class GDALDataConverter {
public:
	GDALDataConverter();

	/**
	 * Transforms the file read with GDAL to a rasdaman Tile.
	 *
	 * WARNING: GDALDataConverter::getTileCells() closes the GDAL dataset.
	 *
	 * @param poDataSet The dataset read from the temporary file with GDAL.
     * @param size Out parameter representing the size of the read data.
     * @param contents Out parameter representing the read file contets casted to char*.
	 */
    static void getTileCells(GDALDataset* poDataSet, /* out */ r_Bytes& size, /* out */ char*& contents);

	virtual ~GDALDataConverter();
private:

	/**
	 * Converts the GDAL dataset to rasdaman dataset, using type T for the rasdaman tile.
     *
     * @param poDataSet The dataset read from the temporary file with GDAL.
     * @param size Out parameter representing the size of the read data.
     * @param contents Out parameter representing the read file contets casted to char*.
     */
	template<typename T>
    static void resolveTileCellsByType(GDALDataset* poDataset, /* out */ r_Bytes& size, /* out */ char*& contents);
};

#endif	/* GDALDATACONVERTER_HH */
