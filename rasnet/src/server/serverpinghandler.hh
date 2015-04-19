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

#ifndef RASNET_SRC_SERVER_SERVERPINGHANDLER_HH
#define RASNET_SRC_SERVER_SERVERPINGHANDLER_HH

#include <vector>
#include <boost/shared_ptr.hpp>

#include "../../../common/src/zeromq/zmq.hh"

namespace rasnet
{
class ServerPingHandler
{
public:
    ServerPingHandler(zmq::socket_t& socket);

    virtual ~ServerPingHandler();

    /**
     * @brief canHandle Check if the message can be handled by this handler
     * @param message
     * @return TRUE if the messages can be handled, FALSE otherwise
     */
    bool canHandle(const std::vector<boost::shared_ptr<zmq::message_t> >&  message);

    /**
     * @brief handle Handle the given message and send the an ALIVE_PONG
     * message through the socket
     * @param message
     * @param socket The socket connected to the server
     * @throws UnsupportedMessageException if an invalid message is passed in.
     * i.e. one for which canHandle returns false
     */
    void handle(const std::vector<boost::shared_ptr<zmq::message_t> >&  message, const std::string& peerId);
private:
    zmq::socket_t& socket;
};
}
#endif // RASNET_SRC_SERVER_SERVERPINGHANDLER_HH
