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

/* SOURCE: ServerPingHandler.cc
 *
 * MODULE:
 * CLASS:   ServerPingHandler
 *
 * COMMENTS:
 *      The ServerPingHandler responds to a Ping message from a client with a Pong message.
 *
 */
#include <stdexcept>

#include "../../../src/util/proto/protozmq.hh"

#include "serverpinghandler.hh"

namespace rasnet
{

using std::runtime_error;

ServerPingHandler::ServerPingHandler()
{
    this->pong = AlivePong::default_instance();
    this->messageType = AlivePing::default_instance().GetTypeName();
}

ServerPingHandler::~ServerPingHandler()
{}

bool ServerPingHandler::canHandle(const base::BaseMessage& message)
{
    return (message.type()
            == this->messageType);
}

void ServerPingHandler::handle(const base::BaseMessage& message,
                               const std::string& peerId, zmq::socket_t& socket)
{
    if (this->canHandle(message))
    {
        //If we receive a ping, we just reply with a pong
        ProtoZmq::zmqSendToPeer(socket, pong, peerId);
    }
    else
    {
        throw runtime_error("Unexpected message type.");
    }
}
}
