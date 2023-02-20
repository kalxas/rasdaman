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

#include "conversion/tiff.hh"
#include "conversion/memfs.hh"
#include "conversion/tmpfile.hh"
#include "raslib/error.hh"
#include "raslib/parseparams.hh"
#include "raslib/structuretype.hh"
#include "raslib/primitivetype.hh"
#include "mymalloc/mymalloc.h"
#include <logging.hh>

#include <iostream>
#include <string.h>
#include <sstream>
#ifdef HAVE_TIFF
#include <tiffio.h>
#endif

using namespace std;

const int r_Conv_TIFF::defaultRPS = 32;

const char r_Conv_TIFF::dummyFileFmt[] = "/tmp/%p.tif";

const unsigned int r_Conv_TIFF::TIFF_DEFAULT_QUALITY = 80;

#ifdef HAVE_TIFF
const struct r_Convertor::convert_string_s r_Conv_TIFF::compNames[] =
{
    {"none", COMPRESSION_NONE},
    {"ccittrle", COMPRESSION_CCITTRLE},
    {"ccittfax3", COMPRESSION_CCITTFAX3},
    {"ccittfax4", COMPRESSION_CCITTFAX4},
    {"lzw", COMPRESSION_LZW},
    {"ojpeg", COMPRESSION_OJPEG},
    {"jpeg", COMPRESSION_JPEG},
    {"next", COMPRESSION_NEXT},
    {"ccittrlew", COMPRESSION_CCITTRLEW},
    {"packbits", COMPRESSION_PACKBITS},
    {"thunderscan", COMPRESSION_THUNDERSCAN},
    {"pixarfilm", COMPRESSION_PIXARFILM},
    {"pixarlog", COMPRESSION_PIXARLOG},
    {"deflate", COMPRESSION_DEFLATE},
    {"dcs", COMPRESSION_DCS},
    {"jbig", COMPRESSION_JBIG},
#ifndef LINUX
    {"sgilog", COMPRESSION_SGILOG},
    {"sgilog24", COMPRESSION_SGILOG24},
    {"it8ctpad", COMPRESSION_IT8CTPAD},
    {"it8lw", COMPRESSION_IT8LW},
    {"it8mp", COMPRESSION_IT8MP},
    {"it8bl", COMPRESSION_IT8BL},
#endif
    {NULL, COMPRESSION_NONE}
};

const struct r_Convertor::convert_string_s r_Conv_TIFF::resunitNames[] =
{
    {"none", RESUNIT_NONE},
    {"inch", RESUNIT_INCH},
    {"centimeter", RESUNIT_CENTIMETER},
    {NULL, RESUNIT_NONE}
};
#endif // HAVE_TIFF

// Change these according to the platform!
// Fill order of bits in bitmap mode. Define 0 for LSB, otherwise MSB
#define _R_TIFF_BITFILLORDER    1

// Setup internal macros according to the fill-order
#if (_R_TIFF_BITFILLORDER == 0)
#define _R_TIFF_MASK_VALUE      1
#define _R_TIFF_MASK_SHIFT(x)   (x) <<= 1;
#else
#define _R_TIFF_MASK_VALUE      (1<<7)
#define _R_TIFF_MASK_SHIFT(x)   (x) >>= 1;
#endif

// TIFF class functions

#ifdef HAVE_TIFF
/// Translate string compression type to libtiff compression type
int r_Conv_TIFF::get_compression_from_name(const char* strComp)
{
    unsigned short i = 0;
    int tiffComp = COMPRESSION_NONE;

    if (strComp != NULL)
    {
        for (i = 0; compNames[i].key != NULL; i++)
        {
            if (strcasecmp(compNames[i].key, strComp) == 0)
            {
                tiffComp = compNames[i].id;
                break;
            }
        }
        if (compNames[i].key == NULL)
        {
            LDEBUG << "r_Conv_TIFF::get_compression_from_name(): error: unsupported compression type " << strComp << ".";
            LERROR << "Error: unsupported compression type " << strComp << ".";
        }
    }

    return tiffComp;
}

