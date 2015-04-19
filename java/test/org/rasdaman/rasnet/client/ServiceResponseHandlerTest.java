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

import com.google.protobuf.Message;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.rasdaman.rasnet.exception.UnsupportedMessageType;
import org.rasdaman.rasnet.message.Test.TestReply;
import org.rasdaman.rasnet.message.internal.Internal;
import org.rasdaman.rasnet.util.PeerMessage;
import org.rasdaman.rasnet.util.PeerStatus;
import org.rasdaman.rasnet.util.ZmqUtil;
import org.zeromq.ZMQ;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.rasdaman.rasnet.message.Communication.MessageType;
import static org.rasdaman.rasnet.message.Communication.MessageType.Types.ALIVE_PING;
import static org.rasdaman.rasnet.message.Communication.MessageType.Types.SERVICE_RESPONSE;
import static org.rasdaman.rasnet.message.Communication.ServiceCallStatus;

public class ServiceResponseHandlerTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private ZMQ.Context context;
    private ZMQ.Socket server;
    private ZMQ.Socket client;
    private String address;
    private String peerId;
    private PeerStatus peerStatus;
    private String errorMessage;
    private ServiceResponseHandler responseHandler;
    private ConcurrentHashMap<String, Map.Entry<String, Message>> serviceRequests;
    private ConcurrentHashMap<String, Map.Entry<Boolean, byte[]>> serviceResponses;
    private TestReply reply;

    private ArrayList<byte[]> createErrorResponse() {
        ArrayList<byte[]> result = new ArrayList<>();
        MessageType type = MessageType.newBuilder().setType(SERVICE_RESPONSE).build();

        result.add(type.toByteArray());
        result.add(peerId.getBytes());

        ServiceCallStatus status = ServiceCallStatus.newBuilder().setSuccess(false).build();
        result.add(status.toByteArray());
        result.add(errorMessage.getBytes());

        return result;
    }

    private ArrayList<byte[]> createInvalidResponse() {
        ArrayList<byte[]> result = new ArrayList<>();
        MessageType type = MessageType.newBuilder().setType(ALIVE_PING).build();

        result.add(type.toByteArray());
        result.add(peerId.getBytes());

        ServiceCallStatus status = ServiceCallStatus.newBuilder().setSuccess(false).build();
        result.add(status.toByteArray());
        result.add(errorMessage.getBytes());

        return result;
    }


    private ArrayList<byte[]> createSuccessResponse() {
        ArrayList<byte[]> result = new ArrayList<>();
        MessageType type = MessageType.newBuilder().setType(SERVICE_RESPONSE).build();

        result.add(type.toByteArray());
        result.add(peerId.getBytes());

        ServiceCallStatus status = ServiceCallStatus.newBuilder().setSuccess(true).build();
        result.add(status.toByteArray());
        result.add(this.reply.toByteArray());

        return result;
    }

    @Before
    public void setUp() throws Exception {
        this.reply = TestReply.newBuilder().setData("data").build();
        this.address = "inproc://some-address";
        this.peerId = "peerId";
        this.errorMessage = "errorMessage";

        this.context = ZMQ.context(1);

        this.client = context.socket(ZMQ.DEALER);
        this.client.setIdentity(this.peerId.getBytes());
        this.client.connect(address);

        this.server = context.socket(ZMQ.ROUTER);
        this.server.bind(address);

        this.peerStatus = mock(PeerStatus.class);

        this.serviceRequests = new ConcurrentHashMap<String, Map.Entry<String, Message>>();
        this.serviceResponses = new ConcurrentHashMap<String, Map.Entry<Boolean, byte[]>>();
        this.responseHandler = new ServiceResponseHandler(server, peerStatus, serviceRequests, serviceResponses);
    }


    @After
    public void tearDown() throws Exception {
        this.client.disconnect(address);
        this.client.close();

        //this.server.unbind(address);
        this.server.close();

        this.context.close();
    }

    @Test
    public void testCanHandle() throws Exception {
        ArrayList<byte[]> message = this.createErrorResponse();
        assertTrue(this.responseHandler.canHandle(message));
    }

    @Test
    public void testCanHandleFail() throws Exception {
        ArrayList<byte[]> message = new ArrayList<>();
        assertFalse(this.responseHandler.canHandle(message));

        message = this.createInvalidResponse();
        assertFalse(this.responseHandler.canHandle(message));
    }

    @Test
    public void testHandleThrowException() throws Exception {
        thrown.expect(UnsupportedMessageType.class);

        ArrayList<byte[]> message = this.createInvalidResponse();
        this.responseHandler.handle(message);
    }

    @Test
    public void testHandleError() throws Exception {
        this.serviceRequests.put(peerId, new AbstractMap.SimpleEntry<String, Message>("method", null));

        ArrayList<byte[]> message = this.createErrorResponse();
        this.responseHandler.handle(message);

        assertTrue(this.serviceRequests.isEmpty());
        assertEquals(1, this.serviceResponses.size());

        Map.Entry<Boolean, byte[]> serviceResponse = this.serviceResponses.get(peerId);
        assertNotNull(serviceResponse);
        assertFalse(serviceResponse.getKey());
        assertEquals(errorMessage, new String(serviceResponse.getValue()));
    }

    @Test
    public void testHandle() throws Exception {
        Internal.ServiceRequestAvailable requestAvailable = Internal.ServiceRequestAvailable.getDefaultInstance();
        ZmqUtil.send(client, requestAvailable);

        PeerMessage peerMessage = new PeerMessage();
        ZmqUtil.receiveFromPeer(server, peerMessage);

        this.serviceRequests.put(peerId, new AbstractMap.SimpleEntry<String, Message>("method", null));

        ArrayList<byte[]> message = this.createSuccessResponse();
        this.responseHandler.handle(message);

        assertTrue(this.serviceRequests.isEmpty());
        assertEquals(1, this.serviceResponses.size());

        Map.Entry<Boolean, byte[]> serviceResponse = this.serviceResponses.get(peerId);
        assertNotNull(serviceResponse);
        assertTrue(serviceResponse.getKey());

        TestReply reply = TestReply.parseFrom(serviceResponse.getValue());

        assertEquals(this.reply.getData(), reply.getData());
    }

}