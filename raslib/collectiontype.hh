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

#ifndef D_COLLECTION_TYPE_HH
#define D_COLLECTION_TYPE_HH

#include "raslib/type.hh"

//@ManMemo: Module: {\bf raslib}
/**
  * \ingroup raslib
  */

/**
  This class represents the collection type in the ODMG conformant
  representation of the RasDaMan type system.
*/
class r_Collection_Type : public r_Type
{
public:
    /// Set type in the only one needed right now.
    typedef enum { SET } r_Kind;

    /// copy constructor
    /// the exception is only raised when the element type of the copied type is NULL.
    /// (this can not happen)
    r_Collection_Type(const r_Collection_Type &);

    /// constructor getting element type
    explicit r_Collection_Type(r_Type &newType);
    
    /// destructor
    ~r_Collection_Type() override;

    /// returns identifier SET of enumeration r_Kind
    r_Kind kind() const;

    /// assignment operator
    /// the exception is only raised when the element type of the copied type is NULL.
    /// (this can not happen)
    const r_Collection_Type &operator=(const r_Collection_Type &oldObj);

    /// get element type
    /// the exception is only raised when the element type of the copied type is NULL.
    /// (this can not happen)
    const r_Type &element_type() const;

    /// clone operation
    r_Type *clone() const override;

    /// retrieve id of the type.
    r_Type::r_Type_Id type_id() const override;

    bool isCollectionType() const override;

    /// converts array of cells from NT byte order to Unix byte order.
    void convertToLittleEndian(char *cells, r_Area noCells) const override;

    /// converts array of cells from Unix byte order to NT byte order.
    void convertToBigEndian(char *cells, r_Area noCells) const override;

    /// writes state of object to specified stream
    void print_status(std::ostream &s) const override;

protected:
    /// default constructor
    /// no one should use that
    r_Collection_Type();

    /// element type
    r_Type *elementType{NULL};
};

//@Doc: write the status of a collection type to a stream
extern std::ostream &operator<<(std::ostream &str, const r_Collection_Type &type);

#endif

