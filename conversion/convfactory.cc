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
/**
 * SOURCE: convertor.cc
 *
 * MODULE:  conversion
 *
 * CLASSES: r_Convertor_Factory
 *  Create convertors out of data formats
 *
 * COMMENTS:
 * - temporary, for debugging
 */

#include "config.h"
#include <logging.hh>

#include "conversion/convfactory.hh"

// all the conversion types, for easy creation
#include "tiff.hh"
#include "hdf.hh"
#include "dem.hh"
#include "csv.hh"
#include "json.hh"
#include "netcdf.hh"
#include "grib.hh"
#include "gdal.hh"


bool r_Convertor_Factory::is_supported(r_Data_Format fmt)
{
    bool retval = false;
    switch (fmt)
    {
    case r_TIFF:
    case r_DEM:
    case r_HDF:
    case r_NETCDF:
    case r_GRIB:
    case r_GDAL:
    case r_CSV:
    case r_JSON:
        retval = true;
        break;
    default:
        retval = false;
        break;
    }

    return retval;
}

r_Convertor *r_Convertor_Factory::create(r_Data_Format fmt, const char *src, const r_Minterval &interv, const r_Type *tp)
{
    r_Convertor *result = NULL;

    switch (fmt)
    {
    case r_TIFF:
        result = new r_Conv_TIFF(src, interv, tp);
        break;
    case r_NETCDF:
        result = new r_Conv_NETCDF(src, interv, tp);
        break;
    case r_GRIB:
        result = new r_Conv_GRIB(src, interv, tp);
        break;
    case r_GDAL:
        result = new r_Conv_GDAL(src, interv, tp);
        break;
    case r_CSV:
        result = new r_Conv_CSV(src, interv, tp);
        break;
    case r_JSON:
        result = new r_Conv_JSON(src, interv, tp);
        break;
    case r_DEM:
        result = new r_Conv_DEM(src, interv, tp);
        break;
    case r_HDF:
        result = new r_Conv_HDF(src, interv, tp);
        break;
    default:
        LERROR << "Error in conversion factory during create: unsupported format: " << fmt;
        r_Error err(CONVERSIONFORMATNOTSUPPORTED);
        throw (err);
    }

    return result;
}


r_Convertor *r_Convertor_Factory::create(r_Data_Format fmt, const char *src, const r_Minterval &interv, int type)
{
    r_Convertor *result = NULL;

    switch (fmt)
    {
    case r_TIFF:
        result = new r_Conv_TIFF(src, interv, type);
        break;
    case r_NETCDF:
        result = new r_Conv_NETCDF(src, interv, type);
        break;
    case r_GRIB:
        result = new r_Conv_GRIB(src, interv, type);
        break;
    case r_GDAL:
        result = new r_Conv_GDAL(src, interv, type);
        break;
    case r_CSV:
        result = new r_Conv_CSV(src, interv, type);
        break;
    case r_JSON:
        result = new r_Conv_JSON(src, interv, type);
        break;
    case r_DEM:
        result = new r_Conv_DEM(src, interv, type);
        break;
    case r_HDF:
        result = new r_Conv_HDF(src, interv, type);
        break;
    default:
        LERROR << "Error in conversion factory during create: unsupported format: " << fmt;
        r_Error err(CONVERSIONFORMATNOTSUPPORTED);
        throw (err);
    }

    return result;
}

