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
 * SOURCE: grib.cc
 *
 * MODULE:  conversion
 *
 * CLASSES: r_Conv_GRIB
 *
 * COMMENTS:
 *
 * Provides functions to convert data to GRIB and back.
 */

#include "config.h"

#include "conversion/grib.hh"
#include "conversion/memfs.hh"
#include "conversion/formatparamkeys.hh"
#include "raslib/error.hh"
#include "raslib/parseparams.hh"
#include "raslib/primitivetype.hh"
#include "raslib/attribute.hh"
#include "raslib/structuretype.hh"
#include "raslib/odmgtypes.hh"
#include "raslib/miterd.hh"
#include "mymalloc/mymalloc.h"

#include <easylogging++.h>

#include <string.h>
#include <errno.h>

#include <utility>
#include <boost/algorithm/string/replace.hpp>
#include <memory>

using namespace std;

/// constructor using an r_Type object. Exception if the type isn't atomic.

r_Conv_GRIB::r_Conv_GRIB(const char* src, const r_Minterval& interv, const r_Type* tp) throw(r_Error)
    : r_Convert_Memory(src, interv, tp, true)
{
}

/// constructor using convert_type_e shortcut

r_Conv_GRIB::r_Conv_GRIB(const char* src, const r_Minterval& interv, int tp) throw(r_Error)
    : r_Convert_Memory(src, interv, tp)
{
}


/// destructor

r_Conv_GRIB::~r_Conv_GRIB(void)
{
}

/// convert to GRIB

r_Conv_Desc& r_Conv_GRIB::convertTo(__attribute__ ((unused)) const char* options) throw(r_Error)
{
    LERROR << "converting to GRIB is not supported.";
    throw r_Error(r_Error::r_Error_FeatureNotSupported);
}

#ifdef HAVE_GRIB

// check if the error code from a grib function call on a message is successful;
// print the given error msg if it isn't
#define VALIDATE_MSG_ERRCODE(err, msg) \
    if (err != GRIB_SUCCESS) { \
        LERROR << msg; \
        LERROR << "reason: " << grib_get_error_message(err); \
        grib_handle_delete(h); \
        if (desc.dest) { free(desc.dest); desc.dest = NULL; } \
        fclose(in); \
        throw r_Error(r_Error::r_Error_Conversion); \
    }

/// convert from GRIB

r_Conv_Desc& r_Conv_GRIB::convertFrom(const char* options) throw(r_Error)
{
    if (options == NULL)
    {
        LERROR << "mandatory format options have not been specified.";
        throw r_Error(INVALIDFORMATPARAMETER);
    }
    formatParams.parse(string(options), true);
    return this->convertFrom(formatParams);
}

r_Conv_Desc& r_Conv_GRIB::convertFrom(r_Format_Params options) throw(r_Error)
{
    formatParams = options;
    Json::Value messageDomains = getMessageDomainsJson();
    unordered_map<int, r_Minterval> messageDomainsMap = getMessageDomainsMap(messageDomains);
    r_Minterval fullBoundingBox = computeBoundingBox(messageDomainsMap);
    LDEBUG << "computed bounding box from the specified messageDomains: " << fullBoundingBox;

    //
    // prepare output structure
    //
    r_Dimension dimNo = fullBoundingBox.dimension();
    setTargetDomain(fullBoundingBox);

    r_Range targetWidth = desc.destInterv[dimNo - 2].get_extent();
    r_Range targetHeight = desc.destInterv[dimNo - 1].get_extent();
    size_t targetArea = (size_t)(targetWidth * targetHeight);
    r_Range messageWidth = fullBoundingBox[dimNo - 2].get_extent();
    r_Range messageHeight = fullBoundingBox[dimNo - 1].get_extent();
    size_t messageArea = (size_t)(messageWidth * messageHeight);

    LDEBUG << "x size: " << targetWidth << ", y size: " << targetHeight << ", number of values per message: " << targetArea;

    size_t messageSize = (size_t) messageArea * sizeof(double);
    unique_ptr<char[]> messageData(new(nothrow) char[messageSize]);
    if (!messageData)
    {
        LERROR << "failed allocating " << messageSize << " bytes of memory.";
        throw r_Error(r_Error::r_Error_MemoryAllocation);
    }

    setTargetDataAndType();

    //
    // open grib file and go through all messages
    //
    FILE* in = getFileHandle();

    int messageIndex = 1;
    grib_handle* h = NULL;
    grib_context* ctx = NULL; // use default context
    int err = GRIB_SUCCESS;
    while ((h = grib_handle_new_from_file(ctx, in, &err)))
    {
        VALIDATE_MSG_ERRCODE(err, "unable to create grib file handle for message " << messageIndex);
        if (messageDomainsMap.count(messageIndex) == 0)
        {
            // skip message, it was not specified for ingestion in the format parameters
            continue;
        }
        validateMessageDomain(in, h, messageIndex, messageWidth, messageHeight, messageArea);

        r_Minterval messageDomain = messageDomainsMap[messageIndex];
        if (!subsetSpecified || desc.destInterv.intersects_with(messageDomain))
        {
            r_Minterval targetDomain;
            if (subsetSpecified)
            {
                targetDomain = desc.destInterv.create_intersection(messageDomain);
            }
            else
            {
                targetDomain = messageDomain;
            }

            size_t sliceOffset = getSliceOffset(desc.destInterv, targetDomain, targetArea);
            err = grib_get_double_array(h, "values", (double*) messageData.get(), &messageArea);
            VALIDATE_MSG_ERRCODE(err, "failed getting the values in message " << messageIndex);
            if (subsetSpecified && (targetWidth != messageWidth || targetHeight != messageHeight))
            {
                decodeSubset(messageData.get(), messageDomain, targetDomain, sliceOffset, targetWidth, targetHeight, targetArea);
            }
            else
            {
                transpose<double>((double*) messageData.get(), (double*)(desc.dest + sliceOffset), targetHeight, targetWidth);
            }

            LTRACE << "processed grib message " << messageIndex << ": x size = " << targetWidth << ", y size = " << targetHeight <<
                   ", number of values = " << targetArea << ", slice offset = " << sliceOffset;
        }

        ++messageIndex;
        grib_handle_delete(h);
    }

    fclose(in);
    return desc;
}

