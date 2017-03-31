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
 * SOURCE: servercomm2.cc
 *
 * MODULE: servercomm
 * CLASS:  ServerComm
 *
 * PURPOSE:
 *
 * COMMENTS:
 *  - FIXME: catch exceptions in all operations
 *  - return values & their meaning, see servercomm.hh
 *  - FIXME: "client not registered" delivers sometimes 1, sometimes 3
 *
*/

#include "config.h"
#include "mymalloc/mymalloc.h"
#include <byteswap.h>

// after some time please take this and everything related to it out (26.06.2001)
#define ANDREAS_2306

#include <iostream>
#ifdef __APPLE__
#include <sys/malloc.h>
#else
#include <malloc.h>
#endif
#include <string.h>
#include <math.h>      // for log(), exp(), floor()
#include <ctime>         // time
#include <iomanip>
#include <boost/scoped_ptr.hpp>

#ifdef ENABLE_PROFILING
#include <google/profiler.h>
#include <gperftools/heap-profiler.h>
#include <string>
#endif

#ifdef PURIFY
#include <purify.h>
#endif

#ifdef SOLARIS
#define PORTMAP        // define to use function declarations for old interfaces
#include <rpc/rpc.h>

extern int _rpcpmstart;

// function prototype with C linkage
extern "C" int gethostname(char* name, int namelen);
#else  // HPUX
#include <rpc/rpc.h>
#endif

#include <rpc/pmap_clnt.h>

#include "debug.hh"
#include "raslib/rminit.hh"
#include "raslib/error.hh"
#include "raslib/minterval.hh"
#include "raslib/mddtypes.hh"
#include "raslib/basetype.hh"
#include "raslib/endian.hh"
#include "raslib/odmgtypes.hh"
#include "raslib/parseparams.hh"

#include "servercomm/servercomm.hh"
#include "catalogmgr/typefactory.hh"

#include "mddmgr/mddcoll.hh"
#include "mddmgr/mddobj.hh"
#include "mddmgr/mddcolliter.hh"
#include "tilemgr/tile.hh"

#include "relcatalogif/mddtype.hh"
#include "relcatalogif/mdddomaintype.hh"
#include "relcatalogif/settype.hh"
#include "reladminif/eoid.hh"

#include "qlparser/qtmdd.hh"
#include "qlparser/qtatomicdata.hh"
#include "qlparser/qtcomplexdata.hh"
#include "qlparser/qtintervaldata.hh"
#include "qlparser/qtmintervaldata.hh"
#include "qlparser/qtpointdata.hh"
#include "qlparser/qtstringdata.hh"

#include "lockmgr/lockmanager.hh"

#include <easylogging++.h>

// console output describing successful/unsuccessful actions
#define MSG_OK          "ok"
#define MSG_FAILED      "failed"

// include and extern declarations for the query parsing
#include "qlparser/querytree.hh"
#include "relcatalogif/structtype.hh"
extern int           yyparse(void*);
extern void          yyreset();
#ifdef NOPRE
char* ppInBuf = 0;
char* ppOutBuf = 0;
void ppreset()
{
    LFATAL << "Error: Preprocessor not compiled in.";
    LTRACE << "Error: Preprocessor not compiled in.";
    throw r_Error(ILLEGALSTATEREACHED);
}

int ppparse()
{
    LFATAL << "Error: Preprocessor not compiled in.";
    LTRACE << "Error: Preprocessor not compiled in.";
    throw r_Error(ILLEGALSTATEREACHED);
}
#else
extern char*         ppInBuf;
extern char*         ppOutBuf;
extern void          ppreset();
extern int       ppparse();
#endif
extern bool          udfEnabled;

extern QueryTree*    parseQueryTree;
extern ParseInfo*    parseError;
extern char*         beginParseString;
extern char*         iterParseString;
extern unsigned long maxTransferBufferSize;
extern char*         dbSchema;

MDDColl*      mddConstants = 0;

ServerComm::ClientTblElt* currentClientTblElt = 0;

// Once again a function prototype. The first one is for the RPC dispatcher
// function located in the server stub file rpcif_svc.c and the second one
// is for the garbage collection function pointed to by the signal handler.
extern "C"
{
    // static void rpcif_1( struct svc_req*, register SVCXPRT* );
    char* rpcif_1(struct svc_req*, register SVCXPRT*);
    void garbageCollection(int);
}

// This is needed in httpserver.cc
char globalHTTPSetTypeStructure[4096];

// constant for clientID
const char* ServerComm::HTTPCLIENT = "HTTPClient";

///ensureTileFormat returns the following:
const int ServerComm::ENSURE_TILE_FORMAT_OK = 0;
const int ServerComm::ENSURE_TILE_FORMAT_BAD = -1;

/// start the gperftools profilers
#ifdef ENABLE_PROFILING
void startProfiler(std::string fileNameTemplate, bool cpuProfiler)
{
    {
        char tmpFileName[fileNameTemplate.size() + 1];
        strcpy(tmpFileName, fileNameTemplate.c_str());

        int fd = mkstemps(tmpFileName, 6);
        if (fd != -1)
        {
            remove(tmpFileName);
            if (cpuProfiler)
            {
                ProfilerStart(tmpFileName);
                LINFO << "CPU profiler file: " << tmpFileName;
            }
            else
            {
                HeapProfilerStart(tmpFileName);
                LINFO << "Heap profiler file: " << tmpFileName << ".????.heap";
            }
            
        }
        else
        {
            LERROR << "failed creating a temporary profiler file: " << tmpFileName;
            LERROR << "reason: " << strerror(errno);
        }
    }
}
#endif

/*************************************************************************
 * Method name...: openDB( unsigned long callingClientId,
 *                         const char*   dbName ,
 *                         const char*   userName )
 ************************************************************************/
unsigned short
ServerComm::openDB(unsigned long callingClientId,
                   const char* dbName,
                   const char* userName)
{
    unsigned short returnValue = 0;
#ifdef DEBUG
    LINFO << "Request: 'open DB', name = " << dbName << "'...";
#endif

    ClientTblElt* context = getClientContext(callingClientId);

    if (context != 0)
    {
        // release transfer collection/iterator
        context->releaseTransferStructures();

        // open the database
        try
        {
            context->database.open(dbName);
        }
        catch (r_Error& err)
        {
            if (err.get_kind() == r_Error::r_Error_DatabaseUnknown)
            {
                LERROR << "Error: database does not exist.";
                returnValue = 2;
            }
            else if (err.get_kind() == r_Error::r_Error_DatabaseOpen)
            {
                // ignore re-open to be fault tolerant -- PB 2004-dec-16
                // LERROR << "Error: database is already open.";
                returnValue = 3;
            }
            else
            {
                LERROR << "Error: exception " << err.get_errorno() << ": " << err.what();
                //should be something else.  but better than no message about the problem at all
                returnValue = 2;
            }
        }

        if (returnValue == 0)
        {
            // database was successfully opened, so assign db and user name
            delete[] context->baseName;
            context->baseName = new char[strlen(dbName) + 1];
            strcpy(context->baseName, dbName);

            delete[] context->userName;
            context->userName = new char[strlen(userName) + 1];
            strcpy(context->userName, userName);
#ifdef DEBUG
            LINFO << MSG_OK;
#endif
        }

        context->release();

        // ignore "already open" error to be more fault tolerant -- PB 2004-dec-16
        if (returnValue == 3)
        {
            LWARNING << "Warning: database already open for user '" << userName << "', ignoring command.";
            returnValue = 0;
        }
    }
    else
    {
        LERROR << "Error: client not registered.";
        returnValue = 1;
    }
    return returnValue;
}



/*************************************************************************
 * Method name...: closeDB( unsigned long callingClientId )
 ************************************************************************/
unsigned short
ServerComm::closeDB(unsigned long callingClientId)
{
    unsigned short returnValue;

#ifdef DEBUG
    LDEBUG << "Request: 'close DB'...";
#endif

    ClientTblElt* context = getClientContext(callingClientId);

    if (context != 0)
    {
        // release transfer collection/iterator
        context->releaseTransferStructures();

        // If the current transaction belongs to this client, abort it.
        if (transactionActive == callingClientId)
        {
            LWARNING << "Warning: transaction is open; aborting this transaction...";

            context->transaction.abort();
            transactionActive = 0;
        }

        // close the database
        context->database.close();

        // reset database name
        delete[] context->baseName;
        context->baseName = new char[5];
        strcpy(context->baseName, "none");

        returnValue = 0;

        context->release();

#ifdef PURIFY
        purify_new_leaks();
#endif

#ifdef DEBUG
        LINFO << MSG_OK;
#endif
    }
    else
    {
        LERROR << "Error: client not registered.";
        returnValue = 1;
    }
    return returnValue;
}



/*************************************************************************
 * Method name...: createDB( char* name )
 ************************************************************************/
unsigned short
ServerComm::createDB(char* name)
{
    unsigned short returnValue;

    // FIXME: what about client id? -- PB 2005-aug-27

#ifdef DEBUG
    LINFO << "Request: 'create DB', name = " << name << "'...";
#endif

    DatabaseIf* tempDbIf = new DatabaseIf;

    // create the database
    try
    {
        tempDbIf->createDB(name, dbSchema);
#ifdef DEBUG
        LINFO << MSG_OK;
#endif
    }
    catch (r_Error& myErr)
    {
#ifdef DEBUG
        LERROR << "Error: exception " << myErr.get_errorno() << ": " << myErr.what();
#endif
    }
    catch (std::bad_alloc)
    {
        LERROR << "Error: cannot allocate memory.";
        throw;
    }
    catch (...)
    {
        LERROR << "Error: Unspecified exception.";
    }

    delete tempDbIf;

    // FIXME: set proper return value on failure (update .hh!) -- PB 2005-aug-27
    returnValue = 0;

    return returnValue;
}



/*************************************************************************
 * Method name...: destroyDB( char* name )
 ************************************************************************/
unsigned short
ServerComm::destroyDB(char* name)
{
    // Note: why no check for client id here? -- PB 2005-aug-25

    unsigned short returnValue = 0;

#ifdef DEBUG
    LINFO << "Request: 'destroy DB', name = " << name << "'...";
#endif

    DatabaseIf* tempDbIf = new DatabaseIf;

    // begin a temporary transaction because persistent data (as databases)
    // can only be manipulated within active transactions.
    // tempTaIf->begin(tempDbIf);

    // destroy the database
    tempDbIf->destroyDB(name);

    // commit the temporary transaction
    // tempTaIf->commit();

    delete tempDbIf;

#ifdef DEBUG
    LINFO << MSG_OK;
#endif

    return returnValue;
}



/*************************************************************************
 * Method name...: beginTA( unsigned long callingClientId )
 ************************************************************************/
unsigned short
ServerComm::beginTA(unsigned long callingClientId,
                    unsigned short readOnly)
{
    unsigned short returnValue;

#ifdef DEBUG
    LINFO << "Request: 'begin TA', mode = " << (readOnly ? "read" : "write") << "...";
#endif

    ClientTblElt* context = getClientContext(callingClientId);

    if (context == 0)
    {
        LERROR << "Error: client not registered.";
        returnValue = 1;
    }
    else if (transactionActive)
    {
        LERROR << "Error: transaction already active.";
        returnValue = 2;
        context->release();
    }
    else
    {
        // release transfer collection/iterator
        context->releaseTransferStructures();

#ifdef RMANBENCHMARK
        if (RManBenchmark > 0)
        {
            context->taTimer = new RMTimer("ServerComm", "transaction");
        }
#endif
        try
        {
            // start the transaction
            context->transaction.begin(&(context->database), readOnly);
#ifdef DEBUG
            LINFO << MSG_OK;
#endif
        }
        catch (r_Error& err)
        {
#ifdef DEBUG
            LFATAL << "Error: exception " << err.get_errorno() << ": " << err.what();
#endif
            context->release();
            throw;
        }

        // lock the semaphor
        transactionActive = callingClientId;
        returnValue = 0;

        context->release();
    }
    return returnValue;
}



/*************************************************************************
 * Method name...: commitTA( unsigned long callingClientId )
 ************************************************************************/
unsigned short
ServerComm::commitTA(unsigned long callingClientId)
{
    unsigned short returnValue;

    ClientTblElt* context = getClientContext(callingClientId);

#ifdef DEBUG
    LINFO << "Request: 'commit TA'...";
#endif

    if (context != 0)
    {

#ifdef RMANBENCHMARK
        RMTimer* commitTimer = 0;
        if (RManBenchmark > 0)
        {
            commitTimer = new RMTimer("ServerComm", "commit");
        }
#endif

        // release transfer collection/iterator within the transaction they are created
        context->releaseTransferStructures();
        if (configuration.isLockMgrOn())
        {
            LockManager* lockmanager = LockManager::Instance();
            lockmanager->unlockAllTiles();
        }

        // commit the transaction
        context->transaction.commit();

        // unlock the semaphor
        transactionActive = 0;

        returnValue = 0;

#ifdef RMANBENCHMARK
        if (commitTimer)
        {
            delete commitTimer;
        }
#endif

        context->release();

#ifdef DEBUG
        LINFO << MSG_OK;
#endif
    }
    else
    {
        LERROR << "Error: client not registered.";
        returnValue = 1;
    }

#ifdef RMANBENCHMARK
    if (context->taTimer)
    {
        delete context->taTimer;
    }
    context->taTimer = 0;
#endif

    return returnValue;
}



/*************************************************************************
 * Method name...: abortTA( unsigned long callingClientId )
 ************************************************************************/
unsigned short
ServerComm::abortTA(unsigned long callingClientId)
{
    unsigned short returnValue;

#ifdef DEBUG
    LINFO << "Request: 'abort TA'...";
#endif

    ClientTblElt* context = getClientContext(callingClientId);

    if (context != 0)
    {
        // release transfer collection/iterator within the transaction they are created
        context->releaseTransferStructures();

        // abort the transaction
        context->transaction.abort();
        if (configuration.isLockMgrOn())
        {
            LockManager* lockmanager = LockManager::Instance();
            lockmanager->unlockAllTiles();
        }

        // unlock the semaphore
        transactionActive = 0;

        returnValue = 0;

        context->release();

#ifdef DEBUG
        LINFO << MSG_OK;
#endif
    }
    else
    {
        LERROR << "Error: client not registered.";
        returnValue = 1;
    }

#ifdef RMANBENCHMARK
    if (context->taTimer)
    {
        delete context->taTimer;
    }
    context->taTimer = 0;
#endif

    return returnValue;
}


