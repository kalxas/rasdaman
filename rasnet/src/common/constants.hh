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
#ifndef RASNET_SRC_CONSTANTS_HH_
#define RASNET_SRC_CONSTANTS_HH_

#include <boost/cstdint.hpp>

//The default number of IO threads that a client channel
//should use 1 thread/(1GB/s)
const boost::int32_t DEFAULT_CHANNEL_IO_THREAD_NO = 1;

//Number of miliseconds after which, a non-responding
//client's lifetime can be reduced
const boost::int32_t DEFAULT_CLIENT_ALIVE_TIMEOUT = 1000;

//Default number of times a client's liveliness can be reduced
const boost::int32_t DEFAULT_CLIENT_ALIVE_RETRIES = 3;

//Default number of miliseconds after which a channel
//should give up trying to connect to a server
const boost::int32_t DEFAULT_CHANNEL_TIMEOUT = 1000;

const boost::int32_t DEFAULT_CHANNEL_MAX_OPEN_SOCKETS = 2048;

const boost::int32_t DEFAULT_SERVER_MAX_OPEN_SOCKETS = 2048;

//Default number of miliseconds after which, a non-responding
//server's lifetime can be reduced
const boost::int32_t DEFAULT_SERVER_ALIVE_TIMEOUT = 1000;

//Default number of times a server's liveliness can be reduced
const boost::int32_t DEFAULT_SERVER_LIVES = 3;

const boost::int32_t DEFAULT_SERVER_IO_THREADS_NO = 1;

const boost::int32_t DEFAULT_SERVER_CPU_THREADS_NO = 4;

#endif // RASNET_SRC_CONSTANTS_HH_
