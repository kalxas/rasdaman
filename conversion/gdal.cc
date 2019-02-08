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
#include "conversion/tmpfile.hh"
#include "conversion/convutil.hh"
#include "conversion/mimetypes.hh"
#include "conversion/formatparamkeys.hh"
#include "raslib/error.hh"
#include "raslib/parseparams.hh"
#include "raslib/primitivetype.hh"
#include "raslib/attribute.hh"
#include "raslib/structuretype.hh"
#include "raslib/odmgtypes.hh"
#include "mymalloc/mymalloc.h"
#include "conversion/transpose.hh"

#include <logging.hh>
#include <limits>
#include <cassert>
#include <boost/algorithm/string.hpp>

#include <string.h>
#include <errno.h>
#include <boost/lexical_cast.hpp>

using namespace std;
using namespace FormatParamKeys::Encode::GDAL;

#ifndef DATA_CHUNK_SIZE
#define DATA_CHUNK_SIZE 10000 //no of bytes to be written in a file
#endif

#ifndef PARAM_SEPARATOR
#define PARAM_SEPARATOR ";"
#endif

#ifndef GDAL_PARAMS
#define GPDAL_PARAMS true
#define NODATA_VALUE_SEPARATOR " ,"
#define NODATA_DEFAULT_VALUE 0.0
#endif

const string r_Conv_GDAL::GDAL_KEY_METADATA{"METADATA"};
const string r_Conv_GDAL::GDAL_KEY_NODATA_VALUES{"NODATA_VALUES"};
const string r_Conv_GDAL::GDAL_KEY_IMAGE_STRUCTURE{"IMAGE_STRUCTURE"};
const string r_Conv_GDAL::GDAL_KEY_PIXELTYPE{"PIXELTYPE"};
const string r_Conv_GDAL::GDAL_VAL_SIGNEDBYTE{"SIGNEDBYTE"};


const string r_Conv_GDAL::PNG_COMPRESSION_PARAM{"ZLEVEL"};
const string r_Conv_GDAL::PNG_DEFAULT_ZLEVEL{"2"};
const string r_Conv_GDAL::PNG_FORMAT{"png"};

/// constructor using an r_Type object. Exception if the type isn't atomic.

r_Conv_GDAL::r_Conv_GDAL(const char* src, const r_Minterval& interv, const r_Type* tp)
    : r_Convert_Memory(src, interv, tp, true), poDataset{NULL}
{
}

/// constructor using convert_type_e shortcut

r_Conv_GDAL::r_Conv_GDAL(const char* src, const r_Minterval& interv, int tp)
    : r_Convert_Memory(src, interv, tp), poDataset{NULL}
{
}


/// destructor

r_Conv_GDAL::~r_Conv_GDAL(void)
{
    if (poDataset != NULL)
    {
        GDALClose(poDataset);
        poDataset = NULL;
    }
}

#ifdef HAVE_GDAL

r_Conv_Desc& r_Conv_GDAL::convertTo(const char* options,
                                    const r_Range* nullValue)
{
    if (format.empty())
    {
        LERROR << "no format specified";
        throw r_Error(r_Error::r_Error_Conversion);
    }
    if (r_MimeTypes::isMimeType(format))
    {
        format = r_MimeTypes::getFormatName(format);
    }
    if (options)
    {
        initEncodeParams(string{options});
    }
    updateNodataValue(nullValue);
    
    //if selected, transposes rasdaman data before converting to gdal
    if(formatParams.isTranspose())
    {
        transpose(const_cast<char*>(desc.src), desc.srcInterv, desc.srcType, formatParams.getTranspose());
    }

    GDALAllRegister();
    GDALDriver* driver = GetGDALDriverManager()->GetDriverByName(format.c_str());
    if (driver == NULL)
    {
        LERROR << "Unsupported format: " << format;
        throw r_Error(r_Error::r_Error_FeatureNotSupported);
    }

    auto imageSize = getImageSize();
    unsigned int width = imageSize.first;
    unsigned int height = imageSize.second;

    unsigned int numBands = ConvUtil::getNumberOfBands(desc.srcType);
    r_Primitive_Type* rasBandType = getBandType(desc.srcType);
    GDALDataType gdalBandType = ConvUtil::rasTypeToGdalType(rasBandType);

    GDALDriver* hMemDriver = static_cast<GDALDriver*>(GDALGetDriverByName("MEM"));
    if (hMemDriver == NULL)
    {
        LERROR << "Could not init GDAL driver: " << CPLGetLastErrorMsg();
        throw r_Error(r_Error::r_Error_Conversion);
    }
    poDataset = hMemDriver->Create("in_memory_image", static_cast<int>(width), static_cast<int>(height),
                             static_cast<int>(numBands), gdalBandType, NULL);
    if (poDataset == NULL)
    {
        LERROR << "failed creating in memory GDAL dataset, error: " << CPLGetLastErrorMsg();
        throw r_Error(r_Error::r_Error_Conversion);
    }

    r_TmpFile tmpFile;
    encodeImage(gdalBandType, rasBandType, width, height, numBands);
    setEncodeParams();
    CPLStringList formatParameters;
    getFormatParameters(formatParameters, rasBandType);

    string tmpFilePath = tmpFile.getFileName();
    GDALDataset* gdalResult = driver->CreateCopy(tmpFilePath.c_str(), poDataset, FALSE, formatParameters.List(), NULL, NULL);
    if (!gdalResult)
    {
        LERROR << "Failed encoding to format '" << format << "': " << CPLGetLastErrorMsg();
        throw r_Error(r_Error::r_Error_Conversion);
    }
    GDALClose(gdalResult);

    long fileSize = 0;
    desc.dest = tmpFile.readData(fileSize);
    desc.destInterv = r_Minterval(1) << r_Sinterval(static_cast<r_Range>(0),
                      static_cast<r_Range>(fileSize) - 1);
    desc.destType = r_Type::get_any_type("char");
    
    return desc;
}

