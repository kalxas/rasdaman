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

#include "conversion/gdal.hh"
#include "conversion/tmpfile.hh"
#include "conversion/convutil.hh"
#include "conversion/mimetypes.hh"
#include "conversion/formatparamkeys.hh"
#include "conversion/transpose.hh"
#include "conversion/colormap.hh"
#include "raslib/error.hh"
#include "raslib/parseparams.hh"
#include "raslib/primitivetype.hh"
#include "raslib/attribute.hh"
#include "raslib/structuretype.hh"
#include "raslib/odmgtypes.hh"
#include "mymalloc/mymalloc.h"
#include "config.h"

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
#define DATA_CHUNK_SIZE 10000  //no of bytes to be written in a file
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

r_Conv_GDAL::r_Conv_GDAL(const char *src, const r_Minterval &interv, const r_Type *tp)
    : r_Convert_Memory(src, interv, tp, true)
{
    // GDALAllRegister() needs to be executed only once:
    // static variable with block scope is a clean and fast way to do this
    static bool init = []()
    {
        GDALAllRegister();
        CPLSetErrorHandler(customGdalErrorHandler);
        return true;
    }();
}

/// constructor using convert_type_e shortcut

r_Conv_GDAL::r_Conv_GDAL(const char *src, const r_Minterval &interv, int tp)
    : r_Convert_Memory(src, interv, tp)
{
    // GDALAllRegister() needs to be executed only once:
    // static variable with block scope is a clean and fast way to do this
    static bool init = []()
    {
        GDALAllRegister();
        CPLSetErrorHandler(customGdalErrorHandler);
        return true;
    }();
}

/// destructor

r_Conv_GDAL::~r_Conv_GDAL(void)
{
#ifdef HAVE_GDAL
    if (poDataset != NULL)
    {
        GDALClose(poDataset);
        poDataset = NULL;
    }
    if (colorMapEvaluated)
    {
        // Deleting the image data allocated during color mapping
        delete[] desc.src;
        desc.src = NULL;

        // Deleting allocated data caused by r_Convertor::get_external_type
        delete desc.srcType;
        desc.srcType = NULL;
    }
#endif
}

#ifdef HAVE_GDAL

r_Conv_Desc &r_Conv_GDAL::convertTo(const char *options,
                                    const r_Range *nullValue)
{
    if (format.empty())
    {
        throw r_Error(r_Error::r_Error_Conversion, "no format specified");
    }
    if (r_MimeTypes::isMimeType(format))
        format = r_MimeTypes::getFormatName(format);

    if (options)
        initEncodeParams(string{options});

    updateNodataValue(nullValue);

    //if selected, transposes rasdaman data before converting to gdal
    if (formatParams.isTranspose())
    {
        transpose(const_cast<char *>(desc.src), desc.srcInterv, desc.srcType,
                  formatParams.getTranspose());
    }

    if (formatParams.isColorMap())
    {
        auto coloredSrc = formatParams.colorMapTable.applyColorMap(
            desc.srcType, desc.src, desc.srcInterv, desc.baseType);
        desc.src = reinterpret_cast<const char *>(coloredSrc.release());
        desc.srcType = r_Convertor::get_external_type(desc.baseType);
        colorMapEvaluated = true;
    }

    GDALDriver *driver = GetGDALDriverManager()->GetDriverByName(format.c_str());
    if (driver == NULL)
    {
        throw r_Error(r_Error::r_Error_Conversion, "unsupported format " + format);
    }

    auto imageSize = getImageSize();
    unsigned int width = imageSize.first;
    unsigned int height = imageSize.second;

    unsigned int numBands = ConvUtil::getNumberOfBands(desc.srcType);
    std::unique_ptr<r_Primitive_Type> rasBandType;
    rasBandType.reset(getBandType(desc.srcType));
    GDALDataType gdalBandType = ConvUtil::rasTypeToGdalType(rasBandType.get());

    GDALDriver *hMemDriver = static_cast<GDALDriver *>(GDALGetDriverByName("MEM"));
    if (hMemDriver == NULL)
    {
        std::string err{"failed initializing the GDAL driver, "};
        err += CPLGetLastErrorMsg();
        throw r_Error(r_Error::r_Error_Conversion, err);
    }
    poDataset = hMemDriver->Create("in_memory_image", static_cast<int>(width), static_cast<int>(height),
                                   static_cast<int>(numBands), gdalBandType, NULL);
    if (poDataset == NULL)
    {
        std::string err{"failed creating in memory GDAL dataset, "};
        err += CPLGetLastErrorMsg();
        throw r_Error(r_Error::r_Error_Conversion, err);
    }

    r_TmpFile tmpFile;
    encodeImage(gdalBandType, rasBandType.get(), width, height, numBands);
    setEncodeParams();
    CPLStringList formatParameters;
    getFormatParameters(formatParameters, rasBandType.get());
    if (rasBandType.get()->type_id() != r_Type::CHAR && !formatParams.getParams().get(COLOR_PALETTE, Json::Value::null).isNull())
    {
        LERROR << "MDD has a non-char cell type, cannot apply color palette table.";
        throw r_Error(COLORPALETTEFORNONCHAR);
    }
    string tmpFilePath = tmpFile.getFileName();
    GDALDataset *gdalResult = driver->CreateCopy(tmpFilePath.c_str(), poDataset, FALSE, formatParameters.List(), NULL, NULL);
    if (!gdalResult)
    {
        std::string err{"failed encoding to format "};
        err += format;
        err += ", ";
        err += CPLGetLastErrorMsg();
        throw r_Error(r_Error::r_Error_Conversion, err);
    }
    GDALClose(gdalResult);

    long fileSize = 0;
    desc.dest = tmpFile.readData(fileSize);
    desc.destInterv = r_Minterval(1) << r_Sinterval(static_cast<r_Range>(0),
                                                    static_cast<r_Range>(fileSize) - 1);
    desc.destType = r_Type::get_any_type("char");

    return desc;
}

