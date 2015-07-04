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

static const char rcsid[] = "@(#)qlparser, QtUnaryInduce: $Id: qtunaryinduce.cc,v 1.47 2002/08/19 11:13:27 coman Exp $";

#include "config.h"
#include "raslib/rmdebug.hh"

#include "qlparser/qtunaryinduce.hh"
#include "qlparser/qtmdd.hh"
#include "qlparser/qtatomicdata.hh"
#include "qlparser/qtcomplexdata.hh"
#include "qlparser/qtnode.hh"

#include "mddmgr/mddobj.hh"
#include "tilemgr/tile.hh"

#include "catalogmgr/typefactory.hh"
#include "relcatalogif/structtype.hh"
#include "relcatalogif/mdddimensiontype.hh"
#include "relcatalogif/syntaxtypes.hh"

#include <sstream>
#ifndef CPPSTDLIB
#include <ospace/string.h> // STL<ToolKit>
#else
#include <string>
using namespace std;
#endif

const QtNode::QtNodeType QtUnaryInduce::nodeType = QtNode::QT_UNARY_INDUCE;

QtUnaryInduce::QtUnaryInduce( QtOperation* initInput )
    :  QtUnaryOperation( initInput )
{
}



bool
QtUnaryInduce::getOperand( QtDataList* inputList, QtData* &operand )
{
    bool success = false;

    // get the operands
    operand = input->evaluate( inputList );

    // Test, if the operands are valid.
    if( operand )
        success = true;
    else
    {
        RMDBGMIDDLE( 1, RMDebug::module_qlparser, "QtUnaryInduce", "Information: QtUnaryInduce::getOperand() - operand is not provided." )
    }

    return success;
}



QtData*
QtUnaryInduce::computeOp( QtData* operand, Ops::OpType operation, double param )
{
    QtData* returnValue = NULL;

    if( operand->getDataType() == QT_MDD )
    {
        QtMDD* mdd = static_cast<QtMDD*>(operand);

        const BaseType* resultCellType = const_cast<BaseType*>(Ops::getResultType( operation, mdd->getCellType() ));

        returnValue = computeUnaryMDDOp( mdd, resultCellType, operation, 0, param);
        
        (static_cast<QtMDD*>(returnValue))->setFromConversion(mdd->isFromConversion());
    }
    else if( operand->isScalarData() )
    {
        QtScalarData* scalar = static_cast<QtScalarData*>(operand);

        const BaseType* resultCellType = const_cast<BaseType*>(Ops::getResultType( operation, scalar->getValueType() ));

        returnValue = computeUnaryOp( scalar, resultCellType, operation, 0, param );
    }

    return returnValue;
}



