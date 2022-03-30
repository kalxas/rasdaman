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

#include "include/globals.hh"
#include "common/exceptions/rasexceptions.hh"
#include "common/grpc/grpcutils.hh"
#include "common/uuid/uuid.hh"
#include "common/crypto/crypto.hh"
#include "common/logging/signalhandler.hh"
#include "common/util/networkutils.hh"
#include "common/util/system.hh"
#include "common/util/vectorutils.hh"
#include "common/string/stringutil.hh"
#include <logging.hh>

#include "rasnet/messages/rassrvr_rasmgr_service.pb.h"

#include "exceptions/rasmgrexceptions.hh"

#include "constants.hh"
#include "serverrasnet.hh"
#include "rasmgrconfig.hh"
#include "userdatabaserights.hh"

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
using std::string;
using std::set;
using std::pair;
using std::runtime_error;

using std::shared_ptr;
using std::unique_lock;
using boost::shared_lock;
using boost::shared_mutex;

using grpc::Channel;
using grpc::ClientContext;
using grpc::Status;

using common::UUID;
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
const std::int32_t ServerRasNet::SERVER_CLEANUP_TIMEOUT = 3000000;
// 10 milliseconds
const std::int32_t ServerRasNet::SERVER_CHECK_INTERVAL = 10000;

ServerRasNet::ServerRasNet(const ServerConfig &config):
  hostName{config.getHostName()}, port(int32_t(config.getPort())), dbHost(config.getDbHost()),
  options{config.getOptions()}, processId{-1}, serverId{UUID::generateUUID()},
  registered{false}, allocatedClientsNo{0}, started{false}, sessionNo{0}
{
    // Initialize the service
    std::string serverAddress = GrpcUtils::constructAddressString(this->hostName, std::uint32_t(this->port));
    this->service.reset(new ::rasnet::service::RasServerService::Stub(
                          grpc::CreateChannel(serverAddress, grpc::InsecureChannelCredentials())));
}

ServerRasNet::~ServerRasNet()
{
    boost::lock_guard<boost::shared_mutex> lock(sessionListMutex);
    for (const auto &s: this->sessionList)
    {
        this->dbHost->removeClientSessionFromDB(s.first, s.second);
    }
    this->sessionList.clear();

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

void ServerRasNet::startProcess()
{
    {
        boost::lock_guard<shared_mutex> lock(this->stateMutex);
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
        LDEBUG << "Server process started: " << serverId;
        this->started = true;
    }
    }
}

bool ServerRasNet::isAlive()
{
    //Assume the process is dead
    bool result = false;

    boost::lock_guard<shared_mutex> lock(this->stateMutex);
    if (this->started && common::SystemUtil::isProcessAlive(processId))
    {
        // If the process is alive and the server is responding.
        ServerStatusReq request = ServerStatusReq::default_instance();
        ServerStatusRepl reply;

        // Set timeout for API
        ClientContext context;
        this->configureClientContext(context);

        LDEBUG << "Check if server " << this->serverId << " is alive";
        Status status = this->service->GetServerStatus(&context, request, &reply);

        //If the communication has not failed, the server is alive
        result = status.ok();
        if (!result)
        {
            LDEBUG << "Failed getting status from server " << serverId
                   << ", error: " << status.error_message();
        }
        else
        {
            LDEBUG << "Server " << serverId << " is alive.";
        }
    }

    return result;
}

bool ServerRasNet::isClientAlive(const std::string &clientId)
{
    bool result = false;

    boost::lock_guard<shared_mutex> lock(this->stateMutex);
    // The server must be started and responding to messages
    if (this->started)
    {
        ClientStatusRepl response;
        ClientStatusReq request;
        request.set_clientid(clientId);
        ClientContext context;
        this->configureClientContext(context);

        LDEBUG << "Check if client " << clientId << " is alive";
        Status status = this->service->GetClientStatus(&context, request, &response);

        result = (status.ok() && response.status() == ClientStatusRepl::ALIVE);
        if (!result)
            LDEBUG << "Failed getting status from client " << clientId
                   << ", error: " << status.error_message();
        else
        {
            LDEBUG << "Client " << clientId << " is alive.";
        }
    }

    return result;
}

