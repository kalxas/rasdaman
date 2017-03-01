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

static const char rcsid[] = "@(#)qlparser, QtVariable: $Header: /home/rasdev/CVS-repository/rasdaman/qlparser/qtvariable.cc,v 1.24 2003/12/27 20:40:52 rasdev Exp $";

#include "config.h"
#include <iostream>
#include <sstream>
#include <vector>
using namespace std;

#include "raslib/rmdebug.hh"

#include "qlparser/qtvariable.hh"
#include "qlparser/qtmdd.hh"
#include "mymalloc/mymalloc.h"
#include "mddmgr/mddobj.hh"

#include <easylogging++.h>

const QtNode::QtNodeType QtVariable::nodeType = QT_MDD_VAR;


QtVariable::QtVariable(const string& initName)
    :  iteratorName(initName),
       loadDomain(),
       oldLoadDomain(),
       dataIndex(-1)
{
    domainFlag = new vector<bool>();
}


QtVariable::QtVariable(const string& initName, const r_Minterval& initDomain)
    :  iteratorName(initName),
       loadDomain(initDomain),
       oldLoadDomain(),
       dataIndex(-1)
{
    domainFlag = new vector<bool>();
}


QtVariable::~QtVariable()
{
    // delete STL vector including its elements
    delete domainFlag;
    domainFlag = NULL;
}


bool
QtVariable::equalMeaning(QtNode* node)
{
    bool result = false;

    if (nodeType == node->getNodeType())
    {
        QtVariable* mddVarNode;

        mddVarNode = static_cast<QtVariable*>(node); // by force

        if (iteratorName.compare(mddVarNode->getIteratorName()) == 0)
        {
            if ((loadDomain.dimension() == 0) ||
                    ((mddVarNode->getLoadDomain()).dimension() == 0))
            {
                result = true;
            }
            else if (loadDomain.dimension() == (mddVarNode->getLoadDomain()).dimension())
            {
                result = loadDomain.intersects_with(mddVarNode->getLoadDomain());
            }
        }
    };

    // equalMeaning() depends only on the loadDomain and not on the domainFlag!

    return result;
}


string
QtVariable::getSpelling()
{
    r_Point point;
    r_Dimension d;

    char tempStr[20];
    ostringstream os;
    sprintf(tempStr, "%lu", static_cast<unsigned long>(getNodeType()));
    string result  = string(tempStr);

    result.append(iteratorName);

    if (loadDomain.dimension() > 0)
    {
        os << loadDomain << ends;
        result.append(os.str());
    };

    return result;
}


QtNode::QtAreaType
QtVariable::getAreaType()
{
    return QT_AREA_MDD;
}


void
QtVariable::optimizeLoad(QtTrimList* trimList)
{
    if (!trimList->empty())
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
        loadDomain = r_Minterval(maxDimension + 1);

        delete domainFlag; // delete the old array
        domainFlag = new vector<bool>(maxDimension + 1);

        for (unsigned int j = 0; j < loadDomain.dimension(); j++)
        {
            loadDomain[j]    = r_Sinterval('*', '*');
            (*domainFlag)[j] = true;
        }

        // fill the loadDomain object with the QtTrimList specifications
        for (i = trimList->begin(); i != trimList->end(); i++)
        {
            loadDomain[(*i)->dimension]    = (*i)->interval;
            (*domainFlag)[(*i)->dimension] = (*i)->intervalFlag;
        }

        // delete heap based elements
        // release( trimList->begin(), trimList->end() );
        for (QtNode::QtTrimList::iterator iter = trimList->begin(); iter != trimList->end(); iter++)
        {
            delete *iter;
            *iter = NULL;
        }

        // changed from RMInit:logOut -- PB 2003-nov-20
        LTRACE << "optimizeLoad: geometric load optimization: " << iteratorName << loadDomain;
    }

    // delete list
    delete trimList;
    trimList = NULL;
}



