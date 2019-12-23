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

#include "raslib/structuretype.hh"
#include "raslib/attribute.hh"
#include "raslib/error.hh"

#include <logging.hh>

r_Structure_Type::r_Structure_Type(const char *newTypeName,
                                   unsigned int newNumAttrs,
                                   r_Attribute *newAttrs, int offset)
    : r_Base_Type(newTypeName, 0), myAttributes(newNumAttrs)
{
    for (unsigned int i = 0; i < newNumAttrs; i++)
    {
        myAttributes[i] = newAttrs[i];
        myAttributes[i].set_offset(typeSize);
        myAttributes[i].set_global_offset(typeSize + static_cast<r_Bytes>(offset));
        typeSize += myAttributes[i].type_of().size();
    }
}

r_Structure_Type::r_Structure_Type(const r_Structure_Type &oldObj)
    : r_Base_Type(oldObj), myAttributes(oldObj.myAttributes)
{
}

const r_Structure_Type &
r_Structure_Type::operator=(const r_Structure_Type &oldObj)
{
    if (this != &oldObj)
    {
        r_Base_Type::operator=(oldObj);
        myAttributes = oldObj.myAttributes;
    }
    return *this;
}

r_Type *
r_Structure_Type::clone() const
{
    return new r_Structure_Type(*this);
}


r_Type::r_Type_Id
r_Structure_Type::type_id() const
{
    return STRUCTURETYPE;
}

bool
r_Structure_Type::isStructType() const
{
    return true;
}

bool
r_Structure_Type::compatibleWith(const r_Structure_Type *myType) const
{
    if (myType == NULL)
    {
        return false;
    }
    if (count_elements() != myType->count_elements())
    {
        return false;
    }
    for (unsigned int i = 0; i < count_elements(); ++i)
    {
        if ((*this)[i].type_of().type_id() != (*myType)[i].type_of().type_id())
        {
            return false;
        }
    }
    return true;
}

r_Attribute &
r_Structure_Type::resolve_attribute(const char *newName)
{
    for (unsigned int i = 0; i < count_elements(); ++i)
    {
        if (strcmp(myAttributes[i].name(), newName) == 0)
        {
            return myAttributes[i];
        }
    }
    LERROR << "'" << newName << "' is not a valid atribute name.";
    throw r_Error(r_Error::r_Error_NameInvalid);
}

r_Attribute &
r_Structure_Type::resolve_attribute(unsigned int number)
{
    if (number < count_elements())
    {
        return myAttributes[number];
    }
    LERROR << "index out of bounds (" << number << ")";
    throw r_Eindex_violation(0, count_elements() - 1, number);
}

r_Attribute &
r_Structure_Type::operator[](unsigned int number)
{
    return resolve_attribute(number);
}

const r_Attribute &
r_Structure_Type::resolve_attribute(const char *newName) const
{
    for (unsigned int i = 0; i < count_elements(); ++i)
    {
        if (strcmp(myAttributes[i].name(), newName) == 0)
        {
            return myAttributes[i];
        }
    }
    LERROR << "'" << newName << "' is not a valid atribute name.";
    throw r_Error(r_Error::r_Error_NameInvalid);
}

const r_Attribute &
r_Structure_Type::resolve_attribute(unsigned int number) const
{
    if (number < count_elements())
    {
        return myAttributes[number];
    }
    LERROR << "index out of bounds (" << number << ")";
    throw r_Eindex_violation(0, count_elements() - 1, number);
}

const r_Attribute &
r_Structure_Type::operator[](unsigned int number) const
{
    return resolve_attribute(number);
}

const std::vector<r_Attribute> &r_Structure_Type::getAttributes() const
{
    return myAttributes;
}

unsigned int
r_Structure_Type::count_elements() const
{
    return static_cast<unsigned int>(myAttributes.size());
}

void
r_Structure_Type::convertToLittleEndian(char *cells, r_Area noCells) const
{
    r_Area i = 0;
    unsigned int j = 0;

    for (i = 0; i < noCells; i++)
    {
        for (j = 0; j < count_elements(); j++)
        {
            myAttributes[j].type_of().convertToLittleEndian(
                &cells[i * typeSize + myAttributes[j].offset()], 1);
        }
    }
}

void
r_Structure_Type::convertToBigEndian(char *cells, r_Area noCells) const
{
    r_Area i = 0;
    unsigned int j = 0;

    for (i = 0; i < noCells; i++)
    {
        for (j = 0; j < count_elements(); j++)
        {
            myAttributes[j].type_of().convertToBigEndian(
                &cells[i * typeSize + myAttributes[j].offset()], 1);
        }
    }
}

void
r_Structure_Type::print_status(std::ostream &s) const
{
    s << "struct{ ";
    bool addComma = false;
    for (const auto &att: myAttributes)
    {
        if (addComma)
            s << ", ";
        else
            addComma = true;
        att.print_status(s);
    }
    s << " }";
}

void
r_Structure_Type::print_value(const char *storage,  std::ostream &s) const
{
    s << "{ ";
    bool addComma = false;
    for (const auto &att: myAttributes)
    {
        if (addComma)
            s << ", ";
        else
            addComma = true;
        att.type_of().print_value(storage + att.offset(),  s);
    }
    s << "}  ";
}


std::ostream &operator<<(std::ostream &str, const r_Structure_Type &type)
{
    type.print_status(str);
    return str;
}
