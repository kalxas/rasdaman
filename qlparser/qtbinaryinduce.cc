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

static const char rcsid[] = "@(#)qlparser, QtBinaryInduce: $Id: qtbinaryinduce.cc,v 1.47 2003/12/27 20:39:35 rasdev Exp $";

#include "config.h"
#include "debug.hh"

#include "qlparser/qtbinaryinduce.hh"
#include "qlparser/qtatomicdata.hh"
#include "qlparser/qtcomplexdata.hh"
#include "qlparser/qtconst.hh"
#include "qlparser/qtstringdata.hh"

#include "mddmgr/mddobj.hh"

#include "../common/src/logging/easylogging++.hh"

#include "catalogmgr/typefactory.hh"
#include "relcatalogif/mdddomaintype.hh"

#include "tilemgr/tile.hh"

#include <iostream>
#include <string>
#include <vector>
#include <memory>
using namespace std;

const QtNode::QtNodeType QtBinaryInduce::nodeType = QtNode::QT_BINARY_INDUCE;

QtBinaryInduce::QtBinaryInduce( QtOperation* initInput1, QtOperation* initInput2, Ops::OpType initOpType )
    :  QtBinaryOperation( initInput1, initInput2 ),
       opType( initOpType )
{
}



QtData*
QtBinaryInduce::computeOp( QtData* operand1, QtData* operand2 )
{
    QtData* returnValue = NULL;

    if     ( operand1->getDataType() == QT_MDD &&
             operand2->getDataType() == QT_MDD    )
    {
        QtMDD* mdd1 = static_cast<QtMDD*>(operand1);
        QtMDD* mdd2 = static_cast<QtMDD*>(operand2);
        MDDObj* op1 = mdd1->getMDDObject();
        MDDObj* op2 = mdd2->getMDDObject();
        const BaseType* resultBaseType = (static_cast<MDDBaseType*>(const_cast<Type*>(dataStreamType.getType())))->getBaseType();
        BinaryOp* myOp = Ops::getBinaryOp(opType, resultBaseType, op1->getCellType(), op2->getCellType());
        try {
            returnValue = computeBinaryMDDOp( mdd1, mdd2, resultBaseType, myOp );
        }
        catch (int errcode) {
            parseInfo.setErrorNo(static_cast<unsigned long>(errcode));
            throw parseInfo;
        }
        delete myOp;
    }
    else if( operand1->getDataType() == QT_MDD &&
             operand2->isScalarData()             )
    {
        QtMDD*        mdd    = static_cast<QtMDD*>(operand1);
        QtScalarData* scalar = static_cast<QtScalarData*>(operand2);

        const BaseType* resultBaseType = (static_cast<MDDBaseType*>(const_cast<Type*>(dataStreamType.getType())))->getBaseType();

        returnValue = computeUnaryMDDOp( mdd, scalar, resultBaseType, 2 );
    }
    else if( operand1->isScalarData() &&
             operand2->getDataType() == QT_MDD  )
    {
        QtMDD*        mdd    = static_cast<QtMDD*>(operand2);
        QtScalarData* scalar = static_cast<QtScalarData*>(operand1);

        const BaseType* resultBaseType = (static_cast<MDDBaseType*>(const_cast<Type*>(dataStreamType.getType())))->getBaseType();

        returnValue = computeUnaryMDDOp( mdd, scalar, resultBaseType, 1 );
    }
    else if( operand1->isScalarData() &&
             operand2->isScalarData()    )
    {
        QtScalarData* scalar1 = static_cast<QtScalarData*>(operand1);
        QtScalarData* scalar2 = static_cast<QtScalarData*>(operand2);

        BaseType* resultBaseType = static_cast<BaseType*>(const_cast<Type*>(dataStreamType.getType()));

        returnValue = computeBinaryOp( scalar1, scalar2, resultBaseType );
    }
    else if( operand1->getDataType() == QT_STRING && operand2->getDataType() == QT_STRING )
    {
        // opType == Ops::OP_EQUAL
        QtStringData* strObj1 = static_cast<QtStringData*>(operand1);
        QtStringData* strObj2 = static_cast<QtStringData*>(operand2);

        bool booleanResult = strObj1->getStringData() == strObj2->getStringData();

        returnValue = new QtAtomicData( booleanResult );
    }

    return returnValue;
}



