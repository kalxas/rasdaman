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

#ifndef QTCREATEMDDTYPE_HH
#define QTCREATEMDDTYPE_HH

#include "qtexecute.hh"
#include "raslib/minterval.hh"
#include "raslib/mddtypes.hh"

/**
 * @brief The QtCreateMarrayType class - Creates a Marray type
 */
class QtCreateMarrayType : public QtExecute
{
public:
    /**
     * @param typeName - Name of the type
     * @param cellTypeName - Cell type (which can be either a base type or a struct type)
     * @param dimensioanlity - the number of dimensions this type has
     */
    QtCreateMarrayType(const std::string& typeName, const std::string cellTypeName, r_Dimension dimensioanlity);

    /**
     * @param typeName - Name of the type
     * @param cellTypeName - Cell type (which can be either a base type or a struct type)
     * @param domainNode - dimensionality ca be specified as minterval (e.g. [0:100,0:100]
     */
    QtCreateMarrayType(const std::string& typeName, const std::string cellTypeName, QtOperation* domainNode);

    virtual QtData* evaluate();
    virtual void printTree( int tab, std::ostream& s = std::cout, QtChildType mode = QT_ALL_NODES );
    virtual void checkType();
    virtual void printAlgebraicExpression( std::ostream& s = std::cout );

    virtual QtNodeType getNodeType() const;

private:
    std::string typeName;
    std::string cellTypeName;
    r_Dimension dimensioanlity;
    QtOperation* domainNode;

    static const QtNodeType nodeType;

};

#endif // QTCREATEMDDTYPE_HH
