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
/
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

#include "config.h"
#include "conversion/csv.hh"
#include "raslib/error.hh"
#include "raslib/rminit.hh"
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

#include <stdio.h>
#include <iostream>
#include <stack>

#include "debug/debug-srv.hh"

#define DIM_BOUNDARY -1

using namespace std;

r_Conv_CSV::r_Conv_CSV(const char *src, const r_Minterval &interv, const r_Type *tp) throw(r_Error)
    : r_Convertor(src, interv, tp, true)
{
}



r_Conv_CSV::r_Conv_CSV(const char *src, const r_Minterval &interv, int tp) throw(r_Error)
    : r_Convertor(src, interv, tp)
{
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
void r_Conv_CSV::printValue(std::stringstream &f, const r_Base_Type &type)
{
    if (type.isStructType()) {
        printStructValue(f);
    } else if (type.isComplexType()) {
        printComplexValue(f, type);
    } else if (type.isPrimitiveType()) {
        printPrimitiveValue(f, type);
    } else {
        RMInit::logOut << "r_Conv_CSV::convertTo(): unsupported type " << type.type_id() << endl;
        throw r_Error(r_Error::r_Error_TypeInvalid);
    }
}

void r_Conv_CSV::printStructValue(std::stringstream &f)
{
    r_Structure_Type *st = (r_Structure_Type*) desc.srcType;
    r_Structure_Type::attribute_iterator iter(st->defines_attribute_begin());
    f << STRUCT_DELIMITER_OPEN;
    while (iter != st->defines_attribute_end())
    {
        printValue(f, (*iter).type_of());
        iter++;
        if (iter != st->defines_attribute_end())
            f << STRUCT_DELIMITER_ELEMENT;
    }
    f << STRUCT_DELIMITER_CLOSE;
}

void r_Conv_CSV::printComplexValue(std::stringstream &f, const r_Base_Type &type)
{
    const r_Complex_Type *ptr = (const r_Complex_Type *) &type;
    ptr->print_value(val, f);
    val += ptr->size();
}

void r_Conv_CSV::printPrimitiveValue(std::stringstream &f, const r_Base_Type &type)
{
    const r_Primitive_Type *ptr = (const r_Primitive_Type *) &type;
    switch (ptr->type_id())
    {
    case r_Type::ULONG:
        f << ptr->get_ulong(val);
        break;
    case r_Type::USHORT:
        f << ptr->get_ushort(val);
        break;
    case r_Type::BOOL:
        f << ptr->get_boolean(val) ? "T": "F";
        break;
    case r_Type::LONG:
        f << ptr->get_long(val);
        break;
    case r_Type::SHORT:
        f << ptr->get_short(val);
        break;
    case r_Type::OCTET:
        f << (int) (ptr->get_octet(val));
        break;
    case r_Type::DOUBLE:
        f << ptr->get_double(val);
        break;
    case r_Type::FLOAT:
        f << ptr->get_float(val);
        break;
    case r_Type::CHAR:
        f << (int) (ptr->get_char(val));
        break;
    default:
        f << (int) (ptr->get_char(val));
        break;
    }
    val += ptr->size();
}

void r_Conv_CSV::printArray(std::stringstream &f, int *dims, int dim, const r_Base_Type &type)
{
    for (int i = 0; i < dims[0]; ++i)
    {
        if (dim == 1) {
            printValue(f, type);
        } else {
            f << "{";
            printArray(f, dims + 1, dim - 1, type);
            f << "}";
        }
        if (i < dims[0] - 1)
            f << ",";
    }
}

r_convDesc &r_Conv_CSV::convertTo( const char *options ) throw(r_Error)
{
    ENTER("r_Conv_CSV::convertTo()");

    std::stringstream csvtemp;

    //int size = getTypeSize(desc.baseType);
    int rank, i;
    int *dimsizes;
    rank = desc.srcInterv.dimension();
    char *src = (char*) desc.src;

    dimsizes = new int[rank];

    for (i=0; i<rank; i++)
    {
        dimsizes[i] = desc.srcInterv[i].high() - desc.srcInterv[i].low() + 1;
    }
    const r_Base_Type *base_type = (const r_Base_Type *) desc.srcType;
    val = (char*) desc.src;
    try
    {
        if (rank == 1) {
            csvtemp << "{";
            printArray(csvtemp, dimsizes, rank, *base_type);
            csvtemp << "}";
        } else {
            printArray(csvtemp, dimsizes, rank, *base_type);
        }
        }
    catch (r_Error &err)
    {
        delete [] dimsizes;
        LEAVE("r_Conv_CSV::convertTo()");
        throw err;
    }

    delete [] dimsizes;
    dimsizes=NULL;

    std::string str = csvtemp.str();
    int stringsize = str.length();

    desc.destInterv = r_Minterval(1);
    desc.destInterv << r_Sinterval((r_Range)0, (r_Range)stringsize - 1);

    if ((desc.dest = (char*)mystore.storage_alloc(stringsize)) == NULL)
    {
        RMInit::logOut << "r_Conv_CSV::convertTo(): out of memory error" << endl;
        LEAVE("r_Conv_CSV::convertTo()");
        throw r_Error(MEMMORYALLOCATIONERROR);
    }
    memcpy(desc.dest, str.c_str(), stringsize);

    // Result is just a bytestream
    desc.destType = r_Type::get_any_type("char");

    LEAVE("r_Conv_CSV::convertTo()");
    return desc;
}



r_convDesc &r_Conv_CSV::convertFrom(const char *options) throw(r_Error)
{
    RMInit::logOut << "importing CSV data not yet implemented" << endl;
    throw new r_Error(CONVERSIONFORMATNOTSUPPORTED);
    return desc;
}



const char *r_Conv_CSV::get_name( void ) const
{
    return "csv";
}


r_Data_Format r_Conv_CSV::get_data_format( void ) const
{
    return r_CSV;
}


r_Convertor *r_Conv_CSV::clone( void ) const
{
    return new r_Conv_CSV(desc.src, desc.srcInterv, desc.baseType);
}
