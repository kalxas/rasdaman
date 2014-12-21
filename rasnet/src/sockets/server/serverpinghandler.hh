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

/* INCLUDE: ServerPingHandler.hh
 *
 * MODULE:  zmq_components/server
 * CLASS:   ServerPingHandler
 *
 * COMMENTS:
 *      The ServerPingHandler responds to a Ping message from a client with a Pong message.
 *
 */
#ifndef RASNET_SRC_SOCKETS_SERVER_SERVERPINGHANDLER_HH_
#define RASNET_SRC_SOCKETS_SERVER_SERVERPINGHANDLER_HH_

#include <string>

#include "../../../src/messages/communication.pb.h"
#include "servercommunicationhandler.hh"

namespace rasnet
{
class ServerPingHandler: public ServerCommunicationHandler
{
public:
    ServerPingHandler();

    virtual ~ServerPingHandler();

    virtual bool canHandle(const base::BaseMessage& message);

    virtual void handle(const base::BaseMessage& message,
                        const std::string& peerId, zmq::socket_t& socket);


private:
    AlivePong pong; /*!<Cached pong message to be sent everytime we handle an AlivePing message */
    std::string messageType; /*!< Cached representation of the message type this handler can handler */
};
}

#endif /* RASNET_SOCKETS_SERVER_SERVERPINGHANDLER_HH_ */
