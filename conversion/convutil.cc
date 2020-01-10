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

#include "conversion/convutil.hh"
#include "conversion/hdfincludes.hh"
#include "raslib/attribute.hh"
#include "raslib/structuretype.hh"
#include "raslib/error.hh"
#include "config.h"

#include <logging.hh>
#include <sstream>
#include <string>
#include <boost/algorithm/string.hpp>

using namespace std;

ConvUtil::ConvUtil()
{
}

#ifdef HAVE_GDAL

string ConvUtil::gdalTypeToRasTypeString(GDALDataType dataType)
{
    string ret;
    switch (dataType)
    {
    case GDT_Byte:
        //r_type::CHAR || r_type::BOOL
        ret = string("char");
        break;

    case GDT_UInt16:
        //r_type::USHORT
        ret = string("ushort");
        break;
    case GDT_Int16:
        //r_type::SHORT
        ret = string("short");
        break;
    case GDT_UInt32:
        //r_type::ULONG
        ret = string("ulong");
        break;
    case GDT_Int32:
        //r_type::LONG
        ret = string("long");
        break;
    case GDT_Float32:
        //r_type::FLOAT
        ret = string("float");
        break;
    case GDT_Float64:
        //r_type::DOUBLE
        ret = string("double");
        break;
    case GDT_CFloat32:
        ret = string("complex");
        break;
    case GDT_CFloat64:
        ret = string("complexd");
        break;
    case GDT_CInt16:
        ret = string("cint16");
        break;
    case GDT_CInt32:
        ret = string("cint32");
        break;
    default:
        throw r_Error(r_Error::r_Error_FeatureNotSupported);
        break;
    }
    return ret;
}

r_Type* ConvUtil::gdalTypeToRasType(GDALDataset* poDataset, const vector<int>& bandIds)
{
    size_t nBands = bandIds.size();
    r_Type* baseType = NULL;
    if (nBands == 1 || nBands == 0) // primitive type
    {
        if (poDataset->GetRasterCount())
        {
            GDALDataType gdalType = poDataset->GetRasterBand(1)->GetRasterDataType();
            string rasType = gdalTypeToRasTypeString(gdalType);
            baseType = r_Type::get_any_type(rasType.c_str());
        }
        else
        {
            LERROR << "empty GDAL dataset.";
            throw r_Error(r_Error::r_Error_Conversion);
        }
    }
    else if (nBands > 1) // struct type
    {
        stringstream destType(stringstream::out);
        destType << "struct { ";
        for (size_t i = 0; i < nBands; ++i)
        {
            int bandId = bandIds[i];
            if (bandId < 0 || bandId >= poDataset->GetRasterCount())
            {
                LERROR << "band id '" << bandId << "' out of range 0 - " << (poDataset->GetRasterCount() - 1) << ".";
                throw r_Error(INVALIDFORMATPARAMETER);
            }
            if (i > 0)
            {
                destType << ", ";
            }
            GDALDataType gdalType = poDataset->GetRasterBand(bandId + 1)->GetRasterDataType();
            string rasType = gdalTypeToRasTypeString(gdalType);
            destType << rasType;
        }
        destType << " }";
        string destTypeStr = destType.str();
        baseType = r_Type::get_any_type(destTypeStr.c_str());
    }

    if (baseType == NULL)
    {
        LERROR << "failed converting GDAL type to rasdaman type.";
        throw r_Error::r_Error_FeatureNotSupported;
    }

    return baseType;
}

GDALDataType ConvUtil::rasTypeToGdalType(r_Type* rasType)
{
    GDALDataType ret = GDT_Unknown;
    switch (rasType->type_id())
    {
    case r_Type::BOOL:
        ret = GDT_Byte;
        break;
    case r_Type::CHAR:
        ret = GDT_Byte;
        break;
    case r_Type::USHORT:
        ret = GDT_UInt16;
        break;
    case r_Type::SHORT:
        ret = GDT_Int16;
        break;
    case r_Type::ULONG:
        ret = GDT_UInt32;
        break;
    case r_Type::LONG:
        ret = GDT_Int32;
        break;
    case r_Type::FLOAT:
        ret = GDT_Float32;
        break;
    case r_Type::DOUBLE:
        ret = GDT_Float64;
        break;
    case r_Type::COMPLEXTYPE1:
        ret = GDT_CFloat32;
        break;
    case r_Type::COMPLEXTYPE2:
        ret = GDT_CFloat64;
        break;
    case r_Type::CINT16:
        ret = GDT_CInt16;
        break;
    case r_Type::CINT32:
        ret = GDT_CInt32;
        break;
    default:
        LERROR << "Unable to convert rasdaman type " <<
               rasType->name() << " to GDAL type.";
        throw r_Error(r_Error::r_Error_General);
    }
    return ret;
}
#endif // HAVE_GDAL

