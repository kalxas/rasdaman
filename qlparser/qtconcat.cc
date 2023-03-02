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

#include "qlparser/qtconcat.hh"
#include "qlparser/qtdata.hh"
#include "qlparser/qtmdd.hh"
#include "tilemgr/tile.hh"
#include "relcatalogif/typefactory.hh"
#include "relcatalogif/basetype.hh"
#include "relcatalogif/mddbasetype.hh"
#include <logging.hh>

#include "mddmgr/mddobj.hh"

#include <iostream>
#include <string>
using namespace std;

const QtNode::QtNodeType QtConcat::nodeType = QT_CONCAT;

QtConcat::QtConcat(QtOperationList *opList, unsigned int dim)
    : QtNaryOperation(opList),
      dimension(dim)
{
}

bool QtConcat::equalMeaning(QtNode *node)
{
    bool result = false;

    if (nodeType == node->getNodeType())
    {
        QtConcat *condNode;
        condNode = static_cast<QtConcat *>(node);  // by force

        // check domain and cell expression
        result = QtNaryOperation::equalMeaning(condNode);

        // check dimension
        result &= (this->dimension == condNode->getDimension());
    };

    return (result);
}

string
QtConcat::getSpelling()
{
    char tempStr[20];
    sprintf(tempStr, "%lu", static_cast<unsigned long>(getNodeType()));
    string result = string(tempStr);
    result.append("(");
    result.append(QtNaryOperation::getSpelling());
    result.append(",");

    sprintf(tempStr, "%u", dimension);
    result.append(string(tempStr));

    result.append(")");

    return result;
}

void QtConcat::simplify()
{
    LTRACE << "simplify() warning: QtConcat itself is not simplified yet";

    // Default method for all classes that have no implementation.
    // Method is used bottom up.

    QtNodeList *resultList = NULL;
    QtNodeList::iterator iter;

    resultList = getChilds(QT_DIRECT_CHILDS);
    for (iter = resultList->begin(); iter != resultList->end(); iter++)
    {
        (*iter)->simplify();
    }

    delete resultList;
    resultList = NULL;
}

QtData *
QtConcat::evaluate(QtDataList *inputList)
{
    QtData *returnValue = NULL;
    QtDataList *operandList = NULL;

    if (getOperands(inputList, operandList))
    {
#ifdef QT_RUNTIME_TYPE_CHECK
        for (auto iter = operandList->begin(); iter != operandList->end(); iter++)
        {
            if ((*iter)->getDataType() != QT_MDD)
            {
                LERROR << "Internal error in QtConcat::evaluate() - "
                       << "runtime type checking failed (MDD).";

                // delete old operand list
                if (operandList)
                {
                    operandList->deleteRef();
                }

                return 0;
            }
        }
#endif
        std::unique_ptr<QtDataList> operandListDeleter(operandList);

        // check if type coercion is possible and compute the result type
        const auto *resultMDDType = static_cast<const MDDBaseType *>(dataStreamType.getType());
        const BaseType *baseType = resultMDDType->getBaseType();

        // compute the result domain
        vector<r_Point> tVector(operandList->size());  // save the translating vectors for all arrays except the first
        r_Minterval destinationDomain;
        unsigned int i = 0;
        for (auto iter = operandList->begin(); iter != operandList->end(); iter++, i++)
        {
            QtMDD *qtMDDObj = static_cast<QtMDD *>(*iter);
            if (iter == operandList->begin())
            {
                destinationDomain = qtMDDObj->getLoadDomain();
                if (destinationDomain.dimension() <= static_cast<r_Dimension>(dimension))
                {
                    LERROR << "the operands have less dimensions than the one specified";
                    parseInfo.setErrorNo(CONCAT_DIMENSIONMISMATCH);
                    throw parseInfo;
                }
            }
            else
            {
                // compute target position of the array in the result
                r_Point newPosB = destinationDomain.get_origin();
                newPosB[dimension] += destinationDomain.get_extent()[dimension];
                // translating vector as a difference between intial lower left
                // corner and target position in the new array
                const auto &opDom = qtMDDObj->getLoadDomain();
                tVector[i] = newPosB - opDom.get_origin();

                auto opDomTranslated = opDom.create_translation(tVector[i]);
                if (destinationDomain.is_mergeable(opDomTranslated))
                {
                    destinationDomain = opDomTranslated.create_closure(destinationDomain);
                }
                else
                {
                    LERROR << "operands of concat have non-mergeable domains";
                    parseInfo.setErrorNo(CONCAT_MINTERVALSNOTMERGEABLE);
                    throw parseInfo;
                }
            }
        }

        // create a transient MDD object for the query result
        MDDObj *resultMDD = new MDDObj(resultMDDType, destinationDomain);
        std::vector<std::pair<r_Double, r_Double>> nullvalues;
        i = 0;
        for (auto iter = operandList->begin(); iter != operandList->end(); iter++, i++)
        {
            QtMDD *qtMDDObj = static_cast<QtMDD *>(*iter);
            MDDObj *mddOp = qtMDDObj->getMDDObject();

            processOperand(i, qtMDDObj, resultMDD, baseType, tVector);

            auto *tempValues = mddOp->getNullValues();
            if (tempValues != NULL)
            {
                for (const auto &p: tempValues->getNullvalues())
                    nullvalues.push_back(p);
            }
        }

        if (!nullvalues.empty())
        {
            auto nullvaluesTmp = nullvalues;
            auto *tmp = new r_Nullvalues(std::move(nullvaluesTmp));
            resultMDD->setNullValues(tmp);
        }
        // create a new QtMDD object as carrier object for the transient MDD object
        returnValue = new QtMDD(resultMDD);
        if (!nullvalues.empty())
        {
            auto nullvaluesTmp = nullvalues;
            auto *tmp = new r_Nullvalues(std::move(nullvaluesTmp));
            returnValue->setNullValues(tmp);
        }
    }

    return returnValue;
}

