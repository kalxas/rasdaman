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

#include "httpserver.hh"
#include "config.h"
#include "accesscontrol.hh"
#include "rasnetprotocol/rpcif.h"

#include "raslib/error.hh"
#include "raslib/minterval.hh"
#include "raslib/endian.hh"
#include "raslib/basetype.hh"

#include "catalogmgr/typefactory.hh"
#include "mymalloc/mymalloc.h"
#include "mddmgr/mddcoll.hh"
#include "qlparser/qtnode.hh"
#include "qlparser/qtdata.hh"
#include "qlparser/qtmdd.hh"
#include "qlparser/qtatomicdata.hh"
#include "qlparser/qtcomplexdata.hh"
#include "qlparser/qtintervaldata.hh"
#include "qlparser/qtmintervaldata.hh"
#include "qlparser/qtpointdata.hh"
#include "qlparser/qtstringdata.hh"
#include "qlparser/querytree.hh"
#include "relcatalogif/mddtype.hh"
#include "relcatalogif/mdddomaintype.hh"
#include "relcatalogif/settype.hh"
#include "tilemgr/tile.hh"

#include <logging.hh>

#include <iostream>
#include <time.h>      // for time()
#include <string.h>
#include <signal.h>    // for sigaction()
#include <unistd.h>    // for alarm(), gethostname()
#include <cstdint>
#include <iomanip>

#define UNEXPECTED_INTERNAL_ERROR 10000

// --------------------------------------------------------------------------------
//                          globals
// --------------------------------------------------------------------------------

//a dirty hack: needed to get a died client lost. Who designed this should do it better
static long lastCallingClientId = -1;

#ifdef IS_LITTLE_ENDIAN
const int systemEndianess = ServerComm::ENDIAN_LITTLE;
#else
const int systemEndianess = ServerComm::ENDIAN_BIG;
#endif

// ack code types
const int ackCodeOK = 99;
const int ackCodeNotOK = 98;

// defined in server/rasserver_main.cc, configurable as a rasserver parameter:
//       --transbuffer <nnnn>   (default: 4194304)
//              maximal size of the transfer buffer in bytes
extern unsigned long maxTransferBufferSize;
extern AccessControl accessControl;

const int HttpServer::commOpenDB           = 1;
const int HttpServer::commCloseDB          = 2;
const int HttpServer::commBeginTAreadOnly  = 3;
const int HttpServer::commBeginTAreadWrite = 4;
const int HttpServer::commCommitTA         = 5;
const int HttpServer::commAbortTA          = 6;
const int HttpServer::commIsOpenTA         = 7;
const int HttpServer::commQueryExec        = 8;
const int HttpServer::commUpdateQueryExec  = 9;
const int HttpServer::commGetNewOID        = 10;
const int HttpServer::commInsertQueryExec  = 11;

const long HttpServer::unknownError        = -10;


// --------------------------------------------------------------------------------

template <typename T>
T decodeNumber(char **input, int endianess)
{
    static const auto myEndianess = r_Endian::get_endianness();
    T ret = *reinterpret_cast<const T *>(*input);
    if (myEndianess != endianess)
        ret = r_Endian::swap(ret);
    
    *input += sizeof(T);
    return ret;
}

template <typename T>
void encodeNumber(char **output, T value)
{
#ifdef RASDEBUG
    if (sizeof(T) == 1)
        LTRACE << "encoding number: " << (int) value << ", bytes: " << sizeof(T);
    else
        LTRACE << "encoding number: " << value << ", bytes: " << sizeof(T);
#endif
    *reinterpret_cast<T *>(*output) = value;
    *output += sizeof(T);
}

/**
 * Return the string starting at beginning of *input and ending at the first '\0';
 * If check is specified and the decoded string matches it, nullptr is returned,
 * otherwise the extracted string.
 */
static char *decodeString(char **input, bool checkNull = true)
{
    static const char *nullParam = "null";
    static constexpr size_t MAX_PARAM_SIZE = 1024;
    static char stringBuffer[MAX_PARAM_SIZE];

    // prevent buffer overflow
    size_t len = strlen(*input);
    if (len + 1 > MAX_PARAM_SIZE)
    {
        LERROR << "Invalid request, expected parameter of max length " << MAX_PARAM_SIZE
               << ", got " << len << ".";
        throw r_Error(r_Error::r_Error_TransferFailed);
    }
    strcpy(stringBuffer, *input);
    *input += len + 1;

    if (!checkNull || strcmp(stringBuffer, nullParam) != 0)
        return strdup(stringBuffer);
    else
        return nullptr;
}

