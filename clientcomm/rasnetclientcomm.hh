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

#include "clientcomm/clientcomm.hh"
#include "globals.hh"
#include "raslib/parseparams.hh"

#include "rasnet/messages/rasmgr_client_service.grpc.pb.h"
#include "rasnet/messages/client_rassrvr_service.grpc.pb.h"
#include "common/grpc/messages/health_service.grpc.pb.h"

#include <memory>
#include <boost/thread/shared_mutex.hpp>
#include <boost/thread/thread.hpp>
#include <google/protobuf/service.h>
#include <google/protobuf/stubs/common.h>

class TurboQueryResult;
class r_Ref_Any;
template <typename T>
class r_Ref;
class r_GMarray;

// rpcif.h
struct RPCMarray;
struct GetMDDRes;
struct GetTileRes;
struct GetElementRes;

class RasnetClientComm : public ClientComm
{
public:
    explicit RasnetClientComm(const std::string &rasmgrHost, int rasmgrPort = DEFAULT_PORT);
    virtual ~RasnetClientComm() noexcept;

    int connectClient(const std::string &userName, const std::string &passwordHash);
    int disconnectClient();

    int openDB(const char *database) override;
    int closeDB() override;
    int createDB(const char *name) override;
    int destroyDB(const char *name) override;

    int openTA(unsigned short readOnly) override;
    int commitTA() override;
    int abortTA() override;

    void insertMDD(const char *collName, r_GMarray *mar) override;
    r_Ref_Any getMDDByOId(const r_OId &oid) override;

    void insertColl(const char *collName, const char *typeName, const r_OId &oid) override;
    void deleteCollByName(const char *collName) override;
    void deleteObjByOId(const r_OId &oid) override;
    void removeObjFromColl(const char *name, const r_OId &oid) override;
    r_Ref_Any getCollByName(const char *name) override;
    r_Ref_Any getCollByOId(const r_OId &oid) override;
    r_Ref_Any getCollOIdsByName(const char *name) override;
    r_Ref_Any getCollOIdsByOId(const r_OId &oid) override;

    void executeQuery(const r_OQL_Query &query, r_Set<r_Ref_Any> &result) override;
    void executeQuery(const r_OQL_Query &query) override;
    void executeQuery(const r_OQL_Query &query, r_Set<r_Ref_Any> &result, int dummy) override;

    r_OId getNewOId(unsigned short objType) override;
    unsigned short getObjectType(const r_OId &oid) override;

    char *getTypeStructure(const char *typeName, r_Type_Type typeType) override;

    int setTransferFormat(r_Data_Format format, const char *formatParams = NULL) override;
    int setStorageFormat(r_Data_Format format, const char *formatParams = NULL) override;

    void setUserIdentification(const char *userName, const char *plainTextPassword) override;
    void setMaxRetry(unsigned int newMaxRetry) override;
    unsigned int getMaxRetry() override;
    void setTimeoutInterval(int seconds) override;
    int getTimeoutInterval() override;

private:
    
    // -------------------------------------------------------------------------
    // rasmgr service
    std::shared_ptr<rasnet::service::RasmgrClientService::Stub> rasmgrService; /*! Service stub used to communicate with the RasServer process */
    bool initializedRasMgrService{false}; /*! Flag used to indicate if the service was initialized */
    boost::shared_mutex rasMgrServiceMtx;
    std::shared_ptr<common::HealthService::Stub> rasmgrHealthService;
    //
    std::shared_ptr<rasnet::service::RasmgrClientService::Stub> getRasMgrService(bool throwIfConnectionFailed = true);
    void initRasserverService();
    void closeRasserverService();
    
    // -------------------------------------------------------------------------
    // rasserver service
    std::shared_ptr<rasnet::service::ClientRassrvrService::Stub> rasserverService; /*! Service stub used to communicate with the RasServer process */
    bool initializedRasServerService{false}; /*! Flag used to indicate if the service was initialized */
    boost::shared_mutex rasServerServiceMtx;
    std::shared_ptr<common::HealthService::Stub> rasserverHealthService;
    //
    std::shared_ptr<rasnet::service::ClientRassrvrService::Stub> getRasServerService(bool throwIfConnectionFailed = true);
    void initRasmgrService();
    void closeRasmgrService();
    
    // -------------------------------------------------------------------------
    // START: KEEP ALIVE THREADS
    int64_t keepAliveTimeout{};

