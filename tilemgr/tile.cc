#include "config.h"
#include "mymalloc/mymalloc.h"

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
 *   Tiles store BLOBTiles together with their type and
 *   their coordinates.
 *
 *
 * COMMENTS:
 *   none
 *
 ************************************************************/

static const char rcsid[] = "@(#)cachetamgr,Tile: $Id: tile.cc,v 1.79 2005/09/03 21:05:53 rasdev Exp $";

#include <iostream>
#include "catalogmgr/ops.hh"
#include "tile.hh"
#include "relblobif/blobtile.hh"
#include "reladminif/adminif.hh"
#include "relblobif/inlinetile.hh"
#include "raslib/miter.hh"
#include "raslib/miterf.hh"
#include "raslib/miterd.hh"
#include "raslib/basetype.hh"

#include <easylogging++.h>

#include <cstring>

#ifdef RMANBENCHMARK
RMTimer Tile::opTimer("Tile","opTimer");
RMTimer Tile::relTimer("Tile","relTimer");
// RMTimer Tile::o2Timer("Tile","o2Timer");
#endif

const Tile&
Tile::operator=(const Tile& tile)
{
    if(this != &tile)
    {
        type = tile.type;
        domain = tile.domain;
        blobTile->resize(tile.blobTile->getSize());
        blobTile->setDataFormat(tile.blobTile->getDataFormat());
        blobTile->setCurrentFormat(tile.blobTile->getCurrentFormat());
        memcpy(blobTile->getCells(), tile.blobTile->getCells(), blobTile->getSize());
    }
    return *this;
}

Tile::Tile(const Tile& tile)
    :   domain(tile.domain),
        type(tile.type),
        blobTile((DBTile*)NULL)
{
    if (RMInit::useTileContainer)
        blobTile = new InlineTile(tile.blobTile->getSize(), tile.blobTile->getCells(), tile.blobTile->getDataFormat());
    else
        blobTile = new BLOBTile(tile.blobTile->getSize(), tile.blobTile->getCells(), tile.blobTile->getDataFormat());
    blobTile->setCurrentFormat(tile.blobTile->getCurrentFormat());
}

Tile::Tile(std::vector<Tile*>* tilesVec)
    :   domain(),
        type(NULL),
        blobTile((DBTile*)NULL)
{
    // iterators for tiles
    std::vector<Tile*>::iterator tileIt = tilesVec->begin();
    std::vector<Tile*>::iterator tileEnd = tilesVec->end();
    domain = (*(tileIt++))->getDomain();
    while (tileIt !=  tilesVec->end())
    {
        domain.closure_with((*(tileIt++))->getDomain());
    }
    tileIt = tilesVec->begin();
    // initialize type with type of first tile
    type = (*tileIt)->getType();
    if (RMInit::useTileContainer)
        blobTile = new InlineTile(getSize(), static_cast<char>(0), (*tileIt)->getDataFormat());
    else
        blobTile = new BLOBTile(getSize(), static_cast<char>(0), (*tileIt)->getDataFormat());
    // initialize domain
    domain = (*(tileIt++))->getDomain();
    while (tileIt != tileEnd)
        domain.closure_with((*(tileIt++))->getDomain());

    // insert all tiles in the result tile
    tileIt = tilesVec->begin();
    while (tileIt != tileEnd)
    {
        const r_Minterval& currDom = (*tileIt)->getDomain();
        copyTile(currDom, (*tileIt), currDom);
        tileIt++;
    }
}

