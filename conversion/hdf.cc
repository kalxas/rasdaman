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
 * SOURCE: hdf.cc
 *
 * MODULE:  conversion
 *
 * CLASSES: r_Conv_HDF
 *
 * COMMENTS:
 *
 * Provides functions to convert data to HDF SD and back.
 *
 */

#include "config.h"

#include "conversion/hdf.hh"
#include "conversion/hdfincludes.hh"
#include "conversion/convutil.hh"
#include "raslib/error.hh"
#include "raslib/parseparams.hh"
#include "raslib/primitivetype.hh"
#include "mymalloc/mymalloc.h"
#include <logging.hh>

#include <stdio.h>
#include <iostream>

// HDF changed from MAX_VAR_DIMS to H4_MAX_VAR_DIMS around 9/5/2007
// to avoid potential conflicts with NetCDF-3 library
#ifndef H4_MAX_VAR_DIMS
#define H4_MAX_VAR_DIMS MAX_VAR_DIMS
#endif

// make this code robust against different HDF versions and trouble:
#ifndef MAX_VAR_DIMS
#define MAX_VAR_DIMS 32
#endif

#ifdef HAVE_HDF

const r_Convertor::convert_string_t r_Conv_HDF::compNames[] =
    {
        {"none", COMP_CODE_NONE},
        {"rle", COMP_CODE_RLE},
        {"huffman", COMP_CODE_SKPHUFF},
        {"deflate", COMP_CODE_DEFLATE},
        {NULL, COMP_CODE_NONE}};

//#else
//
//const r_Convertor::convert_string_t r_Conv_HDF::compNames[] =
//    {};

#endif

// Buffer used for switching the majorness (column <--> row) of the array data
const int r_Conv_HDF::MaxSwapBufferSize = 0x10000;

void r_Conv_HDF::initHDF(void)
{
    compType = NULL;
    quality = 80;
    skiphuff = 0;

    if (params == NULL)
    {
        params = new r_Parse_Params;
    }

    params->add("comptype", &compType, r_Parse_Params::param_type_string);
    params->add("quality", &quality, r_Parse_Params::param_type_int);
    params->add("skiphuff", &skiphuff, r_Parse_Params::param_type_int);
}

r_Conv_HDF::r_Conv_HDF(const char *src, const r_Minterval &interv, const r_Type *tp)
    : r_Convertor(src, interv, tp, true)
{
    initHDF();

    if (tp->isStructType())
    {
        LERROR << "r_Conv_HDF::r_Conv_HDF(): structured types not supported.";
        throw r_Error(r_Error::r_Error_General);
    }
}

r_Conv_HDF::r_Conv_HDF(const char *src, const r_Minterval &interv, int tp)
    : r_Convertor(src, interv, tp)
{
    initHDF();
}

r_Conv_HDF::~r_Conv_HDF(void)
{
    if (compType != NULL)
    {
        delete[] compType;
        compType = NULL;
    }
}

r_Conv_Desc &r_Conv_HDF::convertTo(const char *options,
                                   const r_Range *nullValue)
{
#ifdef HAVE_HDF
    char name[] = "hdfTempXXXXXX";
    int32 handle = 0, sds_id = 0;
    comp_coder_t comp_type = COMP_CODE_NONE;
    int32 *dimsizes = NULL, *start = NULL;
    FILE *fp = NULL;
    comp_info c_info;
    int tempFD;

    tempFD = mkstemp(name);
    if (tempFD == -1)
    {
        LERROR << "r_Conv_hdf::convertTo(" << (options ? options : "NULL")
               << ") desc.srcType (" << desc.srcType->type_id()
               << ") unable to generate a tempory file !";
        throw r_Error();
    }

    if ((handle = SDstart(name, DFACC_CREATE)) == FAIL)
    {
        LERROR << "r_Conv_HDF::convertTo(): unable to open output file.";
        throw r_Error(r_Error::r_Error_General);
    }
    auto rank = desc.srcInterv.dimension();

    dimsizes = new int32[rank];
    start = new int32[rank];
    datatype = ConvUtil::ctypeToHdfType(desc.baseType, datasize);

    for (size_t i = 0; i < rank; i++)
    {
        dimsizes[i] = desc.srcInterv[i].high() - desc.srcInterv[i].low() + 1;
        start[i] = 0;
    }

    if ((sds_id = SDcreate(handle, "RasDaMan object", datatype, static_cast<int32>(rank), dimsizes)) == FAIL)
    {
        LERROR << "r_Conv_HDF::convertTo(): unable to create object.";
        SDend(handle);
        remove(name);
        throw r_Error(r_Error::r_Error_General);
    }
    SDsetfillmode(sds_id, SD_NOFILL);

    params->process(options);
    updateNodataValue(nullValue);

    comp_type = COMP_CODE_DEFLATE;
    if (compType != NULL)
    {
        size_t i;
        for (i = 0; compNames[i].key != NULL; i++)
        {
            if (strcasecmp(compNames[i].key, compType) == 0)
            {
                comp_type = (comp_coder_t)compNames[i].id;
                break;
            }
        }
        if (compNames[i].key == NULL)
        {
            LERROR << "r_Conv_HDF::convertTo(): unsupported compression type " << compType;
        }
    }
    c_info.skphuff.skp_size = skiphuff;
    c_info.deflate.level = quality;

    SDsetcompress(sds_id, comp_type, &c_info);

    SDwritedata(sds_id, start, NULL, dimsizes, const_cast<char *>(desc.src));

    delete[] dimsizes;
    dimsizes = NULL;
    delete[] start;
    start = NULL;

    SDendaccess(sds_id);

    SDend(handle);

    if ((fp = fopen(name, "rb")) == NULL)
    {
        LERROR << "r_Conv_HDF::convertTo(): unable to read back file.";
        throw r_Error(r_Error::r_Error_General);
    }
    fseek(fp, 0, SEEK_END);
    auto filesize = ftell(fp);

    desc.destInterv = r_Minterval(1);
    desc.destInterv << r_Sinterval((r_Range)0, (r_Range)filesize - 1);

    if ((desc.dest = (char *)mymalloc(static_cast<size_t>(filesize))) == NULL)
    {
        LERROR << "r_Conv_HDF::convertTo(): out of memory error";
        fclose(fp);
        throw r_Error(MEMMORYALLOCATIONERROR);
    }
    fseek(fp, 0, SEEK_SET);
    fread(desc.dest, 1, static_cast<size_t>(filesize), fp);

    fclose(fp);

    remove(name);

    // Result is just a bytestream
    desc.destType = r_Type::get_any_type("char");

    return desc;

#else  // HAVE_HDF
    (void)options;
    LERROR << "support for encoding HDF4 is not enabled; rasdaman should be configured with option --with-hdf4 to enable it.";
    throw r_Error(r_Error::r_Error_FeatureNotSupported);

#endif
}

