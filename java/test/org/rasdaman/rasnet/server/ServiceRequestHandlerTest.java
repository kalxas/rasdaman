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

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import com.google.protobuf.Service;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.rasdaman.rasnet.TestUtilities;
import org.rasdaman.rasnet.exception.DuplicateService;
import org.rasdaman.rasnet.exception.UnsupportedMessageType;
import org.rasdaman.rasnet.message.Test.TestReply;
import org.rasdaman.rasnet.message.Test.TestRequest;
import org.rasdaman.rasnet.util.PeerMessage;
import org.rasdaman.rasnet.util.ZmqUtil;
import org.zeromq.ZMQ;

import java.util.ArrayList;

import static org.junit.Assert.*;
import static org.rasdaman.rasnet.message.internal.Internal.ServiceResponseAvailable;

public class ServiceRequestHandlerTest {

    private class SearchServiceImpl extends org.rasdaman.rasnet.message.Test.SearchService {

        @Override
        public void search(RpcController controller,
                           TestRequest request,
                           RpcCallback<TestReply> done) {
            if (request.getData().equals("hello")) {
                TestReply reply = TestReply.newBuilder().setData(request.getData()).build();
                done.run(reply);
            } else if (request.getData().equals("fail")) {
                throw new RuntimeException("runtime");
            } else {
                controller.setFailed("controller");
            }
        }
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private ZMQ.Context context;
    private ZMQ.Socket client;
    private ZMQ.Socket server;
    private ZMQ.Socket bridge;
    private String bridgeAddress;
    private String serverAddress;
    private String serverEndpoint;
    private ServiceRequestHandler requestHandler;

    @Before
    public void setUp() {
        int port = TestUtilities.randInt(8000, 40000);
        this.serverEndpoint = "tcp://*:"+port;
        this.serverAddress = "tcp://localhost:"+port;
        this.bridgeAddress = "inproc://bridge_address";

        this.context = ZMQ.context(1);

        this.bridge = context.socket(ZMQ.ROUTER);
        this.bridge.bind(bridgeAddress);

        this.server = context.socket(ZMQ.ROUTER);
        this.server.bind(serverEndpoint);

        this.client = context.socket(ZMQ.DEALER);
        this.client.connect(serverAddress);

        this.requestHandler = new ServiceRequestHandler(context, bridgeAddress);
    }

    @After
    public void tearDown() {
//        this.bridge.unbind(bridgeAddress);
        this.bridge.close();

        this.server.unbind(serverEndpoint);
        this.server.close();

        this.client.disconnect(serverAddress);
        this.client.close();

        this.context.close();
    }

    @Test
    public void testAddService() throws Exception {
        Service service = new SearchServiceImpl();

        this.requestHandler.addService(service);

        thrown.expect(DuplicateService.class);
        this.requestHandler.addService(service);
    }


    @Test
    public void testCanHandle() throws Exception {
        Service service = new SearchServiceImpl();
        String methodName = ZmqUtil.getMethodName(service.getDescriptorForType().getMethods().get(0));
        String callId = "callId";
        TestRequest request = TestRequest.newBuilder().setData("hello").build();
        ZmqUtil.sendServiceRequest(client, callId, methodName, request);

        ArrayList<byte[]> message = new ArrayList<>();
        ZmqUtil.receiveCompositeMessageFromPeer(server, message);

        assertTrue(requestHandler.canHandle(message));
    }

    @Test
    public void handleFail() throws Exception {
        thrown.expect(UnsupportedMessageType.class);

        String peerId = "peerId";
        ArrayList<byte[]> message = new ArrayList<>();
        requestHandler.handle(message, peerId);
    }

    @Test
    public void handleInvalidMethodCall() throws Exception {
        Service service = new SearchServiceImpl();

        String methodName = ZmqUtil.getMethodName(service.getDescriptorForType().getMethods().get(0));
        String callId = "callId";
        TestRequest request = TestRequest.newBuilder().setData("hello").build();
        ZmqUtil.sendServiceRequest(client, callId, methodName, request);

        String peerId = "peerID";
        ArrayList<byte[]> message = new ArrayList<>();
        ZmqUtil.receiveCompositeMessageFromPeer(server, message);

        requestHandler.handle(message, peerId);

        ServiceResponse response = requestHandler.getTaskResult();
        assertFalse(response.isSuccess());
        assertNotEquals("", response.getError());

        //A message must be sent from the service handler to the bridge
        PeerMessage peerMessage = new PeerMessage();
        assertTrue(ZmqUtil.receiveFromPeer(bridge, peerMessage));

        ServiceResponseAvailable responseAvailable = ServiceResponseAvailable.getDefaultInstance();
        assertEquals(ZmqUtil.getType(responseAvailable), peerMessage.getMessage().getType());
    }

