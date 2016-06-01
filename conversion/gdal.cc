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

#include "conversion/gdal.hh"
#include "conversion/memfs.hh"
#include "conversion/tmpfile.hh"
#include "conversion/convutil.hh"
#include "conversion/mimetypes.hh"
#include "raslib/error.hh"
#include "raslib/parseparams.hh"
#include "raslib/primitivetype.hh"
#include "raslib/attribute.hh"
#include "raslib/structuretype.hh"
#include "raslib/odmgtypes.hh"
#include "mymalloc/mymalloc.h"

#include <easylogging++.h>

#include <string.h>
#include <errno.h>
#include <float.h>

using namespace std;

#ifndef DATA_CHUNK_SIZE
#define DATA_CHUNK_SIZE 10000 //no of bytes to be written in a file
#endif

#ifndef PARAM_SEPARATOR
#define PARAM_SEPARATOR ";"
#endif

#ifndef GDAL_PARAMS
#define GPDAL_PARAMS true

#define PARAM_XMIN "xmin"
#define PARAM_XMAX "xmax"
#define PARAM_YMIN "ymin"
#define PARAM_YMAX "ymax"

#define PARAM_CRS  "crs"
#define PARAM_METADATA "metadata"
#define PARAM_NODATA "nodata"
#define PARAM_CONFIG "config"

#define NODATA_VALUE_SEPARATOR " ,"
#define NODATA_DEFAULT_VALUE 0.0
#endif

/// constructor using an r_Type object. Exception if the type isn't atomic.

r_Conv_GDAL::r_Conv_GDAL(const char *src, const r_Minterval &interv, const r_Type *tp) throw(r_Error)
: r_Convert_Memory(src, interv, tp, true), fParams(NULL)
{
}

/// constructor using convert_type_e shortcut

r_Conv_GDAL::r_Conv_GDAL(const char *src, const r_Minterval &interv, int tp) throw(r_Error)
: r_Convert_Memory(src, interv, tp), fParams(NULL)
{
}


/// destructor

r_Conv_GDAL::~r_Conv_GDAL(void)
{
#ifdef HAVE_GDAL
    if (fParams)
    {
        CSLDestroy(fParams);
        fParams = NULL;
    }
#endif // HAVE_GDAL
}

#ifdef HAVE_GDAL