void ServerRasNet::allocateClientSession(const std::string &clientId,
                                         const std::string &sessionId,
                                         const std::string &dbName,
                                         const UserDatabaseRights &dbRights)
{
    // Check if the server is responding to requests before trying to assign it a new client.
    if (!this->isAvailable())
    {
        throw common::RuntimeException("The server cannot accept any new clients, "
                                       "it is serving the maximum number of clients " + 
                                       std::to_string(allocatedClientsNo) + " already.");
    }

    AllocateClientReq request;
    Void response;
     
    auto capabilities =  this->getCapability(serverId.c_str(), dbName.c_str(), dbRights);
    request.set_capabilities(capabilities);
    request.set_clientid(clientId);
    request.set_sessionid(sessionId);

    ClientContext context;
    this->configureClientContext(context);

    LDEBUG << "Allocating client " << clientId << " on server " << serverId;
    Status status = this->service->AllocateClient(&context, request, &response);

    if (!status.ok())
    {
        LDEBUG << "Failed allocating client " << clientId << " on server " << serverId;
        GrpcUtils::convertStatusToExceptionAndThrow(status);
    }

    //If everything went well so far, increase the session count for this database
    this->dbHost->addClientSessionOnDB(dbName, clientId, sessionId);
    {
        boost::lock_guard<shared_mutex> listLock(this->sessionListMutex);
        this->sessionList.insert(std::make_pair(clientId, sessionId));
    }
    {
        boost::lock_guard<shared_mutex> stateLock(this->stateMutex);
        this->allocatedClientsNo++;
    }

    //Increase the session counter
    this->sessionNo++;

    LDEBUG << "Allocated client " << clientId << " on server " << serverId
           << "; session counter: " << this->sessionNo;
}

void ServerRasNet::deallocateClientSession(const std::string &clientId,
                                           const std::string &sessionId)
{
    // Remove the client session from rasmgr objects.
    //Decrease the session count
    this->dbHost->removeClientSessionFromDB(clientId, sessionId);
    {
        boost::lock_guard<shared_mutex> listLock(this->sessionListMutex);
        this->sessionList.erase(std::make_pair(clientId, sessionId));
    }
    {
        boost::lock_guard<shared_mutex> stateLock(this->stateMutex);
        this->allocatedClientsNo--;
    }

    // Check if the server is alive
    Void response;
    DeallocateClientReq request;
    request.set_clientid(clientId);
    request.set_sessionid(sessionId);

    ClientContext context;
    this->configureClientContext(context);
    LDEBUG << "Deallocating client " << clientId << " on server " << serverId;
    Status status = this->service->DeallocateClient(&context, request, &response);

    if (!status.ok())
    {
        LDEBUG << "Failed deallocating client " << clientId << " on server " << serverId;
        GrpcUtils::convertStatusToExceptionAndThrow(status);
    }
    LDEBUG << "Deallocated client" << clientId << " on server " << serverId;
}

