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

static const char rcsid[] = "@(#)qlparser, QtTrim: $Id: qtdomainoperation.cc,v 1.35 2002/06/15 20:16:42 coman Exp $";

#include "config.h"
#include <vector>
#include <string>
using namespace std;

#include "raslib/rmdebug.hh"

#include "qlparser/qtmdd.hh"
#include "qlparser/qtunaryoperation.hh"
#include "qlparser/qtdomainoperation.hh"
#include "qlparser/qtnode.hh"
#include "qlparser/qtconst.hh"
#include "qlparser/qtmintervaldata.hh"
#include "qlparser/qtintervaldata.hh"
#include "qlparser/qtpointdata.hh"
#include "qlparser/qtatomicdata.hh"
#include "qlparser/qtcomplexdata.hh"

#include "mddmgr/mddobj.hh"
#include "tilemgr/tile.hh"

#include "catalogmgr/ops.hh"
#include "relcatalogif/mddbasetype.hh"
#include "relcatalogif/ulongtype.hh"
#include "mymalloc/mymalloc.h"

#include <iostream>
#include <string.h>

#include <easylogging++.h>


const QtNode::QtNodeType QtDomainOperation::nodeType = QtNode::QT_DOMAIN_OPERATION;


QtDomainOperation::QtDomainOperation( QtOperation* mintOp )
    : mintervalOp( mintOp ),
      dynamicMintervalExpression( true )
{
    if( mintervalOp ) mintervalOp->setParent( this );
}



QtDomainOperation::QtDomainOperation( r_Minterval domainNew, const vector<bool>* newTrimFlags )
    : mintervalOp(0),
      dynamicMintervalExpression( false )
{
    // make a copy
    vector<bool>* trimFlags  = new vector<bool>( *newTrimFlags );
    mintervalOp = new QtConst( new QtMintervalData( domainNew, trimFlags ) );
    mintervalOp->setParent( this );
}



QtDomainOperation::~QtDomainOperation()
{
    if( mintervalOp )
    {
        delete mintervalOp;
        mintervalOp=NULL;
    }
}



QtNode::QtNodeList*
QtDomainOperation::getChilds( QtChildType flag )
{
    QtNodeList* resultList=NULL;

    resultList = QtUnaryOperation::getChilds( flag );

    if( mintervalOp )
    {
        if( flag == QT_LEAF_NODES || flag == QT_ALL_NODES )
        {
            QtNodeList* subList;

            subList = mintervalOp->getChilds( flag );

            // remove all elements in subList and insert them at the beginning in resultList
            resultList->splice( resultList->begin(), *subList );

            // delete temporary subList
            delete subList;
            subList=NULL;
        }

        // add the nodes of the current level
        if( flag == QT_DIRECT_CHILDS || flag == QT_ALL_NODES )
            resultList->push_back( mintervalOp );
    }

    return resultList;
}



bool
QtDomainOperation::equalMeaning( QtNode* node )
{
    bool result = false;

    if( nodeType == node->getNodeType() )
    {
        QtDomainOperation* domainNode;
        domainNode = static_cast<QtDomainOperation*>(node); // by force

        result = input->equalMeaning( domainNode->getInput() ) &&
                 mintervalOp->equalMeaning( domainNode->getMintervalOp() );
    };

    return ( result );
}


string
QtDomainOperation::getSpelling()
{
    char tempStr[20];
    sprintf(tempStr, "%lu", static_cast<unsigned long>(getNodeType()));
    string result  = string(tempStr);
    result.append( mintervalOp->getSpelling() );
    result.append( "(" );
    result.append( input->getSpelling() );
    result.append( ")" );

    return result;
}


void
QtDomainOperation::setInput( QtOperation* inputOld, QtOperation* inputNew )
{
    QtUnaryOperation::setInput( inputOld, inputNew );

    if( mintervalOp == inputOld )
    {
        mintervalOp = inputNew;

        if( inputNew )
            inputNew->setParent( this );
    }
}