    @Test
    public void handleInvalidInputData() throws Exception {
        Service service = new SearchServiceImpl();
        requestHandler.addService(service);

        String methodName = ZmqUtil.getMethodName(service.getDescriptorForType().getMethods().get(0));
        String callId = "callId";
        TestRequest request = TestRequest.newBuilder().setData("hello").build();
        ZmqUtil.sendServiceRequest(client, callId, methodName, request);

        String peerId = "peerID";
        ArrayList<byte[]> message = new ArrayList<>();
        ZmqUtil.receiveCompositeMessageFromPeer(server, message);

        //Set the input data to something invalid.
        message.set(3, new byte[]{});

        requestHandler.handle(message, peerId);

        ServiceResponse response = requestHandler.getTaskResult();
        assertFalse(response.isSuccess());
        assertNotEquals(null, response.getError());

        //A message must be sent from the service handler to the bridge
        PeerMessage peerMessage = new PeerMessage();
        assertTrue(ZmqUtil.receiveFromPeer(bridge, peerMessage));

        ServiceResponseAvailable responseAvailable = ServiceResponseAvailable.getDefaultInstance();
        assertEquals(ZmqUtil.getType(responseAvailable), peerMessage.getMessage().getType());
    }

    @Test
    public void handleRuntimeException() throws Exception {
        Service service = new SearchServiceImpl();
        requestHandler.addService(service);

        String methodName = ZmqUtil.getMethodName(service.getDescriptorForType().getMethods().get(0));
        String callId = "callId";
        TestRequest request = TestRequest.newBuilder().setData("fail").build();
        ZmqUtil.sendServiceRequest(client, callId, methodName, request);

        String peerId = "peerID";
        ArrayList<byte[]> message = new ArrayList<>();
        ZmqUtil.receiveCompositeMessageFromPeer(server, message);

        requestHandler.handle(message, peerId);

        ServiceResponse response = requestHandler.getTaskResult();
        assertFalse(response.isSuccess());
        assertEquals("runtime", response.getError());

        //A message must be sent from the service handler to the bridge
        PeerMessage peerMessage = new PeerMessage();
        assertTrue(ZmqUtil.receiveFromPeer(bridge, peerMessage));

        ServiceResponseAvailable responseAvailable = ServiceResponseAvailable.getDefaultInstance();
        assertEquals(ZmqUtil.getType(responseAvailable), peerMessage.getMessage().getType());
    }

    @Test
    public void handleControllerFailure() throws Exception {
        Service service = new SearchServiceImpl();
        requestHandler.addService(service);

        String methodName = ZmqUtil.getMethodName(service.getDescriptorForType().getMethods().get(0));
        String callId = "callId";
        TestRequest request = TestRequest.newBuilder().setData("controller").build();
        ZmqUtil.sendServiceRequest(client, callId, methodName, request);

        String peerId = "peerID";
        ArrayList<byte[]> message = new ArrayList<>();
        ZmqUtil.receiveCompositeMessageFromPeer(server, message);

        requestHandler.handle(message, peerId);

        ServiceResponse response = requestHandler.getTaskResult();
        assertFalse(response.isSuccess());
        assertEquals("controller", response.getError());

        //A message must be sent from the service handler to the bridge
        PeerMessage peerMessage = new PeerMessage();
        assertTrue(ZmqUtil.receiveFromPeer(bridge, peerMessage));

        ServiceResponseAvailable responseAvailable = ServiceResponseAvailable.getDefaultInstance();
        assertEquals(ZmqUtil.getType(responseAvailable), peerMessage.getMessage().getType());
    }

    @Test
    public void handleSuccess() throws Exception {
        Service service = new SearchServiceImpl();
        requestHandler.addService(service);

        String methodName = ZmqUtil.getMethodName(service.getDescriptorForType().getMethods().get(0));
        String callId = "callId";
        TestRequest request = TestRequest.newBuilder().setData("hello").build();
        ZmqUtil.sendServiceRequest(client, callId, methodName, request);

        String peerId = "peerID";
        ArrayList<byte[]> message = new ArrayList<>();
        ZmqUtil.receiveCompositeMessageFromPeer(server, message);

        requestHandler.handle(message, peerId);

        ServiceResponse response = requestHandler.getTaskResult();
        assertTrue(response.isSuccess());
        assertEquals(null, response.getError());
        TestReply reply = TestReply.parseFrom(response.getOutputValue());
        assertEquals(request.getData(), reply.getData());

        //A message must be sent from the service handler to the bridge
        PeerMessage peerMessage = new PeerMessage();
        assertTrue(ZmqUtil.receiveFromPeer(bridge, peerMessage));

        ServiceResponseAvailable responseAvailable = ServiceResponseAvailable.getDefaultInstance();
        assertEquals(ZmqUtil.getType(responseAvailable), peerMessage.getMessage().getType());
    }
}