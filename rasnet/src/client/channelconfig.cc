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

#include "../common/constants.hh"

#include "channelconfig.hh"

namespace rasnet
{

ChannelConfig::ChannelConfig()
{
    this->numberOfIoThreads = DEFAULT_CHANNEL_IO_THREAD_NO;
    this->aliveTimeout = DEFAULT_CLIENT_ALIVE_TIMEOUT;
    this->aliveRetryNo = DEFAULT_CLIENT_ALIVE_RETRIES;
    this->connectionTimeout = DEFAULT_CHANNEL_TIMEOUT;
    this->maxOpenSockets = DEFAULT_CHANNEL_MAX_OPEN_SOCKETS;
}

boost::int32_t ChannelConfig::getConnectionTimeout() const
{
    return this->connectionTimeout;
}

void ChannelConfig::setConnectionTimeout(const boost::int32_t & connectionTimeout)
{
    this->connectionTimeout=connectionTimeout;
}

boost::int32_t ChannelConfig::getAliveRetryNo() const
{
    return this->aliveRetryNo;
}

void ChannelConfig::setAliveRetryNo(const boost::int32_t & aliveRetryNo)
{
    this->aliveRetryNo=aliveRetryNo;
}

boost::int32_t ChannelConfig::getAliveTimeout() const
{
    return this->aliveTimeout;
}

void  ChannelConfig::setAliveTimeout(const boost::int32_t & aliveTimeout)
{
    this->aliveTimeout=aliveTimeout;
}

boost::int32_t  ChannelConfig::getNumberOfIoThreads() const
{
    return this->numberOfIoThreads;
}

void ChannelConfig::setNumberOfIoThreads(const boost::int32_t & threadNo)
{
    this->numberOfIoThreads=threadNo;
}

boost::int32_t ChannelConfig::getMaxOpenSockets() const
{
    return maxOpenSockets;
}

void ChannelConfig::setMaxOpenSockets(const boost::int32_t &value)
{
    maxOpenSockets = value;
}

} /* namespace rnp */
