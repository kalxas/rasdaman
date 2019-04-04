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

#ifndef QTDROPTYPE_HH
#define QTDROPTYPE_HH

#include "qtexecute.hh"

class QtDropType : public QtExecute
{
public:
    /**
     * @param typeName - name of the type to be deleted
     */
    QtDropType(const std::string &typeName);

    virtual QtData *evaluate();
    virtual void printTree(int tab, std::ostream &s = std::cout, QtChildType mode = QT_ALL_NODES);

    virtual void checkType();

    /// prints the algebraic expression
    virtual void printAlgebraicExpression(std::ostream &s = std::cout);
    virtual QtNodeType getNodeType() const;

private:
    std::string typeName;

    static const QtNodeType nodeType;

    enum DropType
    {
        CELL_TYPE,
        MDD_TYPE,
        SET_TYPE
    };

    DropType dropType;


};

#endif // QTDROPTYPE_HH
