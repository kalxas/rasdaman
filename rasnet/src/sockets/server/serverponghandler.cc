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

/* SOURCE: ServerPongHandler.cc
 *
 * MODULE:  communication/socket/server
 * CLASS:   ServerPongHandler
 *
 * COMMENTS: The ServerPongHandler is used to handle AlivePong messages.
 * Everytime an AlivePong message is handled, the status of the client is reset.
 *
 *
 */
#include <stdexcept>

#include "serverponghandler.hh"

namespace rasnet
{
ServerPongHandler::ServerPongHandler(boost::shared_ptr<ClientPool> clientPool)
{
    this->clientPool = clientPool;
    this->messageType = AlivePong::default_instance().GetTypeName();
}

ServerPongHandler::~ServerPongHandler()
{}

bool ServerPongHandler::canHandle(const base::BaseMessage& message)
{
    return (message.type() == this->messageType);
}

void ServerPongHandler::handle(const base::BaseMessage& message,
                               const std::string& peerId, zmq::socket_t& socket)
{
    if(this->canHandle(message))
    {
        this->clientPool->resetClientStatus(peerId);
    }
    else
    {
        throw std::runtime_error("Unsupported message type");
    }
}

} /* namespace rasnet */
