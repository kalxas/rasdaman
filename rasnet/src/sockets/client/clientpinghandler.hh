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

/* INCLUDE: ClientPingHandler.hh
 *
 * MODULE:  communication/socket/client
 * CLASS:   ClientPingHandler
 *
 * COMMENTS:
 *      The ClientPingHandler responds to a Ping message from the server with a Pong message.
 *
 */

#ifndef RASNET_SRC_SOCKETS_CLIENT_CLIENTPINGHANDLER_HH_
#define RASNET_SRC_SOCKETS_CLIENT_CLIENTPINGHANDLER_HH_

#include <string>

#include "../../../src/messages/communication.pb.h"

#include "clientcommunicationhandler.hh"

namespace rasnet
{
class ClientPingHandler: public ClientCommunicationHandler
{
public:
	ClientPingHandler();

    virtual ~ClientPingHandler();

    virtual bool canHandle(const base::BaseMessage& message);

    virtual void handle(const base::BaseMessage& message, zmq::socket_t& socket);
private:
    std::string messageType;
    AlivePong pong;
};
}

#endif /* RASNET_SOCKETS_CLIENT_CLIENTPINGHANDLER_HH_ */
