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

/* SOURCE: ClientSocketConfig.cc
 *
 * MODULE:
 * CLASS:
 *
 * COMMENTS:
 *
 *
 */
#include "../../../../common/src/logging/easylogging++.hh"

#include "clientsocketconfig.hh"

namespace rasnet
{

ClientSocketConfig::ClientSocketConfig()
{
    this->connectionTimeout=1000;
    this->aliveRetryNo=3;
    this->aliveTimeout=1000;
    this->numberOfIoThreads=1;
}

ClientSocketConfig::~ClientSocketConfig()
{}


boost::int32_t ClientSocketConfig::getConnectionTimeout() const
{
    return this->connectionTimeout;
}

void ClientSocketConfig::setConnectionTimeout(boost::int32_t connectionTimeout)
{
    this->connectionTimeout=connectionTimeout;
}

void  ClientSocketConfig::setAliveTimeout(boost::int32_t aliveTimeout)
{
    this->aliveTimeout=aliveTimeout;
}

boost::int32_t ClientSocketConfig::getAliveTimeout() const
{
    return this->aliveTimeout;
}

void ClientSocketConfig::setAliveRetryNo(boost::int32_t aliveRetryNo)
{
    this->aliveRetryNo=aliveRetryNo;
}

boost::int32_t ClientSocketConfig::getAliveRetryNo() const
{
    return this->aliveRetryNo;
}

boost::int32_t  ClientSocketConfig::getNumberOfIoThreads() const
{
    return this->numberOfIoThreads;

}
void ClientSocketConfig::setNumberOfIoThreads(boost::int32_t threadNo)
{
    this->numberOfIoThreads=threadNo;
}

} /* namespace rasnet */
