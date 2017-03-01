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
#include <boost/thread/locks.hpp>

#include <easylogging++.h>

#include "rasmgrconfig.hh"

namespace rasmgr
{

boost::mutex RasMgrConfig::instanceMutex;
boost::shared_ptr<RasMgrConfig> RasMgrConfig::instance;

boost::shared_ptr<RasMgrConfig> RasMgrConfig::getInstance()
{
    boost::unique_lock<boost::mutex> lock(RasMgrConfig::instanceMutex);
    if (!instance)
    {
        LDEBUG << "Started RasMgrConfig initialization";
        instance.reset(new RasMgrConfig());
        LDEBUG << "Finished RasMgrConfig initialization";
    }

    return RasMgrConfig::instance;
}

RasMgrConfig::RasMgrConfig()
{
    //TODO Read these from a configuration file.
    this->clientLifeTime = 3000;
    this->clientManagementGarbageCollectionInterval = 600000;
    this->serverManagementGarbageCollectionInterval = 300000;
    this->rasServerTimeout = 3000;
    this->rasMgrPort = 7001;
    this->maximumNumberOfClientsPerServer = 1;
    this->clientGetServerRetryNo = 3;
    this->clientGetServerRetryTimeout = 1000;

    //TODO:This must be given as a parameter at runtime
    this->rasServerExecPath = BINDIR"rasserver";

    this->connectHostName = "localhost";
}

RasMgrConfig::~RasMgrConfig()
{}


boost::int32_t RasMgrConfig::getRasMgrPort()
{
    return this->rasMgrPort;
}

void RasMgrConfig::setRasMgrPort(boost::int32_t value)
{
    this->rasMgrPort = value;
}

std::string RasMgrConfig::getConnectHostName()
{
    return this->connectHostName;
}

std::string RasMgrConfig::getRasServerExecPath()
{
    return this->rasServerExecPath;
}

boost::uint32_t RasMgrConfig::getMaximumNumberOfClientsPerServer()
{
    return this->maximumNumberOfClientsPerServer;
}

boost::uint32_t RasMgrConfig::getClientGetServerRetryNo()
{
    return this->clientGetServerRetryNo;
}

boost::int32_t RasMgrConfig::getClientGetServerRetryTimeout()
{
    return this->clientGetServerRetryTimeout;
}

}
