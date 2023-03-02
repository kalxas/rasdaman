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

#include "dbtcindex.hh"                  // for DBTCIndex
#include "hierindex.hh"                  // for DBHierIndex
#include "reladminif/adminif.hh"         // for AdminIf
#include "reladminif/dbobject.hh"        // for DBObjectId, DBObject
#include "reladminif/objectbroker.hh"    // for ObjectBroker
#include "reladminif/oidif.hh"           // for OId, operator<<, OId::INLINETILEOID
#include "indexmgr/hierindexds.hh"       // for HierIndexDS
#include "indexmgr/keyobject.hh"         // for KeyObject, operator<<
#include "relblobif/inlinetile.hh"       // for InlineTile
#include "reladminif/lists.h"            // for DBObjectPMap, KeyObjectVector
#include "raslib/error.hh"               // for r_Error, r_Error::r_Error_Featur...
#include "raslib/mddtypes.hh"            // for r_Dimension
#include "storagemgr/sstoragelayout.hh"  // for StorageLayout, StorageLayout::De...
#include <logging.hh>                    // for Writer, CTRACE, LTRACE

#include <map>      // for _Rb_tree_iterator, map<>::iterator
#include <memory>   // for allocator_traits<>::value_type
#include <ostream>  // for operator<<, ostream, basic_ostream
#include <utility>  // for pair
#include <vector>   // for vector, vector<>::iterator

void DBTCIndex::setMappingHasChanged()
{
    mappingHasChanged = true;
}

void DBTCIndex::setInlineTileHasChanged()
{
    inlineTileHasChanged = true;
}

DBTCIndex::DBTCIndex(const OId &id)
    : DBHierIndex(id),
      mappingHasChanged(false),
      inlineTileHasChanged(false),
      _isLoaded(false),
      hasBlob(false)
{
    throw r_Error(r_Error::r_Error_FeatureNotSupported);
    readFromDb();
    _isLoaded = !hasBlob;
}

IndexDS *DBTCIndex::getNewInstance() const
{
    return static_cast<HierIndexDS *>(new DBTCIndex(getDimension(), !isLeaf()));
}

DBTCIndex::DBTCIndex(r_Dimension dim, bool isNode)
    : DBHierIndex(dim, isNode, false),
      mappingHasChanged(false),
      inlineTileHasChanged(false),
      _isLoaded(true),
      hasBlob(false)
{
    throw r_Error(r_Error::r_Error_FeatureNotSupported);
    objecttype = OId::DBTCINDEXOID;
    setPersistent(true);
    setCached(true);
}

void DBTCIndex::printStatus(unsigned int level, std::ostream &stream) const
{
    auto *indent = new char[level * 2 + 1];
    for (unsigned int j = 0; j < level * 2; j++)
        indent[j] = ' ';
    indent[level * 2] = '\0';

    stream << indent << "DBTCIndex ";
    DBHierIndex::printStatus(level + 1, stream);
    delete[] indent;
}

DBTCIndex::~DBTCIndex() noexcept(false)
{
    if (!isModified())
    {
        if (!AdminIf::isReadOnlyTA())
        {
            if (isLeaf()) decideForInlining();
            if (mappingHasChanged) updateTileIndexMappings();
            if (inlineTileHasChanged)
            {
                storeTiles();
                changeBOIdToIOId();
            }
            if (isModified()) DBHierIndex::updateInDb();
            if (isLeaf()) changeIOIdToBOId();
        }
    }
    else
        validate();
    currentDbRows = 0;
    parent = OId(0);
}

void DBTCIndex::registerIOIds()
{
    for (auto i = myKeyObjects.begin(); i != myKeyObjects.end(); i++)
        if ((*i).getObject().getOId().getType() == OId::INNEROID)
        {
            LTRACE << "registering tileoid " << OId((*i).getObject().getOId().getCounter(), OId::INLINETILEOID) << " indexoid " << myOId;
            ObjectBroker::registerTileIndexMapping(OId((*i).getObject().getOId().getCounter(), OId::INLINETILEOID), myOId);
            hasBlob = true;
        }
}

void DBTCIndex::changeIOIdToBOId()
{
    for (auto it = myKeyObjects.begin(); it != myKeyObjects.end(); it++)
        if ((*it).getObject().getOId().getType() == OId::INNEROID)
        {
            OId o((*it).getObject().getOId().getCounter(), OId::INLINETILEOID);
            DBObjectPPair p(o, nullptr);
            inlineTiles.insert(p);
            (*it).setObject(o);
        }
}

void DBTCIndex::changeBOIdToIOId()
{
    for (auto it = myKeyObjects.begin(); it != myKeyObjects.end(); it++)
    {
        auto itit = inlineTiles.find((*it).getObject().getOId());
        if (itit != inlineTiles.end())
        {
            (*it).setObject(
                OId((*it).getObject().getOId().getCounter(), OId::INNEROID));
        }
    }
}

