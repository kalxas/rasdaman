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
 *
 * Provides functions to transpose rasdaman-stored data.
 *
 * Created on February 3, 2017, 1:54 PM
 */

#ifndef TRANSPOSE_HH
#define TRANSPOSE_HH


#include "raslib/minterval.hh"
#include "raslib/basetype.hh"

//find size of data type of an r_Type object in case it can be recast to r_Base_Type
//e.g. srcType and destType in r_Conv_Desc
int dataTypeSize(r_Type* base_type);
//transpose the last two dimensions of data via a temporary 2D object dataTemp
void transposeLastTwo(char* data, r_Minterval& dimData, r_Type* dataType);

#endif /* TRANSPOSE_HH */