QtData*
QtUnaryInduce::computeUnaryMDDOp( QtMDD* operand, const BaseType* resultBaseType,
                                  Ops::OpType operation, unsigned int operandOffset, double param )
{
    RMDBCLASS( "QtUnaryInduce", "computeUnaryMDDOp( QtMDD*, BaseType*, Ops::OpType, unsigned int ) ", "qlparser", __FILE__, __LINE__ )

    QtData* returnValue = NULL;

    // get the MDD object
    MDDObj* op = (static_cast<QtMDD*>(operand))->getMDDObject();
    r_Minterval* nullValues = op->getNullValues();

    //  get the area, where the operation has to be applied
    const r_Minterval &areaOp = (static_cast<QtMDD*>(operand))->getLoadDomain();

    const r_Dimension dim = areaOp.dimension();

    // iterator for tiles

    // create MDDObj for result
  // this should rather be MDDDomainType? -- DM 2011-aug-12
    //Old implementation was :MDDBaseType* mddBaseType = new MDDBaseType( "tmp", resultBaseType );
    //Had type incompatibility issue because of missing dimension.
    MDDDimensionType* mddDimensionType = new MDDDimensionType("tmp", resultBaseType, dim);

    MDDBaseType* mddBaseType = static_cast<MDDBaseType*>(mddDimensionType);

    TypeFactory::addTempType( mddBaseType );

    MDDObj* mddres = new MDDObj( mddBaseType, areaOp, op->getNullValues() );

    // get all tiles in relevant area
    vector< boost::shared_ptr<Tile> >* allTiles = op->intersect(areaOp);
    std::vector< boost::shared_ptr<Tile> >::iterator tileIt = allTiles->begin();
    UnaryOp* myOp = NULL;

    if (operation == Ops::OP_IDENTITY)
    {
        myOp = Ops::getUnaryOp(operation, resultBaseType, resultBaseType, 0, operandOffset);
        myOp->setNullValues(nullValues);
    }
    else
    {
        myOp = Ops::getUnaryOp(operation, resultBaseType, op->getCellType(), 0, 0);
        myOp->setNullValues(nullValues);
    }
    if (myOp == NULL)
    {
            RMInit::logOut << "QtUnaryInduce::computeUnaryMDDOp(...) could not get operation for result type " << resultBaseType->getName() << " argument type " << (*tileIt)->getType() << " operation " << static_cast<int>(operation) << endl;
        delete allTiles;
        allTiles = NULL;
        //contents of allTiles are deleted when index is deleted
        delete mddres;
        mddres = NULL;
        delete mddres;
        mddres = NULL;
        // i am not sure about that error number...
        parseInfo.setErrorNo(366);
        throw parseInfo;
    }

    if (tileIt != allTiles->end())
    {
        Tile* resTile = NULL;
        // set exponent for pow operations
        if (operation == Ops::OP_POW)
        {
            (static_cast<OpPOWCDouble*>(myOp))->setExponent(param);
        }
        
        // and iterate over them
        try
        {
            for( ; tileIt !=  allTiles->end(); tileIt++ )
            {
                // domain of the actual tile
                const r_Minterval &tileDom = (*tileIt)->getDomain();

                // domain of the relevant area of the actual tile
                r_Minterval intersectDom( tileDom.create_intersection( areaOp ) );

                // create tile for result
                resTile = new Tile( intersectDom, resultBaseType );

                // carry out operation on the relevant area of the tiles
                resTile->execUnaryOp(myOp, intersectDom, tileIt->get(), intersectDom);
                // insert Tile in result mdd
                mddres->insertTile( resTile );
            }
        }
        catch(r_Error& err)
        {
            RMInit::logOut << "QtUnaryInduce::computeUnaryMDDOp caught " << err.get_errorno() << " " << err.what() << endl;
            delete allTiles;
            allTiles = NULL;
            //contents of allTiles are deleted when index is deleted
            delete mddres;
            mddres = NULL;
            delete resTile;
            resTile = NULL;
            delete mddres;
            mddres = NULL;
            parseInfo.setErrorNo(err.get_errorno());
            throw parseInfo;
        }
        catch (int err)
        {
            RMInit::logOut << "QtUnaryInduce::computeUnaryMDDOp caught errno error (" << err << ") in unaryinduce" << endl;
            delete allTiles;
            allTiles = NULL;
            //contents of allTiles are deleted when index is deleted
            delete mddres;
            mddres = NULL;
            delete resTile;
            resTile = NULL;
            delete mddres;
            mddres = NULL;
            parseInfo.setErrorNo(static_cast<unsigned int>(err));
            throw parseInfo;
        }

    }
    // delete tile vector
    delete allTiles;
    allTiles = NULL;

    // create a new QtMDD object as carrier object for the transient MDD object
    returnValue = new QtMDD( mddres );
    if (myOp != NULL)
    {
        returnValue->cloneNullValues(myOp);
    }

    delete myOp;
    myOp = NULL;
    // The following is now done when deleting the last reference to the operand
    // delete the obsolete MDD object
    //  delete op;

    return returnValue;
}


QtData*
QtUnaryInduce::computeUnaryOp( QtScalarData* operand, const BaseType* resultBaseType,
                               Ops::OpType operation, unsigned int operandOffset, double param )
{
    RMDBCLASS( "QtUnaryInduce", "computeUnaryOp( QtScalarData*, BaseType*, Ops::OpType, unsigned int ) ", "qlparser", __FILE__, __LINE__ )

    QtScalarData* scalarDataObj = NULL;
    r_Minterval* nullValues = operand->getNullValues();

    // allocate memory for the result
    char* resultBuffer = new char[ resultBaseType->getSize() ];

    RMDBGIF( 4, RMDebug::module_qlparser, "QtUnaryInduce", \
             RMInit::dbgOut << "Operand value "; \
             operand->getValueType()->printCell( RMInit::dbgOut, operand->getValueBuffer() ); \
             RMInit::dbgOut << endl; \
           )

    UnaryOp* myOp = NULL;

    try
    {
        if(( operation == Ops::OP_IDENTITY ))  // || ( operation == Ops::OP_SQRT ))
        {
            // operand type is the same as result type
            myOp = Ops::getUnaryOp( operation, resultBaseType, resultBaseType, 0, operandOffset );
            myOp->setNullValues(nullValues);
			// set exponent for pow operations
			if (operation == Ops::OP_POW)
			{
				(static_cast<OpPOWCDouble*>(myOp))->setExponent(param);
			}

            (*myOp)(resultBuffer,operand->getValueBuffer());
        }
        else
            try
            {
                myOp = Ops::getUnaryOp( operation, resultBaseType, operand->getValueType(), 0, operandOffset );
                myOp->setNullValues(nullValues);
				// set exponent for pow operations
				if (operation == Ops::OP_POW)
				{
					(static_cast<OpPOWCDouble*>(myOp))->setExponent(param);
				}
                (*myOp)(resultBuffer,operand->getValueBuffer());
            }
            catch(int err)
            {
                delete[] resultBuffer;
                resultBuffer = NULL;
                parseInfo.setErrorNo(static_cast<unsigned int>(err));
                throw parseInfo;
            }
    }
    catch(...)
    {
        delete myOp;  // cleanup
        throw;
    }



    RMDBGIF( 4, RMDebug::module_qlparser, "QtUnaryInduce", \
             RMInit::dbgOut << "Result value "; \
             resultBaseType->printCell( RMInit::dbgOut, resultBuffer ); \
             RMInit::dbgOut << endl; \
           )

    if( resultBaseType->getType() == STRUCT )
        scalarDataObj = new QtComplexData();
    else
        scalarDataObj = new QtAtomicData();

    scalarDataObj->setValueType  ( resultBaseType );
    scalarDataObj->setValueBuffer( resultBuffer );
    if (myOp != NULL)
    {
        scalarDataObj->cloneNullValues(myOp);
    }

    delete myOp;

    return scalarDataObj;
}


