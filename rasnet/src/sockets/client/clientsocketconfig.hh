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

/* SOURCE: ClientSocketConfig.hh
 *
 * MODULE:
 * CLASS:
 *
 * COMMENTS:
 *
 *
 */
#ifndef RASNET_SRC_SOCKETS_CLIENT_CLIENTSOCKETCONFIG_HH_
#define RASNET_SRC_SOCKETS_CLIENT_CLIENTSOCKETCONFIG_HH_

#include <boost/cstdint.hpp>
#include <boost/smart_ptr.hpp>
#include <boost/thread.hpp>

namespace rasnet
{
class ClientSocketConfig
{
public:
    ClientSocketConfig();
    virtual ~ClientSocketConfig();

    void setAliveTimeout(boost::int32_t aliveTimeout);
    boost::int32_t getAliveTimeout() const;

    void setAliveRetryNo(boost::int32_t aliveRetryNo);
    boost::int32_t getAliveRetryNo() const;

    boost::int32_t getNumberOfIoThreads() const;
    void setNumberOfIoThreads(boost::int32_t threadNo);

    boost::int32_t getConnectionTimeout() const;
    void setConnectionTimeout(boost::int32_t connectionTimeout);

private:
    boost::int32_t connectionTimeout; /*!< Number of milliseconds before a ClientSocket should give up on attempting to connect to server socket */
    boost::int32_t aliveTimeout;
    boost::int32_t aliveRetryNo;
    boost::int32_t numberOfIoThreads;
};

} /* namespace rasnet */

#endif /* CLIENTSOCKETCONFIG_HH_ */
