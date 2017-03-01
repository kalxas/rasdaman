#ifndef __QTCOMMAND_HH__
#define __QTCOMMAND_HH___

#include "qlparser/qtexecute.hh"
#include "qlparser/qtoperationiterator.hh"
#include "qlparser/querytree.hh"
#include "raslib/oid.hh"

#ifndef CPPSTDLIB
#include <ospace/string.h> // STL<ToolKit>
#else
#include <string>
#endif
#include <iostream>

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
/*************************************************************
 *
 *
 * COMMENTS:
 *
 ************************************************************/

//@ManMemo: Module: {\bf qlparser}

/**

*/

class QtCommand : public QtExecute
{
public:
    enum QtCommandType
    {
        QT_DROP_COLLECTION,
        QT_CREATE_COLLECTION,
        QT_CREATE_COLLECTION_FROM_QUERY_RESULT,
        QT_COMMIT
    };

    /// constructor getting command, collection and type name (create collection)
    QtCommand(QtCommandType initCommand, const std::string& initCollection, const std::string& initType);

    /// constructor getting command and collection name (drop collection)
    QtCommand(QtCommandType initCommand, const std::string& initCollection);

    /// constructor getting command, collection name and query tree node (create collection from query result)
    QtCommand(QtCommandType initCommand, const std::string& initCollection, QtOperationIterator* collection);

    /// method for evaluating the node
    virtual QtData* evaluate();

    /// prints the tree
    virtual void printTree(int tab, std::ostream& s = std::cout, QtChildType mode = QT_ALL_NODES);

    /// prints the algebraic expression
    virtual void printAlgebraicExpression(std::ostream& s = std::cout);

    /// method for identification of nodes
    inline virtual QtNodeType getNodeType() const;

    /// type checking
    virtual void checkType();

private:

    /// create a collection
    OId createCollection(std::string collectionName, std::string typeName);

    /// drop a given collection
    void dropCollection(std::string collectionName);

    /// Creates a datatype from query results. Returns the type name of the new collection.
    std::string getSelectedDataType(std::vector<QtData*>* data);

    /// Inserts evaluated "data" into the given collection
    void insertIntoCollection(std::vector<QtData*>* data, std::string collectionName);

    /// Returns true if a collection exists with the given name
    bool collectionExists(std::string collectionName);

    /// command type
    QtCommandType command;

    /// attribute for identification of nodes
    static const QtNodeType nodeType;

    /// collection name for drop/create collection
    std::string collectionName;

    /// type name for create collection
    std::string typeName;

    /// query tree operation; its results will be inserted into a new collection
    QtOperationIterator* childNode;

    /// temporary type prefixes
    static const std::string tmpSetTypePrefix;
    static const std::string tmpMddTypePrefix;
};

#include "qlparser/qtcommand.icc"

#endif



