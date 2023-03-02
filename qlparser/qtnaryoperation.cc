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

#include "qlparser/qtnaryoperation.hh"
#include "qlparser/qtconst.hh"

#include <logging.hh>

#include <iostream>
#include <string>

using namespace std;

const QtNode::QtNodeType QtNaryOperation::nodeType = QtNode::QT_NARY_OPERATION;

QtNaryOperation::QtNaryOperation()
    : QtOperation(),
      operationList(NULL)
{
}

QtNaryOperation::QtNaryOperation(QtNode *node)
    : QtOperation(node),
      operationList(NULL)
{
}

QtNaryOperation::QtNaryOperation(QtOperationList *opList)
    : QtOperation(),
      operationList(opList)
{
    if (operationList)
    {
        QtOperationList::iterator iter;

        for (iter = operationList->begin(); iter != operationList->end(); iter++)
        {
            (*iter)->setParent(this);
        }
    }
}

QtNaryOperation::~QtNaryOperation()
{
    if (operationList)
    {
        QtOperationList::iterator iter;

        for (iter = operationList->begin(); iter != operationList->end(); iter++)
        {
            delete *iter;
            *iter = NULL;
        }

        delete operationList;
        operationList = NULL;
    }
}

void QtNaryOperation::simplify()
{
    // In order to work bottom up, first inspect the descendants
    QtNode::simplify();

    // Test, if all operands are available.
    if (!operationList)
    {
        return;
    }

    // Test, if all operands are of const type.
    for (auto iter = operationList->begin(); iter != operationList->end(); iter++)
        if (!(*iter) || (*iter)->getNodeType() != QT_CONST)
        {
            return;
        }

    // evaluate the self node with no input list
    QtData *newConst = this->evaluate(NULL);
    if (newConst)
    {
        // create a new constant node and fill it with newConst
        QtConst *newNode = new QtConst(newConst);
        // set its data stream type
        newNode->checkType(NULL);
        // link it to the parent
        getParent()->setInput(this, newNode);
        // delete the self node and its descendants
        delete this;
    }
}

bool QtNaryOperation::equalMeaning(QtNode *node)
{
    bool result;
    result = false;

    if (node && getNodeType() == node->getNodeType())
    {
        QtNaryOperation *naryNode = static_cast<QtNaryOperation *>(node);  // by force

        // get 2nd operation list
        QtOperationList *operationList2 = naryNode->getInputs();
        if (!operationList2)
            return result;

        // create iterators
        QtOperationList::iterator iter, iter2;
        result = true;
        for (iter = operationList->begin(), iter2 = operationList2->begin();
             iter != operationList->end() && iter2 != operationList2->end();
             iter++, iter2++)
        {
            if (!((*iter)->equalMeaning(*iter2)))
            {
                result = false;
                break;
            }
        }
        // input lists must have the same length
        result &= (iter == operationList->end() && iter2 == operationList2->end());
    }

    return result;
}

QtNode::QtNodeList *
QtNaryOperation::getChilds(QtChildType flag)
{
    QtNodeList *resultList = new QtNodeList();
    QtNodeList *subList = NULL;

    if (operationList)
    {
        for (auto iter = operationList->begin(); iter != operationList->end(); iter++)
        {
            if (flag == QT_LEAF_NODES || flag == QT_ALL_NODES)
            {
                subList = (*iter)->getChilds(flag);
                resultList->splice(resultList->begin(), *subList);
                delete subList;
                subList = NULL;
            }
            if (flag == QT_DIRECT_CHILDS || flag == QT_ALL_NODES)
            {
                resultList->push_back(*iter);
            }
        }
    }
    return resultList;
}

bool QtNaryOperation::getOperands(QtDataList *inputList, QtDataList *&operandList)
{
    if (operationList == 0)
    {
        LERROR << "No operation list specified.";
        return false;
    }
    if (std::any_of(operationList->begin(), operationList->begin(),
                    [](QtOperation *op)
                    {
                        return op == NULL;
                    }))
    {
        LERROR << "At least one operand branch is invalid.";
        return false;
    }

    operandList = NULL;  // make sure it's NULL in case of error

    auto *tmpOperandList = new QtDataList(operationList->size());
    QtDataListDeleter tmpOperandListDel{tmpOperandList};  // cleanup in case of error

    unsigned int pos = 0;
    for (auto iter = operationList->begin(); iter != operationList->end(); iter++)
    {
        (*tmpOperandList)[pos] = (*iter)->evaluate(inputList);
        if (!(*tmpOperandList)[pos])
        {
            LTRACE << "Operand " << pos << " is not provided.";
            return false;
        }
        pos++;
    }
    operandList = tmpOperandList;
    tmpOperandListDel.obj = NULL;  // reset so that operandList is not deleted
    return true;
}

string
QtNaryOperation::getSpelling()
{
    QtOperationList::iterator iter;

    char tempStr[20];
    sprintf(tempStr, "%lu", static_cast<unsigned long>(getNodeType()));
    string result = string(tempStr);

    result.append("(");

    if (operationList)
    {
        for (iter = operationList->begin(); iter != operationList->end(); iter++)
        {
            if (iter != operationList->begin())
            {
                result.append(",");
            }

            result.append((*iter)->getSpelling());
        }
    }

    result.append(")");

    LTRACE << "Result:" << result.c_str();

    return result;
}

void QtNaryOperation::setInput(QtOperation *inputOld, QtOperation *inputNew)
{
    QtOperationList::iterator iter;

    for (iter = operationList->begin(); iter != operationList->end(); iter++)
    {
        if (*iter == inputOld)
        {
            (*iter) = inputNew;

            if (inputNew)
            {
                inputNew->setParent(this);
            }
        }
    }
}

QtNode::QtAreaType
QtNaryOperation::getAreaType()
{
    return QT_AREA_SCALAR;
}

void QtNaryOperation::optimizeLoad(QtTrimList *trimList)
{
    // delete trimList
    // release( trimList->begin(), trimList->end() );
    for (QtNode::QtTrimList::iterator iter = trimList->begin(); iter != trimList->end(); iter++)
    {
        delete *iter;
        *iter = NULL;
    }
    delete trimList;
    trimList = NULL;

    if (operationList)
    {
        QtOperationList::iterator iter;

        for (iter = operationList->begin(); iter != operationList->end(); iter++)
            if (*iter)
            {
                (*iter)->optimizeLoad(new QtNode::QtTrimList);
            }
    }
}

void QtNaryOperation::printTree(int tab, ostream &s, QtChildType mode)
{
    if (mode != QT_DIRECT_CHILDS)
    {
        if (operationList)
        {
            QtOperationList::iterator iter;
            int no;

            for (no = 1, iter = operationList->begin(); iter != operationList->end(); iter++, no++)
                if (*iter)
                {
                    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "input" << no << ": " << endl;
                    (*iter)->printTree(tab + 2, s, mode);
                }
        }
    }
}

void QtNaryOperation::printAlgebraicExpression(ostream &s)
{
    s << "(";

    if (operationList)
    {
        QtOperationList::iterator iter;

        for (iter = operationList->begin(); iter != operationList->end(); iter++)
        {
            if (iter != operationList->begin())
            {
                s << ",";
            }

            if (*iter)
            {
                (*iter)->printAlgebraicExpression(s);
            }
            else
            {
                s << "<nn>";
            }
        }
    }
    else
    {
        s << "<nn>";
    }

    s << ")";
}
