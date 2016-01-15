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
#include <easylogging++.h>

void
DBHierIndex::insertInDb() throw (r_Error)
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
        parentid2 = 0;
    else
        parentid2 = parent;

    // (1) -- set all buffers
    r_Bytes headersize = sizeof (header);

    //number of bytes for bounds for "size" entries and mydomain
    r_Bytes boundssize = sizeof (r_Range) * (size2 + 1) * dimension2;
    //number of bytes for fixes for "size" entries and mydomain
    r_Bytes fixessize = sizeof (char) * (size2 + 1) * dimension2;
    //number of bytes for ids of entries
    r_Bytes idssize = sizeof (OId::OIdCounter) * size2;
    //number of bytes for types of entries
    r_Bytes typessize = sizeof (char) * size2;
    //number of bytes for the dynamic data, plus 1 starter byte (see below)
    r_Bytes completesize = headersize + boundssize * 2 + fixessize * 2 + idssize + typessize;

    // HST After some testing of the new format all these allocations
    // should be removed.
    char* completebuffer = (char*) mymalloc(completesize);
    if (completebuffer == NULL)
    {
        LFATAL << "DBHierIndex::insertInDb() cannot malloc buffer";
        throw r_Error(r_Error::r_Error_MemoryAllocation);
    }
    r_Range* upperboundsbuf = (r_Range*) mymalloc(boundssize);
    if (upperboundsbuf == NULL)
    {
        LFATAL << "DBHierIndex::insertInDb() cannot malloc buffer";
        throw r_Error(r_Error::r_Error_MemoryAllocation);
    }
    r_Range* lowerboundsbuf = (r_Range*) mymalloc(boundssize);
    if (lowerboundsbuf == NULL)
    {
        LFATAL << "DBHierIndex::insertInDb() cannot malloc buffer";
        throw r_Error(r_Error::r_Error_MemoryAllocation);
    }
    char* upperfixedbuf = (char*) mymalloc(fixessize);
    if (upperfixedbuf == NULL)
    {
        LFATAL << "DBHierIndex::insertInDb() cannot malloc buffer";
        throw r_Error(r_Error::r_Error_MemoryAllocation);
    }
    char* lowerfixedbuf = (char*) mymalloc(fixessize);
    if (lowerfixedbuf == NULL)
    {
        LFATAL << "DBHierIndex::insertInDb() cannot malloc buffer";
        throw r_Error(r_Error::r_Error_MemoryAllocation);
    }
    OId::OIdCounter* entryidsbuf = (OId::OIdCounter*)mymalloc(idssize);
    if (entryidsbuf == NULL)
    {
        LFATAL << "DBHierIndex::insertInDb() cannot malloc buffer";
        throw r_Error(r_Error::r_Error_MemoryAllocation);
    }
    char* entrytypesbuf = (char*) mymalloc(typessize);
    if (entrytypesbuf == NULL)
    {
        LFATAL << "DBHierIndex::insertInDb() cannot malloc buffer";
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
        entrytypesbuf[i] = (char) (*it).getObject().getOId().getType();
        LTRACE << "entry " << entryidsbuf[i] << " " << (OId::OIdType)entrytypesbuf[i] << " at " << InlineMinterval(dimension2, &(lowerboundsbuf[(i + 1) * dimension2]), &(upperboundsbuf[(i + 1) * dimension2]), &(lowerfixedbuf[(i + 1) * dimension2]), &(upperfixedbuf[(i + 1) * dimension2]));
    }

    // write the buffers in the complete buffer, free all unnecessary buffers
    // OUTDATED this indirection is necessary because of memory alignement of longs...
    // this is only necessary for the old format, not the new format...

    // write the new header
    memcpy(&completebuffer[0], &header, sizeof (header)); //the first char must not be a \0 ??

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
DBHierIndex::readFromDb() throw (r_Error)
{
#ifdef RMANBENCHMARK
    DBObject::readTimer.resume();
#endif
    r_Bytes headersize;
    long long header;
    int blobformat;

    long long id1;
    long long parentid1;
    r_Bytes dimension1;
    r_Bytes size1;
    int indexsubtype1;
    r_Bytes blobsize = 0;
    char* blobbuffer = NULL;

    // (0) --- prepare variables
    id1 = myOId.getCounter();

    // (1) --- fetch tuple from database
    SQLiteQuery query("SELECT NumEntries, Dimension, ParentOId, IndexSubType, DynData FROM RAS_HIERIX WHERE MDDObjIxOId = %lld", id1);
    if (query.nextRow())
    {
        size1 = static_cast<r_Bytes>(query.nextColumnInt());
        dimension1 = static_cast<r_Bytes>(query.nextColumnInt());
        parentid1 = query.nextColumnLong();
        indexsubtype1 = query.nextColumnInt();

        // read blob
        char* tmpblobbuffer = query.nextColumnBlob();
        blobsize = static_cast<r_Bytes>(query.currColumnBytes());
        blobbuffer = (char*) mymalloc(blobsize);
        memcpy(blobbuffer, tmpblobbuffer, blobsize);
    }
    else
    {
        LFATAL << "DBHierIndex::readFromDb() - index entry: " << id1 << " not found in the database.";
        throw r_Ebase_dbms(SQLITE_NOTFOUND, "index entry not found in the database.");
    }

    // (2) --- fill variables and buffers

    _isNode = indexsubtype1;

    if (parentid1)
        parent = OId(parentid1);
    else
        parent = OId(0, OId::INVALID);

    r_Bytes idssize = 0;
    r_Bytes newidssize = 0;
    r_Bytes boundssize = 0;
    r_Bytes newboundssize = 0;

    // blobformat == 8: old format, contains 13 in first byte
    // blobformat == 9: OIDcounter is now long, but r_Range is still int
    // blobformat == 10: r_Range is long as well

    LDEBUG << "blobbuffer[0]: " << blobbuffer[0];
    if (blobbuffer[0] == 13)
    { // old format
        blobformat = 8;
        idssize = sizeof (int) * size1;
        newidssize = sizeof (OId::OIdCounter) * size1;
        //number of bytes for types of entries
        //number of bytes for bounds for "size" entries and mydomain
        boundssize = sizeof (int) * (size1 + 1) * dimension1;
        newboundssize = sizeof (r_Range) * (size1 + 1) * dimension1;
        headersize = 1;
    }
    else
    {
        headersize = 8;
        memcpy(&header, &blobbuffer[0], headersize);
        if (header == 1009)
        {
            blobformat = 9;
            idssize = sizeof (OId::OIdCounter) * size1;
            boundssize = sizeof (int) * (size1 + 1) * dimension1;
            newboundssize = sizeof (r_Range) * (size1 + 1) * dimension1;
        }
        else
        {
            blobformat = 10;
            idssize = sizeof (OId::OIdCounter) * size1;
            //number of bytes for types of entries
            boundssize = sizeof (r_Range) * (size1 + 1) * dimension1;
        }
    }

    LDEBUG << "blobformat: " << blobformat;

    //number of bytes for fixes for "size" entries and mydomain
    r_Bytes fixessize = sizeof (char) * (size1 + 1) * dimension1;
    //number of bytes for types of entries
    r_Bytes typessize = sizeof (char) * size1;
    //number of bytes for the dynamic data
    r_Bytes completesize = headersize + boundssize * 2 + fixessize * 2 + idssize + typessize;

    LTRACE << "complete=" << completesize << " bounds=" << boundssize << " fixes=" << fixessize << " ids=" << idssize << " types=" << typessize << ", size=" << size1 << " dimension=" << dimension1;

    char* completebuffer = blobbuffer;

    OId::OIdCounter* entryidsbuf = NULL;
    r_Range* upperboundsbuf = NULL;
    r_Range* lowerboundsbuf = NULL;
    char* upperfixedbuf = NULL;
    char* lowerfixedbuf = NULL;

    int* oldupperboundsbuf = NULL;
    int* oldlowerboundsbuf = NULL;
    unsigned int* oldentryidsbuf = NULL;

    if (blobformat == 8)
    {
        // old entries need to be allocated because the data is not aligned
        oldentryidsbuf = (unsigned int*) mymalloc(idssize);
        oldupperboundsbuf = (int*) mymalloc(boundssize);
        oldlowerboundsbuf = (int*) mymalloc(boundssize);

        // new entries need to be allocated because blobbuffer is smaller
        entryidsbuf = (long long*) mymalloc(newidssize);
        upperboundsbuf = (long long*) mymalloc(newboundssize);
        lowerboundsbuf = (long long*) mymalloc(newboundssize);
    }
    else if (blobformat == 9)
    {
        // r_Range is still an int
        oldupperboundsbuf = (int*) mymalloc(boundssize);
        oldlowerboundsbuf = (int*) mymalloc(boundssize);
        upperboundsbuf = (long long*) mymalloc(newboundssize);
        lowerboundsbuf = (long long*) mymalloc(newboundssize);
    }
    else
    {
        // can be just pointers into blobbuffer
    }

    if (completesize != blobsize) // this because I don't trust computations
    {
        LTRACE << "BLOB (" << id1 << ") read: xcompletesize=" << completesize << ", but blobsize=" << blobsize;
        throw r_Error(r_Error::r_Error_LimitsMismatch);
    }

    // (4) --- copy data into buffers
    if (blobformat <= 9)
    { // for blobformat 8 and 9 were only
        memcpy(oldlowerboundsbuf, &completebuffer[headersize], boundssize);
        memcpy(oldupperboundsbuf, &completebuffer[boundssize + headersize], boundssize);
        // we need to copy all values over
        for (long i = 0; i < (size1 + 1) * dimension1; i++)
        {
            lowerboundsbuf[i] = (r_Range) oldlowerboundsbuf[i];
            upperboundsbuf[i] = (r_Range) oldupperboundsbuf[i];
        }
    }
    else
    {
        lowerboundsbuf = (r_Range*) & completebuffer[headersize];
        upperboundsbuf = (r_Range*) & completebuffer[boundssize + headersize];
    }

    lowerfixedbuf = &completebuffer[boundssize * 2 + headersize];
    upperfixedbuf = &completebuffer[boundssize * 2 + fixessize + headersize];

    if (blobformat == 8)
    {
        memcpy(oldentryidsbuf, &completebuffer[boundssize * 2 + fixessize * 2 + headersize], idssize);
        // we need to copy all values over
        for (long i = 0; i < size1; i++)
        {
            entryidsbuf[i] = (OId::OIdCounter) oldentryidsbuf[i];
        }
    }
    else
    {
        lowerfixedbuf = &completebuffer[boundssize * 2 + headersize];
        upperfixedbuf = &completebuffer[boundssize * 2 + fixessize + headersize];
        entryidsbuf = (OId::OIdCounter*) & completebuffer[boundssize * 2 + fixessize * 2 + headersize];
    }

    char *entrytypesbuf = &completebuffer[boundssize * 2 + fixessize * 2 + idssize + headersize];

    // rebuild the attributes from the buffers
    long i = 0;
    myDomain = InlineMinterval(dimension1, &(lowerboundsbuf[0]), &(upperboundsbuf[0]), &(lowerfixedbuf[0]), &(upperfixedbuf[i * dimension1]));
    LTRACE << "domain " << myDomain << " constructed from " << InlineMinterval(dimension1, &(lowerboundsbuf[0]), &(upperboundsbuf[0]), &(lowerfixedbuf[0]), &(upperfixedbuf[0]));
    KeyObject theKey = KeyObject(DBObjectId(), myDomain);
    for (i = 0; i < size1; i++)
    {
        theKey.setDomain(InlineMinterval(dimension1, &(lowerboundsbuf[(i + 1) * dimension1]), &(upperboundsbuf[(i + 1) * dimension1]), &(lowerfixedbuf[(i + 1) * dimension1]), &(upperfixedbuf[(i + 1) * dimension1])));
        theKey.setObject(OId(entryidsbuf[i], (OId::OIdType)entrytypesbuf[i]));
        myKeyObjects.push_back(theKey);
        LTRACE << "entry " << entryidsbuf[i] << " " << (OId::OIdType)entrytypesbuf[i] << " at " << InlineMinterval(dimension1, &(lowerboundsbuf[(i + 1) * dimension1]), &(upperboundsbuf[(i + 1) * dimension1]), &(lowerfixedbuf[(i + 1) * dimension1]), &(upperfixedbuf[(i + 1) * dimension1]));
    }

    // we only needed to allocated bounds for format 8 and 9
    if (blobformat <= 9)
    {
        free(upperboundsbuf);
        free(lowerboundsbuf);
        free(oldupperboundsbuf);
        free(oldlowerboundsbuf);
    }

    // we only need to allocate entries for format 8
    if (blobformat == 8)
    {
        free(oldentryidsbuf);
        free(entryidsbuf);
    }

    // Expand r_Range to long long
    free(completebuffer);
    completebuffer = NULL;

#ifdef RMANBENCHMARK
    DBObject::readTimer.pause();
#endif

    // (5) --- fill dbobject
    DBObject::readFromDb();

} // readFromDb()