QtData*
QtBinaryInduce::computeUnaryMDDOp( QtMDD* operand1, QtScalarData* operand2, const BaseType* resultBaseType, int scalarPos )
{
    QtData* returnValue = NULL;

    // get the MDD object
    MDDObj* op = operand1->getMDDObject();
    r_Minterval* nullValues = op->getNullValues();

    // create ULong type with QtIntData value
    const BaseType* constBaseType = operand2->getValueType();
    const char*     constValue    = operand2->getValueBuffer();

    //  get the area, where the operation has to be applied
    const r_Minterval &areaOp = operand1->getLoadDomain();

    // contains all tiles of the operand
    vector< boost::shared_ptr<Tile> >* allTiles;

    // iterator for tiles
    vector< boost::shared_ptr<Tile> >::iterator tileIt;

    // create MDDObj for result
    MDDDomainType* mddBaseType = new MDDDomainType( "tmp", resultBaseType, areaOp );
    TypeFactory::addTempType( mddBaseType );

    MDDObj* mddres = new MDDObj( mddBaseType, areaOp, op->getNullValues() );

    // get all tiles in relevant area
    allTiles = op->intersect(areaOp);
    tileIt = allTiles->begin();
    BinaryOp* myOp = NULL;
    if (scalarPos == 1)
    {
        myOp = (Ops::getBinaryOp(opType, resultBaseType, constBaseType, op->getCellType()));
        myOp->setNullValues(nullValues);
    }
    else
    {
        myOp = (Ops::getBinaryOp(opType, resultBaseType, op->getCellType(), constBaseType));
        myOp->setNullValues(nullValues);
    }
    // and iterate over them
    for( ; tileIt != allTiles->end(); tileIt++ )
    {
        // domain of the actual tile
        const r_Minterval &tileDom = (*tileIt)->getDomain();

        // domain of the relevant area of the actual tile
        r_Minterval intersectDom( tileDom.create_intersection( areaOp ) );

        // create tile for result
        Tile* resTile = new Tile( intersectDom, resultBaseType );

        //
        // carry out operation on the relevant area of the tiles
        //

#ifdef DEBUG
        char* typeStructure = resTile->getType()->getTypeStructure();
        LTRACE << "  result tile, area " << intersectDom <<
                     ", type " << resTile->getType()->getTypeName() <<
                     ", structure " << typeStructure;
        free( typeStructure ); typeStructure=NULL;
        typeStructure = (*tileIt)->getType()->getTypeStructure();
        LTRACE << "  operand1 tile, area " << intersectDom <<
                     ", type " << (*tileIt)->getType()->getTypeName() <<
                     ", structure " << typeStructure;
        free( typeStructure ); typeStructure=NULL;

        typeStructure = constBaseType->getTypeStructure();
        LTRACE << "  constant type " << constBaseType->getTypeName() <<
                     ", structure " << typeStructure <<
                     ", value ";
        free( typeStructure ); typeStructure=NULL;
        for( int x=0; x<constBaseType->getSize(); x++ )
            LTRACE << hex << (int)(constValue[x]);
#endif

        try
        {
            LTRACE << "  before execConstOp";

            resTile->execConstOp( myOp, intersectDom, tileIt->get(), intersectDom, constValue, scalarPos );
            LTRACE << "  after execConstOp";
        }
        catch (int errcode)
        {
            LFATAL << "Error: QtBinaryInduce::computeUnaryMDDOp() caught errno " << errcode;
            delete resTile;
            delete myOp;
            delete mddres;
            delete allTiles;
            parseInfo.setErrorNo(static_cast<unsigned long>(errcode));
            throw parseInfo;
        }

        // insert Tile in result tile
        mddres->insertTile( resTile );
    }
    // create a new QtMDD object as carrier object for the transient MDD object
    returnValue = new QtMDD( mddres );
    returnValue->cloneNullValues(myOp);

    delete myOp;
    myOp = NULL;

    // delete tile vector
    delete allTiles;
    allTiles=NULL;

    return returnValue;
}