void
QtDomainOperation::optimizeLoad( QtTrimList* trimList )
{
    // test, if there is already a specification for that dimension
    bool trimming = false;

    if( mintervalOp )
    {
        // pass optimization to minterval tree
        mintervalOp->optimizeLoad( new QtNode::QtTrimList );

        // evaluate minterval tree
        QtData* operand = mintervalOp->evaluate(NULL);

        // if spatial operation could be determined, it is static
        dynamicMintervalExpression = !operand;

        if( operand )
        {
            if( operand->getDataType() == QT_MINTERVAL )
            {
                r_Minterval   domain    = (static_cast<QtMintervalData*>(operand))->getMintervalData();
                vector<bool>* trimFlags = new vector<bool>( *((static_cast<QtMintervalData*>(operand))->getTrimFlags()) );

                if( trimList && trimList->empty() )
                {
                    // no previous specification for that dimension
                    trimming = true;
                    for( unsigned int i=0; i!=domain.dimension(); i++ )
                    {
                        // create a new element
                        QtTrimElement* elem = new QtTrimElement;

                        elem->interval     = domain[i];
                        elem->intervalFlag = (*trimFlags)[i];
                        elem->dimension    = i;

                        // and add it to the list
                        trimList->push_back( elem );

                        trimming &= (*trimFlags)[i];
                    }
                }
                else
                {
                    // previous specification exists, test for compatibility
                }

                if( trimFlags )
                {
                    delete trimFlags;
                    trimFlags = NULL;
                }
            }

            //      else if( operand->getDataType() == QtData::QT_POINT )

            // delete the operand
            operand->deleteRef();
        }
    }

    // pass optimization process to the input tree
    if( input ) input->optimizeLoad( trimList );

    // Eliminate node QtDomainOperation if only trimming occurs.
    if( trimming )
    {
        LTRACE << "all trimming";

        getParent()->setInput( this, input );

        // Delete the node itself after resetting its input because
        // otherwise the input is delete either.
        setInput( input, NULL );
        delete this;
    }
}