/// Translate string resolution unit type to libtiff resolution unit type
int r_Conv_TIFF::get_resunit_from_name(const char* strResUnit)
{
    unsigned short i = 0;
    int tiffResUnit = RESUNIT_NONE;
    if (strResUnit != NULL)
    {
        for (i = 0; resunitNames[i].key != NULL; i++)
        {
            if (strcasecmp(resunitNames[i].key, strResUnit) == 0)
            {
                tiffResUnit = resunitNames[i].id;
                break;
            }
        }
        if (resunitNames[i].key == NULL)
        {
            LDEBUG << "r_Conv_TIFF::get_resunit_from_name(): error: unsupported resolution unit type " << strResUnit << ".";
            LERROR << "Error: unsupported resolution unit type " << strResUnit << ".";
        }
    }

    return tiffResUnit;
}
#endif // HAVE_TIFF

/// Capture errors
void TIFFError(__attribute__((unused)) const char* module, const char* fmt, va_list argptr)
{
    char msg[10240];
    vsprintf(msg, fmt, argptr);
    LERROR << "TIFF error: " << msg;
    throw r_Error(r_Error::r_Error_General);
}

/// Capture warnings
void TIFFWarning(__attribute__((unused)) const char* module, const char* fmt, va_list argptr)
{
    char msg[10240];
    vsprintf(msg, fmt, argptr);
    LWARNING << "TIFF warning: " << msg;
}

/// internal initialization, common to all constructors
void r_Conv_TIFF::initTIFF(void)
{
    compType = NULL;
    sampleType = NULL;
    quality = r_Conv_TIFF::TIFF_DEFAULT_QUALITY;
    override_bpp = 0;
    override_bps = 0;
    override_depth = 0;

    if (params == NULL)
    {
        params = new r_Parse_Params();
    }

    params->add("comptype", &compType, r_Parse_Params::param_type_string);
    params->add("quality", &quality, r_Parse_Params::param_type_int);
    params->add("bpp", &override_bpp, r_Parse_Params::param_type_int);
    params->add("bps", &override_bps, r_Parse_Params::param_type_int);
    params->add("depth", &override_depth, r_Parse_Params::param_type_int);
    params->add("sampletype", &sampleType, r_Parse_Params::param_type_string);

#ifdef HAVE_TIFF
    // set our error handlers
    TIFFSetErrorHandler(TIFFError);
    TIFFSetWarningHandler(TIFFWarning);
#endif // HAVE_TIFF
}

/// constructor using type structure
r_Conv_TIFF::r_Conv_TIFF(const char* src, const r_Minterval& interv, const r_Type* tp)
    : r_Convert_Memory(src, interv, tp, true)
{
    initTIFF();
}

/// constructor using int type indicator
r_Conv_TIFF::r_Conv_TIFF(const char* src, const r_Minterval& interv, int type)
    : r_Convert_Memory(src, interv, type)
{
    initTIFF();
}

/// destructor
r_Conv_TIFF::~r_Conv_TIFF(void)
{
    if (compType != NULL)
    {
        delete [] compType;
        compType = NULL;
    }
}