QtData*
QtBinaryInduce::computeBinaryMDDOp( QtMDD* operand1, QtMDD* operand2, const BaseType* resultBaseType, BinaryOp* myOp )
{
    QtData* returnValue = NULL;
    // get the MDD objects
    MDDObj* op1 = operand1->getMDDObject();
    MDDObj* op2 = operand2->getMDDObject();
    r_Minterval* nullValues1 = op1->getNullValues();
    r_Minterval* nullValues2 = op2->getNullValues();
    //  get the areas, where the operation has to be applied
    const r_Minterval &areaOp1 = operand1->getLoadDomain();
    const r_Minterval &areaOp2 = operand2->getLoadDomain();

    // Check, if the domains are compatible which means that they have the same
    // dimensionality and each dimension has the same number of elements.
    if( areaOp1.get_extent() == areaOp2.get_extent() )
    {
        // contains all tiles of op1
        vector< boost::shared_ptr<Tile> >* allTilesOp1;

        // contains all tiles of op2 which intersect a given op1 Tile in the relevant area.
        vector< boost::shared_ptr<Tile> >* intersectTilesOp2=NULL;

        // iterators for tiles of the MDDs
        vector< boost::shared_ptr<Tile> >::iterator tileOp1It;
        vector< boost::shared_ptr<Tile> >::iterator intersectTileOp2It;

        // intersection of domains in relevant area.
        r_Minterval intersectDom;

        // pointer to generated result tile
        Tile* resTile=NULL;

        // MDDObj for result
        MDDObj* mddres=NULL;
        // translations between the two areas
        r_Point offset12(areaOp1.dimension());
        r_Point offset21(areaOp1.dimension());

        // calculate translations
        r_Point originOp1 = areaOp1.get_origin();
        r_Point originOp2 = areaOp2.get_origin();
        for(r_Dimension i = 0; i<areaOp1.dimension(); i++)
        {
            offset12[i] = originOp2[i] - originOp1[i];
            offset21[i] = originOp1[i] - originOp2[i];
        }

        LTRACE << "  Domain op1 " << areaOp1 << " op2 " << areaOp2;
        LTRACE << "  Translation vector " << offset12;

        // create MDDObj for result
        MDDDomainType* mddBaseType = new MDDDomainType( "tmp", resultBaseType, areaOp1 );
        TypeFactory::addTempType( mddBaseType );

        mddres = new MDDObj( mddBaseType, areaOp1, op1->getNullValues() ); // FIXME consider op2 too
        // get all tiles in relevant area of MDD op1
        allTilesOp1 = op1->intersect(areaOp1);

        // and iterate over them

        //auto_ptr<BinaryOp> myOp(Ops::getBinaryOp(opType, mddBaseType->getBaseType(), op1->getCellType(), op2->getCellType()));
        myOp->setNullValues(nullValues1);
        for( tileOp1It = allTilesOp1->begin(); tileOp1It !=  allTilesOp1->end(); tileOp1It++ )
        {
            // domain of the op1 tile
            const r_Minterval &tileOp1Dom = (*tileOp1It)->getDomain();

            // relevant area of op1's domain
            r_Minterval intersectionTileOp1Dom( tileOp1Dom.create_intersection( areaOp1 ) );

            // intersect relevant area of the tile with MDD op2 (including translation)
            intersectTilesOp2 = op2->intersect(intersectionTileOp1Dom.create_translation(offset12));

            // cout << "INTERSECT" << tileOp1Dom.create_translation(offset12) << endl;
            //    for( intersectTileOp2It = intersectTilesOp2->begin();
            //         intersectTileOp2It !=  intersectTilesOp2->end();
            //         intersectTileOp2It++ )
            //      cout << (*intersectTileOp2It)->getDomain() << endl;

            // iterate over intersecting tiles
            for( intersectTileOp2It  = intersectTilesOp2->begin();
                    intersectTileOp2It != intersectTilesOp2->end();
                    intersectTileOp2It++ )
            {
                const r_Minterval &tileOp2Dom = (*intersectTileOp2It)->getDomain();

                // the relevant domain is the intersection of the
                // domains of the two tiles with the relevant area.
                intersectDom = tileOp1Dom.create_intersection(tileOp2Dom.create_translation(offset21));

                intersectDom.intersection_with(areaOp1);

                // create tile for result
                resTile = new Tile( intersectDom, resultBaseType );

                //
                // carry out operation on the relevant area of the tiles
                //
                try
                {
                    LTRACE << "  before execBinaryOp";
                    LTRACE << "  result tile, area " << intersectDom <<
                              ", type " << resTile->getType()->getTypeName();
                    LTRACE << "  operand1 tile, area " << intersectDom <<
                              ", type " << (*tileOp1It)->getType()->getTypeName();
                    LTRACE << "  operand2 tile, type " << (*tileOp1It)->getType()->getTypeName();
                    resTile->execBinaryOp(&(*myOp), intersectDom, tileOp1It->get(), intersectDom, intersectTileOp2It->get(), intersectDom.create_translation(offset12));
                    LTRACE << "  after execBinaryOp";
                }
                catch (int errcode)
                {
                    LFATAL << "Error: QtBinaryInduce::computeBinaryMDDOp() caught errno " << errcode;
                    delete myOp;
                    delete resTile;
                    delete mddres;
                    delete intersectTilesOp2;
                    delete allTilesOp1;
                    throw errcode;
                }

                // insert Tile in result mddobj
                mddres->insertTile( resTile );
            }
            delete intersectTilesOp2;
            intersectTilesOp2=NULL;
        }
        delete allTilesOp1;
        allTilesOp1=NULL;

        // create a new QtMDD object as carrier object for the transient MDD object
        returnValue = new QtMDD(mddres);
        returnValue->cloneNullValues(myOp);
    }
    else
    {
        LERROR << "Error: QtBinaryInduce::computeBinaryMDDOp() - domains of the operands are incompatible.";
        LERROR << "areaOp1 " << areaOp1 << " with extent " << areaOp1.get_extent();
        LERROR << "areaOp2 " << areaOp2 << " with extent " << areaOp2.get_extent();

        throw 351;
    }

    // The following is now done, when the last reference is deleted.
    // delete obsolete MDD objects
    //  delete op1;
    //  delete op2;

    return returnValue;
}