void r_Conv_GDAL::encodeImage(GDALDataType gdalBandType, r_Primitive_Type *rasBandType,
                              unsigned int width, unsigned int height, unsigned int numBands)
{
    bool isBoolean = rasBandType->type_id() == r_Type::BOOL;
    switch (gdalBandType)
    {
    case GDT_Byte:
    {
        encodeImage<r_Char>(gdalBandType, isBoolean, width, height, numBands, false);
        if (rasBandType->type_id() == r_Type::OCTET)
        {
            for (int i = 1; i <= int(numBands); ++i)
            {
                poDataset->GetRasterBand(i)->SetMetadataItem(GDAL_KEY_PIXELTYPE.c_str(),
                                                             GDAL_VAL_SIGNEDBYTE.c_str(),
                                                             GDAL_KEY_IMAGE_STRUCTURE.c_str());
            }
        }
        break;
    }
    case GDT_UInt16:
    {
        encodeImage<r_UShort>(gdalBandType, isBoolean, width, height, numBands, false);
        break;
    }
    case GDT_Int16:
    {
        encodeImage<r_Short>(gdalBandType, isBoolean, width, height, numBands, false);
        break;
    }
    case GDT_UInt32:
    {
        encodeImage<r_ULong>(gdalBandType, isBoolean, width, height, numBands, false);
        break;
    }
    case GDT_Int32:
    {
        encodeImage<r_Long>(gdalBandType, isBoolean, width, height, numBands, false);
        break;
    }
    case GDT_Float32:
    {
        encodeImage<r_Float>(gdalBandType, isBoolean, width, height, numBands, false);
        break;
    }
    case GDT_Float64:
    {
        encodeImage<r_Double>(gdalBandType, isBoolean, width, height, numBands, false);
        break;
    }
    case GDT_CFloat32:
    {
        encodeImage<r_Float>(gdalBandType, isBoolean, 2 * width, height, numBands, true);
        break;
    }
    case GDT_CFloat64:
    {
        encodeImage<r_Double>(gdalBandType, isBoolean, 2 * width, height, numBands, true);
        break;
    }
    case GDT_CInt16:
    {
        encodeImage<r_Short>(gdalBandType, isBoolean, 2 * width, height, numBands, true);
        break;
    }
    case GDT_CInt32:
    {
        encodeImage<r_Long>(gdalBandType, isBoolean, 2 * width, height, numBands, true);
        break;
    }
    default:
    {
        std::string err{"unsupported base type "};
        err += rasBandType->name();
        throw r_Error(r_Error::r_Error_Conversion, err);
    }
    }
}

