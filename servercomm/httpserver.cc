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
 * SOURCE: httpserver.cc
 *
 * MODULE: httpserver
 * CLASS:  HttpServer
 *
 * COMMENTS:
 *      No Comments
*/

#include "config.h"
#include "mymalloc/mymalloc.h"

static const char rcsid[] = "@(#)servercomm, HttpServer: $Id: httpserver.cc,v 1.54 2005/09/03 21:05:14 rasdev Exp $";

#include <iostream>
#include <time.h>      // for time()
#include <string.h>

#include <signal.h>    // for sigaction()
#include <unistd.h>    // for alarm(), gethostname()
#include <iomanip>

#include "raslib/error.hh"
#include "raslib/minterval.hh"
#include "raslib/endian.hh"
#include "raslib/basetype.hh"

#include "servercomm/httpserver.hh"
#include "qlparser/qtnode.hh"
#include "qlparser/qtdata.hh"
#include "catalogmgr/typefactory.hh"

#include "mddmgr/mddcoll.hh"
#include "tilemgr/tile.hh"

#include "relcatalogif/mddtype.hh"
#include "relcatalogif/mdddomaintype.hh"
#include "relcatalogif/settype.hh"

#ifdef PURIFY
#include <purify.h>
#endif

#include "httpserver/httpserver.h"

#include "qlparser/qtmdd.hh"
#include "qlparser/qtatomicdata.hh"
#include "qlparser/qtcomplexdata.hh"
#include "qlparser/qtintervaldata.hh"
#include "qlparser/qtmintervaldata.hh"
#include "qlparser/qtpointdata.hh"
#include "qlparser/qtstringdata.hh"

#include <logging.hh>

#define UNEXPECTED_INTERNAL_ERROR 10000

// ok message for log output; should go into a central file
#define MSG_OK            "ok"

// include and extern declarations for the query parsing
#include "qlparser/querytree.hh"
extern int           yyparse();
extern void          yyreset();
extern QueryTree*    parseQueryTree;
extern ParseInfo*    parseError;
extern char*         beginParseString;
extern char*         iterParseString;
extern unsigned long maxTransferBufferSize;
extern char*         dbSchema;
extern MDDColl*      mddConstants;

// this is a temporary thing
extern int      globalOptimizationLevel;

#ifdef RMANBENCHMARK
#if BASEDB == 1
static o2_ClientStat newO2Stats;
static o2_ClientStat oldO2Stats;
#endif
#endif

// defined in servercomm2.cc
extern char globalHTTPSetTypeStructure[];

// This currently represents the one and only client active at one time.
static HttpServer::ClientTblElt globalClientContext(ServerComm::HTTPCLIENT, 1);

// At the beginning, no servercomm object exists.
HttpServer* HttpServer::actual_httpserver = 0;

// This is a local inline function to enable encoding of longs into chars
static inline void encodeLong(char* res, const r_Long* val)
{
    *res = *(char*)const_cast<r_Long*>(val);
    *(res + 1) = *((char*)const_cast<r_Long*>(val) + 1);
    *(res + 2) = *((char*)const_cast<r_Long*>(val) + 2);
    *(res + 3) = *((char*)const_cast<r_Long*>(val) + 3);
}

static inline void encodeULong(char* res, const r_ULong* val)
{
    *res = *(char*)const_cast<r_ULong*>(val);
    *(res + 1) = *((char*)const_cast<r_ULong*>(val) + 1);
    *(res + 2) = *((char*)const_cast<r_ULong*>(val) + 2);
    *(res + 3) = *((char*)const_cast<r_ULong*>(val) + 3);
}

// This function takes a binary data block and returns a vector of mddTransferEncodings
void
getMDDs(int binDataSize, char* binData, int Endianess, vector<HttpServer::MDDEncoding*>& resultVector)
{
    char* currentPos;
    char* stringBuffer;
    char* binDataBuffer = NULL;
    r_Long intValue;
    HttpServer::MDDEncoding* currentMDD;
    r_Endian::r_Endianness  myEndianess = r_Endian::get_endianness();

    //LINFO << "Starting getMDDs , binDataSize is " << binDataSize;

    stringBuffer = static_cast<char*>(mymalloc(1024));
    currentPos = binData;
    // Examine the whole array...
    while (currentPos < (binData + binDataSize))
    {
        // Create new MDDEncoding object
        currentMDD = new HttpServer::MDDEncoding();
        // get objectType
        memcpy(&intValue, currentPos, sizeof(r_Long));
        // intValue = *(int *)intValue;
        if (myEndianess != Endianess)
        {
            intValue = r_Endian::swap(intValue);
        }
        currentMDD->setObjectType(intValue);
        currentPos += 4;
        //LINFO << "object type is " << intValue;

        // get objectTypeName
        strcpy(stringBuffer, currentPos);
        if (strcmp(stringBuffer, "null") != 0)
        {
            currentMDD->setObjectTypeName(strdup(stringBuffer));
        }
        currentPos += strlen(stringBuffer) + 1;
        //LINFO << "objectTypeName is " << stringBuffer;

        // get typeStructure
        strcpy(stringBuffer, currentPos);
        if (strcmp(stringBuffer, "null") != 0)
        {
            currentMDD->setTypeStructure(strdup(stringBuffer));
        }
        currentPos += strlen(stringBuffer) + 1;
        //LINFO << "typeStructure is " << stringBuffer;

        // get typeLength
        memcpy(&intValue, currentPos, sizeof(r_Long));
        //intValue = *(int *)currentPos;
        if (myEndianess != Endianess)
        {
            intValue = r_Endian::swap(intValue);
        }
        currentMDD->setTypeLength(intValue);
        currentPos += sizeof(r_Long);
        //LINFO << "type length is " << intValue;

        // get domain
        strcpy(stringBuffer, currentPos);
        if (strcmp(stringBuffer, "null") != 0)
        {
            currentMDD->setDomain(strdup(stringBuffer));
        }
        currentPos += strlen(stringBuffer) + 1;
        //LINFO << "domain is " << stringBuffer;

        // get tileSize - later this will be the storage_layout
        strcpy(stringBuffer, currentPos);
        if (strcmp(stringBuffer, "null") != 0)
        {
            currentMDD->setTileSize(strdup(stringBuffer));
        }
        currentPos += strlen(stringBuffer) + 1;
        //LINFO << "tileSize is " << stringBuffer;
        // get oid
        strcpy(stringBuffer, currentPos);
        if (strcmp(stringBuffer, "null") != 0)
        {
            currentMDD->setOID(strdup(stringBuffer));
        }
        currentPos += strlen(stringBuffer) + 1;
        //LINFO << "OID is " << stringBuffer;
        // get dataSize
        /* LINFO << "Byte1: " << (short)*currentPos;
         LINFO << "Byte2: " << (short)*(currentPos+1);
         LINFO << "Byte3: " << (short)*(currentPos+2);
         LINFO << "Byte4: " << (short)*(currentPos+3);*/
        memcpy(&intValue, currentPos, sizeof(r_Long));
        //intValue = *(int *)currentPos;
        if (myEndianess != Endianess)
        {
            intValue = r_Endian::swap(intValue);
        }
        currentMDD->setDataSize(intValue);
        currentPos += sizeof(r_Long);
        //LINFO << "Data size is " << intValue;

        // get binData
        binDataBuffer = static_cast<char*>(mymalloc(static_cast<size_t>(intValue) + 1));
        memcpy(binDataBuffer, currentPos, static_cast<size_t>(intValue));
        currentMDD->setBinData(binDataBuffer);
        currentPos += intValue;

        // Put object into result vector
        resultVector.insert(resultVector.begin(), currentMDD);
    }
    // free Buffer
    free(stringBuffer);

    //LINFO << "vector has " << resultVector.size() << " entries.";

}


