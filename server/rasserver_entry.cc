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


#include "rasserver_entry.hh"
#include "raslib/error.hh"
#include "raslib/oid.hh"
#include "servercomm/cliententry.hh"
#include "servercomm/accesscontrol.hh"
#include "reladminif/adminif.hh"

#include <logging.hh>
#include <limits>

using namespace std;

extern AccessControl accessControl;

unsigned long RasServerEntry::clientCount = 0;
const unsigned long RasServerEntry::noClientConnected = numeric_limits<unsigned long>::max();

RasServerEntry& RasServerEntry::getInstance()
{
    static RasServerEntry instance;
    return instance;
}

void RasServerEntry::connectToRasbase()
{
    auto *admin = AdminIf::instance();
    if (!admin)
        throw r_Error(r_Error::r_Error_BaseDBMSFailed);
    server.setAdmin(admin);
}

void RasServerEntry::connectNewClient(const char* capability)
{
    if (!currentClientContext)
    {
        if (accessControl.crunchCapability(capability) == CAPABILITY_REFUSED) {
            LWARNING << "Invalid capability: '" << capability << "'";
            throw r_Ecapability_refused();
        }
        
        currentClientIdx = ++clientCount;
        currentClientContext = new ClientTblElt(ClientType::Regular, currentClientIdx);
        server.addClientTblEntry(currentClientContext);
    }
    else
    {
        LERROR << "Another client is already connected to this server, cannot serve any new clients.";
        throw r_Error(SERVEROCCUPIEDWITHOTHERCLIENT);
    }
}

ClientTblElt *RasServerEntry::getClientContext()
{
    return server.getClientContext(currentClientIdx);
}

void RasServerEntry::disconnectClient()
{
    server.deleteClientTblEntry(currentClientIdx);
    currentClientIdx = noClientConnected;
    currentClientContext = nullptr;
}

void RasServerEntry::openDB(const char* databaseName)
{
    server.openDB(currentClientIdx, databaseName, "");
}

void RasServerEntry::closeDB()
{
    server.closeDB(currentClientIdx);
}

void RasServerEntry::beginTA(bool rw)
{
    server.beginTA(currentClientIdx, rw ? 0 : 1);
}

void RasServerEntry::commitTA()
{
    server.commitTA(currentClientIdx);
}

void RasServerEntry::abortTA()
{
    server.abortTA(currentClientIdx);
}

bool RasServerEntry::isOpenTA()
{
    return server.isTAOpen(currentClientIdx);
}

long RasServerEntry::compat_executeQueryHttp(const char* httpParams, int httpParamsLen, char*& resultBuffer)
{
    currentClientContext->clientType = ClientType::Http;
    return server.processRequest(currentClientIdx, httpParams, httpParamsLen, resultBuffer);
}

int RasServerEntry::compat_executeQueryRpc(const char* query, ExecuteQueryRes& queryResult, bool insert)
{
    return server.executeQuery(currentClientIdx, query, queryResult, insert);
}

int RasServerEntry::compat_getNextElement(char*& buffer, unsigned int&  bufferSize)
{
    return server.getNextElement(currentClientIdx, buffer, bufferSize);
}

int RasServerEntry::compat_endTransfer()
{
    return server.endTransfer(currentClientIdx);
}

int RasServerEntry::compat_getNextMDD(r_Minterval& mddDomain, std::string &typeName, std::string &typeStructure, r_OId& oid, unsigned short& currentFormat)
{
    return server.getNextMDD(currentClientIdx, mddDomain, typeName, typeStructure, oid, currentFormat);
}
int RasServerEntry::compat_getNextTile(RPCMarray** rpcMarray)
{
    return server.getNextTile(currentClientIdx, rpcMarray);
}

int RasServerEntry::compat_ExecuteUpdateQuery(const char* query, ExecuteUpdateRes& returnStructure)
{
    // update query (and insert < v9.1), does not return results
    return server.executeUpdate(currentClientIdx, query, returnStructure);
}

int RasServerEntry::compat_ExecuteInsertQuery(const char* query, ExecuteQueryRes& queryResult)
{
    // insert query (>= v9.1), returns results
    return server.executeInsert(currentClientIdx, query, queryResult);
}

int RasServerEntry::compat_InitUpdate()
{
    return server.initExecuteUpdate(currentClientIdx);
}

int RasServerEntry::compat_StartInsertTransMDD(const char* domain, int typeLength, const char* typeName)
{
    r_Minterval mddDomain(domain);
    return server.startInsertTransMDD(currentClientIdx, mddDomain, static_cast<unsigned int>(typeLength), typeName);
}

int RasServerEntry::compat_InsertTile(bool persistent, RPCMarray* rpcMarray)
{
    return server.insertTile(currentClientIdx, persistent, rpcMarray);
}

