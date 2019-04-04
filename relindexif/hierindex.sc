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
 *   Code with embedded SQL for PostgreSQL DBMS
 *
 *
 * COMMENTS:
 * - blobs contain 13c as first byte; this is not required here,
 *   but kept for data compatibility with other base DBMSs.
 *
 ************************************************************/

#include <cstring>
#include "debug-srv.hh"

#include "hierindex.hh"
#include "reladminif/sqlerror.hh"
#include "reladminif/sqlglobals.h"
#include "relblobif/blobtile.hh"
#include "indexmgr/keyobject.hh"
#include "reladminif/sqlitewrapper.hh"
#include <logging.hh>
#include <memory>

void
DBHierIndex::insertInDb()
{
    // old format a 13, new format >= 1009 (to align with dbrcindex.pgc)
    long long header = 1010;

    long long id2;
    r_Dimension dimension2;
    r_Bytes size2;
    long long parentid2;
    int indexsubtype2;

    // (0) --- prepare variables
    id2 = myOId.getCounter();
    dimension2 = myDomain.dimension();
    // size2 = myKeyObjects.size();
    size2 = getSize();
    indexsubtype2 = _isNode;

    if (parent.getType() == OId::INVALID)
    {
        parentid2 = 0;
    }
    else
    {
        parentid2 = parent;
    }

    // (1) -- set all buffers
    r_Bytes headersize = sizeof(header);

    //number of bytes for bounds for "size" entries and mydomain
    r_Bytes boundssize = sizeof(r_Range) * (size2 + 1) * dimension2;
    //number of bytes for fixes for "size" entries and mydomain
    r_Bytes fixessize = sizeof(char) * (size2 + 1) * dimension2;
    //number of bytes for ids of entries
    r_Bytes idssize = sizeof(OId::OIdCounter) * size2;
    //number of bytes for types of entries
    r_Bytes typessize = sizeof(char) * size2;
    //number of bytes for the dynamic data, plus 1 starter byte (see below)
    r_Bytes completesize = headersize + boundssize * 2 + fixessize * 2 + idssize + typessize;

    // HST After some testing of the new format all these allocations
    // should be removed.
    char *completebuffer = (char *) mymalloc(completesize);
    if (completebuffer == NULL)
    {
        LERROR << "DBHierIndex::insertInDb() cannot malloc buffer";
        throw r_Error(r_Error::r_Error_MemoryAllocation);
    }
    r_Range *upperboundsbuf = (r_Range *) mymalloc(boundssize);
    if (upperboundsbuf == NULL)
    {
        LERROR << "DBHierIndex::insertInDb() cannot malloc buffer";
        throw r_Error(r_Error::r_Error_MemoryAllocation);
    }
    r_Range *lowerboundsbuf = (r_Range *) mymalloc(boundssize);
    if (lowerboundsbuf == NULL)
    {
        LERROR << "DBHierIndex::insertInDb() cannot malloc buffer";
        throw r_Error(r_Error::r_Error_MemoryAllocation);
    }
    char *upperfixedbuf = (char *) mymalloc(fixessize);
    if (upperfixedbuf == NULL)
    {
        LERROR << "DBHierIndex::insertInDb() cannot malloc buffer";
        throw r_Error(r_Error::r_Error_MemoryAllocation);
    }
    char *lowerfixedbuf = (char *) mymalloc(fixessize);
    if (lowerfixedbuf == NULL)
    {
        LERROR << "DBHierIndex::insertInDb() cannot malloc buffer";
        throw r_Error(r_Error::r_Error_MemoryAllocation);
    }
    OId::OIdCounter *entryidsbuf = (OId::OIdCounter *)mymalloc(idssize);
    if (entryidsbuf == NULL)
    {
        LERROR << "DBHierIndex::insertInDb() cannot malloc buffer";
        throw r_Error(r_Error::r_Error_MemoryAllocation);
    }
    char *entrytypesbuf = (char *) mymalloc(typessize);
    if (entrytypesbuf == NULL)
    {
        LERROR << "DBHierIndex::insertInDb() cannot malloc buffer";
        throw r_Error(r_Error::r_Error_MemoryAllocation);
    }

    LTRACE << "complete=" << completesize << " bounds=" << boundssize << " fixes=" << fixessize << " ids=" << idssize << " types=" << typessize << ", size=" << size2 << " dimension=" << dimension2;

    myDomain.insertInDb(&(lowerboundsbuf[0]), &(upperboundsbuf[0]), &(lowerfixedbuf[0]), &(upperfixedbuf[0]));
    LTRACE << "domain " << myDomain << " stored as " << InlineMinterval(dimension2, &(lowerboundsbuf[0]), &(upperboundsbuf[0]), &(lowerfixedbuf[0]), &(upperfixedbuf[0]));
    //populate the buffers with data
    KeyObjectVector::iterator it = myKeyObjects.begin();
    InlineMinterval indom;
    for (long i = 0; i < size2; i++, it++)
    {
        indom = (*it).getDomain();
        indom.insertInDb(&(lowerboundsbuf[(i + 1) * dimension2]), &(upperboundsbuf[(i + 1) * dimension2]), &(lowerfixedbuf[(i + 1) * dimension2]), &(upperfixedbuf[(i + 1) * dimension2]));
        entryidsbuf[i] = (*it).getObject().getOId().getCounter();
        entrytypesbuf[i] = (char)(*it).getObject().getOId().getType();
        LTRACE << "entry " << entryidsbuf[i] << " " << (OId::OIdType)entrytypesbuf[i] << " at " << InlineMinterval(dimension2, &(lowerboundsbuf[(i + 1) * dimension2]), &(upperboundsbuf[(i + 1) * dimension2]), &(lowerfixedbuf[(i + 1) * dimension2]), &(upperfixedbuf[(i + 1) * dimension2]));
    }

    // write the buffers in the complete buffer, free all unnecessary buffers
    // OUTDATED this indirection is necessary because of memory alignement of longs...
    // this is only necessary for the old format, not the new format...

    // write the new header
    memcpy(&completebuffer[0], &header, sizeof(header));  //the first char must not be a \0 ??

    (void) memcpy(&completebuffer[headersize], lowerboundsbuf, boundssize);
    free(lowerboundsbuf);
    (void) memcpy(&completebuffer[boundssize + headersize], upperboundsbuf, boundssize);
    free(upperboundsbuf);
    (void) memcpy(&completebuffer[boundssize * 2 + headersize], lowerfixedbuf, fixessize);
    free(lowerfixedbuf);
    (void) memcpy(&completebuffer[boundssize * 2 + fixessize + headersize], upperfixedbuf, fixessize);
    free(upperfixedbuf);
    (void) memcpy(&completebuffer[boundssize * 2 + fixessize * 2 + headersize], entryidsbuf, idssize);
    free(entryidsbuf);
    (void) memcpy(&completebuffer[boundssize * 2 + fixessize * 2 + idssize + headersize], entrytypesbuf, typessize);
    free(entrytypesbuf);

    // (3) --- insert HIERIX tuple into db
    SQLiteQuery query("INSERT INTO RAS_HIERIX ( MDDObjIxOId, NumEntries, Dimension, ParentOId, IndexSubType, DynData ) VALUES ( %lld, %ld, %ld, %lld, %d, ? )",
                      id2, size2, dimension2, parentid2, indexsubtype2);
    query.bindBlob(completebuffer, static_cast<int>(completesize));
    query.execute();

    free(completebuffer); // free main buffer

    // (4) --- dbobject insert
    DBObject::insertInDb();

} // insertInDb()

