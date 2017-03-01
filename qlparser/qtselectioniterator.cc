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

static const char rcsid[] = "@(#)qlparser, QtSelectionIterator: $Id: qtselectioniterator.cc,v 1.24 2001/08/07 12:00:12 barbat Exp $";

#include "config.h"

#include "qlparser/qtselectioniterator.hh"
#include "qlparser/qtatomicdata.hh"

#include "relcatalogif/type.hh"

#include <easylogging++.h>

#include <iostream>
#include <string>
#include <vector>
using namespace std;

const QtNode::QtNodeType QtSelectionIterator::nodeType = QtNode::QT_SELECTION_ITERATOR;


QtSelectionIterator::QtSelectionIterator()
    : QtIterator(),
      conditionTree(NULL)
{
}


QtSelectionIterator::QtSelectionIterator(QtNode* node)
    : QtIterator(node),
      conditionTree(NULL)
{
}


QtSelectionIterator::~QtSelectionIterator()
{
    if (conditionTree)
    {
        delete conditionTree;
        conditionTree = NULL;
    }
}


QtNode::QtNodeList*
QtSelectionIterator::getChilds(QtChildType flag)
{
    QtNodeList* resultList = NULL;
    QtNodeList* subList = NULL;

    resultList = QtIterator::getChilds(flag);

#ifdef DEBUG
    LTRACE << "1. childs from stream subtree ";
    for (list<QtNode*>::iterator debugIter = resultList->begin(); debugIter != resultList->end(); debugIter++)
    {
        (*debugIter)->printTree(2, RMInit::dbgOut, QtNode::QT_DIRECT_CHILDS);
    }
#endif

    if (conditionTree)
    {
        if (flag == QT_LEAF_NODES || flag == QT_ALL_NODES)
        {
            subList = conditionTree->getChilds(flag);

#ifdef DEBUG
            LTRACE << "2. childs from operation subtree (without direct childs) ";
            for (list<QtNode*>::iterator debugIter = subList->begin(); debugIter != subList->end(); debugIter++)
            {
                (*debugIter)->printTree(2, RMInit::dbgOut, QtNode::QT_DIRECT_CHILDS);
            }
#endif

            // remove all elements in subList and insert them at the beginning in resultList
            resultList->splice(resultList->begin(), *subList);

#ifdef DEBUG
            LTRACE << "3. merge of the lists ";
            for (list<QtNode*>::iterator debugIter = resultList->begin(); debugIter != resultList->end(); debugIter++)
            {
                (*debugIter)->printTree(2, RMInit::dbgOut, QtNode::QT_DIRECT_CHILDS);
            }


            LTRACE << "4. old list (must be empty)";
            for (list<QtNode*>::iterator debugIter = subList->begin(); debugIter != subList->end(); debugIter++)
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
            resultList->push_back(conditionTree);
        }

#ifdef DEBUG
        LTRACE << "4. current child list including direct childs ";
        for (list<QtNode*>::iterator debugIter = resultList->begin(); debugIter != resultList->end(); debugIter++)
        {
            (*debugIter)->printTree(2, RMInit::dbgOut, QtNode::QT_DIRECT_CHILDS);
        }
#endif
    };

    return resultList;
}


void
QtSelectionIterator::printTree(int tab, ostream& s, QtChildType mode)
{
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "QtSelectionIterator Object: type " << flush;
    dataStreamType.printStatus(s);
    s << getEvaluationTime();
    s << endl;

    if (mode != QtNode::QT_DIRECT_CHILDS)
    {
        if (conditionTree)
        {
            s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "condition : " << endl;
            conditionTree->printTree(tab + 2, s);
        }
        else
        {
            s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "no condition" << endl;
        }
    }

    QtIterator::printTree(tab, s, mode);
}



void
QtSelectionIterator::printAlgebraicExpression(ostream& s)
{
    s << "sel<";

    conditionTree->printAlgebraicExpression(s);

    s << ">";

    QtIterator::printAlgebraicExpression(s);
}



QtNode::QtDataList*
QtSelectionIterator::next()
{
    resumeTimer();

    QtDataList* returnValue = NULL;

    if (inputs)
    {
        bool        nextTupleValid = false;
        QtDataList* actualTuple = NULL;

        while (!nextTupleValid)
        {
            actualTuple = (*inputs)[0]->next();

            if (actualTuple)
            {
                if (conditionTree)
                {
                    // evaluate the condition tree
                    QtData* resultData = conditionTree->evaluate(actualTuple);

                    if (resultData)
                    {
                        if (resultData->getDataType() == QT_BOOL)
                        {
                            nextTupleValid = static_cast<bool>((static_cast<QtAtomicData*>(resultData))->getUnsignedValue());
                        }
                        else
                        {
                            LFATAL << "Error: QtSelectionIterator::next() - result of the WHERE part must be of type Bool.";
                            parseInfo.setErrorNo(359);
                            throw parseInfo;
                        }

                        resultData->deleteRef();

                        if (!nextTupleValid)
                        {
                            // delete transient objects
                            vector<QtData*>::iterator iter;

                            for (iter = actualTuple->begin(); iter != actualTuple->end(); iter++)
                                if (*iter)
                                {
                                    (*iter)->deleteRef();
                                }

                            // delete vector itself
                            delete actualTuple;
                            actualTuple = NULL;
                        };
                    }
                }
                else
                {
                    nextTupleValid = true;
                }
            }
            else
            {
                break;
            }
        }

        returnValue = actualTuple;
    }

    pauseTimer();

    return returnValue;
}


const QtTypeTuple&
QtSelectionIterator::checkType()
{
    // concatenate types of inputs
    getInputTypeTuple(dataStreamType);

    // type check for condition tree
    if (conditionTree)
    {
        const QtTypeElement& type = conditionTree->checkType(static_cast<QtTypeTuple*>(&dataStreamType));

        if (type.getDataType() != QT_BOOL)
        {
            LFATAL << "Error: QtSelectionIterator::next() - result of the WHERE part must be of type Bool.";
            parseInfo.setErrorNo(359);
            throw parseInfo;
        }
    }

    // pass type tuple
    return dataStreamType;
}
