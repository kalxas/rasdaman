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

package org.rasdaman.rasnet.server;

import com.google.protobuf.InvalidProtocolBufferException;
import org.rasdaman.rasnet.exception.UnsupportedMessageType;
import org.rasdaman.rasnet.message.Communication;
import org.rasdaman.rasnet.util.ZmqUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;

import java.util.ArrayList;

/**
 * @brief The ServerPingHandler class Handle ping messages from already connected clients
 * by replying with a pong message
 */
public class ServerPingHandler {
    private static Logger LOG = LoggerFactory.getLogger(ClientPool.class);
    private ClientPool clientPool;
    private ZMQ.Socket client;

    /**
     * @param client     Socket through which the server communicates with the client.
     * @param clientPool Pool managing the collection of clients connected to this server
     */
    public ServerPingHandler(ZMQ.Socket client, ClientPool clientPool) {
        this.client = client;
        this.clientPool = clientPool;
    }

    /**
     * @param message Format: |MessageType| with type ALIVE_PING
     * @return TRUE if the messages can be handled, FALSE otherwise
     * @brief canHandle Check if the message can be handled by this handler
     */
    public boolean canHandle(ArrayList<byte[]> message) {
        boolean success = false;

        if (message.size() == 1) {
            Communication.MessageType type;
            try {
                type = Communication.MessageType.parseFrom(message.get(0));
                success = (type.getType() == Communication.MessageType.Types.ALIVE_PING);
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
                success = false;
            }
        }

        return success;
    }

    /**
     * @param message
     * @param peerId
     * @throws UnsupportedMessageType if an invalid message is passed in.
     *                                i.e. one for which canHandle returns false
     * @brief handle Handle the given message and send the an ALIVE_PONG
     * message through the socket
     */
    public void handle(ArrayList<byte[]> message, String peerId) throws InvalidProtocolBufferException, UnsupportedMessageType {
        //Parse the message to make sure the correct message was passed in.
        if (!this.canHandle(message)) {
            throw new UnsupportedMessageType();

        } else if (!clientPool.isClientAlive(peerId)) {
            LOG.debug("Clieht with ID:" + peerId + " sent PING, but the client is not part of the pool of active clients.");
        } else {
            clientPool.resetClientStatus(peerId);

            if (!ZmqUtil.sendCompositeMessageToPeer(client, peerId, Communication.MessageType.Types.ALIVE_PONG)) {
                LOG.error("Failed to send pong message to client");
            }
        }
    }
}
