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
/**
 * SOURCE: persmddobj.cc
 *
 * MODULE: cachetamgr
 * CLASS:   MDDObj
 *
 * COMMENTS:
 * none
 *
*/

#include "config.h"
#include "mymalloc/mymalloc.h"

static const char rcsid[] = "@(#)persmddobj, PersMDDObj: $Id: mddobj.cc,v 1.26 2005/07/06 22:43:20 rasdev Exp $";

#include <iostream>
#include <stdlib.h>
#include <cstring>

#include "mddobj.hh"
#include "relmddif/dbmddobj.hh"
#include "relindexif/indexid.hh"
#include "reladminif/eoid.hh"

#include "tilemgr/tile.hh"
#include "relcatalogif/mdddomaintype.hh"
#include "raslib/mddtypes.hh"
#include "indexmgr/mddobjix.hh"
#include <easylogging++.h>

using boost::shared_ptr;

const r_Minterval&
MDDObj::checkStorage(const r_Minterval& domain2) throw (r_Error)
{
    r_Minterval domain(domain2.dimension());
    if (myStorageLayout->getIndexType() == r_Reg_Computed_Index)
    {
        if (myStorageLayout->getTilingScheme() != r_RegularTiling)
        {
            LFATAL << "MDDObj::checkStorage(" << domain2 << ") the rc index needs a regular tiling defined";
            throw r_Error(RCINDEXWITHOUTREGULARTILING);
        }
        r_Dimension dim = domain2.dimension();
        // make sure the tileConfig is fixed
        r_Minterval tileConfig = myStorageLayout->getTileConfiguration();
        myStorageLayout->setTileConfiguration(tileConfig);
        r_Point mddDomainExtent = domain2.get_extent();
        r_Point tileConfigExtent = tileConfig.get_extent();
        for (r_Dimension i = 0; i < dim; i++)
        {
            if (!domain2[i].is_high_fixed() || !domain2[i].is_low_fixed() || !tileConfig[i].is_high_fixed() || !tileConfig[i].is_low_fixed())
            {
                LFATAL << "MDDObj::checkStorage(" << domain2 << ") the rc index needs a domain and tile configuration with fixed domains in all dimensions.  Dimension " << i << " seems not to be fixed.";
                throw r_Error(RCINDEXWITHINCOMPATIBLEMARRAYTYPE);
            }
            if (mddDomainExtent[i] % tileConfigExtent[i] != 0)
            {
                LFATAL << "MDDObj::checkStorage(" << domain2 << ") the tile configuration (" << tileConfig << ") does not fit the domain of the marray (" << domain << ").";
                throw r_Error(TILECONFIGMARRAYINCOMPATIBLE);
            }
        }
    }
    return domain2;
}

MDDObj::MDDObj(const MDDBaseType* mddType, const r_Minterval& domain)
    :   NullValuesHandler(),
        myDBMDDObj(),
        myMDDIndex(NULL),
        myStorageLayout(NULL)
{
    if (!mddType)
    {
        LFATAL << "MDD type is NULL.  Please report query or raslib program to Customer Support.";
        throw r_Error(MDDTYPE_NULL);
    }

    LTRACE << "MDDObj(" << mddType->getName() << ", " << domain << ") " << (r_Ptr)this;
    myStorageLayout = new StorageLayout(r_Directory_Index);
    myStorageLayout->setCellSize(static_cast<int>(mddType->getBaseType()->getSize()));
    myMDDIndex = new MDDObjIx(*myStorageLayout, domain, mddType->getBaseType(), false);
    myDBMDDObj = new DBMDDObj(mddType, domain, myMDDIndex->getDBMDDObjIxId(), myStorageLayout->getDBStorageLayout());
}

MDDObj::MDDObj(const MDDBaseType* mddType, const r_Minterval& domain, r_Minterval* newNullValues)
    :   NullValuesHandler(),
        myDBMDDObj(),
        myMDDIndex(NULL),
        myStorageLayout(NULL)
{
    if (!mddType)
    {
        LFATAL << "MDD type is NULL.  Please report query or raslib program to Customer Support.";
        throw r_Error(MDDTYPE_NULL);
    }

    LTRACE << "MDDObj(" << mddType->getName() << ", " << domain << ") " << (r_Ptr)this;
    setNullValues(newNullValues);
    myStorageLayout = new StorageLayout(r_Directory_Index);
    myStorageLayout->setCellSize(static_cast<int>(mddType->getBaseType()->getSize()));
    myMDDIndex = new MDDObjIx(*myStorageLayout, domain, mddType->getBaseType(), false);
    myDBMDDObj = new DBMDDObj(mddType, domain, myMDDIndex->getDBMDDObjIxId(), myStorageLayout->getDBStorageLayout());
}