void r_Conv_GDAL::encodeImage(GDALDataType gdalBandType, r_Primitive_Type* rasBandType,
                              unsigned int width, unsigned int height, unsigned int numBands)
{
    bool isBoolean = rasBandType->type_id() == r_Type::BOOL;
    switch (gdalBandType)
    {
    case GDT_Byte:
    {
        encodeImage<r_Char>(gdalBandType, isBoolean, width, height, numBands);
        break;
    }
    case GDT_UInt16:
    {
        encodeImage<r_UShort>(gdalBandType, isBoolean, width, height, numBands);
        break;
    }
    case GDT_Int16:
    {
        encodeImage<r_Short>(gdalBandType, isBoolean, width, height, numBands);
        break;
    }
    case GDT_UInt32:
    {
        encodeImage<r_ULong>(gdalBandType, isBoolean, width, height, numBands);
        break;
    }
    case GDT_Int32:
    {
        encodeImage<r_Long>(gdalBandType, isBoolean, width, height, numBands);
        break;
    }
    case GDT_Float32:
    {
        encodeImage<r_Float>(gdalBandType, isBoolean, width, height, numBands);
        break;
    }
    case GDT_Float64:
    {
        encodeImage<r_Double>(gdalBandType, isBoolean, width, height, numBands);
        break;
    }
    default:
    {
        LERROR << "unsupported base type: '" << rasBandType->name() << "'.";
        throw r_Error(r_Error::r_Error_FeatureNotSupported);
    }
    }
}

template<typename T>
void r_Conv_GDAL::encodeImage(GDALDataType gdalBandType, bool isBoolean,
                              unsigned int width, unsigned int height, unsigned int numBands)
{
    size_t area = static_cast<size_t>(width) * static_cast<size_t>(height);
    unique_ptr<T[]> dstCells;
    dstCells.reset(new(nothrow) T[area]);
    if (!dstCells)
    {
        LERROR << "failed allocating " << area << " bytes of memory.";
        throw r_Error(r_Error::r_Error_MemoryAllocation);
    }

    unsigned int col_offset;
    for (unsigned int band = 0; band < numBands; band++)
    {
        T* dst = dstCells.get();
        T* src = (reinterpret_cast<T*>(const_cast<char*>(desc.src))) + band;

        if (!isBoolean)
        {
            for (unsigned int row = 0; row < height; row++)
            {
                for (unsigned int col = 0; col < width; col++, dst++)
                {
                    col_offset = (row + height * col) * numBands;
                    dst[0] = src[col_offset];
                }
            }
        }
        else
        {
            for (unsigned int row = 0; row < height; row++)
            {
                for (unsigned int col = 0; col < width; col++, dst++)
                {
                    col_offset = (row + height * col) * numBands;
                    if (src[col_offset] == 1)
                    {
                        dst[0] = static_cast<unsigned char>(255);
                    }
                    else
                    {
                        dst[0] = 0;
                    }
                }
            }
        }

        CPLErr error = poDataset->GetRasterBand((int)(band + 1))->
                       RasterIO(GF_Write, 0, 0, (int)width, (int)height, (char*) dstCells.get(),
                                (int)width, (int)height, gdalBandType, 0, 0);
        if (error != CE_None)
        {
            LERROR << "Failed writing data to GDAL raster band: " << CPLGetLastErrorMsg();
            throw r_Error(r_Error::r_Error_Conversion);
        }
    }
    dstCells.reset();
}

pair<unsigned int, unsigned int> r_Conv_GDAL::getImageSize()
{
    if (desc.srcInterv.dimension() != 2)
    {
        LERROR << "only 2D data can be encoded with GDAL.";
        throw r_Error(r_Error::r_Error_Conversion);
    }
    r_Range w = desc.srcInterv[0].get_extent();
    r_Range h = desc.srcInterv[1].get_extent();
    if (w > numeric_limits<int>::max() || h > numeric_limits<int>::max())
    {
        LERROR << "cannot encode array of size " << w << " x " << h << ", " <<
               "maximum support size by GDAL is " <<
               numeric_limits<int>::max() << " x " << numeric_limits<int>::max() << ".";
        throw r_Error(r_Error::r_Error_Conversion);
    }
    return make_pair((unsigned int)w, (unsigned int)h);
}

