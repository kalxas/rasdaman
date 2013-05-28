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

static const char rcsid[] = "@(#)qlparser, QtUpdate: $Header: /home/rasdev/CVS-repository/rasdaman/qlparser/qtupdate.cc,v 1.28 2003/12/27 20:51:28 rasdev Exp $";

#include "config.h"
#include "raslib/dlist.hh"

#include "qlparser/qtupdate.hh"
#include "qlparser/qtdata.hh"
#include "qlparser/qtmdd.hh"
#include "qlparser/qtmintervaldata.hh"

#include "tilemgr/tile.hh"
#include "tilemgr/tiler.hh"

#include "mddmgr/mddobj.hh"
#include "debug/debug-srv.hh"

#include <iostream>
#include <memory>


const QtNode::QtNodeType QtUpdate::nodeType = QtNode::QT_UPDATE;



QtUpdate::QtUpdate( QtOperation* initUpdateTarget, QtOperation* initUpdateDomain, QtOperation* initUpdateSource )
    : QtExecute(), input(NULL),
      updateTarget( initUpdateTarget ),
      updateDomain( initUpdateDomain ),
      updateSource( initUpdateSource )
{
    if( updateTarget ) updateTarget->setParent( this );
    if( updateDomain ) updateDomain->setParent( this );
    if( updateSource ) updateSource->setParent( this );
}



QtUpdate::~QtUpdate()
{
    if( updateTarget )
    {
        delete updateTarget;
        updateTarget=NULL;
    }

    if( updateDomain )
    {
        delete updateDomain;
        updateDomain=NULL;
    }

    if( updateSource )
    {
        delete updateSource;
        updateSource=NULL;
    }

    if( input )
    {
        delete input;
        input=NULL;
    }
}



int
QtUpdate::evaluate()
{
    RMDBCLASS( "QtUpdate", "evaluate()", "qlparser", __FILE__, __LINE__ )
    startTimer("QtUpdate");

    // Test, if all necessary operands are available.
    if( updateTarget && updateSource && input )
    {
        QtNode::QtDataList* nextTupel;

        // open input stream
        try
        {
            input->open();
        }
        catch( ... )
        {
            input->close();
            throw;
        }

        try
        {
            while( (nextTupel = input->next()) )
            {
                evaluateTupel(nextTupel);

                // delete tupel vector received by next()
                for (vector<QtData*>::iterator dataIter = nextTupel->begin();
                        dataIter != nextTupel->end(); dataIter++)
                    if (*dataIter) (*dataIter)->deleteRef();
                delete nextTupel;
                nextTupel = NULL;
            } // while
        }
        catch( ... )
        {
            input->close();
            throw;
        }

        input->close();
    }
    else
        RMInit::logOut << "Error: QtUpdate::evaluate() - at least one operand branch is invalid." << endl;
    
    stopTimer();

    return 0;
}

