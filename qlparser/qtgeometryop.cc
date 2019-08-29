/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * File:   QtGeometryOp.cc
 * Author: bbell
 *
 * Created on June 26, 2018, 2:10 PM
 */

#include "qlparser/qtgeometryop.hh"
#include "qlparser/qtgeometrydata.hh"
#include "qlparser/qtnode.hh"

//the node type is static
const QtNode::QtNodeType QtGeometryOp::nodeType = QtNode::QT_GEOMETRYOP;

QtGeometryOp::~QtGeometryOp()
{
}

QtGeometryOp::QtGeometryOp(QtOperationList *vectorListsArg,
                           QtGeometryData::QtGeometryType geomTypeArg,
                           QtGeometryData::QtGeometryFlag geomFlagArg)
    : QtNaryOperation(vectorListsArg), geomType(geomTypeArg), geomFlag(geomFlagArg)
{
}

QtData *
QtGeometryOp::evaluate(QtDataList *inputList)
{
    startTimer("QtGeometryOp");

    QtData *returnValue = NULL;
    QtDataList *operandList = NULL;

    if (!getOperands(inputList, operandList) || !operandList)
    {
        stopTimer();
        return returnValue;
    }

    std::unique_ptr<QtDataList> operandListPtr{operandList};

    //we want to create a QtGeometryData object (derived class from QtData)
    //geometry data objects are constructed from vectors of vectors of QtMShapeData and a geometry type

    //build the result data vector
    vector<vector<QtMShapeData *>> resultDataVector;
    resultDataVector.reserve(geomType == QtGeometryData::GEOM_POLYGON ? 1 : operandList->size());

    vector<QtMShapeData *> intPolyRow;
    intPolyRow.reserve(operandList->size());

    for (auto *data : *operandList)
    {
        //in this case, we can recast to QtGeometryData and concatenate with our result
        if (data->getDataType() == QT_GEOMETRY)
        {
            vector<vector<QtMShapeData *>> ptData = (static_cast< QtGeometryData * >(data))->getData();
            //do not foresee this ever being of dims more than 1 x N, but just in case, let's do a concatenation here.
            for (auto &ptDataIter : ptData)
            {
                resultDataVector.push_back(std::move(ptDataIter));
            }
        }
        else if (geomType == QtGeometryData::GEOM_POLYGON && data->getDataType() == QT_MSHAPE)
        {
            //check dimensionality of the polygon
            auto *mshape = static_cast<QtMShapeData *>(data);
            mshape->computeDimensionality();
            intPolyRow.emplace_back(mshape);
        }
        //in this case, we can recast to the point data, and push it back
        else if (data->getDataType() == QT_MSHAPE)
        {
            vector<QtMShapeData *> ptData{static_cast< QtMShapeData * >(data)};
            resultDataVector.emplace_back(std::move(ptData));
        }
    }
    if (geomType == QtGeometryData::GEOM_POLYGON)
    {
        resultDataVector.emplace_back(std::move(intPolyRow));
    }
    //the result object
    returnValue = new QtGeometryData(resultDataVector, geomType, geomFlag);

    stopTimer();

    return returnValue;
}

void
QtGeometryOp::printTree(int tab, std::ostream &s, QtChildType mode)
{
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "QtGeometryOp Object " << static_cast<int>(getNodeType()) << getEvaluationTime() << std::endl;

    QtNaryOperation::printTree(tab, s, mode);
}

const QtTypeElement &
QtGeometryOp::checkType(QtTypeTuple *typeTuple)
{
    dataStreamType.setDataType(QT_TYPE_UNKNOWN);

//    QtOperationList::iterator iter;
    bool opTypesValid = true;
    for (auto iter = operationList->begin(); iter != operationList->end() && opTypesValid; iter++)
    {
        const QtTypeElement &type = (*iter)->checkType(typeTuple);
        // valid types: qt_mshapedata, qt_geometrydata

        // dimension must match between points
        if (type.getDataType() != QT_MSHAPE && type.getDataType() != QT_GEOMETRY)
        {
            opTypesValid = false;
            break;
        }
    }

    if (!opTypesValid)
    {
        LERROR << "Error: Operands must consist of geometric data only";
        parseInfo.setErrorNo(410);
        throw parseInfo;
    }

    dataStreamType.setDataType(QT_GEOMETRY);

    return dataStreamType;
}