Tile::Tile(const Tile* projTile, const r_Minterval& projDom, const std::set<r_Dimension, std::less<r_Dimension> >* projDimSet)
    :   domain(projDom.dimension() - projDimSet->size()),
        type(projTile->type),
        blobTile((DBTile*)NULL)
{
    // calculate dimension of new Tile
    r_Dimension dim = 0;
    // pointer to cells in tile to be projected and new tile
    char* cellTile = NULL;
    char* cellProj = NULL;

    for(dim = 0; dim < projDom.dimension(); dim++)
    {
        // do not include dimensions projected away
        std::set<r_Dimension, std::less<r_Dimension> >::const_iterator tempIt = projDimSet->find(dim);
        if(tempIt == projDimSet->end())
        {
            domain << projDom[dim];
        }
    }

    LTRACE << "domain result: " << domain << " domain original: " << projTile->getDomain();

    // init contents
    if (RMInit::useTileContainer)
        blobTile = new InlineTile(getSize(), static_cast<char>(0), projTile->getDataFormat());
    else
        blobTile = new BLOBTile(getSize(), static_cast<char>(0), projTile->getDataFormat());
    // using r_Miter to iterate through tile to be projected and new tile.
    r_Miter projTileIter(&projDom, &projTile->getDomain(), type->getSize(), static_cast<const char*>(projTile->getContents()));
    r_Miter newTileIter(&domain, &domain, type->getSize(), blobTile->getCells());

    // identity operation for base type, used for copying
    UnaryOp* op = type->getUnaryOp(Ops::OP_IDENTITY, const_cast<BaseType*>(type));

    while(!projTileIter.isDone())
    {
        cellTile = newTileIter.nextCell();
        cellProj = projTileIter.nextCell();
#ifdef DEBUG
        LTRACE << "offset in original: " << (int)(cellProj-(projTile)->getContents()) << " {" << (int)*cellProj << "} offset in result: " << (int)((int)(cellTile-getContents())) << " {" << (int)*cellTile << "}";
#endif
        // execute operation on cell
        (*op)(cellTile, cellProj);
    }

    delete op;
}

Tile::Tile(const r_Minterval& newDom, const BaseType* newType, DBTileId newBLOBTile)
    :   domain(newDom),
        type(newType),
        blobTile(newBLOBTile)
{
    LTRACE << "Tile(" << newDom << ", " << newType->getName() << ", blob)";
}

Tile::Tile(const r_Minterval& newDom, const BaseType* newType, r_Data_Format newFormat)
    :   domain(newDom),
        type(newType),
        blobTile((DBTile*)NULL)
{
    LTRACE << "Tile(new), size " << getSize();
    // note that the size is not correct (usually too big) for compressed
    // tiles. Doesn't matter, because resize is called anyway.
    if (RMInit::useTileContainer)
        blobTile = new InlineTile(getSize(), static_cast<char>(0), newFormat);
    else
        blobTile = new BLOBTile(getSize(), static_cast<char>(0), newFormat);
}

Tile::Tile(const r_Minterval& newDom, const BaseType* newType, char* newCells, r_Bytes newSize, r_Data_Format newFormat)
    :   domain(newDom),
        type(newType),
        blobTile((DBTile*)NULL)
{
    LTRACE << "Tile(), fmt " << newFormat << ", size " << newSize;
    r_Data_Format current = r_Array;
    if (!newSize)
    {
        // setting uncompressed contents
        newSize = getSize();
    }
    else
    {
        // setting compressed contents
        current = newFormat;
    }
    if (RMInit::useTileContainer)
        blobTile = new InlineTile(newSize, newCells, newFormat);
    else
        blobTile = new BLOBTile(newSize, newCells, newFormat);
    blobTile->setCurrentFormat(current);
    free(newCells);
}

Tile::Tile(const r_Minterval& newDom, const BaseType* newType, const char* newCells, bool, r_Bytes newSize, r_Data_Format newFormat)
    :   domain(newDom),
        type(newType),
        blobTile((DBTile*)NULL)
{
    LTRACE <<"Tile(), fmt " << newFormat << ", size " << newSize;
    r_Data_Format current = r_Array;
    if (!newSize)
    {
        // setting uncompressed contents
        newSize = getSize();
    }
    else
    {
        // setting compressed contents
        current = newFormat;
    }
    if (RMInit::useTileContainer)
        blobTile = new InlineTile(newSize, newCells, newFormat);
    else
        blobTile = new BLOBTile(newSize, newCells, newFormat);
    blobTile->setCurrentFormat(current);
}