QtData*
QtBinaryInduce::computeBinaryOp( QtScalarData* operand1, QtScalarData* operand2, const BaseType* resultBaseType )
{
    QtScalarData* scalarDataObj = NULL;
    r_Minterval* nullValues = operand1->getNullValues();  // FIXME use also operand2

    // allocate memory for the result
    char* resultBuffer = new char[ resultBaseType->getSize() ];

    BinaryOp* myOp = Ops::getBinaryOp( opType, resultBaseType,
                                       operand1->getValueType(),   operand2->getValueType() );
    myOp->setNullValues(nullValues);
    try
    {
        (*myOp)(resultBuffer, operand1->getValueBuffer(), operand2->getValueBuffer() );
    }
    catch (int errcode)
    {
        LFATAL << "Error: QtBinaryInduce::computeBinaryOp() caught errno " << errcode;
        delete myOp;
        delete[] resultBuffer;
        parseInfo.setErrorNo(static_cast<unsigned long>(errcode));
        throw parseInfo;
    }

    if( resultBaseType->getType() == STRUCT )
        scalarDataObj = new QtComplexData();
    else
        scalarDataObj = new QtAtomicData();

    scalarDataObj->setValueType  ( resultBaseType );
    scalarDataObj->setValueBuffer( resultBuffer );
    scalarDataObj->cloneNullValues(myOp);

    delete myOp;

    return scalarDataObj;
}



QtData*
QtBinaryInduce::evaluate( QtDataList* inputList )
{
    startTimer("QtBinaryInduce");
    QtData* returnValue = NULL;
    QtData* operand1 = NULL;
    QtData* operand2 = NULL;

    if( getOperands( inputList, operand1, operand2 ) )
    {
        returnValue = computeOp( operand1, operand2 );

        // delete the old operands
        if( operand1 ) operand1->deleteRef();
        if( operand2 ) operand2->deleteRef();
    }
    stopTimer();
    return returnValue;
}



