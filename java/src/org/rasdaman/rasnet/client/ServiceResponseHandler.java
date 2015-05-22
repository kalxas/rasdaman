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
import com.google.protobuf.Message;
import org.rasdaman.rasnet.exception.UnsupportedMessageType;
import org.rasdaman.rasnet.message.Communication;
import org.rasdaman.rasnet.message.internal.Internal;
import org.rasdaman.rasnet.util.PeerStatus;
import org.rasdaman.rasnet.util.ZmqUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @brief The ServiceResponseHandler class Handles ServiceResponse messages sent by the
 * server as a response to a service request
 */
public class ServiceResponseHandler {
    private static Logger LOG = LoggerFactory.getLogger(ServiceResponseHandler.class);

    //<CallID, <Method Name, Input Data> >
    private final ConcurrentHashMap<String, Map.Entry<String, Message>> serviceRequests;

    //<CallID, <Success, Response Message> >
    private final ConcurrentHashMap<String, Map.Entry<Boolean, byte[]>> serviceResponses;

    private final PeerStatus peerStatus;

    private ZMQ.Socket bridgeSocket;

    public ServiceResponseHandler(ZMQ.Socket bridge,
                                  PeerStatus peerStatus,
                                  ConcurrentHashMap<String, Map.Entry<String, Message>> serviceRequests,
                                  ConcurrentHashMap<String, Map.Entry<Boolean, byte[]>> serviceResponses) {
        this.bridgeSocket = bridge;
        this.peerStatus = peerStatus;
        this.serviceRequests = serviceRequests;
        this.serviceResponses = serviceResponses;
    }

    /**
     * @param message
     * @return TRUE if the messages can be handled, FALSE otherwise
     * @brief canHandle Check if the message can be handled by this message handler
     * This handler accepts messages of the format:
     * | rasnet.MessageType | Call ID | ServiceCallStatus | Response data|
     * with MessageType.type() == MessageType::SERVICE_RESPONSE
     * Call ID is the ID of the call
     */
    public boolean canHandle(ArrayList<byte[]> message) {
        boolean success = false;

        if (message.size() == 4) {
            Communication.MessageType type;
            try {
                type = Communication.MessageType.parseFrom(message.get(0));
                success = (type.getType() == Communication.MessageType.Types.SERVICE_RESPONSE);
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
                success = false;
            }
        }

        return success;
    }

    /**
     * @param message
     * @throws UnsupportedMessageType if an invalid message is passed in.
     *                                i.e. one for which canHandle returns false
     * @brief handle See inline documentation for a thorough explanation
     */
    public void handle(ArrayList<byte[]> message) throws UnsupportedMessageType {
        //Accepted message format:
        //| rasnet.MessageType | Call ID | ServiceCallStatus | Response data|
        if (this.canHandle(message)) {
            try {
                peerStatus.reset();

                //1. Extract the data
                String requestId = new String(message.get(1));

                //If an exception is thrown, the error is printed, but the exception is catched
                Communication.ServiceCallStatus status = Communication.ServiceCallStatus.parseFrom(message.get(2));

                //2.Check if the request is valid.
                Map.Entry<String, Message> pendingRequest = serviceRequests.get(requestId);
                if (pendingRequest != null) {
                    //3. Add the response to the list of pending responses
                    SimpleEntry<Boolean, byte[]> responseEntry = new SimpleEntry<>(status.getSuccess(), message.get(3));
                    serviceResponses.put(requestId, responseEntry);

                    //4. Notify the other thread that the response is ready
                    Internal.ServiceResponseAvailable responseAvailable = Internal.ServiceResponseAvailable.getDefaultInstance();
                    ZmqUtil.sendToPeer(bridgeSocket, requestId, responseAvailable);

                    //5. Remove the request from the list of pending requests
                    serviceRequests.remove(requestId);
                } else {
                    LOG.error("Received service response with invalid ID.");
                }
            } catch (InvalidProtocolBufferException e) {
                LOG.error(e.getLocalizedMessage());
                e.printStackTrace();
            }
        } else {
            throw new UnsupportedMessageType();
        }
    }
}
