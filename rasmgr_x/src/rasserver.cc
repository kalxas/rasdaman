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

#include <stdexcept>

#include <boost/lexical_cast.hpp>

#include <google/protobuf/service.h>
#include <google/protobuf/stubs/common.h>

#include "../../common/src/uuid/uuid.hh"
#include "../../common/src/logging/easylogging++.hh"
#include "../../rasnet/src/messages/rassrvr_rasmgr_service.pb.h"
#include "../../rasnet/src/service/client/channel.hh"
#include "../../rasnet/src/service/client/clientcontroller.hh"

#include "rasserver.hh"
#include "rasmgrconfig.hh"

namespace rasmgr
{
using std::runtime_error;
using std::string;
using std::set;
using std::pair;

using boost::scoped_ptr;
using boost::shared_ptr;
using boost::unique_lock;
using boost::shared_lock;
using boost::shared_mutex;
using boost::lexical_cast;

using google::protobuf::Closure;
using google::protobuf::DoNothing;
using google::protobuf::NewPermanentCallback;

using common::UUID;

using rasnet::service::Void;
using rasnet::service::AllocateClientReq;
using rasnet::service::ClientStatusRepl;
using rasnet::service::ClientStatusReq;
using rasnet::service::CloseServerReq;
using rasnet::service::ServerStatusRepl;
using rasnet::service::ServerStatusReq;
using rasnet::service::DeallocateClientReq;
using rasnet::service::RasServerService;
using rasnet::service::RasServerService_Stub;
using rasnet::service::DatabaseRights;
using rasnet::Channel;
using rasnet::ChannelConfig;
using rasnet::ClientController;

#define RASEXECUTABLE BINDIR"rasserver"

RasServer::RasServer(const std::string &hostName, const boost::int32_t &port, boost::shared_ptr<DatabaseHost> dbHost)
{
    this->hostName = "tcp://" + hostName;
    this->port = port;
    this->dbHost = dbHost;
    this->serverId = UUID::generateUUID();
    this->doNothing.reset(NewPermanentCallback(&DoNothing));

    this->registered=false;
    this->started=false;
    this->initializedService=false;
    this->allocatedClientsNo=0;
    this->processId = -1;
}

RasServer::~RasServer()
{
    for(set<pair<string,string> > ::iterator it = this->
            sessionList.begin();
            it!=this->sessionList.end();
            ++it)
    {
        this->dbHost->removeClientSessionFromDB(it->first, it->second);
    }

    this->sessionList.clear();
    int status;
    int options=0;

    waitpid(this->processId,&status, options);
    LDEBUG<<"RasServer destructed.";
}

void RasServer::startProcess()
{
    unique_lock<shared_mutex> lock(this->stateMtx);

    if(this->started)
    {
        throw runtime_error("The process has already been started.");
    }

    this->processId = fork();

    switch(this->processId)
    {
    case -1:
        {
            //Failure
            LERROR<<"Server spawning failed to fork process:"<<strerror(errno);
            return;
        }
        break;
    case 0:
        {
            //TODO-AT:Double check this and test it.
            //Child process
            shared_ptr<RasMgrConfig> config = RasMgrConfig::getInstance();

            if (execl(RASEXECUTABLE,
			config->getRasServerExecPath().c_str(),
                      "--lport", lexical_cast<string>(port).c_str(),
                      "--serverId", this->getServerId().c_str(),
                      "--mgr", config->
                      getConnectHostName().c_str(),
                      "--rsn", this->getServerId().c_str(),
                      "--mgrport", lexical_cast<string>(config->getRasMgrPort()).c_str(),
                      "--connect", this->dbHost->getConnectString().c_str(),
                      "--rasnet" ,(char*)0)
                    == -1)
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
        break;
    }
}

bool RasServer::isAlive()
{
    ClientController controller;
    ServerStatusReq request = ServerStatusReq::default_instance();
    ServerStatusRepl reply;
    bool result=false;

    unique_lock<shared_mutex> lock(this->stateMtx);
    if(this->started)
    {
        //TODO-AT: This is not well defined.
        int status;
        int options;
        waitpid(this->processId,&status, WNOHANG);
        bool isProcessAlive = kill(this->processId, 0)==0;
        if(isProcessAlive)
        {
            LDEBUG<<"Check if server:"<<this->serverId<<" is alive";
            this->getService()->GetServerStatus(&controller, &request, &reply, this->doNothing.get());

            //If the communication has not failed, the server is alive
            result= !controller.Failed();
            LDEBUG<<"Server is alive:"<<result;
        }
        else
        {
            result=false;
        }
    }

    return result;
}


bool RasServer::isClientAlive(const std::string& clientId)
{
    bool result=false;

    unique_lock<shared_mutex> lock(this->stateMtx);
    if(this->started)
    {
        ClientController controller;
        ClientStatusRepl response;
        ClientStatusReq request;

        request.set_clientid(clientId);

        this->getService()->GetClientStatus(&controller, &request, &response, this->doNothing.get());

        if(controller.Failed())
        {
            throw runtime_error(controller.ErrorText());
        }

        result = (response.status()==ClientStatusRepl::ALIVE);
    }

    return result;
}

void RasServer::allocateClientSession(const std::string& clientId,
                                      const std::string& sessionId,
                                      const std::string& dbName,
                                      const UserDatabaseRights& dbRights)
{
    ClientController controller;
    AllocateClientReq request;
    Void response;
    DatabaseRights* rights = new DatabaseRights;

    if(!this->isAvailable())
    {
        throw runtime_error("The server cannot accept any new clients.");
    }

    rights->set_read(dbRights.hasReadAccess());
    rights->set_write(dbRights.hasWriteAccess());

    request.set_clientid(clientId);
    request.set_sessionid(sessionId);
    const char* capabilities =  this->getCapability(this->serverId.c_str(), dbName.c_str(),dbRights);
    request.set_capabilities(capabilities);

    this->getService()->AllocateClient(&controller, &request, &response, this->doNothing.get());

    if(controller.Failed())
    {
        throw runtime_error(controller.ErrorText());
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
}

void RasServer::deallocateClientSession(const std::string& clientId, const std::string& sessionId)
{
    ClientController controller;
    Void response;
    DeallocateClientReq request;

    request.set_clientid(clientId);
    request.set_sessionid(sessionId);

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

    this->getService()->DeallocateClient(&controller, &request, &response, this->doNothing.get());

    if(controller.Failed())
    {
        throw runtime_error(controller.ErrorText());
    }
}

void RasServer::registerServer(const std::string& serverId)
{
    unique_lock<shared_mutex> stateLock(this->stateMtx);
    if(this->started && serverId==this->serverId)
    {
        this->registered=true;
    }
    else
    {
        throw runtime_error("Server registration failed. "+this->serverId+"!="+serverId);
    }
}

void RasServer::stop(bool force)
{
    //TODO-AT: Implement force kill. Check the logic of this class
    // unique_lock<shared_mutex> lock(this->mtx);

    ClientController controller;
    CloseServerReq request;
    Void response;

    if(force)
    {
        //TODO-AT: SIGKILL or SIGTERM? Define a policy for this and document it
        kill(this->processId, SIGKILL);
    }
    else
    {
        request.set_serverid(this->serverId.c_str());

        this->getService()->Close(&controller, &request, &response, this->doNothing.get());
        //If the controller fails, we assume that the server was stopped
    }
}

bool RasServer::isStarting()
{
    unique_lock<shared_mutex> stateLock(this->stateMtx);
    return !this->registered;
}

bool RasServer::isFree()
{
    unique_lock<shared_mutex> stateLock(this->stateMtx);
    if(!this->registered)
    {
        throw runtime_error("The server is not registered with RasMgr.");
    }
    if(this->allocatedClientsNo == 0)
    {
        return true;
    }
    else
    {
        this->allocatedClientsNo = this->getClientQueueSize() ;
        return (this->allocatedClientsNo ==0 );
    }
}

bool RasServer::isAvailable()
{
    unique_lock<shared_mutex> stateLock(this->stateMtx);
    if(!this->registered || !this->started)
    {
        throw runtime_error("The server is not registered with RasMgr.");
    }
    if(this->allocatedClientsNo < RasMgrConfig::getInstance()->getMaximumNumberOfClientsPerServer())
    {
        return true;
    }
    else
    {
        this->allocatedClientsNo = this->getClientQueueSize() ;
        return (this->allocatedClientsNo < RasMgrConfig::getInstance()->getMaximumNumberOfClientsPerServer());
    }
}

boost::int32_t RasServer::getPort() const
{
    return this->port;
}

string RasServer::getHostName() const
{
    return this->hostName;
}

string RasServer::getServerId() const
{
    return this->serverId;
}

boost::uint32_t RasServer::getClientQueueSize()
{
    //Query the server's status and use that to report
    ClientController controller;
    ServerStatusReq request = ServerStatusReq::default_instance();
    ServerStatusRepl reply;

    this->getService()->GetServerStatus(&controller, &request, &reply, this->doNothing.get());

    if(controller.Failed())
    {
        throw runtime_error(controller.ErrorText());
    }
    else
    {
        return (reply.clientqueuesize());
    }
}

boost::shared_ptr<rasnet::service::RasServerService> RasServer::getService()
{
    unique_lock<shared_mutex> lock(this->serviceMtx);
    if(!this->initializedService)
    {
        ChannelConfig config;
        //Rasserver is running on the same machine as rasmgr and should be able to reply
        //to a message in under 100 milliseconds
        config.setConnectionTimeout(100);
        channel.reset(new Channel(this->hostName, this->port, config));
        this->service.reset(new RasServerService_Stub(this->channel.get()));
    }

    return this->service;
}

const char *RasServer::getCapability(const char *serverName, const char *databaseName, const UserDatabaseRights& rights)
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

int RasServer::messageDigest(const char *input, char *output, const char *mdName)
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

const char *RasServer::convertDatabRights(const UserDatabaseRights &dbRights)
{

    static char buffer[20];
    char R= (dbRights.hasReadAccess())  ? 'R':'.';
    char W= (dbRights.hasWriteAccess()) ? 'W':'.';

    sprintf(buffer,"%c%c",R,W);
    return buffer;
}

}