QtData*
QtDomainOperation::evaluate( QtDataList* inputList )
{
    startTimer("QtDomainOperation");

    QtData* returnValue = NULL;

    switch( input->getDataStreamType().getDataType() )
    {
    case QT_MDD:
    {
        if( mintervalOp->getDataStreamType().getDataType() == QT_POINT ||
                mintervalOp->getDataStreamType().isInteger()                  )
        {
            //
            // Projection to one cell.
            //

            QtData* indexData = mintervalOp->evaluate( inputList );

            if( !indexData )
                return 0;

#ifdef QT_RUNTIME_TYPE_CHECK
            if(  indexData->getDataType() != QT_POINT &&
                    !indexData->getDataType().isInteger()    )
            {
                LERROR << "Internal error in QtDomainOperation::evaluate() - "
                               << "runtime type checking failed (QT_POINT, INTEGER).";

                // delete index data
                indexData->deleteRef();

                return 0;
            }
#endif

            // get projection point
            r_Point projPoint;

            if( indexData->getDataType() == QT_POINT )
            {
                projPoint = (static_cast<QtPointData*>(indexData))->getPointData();
            }
            else
            {
                projPoint = r_Point(1);

                if( indexData->getDataType() == QT_SHORT ||
                        indexData->getDataType() == QT_OCTET ||
                        indexData->getDataType() == QT_LONG     )
                    projPoint[0] = (static_cast<QtAtomicData*>(indexData))->getSignedValue();
                else
                    projPoint[0] = (static_cast<QtAtomicData*>(indexData))->getUnsignedValue();

            }

            //
            // In case of dynamic index expressions, load optimization has to
            // be performed for the current input expression.
            //
            /*
                if( dynamicMintervalExpression )
                {
                  QtNode::QtTrimList* trimList = new QtNode::QtTrimList;

                  for( int i=0; i!=domain.dimension(); i++ )
                  {
                    // create a new element
                    QtTrimElement* elem = new QtTrimElement;

                    elem->interval     = domain[i];
                    elem->intervalFlag = (*trimFlags)[i];
                    elem->dimension    = i;

                    // and add it to the list
                    trimList->push_back( elem );
                  }

                  // pass optimization process to the input tree
                  input->optimizeLoad( trimList );
                }
            */

            //  get operand data
            QtData* operand = input->evaluate( inputList );

            if( !operand )
            {
                // delete indexData
                indexData->deleteRef();

                return 0;
            }

#ifdef QT_RUNTIME_TYPE_CHECK
            if( operand->getDataType() != QT_MDD )
            {
                LERROR << "Internal error in QtDomainOperation::evaluate() - runtime type checking failed (QT_MDD).";

                // delete index and operand data
                indexData->deleteRef();
                operand  ->deleteRef();

                return 0;
            }
#endif

            QtMDD*  qtMDD         = static_cast<QtMDD*>(operand);
            MDDObj* currentMDDObj = qtMDD->getMDDObject();
            r_Minterval* nullValues = currentMDDObj->getNullValues();

            if( currentMDDObj )
            {
                LTRACE << "  mdd domain: "              << currentMDDObj->getCurrentDomain();
                LTRACE << "  mdd load domain: "         << qtMDD->getLoadDomain();

                // reset loadDomain to intersection of domain and loadDomain
                // if( domain.intersection_with( qtMDD->getLoadDomain() ) != qtMDD->getLoadDomain() )
                //   qtMDD->setLoadDomain( domain.intersection_with( qtMDD->getLoadDomain() ) );

                // get type of cell
                const BaseType* cellType = (const_cast<MDDBaseType*>(currentMDDObj->getMDDBaseType()))->getBaseType();

                LTRACE << "  point access: " << projPoint;
                char* resultCell = NULL;
                if (projPoint.dimension() == currentMDDObj->getDimension())
                    resultCell = currentMDDObj->pointQuery( projPoint );
                if (resultCell == NULL)
                {
                    resultCell = new char[cellType->getSize()];
                    memset(resultCell, 0, cellType->getSize());
                }

                // allocate cell buffer
                char* resultBuffer = new char[ cellType->getSize() ];

                // copy cell content
                memcpy( resultBuffer, resultCell, cellType->getSize() );

                // create data object for the cell
                QtScalarData* scalarDataObj = NULL;
                if( cellType->getType() == STRUCT )
                    scalarDataObj = new QtComplexData();
                else
                    scalarDataObj = new QtAtomicData();

                scalarDataObj->setValueType  ( cellType );
                scalarDataObj->setValueBuffer( resultBuffer );

                // set return data object
                returnValue = scalarDataObj;
                returnValue->setNullValues(nullValues);
            }

            // delete indexData
            indexData->deleteRef();

            // delete old operand
            operand->deleteRef();

        }
        else // mintervalOp->getDataStreamType() == QT_MINTERVAL
        {
            //
            // Trimming/Projection to an MDD object
            //
            QtData* indexData = mintervalOp->evaluate( inputList );

            if( !indexData )
                return 0;

#ifdef QT_RUNTIME_TYPE_CHECK
            if( indexData->getDataType() != QT_MINTERVAL )
            {
                LERROR << "Internal error in QtDomainOperation::evaluate() - "
                               << "runtime type checking failed (QT_MINTERVAL).";

                // delete index data
                indexData->deleteRef();

                return 0;
            }
#endif

            // get minterval data
            vector<bool>*  trimFlags = new vector<bool>( *((static_cast<QtMintervalData*>(indexData))->getTrimFlags()) );
            r_Minterval    domain    = (static_cast<QtMintervalData*>(indexData))->getMintervalData();

            //
            // In case of dynamic index expressions, load optimization has to
            // be performed for the current input expression.
            //

            if( dynamicMintervalExpression )
            {
                QtNode::QtTrimList* trimList = new QtNode::QtTrimList;

                for( unsigned int i=0; i!=domain.dimension(); i++ )
                {
                    // create a new element
                    QtTrimElement* elem = new QtTrimElement;

                    elem->interval     = domain[i];
                    elem->intervalFlag = (*trimFlags)[i];
                    elem->dimension    = i;

                    // and add it to the list
                    trimList->push_back( elem );
                }

                // pass optimization process to the input tree
                input->optimizeLoad( trimList );
            }

            //  get operand data
            QtData* operand = input->evaluate( inputList );

            if( !operand )
            {
                // delete index data
                indexData->deleteRef();
                return 0;
            }

#ifdef QT_RUNTIME_TYPE_CHECK
            if( operand->getDataType() != QT_MDD )
            {
                LERROR << "Internal error in QtDomainOperation::evaluate() - runtime type checking failed (QT_MDD).";

                // delete index and operand data
                indexData->deleteRef();
                operand  ->deleteRef();

                return 0;
            }
#endif

            QtMDD*  qtMDD         = static_cast<QtMDD*>(operand);
            MDDObj* currentMDDObj = qtMDD->getMDDObject();
            r_Minterval* nullValues = currentMDDObj->getNullValues();

            if( currentMDDObj )
            {
                bool trimming   = false;
                bool projection = false;

                // reset loadDomain to intersection of domain and loadDomain
                if( domain.intersection_with( qtMDD->getLoadDomain() ) != qtMDD->getLoadDomain() )
                    qtMDD->setLoadDomain( domain.intersection_with( qtMDD->getLoadDomain() ) );

                // Test, if trimming has to be done; trimming = 1 if !(load domain is subset of spatial operation)
                trimming = ( domain.intersection_with( qtMDD->getLoadDomain() ) == qtMDD->getLoadDomain() );

                // Test, if a projection has to be made and build the projSet in projection case
                set< r_Dimension, less<r_Dimension> > projSet;

                for( unsigned int i=0; i<trimFlags->size(); i++ )
                    if( !(*trimFlags)[i] )
                    {
                        projection = true;
                        projSet.insert( i );
                    }

                r_Minterval projectedDom( domain.dimension() - projSet.size() );

                // build the projected domain
                for( unsigned int i = 0; i < domain.dimension(); i++ )
                    // do not include dimensions projected away
                    if(projSet.find(i) == projSet.end())
                        projectedDom << domain[i];

                if( trimFlags )
                {
                    delete trimFlags;
                    trimFlags = NULL;
                }

                LTRACE << "  operation domain..: " << domain << " with projection " << projectedDom;
                LTRACE << "  mdd load    domain: " << qtMDD->getLoadDomain();
                LTRACE << "  mdd current domain: " << currentMDDObj->getCurrentDomain();

                if( trimming || projection )
                {
                    // get relevant tiles
                    vector< boost::shared_ptr<Tile> >* relevantTiles = currentMDDObj->intersect( domain );

                    if( relevantTiles->size() > 0 )
                    {
                        // iterator for tiles
                        vector< boost::shared_ptr<Tile> >::iterator tileIt;

                        // create a transient MDD object for the query result
                        MDDObj* resultMDD = new MDDObj( currentMDDObj->getMDDBaseType(), projectedDom, currentMDDObj->getNullValues() );

                        // and iterate over them
                        for( tileIt = relevantTiles->begin(); tileIt !=  relevantTiles->end(); tileIt++ )
                        {
                            // domain of the actual tile
                            r_Minterval tileDom = (*tileIt)->getDomain();

                            // domain of the relevant area of the actual tile
                            r_Minterval intersectDom = tileDom.create_intersection( domain );

                            LTRACE << "  trimming/projecting tile with domain " << tileDom << " to domain " << intersectDom;

                            // create projected tile
                            Tile* resTile = new Tile( tileIt->get(), intersectDom, &projSet );

                            // insert Tile in result mddObj
                            resultMDD->insertTile( resTile );
                        }

                        // create a new QtMDD object as carrier object for the transient MDD object
                        returnValue = new QtMDD( static_cast<MDDObj*>(resultMDD) );
                        returnValue->setNullValues(nullValues);

                        // delete the tile vector
                        delete relevantTiles;
                        relevantTiles=NULL;
                    }
                    else
                    {
                        // Instead of throwing an exception, return an MDD initialized
                        // with null values when selecting an area that doesn't intersect
                        // with any existing tiles in the database -- DM 2012-may-24

                        const MDDBaseType* mddType = currentMDDObj->getMDDBaseType();
                        const unsigned int mddTypeSize = mddType->getBaseType()->getSize();
                        const r_Area cellCount = projectedDom.cell_count();
                        const r_Bytes arrayLength = cellCount * mddTypeSize;

                        // create a transient MDD object for the query result
                        MDDObj* resultMDD = new MDDObj( mddType, projectedDom );
                        char* data = static_cast<char*>(mymalloc( arrayLength ));

                        // fill with null value
                        memset( data, 0, arrayLength );

                        // create transient tile
                        Tile* resTile = new Tile( projectedDom, mddType->getBaseType(), data, arrayLength );
                        resTile->setPersistent(false);

                        // insert Tile in result mddObj
                        resultMDD->insertTile( resTile );
                        returnValue = new QtMDD( static_cast<MDDObj*>(resultMDD) );

//                LFATAL << "Error: QtDomainOperation::evaluate() - the load domain does not intersect with tiles in the current MDD.";
//                parseInfo.setErrorNo(356);
//
//                // delete index and operand data
//                indexData->deleteRef();
//                operand  ->deleteRef();
//
//                throw parseInfo;
                    }

                } // trimming || projection
                else
                    // operand is passed through
                    returnValue = operand;

            } // if( currentMDDObj )

            // delete index and operand data
            if( indexData ) indexData->deleteRef();
            if( operand   ) operand  ->deleteRef();

        }
        break;
    }
    case QT_MINTERVAL:
    {
        QtData* operandData = NULL;
        QtData* indexData   = NULL;

        operandData = input->evaluate( inputList );
        r_Minterval* nullValues = NULL;
        if (operandData != NULL) {
          nullValues = operandData->getNullValues();
        }

        if( operandData )
        {
#ifdef QT_RUNTIME_TYPE_CHECK
            if( operandData->getDataType() != QT_MINTERVAL )
            {
                LERROR << "Internal error in QtDomainOperation::evaluate() - runtime type checking failed (QT_MINTERVAL).";
                return 0;
            }
#endif
            // get point
            const r_Minterval& minterval = (static_cast<QtMintervalData*>(operandData))->getMintervalData();

            // get index
            indexData = mintervalOp->evaluate( inputList );

#ifdef QT_RUNTIME_TYPE_CHECK
            if( indexData->getDataType() != QT_POINT  && indexData->getDataType() != QT_CHAR &&
                    indexData->getDataType() != QT_USHORT && indexData->getDataType() != QT_ULONG   )
            {
                LERROR << "Internal error in QtDomainOperation::evaluate() - runtime type checking failed.";
                return 0;
            }
#endif
            r_Dimension indexValue = 0;

            switch( indexData->getDataType() )
            {
            case QT_POINT:
            {
                // get first element as index
                const r_Point& indexPoint = (static_cast<QtPointData*>(indexData))->getPointData();

                if( indexPoint.dimension() != 1 )
                {
                    LFATAL << "Error: QtDomainOperation::evaluate() - Operand of minterval selection must be of type unsigned integer.";
                    parseInfo.setErrorNo(397);

                    // delete ressources
                    if( operandData ) operandData->deleteRef();
                    if( indexData   ) indexData  ->deleteRef();

                    throw parseInfo;
                }

                indexValue = indexPoint[0];
            }
            break;

            case QT_CHAR:
            case QT_USHORT:
            case QT_ULONG:
                indexValue = (static_cast<QtAtomicData*>(indexData))->getUnsignedValue();
                break;

            case QT_OCTET:
            case QT_SHORT:
            case QT_LONG:
                indexValue = static_cast<r_Dimension>((static_cast<QtAtomicData*>(indexData))->getSignedValue());
                break;
            default:
                LTRACE << "evaluate() bad type " << indexData->getDataType();
                break;
            }
            if( indexValue >= minterval.dimension() )
            {
                LFATAL << "Error: QtDomainOperation::evaluate() - index for minterval selection is out of range.";
                parseInfo.setErrorNo(398);

                // delete ressources
                if( operandData ) operandData->deleteRef();
                if( indexData   ) indexData  ->deleteRef();

                throw parseInfo;
            }

            returnValue = new QtIntervalData( minterval[indexValue] );
            returnValue->setNullValues(nullValues);
        }

        // delete ressources
        if( operandData ) operandData->deleteRef();
        if( indexData   ) indexData  ->deleteRef();
        break;
    }

    case QT_POINT:
    {
        QtData* operandData = NULL;
        QtData* indexData   = NULL;

        operandData = input->evaluate( inputList );

        if( operandData )
        {
#ifdef QT_RUNTIME_TYPE_CHECK
            if( operandData->getDataType() != QT_POINT )
            {
                LERROR << "Internal error in QtDomainOperation::evaluate() - runtime type checking failed (QT_POINT).";
                return 0;
            }
#endif
            // get point
            const r_Point& pt = (static_cast<QtPointData*>(operandData))->getPointData();

            // get index
            indexData = mintervalOp->evaluate( inputList );

#ifdef QT_RUNTIME_TYPE_CHECK
            if( indexData->getDataType() != QT_POINT  && indexData->getDataType() != QT_CHAR &&
                    indexData->getDataType() != QT_USHORT && indexData->getDataType() != QT_ULONG   )
            {
                LERROR << "Internal error in QtDomainOperation::evaluate() - runtime type checking failed.";
                return 0;
            }
#endif
            r_Dimension indexValue = 0;
            r_Minterval* nullValues = NULL;

            switch( indexData->getDataType() )
            {
            case QT_POINT:
            {
                // get first element as index
                const r_Point& indexPoint = (static_cast<QtPointData*>(indexData))->getPointData();
                if (indexData != NULL) {
                  nullValues = (static_cast<QtAtomicData*>(indexData))->getNullValues();
                }

                if( indexPoint.dimension() != 1 )
                {
                    LFATAL << "Error: QtDomainOperation::evaluate() - Operand of point selection must be of type unsigned integer.";
                    parseInfo.setErrorNo(399);

                    // delete ressources
                    if( operandData ) operandData->deleteRef();
                    if( indexData   ) indexData  ->deleteRef();

                    throw parseInfo;
                }

                indexValue = indexPoint[0];
            }
            break;

            case QT_CHAR:
            case QT_USHORT:
            case QT_ULONG:
                indexValue = (static_cast<QtAtomicData*>(indexData))->getUnsignedValue();
                if (indexData != NULL) {
                  nullValues = (static_cast<QtAtomicData*>(indexData))->getNullValues();
                }
                break;

            case QT_OCTET:
            case QT_SHORT:
            case QT_LONG:
                indexValue = static_cast<r_Dimension>((static_cast<QtAtomicData*>(indexData))->getSignedValue());
                if (indexData != NULL) {
                  nullValues = (static_cast<QtAtomicData*>(indexData))->getNullValues();
                }
                break;
            default:
                LTRACE << "evaluate() 2 - bad type " << indexData->getDataType();
                break;
            }

            if( indexValue >= pt.dimension() )
            {
                LFATAL << "Error: QtDomainOperation::evaluate() - index for point selection is out of range.";
                parseInfo.setErrorNo(411);

                // delete ressources
                if( operandData ) operandData->deleteRef();
                if( indexData   ) indexData  ->deleteRef();

                throw parseInfo;
            }

            returnValue = new QtAtomicData( static_cast<r_Long>(pt[indexValue]), 4 );
            returnValue->setNullValues(nullValues);
        }

        // delete ressources
        if( operandData ) operandData->deleteRef();
        if( indexData   ) indexData  ->deleteRef();
        break;
    }

    default:
    {
        LFATAL << "Error: QtDomainOperation::evaluate() - selection operation is not supported on this data type.";
        parseInfo.setErrorNo(396);
        throw parseInfo;
    }
    }
    
    stopTimer();

    return returnValue;
}


