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
// This is -*- C++ -*-
/*************************************************************************
 *
 *
 * PURPOSE:
 *   switching between various cases of dropping
 *   user-defined mdd, set, and struct types
 *
 *
 * COMMENTS:
 *   uses embedded SQL
 *
 ***********************************************************************/

#include "qtdroptype.hh"
#include "relcatalogif/alltypes.hh"
#include "relcatalogif/typefactory.hh"

const QtNode::QtNodeType QtDropType::nodeType = QtNode::QT_DROP_TYPE;

QtDropType::QtDropType(const std::string &typeName2)
    : typeName(typeName2)
{
}

QtData *QtDropType::evaluate()
{
    QtData *returnValue = NULL;
    // here we are sure that the type exists in the database (checkType passed)
    switch (dropType)
    {
    case CELL_TYPE:
    {
        TypeFactory::deleteStructType(this->typeName.c_str());
        break;
    }
    case MDD_TYPE:
    {
        TypeFactory::deleteMDDType(this->typeName.c_str());
        break;
    }
    case SET_TYPE:
    {
        TypeFactory::deleteSetType(this->typeName.c_str());
        break;
    }
    default:
    {
        parseInfo.setErrorNo(TYPE_NAMEUNKNOWN);
        parseInfo.setToken(this->typeName.c_str());
        throw parseInfo;
    }
    }
    return returnValue;
}

void QtDropType::checkType()
{
    // determine the type of the type
    // if no type exists then all if() will fail
    if (TypeFactory::mapType(this->typeName.c_str()) != NULL)
    {
        dropType = CELL_TYPE;
    }
    else if (TypeFactory::mapMDDType(this->typeName.c_str()) != NULL)
    {
        dropType = MDD_TYPE;
    }
    else if (TypeFactory::mapSetType(this->typeName.c_str()) != NULL)
    {
        dropType = SET_TYPE;
    }
    else
    {
        parseInfo.setErrorNo(TYPE_NAMEUNKNOWN);
        parseInfo.setToken(this->typeName.c_str());
        throw parseInfo;
    }
}

void QtDropType::printTree(int tab, std::ostream &s, __attribute__((unused)) QtChildType mode)
{
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "QtDropType Object" << std::endl;
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "  DROP TYPE " << typeName;
}


void QtDropType::printAlgebraicExpression(std::ostream &s)
{
    s << "command <";
    s << "DROP TYPE " << typeName;
    s << ">";
}

QtNode::QtNodeType QtDropType::getNodeType() const
{
    return nodeType;
}
