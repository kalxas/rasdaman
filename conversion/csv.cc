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
#include "common/string/stringutil.hh"
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

using namespace std;

const string r_Conv_CSV::ORDER_INNER_OUTER{"inner_outer"};
const string r_Conv_CSV::ORDER_OUTER_INNER{"outer_inner"};
const string r_Conv_CSV::BOOL_TRUE{"true"};
const string r_Conv_CSV::BOOL_FALSE{"false"};

/// internal initialization, common to all constructors
void r_Conv_CSV::initCSV(void)
{
    if (params == NULL)
        params = new r_Parse_Params();
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

r_Conv_Desc& r_Conv_CSV::convertTo(const char* options, const r_Range* nullVal)
{
    if (options)
        processEncodeOptions(string{options});

    updateNodataValue(nullVal);
    
    validateType(desc.srcType);

    if (formatParams.isTranspose())
    {
        transpose(const_cast<char*>(desc.src), desc.srcInterv, desc.srcType, 
                  formatParams.getTranspose());
    }

    std::stringstream resultStream;

    unsigned long rank = desc.srcInterv.dimension();
    // if rank is 0 then we want to allocate at least one value in the below vectors,
    // otherwise we get memory error for scalars with 0 dimension
    auto rankSize = rank > 0 ? rank : rank+1;
    vector<long> dimsizes(rankSize);
    // offsets describe how many data cells are between values of the same dimension slice
    vector<size_t> offsets(rankSize);

    if (rank > 0)
    {
        for (r_Dimension i = 0; i < rank; i++)
            dimsizes[i] = long(desc.srcInterv[i].get_extent());

        offsets[rank - 1] = 1;
        for (unsigned long i = rank - 1; i > 0; --i)
            offsets[i - 1] = offsets[i] * size_t(dimsizes[i]);

        if (order == r_Conv_CSV::INNER_OUTER)
        {
            std::reverse(dimsizes.begin(), dimsizes.end());
            std::reverse(offsets.begin(), offsets.end());
        }
    }

    if (rank == 0)
        outerDelimiters = false;
    
    // fill in resultStream
    if (outerDelimiters)
        resultStream << dimensionStart;
    
    printArray(resultStream, &dimsizes[0], &offsets[0], int(rank), desc.src,
               *static_cast<const r_Base_Type*>(desc.srcType));
    
    if (outerDelimiters)
        resultStream << dimensionEnd;
    
    // transfer to descriptor dest fields
    std::string result = resultStream.str();
    auto resultSize = result.length();

    desc.destInterv = r_Minterval({r_Sinterval(0ll, r_Range(resultSize) - 1)});
    if ((desc.dest = static_cast<char*>(mystore.storage_alloc(resultSize))) == NULL)
    {
        LERROR << "r_Conv_CSV::convertTo(): out of memory error";
        throw r_Error(MEMMORYALLOCATIONERROR);
    }
    memcpy(desc.dest, result.c_str(), resultSize);
    desc.destType = r_Type::get_any_type("char");

    return desc;
}

void r_Conv_CSV::printArray(std::stringstream& f, long* dims, size_t* offsets, int dim,
                            const char* data, const r_Base_Type& type)
{
    size_t typeSize = type.size();

    if (dim == 0)
    {
        printValue(f, type, data, 0);
    }
    else
    {
        for (long i = 0; i < dims[0]; data += offsets[0] * typeSize, ++i)
        {
            if (dim == 1)
            {
                printValue(f, type, data, 0);
            }
            else
            {
                f << dimensionStart;
                printArray(f, dims + 1, offsets + 1, dim - 1, data, type);
                f << dimensionEnd;
            }
            
            if (i < dims[0] - 1)
            {
                f << (dim == 1 ? valueSeparator : dimensionSeparator);
            }
        }
    }
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
const char* r_Conv_CSV::printValue(std::stringstream& f, const r_Base_Type& type, const char* val, size_t band)
{
    if (type.isStructType())
    {
        return printStructValue(f, val);
    }
    else if (type.isComplexType())
    {
        return printComplexValue(f, type, val, band);
    }
    else if (type.isPrimitiveType())
    {
        return printPrimitiveValue(f, type, val, band);
    }
    else
    {
        LERROR << "r_Conv_CSV::convertTo(): unsupported type " << type.type_id();
        throw r_Error(r_Error::r_Error_TypeInvalid);
    }
}

const char* r_Conv_CSV::printStructValue(std::stringstream& f, const char* val)
{
    f << structValueStart;
    bool addDelimiter = false;
    size_t band = 0;
    const r_Structure_Type* st = static_cast<const r_Structure_Type*>(desc.srcType);
    for (const auto& att : st->getAttributes())
    {
        if (addDelimiter)
            f << componentSeparator;
        else
            addDelimiter = true;

        val = printValue(f, att.type_of(), val, band);
        
        ++band;
    }
    f << structValueEnd;
    return val;
}

const char* r_Conv_CSV::printComplexValue(std::stringstream& f, const r_Base_Type& type, const char* val, size_t band)
{
    const r_Complex_Type* ptr = static_cast<const r_Complex_Type*>(&type);
    {
        ptr->print_value(val, f);
    }
    val += componentSizes[band];
    return val;
}

const char* r_Conv_CSV::printPrimitiveValue(std::stringstream& f, const r_Base_Type& type,
                                            const char* val, size_t band)
{
    const r_Primitive_Type* ptr = static_cast<const r_Primitive_Type*>(&type);
    {
        switch (componentTypes[band])
        {
        case r_Type::ULONG:
            f << ptr->get_ulong(val);
            break;
        case r_Type::USHORT:
            f << ptr->get_ushort(val);
            break;
        case r_Type::BOOL:
            f << (ptr->get_boolean(val) ? trueValue : falseValue);
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
            f << std::setprecision(std::numeric_limits<double>::digits10 + 1)
              << ptr->get_double(val);
            break;
        case r_Type::FLOAT:
            f << std::setprecision(std::numeric_limits<float>::digits10 + 1)
              << ptr->get_float(val);
            break;
        case r_Type::CHAR:
            f << static_cast<int>(ptr->get_char(val));
            break;
        default:
            f << static_cast<int>(ptr->get_char(val));
            break;
        }
    }
    val += componentSizes[band];
    return val;
}

void r_Conv_CSV::processEncodeOptions(const string& options)
{
    if (options.empty())
        return;
    
    using namespace FormatParamKeys::Encode::CSV;
    
    string order_option{ORDER_OUTER_INNER};
    
    if (formatParams.parse(options))
    {
        for (const auto& configParam : formatParams.getFormatParameters())
        {
            const auto &key = configParam.first;
            const auto &val = configParam.second;
            if (key == ORDER)
                order_option = val;
            else if (key == ENABLE_NULL)
                enableNull = processBoolOption(ENABLE_NULL, val);
            else if (key == OUTER_DELIMITERS)
                outerDelimiters = processBoolOption(OUTER_DELIMITERS, val);
            else if (key == TRUE_VALUE)
                trueValue = val;
            else if (key == FALSE_VALUE)
                falseValue = val;
            else if (key == NULL_VALUE)
                nullValue = val;
            else if (key == DIMENSION_START)
                dimensionStart = val;
            else if (key == DIMENSION_END)
                dimensionEnd = val;
            else if (key == DIMENSION_SEPARATOR)
                dimensionSeparator = val;
            else if (key == VALUE_SEPARATOR)
                valueSeparator = val;
            else if (key == COMPONENT_SEPARATOR)
                componentSeparator = val;
            else if (key == STRUCT_VALUE_START)
                structValueStart = val;
            else if (key == STRUCT_VALUE_END)
                structValueEnd = val;
            else {
                LERROR << "invalid CSV/JSON option \"" << key << "\"";
                throw r_Error(INVALIDFORMATPARAMETER);
            }
        }
    }
    else
    {
        char* tmp_order_option = NULL;
        params->add(ORDER, &tmp_order_option, r_Parse_Params::param_type_string);
        params->process(options.c_str());
        if (tmp_order_option)
        {
            order_option = string(tmp_order_option);
            delete [] tmp_order_option;
        }
    }
    
    if (order_option == ORDER_OUTER_INNER)
        order = r_Conv_CSV::OUTER_INNER;
    else if (order_option == ORDER_INNER_OUTER)
        order = r_Conv_CSV::INNER_OUTER;
    else
    {
        LERROR << "illegal CSV/JSON option string: \"" << options << "\", expected "
               << ORDER << "=(" << ORDER_OUTER_INNER << "|" << ORDER_INNER_OUTER << ").";
        throw r_Error(INVALIDFORMATPARAMETER);
    }
}

bool r_Conv_CSV::processBoolOption(const std::string& optionKey, const std::string& optionValue) const
{
    auto boolOption = common::StringUtil::toLowerCase(optionValue);
    if (boolOption == BOOL_FALSE)
        return false;
    else if (boolOption == BOOL_TRUE)
        return true;
    else
    {
        LERROR << "illegal value for option \"" << optionKey << "\", expected "
               << boolOption << "=(" << BOOL_TRUE << "|" << BOOL_FALSE << ").";
        throw r_Error(INVALIDFORMATPARAMETER);
    }
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
    
    validateType(desc.destType);

    unsigned int totalSize = 0;
    unsigned int typeSize = ((r_Base_Type*)desc.destType)->size();
    unsigned int numElem = desc.destInterv.cell_count();
    const r_Base_Type* type = static_cast<const r_Base_Type*>(desc.destType);

    totalSize = numElem * typeSize;

    if ((desc.dest = static_cast<char*>(mystore.storage_alloc(totalSize))) == NULL)
    {
        LERROR << "out of memory error";
        throw r_Error(MEMMORYALLOCATIONERROR);
    }

    parseData(*type, numElem);

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

void r_Conv_CSV::parseData(const r_Base_Type& type, unsigned int numElem)
{
    if (type.isPrimitiveType())
    {
        MAKE_SWITCH_TYPEID(type.type_id(), T, CODE(
            parsePrimitive<T>(desc.dest, desc.src, numElem,
                              desc.srcInterv.cell_count());
        ), CODE(throw r_Error(r_Error::r_Error_Conversion);))
    }
    else if (type.isStructType())
    {
        parseStruct(numElem);
    }
    else
    {
        LERROR << "unsupported type " << type.type_id();
        throw r_Error(BASETYPENOTSUPPORTEDBYOPERATION);
    }
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
void parsePrimitive(char* dest, const char* src, unsigned int numElem, size_t srcSize)
{
    size_t srcIndex = 0;
    unsigned int countVal = 0;
    T* destT = reinterpret_cast<T*>(dest);

    while (countVal < numElem)
    {
        srcIndex = skipToValueBegin(src, srcSize, srcIndex);
        auto srcIndexBegin = srcIndex;
        srcIndex = skipToValueEnd(src, srcSize, srcIndex);
        auto valueLen = srcIndex - srcIndexBegin;
        if (valueLen > 0)
        {
            try
            {
                *destT = cast_from_string<T>(&src[srcIndexBegin], valueLen);
            }
            catch (boost::bad_lexical_cast&)
            {
                LWARNING << "Failed decoding value, will be ignored: '"
                         << string(&src[srcIndexBegin], valueLen) << "'";
            }
            ++countVal;
            ++destT;
        }
        else
        {
            LERROR << "wrong number of values, read " << countVal
                   << ", but expected " << numElem;
            throw r_Error(r_Error::r_Error_Conversion);
        }
    }
}

void r_Conv_CSV::parseStruct(unsigned int numElem)
{
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
                       *reinterpret_cast<T*>(dest) =
                          cast_from_string<T>(&src[srcIndexBegin], valueLen);
                    ), CODE(throw r_Error(r_Error::r_Error_Conversion);))
                }
                catch (boost::bad_lexical_cast&)
                {
                    LWARNING << "Failed decoding value, will be ignored: '"
                             << string(&src[srcIndexBegin], valueLen) << "'";
                }
                ++countVal;
                dest += componentSizes[j];
            }
            else
            {
                LERROR << "wrong number of values, read " << countVal 
                       << ", but expected " << (numElem * componentTypes.size());
                throw r_Error(r_Error::r_Error_Conversion);
            }
        }
    }
}

