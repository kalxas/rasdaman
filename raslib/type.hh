/*
 * This file is part of rasdaman community.
 *
 * Rasdaman community is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Rasdaman community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003 - 2010 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
*/

/**
 * INCLUDE: type.hh
 *
 * MODULE:  raslib
 * CLASS:   r_Type
 *
 * COMMENTS:
 *
*/

#ifndef D_TYPE_HH
#define D_TYPE_HH

#include "raslib/metaobject.hh"
#include "raslib/mddtypes.hh"

class r_Primitive_Type;
class r_Structure_Type;
class r_Marray_Type;
class r_Sinterval_Type;
class r_Minterval_Type;
class r_Point_Type;
class r_Oid_Type;
class r_Base_Type;
class r_Collection_Type;

//@ManMemo: Module: {\bf raslib}

/**
  This class the superclass for all types in the ODMG conformant
  representation of the RasDaMan type system.
*/

class r_Type : public r_Meta_Object
{
public:
    /// typedef for the enum specifying a primitive type, structure type,
    /// marray type, interval type, minterval type, point type or oid type
    enum r_Type_Id { ULONG, USHORT, BOOL, LONG, SHORT, OCTET,
                     DOUBLE, FLOAT, CHAR, COMPLEXTYPE1, COMPLEXTYPE2, CINT16, CINT32,
                     STRUCTURETYPE, MARRAYTYPE, COLLECTIONTYPE,
                     SINTERVALTYPE, MINTERVALTYPE, POINTTYPE, OIDTYPE,
                     UNKNOWNTYPE
                   };
    /// default constructor.
    r_Type() = default;
    /// constructor getting name of type.
    explicit r_Type(const char *newTypeName);
    /// destructor.
    virtual ~r_Type() = default;

    /// clone operation
    virtual r_Type *clone() const = 0;

    /// retrieve id of the type.
    virtual r_Type::r_Type_Id type_id() const = 0;

    /// check, if type is primitive or structured.
    virtual bool isStructType() const;

    /// check, if type is a base type ( primitive type or structure type).
    virtual bool isBaseType() const;

    /// check, if type is a base type ( primitive type or structure type).
    virtual bool isComplexType() const;

    /// check, if type is a marray type.
    virtual bool isMarrayType() const;

    /// check, if type is a primitive type.
    virtual bool isPrimitiveType() const;

    /// check, if type is a Sinterval
    virtual bool isSintervalType() const;

    /// check, if type is a Minterval
    virtual bool isMintervalType() const;

    /// check, if type is a Colelction type
    virtual bool isCollectionType() const;

    /// check, if type is a Point
    virtual bool isPointType() const;

    /// check, if type is a oid
    virtual bool isOidType() const;

    /// build type schema from string representation
    static r_Type *get_any_type(const char *type_string);
    static r_Type *get_any_type(const std::string &type_string);

    /// converts array of cells from NT byte order to Unix byte order.
    virtual void convertToLittleEndian(char *cells, r_Area noCells) const = 0;

    /// converts array of cells from Unix byte order to NT byte order.
    virtual void convertToBigEndian(char *cells, r_Area noCells) const = 0;


private:

    //@Man: Methodes and structures for dl parser:
    //@{
    ///
    
    /// token enumeration for parser
    enum DLTOKEN   { DLMARRAY, DLSET, DLSTRUCT, DLCOMMA,
                     DLLEP, DLREP, DLLAP, DLRAP, DLLCP, DLRCP,
                     DLIDENTIFIER, DLCHAR, DLOCTET, DLSHORT, DLUSHORT,
                     DLLONG, DLULONG, DLFLOAT, DLDOUBLE, DLBOOL, DLCOMPLEXTYPE1, DLCOMPLEXTYPE2, DLCINT16, DLCINT32, 
                     DLINTERVAL,  DLMINTERVAL, DLPOINT, DLOID, DLUNKNOWN
                   };
    ///
    static DLTOKEN getNextToken(char *&pos, char *&identifier);
    ///
    static r_Collection_Type *getCollectionType(char *&pos);
    ///
    static r_Type *getType(char *&pos);
    ///
    static r_Marray_Type *getMarrayType(char *&pos);
    ///
    static r_Base_Type *getBaseType(char *&pos, int offset = 0);
    ///
    static r_Primitive_Type *getPrimitiveType(char *&pos);
    ///
    static r_Structure_Type *getStructureType(char *&pos, int offset = 0);
    ///
    static r_Sinterval_Type *getSintervalType(char *&pos);
    ///
    static r_Minterval_Type *getMintervalType(char *&pos);
    ///
    static r_Point_Type *getPointType(char *&pos);
    ///
    static r_Oid_Type *getOidType(char *&pos);

    ///
    //@}


};

extern std::ostream &operator<<(std::ostream &s, r_Type::r_Type_Id t);

//ULONG, USHORT, CHAR, BOOL, LONG, SHORT, OCTET,
//DOUBLE, FLOAT, COMPLEXTYPE1, COMPLEXTYPE2, CINT16, CINT32,
//STRUCTURETYPE, MARRAYTYPE, COLLECTIONTYPE,
//SINTERVALTYPE, MINTERVALTYPE, POINTTYPE, OIDTYPE,
//UNKNOWNTYPE
//};

//
// Generate a switch for all base r_Type_Id, and put the given code
// block in each case.
//
#define CODE(...) __VA_ARGS__
#define MAKE_SWITCH_TYPEID(cellType, T, code, codeDefault) \
    switch (cellType) { \
    case r_Type::r_Type_Id::ULONG: { using T = r_ULong;   code break; } \
    case r_Type::r_Type_Id::USHORT:{ using T = r_UShort;  code break; } \
    case r_Type::r_Type_Id::CHAR:  { using T = r_Char;    code break; } \
    case r_Type::r_Type_Id::BOOL:  { using T = r_Boolean; code break; } \
    case r_Type::r_Type_Id::LONG:  { using T = r_Long;    code break; } \
    case r_Type::r_Type_Id::SHORT: { using T = r_Short;   code break; } \
    case r_Type::r_Type_Id::OCTET: { using T = r_Octet;   code break; } \
    case r_Type::r_Type_Id::DOUBLE:{ using T = r_Double;  code break; } \
    case r_Type::r_Type_Id::FLOAT: { using T = r_Float;   code break; } \
    default:       { codeDefault break; } \
    }


#endif
