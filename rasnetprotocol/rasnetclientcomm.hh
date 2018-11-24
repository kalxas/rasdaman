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

#ifndef RASNETCLIENTCOMM_HH
#define RASNETCLIENTCOMM_HH

#include <boost/scoped_ptr.hpp>
#include <boost/shared_ptr.hpp>
#include <boost/make_shared.hpp>
#include <boost/thread/locks.hpp>
#include <boost/thread/shared_mutex.hpp>
#include <boost/thread/thread.hpp>

#include <google/protobuf/service.h>
#include <google/protobuf/stubs/common.h>

#include "rasnet/messages/rasmgr_client_service.grpc.pb.h"
#include "rasnet/messages/client_rassrvr_service.grpc.pb.h"
#include "common/grpc/messages/health_service.grpc.pb.h"

#include "clientcomm/clientcomm.hh"
#include "clientcomm/rpcif.h"
#include "rasodmg/ref.hh"

class RasnetClientComm : public ClientComm
{
public:
    RasnetClientComm(std::string rasmgrHost, int rasmgrPort = RASMGRPORT);
    virtual ~RasnetClientComm() noexcept;

    int connectClient(std::string userName, std::string passwordHash);
    int disconnectClient();

    int openDB(const char* database);
    int closeDB();
    int createDB(const char* name);
    int destroyDB(const char* name);

    int openTA(unsigned short readOnly);
    int commitTA();
    int abortTA();

    void insertMDD(const char* collName, r_GMarray* mar);
    r_Ref_Any getMDDByOId(const r_OId& oid);

    void insertColl(const char* collName, const char* typeName, const r_OId& oid);
    void deleteCollByName(const char* collName);
    void deleteObjByOId(const r_OId& oid);
    void removeObjFromColl(const char* name, const r_OId& oid);
    r_Ref_Any getCollByName(const char* name);
    r_Ref_Any getCollByOId(const r_OId& oid);
    r_Ref_Any getCollOIdsByName(const char* name);
    r_Ref_Any getCollOIdsByOId(const r_OId& oid);

    void executeQuery(const r_OQL_Query& query, r_Set<r_Ref_Any>& result);
    void executeQuery(const r_OQL_Query& query);
    void executeQuery(const r_OQL_Query& query, r_Set<r_Ref_Any>& result, int dummy);

    r_OId getNewOId(unsigned short objType);
    unsigned short getObjectType(const r_OId& oid);

    char* getTypeStructure(const char* typeName, r_Type_Type typeType);

    int setTransferFormat(r_Data_Format format, const char* formatParams = NULL);
    int setStorageFormat(r_Data_Format format, const char* formatParams = NULL);

    bool effectivTypeIsRNP();
    long unsigned int getClientID() const;
    void triggerAliveSignal();
    void sendAliveSignal();
    const char* getExtendedErrorInfo();
    void setUserIdentification(const char* userName, const char* plainTextPassword);
    void setMaxRetry(unsigned int newMaxRetry);
    unsigned int  getMaxRetry();
    void setTimeoutInterval(int seconds);
    int  getTimeoutInterval();

private:
    boost::shared_ptr<rasnet::service::ClientRassrvrService::Stub> rasserverService; /*! Service stub used to communicate with the RasServer process */
    bool initializedRasServerService; /*! Flag used to indicate if the service was initialized */
    boost::shared_mutex rasServerServiceMtx;
    boost::shared_ptr<common::HealthService::Stub> rasserverHealthService;

    boost::shared_ptr<rasnet::service::RasmgrClientService::Stub> rasmgrService; /*! Service stub used to communicate with the RasServer process */
    bool initializedRasMgrService; /*! Flag used to indicate if the service was initialized */
    boost::shared_mutex rasMgrServiceMtx;
    boost::shared_ptr<common::HealthService::Stub> rasmgrHealthService;

    /* START: KEEP ALIVE */
    int64_t keepAliveTimeout;

    /* RASMGR */
    ::boost::scoped_ptr<::boost::thread> rasMgrKeepAliveManagementThread;
    boost::mutex rasmgrKeepAliveMutex;/*! Mutex used to safely stop the worker thread */
    bool isRasmgrKeepAliveRunning; /*! Flag used to stop the worker thread */
    boost::condition_variable isRasmgrKeepAliveRunningCondition; /*! Condition variable used to stop the worker thread */