void r_Conv_CSV::processDecodeOptions(const string& options)
{
    // process the arguments domain and basetype
    const auto &dataDomainKey = FormatParamKeys::Decode::CSV::DATA_DOMAIN;
    const auto &baseTypeKey = FormatParamKeys::Decode::CSV::BASETYPE;
    
    if (formatParams.parse(options))
    {
        for (const auto& configParam : formatParams.getFormatParameters())
        {
            if (configParam.first == baseTypeKey)
                basetype = configParam.second;
            else if (configParam.first == dataDomainKey)
                domain = configParam.second;
        }
    }
    else
    {
        char* domainStr = NULL;
        char* basetypeStr = NULL;
        params->add(dataDomainKey, &domainStr, r_Parse_Params::param_type_string);
        params->add(baseTypeKey, &basetypeStr, r_Parse_Params::param_type_string);
        params->process(options.c_str(), ';', true);
        if (domainStr)
        {
            domain = string{domainStr};
            delete[] domainStr;
        }
        if (basetypeStr)
        {
            basetype = string{basetypeStr};
            delete[] basetypeStr;
        }
    }
    if (domain.empty())
    {
        LERROR << "mandatory parameter '" << dataDomainKey << "' must be specified.";
        throw r_Error(INVALIDFORMATPARAMETER);
    }
    if (basetype.empty())
    {
        LERROR << "mandatory parameter '" << baseTypeKey << "' must be specified.";
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
        ++srcIndex;
    return srcIndex;
}
size_t skipToValueEnd(const char* src, size_t srcSize, size_t srcIndex)
{
    // skip valid characters
    while (isValidCharacter(src[srcIndex]) && srcIndex < srcSize)
        ++srcIndex;
    return srcIndex;
}

void r_Conv_CSV::validateType(const r_Type *type)
{
  if (type->isStructType())
  {
      const auto* st = static_cast<const r_Structure_Type*>(type);
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
  }
  else
  {
      const auto* st = static_cast<const r_Base_Type*>(type);
      componentTypes.push_back(st->type_id());
      componentSizes.push_back(st->size());
  }
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
