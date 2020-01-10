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
 * Provides functions to convert data to CSV SD and back.
 * 2011-may-24  DM          added support for structured types
 * 2012-feb-05  DM          convert recursive printing to iterative
 */

/* Added by Sorin Stancu-Mara. Definition clashed for type int8, define in both
* /usr/include/csv.h and in /usr/include/tiff.h
* This will supress the tiff.h definition.
* Both definitions are similar
*/
#define HAVE_INT8

#include "csv.hh"
#include "config.h"
#include "transpose.hh"
#include "formatparamkeys.hh"
#include "raslib/error.hh"
#include "raslib/parseparams.hh"
#include "raslib/primitivetype.hh"
#include "raslib/structuretype.hh"
#include "raslib/complextype.hh"
#include "raslib/type.hh"
#include <logging.hh>

#include <iostream>
#include <fstream>
#include <cstring>
#include <string>
#include <sstream>
#include <algorithm>
#include <stdio.h>
#include <stack>
#include <limits>
#include <iomanip>
#include <boost/lexical_cast.hpp>

#define STRUCT_DELIMITER_OPEN "\""
#define STRUCT_DELIMITER_CLOSE "\""
#define STRUCT_DELIMITER_ELEMENT " "

#define DIM_BOUNDARY -1

#define ORDER_OUTER_INNER "outer_inner"
#define ORDER_INNER_OUTER "inner_outer"

using namespace std;

const char* r_Conv_CSV::FALSE = "f";
const char* r_Conv_CSV::TRUE = "t";

const string r_Conv_CSV::LEFT_PAREN{"{"};
const string r_Conv_CSV::RIGHT_PAREN{"}"};
const string r_Conv_CSV::SEPARATOR{","};

/// internal initialization, common to all constructors
void r_Conv_CSV::initCSV(void)
{
    if (params == NULL)
    {
        params = new r_Parse_Params();
    }

    leftParen = LEFT_PAREN;
    rightParen = RIGHT_PAREN;
    valueSeparator = SEPARATOR;
    outerParens = false;
}


r_Conv_CSV::r_Conv_CSV(const char* src, const r_Minterval& interv, const r_Type* tp)
    : r_Convertor(src, interv, tp, true)
{
    initCSV();
}



r_Conv_CSV::r_Conv_CSV(const char* src, const r_Minterval& interv, int tp)
    : r_Convertor(src, interv, tp)
{
    initCSV();
}

r_Conv_CSV::~r_Conv_CSV(void)
{
}

/**
 * The format written to the stream is of the following format:
 * Each dimension is surrounded by braces {} and points are separated by a comma
 * while each band value in a point is delimited by a space
 *
 * Example:
 * For a rgb image:
 *   {100 210 222, 50 10 25},
 *   {120 314 523, 25 30 45}
 * For a grey cube of [0:2,0:2,0:2]
 *   {{6,2,2},{2,2,32},{2,32,2}},
 *   {{2,1,2},{2,7,22},{12,2,42}},
 *   {{12,26,62},{23,2,21},{2,2,2}}
 *
 * Please note that the implementation of the tupleList GML elements in Petascope is dependent
 * on this format so on change update RasUtil as well.
 */
const char* r_Conv_CSV::printValue(std::stringstream& f, const r_Base_Type& type, const char* val)
{
    if (type.isStructType())
    {
        return printStructValue(f, val);
    }
    else if (type.isComplexType())
    {
        return printComplexValue(f, type, val);
    }
    else if (type.isPrimitiveType())
    {
        return printPrimitiveValue(f, type, val);
    }
    else
    {
        LERROR << "r_Conv_CSV::convertTo(): unsupported type " << type.type_id();
        throw r_Error(r_Error::r_Error_TypeInvalid);
    }
}