MDDObj::MDDObj(const MDDBaseType* mddType, const r_Minterval& domain, const OId& newOId, const StorageLayout& ms) throw (r_Error)
    :   NullValuesHandler(),
        myDBMDDObj(),
        myMDDIndex(NULL),
        myStorageLayout(NULL)
{
    if (!mddType)
    {
        LFATAL << "MDD type is NULL.  Please report query or raslib program to Customer Support.";
        throw r_Error(MDDTYPE_NULL);
    }

    LTRACE << "MDDObj(" << mddType->getName() << ", " << domain << ", " << newOId << ", " << ms.getDBStorageLayout().getOId() << ") " << (r_Ptr)this;
    myStorageLayout = new StorageLayout(ms);
    myStorageLayout->setCellSize(static_cast<int>(mddType->getBaseType()->getSize()));
    myMDDIndex = new MDDObjIx(*myStorageLayout, checkStorage(domain), mddType->getBaseType());
    myDBMDDObj = new DBMDDObj(mddType, domain, myMDDIndex->getDBMDDObjIxId(), ms.getDBStorageLayout(), newOId);
}

MDDObj::MDDObj(const MDDBaseType* mddType, const r_Minterval& domain, const OId& newOId) throw (r_Error)
    : myDBMDDObj(),
      myMDDIndex(NULL),
      myStorageLayout(NULL)
{
    if (!mddType)
    {
        LFATAL << "MDD type is NULL.  Please report query or raslib program to Customer Support.";
        throw r_Error(MDDTYPE_NULL);
    }
    myStorageLayout = new StorageLayout();
    myMDDIndex = new MDDObjIx(*myStorageLayout, checkStorage(domain), mddType->getBaseType());
    LTRACE << "MDDObj(" << mddType->getName() << ", " << domain << ", " << newOId;
    myDBMDDObj = new DBMDDObj(mddType, domain, myMDDIndex->getDBMDDObjIxId(), myStorageLayout->getDBStorageLayout(), newOId);
}



MDDObj::MDDObj(const DBMDDObjId& dbmddobj) throw(r_Error)
    :   NullValuesHandler(),
        myDBMDDObj(dbmddobj),
        myMDDIndex(NULL),
        myStorageLayout(NULL)
{
    LTRACE << "MDDObj(DBRef " << dbmddobj.getOId() << ") " << (r_Ptr)this;
    myStorageLayout = new StorageLayout(myDBMDDObj->getDBStorageLayout());
    myStorageLayout->setCellSize(static_cast<int>(myDBMDDObj->getCellType()->getSize()));
    myMDDIndex = new MDDObjIx(myDBMDDObj->getDBIndexDS(), *myStorageLayout, myDBMDDObj->getMDDBaseType()->getBaseType());
}

MDDObj::MDDObj(const OId& givenOId) throw(r_Error)
    :   NullValuesHandler(),
        myDBMDDObj(OId()),
        myMDDIndex(NULL),
        myStorageLayout(NULL)
{
    LTRACE << "MDDObj(" << givenOId << ") " << (r_Ptr)this;
    myDBMDDObj = DBMDDObjId(givenOId);
    myStorageLayout = new StorageLayout(myDBMDDObj->getDBStorageLayout());
    myStorageLayout->setCellSize(static_cast<int>(myDBMDDObj->getCellType()->getSize()));
    myMDDIndex = new MDDObjIx(myDBMDDObj->getDBIndexDS(), *myStorageLayout, myDBMDDObj->getMDDBaseType()->getBaseType());
}