/**********************************************************************
 *  Class MDDEncoding
 *
 *  IMPORTANT: This class does not copy its parameters for performance reasons!
 *             So the memory allocated for the parameters must not be freed in
 *             the calling functions or methods, this is done by the destructor
 *             of this class!
 *
 *********************************************************************/
HttpServer::MDDEncoding::MDDEncoding()
{
    objectType = 0;
    objectTypeName = NULL;
    typeStructure = NULL;
    typeLength = 0;
    domain = NULL;
    tileSize = NULL;
    oidString = NULL;
    dataSize = 0;
    binData = NULL;
    stringRepresentation = static_cast<char*>(mymalloc(4096));
}

HttpServer::MDDEncoding::~MDDEncoding()
{
    if (objectTypeName != NULL)
    {
        free(objectTypeName);
    }
    if (typeStructure != NULL)
    {
        free(typeStructure);
    }
    if (domain != NULL)
    {
        free(domain);
    }
    if (tileSize != NULL)
    {
        free(tileSize);
    }
    if (oidString != NULL)
    {
        free(oidString);
    }
    // binData is freed elsewhere!
    free(stringRepresentation);
}

void HttpServer::MDDEncoding::setObjectType(int type)
{
    objectType = type;
}

void HttpServer::MDDEncoding::setObjectTypeName(char* name)
{
    if (objectTypeName)
    {
        free(objectTypeName);
    }
    objectTypeName = name;
}

void HttpServer::MDDEncoding::setTypeStructure(char* type)
{
    if (typeStructure)
    {
        free(typeStructure);
    }
    typeStructure = type;
}

void HttpServer::MDDEncoding::setTypeLength(int len)
{
    typeLength = len;
}

void HttpServer::MDDEncoding::setDomain(char* dom)
{
    if (domain)
    {
        free(domain);
    }
    domain = dom;
}

void HttpServer::MDDEncoding::setTileSize(char* size)
{
    if (tileSize)
    {
        free(tileSize);
    }
    tileSize = size;
}

void HttpServer::MDDEncoding::setOID(char* o)
{
    if (oidString)
    {
        free(oidString);
    }
    oidString = o;
}

void HttpServer::MDDEncoding::setDataSize(int size)
{
    dataSize = size;
}

void HttpServer::MDDEncoding::setBinData(char* data)
{
    if (binData)
    {
        free(binData);
    }
    binData = data;
}

const char* HttpServer::MDDEncoding::toString()
{
    char* intBuffer = static_cast<char*>(mymalloc(128));

    strcpy(stringRepresentation, "\n\n MDD information: \n     ObjectTypeName: ");
    strcat(stringRepresentation, objectTypeName);
    strcat(stringRepresentation, "\n     ObjectType: ");
    sprintf(intBuffer, "%d", objectType);
    strcat(stringRepresentation, intBuffer);
    strcat(stringRepresentation, "\n     OID: ");
    strcat(stringRepresentation, oidString);
    strcat(stringRepresentation, "\n     Domain: ");
    strcat(stringRepresentation, domain);
    strcat(stringRepresentation, "\n     TypeStructure: ");
    strcat(stringRepresentation, typeStructure);
    strcat(stringRepresentation, "\n     tileSize: ");
    strcat(stringRepresentation, tileSize);
    strcat(stringRepresentation, "\n     DataSize: ");
    sprintf(intBuffer, "%d", dataSize);
    strcat(stringRepresentation, intBuffer);

    free(intBuffer);
    return stringRepresentation;
}


/*************************************************************************
 * Method name...: HttpServer()   (constructor)
 ************************************************************************/
HttpServer::HttpServer()
{
    if (actual_httpserver)
    {
        std::cerr << "Internal Error: Tried to instantiate more than one HttpServer object." << endl;
        exit(1);
    }

    // So that it should be never freed
    globalClientContext.currentUsers++;
    actual_httpserver = this;
    flagInformRasMgr = false;
    isHttpServer = true;
}

HttpServer::HttpServer(unsigned long timeOut, unsigned long managementInterval , unsigned long newListenPort, char* newRasmgrHost, unsigned int newRasmgrPort, char* newServerName)
    : ServerComm(timeOut, managementInterval, newListenPort, newRasmgrHost, newRasmgrPort, newServerName)
{
    if (actual_httpserver)
    {
        std::cerr << "Internal Error: Tried to instantiate more than one HTTPServer object." << endl;
        exit(1);
    }

    actual_httpserver = this;

    flagInformRasMgr = false;

    // So that it should be never freed
    globalClientContext.currentUsers++;

    isHttpServer = true;
}

/*************************************************************************
 * Method name...: ~HttpServer()    (destructor)
 ************************************************************************/
HttpServer::~HttpServer()
{
    // delete communication object
    if (admin)
    {
        delete admin;
    }

    actual_httpserver = 0;
}



/*************************************************************************
 * Method name...: startHttpServer()
 ************************************************************************/
void termSignalHandler(int sig);


void
HttpServer::startRpcServer()

{
    // create administraion object (O2 session is initialized)
    admin = AdminIf::instance();
    if (!admin)
    {
        throw r_Error(r_Error::r_Error_BaseDBMSFailed);
    }

    // simulating command line arguments
    const char* dummy[] = { "rasserver", "-c", "httpserver.conf" };

    struct sigaction termSignal;
    memset(&termSignal,0,sizeof(termSignal));
    termSignal.sa_handler = termSignalHandler;
    sigaction(SIGALRM, &termSignal, NULL);
    cout << "RasDaMan server " << serverName << " is up." << endl;
    LINFO << "RasDaMan server " << serverName << " is up.";

    doIt_httpserver(3, const_cast<char**>(dummy));

}

void termSignalHandler(__attribute__((unused)) int sig)
{
    static int in_progress = 0;

    if (in_progress)
    {
        return;
    }

    in_progress = 1;

    if (HttpServer::actual_httpserver)
    {
        HttpServer::actual_httpserver->stopRpcServer();
    }

}

void clearLastClient(); // is down at end of file


void
HttpServer::stopRpcServer()
{
    LINFO << " Shutdown request received.";
    // LINFO << "Unregistering interface...";
    // has to be adapted for HTTP server
    // svc_unregister( RPCIF, RPCIFVERS );
    // LINFO << "ok";

    LINFO << "Clearing clients...";

    clearLastClient();

    LINFO << "informing rasmgr...";
    informRasMGR(SERVER_DOWN);
    cout   << "rasdaman server " << serverName << " is down." << endl;

    exit(0);
    //  svc_exit();
}

HttpServer::ClientTblElt*
HttpServer::getClientContext(__attribute__((unused)) unsigned long clientId)
{
    // this is a simplification and only works for one client
    globalClientContext.currentUsers++;
    return &globalClientContext;
}

/*************************************************************************
 * Method name...: printServerStatus( )
 ************************************************************************/
