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
/**
 * SOURCE: rasmgr_host.cc
 *
 * MODULE: rasmgr
 * CLASS:  ServerHost, HostManager
 *
 * COMMENTS:
 *
*/

#include "config.h"
#include "rasmgr_host.hh"
#include "rasmgr_srv.hh"
#include "rasmgr_master.hh"

#include "debug-srv.hh"
#include <easylogging++.h>

#ifdef SOLARIS
#define PORTMAP        // define to use function declarations for old interfaces
#include<rpc/rpc.h>
#include<rpc/pmap_clnt.h>
#else
#include<rpc/rpc.h>
#include<rpc/pmap_clnt.h>
#endif

typedef unsigned long r_Ptr;

//int _rpcpmstart = 0;

// function prototype with C linkage
//extern "C" int gethostname(char *name, int namelen);

extern bool hostCmp( const char *h1, const char *h2);


ServerHost::ServerHost()
{
    hostName[0]=0;
    netwName[0]=0;
    setNotUp();
    isinternal=false;
    valid=false;
    startedServers=0;
    isuseLocalHost=true;
    //LDEBUG << "ServerHost::ServerHost(): hostName=" << this->hostName << ", netwName=" << this->netwName << ", listenPort=" << this->listenPort << ", isinternal=" << this->isinternal << ", valid=" << valid;
}

ServerHost::~ServerHost()
{
}

bool ServerHost::isValid() // used for the protection element
{
    LDEBUG << "ServerHost::isValid() -> " << valid;
    return valid;
}

const char* ServerHost::getName()
{
    return hostName;
}

const char* ServerHost::getNetworkName()
{
    return netwName;
}

long ServerHost::getListenPort()
{
    return listenPort;
}

bool  ServerHost::isInternal()
{
    return isinternal;
}

void  ServerHost::init(const char* newHostName,const char* newNetwName,int newListenPort,bool newIsinternal)
{
    strcpy(this->hostName,newHostName);
    strcpy(this->netwName,newNetwName);

    this->listenPort=newListenPort;
    this->isinternal=newIsinternal;

    isup = newIsinternal;

    valid=true;
    LDEBUG << "ServerHost::init(): hostName=" << this->hostName << ", netwName=" << this->netwName << ", listenPort=" << this->listenPort << ", isinternal=" << this->isinternal << ", valid=" << valid;
}

char* ServerHost::getDescriptionHeader(char *destBuffer)
{
    sprintf(destBuffer,"    %-10s %-32s  %-4s  %-4s   %-10s  ulh","Host Name","Netw. Addr","Port","Stat","Servers");
    return destBuffer;
}
char* ServerHost::getDescription(char *destBuffer)
{
    const char* sUp= isup ? "UP  ":"DOWN";
    const char* uLh= " -";
    long lPort = listenPort;

    if(isinternal)
    {
        uLh = useLocalHost() ? "on":"off";
        lPort = config.getListenPort();
    }

    sprintf(destBuffer,"%-10s %-32s  %4ld  %-4s  %2d   %s",hostName,netwName,lPort,sUp,startedServers,uLh);

    return destBuffer;
}

bool ServerHost::isUp()
{
    return isup;
}

bool  ServerHost::downHost()
{
    // you can't stop the master with this
    if(isinternal)
        return false;

    int socket=getConnectionSocket();

    if(socket<0)
    {
        setNotUp();
        //close(socket);
        return false;
    }

    const char *text="POST downhost HTTP/1.1\r\nAccept: text/plain\r\nUserAgent: RasMGR/1.0\r\n\r\nRasMGR";

    int nbytes=write(socket,text,strlen(text)+1);

    isup=false;

    return true;
}

bool ServerHost::checkStatus()
{
    if(isinternal)
    {
        isup=true;
        return true;
    }

    int socket=getConnectionSocket();

    if(socket<0)
    {
        setNotUp();
        //close(socket);
        LDEBUG << "ServerHost::checkStatus() -> false (no socket)";
        return false;
    }
    const char *text="POST getstatus HTTP/1.1\r\nAccept: text/plain\r\nUserAgent: RasMGR/1.0\r\n\r\nRasMGR";

    int nbytes=write(socket,text,strlen(text)+1);

    if(nbytes<0)
    {
        setNotUp();
        close(socket);
        LDEBUG << "ServerHost::checkStatus() -> false (cannot write socket)";
        return false;
    }

    char message[200];
    nbytes=read(socket,message,200);
    close(socket);
    if(nbytes<0)
    {
        setNotUp();
        LDEBUG << "ServerHost::checkStatus() -> false (cannot read socket)" ;
        return false;
    }

    char *body=strstr(message,"\r\n\r\n");
    if(body==NULL)
    {
        setNotUp();
        LDEBUG << "ServerHost::checkStatus() -> false (null body)";
        return false;
    }

    int localStartedServers; //not sure that we will provide this info
    sscanf(body,"%d ",&localStartedServers);

    isup=true;

    LDEBUG << "ServerHost::checkStatus() -> true (all done)";
    return true;
} // checkStatus()