const char* r_Conv_CSV::printStructValue(std::stringstream& f, const char* val)
{
    r_Structure_Type* st = static_cast<r_Structure_Type*>(const_cast<r_Type*>(desc.srcType));
    f << STRUCT_DELIMITER_OPEN;
    bool addDelimiter = false;
    for (const auto& att : static_cast<const r_Structure_Type*>(desc.srcType)->getAttributes())
    {
        if (addDelimiter)
        {
            f << STRUCT_DELIMITER_ELEMENT;
        }
        else
        {
            addDelimiter = true;
        }
        val = printValue(f, att.type_of(), val);
    }
    f << STRUCT_DELIMITER_CLOSE;
    return val;
}

const char* r_Conv_CSV::printComplexValue(std::stringstream& f, const r_Base_Type& type, const char* val)
{
    const r_Complex_Type* ptr = static_cast<const r_Complex_Type*>(&type);
    ptr->print_value(val, f);
    val += ptr->size();
    return val;
}

const char* r_Conv_CSV::printPrimitiveValue(std::stringstream& f, const r_Base_Type& type, const char* val)
{
    const r_Primitive_Type* ptr = static_cast<const r_Primitive_Type*>(&type);
    switch (ptr->type_id())
    {
    case r_Type::ULONG:
        f << ptr->get_ulong(val);
        break;
    case r_Type::USHORT:
        f << ptr->get_ushort(val);
        break;
    case r_Type::BOOL:
        f << (ptr->get_boolean(val) ? TRUE : FALSE);
        break;
    case r_Type::LONG:
        f << ptr->get_long(val);
        break;
    case r_Type::SHORT:
        f << ptr->get_short(val);
        break;
    case r_Type::OCTET:
        f << static_cast<int>(ptr->get_octet(val));
        break;
    case r_Type::DOUBLE:
        f << std::setprecision(std::numeric_limits<double>::digits10 + 1) << ptr->get_double(val);
        break;
    case r_Type::FLOAT:
        f << std::setprecision(std::numeric_limits<float>::digits10 + 1) << ptr->get_float(val);
        break;
    case r_Type::CHAR:
        f << static_cast<int>(ptr->get_char(val));
        break;
    default:
        f << static_cast<int>(ptr->get_char(val));
        break;
    }
    val += ptr->size();
    return val;
}

void r_Conv_CSV::printArray(std::stringstream& f, int* dims, size_t* offsets, int dim,
                            const char* ptr, const r_Base_Type& type)
{
    size_t typeSize = type.size();

    if (dim == 0)
    {
        printValue(f, type, ptr);
    }
    else
    {
        for (int i = 0; i < dims[0]; ptr += offsets[0] * typeSize, ++i)
        {
            if (dim == 1)
            {
                printValue(f, type, ptr);
            }
            else
            {
                f << leftParen;
                printArray(f, dims + 1, offsets + 1, dim - 1, ptr, type);
                f << rightParen;
            }
            if (i < dims[0] - 1)
            {
                f << valueSeparator;
            }
        }
    }
}

void r_Conv_CSV::processEncodeOptions(const string& options)
{
    if (options.empty())
    {
        return;
    }
    string order_option{ORDER_OUTER_INNER};
    if (formatParams.parse(options))
    {
        for (const pair<string, string>& configParam : formatParams.getFormatParameters())
            if (configParam.first == FormatParamKeys::Encode::CSV::ORDER)
            {
                order_option = configParam.second;
            }
    }
    else
    {
        char* tmp_order_option = NULL;
        params->add(FormatParamKeys::Encode::CSV::ORDER, &tmp_order_option, r_Parse_Params::param_type_string);
        params->process(options.c_str());
        if (tmp_order_option)
        {
            order_option = string(tmp_order_option);
            delete [] tmp_order_option;
        }
    }
    if (order_option == ORDER_OUTER_INNER)
    {
        order = r_Conv_CSV::OUTER_INNER;
    }
    else if (order_option == ORDER_INNER_OUTER)
    {
        order = r_Conv_CSV::INNER_OUTER;
    }
    else
    {
        LERROR << "illegal CSV option string: \"" << options << "\", "
               << "only " << FormatParamKeys::Encode::CSV::ORDER << "=(" ORDER_OUTER_INNER "|" ORDER_INNER_OUTER ") "
               << "is supported.";
        throw r_Error(INVALIDFORMATPARAMETER);
    }
}

