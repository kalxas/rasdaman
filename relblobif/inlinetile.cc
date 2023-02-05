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

#include "inlinetile.hh"
#include "blobtile.hh"              // for BLOBTile
#include "reladminif/dbobject.hh"   // for DBObject
#include "relindexif/dbtcindex.hh"  // for DBTCIndex
#include "relindexif/indexid.hh"    // for DBTCIndexId
#include "storagemgr/sstoragelayout.hh" // for StorageLayout

#include "logging.hh"               // for LTRACE
#include <cstring>                  // for memcpy
#include <iostream>                 // for operator<<, ostream, endl

using std::endl;
using std::vector;

InlineTile::InlineTile(r_Data_Format dataformat)
    : BLOBTile(dataformat)
{
    objecttype = OId::INLINETILEOID;
}

InlineTile::InlineTile(r_Bytes newSize, r_Data_Format dataformat)
    : BLOBTile(newSize, dataformat)
{
    objecttype = OId::INLINETILEOID;
}

InlineTile::InlineTile(r_Bytes newSize, char c,
                       r_Data_Format dataformat)
    : BLOBTile(newSize, c, dataformat)
{
    objecttype = OId::INLINETILEOID;
}

InlineTile::InlineTile(r_Bytes newSize, const char *newCells,
                       r_Data_Format dataformat)
    : BLOBTile(newSize, newCells, dataformat)
{
    objecttype = OId::INLINETILEOID;
}

InlineTile::InlineTile(r_Bytes newSize, bool takeOwnershipOfNewCells,
                       char *newCells, r_Data_Format dataformat)
    : BLOBTile(newSize, takeOwnershipOfNewCells, newCells, dataformat)
{
    objecttype = OId::INLINETILEOID;
}

InlineTile::InlineTile(const OId &id) : BLOBTile(id)
{
    objecttype = OId::INLINETILEOID;
}

void InlineTile::printStatus(unsigned int level, std::ostream &stream) const
{
    auto *indent = new char[level * 2 + 1];
    for (unsigned int j = 0; j < level * 2; j++)
    {
        indent[j] = ' ';
    }
    indent[level * 2] = '\0';

    stream << indent << "InlineTile ";
    if (isInlined())
    {
        stream << "is Inlined at " << myIndexOId << endl;
    }
    else
    {
        stream << "is not Inlined " << endl;
    }
    BLOBTile::printStatus(level + 1, stream);
    delete[] indent;
}

void InlineTile::destroy()
{
    if (isCached())
    {
        return;
    }
    else
    {
        DBObject::destroy();
    }
}

bool InlineTile::isCached() const
{
    char retval = true;
    // not previously cached
    if (!_isCached)
    {
        if (!isInlined())
        {
            // outlined
            if (getSize() > StorageLayout::DefaultMinimalTileSize)  // size is ok
            {
                retval = false;
            }
        }
        else    // inlined
        {
            if (getSize() < StorageLayout::DefaultPCTMax)  // size is ok
            {
                retval = false;
            }
        }
    }
    return retval;
}

void InlineTile::setModified()
{
    DBObject::setModified();
    if (isInlined())
    {
        DBTCIndexId t(myIndexOId);
        t->setInlineTileHasChanged();
    }
}

const OId &InlineTile::getIndexOId() const
{
    return myIndexOId;
}

void InlineTile::setIndexOId(const OId &oid)
{
    myIndexOId = oid;
}

r_Bytes InlineTile::getStorageSize() const
{
    return size + sizeof(r_Bytes) + sizeof(r_Data_Format) + sizeof(OId);
}

void InlineTile::outlineTile()
{
    DBTCIndexId t(myIndexOId);
    t->removeInlineTile(this);
    setIndexOId(OId(0, OId::INVALID));
    _isInDatabase = false;
    _isModified = true;
}

void InlineTile::inlineTile(const OId &ixId)
{
    setIndexOId(ixId);
    DBTCIndexId t(myIndexOId);
    t->addInlineTile(this);
    if (_isInDatabase)
    {
        bool pers = _isPersistent;
        bool modi = _isModified;
        BLOBTile::deleteFromDb();
        _isPersistent = pers;
        _isModified = modi;
    }
}

bool InlineTile::isInlined() const
{
    return (myIndexOId.getType() == OId::DBTCINDEXOID);
}

char *InlineTile::insertInMemBlock(char *thecontents)
{
    // store size of blob
    memcpy(thecontents, &size, sizeof(r_Bytes));
    thecontents = thecontents + sizeof(r_Bytes);

    // store the dataformat
    memcpy(thecontents, &dataFormat, sizeof(r_Data_Format));
    thecontents = thecontents + sizeof(r_Data_Format);

    // store my own oid
    memcpy(thecontents, &myOId, sizeof(OId));
    thecontents = thecontents + sizeof(OId);

    // store the blob
    memcpy(thecontents, cells, size * sizeof(char));
    thecontents = thecontents + size * sizeof(char);
    
    DBObject::updateInDb();
    return thecontents;
}


