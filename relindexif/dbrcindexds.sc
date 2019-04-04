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
 *
 ************************************************************/

#include <cstring>
#include "debug-srv.hh"

#include "dbrcindexds.hh"
#include "reladminif/sqlerror.hh"
#include "reladminif/sqlglobals.h"
#include "reladminif/sqlitewrapper.hh"
#include <logging.hh>

// container size for index node
// BEWARE: keep these parameters always consistent!
#define BYTES_PER_TUPLE 3990
#define SQL_BYTES_PER_TUPLE 3991;

r_Bytes
DBRCIndexDS::BytesPerTuple = BYTES_PER_TUPLE;

void
DBRCIndexDS::insertInDb()
{
    int header = 1010;
    int headersize = 4;

    long long id2;
    int count2;
    long blobOid;
    char pgQuery[SQL_QUERY_BUFFER_SIZE]; // prelim

    // alternative solution for now:

    // (1) --- prepare buffer
    id2 = myOId;
    r_Dimension dimension = myDomain.dimension();

    //number of bytes for bounds in 1 minterval
    r_Bytes boundssize = sizeof(r_Range) * dimension;
    //number of bytes for fixes in 1 minterval
    r_Bytes fixessize = sizeof(char) * dimension;
    //number of bytes for the dynamic data
    r_Bytes completesize = sizeof(header) + sizeof(r_Dimension) + sizeof(long long)
                           + sizeof(OId::OIdCounter) + sizeof(OId::OIdCounter)
                           + boundssize * 2 + fixessize * 2;

    char *completebuffer = (char *) mymalloc(completesize);
    // At a later stage get rid of all the unnecessary mallocs and memcpys,
    // but first make sure that everything works as expected

    r_Range *upperboundsbuf = (r_Range *) mymalloc(boundssize);
    r_Range *lowerboundsbuf = (r_Range *) mymalloc(boundssize);
    char *upperfixedbuf = (char *) mymalloc(fixessize);
    char *lowerfixedbuf = (char *) mymalloc(fixessize);

    LTRACE << "complete " << completesize << " bounds " << boundssize << " fixes " << fixessize;
    LDEBUG << "DBRCIndexDS: complete " << completesize << " bounds " << boundssize << " fixes " << fixessize;

    // insert myDomain in buffers
    myDomain.insertInDb(&(lowerboundsbuf[0]), &(upperboundsbuf[0]), &(lowerfixedbuf[0]), &(upperfixedbuf[0]));
    LTRACE << "domain " << myDomain << " stored as " << InlineMinterval(dimension, &(lowerboundsbuf[0]), &(upperboundsbuf[0]), &(lowerfixedbuf[0]), &(upperfixedbuf[0]));
    LDEBUG << "DBRCIndexDS: domain " << myDomain << " stored as " << InlineMinterval(dimension, &(lowerboundsbuf[0]), &(upperboundsbuf[0]), &(lowerfixedbuf[0]), &(upperfixedbuf[0]));

    char *insertionpointer = completebuffer;

    // write the buffers in the complete buffer
    // this indirection is necessary because of memory alignment of longs...
    // insert dimension

    memcpy(insertionpointer, &header, sizeof(header));
    insertionpointer = insertionpointer + sizeof(header);

    memcpy(insertionpointer, &dimension, sizeof(r_Dimension));
    insertionpointer = insertionpointer + sizeof(r_Dimension);

    // insert oid type
    long long tmpMyBaseOIdType;
    tmpMyBaseOIdType = myBaseOIdType;
    memcpy(insertionpointer, &tmpMyBaseOIdType, sizeof(long long));
    insertionpointer = insertionpointer + sizeof(long long);

    // insert oid counter
    memcpy(insertionpointer, &myBaseCounter, sizeof(OId::OIdCounter));
    insertionpointer = insertionpointer + sizeof(OId::OIdCounter);

    // insert oid counter
    memcpy(insertionpointer, &mySize, sizeof(OId::OIdCounter));
    insertionpointer = insertionpointer + sizeof(OId::OIdCounter);

    // insert domains
    memcpy(insertionpointer, lowerboundsbuf, boundssize);
    insertionpointer = insertionpointer + boundssize;
    free(lowerboundsbuf);

    memcpy(insertionpointer, upperboundsbuf, boundssize);
    insertionpointer = insertionpointer + boundssize;
    free(upperboundsbuf);

    memcpy(insertionpointer, lowerfixedbuf, fixessize);
    insertionpointer = insertionpointer + fixessize;
    free(lowerfixedbuf);

    memcpy(insertionpointer, upperfixedbuf, fixessize);
    free(upperfixedbuf);

#ifdef RMANDEBUG        // dump low-level blob byte string
    {
        char printbuf[10000];
        (void) sprintf(printbuf, "DBRCIndexDS::insertInDb(): [%d]", completesize);
#if 0   // extra verbose output: dump buffer
        char bytebuf[3];
        for (long i = 0; i < completesize; i++)
        {
            (void) sprintf(bytebuf, " %2X", (unsigned char) completebuffer[i]);
            strcat(printbuf, bytebuf);
        }
#endif // 0
        LDEBUG << printbuf;
    }
#endif //RMANDEBUG

    // (3) --- insert HIERIX tuple into db
    count2 = 0; // we only have one entry
    SQLiteQuery query("INSERT INTO RAS_RCINDEXDYN ( Id, Count, DynData ) VALUES ( %lld, %d, ? )",
                      id2, count2);
    query.bindBlob(completebuffer, static_cast<int>(completesize));
    query.execute();

    free(completebuffer); // free main buffer

    // (4) --- dbobject insert
    DBObject::insertInDb();
}

