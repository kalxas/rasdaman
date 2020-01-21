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

#ifndef TYPEENUM_HH
#define TYPEENUM_HH

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


inline bool isIntType(TypeEnum type)
{
    return type <= OCTET;
}
inline bool isSignedType(TypeEnum type)
{
    return type >= LONG && type <= OCTET;
}
inline bool isUnsignedType(TypeEnum type)
{
    return type >= ULONG && type <= BOOLTYPE;
}
inline bool isComplexType(TypeEnum type)
{
    return type == CINT16 || type == CINT32 || type == COMPLEXTYPE1 || type == COMPLEXTYPE2;
}
inline bool isPrimitiveType(TypeEnum type)
{
    return type <= NUMERICAL_TYPES_END;
}
inline bool isFloatType(TypeEnum type)
{
    return type == FLOAT || type == DOUBLE;
}

/// return type size in bytes of a given atomic/complex type.
/// return 0 if type is not supported (e.g. struct).
inline int typeSize(TypeEnum type)
{
    switch (type)
    {
    case BOOLTYPE:
    case CHAR:
    case OCTET:  return 1;
    case USHORT:
    case SHORT:  return 2;
    case ULONG:
    case LONG:
    case CINT16:
    case FLOAT:  return 4;
    case COMPLEXTYPE1:
    case CINT32:
    case DOUBLE: return 8;
    case COMPLEXTYPE2: return 16;
    default:     return 0;
    }
}

/// return the type which is greater in terms of size.
inline TypeEnum greaterType(TypeEnum type1, TypeEnum type2)
{
    static char rank[STRUCT] = {};
    static bool rankInitialized = false;
    if (!rankInitialized)
    {
        rank[BOOLTYPE] = 1;
        rank[CHAR] = 2;
        rank[OCTET] = 3;
        rank[USHORT] = 4;
        rank[SHORT] = 5;
        rank[ULONG] = 6;
        rank[LONG] = 7;
        rank[FLOAT] = 8;
        rank[DOUBLE] = 9;
        rank[CINT16] = 10;
        rank[CINT32] = 11;
        rank[COMPLEXTYPE1] = 13;
        rank[COMPLEXTYPE2] = 14;
        rankInitialized = true;
    }
    return rank[type1] >= rank[type2] ? type1 : type2;
}

/// return the next greater type in terms of size.
inline TypeEnum nextGreaterType(TypeEnum type)
{
    switch (type)
    {
    case BOOLTYPE:
    case CHAR:   return USHORT;
    case OCTET:  return SHORT;
    case USHORT: return ULONG;
    case SHORT:  return LONG;
    case FLOAT:  return DOUBLE;
    case CINT16: return CINT32;
    case COMPLEXTYPE1: return COMPLEXTYPE2;
    default:     return type;
    }
}

/// convert a type to signed type of the same size.
inline TypeEnum toSignedType(TypeEnum type)
{
    switch (type)
    {
    case BOOLTYPE:
    case CHAR:   return OCTET;
    case USHORT: return SHORT;
    case ULONG:  return LONG;
    default:     return type;
    }
}

/// convert type to string; return 0 (NULL) if type is not atomic or complex.
inline const char* typeToString(TypeEnum type)
{
    switch (type)
    {
    case BOOLTYPE: return "Bool";
    case CHAR:   return "Char";
    case OCTET:  return "Octet";
    case USHORT: return "UShort";
    case SHORT:  return "Short";
    case ULONG:  return "ULong";
    case LONG:   return "Long";
    case CINT16: return "CInt16";
    case FLOAT:  return "Float";
    case COMPLEXTYPE1: return "Complex";
    case CINT32: return "CInt32";
    case DOUBLE: return "Double";
    case COMPLEXTYPE2: return "Complexd";
    default:     return 0;
    }
}


//
// Generate a switch for all base TypeEnums, and put the given code
// block in each case.
//
#define CODE(...) __VA_ARGS__
#define MAKE_SWITCH_TYPEENUM(cellType, T, code, codeDefault) \
    switch (cellType) { \
    case TypeEnum::ULONG:    { using T = r_ULong;   code break; } \
    case TypeEnum::USHORT:   { using T = r_UShort;  code break; } \
    case TypeEnum::CHAR:     { using T = r_Char;    code break; } \
    case TypeEnum::BOOLTYPE: { using T = r_Boolean; code break; } \
    case TypeEnum::LONG:     { using T = r_Long;    code break; } \
    case TypeEnum::SHORT:    { using T = r_Short;   code break; } \
    case TypeEnum::OCTET:    { using T = r_Octet;   code break; } \
    case TypeEnum::DOUBLE:   { using T = r_Double;  code break; } \
    case TypeEnum::FLOAT:    { using T = r_Float;   code break; } \
    default:       { codeDefault break; } \
    }


#endif
