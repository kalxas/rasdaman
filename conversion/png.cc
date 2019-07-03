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
 * SOURCE: png.cc
 *
 * MODULE:  conversion
 * Provides functions to convert data to PNG and back.
 *
 * CLASSES: r_Conv_PNG
 *
 * COMMENTS:
 * - option parsing known bugs:
 *  * no overflow check on options string buffer
 *  * missing ")" is silently ignored
 *  * on hex input, non-hex chars are silently discarded (!)
 *  * too large numbers are mapped to unsigned short's max int
 *  * negative numbers are mapped to unsigned short's max int
 * - do not use "," within an option because this is the parse_param separator
 * - FIXME: define some 3xxx error codes instead of general exception
 *
*/

#include "config.h"

#include "debug.hh"
#include "raslib/rmdebug.hh"
#include <logging.hh>

#include "conversion/png.hh"
#include "conversion/memfs.hh"

#include "raslib/parseparams.hh"
#include "raslib/structuretype.hh"

#include <cstdlib>
#include <iostream>
#include <cstring>
#ifdef HAVE_PNG
#include <png.h>
#endif

// transparency keyword in option string (cf. PNG standard):
#define TRANSP_KEY "tRNS"

#ifdef HAVE_PNG

/* memfs interface functions in C namespace */
extern "C" {

static void png_mem_read_data(png_struct *png_ptr, png_byte *data, png_size_t length)
{
    void *handle=NULL;

    handle = (void*)png_get_io_ptr(png_ptr);
    memfs_chunk_read(handle, (ras_data_t)data, (ras_size_t)length);
}
static void png_mem_write_data(png_struct *png_ptr, png_byte *data, png_size_t length)
{
    void *handle=NULL;

    handle = (void*)png_get_io_ptr(png_ptr);
    memfs_write(handle, (ras_data_t)data, (ras_size_t)length);
}
static void png_mem_flush_data(png_struct *png_ptr)
{
    void *handle=NULL;

    handle = (void*)png_get_io_ptr(png_ptr);
}

/* Customized error handling */
static void *png_user_error_ptr = NULL;

static void png_user_warning_fn(__attribute__ ((unused)) png_struct *png_ptr, const char *warning_msg)
{
    fprintf(stdout, "r_Conv_PNG warning: %s\n", warning_msg);
    fflush(stdout);
}
static void png_user_error_fn(__attribute__ ((unused)) png_struct *png_ptr, const char *error_msg)
{
    fprintf(stderr, "r_Conv_PNG error: %s\n", error_msg);
    // return from this routine, exception will be thrown in setjmp code
}

} // end of C namespace

#endif // HAVE_PNG


/*
 *  r_Conv_PNG class for converting MDD to PNG and back
 */

const char *r_Conv_PNG::name_InfoKey = "Description";
const char *r_Conv_PNG::name_InfoText = "rasdaman MDD encoded as PNG";
const char *r_Conv_PNG::method_convertTo = "r_Conv_PNG::convertTo()";
const char *r_Conv_PNG::method_convertFrom = "r_Conv_PNG::convertFrom()";

r_Conv_PNG::r_Conv_PNG(const char *src, const r_Minterval &interv, int tp)
    : r_Convert_Memory(src, interv, tp)
{
}


r_Conv_PNG::r_Conv_PNG(const char *src, const r_Minterval &interv, const r_Type *tp)
    : r_Convert_Memory(src, interv, tp)
{
}


r_Conv_PNG::~r_Conv_PNG(void)
{
}


r_Conv_Desc &r_Conv_PNG::convertFrom(r_Format_Params options)
{
    formatParams = options;
    return convertFrom(NULL);
}