template <typename T>
void r_Conv_GDAL::encodeImage(GDALDataType gdalBandType, bool isBoolean,
                              unsigned int width, unsigned int height, unsigned int numBands, bool isComplex)
{
    size_t area = static_cast<size_t>(width) * static_cast<size_t>(height);
    unique_ptr<T[]> dstCells;
    dstCells.reset(new (nothrow) T[area]);
    if (!dstCells)
    {
        LERROR << "failed allocating " << area << " bytes of memory.";
        throw r_Error(r_Error::r_Error_MemoryAllocation);
    }

    unsigned int col_offset;
    for (unsigned int band = 0; band < numBands; band++)
    {
        T *dst = dstCells.get();
        T *src = (reinterpret_cast<T *>(const_cast<char *>(desc.src))) + band;

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

        if (isComplex)
            width /= 2;

        CPLErr error = poDataset->GetRasterBand((int)(band + 1))->RasterIO(GF_Write, 0, 0, (int)width, (int)height, (char *)dstCells.get(), (int)width, (int)height, gdalBandType, 0, 0);
        if (error != CE_None)
        {
            std::string err{"failed writing data to GDAL raster band "};
            err += std::to_string(band + 1);
            err += ", ";
            err += CPLGetLastErrorMsg();
            throw r_Error(r_Error::r_Error_Conversion, err);
        }
    }
    dstCells.reset();
}

pair<unsigned int, unsigned int> r_Conv_GDAL::getImageSize()
{
    if (desc.srcInterv.dimension() != 2)
    {
        std::string err{"cannot encode "};
        err += std::to_string(desc.srcInterv.dimension());
        err += "D array with the GDAL library, only 2D is supported";
        throw r_Error(r_Error::r_Error_Conversion, err);
    }
    r_Range w = r_Range(desc.srcInterv[0].get_extent());
    r_Range h = r_Range(desc.srcInterv[1].get_extent());
    if (w > numeric_limits<int>::max() || h > numeric_limits<int>::max())
    {
        std::stringstream err;
        err << "cannot encode array of size " << w << " x " << h << ", "
            << "maximum support size by GDAL is " << numeric_limits<int>::max() << " x " << numeric_limits<int>::max();
        throw r_Error(r_Error::r_Error_Conversion, err.str());
    }
    return make_pair((unsigned int)w, (unsigned int)h);
}

r_Conv_Desc &r_Conv_GDAL::convertFrom(const char *options)
{
    if (options)
    {
        formatParams.parse(string{options});
        setConfigOptions();
    }
    return this->convertFrom(formatParams);
}

r_Conv_Desc &r_Conv_GDAL::convertFrom(r_Format_Params options)
{
    formatParams = options;
    string tmpFilePath("");
    r_TmpFile tmpFileObj;
    if (formatParams.getFilePaths().empty())
    {
        tmpFileObj.writeData(desc.src, (size_t)desc.srcInterv.cell_count());
        tmpFilePath = tmpFileObj.getFileName();
    }
    else
    {
        tmpFilePath = formatParams.getFilePath();
    }

#if GDAL_VERSION_MAJOR >= 2
    // Open dataset with GDALOpenEx, only available since GDAL 2.0
    // https://gdal.org/api/raster_c_api.html#_CPPv410GDALOpenExPKcjPPCKcPPCKcPPCKc
    unsigned nOpenFlags = GDAL_OF_RASTER | GDAL_OF_SHARED;
    auto openOptions = getOpenOptions();
    char **papszOpenOptions = openOptions.Count() > 0 ? openOptions.List() : NULL;
    char **papszAllowedDrivers = NULL;  // all drivers
    char **papszSiblingFiles = NULL;    // find them automatically
    poDataset = static_cast<GDALDataset *>(GDALOpenEx(tmpFilePath.c_str(),
                                                      nOpenFlags,
                                                      papszAllowedDrivers,
                                                      papszOpenOptions,
                                                      papszSiblingFiles));
#else
    // Use 1.x GDALOpen
    poDataset = static_cast<GDALDataset *>(GDALOpen(tmpFilePath.c_str(), GA_ReadOnly));
#endif

    if (poDataset == NULL)
    {
        std::string err{"failed opening file with GDAL"};
        std::string details = CPLGetLastErrorMsg();
        if (!details.empty())
            err += ", " + details;
        throw r_Error(r_Error::r_Error_Conversion, err);
    }

    bandIds = getBandIds();
    desc.destType = ConvUtil::gdalTypeToRasType(poDataset, bandIds);
    setTargetDomain();
    desc.dest = decodeImage();

    // if selected, transposes rasdaman data after converting from netcdf
    // if (formatParams.isTranspose())
    // {
    //     LDEBUG << "transposing decoded data of sdom: " << desc.destInterv;
    //     transpose(desc.dest, desc.destInterv, (const r_Type*) desc.destType,
    //               formatParams.getTranspose());
    // }

    return desc;
}

