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

package org.rasdaman.rasnet.util;

import com.google.protobuf.Descriptors;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import org.rasdaman.rasnet.common.Constants;
import org.rasdaman.rasnet.exception.NetworkingException;
import org.rasdaman.rasnet.message.Communication;
import org.rasdaman.rasnet.message.Communication.MessageType.Types;
import org.rasdaman.rasnet.server.ServiceResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;

import java.util.ArrayList;

import static org.rasdaman.rasnet.message.Communication.BaseMessage;

public final class ZmqUtil {
    private static final Logger LOG = LoggerFactory.getLogger(ZmqUtil.class);

    private static final char ADDRESS_PORT_SEPARATOR = ':';
    private static final String TCP_PREFIX = "tcp://";
    private static final String INPROC_PREFIX = "inproc://";

    public static final String ALL_LOCAL_INTERFACES = TCP_PREFIX + "*";
    public static final int ZMQ_NO_FLAG = 0;

    private ZmqUtil() {
    }

    public static String getType(Message message) {
        return message.getDescriptorForType().getFullName();
    }

    public static String toTcpAddress(String address) {
        return addPrefixIfMissing(address, TCP_PREFIX);
    }

    public static String toInprocAddress(String address) {
        return addPrefixIfMissing(address, INPROC_PREFIX);
    }

    public static String toAddressPort(String address, int port) {
        StringBuilder result = new StringBuilder();

        result.append(address);
        result.append(ADDRESS_PORT_SEPARATOR);
        result.append(port);

        return result.toString();
    }

    /**
     * Serialize the given Proto message and send
     * it using zero-copy over the ZeroMQ socket.
     * The message is wrapped into a BaseMessage
     *
     * @param socket
     * @param message
     * @return TRUE if the sending was successful, FALSE otherwise
     */
    public static boolean send(ZMQ.Socket socket, Message message) {
        BaseMessage envelope = BaseMessage.newBuilder()
                .setData(message.toByteString())
                .setType(ZmqUtil.getType(message))
                .build();

        return ZmqUtil.rawSend(socket, envelope);
    }

    /**
     * Send the protobuf message to the peer identified by the given ID.
     * The message is wrapped into a BaseMessage
     *
     * @param router
     * @param peerId  identity of the ZMQ_DEALER socket that represents the client
     * @param message The Google protobuf message
     * @return TRUE if the sending was successful, FALSE otherwise
     */
    public static boolean sendToPeer(ZMQ.Socket router, String peerId, Message message) {
        boolean success = router.send(peerId.getBytes(Constants.DEFAULT_ENCODING), ZMQ.SNDMORE);

        success = success && ZmqUtil.send(router, message);
        return success;
    }

    /**
     * Receive a message from the socket.
     *
     * @param socket    The socket from which to read.
     * @param container Container for storing the message
     * @return TRUE if the receiving was successful, FALSE otherwise
     */
    public static boolean receive(ZMQ.Socket socket, MessageContainer container) {
        boolean success = false;

        try {
            byte[] message = socket.recv();
            BaseMessage base = BaseMessage.parseFrom(message);
            container.setData(base.getData());
            container.setType(base.getType());

            if (socket.hasReceiveMore()) {
                LOG.error("The message has more parts than excepted");
                ZmqUtil.dumpData(socket);
            } else {
                success = true;
            }
        } catch (InvalidProtocolBufferException e) {
            LOG.error("Failed to parse received message:" + e.getLocalizedMessage());
            ;
        }

        return success;
    }


    public static boolean receiveFromPeer(ZMQ.Socket socket, PeerMessage message) {
        boolean success = false;

        try {
            String peerId = socket.recvStr(Constants.DEFAULT_ENCODING);

            if (socket.hasReceiveMore()) {
                byte[] baseData = socket.recv();
                BaseMessage base = BaseMessage.parseFrom(baseData);

                MessageContainer container = new MessageContainer();
                container.setData(base.getData());
                container.setType(base.getType());

                if (socket.hasReceiveMore()) {
                    LOG.error("The message has more parts than excepted");
                    ZmqUtil.dumpData(socket);
                } else {
                    message.setPeerId(peerId);
                    message.setMessage(container);

                    success = true;
                }
            } else {
                LOG.error("The message has less parts than excepted");
                ZmqUtil.dumpData(socket);
            }

        } catch (InvalidProtocolBufferException e) {
            LOG.error("Failed to parse received message:" + e.getLocalizedMessage());
        }

        return success;
    }

    public static boolean sendCompositeMessage(ZMQ.Socket socket, Types type) {
        Communication.MessageType messageType = Communication.MessageType
                .newBuilder()
                .setType(type)
                .build();

        return ZmqUtil.rawSend(socket, messageType);
    }