void
QtDomainOperation::printTree( int tab, ostream& s, QtChildType mode )
{
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "QtDomainOperation Object: type " << flush;
    dataStreamType.printStatus( s );
    s << getEvaluationTime();
    s << endl;

    if( mintervalOp )
    {
        s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "spatial operation: " << endl;
        mintervalOp->printTree( tab + 2, s );
    }
    else
        s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "no spatial operation" << endl;

    QtUnaryOperation::printTree( tab, s, mode );
}



void
QtDomainOperation::printAlgebraicExpression( ostream& s )
{
    s << "geo(";

    if( mintervalOp )
        mintervalOp->printAlgebraicExpression( s );
    else
        s << "<nn>";

    s << ",";

    if( input )
        input->printAlgebraicExpression( s );
    else
        s << "<nn>";

    s << ")";
}



const QtTypeElement&
QtDomainOperation::checkType( QtTypeTuple* typeTuple )
{
    dataStreamType.setDataType( QT_TYPE_UNKNOWN );

    //
    // operation signatures
    //
    // MDD<T,D>  x Minterval  ->  MDD<T,D>
    //  // MDD<T,D>  x Interval   ->  MDD<T,D>
    // MDD<T,D>  x Point      ->  T
    // MDD<T,D>  x Integer    ->  T
    // Minterval x Integer    ->  Interval
    // Minterval x Point(1)   ->  Interval
    // Point     x Integer    ->  Integer
    // Point     x Point(1)   ->  Integer
    //

    // check operand branches
    if( input && mintervalOp )
    {

        // 1. check input expression type
        const QtTypeElement& inputType = input->checkType( typeTuple );

        // 2. check index expression type
        const QtTypeElement& indexType = mintervalOp->checkType( typeTuple );

        // 3. determine result type
        if( inputType.getDataType() == QT_MDD )
        {
            // check index type
            if(     indexType.getDataType() != QT_MINTERVAL
                    //        && indexType.getDataType() != QT_INTERVAL
                    &&  indexType.getDataType() != QT_POINT
                    && !indexType.isInteger()
              )
            {
                LFATAL << "Error: QtDomainOperation::checkType() - spatial domain expressions must be either of type minterval, point, or integer.";
                parseInfo.setErrorNo(391);
                throw parseInfo;
            }

            // MDD
            if( indexType.getDataType() == QT_MINTERVAL /* || indexType.getDataType() == QT_INTERVAL */ )
                // pass MDD type
                dataStreamType = inputType;
            else
                // use MDD cell type
                dataStreamType.setType( (static_cast<MDDBaseType*>(const_cast<Type*>(inputType.getType())))->getBaseType() );
        }
        else if( inputType.getDataType() == QT_MINTERVAL )
        {
            // check index type
            if( !indexType.isInteger() && indexType.getDataType() != QT_POINT )
            {
                LFATAL << "Error: QtDomainOperation::checkType() - Operand of minterval selection must be of type integer.";
                parseInfo.setErrorNo(397);
                throw parseInfo;
            }

            dataStreamType.setDataType( QT_INTERVAL );
        }
        else if( inputType.getDataType() == QT_POINT )
        {
            // check index type
            if( !indexType.isInteger() && indexType.getDataType() != QT_POINT )
            {
                LFATAL << "Error: QtDomainOperation::checkType() - Operand of point selection must be of type integer.";
                parseInfo.setErrorNo(399);
                throw parseInfo;
            }

            dataStreamType.setDataType( QT_LONG );
        }
        else
        {
            LFATAL << "Error: QtDomainOperation::checkType() - selection operation is not supported on this data type.";
            parseInfo.setErrorNo(396);
            throw parseInfo;
        }
    }
    else
        LERROR << "Error: QtDomainOperation::checkType() - input or index branch invalid.";

    return dataStreamType;
}


