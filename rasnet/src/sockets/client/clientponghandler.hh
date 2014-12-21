/*
 * This file is part of rasdaman community.
 *
 * Rasdaman community is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Rasdaman community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Peter Baumann /
 rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */

/* SOURCE: ClientPongHandler.hh
 *
 * MODULE:
 * CLASS:   ClientPongHandler
 *
 * COMMENTS: The ClientPongHandler handles a pong message received as a response to an earlier ping.
 *
 *
 */
#ifndef RASNET_SRC_SOCKETS_CLIENT_CLIENTPONGHANDLER_HH_
#define RASNET_SRC_SOCKETS_CLIENT_CLIENTPONGHANDLER_HH_

#include "clientcommunicationhandler.hh"

namespace rasnet
{

class ClientPongHandler: public ClientCommunicationHandler
{
public:
    ClientPongHandler();

    virtual bool canHandle(const base::BaseMessage& message);

    virtual void handle(const base::BaseMessage& message,
                        zmq::socket_t& socket);

    virtual ~ClientPongHandler();

private:
    std::string messageType; /*!< Type of the message that this handler can handler */
};

} /* namespace rasnet */

#endif /* RASNET_SOCKETS_CLIENT_CLIENTPONGHANDLER_HH_ */
