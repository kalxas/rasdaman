static const char rcsid[] = "@(#)qlparser, QtMulticlipping: $Id: qtmulticlipping.cc,v 1.47 2002/08/19 11:13:27 coman Exp $";

#include "qlparser/qtmulticlipping.hh"
#include "raslib/miter.hh"

#include "config.h"

#include "qlparser/qtmdd.hh"
#include "qlparser/qtatomicdata.hh"
#include "qlparser/qtcomplexdata.hh"
#include "qlparser/qtnode.hh"
#include "qlparser/qtmshapedata.hh"

#include "mddmgr/mddobj.hh"
#include "tilemgr/tile.hh"

#include "catalogmgr/typefactory.hh"
#include "relcatalogif/structtype.hh"
#include "relcatalogif/mdddimensiontype.hh"
#include "relcatalogif/syntaxtypes.hh"
#include "qlparser/qtmshapeop.hh"
#include "qlparser/qtfindsection.hh"

#include <easylogging++.h>

#include "qlparser/qtpointdata.hh"
#include "qlparser/qtoperation.hh"

#include <sstream>
#ifndef CPPSTDLIB
#else
#include <string>
#include <cmath>
#include <array>
#endif

#include <iostream>

// constructor for QtMulticlipping

const QtNode::QtNodeType QtMulticlipping::nodeType = QtNode::QT_MULTICLIPPING;

QtMulticlipping::QtMulticlipping(QtOperation* mddOp, const std::vector<QtMShapeData*>& mshapeVector, QtMulticlipType ct)
    : QtUnaryOperation(mddOp), mshapeList(mshapeVector), clipType(ct)
{
}

MDDObj*
QtMulticlipping::extractMultipolygon(const r_Minterval& areaOp, const MDDObj* op)
{   
    //for each mshape, generate a polygon clipping object and the minterval it belongs to, and append that data to a global result mask.
    
    //the polygon clipping object vector
    std::vector<QtPolygonClipping> theseClippings;
    theseClippings.reserve(mshapeList.size());
    
    for(auto i = mshapeList.begin(); i != mshapeList.end(); i++)
    {
        //construct the polygon clipping object and generate a mask
        QtPolygonClipping thisClipping( (*i)->convexHull(), (*i)->getPolytopePoints() );
        theseClippings.emplace_back(thisClipping);
    }

    //constructing the result domain for the mask and the resultmdd
    //result domain
    std::unique_ptr<r_Minterval> resultDom;
    
    for(auto i = theseClippings.begin(); i != theseClippings.end(); i++)
    {
        if(i->getDomain().intersects_with(areaOp))
        {
            if(resultDom)
            {
                *resultDom = resultDom->closure_with(i->getDomain());
            }
            else
            {
                resultDom.reset( new r_Minterval(i->getDomain()) );
            }
        }
    }
    
    //in case nothing was hit, we can simply return to the parent method, where we will throw an error.
    if(!resultDom)
    {
        return NULL;
    }
    //result mask
    char* resultMask = new char[resultDom->cell_count()];
    memset(resultMask, 2, resultDom->cell_count());

    //starting point of the mask, for iteration
    const char* resultMaskPtr = &resultMask[0];
    
    for(auto iter = theseClippings.begin(); iter != theseClippings.end(); iter++)
    {
        if(iter->getDomain().intersects_with(areaOp))
        {
            const r_Minterval* currentDomain = new r_Minterval(iter->getDomain());
            vector< vector<char> > polygonMask = iter->generateMask();
            
            r_Miter resultMaskIter(currentDomain, resultDom.get(), sizeof(char), resultMaskPtr);
            for(size_t m = 0; m < polygonMask.size(); m++)
            {
                for(size_t n = 0; n < polygonMask[m].size(); n++)
                {
                    if(polygonMask[m][n] < 2) //copy the new mask's polygon into the overall mask
                    {
                        *(resultMaskIter.nextCell()) = polygonMask[m][n];
                    }
                    else
                    {
                        resultMaskIter.nextCell();
                    }
                }
            }       
        }
    }   
    
    //gennerate resultMDD
    MDDDimensionType* mddDimensionType = new MDDDimensionType("tmp", op->getCellType(), 3);
    MDDBaseType* mddBaseType = static_cast<MDDBaseType*>(mddDimensionType);

    TypeFactory::addTempType(mddBaseType);

    std::unique_ptr<MDDObj> resultMDD;
    resultMDD.reset( new MDDObj(mddBaseType, *resultDom, op->getNullValues()) );
    
    // here, we apply the resultMask to each tile to generate the output tiles.
    
    //iterate over the source tiles
    std::unique_ptr<std::vector<boost::shared_ptr<Tile>>> allTiles;
    allTiles.reset(op->intersect(*resultDom));
    try
    {        
        //data type size
        size_t typeSize = (*(allTiles->begin()))->getType()->getSize();
        
        //check for nullValues for initializing the result tiles.
        std::shared_ptr<r_Minterval> firstNullValueDomain;
        firstNullValueDomain.reset( op->getNullValues() );
        
        //pointer to the nullValue values
        std::unique_ptr<char> nullValue;
        nullValue.reset( new char[typeSize] );
        
        if(firstNullValueDomain)
        {
            //assign value of null value data.
            memcpy(nullValue.get(), op->pointQuery(firstNullValueDomain->get_origin()), typeSize);
        }
        else
        {
            memset(nullValue.get(), 0, typeSize);
        }
        
        
        for (auto tileIt = allTiles->begin(); tileIt != allTiles->end(); tileIt++)
        {
            //domain of source tile
            const r_Minterval& srcTileDom = (*tileIt)->getDomain();
            //data pointer of source tile
            const char* sourceDataPtr = (*tileIt)->getContents();
            
            //construct result tile
            boost::shared_ptr<Tile> resTile;
            r_Minterval intersectDom = resultDom->create_intersection(srcTileDom);
            resTile.reset( new Tile(intersectDom, op->getCellType()) );
            
            const char* resDataPtr = resTile->getContents();
            
            //construct iterators for filling data in result tile
            r_Miter resTileMaskIterator(&intersectDom, resultDom.get(), sizeof(char), resultMaskPtr);
            r_Miter sourceTileIterator(&intersectDom, &srcTileDom, typeSize, sourceDataPtr);
            r_Miter resTileIterator(&intersectDom, &intersectDom, typeSize, resDataPtr);
            
            while(!resTileMaskIterator.isDone())
            {
                //step to next cell for resTileMaskIterator
                if(*resTileMaskIterator.nextCell() < 2)
                {
                    //step to next cell for resTileIterator and sourceTileIterator, and copy data
                    memcpy(resTileIterator.nextCell(), sourceTileIterator.nextCell(), typeSize);
                }
                else
                {
                    //step to next cell for resTileIterator and sourceTileIterator
                    //and assign a nullValue to the current result tile cell.
                    sourceTileIterator.nextCell();
                    memcpy(resTileIterator.nextCell(), nullValue.get(), typeSize);
                }
            }
            
            // insert Tile in result mdd
            resultMDD->insertTile(resTile);
        }
    }
    catch (r_Error &err)
    {
        LFATAL << "QtClipping::extractMultipolygon caught " << err.get_errorno() << " " << err.what();
        parseInfo.setErrorNo(err.get_errorno());
        throw parseInfo;
    }
    catch (int err)
    {
        LFATAL << "QtClipping::extractMultipolygon caught errno error (" << err << ") in qtclipping";
        parseInfo.setErrorNo(err);
        throw parseInfo;
    }

    
    return resultMDD.release();
}

