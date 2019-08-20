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

#include "raslib/rmdebug.hh"

#include "qlparser/qtjoiniterator.hh"
#include "qlparser/qtmdd.hh"
#include <logging.hh>

#include <iostream>
#include <string>
using namespace std;

const QtNode::QtNodeType QtJoinIterator::nodeType = QtNode::QT_JOIN_ITERATOR;


QtJoinIterator::QtJoinIterator()
    : QtIterator(),
      outputStreamIsEmpty(false),
      actualTuple(NULL)
{
}


QtJoinIterator::QtJoinIterator(QtNode *node)
    : QtIterator(node),
      outputStreamIsEmpty(false),
      actualTuple(NULL)
{
}


QtJoinIterator::~QtJoinIterator()
{
    vector<QtData *>::iterator i; //default

    if (actualTuple)
    {
        // first delete still existing data carriers
        for (QtDataList::iterator iter = actualTuple->begin(); iter != actualTuple->end(); iter++)
            if (*iter)
            {
                (*iter)->deleteRef();
            }

        delete actualTuple;
        actualTuple = NULL;
    }
}


void
QtJoinIterator::printTree(int tab, ostream &s, QtChildType mode)
{
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "QtJoinIterator Object: type " << flush;
    dataStreamType.printStatus(s);
    s << getEvaluationTime();
    s << endl;

    QtIterator::printTree(tab, s, mode);
}



void
QtJoinIterator::printAlgebraicExpression(ostream &s)
{
    s << "join";

    QtIterator::printAlgebraicExpression(s);
}


void
QtJoinIterator::open()
{
    startTimer("QtJoinIterator");

    QtIterator::open();

    outputStreamIsEmpty = false;  // initialization

    if (inputs)
    {
        // The idea of actualTuple initialization:
        //
        //    tuple[0]  tuple[1]  tuple[2]  ...  |
        //    -----------------------------------------------------
        //       0         0         0           |  initial phase
        //       0        b1        c1           |  open
        //      a1        b1        c1           |  next invocation
        //      a2        b1        c1           |        "
        //      a1        b2        c1           |        "
        //       :         :         :           |        "

        // allocate an empty tuple, right now each input stream provides one data element
        actualTuple = new QtDataList(inputs->size());

        // set the first element of the tuple to NULL
        (*actualTuple)[0] = NULL;

        // fill the tuple, except of the first element, with the first elements of the input streams
        //the first element is filled in the ::next() method
        for (unsigned int tuplePos = 1; tuplePos < actualTuple->size(); tuplePos++)
        {
            QtDataList *resultList = (*inputs)[tuplePos]->next();

            if (resultList)
            {
                // take the first data element of the input stream result
                (*actualTuple)[tuplePos] = (*resultList)[0];

                // delete the result vector (only the first data carriers is taken, the others are never deleted)
                delete resultList;
                resultList = NULL;
            }
            else
            {
                // In that case, one of the input streams is empty. Therefore, the output stream of
                // the self object is empty either.
                (*actualTuple)[tuplePos] = NULL;
                outputStreamIsEmpty = true;
            }
        }

        // Reset the first stream again, because the first tuple element is catched when next() is
        // called for the first time.
        // (*inputs)[0]->reset();
    }
    pauseTimer();
}


