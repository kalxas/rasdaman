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

QtGeometryOp::QtGeometryOp(QtOperationList* vectorListsArg, QtGeometryData::QtGeometryType geomTypeArg)
    : QtNaryOperation(vectorListsArg), geomType(geomTypeArg)
{
}

QtData* 
QtGeometryOp::evaluate(QtDataList* inputList)
{
    startTimer("QtGeometryOp");

    QtData* returnValue = NULL;
    QtDataList *operandList = NULL;

    if (getOperands(inputList, operandList))
    {
        if (operandList)
        {
            //we want to create a QtGeometryData object (derived class from QtData)
            //geometry data objects are constructed from vectors of vectors of QtMShapeData and a geometry type
            
            //build the result data vector
            vector< vector< QtMShapeData* > > resultDataVector;
            if(geomType == QtGeometryData::GEOM_POLYGON)
            {
                resultDataVector.reserve(1);
            }
            else
            {
                resultDataVector.reserve(operandList->size());                
            }
            vector< QtMShapeData* > intPolyRow;
            intPolyRow.reserve(operandList->size());
            for (auto dataIter = operandList->begin(); dataIter != operandList->end(); dataIter++)
            {
                //in this case, we can recast to QtGeometryData and concatenate with our result
                if((*dataIter)->getDataType() == QT_GEOMETRY)
                {
                    vector< vector< QtMShapeData* > > ptData = (dynamic_cast< QtGeometryData* >(*dataIter))->getData();
                    //do not foresee this ever being of dims more than 1 x N, but just in case, let's do a concatenation here.
                    for(auto ptDataIter = ptData.begin(); ptDataIter != ptData.end(); ++ptDataIter)
                    {
                        //each row is a positive genus polygon
                        vector< QtMShapeData* > rowOfData;
                        rowOfData.reserve(ptData.size());
                        for(auto it = ptDataIter->begin(); it != ptDataIter->end(); ++it)
                        {
                            rowOfData.emplace_back(*it);
                        }
                        resultDataVector.emplace_back(rowOfData);
                    }
                }
                else if(geomType == QtGeometryData::GEOM_POLYGON && (*dataIter)->getDataType() == QT_MSHAPE)
                {
                    //check dimensionality of the polygon
                    static_cast< QtMShapeData* >(*dataIter)->computeDimensionality();
                    if(static_cast< QtMShapeData* >(*dataIter)->getDimension() < 2)
                    {
                        LERROR << "Error: The polygon is degenerate in the sense that its vertices are colinear.";
                        throw r_Error(INCORRECTPOLYGON);
                    }
                    
                    intPolyRow.emplace_back(static_cast< QtMShapeData* >(*dataIter));
                }
                //in this case, we can recast to the point data, and push it back
                else if((*dataIter)->getDataType() == QT_MSHAPE)
                {                    
                    vector< QtMShapeData* > ptData;
                    ptData.reserve(1);
                    ptData.emplace_back(static_cast< QtMShapeData* >(*dataIter));
                    resultDataVector.push_back(ptData);
                }
            }
            if(geomType == QtGeometryData::GEOM_POLYGON)
            {
                resultDataVector.emplace_back(intPolyRow);
            }
            //the result object
            returnValue = new QtGeometryData(resultDataVector, geomType);
            
        }
    }

    stopTimer();

    return returnValue;
}

void 
QtGeometryOp::printTree(int tab, std::ostream &s, QtChildType mode)
{
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "QtGeometryOp Object " << static_cast<int>(getNodeType()) << getEvaluationTime() << std::endl;

    QtNaryOperation::printTree(tab, s, mode);
}

const QtTypeElement& 
QtGeometryOp::checkType(QtTypeTuple* typeTuple)
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