/*************************************************************************
 * Method name...: isTAOpen()
 * as right now only one transaction can be active per server,
 * we only have to check the sema.
 * returns:
 *  true    iff a transaction is open
 ************************************************************************/
bool
ServerComm::isTAOpen(__attribute__((unused)) unsigned long callingClientId)
{
#ifdef DEBUG
    LINFO << "Request: 'is TA open'...";
#endif

    bool returnValue = transactionActive;

#ifdef DEBUG
    LINFO << MSG_OK << (transactionActive ? "yes." : "no.");
#endif

    return returnValue;
}



unsigned short
ServerComm::insertColl(unsigned long callingClientId,
                       const char*   collName,
                       const char*   typeName,
                       r_OId&        oid)
{
    unsigned short returnValue = 0;

    LINFO << "Request: 'insert collection', collection name = '" << collName << "', type = '" << typeName << "'...";

    ClientTblElt* context = getClientContext(callingClientId);

    if (context != 0)
    {
        // delete old transfer structures
        context->releaseTransferStructures();

        //
        // create the collenction
        //

        // get collection type
        CollectionType* collType = static_cast<CollectionType*>(const_cast<SetType*>(TypeFactory::mapSetType(typeName)));

        if (collType)
        {
            try
            {
                MDDColl* coll = MDDColl::createMDDCollection(collName, OId(oid.get_local_oid()), collType);
                delete coll;
                LINFO << MSG_OK;
            }
            catch (r_Error& obj)
            {
                if (obj.get_kind() == r_Error::r_Error_NameNotUnique)
                {
#ifdef DEBUG
                    LERROR << "Error: collection exists already.";
#endif
                    returnValue = 3;
                }
                else
                {
#ifdef DEBUG
                    LERROR << "Error: cannot create collection: " << obj.get_errorno() << " " << obj.what();
#endif
                    //this should be another code...
                    returnValue = 3;
                }
            }

        }
        else
        {
            LERROR << "Error: unknown collection type: '" << typeName << "'.";
            returnValue = 2;
        }

        //
        // done
        //

        context->release();
    }
    else
    {
        LERROR << "Error: client not registered.";
        returnValue = 1;
    }

    return returnValue;
}



/*************************************************************************
 * Method name...: deleteCollByName( unsigned long callingClientId,
 *                                      const char*   collName )
 ************************************************************************/
unsigned short
ServerComm::deleteCollByName(unsigned long callingClientId,
                             const char*   collName)
{
    unsigned short returnValue;

    LINFO << "Request: 'delete collection by name', name = '" << collName << "'...";

    ClientTblElt* context = getClientContext(callingClientId);

    if (context != 0)
    {
        // delete old transfer structures
        context->releaseTransferStructures();

        //
        // delete collenction
        //

        // delete root object with collection name
        if (MDDColl::dropMDDCollection(collName))
        {
            LTRACE << "collection dropped";
            LINFO << MSG_OK;
            returnValue = 0;
        }
        else
        {
            LTRACE << "did not drop collection";
            LERROR << "Error: collection does not exist.";
            returnValue = 2;
        }

        //
        // done
        //

        context->release();
    }
    else
    {
        LERROR << "Error: client not registered.";
        returnValue = 1;
    }

    return returnValue;
}



unsigned short
ServerComm::deleteObjByOId(unsigned long callingClientId,
                           r_OId&        oid)
{
    unsigned short returnValue;

    LINFO << "Request: 'delete MDD by OID', oid = '" << oid << "'...";

    ClientTblElt* context = getClientContext(callingClientId);

    if (context != 0)
    {
        // delete old transfer structures
        context->releaseTransferStructures();

        // determine type of object
        OId oidIf(oid.get_local_oid());

        LTRACE << "OId of object " << oidIf;
        OId::OIdType objType = oidIf.getType();

        switch (objType)
        {
        case OId::MDDOID:
            // FIXME: why not deleted?? -- PB 2005-aug-27
            LINFO << "found MDD object; NOT deleted yet..." << MSG_OK;
            LTRACE << "not deleting mdd object";
            returnValue = 0;
            break;
        case OId::MDDCOLLOID:
            LINFO << "deleting collection...";
            // delete root object with collection name
            if (MDDColl::dropMDDCollection(oidIf))
            {
                LINFO << MSG_OK;
                LTRACE << "deleted mdd coll";
                returnValue = 0;
            }
            else
            {
                LERROR << "Error: Collection does not exist.";
                LTRACE << "did not delete mdd coll";
                returnValue = 2;
            }
            break;
        default:
            LERROR << "Error: object has unknown type: " << objType;
            returnValue = 2;
        }

        //
        // done
        //

        context->release();
    }
    else
    {
        LERROR << "Error: client not registered.";
        returnValue = 1;
    }

    return returnValue;
}



unsigned short
ServerComm::removeObjFromColl(unsigned long callingClientId,
                              const char*   collName,
                              r_OId&        oid)
{
    unsigned short returnValue;

    LINFO << "Request: 'remove MDD from collection', collection name = '" << collName << "', oid = '" << oid << "'...";

    ClientTblElt* context = getClientContext(callingClientId);

    if (context != 0)
    {
        // delete old transfer structures
        context->releaseTransferStructures();

        OId oidIf(oid.get_local_oid());

        LTRACE << "mdd object oid " << oidIf;

        // open collection
        MDDColl* coll = 0;

        try
        {
            coll = MDDColl::getMDDCollection(collName);
            LTRACE << "retrieved mdd coll";
        }
        catch (r_Error& obj)
        {
            // collection name invalid
            if (obj.get_kind() == r_Error::r_Error_ObjectUnknown)
            {
#ifdef DEBUG
                LERROR << "Error: collection not found.";
#endif
                returnValue = 2;
            }
            else
            {
#ifdef DEBUG
                LERROR << "Error " << obj.get_errorno() << ": " << obj.what();
#endif
                // there should be another return code
                returnValue = 2;
            }
            coll = NULL;
        }
        catch (std::bad_alloc)
        {
            LERROR << "Error: cannot allocate memory.";
            throw;
        }
        catch (...)
        {
            // collection name invalid
            LERROR << "Error: unspecified exception.";
            returnValue = 2;
        }

        if (coll)
        {
            if (coll->isPersistent())
            {
                LTRACE << "retrieved persistent mdd coll";

                OId collId;
                coll->getOId(collId);
                LTRACE << "mdd coll oid " << collId;
                MDDColl::removeMDDObject(collId, oidIf);

                // no error management yet -> returnValue = 3

                LINFO << MSG_OK;
                returnValue = 0;

                delete coll;
            }
        }

        //
        // done
        //

        context->release();
    }
    else
    {
        LERROR << "Error: client not registered.";
        returnValue = 1;
    }

    return returnValue;
}


unsigned short
ServerComm::insertMDD(unsigned long  callingClientId,
                      const char*    collName,
                      RPCMarray*     rpcMarray,
                      const char*    typeName,
                      r_OId&         oid)
{
    unsigned short returnValue = 0;

    LINFO << "Request: 'insert MDD type', type = '" << typeName << "', collection = '" << collName << "'...";

    ClientTblElt* context = getClientContext(callingClientId);
    r_Data_Format myDataFmt = r_Array;
    r_Data_Format myCurrentFmt = r_Array;

    if (context != 0)
    {
        // delete old transfer structures
        context->releaseTransferStructures();

        //
        // insert the object into the collection
        //

        // Determine the type of the MDD to be inserted.
        const MDDType* mddType = TypeFactory::mapMDDType(typeName);
        if (mddType)
        {
            if (mddType->getSubtype() != MDDType::MDDONLYTYPE)
            {
                //
                // open the collection
                //

                MDDColl* collection = 0;
                MDDColl* almost = 0;

                try
                {
                    almost = MDDColl::getMDDCollection(collName);
                    if (almost->isPersistent())
                    {
                        collection = static_cast<MDDColl*>(almost);
                    }
                    else
                    {
                        LFATAL << "Error: inserting into system collection is illegal.";
                        context->release(); //!!!
                        throw r_Error(SYSTEM_COLLECTION_NOT_WRITABLE);
                    }
                }
                catch (std::bad_alloc)
                {
                    LFATAL << "Error: cannot allocate memory.";
                    context->release(); //!!!
                    throw;
                }
                catch (r_Error& err)
                {
#ifdef DEBUG
                    LERROR << "Error " << err.get_errorno() << ": " << err.what();
#endif
                    returnValue = 5;
                    context->release(); //!!!
                    throw;
                }
                catch (...)
                {
                    returnValue = 5;
                    LERROR << "Error: unspecific exception during collection read.";
                    context->release();
                    return returnValue;
                }

                //
                // check MDD and collection type for compatibility
                //

                r_Minterval   domain(rpcMarray->domain);

#ifdef DEBUG
                char* collTypeStructure = collection->getCollectionType()->getTypeStructure();
                char* mddTypeStructure  = mddType->getTypeStructure();
                LTRACE << "Collection type structure.: " << collTypeStructure << "\n"
                       << "MDD type structure........: " << mddTypeStructure << "\n"
                       << "MDD domain................: " << domain;
                free(collTypeStructure);
                free(mddTypeStructure);
#endif

                if (!mddType->compatibleWithDomain(&domain))
                {
                    // free resources
                    collection->releaseAll();
                    delete collection;

                    // return error
                    returnValue = 4;
                    LERROR << "Error: MDD type is not compatible wrt. its domain: " << domain;

                    context->release();

                    return returnValue;
                }

                if (!collection->getCollectionType()->compatibleWith(mddType))
                {
                    // free resources
                    collection->releaseAll();
                    delete collection;

                    // return error
                    returnValue = 3;
                    LERROR << "Error: MDD and collection types are incompatible.";

                    context->release();

                    return returnValue;
                }

                //
                // create persistent MDD object
                //

                MDDBaseType*  mddBaseType = static_cast<MDDBaseType*>(const_cast<MDDType*>(mddType));
                char*         dataPtr  = rpcMarray->data.confarray_val;
                r_Bytes       dataSize = (r_Bytes) rpcMarray->data.confarray_len;
                // reset data area from rpc structure so that it is not deleted
                // deletion is done by TransTile resp. Tile
                rpcMarray->data.confarray_len = 0;
                rpcMarray->data.confarray_val = 0;
                r_Bytes getMDDData = 0;
                const BaseType*     baseType = mddBaseType->getBaseType();
                unsigned long byteCount  = domain.cell_count() * rpcMarray->cellTypeLength;
                //r_Data_Format storageFormat = (r_Data_Format)(rpcMarray->storageFormat);

                MDDObj*   mddObj = 0;
                StorageLayout ms;
                ms.setTileSize(StorageLayout::DefaultTileSize);
                ms.setIndexType(StorageLayout::DefaultIndexType);
                ms.setTilingScheme(StorageLayout::DefaultTilingScheme);
                if (domain.dimension() == StorageLayout::DefaultTileConfiguration.dimension())
                {
                    ms.setTileConfiguration(StorageLayout::DefaultTileConfiguration);
                }

                try
                {
                    mddObj = new MDDObj(mddBaseType, domain, OId(oid.get_local_oid()), ms);
                }
                catch (std::bad_alloc)
                {
                    LFATAL << "Error: cannot allocate memory.";
                    context->release(); //!!!
                    throw;
                }
                catch (r_Error& obj)
                {
#ifdef DEBUG
                    LERROR << "Error " << obj.get_errorno() << ": " << obj.what();
#endif
                    context->release(); //!!!
                    throw;
                }
                catch (...)
                {
                    returnValue = 6;
                    LERROR << "Error: unspecific exception during creation of persistent object.";

                    context->release();

                    return returnValue;
                }

                myDataFmt = static_cast<r_Data_Format>(rpcMarray->storageFormat);
                myCurrentFmt = static_cast<r_Data_Format>(rpcMarray->currentFormat);
                LTRACE  << "oid " << oid
                        << ", domain " << domain
                        << ", cell length " << rpcMarray->cellTypeLength
                        << ", data size " << dataSize
                        << ", rpc storage " << myDataFmt
                        << ", rpc transfer " << myCurrentFmt << " ";

                // store in the specified storage format; the current tile format afterwards will be the
                // requested format if all went well, but use the (new) current format to be sure.
                // Don't repack here, however, because it might be retiled before storage.
                if (ensureTileFormat(myCurrentFmt, myDataFmt, domain,
                                     baseType, dataPtr, dataSize, 0, 1, context->storageFormatParams) != ENSURE_TILE_FORMAT_OK)
                {
                    //FIXME returnValue
                    returnValue = 6;
                    LERROR << "Error: illegal tile format for creating object.";

                    context->release();

                    return returnValue;
                }

                // if compressed, getMDDData is != 0
                if (myCurrentFmt != r_Array)
                {
                    getMDDData = dataSize;
                }

                // This should check the compressed size rather than the raw data size
                if (RMInit::tiling && dataSize > StorageLayout::DefaultTileSize)
                {
                    r_Range edgeLength = static_cast<r_Range>(floor(exp((1 / static_cast<r_Double>(domain.dimension())) *
                                         log(static_cast<r_Double>(StorageLayout::DefaultTileSize) / rpcMarray->cellTypeLength))));

                    if (edgeLength < 1)
                    {
                        edgeLength = 1;
                    }

                    r_Minterval tileDom(domain.dimension());
                    for (unsigned int i = 0; i < tileDom.dimension(); i++)
                    {
                        tileDom << r_Sinterval(static_cast<r_Range>(0), static_cast<r_Range>(edgeLength - 1));
                    }

                    Tile* entireTile = 0;
                    myCurrentFmt = r_Array;
                    entireTile = new Tile(domain, baseType, true, dataPtr, getMDDData, myDataFmt);

                    vector<Tile*>* tileSet = entireTile->splitTile(tileDom);
                    if (entireTile->isPersistent())
                    {
                        entireTile->setPersistent(0);
                    }
                    delete entireTile;

                    LINFO << "creating " << tileSet->size() << " tile(s)...";

                    for (vector<Tile*>::iterator iter = tileSet->begin(); iter != tileSet->end(); iter++)
                    {
                        mddObj->insertTile(*iter);
                    }

                    // delete the vector again
                    delete tileSet;
                }
                else
                {
                    Tile* tile = 0;

                    tile = new Tile(domain, baseType, true, dataPtr, getMDDData, myDataFmt);
                    LTRACE << "insertTile created new TransTile (" << myDataFmt << "), ";

                    LTRACE << "one tile...";
                    mddObj->insertTile(tile);
                }

                collection->insert(mddObj);

                // free transient memory
                collection->releaseAll();

                delete collection;

                //
                // done
                //

                LINFO << MSG_OK;
            }
            else
            {
                LERROR << "Error: MDD type name '" << typeName << "' has no base type.";
                returnValue = 2;
            }
        }
        else
        {
            LERROR << "Error: MDD type name '" << typeName << "' not found.";
            returnValue = 2;
        }

        context->release();
    }
    else
    {
        LERROR << "Error: client not registered.";
        returnValue = 1;
    }

    return returnValue;
}