void
DBHierIndex::readFromDb()
{

    // (0) --- prepare variables

    long long oid = myOId.getCounter();

    long long parentOid;
    r_Bytes dimension;
    r_Bytes numEntries;
    int indexSubType;
    r_Bytes blobSize = 0;
    std::unique_ptr<char[]> blobBuffer;

    // (1) --- fetch tuple from database

    SQLiteQuery query(
        "SELECT NumEntries, Dimension, ParentOId, IndexSubType, DynData "
        "FROM RAS_HIERIX WHERE MDDObjIxOId = %lld", oid);
    if (query.nextRow())
    {
        numEntries = static_cast<r_Bytes>(query.nextColumnInt());
        dimension = static_cast<r_Bytes>(query.nextColumnInt());
        parentOid = query.nextColumnLong();
        indexSubType = query.nextColumnInt();
        _isNode = indexSubType;
        // read blob
        const auto *tmpblobbuffer = query.nextColumnBlob();
        blobSize = static_cast<r_Bytes>(query.currColumnBytes());
        blobBuffer.reset(new char[blobSize]);
        memcpy(blobBuffer.get(), tmpblobbuffer, blobSize);
    }
    else
    {
        LERROR << "index entry: " << oid << " not found in the database.";
        throw r_Ebase_dbms(SQLITE_NOTFOUND, "index entry not found in the database.");
    }

    // (2) --- fill variables and buffers

    parent = parentOid ? OId(parentOid) : OId(0, OId::INVALID);

    // old format, contains 13 in first byte
    static const int BLOB_FORMAT_V1 = 8;
    static const int BLOB_FORMAT_V1_HEADER_SIZE = 1;
    static const int BLOB_FORMAT_V1_HEADER_MAGIC = 13;
    // OIDcounter is now long, but r_Range is still int
    static const int BLOB_FORMAT_V2 = 9;
    static const int BLOB_FORMAT_V2_HEADER_SIZE = 8;
    static const long long BLOB_FORMAT_V2_HEADER_MAGIC = 1009;
    // blobFormat == 10: r_Range is long as well
    static const int BLOB_FORMAT_V3 = 10;

    const size_t newIdSize = sizeof(OId::OIdCounter);
    size_t idSize = newIdSize;
    const size_t newBoundSize = sizeof(r_Range);
    size_t boundSize = newBoundSize;

    int blobFormat{};
    r_Bytes headerSize{};
    if (blobBuffer[0] == BLOB_FORMAT_V1_HEADER_MAGIC)
    {
        // old format
        blobFormat = BLOB_FORMAT_V1;
        idSize = sizeof(int);
        boundSize = sizeof(int);
        headerSize = BLOB_FORMAT_V1_HEADER_SIZE;
    }
    else
    {
        // new format (v2 or v3)
        headerSize = BLOB_FORMAT_V2_HEADER_SIZE;
        auto header = *reinterpret_cast<long long *>(&blobBuffer[0]);
        blobFormat = header == BLOB_FORMAT_V2_HEADER_MAGIC ? BLOB_FORMAT_V2
                     : BLOB_FORMAT_V3;
    }
    r_Bytes idsSize = idSize * numEntries;
    r_Bytes boundsSize = boundSize * (numEntries + 1) * dimension;
    r_Bytes newBoundsSize = newBoundSize * (numEntries + 1) * dimension;


    // number of bytes for fixes for "size" entries and mydomain
    r_Bytes fixesSize = sizeof(char) * (numEntries + 1) * dimension;
    // number of bytes for types of entries
    r_Bytes typesSize = sizeof(char) * numEntries;
    // number of bytes for the dynamic data
    r_Bytes completeSize =
        headerSize +
        boundsSize * 2 +
        fixesSize * 2 +
        idsSize + typesSize;

    LTRACE << "blob format: " << blobFormat;
    LTRACE << "complete=" << completeSize << " bounds=" << boundsSize
           << " fixes=" << fixesSize << " ids=" << idsSize
           << " types=" << typesSize << ", entries=" << numEntries
           << " dimension=" << dimension;

    if (completeSize != blobSize)  // this because I don't trust computations
    {
        LTRACE << "BLOB (" << oid << ") read: completeSize=" << completeSize
               << ", but blobSize=" << blobSize;
        throw r_Error(r_Error::r_Error_LimitsMismatch);
    }

    char *completeBuf = blobBuffer.get();
    completeBuf += headerSize;

    char *lowerBoundsBuf = &completeBuf[0];
    char *upperBoundsBuf = &completeBuf[boundsSize];
    char *lowerFixedBuf = &completeBuf[boundsSize * 2];
    char *upperFixedBuf = &completeBuf[boundsSize * 2 + fixesSize];
    char *entryIdsBuf = &completeBuf[boundsSize * 2 + fixesSize * 2];
    char *entryTypesBuf = &completeBuf[boundsSize * 2 + fixesSize * 2 + idsSize];

    // (4) --- copy data into buffers

#define GET_BOUND(buf) \
    (blobFormat <= BLOB_FORMAT_V2) ? static_cast<r_Range>(*reinterpret_cast<int*>(buf)) \
    : *reinterpret_cast<r_Range*>(buf)

    auto lowerBounds = std::unique_ptr<r_Range[]>(new r_Range[newBoundsSize]);
    auto upperBounds = std::unique_ptr<r_Range[]>(new r_Range[newBoundsSize]);

    char buf[sizeof(r_Range)];
    for (size_t i = 0; i < (numEntries + 1) * dimension; i++)
    {
        memcpy(buf, lowerBoundsBuf, boundSize);
        lowerBounds[i] = GET_BOUND(buf);
        lowerBoundsBuf += boundSize;

        memcpy(buf, upperBoundsBuf, boundSize);
        upperBounds[i] = GET_BOUND(buf);
        upperBoundsBuf += boundSize;
    }

    // rebuild the attributes from the buffers
    myDomain = InlineMinterval(dimension,
                               &lowerBounds[0], &upperBounds[0], lowerFixedBuf, upperFixedBuf);
    KeyObject theKey = KeyObject(DBObjectId(), myDomain);
    for (r_Bytes i = 0; i < numEntries; i++)
    {
        lowerFixedBuf += dimension;
        upperFixedBuf += dimension;
        theKey.setDomain(InlineMinterval(dimension,
                                         &lowerBounds[(i + 1) * dimension], &upperBounds[(i + 1) * dimension],
                                         lowerFixedBuf, upperFixedBuf));

        memcpy(buf, entryIdsBuf, idSize);
        auto entryId = (blobFormat == BLOB_FORMAT_V1)
                       ? static_cast<OId::OIdCounter>(*reinterpret_cast<unsigned int *>(buf))
                       : *reinterpret_cast<OId::OIdCounter *>(buf);
        entryIdsBuf += idSize;

        theKey.setObject(OId(entryId, (OId::OIdType) entryTypesBuf[i]));
        myKeyObjects.push_back(theKey);
    }

    // (5) --- fill dbobject
    DBObject::readFromDb();

}  // readFromDb()