void
HttpServer::printServerStatus(ostream& s)
{
    unsigned long currentTime = static_cast<unsigned long>(time(NULL));

    s << endl;
    s << "HTTP Server state information at " << endl; // << ctime((time_t*)&currentTime) << endl;
    s << "  Transaction active.............: " << (transactionActive ? "yes" : "no") << endl;
    s << "  Max. transfer buffer size......: " << maxTransferBufferSize << " bytes" << endl;
    s << endl;

    // Purify?
    /*
    s << "memory map----------------------------------------------------" << endl;

    // memorymap(1);

    struct mallinfo meminfo = mallinfo();

    s << "space in arena                 : " << meminfo.arena << endl;
    s << "number of small blocks         : " << meminfo.smblks << endl;
    s << "number of ordinary blocks      : " << meminfo.ordblks << endl;
    s << "space in free ordinary blocks  : " << meminfo.fordblks << endl;
    s << "space in used ordinary blocks  : " << meminfo.uordblks << endl;

    s << "additional space from last call: " << meminfo.uordblks - memUsed << endl;

    memUsed = meminfo.uordblks;

    s << "end memory map------------------------------------------------" << endl << endl;
    */
}

//For future, to make this shit nicer

#ifdef IS_LITTLE_ENDIAN
const int systemEndianess = 1;
#else
const int systemEndianess = 0;
#endif

int encodeAckn(char*& result, int ackCode = 99) // 99 is the 'OK', but we have other ack to
{
    result = static_cast<char*>(mymalloc(1));
    *result = ackCode;
    return 1;
}

void cleanExecuteQueryRes(ExecuteQueryRes& res)
{
    if (res.typeStructure)
    {
        free(res.typeStructure);
        res.typeStructure = NULL;
    }
    if (res.token)
    {
        free(res.token);
        res.token = NULL;
    }
    if (res.typeName)
    {
        free(res.typeName);
        res.typeName = NULL;
    }
}

int encodeError(char*& result, const r_ULong  errorNo, const r_ULong lineNo, const r_ULong columnNo, const char* text)
{
    int totalLength = 14 + strlen(text) + 1;
    result = static_cast<char*>(mymalloc(static_cast<size_t>(totalLength)));
    char* currentPos = result;
    // fill it with data
    *currentPos = 0; // result is error
    currentPos++;
    *currentPos = systemEndianess;
    currentPos++;
    encodeULong(currentPos, &errorNo);
    currentPos += sizeof(r_ULong);
    encodeULong(currentPos, &lineNo);
    currentPos += sizeof(r_ULong);
    encodeULong(currentPos, &columnNo);
    currentPos += sizeof(r_ULong);
    strcpy(currentPos, text);

    return totalLength;
}

//a dirty hack:
static long lastCallingClientId = -1;
// this is needed to get a died client lost. Who designed this should do it better


