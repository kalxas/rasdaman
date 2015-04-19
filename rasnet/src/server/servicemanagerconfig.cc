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

#include "servicemanagerconfig.hh"

namespace rasnet
{

rasnet::ServiceManagerConfig::ServiceManagerConfig()
{
    this->aliveTimeout = DEFAULT_SERVER_ALIVE_TIMEOUT;
    this->aliveRetryNo = DEFAULT_SERVER_LIVES;
    this->ioThreadsNo = DEFAULT_SERVER_IO_THREADS_NO;
    this->cpuThreadsNo = DEFAULT_SERVER_CPU_THREADS_NO;
    this->maxOpenSockets = DEFAULT_SERVER_MAX_OPEN_SOCKETS;
}

boost::int32_t ServiceManagerConfig::getAliveTimeout() const
{
    return aliveTimeout;
}

void ServiceManagerConfig::setAliveTimeout(const boost::int32_t &value)
{
    aliveTimeout = value;
}

boost::int32_t ServiceManagerConfig::getAliveRetryNo() const
{
    return aliveRetryNo;
}

void ServiceManagerConfig::setAliveRetryNo(const boost::int32_t &value)
{
    aliveRetryNo = value;
}
boost::int32_t ServiceManagerConfig::getIoThreadsNo() const
{
    return ioThreadsNo;
}

void ServiceManagerConfig::setIoThreadsNo(const boost::int32_t &value)
{
    ioThreadsNo = value;
}
boost::uint32_t ServiceManagerConfig::getCpuThreadsNo() const
{
    return cpuThreadsNo;
}

void ServiceManagerConfig::setCpuThreadsNo(const boost::uint32_t &value)
{
    cpuThreadsNo = value;
}
boost::int32_t ServiceManagerConfig::getMaxOpenSockets() const
{
    return maxOpenSockets;
}

void ServiceManagerConfig::setMaxOpenSockets(const boost::int32_t &value)
{
    maxOpenSockets = value;
}


}
