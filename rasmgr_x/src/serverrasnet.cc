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

#include <cstring>
#include <iostream>
#include <stdexcept>
#include <chrono>

#include <boost/lexical_cast.hpp>

#include <grpc++/grpc++.h>
#include <grpc++/security/credentials.h>

#include "../../include/globals.hh"
#include "../../common/src/exceptions/rasexceptions.hh"

#include "../../common/src/grpc/grpcutils.hh"
#include "../../common/src/uuid/uuid.hh"
#include <easylogging++.h>

#include "../../rasnet/messages/rassrvr_rasmgr_service.pb.h"

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

using boost::scoped_ptr;
using boost::shared_ptr;
using boost::unique_lock;
using boost::shared_lock;
using boost::shared_mutex;
using boost::lexical_cast;

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

const boost::uint32_t ServerRasNet::SERVER_CLEANUP_TIMEOUT = 30000;

ServerRasNet::ServerRasNet(const ServerConfig &config)
{
    this->hostName = config.getHostName();
    this->port = config.getPort();
    this->dbHost = config.getDbHost();
    this->options = config.getOptions();
    this->serverId = UUID::generateUUID();

    this->registered = false;
    this->started = false;
    this->allocatedClientsNo = 0;
    this->processId = -1;
    this->sessionNo = 0;

    // Initialize the service
    std::string serverAddress = GrpcUtils::constructAddressString(this->hostName, boost::uint32_t(this->port));
    this->service.reset(new ::rasnet::service::RasServerService::Stub(grpc::CreateChannel(serverAddress, grpc::InsecureCredentials())));
}

ServerRasNet::~ServerRasNet()
{
    set<pair<string,string> > ::iterator it;
    for( it= this->sessionList.begin(); it!=this->sessionList.end(); ++it)
    {
        this->dbHost->removeClientSessionFromDB(it->first, it->second);
    }

    this->sessionList.clear();

    //Wait for the process to finish
    if(this->started)
    {
        LWARNING<<"The server process is running."
                <<"Waiting for the process will cause this thread to block.";
    }

    int status;
    int waitOptions = 0;
    waitpid(this->processId, &status, waitOptions);

    LDEBUG<<"RasServer destructed.";
}