const QtNode::QtNodeType QtNot::nodeType = QtNode::QT_NOT;

QtNot::QtNot( QtOperation* initInput )
    :  QtUnaryInduce( initInput )
{
}


QtData*
QtNot::evaluate( QtDataList* inputList )
{
    startTimer("QtNot");
    QtData* returnValue = NULL;
    QtData* operand = NULL;

    if( getOperand( inputList, operand ) )
    {
        returnValue = computeOp( operand, Ops::OP_NOT );

        // delete old operand
        if( operand ) operand->deleteRef();
    }
    
    stopTimer();
    
    return returnValue;
}



void
QtNot::printTree( int tab, ostream& s, QtChildType mode )
{
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "QtNot Object" << getEvaluationTime() << endl;

    QtUnaryInduce::printTree( tab+2, s, mode );
}



void
QtNot::printAlgebraicExpression( ostream& s )
{
    s << "not(";

    if( input )
        input->printAlgebraicExpression( s );
    else
        s << "<nn>";

    s << ")";
}



const QtTypeElement&
QtNot::checkType( QtTypeTuple* typeTuple )
{
    RMDBCLASS( "QtNot", "checkType( QtTypeTuple* )", "qlparser", __FILE__, __LINE__ )

    dataStreamType.setDataType( QT_TYPE_UNKNOWN );

    // check operand branches
    if( input )
    {

        // get input types
        const QtTypeElement& inputType = input->checkType( typeTuple );

        RMDBGIF( 4, RMDebug::module_qlparser, "QtNot", \
                 RMInit::dbgOut << "Operand: " << flush; \
                 inputType.printStatus( RMInit::dbgOut ); \
                 RMInit::dbgOut << endl; \
               )

        if( inputType.getDataType() == QT_MDD )
        {
            const BaseType* baseType = (static_cast<const MDDBaseType*>(inputType.getType()))->getBaseType();

            const BaseType* resultBaseType = const_cast<BaseType*>(Ops::getResultType( Ops::OP_NOT, baseType ));

            if( !resultBaseType )
            {
                RMInit::logOut << "Error: QtNot::checkType() - induce operand type is not supported." << endl;
                parseInfo.setErrorNo(366);
                throw parseInfo;
            }

            MDDBaseType* resultMDDType = new MDDBaseType( "tmp", resultBaseType );
            TypeFactory::addTempType( resultMDDType );

            dataStreamType.setType( resultMDDType );
        }
        else if( inputType.isBaseType() )
        {
            BaseType* baseType = static_cast<BaseType*>(const_cast<Type*>(inputType.getType()));

            const BaseType* resultBaseType = const_cast<BaseType*>(Ops::getResultType( Ops::OP_NOT, baseType ));

            if( !resultBaseType )
            {
                RMInit::logOut << "Error: QtNot::checkType() - operand type is not supported." << endl;
                parseInfo.setErrorNo(367);
                throw parseInfo;
            }

            // MDDBaseType* resultMDDType = new MDDBaseType( "tmp", resultBaseType );
            // TypeFactory::addTempType( resultMDDType );

            // dataStreamType.setType( resultMDDType );
            dataStreamType.setType( resultBaseType );
        }
        else
        {
            RMInit::logOut << "Error: QtNot::checkType() - operation is not supported for strings." << endl;
            parseInfo.setErrorNo(385);
            throw parseInfo;
        }
    }
    else
        RMInit::logOut << "Error: QtNot::checkType() - operand branch invalid." << endl;

    return dataStreamType;
}




const QtNode::QtNodeType QtDot::nodeType = QtNode::QT_DOT;



QtDot::QtDot( const string& initElementName )
    :  QtUnaryInduce( NULL ),
       elementName( initElementName ),
       elementNo(-1)
{
}



