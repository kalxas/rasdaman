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

#include <unistd.h>
#include <signal.h>
#include <sys/wait.h>
#include <errno.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netdb.h>

#include <cstring>
#include <iostream>
#include <stdexcept>
#include <chrono>

#include <boost/lexical_cast.hpp>
#include <boost/tokenizer.hpp>

#include <grpc++/grpc++.h>
#include <grpc++/security/credentials.h>

#include "include/globals.hh"
#include "common/exceptions/rasexceptions.hh"

#include "common/grpc/grpcutils.hh"
#include "common/uuid/uuid.hh"
#include "common/logging/signalhandler.hh"
#include <logging.hh>

#include "rasnet/messages/rassrvr_rasmgr_service.pb.h"

#include "exceptions/rasmgrexceptions.hh"

#include "constants.hh"
#include "serverrasnet.hh"
#include "rasmgrconfig.hh"
#include "userdatabaserights.hh"

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

ServerRasNet::ServerRasNet(const ServerConfig &config)
{
    this->hostName = config.getHostName();
    this->port = static_cast<int32_t>(config.getPort());
    this->dbHost = config.getDbHost();
    this->options = config.getOptions();
    this->serverId = UUID::generateUUID();

    this->registered = false;
    this->started = false;
    this->allocatedClientsNo = 0;
    this->processId = -1;
    this->sessionNo = 0;

    // Initialize the service
    std::string serverAddress = GrpcUtils::constructAddressString(this->hostName, std::uint32_t(this->port));
    this->service.reset(new ::rasnet::service::RasServerService::Stub(grpc::CreateChannel(serverAddress, grpc::InsecureChannelCredentials())));
}

ServerRasNet::~ServerRasNet()
{
    set<pair<string, string>> ::iterator it;
    for (it = this->sessionList.begin(); it != this->sessionList.end(); ++it)
    {
        this->dbHost->removeClientSessionFromDB(it->first, it->second);
    }

    this->sessionList.clear();

    //Wait for the process to finish
    if (this->started)
    {
        LWARNING << "The server process is running; "
                 << "waiting for the process will cause this thread to block.";
    }

    int status;
    int waitOptions = 0;
    waitpid(this->processId, &status, waitOptions);

    LDEBUG << "Rasserver destructed: " << serverId;
}

void ServerRasNet::startProcess()
{
    unique_lock<shared_mutex> lock(this->stateMtx);

    if (this->started)
    {
        throw common::InvalidStateException("The server process has already been started.");
    }

    lock.unlock();

    std::string command = this->getStartProcessCommand();
    LDEBUG << "Starting server process " << serverId << " with command: " << command;

    this->processId = fork();

    switch (this->processId)
    {
    case -1:
    {
        //Failure
        LERROR << "Server spawning failed to fork process. Reason: " << strerror(errno);
        return;
    }
    break;

    case 0:
    {
        //Child process

        boost::char_separator<char> sep(" \t\r\n");
        boost::tokenizer<boost::char_separator<char>> tokens(command, sep);

        std::vector<std::string> commandVec;

        for (auto it = tokens.begin(); it != tokens.end(); ++it)
        {
            commandVec.push_back((*it));
        }

        char **commandArr = new char *[commandVec.size() + 1];

        for (std::size_t i = 0; i < commandVec.size(); ++i)
        {
            commandArr[i] = new char[commandVec[i].size() + 1];
            strcpy(commandArr[i], commandVec[i].c_str());
            commandArr[i][commandVec[i].size()] = '\0';
        }

        commandArr[commandVec.size()] = (char *)NULL;

        if (execv(RASEXECUTABLE, commandArr) == -1)
        {
            LERROR << "Starting server process failed: " << strerror(errno);
            LERROR << "Failed command: " << command;
            exit(EXIT_FAILURE);
        }

        // free
        for (std::size_t i = 0; i < commandVec.size(); ++i)
        {
            free(commandArr[i]);
            commandArr[i] = NULL;
        }
        delete [] commandArr;
        commandArr = NULL;
    }
    break;

    default:
    {
        //Parent process
        this->started = true;
    }
    }
}