unsigned short
ServerComm::insertTileSplitted(unsigned long  callingClientId,
                               bool            isPersistent,
                               RPCMarray*     rpcMarray,
                               r_Minterval*   tileSize)
{
    unsigned short returnValue = 0;

    LINFO << "Request: 'insert tile'...";

    ClientTblElt* context = getClientContext(callingClientId);

    if (context != 0)
    {
        BaseType* baseType = NULL;

        if (isPersistent)
        {
            baseType = const_cast<BaseType*>(context->assembleMDD->getCellType());
        }
        else
        {
            baseType = const_cast<BaseType*>(context->transferMDD->getCellType());
        }

        // The type of the tile has to be the one of the MDD.
        // type check missing

        if (baseType != NULL)
        {
            r_Minterval   domain(rpcMarray->domain);
            char*         dataPtr  = rpcMarray->data.confarray_val;
            r_Bytes       dataSize = (r_Bytes) rpcMarray->data.confarray_len;
            // reset data area from rpc structure so that it is not deleted
            // deletion is done by TransTile resp. Tile
            rpcMarray->data.confarray_len = 0;
            rpcMarray->data.confarray_val = 0;
            r_Bytes         getMDDData    = 0;
            r_Data_Format myDataFmt = static_cast<r_Data_Format>(rpcMarray->storageFormat);
            r_Data_Format myCurrentFmt = static_cast<r_Data_Format>(rpcMarray->currentFormat);
            LTRACE << "insertTileSplitted - rpc storage  format : " << myDataFmt;
            LTRACE << "insertTileSplitted - rpc transfer format : " << myCurrentFmt;
            // store in specified storage format; use (new) current format afterwards
            // Don't repack here because of possible retiling.
            if (ensureTileFormat(myCurrentFmt, myDataFmt, domain,
                                 baseType, dataPtr, dataSize, 0, 1, context->storageFormatParams) != ENSURE_TILE_FORMAT_OK)
            {
                //FIXME returnValue
                returnValue = 1;

                context->release();

                return returnValue;
            }

            if (myCurrentFmt != r_Array)
            {
                getMDDData = dataSize;
            }

            Tile* tile = 0;

            LTRACE << "insertTile created new TransTile (" << myDataFmt << "), ";
            myDataFmt = r_Array;
            tile = new Tile(domain, baseType, true, dataPtr, getMDDData, myDataFmt);

            // for java clients only: check endianness and split tile if necessary
            if (strcmp(context->clientIdText, ServerComm::HTTPCLIENT) == 0)
            {
                // check endianess
                r_Endian::r_Endianness serverEndian = r_Endian::get_endianness();
                if (serverEndian != r_Endian::r_Endian_Big)
                {
                    LTRACE << "changing endianness...";

                    // we have to swap the endianess
                    char* tpstruct;
                    r_Base_Type* useType;
                    tpstruct = baseType->getTypeStructure();
                    useType = static_cast<r_Base_Type*>(r_Type::get_any_type(tpstruct));

                    char* tempT = static_cast<char*>(mymalloc(sizeof(char) * tile->getSize()));

                    // change the endianness of the entire tile for identical domains for src and dest
                    r_Endian::swap_array(useType, domain, domain, tile->getContents(), tempT);
                    // deallocate old contents
                    char* oldCells = tile->getContents();
                    free(oldCells);
                    oldCells = NULL;
                    // set new contents
                    tile->setContents(tempT);

                    delete useType;
                    free(tpstruct);
                }

                // Split the tile!
                vector<Tile*>* tileSet = tile->splitTile(*tileSize);
                LTRACE << "inserting split tile...";
                for (vector<Tile*>::iterator iter = tileSet->begin(); iter != tileSet->end(); iter++)
                {
                    if (isPersistent)
                    {
                        context->assembleMDD->insertTile(*iter);
                    }
                    else
                    {
                        context->transferMDD->insertTile(*iter);
                    }
                }
                // delete the vector again
                delete tile;
                tile = NULL;
                delete tileSet;
            }
            // for c++ clients: insert tile
            else
            {
                //insert one single tile
                // later, we should take into consideration the default server tile-size!
                LTRACE << "inserting single tile...";
                if (isPersistent)
                {
                    context->assembleMDD->insertTile(tile);
                }
                else
                {
                    context->transferMDD->insertTile(tile);
                }
                //do not access tile again, because it was already deleted in insertTile
            }
            //
            // done
            //

            LINFO << MSG_OK;
        }
        else
        {
            LERROR << "Error: tile and MDD base type do not match.";
        }

        context->release();
    }
    else
    {
        LERROR << "Error: client not registered.";
        returnValue = 1;
    }

    return returnValue;
}


unsigned short
ServerComm::insertTile(unsigned long  callingClientId,
                       bool            isPersistent,
                       RPCMarray*     rpcMarray)
{
    // no log here, is done in RNP comm.

    unsigned short returnValue = insertTileSplitted(callingClientId, isPersistent, rpcMarray, NULL);

    return returnValue;
}


unsigned short
ServerComm::startInsertPersMDD(unsigned long  callingClientId,
                               const char*    collName,
                               r_Minterval&    domain,
                               unsigned long  typeLength,
                               const char*    typeName,
                               r_OId&         oid)
{
    unsigned short returnValue = 0;

    LINFO << "Request: 'start inserting persistent MDD type', type = '" << typeName
          << "', collection = '" << collName << "', domain = " << domain << ", cell size = " << typeLength
          << ", " << domain.cell_count()*typeLength << "...";

    ClientTblElt* context = getClientContext(callingClientId);

    if (context != 0)
    {
        // delete old transfer structures
        context->releaseTransferStructures();

        // Determine the type of the MDD to be inserted.
        const MDDType* mddType = TypeFactory::mapMDDType(typeName);

        if (mddType)
        {
            if (mddType->getSubtype() != MDDType::MDDONLYTYPE)
            {
                MDDBaseType* mddBaseType = static_cast<MDDBaseType*>(const_cast<MDDType*>(mddType));

                try
                {
                    // store PersMDDColl for insert operation at the end of the transfer
                    context->transferColl = MDDColl::getMDDCollection(collName);
                    if (!context->transferColl->isPersistent())
                    {
                        LFATAL << "Error: inserting into system collection is illegal.";
                        context->release(); //!!!
                        throw r_Error(SYSTEM_COLLECTION_NOT_WRITABLE);
                    }
                }
                catch (r_Error& obj)
                {
#ifdef DEBUG
                    LFATAL << "Error " << obj.get_errorno() << ": " << obj.what();
#endif
                    context->release(); //!!!
                    throw;
                }
                catch (std::bad_alloc)
                {
                    LFATAL << "Error: cannot allocate memory.";
                    context->release(); //!!!
                    throw;
                }
                catch (...)
                {
                    returnValue = 5;
                    LERROR << "Error: unspecific exception while opening collection.";

                    context->release();

                    return returnValue;
                }

                //
                // check MDD and collection type for compatibility
                //
#ifdef DEBUG
                char* collTypeStructure = context->transferColl->getCollectionType()->getTypeStructure();
                char* mddTypeStructure  = mddType->getTypeStructure();
                LTRACE << "Collection type structure.: " << collTypeStructure << "\n"
                       << "MDD type structure........: " << mddTypeStructure << "\n"
                       << "MDD domain................: " << domain;
                free(collTypeStructure);
                free(mddTypeStructure);
#endif

                if (!mddType->compatibleWithDomain(&domain))
                {
                    // free resources
                    context->transferColl->releaseAll();
                    delete context->transferColl;
                    context->transferColl = 0;

                    // return error
                    returnValue = 4;
                    LERROR << "Error: MDD type not compatible wrt. its domain: " << domain << MSG_FAILED;

                    context->release();

                    return returnValue;
                }

                if (!context->transferColl->getCollectionType()->compatibleWith(mddType))
                {
                    // free resources
                    context->transferColl->releaseAll();
                    delete context->transferColl;
                    context->transferColl = 0;

                    // return error
                    returnValue = 3;
                    LERROR << "Error: incompatible MDD and collection types.";

                    context->release();

                    return returnValue;
                }

                //
                // Create persistent MDD for further tile insertions
                //

                StorageLayout ms;
                ms.setTileSize(StorageLayout::DefaultTileSize);
                ms.setIndexType(StorageLayout::DefaultIndexType);
                ms.setTilingScheme(StorageLayout::DefaultTilingScheme);
                if (domain.dimension() == StorageLayout::DefaultTileConfiguration.dimension())
                {
                    ms.setTileConfiguration(StorageLayout::DefaultTileConfiguration);
                }
                try
                {
                    context->assembleMDD = new MDDObj(mddBaseType, domain, OId(oid.get_local_oid()), ms);
                }
                catch (r_Error& err)
                {
#ifdef DEBUG
                    LFATAL << "Error: while creating persistent tile: " << err.get_errorno() << ": " << err.what();
#endif
                    context->release(); //!!!
                    throw;
                }
                catch (std::bad_alloc)
                {
                    LFATAL << "Error: cannot allocate memory.";
                    context->release(); //!!!
                    throw;
                }
                catch (...)
                {
                    returnValue = 6;
                    LERROR << "Error: unspecific exception during creation of persistent object.";

                    context->release();

                    return returnValue;
                }

                LINFO << MSG_OK;
            }
            else
            {
                LERROR << "Error: MDD type '" << typeName << "' has no base type...";
                returnValue = 2;
            }
        }
        else
        {
            LERROR << "Error: MDD type name '" << typeName << "' not found.";
            returnValue = 2;
        }

        context->release();
    }
    else
    {
        LERROR << "Error: client not registered.";
        returnValue = 1;
    }

    return returnValue;
}


/*************************************************************************
 * Method name...: executeQuery( unsigned long   callingClientId,
 *                               const char*     query
 *                               ExecuteQueryRes &returnStructure )
 ************************************************************************/