void DBTCIndex::removeInlineTile(InlineTile *it)
{
    auto itit = inlineTiles.find(it->getOId());
    if (itit != inlineTiles.end())
    {
        inlineTiles.erase(itit);
        setMappingHasChanged();
        setInlineTileHasChanged();
        ObjectBroker::deregisterTileIndexMapping(it->getOId(), myOId);
    }
    else
    {
        LTRACE << "deregisterInlineTile(" << it->getOId() << ") it not found";
    }
}

void DBTCIndex::addInlineTile(InlineTile *it)
{
    if (!_isLoaded) readInlineTiles();
    DBObjectPPair p(it->getOId(), it);
    inlineTiles.insert(p);
    ObjectBroker::registerTileIndexMapping(it->getOId(), myOId);
    setMappingHasChanged();
    setInlineTileHasChanged();
}

void DBTCIndex::insertInDb()
{
    if (isLeaf())
    {
        decideForInlining();
        if (inlineTileHasChanged || mappingHasChanged)
        {
            updateTileIndexMappings();
            insertBlob();
            storeTiles();
            changeBOIdToIOId();
        }
    }
    DBHierIndex::insertInDb();
    if (isLeaf()) changeIOIdToBOId();
}

void DBTCIndex::readFromDb()
{
    DBHierIndex::readFromDb();
    if (isLeaf())
    {
        registerIOIds();
        changeIOIdToBOId();
    }
    inlineTileHasChanged = false;
    mappingHasChanged = false;
}

void DBTCIndex::updateInDb()
{
    if (isLeaf()) decideForInlining();
    if (mappingHasChanged) updateTileIndexMappings();
    if (inlineTileHasChanged) storeTiles();
    if (inlineTiles.size() != 0) changeBOIdToIOId();
    DBHierIndex::updateInDb();
    if (isLeaf()) changeIOIdToBOId();
}

InlineTile *DBTCIndex::getInlineTile(const OId &itid)
{
    InlineTile *retval = nullptr;
    DBObjectPMap::iterator itit;
    if (!_isLoaded)
    {
        readInlineTiles();
    }
    itit = inlineTiles.find(itid);
    if (itit != inlineTiles.end())
    {
        retval = static_cast<InlineTile *>((*itit).second);
    }
    return retval;
}

void DBTCIndex::readyForRemoval(const OId &id)
{
    if (id.getType() == OId::INLINETILEOID)
    {
        DBObjectPMap::iterator itit;

        itit = inlineTiles.find(id);
        if (inlineTiles.end() != itit)
        {
            if (!_isLoaded)
            {
                readInlineTiles();
                itit = inlineTiles.find(id);
                ((*itit).second)->setCached(false);
                (static_cast<InlineTile *>((*itit).second))->outlineTile();
            }
            else
            {
                ((*itit).second)->setCached(false);
                (static_cast<InlineTile *>((*itit).second))->outlineTile();
            }
        }
    }
}

bool DBTCIndex::removeObject(const KeyObject &entry)
{
    if (isLeaf()) readyForRemoval(entry.getObject().getOId());
    bool found = DBHierIndex::removeObject(entry);
    return found;
}

bool DBTCIndex::removeObject(unsigned int pos)
{
    if (isLeaf())
        if (pos <= myKeyObjects.size())
            readyForRemoval(myKeyObjects[pos].getObject().getOId());
    bool found = DBHierIndex::removeObject(static_cast<unsigned int>(pos));
    return found;
}

void DBTCIndex::decideForInlining()
{
    if (isLeaf())
    {
        InlineTile *itile = nullptr;
        KeyObjectVector::iterator it;
        for (it = myKeyObjects.begin(); it != myKeyObjects.end(); it++)
        {
            LTRACE << " we do oid " << (*it);
            if ((*it).getObject().getOId().getType() == OId::INLINETILEOID)
            {
                if ((itile = static_cast<InlineTile *>(ObjectBroker::isInMemory(
                         (*it).getObject().getOId()))) != nullptr)
                {
                    LTRACE << "in memory";
                    // decide for inlineing
                    if (itile->isInlined())
                    {
                        LTRACE << "inlined";
                        if (itile->getSize() > StorageLayout::DefaultPCTMax)
                        {
                            LTRACE << "needs to be outlined";
                            itile->outlineTile();
                        }
                    }
                    else
                    {
                        LTRACE << "outlined";
                        if (itile->getSize() < StorageLayout::DefaultMinimalTileSize)
                        {
                            LTRACE << "needs to be inlined";
                            itile->inlineTile(myOId);
                        }
                    }
                }
                else
                {
                    LTRACE << "not in memory";
                }
            }
        }
    }
}