void
QtUpdate::evaluateTupel(QtNode::QtDataList* nextTupel)
{
    // mdd object to be updated
    QtData* target = updateTarget->evaluate(nextTupel);
    
    // mdd object that is the source of the update
    QtData* source = updateSource->evaluate(nextTupel);
    
    // check if target and source are valid
    if (!checkOperands(nextTupel, target, source))
    {
        return;
    }

    QtMDD* targetMDD = (QtMDD*) target;
    QtMDD* sourceMDD = (QtMDD*) source;

    MDDObj* targetObj = targetMDD->getMDDObject();
    MDDObj* sourceObj = sourceMDD->getMDDObject();

    // test, if target is a persistent object
    if (!targetObj->isPersistent())
    {
        RMInit::logOut << "Error: QtUpdate::evaluate() - result of target expression must be an assignable value (l-value)." << endl;
        throwError(nextTupel, target, source, 954);
    }

    // get optional domain
    QtData* targetDomainData = NULL;
    r_Minterval targetDomain;
    r_Minterval sourceMDDDomain(sourceMDD->getLoadDomain());

    if (targetObj->getStorageLayout())
    {
        targetObj->getStorageLayout()->setCellSize(targetObj->getCellType()->getSize());
    }

    if (updateDomain)
    {
        targetDomainData = updateDomain->evaluate(nextTupel);

        if (targetDomainData)
            targetDomain = ((QtMintervalData*) targetDomainData)->getMintervalData();
    }

    RMDBGIF(1, RMDebug::module_qlparser, "QtUpdate", \
                         if (targetDomainData) \
                            RMInit::logOut << endl << "  target MDD, domain " << targetDomain << endl; \
                         else  \
                            RMInit::logOut << endl << "  target MDD" << endl; \
                         targetMDD->printStatus(RMInit::logOut); \
                         RMInit::logOut << endl << "  source MDD, domain " << sourceMDDDomain << endl; \
                         sourceMDD->printStatus(RMInit::logOut); \
                         RMInit::logOut << endl; \
           );

    // 1st update strategy:
    //
    // 1. All cell values of the source domain which are already defined are
    //    updated with the corresponding new values.
    // 2. If a source tile does not intersect with any tile of the target
    //    object, it is inserted.

    // In case of update domain existence, test for compatibility.
    checkDomainCompatibility(nextTupel, target, source, targetDomainData, targetMDD, sourceMDD);

    // compute source MDD domain taking into account update domain
    if (targetDomainData)
    {
        const vector<bool>* trimFlags = ((QtMintervalData*) targetDomainData)->getTrimFlags();
        r_Minterval newSourceMDDDomain(targetMDD->getLoadDomain().dimension());

        for (int i = 0, j = 0; i < trimFlags->size(); i++)
            if ((*trimFlags)[i])
                newSourceMDDDomain << sourceMDDDomain[j++];
            else
                newSourceMDDDomain << targetDomain[i];

        sourceMDDDomain = newSourceMDDDomain;

        TALK("  new source update domain " << sourceMDDDomain);
    }
    
    // check if you may update that are ( you should not go out of bounds for mdddomaintype )
    if (!targetObj->getMDDBaseType()->compatibleWithDomain(&sourceMDDDomain))
    {
        RMInit::logOut << "Error: QtUpdate::evaluate() - The update domain is outside the allowed domain of the target mdd." << endl;
        throwError(nextTupel, target, source, 953, targetDomainData);
    }
    
    //
    // get all source tiles
    //
    vector<Tile*>* sourceTiles = sourceObj->getTiles();
    
    RMDBGIF(1, RMDebug::module_qlparser, "QtUpdate", \
                        if (sourceTiles) \
                        { \
                            TALK("  there are " << sourceTiles->size() << " source tiles") \
                            vector<Tile*>::iterator sourceTilesIterator; \
                            for (sourceTilesIterator = sourceTiles->begin(); sourceTilesIterator != sourceTiles->end(); sourceTilesIterator++) \
                                TALK("    tile domain: " << (*sourceTilesIterator)->getDomain()) \
                        } \
                        else \
                            TALK("  there are no source tiles") \
    );

    //
    // get all target tiles in the relevant area
    //
    unsigned long targetTileArea = 0;
    unsigned long sourceTileArea = 0;
    unsigned long targetTileDomain = 0;
    unsigned long updatedArea = 0;
    bool computed = false;
    vector<r_Minterval> insertedDomains;
    vector<r_Minterval>::iterator domIt;
    vector<r_Minterval>::iterator intervalIt;
    vector<r_Minterval> sourceDomains;
    vector<r_Minterval> targetDomains;
    vector<Tile*>::iterator retvalIt;
    vector<Tile*>::iterator sourceIt;
    vector<Tile*>::iterator targetIt;
    sourceIt = sourceTiles->begin();
    sourceDomains.push_back(sourceMDDDomain);
    vector<Tile*>* targetTiles = NULL;
    targetTiles = targetObj->intersect(sourceMDDDomain);
    if (!targetTiles)
    {
        targetTiles = new vector<Tile*>;
        TALK("  there are no target tiles");
    }
    else
    {
        TALK("  there are " << targetTiles->size() << " target tiles");
        vector<Tile*>::iterator targetTilesIterator;
        for (targetTilesIterator = targetTiles->begin(); targetTilesIterator != targetTiles->end(); targetTilesIterator++)
        {
            TALK("    tile domain: " << (*targetTilesIterator)->getDomain());
            targetDomains.push_back((*targetTilesIterator)->getDomain());
        }
    }

    //
    // iterate over source tiles
    //
    //
    r_Tiler t(sourceDomains, targetDomains);
    t.split();
    t.removeDoubleDomains();
    t.removeCoveredDomains();
    t.mergeDomains();
    vector<Tile*> retval = t.generateTiles(*sourceTiles);
    for (retvalIt = retval.begin(); retvalIt != retval.end(); retvalIt++)
    {
        targetTiles->push_back(*retvalIt);
    }
    
    //this lives here because we don't want memory leaks because of exceptions
    //of course we seldom use this operation ( as we use copy tile most of the time )
    UnaryOp* tempOp = NULL;
    if (sourceIt != sourceTiles->end())
        tempOp = Ops::getUnaryOp(Ops::OP_IDENTITY, targetObj->getCellType(), (*sourceIt)->getType(), 0, 0);
    std::auto_ptr<UnaryOp> identityOp(tempOp);
    
    const vector<bool>* trimFlags = NULL;
    
    //
    // iterate over source tiles
    //
    TALK("");
    for (; sourceIt != sourceTiles->end(); sourceIt++)
    {
        // calculate relevant area of source tile
        r_Minterval sourceTileDomain = (*sourceIt)->getDomain().create_intersection(sourceMDD->getLoadDomain());
        TALK("original source tile domain " << sourceTileDomain);

        // compute update source tile domain taking into account update domain
        r_Minterval updateSourceTileDomain;
        if (targetDomainData)
        {
            updateSourceTileDomain = r_Minterval(targetMDD->getLoadDomain().dimension());
            trimFlags = ((QtMintervalData*) targetDomainData)->getTrimFlags();

            for (int i = 0, j = 0; i < trimFlags->size(); i++)
                if ((*trimFlags)[i])
                    updateSourceTileDomain << sourceTileDomain[j++];
                else
                    updateSourceTileDomain << targetDomain[i];
        }
        else
            updateSourceTileDomain = sourceTileDomain;
        TALK("update source tile domain " << updateSourceTileDomain);

        // calculate number of cells in this area
        sourceTileArea = sourceTileArea + sourceTileDomain.cell_count();

        bool intersection = false;

        // see if there are existing tiles to be updated
        for (targetIt = targetTiles->begin(); targetIt != targetTiles->end(); targetIt++)
        {
            r_Minterval targetTileDomain = (*targetIt)->getDomain();
            
            if (!computed)
            {
                targetTileArea = targetTileArea + sourceMDDDomain.create_intersection(targetTileDomain).cell_count();
            }
            
            // if tiles are intersecting
            if (updateSourceTileDomain.intersects_with(targetTileDomain))
            {
                TALK("  target tile domain " << targetTileDomain);
                intersection = true;

                // get intersecting updateSourceTileDomain
                r_Minterval intersectUpdateSourceTileDomain = updateSourceTileDomain.create_intersection(targetTileDomain);
                TALK("    source and target tiles intersect: " << intersectUpdateSourceTileDomain);

                // compute corresponding sourceTileDomain
                r_Minterval intersectSourceTileDomain = intersectUpdateSourceTileDomain;
                if (targetDomainData)
                {
                    const vector<bool>* trimFlags = ((QtMintervalData*) targetDomainData)->getTrimFlags();

                    for (int i = 0, j = 0; i < trimFlags->size(); i++)
                        if (!((*trimFlags)[i]))
                            intersectSourceTileDomain.delete_dimension(j);
                        else
                            j++;
                }

                TALK("    intersection after removing slices: " << intersectSourceTileDomain);
                
                TALK("    updating target tile with source tile data...");
                if (intersectUpdateSourceTileDomain.dimension() == intersectSourceTileDomain.dimension())
                    (*targetIt)->copyTile(intersectUpdateSourceTileDomain, *sourceIt, intersectSourceTileDomain);
                else
                    (*targetIt)->execUnaryOp(&(*identityOp), intersectUpdateSourceTileDomain, *sourceIt, intersectSourceTileDomain);
                updatedArea = updatedArea + intersectUpdateSourceTileDomain.cell_count();
            }
        }
        computed = true;
        
        // insert the tile
        if (!intersection)
        {
            // Create a new persistent tile, copy the transient data,
            // and insert it into the target mdd object.
            TALK("  no intersection with target tiles found, inserting new tile");
            Tile* newPersTile = new Tile(updateSourceTileDomain, targetObj->getCellType(), (*sourceIt)->getDataFormat());
            if (updateSourceTileDomain.dimension() == sourceTileDomain.dimension())
                newPersTile->copyTile(updateSourceTileDomain, *sourceIt, sourceTileDomain);
            else
                newPersTile->execUnaryOp(&(*identityOp), updateSourceTileDomain, *sourceIt, sourceTileDomain);
            
            TALK("  update domains: target tile " << newPersTile->getDomain() 
                    << " update target at " << updateSourceTileDomain << 
                    ", source tile " << (*sourceIt)->getDomain() 
                    << " update with data at " << sourceTileDomain)

            targetObj->insertTile(newPersTile);
            updatedArea = updatedArea + updateSourceTileDomain.cell_count();
            insertedDomains.push_back((*sourceIt)->getDomain());
        }
    }//for is done

    for (retvalIt = retval.begin(); retvalIt != retval.end(); retvalIt++)
    {
        targetObj->insertTile((Tile*) (*retvalIt));
    }

    // delete tile vectors
    delete sourceTiles;
    sourceTiles = NULL;
    delete targetTiles;
    targetTiles = NULL;

    // delete optional operand
    if (targetDomainData) targetDomainData->deleteRef();

    // delete the operands
    if (target) target->deleteRef();
    if (source) source->deleteRef();
}

