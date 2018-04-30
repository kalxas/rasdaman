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
 * SOURCE: csv.cc
 *
 * MODULE:  conversion
 *
 * CLASSES: r_Conv_CSV
 *
 * COMMENTS:
 *
 * Provides functions to convert data to CSV SD and back.
 *
*/

/* Added by Sorin Stancu-Mara. Definition clashed for type int8, define in both
* /usr/include/csv.h and in /usr/include/tiff.h
* This will supress the tiff.h definition.
* Both definitions are similar
*
* 2011-may-24  DM          added support for structured types
* 2012-feb-05  DM          convert recursive printing to iterative
*/
#define HAVE_INT8
#define STRUCT_DELIMITER_OPEN "\""
#define STRUCT_DELIMITER_CLOSE "\""
#define STRUCT_DELIMITER_ELEMENT " "

#include <limits>
#include <iomanip>
#include "config.h"
#include "conversion/csv.hh"
#include "raslib/error.hh"
#include "raslib/parseparams.hh"
#include "raslib/primitivetype.hh"
#include "raslib/structuretype.hh"
#include "raslib/complextype.hh"
#include <iostream>
#include <fstream>
#include <cstring>
#include <string>
#include <sstream>

#include "csv.hh"

#include <algorithm>
#include <stdio.h>
#include <iostream>
#include <stack>

#include "debug/debug-srv.hh"
#include "formatparamkeys.hh"
#include <logging.hh>

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


r_Conv_CSV::r_Conv_CSV(const char* src, const r_Minterval& interv, const r_Type* tp) throw(r_Error)
    : r_Convertor(src, interv, tp, true)
{
    initCSV();
}



r_Conv_CSV::r_Conv_CSV(const char* src, const r_Minterval& interv, int tp) throw(r_Error)
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
        LFATAL << "r_Conv_CSV::convertTo(): unsupported type " << type.type_id();
        throw r_Error(r_Error::r_Error_TypeInvalid);
    }
}

