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

#include <stdexcept>
#include <algorithm>

#include <boost/lexical_cast.hpp>
#include <boost/cstdint.hpp>

#include "../../common/src/logging/easylogging++.hh"

#include "rasmgrconfig.hh"
#include "servergroup.hh"
#include "../../common/src/logging/easylogging++.hh"


namespace rasmgr
{
using std::map;
using std::list;
using std::string;
using std::pair;
using std::runtime_error;

using boost::mutex;
using boost::shared_ptr;
using boost::unique_lock;
using boost::shared_lock;
using boost::shared_mutex;
using boost::int32_t;
using boost::uint32_t;
using boost::lexical_cast;

using common::Timer;

//TODO-AT:Validate the server group config and add documentation in the server
//group config that any modification made there should be propagated
ServerGroup::ServerGroup(const ServerGroupConfig& config, boost::shared_ptr<DatabaseHostManager> dbhManager, boost::shared_ptr<IServerCreator> serverCreator):config(config), serverCreator(serverCreator)
{
    this->databaseHost = dbhManager->getAndLockDH(config.getDbHost());
    this->stopped = true;

    if(config.getPorts().size()!=config.getGroupSize())
    {
        throw runtime_error("The number of allocated ports must be equal to the size of the group.");
    }

    this->availablePorts = config.getPorts();

    if(config.getMinAliveServers() > config.getGroupSize())
    {
        throw runtime_error("The ServerGroupConfig is invalid.");
    }

    if(config.getMinAvailableServers() > config.getGroupSize())
    {
        throw runtime_error("The ServerGroupConfig is invalid.");
    }

    if(config.getMinAliveServers() < config.getMinAvailableServers())
    {
        throw runtime_error("The ServerGroupConfig is invalid.");
    }
}

ServerGroup::~ServerGroup()
{
    this->databaseHost->decreaseServerCount();

    list<shared_ptr<RasServer> >::iterator runningServer;
    for(runningServer=this->runningServers.begin(); runningServer!=this->runningServers.end(); ++runningServer)
    {
        try
        {
            (*runningServer)->stop(true);
        }
        catch(std::exception& ex)
        {
            LERROR<<"ServerGroup destruction failed:"<<ex.what();
        }
        catch(...)
        {
            LERROR<<"ServerGroup destruction failed for an unkown reason";
        }
    }

    map<string, pair<shared_ptr<RasServer>,Timer> >::iterator startingServerEntry;
    for(startingServerEntry=this->startingServers.begin(); startingServerEntry!=this->startingServers.end(); ++startingServerEntry)
    {
        try
        {
            startingServerEntry->second.first->stop(true);
        }
        catch(std::exception& ex)
        {
            LERROR<<"ServerGroup destruction failed:"<<ex.what();
        }
        catch(...)
        {
            LERROR<<"ServerGroup destruction failed for an unkown reason";
        }
    }
}

bool ServerGroup::isBusy()
{
    unique_lock<shared_mutex> groupLock(this->groupMutex);
    return (( this->runningServers.size() > 0 ) || (this->startingServers.size() > 0));
}

void ServerGroup::start()
{
    /**
      1. Get exclusive access and start the minimum number of alive servers
      */
    boost::unique_lock<shared_mutex> groupLock(this->groupMutex);
    if(this->stopped)
    {
        this->stopped=false;

        for(boost::int32_t i=0; i<this->config.getMinAliveServers(); i++)
        {
            this->startServer();
        }
    }
}

bool ServerGroup::isStopped()
{
    unique_lock<shared_mutex> groupLock(this->groupMutex);
    return this->stopped;
}

void ServerGroup::stop(bool force)
{
    list<shared_ptr<RasServer> >::iterator it;
    list<shared_ptr<RasServer> >::iterator toErase;

    unique_lock<shared_mutex> groupLock(this->groupMutex);

    this->stopped=true;

    list<shared_ptr<RasServer> >::iterator runningServer;
    for(runningServer=this->runningServers.begin(); runningServer!=this->runningServers.end(); ++runningServer)
    {
        try
        {
            (*runningServer)->stop(force);
        }
        catch(std::exception& ex)
        {
            LERROR<<"ServerGroup destruction failed:"<<ex.what();
        }
        catch(...)
        {
            LERROR<<"ServerGroup destruction failed for an unkown reason";
        }
    }

    map<string, pair<shared_ptr<RasServer>,Timer> >::iterator startingServerEntry;
    for(startingServerEntry=this->startingServers.begin(); startingServerEntry!=this->startingServers.end(); ++startingServerEntry)
    {
        try
        {
            startingServerEntry->second.first->stop(force);
        }
        catch(std::exception& ex)
        {
            LERROR<<"ServerGroup destruction failed:"<<ex.what();
        }
        catch(...)
        {
            LERROR<<"ServerGroup destruction failed for an unkown reason";
        }
    }

    this->evaluateGroup();
}

bool ServerGroup::registerServer(const std::string &serverId)
{
    /**
      * If the server group was not shutdown, add the server to the list of running
      * servers and remove it from the list of starting servers.
      */
    unique_lock<shared_mutex> groupLock(this->groupMutex);
    map<string, pair<shared_ptr<RasServer>,Timer> >::iterator it;
    bool registered=false;
    it=this->startingServers.find(serverId);

    if(it!=this->startingServers.end())
    {
        //If the server group is stopped, we cannot register new servers.
        if(this->stopped)
        {
            throw runtime_error("The server group is stopped. No new servers can register.");
        }

        it->second.first->registerServer(serverId);
        this->runningServers.push_back(it->second.first);
        this->startingServers.erase(it);

        registered = true;
    }

    return registered;
}

void ServerGroup::evaluateServerGroup()
{
    unique_lock<shared_mutex> groupLock(this->groupMutex);
    this->evaluateGroup();
}

bool ServerGroup::getAvailableServer(const std::string &dbName, boost::shared_ptr<RasServer>& out_server)
{
    unique_lock<shared_mutex> groupLock(this->groupMutex);

    boost::shared_ptr<RasServer> result;
    if(this->databaseHost->ownsDatabase(dbName))
    {
        list<shared_ptr<RasServer> >::iterator it;

        for(it=this->runningServers.begin(); it!=this->runningServers.end(); ++it)
        {
            if((*it)->isAvailable())
            {
                out_server=(*it);
                //Round-robin distribution of load
                this->runningServers.push_back(out_server);
                this->runningServers.erase(it);
                return true;
            }
        }
    }

    return false;
}

ServerGroupConfig ServerGroup::getConfig() const
{
    return config;
}

void ServerGroup::setConfig(const ServerGroupConfig &value)
{
    //TODO-AT: Validate the config
    config = value;
}

std::string ServerGroup::getGroupName() const
{
    return this->config.getGroupName();
}

void ServerGroup::removeDeadServers()
{
    map<string, pair<shared_ptr<RasServer>,Timer> >::iterator startingIt;
    map<string, pair<shared_ptr<RasServer>,Timer> >::iterator startingToEraseIt;

    list<shared_ptr<RasServer> >::iterator runningIt;
    list<shared_ptr<RasServer> >::iterator runningToErase;

    /**
      1.Remove servers that have failed to register in the allocated time.
      2.Remove servers that are no longer responding to pings
      */
    try
    {
        runningIt = this->runningServers.begin();
        while(runningIt!=this->runningServers.end())
        {
            runningToErase=runningIt;
            ++runningIt;

            try
            {
                if(!(*runningToErase)->isAlive())
                {
                    LDEBUG<<"Removed dead server with id"<<(*runningToErase)->getServerId();
                    this->availablePorts.insert((*runningToErase)->getPort());
                    this->runningServers.erase(runningToErase);
                }
            }
            catch(...)
            {
                LDEBUG<<"Server with id: "<<(*runningToErase)->getServerId()<<" is not responding to pings.";
            }
        }

        startingIt = this->startingServers.begin();
        while(startingIt!=this->startingServers.end())
        {
            startingToEraseIt = startingIt;
            ++startingIt;

            if(startingToEraseIt->second.second.hasExpired())
            {
                LDEBUG<<"Removed server that failed to start with id:"<< startingToEraseIt->second.first->getServerId();

                this->availablePorts.insert(startingToEraseIt->second.first->getPort());
                this->startingServers.erase(startingToEraseIt);
            }
        }
    }
    catch(std::exception& ex)
    {
        LERROR<<"Removing dead servers has failed."<<ex.what();
    }
    catch(...)
    {
        LERROR<<"Removing dead servers has failed for an unknown reason";
    }
}

void ServerGroup::startServer()
{
    if(this->availablePorts.empty())
    {
        throw runtime_error("The group has reached capacity. No more servers can be startedG");
    }
    else
    {
        std::set<boost::int32_t>::iterator it = this->availablePorts.begin();
        int32_t port = *it;
        this->availablePorts.erase(port);

        shared_ptr<RasServer> server = this->serverCreator->createServer(this->config.getHost(),port, this->databaseHost);

        pair<shared_ptr<RasServer>, Timer> startingServerEntry(server, Timer(RasMgrConfig::getInstance()->getRasServerTimeout()));

        this->startingServers.insert(pair<string, pair<shared_ptr<RasServer>, Timer> >(server->getServerId(), startingServerEntry));

        server->startProcess();
    }

}

void ServerGroup::evaluateGroup()
{
    /**
    1. Remove dead servers
    2. Try to keep the minimum number of available servers
    3. Try to keep the minimum number of alive servers
    */
    uint32_t availableServerNo=0;
    uint32_t freeServerNo=0;
    list<shared_ptr<RasServer> >::iterator it;

    LDEBUG<<"Removing dead servers in group("<<this->getGroupName()<<");";
    this->removeDeadServers();
    LDEBUG<<"Removed dead servers in group("<<this->getGroupName()<<");";

    if(!this->stopped)
    {
        for(it=this->runningServers.begin(); it!=this->runningServers.end(); ++it)
        {
            if((*it)->isAvailable())
            {
                availableServerNo++;
            }

            if((*it)->isFree())
            {
                freeServerNo++;
            }
        }

        LDEBUG<<"There are "<< availableServerNo<<" available servers.";
        LDEBUG<<"There are "<< freeServerNo<< " free servers.";

        if(availableServerNo < this->config.getMinAvailableServers())
        {
            uint32_t maxNoServersToStart = this->config.getMinAvailableServers() - availableServerNo;
            uint32_t notStartedServerNo = this->config.getGroupSize() - this->startingServers.size() - this->runningServers.size();

            LDEBUG<<"Server group("<<this->getGroupName()<<") has "<<notStartedServerNo<<"servers that have not been started";
            LDEBUG<<"Server group("<<this->getGroupName()<<") wants to start at most"<<maxNoServersToStart;

            uint32_t serversToStartNo = std::min(maxNoServersToStart,notStartedServerNo);

            LDEBUG<<"Server group("<<this->getGroupName()<<") will start "<<serversToStartNo<<" servers";


            for(uint32_t i=0; i<serversToStartNo; i++)
            {
                this->startServer();
            }
        }
        else if(availableServerNo> this->config.getMinAvailableServers() && freeServerNo > this->config.getMaxIdleServersNo())
        {
            uint32_t maxServersToStop = freeServerNo-this->config.getMaxIdleServersNo();
            uint32_t availableServersToStop=  availableServerNo - this->config.getMinAvailableServers();

            LDEBUG<<"Server group("<<this->getGroupName()<<") has "<<maxServersToStop<<" servers more than the maximum number of allowed idle servers";
            LDEBUG<<"Server group("<<this->getGroupName()<<") can stop"<<availableServersToStop <<" servers while maintaining the minimum number of available servers.";

            uint32_t serversToStop = std::min(availableServersToStop, maxServersToStop);

            LDEBUG<<"Server group("<<this->getGroupName()<<") will stop"<<serversToStop <<" free servers";

            list<shared_ptr<RasServer> >::iterator it;
            for(it=this->runningServers.begin(); serversToStop>0 && it!=this->runningServers.end(); ++it)
            {

                if((*it)->isFree())
                {
                    (*it)->stop(false);
                    serversToStop--;
                }
            }
        }
    }
}
}
