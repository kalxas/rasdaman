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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.rasdaman.rasnet.common.Constants;
import org.rasdaman.rasnet.message.Test.TestRequest;
import org.zeromq.ZMQ;

import java.util.ArrayList;

import static org.junit.Assert.*;
import static org.rasdaman.rasnet.message.Communication.BaseMessage;
import static org.rasdaman.rasnet.message.Communication.MessageType;

public class ZmqUtilTest {
    private ZMQ.Context context;
    private String dummyData;
    private TestRequest dummyRequest;
    private String inprocAddress;
    private String tcpAddress;
    private String unqualifiedAddress;
    private String clientID;
    private ZMQ.Socket server;
    private ZMQ.Socket client;

    @Before
    public void setUp() {
        this.unqualifiedAddress = "clientping";
        this.inprocAddress = "inproc://clientping";
        this.tcpAddress = "tcp://clientping";
        this.clientID = "clientId";

        this.dummyData = "dummyData";
        this.dummyRequest = TestRequest.newBuilder().setData(dummyData).build();

        this.context = ZMQ.context(1);

        this.server = this.context.socket(ZMQ.ROUTER);
        this.server.bind(inprocAddress);

        this.client = this.context.socket(ZMQ.DEALER);
        client.setIdentity(clientID.getBytes(Constants.DEFAULT_ENCODING));
        this.client.connect(inprocAddress);
    }

    @After
    public void tearDown() {
        this.client.disconnect(inprocAddress);
        this.client.close();

        //Uncomment when upgrading to ZMQ 4.1+
        //this.server.unbind(inprocAddress);
        this.server.close();

        this.context.close();
    }

    @Test
    public void testToTcpAddress() throws Exception {
        assertEquals(tcpAddress, ZmqUtil.toTcpAddress(tcpAddress));
        assertEquals(tcpAddress, ZmqUtil.toTcpAddress(unqualifiedAddress));
    }

    @Test
    public void testToInprocAddress() throws Exception {
        assertEquals(inprocAddress, ZmqUtil.toInprocAddress(inprocAddress));
        assertEquals(inprocAddress, ZmqUtil.toInprocAddress(unqualifiedAddress));
    }

    @Test
    public void testToAddressPort() throws Exception {
        //TODO-GM
    }

    @Test
    public void testSend() throws Exception {
        assertTrue(ZmqUtil.send(client, dummyRequest));

        String peerId = server.recvStr(Constants.DEFAULT_ENCODING);
        assertNotNull(peerId);

        byte[] base = server.recv();
        BaseMessage message = BaseMessage.parseFrom(base);

        assertEquals(ZmqUtil.getType(dummyRequest), message.getType());
        assertArrayEquals(dummyRequest.toByteArray(), message.getData().toByteArray());
    }

    @Test
    public void testSendToPeer() throws Exception {
        //The client must first contact the server
        //so that the server can later send a message back
        String init = "init";
        client.send(init);
        String peerId = server.recvStr(Constants.DEFAULT_ENCODING);
        String recvInit = server.recvStr(Constants.DEFAULT_ENCODING);

        ZmqUtil.sendToPeer(server, peerId, dummyRequest);

        byte[] baseData = client.recv();
        BaseMessage message = BaseMessage.parseFrom(baseData);

        assertEquals(ZmqUtil.getType(dummyRequest), message.getType());
        assertArrayEquals(dummyRequest.toByteArray(), message.getData().toByteArray());
    }

    @Test
    public void testReceive() throws Exception {
        //The client must first contact the server
        //so that the server can later send a message back
        String init = "init";
        client.send(init);
        String peerId = server.recvStr(Constants.DEFAULT_ENCODING);
        String recvInit = server.recvStr(Constants.DEFAULT_ENCODING);

        ZmqUtil.sendToPeer(server, peerId, dummyRequest);

        MessageContainer container = new MessageContainer();
        assertTrue(ZmqUtil.receive(client, container));

        assertEquals(ZmqUtil.getType(dummyRequest), container.getType());
        assertArrayEquals(dummyRequest.toByteArray(), container.getData().toByteArray());

        //Test when message is invalid
        server.send(peerId, ZMQ.SNDMORE);
        server.send(peerId, ZMQ.SNDMORE);
        server.send(peerId);

        assertFalse(ZmqUtil.receive(client, container));

        server.send(peerId, ZMQ.SNDMORE);
        server.send(peerId);
        assertFalse(ZmqUtil.receive(client, container));
    }