r_Conv_Desc &r_Conv_PNG::convertTo( const char *options, const r_Range *nullValue )
{
#ifdef HAVE_PNG
    png_struct *write_ptr=NULL;
    png_info *info_ptr = NULL;
    unsigned int i=0, j=0;
    png_uint_32 width=0, height=0;
    int colourType=0, compType=0;
    int spp=0, bps=0, lineAdd=0, pixelAdd=0;
    png_color_8 sig_bit;
    png_text infotext[1];
    char *trans_string = NULL;        // transparency string buffer
    int itemsScanned = 0;         // # of items scanned in options string
    bool transpFound = false;     // keyword for transparency found in options?

    i = 0;                // error state: 0 is ok, !=0 is error

    // option analysis: create parse object -- PB 2005-jul-12
    if (params == NULL)
        params = new r_Parse_Params();
    params->add(TRANSP_KEY, &trans_string, r_Parse_Params::param_type_string);
    transpFound = params->process(options);

    // check for good options, if any
    if (options != NULL && ! transpFound)
    {
        LERROR << "Error: illegal PNG option string: " << options;
        throw r_Error(r_Error::r_Error_General);
    }

    write_ptr = png_create_write_struct(PNG_LIBPNG_VER_STRING, png_user_error_ptr, png_user_error_fn, png_user_warning_fn);

    if (write_ptr == NULL)
        i=1;
    else
    {
        info_ptr = png_create_info_struct(write_ptr);
        if (info_ptr == NULL)
        {
            LERROR << "Error: unable to allocate PNG header.";
            i=1;
        }
        else if (setjmp(png_jmpbuf(write_ptr)))
        {
            png_destroy_write_struct(&write_ptr, &info_ptr);
            LERROR << "Error: unable to save PNG stack";
            throw r_Error(r_Error::r_Error_General);
        }
    }

    if (i != 0)
    {
        png_destroy_write_struct(&write_ptr, &info_ptr);
        throw r_Error(r_Error::r_Error_General);
    }

    memfs_newfile(handle);

    png_set_write_fn(write_ptr, static_cast<void*>(handle), png_mem_write_data, png_mem_flush_data);

    // Compression
    compType = PNG_COMPRESSION_TYPE_DEFAULT;

    // Size
    width  = static_cast<png_uint_32>(desc.srcInterv[0].high() - desc.srcInterv[0].low() + 1);
    height = static_cast<png_uint_32>(desc.srcInterv[1].high() - desc.srcInterv[1].low() + 1);

    png_bytep trans_alpha = NULL;
    png_color_16p trans_color = NULL;
    int num_trans = 0;

    png_get_tRNS(write_ptr, info_ptr, &trans_alpha, &num_trans, &trans_color);

#define GET_TRANS_COLOR(color) trans_color->color

    // Depth and sample format and transparency
    // added transparency -- PB 2005-jul-12
    switch (desc.baseType)
    {
    case ctype_bool:
        spp = 1;
        bps = 1;
        pixelAdd = static_cast<int>(height);
        lineAdd = 1;
        colourType = PNG_COLOR_TYPE_GRAY;
        sig_bit.gray = 1;
        if (transpFound)
        {
            itemsScanned = sscanf( trans_string, " %hu ", &GET_TRANS_COLOR(gray) );
            if (itemsScanned != 1)
            {
                LERROR << "Error: illegal syntax in transparency color specification - should be \"%i\", but is: " << trans_string;
                throw r_Error(r_Error::r_Error_General);
            }
        }
        break;

    case ctype_char:
        spp = 1;
        bps = 8;
        pixelAdd = static_cast<int>(height);
        lineAdd = 1;
        colourType = PNG_COLOR_TYPE_GRAY;
        sig_bit.gray = 8;
        if (transpFound)
        {
            itemsScanned = sscanf( trans_string, " %hu ", &GET_TRANS_COLOR(gray) );
            if (itemsScanned != 1)
            {
                LERROR << "Error: illegal syntax in transparency color specification - should be \"%i\", but is: " << trans_string;
                throw r_Error(r_Error::r_Error_General);
            }
        }
        break;

    case ctype_rgb:
        LINFO << "ctype_rgb";
        spp = 3;
        bps = 8;
        pixelAdd = 3*static_cast<int>(height);
        lineAdd = 3;
        colourType = PNG_COLOR_TYPE_RGB;
        sig_bit.red = 8;
        sig_bit.green = 8;
        sig_bit.blue = 8;
        if (transpFound)
        {
            itemsScanned = sscanf( trans_string, " ( %hu ; %hu ; %hu ) ", &GET_TRANS_COLOR(red), &GET_TRANS_COLOR(green), &GET_TRANS_COLOR(blue) );
            if (itemsScanned != 3)
            {
                LERROR << "Error: illegal syntax in item #" << itemsScanned << " of transparency color specification - should be \"(%i;%i;%i)\", but is: " << trans_string;
                throw r_Error(r_Error::r_Error_General);
            }
        }
        break;

    case ctype_struct:
        LINFO << "ctype_struct";
        // check first if it's 4 char bands
        {
            r_Structure_Type *st = static_cast<r_Structure_Type*>(const_cast<r_Type*>(desc.srcType));
            r_Structure_Type::attribute_iterator iter(st->defines_attribute_begin());
            int bands = 0;
            while (iter != st->defines_attribute_end())
            {
                ++bands;
                if ((*iter).type_of().type_id() != r_Type::CHAR)
                {
                    LERROR << "Error: the PNG convertor expects bands of type char";
                    throw r_Error(r_Error::r_Error_General);
                }
                iter++;
            }
            if (bands > 4)
            {
                LERROR << "Error: the PNG convertor can not handle " << bands << " bands, only up 1, 3, and 4 bands are allowed.";
                throw r_Error(r_Error::r_Error_General);
            }
        }

        spp = 4;
        bps = 8;
        pixelAdd = 4*static_cast<int>(height);
        lineAdd = 4;
        colourType = PNG_COLOR_TYPE_RGBA;
        sig_bit.red = 8;
        sig_bit.green = 8;
        sig_bit.blue = 8;
        sig_bit.alpha = 8;
        break;

    default:
        LERROR << "Error: " << method_convertTo << ": Unknown base type.";
        throw r_Error(r_Error::r_Error_General);
    } // switch

    // TODO: adapt below for png 1.5+
    // adjust transparency color value to pixel depth (unconditionally, even if transparency is unused)
    // if (bps == 8)
    // {
    //     trans_color->red   &= 0xff;
    //     trans_color->green &= 0xff;
    //     trans_color->blue  &= 0xff;
    //     trans_color->gray  &= 0xff;
    // }

    if (trans_string != NULL)
    {
        delete [] trans_string;
        trans_string = NULL;
    }


    png_set_IHDR(write_ptr, info_ptr, width, height, bps, colourType, PNG_INTERLACE_NONE, compType, PNG_FILTER_TYPE_BASE);
    png_set_compression_level(write_ptr, 2);
    png_set_sBIT(write_ptr, info_ptr, &sig_bit);

    // Info text
    infotext[0].key = new char[strlen(name_InfoKey)+1];
    strcpy(infotext[0].key, name_InfoKey);
    infotext[0].text = new char[strlen(name_InfoText)+1];
    strcpy(infotext[0].text, name_InfoText);
    infotext[0].compression = PNG_TEXT_COMPRESSION_NONE;
    infotext[0].text_length = strlen(infotext[0].text);
    png_set_text(write_ptr, info_ptr, infotext, 1);

    // Write header
    png_write_info(write_ptr, info_ptr);

    png_byte *row=NULL, *rowPtr=NULL;
    const unsigned char *src=NULL, *srcPtr=NULL;

    row = new png_byte[((bps * spp * static_cast<int>(width) + 7) >> 3)];
    src = (const unsigned char*)(desc.src);

    for (j=0; j<height; j++)
    {
        rowPtr = row;
        srcPtr = src;

        switch (desc.baseType)
        {
        case ctype_bool:
        {
            int mask=0;
            png_byte val=0;

            val = 0;
            mask = 0x80; // png docs: leftmost pixel in high-order bits
            for (i=0; i<width; i++, srcPtr += pixelAdd)
            {
                if (*srcPtr != 0) val |= mask;
                mask >>= 1;
                if (mask == 0)
                {
                    *rowPtr++ = val;
                    val = 0;
                    mask = 0x80;
                }
            }
            if (mask != 0x80) *rowPtr++ = val;
        }
        break;
        case ctype_char:
        {
            for (i=0; i<width; i++, srcPtr += pixelAdd)
            {
                *rowPtr++ = *srcPtr;
            }
        }
        break;
        case ctype_rgb:
        {
            for (i=0; i<width; i++, srcPtr += pixelAdd)
            {
                *rowPtr++ = srcPtr[0];
                *rowPtr++ = srcPtr[1];
                *rowPtr++ = srcPtr[2];
            }
        }
        break;
        case ctype_struct:
        {
            for (i=0; i<width; i++, srcPtr += pixelAdd)
            {
                *rowPtr++ = srcPtr[0];
                *rowPtr++ = srcPtr[1];
                *rowPtr++ = srcPtr[2];
                *rowPtr++ = srcPtr[3];
            }
        }
        break;
        default: break;
        }

        png_write_row(write_ptr, row);

        src += lineAdd;
    }

    delete [] row;
    row=NULL;

    png_write_end(write_ptr, info_ptr);

    // --- Cleanup -------------------------------------------------------
    png_destroy_write_struct(&write_ptr, &info_ptr);

    delete [] infotext[0].key;
    infotext[0].key=NULL;
    delete [] infotext[0].text;
    infotext[0].text=NULL;

    r_Long pngSize = static_cast<r_Long>(memfs_size(handle));

    if ((desc.dest = static_cast<char*>(mystore.storage_alloc(static_cast<size_t>(pngSize)))) == NULL)
    {
        LERROR << "Error: " << method_convertTo << ": out of memory.";
        throw r_Error(MEMMORYALLOCATIONERROR);
    }
    memfs_seek(handle, 0, SEEK_SET);
    memfs_read(handle, desc.dest, pngSize);

    desc.destInterv = r_Minterval(1);
    desc.destInterv << r_Sinterval(static_cast<r_Range>(0), static_cast<r_Range>(pngSize) - 1);

    // set result type to char string
    desc.destType = r_Type::get_any_type("char");

    return desc;
#else
    LERROR << "encoding PNG with internal encoder is not supported; rasdaman should be configured with option -DUSE_PNG=ON to enable it.";
    throw r_Error(r_Error::r_Error_FeatureNotSupported);
#endif // HAVE_PNG
}