char *r_Conv_GDAL::decodeImage()
{
    assert(desc.destType);

    const auto &a = formatParams.isTranspose() ? desc.destInterv[1] : desc.destInterv[0];
    const auto &b = formatParams.isTranspose() ? desc.destInterv[0] : desc.destInterv[1];
    int width = a.get_extent();
    int height = b.get_extent();
    int offsetX = a.low();
    int offsetY = b.low();

    size_t tileBaseTypeSize = static_cast<size_t>(((r_Base_Type *)desc.destType)->size());
    size_t dataSize = static_cast<size_t>(width) * static_cast<size_t>(height) * tileBaseTypeSize;
    LTRACE << "allocating tile cells of size " << dataSize;
    char *tileCells RAS_ALIGNED = (char *)mymalloc(dataSize);
    if (tileCells == NULL)
    {
        throw r_Error(r_Error::r_Error_MemoryAllocation,
                      std::to_string(dataSize) + " bytes for decoding input file");
    }

    // copy data from all GDAL bands to rasdaman
    size_t bandOffset = 0;
    size_t bandSize = 0;
    char *bandCells RAS_ALIGNED = NULL;
    int bandIndex = 0;
    for (int bandId: bandIds)
    {
        size_t bandBaseTypeSize = ConvUtil::getBandBaseTypeSize(desc.destType, bandIndex);
        size_t newBandSize = static_cast<size_t>(width) * static_cast<size_t>(height) * bandBaseTypeSize;
        LTRACE << "allocating band cells of size " << newBandSize;
        bandCells = upsizeBufferIfNeeded(bandCells, bandSize, newBandSize);

        GDALRasterBand *gdalBand = poDataset->GetRasterBand(bandId + 1);
        GDALDataType bandType = gdalBand->GetRasterDataType();
        CPLErr error = gdalBand->RasterIO(GF_Read, offsetX, offsetY, width, height,
                                          (void *)bandCells, width, height, bandType, 0, 0);
        if (error != CE_None)
        {
            free(bandCells);
            bandCells = NULL;
            free(tileCells);
            tileCells = NULL;

            std::string err{"failed decoding band "};
            err += std::to_string(bandId);
            err += ", ";
            err += CPLGetLastErrorMsg();
            throw r_Error(r_Error::r_Error_Conversion, err);
        }

        bool signedByte = false;
        const char *pixelType = gdalBand->GetMetadataItem(GDAL_KEY_PIXELTYPE.c_str(), GDAL_KEY_IMAGE_STRUCTURE.c_str());
        if (pixelType)
            signedByte = string{pixelType} == GDAL_VAL_SIGNEDBYTE;

        decodeBand(bandCells, tileCells + bandOffset, tileBaseTypeSize, width, height, bandType, signedByte);
        bandOffset += bandBaseTypeSize;
        ++bandIndex;
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
        LDEBUG << "Image to be decoded will be subsetted to sdom " << desc.destInterv.to_string();
    }
    else if (subsetDomain.dimension() != 0)
    {
        std::stringstream s;
        s << "invalid 'subsetDomain' parameter '" << subsetDomain
          << "', the GDAL convertor supports only 2D subsets.";
        throw r_Error(r_Error::r_Error_Conversion, s.str());
    }
    else
    {
        desc.destInterv = r_Minterval(2)
                          << r_Sinterval(0l, r_Range(poDataset->GetRasterXSize()) - 1)
                          << r_Sinterval(0l, r_Range(poDataset->GetRasterYSize()) - 1);
        LDEBUG << "Image to be decoded has sdom " << desc.destInterv.to_string();
    }
    if (transpose && formatParams.isTranspose())
    {
        desc.destInterv.swap_dimensions(0, 1);
    }
}

