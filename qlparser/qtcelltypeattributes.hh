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
* Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2014, 2015 Peter Baumann /
rasdaman GmbH.
*
* For more information please see <http://www.rasdaman.org>
* or contact Peter Baumann via <baumann@rasdaman.com>.
*/
/*************************************************************
 *
 *
 * COMMENTS:
 *
 ************************************************************/

#ifndef QTCELLTYPEATTRIBUTES_HH
#define QTCELLTYPEATTRIBUTES_HH

#include "config.h"
#include "qtoperation.hh"
#include "qtunaryinduce.hh"

/**
 * @brief The QtCellTypeAttributes class - holds information about struct types attributes which are
 * characterizes by an attribute name and an attribute type
 */
class QtCellTypeAttributes : public QtOperation
{
public:
    QtCellTypeAttributes(const std::string& attributeName, const std::string& attributeType);

    virtual void printTree( int tab, std::ostream& s = std::cout, QtChildType mode = QT_ALL_NODES );

    std::string getAttributeName();
    std::string getAttributeType();

    virtual QtNodeType getNodeType() const;

private:
    std::string attributeName;
    std::string attributeType;

    static const QtNodeType nodeType;
};

#endif // QTCELLTYPEATTRIBUTES_HH
