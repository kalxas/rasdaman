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
#include "../../rasnet/src/util/proto/zmqutil.hh"
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

using base::BaseMessage;
using common::UUID;
using rasnet::ProtoZmq;
using rasnet::InternalDisconnectReply;
using rasnet::InternalDisconnectRequest;
using zmq::socket_t;
using rasnet::ZmqUtil;

ServerManager::ServerManager(const ServerManagerConfig& config, boost::shared_ptr<ServerGroupFactory> serverGroupFactory)
    :config(config), serverGroupFactory(serverGroupFactory)
{
    this->controlEndpoint = ZmqUtil::toInprocAddress(UUID::generateUUID());
    this->workerCleanup.reset ( new thread ( &ServerManager::workerCleanupRunner, this ) );

    this->controlSocket.reset ( new zmq::socket_t ( this->context, ZMQ_PAIR ) );
    this->controlSocket->connect ( this->controlEndpoint.c_str() );
}

ServerManager::~ServerManager()
{
    try
    {
        InternalDisconnectRequest request = InternalDisconnectRequest::default_instance();
        BaseMessage reply;
        ProtoZmq::zmqSend ( * ( this->controlSocket.get() ), request );
        ProtoZmq::zmqReceive ( * ( this->controlSocket.get() ), reply );

        if ( reply.type() != InternalDisconnectReply::default_instance().GetTypeName() )
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

bool ServerManager::tryGetFreeServer(const std::string &databaseName, boost::shared_ptr<Server> &out_server)
{
    bool success = false;
    list<shared_ptr<ServerGroup> >::iterator it;

    shared_lock<shared_mutex> lockMutexGroups ( this->serverGroupMutex );

    for ( it=this->serverGroupList.begin(); it!=this->serverGroupList.end(); ++it )
    {
        if ( ( *it )->tryGetAvailableServer( databaseName, out_server ) )
        {
            success = true;
            break;
        }
    }

    return success;
}


void ServerManager::registerServer ( const string& serverId )
{
    bool registered = false;
    list<shared_ptr<ServerGroup> >::iterator it;

    shared_lock<shared_mutex> lockMutexGroups ( this->serverGroupMutex );

    for ( it=this->serverGroupList.begin(); it!=this->serverGroupList.end(); ++it )
    {
        if ( ( ( *it )->tryRegisterServer( serverId ) ) )
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

void ServerManager::defineServerGroup(const ServerGroupConfigProto &serverGroupConfig)
{
    list<shared_ptr<ServerGroup> >::iterator it;

    unique_lock<shared_mutex> lock ( this->serverGroupMutex );

    for ( it=this->serverGroupList.begin(); it!=this->serverGroupList.end(); ++it )
    {
        if ( ( *it )->getGroupName() ==serverGroupConfig.name() )
        {
            throw runtime_error ( "There already exists a server group named:\""+serverGroupConfig.name()+"\".");
        }
    }

    this->serverGroupList.push_back(this->serverGroupFactory->createServerGroup(serverGroupConfig));
}

void ServerManager::changeServerGroup(const std::string &oldServerGroupName, const ServerGroupConfigProto &newServerGroupConfig)
{
    list<shared_ptr<ServerGroup> >::iterator it;
    bool changed=false;

    unique_lock<shared_mutex> lock ( this->serverGroupMutex );
    for ( it=this->serverGroupList.begin(); it!=this->serverGroupList.end(); ++it )
    {
        if ( ( *it )->getGroupName() == oldServerGroupName )
        {
            if(( *it )->isStopped() )
            {
                changed=true;
                ( *it )->changeGroupConfig ( newServerGroupConfig );
                break;
            }
            else
            {
                throw runtime_error ( string("Cannot change server group properties while it is running.")
                                      + string(" Stop the server group.") );
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
        if ( ( *it )->getGroupName() == serverGroupName )
        {
            if(( *it )->isStopped() )
            {
                removed=true;
                this->serverGroupList.erase ( it );
                break;
            }
            else
            {
                throw runtime_error ( string("Cannot remove server group while it is running.")
                                      + string(" Stop the server group.") );
            }
        }
    }

    if ( !removed )
    {
        throw runtime_error ( "There is no server group named \""+serverGroupName+"\"" );
    }
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
            if ( srv->getGroupName() == startGroup.group_name() )
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
            if ( ( *it )->getConfig().host() == onHost )
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
    else
    {
        throw runtime_error("Invalid start server command.");
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
            if ( srv->getGroupName() == stopGroup.group_name() )
            {
                found=true;

                if(srv->isStopped())
                {
                    throw runtime_error ( "The server group \""+stopGroup.group_name() +"\" is already stopped." );
                }
                else
                {
                    srv->stop(stopGroup.kill_level());
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
            if ( ( *it )->getConfig().host() ==onHost )
            {
                hostExists=true;
                if ( ! ( *it )->isStopped() )
                {
                    ( *it )->stop(stopGroup.kill_level());
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
                ( *it )->stop(stopGroup.kill_level());
            }
        }
    }
    else
    {
        throw runtime_error("Invalid stop server command.");
    }
}

bool ServerManager::hasRunningServers()
{
    bool found=false;

    unique_lock<shared_mutex> lock ( this->serverGroupMutex );

    list<shared_ptr<ServerGroup> >::iterator it;
    for ( it=this->serverGroupList.begin(); it!=this->serverGroupList.end(); ++it )
    {
        if( !( *it )->isStopped() )
        {
            found = true;
            break;
        }
    }

    return found;
}

ServerMgrProto ServerManager::serializeToProto()
{
    ServerMgrProto result;

    shared_lock<shared_mutex> lockMutexGroups ( this->serverGroupMutex );
    list<shared_ptr<ServerGroup> >::iterator it;

    for ( it=this->serverGroupList.begin(); it!=this->serverGroupList.end(); ++it )
    {
        result.add_server_groups()->CopyFrom(( *it )->serializeToProto());
    }

    return result;
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
            zmq::poll ( items, 1, this->config.getCleanupInterval() );

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
