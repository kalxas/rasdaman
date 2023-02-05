// -*-C++-*- (for Emacs)

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

#ifndef _TYPE_HH_
#define _TYPE_HH_

#include "reladminif/dbnamedobject.hh"  // for DBNamedObject
#include "catalogmgr/typeenum.hh"

#include <iosfwd>                     // for cout, endl, ostream
#include <vector>                     // for vector

class BaseType;
class OId;

//@ManMemo: Module: {\bf relcatalogif}.

/*@Doc:
Type is the abstract base class for CollectionType, BaseType, and MDDType.

Common to each type is the ability to get its name.
This functionality is defined as a pure virtual function here.

{\bf Interdependencies}

Each \Ref{Tile} has a pointer to its BaseType. Pointers to BaseType
are also used in subclasses of \Ref{MDDObject}.
*/

/**
  * \ingroup Relcatalogifs
  */
class Type : public DBNamedObject
{
public:
    Type();
    explicit Type(const OId &id);
    Type(const Type &) = default;
    Type &operator=(const Type &) = default;
    ~Type() override = default;

    void destroy() override;
    /*@Doc:
    does nothing.  is neccessary to stop types from being deleted by ~DBRef<Type>
    */

    /// returns the name of the type as a C string.
    virtual const char *getTypeName() const;
    /*@Doc:
      The name of the type is the class name without the Type suffix.
      e.g. "Bool" for \Ref{BoolType}, or "ULong" for \Ref{ULongType},
      or "Set" for \Ref{SetType}, or "Dimension" for \Ref{DimensionType}.
    */

    /// generate equivalent C type names
    virtual void generateCTypeName(std::vector<const char *> &names) const;
    virtual void generateCTypePos(std::vector<int> &positions, int offset = 0) const;

    virtual void getTypes(std::vector<const BaseType *> &types) const;

    /// returns the structure of the type as a C string.
    virtual std::string getTypeStructure() const;
    /*@Doc:
      Returns a copy of getTypeName() for non-structured base types. For
      structured types a list of the elements in the form of #struct {
      ulong elemName1, ushort elemName2 }# is returned. MDDTypes are
      printed in the form #marray< RGBPixel, [10:20]# (less information,
      if domain is not specified). Sets are printed in the form
      #set<setName>#. The char* has to be freed by the caller!
    */
    virtual std::string getNewTypeStructure() const;

    TypeEnum getType() const;
    /*@Doc:
    returns the type as a TypeEnum.
    */

    virtual int compatibleWith(const Type *aType) const;
    /*@Doc:
    checks, if two types are compatible (see also \Ref{MDDType}).
    */
    
    virtual bool operator==(const Type &o) const;

protected:
    TypeEnum myType;
    /*@Doc:
    enum for type.  this can be ULONG, USHORT, CHAR,
        BOOLTYPE, LONG, SHORT, OCTET, DOUBLE,
        FLOAT, STRUCT, CLASSTYPE, SETTYPE, MDDTYPE
    */

    Type(const char *name);
};

#endif