const QtTypeElement&
QtBinaryInduce::checkType( QtTypeTuple* typeTuple )
{
    dataStreamType.setDataType( QT_TYPE_UNKNOWN );

    // check operand branches
    if( input1 && input2 )
    {

        // get input types
        const QtTypeElement& inputType1 = input1->checkType( typeTuple );
        const QtTypeElement& inputType2 = input2->checkType( typeTuple );

#ifdef DEBUG
        LTRACE << "Operand 1: ";
        inputType1.printStatus( RMInit::dbgOut );
        LTRACE << "Operand 2: ";
        inputType2.printStatus( RMInit::dbgOut );
        LTRACE << "Operation            " << opType;
#endif
        if( inputType1.getDataType() == QT_MDD &&
                inputType2.getDataType() == QT_MDD    )
        {
            const BaseType* baseType1 = (static_cast<MDDBaseType*>(const_cast<Type*>(inputType1.getType())))->getBaseType();
            const BaseType* baseType2 = (static_cast<MDDBaseType*>(const_cast<Type*>(inputType2.getType())))->getBaseType();

            const BaseType* resultBaseType = Ops::getResultType( opType, baseType1, baseType2 );

            if( !resultBaseType )
            {
                LFATAL << "Error: QtBinaryInduce::checkType() - binary induce (MDD + MDD): operand types are incompatible.";
                parseInfo.setErrorNo(363);
                throw parseInfo;
            }

            MDDBaseType* resultMDDType = new MDDBaseType( "tmp", resultBaseType );
            TypeFactory::addTempType( resultMDDType );

            dataStreamType.setType( resultMDDType );
        }
        else if( inputType1.getDataType() == QT_MDD &&
                 inputType2.isBaseType() )
        {
            const BaseType* baseType1 = (static_cast<MDDBaseType*>(const_cast<Type*>(inputType1.getType())))->getBaseType();
            BaseType* baseType2 = static_cast<BaseType*>(const_cast<Type*>(inputType2.getType()));

            const BaseType* resultBaseType = Ops::getResultType( opType, baseType1, baseType2 );

            if( !resultBaseType )
            {
                LFATAL << "Error: QtBinaryInduce::checkType() - unary induce (MDD + BaseType): operand types are incompatible.";
                parseInfo.setErrorNo(364);
                throw parseInfo;
            }

            MDDBaseType* resultMDDType = new MDDBaseType( "tmp", resultBaseType );
            TypeFactory::addTempType( resultMDDType );

            dataStreamType.setType( resultMDDType );
        }
        else if( inputType1.isBaseType() &&
                 inputType2.getDataType() == QT_MDD )
        {
            BaseType* baseType1 = static_cast<BaseType*>(const_cast<Type*>(inputType1.getType()));
            const BaseType* baseType2 = (static_cast<MDDBaseType*>(const_cast<Type*>(inputType2.getType())))->getBaseType();

            const BaseType* resultBaseType = Ops::getResultType( opType, baseType1, baseType2 );

            if( !resultBaseType )
            {
                LFATAL << "Error: QtBinaryInduce::checkType() - unary induce (BaseType + MDD): operand types are incompatible.";
                parseInfo.setErrorNo(364);
                throw parseInfo;
            }

            MDDBaseType* resultMDDType = new MDDBaseType( "tmp", resultBaseType );
            TypeFactory::addTempType( resultMDDType );

            dataStreamType.setType( resultMDDType );
        }
        else if( inputType1.isBaseType() &&
                 inputType2.isBaseType() )
        {
            BaseType* baseType1 = static_cast<BaseType*>(const_cast<Type*>(inputType1.getType()));
            BaseType* baseType2 = static_cast<BaseType*>(const_cast<Type*>(inputType2.getType()));

            const BaseType* resultBaseType = Ops::getResultType( opType, baseType1, baseType2 );

            if( !resultBaseType )
            {
                LFATAL << "Error: QtBinaryInduce::checkType() - BaseType + BaseType : operand types are incompatible.";

                parseInfo.setErrorNo(365);
                throw parseInfo;
            }

            dataStreamType.setType( resultBaseType );
        }
        else if( inputType1.getDataType() == QT_STRING &&
                 inputType2.getDataType() == QT_STRING    )
        {
            if( opType != Ops::OP_EQUAL )
            {
                LFATAL << "Error: QtBinaryInduce::checkType() - String + String : operation is not supported on strings.";
                parseInfo.setErrorNo(385);
                throw parseInfo;
            }

            dataStreamType.setDataType( QT_BOOL );
        }
        else
        {
            LFATAL << "Error: QtBinaryInduce::checkType() - operation is not supported on these data types.";
            parseInfo.setErrorNo(403);
            throw parseInfo;
        }
    }
    else
        LERROR << "Error: QtBinaryInduce::checkType() - operand branch invalid.";

    return dataStreamType;
}



