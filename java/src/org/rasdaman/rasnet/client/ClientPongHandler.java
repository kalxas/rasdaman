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
import org.rasdaman.rasnet.message.Communication;
import org.rasdaman.rasnet.util.PeerStatus;

import java.util.ArrayList;

/**
 * @brief The ClientPongHandler class Handles a pong message received as a response to an earlier ping
 * by resetting the server's status.
 */
public class ClientPongHandler {
    private PeerStatus serverStatus;

    public ClientPongHandler(PeerStatus serverStatus) {
        this.serverStatus = serverStatus;
    }

    /**
     * @brief canHandle Check if the message can be handled by this handler
     * This handler accepts messages of the format:
     * | rasnet.MessageType |
     * with MessageType.type() == MessageType::ALIVE_PONG
     * @param message
     * @return TRUE if the message can be handled, FALSE otherwise
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
     * @brief handle Resets the status of the server if a PONG message is received
     * @param message
     * @throws UnsupportedMessageType if an invalid message is passed in.
     * i.e. one for which canHandle returns false
     */
    public void handle(ArrayList<byte[]> message) throws UnsupportedMessageType {
        if (this.canHandle(message)) {
            this.serverStatus.reset();
        } else {
            throw new UnsupportedMessageType();
        }
    }
}
