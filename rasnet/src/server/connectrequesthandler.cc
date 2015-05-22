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

#include "../../../common/src/logging/easylogging++.hh"
#include "../exception/unsupportedmessageexception.hh"
#include "../common/zmqutil.hh"
#include "connectrequesthandler.hh"

namespace rasnet
{

ConnectRequestHandler::ConnectRequestHandler(zmq::socket_t &socket, boost::shared_ptr<ClientPool> clientPool, int32_t retries, int32_t lifetime):
    socket(socket), clientPool(clientPool)
{
    this->connectReply.set_lifetime(lifetime);
    this->connectReply.set_retries(retries);
}

ConnectRequestHandler::~ConnectRequestHandler()
{
}

bool ConnectRequestHandler::canHandle(const std::vector<boost::shared_ptr<zmq::message_t> > &message)
{
    MessageType messageType;

    if(message.size()==2
            && messageType.ParseFromArray(message[0]->data(), message[0]->size())
            && messageType.type() == MessageType::CONNECT_REQUEST)
    {
        return true;
    }
    else
    {
        return false;
    }
}

void ConnectRequestHandler::handle(const std::vector<boost::shared_ptr<zmq::message_t> > &message, const std::string &peerId)
{
    if(this->canHandle(message))
    {
        ConnectRequest request;
        if(request.ParseFromArray(message[1]->data(), message[1]->size()))
        {
            this->clientPool->addClient(peerId, request.lifetime(), request.retries());

            if(!ZmqUtil::sendCompositeMessageToPeer(socket, peerId, MessageType::CONNECT_REPLY, connectReply))
            {
                LERROR<<"Failed to send connect reply to client";
            }
        }
        else
        {
            LERROR<<"Received unparsable ConnectRequest message";
        }
    }
    else
    {
        throw UnsupportedMessageException();
    }
}

}
