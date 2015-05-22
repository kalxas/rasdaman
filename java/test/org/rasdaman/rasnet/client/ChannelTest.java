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
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.rasdaman.rasnet.TestUtilities;
import org.rasdaman.rasnet.exception.ConnectionTimeoutException;
import org.rasdaman.rasnet.exception.NetworkingException;
import org.rasdaman.rasnet.message.Communication;
import org.rasdaman.rasnet.message.Test.SearchService;
import org.rasdaman.rasnet.message.Test.TestReply;
import org.rasdaman.rasnet.message.Test.TestRequest;
import org.rasdaman.rasnet.server.ServiceResponse;
import org.rasdaman.rasnet.util.ZmqUtil;
import org.zeromq.ZMQ;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.rasdaman.rasnet.message.Communication.MessageType.Types.CONNECT_REPLY;
import static org.rasdaman.rasnet.message.Communication.MessageType.Types.SERVICE_REQUEST;

public class ChannelTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private ZMQ.Context context;
    private ZMQ.Socket server;
    private String serverAddress;
    private String serverBindingPoint;

    private void replyToConnect() {
        try {
            ArrayList<byte[]> message = new ArrayList<>();
            String peerId = ZmqUtil.receiveCompositeMessageFromPeer(server, message);

            Communication.MessageType type = Communication.MessageType.parseFrom(message.get(0));
            Communication.ConnectRequest request = Communication.ConnectRequest.parseFrom(message.get(1));
            Communication.ConnectReply reply = Communication.ConnectReply.newBuilder()
                    .setLifetime(request.getLifetime())
                    .setRetries(request.getRetries()).build();

            ZmqUtil.sendCompositeMessageToPeer(server, peerId, CONNECT_REPLY, reply);

        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    private void replyToServiceCall() throws NetworkingException {
        try {
            ArrayList<byte[]> message = new ArrayList<>();
            String peerId = ZmqUtil.receiveCompositeMessageFromPeer(server, message);

            Communication.MessageType type = Communication.MessageType.parseFrom(message.get(0));
            assertEquals(4, message.size());

            assertEquals(SERVICE_REQUEST, type.getType());

            String callId = new String(message.get(1));

            TestRequest request = TestRequest.parseFrom(message.get(3));
            TestReply reply = TestReply.newBuilder().setData(request.getData()).build();

            ServiceResponse response = new ServiceResponse(callId);
            response.setOutputValue(reply.toByteString());
            ZmqUtil.sendServiceResponse(server, peerId, response);

        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    private void replyWithErrorCall() throws NetworkingException {
        try {
            ArrayList<byte[]> message = new ArrayList<>();
            String peerId = ZmqUtil.receiveCompositeMessageFromPeer(server, message);

            Communication.MessageType type = Communication.MessageType.parseFrom(message.get(0));
            assertEquals(4, message.size());

            assertEquals(SERVICE_REQUEST, type.getType());

            String callId = new String(message.get(1));

            TestRequest request = TestRequest.parseFrom(message.get(3));

            ServiceResponse response = new ServiceResponse(callId);
            response.setError(request.getData());
            ZmqUtil.sendServiceResponse(server, peerId, response);

        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    @Before
    public void setUp() throws Exception {
        int port = TestUtilities.randInt(8000, 40000);
        this.serverAddress = "tcp://localhost:"+port;
        this.serverBindingPoint = "tcp://*:"+port;

        this.context = ZMQ.context(1);
        this.server = context.socket(ZMQ.ROUTER);
        this.server.bind(serverBindingPoint);

    }

    @After
    public void tearDown() throws Exception {
        this.server.unbind(serverBindingPoint);
        this.server.close();
    }

    @Test
    public void successFullConnect() throws Exception {
        Thread handlerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                replyToConnect();
            }
        });

        handlerThread.start();
        ChannelConfig config = new ChannelConfig();
        Channel channel = new Channel(serverAddress, config);
        handlerThread.join();

        channel.close();
    }

    @Test
    public void failToConnect() throws Exception {
        thrown.expect(ConnectionTimeoutException.class);

        ChannelConfig config = new ChannelConfig();
        config.setConnectionTimeout(1);

        Channel channel = new Channel(serverAddress, config);

        channel.close();
    }

    @Test
    public void failToConnect2() throws Exception {
        thrown.expect(ConnectionTimeoutException.class);

        ChannelConfig config = new ChannelConfig();
        config.setConnectionTimeout(1);

        Channel channel = new Channel("serverAddress", config);

        channel.close();
    }

    @Test
    public void validReplySuccess() throws Exception {
        Thread handlerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                replyToConnect();
            }
        });

        handlerThread.start();
        ChannelConfig config = new ChannelConfig();
        Channel channel = new Channel(serverAddress, config);
        ClientController controller = new ClientController();
        handlerThread.join();

        TestRequest request = TestRequest.newBuilder().setData("hello").build();
        TestReply reply = TestReply.getDefaultInstance();
        //SearchService.BlockingInterface blockingInterface = SearchService.Stub.newBlockingStub(channel);

        Thread serverThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    replyToServiceCall();
                } catch (NetworkingException e) {
                    e.printStackTrace();
                }
            }
        });
        serverThread.start();

        TestReply response = (TestReply) channel.callBlockingMethod(SearchService.getDescriptor().getMethods().get(0), controller, request, reply);

        serverThread.join();
        channel.close();

        assertFalse(controller.failed());
        assertEquals(request.getData(),response.getData());
    }

    @Test
    public void validReplyError() throws Exception {
        Thread handlerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                replyToConnect();
            }
        });

        handlerThread.start();
        ChannelConfig config = new ChannelConfig();
        Channel channel = new Channel(serverAddress, config);
        ClientController controller = new ClientController();
        handlerThread.join();

        TestRequest request = TestRequest.newBuilder().setData("hello").build();
        TestReply reply = TestReply.getDefaultInstance();
        //SearchService.BlockingInterface blockingInterface = SearchService.Stub.newBlockingStub(channel);

        Thread serverThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    replyWithErrorCall();
                } catch (NetworkingException e) {
                    e.printStackTrace();
                }
            }
        });
        serverThread.start();

        TestReply response = (TestReply) channel.callBlockingMethod(SearchService.getDescriptor().getMethods().get(0), controller, request, reply);

        serverThread.join();
        channel.close();

        assertTrue(controller.failed());
        assertEquals(request.getData(), controller.errorText());
    }
}