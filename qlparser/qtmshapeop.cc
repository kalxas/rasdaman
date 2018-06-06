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

static const char rcsid[] = "@(#)qlparser, QtMShapeOp: $Id: qtMShapeOp.cc,v 1.7 2002/06/05 18:18:17 coman Exp $";

#include "config.h"
#include "raslib/rmdebug.hh"

#include "qlparser/qtmshapeop.hh"
#include "qlparser/qtdata.hh"
#include "qlparser/qtpointdata.hh"
#include "qlparser/qtatomicdata.hh"
#include "qlparser/qtpointop.hh"

#include "catalogmgr/ops.hh"
#include "relcatalogif/type.hh"

#include <logging.hh>

#include <iostream>
#ifndef CPPSTDLIB
#include <ospace/string.h> // STL<ToolKit>
#else
#include <string>
using namespace std;
#endif
#include <cmath>

const QtNode::QtNodeType QtMShapeOp::nodeType = QT_MSHAPEOP;

QtMShapeOp::QtMShapeOp(QtOperationList *parsedOpList)
    : QtNaryOperation(parsedOpList)
{
    //i don't think this is accomplishing what we hope -- it seems that the real work is beign done in evaluate, below, and this is accomplishing little-nothing.
    //todo (bbell): run a few tests for QtMShapeOp. Figure out if this can be invoked in oql.yy without being evaluated.
    // get bounding box
    for (auto iter = parsedOpList->begin(); iter < parsedOpList->end(); iter++)
    {
        if ((*iter)->getNodeType() == QT_POINTOP)
        {
            if ((dynamic_cast<QtPointOp *>(*iter))->getPoints())
            {
                int k = 0;
            }
            points.push_back(*(dynamic_cast<QtPointOp *>(*iter))->getPoints());
        }
        else
        {
            points.insert(points.end(), (dynamic_cast<QtMShapeOp *>(*iter))->getPoints().begin(), (dynamic_cast<QtMShapeOp *>(*iter))->getPoints().end());
        }
    }
}

QtData *
QtMShapeOp::evaluate(QtDataList *inputList)
{
    startTimer("QtMShapeOp");

    QtData *returnValue = NULL;
    QtDataList *operandList = NULL;

    if (getOperands(inputList, operandList))
    {
        vector<QtData *>::iterator dataIter;
        bool goOn = true;

        if (operandList)
        {
            // first check operand types
            for (dataIter = operandList->begin(); dataIter != operandList->end() && goOn; dataIter++)
                if ((*dataIter)->getDataType() != QT_POINT && (*dataIter)->getDataType() != QT_MSHAPE)
                {
                    goOn = false;
                    break;
                }

            if (!goOn)
            {
                LFATAL << "Error: QtMShapeOp::evaluate() - operands of point expression must be of type integer.";

                parseInfo.setErrorNo(GRIDPOINTSONLY);

                // delete the old operands
                if (operandList)
                {
                    for (dataIter = operandList->begin(); dataIter != operandList->end(); dataIter++)
                        if ((*dataIter))
                        {
                            (*dataIter)->deleteRef();
                        }

                    delete operandList;
                    operandList = NULL;
                }

                throw parseInfo;
            }

            // create and initialize the QtMShapeData's vector of polytope vertices
            // We already know that the dataIter will iterate through a set of points
            // since the check is done in the checkType function.
            vector<r_Point> polygonVertices;
            vector<QtMShapeData *> polytopeEdges;
            r_Nullvalues *nullValues = NULL;
            r_Dimension overAllDim{};

            bool isSimplePolytope = false;
            for (dataIter = operandList->begin(); dataIter != operandList->end(); dataIter++)

                if ((*dataIter)->getDataType() == QT_POINT)
                {

                    r_Point pt = (dynamic_cast<QtPointData*>(*dataIter))->getPointData();

                    // 1. Make sure all points have the same dimension
                    if (dataIter == operandList->begin())
                    {
                        overAllDim = pt.dimension();
                    }
                    else
                    {
                        if (overAllDim != pt.dimension())
                        {
                            LFATAL << "Error: QtMShapeOp::evaluate() - polygon vertices must have the same dimension.";
                            parseInfo.setErrorNo(VERTEXDIMENSIONMISMATCH);
                            throw parseInfo;
                        }
                    }

                    polygonVertices.push_back((dynamic_cast<QtPointData*>(*dataIter))->getPointData());
                    isSimplePolytope = true;
                }
                else if ((*dataIter)->getDataType() == QT_MSHAPE)
                {
                    QtMShapeData *mshape = (dynamic_cast<QtMShapeData *>(*dataIter));

                    polytopeEdges.push_back(dynamic_cast<QtMShapeData *>(*dataIter));
                }
            // QtMShape must be created with at least two points
            if (isSimplePolytope)
            {
                if (polygonVertices.size() < 2)
                {
                    LFATAL << "QtMShape::evaluate() - Too few points provided in order to construct the polytope";
                    parseInfo.setErrorNo(435);
                    throw parseInfo;
                }
                returnValue = new QtMShapeData(polygonVertices);
            }
            else
            {
                // the polytopes generating the new polytope must have the same dimension between one another.
                r_Dimension currentPolytopeDim = polytopeEdges[0]->getDimension();

                for (size_t i = 0; i < polytopeEdges.size(); i++)
                {
                    if (polytopeEdges[i]->getDimension() != currentPolytopeDim)
                    {
                        // throw an erro in the object construction since the faces constructing it need
                        // to be of a dimension one less.
                        LFATAL << "Error: QtMShapeOp::evaluate() - the faces of the polytope must have the same dimension.";
                        parseInfo.setErrorNo(FACEDIMENSIONMISMATCH);
                        throw parseInfo;
                    }
                }
                returnValue = new QtMShapeData(polytopeEdges);
            }

            returnValue->setNullValues(nullValues);

            // delete the old operands
            if (operandList)
            {
                for (dataIter = operandList->begin(); dataIter != operandList->end(); dataIter++)
                    if ((*dataIter))
                    {
                        (*dataIter)->deleteRef();
                    }

                delete operandList;
                operandList = NULL;
            }
        }
    }

    stopTimer();

    return returnValue;
}

