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
package org.rasdaman.rasnet.common;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public final class Constants {
    /**
     * @brief DEFAULT_CHANNEL_IO_THREAD_NO The default number of I/O threads
     * that a client channel should use.The rule is to have 1 thread for each
     * 1GB/s of data
     */
    public static final int DEFAULT_CHANNEL_IO_THREAD_NO = 1;

    /**
     * @brief DEFAULT_CLIENT_ALIVE_TIMEOUT Number of milliseconds after which
     * the life of a non-responding server (one that does not reply to ALIVE_PONG)
     * should be decremented
     */
    public static final int DEFAULT_CLIENT_ALIVE_TIMEOUT = 1000;

    /**
     * @brief DEFAULT_CLIENT_ALIVE_RETRIES Number of lives a non-responding Channel
     *  has before being declared dead and being removed from the server
     */
    public static final int DEFAULT_CLIENT_ALIVE_RETRIES = 3;

    /**
     * @brief DEFAULT_CHANNEL_TIMEOUT Number of milliseconds after
     * which a Channel trying to connect to a non-responding server gives up
     */
    public static final int DEFAULT_CHANNEL_TIMEOUT = 1000;

    /**
     * @brief DEFAULT_CHANNEL_MAX_OPEN_SOCKETS
     * Maximum number of open sockets per Channel context. see ZMQ documentation
     */
    public static final int DEFAULT_CHANNEL_MAX_OPEN_SOCKETS = 2048;

    /**
     * @brief DEFAULT_SERVER_MAX_OPEN_SOCKETS
     * Maximum number of open sockets per ServiceManager context. see ZMQ documentation
     */
    public static final int DEFAULT_SERVER_MAX_OPEN_SOCKETS = 2048;

    /**
     * @brief DEFAULT_SERVER_ALIVE_TIMEOUT Default number of miliseconds after which, a non-responding
     * server's lifetime can be reduced
     */
    public static final int DEFAULT_SERVER_ALIVE_TIMEOUT = 1000;

    /**
     * @brief DEFAULT_SERVER_LIVES Number of lives a non-responding Channel
     * has before being declared dead and being removed from the server
     */
    public static final int DEFAULT_SERVER_LIVES = 3;

    /**
     * @brief DEFAULT_SERVER_IO_THREADS_NO The default number of I/O threads
     * that a server should use.The rule is to have 1 thread for each
     * 1GB/s of data
     */
    public static final int DEFAULT_SERVER_IO_THREADS_NO = 1;

    /**
     * @brief DEFAULT_SERVER_CPU_THREADS_NO The default number of CPU threads
     * that a server should use. Each CPU thread can be used to server one client request
     * When the number of requests exceeds the number of CPU threads, the requests are queued
     */
    public static final int DEFAULT_SERVER_CPU_THREADS_NO = 1;

    /**
     * @brief INTER_THREAD_COMMUNICATION_TIMEOUT Number of milliseconds
     * after which inter-thread communication through a ZMQ socket should be aborted.
     * When thread A wants to send a message to thread B through a ZMQ_PAIR socket,
     * this timeout is used to allow thread B to create and listen on the receiving socket.
     * This method assumes that the method in thread B immediately creates the listening
     * socket upon starting execution
     */
    public static final int INTER_THREAD_COMMUNICATION_TIMEOUT = 1000;

    public static final Charset DEFAULT_ENCODING = StandardCharsets.UTF_8;

    private Constants() {

    }
}