unsigned short
ServerComm::executeQuery(unsigned long callingClientId,
                         const char* query,
                         ExecuteQueryRes& returnStructure)
{
    unsigned short returnValue = 0;
#ifdef ENABLE_PROFILING
    startProfiler("/tmp/rasdaman_query_select.XXXXXX.pprof", true);
    startProfiler("/tmp/rasdaman_query_select.XXXXXX.pprof", false);
#endif

    // set all to zero as default. They are not really applicable here.
    returnStructure.errorNo       = 0;
    returnStructure.lineNo        = 0;
    returnStructure.columnNo      = 0;

    LINFO << "Request: '" << query << "'...";

    ClientTblElt* context = getClientContext(callingClientId);

    if (context != 0)
    {
#ifdef RMANBENCHMARK
        Tile::relTimer.start();
        Tile::relTimer.pause();
        Tile::opTimer.start();
        Tile::opTimer.pause();
#endif

#ifdef PURIFY
        purify_printf("%s\n", query);
#endif

        mddConstants     = context->transferColl; // assign the mdd constants collection to the global pointer (temporary)
        context->transferColl = NULL;

        // delete old transfer collection/iterator
        context->releaseTransferStructures();

        //
        // execute the query
        //

        QueryTree* qtree = new QueryTree();   // create a query tree object...
        parseQueryTree   = qtree;             // ...and assign it to the global parse query tree pointer;

        currentClientTblElt = context;      // assign current client table element (temporary)

        int ppRet = 0;
        int parserRet = 0;

        udfEnabled = 0; // Forced for RNP, but only temporary...
        if (udfEnabled)
        {
            //
            // preprocess
            //
            LINFO << "preprocessing...";
            ppInBuf = const_cast<char*>(query);
            ppreset();
            ppRet = ppparse();

            LTRACE << "new query: '" << ppOutBuf << "'...";

            // initialize the input string parameters
            beginParseString = ppOutBuf;
            iterParseString  = ppOutBuf;
        }
        else
        {
            beginParseString = const_cast<char*>(query);
            iterParseString  = const_cast<char*>(query);
        }

        yyreset();

        LINFO << "parsing...";

        parserRet = yyparse(0);
        if ((ppRet == 0) && (parserRet == 0))
        {
            try
            {
#ifdef DEBUG
                qtree->printTree(2, RMInit::logOut);
#endif

                LINFO << "checking semantics...";
                qtree->checkSemantics();

#ifdef DEBUG
                qtree->printTree(2, RMInit::logOut);
#endif

#ifdef RMANBENCHMARK
                if (RManBenchmark > 0)
                {
                    context->evaluationTimer = new RMTimer("ServerComm", "evaluation");
                }
#endif
                //qtree->checkSemantics();
                //qtree->printTree( 2, std::cout );
                LINFO << "evaluating...";
                context->transferData = qtree->evaluateRetrieval();
            }
            catch (ParseInfo& info)
            {
                // this is the old error handling which has been here for quite some time
                // dealing with errors when release data
                context->releaseTransferStructures();

                returnValue = 5;           // execution error

                // set the error values of the return structure
                returnStructure.errorNo    = info.getErrorNo();
                returnStructure.lineNo     = info.getLineNo();
                returnStructure.columnNo   = info.getColumnNo();
                returnStructure.token      = strdup(info.getToken().c_str());

                info.printStatus(RMInit::logOut);
            }
            catch (r_Ebase_dbms& myErr)
            {
                LERROR << "Error: base DBMS exception: " << myErr.what();

                // release data
                context->releaseTransferStructures();
                context->release(); //!!!
                if (mddConstants)
                {
                    mddConstants->releaseAll();
                    delete mddConstants;
                    mddConstants = NULL;
                }

                //delete parser data
                parseQueryTree      = 0;
                currentClientTblElt = 0;
                delete qtree;

                returnValue = 42;           // general serialisable exception

                throw;
            }
            catch (r_Error& myErr)
            {
#ifdef DEBUG
                LERROR << "Error: " << myErr.get_errorno() << " " << myErr.what();
#endif

                // release data
                context->releaseTransferStructures();
                context->release(); //!!!
                if (mddConstants)
                {
                    mddConstants->releaseAll();
                    delete mddConstants;
                    mddConstants = NULL;
                }

                //delete parser data
                parseQueryTree      = 0;
                currentClientTblElt = 0;
                delete qtree;

                returnValue = 5;

                throw;
            }
            catch (std::bad_alloc)
            {
                LERROR << "Error: cannot allocate memory.";

                // release data
                context->releaseTransferStructures();
                context->release(); //!!!
                if (mddConstants)
                {
                    mddConstants->releaseAll();
                    delete mddConstants;
                    mddConstants = NULL;
                }

                //delete parser data
                parseQueryTree      = 0;
                currentClientTblElt = 0;
                delete qtree;

                throw;
            }
            catch (...)
            {
                LERROR << "Error: unspecific exception.";

                context->releaseTransferStructures();
                context->release(); //!!!
                if (mddConstants)
                {
                    mddConstants->releaseAll();
                    delete mddConstants;
                    mddConstants = NULL;
                }

                //delete parser data
                parseQueryTree      = 0;
                currentClientTblElt = 0;
                delete qtree;

                returnValue = 5;

                throw;
            }

            if (returnValue == 0)
            {
                if (context->transferData != 0)
                {
                    // create the transfer iterator
                    context->transferDataIter    = new vector<QtData*>::iterator;
                    *(context->transferDataIter) = context->transferData->begin();

                    //
                    // set typeName and typeStructure
                    //

                    // The type of first result object is used to determine the type of the result
                    // collection.
                    if (*(context->transferDataIter) != context->transferData->end())
                    {
                        QtData* firstElement = (**(context->transferDataIter));

                        if (firstElement->getDataType() == QT_MDD)
                        {
                            QtMDD* mddObj = static_cast<QtMDD*>(firstElement);
                            const BaseType* baseType = mddObj->getMDDObject()->getCellType();
                            r_Minterval     domain   = mddObj->getLoadDomain();

                            MDDType* mddType = new MDDDomainType("tmp", const_cast<BaseType*>(baseType), domain);
                            SetType* setType = new SetType("tmp", mddType);

                            returnStructure.typeName      = strdup(setType->getTypeName());
                            returnStructure.typeStructure = setType->getTypeStructure();  // no copy

                            TypeFactory::addTempType(setType);
                            TypeFactory::addTempType(mddType);

                            // print total data size
                            long totalReturnedSize = 0;
                            for (auto it = context->transferData->begin(); it != context->transferData->end(); it++)
                            {
                                QtMDD* mdd = static_cast<QtMDD*>(*it);
                                auto baseTypeSize = mddObj->getMDDObject()->getCellType()->getSize();
                                r_Minterval domain = mddObj->getLoadDomain();
                                totalReturnedSize += (domain.cell_count() * baseTypeSize);
                            }
                            LINFO << MSG_OK << ", result type '" << returnStructure.typeStructure << "', " << context->transferData->size() << " element(s)"
                                  << ", total size " << totalReturnedSize << " bytes.";
                        }
                        else
                        {
                            returnValue = 1;       // evaluation ok, non-MDD elements

                            returnStructure.typeName      = strdup("");

                            // hack set type
                            char* elementType = firstElement->getTypeStructure();
                            returnStructure.typeStructure = static_cast<char*>(mymalloc(strlen(elementType) + 6));
                            sprintf(returnStructure.typeStructure, "set<%s>", elementType);
                            free(elementType);
                            LINFO << MSG_OK << ", result type '" << returnStructure.typeStructure << "', " << context->transferData->size() << " element(s).";
                        }

                        strcpy(globalHTTPSetTypeStructure, returnStructure.typeStructure);
                    }
                    else
                    {
                        LINFO << MSG_OK << ", result is empty.";
                        returnValue = 2;         // evaluation ok, no elements

                        returnStructure.typeName      = strdup("");
                        returnStructure.typeStructure = strdup("");
                    }
                }
                else
                {
                    LINFO << MSG_OK << ", result is empty.";
                    returnValue = 2;         // evaluation ok, no elements
                }
            }
        }
        else
        {
            if (ppRet)
            {
                LINFO << MSG_OK << ",result is empty.";
                returnValue = 2;         // evaluation ok, no elements
            }
            else    // parse error
            {
                if (parseError)
                {
                    returnStructure.errorNo    = parseError->getErrorNo();
                    returnStructure.lineNo     = parseError->getLineNo();
                    returnStructure.columnNo   = parseError->getColumnNo();
                    returnStructure.token      = strdup(parseError->getToken().c_str());
                    parseError->printStatus(RMInit::logOut);

                    delete parseError;
                    parseError = 0;
                }
                else
                {
                    returnStructure.errorNo = 309;
                    LERROR << "Internal Error: Unknown parse error.";
                }

                yyreset(); // reset the input buffer of the scanner
                returnValue = 4;
            }
        }

        parseQueryTree      = 0;
        currentClientTblElt = 0;
        delete qtree;
        if (mddConstants)
        {
            mddConstants->releaseAll();
            delete mddConstants;
            mddConstants = NULL;
        }

        //
        // done
        //

#ifdef RMANBENCHMARK

        // Evaluation timer can not be stopped because some time spent in the transfer
        // module is added to this phase.
        if (context->evaluationTimer)
        {
            context->evaluationTimer->pause();
        }

        if (RManBenchmark > 0)
        {
            context->transferTimer = new RMTimer("ServerComm", "transfer");
        }
#endif

        // In case of an error or the result set is empty, no endTransfer()
        // is called by the client.
        // Therefore, some things have to be release here.
        if (returnValue >= 2)
        {
#ifdef RMANBENCHMARK
            Tile::opTimer.stop();
            Tile::relTimer.stop();
            if (context->evaluationTimer)
            {
                delete context->evaluationTimer;
            }
            context->evaluationTimer = 0;

            if (context->transferTimer)
            {
                delete context->transferTimer;
            }
            context->transferTimer = 0;

            RMTimer* releaseTimer = 0;

            if (RManBenchmark > 0)
            {
                releaseTimer = new RMTimer("ServerComm", "release");
            }
#endif

            // release transfer collection/iterator
            context->releaseTransferStructures();

#ifdef RMANBENCHMARK
            if (releaseTimer)
            {
                delete releaseTimer;
            }
#endif
        }
        context->release();
    }
    else
    {
        LERROR << "Error: client not registered.";
        returnValue = 3;
    }

#ifdef ENABLE_PROFILING
    ProfilerStop();
    HeapProfilerStop();
#endif

    return returnValue;
}



unsigned short
ServerComm::initExecuteUpdate(unsigned long callingClientId)
{
    unsigned short returnValue = 0;

    LINFO << "Request: 'initialize update'...";

    ClientTblElt* context = getClientContext(callingClientId);

    if (context != 0)
    {
        // delete old transfer structures
        context->releaseTransferStructures();

        MDDType* mddType = new MDDType("tmp");
        SetType* setType = new SetType("tmp", mddType);

        TypeFactory::addTempType(mddType);
        TypeFactory::addTempType(setType);

        // create a transient collection for storing MDD constants
        context->transferColl = new MDDColl(setType);

        context->release();

        LINFO << MSG_OK;
    }
    else
    {
        LERROR << "Error: client not registered.";
        returnValue = 1;
    }

    return returnValue;
}



unsigned short
ServerComm::startInsertTransMDD(unsigned long callingClientId,
                                r_Minterval&   domain, unsigned long typeLength,
                                const char*   typeName)
{
    unsigned short returnValue = 0;

    LINFO << "Request: 'insert MDD', type '"
          << typeName << "', domain " << domain << ", cell length " << typeLength  << ", "
          << domain.cell_count()*typeLength << " bytes...";

    ClientTblElt* context = getClientContext(callingClientId);

    if (context != 0)
    {
        LTRACE << "startInsertTransMDD(...) TRANSFER " << context->transferFormat << ", EXACT " << (bool)context->exactFormat;

        // Determine the type of the MDD to be inserted.
        const MDDType* mddType = TypeFactory::mapMDDType(typeName);

        if (mddType)
        {
            if (mddType->getSubtype() != MDDType::MDDONLYTYPE)
            {
                MDDBaseType* mddBaseType = static_cast<MDDBaseType*>(const_cast<MDDType*>(mddType));

                if (!mddType->compatibleWithDomain(&domain))
                {
                    // return error
                    returnValue = 3;
                    LERROR << "Error: MDD type incompatible wrt. domain: " << domain;

                    context->release();

                    return returnValue;
                }

                // create for further insertions
                context->transferMDD = new MDDObj(mddBaseType, domain);

                LINFO << MSG_OK;
            }
            else
            {
                LERROR << "Error: MDD type has no base type.";
                returnValue = 2;
            }
        }
        else
        {
            LERROR << "Error: MDD type not found.";
            returnValue = 2;
        }

        context->release();
    }
    else
    {
        LERROR << "Error: client not registered.";
        returnValue = 1;
    }

    return returnValue;
}



unsigned short
ServerComm::endInsertMDD(unsigned long callingClientId,
                         int isPersistent)
{
    unsigned short returnValue = 0;

#ifdef DEBUG
    LINFO << "Request: 'end insert MDD'...";
#endif

    ClientTblElt* context = getClientContext(callingClientId);

    if (context != 0)
    {
        if (isPersistent)
        {
            // we are finished with this MDD Object, so insert it into the collection
            context->transferColl->insert(context->assembleMDD);

            // reset assembleMDD, because otherwise it is tried to be freed
            context->assembleMDD = 0;

            // free transfer structure
            context->releaseTransferStructures();

            // old: context->transferColl->releaseAll(); caused a crash because releaseAll() is not idempotent
        }
        else
        {
            // we are finished with this MDD Object, so insert it into the collection
            context->transferColl->insert(context->transferMDD);

            // reset transferMDD
            context->transferMDD = 0;

            // Do not delete the transfer structure because the transient set
            // of MDD objects will be used as constants for executeUpdate().
        }

#ifdef DEBUG
        LINFO << MSG_OK;
#endif

        context->release();
    }
    else
    {
        LERROR << "Error: client not registered.";
        returnValue = 1;
    }

    return returnValue;
}



unsigned short
ServerComm::executeUpdate(unsigned long callingClientId,
                          const char* query,
                          ExecuteUpdateRes& returnStructure)
{
    LINFO << "Request: '" << query << "'...";

#ifdef ENABLE_PROFILING
    startProfiler("/tmp/rasdaman_query_update.XXXXXX.pprof", true);
    startProfiler("/tmp/rasdaman_query_update.XXXXXX.pprof", false);
#endif

#ifdef RMANBENCHMARK
    Tile::relTimer.start();
    Tile::relTimer.pause();
    Tile::opTimer.start();
    Tile::opTimer.pause();
#endif

    unsigned short returnValue = 0;

    ClientTblElt* context = getClientContext(callingClientId);

    if (context != 0)
    {
#ifdef PURIFY
        purify_printf("%s\n", query);
#endif

        //
        // execute the query
        //

        QueryTree* qtree = new QueryTree();   // create a query tree object...
        parseQueryTree   = qtree;             // ...and assign it to the global parse query tree pointer;

        mddConstants     = context->transferColl; // assign the mdd constants collection to the global pointer (temporary)
        currentClientTblElt = context;        // assign current client table element (temporary)

        int ppRet = 0;
        udfEnabled = false; // forced for RNP tests
        if (udfEnabled)
        {
            //
            // preprocess
            //
            LINFO << "preprocessing...";
            ppInBuf = const_cast<char*>(query);
            ppreset();
            ppRet = ppparse();

            if (ppOutBuf)
            {
                LTRACE << "new query: '" << ppOutBuf << "'";
            }
            else
            {
                LTRACE << "new query: empty.";
            }

            // initialize the input string parameters
            beginParseString = ppOutBuf;
            iterParseString  = ppOutBuf;
        }
        else
        {
            beginParseString = const_cast<char*>(query);
            iterParseString  = const_cast<char*>(query);
        }

        yyreset();

        LINFO << "parsing...";

        if (ppRet == 0 && yyparse(0) == 0)
        {
            try
            {
#ifdef DEBUG
                qtree->printTree(2, RMInit::logOut);
#endif

                LINFO << "checking semantics...";

                qtree->checkSemantics();

#ifdef DEBUG
                qtree->printTree(2, RMInit::logOut);
#endif

#ifdef RMANBENCHMARK
                if (RManBenchmark > 0)
                {
                    context->evaluationTimer = new RMTimer("ServerComm", "evaluation");
                }
#endif

                LINFO << "evaluating...";

                vector<QtData*>* updateResult = qtree->evaluateUpdate();

                // release data
                delete updateResult;
                context->releaseTransferStructures();

                LINFO << MSG_OK;
            }
            catch (ParseInfo& info)
            {
                // release data
                context->releaseTransferStructures();

                returnValue = 3;           // evaluation error

                // set the error values of the return structure
                returnStructure.errorNo    = info.getErrorNo();
                returnStructure.lineNo     = info.getLineNo();
                returnStructure.columnNo   = info.getColumnNo();
                returnStructure.token      = strdup(info.getToken().c_str());

                info.printStatus(RMInit::logOut);
            }
            catch (r_Error& err)
            {
                context->releaseTransferStructures();
                context->release();
#ifdef DEBUG
                LFATAL << "Error: " << err.get_errorno() << " " << err.what();
#endif
                throw;
            }
        }
        else
        {
            if (ppRet)
            {
                LINFO << MSG_OK;
                returnValue = 0;
            }
            else    // parse error
            {
                if (parseError)
                {
                    returnStructure.errorNo    = parseError->getErrorNo();
                    returnStructure.lineNo     = parseError->getLineNo();
                    returnStructure.columnNo   = parseError->getColumnNo();
                    returnStructure.token      = strdup(parseError->getToken().c_str());

                    parseError->printStatus(RMInit::logOut);
                    delete parseError;
                    parseError = 0;
                }
                else
                {
                    returnStructure.errorNo = 309;
                    LERROR << "Error: unspecific internal parser error.";
                }

                yyreset(); // reset the input buffer of the scanner

                returnValue = 2;
            }
        }

        parseQueryTree      = 0;
        mddConstants        = 0;
        currentClientTblElt = 0;
        delete qtree;

        // delete set of mdd constants
        context->releaseTransferStructures();

        //
        // done
        //

        context->release();
    }
    else
    {
        LERROR << "Error: client not registered.";
        returnValue = 1;
    }


#ifdef RMANBENCHMARK
    // stop evaluation timer
    if (context->evaluationTimer)
    {
        delete context->evaluationTimer;
        context->evaluationTimer = 0;
    }

    Tile::opTimer.stop();
    Tile::relTimer.stop();
#endif

#ifdef ENABLE_PROFILING
    ProfilerStop();
    HeapProfilerStop();
#endif

    return returnValue;
}