const char* r_Conv_CSV::printStructValue(std::stringstream& f, const char* val)
{
    r_Structure_Type* st = static_cast<r_Structure_Type*>(const_cast<r_Type*>(desc.srcType));
    r_Structure_Type::attribute_iterator iter(st->defines_attribute_begin());
    f << STRUCT_DELIMITER_OPEN;
    while (iter != st->defines_attribute_end())
    {
        val = printValue(f, (*iter).type_of(), val);
        iter++;
        if (iter != st->defines_attribute_end())
        {
            f << STRUCT_DELIMITER_ELEMENT;
        }
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
    
    if(dim == 0)
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
    char* order_option = NULL;    
    bool allocated = false;
    if (formatParams.parse(options))
    {
        for (const pair<string, string>& configParam : formatParams.getFormatParameters())
        {
            if (configParam.first == FormatParamKeys::Encode::CSV::ORDER)
            {
                order_option = const_cast<char*>(configParam.second.c_str());
            }
        }
    }
    else
    {
        params->add(FormatParamKeys::Encode::CSV::ORDER, &order_option, r_Parse_Params::param_type_string);
        params->process(options.c_str());
        allocated = true;
    }

    if (order_option && strcmp(order_option, ORDER_OUTER_INNER) == 0)
    {
        order = r_Conv_CSV::OUTER_INNER;
    }
    else if (order_option && strcmp(order_option, ORDER_INNER_OUTER) == 0)
    {
        order = r_Conv_CSV::INNER_OUTER;
    }
    else
    {
        LFATAL << "illegal CSV option string: \"" << options << "\", "
               << "only " << FormatParamKeys::Encode::CSV::ORDER << "=(" ORDER_OUTER_INNER "|" ORDER_INNER_OUTER ") "
               << "is supported.";
        throw r_Error(INVALIDFORMATPARAMETER);
    }

    if (allocated && order_option)
    {
        delete [] order_option;
        order_option = NULL;
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
        LFATAL << "mandatory parameter '" << FormatParamKeys::Decode::CSV::DATA_DOMAIN << "' must be specified.";
        throw r_Error(INVALIDFORMATPARAMETER);
    }
    if (basetype.empty())
    {
        LFATAL << "mandatory parameter '" << FormatParamKeys::Decode::CSV::BASETYPE << "' must be specified.";
        throw r_Error(INVALIDFORMATPARAMETER);
    }
}

bool isValidCharacter(char c)
{
    if (c == '{' || c == '}' || c == ',' || c == '"' || c == '\'' ||
            c == '(' || c == ')' || c == '[' || c == ']' || isspace(c) || !isprint(c))
    {
        return false;
    }
    else
    {
        return true;
    }
}

template<class T>
void addElem(std::istringstream& str, char** dest)
{
    T* tmpDest = (T*)(*dest);
    T value;
    char charToken = ' ';

    while (!isValidCharacter(charToken))
    {
        str >> charToken;
    }
    str.putback(charToken);
    str >> value;
    *tmpDest = value;
    *dest += sizeof(value);
}

void addCharElem(std::istringstream& str, char** dest)
{
    char* tmpDest = (char*)(*dest);
    short value;
    char charToken = ' ';

    while (!isValidCharacter(charToken))
    {
        str >> charToken;
    }
    str.putback(charToken);
    str >> value;
    *tmpDest = value;
    *dest += sizeof(char);
}

void r_Conv_CSV::addStructElem(char** dest, r_Structure_Type& st, std::istringstream& str)
{
    r_Structure_Type::attribute_iterator iter(st.defines_attribute_begin());
    while (iter != st.defines_attribute_end())
    {
        if (((*iter).type_of()).isStructType())
        {
            addStructElem(dest, (r_Structure_Type&)const_cast<r_Base_Type&>((*iter).type_of()), str);
        }
        else
        {
            if (((*iter).type_of()).isPrimitiveType())
            {
                switch (((*iter).type_of()).type_id())
                {
                case r_Type::ULONG:
                    addElem<r_ULong>(str, dest);
                    break;
                case r_Type::USHORT:
                    addElem<r_UShort>(str, dest);
                    break;
                case r_Type::BOOL:
                    LFATAL << "r_Conv_CSV::convertFrom: unsupported primitive type " << ((*iter).type_of()).type_id();
                    throw r_Error(BASETYPENOTSUPPORTEDBYOPERATION);
                    break;
                case r_Type::LONG:
                    addElem<r_Long>(str, dest);
                    break;
                case r_Type::SHORT:
                    addElem<r_Short>(str, dest);
                    break;
                case r_Type::OCTET:
                    addElem<r_Octet>(str, dest);
                    break;
                case r_Type::DOUBLE:
                    addElem<r_Double>(str, dest);
                    break;
                case r_Type::FLOAT:
                    addElem<r_Float>(str, dest);
                    break;
                case r_Type::CHAR:
                    addCharElem(str, dest);
                    break;
                default:
                    LFATAL << "r_Conv_CSV::convertFrom: unsupported primitive type for structure attribute " << ((*iter).type_of()).type_id();
                    throw r_Error(BASETYPENOTSUPPORTEDBYOPERATION);
                }
            }
            else
            {
                LFATAL << "r_Conv_CSV::convertFrom: unsupported attribute type " << ((*iter).type_of()).type_id();
                throw r_Error(BASETYPENOTSUPPORTEDBYOPERATION);
            }
        }
        iter++;
    }
}

void r_Conv_CSV::constructStruct(unsigned int numElem)
{
    r_Structure_Type* st = static_cast<r_Structure_Type*>(desc.destType);

    std::string s(desc.src);
    istringstream str(s);

    char charToken;
    unsigned int countVal = 0;
    char* tmpDest = desc.dest;

    while (str >> charToken && countVal < numElem)
    {
        if (isValidCharacter(charToken))
        {
            str.putback(charToken);
            addStructElem(&tmpDest, *st, str);
            countVal++;
        }
    }
    if (countVal != numElem)
    {
        LFATAL << "r_Conv_CSV::convertFrom(): wrong number of values!";
        throw r_Error(r_Error::r_Error_General);
    }
}

template<class T>
void constructPrimitive(char* dest, const char* src, unsigned int numElem)
{
    T* tmpDest;
    char charToken;
    T value;
    tmpDest = (T*)dest;

    //istringstream used to read the csv values ignoring all other characters
    std::string srcString(src);
    istringstream str(srcString);
    unsigned int countVal = 0;

    while (str >> charToken && countVal < numElem)
    {
        if (isValidCharacter(charToken))
        {
            str.putback(charToken);
            str >> value;
            countVal++;
            *tmpDest = value;
            tmpDest++;
        }
    }

    if (countVal != numElem)
    {
        LFATAL << "r_Conv_CSV::convertFrom(): wrong number of values!";
        throw r_Error(r_Error::r_Error_General);
    }
}

void r_Conv_CSV::constructDest(const r_Base_Type& type, unsigned int numElem)
{
    if (type.isPrimitiveType())
    {
        switch (type.type_id())
        {
        case r_Type::ULONG:
            constructPrimitive<r_ULong>(desc.dest, desc.src, numElem);
            break;
        case r_Type::USHORT:
            constructPrimitive<r_UShort>(desc.dest, desc.src, numElem);
            break;
        case r_Type::BOOL:
            LFATAL << "r_Conv_CSV::convertFrom: unsupported primitive type " << type.type_id();
            throw r_Error(BASETYPENOTSUPPORTEDBYOPERATION);
            break;
        case r_Type::LONG:
            constructPrimitive<r_Long>(desc.dest, desc.src, numElem);
            break;
        case r_Type::SHORT:
            constructPrimitive<r_Short>(desc.dest, desc.src, numElem);
            break;
        case r_Type::OCTET:
            constructPrimitive<r_Octet>(desc.dest, desc.src, numElem);
            break;
        case r_Type::DOUBLE:
            constructPrimitive<r_Double>(desc.dest, desc.src, numElem);
            break;
        case r_Type::FLOAT:
            constructPrimitive<r_Float>(desc.dest, desc.src, numElem);
            break;
        case r_Type::CHAR:
            constructPrimitive<r_Char>(desc.dest, desc.src, numElem);
            break;
        default:
            LFATAL << "r_Conv_CSV::convertFrom: unsupported primitive type " << type.type_id();
            throw r_Error(BASETYPENOTSUPPORTEDBYOPERATION);
        }
    }
    else if (type.isStructType())
    {
        constructStruct(numElem);
    }
    else
    {
        LFATAL << "r_Conv_CSV::convertFrom: unsupported type " << type.type_id();
        throw r_Error(BASETYPENOTSUPPORTEDBYOPERATION);
    }
}

r_Conv_Desc& r_Conv_CSV::convertTo(const char* options,
                                   const r_Range* nullValue) throw(r_Error)
{
    order = r_Conv_CSV::OUTER_INNER;
    if (options)
    {
        processEncodeOptions(string{options});
    }
    updateNodataValue(nullValue);
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
            size_t dimSize = static_cast<size_t> (dimsizes[i]);
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
        if(rank == 0){
            outerParens = false;
        }
        if (outerParens) 
        {
            csvtemp << leftParen;
        }
        printArray(csvtemp, &dimsizes[0], &offsets[0], rank, const_cast<char*> (desc.src), *base_type);
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
        LFATAL << "r_Conv_CSV::convertTo(): out of memory error";
        throw r_Error(MEMMORYALLOCATIONERROR);
    }
    memcpy(desc.dest, str.c_str(), static_cast<size_t>(stringsize));

    // Result is just a bytestream
    desc.destType = r_Type::get_any_type("char");

    return desc;
}

r_Conv_Desc& r_Conv_CSV::convertFrom(const char* options) throw(r_Error)
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
        LFATAL << "r_Conv_CSV::convertFrom(): out of memory error!";
        throw r_Error(MEMMORYALLOCATIONERROR);
    }

    constructDest(*type, numElem);

    return desc;
}

r_Conv_Desc& r_Conv_CSV::convertFrom(__attribute__ ((unused)) r_Format_Params options) throw(r_Error)
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