char *r_Conv_GDAL::upsizeBufferIfNeeded(char *buffer, size_t &bufferSize, size_t newBufferSize)
{
    if (newBufferSize > bufferSize)
    {
        if (buffer)
        {
            free(buffer);
            buffer = NULL;
        }
        buffer = (char *)mymalloc(newBufferSize);
        if (buffer == NULL)
        {
            throw r_Error(r_Error::r_Error_MemoryAllocation,
                          std::to_string(newBufferSize) + " bytes for decoding a single band");
        }
        bufferSize = newBufferSize;
    }
    return buffer;
}

void r_Conv_GDAL::decodeBand(const char *bandCells, char *tileCells, size_t tileBaseTypeSize, int w, int h,
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
    case GDT_CFloat32:
    {
        transposeBand<r_Float>(bandCells, tileCells, tileBaseTypeSize / 2, 2 * width, height);
        break;
    }
    case GDT_CFloat64:
    {
        transposeBand<r_Double>(bandCells, tileCells, tileBaseTypeSize / 2, 2 * width, height);
        break;
    }
    case GDT_CInt16:
    {
        transposeBand<r_Short>(bandCells, tileCells, tileBaseTypeSize / 2, 2 * width, height);
        break;
    }
    case GDT_CInt32:
    {
        transposeBand<r_Long>(bandCells, tileCells, tileBaseTypeSize / 2, 2 * width, height);
        break;
    }
    default:
        throw r_Error(r_Error::r_Error_FeatureNotSupported);
    }
}

