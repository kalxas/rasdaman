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

static const char rcsid[] = "@(#)qlparser, QtMintervalOp: $Header: /home/rasdev/CVS-repository/rasdaman/qlparser/qtmintervalop.cc,v 1.12 2003/12/27 20:51:28 rasdev Exp $";

#include "config.h"
#include "raslib/rmdebug.hh"

#include "qlparser/qtmintervalop.hh"
#include "qlparser/qtdata.hh"
#include "qlparser/qtmintervaldata.hh"
#include "qlparser/qtintervaldata.hh"
#include "qlparser/qtatomicdata.hh"

#include "catalogmgr/ops.hh"
#include "relcatalogif/type.hh"

#include "../common/src/logging/easylogging++.hh"

#include <iostream>
#ifndef CPPSTDLIB
#include <ospace/string.h> // STL<ToolKit>
#else
#include <string>
using namespace std;
#endif


const QtNode::QtNodeType QtMintervalOp::nodeType = QT_MINTERVALOP;

QtMintervalOp::QtMintervalOp( QtOperationList* opList )
    :  QtNaryOperation( opList )
{
}



QtData*
QtMintervalOp::evaluate( QtDataList* inputList )
{
    startTimer("QtMintervalOp");

    QtData*     returnValue = NULL;
    QtDataList* operandList = NULL;

    if( getOperands( inputList, operandList ) )
    {
        vector<QtData*>::iterator dataIter;
        bool              goOn=true;

        // check for point operand
        if( operandList->size() == 1 && ((*operandList)[0])->getDataType() == QT_MINTERVAL )
        {
            // pass point as minterval projection
            returnValue = (*operandList)[0];

            delete operandList;
            operandList=NULL;
        }
        else
        {
            // first check operand types
            for( dataIter=operandList->begin(); dataIter!=operandList->end() && goOn; dataIter++ )
                if (!( (*dataIter)->getDataType() == QT_SHORT || (*dataIter)->getDataType() == QT_USHORT ||
                        (*dataIter)->getDataType() == QT_LONG  || (*dataIter)->getDataType() == QT_ULONG  ||
                        (*dataIter)->getDataType() == QT_OCTET || (*dataIter)->getDataType() == QT_CHAR   ||
                        (*dataIter)->getDataType() == QT_INTERVAL))
                {
                    goOn=false;
                    break;
                }

            if( !goOn )
            {
                LFATAL << "Error: QtMintervalOp::evaluate() - expressions for minterval dimensions must be either of type integer or interval.";
                parseInfo.setErrorNo(390);

                // delete the old operands
                if( operandList )
                {
                    for( dataIter=operandList->begin(); dataIter!=operandList->end(); dataIter++ )
                        if( (*dataIter) ) (*dataIter)->deleteRef();

                    delete operandList;
                    operandList=NULL;
                }

                throw parseInfo;
            }

            //
            // create a QtMintervalData object and fill it
            //
            r_Minterval   domainData( operandList->size() );
            vector<bool>* trimFlags = new vector<bool>( operandList->size() );
            unsigned int pos;

            for( dataIter=operandList->begin(), pos=0; dataIter!=operandList->end(); dataIter++,pos++ )
            {
                if( (*dataIter)->getDataType() == QT_INTERVAL )
                {
                    domainData << (static_cast<QtIntervalData*>(*dataIter))->getIntervalData();
                    (*trimFlags)[pos] = true;
                }
                else
                {
                    if( (*dataIter)->getDataType() == QT_SHORT ||
                            (*dataIter)->getDataType() == QT_LONG  ||
                            (*dataIter)->getDataType() == QT_OCTET )
                        domainData << (static_cast<QtAtomicData*>(*dataIter))->getSignedValue();
                    else
                        domainData << (static_cast<QtAtomicData*>(*dataIter))->getUnsignedValue();

                    (*trimFlags)[pos] = false;
                }
            }

            returnValue = new QtMintervalData( domainData, trimFlags );

            // delete the old operands
            if( operandList )
            {
                for( dataIter=operandList->begin(); dataIter!=operandList->end(); dataIter++ )
                    if( (*dataIter) ) (*dataIter)->deleteRef();

                delete operandList;
                operandList=NULL;
            }
        }
    }
    
    stopTimer();

    return returnValue;
}



void
QtMintervalOp::printTree( int tab, ostream& s, QtChildType mode )
{
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "QtMintervalOp Object " << static_cast<int>(getNodeType()) << getEvaluationTime() << endl;

    QtNaryOperation::printTree( tab, s, mode );
}



void
QtMintervalOp::printAlgebraicExpression( ostream& s )
{
    s << "[";

    QtNaryOperation::printAlgebraicExpression( s );

    s << "]";
}



const QtTypeElement&
QtMintervalOp::checkType( QtTypeTuple* typeTuple )
{
    dataStreamType.setDataType( QT_TYPE_UNKNOWN );

    QtOperationList::iterator iter;
    bool              opTypesValid = true;

    for( iter=operationList->begin(); iter!=operationList->end() && opTypesValid; iter++ )
    {
        const QtTypeElement& type = (*iter)->checkType( typeTuple );

        // valid types: interval, integers
        if (!( type.getDataType() == QT_INTERVAL ||
                type.getDataType() == QT_SHORT    ||
                type.getDataType() == QT_LONG     ||
                type.getDataType() == QT_OCTET    ||
                type.getDataType() == QT_USHORT   ||
                type.getDataType() == QT_ULONG    ||
                type.getDataType() == QT_CHAR))
        {
            opTypesValid=false;
            break;
        }
    }

    if( !opTypesValid )
    {
        LFATAL << "Error: QtMintervalOp::checkType() - expressions for minterval dimensions must be either of type integer or interval.";
        parseInfo.setErrorNo(390);
        throw parseInfo;
    }

    dataStreamType.setDataType( QT_MINTERVAL );

    return dataStreamType;
}