bool ServerRasNet::isAlive()
{
    //Assume the process is dead
    bool result = false;

    unique_lock<shared_mutex> lock(this->stateMtx);
    if (this->started && isProcessAlive())
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
            LDEBUG << "Failed getting status from server " << serverId
                   << ", error: " << status.error_message();
        else
        {
            LDEBUG << "Server " << this->serverId << " is alive.";
        }
    }

    return result;
}


bool ServerRasNet::isClientAlive(const std::string &clientId)
{
    bool result = false;

    unique_lock<shared_mutex> lock(this->stateMtx);
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
        throw common::RuntimeException("The server cannot accept any new clients.");
    }

    AllocateClientReq request;
    Void response;
    DatabaseRights *rights = new DatabaseRights;

    rights->set_read(dbRights.hasReadAccess());
    rights->set_write(dbRights.hasWriteAccess());

    const char *capabilities =  this->getCapability(this->serverId.c_str(), dbName.c_str(), dbRights);
    request.set_capabilities(capabilities);
    request.set_clientid(clientId);
    request.set_sessionid(sessionId);

    ClientContext context;
    this->configureClientContext(context);

    LDEBUG << "Allocating client " << clientId << " on server " << serverId;
    Status status = this->service->AllocateClient(&context, request, &response);

    if (!status.ok())
    {
        LDEBUG << "Failed allocating client " << clientId;
        GrpcUtils::convertStatusToExceptionAndThrow(status);
    }

    //If everything went well so far, increase the session count for this database
    this->dbHost->addClientSessionOnDB(dbName, clientId, sessionId);

    {
        unique_lock<shared_mutex> listLock(this->sessionMtx);
        this->sessionList.insert(std::make_pair(clientId, sessionId));
    }

    {
        unique_lock<shared_mutex> stateLock(this->stateMtx);
        this->allocatedClientsNo++;
    }

    //Increase the session counter
    this->sessionNo++;

    LDEBUG << "Allocated client " << clientId << " on server " << serverId
           << "; session counter: " << this->sessionNo;
}