unsigned short
ServerComm::executeInsert(unsigned long callingClientId,
                          const char* query,
                          ExecuteQueryRes& returnStructure)
{
    LINFO << "Request: '" << query << "'...";

#ifdef ENABLE_PROFILING
    startProfiler("/tmp/rasdaman_query_insert.XXXXXX.pprof", true);
    startProfiler("/tmp/rasdaman_query_insert.XXXXXX.pprof", false);
#endif

#ifdef RMANBENCHMARK
    Tile::relTimer.start();
    Tile::relTimer.pause();
    Tile::opTimer.start();
    Tile::opTimer.pause();
#endif

    unsigned short returnValue = 0;

    ClientTblElt* context = getClientContext(callingClientId);

    if (context != 0)
    {
#ifdef PURIFY
        purify_printf("%s\n", query);
#endif

        //
        // execute the query
        //

        QueryTree* qtree = new QueryTree();   // create a query tree object...
        parseQueryTree   = qtree;             // ...and assign it to the global parse query tree pointer;

        mddConstants     = context->transferColl; // assign the mdd constants collection to the global pointer (temporary)
        currentClientTblElt = context;        // assign current client table element (temporary)

        int ppRet = 0;
        udfEnabled = false; // forced for RNP tests
        if (udfEnabled)
        {
            //
            // preprocess
            //
            LINFO << "preprocessing...";
            ppInBuf = const_cast<char*>(query);
            ppreset();
            ppRet = ppparse();

            if (ppOutBuf)
            {
                LTRACE << "new query: '" << ppOutBuf << "'";
            }
            else
            {
                LTRACE << "new query: empty.";
            }

            // initialize the input string parameters
            beginParseString = ppOutBuf;
            iterParseString  = ppOutBuf;
        }
        else
        {
            beginParseString = const_cast<char*>(query);
            iterParseString  = const_cast<char*>(query);
        }

        yyreset();

        LINFO << "parsing...";

        if (ppRet == 0 && yyparse(0) == 0)
        {
            try
            {
#ifdef DEBUG
                qtree->printTree(2, RMInit::logOut);
#endif

                LINFO << "checking semantics...";

                qtree->checkSemantics();

#ifdef DEBUG
                qtree->printTree(2, RMInit::logOut);
#endif

#ifdef RMANBENCHMARK
                if (RManBenchmark > 0)
                {
                    context->evaluationTimer = new RMTimer("ServerComm", "evaluation");
                }
#endif

                LINFO << "evaluating...";

                context->transferData = qtree->evaluateUpdate();
            }
            catch (ParseInfo& info)
            {
                // release data
                context->releaseTransferStructures();

                returnValue = 5;           // evaluation error

                // set the error values of the return structure
                returnStructure.errorNo    = info.getErrorNo();
                returnStructure.lineNo     = info.getLineNo();
                returnStructure.columnNo   = info.getColumnNo();
                returnStructure.token      = strdup(info.getToken().c_str());

                info.printStatus(RMInit::logOut);
            }
            catch (r_Error& err)
            {
                context->releaseTransferStructures();
                context->release();
#ifdef DEBUG
                LFATAL << "Error: " << err.get_errorno() << " " << err.what();
#endif
                throw;
            }

            if (returnValue == 0)
            {
                if (context->transferData != 0)
                {
                    // create the transfer iterator
                    context->transferDataIter    = new vector<QtData*>::iterator;
                    *(context->transferDataIter) = context->transferData->begin();

                    //
                    // set typeName and typeStructure
                    //

                    // The type of first result object is used to determine the type of the result
                    // collection.
                    if (*(context->transferDataIter) != context->transferData->end())
                    {
                        QtData* firstElement = (**(context->transferDataIter));

                        if (firstElement->getDataType() == QT_MDD)
                        {
                            QtMDD* mddObj = static_cast<QtMDD*>(firstElement);
                            const BaseType* baseType = mddObj->getMDDObject()->getCellType();
                            r_Minterval     domain   = mddObj->getLoadDomain();

                            MDDType* mddType = new MDDDomainType("tmp", const_cast<BaseType*>(baseType), domain);
                            SetType* setType = new SetType("tmp", mddType);

                            returnStructure.typeName      = strdup(setType->getTypeName());
                            returnStructure.typeStructure = setType->getTypeStructure();  // no copy

                            TypeFactory::addTempType(setType);
                            TypeFactory::addTempType(mddType);
                        }
                        else
                        {
                            returnValue = 1;       // evaluation ok, non-MDD elements

                            returnStructure.typeName      = strdup("");

                            // hack set type
                            char* elementType = firstElement->getTypeStructure();
                            returnStructure.typeStructure = static_cast<char*>(mymalloc(strlen(elementType) + 6));
                            sprintf(returnStructure.typeStructure, "set<%s>", elementType);
                            free(elementType);
                        }

                        strcpy(globalHTTPSetTypeStructure, returnStructure.typeStructure);

                        LINFO << MSG_OK << ", result type '" << returnStructure.typeStructure << "', " << context->transferData->size() << " element(s).";
                    }
                    else
                    {
                        LINFO << MSG_OK << ", result is empty.";
                        returnValue = 2;         // evaluation ok, no elements

                        returnStructure.typeName      = strdup("");
                        returnStructure.typeStructure = strdup("");
                    }
                }
                else
                {
                    LINFO << MSG_OK << ", result is empty.";
                    returnValue = 2;         // evaluation ok, no elements
                }
            }
        }
        else
        {
            if (ppRet)
            {
                LINFO << MSG_OK;
                returnValue = 2;
            }
            else    // parse error
            {
                if (parseError)
                {
                    returnStructure.errorNo    = parseError->getErrorNo();
                    returnStructure.lineNo     = parseError->getLineNo();
                    returnStructure.columnNo   = parseError->getColumnNo();
                    returnStructure.token      = strdup(parseError->getToken().c_str());
                    parseError->printStatus(RMInit::logOut);

                    delete parseError;
                    parseError = 0;
                }
                else
                {
                    returnStructure.errorNo = 309;
                    LERROR << "Error: unspecific internal parser error.";
                }

                yyreset(); // reset the input buffer of the scanner

                returnValue = 4;
            }
        }

        parseQueryTree      = 0;
        mddConstants        = 0;
        currentClientTblElt = 0;
        delete qtree;

        // In case of an error or the result set is empty, no endTransfer()
        // is called by the client.
        // Therefore, some things have to be release here.
        if (returnValue >= 2)
        {
            context->releaseTransferStructures();
        }
        context->release();

        // delete set of mdd constants
        //context->releaseTransferStructures();

        //
        // done
        //

        //context->release();
    }
    else
    {
        LERROR << "Error: client not registered.";
        returnValue = 3;
    }


#ifdef RMANBENCHMARK
    // stop evaluation timer
    if (context->evaluationTimer)
    {
        delete context->evaluationTimer;
        context->evaluationTimer = 0;
    }

    Tile::opTimer.stop();
    Tile::relTimer.stop();
#endif

#ifdef ENABLE_PROFILING
    ProfilerStop();
    HeapProfilerStop();
#endif

    return returnValue;
}




unsigned short
ServerComm::getCollByName(unsigned long callingClientId,
                          const char*   collName,
                          char*&         typeName,
                          char*&         typeStructure,
                          r_OId&         oid)
{
    LINFO << "Request: 'get collection by name', name = " << collName << "'...";

    unsigned short returnValue = 0;

    ClientTblElt* context = getClientContext(callingClientId);

    if (context != 0)
    {
        //
        // create the actual transfer collenction
        //

        // delete old transfer collection/iterator
        context->releaseTransferStructures();

        // create the transfer collection
        try
        {
            context->transferColl = MDDColl::getMDDCollection(collName);
            LTRACE << "retrieved mdd collection";
        }
        catch (std::bad_alloc)
        {
            LFATAL << "Error: cannot allocate memory.";
            context->release(); //!!!
            throw;
        }
        catch (r_Error& err)
        {
#ifdef DEBUG
            LFATAL << "Error " << err.get_errorno() << " " << err.what();
#endif
            context->release(); //!!!
            throw;
        }
        catch (...)
        {
            returnValue = 2;  // collection name invalid
            LERROR << "Error: unspecific exception.";
        }

        if (returnValue == 0)
        {
            // create the transfer iterator
            context->transferCollIter = context->transferColl->createIterator();
            context->transferCollIter->reset();

            // set typeName and typeStructure
            CollectionType* collectionType = const_cast<CollectionType*>(context->transferColl->getCollectionType());

            if (collectionType)
            {
                typeName      = strdup(collectionType->getTypeName());
                typeStructure = collectionType->getTypeStructure();  // no copy !!!

                // set oid in case of a persistent collection
                if (context->transferColl->isPersistent())
                {
                    EOId eOId;
                    if (context->transferColl->isPersistent())
                    {
                        if (context->transferColl->getEOId(eOId) == true)
                        {
                            oid = r_OId(eOId.getSystemName(), eOId.getBaseName(), eOId.getOId());
                        }
                    }
                }
                LINFO << MSG_OK;
            }
            else
            {
                LWARNING << "Warning: cannot obtain collection type information.";
                typeName      = strdup("");
                typeStructure = strdup("");
            }

            if (!context->transferCollIter->notDone())
            {
                LINFO << MSG_OK << ", result empty.";
                returnValue = 1;

                // delete transfer collection/iterator
                context->releaseTransferStructures();
            }
        }

        //
        // done
        //

        context->release();
    }
    else
    {
        LERROR << "Error: client not registered.";
        returnValue = 3;
    }

    return returnValue;
}



unsigned short
ServerComm::getCollByOId(unsigned long callingClientId,
                         r_OId&         oid,
                         char*&         typeName,
                         char*&         typeStructure,
                         char*&         collName)
{
    LINFO << "Request: 'get collection by OID', oid = " << oid << "...";

    unsigned short returnValue = 0;

    ClientTblElt* context = getClientContext(callingClientId);

    if (context != 0)
    {
        // delete old transfer collection/iterator
        context->releaseTransferStructures();

        // check type and existence of oid
        OId oidIf(oid.get_local_oid());
        OId::OIdType objType = oidIf.getType();

        if (objType == OId::MDDCOLLOID)
        {
            //
            // get collection
            //

            try
            {
                LTRACE << "  execute new PersMDDColl(" << oid << ")";
                context->transferColl = MDDColl::getMDDCollection(oidIf);
                LTRACE << "  ok";
            }
            catch (std::bad_alloc)
            {
                LFATAL << "Error: cannot allocate memory.";
                throw;
            }
            catch (r_Error& err)
            {
#ifdef DEBUG
                LFATAL << "Error " << err.get_errorno() << " " << err.what();
#endif
                throw;
            }
            catch (...) // not found (?)
            {
                returnValue = 2;
                LERROR << "Error: unspecific exception.";
            }

            //
            // create the actual transfer collenction
            //

            if (returnValue == 0)
            {
                // get collection name
                collName = strdup(context->transferColl->getName());

                // create the transfer iterator
                context->transferCollIter = context->transferColl->createIterator();
                context->transferCollIter->reset();

                // set typeName and typeStructure
                CollectionType* collectionType = const_cast<CollectionType*>(context->transferColl->getCollectionType());

                if (collectionType)
                {
                    typeName      = strdup(collectionType->getTypeName());
                    typeStructure = collectionType->getTypeStructure();  // no copy !!!

                    // set oid in case of a persistent collection
                    if (context->transferColl->isPersistent())
                    {
                        EOId eOId;

                        if (context->transferColl->getEOId(eOId) == true)
                        {
                            oid = r_OId(eOId.getSystemName(), eOId.getBaseName(), eOId.getOId());
                        }
                    }
                    LINFO << MSG_OK;
                }
                else
                {
                    LINFO << MSG_OK << ", but warning: cannot obtain type information.";
                    typeName      = strdup("");
                    typeStructure = strdup("");
                }

                if (!context->transferCollIter->notDone())
                {
                    LINFO << MSG_OK << ", result empty.";
                    returnValue = 1;

                    // delete transfer collection/iterator
                    context->releaseTransferStructures();
                }
            }

        }
        else
        {
            returnValue = 2; // oid does not belong to a collection object
            LERROR << "Error: oid does not belong to a collection object.";
        }

        //
        // done
        //

        context->release();
    }
    else
    {
        LERROR << "Error: client not registered.";
        returnValue = 3;
    }

    return returnValue;
}