r_Data_Format
ConvUtil::getDataFormat(string formatName)
{
    r_Data_Format ret = r_Array;
    boost::algorithm::to_lower(formatName);
    if (formatName == "png")
    {
        ret = r_PNG;
    }
    else if (formatName == "netcdf")
    {
        ret = r_NETCDF;
    }
    else if (formatName == "gtiff" || formatName == "tiff")
    {
        ret = r_TIFF;
    }
    else if (formatName == "jpeg")
    {
        ret = r_JPEG;
    }
    else if (formatName == "jpeg2000" || formatName == "jp2openjpeg")
    {
        ret = r_JP2;
    }
    else if (formatName == "nitf")
    {
        ret = r_NITF;
    }
    else if (formatName == "ecw")
    {
        ret = r_ECW;
    }
    else if (formatName == "hdf" || formatName == "hdf4" || formatName == "hdf4image" || formatName == "hdf5")
    {
        ret = r_HDF;
    }
    else if (formatName == "bmp")
    {
        ret = r_BMP;
    }
    else if (formatName == "csv")
    {
        ret = r_CSV;
    }
    else if (formatName == "json")
    {
        ret = r_JSON;
    }
    else if (formatName == "grib")
    {
        ret = r_GRIB;
    }
    else if (formatName == "ppm")
    {
        ret = r_PPM;
    }
    else if (formatName == "dem")
    {
        ret = r_DEM;
    }
    return ret;
}

size_t ConvUtil::getBandBaseTypeSize(r_Type* type, int bandId)
{
    size_t ret = 0;
    if (type->isStructType())
    {
        r_Attribute att = ((r_Structure_Type*) type)->resolve_attribute((unsigned int) bandId);
        ret = att.type_of().size();
    }
    else
    {
        ret = ((r_Base_Type*) type)->size();
    }
    return ret;
}

unsigned int ConvUtil::getNumberOfBands(const r_Type* type)
{
    unsigned int ret = 1;
    if (type->isStructType())
    {
        ret = static_cast<const r_Structure_Type*>(type)->count_elements();
    }
    return ret;
}


#ifdef HAVE_HDF

int ConvUtil::ctypeToHdfType(int ctype, int& size)
{
    int result = 0;

    switch (ctype)
    {
    case ctype_int8:
        result = DFNT_CHAR8;
        size = 1;
        break;
    case ctype_uint8:
    case ctype_char:
    case ctype_bool:
        result = DFNT_UCHAR8;
        size = 1;
        break;
    case ctype_int16:
        result = DFNT_INT16;
        size = 2;
        break;
    case ctype_uint16:
        result = DFNT_UINT16;
        size = 2;
        break;
    case ctype_int32:
        result = DFNT_INT32;
        size = 4;
        break;
    case ctype_uint32:
        result = DFNT_UINT32;
        size = 4;
        break;
    case ctype_int64:
        result = DFNT_INT64;
        size = 8;
        break;
    case ctype_uint64:
        result = DFNT_UINT64;
        size = 8;
        break;
    case ctype_float32:
        result = DFNT_FLOAT32;
        size = 4;
        break;
    case ctype_float64:
        result = DFNT_FLOAT64;
        size = 8;
        break;
    default:
        result = 0;
        size = 1;
        break;
    }
    return result;
}

int ConvUtil::hdfTypeToCtype(int hdfType, int& size)
{
    int result = 0;

    switch (hdfType)
    {
    case DFNT_CHAR8:
        result = ctype_int8;
        size = 1;
        break;
    case DFNT_UCHAR8:
        result = ctype_uint8;
        size = 1;
        break;
    case DFNT_INT16:
        result = ctype_int16;
        size = 2;
        break;
    case DFNT_UINT16:
        result = ctype_uint16;
        size = 2;
        break;
    case DFNT_INT32:
        result = ctype_int32;
        size = 4;
        break;
    case DFNT_UINT32:
        result = ctype_uint32;
        size = 4;
        break;
    case DFNT_INT64:
        result = ctype_int64;
        size = 8;
        break;
    case DFNT_UINT64:
        result = ctype_uint64;
        size = 8;
        break;
    case DFNT_FLOAT32:
        result = ctype_float32;
        size = 4;
        break;
    case DFNT_FLOAT64:
        result = ctype_float64;
        size = 8;
        break;
    default:
        result = ctype_void;
        size = 1;
        break;
    }
    return result;
}
#endif

ConvUtil::~ConvUtil()
{
}
