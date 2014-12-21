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

/* SOURCE: ConnectRequestHandler.hh
 *
 * MODULE:
 * CLASS:   ConnectRequestHandler
 *
 * COMMENTS: The ConnectRequestHandler is used to handle an incoming ConnectRequest message.
 * When the handle method is called, the client that sent the request is added to the pool of clients.
 *
 *
 *
 */
#ifndef RASNET_SOCKETS_SERVER_CONNECTREQUESTHANDLER_HH_
#define RASNET_SOCKETS_SERVER_CONNECTREQUESTHANDLER_HH_

#include <string>
#include <boost/shared_ptr.hpp>
#include <boost/cstdint.hpp>

#include "../../../src/messages/communication.pb.h"

#include "servercommunicationhandler.hh"
#include "clientpool.hh"

namespace rasnet
{

class ConnectRequestHandler: public ServerCommunicationHandler
{
public:
    /**
     * Create a ClientRequestHandler
     * @param pool Reference to the pool of clients owned by the ServerProxy using this handler.
     * @param retries The number of times a client should try to contact the server before giving up
     * @param period The number of milliseconds between each consecutive retry.
     */
    ConnectRequestHandler(boost::shared_ptr<ClientPool> clientPool, boost::int32_t retries, boost::int32_t period);

    virtual ~ConnectRequestHandler();

    /**
     * Decide if the given message can be processed by this handler.
     * @param message
     * @return true if the message contains a ConnectRequest message, false otherwise
     */
    virtual bool canHandle(const base::BaseMessage& message);

    /**
     * Handle the ConnectRequest message contained in the BaseMessage by adding the client to the pool
     * and sending back a ConnectReply message.
     * @param message
     * @param peer_id
     * @param socket
     */
    virtual void handle(const base::BaseMessage& message,
                        const std::string& peer_id, zmq::socket_t& socket);

private:
    boost::shared_ptr<ClientPool> clientPool;/*!< Pool of clients.*/

    ExternalConnectReply reply; /*!< ConnectReply message to be sent to each peer that sends a ConnectRequest*/

    std::string messageType; /*!< String representing the message type that this handler can process */
};

} /* namespace rnp */

#endif /* RASNET_SOCKETS_SERVER_CONNECTREQUESTHANDLER_HH_ */
