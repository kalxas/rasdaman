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

static const char rcsid[] = "@(#)qlparser, QtConst: $Header: /home/rasdev/CVS-repository/rasdaman/qlparser/qtconst.cc,v 1.22 2005/07/06 22:48:34 rasdev Exp $";

#include "config.h"
#include "qlparser/qtconst.hh"
#include "qlparser/qtscalardata.hh"
#include "qlparser/qtstringdata.hh"

#include "qlparser/qtmdd.hh"            // these three can be deleted with new memory management

#include "relcatalogif/basetype.hh"
#include "mddmgr/mddobj.hh"

#include <iostream>
#ifndef CPPSTDLIB
#include <ospace/string.h> // STL<ToolKit>
#else
#include <string>
using namespace std;
#endif

#include <iostream>

// deprecated, not available any longer -- PB 2005-jan-14
// #include <strstream.h>

const QtNode::QtNodeType QtConst::nodeType = QtNode::QT_CONST;


QtConst::QtConst(QtData* newDataObj)
    :  QtOperation(),
       dataObj(newDataObj)
{
    // store parse info of the data object
    setParseInfo(dataObj->getParseInfo());
}



QtConst::~QtConst()
{
    if (dataObj)
    {
        dataObj->deleteRef();
    }
}



bool
QtConst::equalMeaning(QtNode* node)
{
    bool result = false;

    if (nodeType == node->getNodeType())
    {
        QtConst* constObj = static_cast<QtConst*>(node);

        result = dataObj->equal(constObj->getDataObj());
    }

    return result;
}


string
QtConst::getSpelling()
{
    char tempStr[20];
    sprintf(tempStr, "%lu", static_cast<unsigned long>(getNodeType()));
    string result  = string(tempStr);
    result.append(dataObj->getSpelling());

    return result;
}


QtNode::QtAreaType
QtConst::getAreaType()
{
    if (dataObj && dataObj->getDataType() == QT_MDD)
    {
        return QT_AREA_MDD;
    }
    else
    {
        return QT_AREA_SCALAR;
    }
}


void
QtConst::optimizeLoad(QtTrimList* trimList)
{
    if (trimList)
    {
        if (trimList->size())
        {
            if (dataObj && dataObj->getDataType() == QT_MDD)
            {
                // get the highest specified dimension
                r_Dimension maxDimension = 0;
                QtTrimList::iterator i;

                for (i = trimList->begin(); i != trimList->end(); i++)
                    // get the maximum
                {
                    maxDimension = maxDimension > (*i)->dimension ? maxDimension : (*i)->dimension;
                }

                // create a new loadDomain object and initialize it with open bounds
                r_Minterval loadDomain(maxDimension + 1);

                // fill the loadDomain object with the QtTrimList specifications
                for (i = trimList->begin(); i != trimList->end(); i++)
                {
                    loadDomain[(*i)->dimension]    = (*i)->interval;
                }

                (static_cast<QtMDD*>(dataObj))->setLoadDomain(loadDomain);
            }

            // release( trimList->begin(), trimList->end() );
            vector<QtNode::QtTrimElement*>::iterator iter;
            for (iter = trimList->begin(); iter != trimList->end(); iter++)
            {
                delete *iter;
                *iter = NULL;
            }
        }

        delete trimList;
        trimList = NULL;
    }
}


QtData*
QtConst::evaluate(QtDataList* /*inputList*/)
{
    startTimer("QtConst");

    QtData* returnValue = NULL;

    if (dataObj)
    {
        dataObj->incRef();
        returnValue = dataObj;
    }

    stopTimer();

    return returnValue;
}



void
QtConst::printTree(int tab, ostream& s, QtChildType /*mode*/)
{
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "QtConst Object: type " << flush;
    dataStreamType.printStatus(s);
    s << endl;

    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "  ";

    if (dataObj)
    {
        dataObj->printStatus(s);
    }
    else
    {
        s << "<no data object>";
    }

    s << getEvaluationTime();
    s << endl;
}



void
QtConst::printAlgebraicExpression(ostream& s)
{
    if (dataObj->isScalarData())
    {
        QtScalarData* scalarDataObj = static_cast<QtScalarData*>(dataObj);

        if (scalarDataObj->getValueType())
        {
            // Print the value but first cut leading blanks.
            char valueString[1024];
            // replaced deprecated ostrstream -- PB 2005-jan-14
            // ostrstream valueStream( valueString, 1024 );
            ostringstream valueStream(valueString);

            scalarDataObj->getValueType()->printCell(valueStream, scalarDataObj->getValueBuffer());

            valueStream << ends;

            char* p = valueString;
            while (*p == ' ')
            {
                p++;
            }

            s << p;
        }
        else
        {
            s << "<nn>";
        }
    }
    else if (dataObj->getDataType() == QT_STRING)
    {
        s << (static_cast<QtStringData*>(dataObj))->getStringData().c_str();
    }
    else
    {
        dataObj->printStatus(s);
    }
}



const QtTypeElement&
QtConst::checkType(__attribute__((unused)) QtTypeTuple* typeTuple)
{
    dataStreamType.setDataType(QT_TYPE_UNKNOWN);

    if (dataObj)
    {
        switch (dataObj->getDataType())
        {
        case QT_STRING:
        case QT_INTERVAL:
        case QT_MINTERVAL:
        case QT_POINT:
        case QT_MSHAPE:
        case QT_GEOMETRY:
        case QT_NULLVALUES:
            dataStreamType.setDataType(dataObj->getDataType());
            break;
        case QT_MDD:
            if ((static_cast<QtMDD*>(dataObj))->getMDDObject())
            {
                dataStreamType.setType(static_cast<Type*>(const_cast<MDDBaseType*>((static_cast<QtMDD*>(dataObj))->getMDDObject()->getMDDBaseType())));
            }
            break;
        default:
            dataStreamType.setType((static_cast<QtScalarData*>(dataObj))->getValueType());
        }
    }

    return dataStreamType;
}