QtDot::QtDot( unsigned initElementNo )
    :  QtUnaryInduce( NULL ),
       elementNo( static_cast<int>(initElementNo) )
{
}



bool
QtDot::equalMeaning( QtNode* node )
{
    bool result = false;

    if( nodeType == node->getNodeType() )
    {
        QtDot* dotNode = static_cast<QtDot*>(node); // by force

        // In future, elementName have to be converted to elementNo
        // and then just the numbers are compared.
        if( (elementNo != -1 && elementNo == dotNode->elementNo)     ||
                (elementNo == -1 && elementName == dotNode->elementName)    )
            result = input->equalMeaning( dotNode->getInput() );
    };

    return result;
}


string
QtDot::getSpelling()
{
    char tempStr[20];
    sprintf(tempStr, "%lud", static_cast<unsigned long>(getNodeType()));
    string result  = string(tempStr);

    if( elementNo == -1 )
        result.append( elementName );
    else
    {
        std::ostringstream bufferStream;
        bufferStream << elementNo << ends;

        result.append( bufferStream.str() );
    }

    result.append( "(" );
    result.append( input->getSpelling() );
    result.append( ")" );

    return result;
}


QtData*
QtDot::evaluate( QtDataList* inputList )
{
    RMDBCLASS( "QtDot", "evaluate( QtDataList* )", "qlparser", __FILE__, __LINE__ )
    startTimer("QtDot");

    QtData* returnValue = NULL;
    QtData* operand = NULL;

    if( getOperand( inputList, operand ) )
    {
        if( operand->getDataType() == QT_MDD )
        {
            QtMDD* mdd = static_cast<QtMDD*>(operand);

#ifdef QT_RUNTIME_TYPE_CHECK
            // test, if operand has complex base type
            if( mdd->getCellType()->getType() != STRUCT )
            {
                RMInit::logOut << "Internal error in QtDot::evaluate() - "
                               << "runtime type checking failed." << endl;

                // delete old operand
                if( operand ) operand->deleteRef();

                return 0;
            }
#endif

            StructType*  operandType    = static_cast<StructType*>(mdd->getCellType());
            unsigned int operandOffset;
            const BaseType*    resultCellType = NULL;

            if( elementNo == -1 )
                resultCellType = operandType->getElemType( elementName.c_str() );
            else
                resultCellType = operandType->getElemType( static_cast<unsigned int>(elementNo) );

            if( !resultCellType )
            {
                RMInit::logOut << "Error: QtDot::evaluate() - struct selector is not valid." << endl;
                parseInfo.setErrorNo(370);
                throw parseInfo;
            }

            if( elementNo == -1 )
                operandOffset = operandType->getOffset( elementName.c_str() );
            else
                operandOffset = operandType->getOffset( static_cast<unsigned int>(elementNo) );

            RMDBGIF( 1, RMDebug::module_qlparser, "QtUnaryInduce", \
                     char* typeStructure = operandType->getTypeStructure();  \
                     RMDBGMIDDLE( 4, RMDebug::module_qlparser, "QtUnaryInduce", "Operand base type   " << operandType->getTypeName() << ", structure " << typeStructure ) \
                     free( typeStructure ); typeStructure=NULL; \
                     RMDBGMIDDLE( 4, RMDebug::module_qlparser, "QtUnaryInduce", "Operand base offset " << operandOffset ) \
                     typeStructure = resultCellType->getTypeStructure();   \
                     RMDBGMIDDLE( 4, RMDebug::module_qlparser, "QtUnaryInduce", "Result base type    " << resultCellType->getTypeName() << ", structure " << typeStructure ) \
                     free( typeStructure ); typeStructure=NULL; \
                   )

            returnValue = computeUnaryMDDOp( mdd, resultCellType, Ops::OP_IDENTITY, operandOffset );
        }
        else if( operand->isScalarData() )
        {
            QtScalarData* scalar = static_cast<QtScalarData*>(operand);

#ifdef QT_RUNTIME_TYPE_CHECK
            // test, if operand has complex base type
            if( scalar->getValueType()->getType() != STRUCT )
            {
                RMInit::logOut << "Internal error in QtDot::evaluate() - "
                               << "runtime type checking failed." << endl;

                // delete old operand
                if( operand ) operand->deleteRef();

                return 0;
            }
#endif

            StructType*  operandType    = static_cast<StructType*>(const_cast<BaseType*>(scalar->getValueType()));
            unsigned int operandOffset;
            const BaseType*    resultCellType=NULL;

            if( elementNo == -1 )
                resultCellType = operandType->getElemType( elementName.c_str() );
            else
                resultCellType = operandType->getElemType( static_cast<unsigned int>(elementNo) );

            if( !resultCellType )
            {
                RMInit::logOut << "Error: QtDot::evaluate() - struct selector is not valid." << endl;
                parseInfo.setErrorNo(370);
                throw parseInfo;
            }

            if( elementNo == -1 )
                operandOffset = operandType->getOffset( elementName.c_str() );
            else
                operandOffset = operandType->getOffset( static_cast<unsigned int>(elementNo) );

            RMDBGIF( 1, RMDebug::module_qlparser, "QtUnaryInduce", \
                     char* typeStructure = operandType->getTypeStructure();  \
                     RMDBGMIDDLE( 4, RMDebug::module_qlparser, "QtUnaryInduce", "Operand scalar type   " << operandType->getTypeName() << ", structure " << typeStructure ) \
                     free( typeStructure ); typeStructure=NULL; \
                     RMDBGMIDDLE( 4, RMDebug::module_qlparser, "QtUnaryInduce", "Operand scalar offset " << operandOffset ) \
                     typeStructure = resultCellType->getTypeStructure();  \
                     RMDBGMIDDLE( 4, RMDebug::module_qlparser, "QtUnaryInduce", "Result scalar type    " << resultCellType->getTypeName() << ", structure " << typeStructure )  \
                     free( typeStructure ); typeStructure=NULL; \
                   )

            returnValue = computeUnaryOp( scalar, resultCellType, Ops::OP_IDENTITY, operandOffset );
        }
        else
        {
            RMInit::logOut << "Error: QtDot::evaluate() - operation is not supported for strings." << endl;
            parseInfo.setErrorNo(385);
            throw parseInfo;
        }

        // delete old operand
        if( operand ) operand->deleteRef();
    }
    
    stopTimer();

    return returnValue;
}



