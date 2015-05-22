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
 * Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2014, 2015 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package org.rasdaman.rasnet.client;

import org.rasdaman.rasnet.common.Constants;

/**
 * @brief The ChannelConfig class Configuration object used for initializing
 * a Channel @see Channel
 */
public class ChannelConfig {
    /**
     * @brief connectionTimeout Number of milliseconds after which a Channel trying to connect
     * to a non-responding server gives up
     */
    private int connectionTimeout;

    /**
     * @brief aliveTimeout Number of milliseconds after which the life of a non-responding
     * Channel (one that does not reply to ALIVE_PONG) should be decremented
     */
    private int aliveTimeout;

    /**
     * @brief aliveRetryNo Number of lives a non-responding Channel has before being declared
     * dead and being removed from the server
     */
    private int aliveRetryNo;

    /**
     * @brief maxOpenSockets Maximum number of open sockets per Channel context.
     * see ZMQ documentation
     */
    private int maxOpenSockets;

    /**
     * @brief numberOfIoThreads The number of IO threads that should be used by the channel
     * to process messages. This parameter is used by the ZMQ.Context used inside the channel.
     */
    private int numberOfIoThreads;

    public ChannelConfig() {
        this.connectionTimeout = Constants.DEFAULT_CHANNEL_TIMEOUT;
        this.aliveTimeout = Constants.DEFAULT_CLIENT_ALIVE_TIMEOUT;
        this.aliveRetryNo = Constants.DEFAULT_CLIENT_ALIVE_RETRIES;
        this.numberOfIoThreads = Constants.DEFAULT_CHANNEL_IO_THREAD_NO;
        this.maxOpenSockets = Constants.DEFAULT_CHANNEL_MAX_OPEN_SOCKETS;
    }

    public int getAliveRetryNo() {
        return aliveRetryNo;
    }

    public void setAliveRetryNo(int aliveRetryNo) {
        this.aliveRetryNo = aliveRetryNo;
    }

    public int getAliveTimeout() {
        return aliveTimeout;
    }

    public int getNumberOfIoThreads() {
        return numberOfIoThreads;
    }

    public void setNumberOfIoThreads(int numberOfIoThreads) {
        this.numberOfIoThreads = numberOfIoThreads;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getMaxOpenSockets() {
        return maxOpenSockets;
    }

    public void setMaxOpenSockets(int maxOpenSockets) {
        this.maxOpenSockets = maxOpenSockets;
    }

}