r_Conv_Desc& r_Conv_GDAL::convertFrom(const char* options)
{
    if (options)
    {
        formatParams.parse(string{options});
        setConfigOptions();
    }
    return this->convertFrom(formatParams);
}

r_Conv_Desc& r_Conv_GDAL::convertFrom(r_Format_Params options)
{
    formatParams = options;
    string tmpFilePath("");
    r_TmpFile tmpFileObj;
    if (formatParams.getFilePaths().empty())
    {
        tmpFileObj.writeData(desc.src, (size_t) desc.srcInterv.cell_count());
        tmpFilePath = tmpFileObj.getFileName();
    }
    else
    {
        tmpFilePath = formatParams.getFilePath();
    }

    GDALAllRegister();
    poDataset = static_cast<GDALDataset*>(GDALOpen(tmpFilePath.c_str(), GA_ReadOnly));
    if (poDataset == NULL)
    {
        LERROR << "failed opening file with GDAL, reason: " << CPLGetLastErrorMsg();
        throw r_Error(r_Error::r_Error_Conversion);
    }

    bandIds = getBandIds();
    desc.destType = ConvUtil::gdalTypeToRasType(poDataset, bandIds);
    setTargetDomain();
    desc.dest = decodeImage();

    return desc;

}

char* r_Conv_GDAL::decodeImage()
{
    assert(desc.destType);

    const auto &a = formatParams.isTranspose() ? desc.destInterv[1] : desc.destInterv[0];
    const auto &b = formatParams.isTranspose() ? desc.destInterv[0] : desc.destInterv[1];
    int width = a.get_extent();
    int height = b.get_extent();
    int offsetX = a.low();
    int offsetY = b.low();

    size_t tileBaseTypeSize = static_cast<size_t>(((r_Base_Type*)desc.destType)->size());
    size_t dataSize = static_cast<size_t>(width) * static_cast<size_t>(height) * tileBaseTypeSize;
    LTRACE << "allocating tile cells of size " << dataSize;
    char* tileCells RAS_ALIGNED = (char*) mymalloc(dataSize);
    if (tileCells == NULL)
    {
        LERROR << "failed allocating " << dataSize << " bytes for decoding input file.";
        throw r_Error(r_Error::r_Error_MemoryAllocation);
    }

    // copy data from all GDAL bands to rasdaman
    size_t bandOffset = 0;
    size_t bandSize = 0;
    char* bandCells RAS_ALIGNED = NULL;
    for (int bandId : bandIds)
    {
        size_t bandBaseTypeSize = ConvUtil::getBandBaseTypeSize(desc.destType, bandId);
        size_t newBandSize = static_cast<size_t>(width) * static_cast<size_t>(height) * bandBaseTypeSize;
        LTRACE << "allocating band cells of size " << newBandSize;
        bandCells = upsizeBufferIfNeeded(bandCells, bandSize, newBandSize);

        GDALRasterBand* gdalBand = poDataset->GetRasterBand(bandId + 1);
        GDALDataType bandType = gdalBand->GetRasterDataType();
        CPLErr error = gdalBand->RasterIO(GF_Read, offsetX, offsetY, width, height,
                                          (void*) bandCells, width, height, bandType, 0, 0);
        if (error != CE_None)
        {
            LERROR << "failed decoding band " << bandId << " from the input file; reason: " << CPLGetLastErrorMsg();
            free(bandCells);
            bandCells = NULL;
            free(tileCells);
            tileCells = NULL;
            throw r_Error(r_Error::r_Error_Conversion);
        }

        bool signedByte = false;
        const char* pixelType = gdalBand->GetMetadataItem(GDAL_KEY_PIXELTYPE.c_str(), GDAL_KEY_IMAGE_STRUCTURE.c_str());
        if (pixelType)
        {
            signedByte = string{pixelType} == GDAL_VAL_SIGNEDBYTE;
        }

        decodeBand(bandCells, tileCells + bandOffset, tileBaseTypeSize, width, height, bandType, signedByte);
        bandOffset += bandBaseTypeSize;
    }
    // Free resources
    if (bandCells)
    {
        free(bandCells);
        bandCells = NULL;
    }

    return tileCells;
}

void r_Conv_GDAL::setTargetDomain(bool transpose)
{
    const auto &subsetDomain = formatParams.getSubsetDomain();
    if (subsetDomain.dimension() == 2)
    {
        desc.destInterv = subsetDomain;
    }
    else if (subsetDomain.dimension() != 0)
    {
        LERROR << "invalid 'subsetDomain' parameter '" << subsetDomain << "', the GDAL convertor supports only 2D subsets.";
        throw r_Error(INVALIDFORMATPARAMETER);
    }
    else
    {
        desc.destInterv = r_Minterval(2) << r_Sinterval(0ll, static_cast<r_Range>(poDataset->GetRasterXSize()) - 1)
                          << r_Sinterval(0ll, static_cast<r_Range>(poDataset->GetRasterYSize()) - 1);
    }
    if (transpose && formatParams.isTranspose())
        desc.destInterv.transpose(0, 1);
}

