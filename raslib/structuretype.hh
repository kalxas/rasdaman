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

#ifndef _D_STRUCTURE_TYPE_
#define _D_STRUCTURE_TYPE_

#include "raslib/basetype.hh"
#include "raslib/attribute.hh"
#include <vector>

//@ManMemo: Module: {\bf raslib}
/**
  * \ingroup raslib
  */

/**
  This class represents all user defined structured types in the
  ODMG conformant representation of the RasDaMan type system.
*/

class r_Structure_Type : public r_Base_Type
{
public:
    /// default constructor.
    r_Structure_Type() = default;
    /// constructor getting name of type and type id.
    r_Structure_Type(const char *newTypeName, unsigned int newNumAttrs, r_Attribute *newAttrs, int offset = 0);
    /// copy constructor
    r_Structure_Type(const r_Structure_Type &oldObj);
    /// assignment operator.
    const r_Structure_Type &operator=(const r_Structure_Type &oldObj);
    /// destructor.
    ~r_Structure_Type() override = default;

    /// clone operation
    r_Type *clone() const override;

    /// retrieve id of the type.
    r_Type::r_Type_Id type_id() const override;

    /// check, if type is primitive or structured.
    bool isStructType() const override;

    /// check, if this type is compatible with myType (e.g. check the structure ignoring then names of atributtes)
    virtual bool compatibleWith(const r_Structure_Type *myType) const;

    /// return attribute specified by name.
    r_Attribute &resolve_attribute(const char *name);
    /// return attribute specified by number starting with zero.
    r_Attribute &resolve_attribute(unsigned int number);
    /// subscript operator to access attributes by index
    r_Attribute &operator[](unsigned int number);

    /// return attribute specified by name.
    const r_Attribute &resolve_attribute(const char *name) const;
    /// return attribute specified by number starting with zero.
    const r_Attribute &resolve_attribute(unsigned int number) const;
    /// subscript operator to access attributes by index
    const r_Attribute &operator[](unsigned int number) const;

    const std::vector<r_Attribute> &getAttributes() const;

    /// get number of attributes
    unsigned int count_elements() const;

    /// converts array of cells from NT byte order to Unix byte order.
    void convertToLittleEndian(char *cells, r_Area noCells) const override;

    /// converts array of cells from Unix byte order to NT byte order.
    void convertToBigEndian(char *cells, r_Area noCells) const override;

    /// writes state of object to specified stream
    void print_status(std::ostream &s) const override;

    /// prints values of a structured type
    void print_value(const char *storage, std::ostream &s) const override;

protected:
    std::vector<r_Attribute> myAttributes;
};

//@Doc: write the status of a structure type to a stream
extern std::ostream &operator<<(std::ostream &str, const r_Structure_Type &type);

#endif