void
Tile::setPersistent(bool state)
{
    blobTile->setPersistent(state);
}

r_Bytes
Tile::getCompressedSize() const
{
    LTRACE << "getCompressedSize()";
    return blobTile->getSize();
}


bool
Tile::isPersistent() const
{
    return blobTile->isPersistent();
}

Tile::~Tile()
{
    LTRACE << "~Tile() " << (r_Ptr) this;
    // this function has to check now if a tile has to be compressed.
    // The old scheme of compression in AdminIf::compCompTiles does
    // not work, because tiles may be destroyed before with releaseAll.
}

DBTileId
Tile::getDBTile()
{
    return blobTile;
}

char*
Tile::execCondenseOp(CondenseOp* myOp, const r_Minterval& areaOp)
{
    char* cellOp = NULL;
    char* dummy = getContents();
    r_Miter opTileIter(&areaOp, &getDomain(), getType()->getSize(), dummy);
#ifdef RMANBENCHMARK
    opTimer.resume();
#endif

    while(!opTileIter.isDone())
    {
        cellOp = opTileIter.nextCell();
        // execute operation on cell
        (*myOp)(cellOp);
    }

#ifdef RMANBENCHMARK
    opTimer.pause();
#endif

    return myOp->getAccuVal();
}


void
Tile::execUnaryOp(UnaryOp* myOp, const r_Minterval& areaRes, const Tile* opTile, const r_Minterval& areaOp)
{
    char* cellRes = NULL;
    const char* cellOp = NULL;

    char* dummy1 = getContents();
    const char* dummy2 = opTile->getContents();
    r_Miter resTileIter(&areaRes, &getDomain(), getType()->getSize(), dummy1);
    r_Miter opTileIter(&areaOp, &opTile->getDomain(), opTile->getType()->getSize(), dummy2);

#ifdef RMANBENCHMARK
    opTimer.resume();
#endif

    while(!resTileIter.isDone())
    {
        cellRes = resTileIter.nextCell();
        cellOp = opTileIter.nextCell();
        // execute operation on cell
        (*myOp)(cellRes, cellOp);
    }

#ifdef RMANBENCHMARK
    opTimer.pause();
#endif
}

void
Tile::execBinaryOp(BinaryOp* myOp, const r_Minterval& areaRes, const Tile* op1Tile, const r_Minterval& areaOp1, const Tile* op2Tile, const r_Minterval& areaOp2)

{
    if ( myOp == NULL )
    {
        throw r_Error (OPERANDSRESULTTYPESNOMATCH);
    }
    char* cellRes = NULL;
    const char* cellOp1 = NULL;
    const char* cellOp2 = NULL;

    char* dummy1 = getContents();
    const char* dummy2 = op1Tile->getContents();
    const char* dummy3 = op2Tile->getContents();

    r_Miter resTileIter(&areaRes, &getDomain(), getType()->getSize(), dummy1);
    r_Miter op1TileIter(&areaOp1, &op1Tile->getDomain(), op1Tile->getType()->getSize(), dummy2);
    r_Miter op2TileIter(&areaOp2, &op2Tile->getDomain(), op2Tile->getType()->getSize(), dummy3);

#ifdef RMANBENCHMARK
    opTimer.resume();
#endif

    while(!resTileIter.isDone())
    {
        cellRes = resTileIter.nextCell();
        cellOp1 = op1TileIter.nextCell();
        cellOp2 = op2TileIter.nextCell();
        // execute operation on cell
        (*myOp)(cellRes, cellOp1, cellOp2);
    }

#ifdef RMANBENCHMARK
    opTimer.pause();
#endif
}