unsigned short
ServerComm::getCollOIdsByName(unsigned long callingClientId,
                              const char*   collName,
                              char*&         typeName,
                              char*&         typeStructure,
                              r_OId&         oid,
                              RPCOIdEntry*&  oidTable,
                              unsigned int&  oidTableSize)
{
    LINFO << "Request: 'get collection OIds by name', name = " << collName << "'...";

    unsigned short returnValue = 0;

    ClientTblElt* context = getClientContext(callingClientId);

    if (context != 0)
    {
        //
        // get collection
        //

        MDDColl* coll = 0;
        MDDColl* almost = 0;

        try
        {
            LTRACE << "retrieving collection " << collName;
            almost = MDDColl::getMDDCollection(collName);
            LTRACE << "retrieved collection " << collName;
            if (!almost->isPersistent())
            {
                LTRACE << "retrieved system collection";
                LFATAL << "Error: trying to get oid of system collection: " << collName;
                throw r_Error(SYSTEM_COLLECTION_HAS_NO_OID);
            }
            else
            {
                LTRACE << "retrieved persistent collection";
                coll = static_cast<MDDColl*>(almost);
            }
        }
        catch (std::bad_alloc)
        {
            LFATAL << "Error: cannot allocate memory.";
            throw;
        }
        catch (r_Error& err)
        {
            LTRACE << "caught exception";
#ifdef DEBUG
            LERROR << "Error " << err.get_errorno() << ": " << err.what();
#endif
            returnValue = 2;  // collection name invalid
        }
        catch (...)
        {
            LTRACE << "caught exception";
            returnValue = 2;  // collection name invalid
            LERROR << "Error: unspecific exception.";
        }
        LTRACE << "after exception catching";

        if (returnValue == 0)
        {
            // set typeName and typeStructure
            CollectionType* collectionType = const_cast<CollectionType*>(coll->getCollectionType());

            if (collectionType)
            {
                typeName      = strdup(collectionType->getTypeName());
                typeStructure = collectionType->getTypeStructure();  // no copy !!!

                // set oid
                EOId eOId;

                if (coll->getEOId(eOId) == true)
                {
                    oid = r_OId(eOId.getSystemName(), eOId.getBaseName(), eOId.getOId());
                }
            }
            else
            {
                LWARNING << "Warning: no type information available...";
                typeName      = strdup("");
                typeStructure = strdup("");
            }

            if (coll->getCardinality())
            {
                // create iterator
                MDDCollIter* collIter = coll->createIterator();
                int          i;

                oidTableSize = coll->getCardinality();
                oidTable     = static_cast<RPCOIdEntry*>(mymalloc(sizeof(RPCOIdEntry) * oidTableSize));

                LDEBUG << oidTableSize << " elements..." ;

                for (collIter->reset(), i = 0; collIter->notDone(); collIter->advance(), i++)
                {
                    MDDObj* mddObj = collIter->getElement();

                    if (mddObj->isPersistent())
                    {
                        EOId eOId;

                        if ((static_cast<MDDObj*>(mddObj))->getEOId(&eOId) == 0)
                        {
                            oidTable[i].oid = strdup(r_OId(eOId.getSystemName(), eOId.getBaseName(), eOId.getOId()).get_string_representation());
                        }
                        else
                        {
                            oidTable[i].oid = strdup("");
                        }
                        mddObj = 0;
                    }
                    else
                    {
                        oidTable[i].oid = strdup("");
                    }
                }

                delete collIter;

                LINFO << MSG_OK << ", " << coll->getCardinality() << " result(s).";
            }
            else
            {
                LINFO << MSG_OK << ", result empty.";
                returnValue = 1;
            }

            delete coll;
        }

        //
        // done
        //

        context->release();
    }
    else
    {
        LERROR << "Error: client not registered.";
        returnValue = 3;
    }

    return returnValue;
}


unsigned short
ServerComm::getCollOIdsByOId(unsigned long callingClientId,
                             r_OId&         oid,
                             char*&         typeName,
                             char*&         typeStructure,
                             RPCOIdEntry*&  oidTable,
                             unsigned int&  oidTableSize,
                             char*&         collName)
{
    LINFO << "Request: 'get collection OIDs by OId', oid = " << oid << "...";

    unsigned short returnValue = 0;

    ClientTblElt* context = getClientContext(callingClientId);

    if (context != 0)
    {
        // check type and existence of oid
        OId oidIf(oid.get_local_oid());
        OId::OIdType objType = oidIf.getType();

        if (objType == OId::MDDCOLLOID)
        {
            //
            // get collection
            //

            MDDColl* coll = 0;

            try
            {
                LTRACE << "get mdd coll by oid " << oidIf;
                coll = MDDColl::getMDDCollection(oidIf);
                LTRACE << "retrieved mdd coll";
            }
            catch (std::bad_alloc)
            {
                LFATAL << "Error: cannot allocate memory.";
                throw;
            }
            catch (r_Error& err)
            {
#ifdef DEBUG
                LERROR << "Error " << err.get_errorno() << ": " << err.what();
#endif
                returnValue = 2;  // collection name invalid
                if (err.get_kind() != r_Error::r_Error_RefNull)
                {
                    throw;
                }
            }
            catch (...)
            {
                returnValue = 2;  // collection name invalid
                LERROR << "Error: unknown collection name.";
            }

            if (returnValue == 0)
            {
                // get collection name
                collName = strdup(coll->getName());

                // set typeName and typeStructure
                CollectionType* collectionType = const_cast<CollectionType*>(coll->getCollectionType());

                if (collectionType)
                {
                    typeName      = strdup(collectionType->getTypeName());
                    typeStructure = collectionType->getTypeStructure();  // no copy !!!

                    // set oid
                    EOId eOId;

                    if (coll->getEOId(eOId) == true)
                    {
                        oid = r_OId(eOId.getSystemName(), eOId.getBaseName(), eOId.getOId());
                    }
                }
                else
                {
                    LWARNING << "Warning: no type information available...";
                    typeName      = strdup("");
                    typeStructure = strdup("");
                }

                if (coll->getCardinality())
                {
                    // create iterator
                    MDDCollIter* collIter = coll->createIterator();
                    int          i;

                    oidTableSize = coll->getCardinality();
                    oidTable     = static_cast<RPCOIdEntry*>(mymalloc(sizeof(RPCOIdEntry) * oidTableSize));

                    LDEBUG << oidTableSize << " elements..." ;

                    for (collIter->reset(), i = 0; collIter->notDone(); collIter->advance(), i++)
                    {
                        MDDObj* mddObj = collIter->getElement();

                        if (mddObj->isPersistent())
                        {
                            EOId eOId;

                            if ((static_cast<MDDObj*>(mddObj))->getEOId(&eOId) == 0)
                            {
                                oidTable[i].oid = strdup(r_OId(eOId.getSystemName(), eOId.getBaseName(), eOId.getOId()).get_string_representation());
                            }
                            else
                            {
                                oidTable[i].oid = strdup("");
                            }
                        }
                        else
                        {
                            oidTable[i].oid = strdup("");
                        }
                    }

                    delete collIter;
                    //coll->releaseAll();

                    LINFO << MSG_OK << ", " << coll->getCardinality() << " result(s).";
                }
                else
                {
                    LINFO << MSG_OK << ", result empty.";
                    returnValue = 1;
                }

                delete coll;
            }
        }
        else
        {
            returnValue = 2; // oid does not belong to a collection object
            LERROR << "Error: not a collection oid: " << oid;
        }

        //
        // done
        //

        context->release();
    }
    else
    {
        LERROR << "Error: client not registered.";
        returnValue = 3;
    }

    return returnValue;
}



unsigned short
ServerComm::getNextMDD(unsigned long   callingClientId,
                       r_Minterval&     mddDomain,
                       char*&           typeName,
                       char*&           typeStructure,
                       r_OId&           oid,
                       unsigned short&  currentFormat)
{
#ifdef DEBUG
    LINFO << "Request (continuing): 'get next MDD'...";
#endif

    unsigned short returnValue = 0;

    ClientTblElt* context = getClientContext(callingClientId);

    if (context != 0)
    {
        try
        {
            if (context->transferData && context->transferDataIter && *(context->transferDataIter) != context->transferData->end())
            {
                //
                // convert the mdd to transfer to rpc data structures
                //

                // get the MDD object to be transfered
                QtMDD*  mddData = static_cast<QtMDD*>(**(context->transferDataIter));
                MDDObj* mddObj  = mddData->getMDDObject();

                // initialize mddDomain to give it back
                mddDomain = mddData->getLoadDomain();

                LDEBUG << "domain " << mddDomain;

                //
                // initialize tiles to transfer
                //

#ifdef RMANBENCHMARK
                // pause transfer timer and resume evaluation timer
                if (context->transferTimer)
                {
                    context->transferTimer->pause();
                }
                if (context->evaluationTimer)
                {
                    context->evaluationTimer->resume();
                }
#endif

                if (mddObj->getCurrentDomain() == mddData->getLoadDomain())
                {
                    // This is a hack. The mddObj is a part of context->transferDataIter and it will
                    // not be deleted until the end of transaction, so storing raw pointers is safe.
                    // FIXME: change context->transTiles type to vector< shared_ptr<Tile> >
                    boost::scoped_ptr<vector<boost::shared_ptr<Tile>>> tiles(mddObj->getTiles());
                    context->transTiles = new vector<Tile*>();
                    for (size_t i = 0; i < tiles->size(); ++i)
                    {
                        context->transTiles->push_back((*tiles)[i].get());
                    }
                }
                else
                {
                    // If the load domain is different from the current domain, we have
                    // a persitent MDD object. The border tiles have to be cut (and
                    // therefore copied) in order to be ready for transfering them.
                    // These temporary border tiles are added to the deletableTiles list
                    // which is deleted at the end.

                    // This is a hack. The mddObj is a part of context->transferDataIter and it will
                    // not be deleted until the end of transaction, so storing raw pointers is safe.
                    // FIXME: change context->transTiles type to vector< shared_ptr<Tile> >
                    boost::scoped_ptr<vector<boost::shared_ptr<Tile>>> tiles(mddObj->intersect(mddData->getLoadDomain()));
                    context->transTiles = new vector<Tile*>();
                    for (size_t i = 0; i < tiles->size(); ++i)
                    {
                        context->transTiles->push_back((*tiles)[i].get());
                    }

                    // iterate over the tiles
                    for (vector<Tile*>::iterator iter = context->transTiles->begin(); iter != context->transTiles->end(); iter++)
                    {
                        // get relevant area of source tile
                        r_Minterval sourceTileDomain(mddData->getLoadDomain().create_intersection((*iter)->getDomain()));

                        if (sourceTileDomain != (*iter)->getDomain())
                        {
                            // create a new transient tile and copy the transient data
                            Tile* newTransTile = new Tile(sourceTileDomain, mddObj->getCellType());
                            newTransTile->copyTile(sourceTileDomain, *iter, sourceTileDomain);

                            // replace the tile in the list with the new one
                            *iter = newTransTile;

                            // add the new tile to deleteableTiles
                            if (!(context->deletableTiles))
                            {
                                context->deletableTiles = new vector<Tile*>();
                            }
                            context->deletableTiles->push_back(newTransTile);
                        }
                    }
                }

#ifdef RMANBENCHMARK
                // In order to be sure that reading tiles from disk is done
                // in the evaluation phase, the contents pointers of each tile
                // are got.
                char* benchmarkPointer;

                for (vector<Tile*>::iterator benchmarkIter = context->transTiles->begin();
                        benchmarkIter != context->transTiles->end(); benchmarkIter++)
                {
                    benchmarkPointer = (*benchmarkIter)->getContents();
                }

                // pause evaluation timer and resume transfer timer
                if (context->evaluationTimer)
                {
                    context->evaluationTimer->pause();
                }
                if (context->transferTimer)
                {
                    context->transferTimer->resume();
                }
#endif

                // initialize tile iterator
                context->tileIter   = new vector<Tile*>::iterator;
                *(context->tileIter) = context->transTiles->begin();

                const BaseType* baseType = mddObj->getCellType();

                LDEBUG << "cell length " << baseType->getSize();

                //
                // set typeName and typeStructure
                //
                // old: typeName = strdup( mddObj->getCellTypeName() ); not known for the moment being
                typeName      = strdup("");

                // create a temporary mdd type for the moment being
                r_Minterval typeDomain(mddData->getLoadDomain());
                MDDType* mddType = new MDDDomainType("tmp", const_cast<BaseType*>(baseType), typeDomain);
                TypeFactory::addTempType(mddType);

                typeStructure = mddType->getTypeStructure();  // no copy !!!

                // I'm not sure about this code...
#if 0
                // determine data format from the 1st tile
                if (context->transTiles->size() && (*(context->transTiles))[0]->getDataFormat() == r_TIFF)
                {
                    currentFormat = r_TIFF;
                }
                else
                {
                    currentFormat = r_Array;
                }
#else
                if (context->transTiles->size())
                {
                    currentFormat = (*(context->transTiles))[0]->getDataFormat();
                }
                else
                {
                    currentFormat = r_Array;
                }
#endif

                // set oid in case of persistent MDD objects
                if (mddObj->isPersistent())
                {
                    EOId eOId;

                    if ((static_cast<MDDObj*>(mddObj))->getEOId(&eOId) == 0)
                    {
                        oid = r_OId(eOId.getSystemName(), eOId.getBaseName(), eOId.getOId());
                    }
                }

                //
                //
                //

                if (context->transTiles->size() > 0)
                {
#ifdef DEBUG
                    LINFO << MSG_OK << ", " << context->transTiles->size() << " more tile(s)";
#endif
                }
                else   // context->transTiles->size() == 0
                {
                    returnValue = 2;
                    LERROR << "Error: no tiles in MDD object.";
                }

                context->totalTransferedSize = 0;
                context->totalRawSize = 0;
            }
            else
            {
                if (context->transferDataIter && *(context->transferDataIter) == context->transferData->end())
                {
                    returnValue = 1;  // nothing left in the collection
#ifdef DEBUG
                    LINFO << MSG_OK << ", no more tiles.";
#endif
                    context->releaseTransferStructures();
                }
                else
                {
                    returnValue = 2;  // no actual transfer collection
                    LERROR << "Error: no transfer collection. ";
                }
            }

            //
            // done
            //

            context->release();
        }
        catch (r_Ebase_dbms& myErr)
        {
            LFATAL << "Error: base DBMS exception (kind " << static_cast<unsigned int>(myErr.get_kind()) << ", errno " << myErr.get_errorno() << ") " << myErr.what();
            returnValue = 42;
            throw;
        }
        catch (r_Error& myErr)
        {
#ifdef DEBUG
            LFATAL << "Error: (kind " << myErr.get_kind() << ", errno " << myErr.get_errorno() << ") " << myErr.what();
#endif
            throw;
        }
        catch (std::bad_alloc)
        {
            LFATAL << "Error: cannot allocate memory.";
            throw;
        }
        catch (...)
        {
            LERROR << "Error: unspecified exception.";
        }
    }
    else
    {
        LERROR << "Error: client not registered.";
        returnValue = 2;
    }

    return returnValue;
}