char* r_Conv_GDAL::upsizeBufferIfNeeded(char* buffer, size_t& bufferSize, size_t newBufferSize)
{
    if (newBufferSize > bufferSize)
    {
        if (buffer)
        {
            free(buffer);
            buffer = NULL;
        }
        buffer = (char*) mymalloc(newBufferSize);
        if (buffer == NULL)
        {
            LERROR << "failed allocating " << newBufferSize << " bytes for decoding a single band from the input file.";
            throw r_Error(r_Error::r_Error_MemoryAllocation);
        }
        bufferSize = newBufferSize;
    }
    return buffer;
}

void r_Conv_GDAL::decodeBand(const char* bandCells, char* tileCells, size_t tileBaseTypeSize, int w, int h,
        GDALDataType gdalBandType, bool signedByte)
{
    size_t width = static_cast<size_t>(w);
    size_t height = static_cast<size_t>(h);
    switch (gdalBandType)
    {
    case GDT_Byte:
    {
        if (!signedByte)
        {
            transposeBand<r_Char>(bandCells, tileCells, tileBaseTypeSize, width, height);
        }
        else
        {
            transposeBand<r_Octet>(bandCells, tileCells, tileBaseTypeSize, width, height);
        }
        break;
    }
    case GDT_UInt16:
    {
        transposeBand<r_UShort>(bandCells, tileCells, tileBaseTypeSize, width, height);
        break;
    }
    case GDT_Int16:
    {
        transposeBand<r_Short>(bandCells, tileCells, tileBaseTypeSize, width, height);
        break;
    }
    case GDT_UInt32:
    {
        transposeBand<r_ULong>(bandCells, tileCells, tileBaseTypeSize, width, height);
        break;
    }
    case GDT_Int32:
    {
        transposeBand<r_Long>(bandCells, tileCells, tileBaseTypeSize, width, height);
        break;
    }
    case GDT_Float32:
    {
        transposeBand<r_Float>(bandCells, tileCells, tileBaseTypeSize, width, height);
        break;
    }
    case GDT_Float64:
    {
        transposeBand<r_Double>(bandCells, tileCells, tileBaseTypeSize, width, height);
        break;
    }
    default:
        throw r_Error(r_Error::r_Error_FeatureNotSupported);
    }
}

template<typename T>
void r_Conv_GDAL::transposeBand(const char *__restrict__ srcIn,
                                char *__restrict__ dst, size_t tileBaseTypeSize,
                                size_t width, size_t height)
{
    const T* __restrict__ src RAS_ALIGNED = reinterpret_cast<T*>(const_cast<char*>(srcIn));

    if (!formatParams.isTranspose())
    {
        LTRACE << "transposing " << width << " x " << height << " matrix...";
        if (bandIds.size() > 1)
        {
            for (size_t col = 0; col < width; ++col)
                for (size_t row = 0; row < height; ++row, dst += tileBaseTypeSize)
                    *((T *) dst) = src[row * width + col];
        }
        else
        {
            // single band optimization
            T* __restrict__ dstT RAS_ALIGNED = reinterpret_cast<T*>(dst);
            for (size_t col = 0; col < width; ++col)
                for (size_t row = 0; row < height; ++row, ++dstT)
                    *dstT = src[row * width + col];
        }
    }
    else
    {
        LTRACE << "copying " << width << " x " << height << " matrix...";
        if (bandIds.size() > 1)
        {
            for (size_t i = 0; i < width * height; ++i, dst += tileBaseTypeSize)
                *((T *) dst) = src[i];
        }
        else
        {
            // single band optimization
            memcpy(dst, srcIn, width * height * tileBaseTypeSize);
        }
    }
}

vector<int> r_Conv_GDAL::getBandIds()
{
    vector<int> ret = formatParams.getBandIds();
    int noOfBands = 0;
    if (ret.empty())
    {
        noOfBands = poDataset->GetRasterCount();
        for (int band = 0; band < noOfBands; band++)
        {
            ret.push_back(band);
        }
    }
    else
    {
        noOfBands = (int) ret.size();
        for (int i = 0; i < noOfBands; i++)
        {
            int bandId = ret[(size_t)i];
            if (bandId < 0 || bandId >= poDataset->GetRasterCount())
            {
                LERROR << "band id '" << bandId << "' out of range 0 - " << (poDataset->GetRasterCount() - 1) << ".";
                throw r_Error(INVALIDFORMATPARAMETER);
            }
            ret.push_back(bandId);
        }
    }
    return ret;
}

r_Primitive_Type* r_Conv_GDAL::getBandType(const r_Type* baseType)
{
    r_Primitive_Type* ret = NULL;
    if (baseType->isPrimitiveType()) // = one band
    {
        ret = static_cast<r_Primitive_Type*>(const_cast<r_Type*>(baseType));
    }
    else if (baseType->isStructType()) // = multiple bands
    {
        r_Structure_Type* structType = static_cast<r_Structure_Type*>(const_cast<r_Type*>(baseType));
        r_Structure_Type::attribute_iterator iter(structType->defines_attribute_begin());
        while (iter != structType->defines_attribute_end())
        {
            // check the band types, they have to be of the same type
            if ((*iter).type_of().isPrimitiveType())
            {
                r_Primitive_Type pt = static_cast<r_Primitive_Type&>(const_cast<r_Base_Type&>((*iter).type_of()));
                if (ret != NULL)
                {
                    if (ret->type_id() != pt.type_id())
                    {
                        LERROR << "Can not handle bands of different types.";
                        throw r_Error(r_Error::r_Error_Conversion);
                    }
                }
                else
                {
                    ret = static_cast<r_Primitive_Type*>(pt.clone());
                }
            }
            else
            {
                LERROR << "Can not handle composite bands.";
                throw r_Error(r_Error::r_Error_Conversion);
            }
            ++iter;
        }
    }
    return ret;
}