void
DBHierIndex::updateInDb() throw (r_Error)
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
        parentid4 = 0;
    else
        parentid4 = parent;

    // (1) --- prepare buffer
    // number of bytes for header
    r_Bytes headersize = sizeof (header);

    //number of bytes for bounds for "size" entries and mydomain
    r_Bytes boundssize = sizeof (r_Range) * (size4 + 1) * dimension4;
    //number of bytes for fixes for "size" entries and mydomain
    r_Bytes fixessize = sizeof (char) * (size4 + 1) * dimension4;
    //number of bytes for ids of entries
    r_Bytes idssize = sizeof (OId::OIdCounter) * size4;
    //number of bytes for types of entries
    r_Bytes typessize = sizeof (char) * size4;
    //number of bytes for the dynamic data; 1 starter byte!
    r_Bytes completesize = headersize + boundssize * 2 + fixessize * 2 + idssize + typessize;

    char* completebuffer = (char*) mymalloc(completesize);
    r_Range* upperboundsbuf = (r_Range*) mymalloc(boundssize);
    r_Range* lowerboundsbuf = (r_Range*) mymalloc(boundssize);
    char* upperfixedbuf = (char*) mymalloc(fixessize);
    char* lowerfixedbuf = (char*) mymalloc(fixessize);
    OId::OIdCounter* entryidsbuf = (OId::OIdCounter*)mymalloc(idssize);
    char* entrytypesbuf = (char*) mymalloc(typessize);

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
        entrytypesbuf[i] = (char) (*it).getObject().getOId().getType();
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
DBHierIndex::deleteFromDb() throw (r_Error)
{
    long long id = myOId.getCounter();

    OId oi;
    while (!myKeyObjects.empty())
    {
        oi = myKeyObjects.begin()->getObject().getOId();
        DBObjectId dbo;
        if ((oi.getType() == OId::BLOBOID) || (oi.getType() == OId::INLINETILEOID))
        {
            BLOBTile::kill(oi);
        }
        else
        {
            dbo = DBObjectId(oi);
            if (!dbo.is_null())
            {
                dbo->setCached(false);
                dbo->setPersistent(false);
                dbo = (unsigned int) 0;
            }
        }
        myKeyObjects.erase(myKeyObjects.begin());
    }

    SQLiteQuery::executeWithParams("DELETE FROM RAS_HIERIX WHERE MDDObjIxOId = %lld", id);
    DBObject::deleteFromDb();

} // deleteFromDb()
