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

/* SOURCE: ClientPingHandler.cc
 * MODULE:  communication
 * CLASS:   ClientPingHandler
 *
 * COMMENTS:
 *      The ClientPingHandler responds to a Ping message from the server with a Pong message.
 *
 */

#include <stdexcept>

#include "../../../../common/src/zeromq/zmq.hh"
#include "../../util/proto/protozmq.hh"

#include "clientpinghandler.hh"

namespace rasnet
{
using std::runtime_error;

ClientPingHandler::ClientPingHandler()
{
    this->messageType=AlivePing::default_instance().GetTypeName();
}

ClientPingHandler::~ClientPingHandler()
{}

bool ClientPingHandler::canHandle(const base::BaseMessage& message)
{
    return (message.type() == this->messageType);
}

void ClientPingHandler::handle(const base::BaseMessage& message,
                               zmq::socket_t& socket)
{
    if (this->canHandle(message))
    {
        ProtoZmq::zmqSend(socket, this->pong);
    }
    else
    {
        throw runtime_error("Unexpected message type");
    }
}
}
