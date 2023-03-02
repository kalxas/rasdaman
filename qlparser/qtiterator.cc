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

#include "raslib/error.hh"
#include "qlparser/qtiterator.hh"
#include <logging.hh>

#include <iostream>
#include <string>
using namespace std;

const QtNode::QtNodeType QtIterator::nodeType = QtNode::QT_ITERATOR;

QtIterator::QtIterator()
    : QtONCStream(),
      inputs(NULL)
{
}

QtIterator::QtIterator(QtNode *node)
    : QtONCStream(node),
      inputs(NULL)
{
}

QtIterator::~QtIterator()
{
    if (inputs)
    {
        // delete the inputs
        for (unsigned int i = 0; i < inputs->size(); i++)
        {
            delete (*inputs)[i];
            (*inputs)[i] = NULL;
        }

        // delete the list
        delete inputs;
        inputs = NULL;
    }
}

QtNode::QtNodeList *
QtIterator::getChilds(QtChildType flag)
{
    QtNodeList *resultList = NULL;
    list<QtNode *>::iterator it;  //default
    QtNode::QtNodeList::iterator iter;

    // allocate resultList
    resultList = new QtNodeList();

    if (flag == QT_LEAF_NODES || flag == QT_ALL_NODES)
    {
        for (unsigned int i = 0; i < inputs->size(); i++)
        {
            QtNodeList *subList = NULL;
            subList = (*inputs)[i]->getChilds(flag);
            // remove all elements in subList and insert them at the beginning in resultList
            resultList->splice(resultList->begin(), *subList);
            // delete temporary subList
            delete subList;
            subList = NULL;
        };
    };

    // add the nodes of the current level
    if (flag == QT_DIRECT_CHILDS || flag == QT_ALL_NODES)
        for (unsigned int i = 0; i < inputs->size(); i++)
        {
            resultList->push_back((*inputs)[i]);
        }

    return resultList;
}

void QtIterator::open()
{
    startTimer("QtIterator");

    if (inputs)
    {
        QtONCStreamList::iterator i;

        for (i = inputs->begin(); i != inputs->end(); i++)
        {
            (*i)->open();
        }
    }
}

void QtIterator::close()
{
    if (inputs)
    {
        QtONCStreamList::iterator i;

        for (i = inputs->begin(); i != inputs->end(); i++)
        {
            (*i)->close();
        }
    }

    stopTimer();
}

void QtIterator::reset()
{
    if (inputs)
    {
        QtONCStreamList::iterator i;

        for (i = inputs->begin(); i != inputs->end(); i++)
        {
            (*i)->reset();
        }
    }
}

void QtIterator::printTree(int tab, ostream &s, QtChildType mode)
{
    if (mode != QtNode::QT_DIRECT_CHILDS)
    {
        if (inputs)
            for (unsigned int i = 0; i < inputs->size(); i++)
            {
                s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "input " << i + 1 << ": " << endl;
                (*inputs)[i]->printTree(tab + 2, s, mode);
            }
        else
        {
            s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "no inputs" << endl;
        }

        s << endl;
    }
}

void QtIterator::printAlgebraicExpression(ostream &s)
{
    if (inputs)
    {
        s << "( ";

        for (unsigned int i = 0; i < inputs->size(); i++)
        {
            (*inputs)[i]->printAlgebraicExpression(s);

            if (i < inputs->size() - 1)
            {
                s << ", ";
            }
        }

        s << " )";
    }
    else
    {
        s << "(<no input>)";
    }
}

void QtIterator::setStreamInput(QtONCStream *oldInput, QtONCStream *newInput)
{
    QtONCStreamList::iterator iter;
    bool notDone = true;

    for (iter = inputs->begin(); iter != inputs->end() && notDone; iter++)
        if (*iter && (*iter) == oldInput)
        {
            *iter = newInput;
            if (newInput)
            {
                newInput->setParent(this);
            }

            notDone = false;
        }

    if (notDone)
    {
        LERROR << "QtIterator::setStreamInput() - Error: Old input stream not found.";
    }
}

QtIterator::QtONCStreamList *
QtIterator::getStreamInputs()
{
    return inputs;
}

void QtIterator::getInputTypeTuple(QtTypeTuple &typeTuple)
{
    typeTuple = QtTypeTuple(0);

    if (inputs)
    {
        QtONCStreamList::iterator inputIter;

        for (inputIter = inputs->begin(); inputIter != inputs->end(); inputIter++)
        {
            if (*inputIter)
            {
                typeTuple.concat((*inputIter)->checkType());
            }
            else
            {
                LERROR << "Error: QtIterator::getInputTypeTuple() - input branch is invalid.";
            }
        }
    }
    else
    {
        LERROR << "Error: QtIterator::getInputTypeTuple() - inputs branch is invalid.";
    }
}