long
HttpServer::processRequest(unsigned long callingClientId, char* baseName, int rascommand,
                           char* query, int binDataSize, char* binData, int Endianess, char*& result, char* capability)
{

    lastCallingClientId = static_cast<long>(callingClientId);

    //LINFO << "Start Method Processrequest ... ";
    try
    {
        // put to catch all errors which come up here, so HTTP-Server doesn't crush because of them

        long returnValue = 0;
        unsigned short execResult = 0;
        ExecuteQueryRes resultError;
        // needed for result type MDD collection
        r_Minterval resultDom;
        char* typeName = NULL;
        char* typeStructure = NULL;
        char* currentPos = NULL;
        r_OId oid;
        bool valid;
        unsigned short currentFormat;
        // vector with mdds in transfer encoding
        vector<HttpServer::MDDEncoding*> transferredMDDs;
        Tile* resultTile = NULL; // temporary tile with the whole MDD
        unsigned int i;
        unsigned short objType;
        r_OId* roid = NULL;
        int totalLength;
        long l;
        ClientTblElt* context = NULL;
        // server endianess
        r_Endian::r_Endianness serverEndian;
        if (capability)
        {
            long resultCode;
            resultCode = accessControl.crunchCapability(capability);

            if (resultCode)
            {
                flagInformRasMgr = true; // Used in doIt_http...

                return encodeError(result, resultCode, 0, 0, "");
            }
        }


        switch (rascommand)
        {
        case 1:
            // commOpenDB
            // call openDB (userName initialized with "")
            openDB(callingClientId, baseName, "");
            return encodeAckn(result);
            break;

        case 2:
            // commCloseDB
            // call closeDB
            flagInformRasMgr = true; // Used in doIt_http...
            closeDB(callingClientId);
            return encodeAckn(result);
            break;

        case 3:
            // commBTreadOnly
            // call beginTA
            beginTA(callingClientId, 1);
            return encodeAckn(result);
            break;

        case 4:
            // commBTreadWrite
            // call beginTA
            beginTA(callingClientId, 0);
            return encodeAckn(result);
            break;

        case 5:
            // commCT
            // call commitTA
            commitTA(callingClientId);
            return encodeAckn(result);
            break;

        case 6:
            // commAT
            // call abortTA
            abortTA(callingClientId);
            return encodeAckn(result);
            break;

        case 7:
            // commIsOpenTA
            return encodeAckn(result, (transactionActive == callingClientId) ? 99 : 98);
            break;

        case 8:
            // commQueryExec
            // call executeQuery (result contains error information)
            resultError.token = NULL;
            resultError.typeName = NULL;
            resultError.typeStructure = NULL;
            execResult = executeQuery(callingClientId, query, resultError);
            // now we distinguish between different result types
            if (execResult == 0)
            {
                // MDD results
                totalLength = 6; // total length of result in bytes
                r_Long numMDD = 0;     // number of MDD objects in the result
                vector<Tile*> resultTiles; // contains all TransTiles representing the resulting MDDs
                resultTiles.reserve(20);
                vector<char*> resultTypes;
                resultTypes.reserve(20);
                vector<char*> resultDomains;
                resultDomains.reserve(20);
                vector<r_OId> resultOIDs;
                resultOIDs.reserve(20);
                // Here should be something like the following. Unfortunately,
                // context->transferColl seems to be 0 here. I don't know why.
                // Thats why a global variable was introduced in
                // servercomm2.cc.
                // CollectionType* collectionType =
                //   (CollectionType*) context->transferColl->getMDDCollType();
                // char* collTypeStructure = collectionType->getTypeStructure();

                while (getNextMDD(callingClientId, resultDom, typeName, typeStructure, oid,
                                  currentFormat) == 0)
                {
                    numMDD++;
                    // create TransTile for whole data from the tiles stored in context->transTiles
                    context = getClientContext(callingClientId);
                    // that should be enough, just transfer the whole thing ;-)
                    resultTile = new Tile(context->transTiles);

                    // server endianess
                    serverEndian = r_Endian::get_endianness();

                    // This currently represents the one and only client active at one time.
                    // stupid!!! if((context->clientId == 1) && (strcmp(context->clientIdText, ServerComm::HTTPCLIENT) == 0) && (serverEndian != r_Endian::r_Endian_Big))
                    if (serverEndian != r_Endian::r_Endian_Big)
                    {
                        // LINFO << "Changing endianness...";
                        // calling client is a http-client(java -> always BigEndian) and server has LittleEndian
                        char* tpstruct;
                        r_Base_Type* useType;
                        tpstruct = resultTile->getType()->getTypeStructure();
                        useType = static_cast<r_Base_Type*>(r_Type::get_any_type(tpstruct));

                        char* tempT = static_cast<char*>(mymalloc(sizeof(char) * resultTile->getSize()));

                        // change the endianness of the entire tile for identical domains for src and dest
                        r_Endian::swap_array(useType, resultDom, resultDom, resultTile->getContents(), tempT);
                        // deallocate old contents
                        char* oldCells = resultTile->getContents();
                        free(oldCells);
                        oldCells = NULL;
                        resultTile->setContents(tempT);

                        delete useType;
                        free(tpstruct);

                        // LINFO << MSG_OK;
                    }

                    resultTiles.push_back(resultTile);
                    resultTypes.push_back(typeStructure);
                    resultDomains.push_back(resultDom.get_string_representation());
                    resultOIDs.push_back(oid);

                    // this is normally done in getNextTile(). But this is not called, so we do it here.
                    (*(context->transferDataIter))++;
                    if (*(context->transferDataIter) != context->transferData->end())
                    {
                        // clean context->transtile if necessary
                        if (context->transTiles)
                        {
                            delete context->transTiles;
                            context->transTiles = 0;
                        }
                        // clean context->tileIter if necessary
                        if (context->tileIter)
                        {
                            delete context->tileIter;
                            context->tileIter = 0;
                        }
                    }

                    // clean typeName if necessary
                    if (typeName)
                    {
                        free(typeName);
                        typeName = 0;
                    }
                }

                // clean typeName if necessary
                if (typeName)
                {
                    free(typeName);
                    typeName = 0;
                }

                // prepare for transfer
                // calculate total length of the result array
                for (i = 0; i < static_cast<unsigned int>(numMDD); i++)
                {
                    totalLength += static_cast<int>(strlen(resultTypes[i])) + 1;
                    totalLength += static_cast<int>(strlen(resultDomains[i])) + 1;
                    // OID might be NULL
                    if (resultOIDs[i].get_string_representation() != NULL)
                    {
                        totalLength += static_cast<int>(strlen(resultOIDs[i].get_string_representation())) + 1;
                    }
                    else
                    {
                        totalLength++;
                    }
                    totalLength += static_cast<int>(resultTiles[i]->getSize());
                    totalLength += 4;
                }
                // for the type of the collection
                totalLength += static_cast<int>(strlen(globalHTTPSetTypeStructure)) + 1;

                // allocate the result
                result = static_cast<char*>(mymalloc(static_cast<size_t>(totalLength)));
                currentPos = result;
                // fill it with data
                *currentPos = 1; // result is MDD collection
                currentPos++;
                *currentPos = systemEndianess;
                currentPos++;
                // type of the collection
                strcpy(currentPos, globalHTTPSetTypeStructure);
                currentPos += strlen(globalHTTPSetTypeStructure) + 1;
                encodeLong(currentPos, &numMDD);
                currentPos += sizeof(r_Long);
                // encode MDDs
                for (i = 0; i < static_cast<unsigned int>(numMDD); i++)
                {
                    r_Long dummy = static_cast<r_Long>(resultTiles[i]->getSize());
                    strcpy(currentPos, resultTypes[i]);
                    currentPos += strlen(resultTypes[i]) + 1;
                    strcpy(currentPos, resultDomains[i]);
                    currentPos += strlen(resultDomains[i]) + 1;
                    // OID might be NULL
                    if (resultOIDs[i].get_string_representation() != NULL)
                    {
                        strcpy(currentPos, resultOIDs[i].get_string_representation());
                        currentPos += strlen(resultOIDs[i].get_string_representation()) + 1;
                    }
                    else
                    {
                        *currentPos = '\0';
                        currentPos++;
                    }
                    encodeLong(currentPos, &dummy);
                    currentPos += sizeof(r_Long);
                    memcpy(currentPos, resultTiles[i]->getContents(), static_cast<size_t>(dummy));
                    currentPos += dummy;
                }

                // delete all the temporary storage
                for (i = 0; i < static_cast<unsigned int>(numMDD); i++)
                {
                    delete resultTiles[i];
                    free(resultTypes[i]);
                    free(resultDomains[i]);
                }

                returnValue = totalLength;

            }
            else if (execResult == 1)
            {
                // the result is a collection of scalars.
                totalLength = 6; // total length of result in bytes
                r_Long numElem = 0;     // number of MDD objects in the result
                vector<char*> resultElems; // contains all TransTiles representing the resulting MDDs
                resultElems.reserve(20);
                vector<unsigned int> resultLengths;
                resultLengths.reserve(20);

                // we have to get the type of the collection
                totalLength += static_cast<int>(strlen(resultError.typeStructure)) + 1;

                // then we have to get all elements in the collection
                unsigned short dummyRes;
                char* buffer;
                unsigned int bufferSize;
                // This will probably not work for empty collections. Only if getNextElement
                // returns 2 in this case. I really don't get it.
                unsigned short moreElems = 0;

                while (moreElems == 0)
                {
                    moreElems = getNextElement(callingClientId, buffer, bufferSize);

                    //LINFO << "More elems is " << moreElems;
                    numElem++;
                    resultElems.push_back(buffer);
                    resultLengths.push_back(bufferSize);
                    // length of data
                    totalLength += static_cast<int>(bufferSize);
                    // this will be length of type
                    totalLength += 1;
                    // size of each element
                    totalLength += static_cast<int>(sizeof(r_Long));
                }

                // allocate the result
                result = static_cast<char*>(mymalloc(static_cast<size_t>(totalLength)));
                currentPos = result;
                // fill it with data
                *currentPos = 2; // result is collection of other types
                currentPos++;
                *currentPos = systemEndianess;
                currentPos++;
                // type of the collection
                strcpy(currentPos, resultError.typeStructure);
                currentPos += strlen(resultError.typeStructure) + 1;
                // number of elements
                encodeLong(currentPos, &numElem);
                currentPos += sizeof(r_Long);

                // and finally copy them together
                for (i = 0; i < static_cast<unsigned int>(numElem); i++)
                {
                    // This should be the type of the element
                    *currentPos = '\0';
                    currentPos++;
                    // length in bytes of the element
                    r_ULong convDummy = resultLengths[i];
                    encodeULong(currentPos, &convDummy);
                    currentPos += sizeof(r_ULong);
                    // actual data
                    memcpy(currentPos, resultElems[i], resultLengths[i]);
                    currentPos += resultLengths[i];
                }

                // delete all the temporary storage
                for (i = 0; i < static_cast<unsigned int>(numElem); i++)
                {
                    free(resultElems[i]);
                }

                returnValue = totalLength;

            }
            else if (execResult == 2)
            {
                totalLength = 7; // total length of result in bytes
                // the result collection is empty. It is returned as an empty MDD collection.
                // allocate the result
                result = static_cast<char*>(mymalloc(static_cast<unsigned int>(totalLength)));
                currentPos = result;
                // fill it with data
                *currentPos = 1; // result is MDD collection
                currentPos++;
                *currentPos = systemEndianess;
                currentPos++;
                // here the type of the collection should be added, currently empty string!
                *currentPos = '\0';
                currentPos++;
                // now set the number of results to zero
                r_Long dummy = 0;
                encodeLong(currentPos, &dummy);

                returnValue = totalLength;

            }
            else if (execResult == 4 || execResult == 5)
            {
                // parse error or execution error
                returnValue = encodeError(result, resultError.errorNo, resultError.lineNo, resultError.columnNo, resultError.token);

            }
            else
            {
                // unknow error
                returnValue = -10;
            }

            cleanExecuteQueryRes(resultError);

            // insert error handling here
            return returnValue;
            break;

        case 9:
            // commUpdateExec
            // empty transfer structures (only to be sure, this case probably cannot occur :-)
            returnValue = 0;
            valid = false;
            while (!transferredMDDs.empty())
            {
                LINFO << "Freeing old transfer structures...";
                free(transferredMDDs.back()->binData);
                delete(transferredMDDs.back());
                transferredMDDs.pop_back();
                LINFO << MSG_OK;
            }

            // do we have an insert statement?
            // we need to keep this to be compatible with older clients
            if (binDataSize > 0)
            {
                // yes, it is an insert statement => analyze and prepare the MDDs
                // create an empty MDD-Set in the client context
                initExecuteUpdate(callingClientId);

                // Analyze the binData array (the uploaded mdds)
                getMDDs(binDataSize, binData, Endianess, transferredMDDs);

                // Store all MDDs
                //LINFO << "vector has " << transferredMDDs.size() << " entries.";
                returnValue = 0;
                while (!transferredMDDs.empty() && (returnValue == 0))
                {
                    //LINFO << "Element:" << transferredMDDs.back()->toString();
                    // insert MDD into MDD-Set of client context
                    r_Minterval tempStorage(transferredMDDs.back()->domain);

                    roid = new r_OId(transferredMDDs.back()->oidString);
                    // OID valid
                    valid = roid->is_valid();
                    if (valid)
                    {
                        // parse the query string to get the collection name
                        char* collection;
                        char* startPtr;
                        char* endPtr;
                        startPtr = endPtr = query;
                        collection = new char[ endPtr - startPtr + 1 ];
                        // one for insert, one for into and one for the collection name
                        for (i = 0; i < 3; i++)
                        {
                            // delete spaces, tabs  ...
                            while ((*endPtr == ' ' || *endPtr == '\t' || *endPtr == '\r' || *endPtr == '\n') && *endPtr != '\0')
                            {
                                endPtr++;
                            }
                            startPtr = endPtr;
                            // parse next word
                            while (*endPtr != ' ' && *endPtr != '\t' && *endPtr != '\r' && *endPtr != '\n' && *endPtr != '\0')
                            {
                                endPtr++;
                            }
                            if (endPtr - startPtr >= 1)
                            {
                                collection = new char[endPtr - startPtr + 1];
                                strncpy(collection, startPtr, static_cast<size_t>(endPtr - startPtr));
                                collection[endPtr - startPtr] = '\0';
                            }
                        }
                        //LINFO << "collection: "  << collection;

                        //LINFO << "OID is valid: ";
                        execResult = startInsertPersMDD(callingClientId, collection, tempStorage,
                                                        static_cast<unsigned long>(transferredMDDs.back()->typeLength),
                                                        transferredMDDs.back()->objectTypeName, *roid);
                    }
                    // OID not valid
                    else
                    {
                        //LWARNING << "OID is NOT valid";

                        execResult = startInsertTransMDD(callingClientId, tempStorage,
                                                         static_cast<unsigned long>(transferredMDDs.back()->typeLength),
                                                         transferredMDDs.back()->objectTypeName);
                    }

                    //clean roid, we don't needed anymore
                    if (roid)
                    {
                        delete roid;
                        roid = NULL;
                    }

                    if (execResult != 0)
                    {
                        // no or wrong mdd type - return error message
                        unsigned long errNo;
                        if (strlen(transferredMDDs.back()->objectTypeName) < 1)
                        {
                            errNo = 966;
                        }
                        else
                        {
                            switch (execResult)
                            {
                            case 2:
                                errNo = 965;
                                break;
                            case 3:
                                errNo = 959;
                                break;
                            case 4:
                                errNo = 952;
                                break;
                            case 5:
                                errNo = 957;
                                break;
                            default:
                                errNo = 350;
                            }
                            LERROR << "Error: while inserting MDD";
                        }

                        returnValue = encodeError(result, errNo, 0, 0, transferredMDDs.back()->objectTypeName);

                        //clean up
                        while (!transferredMDDs.empty())
                        {
                            LINFO << "Freeing old transfer structures...";
                            free(transferredMDDs.back()->binData);
                            delete(transferredMDDs.back());
                            transferredMDDs.pop_back();
                            LINFO << MSG_OK;
                        }



                        return returnValue;
                    }
                    else
                    {
                        // create RPCMarray data structure - formats are currently hardcoded (r_Array)
                        RPCMarray* rpcMarray = static_cast<RPCMarray*>(mymalloc(sizeof(RPCMarray)));
                        rpcMarray->domain         = strdup(transferredMDDs.back()->domain);
                        rpcMarray->cellTypeLength = static_cast<size_t>(transferredMDDs.back()->typeLength);
                        rpcMarray->currentFormat  = r_Array;
                        rpcMarray->storageFormat  = r_Array;
                        rpcMarray->data.confarray_len = static_cast<u_int>(transferredMDDs.back()->dataSize);
                        rpcMarray->data.confarray_val = transferredMDDs.back()->binData;

                        /*
                        LINFO << "Creating RPCMarray with domain " << rpcMarray->domain << ", size " <<
                          rpcMarray->data.confarray_len << ", typeLength " << rpcMarray->cellTypeLength << " ...";
                        */

                        // split tile if a tileSize (an MInterval) has been specified
                        r_Minterval* splitInterval = NULL;
                        if (transferredMDDs.back()->tileSize != NULL)
                        {
                            splitInterval = new r_Minterval(transferredMDDs.back()->tileSize);
                            LDEBUG << "Splitinterval is " << splitInterval;
                        }
                        // now insert the tile(s)
                        if (valid)
                        {
                            insertTileSplitted(callingClientId, 1, rpcMarray, splitInterval);
                        }
                        else
                        {
                            insertTileSplitted(callingClientId, 0, rpcMarray, splitInterval);
                        }

                        // free the stuff
                        free(rpcMarray->domain);
                        free(rpcMarray);
                        delete(transferredMDDs.back());
                        transferredMDDs.pop_back();
                        delete(splitInterval);
                    }
                }

                // end insertion into client structure
                if (valid)
                {
                    endInsertMDD(callingClientId, 1);
                }
                else
                {
                    endInsertMDD(callingClientId, 0);
                }
            }

            if (returnValue == 0)
            {
                //LINFO << "Executing query: " << query;
                // until now now error has occurred => execute the query
                ExecuteUpdateRes returnStructure;
                returnStructure.token = NULL;
                if (!valid)
                {
                    execResult = executeUpdate(callingClientId, static_cast<const char*>(query) , returnStructure);
                }
                // query was executed successfully
                if (execResult == 0)
                {
                    // allocate the result
                    result = static_cast<char*>(mymalloc(1));
                    currentPos = result;
                    *currentPos = 99; // result is an acknowledgement
                    returnValue = 1;
                }
                // parsing or execution error
                else if (execResult == 2 || execResult == 3)
                {
                    returnValue = encodeError(result, returnStructure.errorNo, returnStructure.lineNo, returnStructure.columnNo, returnStructure.token);
                }
                else
                {
                    // unknow error
                    returnValue = -10;
                }

                if (returnStructure.token)
                {
                    free(returnStructure.token);
                    returnStructure.token = NULL;
                }
            }
            return returnValue;
            break;
        case 10:
            // getnewoid
            accessControl.wantToWrite();
            objType = 1;
            roid = new r_OId();
            ServerComm::getNewOId(static_cast<unsigned long>(lastCallingClientId), objType, *roid);

            // prepare returnValue
            totalLength = 9; // total length of result in bytes
            totalLength += static_cast<int>(strlen(roid->get_system_name())) + 1;
            totalLength += static_cast<int>(strlen(roid->get_base_name())) + 1;
            // allocate the result
            result = static_cast<char*>(mymalloc(static_cast<size_t>(totalLength)));
            currentPos = result;
            // fill it with data
            *currentPos = 4; // result is a OID
            currentPos++;

            // system
            strcpy(currentPos, roid->get_system_name());
            currentPos += strlen(roid->get_system_name()) + 1;

            // base
            strcpy(currentPos, roid->get_base_name());
            currentPos += strlen(roid->get_base_name()) + 1;


            context = getClientContext(callingClientId);
            // server endianess
            serverEndian = r_Endian::get_endianness();
            // This currently represents the one and only client active at one time.
            if ((context->clientId == 1) &&
                    (strcmp(context->clientIdText, ServerComm::HTTPCLIENT) == 0) &&
                    (serverEndian != r_Endian::r_Endian_Big))
            {
                // calling client is a http-client(java -> always BigEndian) and server has LittleEndian
                double tmp = roid->get_local_oid();
                *(double*)currentPos = r_Endian::swap(tmp);
            }
            else
            {
                *(double*)currentPos = roid->get_local_oid();
            }
            //currentPos += 8;
            returnValue = totalLength;

            //clean up we don't need roid anymore
            if (roid)
            {
                delete roid;
                roid = NULL;
            }

            return returnValue;
            break;
        case 11:
            // commInsertExec (>= v9.1)
            // empty transfer structures (only to be sure, this case probably cannot occur :-)
            returnValue = 0;
            valid = false;
            while (!transferredMDDs.empty())
            {
                LINFO << "Freeing old transfer structures...";
                free(transferredMDDs.back()->binData);
                delete(transferredMDDs.back());
                transferredMDDs.pop_back();
                LINFO << MSG_OK;
            }

            // do we have an insert statement?
            // we need to keep this to be compatible with older clients
            if (binDataSize > 0)
            {
                // yes, it is an insert statement => analyze and prepare the MDDs
                // create an empty MDD-Set in the client context
                initExecuteUpdate(callingClientId);

                // Analyze the binData array (the uploaded mdds)
                getMDDs(binDataSize, binData, Endianess, transferredMDDs);

                // Store all MDDs
                //LINFO << "vector has " << transferredMDDs.size() << " entries.";
                returnValue = 0;
                while (!transferredMDDs.empty() && (returnValue == 0))
                {
                    //LINFO << "Element:" << transferredMDDs.back()->toString();
                    // insert MDD into MDD-Set of client context
                    r_Minterval tempStorage(transferredMDDs.back()->domain);

                    roid = new r_OId(transferredMDDs.back()->oidString);
                    // OID valid
                    valid = roid->is_valid();
                    if (valid)
                    {
                        // parse the query string to get the collection name
                        char* collection;
                        char* startPtr;
                        char* endPtr;
                        startPtr = endPtr = query;
                        collection = new char[ endPtr - startPtr + 1 ];
                        // one for insert, one for into and one for the collection name
                        for (i = 0; i < 3; i++)
                        {
                            // delete spaces, tabs  ...
                            while ((*endPtr == ' ' || *endPtr == '\t' || *endPtr == '\r' || *endPtr == '\n') && *endPtr != '\0')
                            {
                                endPtr++;
                            }
                            startPtr = endPtr;
                            // parse next word
                            while (*endPtr != ' ' && *endPtr != '\t' && *endPtr != '\r' && *endPtr != '\n' && *endPtr != '\0')
                            {
                                endPtr++;
                            }
                            if (endPtr - startPtr >= 1)
                            {
                                collection = new char[endPtr - startPtr + 1];
                                strncpy(collection, startPtr, static_cast<size_t>(endPtr - startPtr));
                                collection[endPtr - startPtr] = '\0';
                            }
                        }
                        //LINFO << "collection: "  << collection;

                        //LINFO << "OID is valid: ";
                        execResult = startInsertPersMDD(callingClientId, collection, tempStorage,
                                                        static_cast<unsigned long>(transferredMDDs.back()->typeLength),
                                                        transferredMDDs.back()->objectTypeName, *roid);
                    }
                    // OID not valid
                    else
                    {
                        //LERROR << "OID is NOT valid";

                        execResult = startInsertTransMDD(callingClientId, tempStorage,
                                                         static_cast<unsigned long>(transferredMDDs.back()->typeLength),
                                                         transferredMDDs.back()->objectTypeName);
                    }

                    //clean roid, we don't need it anymore
                    if (roid)
                    {
                        delete roid;
                        roid = NULL;
                    }

                    if (execResult != 0)
                    {
                        // no or wrong mdd type - return error message
                        unsigned long errNo;
                        if (strlen(transferredMDDs.back()->objectTypeName) < 1)
                        {
                            errNo = 966;
                        }
                        else
                        {
                            switch (execResult)
                            {
                            case 2:
                                errNo = 965;
                                break;
                            case 3:
                                errNo = 959;
                                break;
                            case 4:
                                errNo = 952;
                                break;
                            case 5:
                                errNo = 957;
                                break;
                            default:
                                errNo = 350;
                            }
                            LERROR << "Error: while inserting MDD";
                        }

                        returnValue = encodeError(result, errNo, 0, 0, transferredMDDs.back()->objectTypeName);

                        //clean up
                        while (!transferredMDDs.empty())
                        {
                            LINFO << "Freeing old transfer structures...";
                            free(transferredMDDs.back()->binData);
                            delete(transferredMDDs.back());
                            transferredMDDs.pop_back();
                            LINFO << MSG_OK;
                        }

                        return returnValue;
                    }
                    else
                    {
                        // create RPCMarray data structure - formats are currently hardcoded (r_Array)
                        RPCMarray* rpcMarray = static_cast<RPCMarray*>(mymalloc(sizeof(RPCMarray)));
                        rpcMarray->domain         = strdup(transferredMDDs.back()->domain);
                        rpcMarray->cellTypeLength = static_cast<unsigned long>(transferredMDDs.back()->typeLength);
                        rpcMarray->currentFormat  = r_Array;
                        rpcMarray->storageFormat  = r_Array;
                        rpcMarray->data.confarray_len = static_cast<u_int>(transferredMDDs.back()->dataSize);
                        rpcMarray->data.confarray_val = transferredMDDs.back()->binData;


                        LDEBUG << "Creating RPCMarray with domain " << rpcMarray->domain << ", size " <<
                               rpcMarray->data.confarray_len << ", typeLength " << rpcMarray->cellTypeLength << " ...";


                        // split tile if a tileSize (an MInterval) has been specified
                        r_Minterval* splitInterval = NULL;
                        if (transferredMDDs.back()->tileSize != NULL)
                        {
                            splitInterval = new r_Minterval(transferredMDDs.back()->tileSize);
                            LDEBUG << "Splitinterval is " << splitInterval;
                        }
                        // now insert the tile(s)
                        if (valid)
                        {
                            insertTileSplitted(callingClientId, 1, rpcMarray, splitInterval);
                        }
                        else
                        {
                            insertTileSplitted(callingClientId, 0, rpcMarray, splitInterval);
                        }

                        // free the stuff
                        free(rpcMarray->domain);
                        free(rpcMarray);
                        delete(transferredMDDs.back());
                        transferredMDDs.pop_back();
                        delete(splitInterval);
                    }
                }

                // end insertion into client structure
                if (valid)
                {
                    endInsertMDD(callingClientId, 1);
                }
                else
                {
                    endInsertMDD(callingClientId, 0);
                }
            }

            if (returnValue == 0)
            {
                //LINFO << "Executing query: " << query;
                // until now now error has occurred => execute the query
                resultError.token = NULL;
                resultError.typeName = NULL;
                resultError.typeStructure = NULL;
                if (!valid)
                {
                    execResult = executeInsert(callingClientId, static_cast<const char*>(query) , resultError);
                }
                // query was executed successfully
                // now we distinguish between different result types
                if (execResult == 0)
                {
                    // MDD results
                    totalLength = 6; // total length of result in bytes
                    r_Long numMDD = 0;     // number of MDD objects in the result
                    vector<Tile*> resultTiles; // contains all TransTiles representing the resulting MDDs
                    resultTiles.reserve(20);
                    vector<char*> resultTypes;
                    resultTypes.reserve(20);
                    vector<char*> resultDomains;
                    resultDomains.reserve(20);
                    vector<r_OId> resultOIDs;
                    resultOIDs.reserve(20);
                    // Here should be something like the following. Unfortunately,
                    // context->transferColl seems to be 0 here. I don't know why.
                    // Thats why a global variable was introduced in
                    // servercomm2.cc.
                    // CollectionType* collectionType =
                    //   (CollectionType*) context->transferColl->getMDDCollType();
                    // char* collTypeStructure = collectionType->getTypeStructure();

                    while (getNextMDD(callingClientId, resultDom, typeName, typeStructure, oid,
                                      currentFormat) == 0)
                    {
                        numMDD++;
                        // create TransTile for whole data from the tiles stored in context->transTiles
                        context = getClientContext(callingClientId);
                        // that should be enough, just transfer the whole thing ;-)
                        resultTile = new Tile(context->transTiles);

                        // server endianess
                        serverEndian = r_Endian::get_endianness();

                        // This currently represents the one and only client active at one time.
                        // stupid!!! if((context->clientId == 1) && (strcmp(context->clientIdText, ServerComm::HTTPCLIENT) == 0) && (serverEndian != r_Endian::r_Endian_Big))
                        if (serverEndian != r_Endian::r_Endian_Big)
                        {
                            // LINFO << "Changing endianness...";
                            // calling client is a http-client(java -> always BigEndian) and server has LittleEndian
                            char* tpstruct;
                            r_Base_Type* useType;
                            tpstruct = resultTile->getType()->getTypeStructure();
                            useType = static_cast<r_Base_Type*>(r_Type::get_any_type(tpstruct));

                            char* tempT = static_cast<char*>(mymalloc(sizeof(char) * resultTile->getSize()));

                            // change the endianness of the entire tile for identical domains for src and dest
                            r_Endian::swap_array(useType, resultDom, resultDom, resultTile->getContents(), tempT);
                            // deallocate old contents
                            char* oldCells = resultTile->getContents();
                            free(oldCells);
                            oldCells = NULL;
                            resultTile->setContents(tempT);

                            delete useType;
                            free(tpstruct);

                            // LINFO << MSG_OK;
                        }

                        resultTiles.push_back(resultTile);
                        resultTypes.push_back(typeStructure);
                        resultDomains.push_back(resultDom.get_string_representation());
                        resultOIDs.push_back(oid);

                        // this is normally done in getNextTile(). But this is not called, so we do it here.
                        (*(context->transferDataIter))++;
                        if (*(context->transferDataIter) != context->transferData->end())
                        {
                            // clean context->transtile if necessary
                            if (context->transTiles)
                            {
                                delete context->transTiles;
                                context->transTiles = 0;
                            }
                            // clean context->tileIter if necessary
                            if (context->tileIter)
                            {
                                delete context->tileIter;
                                context->tileIter = 0;
                            }
                        }

                        // clean typeName if necessary
                        if (typeName)
                        {
                            free(typeName);
                            typeName = 0;
                        }
                    }

                    // clean typeName if necessary
                    if (typeName)
                    {
                        free(typeName);
                        typeName = 0;
                    }

                    // prepare for transfer
                    // calculate total length of the result array
                    for (i = 0; i < static_cast<unsigned int>(numMDD); i++)
                    {
                        totalLength += static_cast<int>(strlen(resultTypes[i])) + 1;
                        totalLength += static_cast<int>(strlen(resultDomains[i])) + 1;
                        // OID might be NULL
                        if (resultOIDs[i].get_string_representation() != NULL)
                        {
                            totalLength += static_cast<int>(strlen(resultOIDs[i].get_string_representation())) + 1;
                        }
                        else
                        {
                            totalLength++;
                        }
                        totalLength += static_cast<int>(resultTiles[i]->getSize());
                        totalLength += 4;
                    }
                    // for the type of the collection
                    totalLength += static_cast<int>(strlen(globalHTTPSetTypeStructure)) + 1;

                    // allocate the result
                    result = static_cast<char*>(mymalloc(static_cast<size_t>(totalLength)));
                    currentPos = result;
                    // fill it with data
                    *currentPos = 1; // result is MDD collection
                    currentPos++;
                    *currentPos = systemEndianess;
                    currentPos++;
                    // type of the collection
                    strcpy(currentPos, globalHTTPSetTypeStructure);
                    currentPos += strlen(globalHTTPSetTypeStructure) + 1;
                    encodeLong(currentPos, &numMDD);
                    currentPos += sizeof(r_Long);
                    // encode MDDs
                    for (i = 0; i < static_cast<unsigned int>(numMDD); i++)
                    {
                        r_Long dummy = static_cast<r_Long>(resultTiles[i]->getSize());
                        strcpy(currentPos, resultTypes[i]);
                        currentPos += strlen(resultTypes[i]) + 1;
                        strcpy(currentPos, resultDomains[i]);
                        currentPos += strlen(resultDomains[i]) + 1;
                        // OID might be NULL
                        if (resultOIDs[i].get_string_representation() != NULL)
                        {
                            strcpy(currentPos, resultOIDs[i].get_string_representation());
                            currentPos += strlen(resultOIDs[i].get_string_representation()) + 1;
                        }
                        else
                        {
                            *currentPos = '\0';
                            currentPos++;
                        }
                        encodeLong(currentPos, &dummy);
                        currentPos += sizeof(r_Long);
                        memcpy(currentPos, resultTiles[i]->getContents(), static_cast<size_t>(dummy));
                        currentPos += dummy;
                    }

                    // delete all the temporary storage
                    for (i = 0; i < static_cast<unsigned int>(numMDD); i++)
                    {
                        delete resultTiles[i];
                        free(resultTypes[i]);
                        free(resultDomains[i]);
                    }

                    returnValue = totalLength;

                }
                else if (execResult == 1)
                {
                    // the result is a collection of scalars.
                    totalLength = 6; // total length of result in bytes
                    r_Long numElem = 0;     // number of MDD objects in the result
                    vector<char*> resultElems; // contains all TransTiles representing the resulting MDDs
                    resultElems.reserve(20);
                    vector<unsigned int> resultLengths;
                    resultLengths.reserve(20);

                    // we have to get the type of the collection
                    totalLength += static_cast<int>(strlen(resultError.typeStructure)) + 1;

                    // then we have to get all elements in the collection
                    unsigned short dummyRes;
                    char* buffer;
                    unsigned int bufferSize;
                    // This will probably not work for empty collections. Only if getNextElement
                    // returns 2 in this case. I really don't get it.
                    unsigned short moreElems = 0;

                    while (moreElems == 0)
                    {
                        moreElems = getNextElement(callingClientId, buffer, bufferSize);

                        //LINFO << "More elems is " << moreElems;
                        numElem++;
                        resultElems.push_back(buffer);
                        resultLengths.push_back(bufferSize);
                        // length of data
                        totalLength += static_cast<int>(bufferSize);
                        // this will be length of type
                        totalLength += 1;
                        // size of each element
                        totalLength += static_cast<int>(sizeof(r_Long));
                    }

                    // allocate the result
                    result = static_cast<char*>(mymalloc(static_cast<size_t>(totalLength)));
                    currentPos = result;
                    // fill it with data
                    *currentPos = 2; // result is collection of other types
                    currentPos++;
                    *currentPos = systemEndianess;
                    currentPos++;
                    // type of the collection
                    strcpy(currentPos, resultError.typeStructure);
                    currentPos += strlen(resultError.typeStructure) + 1;
                    // number of elements
                    encodeLong(currentPos, &numElem);
                    currentPos += sizeof(r_Long);

                    // and finally copy them together
                    for (i = 0; i < static_cast<unsigned int>(numElem); i++)
                    {
                        // This should be the type of the element
                        *currentPos = '\0';
                        currentPos++;
                        // length in bytes of the element
                        r_ULong convDummy = resultLengths[i];
                        encodeULong(currentPos, &convDummy);
                        currentPos += sizeof(r_ULong);
                        // actual data
                        memcpy(currentPos, resultElems[i], resultLengths[i]);
                        currentPos += resultLengths[i];
                    }

                    // delete all the temporary storage
                    for (i = 0; i < static_cast<unsigned int>(numElem); i++)
                    {
                        free(resultElems[i]);
                    }

                    returnValue = totalLength;

                }
                else if (execResult == 2)
                {
                    totalLength = 7; // total length of result in bytes
                    // the result collection is empty. It is returned as an empty MDD collection.
                    // allocate the result
                    result = static_cast<char*>(mymalloc(static_cast<size_t>(totalLength)));
                    currentPos = result;
                    // fill it with data
                    *currentPos = 1; // result is MDD collection
                    currentPos++;
                    *currentPos = systemEndianess;
                    currentPos++;
                    // here the type of the collection should be added, currently empty string!
                    *currentPos = '\0';
                    currentPos++;
                    // now set the number of results to zero
                    r_Long dummy = 0;
                    encodeLong(currentPos, &dummy);

                    returnValue = totalLength;

                }
                else if (execResult == 4 || execResult == 5)
                {
                    // parse error or execution error
                    returnValue = encodeError(result, resultError.errorNo, resultError.lineNo, resultError.columnNo, resultError.token);
                }
                else
                {
                    // unknow error
                    returnValue = -10;
                }

                cleanExecuteQueryRes(resultError);
            }
            return returnValue;
            break;

        default:
            return -10;
            break;
        }
    }
    catch (r_Error& e) // this shouldn't be here, but ...
    {
        return encodeError(result, e.get_errorno(), 0, 0, e.what());
    }
    catch (r_Eno_permission& e) // this shouldn't be here, but ...
    {
        return encodeError(result, e.get_errorno(), 0, 0, "");
    }
    catch (std::exception& e)
    {
        return encodeError(result, UNEXPECTED_INTERNAL_ERROR, 0, 0, e.what());
    }
    catch (...)
    {
        return encodeError(result, UNEXPECTED_INTERNAL_ERROR, 0, 0, "");
    }

}

