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
 *
 *
 * COMMENTS:
 *
 ************************************************************/

static const char rcsid[] = "@(#)qlparser, QLMarrayOp, QLCondenseOp: $Header: /home/rasdev/CVS-repository/rasdaman/catalogmgr/algebraops.cc,v 1.5 2003/12/20 23:41:27 rasdev Exp $";

#include "config.h"
#include "raslib/rmdebug.hh"

#include "algebraops.hh"

#include "qlparser/qtoperation.hh"
#include "qlparser/qtdata.hh"
#include "qlparser/qtpointdata.hh"
#include "qlparser/qtscalardata.hh"
#include "qlparser/qtatomicdata.hh"
#include "qlparser/qtbinaryinduce.hh"

#include "relcatalogif/basetype.hh"

#include <logging.hh>

QLMarrayOp::QLMarrayOp(QtOperation     *newCellExpression,
                       std::vector<QtData *> *newDataList,
                       std::string           &newIteratorName,
                       BaseType        *newResType,
                       unsigned int     newResOff) :
    MarrayOp(newResType, newResOff),
    cellExpression(newCellExpression),
    dataList(newDataList),
    iteratorName(newIteratorName)
{
}



QLMarrayOp::~QLMarrayOp()
{
}



void
QLMarrayOp::operator()(char *result, const r_Point &p)
{
    // update point data of input list
    if (dataList)
    {
        (static_cast<QtPointData *>(dataList->back()))->setPointData(p);
    }

    if (cellExpression)
    {
        QtData *resultData = cellExpression->evaluate(dataList);

        if (resultData)
        {
            if (resultData->isScalarData())
            {
                QtScalarData *scalarResultData = static_cast<QtScalarData *>(resultData);
                memcpy(static_cast<void *>(result),
                       static_cast<void *>(const_cast<char *>(scalarResultData->getValueBuffer())),
                       scalarResultData->getValueType()->getSize());
            }
            else
            {
                LERROR << "Internal Error: QLMarrayOp::operator() - cell type invalid.";
            }
            resultData->deleteRef();
        }
    }
}




QLCondenseOp::QLCondenseOp(QtOperation     *newCellExpression,
                           QtOperation     *newCondExpression,
                           std::vector<QtData *> *newDataList,
                           std::string           &newIteratorName,
                           const BaseType        *newResType,
                           unsigned int     newResOff,
                           BinaryOp        *newAccuOp,
                           char            *newInitVal)

    :  GenCondenseOp(newResType, newResOff, newAccuOp, newInitVal),
       cellExpression(newCellExpression),
       condExpression(newCondExpression),
       dataList(newDataList),
       iteratorName(newIteratorName)
{
    //
    // add point with its iterator name to the data list
    //

    // create QtPointData object
    QtPointData *pointData = new QtPointData(r_Point());

    // set its iterator name
    pointData->setIteratorName(iteratorName);

    // add it to the list
    dataList->push_back(pointData);
}



QLCondenseOp::~QLCondenseOp()
{
    // remove point data object from inputList again
    dataList->back()->deleteRef();
    dataList->pop_back();
}



void
QLCondenseOp::operator()(const r_Point &p)
{
    unsigned int currentCellValid = 1;

    // update point data of input list
    if (dataList)
    {
        (static_cast<QtPointData *>(dataList->back()))->setPointData(p);
    }

    if (condExpression)
    {
        QtData *condData = condExpression->evaluate(dataList);
#ifdef QT_RUNTIME_TYPE_CHECK
        if (condData->getDataType() != QT_BOOL)
        {
            LERROR << "Internal error in QLCondenseOp::operator() - "
                   << "runtime type checking failed (BOOL).";
        }
        else
#endif
            currentCellValid = (static_cast<QtAtomicData *>(condData))->getUnsignedValue();
        condData->deleteRef();
    }

    if (currentCellValid)
    {
        QtData *resultData = cellExpression->evaluate(dataList);
        if (resultData)
        {
#ifdef QT_RUNTIME_TYPE_CHECK
            if (!(resultData->isScalarData()))
            {
                LERROR << "Internal Error: QLCondenseOp::operator() - cell type invalid.";
            }
            else
#endif
            {
                QtScalarData *scalarResultData = static_cast<QtScalarData *>(resultData);
                (*accuOp)(initVal, initVal, scalarResultData->getValueBuffer());
            }

            resultData->deleteRef();
        }
    }
}