QtNode::QtDataList *
QtJoinIterator::next()
{
    resumeTimer();

    QtDataList *returnValue = NULL;

    if (inputs && actualTuple && !outputStreamIsEmpty)
    {
        bool        nextTupleAvailable = true;
        bool        nextTupleValid = false;
        unsigned int         tuplePos;
        QtDataList *resultList = NULL;
        QtONCStreamList::iterator iter;

        while (!nextTupleValid && nextTupleAvailable && !outputStreamIsEmpty)
        {
            // switch to the next tuple which means

            nextTupleAvailable = false;
            tuplePos           = 0;
            iter               = inputs->begin();

            while (!nextTupleAvailable && iter != inputs->end())
            {
                resultList = (*iter)->next();

                // Test, if the first input stream is empty, because this is not tested in open()
                if (resultList == NULL && tuplePos == 0 && (*actualTuple)[0] == 0)
                {
                    outputStreamIsEmpty = true;
                }

                if (resultList == NULL)
                {
                    (*iter)->reset();              // reset the stream ...
                    //this causes the first element of the list to be deleted - not the others
                    resultList = (*iter)->next();  // ... and read the first element again
                    // this was commented out because it will cause problems when the stream is closed
                    // if it is commented out it will break join queries
                }
                else
                {
                    nextTupleAvailable = true;
                }

                //
                // exchange the actual element in the tuple
                //

                //  delete the data carrier
                if ((*actualTuple)[tuplePos])
                {
                    (*actualTuple)[tuplePos]->deleteRef();
                    (*actualTuple)[tuplePos] = NULL;
                }

                if (resultList)
                {
                    // take the first data element of the input stream result - copy the data carrier pointer
                    (*actualTuple)[tuplePos] = (*resultList)[0];

                    // delete the result vector (only the first data carrier is taken, the others are never deleted)
                    delete resultList;
                    resultList = NULL;
                }

                iter++;
                tuplePos++;
            }

            if (nextTupleAvailable)
            {
                nextTupleValid = true;
            }
        }

        if (nextTupleAvailable)
        {
            // Copy the actual tuple in order to pass it as the next stream element
            // which means increase references to data elements.
            returnValue = new QtDataList(actualTuple->size());

            for (tuplePos = 0; tuplePos < actualTuple->size(); tuplePos++)
                if ((*actualTuple)[tuplePos])
                {
                    (*returnValue)[tuplePos] = (*actualTuple)[tuplePos];
                    (*actualTuple)[tuplePos]->incRef();
                }
                else
                {
                    // should not come here, because now the next tuple isn't valid

                    // delete return value again
                    for (tuplePos = 0; tuplePos < returnValue->size(); tuplePos++)
                        if ((*returnValue)[tuplePos])
                        {
                            (*returnValue)[tuplePos]->deleteRef();
                        }

                    delete returnValue;
                    returnValue = NULL;

                    LERROR << "Internal Error in QtJoinIterator::next()";
                }
        }
    }

    pauseTimer();

    return returnValue;
}



void
QtJoinIterator::close()
{
    if (actualTuple)
    {
        // first delete still existing data carriers
        for (QtDataList::iterator iter = actualTuple->begin(); iter != actualTuple->end(); iter++)
            if (*iter)
            {
                (*iter)->deleteRef();
            }

        delete actualTuple;
        actualTuple = NULL;
    }

    QtIterator::close();

    stopTimer();
}


void
QtJoinIterator::reset()
{
    // reset the input streams
    QtIterator::reset();

    if (inputs)
    {
        // first delete still existing data carriers
        for (QtDataList::iterator iter = actualTuple->begin(); iter != actualTuple->end(); iter++)
            if (*iter)
            {
                (*iter)->deleteRef();
                (*iter) = NULL;
            }

        // fill the tuple with the first elements of the input streams except of the first element
        for (unsigned int tuplePos = 1; tuplePos < actualTuple->size(); tuplePos++)
        {
            QtDataList *resultList = (*inputs)[tuplePos]->next();

            if (resultList)
            {
                // take the first data element of the input stream result
                (*actualTuple)[tuplePos] = (*resultList)[0];

                // delete the result vector (only the first data carriers is taken, the others are never deleted)
                delete resultList;
                resultList = NULL;
            }
            else
            {
                (*actualTuple)[tuplePos] = NULL;
            }

        }

        (*actualTuple)[0] = NULL;  // fist tuple element is catched when next() is called for the first time
    }
}



const QtTypeTuple &
QtJoinIterator::checkType()
{
    getInputTypeTuple(dataStreamType);

    return dataStreamType;
}
