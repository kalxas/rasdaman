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

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.rasdaman.rasnet.common.Constants;
import org.rasdaman.rasnet.exception.UnsupportedMessageType;
import org.rasdaman.rasnet.message.Communication;
import org.rasdaman.rasnet.util.ZmqUtil;
import org.zeromq.ZMQ;

import java.util.ArrayList;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.rasdaman.rasnet.message.Communication.MessageType.Types.*;

public class ConnectRequestHandlerTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private ZMQ.Context context;
    private ZMQ.Socket server;
    private ZMQ.Socket client;
    private String address;
    private String clientId;

    private ConnectRequestHandler connectHandler;
    private ClientPool clientPoolMock;

    private int retries;
    private int period;
    private Communication.ConnectRequest request;

    @Before
    public void setUp() {
        this.retries = 1;
        this.period = 10;
        this.request = Communication.ConnectRequest
                .newBuilder()
                .setLifetime(period)
                .setRetries(retries).build();
        this.address = "inproc://clientping";
        this.clientId = "clientId";

        this.clientPoolMock = mock(ClientPool.class);

        this.context = ZMQ.context(1);

        this.server = this.context.socket(ZMQ.ROUTER);
        this.server.bind(address);

        this.client = this.context.socket(ZMQ.DEALER);
        this.client.setIdentity(clientId.getBytes(Constants.DEFAULT_ENCODING));
        this.client.connect(address);

        this.connectHandler = new ConnectRequestHandler(server, this.clientPoolMock, this.retries, this.period);
    }

    @After
    public void tearDown() {
        this.client.disconnect(address);
        this.client.close();

        //this.server.unbind(address);
        this.server.close();

        this.context.close();
    }

    @Test
    public void testCanHandle() throws Exception {
        ZmqUtil.sendCompositeMessage(client, CONNECT_REQUEST, request);

        ArrayList<byte[]> goodMessage = new ArrayList<>();
        ZmqUtil.receiveCompositeMessageFromPeer(server, goodMessage);

        assertTrue(connectHandler.canHandle(goodMessage));

        byte[] pong = Communication.MessageType.newBuilder().setType(ALIVE_PONG).build().toByteArray();
        ArrayList<byte[]> badMessage = new ArrayList<>();
        badMessage.add(pong);
        //Will fail because the message is too short
        assertFalse(connectHandler.canHandle(badMessage));

        ZmqUtil.sendCompositeMessage(client, SERVICE_REQUEST, request);
        ZmqUtil.receiveCompositeMessageFromPeer(server, badMessage);
        assertFalse(connectHandler.canHandle(badMessage));
    }

    @Test
    public void testHandleFailure() throws Exception {
        thrown.expect(UnsupportedMessageType.class);

        byte[] pong = Communication.MessageType.newBuilder().setType(ALIVE_PONG).build().toByteArray();
        ArrayList<byte[]> badMessage = new ArrayList<>();
        badMessage.add(pong);

        connectHandler.handle(badMessage, clientId);
    }

    @Test
    public void testHandle() throws Exception {
        ZmqUtil.sendCompositeMessage(client, CONNECT_REQUEST, request);
        ArrayList<byte[]> goodMessage = new ArrayList<>();
        String peerId = ZmqUtil.receiveCompositeMessageFromPeer(server, goodMessage);

        connectHandler.handle(goodMessage, peerId);

        ArrayList<byte[]> responseMessage = new ArrayList<>();
        ZmqUtil.receiveCompositeMessage(client, responseMessage);

        assertEquals(2, responseMessage.size());
        Communication.MessageType type = Communication.MessageType.parseFrom(responseMessage.get(0));
        assertEquals(CONNECT_REPLY, type.getType());

        Communication.ConnectReply reply = Communication.ConnectReply.parseFrom(responseMessage.get(1));
        assertEquals(reply.getLifetime(), period);
        assertEquals(reply.getRetries(), retries);

        verify(this.clientPoolMock).addClient(peerId, request.getLifetime(), request.getRetries());
    }
}