void
Tile::execConstOp(BinaryOp* myOp, const r_Minterval& areaRes, const Tile* opTile, const r_Minterval& areaOp, const char* cell, int constPos)
{
    char* cellRes = NULL;
    const char* cellOp = NULL;
    char* dummy1 = getContents();
    const char* dummy2 = opTile->getContents();
    r_Miter resTileIter(&areaRes, &getDomain(), getType()->getSize(), dummy1);
    r_Miter opTileIter(&areaOp, &opTile->getDomain(), opTile->getType()->getSize(), dummy2);
#ifdef RMANBENCHMARK
    opTimer.resume();
#endif

    if (constPos == 1)
    {
        while(!resTileIter.isDone())
        {
            cellRes = resTileIter.nextCell();
            cellOp = opTileIter.nextCell();
            // execute operation on cell
            (*myOp)(cellRes, cell, cellOp);
        }
    }
    else
    {
        while(!resTileIter.isDone())
        {
            cellRes = resTileIter.nextCell();
            cellOp = opTileIter.nextCell();
            // execute operation on cell
            (*myOp)(cellRes, cellOp, cell);
        }
    }

#ifdef RMANBENCHMARK
    opTimer.pause();
#endif
}

void
Tile::execMarrayOp(MarrayOp* myOp, const r_Minterval& areaRes, const r_Minterval& areaOp)
{
    r_Dimension dimRes = areaRes.dimension();
    r_Point pointRes = areaRes.get_origin();
    r_Point pointOp = areaOp.get_origin();
    char* cellRes = getCell(calcOffset(pointRes));
    unsigned int resSize = type->getSize();

#ifdef RMANBENCHMARK
    opTimer.resume();
#endif

    bool done = false;
    bool recalc = false;

    // iterate over all cells
    while(!done)
    {
        if(recalc)
        {
            cellRes = getCell(calcOffset(pointRes));
            recalc = false;
        }

        (*myOp)(cellRes, pointOp);

        cellRes += resSize;

        // increment coordinates
        r_Dimension dim = dimRes - 1;
        ++pointRes[dim];
        ++pointOp[dim];
        while(pointRes[dim] > areaRes[dim].high())
        {
            recalc = true;
            pointRes[dim] = areaRes[dim].low();
            pointOp[dim] = areaOp[dim].low();
            if(dim == 0)
            {
                done = true;
                break;
            }
            --dim;
            ++pointRes[dim];
            ++pointOp[dim];
        }
    }

#ifdef RMANBENCHMARK
    opTimer.pause();
#endif
}

char*
Tile::execGenCondenseOp(GenCondenseOp* myOp, const r_Minterval& areaOp)
{
    r_Point pointOp = areaOp.get_origin();
    unsigned int dim = areaOp.dimension();

#ifdef RMANBENCHMARK
    opTimer.resume();
#endif

    bool done = false;
    // iterate over all cells
    while(!done)
    {
        (*myOp)(pointOp);

        // increment coordinates
        r_Dimension i = dim - 1;
        ++pointOp[i];
        while(pointOp[i] > areaOp[i].high())
        {
            pointOp[i] = areaOp[i].low();
            if(i == 0)
            {
                done = true;
                break;
            }
            --i;
            ++pointOp[i];
        }
    }
#ifdef RMANBENCHMARK
    opTimer.pause();
#endif

    return myOp->getAccuVal();
}


#ifdef RMANDEBUG
#define CHECK_ITER_SYNC(a,b) \
    if (a.isDone() != b.isDone()) {LTRACE << "iterators out of sync!";}
#else
#define CHECK_ITER_SYNC(a,b)
#endif

template<class T>
static inline void tile_scale_core(r_Miter &iterDest, r_MiterFloat &iterSrc, __attribute__ ((unused)) T *dummy)
{
    while (!iterDest.isDone())
    {
        const T* srcPtr = (const T*)iterSrc.nextCell();
        T* destPtr = (T*)iterDest.nextCell();
        *destPtr = *srcPtr;

        CHECK_ITER_SYNC(iterSrc, iterDest)
    }
    CHECK_ITER_SYNC(iterSrc, iterDest)
}

#ifdef BLVAHELP

