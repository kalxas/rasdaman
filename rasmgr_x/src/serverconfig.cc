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

#include "serverconfig.hh"

namespace rasmgr
{

ServerConfig::ServerConfig(const std::string &hostName, const boost::int32_t &port, boost::shared_ptr<DatabaseHost> dbHost):
    hostName(hostName), port(port), dbHost(dbHost)
{}


ServerConfig::~ServerConfig()
{}

std::string ServerConfig::getHostName() const
{
    return hostName;
}

void ServerConfig::setHostName(const std::string &value)
{
    hostName = value;
}
boost::int32_t ServerConfig::getPort() const
{
    return port;
}

void ServerConfig::setPort(const boost::int32_t &value)
{
    port = value;
}

boost::shared_ptr<DatabaseHost> ServerConfig::getDbHost() const
{
    return dbHost;
}

void ServerConfig::setDbHost(const boost::shared_ptr<DatabaseHost> &value)
{
    dbHost = value;
}

std::string ServerConfig::getOptions() const
{
    return options;
}

void ServerConfig::setOptions(const std::string &value)
{
    options = value;
}

}