    @Test
    public void testReceiveFromPeer() throws Exception {
        //Test success case
        ZmqUtil.send(client, dummyRequest);

        PeerMessage peerMessage = new PeerMessage();
        assertTrue(ZmqUtil.receiveFromPeer(server, peerMessage));
        assertEquals(clientID, peerMessage.getPeerId());
        assertEquals(ZmqUtil.getType(dummyRequest), peerMessage.getMessage().getType());
        assertEquals(dummyRequest.toByteString(), peerMessage.getMessage().getData());

        //Test multipart message
        client.send(clientID, ZMQ.SNDMORE);
        client.send(clientID);
        assertFalse(ZmqUtil.receiveFromPeer(server, peerMessage));

        //Test message with not enough parts
        client.send(clientID);
        assertFalse(ZmqUtil.receiveFromPeer(server, peerMessage));
    }

    @Test
    public void testSendCompositeMessageByType() throws Exception {
        assertTrue(ZmqUtil.sendCompositeMessage(client, MessageType.Types.ALIVE_PING));

        String peerId = server.recvStr(Constants.DEFAULT_ENCODING);
        byte[] messageData = server.recv();

        MessageType messageType = MessageType.parseFrom(messageData);
        assertEquals(MessageType.Types.ALIVE_PING, messageType.getType());
    }

    @Test
    public void testSendCompositeMessage() throws Exception {
        assertTrue(ZmqUtil.sendCompositeMessage(client, MessageType.Types.ALIVE_PING, dummyRequest));

        String peerId = server.recvStr(Constants.DEFAULT_ENCODING);
        byte[] typeData = server.recv();
        byte[] messageData = server.recv();

        MessageType messageType = MessageType.parseFrom(typeData);
        assertEquals(MessageType.Types.ALIVE_PING, messageType.getType());

        TestRequest request = TestRequest.parseFrom(messageData);
        assertEquals(dummyRequest.toByteString(), request.toByteString());
    }


    @Test
    public void testSendCompositeMessageToPeerByType() throws Exception {
        String init = "init";
        client.send(init);
        String peerId = server.recvStr(Constants.DEFAULT_ENCODING);
        String recvInit = server.recvStr(Constants.DEFAULT_ENCODING);

        assertTrue(ZmqUtil.sendCompositeMessageToPeer(server, peerId, MessageType.Types.ALIVE_PING));

        byte[] typeData = client.recv();

        MessageType messageType = MessageType.parseFrom(typeData);
        assertEquals(MessageType.Types.ALIVE_PING, messageType.getType());
    }

    @Test
    public void testSendCompositeMessageToPeer() throws Exception {
        String init = "init";
        client.send(init);
        String peerId = server.recvStr(Constants.DEFAULT_ENCODING);
        String recvInit = server.recvStr(Constants.DEFAULT_ENCODING);

        assertTrue(ZmqUtil.sendCompositeMessageToPeer(server, peerId, MessageType.Types.SERVICE_REQUEST, dummyRequest));

        byte[] typeData = client.recv();
        byte[] messageData = client.recv();

        MessageType messageType = MessageType.parseFrom(typeData);
        assertEquals(MessageType.Types.SERVICE_REQUEST, messageType.getType());

        TestRequest request = TestRequest.parseFrom(messageData);
        assertEquals(dummyRequest.toByteString(), request.toByteString());
    }

    @Test
    public void testReceiveCompositeMessage() throws Exception {
        ZmqUtil.send(client, dummyRequest);

        ArrayList<byte[]> message = new ArrayList<byte[]>();
        ZmqUtil.receiveCompositeMessage(server, message);

        assertEquals(2, message.size());

        BaseMessage envelope = BaseMessage.parseFrom(message.get(1));
        assertEquals(ZmqUtil.getType(dummyRequest), envelope.getType());
        assertEquals(dummyRequest.toByteString(), envelope.getData());
    }

    @Test
    public void testReceiveCompositeMessageFromPeer() throws Exception {
        ZmqUtil.send(client, dummyRequest);

        ArrayList<byte[]> message = new ArrayList<byte[]>();
        String peerId = ZmqUtil.receiveCompositeMessageFromPeer(server, message);

        assertEquals(clientID, peerId);
        assertEquals(1, message.size());

        BaseMessage envelope = BaseMessage.parseFrom(message.get(0));
        assertEquals(ZmqUtil.getType(dummyRequest), envelope.getType());
        assertEquals(dummyRequest.toByteString(), envelope.getData());
    }


    @Test
    public void testSendServiceResponseSuccess() throws Exception {
        //TODO
    }

    @Test
    public void testSendServiceResponse() throws Exception {
        //TODO
    }
}