/// convert array to TIFF stream
// Compression modes recommended:
// Bitmap, Greyscales:  COMPRESSION_LZW, COMPRESSION_DEFLATE
// RGB:                 COMPRESSION_JPEG, COMPRESSION_SGILOG24
r_Conv_Desc& r_Conv_TIFF::convertTo(const char* options,
                                    const r_Range* nullValue)
{
#ifdef HAVE_TIFF
    TIFF* tif = NULL;
    char dummyFile[256];
    uint16_t cmap[256];             // Colour map (for greyscale images)
    uint32_t pixelAdd = 0, lineAdd = 0; // number of _bytes_ to add to a pointer
    // to the source data to get the address
    // of the pixel to the right / downwards.
    uint16_t bps = 0, bpp = 0;
    uint32_t width = 0, height = 0, i = 0;
    int tiffcomp = COMPRESSION_NONE;

    int sampleFormat = SAMPLEFORMAT_INT;
    unsigned int spp = 1; // samples per pixel

    params->process(options);
    updateNodataValue(nullValue);

    // translate string compression type to libtiff compression type
    if (compType != NULL)
    {
        tiffcomp = get_compression_from_name(compType);
    }

    // Set dimensions
    width  = static_cast<uint32_t>(desc.srcInterv[0].high() - desc.srcInterv[0].low() + 1);
    height = static_cast<uint32_t>(desc.srcInterv[1].high() - desc.srcInterv[1].low() + 1);

    switch (desc.baseType)
    {
    // MDD arrays are transposed compared to the format needed for images.
    // Therefore the pixelAdd and lineAdd values change places.
    case ctype_bool:
        bps = 1;
        bpp = 1;
        pixelAdd = height;
        lineAdd = 1;
        break;
    case ctype_char:
    case ctype_int8:
    case ctype_uint8:
        bps = 8;
        bpp = 8;
        pixelAdd = height;
        lineAdd = 1;
        break;
    case ctype_rgb:
        bps = 8;
        bpp = 24;
        pixelAdd = 3 * height;
        lineAdd = 3;
        break;
    case ctype_uint16:
    case ctype_int16:
        bps = 16;
        bpp = 16;
        pixelAdd = 2 * height;
        lineAdd = 2;
        break;
    case ctype_int32:
    case ctype_uint32:
    case ctype_float32:
        bps = 32;
        bpp = 32;
        pixelAdd = 4 * height;
        lineAdd = 4;
        break;
    case ctype_int64:
    case ctype_uint64:
    case ctype_float64:
        bps = 64;
        bpp = 64;
        pixelAdd = 8 * height;
        lineAdd = 8;
        break;
    case ctype_struct:
    {
        r_Structure_Type* st = static_cast<r_Structure_Type*>(const_cast<r_Type*>(desc.srcType));
        spp = st->count_elements();

        unsigned int structSize = 0;
        r_Type::r_Type_Id bandType = r_Type::UNKNOWNTYPE;

        // iterate over the attributes of the struct
        for (const auto& att : st->getAttributes())
        {
            if (att.type_of().isPrimitiveType())
            {
                structSize += att.type_of().size();
                if (bandType == r_Type::UNKNOWNTYPE)
                {
                    bandType = att.type_of().type_id();

                    // set sample format
                    switch (att.type_of().type_id())
                    {
                    case r_Type::CHAR:
                    case r_Type::USHORT:
                    case r_Type::ULONG:
                        sampleFormat = SAMPLEFORMAT_UINT;
                        break;
                    case r_Type::FLOAT:
                    case r_Type::DOUBLE:
                        sampleFormat = SAMPLEFORMAT_IEEEFP;
                    default:
                        break;
                    }
                }
                // check if all bands are of the same type
                if (att.type_of().type_id() != bandType)
                {
                    LERROR << "r_Conv_TIFF::convertTo(): can't handle bands of different types";
                    throw r_Error(BASETYPENOTSUPPORTEDBYOPERATION);
                }
            }
            else
            {
                LERROR << "r_Conv_TIFF::convertTo(): can't handle band of non-primitive type "
                       << att.type_of().name();
                throw r_Error(BASETYPENOTSUPPORTEDBYOPERATION);
            }
        }
        bpp = structSize * 8;
        bps = bpp / spp;
        pixelAdd = structSize * height;
        lineAdd = structSize;
        break;
    }
    default:
        LDEBUG << "r_Conv_TIFF::convertTo(): error: unsupported base type " << desc.baseType << ".";
        LERROR << "Error: encountered unsupported TIFF base type.";
        throw r_Error(BASETYPENOTSUPPORTEDBYOPERATION);
    }

    // Just to make sure nothing serious goes wrong if this conversion
    // function is called more than once.
    memfs_newfile(handle);

    // Open a dummy output file (all operations will be redirected to
    // Memory). Make dummy file unique for each object by using the
    // address of its memFSContext (kind of a hack, I know...). That
    // should ensure re-entrancy.
    sprintf(dummyFile, dummyFileFmt, static_cast<void*>(handle));
    tif = TIFFClientOpen(dummyFile, "w", handle,
                         memfs_read, memfs_write, memfs_seek, memfs_close, memfs_size,
                         memfs_map, memfs_unmap);

    if (tif == NULL)
    {
        LERROR << "r_Conv_TIFF::convertTo(): couldn't open file " << dummyFile;
        throw r_Error(r_Error::r_Error_General);
    }

    TIFFSetField(tif, TIFFTAG_ARTIST, "rasdaman");
    TIFFSetField(tif, TIFFTAG_DOCUMENTNAME, "exported from rasdaman database");
    TIFFSetField(tif, TIFFTAG_SOFTWARE, "rasdaman");
    //TIFFSetField(tif, TIFFTAG_SUBFILETYPE, (uint32_t)0);
    TIFFSetField(tif, TIFFTAG_IMAGEWIDTH, width);
    TIFFSetField(tif, TIFFTAG_IMAGELENGTH, height);
    TIFFSetField(tif, TIFFTAG_BITSPERSAMPLE, bps);
    // UNIX doesn't mind which fill-order. NT only understands this one.
    TIFFSetField(tif, TIFFTAG_FILLORDER, FILLORDER_MSB2LSB);
    TIFFSetField(tif, TIFFTAG_COMPRESSION, static_cast<uint16_t>(tiffcomp));
    TIFFSetField(tif, TIFFTAG_ORIENTATION, (uint16_t)ORIENTATION_TOPLEFT);
    // Format-dependent tags
    if (desc.baseType == ctype_rgb)
    {
        TIFFSetField(tif, TIFFTAG_PHOTOMETRIC, (uint16_t)PHOTOMETRIC_RGB);
        TIFFSetField(tif, TIFFTAG_SAMPLESPERPIXEL, static_cast<uint16_t>(3));
    }
    else
    {
        if (desc.baseType == ctype_char)
        {
            TIFFSetField(tif, TIFFTAG_SAMPLESPERPIXEL, static_cast<uint16_t>(1));
        }
        else if (desc.baseType == ctype_struct)
        {
            TIFFSetField(tif, TIFFTAG_SAMPLESPERPIXEL, spp);
        }
        else
        {
            TIFFSetField(tif, TIFFTAG_SAMPLESPERPIXEL, static_cast<uint16_t>(1));
        }
        TIFFSetField(tif, TIFFTAG_PHOTOMETRIC, (uint16_t)PHOTOMETRIC_MINISBLACK);

        // set sample format tag
        switch (desc.baseType)
        {
        case ctype_float32:
        case ctype_float64:
            TIFFSetField(tif, TIFFTAG_SAMPLEFORMAT, SAMPLEFORMAT_IEEEFP);
            break;
        case ctype_char:
        case ctype_uint8:
        case ctype_uint16:
        case ctype_uint32:
        case ctype_uint64:
            TIFFSetField(tif, TIFFTAG_SAMPLEFORMAT, SAMPLEFORMAT_UINT);
            break;
        case ctype_int8:
        case ctype_int16:
        case ctype_int32:
        case ctype_int64:
            TIFFSetField(tif, TIFFTAG_SAMPLEFORMAT, SAMPLEFORMAT_INT);
            break;
        case ctype_struct:
            TIFFSetField(tif, TIFFTAG_SAMPLEFORMAT, sampleFormat);
            break;
        default:
            break;
        }
    }
    TIFFSetField(tif, TIFFTAG_PLANARCONFIG, (uint16_t)PLANARCONFIG_CONTIG);
    TIFFSetField(tif, TIFFTAG_ROWSPERSTRIP, TIFFDefaultStripSize(tif, static_cast<uint32_t>(-1)));
    //TIFFSetField(tif, TIFFTAG_MINSAMPLEVALUE, (uint16_t)0);
    //TIFFSetField(tif, TIFFTAG_MAXSAMPLEVALUE, (uint16_t)255);
    TIFFSetField(tif, TIFFTAG_RESOLUTIONUNIT, (uint16_t)RESUNIT_INCH);
    TIFFSetField(tif, TIFFTAG_XRESOLUTION, static_cast<float>(90.0));
    TIFFSetField(tif, TIFFTAG_YRESOLUTION, static_cast<float>(90.0));
    TIFFSetField(tif, TIFFTAG_XPOSITION, static_cast<float>(0.0));
    TIFFSetField(tif, TIFFTAG_YPOSITION, static_cast<float>(0.0));
    if ((tiffcomp == COMPRESSION_JPEG) || (tiffcomp == COMPRESSION_OJPEG))
    {
        if (quality == 100)
        {
            TIFFSetField(tif, TIFFTAG_JPEGPROC, JPEGPROC_LOSSLESS);
        }
        else
        {
            TIFFSetField(tif, TIFFTAG_JPEGQUALITY, quality);
        }
    }

    // build the colour-map (greyscale, i.e. all 3 components identical)
    // TIFF needs 16 bit values for this (--> tools/tiffdither.c)
    if (desc.baseType == ctype_rgb)
    {
        for (i = 0; i < 256; i++)
        {
            cmap[i] = static_cast<uint16_t>(i * ((1L << 16) - 1) / 255);
        }
        TIFFSetField(tif, TIFFTAG_COLORMAP, cmap, cmap, cmap);
    }

    // Be VERY, VERY careful about the order and the items you write
    // out. TIFFWriteDirectory, e.g.,  has very ugly side-effects.
    uint32_t* tbuff = NULL;
    const char* l = NULL, *line = desc.src;
    uint8_t* normal = NULL; // normalised source data
    uint32_t row = 0;

    if ((tbuff = static_cast<uint32_t*>(mymalloc(((width * height * bpp) >> 5) * sizeof(uint32_t)))) != NULL)
    {
        int error = 0; // indicates if writing succeeded
        // now go line by line
        for (row = 0; row < height && !error; row++, line += lineAdd)
        {
            normal = (uint8_t*)tbuff;
            l = line;

            // copy data in the correct format to the buffer
            switch (desc.baseType)
            {
            case ctype_bool:
            {
                uint8_t val = 0, mask = _R_TIFF_MASK_VALUE;

                // convert 8bpp bitmap to 1bpp bitmap
                for (i = 0; i < width; i++, l += pixelAdd)
                {
                    // fill bits in lsb order
                    if (*l != 0)
                    {
                        val |= mask;
                    }
                    _R_TIFF_MASK_SHIFT(mask);
                    if (mask == 0)
                    {
                        *normal++ = val;
                        val = 0;
                        mask = _R_TIFF_MASK_VALUE;
                    }
                }
                if (mask != _R_TIFF_MASK_VALUE)
                {
                    *normal++ = val;
                }
            }
            break;
            default:
            {
                // copy data (and transpose)
                for (i = 0; i < width; i++, l += pixelAdd, normal += lineAdd)
                {
                    memcpy(normal, l, lineAdd);
                }
            }
            }
            if (TIFFWriteScanline(tif, static_cast<tdata_t>(tbuff), row, 0) < 0)
            {
                break;
            }
        }

        free(tbuff);
        tbuff = NULL;
    }

    if (row < height)  // error
    {
        LDEBUG << "r_Conv_TIFF::convertTo(): error writing data after " << row << " rows out of " << height << ".";
        LERROR << "Error: cannot write all TIFF data.";
        TIFFClose(tif);
        remove(dummyFile);
        throw r_Error(r_Error::r_Error_General);
    }

    TIFFClose(tif);
    // Now delete the dummy file
    remove(dummyFile);

    r_Long tifSize = static_cast<r_Long>(memfs_size(handle));

    // Allocate an array of just the right size and "load" object there
    if ((desc.dest = static_cast<char*>(mymalloc(sizeof(char) * static_cast<unsigned long>(tifSize)))) == NULL)
    {
        LDEBUG << "r_Conv_TIFF::convertTo(): out of memory.";
        LERROR << "Error: out of memory.";
        throw r_Error(MEMMORYALLOCATIONERROR);
    }
    memfs_seek(handle, 0, SEEK_SET);
    memfs_read(handle, desc.dest, tifSize);

    // Set up destination interval
    desc.destInterv = r_Minterval(1);
    desc.destInterv << r_Sinterval(r_Range(0), r_Range(tifSize - 1));

    // define the base type as char for now
    desc.destType = r_Type::get_any_type("char");

    return desc;
#else
    LERROR << "encoding TIFF with internal encoder is not supported; rasdaman should be configured with option -DUSE_TIFF=ON to enable it.";
    throw r_Error(r_Error::r_Error_FeatureNotSupported);
#endif // HAVE_TIFF
}

