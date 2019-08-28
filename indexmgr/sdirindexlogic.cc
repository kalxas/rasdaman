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
 * SOURCE: dirix.cc
 *
 * MODULE: indexmgr
 * CLASS:   SDirIndexLogic
 *
 * COMMENTS:
 *
*/

#include "sdirindexlogic.hh"
#include "indexds.hh"              // for IndexDS
#include "keyobject.hh"            // for KeyObject, operator<<
#include "raslib/mddtypes.hh"      // for r_Area

#include <logging.hh>              // for Writer, CTRACE


bool SDirIndexLogic::insertObject(IndexDS *ixDS, const KeyObject &newKeyObject,
                                  __attribute__((unused))
                                  const StorageLayout &sl)
{
    r_Minterval newKeyObjectDomain = newKeyObject.getDomain();

    int pos = binarySearch(ixDS, newKeyObjectDomain, Lowest, 0,
                           static_cast<int>(ixDS->getSize()) - 1);
    ixDS->insertObject(newKeyObject, static_cast<unsigned int>(pos + 1));
    // should check if insertion was succesfull
    return true;
}

int SDirIndexLogic::binarySearch(const IndexDS *ixDS,
                                 const r_Minterval &newDomain,
                                 OrderPoint o, int first, int last)
{
    int retval = 0;
    int middle = 0;
    int compResult = 0;

    if (first > last)
        retval = last;
    else
    {
        middle = (last + first) / 2;
        compResult =
            compare(newDomain,
                    ixDS->getObjectDomain(static_cast<unsigned int>(middle)), o, o);
        if (compResult < 0)
            retval = binarySearch(ixDS, newDomain, o, first, middle - 1);
        else if (compResult > 0)
            retval = binarySearch(ixDS, newDomain, o, middle + 1, last);
        else
            retval = middle;
    }
    return retval;
}

int SDirIndexLogic::binaryPointSearch(const IndexDS *ixDS,
                                      const r_Point &pnt,
                                      SDirIndexLogic::OrderPoint o, int first,
                                      int last)
{
    int retval = 0;
    int middle = 0;
    int compResult = 0;
    r_Minterval KeyObjectDomain;
    r_Point pnt2;

    if (first > last)
        retval = -1;
    else
    {
        middle = (last + first) / 2;
        KeyObjectDomain = ixDS->getObjectDomain(static_cast<unsigned int>(middle));
        if (KeyObjectDomain.covers(pnt) == 1)
            retval = middle;
        else
        {
            switch (o)
            {
            case Highest:
                pnt2 = KeyObjectDomain.get_high();
                break;
            case Lowest:
                pnt2 = KeyObjectDomain.get_origin();
                break;
            case None:
                break;
            default:
                break;
            }

            compResult = pnt.compare_with(pnt2);
            LTRACE << "binaryPointSearch compResult " << compResult;

            if (compResult > 0 && o == Highest)
                retval = binaryPointSearch(ixDS, pnt, o, middle + 1, last);
            else if (compResult < 0 && o == Lowest)
                retval = binaryPointSearch(ixDS, pnt, o, first, middle - 1);
            else
            {
                compResult = binaryPointSearch(ixDS, pnt, o, middle + 1, last);
                if (compResult < 0)
                    retval = binaryPointSearch(ixDS, pnt, o, first, middle - 1);
                else
                    retval = compResult;
            }
        }
    }
    return retval;
}