MDDObj::MDDObj(const MDDBaseType* mddType, const r_Minterval& domain, const StorageLayout& ms)
    :   NullValuesHandler(),
        myDBMDDObj(OId()),
        myMDDIndex(NULL),
        myStorageLayout(NULL)
{
    LTRACE << "MDDObj(" << mddType->getName() << ", " << domain << ", " << ms.getDBStorageLayout().getOId() << ") " << (r_Ptr)this;
    if (!mddType)
    {
        LFATAL << "MDD type is NULL.  Please report query or raslib program to Customer Support.";
        throw r_Error(MDDTYPE_NULL);
    }
    myStorageLayout = new StorageLayout(ms);
    myStorageLayout->setCellSize(static_cast<int>(mddType->getBaseType()->getSize()));
    myMDDIndex = new MDDObjIx(*myStorageLayout, checkStorage(domain), mddType->getBaseType());
    myDBMDDObj = new DBMDDObj(mddType, domain, myMDDIndex->getDBMDDObjIxId(), ms.getDBStorageLayout());
    myDBMDDObj->setPersistent();
}

void
MDDObj::insertTile(Tile* newTile)
{
    insertTile(shared_ptr<Tile>(newTile));
}

/*
insert tile:
 tiles may have to be retiled.
 the storage layout returns the domains into which the tile should be divided before insertion.
 if there is not enough data to fill a complete layout domain, then 0 will be set.
*/
void
MDDObj::insertTile(shared_ptr<Tile> newTile)
{
    std::vector <r_Minterval> layoutDoms = myStorageLayout->getLayout(newTile->getDomain());
#ifdef DEBUG
    LTRACE << "storage layout returned the following domains";
    for (std::vector <r_Minterval>::iterator domit = layoutDoms.begin(); domit != layoutDoms.end(); domit++)
    {
        LTRACE << *domit;
    }
    LTRACE << "end of storage layout domains";
#endif

    shared_ptr<Tile> tile;
    shared_ptr<Tile> tile2;
    r_Area tempArea = 0;
    r_Area completeArea = 0;
    r_Minterval tempDom;
    r_Minterval tileDom = newTile->getDomain();
    std::vector<shared_ptr<Tile>>* indexTiles = NULL;
    char* newContents = NULL;
    size_t sizeOfData = 0;
    bool checkEquality = true;
    for (std::vector<r_Minterval>::iterator it = layoutDoms.begin(); it != layoutDoms.end(); it++)
    {
        if (checkEquality && tileDom == *it)
        {
            // normal case.  just insert the tile.
            // this case also means that there was no insertion in the previous loops
            LTRACE << "tile domain is same as layout domain, just inserting data";
            myMDDIndex->insertTile(newTile);
            // set to NULL so it will not get deleted at the end of the method
            newTile.reset();
            if (layoutDoms.size() != 1)
            {
                LFATAL << "MDDObj::insertTile(Tile " << tileDom << ") the layout has more than one element but the tile domain completely covers the layout domain";
                throw r_Error(LAYOUTALGORITHMPROBLEM);
            }
        }
        else     // we need to check if there is already a tile defined here
        {
            // this could have been created in a previous loop run
            // we are using retiling here.  *it is therefore an indivisible layout domain.
            LTRACE << "tile domain (" << tileDom << ") is not the same as layout domain (" << *it << ")";
            indexTiles = myMDDIndex->intersect(*it);
            if (indexTiles && indexTiles->size() > 0)
            {
                // there was a tile in the run before, which overlapped with this layout domain
                // there may only be one entry in the index for this domain.
                LTRACE << "found tiles (" << indexTiles->size() << ") in layout domain " << *it;
                if (indexTiles->size() != 1)
                {
                    LFATAL << "MDDObj::insertTile(Tile " << tileDom << ") the index contains many entries for one layout domain";
                    throw r_Error(LAYOUTALGORITHMPROBLEM);
                }
                // update the existing tile with the new data
                tempDom = (*it).create_intersection(tileDom);
                (*(indexTiles->begin()))->copyTile(tempDom, newTile.get(), tempDom);
                //LDEBUG << "updated tile to";
                // (*(indexTiles->begin()))->printStatus(99,RMInit::dbgOut);
            }
            else     // there was no tile overlapping the current layout domain yet
            {
                // create a new tile covering the whole layout domain
                // must be computed everytime because layoutDoms may change in size
                LTRACE << "found no tiles in layout domain " << *it;
                // generate a tile of the domain : layout domain
                tile.reset(new Tile(*it, getMDDBaseType()->getBaseType(), newTile->getDataFormat()));

                tempDom = (*it).create_intersection(tileDom);
                // only update the actual data - the rest was set to 0
                tile->copyTile(tempDom, newTile.get(), tempDom);
                LTRACE << "created tile with domain " << tile->getDomain();
                //LDEBUG << "insert tile";
                //  tile->printStatus(99,RMInit::dbgOut);
                //FIXME: should not be neccessary
                myMDDIndex->insertTile(tile);
            }
            if (indexTiles)
            {
                delete indexTiles;
                indexTiles = NULL;
            }
        }
        checkEquality = false;
    }
    if (newTile)
    {
        LTRACE << "have to delete newTile";
        if (newTile->isPersistent())
        {
            newTile->getDBTile()->setPersistent(false);
        }
        newTile.reset();
    }
}

