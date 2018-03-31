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
 * PURPOSE:
 *
 *
 * COMMENTS:
 *
 ************************************************************/

static const char rcsid[] = "@(#)qlparser, QtOperationIterator: $Id: qtoperationiterator.cc,v 1.24 2001/08/07 12:36:49 barbat Exp $";


#include "config.h"

#include "qlparser/qtoperationiterator.hh"
#include "qlparser/qtmdd.hh"
#include "qlparser/qtdata.hh"

#include <logging.hh>

#include <iostream>
#include <string>
#include <vector>
using namespace std;

const QtNode::QtNodeType QtOperationIterator::nodeType = QT_OPERATION_ITERATOR;


QtOperationIterator::QtOperationIterator()
    : QtIterator()
{
    operationTreeList = new QtOperationList();
}


QtOperationIterator::QtOperationIterator(QtNode* node)
    : QtIterator(node)
{
    operationTreeList = new QtOperationList();
}


QtOperationIterator::~QtOperationIterator()
{
    // release( operationTreeList->begin(), operationTreeList->end() );
    QtOperationList::iterator iter;
    for (iter = operationTreeList->begin(); iter != operationTreeList->end(); iter++)
    {
        delete *iter;
        *iter = NULL;
    }
    delete operationTreeList;
    operationTreeList = NULL;
}


QtNode::QtNodeList*
QtOperationIterator::getChilds(QtChildType flag)
{
    QtNodeList* resultList = NULL;
    QtNodeList* subList = NULL;

    QtOperationList::iterator iter;

    resultList = QtIterator::getChilds(flag);

#ifdef DEBUG
    LTRACE << "1. childs from stream subtree ";
    list<QtNode*>::iterator debugIter;
    for (debugIter = resultList->begin(); debugIter != resultList->end(); debugIter++)
    {
        (*debugIter)->printTree(2, RMInit::dbgOut, QtNode::QT_DIRECT_CHILDS);
    }
#endif

    for (iter = operationTreeList->begin(); iter != operationTreeList->end(); iter++)
    {
        if (flag == QT_LEAF_NODES || flag == QT_ALL_NODES)
        {
            subList = (*iter)->getChilds(flag) ;

#ifdef DEBUG
            LTRACE << "2. childs from operation subtree (without direct childs) ";
            list<QtNode*>::iterator debugIter;
            if (subList) for (debugIter = subList->begin(); debugIter != subList->end(); debugIter++)
                {
                    (*debugIter)->printTree(2, RMInit::dbgOut, QtNode::QT_DIRECT_CHILDS);
                }
#endif

            // remove all elements in subList and insert them at the beginning of resultList
            if (subList)
            {
                resultList->splice(resultList->begin(), *subList);
            }

#ifdef DEBUG
            LTRACE << "3. merge of the lists ";
            list<QtNode*>::iterator debugIter;
            for (debugIter = resultList->begin(); debugIter != resultList->end(); debugIter++)
            {
                (*debugIter)->printTree(2, RMInit::dbgOut, QtNode::QT_DIRECT_CHILDS);
            }

            LTRACE << "4. old list (must be empty)";
            list<QtNode*>::iterator debugIter;
            if (subList) for (debugIter = subList->begin(); debugIter != subList->end(); debugIter++)
                {
                    (*debugIter)->printTree(2, RMInit::dbgOut, QtNode::QT_DIRECT_CHILDS);
                }
#endif

            // delete temporary subList
            delete subList;
            subList = NULL;
        };

        // add nodes of next level
        if (flag == QT_DIRECT_CHILDS || flag == QT_ALL_NODES)
        {
            resultList->push_back(*iter);
        }

#ifdef DEBUG
        LTRACE << "4. current child list including direct childs ";
        list<QtNode*>::iterator debugIter;
        for (debugIter = resultList->begin(); debugIter != resultList->end(); debugIter++)
        {
            (*debugIter)->printTree(2, RMInit::dbgOut, QtNode::QT_DIRECT_CHILDS);
        }
#endif

    };

    return resultList;
}


