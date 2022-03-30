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
#include <cstdint>

#include <boost/thread/shared_mutex.hpp>

#include <grpc++/grpc++.h>

#include "rasnet/messages/rassrvr_rasmgr_service.grpc.pb.h"
#include "common/grpc/messages/health_service.grpc.pb.h"

#include "databasehost.hh"

#include "serverconfig.hh"
#include "server.hh"

namespace rasmgr
{

/**
 * Models a rasserver with capabilities to start/stop it, check if it's running,
 * allocate and deallocate clients to it.
 */
class ServerRasNet: public Server
{
public:
    /**
     * Initialize a new instance of RasServer which will run on the given host and port
     * @param config contains the following parameters:
     *
     *  * hostName: The name of the host on which the server will run
     *  * port: The port on which the server will run. The port is int32_t to allow 
     *    for compatibility with protobuf and java.
     *  * dbHost: Reference to the database host to which this server will connect.
     */
    explicit ServerRasNet(const ServerConfig &config);

    virtual ~ServerRasNet();

    /**
     * Start the rasserver binary with correct arguments with an excve call.
     * On success, this->started will be true.
     */
    virtual void startProcess() override;

    /**
     *
     * @return TRUE if the server is started, process with processId PID is
     * running on the system and replies to pings within SERVER_CALL_TIMEOUT 
     * milliseconds, false otherwise.
     */
    virtual bool isAlive() override;

    /**
     * If the server is started, check if the client with the given ID is alive.
     * Used by the Client class to check if a client is alive on any server.
     * @param clientId UUID of the client.
     * @return true if the client is alive, false otherwise
     * @throws runtime_error if the server cannot be contacted.
     */
    virtual bool isClientAlive(const std::string &clientId) override;

    /**
     * Allocate the client with the given ID and session ID to the server and
     * respective database. This may fail with a common::RuntimeException
     * if too many clients are already allocated to this server, or a
     * common::Exception if the GRPC AllocateClient call fails.
     * If all is well, the client is added to the sessionList and 
     * sessionNo + allocatedClientsNo are increased.
     * @param clientId UUID of the client
     * @param sessionId UUID of the session.
     * @param dbName name of the database which will be opened.
     * @param dbRights rights the client has on this database
     */
    virtual void allocateClientSession(const std::string &clientId, const std::string &sessionId,
                                       const std::string &dbName, const UserDatabaseRights &dbRights) override;

    /**
     * Remove the client with the given ID and session ID from the server.
     * The client is ALWAYS removed from the sessionList, and allocatedClientsNo
     * is decreased.
     * 
     * @param clientId UUID of the client
     * @param sessionId UUID of the session.
     * @throws a common::Exception if the GRPC DeallocateClient call fails.
     */
    virtual void deallocateClientSession(const std::string &clientId, const std::string &sessionId) override;

    /**
     * Register this server and transfer it to the FREE state.
     * This is called when the server is initalized.
     * The serverId must be the same as the one used to initialize the object,
     * must have been started already with startProcess, should be alive
     * on the system and respond ok to a GRPC GetServerStatus ping.
     * @param serverId UUID used to identify the server
     * @throws common::RuntimeException if the server is not started,
     * serverId does not match, a process with processId is not alive, or the
     * server address hostName:port is not valid. A common::Exception may be
     * thrown if the GRPC GetServerStatus call fails.
     */
    virtual void registerServer(const std::string &serverId) override;

    /**
     * Get the number of client sessions processed by this server throughout its
     * lifetime. This method is used by the ServerGroup to restart a server once
     * it has reached maximum a number of sessions
     * (to prevent memory leaks from getting out of control)
     */
    virtual std::uint32_t getTotalSessionNo() override;

    /**
     * Stop the RasServer process with the given KillLevel.
     * - if the KillLevel is NONE or FORCE only a single SIGTERM will be sent;
     * - if the KillLevel is KILL, then first a SIGTERM will be sent, and if
     *   it fails to stop the server after a SERVER_CLEANUP_TIMEOUT, a SIGKILL
     *   will finally be sent.
     * 
     * Post-condition: this->started and this->registered are false.
     */
    virtual void stop(KillLevel level) override;

    /**
     * @return True if the server process was started but the server has not
     * registered with RasMgr
     */
    virtual bool isStarting() override;

    /**
     *
     * @return True if the server does not have any clients assigned, false otherwise.
     * @throws common::InvalidStateException if the server is not started or
     * not registered with rasmgr.
     */
    virtual bool isFree() override;

    /**
     *
     * @return True if the server has available capacity for more clients, false otherwise.
     * @throws common::InvalidStateException if the server is not started or
     * not registered with rasmgr.
     */
    virtual bool isAvailable() override;

    /**
     * @return the port on which the server is running.
     */
    virtual std::int32_t getPort() const override;

    /**
     * @return the name of the host on which the server is running
     */
    virtual std::string getHostName() const override;

    /**
     * @return the UUID of the server
     */
    virtual std::string getServerId() const override;

private:

    /**
     * Send a signal sig to this server with PID processId; will log an error
     * in case it fails to send the signal.
     */
    void sendSignal(int sig) const;

    /**
     * The number of microseconds between a SIGTERM signal and a SIGKILL signal sent to the server.
     * This timeout allows the server enough time to cleanup after itself.
    */
    static const std::int32_t SERVER_CLEANUP_TIMEOUT;
    /**
     * Microseconds to wait between checks on whether the server is still alive
     */
    static const std::int32_t SERVER_CHECK_INTERVAL;

    std::string hostName;/*! Hostname of the RasServer process */
    std::int32_t port;/*! Port of the RasServer process */
    std::shared_ptr<DatabaseHost> dbHost;/*! Database host to which this server has access */
    std::string options;

    pid_t processId; /*! Id of the server process*/
    std::string serverId; /*! UUID that uniquely identifies the server */

    boost::shared_mutex stateMutex;
    bool registered;/*! Flag to indicate if the server is starting but has not yet registered */
    std::uint32_t allocatedClientsNo; /*! The number of allocated clients */
    bool started; /*True after the process is started*/

    std::uint32_t sessionNo;

    std::shared_ptr<::rasnet::service::RasServerService::Stub> service; /*! Service stub used to communicate with the RasServer process */

    boost::shared_mutex sessionListMutex; /*!Mutex used for making the object thread safe */
    std::set<std::pair<std::string, std::string>> sessionList;

    /**
     * Get the number of clients currently allocated to the the RasServer process.
     * @throws common::Exception if the GRPC GetServerStatus fails or timeouts.
     */
    uint32_t getClientQueueSize();

    /**
     * Get the command that will be used to start a server process as a list of
     * arguments.
     */
    std::vector<std::string> getStartProcessCommand();

    /**
     * Configure the client context for calls to the server. It's important to
     * do this in order to set a timeout of SERVER_CALL_TIMEOUT ms for calls.
     */
    void configureClientContext(grpc::ClientContext &context);

    std::string getCapability(const char *serverName, const char *databaseName, const UserDatabaseRights &rights);

    std::string convertDatabRights(const UserDatabaseRights &dbRights);
};

}

#endif /* RASMGR_X_SRC_RASSERVER_HH_ */