r_Conv_Desc& r_Conv_TIFF::convertFrom(r_Format_Params options)
{
    formatParams = options;
    return convertFrom(NULL);
}


/// convert TIFF stream into array
r_Conv_Desc& r_Conv_TIFF::convertFrom(const char* options) // CONVERTION FROM TIFF TO DATA
{
#ifdef HAVE_TIFF
    if (options && !formatParams.parse(options))
    {
        params->process(options); //==> CHECK THIS "IMP"
    }
    TIFF* tif = NULL;
    char dummyFile[256];
    unsigned int typeSize = 0;
    int bandType = ctype_void;
    uint16_t sampleFormat = 0;
    uint16_t bps = 0, bpp = 0, spp = 0, planar = 0, photometric = 0, Bpp = 0, Bps = 0;
    uint32_t width = 0, height = 0, pixelAdd = 0, lineAdd = 0, i = 0;
    uint16_t* reds = NULL, *greens = NULL, *blues = NULL;

    // Init simple (chunky) memFS

    desc.dest = NULL;

    if (formatParams.getFilePaths().empty())
    {
        memfs_chunk_initfs(handle, const_cast<char*>(reinterpret_cast<const char*>(desc.src)), static_cast<r_Long>(desc.srcInterv.cell_count()));   //==> CHECK THIS
        // Create dummy file for use in the TIFF open function
        sprintf(dummyFile, dummyFileFmt, static_cast<void*>(handle));
        fclose(fopen(dummyFile, "wb"));
        // Open and force memory mapping mode
        tif = TIFFClientOpen(dummyFile, "rM", handle,
                             memfs_chunk_read, memfs_chunk_read, memfs_chunk_seek, memfs_chunk_close,
                             memfs_chunk_size, memfs_chunk_map, memfs_chunk_unmap);
    }
    else
    {
        auto filePath = formatParams.getFilePath();
        tif = TIFFOpen(filePath.c_str(), "r");
    }

    if (tif == NULL)
    {
        LERROR << "r_Conv_TIFF::convertFrom(): unable to open file!";
        throw r_Error(r_Error::r_Error_General);
    }

    //TIFFPrintDirectory(tif, stdout, 0);

    TIFFGetField(tif, TIFFTAG_BITSPERSAMPLE, &bps);
    TIFFGetField(tif, TIFFTAG_SAMPLESPERPIXEL, &spp);

    if (override_bps)
    {
        bps = override_bps;
    }
    bpp = spp * bps;
    if (override_bpp)
    {
        bpp = override_bpp;
    }
    Bpp = bpp / 8; // bytes per pixel
    Bps = bps / 8; // bytes per sample
    if (override_depth)
    {
        Bpp = Bps = override_depth / 8;
    }
    lineAdd = typeSize = Bpp;

    TIFFGetField(tif, TIFFTAG_PLANARCONFIG, &planar);
    TIFFGetField(tif, TIFFTAG_IMAGEWIDTH, &width);
    TIFFGetField(tif, TIFFTAG_IMAGELENGTH, &height);
    TIFFGetField(tif, TIFFTAG_PHOTOMETRIC, &photometric);
    TIFFGetField(tif, TIFFTAG_SAMPLEFORMAT, &sampleFormat);
    pixelAdd = Bpp * height;

    LDEBUG << "Image information:";
    LDEBUG << "  Bytes per sample: " << Bps;
    LDEBUG << "  Samples per pixel: " << spp;
    LDEBUG << "  Bytes per pixel: " << Bpp;
    LDEBUG << "  Size: " << width << "x" << height;

    if (planar == PLANARCONFIG_CONTIG) // must be contiguous for our case to handle, other cases not dealt yet
    {
        switch (sampleFormat)
        {
        case SAMPLEFORMAT_INT:
        {
            switch (Bps)
            {
            case 1:
                bandType = ctype_int8;
                break;
            case 2:
                bandType = ctype_int16;
                break;
            case 4:
                bandType = ctype_int32;
                break;
            default:
            {
                LERROR << "r_Conv_TIFF::convertFrom(): can't handle band type of signed integer, of length: " << Bps;
                throw r_Error(BASETYPENOTSUPPORTEDBYOPERATION);
            }
            }
        }
        break;
        case SAMPLEFORMAT_UINT:
        {
            switch (Bps)
            {
            case 1:
                bandType = ctype_char;
                break;
            case 2:
                bandType = ctype_uint16;
                break;
            case 4:
                bandType = ctype_uint32;
                break;
            default:
            {
                LERROR << "r_Conv_TIFF::convertFrom(): can't handle band type of unsigned integer, of length: " << Bps;
                throw r_Error(BASETYPENOTSUPPORTEDBYOPERATION);
            }
            }
        }
        break;
        case SAMPLEFORMAT_IEEEFP:
        {
            switch (Bps)
            {
            case 4:
                bandType = ctype_float32;
                break;
            case 8:
                bandType = ctype_float64;
                break;
            default:
            {
                LERROR << "r_Conv_TIFF::convertFrom(): can't handle band type of floating point, of length: " << Bps;
                throw r_Error(BASETYPENOTSUPPORTEDBYOPERATION);
            }
            }
        }
        break;
        default:
        {
            switch (bpp)
            {
            case 1 :
                bandType = ctype_bool;
                break;
            case 8 :
                bandType = ctype_char;
                break;
            case 16:
                bandType = ctype_uint16;
                break;
            case 24:
                bandType = ctype_rgb;
                break;
            case 32:
                bandType = ctype_float32;
                break;
            case 64:
                bandType = ctype_float64;
                break;
            default:
                break;
            }
        }
        }

        if ((photometric == PHOTOMETRIC_PALETTE) && (override_depth != 0))
        {
            TIFFGetField(tif, TIFFTAG_COLORMAP, &reds, &greens, &blues);
            for (i = 0; i < 256; i++)
            {
                if ((reds[i] != greens[i]) || (greens[i] != blues[i]))
                {
                    break;
                }
            }

            if (i < 256)
            {
                pixelAdd = 3 * height;
                lineAdd = 3;
                typeSize = 3;
                desc.baseType = ctype_rgb;
            }
            else
            {
                pixelAdd = height;
                lineAdd = 1;
                typeSize = 1;
                desc.baseType = ctype_char;
            }

            switch (override_depth)
            {
            case 1 :
                desc.baseType = ctype_bool;
                break;
            case 8 :
                desc.baseType = ctype_char;
                break;
            case 16:
                desc.baseType = ctype_uint16;
                break;
            case 24:
                desc.baseType = ctype_rgb;
                break;
            case 32:
                desc.baseType = ctype_float32;
                break;
            case 64:
                desc.baseType = ctype_float64;
                break;
            default:
                break;
            }
        }
        else if (spp == 3 && bpp == 24)
        {
            desc.baseType = ctype_rgb;
        }
        else if (spp > 1)
        {
            // multiband when not rgb and more than 1 sample per pixel
            desc.baseType = ctype_struct;
        }
        else
        {
            desc.baseType = bandType;
        }


        if ((desc.dest = static_cast<char*>(mymalloc(width * height * typeSize * sizeof(char)))) == NULL)
        {
            LERROR << "r_Conv_TIFF::convertFrom(): out of memory error!";
            throw r_Error(MEMMORYALLOCATIONERROR);
        }
        else
        {
            uint32_t* tbuff = NULL;
            char* l = NULL, *line = desc.dest;
            uint8_t* normal = NULL;
            uint32_t row = 0;

            if ((tbuff = new uint32_t[(width * bpp + 31) >> 5]) != NULL)
            {
                for (row = 0; row < height; row++, line += lineAdd)
                {
                    if (desc.baseType != ctype_struct)
                    {
                        if (TIFFReadScanline(tif, static_cast<tdata_t>(tbuff), row, 0) < 0)
                        {
                            break;
                        }
                        normal = (uint8_t*)tbuff;
                        l = line;
                    }
                    switch (desc.baseType)
                    {
                    case ctype_bool: // when cytpe is bool
                    {
                        uint8_t mask = _R_TIFF_MASK_VALUE;
                        for (i = 0; i < width; i++, l += pixelAdd)
                        {
                            *l = (((*normal) & mask) == 0) ? 0 : 1;
                            _R_TIFF_MASK_SHIFT(mask);
                            if (mask == 0)
                            {
                                normal++;
                                mask = _R_TIFF_MASK_VALUE;
                            }
                        }
                    }
                    break;

                    case ctype_char: // when ctype is char
                    {
                        if (reds != NULL)
                        {
                            for (i = 0; i < width; i++, l += pixelAdd)
                            {
                                *l = (reds[*normal++]) >> 8;
                            }
                        }
                        else
                        {
                            for (i = 0; i < width; i++, l += pixelAdd)
                            {
                                *l = (char) * normal++;
                            }
                        }
                    }
                    break;

                    case ctype_rgb: // when cytpe is rgb
                    {
                        if (reds != NULL)
                        {
                            for (i = 0; i < width; i++, l += pixelAdd)
                            {
                                uint8_t val = *normal++;
                                l[0] = (reds[val]) >> 8;
                                l[1] = (greens[val]) >> 8;
                                l[2] = (blues[val]) >> 8;
                            }
                        }
                        else
                        {
                            for (i = 0; i < width; i++, l += pixelAdd)
                            {
                                l[0] = (char) * normal++;
                                l[1] = (char) * normal++;
                                l[2] = (char) * normal++;
                            }
                        }
                    }
                    break;
                    case ctype_struct:
                    {
                        for (int j = 0; j < spp; j++)
                        {
                            TIFFReadScanline(tif, static_cast<tdata_t>(tbuff), row, j); // read the j-th band

                            int offset = j * Bps; // an offset to the j-th band
                            l = line + offset;
                            normal = (uint8_t*)tbuff + offset;
                            for (i = 0; i < width; i++, l += pixelAdd, normal += lineAdd)
                            {
                                memcpy(l, normal, Bps);
                            }
                        }
                    }
                    break;
                    default:
                    {
                        for (i = 0; i < width; ++i, l += pixelAdd, normal += lineAdd)
                        {
                            memcpy(l, normal, lineAdd);
                        }
                    }
                    break;

                    } // switch CLOSED
                } // for loop CLOSED
                delete [] tbuff;
                tbuff = NULL;
            } // if ((tbuff = new uint32_t[(width * bpp + 31) >> 5]) != NULL) CLOSED

            if (row < height)
            {
                LDEBUG << "r_Conv_TIFF::convertFrom(): error reading data: got only " << row << " rows out of " << height << ".";
                LERROR << "Error: cannot read all data.";
                TIFFClose(tif);
                remove(dummyFile);
                throw r_Error(r_Error::r_Error_General);
            }
        }
    }
    else
    {
        LERROR << "r_Conv_TIFF::convertFrom(): can't handle bitplanes!";
    }

    TIFFClose(tif);
    remove(dummyFile);

    // Build destination interval
    if (desc.srcInterv.dimension() == 2)
        // this means it was explicitly specified, so we shouldn't override it
    {
        desc.destInterv = desc.srcInterv;
    }
    else
    {
        desc.destInterv = r_Minterval(2);
        desc.destInterv << r_Sinterval(r_Range(0), r_Range(width - 1))
                        << r_Sinterval(r_Range(0), r_Range(height - 1));
    }

    // build destination type
    if (desc.baseType == ctype_struct)
    {
        // construct and set the structure type
        string bt;
        if (sampleType != NULL)
        {
            bt = sampleType;
        }
        else
        {
            bt = type_to_string(bandType);
        }

        stringstream destType(stringstream::out);
        destType << "struct { ";
        for (i = 0; i < spp; i++)
        {
            if (i > 0)
            {
                destType << ", ";
            }
            destType << bt;
        }
        destType << " }";
        desc.destType = r_Type::get_any_type(destType.str().c_str());
    }
    else
    {
        desc.destType = get_external_type(desc.baseType);
    }

    return desc;
#else
    LERROR << "decoding TIFF with internal decoder is not supported; rasdaman should be configured with option -DUSE_TIFF=ON to enable it.";
    throw r_Error(r_Error::r_Error_FeatureNotSupported);
#endif // HAVE_TIFF
}


const char* r_Conv_TIFF::get_name(void) const
{
    return format_name_tiff;
}


r_Data_Format r_Conv_TIFF::get_data_format(void) const
{
    return r_TIFF;
}


r_Convertor* r_Conv_TIFF::clone(void) const
{
    return new r_Conv_TIFF(desc.src, desc.srcInterv, desc.baseType);
}