const QtNode::QtNodeType QtPlus::nodeType = QT_PLUS;

QtPlus::QtPlus( QtOperation* initInput1, QtOperation* initInput2 )
    : QtBinaryInduce( initInput1, initInput2, Ops::OP_PLUS )
{
}



QtOperation*
QtPlus::getUniqueOrder( const QtNode::QtNodeType ID )
{
    QtOperation* returnValue = NULL;

    if( nodeType == ID )
    {
        QtOperation* node = input1->getUniqueOrder( nodeType );

        if( node )
        {
            if( ( node->getSpelling().compare( input2->getSpelling() ) ) > 0 )
                returnValue = node;
            else
                returnValue = input2;
        }
        else
            LERROR << "Error: QtMult::getUniqueOrder(): Query tree invalid";
    }
    else
        returnValue = this;

    return returnValue;
}



void
QtPlus::printTree( int tab, ostream& s, QtChildType mode )
{
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "QtPlus Object " << static_cast<int>(getNodeType()) << getEvaluationTime() << endl;

    QtBinaryInduce::printTree( tab, s, mode );
}



void
QtPlus::printAlgebraicExpression( ostream& s )
{
    s << "(";

    if( input1 )
        input1->printAlgebraicExpression( s );
    else
        s << "<nn>";

    s << " + ";

    if( input2 )
        input2->printAlgebraicExpression( s );
    else
        s << "<nn>";

    s << ")";
}



const QtNode::QtNodeType QtMinus::nodeType = QT_MINUS;

QtMinus::QtMinus( QtOperation* initInput1, QtOperation* initInput2 )
    :  QtBinaryInduce( initInput1, initInput2, Ops::OP_MINUS )
{
}



bool
QtMinus::isCommutative() const
{
    return false; // NOT commutative
}



void
QtMinus::printTree( int tab, ostream& s, QtChildType mode )
{
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "QtMinus Object " << static_cast<int>(getNodeType()) << getEvaluationTime() << endl;

    QtBinaryInduce::printTree( tab, s, mode );
}



void
QtMinus::printAlgebraicExpression( ostream& s )
{
    s << "(";

    if( input1 )
        input1->printAlgebraicExpression( s );
    else
        s << "<nn>";

    s << " - ";

    if( input2 )
        input2->printAlgebraicExpression( s );
    else
        s << "<nn>";

    s << ")";
}


const QtNode::QtNodeType QtMax_binary::nodeType = QT_MAX_BINARY;

QtMax_binary::QtMax_binary( QtOperation* initInput1, QtOperation* initInput2 )
    : QtBinaryInduce( initInput1, initInput2, Ops::OP_MAX_BINARY )
{
}




void
QtMax_binary::printTree( int tab, ostream& s, QtChildType mode )
{
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "QtMax_binary Object " << static_cast<int>(getNodeType()) << getEvaluationTime() << endl;

    QtBinaryInduce::printTree( tab, s, mode );
}



void
QtMax_binary::printAlgebraicExpression( ostream& s )
{
    s << "(";

    if( input1 )
        input1->printAlgebraicExpression( s );
    else
        s << "<nn>";

    s << " binary max ";

    if( input2 )
        input2->printAlgebraicExpression( s );
    else
        s << "<nn>";

    s << ")";
}


const QtNode::QtNodeType QtMin_binary::nodeType = QT_MIN_BINARY;

QtMin_binary::QtMin_binary( QtOperation* initInput1, QtOperation* initInput2 )
    : QtBinaryInduce( initInput1, initInput2, Ops::OP_MIN_BINARY )
{
}




void
QtMin_binary::printTree( int tab, ostream& s, QtChildType mode )
{
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "QtMin_binary Object " << static_cast<int>(getNodeType()) << getEvaluationTime() << endl;

    QtBinaryInduce::printTree( tab, s, mode );
}



void
QtMin_binary::printAlgebraicExpression( ostream& s )
{
    s << "(";

    if( input1 )
        input1->printAlgebraicExpression( s );
    else
        s << "<nn>";

    s << " binary min ";

    if( input2 )
        input2->printAlgebraicExpression( s );
    else
        s << "<nn>";

    s << ")";
}



