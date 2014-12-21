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

/* INCLUDE:
 *
 * MODULE:
 * CLASS:
 *
 * COMMENTS: ServerProxyHandler is an abstract class that represents a message handler that is used in the ServerProxy.
 * It should handle only connection related messages.
 *
 *
 */

#ifndef RASNET_SRC_SOCKETS_SERVER_SERVERCOMMUNICATIONHANDLER_HH_
#define RASNET_SRC_SOCKETS_SERVER_SERVERCOMMUNICATIONHANDLER_HH_

#include <string>

#include "../../../../common/src/zeromq/zmq.hh"
#include "../../../src/messages/base.pb.h"

namespace rasnet
{
class ServerCommunicationHandler
{
public:
    virtual ~ServerCommunicationHandler();

    /**
     * Check if this ServerProxyHandler can handle the message encapsulated in the BaseMessage
     * @param message BaseMessage that contains another protobuf message
     * @return true if the message can be handled, false otherwise
     */
    virtual bool canHandle(const base::BaseMessage& message)=0;

    /**
     * Handle the message.
     * @param message BaseMessage that contains the protobuf message that we have to handle
     * @param socket zmq socket that can be used to reply to the message we are handling
     * @param peerId Id of the peer that sent the message.
     */
    virtual void handle(const base::BaseMessage& message,
                        const std::string& peerId, zmq::socket_t& socket)=0;
};
}
#endif /* RASNET_SOCKETS_SERVER_SERVERCOMMUNICATIONHANDLER_HH_ */