void
ServerComm::getNextStructElement(char*&     buffer,
                                 BaseType*       baseType)
{
    switch (baseType->getType())
    {
    case USHORT:
    {
        r_UShort tmp = *(r_UShort*) buffer;
        *(r_UShort*) buffer = r_Endian::swap(tmp);
    }
    break;

    case SHORT:
    {
        r_Short tmp = *(r_Short*) buffer;
        *(r_Short*) buffer = r_Endian::swap(tmp);
    }
    break;

    case LONG:
    {
        r_Long tmp = *(r_Long*) buffer;
        *(r_Long*) buffer = r_Endian::swap(tmp);
    }
    break;

    case ULONG:
    {
        r_ULong tmp = *(r_ULong*) buffer;
        *(r_ULong*) buffer = r_Endian::swap(tmp);
    }
    break;

    case FLOAT:
    {
        uint32_t value = bswap_32(*(uint32_t*) buffer);
        // use memcpy because older (<4.5?) gcc versions
        // choke if we assign to buffer directly
        memcpy(buffer, &value, sizeof(uint32_t));
    }
    break;

    case DOUBLE:
    {
        uint64_t value = bswap_64(*(uint64_t*) buffer);
        // use memcpy because older (<4.5?) gcc versions
        // choke if we assign to buffer directly
        memcpy(buffer, &value, sizeof(uint64_t));
    }
    break;

    case STRUCT:
    {
        StructType* st = static_cast<StructType*>(baseType);
        unsigned int numElems = st->getNumElems();
        unsigned int i;
        for (i = 0; i < numElems; i++)
        {
            BaseType* bt = const_cast<BaseType*>(st->getElemType(i));
            unsigned int elemTypeSize = bt->getSize();
            getNextStructElement(buffer, bt);
            buffer += elemTypeSize;
        }
    }
    break;

    default:
        break;
    }

}

unsigned short
ServerComm::getNextElement(unsigned long   callingClientId,
                           char*&           buffer,
                           unsigned int&    bufferSize)
{
#ifdef DEBUG
    LINFO << "Request (continuing): 'get next element'...";
#endif

    unsigned short returnValue = 0;

    ClientTblElt* context = getClientContext(callingClientId);

    if (context != 0)
    {
        LTRACE << "getNextElement(...) TRANSFER " << context->transferFormat << ", EXACT " << (bool)context->exactFormat;

        if (context->transferData && context->transferDataIter &&
                *(context->transferDataIter) != context->transferData->end())
        {

            //
            // convert data element to rpc data structures
            //
            // Buffer is allocated and has to be freed by the caller using free().

            // get next object to be transfered
            try
            {
                QtData*  dataObj = **(context->transferDataIter);

                switch (dataObj->getDataType())
                {
                case QT_STRING:
                {
                    QtStringData* stringDataObj       = static_cast<QtStringData*>(dataObj);
                    bufferSize = stringDataObj->getStringData().length() + 1;
                    buffer     = static_cast<char*>(mymalloc(bufferSize));
                    memcpy(buffer, stringDataObj->getStringData().c_str(), bufferSize);
                }
                break;
                case QT_INTERVAL:
                {
                    QtIntervalData*  intervalDataObj  = static_cast<QtIntervalData*>(dataObj);
                    char*            stringData       = intervalDataObj->getIntervalData().get_string_representation();
                    bufferSize = strlen(stringData) + 1;
                    buffer     = static_cast<char*>(mymalloc(bufferSize));
                    memcpy(buffer, stringData, bufferSize);
                    free(stringData);
                }
                break;
                case QT_MINTERVAL:
                {
                    QtMintervalData* mintervalDataObj = static_cast<QtMintervalData*>(dataObj);
                    char*            stringData       = mintervalDataObj->getMintervalData().get_string_representation();
                    bufferSize = strlen(stringData) + 1;
                    buffer     = static_cast<char*>(mymalloc(bufferSize));
                    memcpy(buffer, stringData, bufferSize);
                    free(stringData);
                }
                break;
                case QT_POINT:
                {
                    QtPointData* pointDataObj         = static_cast<QtPointData*>(dataObj);
                    char*            stringData       = pointDataObj->getPointData().get_string_representation();
                    bufferSize = strlen(stringData) + 1;
                    buffer     = static_cast<char*>(mymalloc(bufferSize));
                    memcpy(buffer, stringData, bufferSize);

                    free(stringData);
                }
                break;
                default:
                    if (dataObj->isScalarData())
                    {
                        QtScalarData* scalarDataObj = static_cast<QtScalarData*>(dataObj);
                        bufferSize = scalarDataObj->getValueType()->getSize();
                        buffer     = static_cast<char*>(mymalloc(bufferSize));
                        memcpy(buffer, scalarDataObj->getValueBuffer(), bufferSize);
                        // server endianess
                        r_Endian::r_Endianness serverEndian = r_Endian::get_endianness();

                        // change endianess if necessary
                        // currently only one client is active at one time
                        //  if((context->clientId == 1) && (strcmp(context->clientIdText, ServerComm::HTTPCLIENT) == 0) &&  (serverEndian != r_Endian::r_Endian_Big))
                        if ((strcmp(context->clientIdText, ServerComm::HTTPCLIENT) == 0) && (serverEndian != r_Endian::r_Endian_Big))
                        {
#ifdef DEBUG
                            LINFO << "changing endianness...";
#endif
                            // calling client is a http-client(java -> always BigEndian) and server has LittleEndian
                            switch (scalarDataObj->getDataType())
                            {
                            case QT_USHORT:
                            {
                                r_UShort tmp = *(r_UShort*)buffer;
                                *(r_UShort*)buffer = r_Endian::swap(tmp);
                            }
                            break;

                            case QT_SHORT:
                            {
                                r_Short tmp = *(r_Short*)buffer;
                                *(r_Short*)buffer = r_Endian::swap(tmp);
                            }
                            break;

                            case QT_LONG:
                            {
                                r_Long tmp = *(r_Long*)buffer;
                                *(r_Long*)buffer = r_Endian::swap(tmp);
                            }
                            break;

                            case QT_ULONG:
                            {
                                r_ULong tmp = *(r_ULong*)buffer;
                                *(r_ULong*)buffer = r_Endian::swap(tmp);
                            }
                            break;

                            case QT_FLOAT:
                            {
                                uint32_t value = bswap_32(*(uint32_t*)buffer);
                                // use memcpy because older (<4.5?) gcc versions
                                // choke if we assign to buffer directly
                                memcpy(buffer, &value, sizeof(uint32_t));
                            }
                            break;

                            case QT_DOUBLE:
                            {
                                uint64_t value = bswap_64(*(uint64_t*)buffer);
                                // use memcpy because older (<4.5?) gcc versions
                                // choke if we assign to buffer directly
                                memcpy(buffer, &value, sizeof(uint64_t));
                            }
                            break;

                            case QT_COMPLEX:
                            {
                                StructType* st = static_cast<StructType*>(const_cast<BaseType*>(scalarDataObj->getValueType()));
                                unsigned int numElems = st->getNumElems();
                                unsigned int i;
                                char* tmp = buffer;
                                for (i = 0; i < numElems; i++)
                                {
                                    BaseType* bt = const_cast<BaseType*>(st->getElemType(i));
                                    unsigned int elemTypeSize = bt->getSize();
                                    getNextStructElement(buffer, bt);
                                    buffer += elemTypeSize;
                                }
                                buffer = tmp;
                            }
                            break;

                            default:
                                break;
                            }
                        }
                    }
                    break;
                }
            }
            catch (r_Ebase_dbms& myErr)
            {
                LFATAL << "Error: base BMS exception (kind " << static_cast<unsigned int>(myErr.get_kind()) << ", errno " << myErr.get_errorno() << ") " << myErr.what();
                throw;
            }
            catch (r_Error& err)
            {
#ifdef DEBUG
                LFATAL << "Error: exception (kind " << err.get_kind() << ", errno " << err.get_errorno() << ") " << err.what();
#endif
                throw;
            }

            // increment list iterator
            (*(context->transferDataIter))++;

            if (*(context->transferDataIter) != context->transferData->end())
            {
                returnValue = 0;
#ifdef DEBUG
                LINFO << MSG_OK << ", some more tile(s) left.";
#endif
            }
            else
            {
                returnValue = 1;
#ifdef DEBUG
                LINFO << MSG_OK << ", no more tiles.";
#endif
            }
        }
        else
        {
            if (context->transferDataIter && *(context->transferDataIter) == context->transferData->end())
            {
                returnValue = 1;  // nothing left in the collection
                LDEBUG << "nothing left..." << MSG_OK;
                context->releaseTransferStructures();
            }
            else
            {
                returnValue = 2;  // no actual transfer collection
                LERROR << "Error: no transfer collection.";
            }
        }

        //
        // done
        //

        context->release();
    }
    else
    {
        LERROR << "Error: client not registered.";
        returnValue = 2;
    }

    return returnValue;
}



unsigned short
ServerComm::getMDDByOId(unsigned long   callingClientId,
                        r_OId&           oid,
                        r_Minterval&     mddDomain,
                        char*&           typeName,
                        char*&           typeStructure,
                        unsigned short&  currentFormat)
{
#ifdef DEBUG
    LINFO << "Request: 'get MDD by OId', oid = " << oid << "...";
#endif

    unsigned short returnValue = 0;

    ClientTblElt* context = getClientContext(callingClientId);

    if (context != 0)
    {
        // delete old transfer collection/iterator
        context->releaseTransferStructures();

        // check type and existence of oid
        OId oidIf(oid.get_local_oid());
        OId::OIdType objType = oidIf.getType();

        if (objType == OId::MDDOID)
        {
            // get MDD object
            try
            {
                context->transferMDD = new MDDObj(oidIf);
            }
            catch (std::bad_alloc)
            {
                LFATAL << "Error: cannot allocate memory.";
                context->release();
                throw;
            }
            catch (r_Error& err)
            {
#ifdef DEBUG
                LFATAL << "Error: (kind " << err.get_kind() << ", errno " << err.get_errorno() << ") " << err.what();
#endif
                context->release();
                throw;
            }
            catch (...)
            {
                returnValue = 2;
                LERROR << "Error: unspecified exception.";
            }

            if (!returnValue)
            {
                //
                // convert the mdd to transfer to rpc data structures
                //

                // initialize mddDomain to give it back
                mddDomain = context->transferMDD->getCurrentDomain();

                LDEBUG << "domain " << mddDomain;

                // initialize context fields
                // This is a hack. The mddObj is a part of context->transferDataIter and it will
                // not be deleted until the end of transaction, so storing raw pointers is safe.
                // FIXME: change context->transTiles type to vector< shared_ptr<Tile> >
                boost::scoped_ptr<vector<boost::shared_ptr<Tile>>> tiles(context->transferMDD->getTiles());
                context->transTiles  = new vector<Tile*>;
                for (size_t i = 0; i < tiles->size(); ++i)
                {
                    context->transTiles->push_back((*tiles)[i].get());
                }
                context->tileIter    = new vector<Tile*>::iterator;
                *(context->tileIter) = context->transTiles->begin();

                const BaseType* baseType = context->transferMDD->getCellType();

                LDEBUG << "cell length " << baseType->getSize();

                //
                // set typeName and typeStructure
                //
                // old: typeName = strdup( context->transferMDD->getCellTypeName() ); not known for the moment being

                typeName      = strdup("");

                // create a temporary mdd type for the moment being
                MDDType* mddType = new MDDDomainType("tmp", const_cast<BaseType*>(baseType), context->transferMDD->getCurrentDomain());
                TypeFactory::addTempType(mddType);

                typeStructure = mddType->getTypeStructure();  // no copy !!!

                // I'm not sure about this code either
#if 0
                // determine data format from the 1st tile
                if (context->transTiles->size() && (*(context->transTiles))[0]->getDataFormat() == r_TIFF)
                {
                    currentFormat = r_TIFF;
                }
                else
                {
                    currentFormat = r_Array;
                }
#else
                if (context->transTiles->size())
                {
                    currentFormat = (*(context->transTiles))[0]->getDataFormat();
                }
                else
                {
                    currentFormat = r_Array;
                }
#endif

                // set oid in case of persistent MDD objects
                if (context->transferMDD->isPersistent())
                {
                    EOId eOId;

                    if ((static_cast<MDDObj*>(context->transferMDD))->getEOId(&eOId) == 0)
                    {
                        oid = r_OId(eOId.getSystemName(), eOId.getBaseName(), eOId.getOId());
                    }
                }

                //
                //
                //

                if (context->transTiles->size() > 0)
                {
#ifdef DEBUG
                    LINFO << MSG_OK << ", got " << context->transTiles->size() << " tile(s).";
#endif
                }
                else   // context->transTiles->size() == 0
                {
                    returnValue = 3;
                    LERROR << "Error: no tiles in MDD object.";
                }
            }
        }
        else
        {
            returnValue = 2; // oid does not belong to an MDD object
            LERROR << "Error: oid does not belong to an MDD object.";
        }

        //
        // done
        //

        context->release();
    }
    else
    {
        LERROR << "Error: client not registered.";
        returnValue = 1; // client context not found
    }

    context->totalRawSize = 0;
    context->totalTransferedSize = 0;

    return returnValue;
}



