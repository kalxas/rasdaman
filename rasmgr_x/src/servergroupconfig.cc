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

#include <stdexcept>
#include "servergroupconfig.hh"

namespace rasmgr
{
ServerGroupConfig::ServerGroupConfig(const std::string &groupName, boost::uint32_t groupSize,
                                     const std::string &host, std::set<boost::int32_t> ports,
                                     const std::string &dbHost)
{

    if(ports.size()!=groupSize)
    {
        throw std::runtime_error("You must provide ports for all the servers");
    }

    this->ports = ports;
    this->groupName=groupName;
    this->groupSize = groupSize;
    this->host = host;
    this->dbHost = dbHost;

    this->minAliveServers=1;
    this->countdown = 100;
    this->autorestart = true;
    this->minAvailableServers=1;
    this->maxIdleServersNo = 10;
}
std::string ServerGroupConfig::getGroupName() const
{
    return groupName;
}

void ServerGroupConfig::setGroupName(const std::string &value)
{
    groupName = value;
}
boost::uint32_t ServerGroupConfig::getGroupSize() const
{
    return groupSize;
}

void ServerGroupConfig::setGroupSize(const boost::uint32_t &value)
{
    groupSize = value;
}
boost::uint32_t ServerGroupConfig::getMinAliveServers() const
{
    return minAliveServers;
}

void ServerGroupConfig::setMinAliveServers(const boost::uint32_t &value)
{
    minAliveServers = value;
}
boost::uint32_t ServerGroupConfig::getMinAvailableServers() const
{
    return minAvailableServers;
}

void ServerGroupConfig::setMinAvailableServers(const boost::uint32_t &value)
{
    minAvailableServers = value;
}

boost::uint32_t ServerGroupConfig::getMaxIdleServersNo() const
{
    return this->maxIdleServersNo;
}

void ServerGroupConfig::setMaxIdleServersNo(const boost::uint32_t &value)
{
    this->maxIdleServersNo = value;
}

std::string ServerGroupConfig::getHost() const
{
    return host;
}

void ServerGroupConfig::setHost(const std::string &value)
{
    host = value;
}
std::set<boost::int32_t> ServerGroupConfig::getPorts() const
{
    return ports;
}

void ServerGroupConfig::setPorts(const std::set<boost::int32_t> &value)
{
    ports = value;
}
std::string ServerGroupConfig::getDbHost() const
{
    return dbHost;
}

void ServerGroupConfig::setDbHost(const std::string &value)
{
    dbHost = value;
}
bool ServerGroupConfig::getAutorestart() const
{
    return autorestart;
}

void ServerGroupConfig::setAutorestart(bool value)
{
    autorestart = value;
}
boost::uint32_t ServerGroupConfig::getCountdown() const
{
    return countdown;
}

void ServerGroupConfig::setCountdown(const boost::uint32_t &value)
{
    countdown = value;
}
std::string ServerGroupConfig::getServerOptions() const
{
    return serverOptions;
}

void ServerGroupConfig::setServerOptions(const std::string &value)
{
    serverOptions = value;
}
}