void
r_Conv_GDAL::initEncodeParams(const string& paramsIn)
{
    if (formatParams.parse(paramsIn))
    {
        setConfigOptions();
    }
    else
    {
        // replace escaped characters
        string paramsStr{paramsIn};
        boost::algorithm::replace_all(paramsStr, "\\\"", "\"");
        boost::algorithm::replace_all(paramsStr, "\\\'", "\'");
        boost::algorithm::replace_all(paramsStr, "\\\\", "\\");
        CPLStringList paramsList{CSLTokenizeString2(
                                     paramsStr.c_str(), ";", CSLT_STRIPLEADSPACES | CSLT_STRIPENDSPACES)};

        // set xmin, xmax, .., crs, metadata
        formatParams.setXmin(getDouble(FormatParamKeys::Encode::XMIN, paramsList));
        formatParams.setXmax(getDouble(FormatParamKeys::Encode::XMAX, paramsList));
        formatParams.setYmin(getDouble(FormatParamKeys::Encode::YMIN, paramsList));
        formatParams.setYmax(getDouble(FormatParamKeys::Encode::YMAX, paramsList));
        formatParams.setCrs(getString(FormatParamKeys::Encode::CRS, paramsList));
        formatParams.setMetadata(getString(FormatParamKeys::Encode::METADATA, paramsList));

        // GDAL configuration options (config="key1 value1, key2 value2, ...")
        const string& kvPairs = getString(FormatParamKeys::General::CONFIG_OPTIONS_LEGACY, paramsList);
        if (!kvPairs.empty())
        {
            CPLStringList kvPairsList{CSLTokenizeString2(
                                          kvPairs.c_str(), ",", CSLT_STRIPLEADSPACES | CSLT_STRIPENDSPACES)};
            for (int i = 0; i < kvPairsList.Count(); i++)
            {
                CPLStringList kvPair{CSLTokenizeString2(
                                         kvPairsList[i], " ", CSLT_STRIPLEADSPACES | CSLT_STRIPENDSPACES)};
                CPLSetConfigOption(kvPair[0], kvPair[1]);
            }
        }

        // nodata
        string nodata = getString(FormatParamKeys::Encode::NODATA, paramsList);
        if (!nodata.empty())
        {
            formatParams.addNodata(getDouble(FormatParamKeys::Encode::NODATA, paramsList));
        }

        // format parameters
        for (int i = 0; i < paramsList.Count(); i++)
        {
            CPLStringList kvPair{CSLTokenizeString2(paramsList[i], "=", CSLT_STRIPLEADSPACES | CSLT_STRIPENDSPACES)};
            if (kvPair.Count() == 2)
            {
                string key{kvPair[0]};
                string val{kvPair[1]};
                if (key != FormatParamKeys::Encode::XMIN &&
                        key != FormatParamKeys::Encode::XMAX &&
                        key != FormatParamKeys::Encode::YMIN &&
                        key != FormatParamKeys::Encode::YMAX &&
                        key != FormatParamKeys::Encode::CRS &&
                        key != FormatParamKeys::Encode::NODATA &&
                        key != FormatParamKeys::Encode::METADATA &&
                        key != FormatParamKeys::General::CONFIG_OPTIONS_LEGACY)
                {
                    formatParams.addFormatParameter(key, val);
                }
            }
        }
    }
}

void
r_Conv_GDAL::setEncodeParams()
{
    setMetadata();
    setNodata();
    setGeoreference();
    setColorPalette();
}

void
r_Conv_GDAL::setMetadata()
{
    if (!formatParams.getMetadata().empty())
    {
        if (poDataset->SetMetadataItem(GDAL_KEY_METADATA.c_str(),
                                       formatParams.getMetadata().c_str()) != CE_None)
        {
            LWARNING << "failed setting metadata '" << formatParams.getMetadata() <<
                     "', error: " << CPLGetLastErrorMsg();
        }
    }
    else if (!formatParams.getMetadataKeyValues().empty())
    {
        for (const pair<string, string>& kv : formatParams.getMetadataKeyValues())
        {
            if (poDataset->SetMetadataItem(kv.first.c_str(), kv.second.c_str()) != CE_None)
            {
                LWARNING << "failed setting metadata key '" << kv.first << "' to value '" <<
                         kv.second << "', error: " << CPLGetLastErrorMsg();
            }
        }
    }
}