Json::Value r_Conv_GRIB::getMessageDomainsJson() throw(r_Error)
{
    Json::Value val = formatParams.getParams();
    if (val.isMember(FormatParamKeys::Decode::INTERNAL_STRUCTURE) &&
            val[FormatParamKeys::Decode::INTERNAL_STRUCTURE].isMember(FormatParamKeys::Decode::Grib::MESSAGE_DOMAINS))
    {
        return val[FormatParamKeys::Decode::INTERNAL_STRUCTURE][FormatParamKeys::Decode::Grib::MESSAGE_DOMAINS];
    }
    else
    {
        LERROR << "invalid format options, messageDomains have not been specified.";
        throw r_Error(INVALIDFORMATPARAMETER);
    }
}

FILE* r_Conv_GRIB::getFileHandle() throw (r_Error)
{
    size_t srcSize = (size_t)(desc.srcInterv[0].high() - desc.srcInterv[0].low() + 1);

    FILE* in = NULL;
    if (formatParams.getFilePaths().empty())
    {
        in = fmemopen(static_cast<void*>(const_cast<char*>(desc.src)), srcSize, "r");
    }
    else
    {
        in = fopen(formatParams.getFilePath().c_str(), "r");
    }
    if (in == NULL)
    {
        LERROR << "failed opening GRIB file.";
        LERROR << "reason: " << strerror(errno);
        throw r_Error(r_Error::r_Error_Conversion);
    }
    return in;
}

size_t r_Conv_GRIB::getSliceOffset(const r_Minterval& domain, const r_Minterval& messageDomain, size_t xyLen)
{
    size_t ret = 0;

    size_t prevDimsTotal = xyLen;
    for (int i = (int) domain.dimension() - 3; i >= 0; i--)
    {
        r_Dimension dim = (r_Dimension) i; // silence warnings; using 'unsigned int i' is a bad idea in this case
        size_t dimExtent = (size_t)(messageDomain[dim].low() - domain[dim].low());
        ret += dimExtent * prevDimsTotal;
        prevDimsTotal *= (size_t)domain[dim].get_extent();
    }
    ret *= sizeof(double);
    return ret;
}

unordered_map<int, r_Minterval> r_Conv_GRIB::getMessageDomainsMap(const Json::Value& messageDomains) throw (r_Error)
{
    if (messageDomains.empty())
    {
        LERROR << "invalid format options, messageDomains must have at least one message domain.";
        throw r_Error(INVALIDFORMATPARAMETER);
    }

    unordered_map<int, r_Minterval> ret;
    for (unsigned int messageIndex = 0; messageIndex < messageDomains.size(); messageIndex++)
    {
        int msgId = messageDomains[messageIndex][FormatParamKeys::Decode::Grib::MESSAGE_ID].asInt();
        const char* msgDomain = messageDomains[messageIndex][FormatParamKeys::Decode::Grib::MESSAGE_DOMAIN].asCString();
        r_Minterval domain = domainStringToMinterval(msgDomain);
        if (!ret.insert(make_pair(msgId, domain)).second)
        {
            LWARNING << "duplicate message domain in format parameters for message id " << msgId << ", ignoring.";
        }
    }
    return ret;
}

