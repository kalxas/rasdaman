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

#include "config.h"
#include "conversion/convutil.hh"
#include "conversion/hdfincludes.hh"
#include "raslib/attribute.hh"
#include "raslib/structuretype.hh"
#include "raslib/error.hh"
#include <logging.hh>

#include <sstream>
#include <string>
#include <boost/algorithm/string/case_conv.hpp>  // for to_lower

using namespace std;
using namespace common;


#ifdef HAVE_GDAL

const string ConvUtil::GDAL_KEY_IMAGE_STRUCTURE{"IMAGE_STRUCTURE"};
const string ConvUtil::GDAL_KEY_PIXELTYPE{"PIXELTYPE"};
const string ConvUtil::GDAL_VAL_SIGNEDBYTE{"SIGNEDBYTE"};

string ConvUtil::gdalTypeToRasTypeString(GDALRasterBand* gdalBand)
{
    auto dataType = gdalBand->GetRasterDataType();
    switch (dataType)
    {
    case GDT_Byte:
    {
      // gdal uses type Byte for both signed and unsigned 8 bit integers
      // check in the metadata if the type is signed
      const auto *pixelType =
          gdalBand->GetMetadataItem(GDAL_KEY_PIXELTYPE.c_str(),
                                    GDAL_KEY_IMAGE_STRUCTURE.c_str());
      if (pixelType) {
        if (string{pixelType} == GDAL_VAL_SIGNEDBYTE) {
          // signed, according to the metadata
          return "octet";
        }
      }
      // otherwise, it's unsigned
      return "char";
    }
    case GDT_UInt16:   return "ushort";
    case GDT_Int16:    return "short";
    case GDT_UInt32:   return "ulong";
    case GDT_Int32:    return "long";
    case GDT_Float32:  return "float";
    case GDT_Float64:  return "double";
    case GDT_CFloat32: return "complex";
    case GDT_CFloat64: return "complexd";
    case GDT_CInt16:   return "cint16";
    case GDT_CInt32:   return "cint32";
    default:
        LERROR << "Unable to convert GDAL type " << dataType << " to rasdaman type.";
        throw r_Error(r_Error::r_Error_Conversion);
    }
}

r_Type* ConvUtil::gdalTypeToRasType(GDALDataset* poDataset, const vector<int>& bandIds)
{
    size_t nBands = bandIds.size();
    r_Type* baseType = NULL;
    if (nBands == 1 || nBands == 0) // primitive type
    {
        if (poDataset->GetRasterCount())
        {
            string rasType = gdalTypeToRasTypeString(poDataset->GetRasterBand(1));
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
            string rasType = gdalTypeToRasTypeString(poDataset->GetRasterBand(bandId + 1));
            destType << rasType;
        }
        destType << " }";
        string destTypeStr = destType.str();
        baseType = r_Type::get_any_type(destTypeStr.c_str());
    }

    if (baseType == NULL)
    {
        LERROR << "failed converting GDAL type to rasdaman type.";
        throw r_Error(r_Error::r_Error_FeatureNotSupported);
    }

    return baseType;
}

GDALDataType ConvUtil::rasTypeToGdalType(r_Type* rasType)
{
    switch (rasType->type_id())
    {
    case r_Type::BOOL:
    case r_Type::OCTET:
    case r_Type::CHAR:   return GDT_Byte;
    case r_Type::USHORT: return GDT_UInt16;
    case r_Type::SHORT:  return GDT_Int16;
    case r_Type::ULONG:  return GDT_UInt32;
    case r_Type::LONG:   return GDT_Int32;
    case r_Type::FLOAT:  return GDT_Float32;
    case r_Type::DOUBLE: return GDT_Float64;
    case r_Type::COMPLEXTYPE1: return GDT_CFloat32;
    case r_Type::COMPLEXTYPE2: return GDT_CFloat64;
    case r_Type::CINT16: return GDT_CInt16;
    case r_Type::CINT32: return GDT_CInt32;
    default:
        LERROR << "Unable to convert rasdaman type " << rasType->name() << " to GDAL type.";
        throw r_Error(r_Error::r_Error_Conversion);
    }
}
#endif // HAVE_GDAL