void QtConcat::processOperand(unsigned int i, QtMDD *qtMDDObj, MDDObj *resultMDD,
                              const BaseType *baseType, const vector<r_Point> &tVector)
{
    MDDObj *mddOp = qtMDDObj->getMDDObject();
    const auto &mddOpDomain = qtMDDObj->getLoadDomain();

    // get intersecting tiles
    auto opTiles = std::unique_ptr<std::vector<shared_ptr<Tile>>>(mddOp->intersect(mddOpDomain));

    // iterate over source tiles
    for (const auto &opTile: *opTiles)
    {
        // get relevant area of source tile
        r_Minterval srcTileDomain = mddOpDomain.create_intersection(opTile->getDomain());
        // compute translated tile domain
        r_Minterval dstTileDomain = i == 0 ? srcTileDomain
                                           : srcTileDomain.create_translation(tVector[i]);
        // create a new transient tile, copy the transient data, and insert it into the mdd object
        Tile *newTransTile = new Tile(dstTileDomain, baseType);
        auto myOp = std::unique_ptr<UnaryOp>(Ops::getUnaryOp(
            Ops::OP_IDENTITY, baseType, mddOp->getCellType(), 0, 0));
        newTransTile->execUnaryOp(myOp.get(), dstTileDomain, opTile.get(), srcTileDomain);
        resultMDD->insertTile(newTransTile);
    }
}

void QtConcat::printTree(int tab, ostream &s, QtChildType mode)
{
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "QtConcat Object " << static_cast<int>(getNodeType()) << endl;

    QtNaryOperation::printTree(tab, s, mode);
}

void QtConcat::printAlgebraicExpression(ostream &s)
{
    s << "concat(";
    if (operationList)
    {
        QtOperationList::iterator iter;
        for (iter = operationList->begin(); iter != operationList->end(); iter++)
        {
            if (iter != operationList->begin())
                s << ",";

            if (*iter)
                (*iter)->printAlgebraicExpression(s);
            else
                s << "<nn>";
        }
    }
    else
    {
        s << "<nn>";
    }
    s << "; " << dimension;
    s << ")";
}

const QtTypeElement &
QtConcat::checkType(QtTypeTuple *typeTuple)
{
    dataStreamType.setDataType(QT_TYPE_UNKNOWN);

    QtTypeElement inputType;
    // check operand branches
    if (operationList)
    {
        QtOperationList::iterator iter;
        const BaseType *baseType = NULL;

        for (iter = operationList->begin(); iter != operationList->end(); iter++)
        {
            if (*iter)
                inputType = (*iter)->checkType(typeTuple);
            else
                LERROR << "operand branch invalid.";

            if (inputType.getDataType() != QT_MDD)
            {
                LERROR << "every operand of concat must be an MDD.";
                parseInfo.setErrorNo(CONCAT_WRONGOPERANDTYPES);
                throw parseInfo;
            }

            const auto *opType = static_cast<const MDDBaseType *>(inputType.getType())->getBaseType();
            if (!baseType)
            {
                baseType = opType;
            }
            else
            {
                if (!(*baseType == *opType))
                {
                    LERROR << "operand types of concat are incompatible";
                    parseInfo.setErrorNo(CONCAT_OPERANDTYPESINCOMPATIBLE);
                    throw parseInfo;
                }
            }
        }
        MDDBaseType *resultMDDType = new MDDBaseType("tmp", baseType);
        TypeFactory::addTempType(resultMDDType);
        dataStreamType.setType(resultMDDType);
    }
    else
    {
        LERROR << "operand branch invalid.";
    }
    return dataStreamType;
}

QtNode::QtAreaType
QtConcat::getAreaType()
{
    return QT_AREA_MDD;
}
