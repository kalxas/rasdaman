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
#ifndef _QUERYTREE_
#define _QUERYTREE_

#include "qlparser/qtnode.hh"
#include "qlparser/qtatomicdata.hh"
#include "qlparser/qtbinaryinduce.hh"
#include "qlparser/qtconst.hh"
#include "qlparser/qtdomainoperation.hh"
#include "qlparser/qtmarrayop2.hh"
#include "qlparser/qtpointop.hh"
#include "qlparser/symtab.hh"

#include <string>
#include <algorithm>
#include <ostream>

// forward declarations
class MDDObj;
class QtONCStream;
class QtDomainOperation;

/*************************************************************
 *
 *
 * INCLUDE: querytree.hh
 *
 * MODULE:  qlparser
 * CLASS:   QueryTree
 *
 * COMMENTS:
 *
 ************************************************************/

//@ManMemo: Module: {\bf qlparser}

/*@Doc:

The Query Tree is the internal representation of a RasML query string. It
consists of instances of the above class hierarchy. The tree is created in
the Query Parser Modul in the action sections of the yacc grammar. According
to the parser semantics, the tree is built bottom up which means that the leaf
objects are created first. They are given up via the parse stack. Inner objects
get references to the subobjects after they are created. The whole tree is a
dynamic data structure. The semantics is that if an object is deleted also its
subobjects are deleted which means that the deletion of the root object cleans
up the whole tree.

The class QueryTree encapsulates a query tree consisting of suclasses of type
QtNode. It consists of the entry point and methods working on the whole tree
(e.g. traversing).

*/

class QueryTree : public el::Loggable
{
public:
    /// indicate the type of information requested in a SELECT query
    enum QtInfoType
    {
        QT_INFO_UNKNOWN = 0,
        QT_INFO_VERSION = 1
    };

    /// default constructor
    QueryTree();

    /// constructor getting the root of the query tree
    QueryTree(QtNode *root);

    /// destructor (deletes the whole query tree)
    ~QueryTree();

    /// checks semantics (e.g., type checking)
    void checkSemantics();

    /// recognize common subexpressions
    std::vector<QtNode::QtNodeList> *seeSubexpression();

    /*@Doc:
      The method returns a list of all common subexpressions in the query tree.
      The private methods seeSubexpression(..) are used.
    */

    /// build in common subexpressions in the query tree
    void insertSubexpression(std::vector<QtNode::QtNodeList> *nodeList);

    /*@Doc:
      The method manipulates the query tree to handle common subexpressions.
    */

    /// executes a retrieval tree and gives back the result collection
    std::vector<QtData *> *evaluateRetrieval();

    /*@Doc:
      The method evaluates a retrieval tree and returns the result collection. For this purpose,
      first, the <tt>open()</tt> message is sent to the root node of the tree. Then <tt>next()</tt>
      is invoked on the root node which, each time, returns one element of the result
      collection. It indicates the end of the evaluation process through returning a null pointer.
      At the end, <tt>close()</tt> is called to clean up the ressources. If errors occur, various exceptions
      are thrown.
    */

    /// executes an update tree and throws a ParseInfo if query does not begin with INSERT, DELETE, UPDATE, ...
    std::vector<QtData *> *evaluateUpdate();

    /// debugging method
    void printTree(int tab, std::ostream &s = std::cout);

    virtual std::ostream &operator<<(std::ostream &os);

    void log(el::base::type::ostream_t &ostream1) const override;

    //@Man: read/write methods
    //@{
    ///

    ///
    inline QtNode *getRoot() const;
    ///
    inline void setRoot(QtNode *root);

    void setInfoType(QtInfoType newInfoType);

    ///
    //@}

    //@Man: methods used to maintain pointers to dynamic data structures used in the parse process
    //@{
    ///

    ///
    void addDynamicObject(QtNode *);
    ///
    void removeDynamicObject(QtNode *);
    ///
    void addDynamicObject(QtData *);
    ///
    void removeDynamicObject(QtData *);
    ///
    void addDynamicObject(ParseInfo *);
    ///
    void removeDynamicObject(ParseInfo *);
    ///
    void addDynamicObject(std::vector<QtONCStream *> *);
    ///
    void removeDynamicObject(std::vector<QtONCStream *> *);
    ///
    void releaseDynamicObjects();
    ///
    void addDomainObject(QtDomainOperation *);
    ///
    void removeDomainObject(QtDomainOperation *);
    ///
    void rewriteDomainObjects(r_Minterval *greatDomain, std::string *greatIterator, QtMarrayOp2::mddIntervalListType *greatList);
    ///
    void printDomainObjects();
    ///
    void releaseDomainObjects();
    ///
    void addCString(char *);
    ///
    //@}

    ///
    static SymbolTable<int> symtab;

private:
    /// attribute storing the root of the query tree
    QtNode *rootNode;

    /// used by public seeSubexpression()
    std::vector<QtNode::QtNodeList> *seeSubexpression(QtNode::QtNodeList *leafList);

    /// used by public seeSubexpression()
    QtNode::QtNodeList *seeSubexpression(QtNode::QtNodeList *leafList, std::vector<QtNode::QtNodeList> *leafListsNew);

    /// attribute carrying next free number for CSE iterator
    static unsigned int nextCSENo;

    /// list of unlinked subtrees
    std::list<QtNode *> qtNodeList;
    /**
      This list is used to store subtrees of type \Ref{QtNode} generated in the parse
      process and not linked to the result tree yet. In case of an error
      during the parse process, this list is freed.
    */

    /// list of unlinked subtrees
    std::list<QtData *> qtDataList;
    /**
      This list is used to store subtrees of type \Ref{QtData} generated in the parse
      process and not linked to the result tree yet. In case of an error
      during the parse process, this list is freed.
    */

    /// list of unlinked subtrees
    std::list<ParseInfo *> parseInfoList;
    /**
      This list is used to store elements of type \Ref{ParseInfo} generated in the parse
      process and not linked to the result tree yet. In case of an error
      during the parse process, this list is freed.
    */

    /// list of unlinked lists
    std::list<std::vector<QtONCStream *> *> vectorList;
    /**
      This list is used to store elements of type \Ref{vector<QtONCStream*>} generated
      in the parse process and not linked to the result tree yet. In case of an error
      during the parse process, this list is freed.
    */

    /// list of domain operations relevant to variables in an MArray.
    std::list<QtDomainOperation *> dopList;  // contains basically QtDomainOperation (everything else is evil :-) )
    /**
      This list is used to store elements of type \Ref{QtDomainOperation *} generated
      in the parse process for the purpose of a tree rewrite. In case of an error
      during the parse process, this list is freed.
    */

    /// list of lexed c-strings
    std::list<char *> lexedCStringList;
    /**
      This list is used to store elements of type char* generated during the lexing
      process and not freed yet. In case of an error this list is freed.
    */

    /// indicates if we want some information, like in SELECT VERSION()
    QtInfoType infoType;
};

#include "querytree.icc"

#endif
