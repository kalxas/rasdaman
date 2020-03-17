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

#include "qlparser/qtmarrayop2.hh"
#include "qlparser/qtdata.hh"
#include "qlparser/qtmdd.hh"
#include "qlparser/qtpointdata.hh"
#include "qlparser/qtmintervaldata.hh"
#include "qlparser/qtmintervalop.hh"
#include "qlparser/algebraops.hh"

#include "mddmgr/mddobj.hh"
#include "qlparser/querytree.hh"

#include "relcatalogif/typefactory.hh"
#include "relcatalogif/basetype.hh"
#include "relcatalogif/longtype.hh"

#include <logging.hh>

#include <iostream>
#include <string>
using namespace std;

extern QueryTree *parseQueryTree;

QtMarrayOp2::QtMarrayOp2(mddIntervalListType *&aList, QtOperation *&cellExp)
    :  iterators(*aList), qtOperation(cellExp)
{
}

bool QtMarrayOp2::concatenateIntervals()
{
    // check for validity
    bool concatenate = true;

    for (auto ii = iterators.begin(); ii != iterators.end() ; ii++)
    {
        auto *ddd = (static_cast<QtMintervalOp *>(ii->tree))->getInputs();
        for (auto j = ddd->begin(); j != ddd->end(); j++)
        {
            if (static_cast<QtNode *>(*j))
            {
                auto nodeType = (*j)->getNodeType();
                if (nodeType == QtNode::QT_INTERVALOP)
                {
                    auto *binOp = static_cast<QtBinaryOperation *>(*j);
                    if (binOp->getInput1()->getNodeType() != QtNode::QT_CONST)
                        concatenate = false;
                    if (binOp->getInput2()->getNodeType() != QtNode::QT_CONST)
                        concatenate = false;
                }
                else if (nodeType != QtNode::QT_CONST)
                {
                    concatenate = false;
                }
            }
        }
    }
    if (concatenate)
    {
        // compute total dimension
        LTRACE << "QtMarrayOp2: Dimensions: ";
        greatDimension = 0;
        greatIterator = "";
        std::vector<r_Sinterval> intervals;
        for (auto i = iterators.begin(); i != iterators.end() ; i++)
        {
            // evaluate intervals
            QtData *data = (i->tree)->evaluate(0);
            auto *mintervalData = static_cast<QtMintervalData *>(data);
            const auto &domain = mintervalData->getMintervalData();
            LTRACE << i->variable << ": " << domain.dimension();
            i->dimensionOffset = greatDimension; // used later in rewriteVars / traverse
            
            // extend dimension and list of sintervals
            greatDimension += domain.dimension();
            for (r_Dimension j = 0; j < domain.dimension(); ++j)
                intervals.push_back(domain[j]);
            
            // update new iterator
            if (!greatIterator.empty())
              greatIterator += "_";
            greatIterator += i->variable;
        }
        LTRACE << "Total: " << greatDimension;

        // concatenate the data of the intervals into one big interval
        greatDomain = r_Minterval(greatDimension);
        for (const auto &interval: intervals)
          greatDomain << interval;
        LTRACE << "QtMarray2 combined domain: " << greatDomain;
    }
    return concatenate;
}



void QtMarrayOp2::rewriteVars()
{
    if (!oldMarray)
    {
        // concatenate the identifier names to one big identifier name
        greatIterator = "";
        for (auto i = iterators.begin(); i != iterators.end() ; i++)
        {
            if (!greatIterator.empty())
              greatIterator += "_";
            greatIterator += i->variable;
        }
        LTRACE << "concatenated iterator: " << greatIterator;
    }
    traverse(qtOperation);
}

