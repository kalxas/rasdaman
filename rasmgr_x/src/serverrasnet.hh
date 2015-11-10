/*
 * This file is part of rasdaman community.
 *
 * Rasdaman community is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Rasdaman community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2014 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */

#ifndef RASMGR_X_SRC_RASSERVER_HH_
#define RASMGR_X_SRC_RASSERVER_HH_

#include <sys/types.h>
//TODO-AT:Remove the openssl when you remove the capabilities
#include <openssl/evp.h>

#include <string>
#include <vector>
#include <utility>
#include <memory>

#include <boost/cstdint.hpp>
#include <boost/date_time.hpp>
#include <boost/smart_ptr.hpp>

#include <grpc++/grpc++.h>

#include "../../rasnet/messages/rassrvr_rasmgr_service.grpc.pb.h"

#include "databasehost.hh"

#include "serverconfig.hh"
#include "server.hh"

namespace rasmgr
{

class ServerRasNet:public Server
{
public:
    /**
     * Initialize a new instance of RasServer which will run on the given host and port
     * @param hostName The name of the host on which the server will run
     * @param port The port on which the server will run. The port is int32_t to allow for compatibility with protobuf and java..
     * @param dbHost Reference to the database host to which this server will connect.
     */
    ServerRasNet(const ServerConfig& config);

    virtual ~ServerRasNet();

    /**
     * Start the RasServer Process.
     */
    virtual void startProcess();

    /**
     *
     * @return TRUE if the server replies to pings, false otherwise.
     */
    virtual bool isAlive();

    /**
     * Check if the client with the given ID is alive.
     * @param clientId UUID of the client.
     * @return true if the client is alive, false otherwise
     * @throws runtime_error if the server cannot be contacted.
     */
    virtual bool isClientAlive(const std::string& clientId);

    /**
     * Allocate the client with the given ID and session ID to the server and respective database.
     * @param clientId UUID of the client
     * @param sessionId UUID of the session.
     * @param dbName name of the database which will be opened.
     * @param dbRights rights the client has on this database
     */
    virtual void allocateClientSession(const std::string& clientId, const std::string& sessionId,const std::string& dbName, const UserDatabaseRights& dbRights);

    /**
     * Remove the client with the given ID and session ID from the server.
     * @param clientId
     * @param sessionId
     */
    virtual void deallocateClientSession(const std::string& clientId, const std::string& sessionId);

    /**
     * Register this server and transfer it to the FREE state.
     * This is called when the server is initalized.
     * The serverId must be the same as the one used to initialize the object.
     * @param serverId UUID used to identify the server
     */
    virtual void registerServer(const std::string& serverId);

    /**
     * @brief getTransactionNo Get the number of client sessions processed
     * by this server throughout its lifetime.
     * This method is used by the ServerGroup to restart a server once
     * it has reached a number of sessions
     * (to prevent memory leaks from getting out of control)
     * @return
     */
    virtual boost::uint32_t getTotalSessionNo();

    /**
     * Stop the RasServer process.
     * @param force TRUE if the server should abort any running transaction and terminate,
     * FALSE if the server should terminate after it finishes all running transactions.
     * The server will not accept any more clients from this point.
     */
    virtual void stop(KillLevel level);

    /**
     *
     * @return True if the server process was started but the server has not registered with RasMgr
     */
    virtual bool isStarting();

    /**
     *
     * @return True if the server does not have any clients assigned, false otherwise.
     */
    virtual bool isFree();

    /**
     *
     * @return True if the server has available capacity, false otherwise
     */
    virtual bool isAvailable();

    /**
     * @return the port on which the server is running.
     */
    virtual boost::int32_t getPort() const;

    /**
     * @return the name of the host on which the server is running
     */
    virtual std::string getHostName() const;

    /**
     * @return the UUID of the server
     */
    virtual std::string getServerId() const;

private:
    std::string hostName;/*! Hostname of the RasServer process */
    boost::int32_t port;/*! Port of the RasServer process */
    boost::shared_ptr<DatabaseHost> dbHost;/*! Database host to which this server has access */
    std::string options;

    pid_t processId; /*Id of the server process*/
    std::string serverId; /*! UUID that uniquely identifies the server */

    boost::shared_mutex stateMtx;
    bool registered;/*! Flag to indicate if the server is starting but has not yet registered */
    boost::uint32_t allocatedClientsNo; /*! The number of allocated clients */
    bool started; /*True after the process is started*/

    boost::uint32_t sessionNo;

    //!!!! DO NOT USE THESE DIRECTLY
    boost::shared_ptr<::rasnet::service::RasServerService::Stub> service; /*! Service stub used to communicate with the RasServer process */
    bool initializedService; /*! Flag used to indicate if the service was initialized */
    boost::shared_mutex serviceMtx;
    /**
     * Get the number of clients the RasServer process currently has.
     * @return
     */
    uint32_t getClientQueueSize();

    boost::shared_mutex sessionMtx; /*!Mutex used for making the object thread safe */
    std::set<std::pair<std::string, std::string> > sessionList;

    /**
     * @return Initialized shared_ptr to the RasServerService.
     */
    boost::shared_ptr<::rasnet::service::RasServerService::Stub> getService();
    std::string getStartProcessCommand();

    void configureClientContext(grpc::ClientContext& context);

    //TODO-AT: remove this
    const char* getCapability(const char *serverName,const char *databaseName, const UserDatabaseRights& rights);
    int messageDigest(const char *input,char *output,const char *mdName);
    const char * convertDatabRights(const UserDatabaseRights& dbRights);

};

}

#endif /* RASMGR_X_SRC_RASSERVER_HH_ */