unsigned short
ServerComm::getNextTile(unsigned long   callingClientId,
                        RPCMarray**     rpcMarray)
{
#ifdef DEBUG
    LINFO << "Request (continuing): 'get next tile',...";
#endif

    unsigned long  transOffset = 0;
    unsigned long  transSize = 0;
    unsigned short statusValue = 0;
    unsigned short returnValue = 0;

    // initialize the result parameter for failure cases
    *rpcMarray = 0;

    ClientTblElt* context = getClientContext(callingClientId);

    if (context != 0)
    {
        if (context->transTiles && context->tileIter)
        {
            Tile* resultTile = **(context->tileIter);
            r_Minterval mddDomain = resultTile->getDomain();
            void* useTransData;
            unsigned long totalSize;

            // allocate memory for the output parameter rpcMarray
            *rpcMarray = static_cast<RPCMarray*>(mymalloc(sizeof(RPCMarray)));

            if (context->bytesToTransfer == 0)
            {
                // free old data
                if (context->encodedData != NULL)
                {
                    free(context->encodedData);
                    context->encodedData = NULL;
                    context->encodedSize = 0;
                }
            }

            // note: transfer compression affects the current format, not the storage format.
            if (context->encodedData == NULL)
            {
                totalSize = resultTile->getCompressedSize();
                //this is bad because useTransData is char* although it is not modified
                useTransData = static_cast<char*>(resultTile->getContents());
                (*rpcMarray)->currentFormat = resultTile->getDataFormat();
                LTRACE << "using tile format " << (r_Data_Format)(*rpcMarray)->currentFormat;
            }
            else
            {
                totalSize = context->encodedSize;
                useTransData = context->encodedData;
                (*rpcMarray)->currentFormat = context->transferFormat;
                //FILE *fp = fopen("trans_data.raw", "wb"); fwrite(useTransData, 1, totalSize, fp); fclose(fp);
                LTRACE << "using transfer format " << (r_Data_Format)(*rpcMarray)->currentFormat;
            }
            // Preserve storage format
            (*rpcMarray)->storageFormat = resultTile->getDataFormat();
            LTRACE << "rpc storage  " << (r_Data_Format)(*rpcMarray)->storageFormat;
            LTRACE << "rpc current  " << (r_Data_Format)(*rpcMarray)->currentFormat;

            transSize = totalSize;

            if (totalSize > maxTransferBufferSize)
            {
                // if there is the rest of a tile to transfer, do it!
                if (context->bytesToTransfer)
                {
                    LDEBUG << " resuming block transfer...";
                    transOffset = totalSize - context->bytesToTransfer;
                    if (context->bytesToTransfer > maxTransferBufferSize)
                    {
                        transSize = maxTransferBufferSize;
                        statusValue = 1;
                    }
                    else
                    {
                        transSize = context->bytesToTransfer;
                        statusValue = 2;
                    }

                    context->bytesToTransfer -= transSize;
                }
                else // transfer first block of too large tile
                {
                    LDEBUG << " has to be split...";
                    transSize = maxTransferBufferSize;
                    context->bytesToTransfer = totalSize - transSize;
                    statusValue = 1;
                }
            }
            else    // resultTile->getSize() <= maxTransferBufferSize
            {
                statusValue = 3;
            }

            context->totalTransferedSize += transSize;

            // 1. convert domain
            (*rpcMarray)->domain = mddDomain.get_string_representation();

            // 2. copy data pointers
            LDEBUG << " domain " << mddDomain << ", " << transSize << " bytes";

            // allocate memory for the output parameter data and assign its fields
            (*rpcMarray)->data.confarray_len = static_cast<unsigned int>(transSize);
            (*rpcMarray)->data.confarray_val = (static_cast<char*>(useTransData)) + transOffset;

            // 3. store cell type length
            (*rpcMarray)->cellTypeLength = resultTile->getType()->getSize();

            // increment iterator only if tile is transferred completely
            if (statusValue > 1)
            {
                context->totalRawSize += resultTile->getSize();
                (*context->tileIter)++;
            }

            // delete tile vector and increment transfer collection iterator if tile iterator is exhausted
            if ((*context->tileIter) == context->transTiles->end())
            {

                // delete tile vector transTiles (tiles are deleted when the object is deleted)
                if (context->transTiles)
                {
                    delete context->transTiles;
                    context->transTiles = 0;
                }

                // delete tile iterator
                if (context->tileIter)
                {
                    delete context->tileIter;
                    context->tileIter = 0;
                }

                if (context->transferDataIter)
                {
                    (*(context->transferDataIter))++;

                    if (*(context->transferDataIter) != context->transferData->end())
                    {
                        returnValue = 1;
#ifdef DEBUG
                        LDEBUG << " some MDDs left...";
                        LINFO << MSG_OK << ", some MDD(s) left.";
#endif
                    }
                    else
                    {
                        // no elements left -> delete collection and iterator

                        // Memory of last tile is still needed for the last byte transfer,
                        // therefore, do not release memory now, but with any next RPC call.
                        // context->releaseTransferStructures();

                        returnValue = 0;
#ifdef DEBUG
                        LINFO << MSG_OK << ", all MDDs fetched.";
#endif
                    }
                }
                else
                {
                    returnValue = 0;
#ifdef DEBUG
                    LINFO << MSG_OK << ", MDD transfer complete.";
#endif
                }

                if ((context->totalTransferedSize != context->totalRawSize) && (context->totalRawSize != 0))
                {
                    LDEBUG << "(compressed using " <<  context->transferFormat << " to " << ((r_Double)(100 * context->totalTransferedSize)) / context->totalRawSize << "%) ";
                }
            }
            else
            {
                if (statusValue == 1)    // at least one block in actual tile is left
                {
#ifdef DEBUG
                    LINFO << MSG_OK << ", some block(s) left.";
#endif
                    returnValue = 3;
                }
                else  // tiles left in actual MDD
                {
#ifdef DEBUG
                    LINFO << MSG_OK << ", some tile(s) left.";
#endif
                    returnValue = 2;
                }
            }
        }
        else    // no actual transfer collection or nothing left in the collection
        {
            returnValue = 4;
            LERROR << "Error: no transfer collection or nothing left in collection.";
        }

        context->release();
    }
    else
    {
        // client context not found
        LERROR << "Error: client not registered.";
        returnValue = 4;
    }

    return returnValue;
}


unsigned short
ServerComm::endTransfer(unsigned long client)
{
    unsigned short returnValue = 0;

#ifdef DEBUG
    LINFO << "Client " << client << " called: endTransfer...";
#endif

    ClientTblElt* context = getClientContext(client);

    if (context)
    {
#ifdef RMANBENCHMARK
        Tile::relTimer.stop();
        Tile::opTimer.stop();
        if (context->evaluationTimer)
        {
            delete context->evaluationTimer;
        }
        context->evaluationTimer = 0;
        if (context->transferTimer)
        {
            delete context->transferTimer;
        }
        context->transferTimer = 0;
        RMTimer* releaseTimer = 0;

        if (RManBenchmark > 0)
        {
            releaseTimer = new RMTimer("ServerComm", "release");
        }
#endif
        // release transfer collection/iterator
        context->releaseTransferStructures();

#ifdef RMANBENCHMARK
        if (releaseTimer)
        {
            delete releaseTimer;
        }
#endif

        context->release();

#ifdef DEBUG
        LINFO << MSG_OK;
#endif
    }
    else
    {
        LERROR << "Error: client not registered.";
        returnValue = 1;
    }

    return returnValue;
}



/*************************************************************************
 * Method name...: aliveSignal( unsigned long client )
 ************************************************************************/
unsigned short
ServerComm::aliveSignal(unsigned long client)
{
    unsigned short returnValue = 0;

#ifdef DEBUG
    LINFO << "Client " << client << " called: endTransfer...";
#endif

    ClientTblElt* context = getClientContext(client);

    if (context)
    {
        // set the time of the client's last action to now
        context->lastActionTime = static_cast<unsigned long>(time(NULL));

        returnValue = 1;

        context->release();

#ifdef DEBUG
        LINFO << MSG_OK;
#endif
    }
    else
    {
        LERROR << "Error: client not registered.";
    }

    return returnValue;
}



unsigned short
ServerComm::getNewOId(unsigned long callingClientId,
                      unsigned short objType,
                      r_OId& oid)
{
    unsigned short returnValue = 0;

    LINFO << "Request: 'get new OId for " << (objType == 1 ? "MDD" : "collection") << " type'...";

    ClientTblElt* context = getClientContext(callingClientId);

    if (context != 0)
    {
        EOId eOId;

        if (objType == 1)
        {
            EOId::allocateEOId(eOId, OId::MDDOID);
        }
        else // objType == 2
        {
            EOId::allocateEOId(eOId, OId::MDDCOLLOID);
        }

        LTRACE << "allocated " << eOId;
        oid = r_OId(eOId.getSystemName(), eOId.getBaseName(), eOId.getOId());

        context->release();

        LINFO << MSG_OK;
    }
    else
    {
        LERROR << "Error: client not registered.";
        returnValue = 1;
    }

    return returnValue;
}



unsigned short
ServerComm::getObjectType(unsigned long callingClientId,
                          r_OId& oid,
                          unsigned short& objType)
{
    unsigned short returnValue = 0;

    LINFO << "Request: 'get object type by OID', oid = " << oid << "...";

    ClientTblElt* context = getClientContext(callingClientId);

    if (context != 0)
    {
        OId oidIf(oid.get_local_oid());

        objType = oidIf.getType();

        if (objType == OId::INVALID)
        {
            // oid not found
            LERROR << "Error: no type for this oid.";
            returnValue = 2;
        }
        else
        {
            LINFO << "type is " << (objType == 1 ? "MDD" : "collection") << "..." << MSG_OK;
        }

        context->release();
    }
    else
    {
        LINFO << "Error: client not registered.";
        returnValue = 1;
    }

    return returnValue;
}



unsigned short
ServerComm::getTypeStructure(unsigned long  callingClientId,
                             const char* typeName,
                             unsigned short typeType,
                             char*& typeStructure)
{
    unsigned short returnValue = 0;

    LINFO << "Request: 'get type structure', type = '" << typeName << "'...";

    ClientTblElt* context = getClientContext(callingClientId);
    if (context == 0)
    {
        LERROR << "Error: client not registered.";
        returnValue = 1;
    }

    if (returnValue == 0 && !transactionActive)
    {
        LERROR << "Error: no transaction open.";
        returnValue = 1;
    }

    if (returnValue == 0)
    {
        if (typeType == 1)
        {
            // get collection type
            CollectionType* collType = static_cast<CollectionType*>(const_cast<SetType*>(TypeFactory::mapSetType(const_cast<char*>(typeName))));

            if (collType)
            {
                typeStructure = collType->getTypeStructure();    // no copy
            }
            else
            {
                returnValue = 2;
            }
        }
        else if (typeType == 2)
        {
            // get MDD type
            const MDDType* mddType = TypeFactory::mapMDDType(typeName);

            if (mddType)
            {
                typeStructure = mddType->getTypeStructure();    // no copy
            }
            else
            {
                returnValue = 2;
            }
        }
        else    // base type not implemented
        {
            returnValue = 2;
        }

        if (returnValue == 2)
        {
            LERROR << "Error: unknown type.";
        }
        else
        {
            LINFO << MSG_OK;
        }

        context->release();
    }

    return returnValue;
}


unsigned short
ServerComm::setTransferMode(unsigned long callingClientId,
                            unsigned short format,
                            const char* formatParams)
{
#ifdef DEBUG
    LINFO << "Request: 'set transfer mode', format = '" << format << "', params = '" << formatParams << "'...";
#endif

    unsigned short retval = 1;

    ClientTblElt* context = getClientContext(callingClientId);

    if (context != 0)
    {
        r_Data_Format fmt = static_cast<r_Data_Format>(format);
        if (context->transferFormatParams != NULL)
        {
            delete [] context->transferFormatParams;
            context->transferFormatParams = NULL;
            // revert the transfer format strictness
            context->exactFormat = 0;
        }
        if (formatParams != NULL)
        {
            context->transferFormatParams = new char[strlen(formatParams) + 1];
            strcpy(context->transferFormatParams, formatParams);
            // extract any occurrences of ``exactformat''
            context->clientParams->process(context->transferFormatParams);
        }
        context->transferFormat = fmt;

#ifdef DEBUG
        LINFO << MSG_OK;
#endif
        retval = 0;

        context->release();
        LTRACE << "setTransferMode(...) current transfer format :" << context->transferFormat;
        LTRACE << "setTransferMode(...)current transfer params :" << context->transferFormatParams;
    }
    else
    {
        LERROR << "Error: client not registered.";
        retval = 1;
    }

    return retval;
}

unsigned short
ServerComm::setStorageMode(unsigned long callingClientId,
                           __attribute__((unused)) unsigned short format,
                           const char* formatParams)
{
#ifdef DEBUG
    LINFO << "Request: 'set storage mode', format = " << format << ", params = " << formatParams << "...";
#endif

    unsigned short retval = 1;

    ClientTblElt* context = getClientContext(callingClientId);

    if (context != 0)
    {
        r_Data_Format fmt = r_Array;

        if (context->storageFormatParams != NULL)
        {
            delete [] context->storageFormatParams;
            context->storageFormatParams = NULL;
        }
        if (formatParams != NULL)
        {
            context->storageFormatParams = new char[strlen(formatParams) + 1];
            strcpy(context->storageFormatParams, formatParams);
        }
        context->storageFormat = fmt;

#ifdef DEBUG
        LINFO << MSG_OK;
#endif
        retval = 0;

        context->release();
        LTRACE << "setStorageMode(...) current storage format :" << context->storageFormat;
        LTRACE << "setStorageMode(...) current storage params :" << context->storageFormatParams;
    }
    else
    {
        LERROR << "Error: client not registered.";
        retval = 1;
    }

    return retval;
}

int
ServerComm::ensureTileFormat(__attribute__((unused)) r_Data_Format& hasFmt,
                             __attribute__((unused)) r_Data_Format needFmt,
                             __attribute__((unused)) const r_Minterval& dom,
                             __attribute__((unused)) const BaseType* type,
                             __attribute__((unused)) char*& data,
                             __attribute__((unused)) r_Bytes& size,
                             __attribute__((unused)) int repack,
                             __attribute__((unused)) int owner,
                             __attribute__((unused)) const char* params)
{
    int status = ENSURE_TILE_FORMAT_OK;

    LTRACE << "ensureTileFormat(...) #Size 1=" << size;
    LTRACE << "ensureTileFormat(...) #Size 3=" << size;

    return status;
}