void fast_scale_resample_array(char *dest, const char *src, const r_Minterval &destIv, const r_Minterval &srcIv, const r_Minterval &iterDom, unsigned int type_len, unsigned int length)
{
    r_MiterDirect destIter((void*)dest, destIv, destIv, type_len, 1);
    r_MiterDirect subIter((void*)src, srcIv, iterDom, type_len, 1);
    r_MiterDirect srcIter((void*)src, srcIv, iterDom, type_len, length);
    unsigned int dim = (unsigned int)srcIv.dimension();
    unsigned int i;

    for (i=0; i<dim; i++)
    {
        subIter.id[i].low = 0;
    }

    while (srcIter.done == 0)
    {
        double sum = 0;
        unsigned int count = 0;

        if (destIter.done != 0) cout << "dest out of sync!" << endl;
        if (srcIter.id[dim-1].pos == srcIter.id[dim-1].low)
        {
            cout << srcIter << " : " << destIter << endl;
        }

        // init sub iterator
        subIter.done = 0;
        for (i=0; i<dim; i++)
        {
            long rest;

            subIter.id[i].pos = 0;
            subIter.id[i].data = srcIter.getData();
            rest = srcIter.id[i].high - srcIter.id[i].pos;
            if (rest >= (long)length) rest = (long)length-1;
            subIter.id[i].high = rest;
            //count *= rest+1;
        }
        while (subIter.done == 0)
        {
            sum += *((const unsigned char*)(subIter.getData()));
            ++subIter;
            count++;
        }

        cout<<"Sum="<<sum<<" count="<<count<<endl;
        // use round to nearest
        *((char*)(destIter.getData())) = (char)(sum / count + 0.5);
        //cout << (long)(((const T*)(srcIter.getData())) - src) << " , " << (long)(((T*)(destIter.getData())) - dest) << endl;
        ++srcIter;
        ++destIter;
    }
    cout << "End: " << srcIter << " : " << destIter << endl;
    if (destIter.done == 0) cout << "dest out of sync!" << endl;
}
#endif

void
Tile::execScaleOp(const Tile* opTile, const r_Minterval& sourceDomain)
{
    // origin is not used in this version
#ifdef BLVAHELP
    fast_scale_resample_array((char*)getContents(),((Tile*)opTile)->getContents(), domain, opTile->getDomain(), sourceDomain, 1, 2);
#else

    unsigned int typeLength = getType()->getSize();
    const char *srcPtr = (const_cast<Tile*>(opTile))->getContents();

    r_MiterFloat iterSrc(const_cast<Tile*>(opTile), const_cast<r_Minterval&>(sourceDomain), domain);
    r_Miter iterDest(&domain, &domain, typeLength, getContents());

    // optimize for common basetypes
    switch (getType()->getType())
    {
    case OCTET:
        tile_scale_core(iterDest, iterSrc, static_cast<r_Octet*>(0));
        break;
    case CHAR:
        tile_scale_core(iterDest, iterSrc, static_cast<r_Char*>(0));
        break;
    case SHORT:
        tile_scale_core(iterDest, iterSrc, static_cast<r_Short*>(0));
        break;
    case USHORT:
        tile_scale_core(iterDest, iterSrc, static_cast<r_UShort*>(0));
        break;
    case LONG:
        tile_scale_core(iterDest, iterSrc, static_cast<r_Long*>(0));
        break;
    case ULONG:
        tile_scale_core(iterDest, iterSrc, static_cast<r_ULong*>(0));
        break;
    case FLOAT:
        tile_scale_core(iterDest, iterSrc, static_cast<r_Float*>(0));
        break;
    case DOUBLE:
        tile_scale_core(iterDest, iterSrc, static_cast<r_Double*>(0));
        break;
    default:
        while (!iterDest.isDone())
        {
            char *destPtr = iterDest.nextCell();
            srcPtr  = iterSrc.nextCell();
            memcpy(destPtr, srcPtr, typeLength);
            CHECK_ITER_SYNC(iterSrc, iterDest)
        }
        CHECK_ITER_SYNC(iterSrc, iterDest)
        break;
    }

#endif
}