r_Conv_Desc &r_Conv_GDAL::convertTo(const char *options) throw(r_Error)
{
    if (format.empty())
    {
        LFATAL << "No format specified to encode()";
        throw r_Error(r_Error::r_Error_Conversion);
    }
    if (r_MimeTypes::isMimeType(format))
    {
        format = r_MimeTypes::getFormatName(format);
    }
    
    GDALAllRegister();
    GDALDriver *driver = GetGDALDriverManager()->GetDriverByName(format.c_str());
    if (driver == NULL)
    {
        LFATAL << "Unsupported format: " << format;
        throw r_Error(r_Error::r_Error_FeatureNotSupported);
    }
    
    initEncodeParams(options);
    
    const r_Type* baseType = desc.srcType;
    r_Primitive_Type* bandType = NULL;
    
    // determine bands of MDD
    int numBands = 0;
    if (baseType->isPrimitiveType()) // = one band
    {
        numBands = 1;
        bandType = (r_Primitive_Type*) baseType;
    }
    else if (baseType->isStructType()) // = multiple bands
    {
        r_Structure_Type *structType = (r_Structure_Type*) baseType;
        r_Structure_Type::attribute_iterator iter(structType->defines_attribute_begin());
        while (iter != structType->defines_attribute_end())
        {
            ++numBands;

            // check the band types, they have to be of the same type
            if ((*iter).type_of().isPrimitiveType())
            {
                r_Primitive_Type pt = static_cast<r_Primitive_Type&> (const_cast<r_Base_Type&> ((*iter).type_of()));
                if (bandType != NULL)
                {
                    if (bandType->type_id() != pt.type_id())
                    {
                        LFATAL << "Can not handle bands of different types.";
                        throw r_Error(r_Error::r_Error_Conversion);
                    }
                }
                else
                {
                    bandType = static_cast<r_Primitive_Type*>(pt.clone());
                }
            }
            else
            {
                LFATAL << "Can not handle composite bands.";
                throw r_Error(r_Error::r_Error_Conversion);
            }
            ++iter;
        }
    }
    r_Bytes typeSize = bandType->size();
    bool isBoolean = bandType->type_id() == r_Type::BOOL;
    GDALDataType gdalBandType = ConvUtil::rasTypeToGdalType(bandType);
    
    r_Minterval domain = desc.srcInterv;
    if (domain.dimension() != 2)
    {
        LERROR << "only 2D data can be encoded with GDAL.";
        throw r_Error(r_Error::r_Error_Conversion);
    }
    int width = domain[0].high() - domain[0].low() + 1;
    int height = domain[1].high() - domain[1].low() + 1;
    
    LTRACE << "Converting array of width x height x cell size: " << 
        width << " x " << height << " x " << typeSize;
    
    GDALDriver *hMemDriver = static_cast<GDALDriver*> (GDALGetDriverByName("MEM"));
    if (hMemDriver == NULL)
    {
        LERROR << "Could not init GDAL driver: " << CPLGetLastErrorMsg();
        throw r_Error(r_Error::r_Error_Conversion);
    }
    GDALDataset *hMemDS = hMemDriver->Create("in_memory_image", 
                                             width, height, numBands, gdalBandType, NULL);
    
    char* tileCells = (char*) desc.src;
    char* datasetCells = static_cast<char*> (malloc(typeSize * static_cast<size_t> (height * width)));
    char* dst;
    char* src;
    int col_offset, band_offset;
    if (datasetCells == NULL)
    {
        LFATAL << "QtEncode::convertTileToDataset - Error: Could not allocate memory. ";
        throw r_Error(r_Error::r_Error_MemoryAllocation);
    }
    // for all bands, convert data from column-major form (from Rasdaman) to row-major form (GDAL)
    // and then write the data to GDAL datasets
    for (int band = 0; band < numBands; band++)
    {
        dst = static_cast<char*> (datasetCells);
        band_offset = band * static_cast<int> (typeSize);

        for (int row = 0; row < height; row++)
            for (int col = 0; col < width; col++, dst += typeSize)
            {
                col_offset = ((row + height * col) * numBands * static_cast<int> (typeSize) + band_offset);
                src = tileCells + col_offset;
                if (isBoolean)
                {
                    if (src[0] == 1)
                        dst[0] = static_cast<char> (255);
                    else
                        dst[0] = 0;
                }
                else
                {
                    memcpy(dst, src, typeSize);
                }
            }

        CPLErr error = hMemDS->GetRasterBand(band + 1)->RasterIO(
            GF_Write, 0, 0, width, height, datasetCells, width, height, gdalBandType, 0, 0);
        if (error != CE_None)
        {
            LERROR << "Failed writing data to GDAL raster band: " << CPLGetLastErrorMsg();
            free(datasetCells);
            datasetCells = NULL;
            throw r_Error(r_Error::r_Error_Conversion);
        }
    }
    free(datasetCells);
    datasetCells = NULL;
    
    // set parameters
    setGDALParameters(hMemDS, width, height, numBands);
    
    r_TmpFile tmpFile;
    string tmpFilePath = tmpFile.getFileName();
    GDALDataset* gdalResult = driver->CreateCopy(
        tmpFilePath.c_str(), hMemDS, FALSE, fParams, NULL, NULL);
    if (!gdalResult)
    {
        LFATAL << "Failed encoding to format '" << format << "': " << CPLGetLastErrorMsg();
        throw r_Error(r_Error::r_Error_Conversion);
    }
    GDALClose(gdalResult);
    
    long fileSize = 0;
    desc.dest = tmpFile.readData(fileSize);
    desc.destInterv = r_Minterval(1) << r_Sinterval(static_cast<r_Range> (0),
                                                    static_cast<r_Range> (fileSize) - 1);
    desc.destType = r_Type::get_any_type("char");
    
    return desc;
}

