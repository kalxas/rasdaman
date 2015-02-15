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

#include <fstream>

#include <boost/cstdint.hpp>
#include <boost/thread/locks.hpp>

#include "../../rasnet/src/messages/rasmgr_client_service.pb.h"
#include "../../common/src/logging/easylogging++.hh"
#include "../../common/src/uuid/uuid.hh"

#include "clientcredentials.hh"
#include "clientmanagementservice.hh"
#include "rasmgrconfig.hh"
#include "user.hh"

namespace rasmgr
{
using boost::mutex;
using boost::shared_ptr;
using boost::unique_lock;
using rasnet::service::ClientIdentity;
using std::string;

ClientManagementService::ClientManagementService(boost::shared_ptr<ClientManager> clientManager,boost::shared_ptr<ServerManager> serverManager)
{
    this->clientManager=clientManager;
    this->serverManager=serverManager;
}

ClientManagementService::~ClientManagementService()
{}

void
ClientManagementService::Connect(
    ::google::protobuf::RpcController* controller,
    const ::rasnet::service::ConnectReq* request,
    ::rasnet::service::ConnectRepl* response,
    ::google::protobuf::Closure* done)
{
    /**
     * 1. Use the client manager to register the client and obtain a UUID for the client
     * 2. Set the UUID in the response so that it is sent back to the client.
     */
    try
    {
        LDEBUG<<"Started connecting client";
        std::string out_clientUUID;

        //Create the ClientCredentials object used for authentication.
        ClientCredentials credentials(request->username(),
                                      request->passwordhash());

        //Try to authenticate the client and assign the client an ID.
        //If the authentication fails, an exception is thrown
        this->clientManager->connectClient(credentials, out_clientUUID);

        response->set_clientuuid(out_clientUUID);

        int clientId = common::UUID::generateIntId();
        response->set_clientid(clientId);
        response->set_keepalivetimeout(this->clientManager->getConfig().getClientLifeTime());

        LDEBUG<<"Finished connecting client with ID:"
              <<response->clientuuid();
    }
    catch(std::exception& ex)
    {
        LERROR<<ex.what();
        controller->SetFailed(ex.what());
    }
    catch(...)
    {
        string failureReason="Connect request failed for unknown reason.";
        LERROR<<failureReason;
        controller->SetFailed(failureReason);
    }
}

void
ClientManagementService::Disconnect(
    ::google::protobuf::RpcController* controller,
    const ::rasnet::service::DisconnectReq* request,
    ::rasnet::service::Void* response, ::google::protobuf::Closure* done)
{
    try
    {
        LDEBUG<<"Started disconnecting client with ID:"
              <<request->clientuuid();

        this->clientManager->disconnectClient(request->clientuuid());

        LDEBUG<<"Finished disconnecting client with ID:"
              <<request->clientuuid();
    }
    catch(std::exception& ex)
    {
        LERROR<<ex.what();
        controller->SetFailed(ex.what());
    }
    catch(...)
    {
        string failureReason="Disconnect request failed for unknown reason";
        LERROR<<failureReason;
        controller->SetFailed(failureReason);
    }
}

void
ClientManagementService::OpenDb(
    ::google::protobuf::RpcController* controller,
    const ::rasnet::service::OpenDbReq* request,
    ::rasnet::service::OpenDbRepl* response,
    ::google::protobuf::Closure* done)
{
    try
    {
        /**
         * 1. Lock access to this section.
         * 2. Get a free server
         * 3. Add the session to the client manager.
         * 4. Assign the client to the server.
         * 5. Fill the response to the client with the server's identity
         */
        shared_ptr<Server> server;
        string out_sessionId;
        string clientId = request->clientuuid();
        string dbName = request->databasename();

        unique_lock<mutex> lock(this->assignServerMutex);

        //Try to get a free server that contains the requested database
        if(this->serverManager->tryGetFreeServer(dbName, server))
        {
            //Open a session for the client.
            this->clientManager->openClientDbSession(clientId, dbName, server, out_sessionId);

            response->set_dbsessionid(out_sessionId);
            response->set_serverhostname(server->getHostName());
            response->set_port(server->getPort());
        }
        else
        {
            //Fail if there is no available server.
            controller->SetFailed("There is no available server for the client.");
        }

    }
    catch(std::exception& ex)
    {
        LERROR<<ex.what();
        controller->SetFailed(ex.what());
    }
    catch(...)
    {
        string failureReason="Open Database request failed for unknown reason.";
        LERROR<<failureReason;
        controller->SetFailed(failureReason);
    }
}

void
ClientManagementService::CloseDb(
    ::google::protobuf::RpcController* controller,
    const ::rasnet::service::CloseDbReq* request,
    ::rasnet::service::Void* response, ::google::protobuf::Closure* done)
{
    try
    {
        LDEBUG<<"Started closing database session: "
              <<request->dbsessionid()
              <<"by client with ID"
              <<request->clientid();

        this->clientManager->closeClientDbSession(request->clientuuid(), request->dbsessionid());

        LDEBUG<<"Finished closing database session: "
              <<request->dbsessionid()
              <<"by client with ID"
              <<request->clientid();
    }
    catch(std::exception& ex)
    {
        LERROR<<ex.what();
        controller->SetFailed(ex.what());
    }
    catch(...)
    {
        string failureReason="Close Database request failed with unknown exception";
        LERROR<<failureReason;
        controller->SetFailed(failureReason);
    }
}

void
ClientManagementService::KeepAlive(
    ::google::protobuf::RpcController* controller,
    const ::rasnet::service::KeepAliveReq* request,
    ::rasnet::service::Void* response, ::google::protobuf::Closure* done)
{
    try
    {
        LDEBUG<<"Start processing Keep Alive message from client with ID:"<<request->clientuuid();

        this->clientManager->keepClientAlive(request->clientuuid());

        LDEBUG<<"Finished processing Keep Alive message from client with ID:"<<request->clientuuid();
    }
    catch(std::exception& ex)
    {
        LERROR<<ex.what();
        controller->SetFailed(ex.what());
    }
    catch(...)
    {
        string failureReason="KeepAlive request failed with unknown exception";
        LERROR<<failureReason;
        controller->SetFailed(failureReason);
    }
}

} /* namespace rasmgr */
