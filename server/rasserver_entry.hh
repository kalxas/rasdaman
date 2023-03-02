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
#ifndef RASSERVER_ENTRY_HH
#define RASSERVER_ENTRY_HH

#include "servercomm/httpserver.hh"
#include <vector>
#include <memory>

class ClientTblElt;
class r_OId;
class r_Minterval;
struct ExecuteQueryRes;
struct ExecuteUpdateRes;
struct RPCMarray;
struct RPCOIdEntry;

/** 
 * This class is the entry point of the rasdaman server. Its functions are
 * called by the communication level.
*/

/**
  * \ingroup Servers
  */
class RasServerEntry
{
private:
    RasServerEntry() = default;

    HttpServer server;

    ClientTblElt *currentClientContext;
    std::uint32_t currentClientIdx;
    bool clientConnected{false};

public:
    static RasServerEntry &getInstance();

    ~RasServerEntry() = default;

    /**
     * Before the rasdaman server can answer any questions, it must connect to
     * the backend RASBASE database, where all array information is stored.
     * @throws an r_Error exception in case the database connection fails.
     */
    void connectToRasbase();

    /**
     * Connect a new client to the server and check it's capabilities. Each
     * server instance can have at most one client at a time; this requirement
     * automatically managed by rasmgr.
     * 
     * @param clientId the client ID
     * @param capability an authentication string provided by rasmgr.
     */
    void connectNewClient(std::uint32_t clientId, const char *capability);

    /**
     * @return context data for the current client.
     */
    ClientTblElt *getClientContext();

    /**
     * Disconnect the currently connected client from the server. This should be
     * called once for each connectNewClient.
     */
    void disconnectClient();

    void openDB(const char *databaseName);
    void closeDB();

    void beginTA(bool rw);
    void commitTA();
    void abortTA();
    bool isOpenTA();

    // provided for temporary compatibility with the encoding of the java interface.
    // resultBuffer will be allocated and its address stored in the given pointer
    // result is the length of the result
    long compat_executeQueryHttp(const char *httpParams, int httpParamsLen, char *&resultBuffer);

    r_OId compat_getNewOId(unsigned short objType);  // 1 - mddType, 2 -collType

    int compat_executeQueryRpc(const char *query, ExecuteQueryRes &queryResult, bool insert = false);

    int compat_getNextElement(char *&buffer, unsigned int &bufferSize);

    int compat_endTransfer();

    int compat_getNextMDD(r_Minterval &mddDomain, std::string &typeName, std::string &typeStructure, r_OId &oid, unsigned short &currentFormat);

    int compat_getNextTile(RPCMarray **rpcMarray);

    int compat_ExecuteUpdateQuery(const char *query, ExecuteUpdateRes &returnStructure);

    int compat_ExecuteInsertQuery(const char *query, ExecuteQueryRes &queryResult);

    int compat_InitUpdate();

    int compat_StartInsertTransMDD(const char *domain, int typeLength, const char *typeName);

    int compat_InsertTile(bool persistent, RPCMarray *);

    int compat_EndInsertMDD(int persistent);

    int compat_GetTypeStructure(const char *typeName, int typeType, std::string &typeStructure);

    int compat_StartInsertPersMDD(const char *collName, r_Minterval &mddDomain, int typeLength, const char *typeName, r_OId &oid);

    int compat_InsertMDD(const char *collName, RPCMarray *rpcMarray, const char *typeName, r_OId &oid);

    int compat_InsertCollection(const char *collName, const char *typeName, r_OId &oid);

    int compat_DeleteCollByName(const char *collName);

    int compat_DeleteObjByOId(r_OId &oid);

    int compat_RemoveObjFromColl(const char *collName, r_OId &oid);

    int compat_GetCollectionByName(const char *collName, std::string &typeName, std::string &typeStructure, r_OId &oid);

    int compat_GetCollectionByName(r_OId oid, std::string &typeName, std::string &typeStructure, std::string &collName);

    int compat_GetCollectionOidsByName(const char *collName, std::string &typeName, std::string &typeStructure, r_OId &oid, RPCOIdEntry *&oidTable, unsigned int &oidTableSize);

    int compat_GetCollectionOidsByOId(r_OId oid, std::string &typeName, std::string &typeStructure, RPCOIdEntry *&oidTable, unsigned int &oidTableSize, std::string &collName);

    int compat_GetObjectType(r_OId &oid, unsigned short &objType);

    int compat_SetTransferFormat(int format, const char *params);

    int compat_SetStorageFormat(int format, const char *params);

    r_OId createCollection(const char *collName, const char *collTypeName);

    r_OId createMDD(const char *collName, const char *mddTypeName, const char *definitionDomain, const char *tileDomain, bool rcindex);

    void extendMDD(r_OId mddOId, const char *stripeDomain, const char *tileDomain);

    std::vector<r_Minterval> getTileDomains(r_OId mddOId, const char *stripeDomain);
};
#endif