void ServerRasNet::registerServer(const std::string &serverId)
{
    boost::lock_guard<shared_mutex> stateLock(this->stateMutex);
    if (this->started && serverId == this->serverId)
    {
        if (common::SystemUtil::isProcessAlive(processId))
        {
            ServerStatusReq request = ServerStatusReq::default_instance();
            ServerStatusRepl reply;
            // Set timeout for API
            ClientContext context;
            this->configureClientContext(context);

            Status status = this->service->GetServerStatus(&context, request, &reply);
            if (status.ok())
            {
                this->registered = true;
            }
            else
            {
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
            throw common::RuntimeException("Server process with pid " + std::to_string(processId) + " not found.");
        }
    }
    else
    {
        throw common::RuntimeException("The registration of server " + serverId + " failed.");
    }
}

std::uint32_t ServerRasNet::getTotalSessionNo()
{
    return this->sessionNo;
}

void ServerRasNet::sendSignal(int sig) const
{
    errno = 0;
    if (kill(this->processId, sig) != 0)
    {
        LERROR << "Failed to send " << common::SignalHandler::signalName(sig)
               << " to server " << this->serverId << ": " << strerror(errno);
    }
}

void ServerRasNet::stop(KillLevel level)
{

    LDEBUG << "Stopping server " << serverId;
    if (common::SystemUtil::isProcessAlive(processId))
    {
        switch (level)
        {
        case KillLevel::FORCE:
            sendSignal(SIGTERM);
            break;

        case KillLevel::KILL:
        {
            sendSignal(SIGTERM);

            // wait until the server process is dead
            std::int32_t cleanupTimeout = SERVER_CLEANUP_TIMEOUT;
            while (cleanupTimeout > 0 && common::SystemUtil::isProcessAlive(processId))
            {
                usleep(SERVER_CHECK_INTERVAL);
                cleanupTimeout -= SERVER_CHECK_INTERVAL;
            }

            // if the server is still alive after SERVER_CLEANUP_TIMEOUT, send a SIGKILL
            if (common::SystemUtil::isProcessAlive(processId))
            {
                LWARNING << "Failed stopping server with PID " << processId 
                         << " cleanly with a SIGTERM, force-stopping it with a SIGKILL signal.";
                sendSignal(SIGKILL);
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
    }

    boost::lock_guard<shared_mutex> lock(this->stateMutex);
    this->started = false;
}

bool ServerRasNet::isStarting()
{
    boost::lock_guard<shared_mutex> stateLock(this->stateMutex);
    return this->started && !this->registered;
}

bool ServerRasNet::isFree()
{
    LDEBUG << "Checking if server " << serverId << " is free";
    boost::lock_guard<shared_mutex> stateLock(this->stateMutex);
    if (!this->registered || !this->started)
    {
        LDEBUG << "Error, server " << serverId << " not registered or not started.";
        throw common::InvalidStateException("The server " + serverId +
                                            " is not started or not registered with rasmgr.");
    }
    try
    {
        this->allocatedClientsNo = this->getClientQueueSize();
    }
    catch (std::exception &ex)
    {
        LDEBUG << "Caught exception, server  " << serverId << " is not free: " << ex.what();
        return false;
    }
    catch (...)
    {
        LDEBUG << "Caught exception, server  " << serverId << " is not free.";
        return false;
    }
    const auto ret = (this->allocatedClientsNo == 0);
    LDEBUG << "Server " << serverId << " is free: " << ret;
    return ret;
}

bool ServerRasNet::isAvailable()
{
    LDEBUG << "Checking if server " << serverId << " is available";
    boost::lock_guard<shared_mutex> stateLock(this->stateMutex);
    if (!this->registered || !this->started)
    {
        LDEBUG << "Error, server " << serverId << " not registered or not started.";
        throw common::InvalidStateException("The server is not registered with rasmgr or is not started.");
    }
    try
    {
        this->allocatedClientsNo = this->getClientQueueSize();
    }
    catch (std::exception &ex)
    {
        LDEBUG << "Caught exception, server  " << serverId << " is not available: " << ex.what();
        return false;
    }
    catch (...)
    {
        LDEBUG << "Caught exception, server  " << serverId << " is not available.";
        return false;
    }
    const auto maxClientsPerServer = RasMgrConfig::getInstance()->getMaximumNumberOfClientsPerServer();
    const auto ret = this->allocatedClientsNo < maxClientsPerServer;
    LDEBUG << "Server  " << serverId << " has allocated " << allocatedClientsNo
           << "/" << maxClientsPerServer << "; is it available: " << ret;
    return ret;
}

std::int32_t ServerRasNet::getPort() const
{
    return this->port;
}

string ServerRasNet::getHostName() const
{
    return this->hostName;
}

string ServerRasNet::getServerId() const
{
    return this->serverId;
}

std::uint32_t ServerRasNet::getClientQueueSize()
{
    ServerStatusReq request = ServerStatusReq::default_instance();
    ServerStatusRepl reply;

    ClientContext context;
    this->configureClientContext(context);

    LDEBUG << "Getting server status for " << serverId;
    Status status = this->service->GetServerStatus(&context, request, &reply);

    if (!status.ok())
    {
        LDEBUG << "Failed getting server status for " << serverId << ", throwing exception.";
        GrpcUtils::convertStatusToExceptionAndThrow(status);
    }

    LDEBUG << "Client queue size for " << serverId << ": " << reply.clientqueuesize();
    return reply.clientqueuesize();
}

std::vector<std::string> ServerRasNet::getStartProcessCommand()
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

void ServerRasNet::configureClientContext(grpc::ClientContext &context)
{
    // The server should be able to reply to any call within this window.
    auto deadline = std::chrono::system_clock::now() + std::chrono::milliseconds(SERVER_CALL_TIMEOUT);
    context.set_deadline(deadline);
}

std::string ServerRasNet::getCapability(const char *serverName, const char *databaseName,
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

std::string ServerRasNet::convertDatabRights(const UserDatabaseRights &dbRights)
{
    string ret;
    ret += (dbRights.hasReadAccess())  ? 'R' : '.';
    ret += (dbRights.hasWriteAccess()) ? 'W' : '.';
    return ret;
}

}