int RasServerEntry::compat_EndInsertMDD(int persistent)
{
    return server.endInsertMDD(currentClientIdx, persistent);
}

int RasServerEntry::compat_GetTypeStructure(const char* typeName, int typeType, std::string& typeStructure)
{
    return server.getTypeStructure(currentClientIdx, typeName, static_cast<unsigned short>(typeType), typeStructure);
}

int RasServerEntry::compat_StartInsertPersMDD(const char* collName, r_Minterval& mddDomain, int typeLength, const char* typeName, r_OId& oid)
{
    return server.startInsertPersMDD(currentClientIdx, collName, mddDomain, static_cast<unsigned int>(typeLength), typeName, oid);
}

int RasServerEntry::compat_InsertMDD(const char* /*collName*/, RPCMarray* /*rpcMarray*/, const char* /*typeName*/, r_OId& /*oid*/)
{
    LERROR << "Invoked unsupported server functionality 'insert whole MDD'.";
    throw r_Error(INTERNALSERVERERROR); // Internal error
}

int RasServerEntry::compat_InsertCollection(const char* collName, const char* typeName, r_OId& oid)
{
    return server.insertColl(currentClientIdx, collName, typeName, oid);
}

int RasServerEntry::compat_DeleteCollByName(const char* collName)
{
    return server.deleteCollByName(currentClientIdx, collName);
}

int RasServerEntry::compat_DeleteObjByOId(r_OId& oid)
{
    return server.deleteObjByOId(currentClientIdx, oid);
}

int RasServerEntry::compat_RemoveObjFromColl(const char* collName, r_OId& oid)
{
    return server.removeObjFromColl(currentClientIdx, collName, oid);
}

int RasServerEntry::compat_GetCollectionByName(const char* collName, std::string &typeName, std::string &typeStructure, r_OId& oid)
{
    return server.getCollByName(currentClientIdx, collName, typeName, typeStructure, oid);
}

int RasServerEntry::compat_GetCollectionByName(r_OId oid, std::string &typeName, std::string &typeStructure, std::string &collName)
{
    return server.getCollByOId(currentClientIdx, oid, typeName, typeStructure, collName);
}

int RasServerEntry::compat_GetCollectionOidsByName(const char* collName, std::string &typeName, std::string &typeStructure, r_OId& oid, RPCOIdEntry*& oidTable, unsigned int& oidTableSize)
{
    return server.getCollOIdsByName(currentClientIdx, collName, typeName, typeStructure, oid, oidTable, oidTableSize);
}

int RasServerEntry::compat_GetCollectionOidsByOId(r_OId oid, std::string &typeName, std::string &typeStructure, RPCOIdEntry*& oidTable, unsigned int& oidTableSize, std::string &collName)
{
    return server.getCollOIdsByOId(currentClientIdx, oid, typeName, typeStructure, oidTable, oidTableSize, collName);
}

int RasServerEntry::compat_GetObjectType(r_OId& oid, unsigned short& objType)
{
    return server.getObjectType(currentClientIdx, oid, objType);
}

int RasServerEntry::compat_SetTransferFormat(int format, const char* params)
{
    return server.setTransferMode(currentClientIdx, static_cast<unsigned short>(format), params);
}
int RasServerEntry::compat_SetStorageFormat(int format, const char* params)
{
    return server.setStorageMode(currentClientIdx, static_cast<unsigned short>(format), params);
}
r_OId RasServerEntry::compat_getNewOId(unsigned short objType)
{
    r_OId result;
    server.getNewOId(currentClientIdx, objType, result);
    return result;
}


#include "server/createinitmdd.hh"

r_OId RasServerEntry::createCollection(const char* collName, const char* collTypeName)
{
    FastCollectionCreator fcc(collName, collTypeName);
    return fcc.createCollection();
}
r_OId RasServerEntry::createMDD(const char* collName, const char* mddTypeName, 
                                const char* definitionDomain,
                                const char* tileDomain, bool rcindex)
{
    FastMDDCreator fc;

    fc.setCollectionName(collName);
    fc.setMDDTypeName(mddTypeName);
    if (rcindex)
        return fc.createRCxMDD(definitionDomain, tileDomain);
    else
        return fc.createMDD(definitionDomain);
}
void  RasServerEntry::extendMDD(r_OId mddOId, const char* stripeDomain, const char* tileDomain)
{
    FastMDDCreator fc;
    fc.addStripe(mddOId, stripeDomain, tileDomain);
}
vector<r_Minterval> RasServerEntry::getTileDomains(r_OId mddOId, const char* stripeDomain)
{
    FastMDDCreator fc;
    return fc.getTileDomains(mddOId, stripeDomain);
}