void
QtOperationIterator::printTree(int tab, ostream& s, QtChildType mode)
{
    QtOperationList::iterator iter;

    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "QtOperationIterator Object: type " << flush;
    dataStreamType.printStatus(s);
    s << getEvaluationTime();
    s << endl;

    if (mode != QtNode::QT_DIRECT_CHILDS)
    {
        if (operationTreeList->empty())
        {
            s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "no operation" << endl;
        }
        else
            for (iter = operationTreeList->begin(); iter != operationTreeList->end(); iter++)
            {
                s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "operation: " << endl;
                (*iter)->printTree(tab + 2, s, mode);
            }
    }

    QtIterator::printTree(tab, s, mode);
}



void
QtOperationIterator::printAlgebraicExpression(ostream& s)
{
    s << "op<";

    if (operationTreeList)
    {
        for (unsigned int i = 0; i < operationTreeList->size(); i++)
        {
            (*operationTreeList)[i]->printAlgebraicExpression(s);

            if (i < operationTreeList->size() - 1)
            {
                s << ", ";
            }
        }
    }
    else
    {
        s << "no ops";
    }

    s << ">";

    QtIterator::printAlgebraicExpression(s);
}



QtNode::QtDataList*
QtOperationIterator::next()
{
    resumeTimer();

    QtDataList* returnValue = NULL;

    if (inputs)
    {
        QtDataList* nextTuple = NULL;
        QtDataList* resultList = NULL;

        // create a composed tuple of type QtDataList of the next elements of the input streams
        // right now, just take the QtDataList vector of the first input stream
        nextTuple = (*inputs)[0]->next();

        if (nextTuple)
        {
            QtOperationList::iterator iter;
            vector<QtData*>::iterator dataIter;

            resultList = new QtDataList(operationTreeList->size());

            unsigned int pos = 0;

            for (iter = operationTreeList->begin(); iter != operationTreeList->end(); iter++)
            {
                // send them through the operand tree

                try
                {
                    if (*iter)
                    {
                        (*resultList)[pos] = (*iter)->evaluate(nextTuple);
                    }
                }
                catch (...)
                {
                    // Delete the tuple vector received by next(). Just tuple elements which are not
                    // further referenced are deleted.
                    for (dataIter = nextTuple->begin(); dataIter != nextTuple->end(); dataIter++)
                        if ((*dataIter))
                        {
                            (*dataIter)->deleteRef();
                        }

                    for (QtDataList::iterator deleteIter = resultList->begin(); deleteIter != resultList->end(); deleteIter++)
                    {
                        delete *deleteIter;
                        *deleteIter = NULL;
                    }
                    delete resultList;
                    resultList = NULL;
                    delete nextTuple;
                    nextTuple = NULL;

                    throw;
                }

                pos++;
            }

            // Delete the tuple vector received by next(). Just tuple elements which are not
            // further referenced are deleted.
            for (dataIter = nextTuple->begin(); dataIter != nextTuple->end(); dataIter++)
                if ((*dataIter))
                {
                    (*dataIter)->deleteRef();
                }

            // ... and now the vector itself
            delete nextTuple;
            nextTuple = NULL;

            returnValue = resultList;
        }
    }

    pauseTimer();

    return returnValue;
}



const QtTypeTuple&
QtOperationIterator::checkType()
{
    dataStreamType = QtTypeTuple();

    QtTypeTuple inputTypeTuple;

    getInputTypeTuple(inputTypeTuple);

    // type check of operation trees
    QtOperationList::iterator iter;

    for (iter = operationTreeList->begin(); iter != operationTreeList->end(); iter++)
        if (*iter)
        {
            dataStreamType.concat((*iter)->checkType(&inputTypeTuple));
        }

    return dataStreamType;
}