r_Data_Format
ConvUtil::getDataFormat(string formatName)
{
    boost::algorithm::to_lower(formatName);
    if (formatName == "png")
        return r_PNG;
    else if (formatName == "netcdf")
        return r_NETCDF;
    else if (formatName == "gtiff" || formatName == "tiff")
        return r_TIFF;
    else if (formatName == "jpeg")
        return r_JPEG;
    else if (formatName == "jpeg2000" || formatName == "jp2openjpeg")
        return r_JP2;
    else if (formatName == "nitf")
        return r_NITF;
    else if (formatName == "ecw")
        return r_ECW;
    else if (formatName == "hdf" || formatName == "hdf4" || formatName == "hdf4image" || formatName == "hdf5")
        return r_HDF;
    else if (formatName == "bmp")
        return r_BMP;
    else if (formatName == "csv")
        return r_CSV;
    else if (formatName == "json")
        return r_JSON;
    else if (formatName == "grib")
        return r_GRIB;
    else if (formatName == "ppm")
        return r_PPM;
    else if (formatName == "dem")
        return r_DEM;
    else
        return r_Array;
}

size_t ConvUtil::getBandBaseTypeSize(const r_Type* type, int bandId)
{
    if (type->isStructType())
    {
        r_Attribute att = static_cast<const r_Structure_Type*>(type)->resolve_attribute((unsigned int) bandId);
        return att.type_of().size();
    }
    else
    {
        return static_cast<const r_Base_Type*>(type)->size();
    }
}

unsigned int ConvUtil::getNumberOfBands(const r_Type* type)
{
    return type->isStructType()
        ? static_cast<const r_Structure_Type*>(type)->count_elements() : 1;
}


#ifdef HAVE_HDF

int ConvUtil::ctypeToHdfType(int ctype, int& size)
{
    int result = 0;

    switch (ctype)
    {
    case ctype_int8:   result = DFNT_CHAR8; size = 1; break;
    case ctype_uint8:
    case ctype_char:
    case ctype_bool:   result = DFNT_UCHAR8; size = 1; break;
    case ctype_int16:  result = DFNT_INT16; size = 2; break;
    case ctype_uint16: result = DFNT_UINT16; size = 2; break;
    case ctype_int32:  result = DFNT_INT32; size = 4; break;
    case ctype_uint32: result = DFNT_UINT32; size = 4; break;
    case ctype_int64:  result = DFNT_INT64; size = 8; break;
    case ctype_uint64: result = DFNT_UINT64; size = 8; break;
    case ctype_float32:result = DFNT_FLOAT32; size = 4; break;
    case ctype_float64:result = DFNT_FLOAT64; size = 8; break;
    default:           result = 0; size = 1; break;
    }
    return result;
}

int ConvUtil::hdfTypeToCtype(int hdfType, int& size)
{
    int result = 0;

    switch (hdfType)
    {
    case DFNT_CHAR8:  result = ctype_int8; size = 1; break;
    case DFNT_UCHAR8: result = ctype_uint8; size = 1; break;
    case DFNT_INT16:  result = ctype_int16; size = 2; break;
    case DFNT_UINT16: result = ctype_uint16; size = 2; break;
    case DFNT_INT32:  result = ctype_int32; size = 4; break;
    case DFNT_UINT32: result = ctype_uint32; size = 4; break;
    case DFNT_INT64:  result = ctype_int64; size = 8; break;
    case DFNT_UINT64: result = ctype_uint64; size = 8; break;
    case DFNT_FLOAT32:result = ctype_float32; size = 4; break;
    case DFNT_FLOAT64:result = ctype_float64; size = 8; break;
    default:          result = ctype_void; size = 1; break;
    }
    return result;
}
#endif