void
DBHierIndex::updateInDb()
{
    long long header = 1010;

    long long id4;
    r_Bytes dimension4;
    r_Bytes size4;
    long long parentid4;
    int indexsubtype4;

    // (0) --- prepare variables
    id4 = myOId.getCounter();
    indexsubtype4 = _isNode;
    dimension4 = myDomain.dimension();
    size4 = myKeyObjects.size();
    if (parent.getType() == OId::INVALID)
    {
        parentid4 = 0;
    }
    else
    {
        parentid4 = parent;
    }

    // (1) --- prepare buffer
    // number of bytes for header
    r_Bytes headersize = sizeof(header);

    //number of bytes for bounds for "size" entries and mydomain
    r_Bytes boundssize = sizeof(r_Range) * (size4 + 1) * dimension4;
    //number of bytes for fixes for "size" entries and mydomain
    r_Bytes fixessize = sizeof(char) * (size4 + 1) * dimension4;
    //number of bytes for ids of entries
    r_Bytes idssize = sizeof(OId::OIdCounter) * size4;
    //number of bytes for types of entries
    r_Bytes typessize = sizeof(char) * size4;
    //number of bytes for the dynamic data; 1 starter byte!
    r_Bytes completesize = headersize + boundssize * 2 + fixessize * 2 + idssize + typessize;

    char *completebuffer = (char *) mymalloc(completesize);
    r_Range *upperboundsbuf = (r_Range *) mymalloc(boundssize);
    r_Range *lowerboundsbuf = (r_Range *) mymalloc(boundssize);
    char *upperfixedbuf = (char *) mymalloc(fixessize);
    char *lowerfixedbuf = (char *) mymalloc(fixessize);
    OId::OIdCounter *entryidsbuf = (OId::OIdCounter *)mymalloc(idssize);
    char *entrytypesbuf = (char *) mymalloc(typessize);

    LTRACE << "Updating index in rasbase with oid " << myOId;

    // populate the buffers with data
    myDomain.insertInDb(&(lowerboundsbuf[0]), &(upperboundsbuf[0]), &(lowerfixedbuf[0]), &(upperfixedbuf[0]));
    LTRACE << "domain " << myDomain << " stored as " << InlineMinterval(dimension4, &(lowerboundsbuf[0]), &(upperboundsbuf[0]), &(lowerfixedbuf[0]), &(upperfixedbuf[0]));

    KeyObjectVector::iterator it = myKeyObjects.begin();
    InlineMinterval indom;
    for (long i = 0; i < size4; i++, it++)
    {
        indom = (*it).getDomain();
        indom.insertInDb(&(lowerboundsbuf[(i + 1) * dimension4]), &(upperboundsbuf[(i + 1) * dimension4]), &(lowerfixedbuf[(i + 1) * dimension4]), &(upperfixedbuf[(i + 1) * dimension4]));
        entryidsbuf[i] = (*it).getObject().getOId().getCounter();
        entrytypesbuf[i] = (char)(*it).getObject().getOId().getType();
        LTRACE << "entry " << entryidsbuf[i] << " " << (OId::OIdType)entrytypesbuf[i] << " at " << InlineMinterval(dimension4, &(lowerboundsbuf[(i + 1) * dimension4]), &(upperboundsbuf[(i + 1) * dimension4]), &(lowerfixedbuf[(i + 1) * dimension4]), &(upperfixedbuf[(i + 1) * dimension4]));
    }

    LTRACE << "complete=" << completesize << " bounds=" << boundssize << " fixes=" << fixessize << " ids=" << idssize << " types=" << typessize << ", size=" << size4 << " dimension=" << dimension4;

    // write the buffers in the complete buffer, plus starter byte
    // OUTDATED this indirection is necessary because of memory alignement of longs...
    memcpy(&completebuffer[0], &header, headersize);

    memcpy(&completebuffer[headersize], lowerboundsbuf, boundssize);
    free(lowerboundsbuf);
    memcpy(&completebuffer[boundssize + headersize], upperboundsbuf, boundssize);
    free(upperboundsbuf);
    memcpy(&completebuffer[boundssize * 2 + headersize], lowerfixedbuf, fixessize);
    free(lowerfixedbuf);
    memcpy(&completebuffer[boundssize * 2 + fixessize + headersize], upperfixedbuf, fixessize);
    free(upperfixedbuf);
    memcpy(&completebuffer[boundssize * 2 + fixessize * 2 + headersize], entryidsbuf, idssize);
    free(entryidsbuf);
    memcpy(&completebuffer[boundssize * 2 + fixessize * 2 + idssize + headersize], entrytypesbuf, typessize);
    free(entrytypesbuf);

    // (3) -- update HierIx entry
    SQLiteQuery query("UPDATE RAS_HIERIX SET NumEntries = %ld, Dimension = %ld, ParentOId = %lld, IndexSubType = %d, DynData = ? WHERE MDDObjIxOId = %lld",
                      size4, dimension4, parentid4, indexsubtype4, id4);
    query.bindBlob(completebuffer, static_cast<int>(completesize));
    query.execute();

    free(completebuffer);
    completebuffer = NULL;

    // (4) --- dbobject update
    DBObject::updateInDb();

} // updateInDb()

void
DBHierIndex::deleteFromDb()
{
    long long id = myOId.getCounter();

    OId oi;
    while (!myKeyObjects.empty())
    {
        oi = myKeyObjects.begin()->getObject().getOId();
        if ((oi.getType() == OId::BLOBOID) || (oi.getType() == OId::INLINETILEOID))
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
                LWARNING << "Cannot delete invalid object with OId " << oi << ": " << err.what();
            }
            catch (...)
            {
                LWARNING << "Cannot delete invalid object with OId " << oi << ": unknown exception occurred.";
            }
        }
        myKeyObjects.erase(myKeyObjects.begin());
    }

    SQLiteQuery::executeWithParams("DELETE FROM RAS_HIERIX WHERE MDDObjIxOId = %lld", id);
    DBObject::deleteFromDb();

} // deleteFromDb()