QtData*
QtVariable::evaluate(QtDataList* inputList) throw (ParseInfo)
{
    startTimer("QtVariable");

    vector<QtData*>::iterator i; //default

    QtData* returnValue = NULL;

    if (inputList /* && inputList->size() > dataIndex*/)
    {
        QtData* dataObject = 0;

        if (dataIndex == -1)
        {
            // Search for the data object matching the iterator name
            int pos = 0;

            for (QtDataList::iterator iter = inputList->begin(); iter != inputList->end() && !dataObject; iter++)
            {
                if (*iter && iteratorName == (*iter)->getIteratorName())
                {
                    dataObject = *iter;
                }
                else
                {
                    pos++;
                }
            }

            if (dataObject)
            {
                dataIndex = pos;
            }
        }
        else
            // For performance reasons, take the data element from position determined in the first run.
        {
            dataObject = (*inputList)[static_cast<size_t>(dataIndex)];
        }

        if (!dataObject)
        {
            LFATAL << "Error: QtVariable::evaluate() - collection iterator " <<
                   iteratorName.c_str() << " is unknown.";
            parseInfo.setErrorNo(357);
            throw parseInfo;
        }

        if (dataObject->getDataType() == QT_MDD)
        {
            if (loadDomain.dimension() == 0)
            {
                // If no domain is specified, the load domain equals the current domain.
                // This means that the data object is passed with an incremented reference.
                // This mainly occurs with point accesses.

                dataObject->incRef();
                returnValue = dataObject;
            }
            else
            {
                QtMDD*  qtMDD         = static_cast<QtMDD*>(dataObject);
                MDDObj* currentMDDObj = qtMDD->getMDDObject();

                LTRACE << "  definitionDomain: " << currentMDDObj->getDefinitionDomain();
                LTRACE << "  currentDomain...: " << currentMDDObj->getCurrentDomain();

                // load domain for the actual MDDObj
                r_Minterval actLoadDomain;

                // intersect actLoadDomain with defined domain
                try
                {
                    actLoadDomain.intersection_of(loadDomain, currentMDDObj->getCurrentDomain());
                }
                catch (r_Edim_mismatch&)
                {
                    parseInfo.setErrorNo(362);
                    throw parseInfo;
                }
                catch (r_Eno_interval)
                {
                    // ticket:358
                    // Instead of throwing an exception, return an MDD initialized
                    // with null values when selecting an area that doesn't intersect
                    // with any existing tiles in the database -- DM 2013-nov-15
                    LWARNING << "Warning: specified domain " << loadDomain
                             << " does not intersect with spatial domain of MDD, returning empty result.";

                    const MDDBaseType* mddType = currentMDDObj->getMDDBaseType();

                    // create a transient MDD object for the query result
                    MDDObj* resultMDD = new MDDObj(mddType, loadDomain);

                    // create transient tile
                    Tile* resTile = new Tile(loadDomain, mddType->getBaseType());
                    resTile->setPersistent(false);

                    // insert Tile in result mddObj
                    resultMDD->insertTile(resTile);
                    returnValue = new QtMDD(resultMDD);

                    stopTimer();
                    return returnValue;

//                    LFATAL << "Error: QtVariable::evaluate() - Specified domain does not intersect with spatial domain of MDD.";
//                    parseInfo.setErrorNo(356);
//                    throw parseInfo;
                }
                catch (r_Error& err)
                {
                    LFATAL << "Error: QtVariable::evaluate() - general error.";
                    parseInfo.setErrorNo(350);
                    throw parseInfo;
                }

                LTRACE << "  loadDomain......: " << actLoadDomain;

                if (qtMDD->getLifetime() == QtData::QT_PERSISTENT)
                {
                    //
                    // Create a new QtMDD object as carrier object for the persistent MDD object
                    // and attach the load domain. Now there are more than one MDD objects pointing
                    // to the same persistent MDD object which should not cause any problem.
                    //
                    // Note: Taking the same MDD object would mean sharing also the load domain which
                    //       is not possible if the iterator variable occurs with different spatial operations.
                    //

                    QtMDD* result = new QtMDD(currentMDDObj);
                    result->setLoadDomain(actLoadDomain);

                    returnValue = result;
                }
                else
                {
                    // Take the transient data object and increase its reference.
                    //
                    // Note: For a transient MDD object just one QtMDD carrier object is allowed.

                    qtMDD->incRef();
                    returnValue = qtMDD;
                }

            }

        }
        else
        {
            // Take the atomic data object and increase its reference.
            dataObject->incRef();
            returnValue = dataObject;
        }

    }
    else
    {
        LTRACE << "Error: QtVariable::evaluate() - the input list is empty.";
    }

    stopTimer();

    return returnValue;
}


void
QtVariable::printTree(int tab, ostream& s, QtChildType /*mode*/)
{
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "QtVariable Object: type " << flush;
    dataStreamType.printStatus(s);
    s << " name |" << iteratorName.c_str() << "|" << getEvaluationTime() /* << " pos " << dataIndex */ << endl;

    if (loadDomain.dimension() > 0)
    {
        s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "load domain: ";
        loadDomain.print_status(s);
        s << " - Trimflag: ";

        for (unsigned int i = 0; i < domainFlag->size(); i++)
        {
            s << (*domainFlag)[i];
        }
        s << endl;
    }

    if (oldLoadDomain.dimension() > 0)
    {
        s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "old  domain: ";
        oldLoadDomain.print_status(s);
        s << endl;
    };
}



void
QtVariable::printAlgebraicExpression(ostream& s)
{
    s << iteratorName.c_str() << flush;
}



const QtTypeElement&
QtVariable::checkType(QtTypeTuple* typeTuple) throw (ParseInfo)
{
    dataStreamType.setDataType(QT_TYPE_UNKNOWN);

    if (typeTuple)
    {
        vector<QtTypeElement>::iterator iter;

        // search for the type matching the variable name
        for (iter = typeTuple->tuple.begin();
                iter != typeTuple->tuple.end() && dataStreamType.getDataType() == QT_TYPE_UNKNOWN;
                iter++)
        {
            if ((*iter).getName() && iteratorName == string((*iter).getName()))
            {
                dataStreamType = *iter;
            }
        }
    }

    if ((dataStreamType.getDataType() == QT_TYPE_UNKNOWN))
    {
        LFATAL << "Error: QtVariable::checkType() - variable " << iteratorName.c_str() << " is unknwon.";
        parseInfo.setErrorNo(357);
        throw parseInfo;
    }

    return dataStreamType;
}