template <typename T>
void r_Conv_GDAL::transposeBand(const char *__restrict__ srcIn,
                                char *__restrict__ dst, size_t tileBaseTypeSize,
                                size_t width, size_t height)
{
    const T *__restrict__ src RAS_ALIGNED = reinterpret_cast<const T *>(srcIn);

    if (!formatParams.isTranspose())
    {
        LTRACE << "transposing " << width << " x " << height << " matrix...";
        if (bandIds.size() > 1)
        {
            for (size_t col = 0; col < width; ++col)
                for (size_t row = 0; row < height; ++row, dst += tileBaseTypeSize)
                    *reinterpret_cast<T *>(dst) = src[row * width + col];
        }
        else
        {
            // single band optimization
            T *__restrict__ dstT RAS_ALIGNED = reinterpret_cast<T *>(dst);
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
                *reinterpret_cast<T *>(dst) = src[i];
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
        noOfBands = (int)ret.size();
        for (int i = 0; i < noOfBands; i++)
        {
            int bandId = ret[(size_t)i];
            if (bandId < 0 || bandId >= poDataset->GetRasterCount())
            {
                std::stringstream s;
                s << "band id '" << bandId << "' out of range 0 - " << (poDataset->GetRasterCount() - 1);
                throw r_Error(r_Error::r_Error_Conversion, s.str());
            }
        }
    }
    return ret;
}

r_Primitive_Type *r_Conv_GDAL::getBandType(const r_Type *baseType)
{
    r_Primitive_Type *ret = NULL;
    if (baseType->isPrimitiveType())  // = one band
    {
        ret = static_cast<r_Primitive_Type *>(baseType->clone());
    }
    else if (baseType->isStructType())  // = multiple bands
    {
        const r_Structure_Type *structType = static_cast<const r_Structure_Type *>(baseType);
        for (const auto &att: structType->getAttributes())
        {
            // check the band types, they have to be of the same type
            if (att.type_of().isPrimitiveType())
            {
                const auto pt = static_cast<const r_Primitive_Type &>(att.type_of());
                if (ret != NULL)
                {
                    if (ret->type_id() != pt.type_id())
                    {
                        throw r_Error(r_Error::r_Error_Conversion,
                                      "GDAL driver can not handle bands of different types");
                    }
                }
                else
                {
                    ret = static_cast<r_Primitive_Type *>(pt.clone());
                }
            }
            else
            {
                throw r_Error(r_Error::r_Error_Conversion,
                              "GDAL driver can not handle composite bands");
            }
        }
    }
    return ret;
}

void r_Conv_GDAL::initEncodeParams(const string &paramsIn)
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
        const string &kvPairs = getString(FormatParamKeys::General::CONFIG_OPTIONS_LEGACY, paramsList);
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

void r_Conv_GDAL::setEncodeParams()
{
    setMetadata();
    setNodata();
    setGeoreference();
    setColorPalette();
}

void r_Conv_GDAL::setMetadata()
{
    if (!formatParams.getMetadata().empty())
    {
        if (poDataset->SetMetadataItem(GDAL_KEY_METADATA.c_str(),
                                       formatParams.getMetadata().c_str()) != CE_None)
        {
            LWARNING << "failed setting metadata '" << formatParams.getMetadata() << "', error: " << CPLGetLastErrorMsg();
        }
    }
    else if (!formatParams.getMetadataKeyValues().empty())
    {
        for (const pair<string, string> &kv: formatParams.getMetadataKeyValues())
        {
            if (poDataset->SetMetadataItem(kv.first.c_str(), kv.second.c_str()) != CE_None)
            {
                LWARNING << "failed setting metadata key '" << kv.first << "' to value '" << kv.second << "', error: " << CPLGetLastErrorMsg();
            }
        }
    }
}

void r_Conv_GDAL::setNodata()
{
    const vector<double> &nodata = formatParams.getNodata();
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
                LWARNING << "failed setting nodata, number of nodata values (" << nodata.size() << ") doesn't match the number of bands (" << nBands << ").";
                break;
            }
        }
        string nodataValuesStr = nodataValues.str();
        poDataset->SetMetadataItem(GDAL_KEY_NODATA_VALUES.c_str(), nodataValuesStr.c_str());
    }
}

void r_Conv_GDAL::setGeoreference()
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
        const Json::Value &geoRefJson = formatParams.getParams().get(FormatParamKeys::Encode::GEO_REFERENCE, Json::Value::null);
        if (!geoRefJson.isNull())
        {
            if (geoRefJson.isMember(GCPS))
            {
                setGCPs(geoRefJson[GCPS]);
            }
        }
    }
}

void r_Conv_GDAL::setGeotransform()
{
    double adfGeoTransform[6];
    adfGeoTransform[0] = formatParams.getXmin();
    adfGeoTransform[1] = (formatParams.getXmax() - formatParams.getXmin()) / poDataset->GetRasterXSize();
    adfGeoTransform[2] = 0.0;
    adfGeoTransform[3] = formatParams.getYmax();
    adfGeoTransform[4] = 0.0;
    adfGeoTransform[5] = -(formatParams.getYmax() - formatParams.getYmin()) / poDataset->GetRasterYSize();
    poDataset->SetGeoTransform(adfGeoTransform);

    const string &wktStr = getCrsWkt();
    if (!wktStr.empty())
    {
        if (poDataset->SetProjection(wktStr.c_str()) == CE_Failure)
        {
            LWARNING << "invalid CRS projection '" << formatParams.getCrs() << "', error: " << CPLGetLastErrorMsg();
        }
    }
}

