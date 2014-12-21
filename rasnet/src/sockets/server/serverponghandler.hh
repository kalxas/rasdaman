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

/* SOURCE: ServerPongHandler.hh
 *
 * MODULE:  communication/socket/server
 * CLASS:   ServerPongHandler
 *
 * COMMENTS: The ServerPongHandler is used to handle AlivePong messages.
 * Everytime an AlivePong message is handled, the status of the client is reset.
 *
 *
 */
#ifndef RASNET_SRC_SOCKETS_SERVER_SERVERPONGHANDLER_HH_
#define RASNET_SRC_SOCKETS_SERVER_SERVERPONGHANDLER_HH_

#include <string>

#include <boost/shared_ptr.hpp>

#include "../../../src/sockets/server/clientpool.hh"
#include "../../../src/messages/communication.pb.h"

#include "servercommunicationhandler.hh"

namespace rasnet
{

class ServerPongHandler: public ServerCommunicationHandler
{
public:
    ServerPongHandler(boost::shared_ptr<ClientPool> clientPool);

    virtual ~ServerPongHandler();

    virtual bool canHandle(const base::BaseMessage& message);

    virtual void handle(const base::BaseMessage& message,
                        const std::string& peerId, zmq::socket_t& socket);

private:
    boost::shared_ptr<ClientPool> clientPool; /*!< Pointer to the ClientPool that contains all the clients we can receive messages from */

    std::string messageType;/*!< Cached representation of the message type this handler can handler */
};

} /* namespace rnp */

#endif /* RASNET_SOCKETS_SERVER_SERVERPONGHANDLER_HH_ */