r_Minterval r_Conv_GRIB::computeBoundingBox(const unordered_map<int, r_Minterval>& messageDomains) throw (r_Error)
{
    unordered_map<int, r_Minterval>::const_iterator messageDomainIt = messageDomains.begin();
    r_Minterval ret = messageDomainIt->second;
    checkDomain(ret);
    r_Dimension dims = ret.dimension();

    for (++messageDomainIt; messageDomainIt != messageDomains.end(); messageDomainIt++)
    {
        r_Minterval messageDomain = messageDomainIt->second;

        if (messageDomain.dimension() != dims)
        {
            LERROR << "invalid message domains given, mismatched dimension: " <<
                   messageDomain << ", expected " << dims << " dimensions.";
            throw r_Error(r_Error::r_Error_Conversion);
        }
        checkDomain(messageDomain);

        // update dest interval to max/min in order to create the bounding box
        for (unsigned int i = 0; i < dims - 2; i++)
        {
            if (messageDomain[i].high() > ret[i].high())
            {
                ret[i].set_high(messageDomain[i].high());
            }
            if (messageDomain[i].low() < ret[i].low())
            {
                ret[i].set_low(messageDomain[i].low());
            }
        }
        for (unsigned int i = dims - 2; i < dims; i++)
        {
            if (messageDomain[i] != ret[i])
            {
                LERROR << "invalid message domain bound given: " << messageDomain[i] <<
                       "; x/y bounds must be equal in all message domains, expected: " << ret[i];
                throw r_Error(r_Error::r_Error_Conversion);
            }
        }
    }

    return ret;
}

void r_Conv_GRIB::setTargetDomain(const r_Minterval& fullBoundingBox) throw (r_Error)
{
    if (formatParams.getSubsetDomain().dimension() == 0)
    {
        subsetSpecified = false;
        desc.destInterv = fullBoundingBox;
    }
    else
    {
        subsetSpecified = true;
        desc.destInterv = formatParams.getSubsetDomain();
        if (!desc.destInterv.intersects_with(fullBoundingBox))
        {
            LERROR << "invalid subsetDomain parameter '" << desc.destInterv <<
                   "', does not intersect with the file domain '" << fullBoundingBox << "'.";
            throw r_Error(INVALIDFORMATPARAMETER);
        }
    }
}

void r_Conv_GRIB::setTargetDataAndType() throw (r_Error)
{
    r_Area totalSize = desc.destInterv.cell_count() * sizeof(double);
    LDEBUG << "allocating " << totalSize << " bytes for the result array with domain " << desc.destInterv;
    desc.dest = (char*) mymalloc(totalSize);
    if (!desc.dest)
    {
        LERROR << "failed allocating " << totalSize << " bytes of memory.";
        throw r_Error(r_Error::r_Error_MemoryAllocation);
    }
    memset(desc.dest, 0, totalSize);
    desc.destType = get_external_type(ctype_float64);
}

void r_Conv_GRIB::validateMessageDomain(FILE* in, grib_handle* h, int messageIndex,
                                        r_Range messageWidth, r_Range messageHeight, size_t messageArea) throw (r_Error)
{
    long x = 0;
    int err = grib_get_long(h, "Ni", &x);
    VALIDATE_MSG_ERRCODE(err, "failed determining the X size of message " << messageIndex);
    if (x != messageWidth)
    {
        LERROR << "the x grid size of the grib message (Ni) '" << x <<
               "' does not match the x bound specified in the message domains '" << messageWidth << "'";
        grib_handle_delete(h);
        fclose(in);
        throw r_Error(r_Error::r_Error_Conversion);
    }

    long y = 0;
    err = grib_get_long(h, "Nj", &y);
    VALIDATE_MSG_ERRCODE(err, "failed determining the Y size of message " << messageIndex);
    if (y != messageHeight)
    {
        LERROR << "the y grid size of the grib message (Nj) '" << y <<
               "' does not match the y bound specified in the message domains '" << messageHeight << "'";
        grib_handle_delete(h);
        fclose(in);
        throw r_Error(r_Error::r_Error_Conversion);
    }

    size_t valuesLen = 0;
    err = grib_get_size(h, "values", &valuesLen);
    VALIDATE_MSG_ERRCODE(err, "failed determining the number of values in message " << messageIndex);
    if (valuesLen != messageArea)
    {
        LERROR << "the number of values in the grib message '" << valuesLen <<
               "' does not match the number of values specified in the message domains '" << messageArea << "'";
        grib_handle_delete(h);
        fclose(in);
        throw r_Error(r_Error::r_Error_Conversion);
    }
}