const QtNode::QtNodeType QtMult::nodeType = QT_MULT;

QtMult::QtMult( QtOperation* initInput1, QtOperation* initInput2 )
    :  QtBinaryInduce( initInput1, initInput2, Ops::OP_MULT )
{
}



QtOperation*
QtMult::getUniqueOrder( const QtNode::QtNodeType ID )
{
    QtOperation* returnValue = NULL;

    if( nodeType == ID )
    {
        QtOperation* node = input1->getUniqueOrder( nodeType );

        if( node )
        {
            if( node->getSpelling().compare( input2->getSpelling() ) > 0 )
                returnValue = node;
            else
                returnValue = input2;
        }
        else
            LERROR << "Error: QtMult::getUniqueOrder(): Query tree invalid";
    }
    else
        returnValue = this;

    return returnValue;
}



void
QtMult::printTree( int tab, ostream& s, QtChildType mode )
{
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "QtMult Object " << static_cast<int>(getNodeType()) << getEvaluationTime() << endl;

    QtBinaryInduce::printTree( tab, s, mode );
}



void
QtMult::printAlgebraicExpression( ostream& s )
{
    s << "(";

    if( input1 )
        input1->printAlgebraicExpression( s );
    else
        s << "<nn>";

    s << " * ";

    if( input2 )
        input2->printAlgebraicExpression( s );
    else
        s << "<nn>";

    s << ")";
}



const QtNode::QtNodeType QtDiv::nodeType = QT_DIV;

QtDiv::QtDiv( QtOperation* initInput1, QtOperation* initInput2 )
    :  QtBinaryInduce( initInput1, initInput2, Ops::OP_DIV )
{
}



bool
QtDiv::isCommutative() const
{
    return false; // NOT commutative
}



void
QtDiv::printTree( int tab, ostream& s, QtChildType mode )
{
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "QtDiv Object " << static_cast<int>(getNodeType()) << getEvaluationTime() << endl;

    QtBinaryInduce::printTree( tab, s, mode );
}



void
QtDiv::printAlgebraicExpression( ostream& s )
{
    s << "(";

    if( input1 )
        input1->printAlgebraicExpression( s );
    else
        s << "<nn>";

    s << " / ";

    if( input2 )
        input2->printAlgebraicExpression( s );
    else
        s << "<nn>";

    s << ")";
}

const QtNode::QtNodeType QtIntDiv::nodeType = QT_INTDIV;

QtIntDiv::QtIntDiv( QtOperation* initInput1, QtOperation* initInput2 )
    :  QtBinaryInduce( initInput1, initInput2, Ops::OP_INTDIV )
{
}



bool
QtIntDiv::isCommutative() const
{
    return false; // NOT commutative
}



void
QtIntDiv::printTree( int tab, ostream& s, QtChildType mode )
{
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "QtIntDiv Object " << static_cast<int>(getNodeType()) << getEvaluationTime() << endl;

    QtBinaryInduce::printTree( tab, s, mode );
}



void
QtIntDiv::printAlgebraicExpression( ostream& s )
{
    s << "(";

    if( input1 )
        input1->printAlgebraicExpression( s );
    else
        s << "<nn>";

    s << " div ";

    if( input2 )
        input2->printAlgebraicExpression( s );
    else
        s << "<nn>";

    s << ")";
}
const QtNode::QtNodeType QtMod::nodeType = QT_MOD;

QtMod::QtMod( QtOperation* initInput1, QtOperation* initInput2 )
    :  QtBinaryInduce( initInput1, initInput2, Ops::OP_MOD )
{
}



bool
QtMod::isCommutative() const
{
    return false; // NOT commutative
}



void
QtMod::printTree( int tab, ostream& s, QtChildType mode )
{
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "QtMod Object " << static_cast<int>(getNodeType()) << getEvaluationTime() << endl;

    QtBinaryInduce::printTree( tab, s, mode );
}



void
QtMod::printAlgebraicExpression( ostream& s )
{
    s << "(";

    if( input1 )
        input1->printAlgebraicExpression( s );
    else
        s << "<nn>";

    s << " mod ";

    if( input2 )
        input2->printAlgebraicExpression( s );
    else
        s << "<nn>";

    s << ")";
}