QtData*
QtMulticlipping::evaluate(QtDataList* inputList)
{
    QtData* returnValue = NULL;
    
    // get the operand
    //TODO(BBELL): FIXME -- why does this complain???
    QtData* operand = input->evaluate( inputList );
    
    // evaluate sub-nodes to obtain operand values
    if (operand)
    {

        // source mdd object in the 1st operand
        QtMDD* qtMDDObj = static_cast<QtMDD*>(operand);        
        MDDObj* currentMDDObj = qtMDDObj->getMDDObject();
        
        r_Dimension opDim = qtMDDObj->getLoadDomain().dimension();
        //verify dimensionality of the MShapeData and the point-dimensionality.
        for(auto iter = mshapeList.begin(); iter != mshapeList.end(); iter++)
        {
            if (opDim != (*iter)->getDimension() && opDim != (*iter)->getPointDimension() )
            {
                LFATAL << "Error: QtMulticlipping::evaluate() - Dimension of the polygon vertices is bigger than the domain's dimension.";
                parseInfo.setErrorNo(407);
                throw parseInfo;
            }
        }
        returnValue = computeOp(qtMDDObj);
        
        //delete the old operands
        if (operand)
        {
            operand->deleteRef();
        }
    }

    return returnValue;
}

QtData*
QtMulticlipping::computeOp(QtMDD* operand)
{
    // get the MDD object
    MDDObj* op = operand->getMDDObject();
    //  get the source domain
    r_Minterval areaOp = operand->getLoadDomain();
         
    //extract the multipolygon data as a transient MDD object for the result.
    std::unique_ptr<MDDObj> resultMDD;
    resultMDD.reset( extractMultipolygon(areaOp, op) );
    
    if(!resultMDD)
    {
        parseInfo.setErrorNo(ALLPOLYGONSOUTSIDEMDDOBJ);
        throw parseInfo;
    }
    
    QtData* returnValue = new QtMDD( resultMDD.release() );
    
    return returnValue;
}

const QtTypeElement& QtMulticlipping::checkType(QtTypeTuple *typeTuple)
{
    dataStreamType.setDataType(QT_TYPE_UNKNOWN);

    // check operand branches
    if (input)
    {

        // get input types
        const QtTypeElement &inputType = input->checkType(typeTuple);

        if (inputType.getDataType() != QT_MDD)
        {
            LFATAL << "Error: QtMulticlipping::checkType() - first operand must be of type MDD.";
            parseInfo.setErrorNo(MDDARGREQUIRED);
            throw parseInfo;
        }

        dataStreamType = inputType;
    }
    else
    {
        LERROR << "Error: QtMulticlipping::checkType() - operand branch not a valid MDD.";
    }

    return dataStreamType;
}