bool
QtUpdate::checkOperands(QtNode::QtDataList* nextTupel, QtData* target, QtData* source)
{
    // Test, if the operands are valid.
    if (target && source)
    {
        // check update target
        if (target->getDataType() != QT_MDD)
        {
            RMInit::logOut << "Error: QtUpdate::evaluate() - update target must be an iterator variable." << endl;
            throwError(nextTupel, target, source, 950);
        }

        // check update source
        if (source->getDataType() != QT_MDD)
        {
            RMInit::logOut << "Error: QtUpdate::evaluate() - update source must be an expression resulting in an MDD" << endl;
            throwError(nextTupel, target, source, 951);
        }
    }
    else {
        RMInit::logOut << "Error: QtUpdate::evaluate() - target or source is not provided." << endl;

        // delete the operands
        if (target) target->deleteRef();
        if (source) source->deleteRef();
        
        return false;
    }
    return true;
}


void
QtUpdate::throwError(QtNode::QtDataList* nextTupel, QtData* target, QtData* source, int errorNumber, QtData* domainData)
{

    // delete tupel vector received by next()
    for (vector<QtData*>::iterator dataIter = nextTupel->begin();
            dataIter != nextTupel->end(); dataIter++)
        if (*dataIter) (*dataIter)->deleteRef();
    delete nextTupel;
    nextTupel = NULL;

    // delete the operands
    if (target) target->deleteRef();
    if (source) source->deleteRef();
    if (domainData) domainData->deleteRef();

    parseInfo.setErrorNo(errorNumber);
    throw parseInfo;
}