static void encodeString(char **dst, const char *src, const char *dstStart, size_t totalLength)
{
    size_t srcLen = strlen(src);
    assert(*dst > dstStart);
    auto bytesWritten = static_cast<size_t>(*dst - dstStart);
    if (bytesWritten + srcLen >= totalLength)
    {
        LERROR << "Internal error, actual response length (" << (bytesWritten + srcLen)
               << ") is greater than the estimated response length (" << totalLength << ").";
        throw r_Error(r_Error::r_Error_TransferFailed);
    }
#ifdef RASDEBUG
    LTRACE << "encoding string: '" << src << "', bytes: " << srcLen + 1;
#endif
    strcpy(*dst, src);
    *dst += srcLen + 1;
}

static void encodeBinary(char **dst, const char *src, size_t srcLen, const char *dstStart, size_t totalLength)
{
    assert(*dst > dstStart);
    auto bytesWritten = static_cast<size_t>(*dst - dstStart);
    if (bytesWritten + srcLen > totalLength)
    {
        LERROR << "Internal error, actual response length (" << (bytesWritten + srcLen)
               << ") is greater than the estimated response length (" << totalLength << ").";
        throw r_Error(r_Error::r_Error_TransferFailed);
    }
#ifdef RASDEBUG
    LTRACE << "encoding binary, bytes: " << srcLen;
#endif
    memcpy(*dst, src, srcLen);
    *dst += srcLen;
}

