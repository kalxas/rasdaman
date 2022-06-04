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

#include "qlparser/qtmshapeop.hh"
#include "qlparser/qtdata.hh"
#include "qlparser/qtpointdata.hh"
#include "qlparser/qtatomicdata.hh"
#include "qlparser/qtpointop.hh"

#include "catalogmgr/ops.hh"
#include "relcatalogif/type.hh"

#include <logging.hh>

#include <iostream>
#include <cmath>
#include <string>

using namespace std;

const QtNode::QtNodeType QtMShapeOp::nodeType = QT_MSHAPEOP;

QtMShapeOp::QtMShapeOp(QtOperationList *parsedOpList)
    : QtNaryOperation(parsedOpList)
{

}

QtData *
QtMShapeOp::evaluate(QtDataList *inputList)
{

    startTimer("QtMShapeOp");

    QtData *returnValue = NULL;
    QtDataList *operandList = NULL;

    if (!getOperands(inputList, operandList) || !operandList)
    {
        stopTimer();
        return returnValue;
    }

    std::unique_ptr<QtDataList> operandListPtr{operandList};

    // TODO: should be in checkType?
    // first check operand types
    bool pointAndMshapeOnly = std::all_of(operandList->begin(), operandList->end(), [](const QtData * val)
    {
        return val->getDataType() == QT_POINT || val->getDataType() == QT_MSHAPE;
    });
    if (!pointAndMshapeOnly)
    {
        LERROR << "Operands of mshape expression must be points or mshapes.";
        for (auto *dataIter : *operandList)
            if (dataIter)
            {
                dataIter->deleteRef();
            }
        parseInfo.setErrorNo(GRIDPOINTSONLY);
        throw parseInfo;
    }

    std::vector<r_Point> resultPoints;
    resultPoints.reserve(operandList->size());
    for (auto *data : *operandList)
    {
        if (data->getDataType() == QT_POINT)
        {
            resultPoints.emplace_back((static_cast<QtPointData *>(data))->getPointData());
        }
        else if (data->getDataType() == QT_MSHAPE)
        {
            std::vector<r_Point> appendingVector = (static_cast<QtMShapeData *>(data))->getPolytopePoints();
            resultPoints.insert(resultPoints.end(), appendingVector.begin(), appendingVector.end());
        }
        data->deleteRef();
    }

    returnValue = new QtMShapeData(resultPoints);

    stopTimer();

    return returnValue;
}

void
QtMShapeOp::printTree(int tab, std::ostream &s, QtChildType mode)
{
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "QtMShapeOp Object " << static_cast<int>(getNodeType()) << getEvaluationTime() << std::endl;

    QtNaryOperation::printTree(tab, s, mode);
}

void
QtMShapeOp::printAlgebraicExpression(std::ostream &s)
{
    s << "[";

    QtNaryOperation::printAlgebraicExpression(s);

    s << "]";
}

const
QtTypeElement &
QtMShapeOp::checkType(QtTypeTuple *typeTuple)
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
        LERROR << "Error: Operand of point expression must be of type QT_POINT.";
        parseInfo.setErrorNo(POINTEXP_WRONGOPERANDTYPE);
        throw parseInfo;
    }

    dataStreamType.setDataType(QT_MSHAPE);

    return dataStreamType;
}

int
QtMShapeOp::isLeftTurn(const std::deque<r_Point *> &vertices)
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

bool
QtMShapeOp::isValidSetOfPoints(const vector<r_Point> &polygon)
{
    // This method checks if the vertices of the user-defined polygon end up
    // forming a concave polygon.
    std::deque<r_Point *> vertices;

    // In case our mShape is a simple line in n-dim
    if (polygon.size() == 2)
    {
        return true;
    }
    // put the first two vertices in the deque
    vertices.push_back(const_cast<r_Point *>(&polygon[0]));
    vertices.push_back(const_cast<r_Point *>(&polygon[1]));
    for (size_t i = 2; i < polygon.size(); i++)
    {
        vertices.push_back(const_cast<r_Point *>(&polygon[i]));

        if (isLeftTurn(vertices) != 1)
        {
            return false;
        }

        vertices.pop_front();
    }
    return true;
}