void
QtUpdate::checkDomainCompatibility(QtNode::QtDataList* nextTupel, QtData* target,
                                   QtData* source, QtData* domainData,
                                   QtMDD* targetMDD, QtMDD* sourceMDD)
{
    // In case of update domain existence, test for compatibility.
    if (domainData)
    {
        r_Minterval domain = ((QtMintervalData*) domainData)->getMintervalData();
        // Dimensionality of the udate domain specification has to be equal to
        // the target MDD dimensionality.
        if (domain.dimension() != targetMDD->getLoadDomain().dimension())
        {
            RMInit::logOut << "Error: QtUpdate::evaluate() - Update domain dimensionality must match target MDD dimensionaltiy." << endl;
            throwError(nextTupel, target, source, 963, domainData);
        }

        // The number of interval dimension of the update domain has to be
        // equal to the number of dimensions of the source domain.
        int updateIntervals = 0;
        const vector<bool>* trimFlags = ((QtMintervalData*) domainData)->getTrimFlags();
        for (int i = 0; i < trimFlags->size(); i++)
            if ((*trimFlags)[i])
                updateIntervals++;

        if (updateIntervals != sourceMDD->getLoadDomain().dimension())
        {
            RMInit::logOut << "Error: QtUpdate::evaluate() - Number of update intervals must match source dimensionality." << endl;
            throwError(nextTupel, target, source, 962, domainData);
        }

        // Before: Warning: Fixed bounds in update domain specifications are ignored.

        // After: Actually we should be stricter here and respect the documentation:
        // throw an exception if the source domain isn't within the target domain -- DM 2012-mar-12

        // copied from r_Minterval::covers(..), we have to rule out slices here
        const r_Minterval& sourceDomain = sourceMDD->getLoadDomain();
        int j = 0;
        for (r_Dimension i = 0; i < domain.dimension(); i++)
        {
            if ((*trimFlags)[i]) // consider only trims
            {
                if ((domain[i].is_low_fixed() && (!(sourceDomain[j].is_low_fixed()) || domain[i].low() > sourceDomain[j].low())) ||
                        (domain[i].is_high_fixed() && (!(sourceDomain[j].is_high_fixed()) || domain[i].high() < sourceDomain[j].high())))
                {
                    RMInit::logOut << "Error: QtUpdate::evaluate() - source domain " <<
                            sourceDomain << " isn't within the target domain " << domain << endl;

                    // delete tupel vector received by next()
                    for (vector<QtData*>::iterator dataIter = nextTupel->begin();
                            dataIter != nextTupel->end(); dataIter++)
                        if (*dataIter) (*dataIter)->deleteRef();
                    delete nextTupel;
                    nextTupel = NULL;

                    // delete the operands
                    if (target) target->deleteRef();
                    if (domainData) domainData->deleteRef();
                    if (source) source->deleteRef();

                    parseInfo.setErrorNo(967);
                    throw parseInfo;
                }
                ++j;
            }
        }

    }
}