QLInducedCondenseOp::QLInducedCondenseOp(QtOperation *newCellExpression,
        QtOperation *newCondExpression,
        std::vector<QtData *> *newDataList,
        BinaryOp *newMyOp,
        std::string iteratorName)
    : cellExpression(newCellExpression),
      condExpression(newCondExpression),
      dataList(newDataList),
      myOp(newMyOp)

{
    //
    // add point with its iterator name to the data list
    //

    // create QtPointData object
    QtPointData *pointData = new QtPointData(r_Point());

    // set its iterator name
    pointData->setIteratorName(iteratorName);

    // add it to the list
    dataList->push_back(pointData);

    accumulatedValue = NULL;
}

void
QLInducedCondenseOp::operator()(const r_Point &p)
{
    bool currentCellValid = true;
    // update point data of input list
    if (dataList)
    {
        (static_cast<QtPointData *>(dataList->back()))->setPointData(p);
    }
    if (condExpression)
    {
        QtData *condData = condExpression->evaluate(dataList);
#ifdef QT_RUNTIME_TYPE_CHECK
        if (condData->getDataType() != QT_BOOL)
        {
            LERROR << "Internal error in QLCondenseOp::operator() - "
                   << "runtime type checking failed (BOOL).";
        }
        else
#endif
            currentCellValid = (static_cast<QtAtomicData *>(condData))->getUnsignedValue();
        condData->deleteRef();
    }
    if (currentCellValid)
    {
        QtMDD *resultData = static_cast<QtMDD *>(cellExpression->evaluate(dataList));
        //execute binary operation on current tile
        //if accumulatedValue has not been initialized, it actually takes the value of the tile
        if (accumulatedValue == NULL)
        {
            accumulatedValue = resultData;
        }
        //else, accumulated value becomes the condense op applied to accumulatedValue and currentValue
        else
        {
            QtMDD *result = static_cast<QtMDD *>(QtBinaryInduce::computeBinaryMDDOp(accumulatedValue, resultData, accumulatedValue->getCellType(), myOp));
            //delete the mdds as they are not used
            accumulatedValue->deleteRef();
            resultData->deleteRef();
            //update the accumulated values
            accumulatedValue = result;
        }
    }
}

QtMDD *
QLInducedCondenseOp::getAccumulatedValue()
{
    return accumulatedValue;
}


QtMDD *
QLInducedCondenseOp::execGenCondenseInducedOp(QLInducedCondenseOp *myOp, const r_Minterval &areaOp)
{
    r_Point pOp(areaOp.dimension());
    int done = 0;
    int i = 0;
    unsigned int j = 0;
    unsigned int dim = areaOp.dimension();

    // initialize points
    for (j = 0; j < dim; j++)
    {
        pOp << areaOp[j].low();
    }

#ifdef RMANBENCHMARK
    opTimer.resume();
#endif
    // iterate over all cells
    while (!done)
    {
        (*myOp)(pOp);
        // increment coordinates
        i = static_cast<int>(dim) - 1;
        ++pOp[static_cast<r_Dimension>(i)];
        while (pOp[static_cast<r_Dimension>(i)] > areaOp[static_cast<r_Dimension>(i)].high())
        {
            pOp[static_cast<r_Dimension>(i)] = areaOp[static_cast<r_Dimension>(i)].low();
            i--;
            if (i < 0)
            {
                done = 1;
                break;
            }
            ++pOp[static_cast<r_Dimension>(i)];
        }
    }
#ifdef RMANBENCHMARK
    opTimer.pause();
#endif
    return myOp->getAccumulatedValue();
}

QLInducedCondenseOp::~QLInducedCondenseOp()
{
    // remove point data object from inputList again
    dataList->back()->deleteRef();
    dataList->pop_back();
}
