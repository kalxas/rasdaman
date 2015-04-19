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

public class ServerPingHandler {
    private static Logger LOG = LoggerFactory.getLogger(ClientPool.class);
    private ClientPool clientPool;
    ZMQ.Socket client;

    public ServerPingHandler(ZMQ.Socket client, ClientPool clientPool) {
        this.client = client;
        this.clientPool = clientPool;
    }

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

    public void handle(ArrayList<byte[]> message, String peerId) throws InvalidProtocolBufferException, UnsupportedMessageType {
        //Parse the message to make sure the correct message was passed in.
        if (this.canHandle(message)) {
            clientPool.resetClientStatus(peerId);

            if (!ZmqUtil.sendCompositeMessageToPeer(client, peerId, Communication.MessageType.Types.ALIVE_PONG)) {
                LOG.error("Failed to send pong message to client");
            }
        } else {
            throw new UnsupportedMessageType();
        }
    }
}
