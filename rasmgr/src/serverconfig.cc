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

ServerConfig::ServerConfig(const std::string &hostName1, const std::uint32_t &port1, std::shared_ptr<DatabaseHost> dbHost1):
    hostName(hostName1), port(port1), dbHost(dbHost1)
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
std::uint32_t ServerConfig::getPort() const
{
    return port;
}

void ServerConfig::setPort(const std::uint32_t &value)
{
    port = value;
}

std::shared_ptr<DatabaseHost> ServerConfig::getDbHost() const
{
    return dbHost;
}

void ServerConfig::setDbHost(const std::shared_ptr<DatabaseHost> &value)
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