void
QtDot::printTree( int tab, ostream& s, QtChildType mode )
{
    if( elementNo == -1 )
        s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "QtDot Object: access " << elementName.c_str() << getEvaluationTime() << endl;
    else
        s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "QtDot Object: access no " << elementNo << getEvaluationTime() << endl;

    QtUnaryInduce::printTree( tab+2, s, mode );
}

void
QtDot::printAlgebraicExpression( ostream& s )
{
    s << "(";

    if( input )
        input->printAlgebraicExpression( s );
    else
        s << "<nn>";

    s << ").";

    if( elementNo == -1 )
        s << elementName.c_str();
    else
        s << elementNo;

    s << " ";
}

const QtTypeElement&
QtDot::checkType( QtTypeTuple* typeTuple )
{
    RMDBCLASS( "QtDot", "checkType( QtTypeTuple* )", "qlparser", __FILE__, __LINE__ )

    dataStreamType.setDataType( QT_TYPE_UNKNOWN );

    // check operand branches
    if( input )
    {

        // get input types
        const QtTypeElement& inputType = input->checkType( typeTuple );

        RMDBGIF( 4, RMDebug::module_qlparser, "QtDot", \
                 RMInit::dbgOut << "Operand: " << flush; \
                 inputType.printStatus( RMInit::dbgOut ); \
                 RMInit::dbgOut << endl; \
               )

        if( inputType.getDataType() == QT_MDD )
        {
            const BaseType* baseType = (static_cast<MDDBaseType*>(const_cast<Type*>(inputType.getType())))->getBaseType();

            // test, if operand has complex base type
            if( baseType->getType() != STRUCT )
            {
                RMInit::logOut << "Error: QtDot::evaluate() - operand of induce dot operation must be complex." << endl;
                parseInfo.setErrorNo(368);
                throw parseInfo;
            }

            StructType*  structType    = static_cast<StructType*>(const_cast<BaseType*>(baseType));
            const BaseType*    resultBaseType = NULL;

            if( elementNo == -1 )
                resultBaseType = structType->getElemType( (elementName.c_str()) );
            else
                resultBaseType = structType->getElemType( static_cast<unsigned int>(elementNo) );

            if( !resultBaseType )
            {
                RMInit::logOut << "Error: QtDot::evaluate() - struct selector is not valid." << endl;
                parseInfo.setErrorNo(370);
                throw parseInfo;
            }

            MDDBaseType* resultMDDType = new MDDBaseType( "tmp", resultBaseType );
            TypeFactory::addTempType( resultMDDType );

            dataStreamType.setType( resultMDDType );
        }
        else if( inputType.isBaseType() )
        {
            BaseType* baseType = static_cast<BaseType*>(const_cast<Type*>(inputType.getType()));

            // test, if operand has complex base type
            if( baseType->getType() != STRUCT )
            {
                RMInit::logOut << "Error: QtDot::evaluate() - operand of dot operation must be complex." << endl;
                parseInfo.setErrorNo(369);
                throw parseInfo;
            }

            StructType*  structType    = static_cast<StructType*>(baseType);
            const BaseType*    resultBaseType = NULL;

            if( elementNo == -1 )
                resultBaseType = structType->getElemType( (elementName.c_str()) );
            else
                resultBaseType = structType->getElemType( static_cast<unsigned int>(elementNo) );

            if( !resultBaseType )
            {
                RMInit::logOut << "Error: QtDot::evaluate() - struct selector is not valid." << endl;
                parseInfo.setErrorNo(370);
                throw parseInfo;
            }

            dataStreamType.setType( resultBaseType );
        }
        else
        {
            RMInit::logOut << "Error: QtDot::checkType() - operation is not supported for strings." << endl;
            parseInfo.setErrorNo(385);
            throw parseInfo;
        }
    }
    else
        RMInit::logOut << "Error: QtDot::checkType() - operand branch invalid." << endl;
    return dataStreamType;
}

