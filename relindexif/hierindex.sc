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

#include "hierindex.hh"
#include "relblobif/blobtile.hh"
#include "indexmgr/keyobject.hh"
#include "reladminif/sqlitewrapper.hh"
#include <logging.hh>
#include <memory>
#include <fmt/core.h>


void DBHierIndex::insertInDb()
{
    int completeSize;
    long long parentid;
    auto completeBuf = writeToBlobBuffer(completeSize, parentid);

    SQLiteQuery query(fmt::format(
        "INSERT INTO RAS_HIERIX ( MDDObjIxOId, NumEntries, Dimension, ParentOId, "
        "IndexSubType, DynData ) VALUES ( {}, {}, {}, {}, {}, ? )",
        myOId.getCounter(), getSize(), myDomain.dimension(), parentid, int(_isNode)));
    query.bindBlob(completeBuf.get(), completeSize);
    query.execute();

    DBObject::insertInDb();
}

void DBHierIndex::readFromDb()
{
    SQLiteQuery query(fmt::format(
        "SELECT NumEntries, Dimension, ParentOId, IndexSubType, DynData "
        "FROM RAS_HIERIX WHERE MDDObjIxOId = {}", myOId.getCounter()));
    if (query.nextRow())
    {
        auto numEntries = static_cast<r_Bytes>(query.nextColumnInt());
        auto dimension = static_cast<r_Dimension>(query.nextColumnInt());
        auto parentOid = query.nextColumnLong();
        _isNode = query.nextColumnInt();
        
        // read blob
        const char *tmpblobbuffer = query.nextColumnBlob();
        auto blobSize = static_cast<r_Bytes>(query.currColumnBytes());
        
        readFromBlobBuffer(numEntries, dimension, parentOid, tmpblobbuffer, blobSize);
    }
    else
    {
        LERROR << "index entry: " << myOId.getCounter() << " not found in the database.";
        throw r_Ebase_dbms(SQLITE_NOTFOUND, "index entry not found in the database.");
    }
    DBObject::readFromDb();
}

void DBHierIndex::updateInDb()
{
    int completeSize;
    long long parentid;
    auto completeBuf = writeToBlobBuffer(completeSize, parentid);

    SQLiteQuery query(fmt::format(
        "UPDATE RAS_HIERIX SET NumEntries = {}, Dimension = {}, ParentOId = {}, "
        "IndexSubType = {}, DynData = ? WHERE MDDObjIxOId = {}",
        getSize(), myDomain.dimension(), parentid, int(_isNode), myOId.getCounter()));
    query.bindBlob(completeBuf.get(), completeSize);
    query.execute();
    
    DBObject::updateInDb();
}

void DBHierIndex::deleteFromDb()
{
    while (!myKeyObjects.empty())
    {
        auto oi = myKeyObjects.begin()->getObject().getOId();
        if (oi.getType() == OId::BLOBOID || oi.getType() == OId::INLINETILEOID)
        {
            BLOBTile::kill(oi);
        }
        else
        {
            try
            {
                DBObjectId dbo(oi);
                if (!dbo.is_null())
                {
                    dbo->setCached(false);
                    dbo->setPersistent(false);
                    dbo = (unsigned int) 0;
                }
            }
            catch (const r_Error &err)
            {
                LWARNING << "Cannot delete object with OId " << oi 
                         << ": " << err.what();
            }
            catch (...)
            {
                LWARNING << "Cannot delete object with OId " << oi 
                         << ": unknown exception occurred.";
            }
        }
        myKeyObjects.erase(myKeyObjects.begin());
    }

    SQLiteQuery::execute(fmt::format("DELETE FROM RAS_HIERIX WHERE MDDObjIxOId = {}", 
                                     myOId.getCounter()));
    DBObject::deleteFromDb();

} // deleteFromDb()