int SDirIndexLogic::binaryRegionSearch(const IndexDS *ixDS,
                                       const r_Minterval &mint,
                                       r_Area &area,
                                       KeyObjectVector &intersectedObjects,
                                       int first, int last)
{
    int retval = 0;
    int middle = 0;
    r_Minterval t;
    int inc = 0;
    int ix = 0;
    r_Minterval objDomain;
    int compResult = 0;
    KeyObject newObj;
    r_Minterval intersectedRegion;
    // assumes order according to the lowest corner of the objects
    if (first > last)
        retval = -1;
    else
    {
        middle = (last + first) / 2;
        t = ixDS->getObjectDomain(static_cast<unsigned int>(middle));
        if (mint.get_high().compare_with(t.get_origin()) < 0)
        {
            // R.hi < tile.lo  no tiles after this one
            retval = binaryRegionSearch(ixDS, mint, area, intersectedObjects, first, middle - 1);
        }
        else
        {
            if (t.get_high().compare_with(mint.get_origin()) < 0)
            {
                retval = binaryRegionSearch(ixDS, mint, area, intersectedObjects, middle + 1, last);
                if (area > 0)
                {
                    retval = binaryRegionSearch(ixDS, mint, area, intersectedObjects, first, middle - 1);
                }
            }
            else
            {
                inc = 1;
                ix = middle;
                // starting to search forward, starting in the middle
                while (true)
                {
                    objDomain = ixDS->getObjectDomain(static_cast<unsigned int>(ix));
                    compResult = mint.get_high().compare_with(objDomain.get_origin());
                    LTRACE << "position " << ix << " last " << last << " incrementor "
                           << inc << " object domain " << objDomain << " compare "
                           << compResult;
                    // object intersects region
                    if (objDomain.intersects_with(mint))
                    {
                        intersectedRegion = objDomain;
                        intersectedRegion.intersection_with(mint);
                        area = area - intersectedRegion.cell_count();
                        newObj = ixDS->getObject(static_cast<unsigned int>(ix));
                        intersectedObjects.push_back(newObj);
                        LTRACE << "added one entry, intersected region "
                               << intersectedRegion << " area left " << area;
                    }
                    if (inc != -1 && (ix == last || compResult < 0))
                    {
                        LTRACE << "starting again at middle, but going backwards";
                        ix = middle;
                        inc = -1;
                    }
                    if (ix == first && inc == -1)  // not needed:||first == last
                    {
                        LTRACE << "breaking loop, arrived at start";
                        retval = ix;
                        break;
                    }
                    if (area <= 0)  // || first == last || ix == first)
                    {
                        LTRACE << "breaking loop, area is found";
                        retval = ix;
                        break;
                    }
                    ix += inc;
                }
            }
        }
    }

    return retval;
}

int SDirIndexLogic::compare(const r_Minterval &mint1,
                            const r_Minterval &mint2, OrderPoint o1,
                            OrderPoint o2)
{
    r_Point point1, point2;
    switch (o1)
    {
    case Highest:
        point1 = mint1.get_high();
        break;
    case Lowest:
        point1 = mint1.get_origin();
        break;
    case None:
        break;
    default:
        break;
    }
    switch (o2)
    {
    case Highest:
        point2 = mint2.get_high();
        break;
    case Lowest:
        point2 = mint2.get_origin();
        break;
    case None:
        break;
    default:
        break;
    }
    return point1.compare_with(point2);
}

void SDirIndexLogic::intersect(const IndexDS *ixDS,
                               const r_Minterval &searchInter,
                               KeyObjectVector &intersectedObjs,
                               __attribute__((unused))
                               const StorageLayout &sl)
{
    r_Area area = 0;
    r_Minterval intersectArea(searchInter.dimension());
    r_Minterval currDom(ixDS->getCoveredDomain());
    // avoid exceptions from r_Minterval
    if (!searchInter.intersects_with(currDom))
    {
        LTRACE << "intersect(" << searchInter << ") search interval does not intersect wit current domain " << currDom;
    }
    else
    {
        // Optimization: no need to search the whole area.
        // only the area which is intersected by the current domain.
        intersectArea.intersection_of(searchInter, currDom);
        area = intersectArea.cell_count();
        LTRACE << "Area = " << area;
        binaryRegionSearch(ixDS, intersectArea, area, intersectedObjs, 0,
                           static_cast<int>(ixDS->getSize()) - 1);
    }
}

void SDirIndexLogic::intersectUnOpt(const IndexDS *ixDS,
                                    const r_Minterval &searchInter,
                                    KeyObjectVector &intersectedObjs)
{
    for (unsigned int i = 0; i < ixDS->getSize(); i++)
    {
        r_Minterval objInterval = ixDS->getObjectDomain(i);
        if (searchInter.intersects_with(objInterval))
        {
            KeyObject obj = ixDS->getObject(i);
            intersectedObjs.push_back(obj);
        }
    }
}

void SDirIndexLogic::containPointQuery(const IndexDS *ixDS,
                                       const r_Point &searchPoint,
                                       KeyObject &result,
                                       __attribute__((unused))
                                       const StorageLayout &sl)
{
    int ix = binaryPointSearch(ixDS, searchPoint, Lowest, 0,
                               static_cast<int>(ixDS->getSize()) - 1);
    LTRACE << "result from binaryPointSearch ix " << ix;

    if (ix >= 0)
    {
        result = ixDS->getObject(static_cast<unsigned int>(ix));
    }
}

void SDirIndexLogic::getObjects(const IndexDS *ixDS, KeyObjectVector &objs,
                                __attribute__((unused))
                                const StorageLayout &sl)
{
    LTRACE << "getObjects()";
    ixDS->getObjects(objs);
}

bool SDirIndexLogic::removeObject(IndexDS *ixDS, const KeyObject &objToRemove,
                                  __attribute__((unused))
                                  const StorageLayout &sl)
{
    LTRACE << "removeObject(" << objToRemove << ")";
    return ixDS->removeObject(objToRemove);
}

