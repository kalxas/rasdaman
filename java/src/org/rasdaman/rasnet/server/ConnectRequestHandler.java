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
import org.rasdaman.rasnet.exception.InvalidClient;
import org.rasdaman.rasnet.exception.UnsupportedMessageType;
import org.rasdaman.rasnet.message.Communication;
import org.rasdaman.rasnet.message.Communication.ConnectReply;
import org.rasdaman.rasnet.message.Communication.ConnectRequest;
import org.rasdaman.rasnet.util.ZmqUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;

import java.util.ArrayList;

/**
 * @brief The ConnectRequestHandler class Handles an incoming connect request from a client
 * by responding with a connect reply containing connection parameters.
 */
public class ConnectRequestHandler {
    private static Logger LOG = LoggerFactory.getLogger(ConnectRequestHandler.class);
    private ClientPool clientPool;
    private ConnectReply reply;

    /**
     * ZMQ_ROUTER socket representing the server
     */
    private ZMQ.Socket client;

    /**
     * Create a ClientRequestHandler
     * @param client Socket through which replies to clients are sent.
     * @param clientPool  ClientPool that keeps track of all the clients connected to this server.
     * @param serverRetries The number of times a client should try to contact the server before giving up
     * @param serverLifetime The number of milliseconds between each consecutive retry.
     */
    public ConnectRequestHandler(ZMQ.Socket client, ClientPool clientPool, int serverRetries, int serverLifetime) {
        this.client = client;
        this.clientPool = clientPool;
        this.reply = ConnectReply.newBuilder()
                .setRetries(serverRetries)
                .setLifetime(serverLifetime)
                .build();
    }

    /**
     * Decide if the given message can be processed by this handler.
     * @param message Composite message of the type:
     * | MessageType | ConnectRequest |
     * where MessageType.type() == MessageType::CONNECT_REQUEST
     * @return true if the message contains a ConnectRequest message(@see developer documentation for details),
     * false otherwise.
     */
    public boolean canHandle(ArrayList<byte[]> message) {
        boolean success = false;

        if (message.size() == 2) {
            Communication.MessageType type;
            try {
                type = Communication.MessageType.parseFrom(message.get(0));
                success = (type.getType() == Communication.MessageType.Types.CONNECT_REQUEST);
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        }

        return success;
    }

    /**
     * Handle the ConnectRequest message by adding the client to the pool
     * and sending back a ConnectReply message.
     * @param message
     * @param peerId
     * @throws UnsupportedMessageType If a message for which the handle method returns false
     */
    public void handle(ArrayList<byte[]> message, String peerId) throws UnsupportedMessageType {
        if (this.canHandle(message)) {
            try {
                ConnectRequest request = ConnectRequest.parseFrom(message.get(1));
                clientPool.addClient(peerId, request.getLifetime(), request.getRetries());

                if (!ZmqUtil.sendCompositeMessageToPeer(client, peerId, Communication.MessageType.Types.CONNECT_REPLY, reply)) {
                    LOG.error("Failed to send connect reply to client with ID:" + peerId);
                }

            } catch (InvalidProtocolBufferException e) {
                LOG.error(e.getLocalizedMessage());
                e.printStackTrace();
            } catch (InvalidClient invalidClient) {
                LOG.error(invalidClient.getLocalizedMessage());
                invalidClient.printStackTrace();
            }

        } else {
            throw new UnsupportedMessageType();
        }
    }
}