int   ServerHost::countDefinedServers()
{
    LDEBUG << "ServerHost::countDefinedServers enter.";

    int count=0;
    for(int i=0; i<rasManager.countServers(); i++)
    {
        if(hostCmp(rasManager[i].getHostName(),hostName))
            count++;
    }

    LDEBUG << "ServerHost::countDefinedServers leave -> " << count;
    return count;
}

int   ServerHost::getConnectionSocket()
{
    LDEBUG << "ServerHost::getConnectionSocket enter.";

    // create socket
    int sock;
    struct protoent *getprotoptr;
    getprotoptr=getprotobyname("tcp");
    sock=socket(PF_INET,SOCK_STREAM,getprotoptr->p_proto);
    int tempErrno = errno;
    LDEBUG << "ServerHost::getConnectionSocket socket(PF_INET,SOCK_STREAM," << getprotoptr->p_proto << ") -> " << sock;
    if (sock < 0)
    {
        LDEBUG << "ServerHost::getConnectionSocket cannot create socket: " << strerror( tempErrno );
        return -1;
    }

    // init sockaddr
    sockaddr_in servername;
    struct hostent *hostinfo;
    servername.sin_family=AF_INET;
    servername.sin_port=htons(listenPort);

    // try to resolve address - either by name or IP address -- PB 2007-jun-25
    LDEBUG << "ServerHost::getConnectionSocket: trying to resolve name " << netwName << " ... ";
    hostinfo=gethostbyname(netwName);
    if(hostinfo==NULL)
    {
        close(sock);
        LDEBUG << "ServerHost::getConnectionSocket leave (invalid hostinfo) -> -1";
        return -1;
    }
    servername.sin_addr=*(struct in_addr*)hostinfo->h_addr;

    // connect to slave-manager
    int connectResult = connect(sock,(struct sockaddr*)&servername,sizeof(servername));
    LDEBUG << "ServerHost::getConnectionSocket connect() to host=" << hostinfo->h_name << ", listen port=" << servername.sin_port << " -> " << connectResult;
    if(connectResult < 0)
    {
        close(sock);
        if (errno)
        {
            tempErrno = errno;
            LDEBUG << "ServerHost::getConnectionSocket close(" << sock << ") -> " << strerror(tempErrno);
        }
        LDEBUG << "ServerHost::getConnectionSocket leave (cannot connect to slave mgr " << hostinfo << ") -> -1";
        return -1;
    }

    LDEBUG << "ServerHost::getConnectionSocket leave -> " << sock;
    return sock;
}

void ServerHost::setNotUp()
{
    isup=false;
    startedServers=0;
}

void ServerHost::setIsUp(bool x)
{
    isup=x;
}

void ServerHost::regStartServer()
{
    startedServers++;
}

void ServerHost::regDownServer()
{
    startedServers--;
}

int ServerHost::getStartedServers()
{
    return startedServers;
}

void  ServerHost::useLocalHost(bool how)
{
    isuseLocalHost=how;
}

bool  ServerHost::useLocalHost()
{
    return isinternal ? isuseLocalHost : false;
}

void  ServerHost::changeName(const char *newName)
{
    strcpy(hostName,newName);
}

void  ServerHost::changeNetName(const char *newNetName)
{
    strcpy(netwName,newNetName);
}
void  ServerHost::changeListenPort(int newListenPort)
{
    listenPort=newListenPort;
}

//**********************************************************************
HostManager::HostManager()
{
    //LDEBUG << "HostManager::HostManager()";
}

HostManager::~HostManager()
{
    LDEBUG << "HostManager::~HostManager()";
}