void
DBRCIndexDS::readFromDb()
{
#ifdef RMANBENCHMARK
    DBObject::readTimer.resume();
#endif

    char *completebuffer;
    int blobformat;
    unsigned int blobsize = 0;
    unsigned int headersize = 0;
    int header = 0;

    long long id1;

    // (1) --- prepare variables
    id1 = myOId;

    // (2) --- get tuple
    SQLiteQuery query("SELECT DynData FROM RAS_RCINDEXDYN WHERE Id = %lld", id1);
    if (query.nextRow())
    {
        // read blob
        char *tmpblobbuffer = query.nextColumnBlob();
        blobsize = static_cast<unsigned int>(query.currColumnBytes());
        completebuffer = static_cast<char *>(mymalloc(blobsize));
        memcpy(completebuffer, tmpblobbuffer, blobsize);
    }
    else
    {
        LERROR << "DBHierIndex::readFromDb() - index entry: "
               << id1 << " not found in the database.";
        throw r_Ebase_dbms(SQLITE_NOTFOUND, "index entry not found in the database.");
    }

#ifdef RMANDEBUG        // dump low-level blob byte string
    {
        char printbuf[10000];
        (void) sprintf(printbuf, "DBRCIndexDS::readFromDb(): [%d]", blobsize);
        char bytebuf[3];
        for (long i = 0; i < blobsize; i++)
        {
            (void) sprintf(bytebuf, " %2X", (unsigned char) completebuffer[i]);
            strcat(printbuf, bytebuf);
        }
        LDEBUG << printbuf;
    }
#endif // RMANDEBUG

    // (4) --- fill variables and buffers
    unsigned int bytesdone;
    r_Dimension dimension = 0;

    r_Bytes boundssize;
    r_Bytes newboundssize = 0;

    (void) memcpy(&header, &completebuffer[0], sizeof(int));
    // if header >=1009 then this is considered to be a real header,
    // otherwise the value needs to be interpreted as a dimension

    // blobformat == 8: old format
    // blobformat == 9: OIDcounter is now long, but r_Range is still int
    // blobformat == 10: r_Range is long as well

    // old format
    if (header <= 1008)
    {
        blobformat = 8;
        // no header, first 4 bytes are actually the dimension;
        headersize = 0;
        dimension =  static_cast<r_Dimension>(header);
        bytesdone = headersize + sizeof(r_Dimension);

        // this is needed for correct assignment
        short tmpBaseOIdType;
        memcpy(&tmpBaseOIdType, &(completebuffer[bytesdone]), sizeof(short));
        myBaseOIdType = (OId::OIdType) tmpBaseOIdType;
        bytesdone += sizeof(short);

        int tmpBaseCounter;
        memcpy(&tmpBaseCounter, &(completebuffer[bytesdone]), sizeof(int));
        myBaseCounter = tmpBaseCounter;
        bytesdone += sizeof(int);

        unsigned int tmpMySize;
        memcpy(&tmpMySize, &(completebuffer[bytesdone]), sizeof(unsigned int));
        mySize = tmpMySize;
        bytesdone += sizeof(unsigned int);
    }
    else
    {
        blobformat = header - 1000;
        // first 4 bytes are the header;
        headersize = 4;
        memcpy(&dimension, &completebuffer[headersize], sizeof(r_Dimension));
        bytesdone = headersize + sizeof(r_Dimension);
    }

    if (blobformat == 8 || blobformat == 9)
    {
        // r_Range is still an int
        boundssize = sizeof(int) * dimension;  //number of bytes for bounds in 2 domains
        newboundssize = sizeof(r_Range) * dimension;  //number of bytes for bounds in 2 domains
    }
    else
    {
        boundssize = sizeof(r_Range) * dimension;  //number of bytes for bounds in 2 domains
    }

    LDEBUG << "blobformat: " << blobformat << " boundssize: " << boundssize << " dimension: " << dimension;

    if (blobformat >= 9)
    {

        // this is needed for correct assignment
        long long tmpBaseOIdType;
        memcpy(&tmpBaseOIdType, &(completebuffer[bytesdone]), sizeof(long long));
        myBaseOIdType = (OId::OIdType) tmpBaseOIdType;
        bytesdone += sizeof(long long);

        long long tmpBaseCounter;
        memcpy(&myBaseCounter, &(completebuffer[bytesdone]), sizeof(OId::OIdCounter));
        bytesdone += sizeof(OId::OIdCounter);

        memcpy(&mySize, &(completebuffer[bytesdone]), sizeof(OId::OIdCounter));
        bytesdone += sizeof(OId::OIdCounter);
    }

    r_Bytes fixessize = sizeof(char) * dimension;  //number of bytes for fixes in 2 domains
    r_Bytes completesize = boundssize * 2 + fixessize * 2; //number of bytes for the dynamic data
    char *dynamicBuffer = &completebuffer[bytesdone]; // ptr to start of dynamic part of buffer

    // TODO: UNCOMMENT BELOW
    // additional plausi check
    if (blobsize != bytesdone + completesize)
    {
        LERROR << "DBRCIndexDS::readFromDb() blob size inconsistency: blobSize (" << blobsize << " != bytesdone (" << bytesdone << ") + completesize (" << completesize << ")";
        LDEBUG << "DBRCIndexDS::readFromDb() blob size inconsistency: blobSize (" << blobsize << " != bytesdone (" << bytesdone << ") + completesize (" << completesize << ")";
        throw r_Error(r_Error::r_Error_LimitsMismatch);
    }

    LTRACE << "dimension " << dimension << ", base oid type " << myBaseOIdType << ", base counter " << myBaseCounter << ", size " << mySize << ", complete data size " << completesize;

    int *oldupperboundsbuf = NULL;
    int *oldlowerboundsbuf = NULL;

    r_Range *upperboundsbuf;
    r_Range *lowerboundsbuf;

    if (blobformat == 8 || blobformat == 9)
    {
        oldupperboundsbuf = static_cast<int *>(mymalloc(boundssize));
        oldlowerboundsbuf = static_cast<int *>(mymalloc(boundssize));
        memcpy(oldlowerboundsbuf, dynamicBuffer, boundssize);
        memcpy(oldupperboundsbuf, &dynamicBuffer[boundssize], boundssize);
        upperboundsbuf = static_cast<r_Range *>(mymalloc(newboundssize));
        lowerboundsbuf = static_cast<r_Range *>(mymalloc(newboundssize));
        // we need to copy all values to new variables
        for (long i = 0; i < dimension; i++)
        {
            upperboundsbuf[i] = (r_Range) oldupperboundsbuf[i];
            lowerboundsbuf[i] = (r_Range) oldlowerboundsbuf[i];
        }
    }
    else
    {
        upperboundsbuf = (r_Range *) mymalloc(boundssize);
        lowerboundsbuf = (r_Range *) mymalloc(boundssize);
        memcpy(lowerboundsbuf, dynamicBuffer, boundssize);
        memcpy(upperboundsbuf, &dynamicBuffer[boundssize], boundssize);
    }

    char *upperfixedbuf = (char *) mymalloc(fixessize);
    char *lowerfixedbuf = (char *) mymalloc(fixessize);

    // HST at later stage remove unnecessary copying
    // all dynamic data is in dynamicBuffer
    // put that stuff in the correct buffers
    memcpy(lowerfixedbuf, &dynamicBuffer[boundssize * 2], fixessize);
    memcpy(upperfixedbuf, &dynamicBuffer[boundssize * 2 + fixessize], fixessize);

    // all dynamic data is in its buffer
    free(completebuffer);
    dynamicBuffer = completebuffer = NULL;

    // rebuild attributes from buffers
    myDomain = InlineMinterval(dimension, &(lowerboundsbuf[0]), &(upperboundsbuf[0]), &(lowerfixedbuf[0]), &(upperfixedbuf[0]));
    LTRACE << "domain " << myDomain << " constructed from " << InlineMinterval(dimension, &(lowerboundsbuf[0]), &(upperboundsbuf[0]), &(lowerfixedbuf[0]), &(upperfixedbuf[0]));

    if (blobformat == 8 || blobformat == 9)
    {
        free(oldlowerboundsbuf);
        free(oldupperboundsbuf);
    }
    free(upperboundsbuf);
    upperboundsbuf = NULL;
    free(lowerboundsbuf);
    lowerboundsbuf = NULL;
    free(upperfixedbuf);
    upperfixedbuf = NULL;
    free(lowerfixedbuf);
    lowerfixedbuf = NULL;

#ifdef RMANBENCHMARK
    DBObject::readTimer.pause();
#endif

    // (5) --- dbobject read
    DBObject::readFromDb();
}

void
DBRCIndexDS::deleteFromDb()
{
    long long id3 = myOId;
    // (3) --- delete tuple
    SQLiteQuery::executeWithParams("DELETE FROM RAS_RCINDEXDYN WHERE Id = %lld", id3);
    // (4) --- dbobject delete
    DBObject::deleteFromDb();
}