void QtMarrayOp2::traverse(QtOperation *&node)
{
    if (!node)
        return;

    QtOperation *temp = NULL;

    if (node->getNodeType() == QtNode::QT_DOMAIN_OPERATION)
    {
        // syntax: domainNode ( dinput [ dmiop ] )
        auto *domainNode = static_cast<QtDomainOperation *>(node);

        // traverse dmiop
        QtOperation *dmiop = domainNode->getMintervalOp();
        temp = dmiop;
        traverse(temp);
        dmiop = temp;
        domainNode->setMintervalOp(dmiop);

        // if dinput == QtVariable then rewrite it
        QtOperation *dinput = domainNode->getInput();

        // if not a variable, then recurse
        if (dinput->getNodeType() != QtNode::QT_MDD_VAR)
        {
            // traverse dinput
            temp = dinput;
            traverse(temp);
            dinput = temp;
            domainNode->setInput(dinput);

            // get childs and traverse them
            auto *childList = node->getChilds(QtNode::QT_DIRECT_CHILDS);
            for (auto iter = childList->begin(); iter != childList->end(); iter++)
            {
                temp = static_cast<QtOperation *>(*iter);
                traverse(temp);
                *iter = temp;
            }
            delete childList;
            childList = NULL;
        }
    }
    else
    {
        if (node->getNodeType() == QtNode::QT_MDD_VAR)
        {
            auto *varNode = static_cast<QtVariable *>(node);
            if (QueryTree::symtab.lookupSymbol(varNode->getIteratorName()))
            {
                // find index in new marray minterval
                r_Long index = 0;
                for (const auto &i: iterators)
                    if (i.variable == varNode->getIteratorName())
                        index = r_Long(i.dimensionOffset);
                if (!oldMarray)
                {
                    LTRACE << "marray index variable " << varNode->getIteratorName() 
                           << " will be replaced with " << greatIterator << "[" << index << "]";
                    varNode->setIteratorName(greatIterator);
                }
                // replace with var[0]
                auto *dop = new QtDomainOperation(new QtConst(new QtAtomicData(index, sizeof(r_Long))));
                dop->setInput(varNode);
                node = dop;
                parseQueryTree->addDomainObject(dop);
            }
        }
        else
        {
            // traverse inputs
            switch (node->getNodeType())
            {
            /*
            // with no input
            case QtNode::QT_UNDEFINED_NODE:
            case QtNode::QT_MDD_ACCESS:
            case QtNode::QT_OPERATION_ITERATOR:
            case QtNode::QT_SELECTION_ITERATOR:
            case QtNode::QT_JOIN_ITERATOR:
            case QtNode::QT_UPDATE:
            case QtNode::QT_INSERT:
            case QtNode::QT_DELETE:
            case QtNode::QT_COMMAND:
            case QtNode::QT_PYRAMID:
            case QtNode::QT_MDD_VAR:
            case QtNode::QT_CONST:
            case QtNode::QT_MDD_STREAM:
            */
            // from Unary
            case QtNode::QT_TANH:
            case QtNode::QT_TAN:
            case QtNode::QT_SQRT:
            case QtNode::QT_SINH:
            case QtNode::QT_SIN:
            case QtNode::QT_NOT:
            case QtNode::QT_LOG:
            case QtNode::QT_LN:
            case QtNode::QT_EXP:
            case QtNode::QT_DOT:
            case QtNode::QT_COSH:
            case QtNode::QT_COS:
            case QtNode::QT_ARCTAN:
            case QtNode::QT_ARCSIN:
            case QtNode::QT_ARCCOS:
            case QtNode::QT_ABS:
            case QtNode::QT_REALPART:
            case QtNode::QT_IMAGINARPART:
            case QtNode::QT_CAST:
            case QtNode::QT_SDOM:
            case QtNode::QT_OID:
            case QtNode::QT_LO:
            case QtNode::QT_HI:
         // case QtNode::QT_DOMAIN_OPERATION:
            case QtNode::QT_CONVERSION:
            case QtNode::QT_SOME:
            case QtNode::QT_MINCELLS:
            case QtNode::QT_MAXCELLS:
            case QtNode::QT_COUNTCELLS:
            case QtNode::QT_AVGCELLS:
            case QtNode::QT_ALL:
            case QtNode::QT_ADDCELLS:
            case QtNode::QT_CSE_ROOT:
            {
                auto *unaryNode = static_cast<QtUnaryOperation *>(node);
                QtOperation *uinput = unaryNode->getInput();
                temp = uinput;
                traverse(temp);
                uinput = temp;
                unaryNode->setInput(uinput);
            }
            break;

            // from Binary
            case QtNode::QT_SHIFT:
            case QtNode::QT_SCALE:
            case QtNode::QT_MARRAYOP:
            case QtNode::QT_INTERVALOP:
            case QtNode::QT_CONDENSEOP:
            case QtNode::QT_XOR:
            case QtNode::QT_PLUS:
            case QtNode::QT_MAX_BINARY:
            case QtNode::QT_MIN_BINARY:
            case QtNode::QT_OR:
            case QtNode::QT_NOT_EQUAL:
            case QtNode::QT_MULT:
            case QtNode::QT_MINUS:
            case QtNode::QT_LESS_EQUAL:
            case QtNode::QT_LESS:
            case QtNode::QT_IS:
            case QtNode::QT_EQUAL:
            case QtNode::QT_DIV:
            case QtNode::QT_INTDIV:
            case QtNode::QT_MOD:
            case QtNode::QT_AND:
            case QtNode::QT_OVERLAY:
            case QtNode::QT_BIT:
            {
                auto *binaryNode = static_cast<QtBinaryOperation *>(node);
                QtOperation *binput1 = binaryNode->getInput1();
                QtOperation *binput2 = binaryNode->getInput2();
                QtOperation *temp1 = binput1;
                QtOperation *temp2 = binput2;
                traverse(temp1);
                traverse(temp2);
                binput1 = temp1;
                binput2 = temp2;
                binaryNode->setInput1(binput1);
                binaryNode->setInput2(binput2);
            }
            break;

            // from Nary
            case QtNode::QT_POINTOP:
            case QtNode::QT_MINTERVALOP:
            case QtNode::QT_RANGE_CONSTRUCTOR:
            case QtNode::QT_CONCAT:
            {
                auto *naryNode = static_cast<QtNaryOperation *>(node);
                QtNode::QtOperationList *ninput = naryNode->getInputs();
                for (auto iter = ninput->begin(); iter != ninput->end(); iter++)
                {
                    temp = static_cast<QtOperation *>(*iter);
                    traverse(temp);
                    *iter = temp;
                }
                naryNode->setInputs(ninput);
            }
            break;
            default:
            {
                // do nothing
            }
            break;
            }

            // get childs and traverse them
            QtNode::QtNodeList *childList = node->getChilds(QtNode::QT_DIRECT_CHILDS);
            for (auto iter = childList->begin(); iter != childList->end(); iter++)
            {
                temp = static_cast<QtOperation *>(*iter);
                traverse(temp);
                *iter = temp;
            }
            delete childList;
            childList = NULL;
        }
    }
}
