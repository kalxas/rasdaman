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

package org.rasdaman.rasnet.client;

import com.google.protobuf.InvalidProtocolBufferException;
import org.rasdaman.rasnet.exception.UnsupportedMessageType;
import org.rasdaman.rasnet.util.PeerStatus;
import org.rasdaman.rasnet.util.ZmqUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;

import java.util.ArrayList;

import static org.rasdaman.rasnet.message.Communication.MessageType;

/**
 * @brief The ClientPingHandler class Responds to a Ping message from the server with a Pong message.
 */
public class ClientPingHandler {
    private static Logger LOG = LoggerFactory.getLogger(ClientPingHandler.class);
    private ZMQ.Socket server;
    private PeerStatus serverStatus;

    public ClientPingHandler(ZMQ.Socket server, PeerStatus serverStatus) {
        this.server = server;
        this.serverStatus = serverStatus;
    }

    /**
     * @brief canHandle Check if the message can be handled by this message handler
     * This handler accepts messages of the format:
     * | rasnet.MessageType |
     * with MessageType.type() == MessageType::ALIVE_PING
     * @param message
     * @return TRUE if the messages can be handled, FALSE otherwise
     */
    public boolean canHandle(ArrayList<byte[]> message) {
        boolean success = false;

        if (message.size() == 1) {
            MessageType type;
            try {
                type = MessageType.parseFrom(message.get(0));
                success = (type.getType() == MessageType.Types.ALIVE_PING);
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        }

        return success;
    }

    /**
     * @brief handle Handle the given message and send an ALIVE_PONG
     * message through the socket
     * @param message
     * @throws UnsupportedMessageType if an invalid message is passed in.
     * i.e. one for which canHandle returns false
     */
    public void handle(ArrayList<byte[]> message) throws UnsupportedMessageType {
        //Parse the message to make sure the correct message was passed in.
        if (this.canHandle(message)) {
            serverStatus.reset();
            if (!ZmqUtil.sendCompositeMessage(server, MessageType.Types.ALIVE_PONG)) {
                LOG.error("Failed to send pong message to server.");
            }
        } else {
            throw new UnsupportedMessageType();
        }
    }
}