// the used version
int
Tile::scaleGetDomain(const r_Minterval& areaOp, const std::vector<double>& scaleFactors, r_Minterval &areaScaled)
{
    try
    {
        areaScaled= areaOp.create_scale(scaleFactors);
    }
    catch(r_Error& err)
    {
        //error on scalling
        return 0;
    }

    return 1;
}


void
Tile::copyTile(const r_Minterval &areaRes, const Tile *opTile, const r_Minterval &areaOp)
{
    const char *cellOp = NULL;
    char *cellRes = NULL;

    // this may trigger decompression
    cellOp = opTile->getContents();
    cellRes = getContents();

    r_Dimension dimRes = areaRes.dimension();
    r_Dimension dimOp = areaOp.dimension();

    r_Range width = areaRes[dimRes-1].get_extent();
    if (width > areaOp[dimOp-1].get_extent())
    {
        width = areaOp[dimOp-1].get_extent();
        LWARNING << "RMDebug::module_tilemgr::copyTile() WARNING: had to adjust high dim width to " << width;
    }

    unsigned int tsize = getType()->getSize();
    unsigned int tsizeOp = opTile->getType()->getSize();

    if (tsize != tsizeOp)
    {
        LTRACE << "copyTile() ERROR: type sizes incompatible!\n"
               << "this type: " << getType()->getName() << "(" << tsize << "), opTile type: "
               << opTile->getType()->getName() << "(" << tsizeOp << ")";
        // FIXME here we have to check if is appropiate to continue
    }

    // these iterators iterate last dimension first, i.e. minimal step size
    r_MiterDirect resTileIter(static_cast<void*>(cellRes), getDomain(), areaRes, tsize);
    r_MiterDirect opTileIter(static_cast<void*>(const_cast<char*>(cellOp)), opTile->getDomain(), areaOp, tsize);

#ifdef RMANBENCHMARK
    opTimer.resume();
#endif

    while (!resTileIter.isDone())
    {
        // copy entire line (continuous chunk in last dimension) in one go
        memcpy(resTileIter.getData(), opTileIter.getData(), static_cast<size_t>(width) * tsize);
        // force overflow of last dimension
        resTileIter.id[dimRes-1].pos += width;
        opTileIter.id[dimOp-1].pos += width;
        
        // iterate; the last dimension will always overflow now
        ++resTileIter;
        ++opTileIter;
    }

#ifdef RMANBENCHMARK
    opTimer.pause();
#endif
}

std::vector<Tile*>*
Tile::splitTile(r_Minterval resDom, __attribute__ ((unused)) int storageDomain)
{
    // domain of current tile
    r_Minterval currDom(resDom.dimension());
    // type of result tiles
    const BaseType* resType;
    // iterators for tiles
    std::vector<Tile*>* resultVec = new std::vector<Tile*>;
    // pointer to generated current tile
    Tile* smallTile;
    // current factor for translating
    r_Point cursor(resDom.dimension());
    // origin of big tile
    r_Point origin;
    // size of result tiles
    r_Point tileSize;
    // flag
    int done = 0;
    // for loops
    int i = 0;
    unsigned int j;

    // initialize cursor
    for(j = 0; j < cursor.dimension(); j++)
        cursor[j] = 0;
    // calculate size of Tiles
    tileSize = resDom.get_extent();
    // initialize resType
    resType = this->getType();
    // origin of bigTile
    origin = (this->getDomain()).get_origin();

    // initialize currDom
    for(j = 0; j < cursor.dimension(); j++)
        currDom << r_Sinterval(origin[j], origin[j] + tileSize[j] - 1);
    // resets resDom to lower left side of bigTile
    resDom = currDom;

    // intersect with bigTile
    currDom.intersection_with(this->getDomain());
    // iterate with smallTile over bigTile
    while(!done)
    {
        currDom.intersection_with(this->getDomain());

        // create new smallTile
        smallTile = new Tile(currDom, resType);
        // fill it with relevant area
        smallTile->copyTile(currDom, this, currDom);
        // insert it in result vector
        resultVec->push_back(smallTile);

        // increment cursor, start with highest dimension
        i = static_cast<int>(cursor.dimension()) - 1;
        cursor[static_cast<r_Dimension>(i)] += tileSize[static_cast<r_Dimension>(i)];
        // move cursor
        currDom = resDom.create_translation(cursor);
        while(!(currDom.intersects_with(this->getDomain())))
        {
            cursor[static_cast<r_Dimension>(i)] = 0;
            i--;
            if(i < 0)
            {
                done = 1;
                break;
            }
            cursor[static_cast<r_Dimension>(i)] += tileSize[static_cast<r_Dimension>(i)];
            // move cursor
            currDom = resDom.create_translation(cursor);
        }
    }
    return resultVec;
}

