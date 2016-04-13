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
* Copyright 2003-2015 Peter Baumann / rasdaman GmbH.
*
* For more information please see <http://www.rasdaman.org>
* or contact Peter Baumann via <baumann@rasdaman.com>.
*/
#include "qtcelltypeattributes.hh"

const QtNode::QtNodeType QtCellTypeAttributes::nodeType = QtNode::QT_CELL_TYPE_ATTRIBUTES;

QtCellTypeAttributes::QtCellTypeAttributes(const std::string &name, const std::string &type)
    :attributeName(name), attributeType(type)
{
}

void QtCellTypeAttributes::printTree(int tab, std::ostream &s, __attribute__ ((unused)) QtChildType mode)
{
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "QtCellTypeAttributes (" << attributeName << " "
      << attributeType << ")" << std::endl;
}

std::string QtCellTypeAttributes::getAttributeName()
{
    return this->attributeName;
}

std::string QtCellTypeAttributes::getAttributeType()
{
    return this->attributeType;
}

QtNode::QtNodeType QtCellTypeAttributes::getNodeType() const
{
    return nodeType;
}