void r_Conv_CSV::processDecodeOptions(const string& options)
{
    // process the arguments domain and basetype
    if (formatParams.parse(options))
    {
        for (const pair<string, string>& configParam : formatParams.getFormatParameters())
        {
            if (configParam.first == FormatParamKeys::Decode::CSV::BASETYPE)
            {
                basetype = configParam.second;
            }
            else if (configParam.first == FormatParamKeys::Decode::CSV::DATA_DOMAIN)
            {
                domain = configParam.second;
            }
        }
    }
    else
    {
        char* domainStr = NULL;
        char* basetypeStr = NULL;
        params->add(FormatParamKeys::Decode::CSV::DATA_DOMAIN, &domainStr, r_Parse_Params::param_type_string);
        params->add(FormatParamKeys::Decode::CSV::BASETYPE, &basetypeStr, r_Parse_Params::param_type_string);
        params->process(options.c_str(), ';', true);
        if (domainStr)
        {
            domain = string{domainStr};
            delete[] domainStr;
            domainStr = NULL;
        }
        if (basetypeStr)
        {
            basetype = string{basetypeStr};
            delete[] basetypeStr;
            basetypeStr = NULL;
        }
    }
    if (domain.empty())
    {
        LERROR << "mandatory parameter '" << FormatParamKeys::Decode::CSV::DATA_DOMAIN << "' must be specified.";
        throw r_Error(INVALIDFORMATPARAMETER);
    }
    if (basetype.empty())
    {
        LERROR << "mandatory parameter '" << FormatParamKeys::Decode::CSV::BASETYPE << "' must be specified.";
        throw r_Error(INVALIDFORMATPARAMETER);
    }
}

bool isValidCharacter(char c)
{
    return c != '{' && c != '}' && c != ',' && c != '"' && c != '\'' &&
           c != '(' && c != ')' && c != '[' && c != ']' && !isspace(c) && isprint(c);
}
size_t skipToValueBegin(const char* src, size_t srcSize, size_t srcIndex)
{
    // skip invalid characters
    while (!isValidCharacter(src[srcIndex]) && srcIndex < srcSize)
    {
        ++srcIndex;
    }
    return srcIndex;
}
size_t skipToValueEnd(const char* src, size_t srcSize, size_t srcIndex)
{
    // skip valid characters
    while (isValidCharacter(src[srcIndex]) && srcIndex < srcSize)
    {
        ++srcIndex;
    }
    return srcIndex;
}

// Reason for this wrapping around:
// boost::lexical_cast doesn't work well for char/octet, so these
// have to be handled specially through an int
template<class T>
T cast_from_string(const char* src, size_t len)
{
    return boost::lexical_cast<T>(src, len);
}
template<>
std::int8_t cast_from_string(const char* src, size_t len)
{
    return static_cast<std::int8_t>(cast_from_string<int>(src, len));
}
template<>
std::uint8_t cast_from_string(const char* src, size_t len)
{
    return static_cast<std::uint8_t>(cast_from_string<int>(src, len));
}

template<class T>
void constructPrimitive(char* dest, const char* src, unsigned int numElem, size_t srcSize)
{
    size_t srcIndex = 0, srcIndexBegin = 0, valueLen = 0;
    unsigned int countVal = 0;
    T* destT = reinterpret_cast<T*>(dest);

    while (countVal < numElem)
    {
        srcIndex = skipToValueBegin(src, srcSize, srcIndex);
        srcIndexBegin = srcIndex;
        srcIndex = skipToValueEnd(src, srcSize, srcIndex);
        valueLen = srcIndex - srcIndexBegin;
        if (valueLen > 0)
        {
            try
            {
                *destT = cast_from_string<T>(&src[srcIndexBegin], valueLen);
            }
            catch (boost::bad_lexical_cast& ex)
            {
                LWARNING << "Failed decoding value, will be ignored: '"
                         << string(&src[srcIndexBegin], valueLen) << "'";
            }
            ++countVal;
            ++destT;
        }
        else
        {
            LERROR << "wrong number of values, read " << countVal << ", but expected " << numElem;
            throw r_Error(r_Error::r_Error_Conversion);
        }
    }
}

