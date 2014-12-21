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

#include "channelconfig.hh"

namespace rasnet
{

ChannelConfig::ChannelConfig()
{
    this->channelTimeout=1;
    this->numberOfIoThreads = 1;
    this->aliveTimeout = 1000;
    this->aliveRetryNo = 3;
    this->connectionTimeout = 1000;
}

boost::int32_t ChannelConfig::getChannelTimeout() const
{
    return this->channelTimeout;
}

void ChannelConfig::setChannelTimeout(boost::int32_t timeout)
{
    this->channelTimeout = timeout;
}

boost::int32_t  ChannelConfig::getNumberOfIoThreads() const
{
    return this->numberOfIoThreads;

}
void ChannelConfig::setNumberOfIoThreads(boost::int32_t threadNo)
{
    this->numberOfIoThreads=threadNo;
}

void  ChannelConfig::setAliveTimeout(boost::int32_t aliveTimeout)
{
    this->aliveTimeout=aliveTimeout;
}

boost::int32_t ChannelConfig::getAliveTimeout() const
{
    return this->aliveTimeout;
}

void ChannelConfig::setAliveRetryNo(boost::int32_t aliveRetryNo)
{
    this->aliveRetryNo=aliveRetryNo;
}

boost::int32_t ChannelConfig::getAliveRetryNo() const
{
    return this->aliveRetryNo;
}
boost::int32_t ChannelConfig::getConnectionTimeout() const
{
    return this->connectionTimeout;
}

void ChannelConfig::setConnectionTimeout(boost::int32_t connectionTimeout)
{
    this->connectionTimeout=connectionTimeout;
}

} /* namespace rnp */
