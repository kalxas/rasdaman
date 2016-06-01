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
#include "raslib/attribute.hh"
#include "raslib/structuretype.hh"
#include "catalogmgr/typefactory.hh"
#include "relcatalogif/structtype.hh"
#include "relcatalogif/typeiterator.hh"

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
        ret = string("complex1");
        break;
    case GDT_CFloat64:
        ret = string("complex2");
        break;
    default:
        throw r_Error(r_Error::r_Error_FeatureNotSupported);
        break;
    }
    return ret;
}
#endif // HAVE_GDAL

#ifdef HAVE_GDAL
//BaseType* TypeResolverUtil::gdalToBaseType(GDALDataset* poDataSet)
//{
//	int nBands = poDataSet->GetRasterCount();
//	BaseType* baseType = NULL;
//	if (nBands == 1) // primitive type
//	{
//		GDALDataType dataType = poDataSet->GetRasterBand(1)->GetRasterDataType();
//		baseType = const_cast<BaseType*>(TypeFactory::mapType(getLiteralTypeFromGDAL(dataType)));
//	}
//    else if (nBands > 1) // struct type
//	{
//		StructType* tmpStructType = new StructType("tmp_str_name", static_cast<unsigned int>(nBands));
//
//		for (int band = 1; band <= nBands; ++band)
//		{
//			GDALDataType dataType = poDataSet->GetRasterBand(band)->GetRasterDataType();
//			const char* literalType = getLiteralTypeFromGDAL(dataType);
//			char elementName[20];
//			memset(elementName, '\0', 20);
//			sprintf(elementName, "band%d", band);
//			tmpStructType->addElement(elementName, literalType);
//		}
//		TypeFactory::addTempType(tmpStructType);
//		baseType = tmpStructType;
//	}
//
//	if (baseType == NULL)
//	{
//		throw r_Error::r_Error_FeatureNotSupported;
//	}
//
//	return baseType;
//}
#endif // HAVE_GDAL

#ifdef HAVE_GDAL

r_Type* ConvUtil::gdalTypeToRasType(GDALDataset* poDataSet)
{
    int nBands = poDataSet->GetRasterCount();
    r_Type* baseType = NULL;
    if (nBands == 1) // primitive type
    {
        GDALDataType gdalType = poDataSet->GetRasterBand(1)->GetRasterDataType();
        string rasType = gdalTypeToRasTypeString(gdalType);
        baseType = r_Type::get_any_type(rasType.c_str());
    }
    else if (nBands > 1) // struct type
    {
        stringstream destType(stringstream::out);
        destType << "struct { ";
        for (int band = 1; band <= nBands; ++band)
        {
            if (band > 1)
                destType << ", ";
            GDALDataType gdalType = poDataSet->GetRasterBand(band)->GetRasterDataType();
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
#endif // HAVE_GDAL

#ifdef HAVE_GDAL

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
    default:
        LFATAL << "Unable to convert rasdaman type " <<
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
        ret = r_PNG;
    else if (formatName == "netcdf")
        ret = r_NETCDF;
    else if (formatName == "gtiff" || formatName == "tiff")
        ret = r_TIFF;
    else if (formatName == "jpeg")
        ret = r_JPEG;
    else if (formatName == "jpeg2000" || formatName == "jp2openjpeg")
        ret = r_JP2;
    else if (formatName == "nitf")
        ret = r_NITF;
    else if (formatName == "hdf" || formatName == "hdf4" || formatName == "hdf4image" || formatName == "hdf5")
        ret = r_HDF;
    else if (formatName == "bmp")
        ret = r_BMP;
    else if (formatName == "csv")
        ret = r_CSV;
    else if (formatName == "grib")
        ret = r_GRIB;
    return ret;
}

const BaseType* ConvUtil::rasTypeToBaseType(r_Type* type)
{
    const BaseType *result = NULL;
    if (type->isPrimitiveType())
    {
        result = TypeFactory::mapType(type->name());
        if (!result)
        {
            LFATAL << "no base type for ODMG primitive type '"
                << type->name() << "' was found";
            throw r_Error(BASETYPENOTSUPPORTED);
        }
    }
    else if (type->isStructType())
    {
        r_Structure_Type *structType = static_cast<r_Structure_Type *> (const_cast<r_Type*> (type));
        StructType *restype = new StructType("tmp_struct_type", structType->count_elements());
        r_Structure_Type::attribute_iterator iter(structType->defines_attribute_begin());
        while (iter != structType->defines_attribute_end())
        {
            try
            {
                r_Attribute attr = (*iter);
                const r_Base_Type &attr_type = attr.type_of();
                restype->addElement(attr.name(), rasTypeToBaseType((r_Type*) & attr_type));
            }
            catch (r_Error &e)
            {
                LERROR << "failed converting band type: " << e.what();
                delete restype;
                throw;
            }
            ++iter;
        }
        TypeFactory::addTempType(restype);
        result = restype;
    }
    return result;
}

    
#ifdef HAVE_HDF
int ConvUtil::ctypeToHdfType(int ctype, int &size)
{
    int result=0;

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

int ConvUtil::hdfTypeToCtype( int hdfType, int &size )
{
    int result=0;

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
