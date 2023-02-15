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

#include "server.hh"
#include "constants.hh"
#include "rasmgrconfig.hh"
#include "userdatabaserights.hh"
#include "include/globals.hh"
#include "common/grpc/grpcutils.hh"
#include "common/uuid/uuid.hh"
#include "common/crypto/crypto.hh"
#include "common/logging/signalhandler.hh"
#include "common/util/networkutils.hh"
#include "common/util/system.hh"
#include "common/util/vectorutils.hh"
#include "common/util/fileutils.hh"
#include "common/string/stringutil.hh"
#include "common/exceptions/invalidstateexception.hh"
#include "common/exceptions/runtimeexception.hh"
#include "rasnet/messages/rassrvr_rasmgr_service.pb.h"
#include <logging.hh>

#include <unistd.h>
#include <signal.h>
#include <errno.h>

#include <cstring>
#include <iostream>
#include <stdexcept>
#include <chrono>

#include <boost/lexical_cast.hpp>
#include <boost/tokenizer.hpp>

#include <grpc++/grpc++.h>
#include <grpc++/security/credentials.h>

// waitpid
#include <sys/types.h>
#include <sys/wait.h>

namespace rasmgr
{
using grpc::Channel;
using grpc::ClientContext;
using grpc::Status;

using common::GrpcUtils;

using rasnet::service::Void;
using rasnet::service::AllocateClientReq;
using rasnet::service::ClientStatusRepl;
using rasnet::service::ClientStatusReq;
using rasnet::service::CloseServerReq;
using rasnet::service::ServerStatusRepl;
using rasnet::service::ServerStatusReq;
using rasnet::service::DeallocateClientReq;
using rasnet::service::DatabaseRights;
using rasnet::service::RasServerService;

#define RASEXECUTABLE BINDIR"rasserver"

// give 3 seconds to rasserver to cleanly shutdown, before killing it with a SIGKILL
const std::int32_t Server::SERVER_CLEANUP_TIMEOUT = 3000000;
// 10 milliseconds
const std::int32_t Server::SERVER_CHECK_INTERVAL = 10000;

Server::Server(const ServerConfig &config):
    hostName{config.getHostName()},
    port(int32_t(config.getPort())),
    dbHost(config.getDbHost()),
    options{config.getOptions()},
    processId{-1},
    serverId{common::UUID::generateUUID()},
    registered{false},
    clientConnected{0},
    started{false},
    sessionNo{0}
{
    // Initialize the service
    auto serverAddress = GrpcUtils::constructAddressString(this->hostName, std::uint32_t(this->port));
    auto channel = grpc::CreateCustomChannel(serverAddress, grpc::InsecureChannelCredentials(),
                                             GrpcUtils::getDefaultChannelArguments());
    this->service.reset(new RasServerService::Stub(channel));
}

Server::~Server()
{
    boost::lock_guard<boost::shared_mutex> lock(sessionMutex);
    if (dbHost)
    {
        if (this->sessionOpen)
        {
            this->dbHost->removeClientSessionFromDB(clientId, sessionId);
        }
        this->sessionOpen = false;
    
        //Wait for the process to finish
        if (this->started)
        {
            LWARNING << "The server process " << serverId << " is running; "
                     << "blocking thread to wait for process " << processId << " to exit.";
        }
    
        int status;
        int waitOptions = 0;
        waitpid(this->processId, &status, waitOptions);
    
        LDEBUG << "Rasserver destructed: " << serverId;
    }
}

void Server::startProcess()
{
    {
        boost::lock_guard<boost::shared_mutex> lock(this->stateMutex);
        if (this->started)
        {
            throw common::InvalidStateException(
                "The server process " + std::to_string(processId) + " has already been started.");
        }
    }

    auto commandVec = this->getStartProcessCommand();
#ifdef RASDEBUG
    LDEBUG << "Starting server process " << serverId << " with command: "
           << common::VectorUtils::join(commandVec, " ");
#endif
    
    this->processId = fork();

    switch (this->processId)
    {
    case -1:
    {
        //Failure
        LERROR << "Server spawning failed to fork process. Reason: " << strerror(errno);
        return;
    }
    case 0:
    {
        //Child process
        char **commandArgs = new char *[commandVec.size() + 1];
        for (std::size_t i = 0; i < commandVec.size(); ++i)
        {
            commandArgs[i] = new char[commandVec[i].size() + 1];
            strcpy(commandArgs[i], commandVec[i].c_str());
        }
        commandArgs[commandVec.size()] = (char *)NULL;

        if (execv(RASEXECUTABLE, commandArgs) == -1)
        {
            LERROR << "Starting server process with command\n"
                   << common::VectorUtils::join(commandVec, " ") 
                   << "\nfailed: " << strerror(errno);
            exit(EXIT_FAILURE);
        }

        // free
        for (std::size_t i = 0; i < commandVec.size(); ++i)
        {
            delete [] commandArgs[i];
            commandArgs[i] = NULL;
        }
        delete [] commandArgs;
        commandArgs = NULL;
    }
    break;

    default:
    {
        //Parent rasmgr process
        LDEBUG << "Server process started " << serverId << " with pid " << this->processId;
        this->started = true;
    }
    }
}

bool Server::isAlive()
{
    //Assume the process is dead
    bool result = false;

    boost::lock_guard<boost::shared_mutex> lock(this->stateMutex);
    if (this->started && common::SystemUtil::isProcessAlive(processId))
    {
        // The process is alive - is the server responding?
        ServerStatusReq request = ServerStatusReq::default_instance();
        ServerStatusRepl reply;

        // Set timeout for API
        ClientContext context;
        this->configureClientContext(context);

        LTRACE << "Check if server " << this->serverId << " is alive";
        Status status = this->service->GetServerStatus(&context, request, &reply);
        result = status.ok();
        if (!result)
        {
            LDEBUG << "Server not alive " << serverId << ": "
                   << GrpcUtils::convertStatusToString(status);
        }
    }

    return result;
}

bool Server::isRegistered()
{
    return registered;
}

bool Server::isStarted()
{
    return started;
}

bool Server::isClientAlive(std::uint32_t client)
{
    bool result = false;

    boost::lock_guard<boost::shared_mutex> lock(this->stateMutex);
    // The server must be started and responding to messages
    if (this->started)
    {
        LTRACE << "Check if client " << client << " is alive on server " << serverId;
        ClientStatusReq req;
        req.set_clientid(client);
        ClientContext context;
        this->configureClientContext(context);
        ClientStatusRepl repl;
        Status status = this->service->GetClientStatus(&context, req, &repl);
        result = status.ok() && repl.status() == ClientStatusRepl::ALIVE;
        if (!result)
        {
            LDEBUG << "Client " << client << " not alive on server " << serverId
                   << ": " << GrpcUtils::convertStatusToString(status);
        }
    }

    return result;
}

void Server::allocateClientSession(std::uint32_t client,
                                   std::uint32_t session,
                                   const std::string &dbName,
                                   const UserDatabaseRights &dbRights)
{
    auto capabilities = this->getCapability(serverId.c_str(), dbName.c_str(), dbRights);
    allocateClientSession(client, session, dbName, capabilities);
}

void Server::allocateClientSession(std::uint32_t client,
                                   const std::string &username,
                                   const std::string &password,
                                   const std::string &token,
                                   std::uint32_t session,
                                   const std::string &dbName,
                                   const UserDatabaseRights &dbRights)
{
    auto capabilities = this->getCapability(serverId.c_str(), dbName.c_str(),
                                            dbRights, username, password, token);
    allocateClientSession(client, session, dbName, capabilities);
}

void Server::deallocateClientSession(std::uint32_t client,
                                     std::uint32_t session)
{
    // Remove the client session from rasmgr objects.
    // Decrease the session count
    this->dbHost->removeClientSessionFromDB(client, session);
    {
        boost::lock_guard<boost::shared_mutex> listLock(this->sessionMutex);
        sessionOpen = false;
    }
    {
        boost::lock_guard<boost::shared_mutex> stateLock(this->stateMutex);
        this->clientConnected = false;
    }

    // Deallocate on server if the server is started - it may have died from
    // from a segfault for example, in which case the request below is not needed
    if (this->started)
    {
        LDEBUG << "Deallocating client " << client << " on server " << serverId;
        DeallocateClientReq req;
        req.set_clientid(client);
        req.set_sessionid(session);
        ClientContext context;
        this->configureClientContext(context);
        Void repl;
        Status status = this->service->DeallocateClient(&context, req, &repl);
        if (!status.ok())
        {
            LDEBUG << "Failed deallocating client " << client << " on server " << serverId
                   << ": " << GrpcUtils::convertStatusToString(status);
            GrpcUtils::convertStatusToExceptionAndThrow(status);
        }
    }
    else
    {
        LDEBUG << "Deallocating client " << client << " on server " << serverId << " - server not started.";
    }
    
    LDEBUG << "Deallocated client " << client << " on server " << serverId;
}

void Server::registerServer(const std::string &newServerId)
{
    {
        boost::lock_guard<boost::shared_mutex> stateLock(this->stateMutex);
        if (!this->started)
        {
            throw common::RuntimeException("Server not started, cannot register it: " + newServerId);
        }
        if (newServerId != this->serverId)
        {
            throw common::RuntimeException("Server to register " + newServerId +
                                           " does not match the server in rasmgr " + this->serverId);
        }
    }
  
    if (common::SystemUtil::isProcessAlive(processId))
    {
        LDEBUG << "Sending GetServerStatus request to rasserver to register it in rasmgr " << newServerId;
        ServerStatusReq req = ServerStatusReq::default_instance();
        // Set timeout for API
        ClientContext context;
        this->configureClientContext(context);
        ServerStatusRepl repl;
        Status status = this->service->GetServerStatus(&context, req, &repl);
        if (status.ok())
        {
            boost::lock_guard<boost::shared_mutex> stateLock(this->stateMutex);
            this->registered = true;
            LDEBUG << "Ok, server registered.";
        }
        else
        {
            LWARNING << "Failed getting server status for " << newServerId 
                     << " while attempting to register it in rasmgr: "
                     << GrpcUtils::convertStatusToString(status);
            if (networkutils::isAddressValid(hostName, port))
            {
                GrpcUtils::convertStatusToExceptionAndThrow(status);
            }
            else
            {
                throw common::RuntimeException("Server could not be reached at " +
                                               hostName + ":" + std::to_string(port) + 
                                               ", invalid -host  in " RASMGR_CONF_FILE "?");
            }
        }
    }
    else
    {
        throw common::RuntimeException("Process with pid " + std::to_string(processId) + 
                                       " for rasserver " + serverId + " not found.");
    }
}

std::uint32_t Server::getTotalSessionNo()
{
    return this->sessionNo;
}

void Server::sendSignal(int sig) const
{
    errno = 0;
    if (kill(this->processId, sig) != 0)
    {
        LERROR << "Failed to send " << common::SignalHandler::signalName(sig)
               << " to server " << this->serverId << ": " << strerror(errno);
    }
}

void Server::stop(KillLevel level)
{
    LDEBUG << "Stopping server " << serverId << " with level " << KillLevel_Name(level);
    if (common::SystemUtil::isProcessAlive(processId))
    {
        switch (level)
        {
        case KillLevel::FORCE:
        {
            sendSignal(SIGTERM);
        }
        break;

        case KillLevel::KILL:
        {
            sendSignal(SIGTERM);
            bool alive = waitUntilServerExits();
            if (alive)
            {
                LWARNING << "Failed stopping server with PID " << processId 
                         << " cleanly with a SIGTERM, force-stopping it with a SIGKILL signal.";
                sendSignal(SIGKILL);
            }
            boost::unique_lock<boost::shared_mutex> uniqueLock(this->stateMutex);
            LDEBUG << "started set to false";
            this->started = false;
        }
        break;
        
        case KillLevel::NONE:
        {
            // Set timeout for API
            ClientContext context;
            this->configureClientContext(context);
            CloseServerReq req;
            req.set_serverid(serverId);
            Void repl;
            Status status = this->service->Close(&context, req, &repl);
            if (status.ok())
            {
                LDEBUG << "Sent Close request to server: " << serverId;
                usleep(SERVER_CHECK_INTERVAL + 1000);
            }
            else
            {
                LWARNING << "Failed closing server: " << serverId
                         << ": " << GrpcUtils::convertStatusToString(status);
                GrpcUtils::convertStatusToExceptionAndThrow(status);
            }
        }
        break;
            
        default:
            sendSignal(SIGTERM);
            break;
        }
    }
    else
    {
        LDEBUG << "Process with pid " << processId << " for server " << serverId << " not found.";
        this->started = false;
    }
}

bool Server::isStarting()
{
    boost::lock_guard<boost::shared_mutex> stateLock(this->stateMutex);
    return this->started && !this->registered;
}

bool Server::isFree()
{
    LTRACE << "Checking if server " << serverId << " is free";
    boost::lock_guard<boost::shared_mutex> stateLock(this->stateMutex);
    if (!this->registered)
    {
        LDEBUG << "Error, server " << serverId << " not registered.";
        throw common::InvalidStateException("The server " + serverId + " is not registered with rasmgr.");
    }
    if (!this->started)
    {
        LDEBUG << "Error, server " << serverId << " not started.";
        throw common::InvalidStateException("The server " + serverId + " is not started in rasmgr.");
    }
    try
    {
        this->clientConnected = this->getHasClients();
    }
    catch (common::Exception &ex)
    {
        LDEBUG << "Caught exception, server  " << serverId << " is not free: " << ex.what();
        return false;
    }
    catch (...)
    {
        LDEBUG << "Caught exception, server  " << serverId << " is not free.";
        return false;
    }
    LTRACE << "Server " << serverId << " is free: " << !this->clientConnected;
    return !this->clientConnected;
}

bool Server::isAvailable()
{
    LTRACE << "Checking if server " << serverId << " is available";
    boost::lock_guard<boost::shared_mutex> stateLock(this->stateMutex);
    if (!this->registered)
    {
        LDEBUG << "Error, server " << serverId << " not registered.";
        throw common::InvalidStateException("The server " + serverId + " is not registered with rasmgr.");
    }
    if (!this->started)
    {
        LDEBUG << "Error, server " << serverId << " not started.";
        throw common::InvalidStateException("The server " + serverId + " is not started in rasmgr.");
    }
    try
    {
        this->clientConnected = this->getHasClients();
    }
    catch (common::Exception &ex)
    {
        LDEBUG << "Caught exception, server  " << serverId << " is not available: " << ex.what();
        return false;
    }
    catch (...)
    {
        LDEBUG << "Caught exception, server  " << serverId << " is not available.";
        return false;
    }
    LTRACE << "Server  " << serverId << " is available: " << !clientConnected;
    return !this->clientConnected;
}

std::int32_t Server::getPort() const
{
    return this->port;
}

const std::string &Server::getHostName() const
{
    return this->hostName;
}

const std::string &Server::getServerId() const
{
  return this->serverId;
}

void Server::setStarted(bool value)
{
    boost::unique_lock<boost::shared_mutex> stateLock(this->stateMutex);
    this->started = value;
}

std::pair<std::uint32_t, bool> Server::getCurrentClientSession()
{
    boost::shared_lock<boost::shared_mutex> listLock(this->sessionMutex);
    return std::pair<std::uint32_t, bool>(clientId, sessionOpen);
}

bool Server::getHasClients()
{
    LTRACE << "Getting server status for " << serverId;
    ClientContext context;
    this->configureClientContext(context);
    ServerStatusReq req = ServerStatusReq::default_instance();
    ServerStatusRepl repl;
    Status status = this->service->GetServerStatus(&context, req, &repl);
    if (!status.ok())
    {
        LDEBUG << "Failed getting server status for " << serverId << ", throwing exception.";
        GrpcUtils::convertStatusToExceptionAndThrow(status);
    }
    LTRACE << "Server " << serverId << " has clients: " << repl.hasclients();
    return repl.hasclients();
}

std::vector<std::string> Server::getStartProcessCommand()
{
    auto globalConfig = RasMgrConfig::getInstance();
    std::vector<std::string> ret{
        globalConfig->getRasServerExecPath(),
        "--lport" , std::to_string(port),
        "--serverId", this->getServerId(),
        "--mgr", globalConfig->getConnectHostName(),
        "--rsn", this->getServerId(),
        "--mgrport", std::to_string(globalConfig->getRasMgrPort()),
        "--connect", this->dbHost->getConnectString()
    };
    // extra options (following -xp parameter in rasmgr.conf
    auto optsVec = common::StringUtil::split(this->options, ' ');
    for (const auto &opt: optsVec)
      ret.push_back(opt);
    
    return ret;
}

void Server::configureClientContext(grpc::ClientContext &context)
{
    // The server should be able to reply to any call within this window.
    auto deadline = std::chrono::system_clock::now() +
                    std::chrono::milliseconds(SERVER_CALL_TIMEOUT);
    context.set_deadline(deadline);
}

void Server::allocateClientSession(std::uint32_t client,
                                   std::uint32_t session,
                                   const std::string &dbName,
                                   const std::string &capabilities)
{
    // Check if the server is responding to requests before trying to assign it a new client.
    if (!this->isAvailable())
    {
        throw common::RuntimeException("The server cannot accept any new clients, "
                                       "it is serving the maximum number of clients " + 
                                       std::to_string(int(clientConnected)) + " already.");
    }
    
    LDEBUG << "Allocating client " << client << " on server " << serverId;
    ClientContext context;
    this->configureClientContext(context);
    AllocateClientReq req;
    req.set_capabilities(capabilities);
    req.set_clientid(client);
    req.set_sessionid(session);
    Void repl;
    Status status = this->service->AllocateClient(&context, req, &repl);
    if (!status.ok())
    {
        LDEBUG << "Failed allocating client " << client << " on server " << serverId
               << ": " << GrpcUtils::convertStatusToString(status);
        GrpcUtils::convertStatusToExceptionAndThrow(status);
    }
  
    // If everything went well so far, increase the session count for this database
    this->dbHost->addClientSessionOnDB(dbName, client, session);
    {
        boost::lock_guard<boost::shared_mutex> listLock(this->sessionMutex);
        clientId = client;
        sessionId = session;
    }
    {
        boost::lock_guard<boost::shared_mutex> stateLock(this->stateMutex);
        this->clientConnected = true;
    }
  
    // Increase the session counter
    this->sessionNo++;
    LDEBUG << "Allocated client " << client << " on server " << serverId
           << "; session counter: " << this->sessionNo;
}

std::string Server::getCapability(const char *serverName, const char *databaseName,
                                        const UserDatabaseRights &rights)
{
    //Format of Capability (no brackets())
    //$I(userID)$E(effectivRights)$B(databaseName)$T(timeout)$N(serverName)$D(messageDigest)$K

    time_t tmx = time(NULL) + 180;
    tm *b = localtime(&tmx);
    char formattedTime[30];
    sprintf(formattedTime, "%d:%d:%d:%d:%d:%d", b->tm_mday,
            b->tm_mon + 1, b->tm_year + 1900, b->tm_hour, b->tm_min, b->tm_sec);

    auto rString = this->convertDatabRights(rights);
    long userID = 0;

    char capaS[300];
    sprintf(capaS, "$I%ld$E%s$B%s$T%s$N%s",
            userID, rString.c_str(), databaseName, formattedTime, serverName);

    char capaQ[360];
    sprintf(capaQ, "$Canci%s", capaS);
    auto digest = common::Crypto::messageDigest(capaQ, DEFAULT_DIGEST);

    sprintf(capaQ, "%s$D%s$K", capaS, digest.c_str());
    return capaQ;
}

std::string Server::getCapability(const char *serverName, const char *databaseName, 
                                  const UserDatabaseRights &rights,
                                  const std::string &username, const std::string &password,
                                  const std::string &token)
{
    //Format of Capability (no brackets())
    //$I(userID)$E(effectivRights)$B(databaseName)$T(timeout)$N(serverName)$D(messageDigest)$U(username)$P(password)$J(jwtToken)$K

    time_t tmx = time(NULL) + 180;
    tm *b = localtime(&tmx);
    char formattedTime[30];
    sprintf(formattedTime, "%d:%d:%d:%d:%d:%d", 
            b->tm_mday, b->tm_mon + 1, b->tm_year + 1900, b->tm_hour, b->tm_min, b->tm_sec);

    std::string rString = this->convertDatabRights(rights);

    long userID = 0;

    char capaS[300];
    sprintf(capaS, "$I%ld$E%s$B%s$T%s$N%s",
            userID, rString.c_str(), databaseName, formattedTime, serverName);

    char capaQ[1200];
    sprintf(capaQ, "$Canci%s", capaS);

    auto digest = common::Crypto::messageDigest(capaQ, "MD5");

    sprintf(capaQ, "%s$D%s$U%s$P%s$J%s$K",
            capaS, digest.c_str(), username.c_str(), password.c_str(), token.c_str());
    return capaQ;
}

std::string Server::convertDatabRights(const UserDatabaseRights &dbRights)
{
    std::string ret;
    ret += (dbRights.hasReadAccess())  ? 'R' : '.';
    ret += (dbRights.hasWriteAccess()) ? 'W' : '.';
    return ret;
}

bool Server::waitUntilServerExits()
{
  bool alive = true;
  auto cleanupTimeout = SERVER_CLEANUP_TIMEOUT;
  while (cleanupTimeout > 0 && (alive = common::SystemUtil::isProcessAlive(processId)))
  {
      usleep(SERVER_CHECK_INTERVAL);
      cleanupTimeout -= SERVER_CHECK_INTERVAL;
  }
  return alive;
}

}