void r_Conv_GRIB::decodeSubset(char* messageData, r_Minterval messageDomain, r_Minterval targetDomain,
                               size_t subsetOffset, r_Range subsetWidth, r_Range subsetHeight, size_t subsetArea)
{
    r_Dimension dimNo = targetDomain.dimension();
    // these iterators iterate last dimension first, i.e. minimal step size
    targetDomain.transpose(dimNo - 1, dimNo - 2);
    messageDomain.transpose(dimNo - 1, dimNo - 2);

    r_MiterDirect resTileIter(static_cast<void*>(desc.dest + subsetOffset), targetDomain, targetDomain, sizeof(double));
    r_MiterDirect opTileIter(static_cast<void*>(messageData), messageDomain, targetDomain, sizeof(double));

    r_Range lastDimExtent = targetDomain[dimNo - 1].get_extent();
    while (!resTileIter.isDone())
    {
        // copy entire line (continuous chunk in last dimension) in one go
        memcpy(resTileIter.getData(), opTileIter.getData(), static_cast<size_t>(lastDimExtent) * sizeof(double));
        // force overflow of last dimension
        resTileIter.id[dimNo - 1].pos += lastDimExtent;
        opTileIter.id[dimNo - 1].pos += lastDimExtent;
        // iterate; the last dimension will always overflow now
        ++resTileIter;
        ++opTileIter;
    }
    transpose<double>((double*)(desc.dest + subsetOffset), (double*) messageData, subsetHeight, subsetWidth);
    memcpy((desc.dest + subsetOffset), messageData, subsetArea * sizeof(double));
}

void r_Conv_GRIB::checkDomain(const r_Minterval& domain) throw (r_Error)
{
    r_Dimension xDimIndex = domain.dimension() - 2;
    r_Dimension yDimIndex = domain.dimension() - 1;
    for (unsigned int i = 0; i < domain.dimension() - 2; i++)
    {
        if (!isSlice(domain[i]))
        {
            LERROR << "non x/y bound is not a slice: " << domain[i];
            throw r_Error(r_Error::r_Error_Conversion);
        }
    }
    if (isSlice(domain[xDimIndex]))
    {
        LERROR << "the x bound (second last dimension of the domain) must not be a slice: " << domain[xDimIndex];
        throw r_Error(r_Error::r_Error_Conversion);
    }
    if (isSlice(domain[yDimIndex]))
    {
        LERROR << "the x bound (second last dimension of the domain) must not be a slice: " << domain[yDimIndex];
        throw r_Error(r_Error::r_Error_Conversion);
    }
}

bool r_Conv_GRIB::isSlice(const r_Sinterval& domainAxis)
{
    return domainAxis.get_extent() == 1;
}

r_Minterval r_Conv_GRIB::domainStringToMinterval(const char* domain) throw (r_Error)
{
    try
    {
        return r_Minterval(domain);
    }
    catch (r_Eno_interval& ex)
    {
        LERROR << "invalid domain minterval: " << domain;
        LERROR << ex.what();
        throw r_Error(r_Error::r_Error_Conversion);
    }
}

#else // !HAVE_GRIB

r_Conv_Desc& r_Conv_GRIB::convertFrom(__attribute__ ((unused)) const char* options) throw(r_Error)
{
    LERROR << "support for decoding GRIB file is not supported; rasdaman should be configured with option --with-grib to enable it.";
    throw r_Error(r_Error::r_Error_FeatureNotSupported);
}

r_Conv_Desc& r_Conv_GRIB::convertFrom(__attribute__ ((unused)) r_Format_Params options) throw(r_Error)
{
    LERROR << "support for decoding GRIB file is not supported; rasdaman should be configured with option --with-grib to enable it.";
    throw r_Error(r_Error::r_Error_FeatureNotSupported);
}

#endif // HAVE_GRIB

/// cloning

r_Convertor* r_Conv_GRIB::clone(void) const
{
    return new r_Conv_GRIB(desc.src, desc.srcInterv, desc.baseType);
}

/// identification

const char* r_Conv_GRIB::get_name(void) const
{
    return format_name_grib;
}

r_Data_Format r_Conv_GRIB::get_data_format(void) const
{
    return r_GRIB;
}

template <class baseType>
void r_Conv_GRIB::transpose(baseType* src, baseType* dst, const int N, const int M)
{
    for (int n = 0; n < N * M; n++)
    {
        int i = n / N;
        int j = n % N;
        dst[n] = src[M * j + i];
    }
}
