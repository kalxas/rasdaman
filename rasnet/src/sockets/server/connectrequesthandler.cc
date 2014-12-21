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

/* SOURCE: ConnectRequestHandler.cc
 *
 * MODULE:  communication/socket/server
 * CLASS:   ConnectRequestHandler
 *
 * COMMENTS: The ConnectRequestHandler is used to handle an incoming ConnectRequest message.
 * When the handle method is called, the client that sent the request is added to the pool of clients.
 *
 *
 *
 */
#include <stdexcept>

#include "../../../../common/src/logging/easylogging++.hh"

#include "../../../src/util/proto/protozmq.hh"
#include "../../../src/messages/communication.pb.h"

#include "connectrequesthandler.hh"

namespace rasnet
{
ConnectRequestHandler::ConnectRequestHandler(boost::shared_ptr<ClientPool> clientPool, boost::int32_t retries, boost::int32_t period)
{
    this->clientPool = clientPool;
    this->reply.set_retries(retries);
    this->reply.set_period(period);
    this->messageType = ExternalConnectRequest::default_instance().GetTypeName();
}

ConnectRequestHandler::~ConnectRequestHandler()
{}

bool ConnectRequestHandler::canHandle(const base::BaseMessage& message)
{
    return (message.type() == this->messageType);
}

void ConnectRequestHandler::handle(const base::BaseMessage& message,
                                   const std::string& peerId, zmq::socket_t& socket)
{
    if (this->canHandle(message))
    {
        ExternalConnectRequest req;
        if (req.ParseFromString(message.data()))
        {
            this->clientPool->addClient(peerId, req.period(), req.retries());
            ProtoZmq::zmqSendToPeer(socket, this->reply, peerId);
        }
    }
    else
    {
        throw std::runtime_error("Unexpected message type.");
    }
}

} /* namespace rnp */