void ServerRasNet::startProcess()
{
    unique_lock<shared_mutex> lock(this->stateMtx);

    if(this->started)
    {
        throw common::InvalidStateException("The server process has already been started.");
    }

    lock.unlock();

    this->processId = fork();

    switch(this->processId)
    {
    case -1:
    {
        //Failure
        LERROR<<"Server spawning failed to fork process. Reason:"<<strerror(errno);
        return;
    }
    break;

    case 0:
    {
        //Child process

        std::string command = this->getStartProcessCommand();
        LDEBUG<<"Starting server process with command:"<<command;

        boost::char_separator<char> sep(" \t\r\n");
        boost::tokenizer<boost::char_separator<char> > tokens(command, sep);

        std::vector<std::string> commandVec;

        for ( boost::tokenizer<boost::char_separator<char> >::iterator it = tokens.begin();
                it != tokens.end();
                ++it)
        {
            commandVec.push_back((*it));
        }

        char** commandArr = new char*[commandVec.size()+1];

        for(std::size_t i=0; i<commandVec.size(); ++i)
        {
            commandArr[i] = new char[commandVec[i].size()+1];

            strcpy(commandArr[i], commandVec[i].c_str());

            commandArr[i][commandVec[i].size()]='\0';
        }

        commandArr[commandVec.size()] = (char*)NULL;

        if (execv(RASEXECUTABLE, commandArr)== -1)
        {
            LERROR<<"Starting RasServer  process failed."<<strerror(errno);

            exit(EXIT_FAILURE);
        }
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
    bool result=false;

    unique_lock<shared_mutex> lock(this->stateMtx);
    if(this->started)
    {
        //Remove the process fromt he process table if it has died.
        int status;
        waitpid(this->processId, &status, WNOHANG);

        //Check if the process is still running
        bool isProcessAlive = (kill(this->processId, 0)==0);

        // If the process is alive and the server is responding.
        if(isProcessAlive)
        {
            ServerStatusReq request = ServerStatusReq::default_instance();
            ServerStatusRepl reply;

            // Set timeout for API
            ClientContext context;
            this->configureClientContext(context);

            LDEBUG<<"Check if server:"<<this->serverId<<" is alive";
            Status callStatus = this->service->GetServerStatus(&context, request, &reply);

            //If the communication has not failed, the server is alive
            result= callStatus.ok();
            LDEBUG<<"Server with ID"<<this->serverId<<" alive status:"<<result;
        }
    }

    return result;
}


bool ServerRasNet::isClientAlive(const std::string& clientId)
{
    bool result=false;

    unique_lock<shared_mutex> lock(this->stateMtx);
    // The server must be started and responding to messages
    if(this->started)
    {
        ClientStatusRepl response;
        ClientStatusReq request;

        request.set_clientid(clientId);

        ClientContext context;
        this->configureClientContext(context);

        Status status = this->service->GetClientStatus(&context, request, &response);

        result = (status.ok() && response.status()==ClientStatusRepl::ALIVE);
    }

    return result;
}

void ServerRasNet::allocateClientSession(const std::string& clientId,
        const std::string& sessionId,
        const std::string& dbName,
        const UserDatabaseRights& dbRights)
{
    // Check if the server is responding to requests before trying to assign it a new client.
    if(!this->isAvailable())
    {
        throw common::RuntimeException("The server cannot accept any new clients.");
    }

    AllocateClientReq request;
    Void response;
    DatabaseRights* rights = new DatabaseRights;

    rights->set_read(dbRights.hasReadAccess());
    rights->set_write(dbRights.hasWriteAccess());

    const char* capabilities =  this->getCapability(this->serverId.c_str(), dbName.c_str(), dbRights);
    request.set_capabilities(capabilities);
    request.set_clientid(clientId);
    request.set_sessionid(sessionId);

    ClientContext context;
    this->configureClientContext(context);

    Status status = this->service->AllocateClient(&context, request, &response);

    if(!status.ok())
    {
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
}

void ServerRasNet::deallocateClientSession(const std::string& clientId, const std::string& sessionId)
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
    Status status = this->service->DeallocateClient(&context, request, &response);

    if(!status.ok())
    {
        GrpcUtils::convertStatusToExceptionAndThrow(status);
    }
}

void ServerRasNet::registerServer(const std::string& serverId)
{
    unique_lock<shared_mutex> stateLock(this->stateMtx);
    if(this->started && serverId==this->serverId)
    {
        this->registered=true;
    }
    else
    {
        throw common::RuntimeException("The registration of the server " + serverId + " failed.");
    }
}

boost::uint32_t ServerRasNet::getTotalSessionNo()
{
    return this->sessionNo;
}

void ServerRasNet::stop(KillLevel level)
{
    switch(level)
    {
    case KillLevel::FORCE:
    {
        if(kill(this->processId, SIGTERM))
        {
            LERROR<<"Failed to send SIGTERM to server with ID:"<<this->serverId;
        }
    }
    break;

    case KillLevel::KILL:
    {
        if(kill(this->processId, SIGTERM))
        {
            LERROR<<"Failed to send SIGTERM to server with ID:"<<this->serverId;
        }

        usleep(SERVER_CLEANUP_TIMEOUT);

        if(kill(this->processId, SIGKILL))
        {
            LERROR<<"Failed to send SIGKILL to server with ID:"<<this->serverId;
        }
    }
    break;
    default:

        if(kill(this->processId, SIGTERM))
        {
            LERROR<<"Failed to send SIGTERM to server with ID:"<<this->serverId;
        }
    }

    unique_lock<shared_mutex> lock(this->stateMtx);
    this->started=false;
}

bool ServerRasNet::isStarting()
{
    unique_lock<shared_mutex> stateLock(this->stateMtx);
    return !this->registered;
}

bool ServerRasNet::isFree()
{
    unique_lock<shared_mutex> stateLock(this->stateMtx);
    if(!this->registered || !this->started)
    {
        throw common::InvalidStateException("The server is not registered with rasmgr.");
    }

    if(this->allocatedClientsNo == 0)
    {
        return true;
    }
    else
    {
        this->allocatedClientsNo = this->getClientQueueSize();
        return (this->allocatedClientsNo ==0 );
    }
}

bool ServerRasNet::isAvailable()
{
    unique_lock<shared_mutex> stateLock(this->stateMtx);
    if(!this->registered || !this->started)
    {
        throw common::InvalidStateException("The server is not registered with rasmgr.");
    }
    if(this->allocatedClientsNo < RasMgrConfig::getInstance()->getMaximumNumberOfClientsPerServer())
    {
        return true;
    }
    else
    {
        this->allocatedClientsNo = this->getClientQueueSize();
        return (this->allocatedClientsNo < RasMgrConfig::getInstance()->getMaximumNumberOfClientsPerServer());
    }
}

boost::int32_t ServerRasNet::getPort() const
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

boost::uint32_t ServerRasNet::getClientQueueSize()
{
    ServerStatusReq request = ServerStatusReq::default_instance();
    ServerStatusRepl reply;

    ClientContext context;
    this->configureClientContext(context);

    Status status = this->service->GetServerStatus(&context, request, &reply);

    if(!status.ok())
    {
        GrpcUtils::convertStatusToExceptionAndThrow(status);
    }

    return (reply.clientqueuesize());
}

std::string ServerRasNet::getStartProcessCommand()
{
    //TODO:Factor this out
    shared_ptr<RasMgrConfig> globalConfig = RasMgrConfig::getInstance();
    std::stringstream ss;
    ss<<globalConfig->getRasServerExecPath()<<" ";
    ss<<"--lport"<<" "<<lexical_cast<string>(port)<<" ";
    ss<<"--serverId"<<" "<<this->getServerId()<<" ";
    ss<<"--mgr"<<" "<< globalConfig->getConnectHostName()<<" ";
    ss<<"--rsn"<<" "<<this->getServerId()<<" ";
    ss<<"--mgrport"<<" "<<lexical_cast<string>(globalConfig->getRasMgrPort())<<" ";
    ss<<"--connect"<<" "<<this->dbHost->getConnectString()<<" ";
    ss<<"--rasnet";
    return ss.str();
}

void ServerRasNet::configureClientContext(grpc::ClientContext &context)
{
    // The server should be able to reply to any call within this window.
    std::chrono::system_clock::time_point deadline = std::chrono::system_clock::now() + std::chrono::milliseconds(SERVER_CALL_TIMEOUT);

    context.set_deadline(deadline);
}

const char *ServerRasNet::getCapability(const char *serverName, const char *databaseName, const UserDatabaseRights& rights)
{
    //Format of Capability (no brackets())
    //$I(userID)$E(effectivRights)$B(databaseName)$T(timeout)$N(serverName)$D(messageDigest)$K

    time_t tmx=time(NULL)+180;
    tm *b=localtime(&tmx);
    static char formattedTime[30];
    sprintf(formattedTime,"%d:%d:%d:%d:%d:%d",b->tm_mday,
            b->tm_mon+1,b->tm_year+1900,b->tm_hour,b->tm_min,b->tm_sec);


    const char *rString=this->convertDatabRights(rights);

    long userID=0;

    char capaS[300];
    sprintf(capaS,"$I%ld$E%s$B%s$T%s$N%s",userID,rString,databaseName,formattedTime,serverName);

    static char capaQ[300];
    sprintf(capaQ,"$Canci%s",capaS);

    char digest[50]; // 33 is enough
    messageDigest(capaQ,digest,"MD5");

    sprintf(capaQ,"%s$D%s$K",capaS,digest);

    return capaQ;

}

int ServerRasNet::messageDigest(const char *input, char *output, const char *mdName)
{
    EVP_MD_CTX mdctx;
    const EVP_MD *md;
    unsigned int md_len, i;
    unsigned char md_value[100];

    OpenSSL_add_all_digests();

    md = EVP_get_digestbyname(mdName);

    if(!md)
        return 0;

    EVP_DigestInit(&mdctx, md);
    EVP_DigestUpdate(&mdctx,input, strlen(input));
    EVP_DigestFinal(&mdctx, md_value, &md_len);

    for(i = 0; i < md_len; i++)
        sprintf(output+i+i,"%02x", md_value[i]);

    return strlen(output);
}

const char *ServerRasNet::convertDatabRights(const UserDatabaseRights &dbRights)
{
    static char buffer[20];
    char R= (dbRights.hasReadAccess())  ? 'R':'.';
    char W= (dbRights.hasWriteAccess()) ? 'W':'.';

    sprintf(buffer,"%c%c",R,W);
    return buffer;
}

}
