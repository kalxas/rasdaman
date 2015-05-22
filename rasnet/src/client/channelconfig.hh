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

#ifndef RASNET_SRC_CLIENT_CHANNELCONFIG_HH_
#define RASNET_SRC_CLIENT_CHANNELCONFIG_HH_

#include <string>

#include <boost/cstdint.hpp>

namespace rasnet
{
/**
 * @brief The ChannelConfig class Configuration object used for initializing
 * a Channel @see Channel
 */
class ChannelConfig
{
public:
    ChannelConfig();

    boost::int32_t getConnectionTimeout() const;
    void setConnectionTimeout(const boost::int32_t& connectionTimeout);

    boost::int32_t getAliveRetryNo() const;
    void setAliveRetryNo(const boost::int32_t& aliveRetryNo);

    boost::int32_t getAliveTimeout() const;
    void setAliveTimeout(const boost::int32_t& aliveTimeout);

    boost::int32_t getNumberOfIoThreads() const;
    void setNumberOfIoThreads(const boost::int32_t& timeout);

    boost::int32_t getMaxOpenSockets() const;
    void setMaxOpenSockets(const boost::int32_t& maxOpenSockets);

private:
    /**
     * @brief numberOfIoThreads The number of IO threads that should be used by the channel
     * to process messages. This parameter is used by the ZMQ.Context used inside the channel.
     */
    boost::int32_t numberOfIoThreads;

    /**
     * @brief aliveTimeout Number of milliseconds after which the life of a non-responding
     * Channel (one that does not reply to ALIVE_PONG) should be decremented
     */
    boost::int32_t aliveTimeout;

    /**
     * @brief aliveRetryNo Number of lives a non-responding Channel has before being declared
     * dead and being removed from the server
     */
    boost::int32_t aliveRetryNo;

    /**
     * @brief connectionTimeout Number of milliseconds after which a Channel trying to connect
     * to a non-responding server gives up
     */
    boost::int32_t connectionTimeout;

    /**
     * @brief maxOpenSockets Maximum number of open sockets per Channel context.
     * see ZMQ documentation
     */
    boost::int32_t maxOpenSockets;
};

} /* namespace rnp */

#endif /* RASNET_SRC_CLIENT_CHANNELCONFIG_HH_ */