//--------------------------------------------
//  QtCast
//--------------------------------------------

const QtNode::QtNodeType QtCast::nodeType = QtNode::QT_CAST;

QtCast::QtCast(QtOperation* initInput, cast_types t):
    QtUnaryInduce(initInput), castType(t) {}

QtCast::QtCast(QtOperation* input2, const char *typeName2):
    QtUnaryInduce(input2)
{
    std::map<string, cast_types > baseCastTypes;
    baseCastTypes.insert(std::make_pair(SyntaxType::BOOL_NAME, t_bool));
    baseCastTypes.insert(std::make_pair(SyntaxType::CHAR_NAME, t_char));
    baseCastTypes.insert(std::make_pair(SyntaxType::OCTET_NAME, t_octet));
    baseCastTypes.insert(std::make_pair(SyntaxType::SHORT_NAME, t_short));
    baseCastTypes.insert(std::make_pair(SyntaxType::USHORT_NAME, t_ushort));
    baseCastTypes.insert(std::make_pair(SyntaxType::UNSIGNED_SHORT_NAME, t_ushort));
    baseCastTypes.insert(std::make_pair(SyntaxType::LONG_NAME, t_long));
    baseCastTypes.insert(std::make_pair(SyntaxType::ULONG_NAME, t_ulong));
    baseCastTypes.insert(std::make_pair(SyntaxType::UNSIGNED_LONG_NAME, t_ulong));
    baseCastTypes.insert(std::make_pair(SyntaxType::FLOAT_NAME, t_float));
    baseCastTypes.insert(std::make_pair(SyntaxType::DOUBLE_NAME, t_double));


    map<string, cast_types>::iterator findIt = baseCastTypes.find(string(typeName2));
    if (findIt != baseCastTypes.end())
    {
        this->castType = findIt->second;
    }
    else
    {
        this->typeName = string(typeName2);
    }
}

QtData* QtCast::evaluate(QtDataList* inputList)
{
    startTimer("QtCast");
    
    QtData* returnValue = NULL;
    QtData* operand = NULL;

    if(getOperand(inputList, operand))
    {
        if(typeName.empty())
        {
            // old style cast operator
            returnValue = computeOp( operand, getOp(castType));
        }
        else
        {
            const BaseType *resultType = TypeFactory::mapType(typeName.c_str());
            Ops::OP_CAST_GENERAL;
            if(operand->getDataType() == QT_MDD)
            {
                QtMDD* mdd = static_cast<QtMDD*>(operand);
                returnValue = computeUnaryMDDOp(mdd, resultType, Ops::OP_CAST_GENERAL);
                (static_cast<QtMDD*>(returnValue))->setFromConversion(mdd->isFromConversion());
            }
            else if (operand->isScalarData())
            {
                QtScalarData* scalar = static_cast<QtScalarData*>(operand);
                returnValue = computeUnaryOp(scalar, resultType, Ops::OP_CAST_GENERAL);
            }
        }
    }

    // delete old operand
    if(operand) operand->deleteRef();
    
    stopTimer();
    
    return returnValue;
}

void QtCast::printTree(int tab, ostream& s, QtChildType mode)
{
    const char *type_name[] =
    {
        "bool", "octet", "char", "short", "ushort",
        "long", "ulong", "float", "double"
    };
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "QtCastObject "
      << static_cast<int>(getNodeType())
      << "<" << (typeName.empty() ? type_name[castType] : typeName.c_str()) << ">" << getEvaluationTime()
      << endl;
    QtUnaryInduce::printTree( tab + 2, s, mode );
}

void QtCast::printAlgebraicExpression(ostream& s)
{
    const char *type_name[] =
    {
        "bool", "octet", "char", "short", "ushort",
        "long", "ulong", "float", "double"
    };
    s << "cast<" << (typeName.empty() ? type_name[castType] : typeName.c_str()) << ">(";
    if(input)
        input->printAlgebraicExpression(s);
    else
        s << "<nn>";
    s << ")";
}