//**********************************************************************

#include "raslib/error.hh"

#include "httpserver/defs.h"
#include "httpserver/protos.h"
#include "httpserver/types.h"
#include "httpserver/server.h"


extern struct ServerBase  Server;

extern struct Logging*  LogBase;

void Select(int socket); // wrap around to put timeout in it

int HttpServer::doIt_httpserver(int argc, char* argv[])
{
    pid_t ChildPId;          /* -> Server.ChildInfo   */

    LINFO << "Initialising parameters for HTTP server... ";
    Initialize(argc, argv, &Server);

    LINFO << "Initialising server socket for HTTP server... ";
    listen(Server.SockFD, 5);
    LINFO << "Waiting for client calls... ";

    informRasMGR(SERVER_AVAILABLE);

    for (;;)
    {
        Select(Server.SockFD);
        Accept(Server.SockFD, &Server.Client);
        strcpy(Server.Client.Host.IPAddrString, inet_ntoa(Server.Client.Socket.sin_addr));

        if (Server.Client.Host.IPAddrString == NULL)
        {
            strcpy(Server.Client.Host.IPAddrString, "0.0.0.0");
        }

        Server.Client.Host.IPAddress = inet_addr(Server.Client.Host.IPAddrString);
        Server.Client.Comm.ConnStatus      = CONN_UNDEFINED;
        InitHTTPMsg(&Server.Client.Response);
        InitReqInfo(&Server.Client.Request);
        LogMsg(LG_SERVER, LVL_INFO, "INFO:  ====== Connection from %s accepted...",
               Server.Client.Host.IPAddrString);

        HandleRequest(&Server.Client);
        LogMsg(LG_SERVER, LVL_INFO, "INFO:  ====== EOT. Disconnecting.");

        close(Server.Client.SockFD);

        if (flagInformRasMgr == true)
        {
            informRasMGR(SERVER_AVAILABLE);
            flagInformRasMgr = false;
        }

#ifdef PURIFY
        purify_printf("Request finished.");
        purify_new_leaks();
#endif

    }
    // otherwise Exit(OK) should have been called
    return -1;
}

