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
 * SOURCE: transdirix.cc
 *
 * MODULE: indexmgr
 * CLASS:   TransDirIx
 *
 *
 * COMMENTS:
 *  none
 *
*/

#include "transdirix.hh"
#include "keyobject.hh"         // for KeyObject, operator<<
#include "reladminif/oidif.hh"  // for OId, OId::OIdPrimitive
#include "raslib/error.hh"      // for r_Error, TRANSIENT_INDEX_OUT_OF_BO...
#include "raslib/mddtypes.hh"   // for r_Bytes, r_Ptr, r_Dimension
#include <logging.hh>

#include <boost/shared_ptr.hpp>  // for operator==, shared_ptr
#include <ext/alloc_traits.h>    // for __alloc_traits<>::value_type
#include <memory>                // for allocator_traits<>::value_type
#include <ostream>               // for operator<<, ostream, std::endl, basic_o...
#include <vector>                // for vector

IndexDS *TransDirIx::getNewInstance() const
{
    return new TransDirIx(getDimension());
}

void TransDirIx::freeDS() {}

unsigned int TransDirIx::getOptimalSize() const
{
    return 1000;
}

unsigned int TransDirIx::getDimension() const
{
    return currDomain.dimension();
}

bool TransDirIx::isPersistent() const
{
    return false;
}

TransDirIx::TransDirIx(r_Dimension dim)
    : currDomain(dim), tiles()
{
    LTRACE << "TransDirIx() " << (r_Ptr)this;
}

void TransDirIx::printStatus(__attribute__((unused)) unsigned int level,
                             std::ostream &stream) const
{
    stream << "TransDirIx " << currDomain << std::endl;
    auto entryIt = tiles.begin();

    int i = 0;
    while (entryIt != tiles.end())
    {
        stream << i << ". " << (*entryIt).getDomain() << std::endl;
        entryIt++;
        i++;
    }
}

void TransDirIx::insertObject(const KeyObject &newKeyObject, unsigned int pos)
{
    if (pos > getSize())
    {
        LERROR << "TransDirIx::insertObject(" << newKeyObject << ", " << pos << ") pos out of range";
        throw r_Error(TRANSIENT_INDEX_OUT_OF_BOUNDS);
    }
    else
    {
        if (tiles.size() == 0)
            currDomain = newKeyObject.getDomain();
        else
            currDomain.closure_with(newKeyObject.getDomain());
        tiles.insert(tiles.begin() + pos, newKeyObject);
    }
}

bool TransDirIx::removeObject(const KeyObject &tileToRemove)
{
    bool found = false;
    for (auto iter = tiles.begin(); iter != tiles.end(); iter++)
    {
        if ((*iter).getTransObject() == tileToRemove.getTransObject())
        {
            found = true;
            tiles.erase(iter);
            break;
        }
    }
    return found;
}

OId::OIdPrimitive TransDirIx::getIdentifier() const
{
    return (OId::OIdPrimitive)(r_Ptr)this;
}

void TransDirIx::setAssignedDomain(const r_Minterval &newDomain)
{
    currDomain = newDomain;
}

const KeyObject &TransDirIx::getObject(unsigned int pos) const
{
    return tiles[pos];
}

r_Minterval TransDirIx::getObjectDomain(unsigned int pos) const
{
    return tiles[pos].getDomain();
}

void TransDirIx::getObjects(KeyObjectVector &objs) const
{
    objs = tiles;
}

r_Minterval TransDirIx::getCoveredDomain() const
{
    return currDomain;
}

unsigned int TransDirIx::getSize() const
{
    return tiles.size();
}

r_Bytes TransDirIx::getTotalStorageSize() const
{
    // size of currDomain field:
    r_Bytes sz = currDomain.get_storage_size();

    // size of tiles vector
    // even though the size of each entry summed here corresponds to the
    // KeyObject, it should be added here since it
    // is "additional data" stored due to the storage structure adopted.
    // For each entry (tile), there is a domain specification, a pointer and
    // an r_Bytes size field:
    sz += tiles.size() * (currDomain.get_storage_size() + sizeof(KeyObject) + sizeof(r_Bytes));

    return sz;
}

TransDirIx::~TransDirIx()
{
    tiles.clear();
}

std::vector<r_Minterval *> *TransDirIx::getObjectDomains(void) const
{
    auto *te = new std::vector<r_Minterval *>();
    te->reserve(tiles.size());
    unsigned int end = tiles.size();
    for (unsigned int i = 0; i < end; i++)
    {
        (*te)[i] = new r_Minterval(tiles[i].getDomain());
    }
    return te;
}

bool TransDirIx::isValid() const
{
    return true;
}

bool TransDirIx::isUnderFull() const
{
    return false;
}

bool TransDirIx::isOverFull() const
{
    return false;
}

bool TransDirIx::isSameAs(const IndexDS *ix) const
{
    if (ix->isPersistent())
        return false;
    else if (ix == this)
        return true;
    else
        return false;
}

void TransDirIx::setObject(const KeyObject &theKey, unsigned int i)
{
    tiles[i] = theKey;
}

void TransDirIx::setObjectDomain(const r_Minterval &dom,
                                 unsigned int i)
{
    tiles[i].setDomain(dom);
}

r_Minterval TransDirIx::getAssignedDomain(void) const
{
    return currDomain;
}

bool TransDirIx::removeObject(unsigned int pos)
{
    bool found = false;
    if (pos < tiles.size())
    {
        found = true;
        tiles.erase(tiles.begin() + pos);
    }
    return found;
}