bool HostManager::insertInternalHost()
{
    bool result = false;    // function result

    ServerHost tempServerHost;

    if(hostList.empty()==false)
        result = false;
    else
    {
        //put the internal host, which is always defined, even if it has no servers attached
        hostList.push_back(tempServerHost);
        ServerHost &refServerHost=hostList.back();
        const char *myhostName=config.getHostName();
        const char *myPublicHostName=config.getPublicHostName();
        refServerHost.init(myhostName,myPublicHostName,-1,true);
        result = true;
    }

    return result;
}

bool HostManager::insertNewHost(const char* hostName,const char *netwName,int listenport)
{
    bool result = true; // function result

    if(testUniqueness(hostName)==false)
        result = false;

    if (result == true)
    {
        // FIXME: there should be only explicitly defined hosts; what are "internal" hosts? -- PB 2007-jun-26
        ServerHost tempServerHost;

        if(hostList.empty())
            insertInternalHost(); // just protection, but shouldn't be necessary

        hostList.push_back(tempServerHost);
        ServerHost &refServerHost=hostList.back();
        refServerHost.init(hostName,netwName,listenport,false); //always external
        result = true;
    }

    return result;
}

bool HostManager::removeHost(const char *hostName)
{
    list<ServerHost>::iterator iter=hostList.begin();
    for(unsigned int i=0; i<hostList.size(); i++)
    {
        if(hostCmp(iter->getName(),hostName))
        {
            if(iter->countDefinedServers()>0)
            {
                return false;
            }
            hostList.erase(iter);
            break;
        }
        iter++;
    }

    return true;
}

// FIXME: check for end of list
ServerHost& HostManager::operator[](int x)
{
    list<ServerHost>::iterator iter=hostList.begin();
    for(int i=0; i<x; i++) iter++;

    return *iter;
}

// returns:
//  server host object if found
//  protElem (uninitialized object, not valid) if not found
ServerHost& HostManager::operator[](const char* hostName)
{
    list<ServerHost>::iterator iter=hostList.begin();
    for(unsigned int i=0; i<hostList.size(); i++)
    {
        if(hostCmp(iter->getName(),hostName))
        {
            return *iter;
        }

        iter++;
    }

    return protElem;
}

int  HostManager::countHosts()
{
    return hostList.size();
}
int HostManager::countUpHosts()
{
    int count=0;
    list<ServerHost>::iterator iter=hostList.begin();
    for(unsigned int i=0; i<hostList.size(); i++)
    {
        if(iter->isUp())
            count++;
        iter++;
    }
    return count;
}

bool HostManager::testUniqueness(const char* hostName)
{
    list<ServerHost>::iterator iter=hostList.begin();
    for(unsigned int i=0; i<hostList.size(); i++)
    {
        if(hostCmp(iter->getName(),hostName))
            return false;
        iter++;
    }
    return true;
}

int HostManager::postSlaveMGR(char *body,char *outBuffer)
{
    char answBuffer[100];

    do
    {
        // so we can break

        if(body==NULL)
        {
            strcpy(answBuffer,"Missing identification, this is not a valid rasmgr."); // no rasmgr or soft error
            break;
        }

        char name[100];
        int  licServ;
        sscanf(body,"%s %d",name,&licServ);

        LDEBUG << "HostManager::postSlaveMGR: name="<<name<<" lics="<<licServ;

        ServerHost &sh=operator[](name);
        if(sh.isValid()==false)
        {
            strcpy(answBuffer,"Unknown slave rasmgr.");
            break;
        }

        LDEBUG << "HostManager::postSlaveMGR: Ok, valid.";
        sh.setIsUp(true);
        strcpy(answBuffer,"Welcome!");

    }
    while(0);

    sprintf(outBuffer,"HTTP/1.1 200 OK\r\nContent-type: text/plain\r\nContent-length: %lu\r\n\r\n%s",strlen(answBuffer)+1,answBuffer);

    return strlen(outBuffer)+1;
}

bool HostManager::reset()
{
    if(config.isTestModus()==false)
    {
        return false;
    }

    list<ServerHost>::iterator iter=hostList.begin();
    for(unsigned int i=0; i<hostList.size(); i++,iter++)
    {
        if(iter->countDefinedServers()>0)
        {
            return false;
        }
    }

    while(hostList.size())
    {
        hostList.pop_front();
    }

    return true;
}

bool HostManager::acceptChangeName(const char *oldName,const char *newName)
{
    if(hostCmp(oldName,newName))
        return true; // if someone really wants to change a name with the same,

    return testUniqueness(newName);
}

//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