void ServerRasNet::deallocateClientSession(const std::string &clientId, const std::string &sessionId)
{
    // Remove the client session from rasmgr objects.
    //Decrease the session count
    this->dbHost->removeClientSessionFromDB(clientId, sessionId);

    {
        unique_lock<shared_mutex> listLock(this->sessionMtx);
        this->sessionList.erase(std::make_pair(clientId, sessionId));
    }

    {
        unique_lock<shared_mutex> stateLock(this->stateMtx);
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
    unique_lock<shared_mutex> stateLock(this->stateMtx);
    if (this->started && serverId == this->serverId)
    {
        if (isProcessAlive())
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
                if (this->isAddressValid())
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
    if (isProcessAlive())
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
            while (cleanupTimeout > 0 && isProcessAlive())
            {
                usleep(SERVER_CHECK_INTERVAL);
                cleanupTimeout -= SERVER_CHECK_INTERVAL;
            }

            // if the server is still alive after SERVER_CLEANUP_TIMEOUT, send a SIGKILL
            if (isProcessAlive())
            {
                LDEBUG << "Stopping server with a SIGKILL signal.";
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

    unique_lock<shared_mutex> lock(this->stateMtx);
    this->started = false;
}

bool ServerRasNet::isStarting()
{
    unique_lock<shared_mutex> stateLock(this->stateMtx);
    return !this->registered;
}

bool ServerRasNet::isFree()
{
    LDEBUG << "Checking if server " << serverId << " is free";
    unique_lock<shared_mutex> stateLock(this->stateMtx);
    if (!this->registered || !this->started)
    {
        LDEBUG << "Error, server " << serverId << " not registered or not started.";
        throw common::InvalidStateException("The server is not registered with rasmgr.");
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
    unique_lock<shared_mutex> stateLock(this->stateMtx);
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
    const auto ret = (this->allocatedClientsNo < RasMgrConfig::getInstance()->getMaximumNumberOfClientsPerServer());
    LDEBUG << "Server  " << serverId << " is available: " << ret;
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
    return (reply.clientqueuesize());
}

std::string ServerRasNet::getStartProcessCommand()
{
    //TODO:Factor this out
    auto globalConfig = RasMgrConfig::getInstance();
    std::stringstream ss;
    ss << globalConfig->getRasServerExecPath() << " ";
    ss << "--lport" << " " << std::to_string(port) << " ";
    ss << "--serverId" << " " << this->getServerId() << " ";
    ss << "--mgr" << " " << globalConfig->getConnectHostName() << " ";
    ss << "--rsn" << " " << this->getServerId() << " ";
    ss << "--mgrport" << " " << std::to_string(globalConfig->getRasMgrPort()) << " ";
    ss << "--connect" << " " << this->dbHost->getConnectString();
    // add extra options (following -xp parameter in rasmgr.conf)
    ss << " " << this->options;
    return ss.str();
}

void ServerRasNet::configureClientContext(grpc::ClientContext &context)
{
    // The server should be able to reply to any call within this window.
    std::chrono::system_clock::time_point deadline = std::chrono::system_clock::now() + std::chrono::milliseconds(SERVER_CALL_TIMEOUT);

    context.set_deadline(deadline);
}

const char *ServerRasNet::getCapability(const char *serverName, const char *databaseName, const UserDatabaseRights &rights)
{
    //Format of Capability (no brackets())
    //$I(userID)$E(effectivRights)$B(databaseName)$T(timeout)$N(serverName)$D(messageDigest)$K

    time_t tmx = time(NULL) + 180;
    tm *b = localtime(&tmx);
    static char formattedTime[30];
    sprintf(formattedTime, "%d:%d:%d:%d:%d:%d", b->tm_mday,
            b->tm_mon + 1, b->tm_year + 1900, b->tm_hour, b->tm_min, b->tm_sec);


    const char *rString = this->convertDatabRights(rights);

    long userID = 0;

    char capaS[300];
    sprintf(capaS, "$I%ld$E%s$B%s$T%s$N%s", userID, rString, databaseName, formattedTime, serverName);

    static char capaQ[360];
    sprintf(capaQ, "$Canci%s", capaS);

    char digest[50]; // 33 is enough
    messageDigest(capaQ, digest, DEFAULT_DIGEST);

    sprintf(capaQ, "%s$D%s$K", capaS, digest);

    return capaQ;
}

int ServerRasNet::messageDigest(const char *input, char *output, const char *mdName)
{
    const EVP_MD *md;
    unsigned int md_len, i;
    unsigned char md_value[100];

    OpenSSL_add_all_digests();

    md = EVP_get_digestbyname(mdName);

    if (!md)
    {
        return 0;
    }

#if OPENSSL_VERSION_NUMBER < 0x10100000L
    EVP_MD_CTX mdctx;
    EVP_DigestInit(&mdctx, md);
    EVP_DigestUpdate(&mdctx, input, strlen(input));
    EVP_DigestFinal(&mdctx, md_value, &md_len);
#else
    EVP_MD_CTX *mdctx = EVP_MD_CTX_new();
    EVP_DigestInit(mdctx, md);
    EVP_DigestUpdate(mdctx, input, strlen(input));
    EVP_DigestFinal(mdctx, md_value, &md_len);
    EVP_MD_CTX_free(mdctx);
#endif

    for (i = 0; i < md_len; i++)
    {
        sprintf(output + i + i, "%02x", md_value[i]);
    }

    return static_cast<int>(strlen(output));
}

const char *ServerRasNet::convertDatabRights(const UserDatabaseRights &dbRights)
{
    static char buffer[20];
    char R = (dbRights.hasReadAccess())  ? 'R' : '.';
    char W = (dbRights.hasWriteAccess()) ? 'W' : '.';

    sprintf(buffer, "%c%c", R, W);
    return buffer;
}

bool ServerRasNet::isProcessAlive() const
{
    int status;
    waitpid(processId, &status, WNOHANG) == 0;
    return kill(processId, 0) == 0;
}

bool ServerRasNet::isAddressValid() const
{
    struct addrinfo *addr = NULL;
    std::string portStr = std::to_string(port);
    int ret = getaddrinfo(hostName.c_str(), portStr.c_str(), NULL, &addr);
    if (addr)
    {
        freeaddrinfo(addr);
        addr = NULL;
    }
    return ret == 0;
}

}