const QtTypeElement& QtCast::checkType(QtTypeTuple* typeTuple)
{

    RMDBCLASS( "QtCast", "checkType( QtTypeTuple* )", "qlparser", __FILE__, __LINE__ )

    dataStreamType.setDataType( QT_TYPE_UNKNOWN );

    // check operand branches
    if(input)
    {

        // get input types
        const QtTypeElement& inputType = input->checkType( typeTuple );
        RMDBGIF( 4, RMDebug::module_qlparser, "QtCast", \
                 RMInit::dbgOut << "Operand: " << flush; \
                 inputType.printStatus( RMInit::dbgOut ); \
                 RMInit::dbgOut << endl; \
               )

        if(inputType.getDataType() == QT_MDD)
        {
            const BaseType* resultBaseType = NULL;
            if(typeName.empty())
            {
                const BaseType* baseType = (static_cast<MDDBaseType*>(const_cast<Type*>(inputType.getType())))->getBaseType();
                resultBaseType = const_cast<BaseType*>(Ops::getResultType( getOp(castType), baseType ));
            }
            else
            {
                resultBaseType = TypeFactory::mapType( typeName.c_str() );
            }

            if(!resultBaseType)
            {
                RMInit::logOut << "Error: QtCast::checkType() - induce operand type is not support" << endl;
                parseInfo.setErrorNo(366);
                throw parseInfo;
            }

            MDDBaseType* resultMDDType = new MDDBaseType( "tmp", resultBaseType );
            TypeFactory::addTempType( resultMDDType );
            dataStreamType.setType( resultMDDType );
        }

        else if(inputType.isBaseType())
        {
            const BaseType* resultBaseType = NULL;
            if(typeName.empty())
            {
                BaseType* baseType = static_cast<BaseType*>(const_cast<Type*>(inputType.getType()));
                resultBaseType = const_cast<BaseType*>(Ops::getResultType( getOp(castType), baseType ));
            }
            else
            {
                resultBaseType = TypeFactory::mapType( typeName.c_str() );
            }

            if(!resultBaseType)
            {
                RMInit::logOut << "Error: QtCast::checkType() - operand type is not supported." << endl;
                parseInfo.setErrorNo(367);
                throw parseInfo;
            }

            dataStreamType.setType( resultBaseType );
        }
        else
        {
            RMInit::logOut << "Error: QtCast::checkType() - operation is not supported for strings." << endl;
            parseInfo.setErrorNo(385);
            throw parseInfo;
        }
    }
    else
        RMInit::logOut << "Error: QtCast::checkType() - operand branch invalid." << endl;

    return dataStreamType;
}


//--------------------------------------------
//  QtRealPartOp
//--------------------------------------------

const QtNode::QtNodeType QtRealPartOp::nodeType = QtNode::QT_REALPART;

QtRealPartOp::QtRealPartOp(QtOperation* initInput): QtUnaryInduce(initInput) {}

QtData* QtRealPartOp::evaluate(QtDataList* inputList)
{
    startTimer("QtRealPartOp");
    
    QtData* returnValue = NULL;
    QtData* operand = NULL;

    if(getOperand(inputList, operand))
        returnValue = computeOp( operand, Ops::OP_REALPART );
    // delete old operand
    if(operand) operand->deleteRef();
    
    stopTimer();
    
    return returnValue;
}

void QtRealPartOp::printTree(int tab, ostream& s, QtChildType mode)
{
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "QtRealPartOpObject " << static_cast<int>(getNodeType()) << getEvaluationTime() << endl;
    QtUnaryInduce::printTree( tab + 2, s, mode );
}

void QtRealPartOp::printAlgebraicExpression(ostream& s)
{
    s << "Re(";
    if(input)
        input->printAlgebraicExpression(s);
    else
        s << "<nn>";
    s << ")";
}

const QtTypeElement& QtRealPartOp::checkType(QtTypeTuple* typeTuple)
{
    RMDBCLASS( "QtRealPartOp", "checkType( QtTypeTuple* )", "qlparser", __FILE__, __LINE__ )

    dataStreamType.setDataType( QT_TYPE_UNKNOWN );

    // check operand branches
    if(input)
    {

        // get input types
        const QtTypeElement& inputType = input->checkType( typeTuple );
        RMDBGIF( 1, RMDebug::module_qlparser, "QtRealPartOp", \
                 RMInit::dbgOut << "Operand: " << flush; \
                 inputType.printStatus( RMInit::dbgOut ); \
                 RMInit::dbgOut << endl;
               )

        if(inputType.getDataType() == QT_MDD)
        {
            const BaseType* baseType = (static_cast<MDDBaseType*>(const_cast<Type*>(inputType.getType())))->getBaseType();
            BaseType* resultBaseType = const_cast<BaseType*>(Ops::getResultType( Ops::OP_REALPART, baseType ));
            if(!resultBaseType)
            {
                RMInit::logOut << "Error: QtRealPartOp::checkType() - induce operand type is not support" << endl;
                parseInfo.setErrorNo(366);
                throw parseInfo;
            }
            MDDBaseType* resultMDDType = new MDDBaseType( "tmp", resultBaseType );
            TypeFactory::addTempType( resultMDDType );
            dataStreamType.setType( resultMDDType );
        }
        else if(inputType.isBaseType())
        {
            BaseType* baseType = static_cast<BaseType*>(const_cast<Type*>(inputType.getType()));
            BaseType* resultBaseType = const_cast<BaseType*>(Ops::getResultType( Ops::OP_REALPART, baseType ));
            if(!resultBaseType)
            {
                RMInit::logOut << "Error: QtRealPartOp::checkType() - operand type is not supported." << endl;
                parseInfo.setErrorNo(367);
                throw parseInfo;
            }
            dataStreamType.setType( resultBaseType );
        }
        else
        {
            RMInit::logOut << "Error: QtRealPartOp::checkType() - operation is not supported for strings." << endl;
            parseInfo.setErrorNo(385);
            throw parseInfo;
        }
    }
    else
        RMInit::logOut << "Error: QtRealPartOp::checkType() - operand branch invalid." << endl;

    return dataStreamType;
}