void r_Conv_GDAL::setGCPs(const Json::Value &gcpsJson)
{
    const string &key = GCPS;
    if (gcpsJson.isArray())
    {
        unsigned int gcpCount = gcpsJson.size();
        unique_ptr<GDAL_GCP[]> gdalGcpsPtr(new GDAL_GCP[gcpCount]);
        GDAL_GCP *gdalGcps = gdalGcpsPtr.get();

        for (Json::ArrayIndex i = 0; i < gcpCount; i++)
        {
            const Json::Value &gcp = gcpsJson[i];
            if (!gcp.isObject())
            {
                std::stringstream s;
                s << "parameter '" << key << "' has an invalid value, expected an array of GCP objects";
                throw r_Error(r_Error::r_Error_Conversion, s.str());
            }
            else
            {
                gdalGcps[i].dfGCPZ = 0;  // optional, zero by default
                gdalGcps[i].pszId = NULL;
                gdalGcps[i].pszInfo = NULL;
                for (const string &fkey: gcp.getMemberNames())
                {
                    if (fkey == GCP_ID)
                    {
                        gdalGcps[i].pszId = strdup(gcp[fkey].asCString());
                    }
                    else if (fkey == GCP_INFO)
                    {
                        gdalGcps[i].pszInfo = strdup(gcp[fkey].asCString());
                    }
                    else
                    {
                        if (!gcp[fkey].isDouble())
                        {
                            std::stringstream s;
                            s << "parameter '" << key << "." << fkey << "' has an invalid value, expected double";
                            throw r_Error(r_Error::r_Error_Conversion, s.str());
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
                    gdalGcps[i].pszId = strdup(boost::lexical_cast<string>(i).c_str());
                }
            }
        }

        string wktStr = getCrsWkt();
        if (poDataset->SetGCPs((int)gcpCount, gdalGcps, wktStr.c_str()) != CE_None)
        {
            LWARNING << "failed setting GCPs, reason: " << CPLGetLastErrorMsg();
        }
    }
    else
    {
        std::stringstream s;
        s << "parameter '" << key << "' has an invalid value, expected an array of GCP objects";
        throw r_Error(r_Error::r_Error_Conversion, s.str());
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
            char *wkt = NULL;
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

void r_Conv_GDAL::setColorPalette()
{
    const Json::Value &colorPaletteJson = formatParams.getParams().get(COLOR_PALETTE, Json::Value::null);
    if (colorPaletteJson.isNull())
    {
        return;
    }
    if (!colorPaletteJson.isObject())
    {
        std::stringstream s;
        s << "parameter '" << COLOR_PALETTE << "' has an invalid value, expected an object "
          << "containing palette interpretation, color interpretation, color table.";
        throw r_Error(r_Error::r_Error_Conversion, s.str());
    }

    // color table and palette interpretation
    const Json::Value &colorTableJson = colorPaletteJson.get(COLOR_TABLE, Json::Value::null);
    if (!colorTableJson.isNull())
    {
        if (!colorTableJson.isArray())
        {
            std::stringstream s;
            s << "parameter '" << COLOR_TABLE << "' has an invalid value, expected an array of arrays";
            throw r_Error(r_Error::r_Error_Conversion, s.str());
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
            const Json::Value &colorTableEntry = colorTableJson[i];
            if (!colorTableEntry.isArray())
            {
                std::stringstream s;
                s << "parameter '" << COLOR_TABLE << "' has an invalid value, expected an array of short values";
                throw r_Error(r_Error::r_Error_Conversion, s.str());
            }
            else
            {
                unsigned int colorTableEntryValuesCount = colorTableEntry.size();
                for (Json::ArrayIndex j = 0; j < colorTableEntryValuesCount; j++)
                {
                    if (!colorTableEntry[j].isInt())
                    {
                        std::stringstream s;
                        s << "color entry value '" << colorTableEntry[j].asString()
                          << "' at index [" << i << ", " << j << "] is not a short value";
                        throw r_Error(r_Error::r_Error_Conversion, s.str());
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
                        throw r_Error(r_Error::r_Error_Conversion, "color entry has more than four values.");
                    }
                }
            }
            gdalColorTable.SetColorEntry((int)i, (const GDALColorEntry *)&gdalColorEntry);
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
    const Json::Value &colorInterpJson = colorPaletteJson.get(COLOR_INTERP, Json::Value::null);
    if (!colorInterpJson.isNull())
    {
        if (!colorInterpJson.isArray() || colorInterpJson.size() != static_cast<unsigned int>(poDataset->GetRasterCount()))
        {
            std::stringstream s;
            s << "parameter '" << COLOR_INTERP << " has to be an array of "
              << poDataset->GetRasterCount() << " strings.";
            throw r_Error(r_Error::r_Error_Conversion, s.str());
        }
        for (int i = 1; i <= poDataset->GetRasterCount(); i++)
        {
            const string &val = colorInterpJson[i - 1].asString();
            GDALColorInterp colorInterpGdal = GDALGetColorInterpretationByName(val.c_str());
            if (poDataset->GetRasterBand(i)->SetColorInterpretation(colorInterpGdal) != CE_None)
            {
                LWARNING << "failed setting color interpretation to band " << i << ", reason: " << CPLGetLastErrorMsg();
            }
        }
    }
}

GDALPaletteInterp r_Conv_GDAL::getPaletteInterp(const std::string &paletteInterpVal)
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
        std::stringstream s;
        s << "parameter '" << PALETTE_INTERP << "' has an invalid value '" << paletteInterpVal << "', expected one of: " << PALETTE_INTERP_VAL_GRAY << ", " << PALETTE_INTERP_VAL_RGB << ", " << PALETTE_INTERP_VAL_CMYK << " or " << PALETTE_INTERP_VAL_HLS;
        throw r_Error(r_Error::r_Error_Conversion, s.str());
    }
}

void r_Conv_GDAL::setConfigOptions()
{
    for (const pair<string, string> &configOption: formatParams.getConfigOptions())
    {
        CPLSetConfigOption(configOption.first.c_str(), configOption.second.c_str());
    }
}

void r_Conv_GDAL::getFormatParameters(CPLStringList &stringList, r_Primitive_Type *rasBandType)
{
    for (const pair<string, string> &p: formatParams.getFormatParameters())
    {
        stringList.AddNameValue(p.first.c_str(), p.second.c_str());
    }

    auto formatLower = format;
    boost::algorithm::to_lower(formatLower);
    if (formatLower == PNG_FORMAT && rasBandType->type_id() != r_Type::BOOL)
    {
        // The default compression level for PNG (ZLEVEL) is 6. This level takes too
        // long while not reducing the size too much for typical imagery (except for boolean images).
        // So here the ZLEVEL is set to 2 if the user doesn't explicitly specify it.
        bool pngCompressionSet = false;
        for (const pair<string, string> &formatParameter: formatParams.getFormatParameters())
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

CPLStringList r_Conv_GDAL::getOpenOptions() const
{
    LDEBUG << "Parsing GDAL open options";
    CPLStringList stringList;
    for (const pair<string, string> &p: formatParams.getOpenOptions())
    {
        LDEBUG << "GDAL open option: " << p.first << " = " << p.second;
        stringList.AddNameValue(p.first.c_str(), p.second.c_str());
    }
    return stringList;
}

double
r_Conv_GDAL::getDouble(const string &paramName, const CPLStringList &paramsList)
{
    double ret = numeric_limits<double>::max();
    if (paramsList.FindName(paramName.c_str()) != -1)
    {
        const char *paramValue = paramsList.FetchNameValue(paramName.c_str());
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
r_Conv_GDAL::getString(const string &paramName, const CPLStringList &paramsList)
{
    string ret{""};
    if (paramsList.FindName(paramName.c_str()) != -1)
    {
        ret = string{paramsList.FetchNameValue(paramName.c_str())};
    }
    return ret;
}

#else  // HAVE_GDAL

r_Conv_Desc &r_Conv_GDAL::convertFrom(const char *options)
{
    LERROR << "support for decoding with GDAL is not enabled; rasdaman should be configured with option --with-gdal to enable it.";
    throw r_Error(r_Error::r_Error_FeatureNotSupported);
}

r_Conv_Desc &r_Conv_GDAL::convertFrom(r_Format_Params options)
{
    LERROR << "support for decoding with GDAL is not enabled; rasdaman should be configured with option --with-gdal to enable it.";
    throw r_Error(r_Error::r_Error_FeatureNotSupported);
}

r_Conv_Desc &r_Conv_GDAL::convertTo(const char *options,
                                    const r_Range *nullValue)
{
    LERROR << "support for encoding with GDAL is not enabled; rasdaman should be configured with option --with-gdal to enable it.";
    throw r_Error(r_Error::r_Error_FeatureNotSupported);
}

#endif  // HAVE_GDAL

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
