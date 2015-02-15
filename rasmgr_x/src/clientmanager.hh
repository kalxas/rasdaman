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


#ifndef RASMGR_X_SRC_CLIENTMANAGER_HH_
#define RASMGR_X_SRC_CLIENTMANAGER_HH_

#include <boost/cstdint.hpp>
#include <boost/scoped_ptr.hpp>
#include <boost/shared_ptr.hpp>
#include <boost/thread/shared_mutex.hpp>
#include <boost/thread/thread.hpp>
#include <map>
#include <string>

#include "../../common/src/time/timer.hh"
#include "../../common/src/zeromq/zmq.hh"

#include "usermanager.hh"
#include "client.hh"
#include "clientcredentials.hh"
#include "servermanager.hh"
#include "clientmanagerconfig.hh"

namespace rasmgr
{

/**
 * @brief The ClientManager class maintains the list of active clients,
 * it registers and deregisters clients. It allows clients to open and close db sessions.
 * In the current version, the ClientManager runs a cleanup thread at a fixed interval
 * that removes dead clients from the registry.
 */
class ClientManager
{
public:
    /**
     * @brief ClientManager
     * @param userManager Instance of the user manager that holds information
     * about registered users. It is needed to evaluate the access credentials
     * of each client
     */
    ClientManager(const ClientManagerConfig& config, boost::shared_ptr<UserManager> userManager);

    /**
     * Destruct the ClientManager class object.
     */
    virtual ~ClientManager();

    /**
     * Authenticate and connect the client to RasMgr.
     * If the authentication is successful, the UUID assigned to the client will be returned.
     * If the authentication fails, an exception is thrown.
     * @param clientCredentials Credentials used to authenticate the client.
     * @param out_clientUUID If the method is successful, it will contain the UUID assigned to the client.
     * @throws std::runtime_error
     */
    virtual void connectClient(const ClientCredentials& clientCredentials, std::string& out_clientUUID);

    /**
     * Disconnect the client from RasMgr and remove its information from RasMgr database.
     * @param clientId UUID of the client that will be removed.
     */
    virtual void disconnectClient(const std::string& clientId);

    /**
     * @brief openClientDbSession Open a database session for the client with the given id and provide a unique session id.
     * @param clientId Unique ID identifying the client
     * @param dbName  Database that the client wants to open
     * @param assignedServer  Server that will be assigned to the client if this operation succeeds.
     * @param out_sessionId  Session ID that will uniquely identify this session together with the clientID.
     */
    virtual void openClientDbSession(std::string clientId, const std::string& dbName,boost::shared_ptr<Server> assignedServer, std::string& out_sessionId);

    /**
     * @brief closeClientDbSession Remove a client session from the client manager and the servers
     * @param clientId ID that uniquely identifies a client
     * @param sessionId ID that uniquely identifies a session with respect to a client
     */
    virtual void closeClientDbSession(const std::string& clientId, const std::string& sessionId);

    /**
     * Extend the liveliness of the client and prevent it from being removed
     * from RasMgr database from the list of active clients.
     * @param clientId UUID of the client
     */
    virtual void keepClientAlive(const std::string& clientId);

    /**
     * @brief getConfig Get a copy of the configuration object used by the client manager.
     * @return
     */
    ClientManagerConfig getConfig();

private:
    ClientManagerConfig config;
    zmq::context_t context;/*!< ZMQ context used for inter thread communication */
    boost::scoped_ptr<zmq::socket_t> controlSocket; /*!<Socket for inter-thread communication */
    std::string controlEndpoint; /*!<Endpoint used for inter-thread communication */

    boost::scoped_ptr<boost::thread> managementThread; /*! Thread used to manage the list of clients and remove dead ones */

    std::map<std::string, boost::shared_ptr<Client> > clients; /*! list of active clients */
    boost::shared_mutex clientsMutex; /*! Mutex used to synchronize access to the clients object*/
    boost::shared_ptr<UserManager> userManager;

    /**
     * Evaluate the list of clients and remove the ones that have died.
     */
    void evaluateClientsStatus();
};

} /* namespace rasmgr */

#endif /* CLIENTMANAGER_HH_ */
