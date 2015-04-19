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
#ifndef RASNET_SRC_CLIENT_CLIENTPONGHANDLER_HH
#define RASNET_SRC_CLIENT_CLIENTPONGHANDLER_HH

#include <string>

#include <boost/shared_ptr.hpp>

#include "../../../common/src/zeromq/zmq.hh"

#include "../common/peerstatus.hh"

namespace rasnet
{
class ClientPongHandler
{
public:
    ClientPongHandler(boost::shared_ptr<PeerStatus>  serverStatus);

    virtual ~ClientPongHandler();

    /**
     * @brief canHandle Check if the message can be handled by this handler
     * @param messages
     * @return TRUE if the message can be handled, FALSE otherwise
     */
    bool canHandle(const std::vector<boost::shared_ptr<zmq::message_t> >&  messages);

    /**
     * @brief handle Handle the given message
     * @param messages
     * @throws UnsupportedMessageException if an invalid message is passed in.
     * i.e. one for which canHandle returns false
     */
    void handle(const std::vector<boost::shared_ptr<zmq::message_t> >&   messages);
private:
    boost::shared_ptr<PeerStatus>  serverStatus;
};

} /* namespace rasnet */

#endif /* RASNET_SRC_CLIENT_CLIENTPONGHANDLER_HH */