void r_Conv_CSV::constructStruct(unsigned int numElem)
{
    r_Structure_Type* st = static_cast<r_Structure_Type*>(desc.destType);
    vector<r_Type::r_Type_Id> componentTypes;
    vector<size_t> componentSizes;

    LDEBUG << "Decoding struct data..";
    for (const auto& att : st->getAttributes())
    {
        if (!att.type_of().isPrimitiveType())
        {
            LERROR << "unsupported attribute type " << (att.type_of()).type_id();
            throw r_Error(BASETYPENOTSUPPORTEDBYOPERATION);
        }
        componentTypes.push_back(att.type_of().type_id());
        componentSizes.push_back(att.type_of().size());
        LDEBUG << "component of type " << componentTypes.back() << ", size " << componentSizes.back();
    }

    size_t srcSize = desc.srcInterv.cell_count();
    const char* src = desc.src;
    char* dest = desc.dest;

    size_t srcIndex = 0, srcIndexBegin = 0, valueLen = 0;
    unsigned int countVal = 0;
    for (unsigned int i = 0; i < numElem; ++i)
    {
        for (size_t j = 0; j < componentTypes.size(); ++j)
        {
            srcIndex = skipToValueBegin(src, srcSize, srcIndex);
            srcIndexBegin = srcIndex;
            srcIndex = skipToValueEnd(src, srcSize, srcIndex);
            valueLen = srcIndex - srcIndexBegin;
            if (valueLen > 0)
            {
                try
                {
                    MAKE_SWITCH_TYPEID(componentTypes[j], T, CODE(
                       *reinterpret_cast<T*>(dest) = cast_from_string<T>(&src[srcIndexBegin], valueLen);
                    ), CODE(throw r_Error(r_Error::r_Error_Conversion);))
                }
                catch (boost::bad_lexical_cast& ex)
                {
                    LWARNING << "Failed decoding value, will be ignored: '"
                             << string(&src[srcIndexBegin], valueLen) << "'";
                }
                ++countVal;
                dest += componentSizes[j];
            }
            else
            {
                LERROR << "wrong number of values, read " << countVal << ", but expected "
                       << (numElem * componentTypes.size());
                throw r_Error(r_Error::r_Error_Conversion);
            }
        }
    }
}

void r_Conv_CSV::constructDest(const r_Base_Type& type, unsigned int numElem)
{
    if (type.isPrimitiveType())
    {
        MAKE_SWITCH_TYPEID(type.type_id(), T, CODE(
            constructPrimitive<T>(desc.dest, desc.src, numElem, desc.srcInterv.cell_count());
        ), CODE(throw r_Error(r_Error::r_Error_Conversion);))
    }
    else if (type.isStructType())
    {
        constructStruct(numElem);
    }
    else
    {
        LERROR << "unsupported type " << type.type_id();
        throw r_Error(BASETYPENOTSUPPORTEDBYOPERATION);
    }
}

