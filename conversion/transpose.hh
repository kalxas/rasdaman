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

#include <utility> // std::pair

class r_Type;
class r_Minterval;

//transpose the last two dimensions of data via a temporary 2D object dataTemp
void transposeLastTwo(char* data, r_Minterval& dimData, const r_Type* dataType);

//general transpose function. used to throw errors in case the transpose option,
//and otherwise to call the transposeLastTwo function. Should also simplify
//implementation and make it more transparent
//in the future, one should improve upon the transpose function so that it will
//ensure the indices are valid dimensions and if they are, transpose those two
//axes regardless of whether or not they are the last two. This would require
//either a cumbersome memory computation, a vacancy tracking algorithm, or
//a bit map to implement effectively. For now, we only need this for pictures,
//and as such, transposeLastTwo is good enough for the time being.
void transpose(char* data, r_Minterval& dimData, const r_Type* dataType,
               const std::pair<int, int> transposeParams);

#endif /* TRANSPOSE_HH */