void
r_Conv_GDAL::setNodata()
{
    const vector<double>& nodata = formatParams.getNodata();
    if (!nodata.empty())
    {
        int nBands = poDataset->GetRasterCount();
        stringstream nodataValues{stringstream::out};
        for (int band = 1; band <= nBands; band++)
        {
            if (band > 1)
            {
                nodataValues << " ";
            }
            if (nodata.size() == 1)
            {
                poDataset->GetRasterBand(band)->SetNoDataValue(nodata[0]);
                nodataValues << nodata[0];
            }
            else if (static_cast<int>(nodata.size()) == nBands)
            {
                double value = nodata[(size_t)band - 1];
                poDataset->GetRasterBand(band)->SetNoDataValue(value);
                nodataValues << value;
            }
            else
            {
                LWARNING << "failed setting nodata, number of nodata values (" << nodata.size() <<
                         ") doesn't match the number of bands (" << nBands << ").";
                break;
            }
        }
        string nodataValuesStr = nodataValues.str();
        poDataset->SetMetadataItem(GDAL_KEY_NODATA_VALUES.c_str(), nodataValuesStr.c_str());
    }
}

void
r_Conv_GDAL::setGeoreference()
{
    if (formatParams.getXmin() != numeric_limits<double>::max() &&
            formatParams.getXmax() != numeric_limits<double>::max() &&
            formatParams.getYmin() != numeric_limits<double>::max() &&
            formatParams.getYmax() != numeric_limits<double>::max())
    {
        setGeotransform();
    }
    else
    {
        const Json::Value& geoRefJson = formatParams.getParams().get(FormatParamKeys::Encode::GEO_REFERENCE, Json::Value::null);
        if (!geoRefJson.isNull())
        {
            if (geoRefJson.isMember(GCPS))
            {
                setGCPs(geoRefJson[GCPS]);
            }
        }
    }
}

void
r_Conv_GDAL::setGeotransform()
{
    double adfGeoTransform[6];
    adfGeoTransform[0] = formatParams.getXmin();
    adfGeoTransform[1] = (formatParams.getXmax() - formatParams.getXmin()) / poDataset->GetRasterXSize();
    adfGeoTransform[2] = 0.0;
    adfGeoTransform[3] = formatParams.getYmax();
    adfGeoTransform[4] = 0.0;
    adfGeoTransform[5] = -(formatParams.getYmax() - formatParams.getYmin()) / poDataset->GetRasterYSize();
    poDataset->SetGeoTransform(adfGeoTransform);

    const string& wktStr = getCrsWkt();
    if (!wktStr.empty())
    {
        if (poDataset->SetProjection(wktStr.c_str()) == CE_Failure)
        {
            LWARNING << "invalid CRS projection '" << formatParams.getCrs() << "', error: " << CPLGetLastErrorMsg();
        }
    }
}

void
r_Conv_GDAL::setGCPs(const Json::Value& gcpsJson)
{
    const string& key = GCPS;
    if (gcpsJson.isArray())
    {
        unsigned int gcpCount = gcpsJson.size();
        unique_ptr<GDAL_GCP[]> gdalGcpsPtr(new GDAL_GCP[gcpCount]);
        GDAL_GCP* gdalGcps = gdalGcpsPtr.get();

        for (Json::ArrayIndex i = 0; i < gcpCount; i++)
        {
            const Json::Value& gcp = gcpsJson[i];
            if (!gcp.isObject())
            {
                LERROR << "parameter '" << key << "' has an invalid value, expected an array of GCP objects.";
                throw r_Error(INVALIDFORMATPARAMETER);
            }
            else
            {
                gdalGcps[i].dfGCPZ = 0; // optional, zero by default
                gdalGcps[i].pszId = NULL;
                gdalGcps[i].pszInfo = NULL;
                for (const string& fkey : gcp.getMemberNames())
                {
                    if (fkey == GCP_ID)
                    {
                        gdalGcps[i].pszId = const_cast<char*>(gcp[fkey].asCString());
                    }
                    else if (fkey == GCP_INFO)
                    {
                        gdalGcps[i].pszInfo = const_cast<char*>(gcp[fkey].asCString());
                    }
                    else
                    {
                        if (!gcp[fkey].isDouble())
                        {
                            LERROR << "parameter '" << key << "." << fkey << "' has an invalid value, expected double.";
                            throw r_Error(INVALIDFORMATPARAMETER);
                        }
                        double value = gcp[fkey].asDouble();
                        if (fkey == GCP_LINE)
                        {
                            gdalGcps[i].dfGCPLine = value;
                        }
                        else if (fkey == GCP_PIXEL)
                        {
                            gdalGcps[i].dfGCPPixel = value;
                        }
                        else if (fkey == GCP_X)
                        {
                            gdalGcps[i].dfGCPX = value;
                        }
                        else if (fkey == GCP_Y)
                        {
                            gdalGcps[i].dfGCPY = value;
                        }
                        else if (fkey == GCP_Z)
                        {
                            gdalGcps[i].dfGCPZ = value;
                        }
                    }
                }
                if (gdalGcps[i].pszId == NULL)
                {
                    gdalGcps[i].pszId = const_cast<char*>(boost::lexical_cast<string>(i).c_str());
                }
            }
        }

        const string& wktStr = getCrsWkt();
        if (poDataset->SetGCPs((int) gcpCount, gdalGcps, wktStr.c_str()) != CE_None)
        {
            LWARNING << "failed setting GCPs, reason: " << CPLGetLastErrorMsg();
        }
    }
    else
    {
        LERROR << "parameter '" << key << "' has an invalid value, expected an array of GCP objects.";
        throw r_Error(INVALIDFORMATPARAMETER);
    }
}

