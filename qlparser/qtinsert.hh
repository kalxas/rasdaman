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
#ifndef __QTINSERT_HH__
#define __QTINSERT_HH___

#include "qlparser/qtcollection.hh"
#include "qlparser/qtexecute.hh"
#include "qlparser/qtoperation.hh"
#include "qlparser/qtmddconfig.hh"
#include "qlparser/qtmddcfgop.hh"
#include "qlparser/qtdata.hh"

#include "rasodmg/stattiling.hh"
#include "rasodmg/interesttiling.hh"

#include <string>
#include <iostream>

class StorageLayout;

//@ManMemo: Module: {\bf qlparser}

/**

*/

class QtInsert : public QtExecute
{
public:
    /// constructor getting collection and insert expression
    QtInsert(const QtCollection &initCollection, QtOperation *initSource);

    QtInsert(const QtCollection &initCollection, QtOperation *initSource, QtOperation *storage);

    /// constructor getting collection and data to insert
    QtInsert(const QtCollection &initCollection, QtData *data);

    /// virtual destructor
    virtual ~QtInsert();

    /// method for evaluating the node
    virtual QtData *evaluate();

    /// return childs of the node
    virtual QtNodeList *getChilds(QtChildType flag);

    /// prints the tree
    virtual void printTree(int tab, std::ostream &s = std::cout, QtChildType mode = QT_ALL_NODES);

    /// prints the algebraic expression
    virtual void printAlgebraicExpression(std::ostream &s = std::cout);

    /// method for identification of nodes
    inline virtual QtNodeType getNodeType() const;

    /// method for query rewrite
    inline virtual void setInput(QtOperation *child, QtOperation *input);

    /// returns source
    QtOperation *getSource();

    /// tiling functions
    r_Data_Format getDataFormat(QtMDDConfig *config);
    r_Index_Type getIndexType(QtMDDConfig *config);
    r_Tiling_Scheme getTilingScheme(QtMDDConfig *cfg);
    vector<r_Minterval> getIntervals(QtMDDConfig *cfg);
    r_Minterval getTileConfig(QtMDDConfig *cfg, int baseTypeSize, r_Dimension sourceDimension);

    void setStorageLayout(StorageLayout *layout);

    /// type checking
    virtual void checkType();

private:
    /// insert expression
    QtOperation *source;

    /// insert data
    QtData *dataToInsert;

    // Storage and Tiling type
    QtOperation *stgLayout;

    /// collection
    QtCollection collection;

    /// attribute for identification of nodes
    static const QtNodeType nodeType;
};

#include "qlparser/qtinsert.icc"

#endif
