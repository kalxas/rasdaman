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

/* SOURCE: ServerSocketConfig.cc
 *
 * MODULE:
 * CLASS:
 *
 * COMMENTS:
 *
 *
 */
#include "serversocketconfig.hh"

namespace rasnet
{
ServerSocketConfig::ServerSocketConfig()
{
    this->aliveRetryNo=3;
    this->aliveTimeout=3000;
    this->proxyTimeout=1000;
}
void ServerSocketConfig::setProxyTimeout(boost::int32_t proxyTimeout)
{
    this->proxyTimeout=proxyTimeout;
}

boost::int32_t ServerSocketConfig::getProxyTimeout() const
{
    return this->proxyTimeout;
}

void  ServerSocketConfig::setAliveTimeout(boost::int32_t aliveTimeout)
{
    this->aliveTimeout=aliveTimeout;
}

boost::int32_t ServerSocketConfig::getAliveTimeout() const
{
    return this->aliveTimeout;
}

void ServerSocketConfig::setAliveRetryNo(boost::int32_t aliveRetryNo)
{
    this->aliveRetryNo=aliveRetryNo;
}

boost::int32_t ServerSocketConfig::getAliveRetryNo() const
{
    return this->aliveRetryNo;
}

} /* namespace rasnet */
