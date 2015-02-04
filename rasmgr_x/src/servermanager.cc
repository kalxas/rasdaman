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

#include <cerrno>
#include <cstdio>
#include <unistd.h>

#include <map>
#include <set>
#include <stdexcept>
#include <sstream>

#include <boost/lexical_cast.hpp>
#include <boost/thread.hpp>
#include <boost/unordered_set.hpp>
#include <boost/date_time/posix_time/posix_time.hpp>
#include <boost/thread/thread.hpp>
#include <boost/format.hpp>

#include "../../common/src/logging/easylogging++.hh"
#include "../../common/src/uuid/uuid.hh"
#include "../../rasnet/src/util/proto/protozmq.hh"
#include "../../rasnet/src/messages/communication.pb.h"
#include "../../rasnet/src/messages/base.pb.h"


#include "rasmgrconfig.hh"
#include "serverrasnet.hh"
#include "servermanager.hh"

#include "serverfactoryrasnet.hh"

namespace rasmgr
{

using boost::lexical_cast;
using boost::posix_time::microsec_clock;
using boost::posix_time::milliseconds;
using boost::scoped_ptr;
using boost::shared_lock;
using boost::shared_mutex;
using boost::shared_ptr;
using boost::thread;
using boost::unique_lock;
using boost::unordered_set;
using boost::mutex;
using boost::format;

using std::map;
using std::runtime_error;
using std::set;
using std::string;
using std::list;

using common::UUID;
using zmq::socket_t;
using rasnet::ProtoZmq;
using rasnet::InternalDisconnectReply;
using rasnet::InternalDisconnectRequest;


ServerManager::ServerManager ( boost::shared_ptr<DatabaseHostManager> dbhManager )
{
    this->dbhManager = dbhManager;
    this->controlEndpoint = "inproc://"+UUID::generateUUID();

    this->workerCleanup.reset ( new thread ( &ServerManager::workerCleanupRunner, this ) );
    this->controlSocket.reset ( new zmq::socket_t ( this->context, ZMQ_PAIR ) );
    this->controlSocket->connect ( this->controlEndpoint.c_str() );
    this->serverFactory.reset ( new ServerFactoryRasNet() );
}

ServerManager::~ServerManager()
{
    try
    {
        rasnet::InternalDisconnectRequest request = rasnet::InternalDisconnectRequest::default_instance();
        base::BaseMessage reply;
        ProtoZmq::zmqSend ( * ( this->controlSocket.get() ), request );
        ProtoZmq::zmqReceive ( * ( this->controlSocket.get() ), reply );

        if ( reply.type() !=rasnet::InternalDisconnectReply::default_instance().GetTypeName() )
        {
            LERROR<<"Unexpected message received from control socket."<<reply.DebugString();
        }

        this->workerCleanup->join();
    }
    catch ( std::exception& ex )
    {
        LERROR<<ex.what();
    }
    catch ( ... )
    {
        LERROR<<"ServerManager destructor failed";
    }
}

shared_ptr<Server> ServerManager::getFreeServer ( const std::string& dbName )
{
    /* TODO:
     * 1. Ask peers about available servers
     */
    list<shared_ptr<ServerGroup> >::iterator it;
    shared_ptr<Server> result;
    shared_lock<shared_mutex> lockMutexGroups ( this->serverGroupMutex );

    shared_ptr<RasMgrConfig> config= RasMgrConfig::getInstance();
    boost::int32_t retryTimeout = config->getClientGetServerRetryTimeout();
    boost::uint32_t retryNo =  config->getClientGetServerRetryNo();

    while ( retryNo>0 )
    {
        for ( it=this->serverGroupList.begin(); it!=this->serverGroupList.end(); ++it )
        {
            if ( ( *it )->getAvailableServer ( dbName, result ) )
            {
                return result;
            }
        }
        //There is no free server available so we must force the reevaluation of the server
        //groups.

        this->evaluateServerGroups();

        boost::this_thread::sleep ( boost::posix_time::milliseconds ( retryTimeout ) );

        retryNo--;
    }

    throw runtime_error ( "No available servers." );
}

void ServerManager::registerServer ( const string& serverId )
{
    bool registered=false;
    list<shared_ptr<ServerGroup> >::iterator it;

    shared_lock<shared_mutex> lockMutexGroups ( this->serverGroupMutex );
    for ( it=this->serverGroupList.begin(); it!=this->serverGroupList.end(); ++it )
    {
        if ( ( ( *it )->registerServer ( serverId ) ) )
        {
            registered=true;
            break;
        }
    }

    if ( !registered )
    {
        throw runtime_error ( "There is no server with ID:"+serverId );
    }
}

void ServerManager::defineServerGroup ( const ServerGroupConfig &serverGroupConfig )
{
    list<shared_ptr<ServerGroup> >::iterator it;

    unique_lock<shared_mutex> lock ( this->serverGroupMutex );

    for ( it=this->serverGroupList.begin(); it!=this->serverGroupList.end(); ++it )
    {
        if ( ( *it )->getGroupName() ==serverGroupConfig.getGroupName() )
        {
            throw runtime_error ( "There already exists a server group with the name:"+serverGroupConfig.getGroupName() );
        }
    }

    this->serverGroupList.push_back ( shared_ptr<ServerGroup> ( new ServerGroup ( serverGroupConfig, this->dbhManager, serverFactory ) ) );
}


void ServerManager::changeServerGroup ( const std::string &oldServerGroupName, const ServerGroupConfig &newServerGroupConfig )
{
    list<shared_ptr<ServerGroup> >::iterator it;
    bool changed=false;

    unique_lock<shared_mutex> lock ( this->serverGroupMutex );
    for ( it=this->serverGroupList.begin(); it!=this->serverGroupList.end(); ++it )
    {
        if ( ( *it )->getGroupName() ==oldServerGroupName )
        {
            if ( ( *it )->isBusy() )
            {
                throw runtime_error ( "Cannot change server group while its servers are running." );
            }
            else
            {
                changed=true;
                ( *it )->setConfig ( newServerGroupConfig );
                break;
            }
        }
    }

    if ( !changed )
    {
        throw runtime_error ( "There is not server group named \""+oldServerGroupName+"\"" );
    }
}

void ServerManager::removeServerGroup ( const std::string &serverGroupName )
{
    list<shared_ptr<ServerGroup> >::iterator it;
    bool removed=false;

    unique_lock<shared_mutex> lock ( this->serverGroupMutex );
    for ( it=this->serverGroupList.begin(); it!=this->serverGroupList.end(); ++it )
    {
        if ( ( *it )->getGroupName() ==serverGroupName )
        {
            if ( ( *it )->isBusy() )
            {
                throw runtime_error ( "Cannot remove server group while its servers are running." );
            }
            else
            {
                removed=true;
                this->serverGroupList.erase ( it );
                break;
            }
        }
    }

    if ( !removed )
    {
        throw runtime_error ( "There is no server group named \""+serverGroupName+"\"" );
    }
}

ServerGroupConfig ServerManager::getServerGroupConfig ( const std::string &groupName )
{
    list<shared_ptr<ServerGroup> >::iterator it;
    shared_lock<shared_mutex> lockMutexGroups ( this->serverGroupMutex );

    for ( it=this->serverGroupList.begin(); it!=this->serverGroupList.end(); ++it )
    {
        if ( ( *it )->getGroupName() ==groupName )
        {
            return ( *it )->getConfig();
        }
    }

    throw runtime_error ( "There is no server group named \""+groupName+"\"" );
}

void ServerManager::startServerGroup ( const StartServerGroup &startGroup )
{
    list<shared_ptr<ServerGroup> >::iterator it;
    shared_lock<shared_mutex> lockMutexGroups ( this->serverGroupMutex );

    if ( startGroup.has_group_name() )
    {
        bool found=false;
        for ( it=this->serverGroupList.begin(); it!=this->serverGroupList.end(); ++it )
        {
            shared_ptr<ServerGroup>  srv = ( *it );
            if ( srv->getGroupName() ==startGroup.group_name() )
            {
                found=true;
                if ( srv->isStopped() )
                {
                    srv->start();
                }
                else
                {
                    throw runtime_error ( "The server group \""+startGroup.group_name() +"\" is already running." );
                }

                break;
            }
        }

        if ( !found )
        {
            throw runtime_error ( "There is not server group named \""+startGroup.group_name() +"\"" );
        }
    }
    else if ( startGroup.has_host_name() )
    {
        bool hostExists = false;
        std::string onHost = startGroup.host_name();

        for ( it=this->serverGroupList.begin(); it!=this->serverGroupList.end(); ++it )
        {
            if ( ( *it )->getConfig().getHost() ==onHost )
            {
                hostExists=true;
                if ( ( *it )->isStopped() )
                {
                    ( *it )->start();
                }
            }
        }

        if ( !hostExists )
        {
            throw runtime_error ( "There are no server groups defined on host \""+onHost+"\"" );
        }
    }
    else if ( startGroup.has_all() )
    {
        for ( it=this->serverGroupList.begin(); it!=this->serverGroupList.end(); ++it )
        {
            if ( ( *it )->isStopped() )
            {
                ( *it )->start();
            }
        }
    }
}

void ServerManager::stopServerGroup ( const StopServerGroup &stopGroup )
{
    list<shared_ptr<ServerGroup> >::iterator it;
    shared_lock<shared_mutex> lockMutexGroups ( this->serverGroupMutex );

    if ( stopGroup.has_group_name() )
    {
        bool found=false;

        for ( it=this->serverGroupList.begin(); it!=this->serverGroupList.end(); ++it )
        {
            shared_ptr<ServerGroup>  srv = ( *it );
            if ( srv->getGroupName() ==stopGroup.group_name() )
            {
                found=true;

                if ( !srv->isStopped() )
                {
                    srv->stop();
                }
                else
                {
                    throw runtime_error ( "The server group \""+stopGroup.group_name() +"\" is already stopped." );
                }

                break;
            }
        }

        if ( !found )
        {
            throw runtime_error ( "There is not server group named \""+stopGroup.group_name() +"\"" );
        }
    }
    else if ( stopGroup.has_host_name() )
    {
        bool hostExists = false;
        std::string onHost = stopGroup.host_name();

        for ( it=this->serverGroupList.begin(); it!=this->serverGroupList.end(); ++it )
        {
            if ( ( *it )->getConfig().getHost() ==onHost )
            {
                hostExists=true;
                if ( ! ( *it )->isStopped() )
                {
                    ( *it )->stop();
                }
            }
        }

        if ( !hostExists )
        {
            throw runtime_error ( "There are no server groups defined on host \""+onHost+"\"" );
        }
    }
    else if ( stopGroup.has_all() )
    {
        for ( it=this->serverGroupList.begin(); it!=this->serverGroupList.end(); ++it )
        {
            if ( ! ( *it )->isStopped() )
            {
                ( *it )->stop();
            }
        }
    }
}

bool ServerManager::hasRunningServerGroup()
{
    list<shared_ptr<ServerGroup> >::iterator it;
    shared_lock<shared_mutex> lockMutexGroups ( this->serverGroupMutex );

    for ( it=this->serverGroupList.begin(); it!=this->serverGroupList.end(); ++it )
    {
        if ( ! ( *it )->isStopped() )
        {
            return true;
        }
    }

    return false;
}

std::string ServerManager::getServerGroupInfo ( const std::string &serverGroupName, bool portDetails )
{
    list<shared_ptr<ServerGroup> >::iterator it;
    bool found=false;
    std::stringstream ss;

    shared_lock<shared_mutex> lockMutexGroups ( this->serverGroupMutex );
    for ( it=this->serverGroupList.begin(); it!=this->serverGroupList.end(); ++it )
    {
        if ( ( *it )->getConfig().getGroupName() ==serverGroupName )
        {
            std::string serverGroupName = ( *it )->getGroupName();
            std::string hostName = ( *it )->getConfig().getHost();
            std::string dbHostName = ( *it )->getConfig().getDbHost();
            std::string up = ( *it )->isStopped() ?"DOWN":"UP";

            ss<< ( format ( "Status of server %s\r\n" ) % serverGroupName );
            ss<< ( format ( "    %-20s   %-20s %-20s %-4s\r\n" ) % "Server Name"  % "Host" % "Db Host" % "Stat" );
            ss<< ( format ( "    %-20s %-20s %-20s %s\r\n" ) % serverGroupName % hostName % dbHostName % up );
            found=true;
            break;
        }
    }

    if ( !found )
    {
        throw runtime_error ( "There is not server group named \""+serverGroupName+"\"" );
    }

    return ss.str();
}

std::string ServerManager::getAllServerGroupsInfo ( bool details,const std::string& host )
{
    list<shared_ptr<ServerGroup> >::iterator it;
    std::stringstream ss;
    shared_lock<shared_mutex> lockMutexGroups ( this->serverGroupMutex );

    if ( host.empty() )
    {
        ss<< "List of servers \r\n";
        ss<< ( format ( "    %-20s %s   %-20s %-20s %-4s %-2s     %s   %s" ) %"Server Name"%"Type"%"Host"%"Db Host"%"Stat"%"Av"%"Acc"%"Crc" );

        int counter =1;
        for ( it=this->serverGroupList.begin(); it!=this->serverGroupList.end(); ++it )
        {
            std::string serverGroupName = ( *it )->getGroupName();
            std::string hostName = ( *it )->getConfig().getHost();
            std::string dbHostName = ( *it )->getConfig().getDbHost();
            std::string up = ( *it )->isStopped() ?"DOWN":"UP";

            ss<< ( format ( "\r\n%2d. %-20s %s %-20s %-20s %s %s %6ld    %2d" ) % counter % serverGroupName % "(RASNET)"% hostName % dbHostName % up % "-" %0 % 0 );

            counter ++;
        }
    }
    else
    {}

    return ss.str();
}

void ServerManager::workerCleanupRunner()
{
    base::BaseMessage controlMessage;
    bool keepRunning=true;

    try
    {
        zmq::socket_t control ( this->context, ZMQ_PAIR );
        control.bind ( this->controlEndpoint.c_str() );
        zmq::pollitem_t items[] = {{control,0,ZMQ_POLLIN,0}};

        while ( keepRunning )
        {
            zmq::poll ( items, 1, RasMgrConfig::getInstance()->getServerManagementGarbageCollectionInterval() );

            if ( items[0].revents & ZMQ_POLLIN )
            {
                ProtoZmq::zmqReceive ( control, controlMessage );
                if ( controlMessage.type() ==InternalDisconnectRequest::default_instance().GetTypeName() )
                {
                    keepRunning=false;
                    InternalDisconnectReply disconnectReply;
                    ProtoZmq::zmqSend ( control, disconnectReply );
                }
            }
            else
            {
                shared_lock<shared_mutex> lock ( this->serverGroupMutex );

                this->evaluateServerGroups();
            }
        }
    }
    catch ( std::exception& ex )
    {
        LERROR<<ex.what();
    }
    catch ( ... )
    {
        LERROR<<"Server management thread has failed";
    }
}

void ServerManager::evaluateServerGroups()
{
    /**
                    * For each server group evaluate the group's status.
                    * This means that dead server entries will be removed
                    * and new servers will be started.
                    */
    LINFO<<"Evaluating server groups.";
    for ( list<shared_ptr<ServerGroup> >::iterator it = this->
            serverGroupList.begin();
            it!=this->serverGroupList.end();
            ++it )
    {
        try
        {
            ( *it )->evaluateServerGroup();
        }
        catch ( std::exception& e )
        {
            LERROR<<"Could not evaluate server in group.Reason:"<<e.what();
        }
        catch ( ... )
        {
            LERROR<<"Unexpected exception when starting server";
        }
    }
}


} /* namespace rasmgr */