std::vector<shared_ptr<Tile>>*
                           MDDObj::intersect(const r_Minterval& searchInter) const
{
    std::vector<shared_ptr<Tile>>* retval = myMDDIndex->intersect(searchInter);
#ifdef DEBUG
    if (retval)
    {
        for (std::vector<shared_ptr<Tile>>::iterator it = retval->begin(); it != retval->end(); it++)
        {
            LTRACE << "FOUND " << (*it)->getDomain() << " ";
// TODO: fix with proper stream, RMInit is deprecated
//            (*it)->printStatus(0, RMInit::dbgOut);
        }
    }
#endif
    return retval;
}

std::vector<shared_ptr<Tile>>*
                           MDDObj::getTiles() const
{
    RMTIMER("MDDObj", "getTiles");
    return  myMDDIndex->getTiles();
}

char*
MDDObj::pointQuery(const r_Point& searchPoint)
{
    return  myMDDIndex->pointQuery(searchPoint);
}

const char*
MDDObj::pointQuery(const r_Point& searchPoint) const
{
    return  myMDDIndex->pointQuery(searchPoint);
}

DBMDDObjId
MDDObj::getDBMDDObjId() const
{
    return myDBMDDObj;
}

const MDDBaseType*
MDDObj::getMDDBaseType() const
{
    return myDBMDDObj->getMDDBaseType();
}

r_Minterval
MDDObj::getDefinitionDomain() const
{
    return myDBMDDObj->getDefinitionDomain();
}

r_Minterval
MDDObj::getCurrentDomain() const
{
    return myMDDIndex->getCurrentDomain();
}

const char*
MDDObj::getCellTypeName() const
{
    return myDBMDDObj->getCellTypeName();
}

const BaseType*
MDDObj::getCellType() const
{
    return myDBMDDObj->getCellType();
}

r_Dimension
MDDObj::getDimension() const
{
    return myDBMDDObj->dimensionality();
}

bool
MDDObj::isPersistent() const
{
    return myDBMDDObj->isPersistent();
}


int
MDDObj::getOId(OId* pOId) const
{
    *pOId = myDBMDDObj->getOId();
    return (pOId->getCounter() == 0);
}

int
MDDObj::getEOId(EOId* pEOId) const
{
    *pEOId = myDBMDDObj->getEOId();
    return (pEOId->getCounter() == 0);
}

void
MDDObj::printStatus(unsigned int level, std::ostream& stream) const
{
    myDBMDDObj->printStatus(level, stream);
    myMDDIndex->printStatus(level, stream);
}

void
MDDObj::removeTile(shared_ptr<Tile>& tileToRemove)
{
    int found = myMDDIndex->removeTile(tileToRemove);
    if (found)
    {
        // frees its memory. Persistent freeing??
        LTRACE << "removeTile() about to delete tile";
        tileToRemove->getDBTile().delete_object();
        tileToRemove.reset();
    }
}

MDDObj::~MDDObj()
{
    LTRACE << "~MDDObj() " << (r_Ptr)this;

    if (myMDDIndex)
    {
        delete myMDDIndex;
        myMDDIndex = NULL;
    }
    if (myStorageLayout)
    {
        delete myStorageLayout;
        myStorageLayout = NULL;
    }
}

void
MDDObj::releaseTiles()
{
    myMDDIndex->releasePersTiles();
}

StorageLayout*
MDDObj::getStorageLayout() const
{
    return myStorageLayout;
}

void
MDDObj::setUpdateNullValues(r_Minterval* newNullValues)
{
    nullValues = newNullValues;
    if (newNullValues)
    {
        myDBMDDObj->setNullValues(*nullValues);
    }
}