QtNode::QtNodeList*
QtUpdate::getChilds( QtChildType flag )
{
    RMDBCLASS( "QtUpdate", "getChilds( QtChildType flag )", "qlparser", __FILE__, __LINE__ )

    QtNodeList* resultList=NULL;

    // allocate resultList
    resultList = new QtNodeList();

    if( flag == QT_LEAF_NODES || flag == QT_ALL_NODES )
    {
        if( input )
        {
            QtNodeList* subList = input->getChilds( flag );

            // remove all elements in subList and insert them at the beginning in resultList
            resultList->splice( resultList->begin(), *subList );

            // delete temporary subList
            delete subList;
            subList=NULL;
        }

        if( updateTarget )
        {
            QtNodeList* subList = updateTarget->getChilds( flag );

            // remove all elements in subList and insert them at the beginning in resultList
            resultList->splice( resultList->begin(), *subList );

            // delete temporary subList
            delete subList;
            subList=NULL;
        }

        if( updateDomain )
        {
            QtNodeList* subList = updateDomain->getChilds( flag );

            // remove all elements in subList and insert them at the beginning in resultList
            resultList->splice( resultList->begin(), *subList );

            // delete temporary subList
            delete subList;
            subList=NULL;
        }

        if( updateSource )
        {
            QtNodeList* subList = updateSource->getChilds( flag );

            // remove all elements in subList and insert them at the beginning in resultList
            resultList->splice( resultList->begin(), *subList );

            // delete temporary subList
            delete subList;
            subList=NULL;
        }
    };

    // add the nodes of the current level
    if( flag == QT_DIRECT_CHILDS || flag == QT_ALL_NODES )
    {
        resultList->push_back( input );
        resultList->push_back( updateTarget );
        resultList->push_back( updateDomain );
        resultList->push_back( updateSource );
    }

    return resultList;
}



void
QtUpdate::printTree( int tab, ostream& s, QtChildType mode )
{
    s << SPACE_STR(tab).c_str() << "QtUpdate Object" << getEvaluationTime() << endl;

    if( mode != QtNode::QT_DIRECT_CHILDS )
    {
        if( updateTarget )
        {
            s << SPACE_STR(tab).c_str() << "target: " << endl;
            updateTarget->printTree( tab + 2, s );

            if( updateDomain )
            {
                s << SPACE_STR(tab+2).c_str() << "domain: " << endl;
                updateDomain->printTree( tab + 4, s );
            }
            else
                s << SPACE_STR(tab+2).c_str() << "no domain" << endl;
        }
        else
            s << SPACE_STR(tab).c_str() << "no target" << endl;

        if( updateSource )
        {
            s << SPACE_STR(tab).c_str() << "source: " << endl;
            updateSource->printTree( tab + 2, s );
        }
        else
            s << SPACE_STR(tab).c_str() << "no source" << endl;

        if( input )
        {
            s << SPACE_STR(tab).c_str() << "input: " << endl;
            input->printTree( tab+2, s, mode );
        }
        else
            s << SPACE_STR(tab).c_str() << "no input" << endl;

        s << endl;
    }
}