string r_Conv_GDAL::getCrsWkt()
{
    string ret{""};
    if (!formatParams.getCrs().empty())
    {
        OGRSpatialReference srs;

        // setup input coordinate system. Try to import from EPSG, Proj.4, ESRI and last, from a WKT string
        if (srs.SetFromUserInput(formatParams.getCrs().c_str()) != OGRERR_NONE)
        {
            LWARNING << "GDAL could not understand coordinate reference system: '" << formatParams.getCrs() << "'.";
        }
        else
        {
            char* wkt = NULL;
            if (srs.exportToWkt(&wkt) != OGRERR_NONE)
            {
                LWARNING << "failed exporting CRS '" << formatParams.getCrs() << "' to WKT format.";
            }
            else
            {
                ret = string{wkt};
                CPLFree(wkt);
            }
        }
    }
    return ret;
}

void
r_Conv_GDAL::setColorPalette()
{
    const Json::Value& colorPaletteJson = formatParams.getParams().get(COLOR_PALETTE, Json::Value::null);
    if (colorPaletteJson.isNull())
    {
        return;
    }
    if (!colorPaletteJson.isObject())
    {
        LERROR << "parameter '" << COLOR_PALETTE << "' has an invalid value, expected an object " <<
               "containing palette interpretation, color interpretation, color table.";
        throw r_Error(INVALIDFORMATPARAMETER);
    }

    // color table and palette interpretation
    const Json::Value& colorTableJson = colorPaletteJson.get(COLOR_TABLE, Json::Value::null);
    if (!colorTableJson.isNull())
    {
        if (!colorTableJson.isArray())
        {
            LERROR << "parameter '" << COLOR_TABLE << "' has an invalid value, expected an array of arrays.";
            throw r_Error(INVALIDFORMATPARAMETER);
        }
        GDALPaletteInterp paletteInterpGdal = GPI_RGB;
        if (colorPaletteJson.isMember(PALETTE_INTERP))
        {
            paletteInterpGdal = getPaletteInterp(colorPaletteJson[PALETTE_INTERP].asString());
        }
        GDALColorTable gdalColorTable(paletteInterpGdal);
        GDALColorEntry gdalColorEntry;

        for (Json::ArrayIndex i = 0; i < colorTableJson.size(); i++)
        {
            const Json::Value& colorTableEntry = colorTableJson[i];
            if (!colorTableEntry.isArray())
            {
                LERROR << "parameter '" << COLOR_TABLE << "' has an invalid value, expected an array of short values.";
                throw r_Error(INVALIDFORMATPARAMETER);
            }
            else
            {
                unsigned int colorTableEntryValuesCount = colorTableEntry.size();
                for (Json::ArrayIndex j = 0; j < colorTableEntryValuesCount; j++)
                {
                    if (!colorTableEntry[j].isInt())
                    {
                        LERROR << "color entry value '" << colorTableEntry[j].asString() << "' at index [" << i << ", " << j << "] is not a short value.";
                        throw r_Error(INVALIDFORMATPARAMETER);
                    }
                    short value = colorTableEntry[j].asInt();
                    switch (j)
                    {
                    case 0:
                        gdalColorEntry.c1 = value;
                        break;
                    case 1:
                        gdalColorEntry.c2 = value;
                        break;
                    case 2:
                        gdalColorEntry.c3 = value;
                        break;
                    case 3:
                        gdalColorEntry.c4 = value;
                        break;
                    default:
                        LERROR << "color entry has more than four values.";
                        throw r_Error(INVALIDFORMATPARAMETER);
                    }
                }
            }
            gdalColorTable.SetColorEntry((int) i, (const GDALColorEntry*) &gdalColorEntry);
        }

        // set values
        for (int i = 1; i <= poDataset->GetRasterCount(); i++)
        {
            if (poDataset->GetRasterBand(i)->SetColorTable(&gdalColorTable) != CE_None)
            {
                LWARNING << "failed setting color table to band " << i << ", reason: " << CPLGetLastErrorMsg();
            }
        }
    }

    // color interpretation
    const Json::Value& colorInterpJson = colorPaletteJson.get(COLOR_INTERP, Json::Value::null);
    if (!colorInterpJson.isNull())
    {
        if (!colorInterpJson.isArray() || colorInterpJson.size() != static_cast<unsigned int>(poDataset->GetRasterCount()))
        {
            LERROR << "parameter '" << COLOR_INTERP << " has to be an array of " << poDataset->GetRasterCount() << " strings.";
            throw r_Error(INVALIDFORMATPARAMETER);
        }
        for (int i = 1; i <= poDataset->GetRasterCount(); i++)
        {
            const string& val = colorInterpJson[i - 1].asString();
            GDALColorInterp colorInterpGdal = GDALGetColorInterpretationByName(val.c_str());
            if (poDataset->GetRasterBand(i)->SetColorInterpretation(colorInterpGdal) != CE_None)
            {
                LWARNING << "failed setting color interpretation to band " << i << ", reason: " << CPLGetLastErrorMsg();
            }
        }
    }
}