    // rasmgr
    std::unique_ptr<boost::thread> rasMgrKeepAliveManagementThread;
    boost::mutex rasmgrKeepAliveMutex;/*! Mutex used to safely stop the worker thread */
    bool isRasmgrKeepAliveRunning{false}; /*! Flag used to stop the worker thread */
    boost::condition_variable isRasmgrKeepAliveRunningCondition; /*! Condition variable used to stop the worker thread */
    //
    void startRasMgrKeepAlive();
    void stopRasMgrKeepAlive();
    void clientRasMgrKeepAliveRunner();

    // rasserver
    std::unique_ptr<boost::thread> rasServerKeepAliveManagementThread;
    boost::mutex rasserverKeepAliveMutex;/*! Mutex used to safely stop the worker thread */
    bool isRasserverKeepAliveRunning{false}; /*! Flag used to stop the worker thread */
    boost::condition_variable isRasserverKeepAliveRunningCondition; /*! Condition variable used to stop the worker thread */
    //
    void startRasServerKeepAlive();
    void stopRasServerKeepAlive();
    void clientRasServerKeepAliveRunner();
    // END: KEEP ALIVE

    
    // -------------------------------------------------------------------------
    // various identifiers
    /// The ID allocated by the rasmgr upon a successful connect request
    std::uint32_t clientId{};
    /// The ID allocated to the client upon a OpenDb request.
    /// If the allocated server belongs to the current rasmgr, the UUID is the same
    /// as the clientUUID. If the allocated server belongs to a peer of the current rasmgr,
    /// the UUID uniquely identifies the client on the remote server.
    std::uint32_t remoteClientId;
    std::uint32_t sessionId;
    
    // -------------------------------------------------------------------------
    // authentication
    char capability[100];
    
    // -------------------------------------------------------------------------
    // connection details
    /// full rasmgr address in format hostname:port
    std::string rasmgrHost;
    /// the rasmgr hostname
    std::string rasmgrHostname;
    /// rasserver hostname
    std::string rasServerHost;
    /// rasserver port
    int rasServerPort;
    /// data format for transfer compression
    r_Data_Format transferFormat{r_Array};
    /// storage format for inserting new tiles
    r_Data_Format storageFormat{r_Array};
    /// transfer format parameters
    char *transferFormatParams{nullptr};
    /// storage format parameters
    char *storageFormatParams{nullptr};
    /// connection timeout in milliseconds for calls to rasmgr/rasserver services.
    /// Default is 0: no timeout
    size_t timeoutMs{};
    /// parameter object for configuration
    r_Parse_Params clientParams;
    
    // -------------------------------------------------------------------------
    // error handlers
    static void handleError(const std::string &error);
    static void handleStatusCode(int status, const std::string &method);

    // -------------------------------------------------------------------------
    // utility methods
    int executeStartInsertPersMDD(const char *collName, r_GMarray *mar);
    int executeInsertTile(bool persistent, RPCMarray *tile);
    void executeEndInsertMDD(bool persistent);
    int executeEndTransfer();
    r_Ref_Any executeGetCollByNameOrOId(const char *collName, const r_OId &oid);
    r_Ref_Any executeGetCollOIdsByNameOrOId(const char *collName, const r_OId &oid);
    int  executeStartInsertTransMDD(r_GMarray *mdd);
    int executeInitUpdate();
    GetMDDRes  *executeGetNextMDD();
    GetTileRes *executeGetNextTile();
    int  executeExecuteQuery(const char *query, r_Set<r_Ref_Any> &result);
    GetElementRes *executeGetNextElement();
    int executeExecuteUpdateQuery(const char *query);
    int  executeExecuteUpdateQuery(const char *query, r_Set<r_Ref_Any> &result);
    int executeSetFormat(bool lTransferFormat, r_Data_Format format, const char *formatParams);

    void getMDDCollection(r_Set <r_Ref_Any> &mddColl, unsigned int isQuery);
    unsigned short getMDDCore(r_Ref<r_GMarray> &mdd, GetMDDRes *thisResult, unsigned int isQuery);
    void sendMDDConstants(const r_OQL_Query &query);
    void getElementCollection(r_Set<r_Ref_Any> &resultColl);

    int concatArrayData(const char *source, unsigned long srcSize, char *&dest,
                        unsigned long &destSize, unsigned long &destLevel);
    void freeGetTileRes(GetTileRes *ptr);

    void freeMarRpcRepresentation(const r_GMarray *mar, RPCMarray *rpcMarray);
    void getMarRpcRepresentation(
        const r_GMarray *mar, RPCMarray *&rpcMarray, r_Data_Format initStorageFormat, const r_Base_Type *baseType);
    void checkForRwTransaction();
};

#endif // RASNETCLIENTCOMM_HH