r_Conv_Desc &r_Conv_GDAL::convertFrom(const char *options) throw (r_Error)
{
    GDALAllRegister();
    initDecodeParams(options);
    
    r_TmpFile tmpFileObj;
    tmpFileObj.writeData(desc.src, (size_t) desc.srcInterv.cell_count());
    string tmpFilePath = tmpFileObj.getFileName();
    
    GDALDataset *poDataset = static_cast<GDALDataset*> (GDALOpen(tmpFilePath.c_str(), GA_ReadOnly));
    if (poDataset == NULL)
    {
        LERROR << "failed opening file with GDAL, error: " << CPLGetLastErrorMsg();
        throw r_Error(r_Error::r_Error_Conversion);
    }
    
    /*if the format is specified as the second parameter
      then we pass the gdal parameters and create a new gdal data set*/
    if (!format.empty())
    {
        GDALDriver *driver = GetGDALDriverManager()->GetDriverByName(format.c_str());
        if (driver == NULL)
        {
            LFATAL << "unsupported format '" << format << "'.";
            throw r_Error(r_Error::r_Error_FeatureNotSupported);
        }
        poDataset = driver->CreateCopy(tmpFilePath.c_str(), poDataset, FALSE, fParams, NULL, NULL);
    }
    
    int width = poDataset->GetRasterXSize();
    int height = poDataset->GetRasterYSize();
    desc.destInterv = r_Minterval(2) << r_Sinterval(static_cast<r_Range> (0), static_cast<r_Range> (width) - 1)
                                     << r_Sinterval(static_cast<r_Range> (0), static_cast<r_Range> (height) - 1);

    desc.destType = ConvUtil::gdalTypeToRasType(poDataset);
    r_Bytes dataSize = 0;
    getTileCells(poDataset, /* out */ dataSize, /* out */ desc.dest);
    
    return desc;
}

void r_Conv_GDAL::getTileCells(GDALDataset* poDataset, /* out */ r_Bytes& size, /* out */ char*& contents)
{
	GDALDataType dataType = poDataset->GetRasterBand(1)->GetRasterDataType();
	switch (dataType)
	{
		case GDT_Byte:
		{//   Eight bit unsigned integer
            return resolveTileCellsByType<r_Char>(poDataset, size, contents);
		}
		case GDT_UInt16:
		{//     Sixteen bit unsigned integer
            return resolveTileCellsByType<r_UShort>(poDataset, size, contents);
		}
		case GDT_Int16:
		{ //  Sixteen bit signed integer
            return resolveTileCellsByType<r_Short>(poDataset, size, contents);
		}
		case GDT_UInt32:
		{//     Thirty two bit unsigned integer
            return resolveTileCellsByType<r_ULong>(poDataset, size, contents);
		}
		case GDT_Int32:
		{ //  Thirty two bit signed integer
            return resolveTileCellsByType<r_Long>(poDataset, size, contents);
		}
		case GDT_Float32:
		{//    Thirty two bit floating point
            return resolveTileCellsByType<r_Float>(poDataset, size, contents);
		}
		case GDT_Float64:
		{//    Sixty four bit floating point
            return resolveTileCellsByType<r_Double>(poDataset, size, contents);
		}
		default:
			throw r_Error(r_Error::r_Error_FeatureNotSupported);
			break;
	}
}

template<typename T>
void r_Conv_GDAL::resolveTileCellsByType(GDALDataset* poDataset, /* out */ r_Bytes& size, /* out */ char*& contents)
{
	int noOfBands = poDataset->GetRasterCount();
	int startCoordX = 0, startCoordY = 0, width = poDataset->GetRasterXSize(), height = poDataset->GetRasterYSize();
    GDALDataType gdalType = poDataset->GetRasterBand(1)->GetRasterDataType();

    size = static_cast<r_Bytes>(width) * static_cast<r_Bytes>(height) * static_cast<r_Bytes>(noOfBands) * sizeof(T);

    T *tileCells = (T*) mymalloc(size);
    contents = (char *) tileCells;

    T *gdalBand = (T*) mymalloc(static_cast<r_Bytes>(width) * static_cast<r_Bytes>(height) * sizeof(T));

	if (gdalBand == NULL || tileCells == NULL)
	{
		throw r_Error(r_Error::r_Error_MemoryAllocation);
	}


	//copy data from all GDAL bands to rasdaman
	for (int band = 0; band < noOfBands; band++)
	{
        CPLErr error = poDataset->GetRasterBand(band + 1)->RasterIO(GF_Read, startCoordX, startCoordY, width, height, gdalBand, width, height, gdalType, 0, 0);
		if (error != CE_None)
		{
			CPLError(error, 0, "Error copying band to rasdaman.");
			GDALClose(poDataset);
			free(gdalBand);
			throw r_Error(r_Error::r_Error_FeatureNotSupported);
		}

		//Transform the band data into rasdaman binary format
        T *pos = gdalBand;
		T *tilePos = tileCells + band;
		int tPos = 0 + band;
		for (int col = 0; col < width; col++)
		{
			for (int row = 0; row < height; row++, tilePos += noOfBands, tPos += noOfBands)
			{
				*tilePos = *(pos + (row * width + col));
			}
		}
	}
	//Free resources
	GDALClose(poDataset);
    free(gdalBand);
}

