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
/*************************************************************
 *
 *
 * PURPOSE:
 *   Ops contains an enum for identifying all possible
 *   operations.
 *
 *
 * COMMENTS:
 *
 ************************************************************/

#ifndef _TYPEENUM_HH_
#define _TYPEENUM_HH_

//@Man: TypeEnum
//@Type: typedef
//@Memo: Module: {\bf catalogif}.

enum TypeEnum
{
    ULONG, USHORT, CHAR, BOOLTYPE, LONG, SHORT, OCTET, DOUBLE, FLOAT,
    NUMERICAL_TYPES_END = FLOAT,
    COMPLEXTYPE1,            // COMPLEX already defined as token !!!
    COMPLEXTYPE2,
    CINT16,                  // complex integers consist of 2 shorts (for CINT16)
    CINT32,                  // and 2 longs (for CINT32)
    STRUCT,
    CLASSTYPE, SETTYPE, MDDTYPE, INVALID_TYPE
};

/*@Doc: This is an enum used for handling types instead of using the
    string representation of the name. For some strange reason
    I did not manage to define it in Ops scope. I had to use BOOLTYPE
    instead of BOOL because of name conflicts.

    Attention: DO NOT change the sequence because some code relies on it.
This is the ops code and the persistence code: from the typenum the oids are generated.
changing the order of the enums makes old databases incompatible.
 */

#endif