GDALPaletteInterp r_Conv_GDAL::getPaletteInterp(const std::string& paletteInterpVal)
{
    if (paletteInterpVal == PALETTE_INTERP_VAL_GRAY)
    {
        return GPI_Gray;
    }
    else if (paletteInterpVal == PALETTE_INTERP_VAL_RGB)
    {
        return GPI_RGB;
    }
    else if (paletteInterpVal == PALETTE_INTERP_VAL_CMYK)
    {
        return GPI_CMYK;
    }
    else if (paletteInterpVal == PALETTE_INTERP_VAL_HLS)
    {
        return GPI_HLS;
    }
    else
    {
        LERROR << "parameter '" << PALETTE_INTERP << "' has an invalid value '" << paletteInterpVal <<
               "', expected one of: " << PALETTE_INTERP_VAL_GRAY << ", " << PALETTE_INTERP_VAL_RGB << ", " <<
               PALETTE_INTERP_VAL_CMYK << " or " << PALETTE_INTERP_VAL_HLS;
        throw r_Error(INVALIDFORMATPARAMETER);
    }
}

void
r_Conv_GDAL::setConfigOptions()
{
    for (const pair<string, string>& configOption : formatParams.getConfigOptions())
    {
        CPLSetConfigOption(configOption.first.c_str(), configOption.second.c_str());
    }
}

void
r_Conv_GDAL::getFormatParameters(CPLStringList& stringList, r_Primitive_Type* rasBandType)
{
    for (const pair<string, string>& formatParameter : formatParams.getFormatParameters())
    {
        stringList.AddNameValue(formatParameter.first.c_str(), formatParameter.second.c_str());
    }

    auto formatLower = format;
    boost::algorithm::to_lower(formatLower);
    if (formatLower == PNG_FORMAT && rasBandType->type_id() != r_Type::BOOL)
    {
        // The default compression level for PNG (ZLEVEL) is 6. This level takes too
        // long while not reducing the size too much for typical imagery (except for boolean images).
        // So here the ZLEVEL is set to 2 if the user doesn't explicitly specify it.
        bool pngCompressionSet = false;
        for (const pair<string, string>& formatParameter : formatParams.getFormatParameters())
        {
            if (formatParameter.first == PNG_COMPRESSION_PARAM)
            {
                pngCompressionSet = true;
                break;
            }
        }
        if (!pngCompressionSet)
        {
            stringList.AddNameValue(PNG_COMPRESSION_PARAM.c_str(), PNG_DEFAULT_ZLEVEL.c_str());
        }
    }
}

double
r_Conv_GDAL::getDouble(const string& paramName, const CPLStringList& paramsList)
{
    double ret = numeric_limits<double>::max();
    if (paramsList.FindName(paramName.c_str()) != -1)
    {
        const char* paramValue =  paramsList.FetchNameValue(paramName.c_str());
        try
        {
            ret = boost::lexical_cast<double>(paramValue);
        }
        catch (...)
        {
            LWARNING << "parameter '" << paramName << "' has an invalid double value '" << paramValue << "'.";
        }
    }
    return ret;
}

string
r_Conv_GDAL::getString(const string& paramName, const CPLStringList& paramsList)
{
    string ret{""};
    if (paramsList.FindName(paramName.c_str()) != -1)
    {
        ret = string{paramsList.FetchNameValue(paramName.c_str())};
    }
    return ret;
}

#else // HAVE_GDAL

r_Conv_Desc& r_Conv_GDAL::convertFrom(const char* options)
{
    LERROR << "support for decoding with GDAL is not enabled; rasdaman should be configured with option --with-gdal to enable it.";
    throw r_Error(r_Error::r_Error_FeatureNotSupported);
}

r_Conv_Desc& r_Conv_GDAL::convertFrom(r_Format_Params options)
{
    LERROR << "support for decoding with GDAL is not enabled; rasdaman should be configured with option --with-gdal to enable it.";
    throw r_Error(r_Error::r_Error_FeatureNotSupported);
}

r_Conv_Desc& r_Conv_GDAL::convertTo(const char* options)
{
    LERROR << "support for encoding with GDAL is not enabled; rasdaman should be configured with option --with-gdal to enable it.";
    throw r_Error(r_Error::r_Error_FeatureNotSupported);
}

#endif // HAVE_GDAL

/// cloning

r_Convertor* r_Conv_GDAL::clone(void) const
{
    return new r_Conv_GDAL(desc.src, desc.srcInterv, desc.baseType);
}

/// identification

const char* r_Conv_GDAL::get_name(void) const
{
    return format_name_gdal;
}

r_Data_Format r_Conv_GDAL::get_data_format(void) const
{
    return r_GDAL;
}