    public static boolean sendCompositeMessage(ZMQ.Socket socket, Types type, Message message) {
        boolean success = true;

        Communication.MessageType messageType = Communication.MessageType
                .newBuilder()
                .setType(type)
                .build();

        success = success && ZmqUtil.rawSend(socket, messageType, ZMQ.SNDMORE);
        success = success && ZmqUtil.rawSend(socket, message);

        return success;
    }

    public static boolean sendCompositeMessageToPeer(ZMQ.Socket socket, String peerId, Types type) {
        Communication.MessageType messageType = Communication.MessageType
                .newBuilder()
                .setType(type)
                .build();

        boolean success = socket.send(peerId.getBytes(Constants.DEFAULT_ENCODING), ZMQ.SNDMORE);
        success = success && ZmqUtil.rawSend(socket, messageType);

        return success;
    }

    public static boolean sendCompositeMessageToPeer(ZMQ.Socket socket, String peerId, Types type, Message message) {
        boolean success = socket.send(peerId.getBytes(Constants.DEFAULT_ENCODING), ZMQ.SNDMORE);
        success = success && ZmqUtil.sendCompositeMessage(socket, type, message);

        return success;
    }

    public static void receiveCompositeMessage(ZMQ.Socket socket, ArrayList<byte[]> message) {
        message.clear();

        do {
            message.add(socket.recv());
        }
        while (socket.hasReceiveMore());
    }

    public static String receiveCompositeMessageFromPeer(ZMQ.Socket socket, ArrayList<byte[]> message) {
        String peerId = socket.recvStr(Constants.DEFAULT_ENCODING);

        message.clear();

        while (socket.hasReceiveMore()) {
            message.add(socket.recv());
        }

        return peerId;
    }

    public static void sendServiceResponse(ZMQ.Socket socket, String peerId, ServiceResponse response) throws NetworkingException {
        Communication.MessageType messageType = Communication.MessageType.newBuilder()
                .setType(Types.SERVICE_RESPONSE).build();

        Communication.ServiceCallStatus status = Communication.ServiceCallStatus.newBuilder()
                .setSuccess(response.isSuccess()).build();

        boolean success = ZmqUtil.sendString(socket, peerId, ZMQ.SNDMORE);
        success = success && ZmqUtil.rawSend(socket, messageType, ZMQ.SNDMORE);
        success = success && ZmqUtil.sendString(socket, response.getCallId(), ZMQ.SNDMORE);
        success = success && ZmqUtil.rawSend(socket, status, ZMQ.SNDMORE);

        if (response.isSuccess()) {
            success = success && socket.send(response.getOutputValue().toByteArray(), ZMQ_NO_FLAG);
        } else {
            success = success && socket.send(response.getError(), ZMQ_NO_FLAG);
        }

        if (!success) {
            throw new NetworkingException("Failed to send service response");
        }
    }

    public static boolean sendServiceRequest(ZMQ.Socket socket, String callId, String methodName, Message inputValue) {
        Communication.MessageType messageType = Communication.MessageType.newBuilder()
                .setType(Types.SERVICE_REQUEST).build();

        boolean success = ZmqUtil.rawSend(socket, messageType, ZMQ.SNDMORE);
        success = success && ZmqUtil.sendString(socket, callId, ZMQ.SNDMORE);
        success = success && ZmqUtil.sendString(socket, methodName, ZMQ.SNDMORE);
        success = success && ZmqUtil.rawSend(socket, inputValue);

        return success;
    }

    private static boolean sendString(ZMQ.Socket socket, String data, int flags) {
        return socket.send(data.getBytes(Constants.DEFAULT_ENCODING), flags);
    }

    private static boolean rawSend(ZMQ.Socket socket, Message message, int flags) {
        return socket.send(message.toByteArray(), flags);
    }

    private static void dumpData(ZMQ.Socket socket) {
        while (socket.hasReceiveMore()) {
            LOG.debug(socket.recvStr(Constants.DEFAULT_ENCODING));
        }
    }

    private static boolean rawSend(ZMQ.Socket socket, Message message) {
        return ZmqUtil.rawSend(socket, message, ZMQ_NO_FLAG);
    }

    private static String addPrefixIfMissing(String str, String prefix) {
        if (!str.startsWith(prefix)) {
            str = prefix + str;
        }
        return str;
    }

    public static String getMethodName(Descriptors.MethodDescriptor methodDescriptor) {
        return methodDescriptor.getFullName();
    }

    public static boolean isSocketWritable(ZMQ.Socket socket, long timeout) {
        ZMQ.PollItem[] items = new ZMQ.PollItem[]{
                new ZMQ.PollItem(socket, ZMQ.Poller.POLLOUT),
        };

        return (ZMQ.poll(items, timeout) == 1);
    }

    public static boolean isSocketReadable(ZMQ.Socket socket, long timeout) {
        ZMQ.PollItem[] items = new ZMQ.PollItem[]{
                new ZMQ.PollItem(socket, ZMQ.Poller.POLLIN),
        };

        return (ZMQ.poll(items, timeout) == 1);
    }
}