r_Conv_Desc& r_Conv_CSV::convertTo(const char* options, const r_Range* nullValue)
{
    order = r_Conv_CSV::OUTER_INNER;
    if (options)
    {
        processEncodeOptions(string{options});
    }
    updateNodataValue(nullValue);

    // if selected, transposes rasdaman data before converting to csv
    if (formatParams.isTranspose())
    {
        transpose(const_cast<char*>(desc.src), desc.srcInterv, desc.srcType, formatParams.getTranspose());
    }

    std::stringstream csvtemp;

    unsigned long rank, i;
    rank = desc.srcInterv.dimension();

    vector<int> dimsizes(rank);
    vector<size_t> offsets(rank); // offsets describe how many data cells are between
    // values of the same dimension slice

    if (rank > 0)
    {
        for (i = 0; i < rank; i++)
        {
            dimsizes[i] = desc.srcInterv[i].high() - desc.srcInterv[i].low() + 1;
        }

        offsets[rank - 1] = 1;

        for (i = rank - 1; i > 0; --i)
        {
            size_t dimSize = static_cast<size_t>(dimsizes[i]);
            offsets[i - 1] = offsets[i] * dimSize;
        }

        if (order == r_Conv_CSV::INNER_OUTER)
        {
            std::reverse(dimsizes.begin(), dimsizes.end());
            std::reverse(offsets.begin(), offsets.end());
        }
    }

    const r_Base_Type* base_type = static_cast<const r_Base_Type*>(desc.srcType);
    try
    {
        if (rank == 0)
        {
            outerParens = false;
        }
        if (outerParens)
        {
            csvtemp << leftParen;
        }
        printArray(csvtemp, &dimsizes[0], &offsets[0], rank, const_cast<char*>(desc.src), *base_type);
        if (outerParens)
        {
            csvtemp << rightParen;
        }
    }
    catch (r_Error& err)
    {
        throw err;
    }

    std::string str = csvtemp.str();
    int stringsize = str.length();

    desc.destInterv = r_Minterval(1);
    desc.destInterv << r_Sinterval(static_cast<r_Range>(0), static_cast<r_Range>(stringsize) - 1);

    if ((desc.dest = static_cast<char*>(mystore.storage_alloc(static_cast<size_t>(stringsize)))) == NULL)
    {
        LERROR << "r_Conv_CSV::convertTo(): out of memory error";
        throw r_Error(MEMMORYALLOCATIONERROR);
    }
    memcpy(desc.dest, str.c_str(), static_cast<size_t>(stringsize));

    // Result is just a bytestream
    desc.destType = r_Type::get_any_type("char");

    return desc;
}

r_Conv_Desc& r_Conv_CSV::convertFrom(const char* options)
{
    if (options)
    {
        processDecodeOptions(string{options});
    }
    else
    {
        LERROR << "mandatory decode format options missing: '" <<
               FormatParamKeys::Decode::CSV::BASETYPE << "' and '" <<
               FormatParamKeys::Decode::CSV::DATA_DOMAIN << "'.";
        throw r_Error(INVALIDFORMATPARAMETER);
    }

    desc.destInterv = r_Minterval(domain.c_str());
    desc.destType = r_Type::get_any_type(basetype.c_str());
    desc.dest = NULL;

    unsigned int totalSize = 0;
    unsigned int typeSize = ((r_Base_Type*)desc.destType)->size();
    unsigned int numElem = desc.destInterv.cell_count();
    const r_Base_Type* type = static_cast<const r_Base_Type*>(desc.destType);

    totalSize = numElem * typeSize;

    if ((desc.dest = static_cast<char*>(mystore.storage_alloc(totalSize))) == NULL)
    {
        LERROR << "out of memory error!";
        throw r_Error(MEMMORYALLOCATIONERROR);
    }

    constructDest(*type, numElem);

    // if selected, transposes rasdaman data after converting from csv
    if (formatParams.isTranspose())
    {
        transpose(desc.dest, desc.destInterv, (const r_Type*) desc.destType, formatParams.getTranspose());
    }

    return desc;
}

r_Conv_Desc& r_Conv_CSV::convertFrom(__attribute__((unused)) r_Format_Params options)
{
    throw r_Error(r_Error::r_Error_FeatureNotSupported);
}

const char* r_Conv_CSV::get_name(void) const
{
    return format_name_csv;
}


r_Data_Format r_Conv_CSV::get_data_format(void) const
{
    return r_CSV;
}

r_Convertor* r_Conv_CSV::clone(void) const
{
    return new r_Conv_CSV(desc.src, desc.srcInterv, desc.baseType);
}