r_Conv_Desc &r_Conv_HDF::convertFrom(const char *options)
{
#ifdef HAVE_HDF

    char name[] = "HDFtempXXXXXX";
    int32 handle = 0, sds_id = 0, dtype = 0, numattr = 0, array_size = 0;
    int32 dimsizes[H4_MAX_VAR_DIMS];
    int32 *start = NULL;
    int dsize = 0;
    FILE *fp = NULL;
    int tempFD;

    if (desc.srcInterv.dimension() != 1)
    {
        LERROR << "r_Conv_HDF::convertFrom(): source data must be a bytestream!";
        throw r_Error(r_Error::r_Error_General);
    }

    tempFD = mkstemp(name);
    if (tempFD == -1)
    {
        LERROR << "r_Conv_hdf::convertTo(" << (options ? options : "NULL")
               << ") desc.srcType (" << desc.srcType->type_id()
               << ") unable to generate a tempory file !";
        throw r_Error();
    }

    if ((fp = fopen(name, "wb")) == NULL)
    {
        LERROR << "r_Conv_HDF::convertFrom(): unable to write temporary file!";
        throw r_Error(r_Error::r_Error_General);
    }
    size_t filesize = static_cast<size_t>(
        desc.srcInterv[0].high() - desc.srcInterv[0].low() + 1);
    size_t j = 0;
    if ((j = fwrite(desc.src, 1, filesize, fp)) != filesize)
    {
        LERROR << "r_Conv_HDF::convertFrom(): error writing to temporary file ("
               << j << " / " << filesize << ')';
        throw r_Error(r_Error::r_Error_General);
    }
    fclose(fp);

    if ((handle = SDstart(name, DFACC_READ)) == FAIL)
    {
        LERROR << "r_Conv_HDF::convertFrom(): can't read temporary file!";
        throw r_Error(r_Error::r_Error_General);
    }
    // Only read the first object in the file
    if ((sds_id = SDselect(handle, 0)) == FAIL)
    {
        LERROR << "r_Conv_HDF::convertFrom(): unable to open first object";
        SDend(handle);
        remove(name);
        throw r_Error(r_Error::r_Error_General);
    }

    int32 rank;
    SDgetinfo(sds_id, NULL, &rank, dimsizes, &dtype, &numattr);

    // Ignore native datatype flag
    dtype &= ~DFNT_NATIVE;

    desc.destType = get_external_type(ConvUtil::hdfTypeToCtype(dtype, dsize));

    start = new int32[rank];
    desc.destInterv = r_Minterval(static_cast<r_Dimension>(rank));
    array_size = (int32)dsize;
    for (size_t i = 0; i < static_cast<size_t>(rank); i++)
    {
        desc.destInterv << r_Sinterval(r_Range(0), r_Range(dimsizes[i] - 1));
        array_size *= dimsizes[i];
        start[i] = 0;
    }
    if (desc.srcInterv.dimension() == 2)
    // this means it was explicitly specified, so we shouldn't override it
    {
        desc.destInterv = desc.srcInterv;
    }

    if ((desc.dest = (char *)mymalloc(static_cast<size_t>(array_size))) == NULL)
    {
        LERROR << "r_Conv_HDF::convertFrom(): out of memory error!";
        SDend(handle);
        remove(name);
        throw r_Error(MEMMORYALLOCATIONERROR);
    }

    if (SDreaddata(sds_id, start, NULL, dimsizes, static_cast<void *>(desc.dest)) == FAIL)
    {
        LERROR << "r_Conv_HDF::convertFrom(): error reading data";
        SDend(handle);
        remove(name);
        throw r_Error(r_Error::r_Error_General);
    }

    delete[] start;
    start = NULL;

    SDendaccess(sds_id);

    SDend(handle);

    remove(name);

    return desc;

#else  // HAVE_HDF
    (void)options;
    LERROR << "support for decoding HDF4 is not enabled; rasdaman should be configured with option --with-hdf4 to enable it.";
    throw r_Error(r_Error::r_Error_FeatureNotSupported);

#endif  // HAVE_HDF
}

r_Conv_Desc &r_Conv_HDF::convertFrom(__attribute__((unused)) r_Format_Params options)
{
    throw r_Error(r_Error::r_Error_FeatureNotSupported);
}

const char *r_Conv_HDF::get_name(void) const
{
    return format_name_hdf;
}

r_Data_Format r_Conv_HDF::get_data_format(void) const
{
    return r_HDF;
}

r_Convertor *r_Conv_HDF::clone(void) const
{
    return new r_Conv_HDF(desc.src, desc.srcInterv, desc.baseType);
}
