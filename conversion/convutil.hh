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

#ifndef CONVUTIL_HH
#define CONVUTIL_HH

#include "config.h"
#include "raslib/type.hh"
#include <raslib/mddtypes.hh>  // for r_Data_Format
#include "conversion/gdalincludes.hh"
#include "conversion/convtypes.hh"

#ifdef HAVE_GDAL
#include <cpl_error.h>
void customGdalErrorHandler(CPLErr errCat, int errNum, const char *errMsg);

class GDALDataset;
class GDALRasterBand;
#endif

/**
 * Helper class transforming the provided types into rasdaman types.
 */
class ConvUtil
{
public:
    ConvUtil() = delete;

#ifdef HAVE_GDAL

    /**
     * Extracts the rasdaman supported type from a gdal band.
     * @param gdalBand The band to extract the type from.
     * @return A literal type supported by rasdaman.
     */
    static std::string gdalTypeToRasTypeString(GDALRasterBand *gdalBand);

    /**
     * The type decision is made based on the GDALDataType of the bands and of the number of bands.
     *
     * @param poDataSet GDALDataSet read from the temporary file.
     * @param bandIds a vector of the band ids to be considered for the type translation (0-indexed)
     * @return an r_Type for the dataset
     */
    static r_Type *gdalTypeToRasType(GDALDataset *poDataSet, const std::vector<int> &bandIds);

    /// convert rasdaman type to GDAL type
    static GDALDataType rasTypeToGdalType(r_Type *rasType);

#endif  // HAVE_GDAL

#ifdef HAVE_HDF
    /// translate an internal type into an HDF type and return the size.
    static int ctypeToHdfType(int intType, int &size);

    /// translate an HDF type into an internal type and return the size
    static int hdfTypeToCtype(int hdfType, int &size);
#endif

    /**
     * Convert format string to r_Data_Format
     */
    static r_Data_Format getDataFormat(std::string format);

    /**
     * Get the base type size of the bandId-th band in type.
     * @param type rasdaman type, could be primitive or struct
     * @param bandId the band index, 0-based
     * @return the base type size
     */
    static size_t getBandBaseTypeSize(const r_Type *type, int bandId);

    /**
     * @return the number of bands in type, 1 if type is primitive, more than 1 if struct.
     */
    static unsigned int getNumberOfBands(const r_Type *type);

#ifdef HAVE_GDAL
private:
    static const std::string GDAL_KEY_IMAGE_STRUCTURE;
    static const std::string GDAL_KEY_PIXELTYPE;
    static const std::string GDAL_VAL_SIGNEDBYTE;
#endif
};

#endif /* CONVUTIL_HH */