    void startRasMgrKeepAlive();
    void stopRasMgrKeepAlive();

    void clientRasMgrKeepAliveRunner();

    /* RASSERVER */
    ::boost::scoped_ptr<::boost::thread> rasServerKeepAliveManagementThread;
    boost::mutex rasserverKeepAliveMutex;/*! Mutex used to safely stop the worker thread */
    bool isRasserverKeepAliveRunning; /*! Flag used to stop the worker thread */
    boost::condition_variable isRasserverKeepAliveRunningCondition; /*! Condition variable used to stop the worker thread */

    void startRasServerKeepAlive();
    void stopRasServerKeepAlive();

    void clientRasServerKeepAliveRunner();
    /* END: KEEP ALIVE */

    ::boost::shared_ptr<rasnet::service::ClientRassrvrService::Stub> getRasServerService(bool throwIfConnectionFailed = true);
    ::boost::shared_ptr<rasnet::service::RasmgrClientService::Stub> getRasMgrService(bool throwIfConnectionFailed = true);

    void initRasserverService();
    void initRasmgrService();
    void closeRasserverService();
    void closeRasmgrService();

    long unsigned int clientId;
    /// The ID allocated by the rasmgr upon a successful connect request
    std::string clientUUID;

    /// The ID allocated to the client upon a OpenDb request.
    /// If the allocated server belongs to the current rasmgr, the UUID is the same
    /// as the clientUUID. If the allocated server belongs to a peer of the current rasmgr,
    /// the UUID uniquely identifies the client on the remote server.
    std::string remoteClientUUID;
    std::string sessionId;

    char capability[100];
    /// data format for transfer compression
    r_Data_Format transferFormat;
    /// storage format for inserting new tiles
    r_Data_Format storageFormat;
    /// transfer format parameters
    char* transferFormatParams;
    /// storage format parameters
    char* storageFormatParams;
    /// parameter object for configuration
    r_Parse_Params* clientParams;

    std::string rasServerHost;
    int rasServerPort;

    std::string rasmgrHost;

    static void handleError(const std::string& error);
    static void handleConnectionFailure();
    static void handleStatusCode(int status, const std::string& method);

    int executeStartInsertPersMDD(const char* collName, r_GMarray* mar);
    int executeInsertTile(bool persistent, RPCMarray* tile);
    void executeEndInsertMDD(bool persistent);
    int executeEndTransfer();
    r_Ref_Any executeGetCollByNameOrOId(const char* collName, const r_OId& oid);
    r_Ref_Any executeGetCollOIdsByNameOrOId(const char* collName, const r_OId& oid);
    int  executeStartInsertTransMDD(r_GMarray* mdd);
    int executeInitUpdate();
    GetMDDRes*  executeGetNextMDD();
    GetTileRes* executeGetNextTile();
    int  executeExecuteQuery(const char* query, r_Set<r_Ref_Any>& result);
    GetElementRes* executeGetNextElement();
    int executeExecuteUpdateQuery(const char* query);
    int  executeExecuteUpdateQuery(const char* query, r_Set<r_Ref_Any>& result);
    int executeSetFormat(bool lTransferFormat, r_Data_Format format, const char* formatParams);

    void getMDDCollection(r_Set <r_Ref_Any>& mddColl, unsigned int isQuery);
    unsigned short getMDDCore(r_Ref<r_GMarray>& mdd, GetMDDRes* thisResult, unsigned int isQuery);
    void sendMDDConstants(const r_OQL_Query& query);
    void getElementCollection(r_Set<r_Ref_Any>& resultColl);

    int concatArrayData(const char* source, unsigned long srcSize, char*& dest,
                        unsigned long& destSize, unsigned long& destLevel);
    void freeGetTileRes(GetTileRes* ptr);

    void freeMarRpcRepresentation(const r_GMarray* mar, RPCMarray* rpcMarray);
    void getMarRpcRepresentation(const r_GMarray* mar, RPCMarray*& rpcMarray, r_Data_Format initStorageFormat, const r_Base_Type* baseType);
    void checkForRwTransaction();
};

#endif // RASNETCLIENTCOMM_HH