r_Conv_Desc &r_Conv_PNG::convertFrom(const char *options)
{
#ifdef HAVE_PNG
    png_struct *read_ptr=NULL;
    png_info *info_ptr = NULL;
    int pass=0, numPasses=0;
    png_uint_32 width=0, height=0, pitch=0;
    int colourType=0, interlaceType=0, compType=0, filterType=0;
    int spp=0, bps=0, lineAdd=0, pixelAdd=0;

    unsigned int i = 0, j=0;
    read_ptr = png_create_read_struct(PNG_LIBPNG_VER_STRING, png_user_error_ptr, png_user_error_fn, png_user_warning_fn);
    if (read_ptr == NULL) i=1;
    else
    {
        info_ptr = png_create_info_struct(read_ptr);
        if (info_ptr == NULL) i=1;
        else if (setjmp(png_jmpbuf(read_ptr)))
        {
            png_destroy_read_struct(&read_ptr, &info_ptr, NULL);
            LERROR << "r_Conv_PNG::convertFrom(" << options << "): unable to save the stack";
            throw r_Error(r_Error::r_Error_General);
        }
    }
    if (i != 0)
    {
        png_destroy_read_struct(&read_ptr, &info_ptr, NULL);
        throw r_Error(r_Error::r_Error_General);
    }

    memfs_chunk_initfs(handle, const_cast<char*>(desc.src), static_cast<r_Long>(desc.srcInterv[0].high() - desc.srcInterv[0].low() + 1));

    desc.dest = NULL;

    png_set_read_fn(read_ptr, static_cast<void*>(handle), png_mem_read_data);

    png_read_info(read_ptr, info_ptr);

    png_get_IHDR(read_ptr, info_ptr, &width, &height, &bps, &colourType, &interlaceType, &compType, &filterType);

    if (bps > 8)
    {
        LWARNING << method_convertFrom << " warning: 16 bit samples quantized to 8 bit";
        png_set_strip_16(read_ptr);
    }

    if ((colourType & PNG_COLOR_MASK_ALPHA) != 0)
    {
        LWARNING << method_convertFrom << " warning: image contains alpha channel which will be lost";
        png_set_strip_alpha(read_ptr);
    }

    if (bps < 8)
    {
        png_set_packing(read_ptr);  // extract depths 1-4 as 1 byte per pixel
    }

    switch (colourType)
    {
    case PNG_COLOR_TYPE_GRAY:
    case PNG_COLOR_TYPE_GRAY_ALPHA:
        pitch = width;
        lineAdd = 1;
        pixelAdd = static_cast<int>(height);
        if (bps == 1) desc.baseType = ctype_bool;
        else desc.baseType = ctype_char;
        break;
    case PNG_COLOR_TYPE_PALETTE:
        png_set_expand(read_ptr);
    case PNG_COLOR_TYPE_RGB:
    case PNG_COLOR_TYPE_RGB_ALPHA:
        pitch = 3*width;
        lineAdd = 3;
        pixelAdd = 3*static_cast<int>(height);
        desc.baseType = ctype_rgb;
        break;
    default:
        LERROR << method_convertFrom << ": don't understand image format";
        throw r_Error(r_Error::r_Error_General);
    }

    numPasses = png_set_interlace_handling(read_ptr);

#ifdef RMANDEBUG
    if (RMANDEBUG >= 4)
    {
        LTRACE << "PNG image: width " << width << ", height " << height << ", bps " << bps;
        LTRACE << ", colour ";
        switch (desc.baseType)
        {
        case ctype_bool:
            LTRACE << "bw";
            break;
        case ctype_char:
            LTRACE << "grey";
            break;
        case ctype_rgb:
            LTRACE << "rgb";
            break;
        default:
            LTRACE << "other";
            break;
        }
        LTRACE << ", interlace level " << numPasses;
    }
#endif

    png_byte *row = new png_byte[pitch];

    desc.dest = static_cast<char*>(mystore.storage_alloc(pitch * height));

    for (pass=0; pass < numPasses; pass++)
    {
        unsigned char *dest, *destPtr;

        dest = (unsigned char*)(desc.dest);
        for (j=0; j<height; j++, dest += lineAdd)
        {
            png_byte *rowPtr=NULL;

            destPtr = dest;
            rowPtr = row;
            switch (desc.baseType)
            {
            case ctype_bool:
            case ctype_char:
            {
                // In case of multiple passes set up the buffer according to the last pass
                if (pass != 0)
                {
                    for (i=0; i<width; i++, destPtr += pixelAdd)
                    {
                        *rowPtr++ = *destPtr;
                    }
                    destPtr = dest;
                    rowPtr = row;
                }
                png_read_row(read_ptr, row, NULL);
                for (i=0; i<width; i++, destPtr += pixelAdd)
                {
                    *destPtr = *rowPtr++;
                }
            }
            break;
            case ctype_rgb:
            {
                if (pass != 0)
                {
                    for (i=0; i<width; i++, destPtr += pixelAdd)
                    {
                        *rowPtr++ = destPtr[0];
                        *rowPtr++ = destPtr[1];
                        *rowPtr++ = destPtr[2];
                    }
                    destPtr = dest;
                    rowPtr = row;
                }
                png_read_row(read_ptr, row, NULL);
                for (i=0; i<width; i++, destPtr += pixelAdd)
                {
                    destPtr[0] = *rowPtr++;
                    destPtr[1] = *rowPtr++;
                    destPtr[2] = *rowPtr++;
                }
            }
            break;
            default: break;
            }
        }
    }

    png_read_end(read_ptr, info_ptr);

    delete [] row;
    row=NULL;

    png_destroy_read_struct(&read_ptr, &info_ptr, NULL);

    if (desc.srcInterv.dimension() == 2)
        // this means it was explicitly specified, so we shouldn't override it
        desc.destInterv = desc.srcInterv;
    else
    {
        desc.destInterv = r_Minterval(2);
        desc.destInterv << r_Sinterval(static_cast<r_Range>(0), static_cast<r_Range>(width) - 1)
                        << r_Sinterval(static_cast<r_Range>(0), static_cast<r_Range>(height) - 1);
    }

    desc.destType = get_external_type(desc.baseType);

    return desc;

#else
    LERROR << "decoding PNG with internal decoder is not supported; rasdaman should be configured with option -DUSE_PNG=ON to enable it.";
    throw r_Error(r_Error::r_Error_FeatureNotSupported);
#endif // HAVE_PNG
}



const char *r_Conv_PNG::get_name( void ) const
{
    return format_name_png;
}


r_Data_Format r_Conv_PNG::get_data_format( void ) const
{
    return r_PNG;
}


r_Convertor *r_Conv_PNG::clone( void ) const
{
    return new r_Conv_PNG(desc.src, desc.srcInterv, desc.baseType);
}