void
QtUpdate::printAlgebraicExpression( ostream& s )
{
    s << "update<" << std::flush;

    if( updateTarget )
        updateTarget->printAlgebraicExpression( s );
    else
        s << "<no target>";

    if( updateDomain )
        updateDomain->printAlgebraicExpression( s );

    s << "," << std::flush;

    if( updateSource )
        updateSource->printAlgebraicExpression( s );
    else
        s << "<no source>";

    s << ">" << std::flush;

    if( input )
    {
        s << "( ";
        input->printAlgebraicExpression( s );
        s << " )";
    }
    else
        s << "(<no input>)" << std::flush;
}



void
QtUpdate::setStreamInput( QtONCStream* newInput )
{
    input = newInput;
    input->setParent( this );
};

QtOperation*
QtUpdate::getUpdateTarget()
{
    return updateTarget;
}

QtOperation*
QtUpdate::getUpdateDomain()
{
    return updateDomain;
}

QtOperation*
QtUpdate::getUpdateSource()
{
    return updateSource;
}

QtONCStream*
QtUpdate::getInput()
{
    return input;
}



void
QtUpdate::checkType()
{
    RMDBCLASS( "QtUpdate", "checkType()", "qlparser", __FILE__, __LINE__ )

    // check operand branches
    if( updateTarget && updateSource && input )
    {

        // get input type
        QtTypeTuple inputType  = input->checkType();

        // check target
        const QtTypeElement& targetType = updateTarget->checkType( &inputType );
        if( targetType.getDataType() != QT_MDD )
        {
            RMInit::logOut << "Error: QtUpdate::checkType() - update target must be an iterator variable." << endl;
            parseInfo.setErrorNo(950);
            throw parseInfo;
        }

        // check domain
        if( updateDomain )
        {
            const QtTypeElement& domainType = updateDomain->checkType( &inputType );
            if( domainType.getDataType() != QT_MINTERVAL )
            {
                RMInit::logOut << "Error: QtUpdate::checkType() - update domain must be of type Minterval." << endl;
                parseInfo.setErrorNo(961);
                throw parseInfo;
            }
        }

        // check source
        const QtTypeElement& sourceType = updateSource->checkType( &inputType );
        if( sourceType.getDataType() != QT_MDD )
        {
            RMInit::logOut << "Error: QtUpdate::checkType() - update source must be an expression resulting in an MDD." << endl;
            parseInfo.setErrorNo(951);
            throw parseInfo;
        }

        // test for compatible base types
        bool compatible = false;
        const BaseType* type1 = ((MDDBaseType*)(targetType.getType()))->getBaseType();
        const BaseType* type2 = ((MDDBaseType*)(sourceType.getType()))->getBaseType();

        // substituted the string comparison as it fails for composite types:
        // the update base type is usually "struct { char 0, char 1,...}"
        // while the target type is "struct { char red, char green, ...}"
        // Now we rather use the compatibleWith method which handles such cases -- DM 2012-mar-07

        // If the source MDD comes from an inv_* function we consider it's compatible
        // at this point, as we can't determine the type until the data is decoded -- DM 2012-mar-12

        QtNodeList* convChildren = updateSource->getChild(QT_CONVERSION, QT_ALL_NODES);
        compatible = updateSource->getNodeType() == QT_CONVERSION ||
                     (convChildren != NULL && !convChildren->empty()) ||
                     type1->compatibleWith(type2); //(strcmp(type1, type2) == 0);

        if( !compatible )
        {
            RMInit::logOut << "Error: QtUpdate::checkType() - update base type does not match mdd base type." << endl;
            parseInfo.setErrorNo(952);
            throw parseInfo;
        }
    }
    else
        RMInit::logOut << "Error: QtUpdate::checkType() - operand branch invalid." << endl;
}