void r_Conv_GDAL::initDecodeParams(const char* paramsArg)
{
    fParams = CSLTokenizeString2(paramsArg, PARAM_SEPARATOR, CSLT_STRIPLEADSPACES |
                                    CSLT_STRIPENDSPACES);
}

void
r_Conv_GDAL::initEncodeParams(const char* paramsIn)
{
    if (paramsIn)
    {
        // replace escaped characters
        string paramsStr("");
        int i = 0;
        while (paramsIn[i] != '\0')
        {
            char curr = paramsIn[i];
            char next = paramsIn[i + 1];
            ++i;

            if (curr == '\\' && (next == '"' || next == '\'' || next == '\\'))
                continue;
            paramsStr += curr;
        }

        fParams = CSLTokenizeString2(paramsStr.c_str(), ";",
                                     CSLT_STRIPLEADSPACES |
                                     CSLT_STRIPENDSPACES);
    }
    setDouble(PARAM_XMIN, &gParams.xmin);
    setDouble(PARAM_XMAX, &gParams.xmax);
    setDouble(PARAM_YMIN, &gParams.ymin);
    setDouble(PARAM_YMAX, &gParams.ymax);
    setString(PARAM_CRS, &gParams.crs);
    setString(PARAM_METADATA, &gParams.metadata);

    // GDAL configuration options (config="key1 value1, key2 value2, ...")
    int ind;
    if ((ind = CSLFindName(fParams, PARAM_CONFIG)) != -1)
    {
        LDEBUG << "Found GDAL configuration parameters.";
        // parse the KV-pairs and set them to GDAL environment
        const char* kvPairs = CSLFetchNameValue(fParams, PARAM_CONFIG);
        LDEBUG << " KV-PAIRS = '" << kvPairs << "'";
        char** kvPairsList = CSLTokenizeString2(kvPairs, ",",
                                                CSLT_STRIPLEADSPACES |
                                                CSLT_STRIPENDSPACES);
        for (int iKvPair = 0; iKvPair < CSLCount(kvPairsList); iKvPair++)
        {
            // foreach KV pair in confParamList DO CPLSetConfigOption
            const char* kvPair = kvPairsList[iKvPair];
            char** kvPairList = CSLTokenizeString2(kvPair, " ",
                                                   CSLT_STRIPLEADSPACES |
                                                   CSLT_STRIPENDSPACES);
            CPLString* keyString = new CPLString(static_cast<const char*> (kvPairList[0]));
            CPLString* valueString = new CPLString(static_cast<const char*> (kvPairList[1]));
            const char* confKey = keyString->c_str();
            const char* confValue = valueString->c_str();
            LDEBUG << " KEY = '" << confKey << "' VALUE ='" << confValue << "'";
            CPLSetConfigOption(confKey, confValue); // this option is then read by the CreateCopy() method of the GDAL format
        }
    }

    string nodata("");
    setString(PARAM_NODATA, &nodata);

    if (!nodata.empty())
    {
        char* pch = const_cast<char*> (nodata.c_str());
        pch = strtok(pch, NODATA_VALUE_SEPARATOR);
        while (pch != NULL)
        {
            double value = strtod(pch, NULL);
            gParams.nodata.push_back(value);
            pch = strtok(NULL, NODATA_VALUE_SEPARATOR);
        }
    }
}