//--------------------------------------------
//  QtImaginarPartOp
//--------------------------------------------

const QtNode::QtNodeType QtImaginarPartOp::nodeType = QtNode::QT_IMAGINARPART;

QtImaginarPartOp::QtImaginarPartOp(QtOperation* initInput): QtUnaryInduce(initInput) {}

QtData* QtImaginarPartOp::evaluate(QtDataList* inputList)
{
    startTimer("QtImaginaryPartOp");
    
    QtData* returnValue = NULL;
    QtData* operand = NULL;

    if(getOperand(inputList, operand))
        returnValue = computeOp( operand, Ops::OP_IMAGINARPART );
    // delete old operand
    if(operand) operand->deleteRef();
    
    stopTimer();
    
    return returnValue;
}

void QtImaginarPartOp::printTree(int tab, ostream& s, QtChildType mode)
{
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "QtImaginarPartObject " << static_cast<int>(getNodeType()) << getEvaluationTime() << endl;
    QtUnaryInduce::printTree( tab + 2, s, mode );
}

void QtImaginarPartOp::printAlgebraicExpression(ostream& s)
{
    s << "Im(";
    if(input)
        input->printAlgebraicExpression(s);
    else
        s << "<nn>";
    s << ")";
}

const QtTypeElement& QtImaginarPartOp::checkType(QtTypeTuple* typeTuple)
{
    RMDBCLASS( "QtImaginarPart", "checkType( QtTypeTuple* )", "qlparser", __FILE__, __LINE__ )

    dataStreamType.setDataType( QT_TYPE_UNKNOWN );

    // check operand branches
    if(input)
    {

        // get input types
        const QtTypeElement& inputType = input->checkType( typeTuple );

        RMDBGIF( 4, RMDebug::module_qlparser, "QtImaginarPartOp", \
                 RMInit::dbgOut << "Operand: " << flush; \
                 inputType.printStatus( RMInit::dbgOut ); \
                 RMInit::dbgOut << endl; \
               )

        if(inputType.getDataType() == QT_MDD)
        {
            const BaseType* baseType = (static_cast<MDDBaseType*>(const_cast<Type*>(inputType.getType())))->getBaseType();
            BaseType* resultBaseType = const_cast<BaseType*>(Ops::getResultType( Ops::OP_IMAGINARPART, baseType ));
            if(!resultBaseType)
            {
                RMInit::logOut << "Error: QtImaginarPart::checkType() - induce operand type is not support" << endl;
                parseInfo.setErrorNo(366);
                throw parseInfo;
            }
            MDDBaseType* resultMDDType = new MDDBaseType( "tmp", resultBaseType );
            TypeFactory::addTempType( resultMDDType );
            dataStreamType.setType( resultMDDType );
        }
        else if(inputType.isBaseType())
        {
            BaseType* baseType = static_cast<BaseType*>(const_cast<Type*>(inputType.getType()));
            BaseType* resultBaseType = const_cast<BaseType*>(Ops::getResultType(Ops::OP_IMAGINARPART, baseType));
            if(!resultBaseType)
            {
                RMInit::logOut << "Error: QtImaginarPart::checkType() - operand type is not supported." << endl;
                parseInfo.setErrorNo(367);
                throw parseInfo;
            }
            dataStreamType.setType( resultBaseType );
        }
        else
        {
            RMInit::logOut << "Error: QtImaginarPart::checkType() - operation is not supported for strings." << endl;
            parseInfo.setErrorNo(385);
            throw parseInfo;
        }
    }
    else
        RMInit::logOut << "Error: QtImaginarPart::checkType() - operand branch invalid." << endl;
    return dataStreamType;
}

#include "autogen_qtui.cc"
