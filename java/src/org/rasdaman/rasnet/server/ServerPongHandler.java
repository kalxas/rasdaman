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

import java.util.ArrayList;

/**
 * @brief The ServerPongHandler class Handle PONG messages from
 * a peer by resetting its status
 */
public class ServerPongHandler {
    private ClientPool clientPool;

    public ServerPongHandler(ClientPool clientPool) {
        this.clientPool = clientPool;
    }

    /**
     * @param message
     * @return TRUE if the messages can be handled, FALSE otherwise
     * @brief canHandle Check if the message can be handled by this handler
     */
    public boolean canHandle(ArrayList<byte[]> message) {
        boolean success = false;

        if (message.size() == 1) {
            Communication.MessageType type;
            try {
                type = Communication.MessageType.parseFrom(message.get(0));
                success = (type.getType() == Communication.MessageType.Types.ALIVE_PONG);
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
     * @brief handle Handle the given message by resetting its status
     */
    public void handle(ArrayList<byte[]> message, String peerId) throws UnsupportedMessageType {
        //Sanity check.
        //Parse the message data to make sure that no invalid message arrives here
        if (this.canHandle(message)) {
            this.clientPool.resetClientStatus(peerId);
        } else {
            throw new UnsupportedMessageType();
        }
    }
}