extern int  noTimeOut;
void Select(int socket)
{
    static time_t lastEntry = 0; // last time we entered here

    fd_set read_fd_set, http_fd_set;

    FD_ZERO(&http_fd_set);
    FD_SET(socket, &http_fd_set);

    const int checkTimeOutInterval = 30;

    struct timeval timeout;
    timeout.tv_sec = checkTimeOutInterval;
    timeout.tv_usec = 0 ;

    lastEntry = time(NULL);
    while (1)
    {
        read_fd_set = http_fd_set;
        //cout<<"HTTP Server is waiting..."<<endl;
        timeout.tv_sec = checkTimeOutInterval;
        timeout.tv_usec = 0;

        int rasp = select(FD_SETSIZE, &read_fd_set, NULL, NULL, &timeout);
        //cout<<"rasp="<<rasp<<endl;
        if (rasp > 0)
        {
            break; // means client call
        }

        if (rasp <= 0)
        {
            if (accessControl.isClient() == false)
            {
                // regularly tell the rasmgr that we are available. There is a scenario for DoS-attack (or bug, or firewall-problem)
                // when a client allocates itself a server and never calls, so the server is not usable any more.
                // but we have to find a smarter way of doing this, we need rasmgr-rasserver communication!
                ServerComm::actual_servercomm->informRasMGR(SERVER_REGULARSIG);
            }

            //cout<<"Timeout...noTimeout="<<noTimeOut<<endl; // or a signal
            //unsigned long clientTimeout=(ServerComm::actual_servercomm)->clientTimeout;
            //cout<<"clientTimeout="<<clientTimeout<<"  have Client="<<accessControl.isClient()<<endl;

            if (!noTimeOut && lastCallingClientId != -1)
            {
                time_t now = time(NULL);
                unsigned long clientTimeOut = (ServerComm::actual_servercomm)->clientTimeout;
                if ((now - lastEntry) > static_cast<int>(clientTimeOut) && accessControl.isClient())
                {
                    LINFO << "Timeout: after " << clientTimeOut << ", freeing client " << lastCallingClientId << "...";
                    ServerComm* sc = ServerComm::actual_servercomm;
                    ServerComm::ClientTblElt* clnt = sc-> getClientContext(static_cast<unsigned long>(lastCallingClientId));
                    clnt->transaction.abort();
                    clnt->database.close();
                    sc->informRasMGR(SERVER_AVAILABLE);
                    // cout<<"Http server, client timeout, available again..."<<endl;

                    LINFO << MSG_OK;
                }
            }

        }
    }

}

// used at exit, but put here for some compiling reason
void clearLastClient()
{
    if (accessControl.isClient())
    {
        LINFO <<  "Freeing client " << lastCallingClientId << "...";
        ServerComm* sc = ServerComm::actual_servercomm;
        ServerComm::ClientTblElt* clnt = sc-> getClientContext(static_cast<unsigned long>(lastCallingClientId));
        clnt->transaction.abort();
        clnt->database.close();
        LINFO << MSG_OK;
    }
}