// This function takes a binary data block and returns a vector of MDDEncoding
//
// Order of parameters in binData:
//
// objectType, objectTypeName, typeStructure, typeLength, domain, tileSize, oid, dataSize, data
vector<HttpServer::MDDEncoding *>
HttpServer::getMDDs(int binDataSize, char *binData, int endianess)
{
    vector<HttpServer::MDDEncoding *> resultVector;
    char *currentPos = binData;
    while (currentPos < (binData + binDataSize))
    {
        auto currentMDD = std::unique_ptr<HttpServer::MDDEncoding>(new HttpServer::MDDEncoding());

        currentMDD->objectType = decodeNumber<r_Long>(&currentPos, endianess);
        currentMDD->objectTypeName = decodeString(&currentPos);
        currentMDD->typeStructure = decodeString(&currentPos);
        currentMDD->typeLength = static_cast<unsigned long>(decodeNumber<r_Long>(&currentPos, endianess));
        currentMDD->domain = decodeString(&currentPos);
        currentMDD->tileSize = decodeString(&currentPos);
        currentMDD->oidString = decodeString(&currentPos);

        // TODO: should be size_t, not r_Long == int
        currentMDD->dataSize = static_cast<unsigned long>(decodeNumber<r_Long>(&currentPos, endianess));
        size_t dataSize = static_cast<size_t>(currentMDD->dataSize);

        currentMDD->binData = static_cast<char *>(mymalloc(dataSize));
        memcpy(currentMDD->binData, currentPos, dataSize);
        currentPos += dataSize;

        // Put object into result vector
        resultVector.insert(resultVector.begin(), currentMDD.release());
    }
    return resultVector;
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

HttpServer::MDDEncoding::~MDDEncoding()
{
    if (objectTypeName != NULL)
        free(objectTypeName);
    if (typeStructure != NULL)
        free(typeStructure);
    if (domain != NULL)
        free(domain);
    if (tileSize != NULL)
        free(tileSize);
    if (oidString != NULL)
        free(oidString);
    // binData is freed elsewhere!
}


std::string HttpServer::MDDEncoding::toString() const
{
    std::stringstream ss;
    ss << "\n\nMDD information:\n\tObjectTypeName: " << objectTypeName
       << "\n\tObjectType: " << objectType << "\n\tOID: " <<  oidString
       << "\n\tDomain: " << domain << "\n\tTypeStructure: " << typeStructure
       << "\n\ttileSize: " << tileSize << "\n\tDataSize: " << dataSize;
    return ss.str();
}


/*************************************************************************
 *                             HttpServer
 ************************************************************************/

void
HttpServer::printServerStatus()
{
    LDEBUG << "HTTP Server state information\n"
           << "  Transaction active.............: " << (transactionActive ? "yes" : "no") << "\n"
           << "  Max. transfer buffer size......: " << maxTransferBufferSize << " bytes\n";
}

int HttpServer::encodeAckn(char *&result, int ackCode = ackCodeOK)
{
    result = static_cast<char *>(mymalloc(1));
    *result = ackCode;
    return 1;
}

bool isValidCommand(char* req);

bool isValidCommand(char* req)
{
    while (isalpha(req[0]))
        ++req;
    return req[0] == '=' || req[0] == '\0';
}

long HttpServer::processRequest(unsigned long callingClientId,
                                const char* httpParams, int httpParamsLen,
                                char*& resultBuffer)
{
    char* Database{};
    char* QueryString{};
    char* ClientID{};
    char* BinData{};
    char* Capability{};
    int   Command{};
    int   ClientType{};
    int   Endianess{};
    int   NumberOfQueryParams{};
    int   BinDataSize{};
    
    // -------------------------------------------------------------------------
    
    // copy request to local variable (as strtok below modifies the buffer)
    std::unique_ptr<char[]> inputPtr;
    inputPtr.reset(new char[httpParamsLen + 1]);
    char *input = inputPtr.get();
    memcpy(input, httpParams, static_cast<size_t>(httpParamsLen));
    input[httpParamsLen] = '\0';
    char *inputEnd = input + httpParamsLen;
    
    // Read the message body and check for the post parameters
    int parseResult{};
    char *buffer = strtok(input, "=");
    while (buffer)
    {
        if (strcmp(buffer, "Database") == 0)
        {
            Database = strdup(strtok(NULL, "&"));
            LDEBUG << "Parameter Database is " << Database;
            buffer = strtok(NULL, "=");
        }
        else if (strcmp(buffer, "QueryString") == 0)
        {
            char *tmpQueryString = strtok(NULL, "&");
            char *end = tmpQueryString + strlen(tmpQueryString) + 1;
            while (end && end < inputEnd && !isValidCommand(end))
            {
                // reset the & in the query that was set to \0 by strtok
                *(end - 1) = '&';
                end = strtok(NULL, "&");
            }
            QueryString = strdup(tmpQueryString);
            LDEBUG << "Parameter QueryString is " << QueryString;
            buffer = strtok(NULL, "=");
        }
        else if (strcmp(buffer, "Capability") == 0)
        {
            Capability = strdup(strtok(NULL, "&\0"));
            LDEBUG << "Parameter Capability is " << Capability;
            buffer = strtok(NULL, "=");
        }
        else if (strcmp(buffer, "ClientID") == 0)
        {
            ClientID = strdup(strtok(NULL, "&"));
            LDEBUG << "Parameter ClientID is " << ClientID;
            buffer = strtok(NULL, "=");
        }
        else if (strcmp(buffer, "Command") == 0)
        {
            Command = atoi(strtok(NULL, "&"));
            LDEBUG << "Parameter Command is " << Command;
            buffer = strtok(NULL, "=");
        }
        else if (strcmp(buffer, "Endianess") == 0)
        {
            Endianess = atoi(strtok(NULL, "&"));
            LDEBUG << "Parameter Endianess is " << Endianess;
            buffer = strtok(NULL, "=");
        }
        else if (strcmp(buffer, "NumberOfQueryParameters") == 0)
        {
            NumberOfQueryParams = atoi(strtok(NULL, "&"));
            LDEBUG << "Parameter NumberOfQueryParams is " << NumberOfQueryParams;
            buffer = strtok(NULL, "=");
        }
        else if (strcmp(buffer, "BinDataSize") == 0)
        {
            BinDataSize = atoi(strtok(NULL, "&"));
            LDEBUG << "Parameter BinDataSize is " << BinDataSize;
            buffer = strtok(NULL, "=");
        }
        else if (strcmp(buffer, "BinData") == 0)
        {
            // This parameter has to be the last one!
            BinData = new char[BinDataSize ];
            memcpy(BinData,
                   httpParams + (httpParamsLen - BinDataSize),
                   static_cast<unsigned int>(BinDataSize));
            //set Buffer to NULL => exit this while block
            buffer = NULL;
        }
        else if (strcmp(buffer, "ClientType") == 0)
        {
            buffer = strtok(NULL, "&");
            LDEBUG << "Parameter Type is " << buffer;
            if (strcmp(buffer, "BROWSER") == 0)
                ClientType = 1;
            else if (strcmp(buffer, "RASCLIENT") == 0)
                ClientType = 2;
            else
            {
                LDEBUG << "Error: Unknown Parameter: " << buffer;
                parseResult = 2;
            }
            buffer = strtok(NULL, "=");
        }
        else
        {
            parseResult = 1;
        }
    }
    
    // -------------------------------------------------------------------------

    long resultLen = 0;
    resultBuffer = NULL;
    if (parseResult == 0)
    {
        resultLen = processRequest(callingClientId, Database, Command,
                                   QueryString, BinDataSize, BinData, Endianess,
                                   resultBuffer, Capability);
    }
    else
    {
        LERROR << "Internal HTTP protocol mismatch.";
    }

    // free RequestInfo
    free(Database);
    free(QueryString);
    free(Capability);
    delete [] BinData;
    free(ClientID);

    return resultLen;
}

long
HttpServer::processRequest(unsigned long callingClientId, char *baseName, int rascommand,
                           char *query, int binDataSize, char *binData, int Endianess,
                           char *&result, char *capability)
{
    lastCallingClientId = static_cast<long>(callingClientId);

    //LINFO << "Start Method Processrequest ... ";
    try
    {
        // catch all errors which come up here, so HTTP-Server doesn't crush because of them
        long returnValue = 0;
        if (capability)
        {
            auto rc = static_cast<unsigned int>(accessControl.crunchCapability(capability));
            if (rc)
            {
                return encodeError(result, rc, 0, 0, "");
            }
        }

        switch (rascommand)
        {
        case commOpenDB:
        {
            LTRACE << "open db " << baseName << " (client id " << callingClientId << ")";
            openDB(callingClientId, baseName, "");
            return encodeAckn(result);
        }
        case commCloseDB:
        {
            LTRACE << "close db (client id " << callingClientId << ")";
            closeDB(callingClientId);
            return encodeAckn(result);
        }
        case commBeginTAreadOnly:
        {
            LTRACE << "begin ta read only (client id " << callingClientId << ")";
            beginTA(callingClientId, 1);
            return encodeAckn(result);
        }
        case commBeginTAreadWrite:
        {
            LTRACE << "begin ta read/write (client id " << callingClientId << ")";
            beginTA(callingClientId, 0);
            return encodeAckn(result);
        }
        case commCommitTA:
        {
            LTRACE << "commit ta (client id " << callingClientId << ")";
            commitTA(callingClientId);
            return encodeAckn(result);
        }
        case commAbortTA:
        {
            LTRACE << "abort ta (client id " << callingClientId << ")";
            abortTA(callingClientId);
            return encodeAckn(result);
        }
        case commIsOpenTA:
        {
            LTRACE << "is open ta (client id " << callingClientId << "): "
                   << (transactionActive == callingClientId ? "yes" : "no");
            return encodeAckn(result, (transactionActive == callingClientId) ? ackCodeOK : ackCodeNotOK);
        }
        case commQueryExec:
        {
            LTRACE << "execute query (client id " << callingClientId << "): " << query;
            // call executeQuery (result contains error information)
            ExecuteQueryRes resultError;
            unsigned short execResult = executeQuery(callingClientId, query, resultError);
            auto resultSize = encodeResult(execResult, callingClientId, result, resultError);

            auto context = getClientContext(callingClientId);
            if (context && resultSize > 0)
                context->totalTransferedSize = static_cast<unsigned long>(resultSize);
            
            endTransfer(callingClientId); // finalize the log stmt of executeQuery with the transfered size

            return resultSize;
        }
        case commUpdateQueryExec:
        {
            LTRACE << "execute update (client id " << callingClientId
                   << ", binDataSize " << binDataSize << "): " << query;
            unsigned short execResult = RESPONSE_ERROR;
            returnValue = 0;
            bool isPersistent = false;

            returnValue = insertIfNeeded(callingClientId, query, binDataSize, binData, Endianess, result, isPersistent);

            if (returnValue == 0)
            {
                //LINFO << "Executing query: " << query;
                // until now no error has occurred => execute the query
                ExecuteUpdateRes returnStructure;
                if (!isPersistent)
                    execResult = executeUpdate(callingClientId, query, returnStructure);
                
                switch (execResult)
                {
                case 0:
                    returnValue = encodeAckn(result); // ok
                    break;
                case 2:
                case 3:
                    returnValue = encodeError(result, returnStructure.errorNo, returnStructure.lineNo,
                                              returnStructure.columnNo, returnStructure.token);
                    break;
                default:
                    returnValue = unknownError;
                    break;
                }

                if (returnStructure.token)
                    free(returnStructure.token);
            }
            return returnValue;
        }
        case commGetNewOID:
        {
            LTRACE << "get new oid (client id " << callingClientId << ")";
            accessControl.wantToWrite();
            unsigned short objType = 1;
            auto roid = std::unique_ptr<r_OId>(new r_OId());
            ServerComm::getNewOId(static_cast<unsigned long>(lastCallingClientId), objType, *roid);

            // prepare returnValue
            size_t totalLength = 9; // total length of result in bytes
            totalLength += strlen(roid->get_system_name()) + 1;
            totalLength += strlen(roid->get_base_name()) + 1;
            // allocate the result
            result = static_cast<char *>(mymalloc(totalLength));
            char *currentPos = result;
            // fill it with data
            encodeNumber(&currentPos, static_cast<char>(RESPONSE_OID));
            // system
            encodeString(&currentPos, roid->get_system_name(), result, totalLength);
            // base
            encodeString(&currentPos, roid->get_base_name(), result, totalLength);

            {
                auto *context = getClientContext(callingClientId);
                auto oid = roid->get_local_oid_double();
                // swap oid if http client
                if (context->clientId == 1 && context->clientType == ClientType::Http && systemEndianess != ENDIAN_BIG)
                {
                    oid = r_Endian::swap(oid);
                }
                encodeNumber(&currentPos, oid);
            }

            return static_cast<long>(totalLength);
        }
        case commInsertQueryExec:
        {
            LTRACE << "execute insert (client id " << callingClientId
                   << ", binDataSize " << binDataSize << "): " << query;
            // commInsertExec (>= v9.1)
            unsigned short execResult = RESPONSE_ERROR;
            bool isPersistent = false;

            returnValue = insertIfNeeded(callingClientId, query, binDataSize, binData, Endianess, result, isPersistent);
            if (returnValue == 0)
            {
                ExecuteQueryRes resultError;
                resetExecuteQueryRes(resultError);
                if (!isPersistent)
                {
                    execResult = executeInsert(callingClientId, query, resultError);
                }
                returnValue = encodeResult(execResult, callingClientId, result, resultError);
            }
            return returnValue;
        }
        default:
            return unknownError;
        }
    }
    catch (r_Error &e) // this shouldn't be here, but ...
    {
        return encodeError(result, e.get_errorno(), 0, 0, e.what());
    }
    catch (std::exception &e)
    {
        return encodeError(result, UNEXPECTED_INTERNAL_ERROR, 0, 0, e.what());
    }
    catch (...)
    {
        return encodeError(result, UNEXPECTED_INTERNAL_ERROR, 0, 0, "");
    }
}

/**
 * Result is encoded as follows: header is same in all cases, element encoding
 * varies. All strings are '\0' terminated.
 *
     Header:

     +----------+------------------------+
     | bytes    | what                   |
     +----------+------------------------+
     | 1        | result type            |
     | 1        | endianess              |
     | 4 (int)  | number of elements     |
     | string   | collection type        |
     +----------+------------------------+
 */
long HttpServer::encodeResult(unsigned short execResult, unsigned long callingClientId,
                              char *&result, ExecuteQueryRes &resultError)
{
    long ret{};
    switch (execResult)
    {
    case EXEC_RESULT_MDDS:
        ret = encodeMDDs(callingClientId, result, resultError.typeStructure);
        break;
    case EXEC_RESULT_SCALARS:
        ret = encodeScalars(callingClientId, result, resultError.typeStructure);
        break;
    case EXEC_RESULT_EMPTY:
        ret = encodeEmpty(result);
        break;
    case EXEC_RESULT_PARSE_ERROR:
    case EXEC_RESULT_EXEC_ERROR:
        ret = encodeError(result, resultError.errorNo, resultError.lineNo,
                          resultError.columnNo, resultError.token);
        break;
    default:
        ret = unknownError;
        break;
    }
    cleanExecuteQueryRes(resultError);
    return ret;
}

/**
 * For each MDD encoding is as follows:
 *
     +----------+------------------------+
     | bytes    | what                   |
     +----------+------------------------+
     | string   | type                   |
     | string   | domain                 |
     | string   | oid                    |
     | 8 size_t | array size             |
     | array sz | array data             |
     +----------+------------------------+
 */
long HttpServer::encodeMDDs(unsigned long callingClientId, char *&result, const char *resultTypeStructure)
{
    // contains all TransTiles representing the resulting MDDs
    vector<std::unique_ptr<Tile>> resultTiles;
    resultTiles.reserve(20);
    vector<char *> resultTypes;
    resultTypes.reserve(20);
    vector<std::string> resultDomains;
    resultDomains.reserve(20);
    vector<r_OId> resultOIDs;
    resultOIDs.reserve(20);

    r_Minterval resultDom;
    char *typeName = NULL;
    char *typeStructure = NULL;
    r_OId oid;
    unsigned short currentFormat;
    r_Long numMDD{};

    // get all result MDDs and calculate total length of the result
    size_t totalLength{};
    while (getNextMDD(callingClientId, resultDom, typeName, typeStructure, oid, currentFormat) == 0)
    {
        numMDD++;
        // create TransTile for whole data from the tiles stored in context->transTiles
        auto *context = getClientContext(callingClientId);
        // that should be enough, just transfer the whole thing ;-)
        auto resultTile = std::unique_ptr<Tile>(new Tile(context->transTiles));
        swapArrayIfNeeded(resultTile, resultDom);
        resultTiles.push_back(std::move(resultTile));
        resultTypes.push_back(typeStructure);
        resultDomains.push_back(resultDom.to_string());
        resultOIDs.push_back(oid);
        releaseContext(context);

        totalLength += strlen(resultTypes.back()) + 1;  // array type
        totalLength += resultDomains.back().size() + 1; // array domain
        const auto *oidStr = oid.get_string_representation();
        totalLength += oidStr ? strlen(oidStr) + 1 : 1; // array oid
        totalLength += sizeof(std::uint64_t);           // array size
        totalLength += resultTiles.back()->getSize();   // array itself

        if (typeName)
        {
            free(typeName);
            typeName = NULL;
        }
    }
    totalLength += getHeaderSize(resultTypeStructure);
    if (typeName)
    {
        free(typeName);
        typeName = NULL;
    }

    // encode result

    result = static_cast<char *>(mymalloc(totalLength));
    char *currentPos = result;

    encodeHeader(&currentPos, RESPONSE_MDDS, systemEndianess, numMDD,
                 resultTypeStructure, result, totalLength);

    // encode MDDs
    for (size_t i = 0; i < static_cast<size_t>(numMDD); i++)
    {
        // array type
        encodeString(&currentPos, resultTypes[i], result, totalLength);
        // array domain
        encodeString(&currentPos, resultDomains[i].c_str(), result, totalLength);
        // array oid
        if (resultOIDs[i].get_string_representation() != NULL)
            encodeString(&currentPos, resultOIDs[i].get_string_representation(), result, totalLength);
        else
            encodeNumber(&currentPos, '\0');
        // array size
        const auto tileSize = static_cast<std::uint64_t>(resultTiles[i]->getSize());
        encodeNumber(&currentPos, tileSize);
        // array data
        encodeBinary(&currentPos, resultTiles[i]->getContents(), tileSize, result, totalLength);
    }

    // delete temporary storage
    // TODO: put in a unique_ptr
    for (size_t i = 0; i < static_cast<size_t>(numMDD); i++)
        free(resultTypes[i]);

    return static_cast<long>(totalLength);
}

/**
 * For each scalar encoding is as follows:
 *
     +----------+------------------------+
     | bytes    | what                   |
     +----------+------------------------+
     | 4 int    | element size in bytes  |
     | elem sz  | element data           |
     +----------+------------------------+
 */
long HttpServer::encodeScalars(unsigned long callingClientId, char *&result, const char *typeStructure)
{
    vector<char *> resultElems; // contains all TransTiles representing the resulting MDDs
    resultElems.reserve(20);
    vector<r_ULong> resultLengths;
    resultLengths.reserve(20);

    size_t totalLength{}; // total length of result in bytes

    r_Long numElem{};     // number of objects in the result
    // This will probably not work for empty collections. Only if getNextElement
    // returns 2 in this case. I really don't get it.
    unsigned short moreElems = 0;

    while (moreElems == 0)
    {
        char *buffer;
        unsigned int bufferSize;
        moreElems = getNextElement(callingClientId, buffer, bufferSize);

        //LINFO << "More elems is " << moreElems;
        numElem++;
        resultElems.push_back(buffer);
        resultLengths.push_back(bufferSize);
        // element type (empty)
        totalLength++;
        // size of each element
        totalLength += sizeof(decltype(numElem));
        // length of data
        totalLength += bufferSize;
    }
    totalLength += getHeaderSize(typeStructure);

    // encode result

    result = static_cast<char *>(mymalloc(totalLength));
    char *currentPos = result;

    encodeHeader(&currentPos, RESPONSE_SCALARS, systemEndianess, numElem,
                 typeStructure, result, totalLength);

    // encode each element
    for (size_t i = 0; i < static_cast<unsigned int>(numElem); i++)
    {
        // This should be the type of the element
        encodeNumber(&currentPos, '\0');
        // length in bytes of the element
        encodeNumber(&currentPos, resultLengths[i]);
        // actual data
        encodeBinary(&currentPos, resultElems[i], resultLengths[i], result, totalLength);
    }
    // delete temporary storage
    for (size_t i = 0; i < static_cast<unsigned int>(numElem); i++)
        free(resultElems[i]);

    return static_cast<long>(totalLength);
}


long HttpServer::encodeEmpty(char *&result)
{
    size_t totalLength = getHeaderSize("");
    // the result collection is empty. It is returned as an empty MDD collection.
    // allocate the result
    result = static_cast<char *>(mymalloc(totalLength));
    char *currentPos = result;
    encodeHeader(&currentPos, RESPONSE_MDDS, systemEndianess, 0, "", result, totalLength);
    return static_cast<long>(totalLength);
}

long HttpServer::encodeError(char *&result, const r_ULong  errorNo,
                             const r_ULong lineNo, const r_ULong columnNo, const char *text)
{
    size_t textLen = strlen(text);

    size_t totalLength = 2 + 3 * sizeof(r_ULong) + textLen + 1;
    result = static_cast<char *>(mymalloc(totalLength));

    char *currentPos = result;
    *currentPos = RESPONSE_ERROR;
    currentPos++;
    *currentPos = systemEndianess;
    currentPos++;
    encodeNumber(&currentPos, errorNo);
    encodeNumber(&currentPos, lineNo);
    encodeNumber(&currentPos, columnNo);
    encodeString(&currentPos, text, result, totalLength);

    return static_cast<long>(totalLength);
}


size_t HttpServer::getHeaderSize(const char *collType) const
{
    size_t ret{};
    ++ret;                 // result type
    ++ret;                 // endianess
    ret += sizeof(r_Long); // number of objects
    if (collType)
        ret += strlen(collType) + 1;
    else
        ret += 1;    // '\0' in this case
    return ret;
}

void HttpServer::encodeHeader(char **dst, int responseType, int endianess,
                              r_Long numObjects, const char *collType, const char *dstStart, size_t totalLength) const
{
    encodeNumber(dst, static_cast<char>(responseType));
    encodeNumber(dst, static_cast<char>(endianess));
    encodeString(dst, collType, dstStart, totalLength);
    encodeNumber(dst, numObjects);
}


void HttpServer::swapArrayIfNeeded(const std::unique_ptr<Tile> &tile, const r_Minterval &dom) const
{
    if (systemEndianess == ENDIAN_BIG)
        return;

#ifdef RASDEBUG
    LTRACE << "Changing endianness of tile with domain " << dom;
#endif
    // calling client is a http-client(java -> always BigEndian) and server has LittleEndian
    char *typeStruct = tile->getType()->getTypeStructure();
    auto *baseType = static_cast<r_Base_Type *>(r_Type::get_any_type(typeStruct));
    free(typeStruct);

    char *dest = static_cast<char *>(mymalloc(tile->getSize()));

    // change the endianness of the entire tile for identical domains for src and dest
    r_Endian::swap_array(baseType, dom, dom, tile->getContents(), dest);
    delete baseType;

    // deallocate old contents
    char *oldCells = tile->getContents();
    free(oldCells);
    oldCells = NULL;
    tile->setContents(dest);
    // LINFO << "ok";
}

void HttpServer::releaseContext(ClientTblElt *context) const
{
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
}

void HttpServer::skipWhitespace(char **s) const
{
    char *ss = *s;
    while (isspace(*ss) && *ss != '\0')
    {
        ss++;
    }
}

void HttpServer::skipWord(char **s) const
{
    char *ss = *s;
    while (!isspace(*ss) && *ss != '\0')
    {
        ss++;
    }
}

long HttpServer::insertIfNeeded(unsigned long callingClientId, char *query, int binDataSize, char *binData,
                                int Endianess, char *&result, bool &isPersistent)
{
    // do we have an insert statement?
    // we need to keep this to be compatible with older clients
    if (binDataSize > 0)
    {
        // yes, it is an insert statement => analyze and prepare the MDDs
        // create an empty MDD-Set in the client context
        initExecuteUpdate(callingClientId);

        // vector with mdds in transfer encoding
        // it's deleted progressively in the loop below
        auto transferredMDDs = getMDDs(binDataSize, binData, Endianess);

        // Store all MDDs
        //LINFO << "vector has " << transferredMDDs.size() << " entries.";
        unsigned short execResult{};
        while (!transferredMDDs.empty())
        {
            execResult = startInsertMDD(callingClientId, query, transferredMDDs, isPersistent);
            if (execResult == 0)
                execResult = insertMDD(callingClientId, transferredMDDs, isPersistent);
            if (execResult != 0)
                return encodeInsertError(result, execResult, transferredMDDs);
        }

        execResult = endInsertMDD(callingClientId, isPersistent);
        if (execResult != 0)
            return encodeInsertError(result, execResult, transferredMDDs);
    }
    return 0;
}

unsigned short HttpServer::startInsertMDD(unsigned long callingClientId, char *query,
        const vector<HttpServer::MDDEncoding *> &transferredMDDs, bool &isPersistent)
{
    r_Minterval tempStorage(transferredMDDs.back()->domain);
    auto roid = std::unique_ptr<r_OId>(new r_OId(transferredMDDs.back()->oidString));
    isPersistent = roid->is_valid();
    if (roid->is_valid())
    {
        // parse the query string to get the collection name
        char *endPtr = query;
        skipWhitespace(&endPtr);
        skipWord(&endPtr); // insert
        skipWhitespace(&endPtr);
        skipWord(&endPtr); // into
        skipWhitespace(&endPtr);
        char *startPtr = endPtr;
        skipWord(&endPtr); // coll name

        std::unique_ptr<char[]> collection;
        if (endPtr - startPtr > 0)
        {
            collection.reset(new char[endPtr - startPtr + 1]);
            strncpy(collection.get(), startPtr, static_cast<size_t>(endPtr - startPtr));
            collection[static_cast<size_t>(endPtr - startPtr)] = '\0';
        }
        else
        {
            collection.reset(new char[1]);
        }
        return startInsertPersMDD(callingClientId, collection.get(), tempStorage,
                                  transferredMDDs.back()->typeLength,
                                  transferredMDDs.back()->objectTypeName, *roid);
    }
    else
    {
        return startInsertTransMDD(callingClientId, tempStorage,
                                   transferredMDDs.back()->typeLength,
                                   transferredMDDs.back()->objectTypeName);
    }
}


unsigned short HttpServer::insertMDD(unsigned long callingClientId,
                                     vector<HttpServer::MDDEncoding *> &transferredMDDs, bool isPersistent)
{
    auto *mdd = transferredMDDs.back();
    // create RPCMarray data structure - formats are currently hardcoded (r_Array)
    RPCMarray *rpcMarray = static_cast<RPCMarray *>(mymalloc(sizeof(RPCMarray)));
    rpcMarray->domain         = strdup(mdd->domain);
    rpcMarray->cellTypeLength = mdd->typeLength;
    rpcMarray->currentFormat  = r_Array;
    rpcMarray->storageFormat  = r_Array;
    rpcMarray->data.confarray_len = mdd->dataSize;
    rpcMarray->data.confarray_val = mdd->binData;

    /*
    LDEBUG << "Creating RPCMarray with domain " << rpcMarray->domain << ", size " <<
      rpcMarray->data.confarray_len << ", typeLength " << rpcMarray->cellTypeLength << " ...";
    */

    // split tile if a tileSize (an MInterval) has been specified
    std::unique_ptr<r_Minterval> splitInterval;
    if (mdd->tileSize != NULL)
    {
        splitInterval.reset(new r_Minterval(mdd->tileSize));
        LTRACE << "Split interval is " << *splitInterval;
    }
    // now insert the tile(s)
    auto ret = insertTile(callingClientId, isPersistent, rpcMarray, splitInterval.get());

    // free the stuff
    free(rpcMarray->domain);
    free(rpcMarray);
    delete (mdd);
    transferredMDDs.pop_back();
    return ret;
}

long HttpServer::encodeInsertError(char *&result, unsigned short execResult, vector<HttpServer::MDDEncoding *> &transferredMDDs)
{
    // TODO random numbers below -> constants..
    // no or wrong mdd type - return error message
    unsigned long errNo{};
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
        LERROR << "Failed inserting MDD";
    }

    auto returnValue = encodeError(result, errNo, 0, 0, transferredMDDs.back()->objectTypeName);

    //clean up
    while (!transferredMDDs.empty())
    {
        NNLINFO << "Freeing old transfer structures... ";
        free(transferredMDDs.back()->binData);
        delete (transferredMDDs.back());
        transferredMDDs.pop_back();
        BLINFO << "ok";
    }

    return returnValue;
}

