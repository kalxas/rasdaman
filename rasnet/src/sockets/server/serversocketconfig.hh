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

/* SOURCE: ServerSocketConfig.hh
 *
 * MODULE:
 * CLASS:
 *
 * COMMENTS:
 *
 *
 */
#ifndef RASNET_SOCKETS_SERVER_SERVERSOCKETCONFIG_HH_
#define RASNET_SOCKETS_SERVER_SERVERSOCKETCONFIG_HH_

#include <boost/cstdint.hpp>

namespace rasnet
{

class ServerSocketConfig
{
public:
	ServerSocketConfig();

    void setProxyTimeout(boost::int32_t proxyTimeout);
    boost::int32_t getProxyTimeout() const;

    void setAliveTimeout(boost::int32_t aliveTimeout);
    boost::int32_t getAliveTimeout() const;

    void setAliveRetryNo(boost::int32_t aliveRetryNo);
    boost::int32_t getAliveRetryNo() const;

private:
    boost::int32_t proxyTimeout;/*! */
    boost::int32_t aliveTimeout;
    boost::int32_t aliveRetryNo;


};

} /* namespace rnp */

#endif /* RASNET_SOCKETS_SERVER_SERVERSOCKETCONFIG_HH_ */