void QtMShapeOp::printTree(int tab, std::ostream &s, QtChildType mode)
{
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "QtMShapeOp Object " << static_cast<int>(getNodeType()) << getEvaluationTime() << std::endl;

    QtNaryOperation::printTree(tab, s, mode);
}

void QtMShapeOp::printAlgebraicExpression(std::ostream &s)
{
    s << "[";

    QtNaryOperation::printAlgebraicExpression(s);

    s << "]";
}

const QtTypeElement &
QtMShapeOp::checkType(QtTypeTuple* typeTuple)
{
    dataStreamType.setDataType(QT_TYPE_UNKNOWN);

    QtOperationList::iterator iter;
    bool opTypesValid = true;
    for (iter = operationList->begin(); iter != operationList->end() && opTypesValid; iter++)
    {
        const QtTypeElement &type = (*iter)->checkType(typeTuple);
        // valid types: qt_point

        // dimension must match between points
        if (type.getDataType() != QT_POINT && type.getDataType() != QT_MSHAPE)
        {
            opTypesValid = false;
            break;
        }
    }

    if (!opTypesValid)
    {
        LFATAL << "Error: QtMShapeOp::checkType() - operand of point expression must be of type QT_POINT.";
        parseInfo.setErrorNo(410);
        throw parseInfo;
    }

    dataStreamType.setDataType(QT_MSHAPE);

    return dataStreamType;
}

int QtMShapeOp::isLeftTurn(const std::deque<r_Point *>& vertices)
{
    // This method checks if any vertices of the user-defined polygon end up
    // being co-linear or if the points form a non convex polygon
    // Algorithm used: Transformation of the dot product betweent the vector
    //                 extracted from the three points

    r_Point vec1 = *(vertices[0]) - *(vertices[1]);
    r_Point vec2 = *(vertices[1]) - *(vertices[2]);

    r_Point product = vec1 * vec2;
    r_Range length1 = 0, length2 = 0, length3 = 0;
    for (size_t i = 0; i < vec1.dimension(); i++)
    {
        length1 += vec1[i] * vec1[i];
        length2 += vec2[i] * vec2[i];
        length3 += product[i] * product[i];
    }
    length1 = sqrt(length1);
    length2 = sqrt(length2);
    length3 = sqrt(length3);
    // TODO(joana): change comparison if we decide to include DOUBLES
    if (length1 * length2 - length3 > 0)
    {
        // points form a counter clock wise angle
        return 1;
    }
    else if (length1 * length2 - length3 < 0)
    {
        // points form a clock wise angle
        return -1;
    }
    // Return 0 in case the points are collinear
    return 0;
}

bool QtMShapeOp::isValidSetOfPoints(const vector<r_Point>& polygon)
{
    // This method checks if the vertices of the user-defined polygon end up
    // forming a concave polygon.
    std::deque<r_Point*> vertices;

    // In case our mShape is a simple line in n-dim
    if (polygon.size() == 2)
    {
        return true;
    }
    // put the first two vertices in the deque
    vertices.push_back(const_cast<r_Point*>(&polygon[0]));
    vertices.push_back(const_cast<r_Point*>(&polygon[1]));
    for (size_t i = 2; i < polygon.size(); i++)
    {
        vertices.push_back(const_cast<r_Point*>(&polygon[i]));

        if (isLeftTurn(vertices) != 1)
        {
            return false;
        }

        vertices.pop_front();
    }
    return true;
}