void
r_Conv_GDAL::setGDALParameters(GDALDataset *gdalDataSet, int width, int height, int nBands)
{
    if (gParams.xmin != DBL_MAX && gParams.xmax != DBL_MAX && gParams.ymin != DBL_MAX && gParams.ymax != DBL_MAX)
    {
        double adfGeoTransform[6];
        adfGeoTransform[0] = gParams.xmin;
        adfGeoTransform[1] = (gParams.xmax - gParams.xmin) / width;
        adfGeoTransform[2] = 0.0;
        adfGeoTransform[3] = gParams.ymax;
        adfGeoTransform[4] = 0.0;
        adfGeoTransform[5] = -(gParams.ymax - gParams.ymin) / height;
        gdalDataSet->SetGeoTransform(adfGeoTransform);
    }

    if (gParams.crs != string(""))
    {
        OGRSpatialReference srs;

        // setup input coordinate system. Try import from EPSG, Proj.4, ESRI and last, from a WKT string
        const char *crs = gParams.crs.c_str();
        char *wkt = NULL;

        OGRErr err = srs.SetFromUserInput(crs);
        if (err != OGRERR_NONE)
        {
            LWARNING << "GDAL could not understand coordinate reference system: '" << crs << "'.";
        }
        else
        {
            srs.exportToWkt(&wkt);
            gdalDataSet->SetProjection(wkt);
        }
    }

    if (gParams.metadata != string(""))
    {
        char** metadata = NULL;
        metadata = CSLAddNameValue(metadata, "metadata", gParams.metadata.c_str());
        gdalDataSet->SetMetadata(metadata);
    }

    if (gParams.nodata.size() > 0)
    {
        for (int band = 0; band < nBands; band++)
        {
            GDALRasterBand* rasterBand = gdalDataSet->GetRasterBand(band + 1);

            // if only one value is provided use the same for all bands
            if (gParams.nodata.size() == 1)
            {
                rasterBand->SetNoDataValue(gParams.nodata.at(0));
            }
            else if (static_cast<int> (gParams.nodata.size()) == nBands)
            {
                rasterBand->SetNoDataValue(gParams.nodata.at(static_cast<size_t> (band)));
            }
            else
            {
                // warning, nodata value no != band no -- DM 2012-dec-10
                LWARNING << "Warning: ignored setting NODATA value, number of NODATA values (" <<
                    gParams.nodata.size() << ") doesn't match the number of bands (" << nBands << ").";
                break;
            }
        }
    }
}

void
r_Conv_GDAL::setDouble(const char* paramName, double* value)
{
    int ind;
    if ((ind = CSLFindName(fParams, paramName)) != -1)
        *value = strtod(CSLFetchNameValue(fParams, paramName), NULL);
    else
        *value = DBL_MAX;
}

void
r_Conv_GDAL::setString(const char* paramName, string* value)
{
    int ind;
    if ((ind = CSLFindName(fParams, paramName)) != -1)
        *value = CSLFetchNameValue(fParams, paramName);
    else
        *value = "";
}

#else // HAVE_GDAL

r_Conv_Desc &r_Conv_GDAL::convertFrom(const char *options) throw(r_Error)
{
    LERROR << "support for decoding with GDAL is not enabled; rasdaman should be configured with option --with-gdal to enable it.";
    throw r_Error(r_Error::r_Error_FeatureNotSupported);
}

r_Conv_Desc &r_Conv_GDAL::convertTo(const char *options) throw(r_Error)
{
    LERROR << "support for encoding with GDAL is not enabled; rasdaman should be configured with option --with-gdal to enable it.";
    throw r_Error(r_Error::r_Error_FeatureNotSupported);
}

#endif // HAVE_GDAL

/// cloning

r_Convertor *r_Conv_GDAL::clone(void) const
{
    return new r_Conv_GDAL(desc.src, desc.srcInterv, desc.baseType);
}

/// identification

const char *r_Conv_GDAL::get_name(void) const
{
    return format_name_gdal;
}

r_Data_Format r_Conv_GDAL::get_data_format(void) const
{
    return r_GDAL;
}