void
Tile::printStatus(__attribute__ ((unused)) unsigned int level, std::ostream& stream) const
{
    r_Point p(domain.dimension());
    int done = 0;
    int i = 0;
    unsigned int j = 0;
    const char* cell;
    unsigned int dim = domain.dimension();

    // print the contents only on very high debug level

    // initialize point
    for(j = 0; j < dim; j++)
    {
        p << domain[j].low();
    }

    // iterate over all cells
    while(!done)
    {
        // print cell
        cell = getCell(calcOffset(p));
        type->printCell(stream, cell);

        // increment coordinate
        i = static_cast<int>(dim) - 1;
        while(++p[static_cast<r_Dimension>(i)] > domain[static_cast<r_Dimension>(i)].high())
        {
            stream << endl;
            p[static_cast<r_Dimension>(i)] = domain[static_cast<r_Dimension>(i)].low();
            i--;
            if(i < 0)
            {
                done = 1;
                break;
            }
        }
        if(i < (static_cast<int>(dim) - 2)) stream << endl;
    }
}

char*
Tile::getCell(const r_Point& aPoint)
{
    r_Area cellOffset = domain.cell_offset(aPoint);
    return getCell(cellOffset);
}

const char*
Tile::getCell(const r_Point& aPoint) const
{
    r_Area cellOffset = domain.cell_offset(aPoint);
    return getCell(cellOffset);
}

const r_Minterval&
Tile::getDomain() const
{
    return domain;
}

const BaseType*
Tile::getType() const
{
    return type;
}

r_Dimension
Tile::getDimension() const
{
    return domain.dimension();
}

r_Bytes
Tile::getSize() const
{
    return domain.cell_count() * type->getSize();
}

r_Data_Format
Tile::getDataFormat() const
{
    return blobTile->getDataFormat();
}

const char*
Tile::getCell(r_Area index) const
{
    return &(blobTile->getCells()[index * type->getSize()]);
}

char*
Tile::getCell(r_Area index)
{
    return &(blobTile->getCells()[index * type->getSize()]);
}

void
Tile::setCell(r_Area index, const char* newCell)
{
    char* cells = getCell(index);
    unsigned int typeSize = type->getSize();
    for (unsigned int i = 0; i < typeSize; i++)
        cells[i] = newCell[i];
}

char*
Tile::getContents()
{
    return blobTile->getCells();
}

const char*
Tile::getContents() const
{
    return blobTile->getCells();
}

void
Tile::setContents(char* newContents)
{
    blobTile->setCells(newContents);
}

r_Bytes
Tile::calcOffset(const r_Point& point) const
{
    int i = 0;
    r_Bytes offset = 0;
    r_Bytes factor = 1;

    // calculate offset
    for(i = static_cast<int>(domain.dimension()) - 1; i >= 0; i--)
    {
        offset += (point[static_cast<r_Dimension>(i)] - domain[static_cast<r_Dimension>(i)].low()) * factor;
        factor *= domain[static_cast<r_Dimension>(i)].high() - domain[static_cast<r_Dimension>(i)].low() + 1;
    }

    return offset;
}
