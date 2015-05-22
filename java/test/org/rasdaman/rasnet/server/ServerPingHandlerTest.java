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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.rasdaman.rasnet.message.Communication.MessageType.Types.ALIVE_PING;
import static org.rasdaman.rasnet.message.Communication.MessageType.Types.ALIVE_PONG;

public class ServerPingHandlerTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private ZMQ.Context context;
    private ZMQ.Socket server;
    private ZMQ.Socket client;
    private String address;
    private String clientId;
    private ClientPool clientPool;
    private ServerPingHandler pingHandler;

    @Before
    public void setUp() throws Exception {
        this.address = "inproc://clientping";
        this.clientId = "clientId";
        this.context = ZMQ.context(1);

        this.clientPool = mock(ClientPool.class);

        this.server = this.context.socket(ZMQ.ROUTER);
        this.server.bind(address);

        this.client = this.context.socket(ZMQ.DEALER);
        this.client.setIdentity(clientId.getBytes(Constants.DEFAULT_ENCODING));
        this.client.connect(address);

        pingHandler = new ServerPingHandler(server, clientPool);
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
        ZmqUtil.sendCompositeMessage(client, ALIVE_PING);
        ArrayList<byte[]> goodMessage = new ArrayList<>();
        ZmqUtil.receiveCompositeMessageFromPeer(server, goodMessage);

        assertTrue(pingHandler.canHandle(goodMessage));

        byte[] pong = Communication.MessageType.newBuilder().setType(ALIVE_PONG).build().toByteArray();
        ArrayList<byte[]> badMessage = new ArrayList<>();
        badMessage.add(pong);

        assertFalse(pingHandler.canHandle(badMessage));
    }

    @Test
    public void testHandleFailure() throws Exception {
        thrown.expect(UnsupportedMessageType.class);

        byte[] pong = Communication.MessageType.newBuilder().setType(ALIVE_PONG).build().toByteArray();
        ArrayList<byte[]> badMessage = new ArrayList<>();
        badMessage.add(pong);

        pingHandler.handle(badMessage, clientId);
    }

    @Test
    public void testHandle() throws Exception {
        ZmqUtil.sendCompositeMessage(client, ALIVE_PING);
        ArrayList<byte[]> goodMessage = new ArrayList<>();
        String peerId = ZmqUtil.receiveCompositeMessageFromPeer(server, goodMessage);

        when(clientPool.isClientAlive(anyString())).thenReturn(true);

        pingHandler.handle(goodMessage, peerId);

        ArrayList<byte[]> message = new ArrayList<>();
        ZmqUtil.receiveCompositeMessage(client, message);

        assertEquals(1, message.size());
        Communication.MessageType type = Communication.MessageType.parseFrom(message.get(0));
        assertEquals(ALIVE_PONG, type.getType());

        verify(this.clientPool).resetClientStatus(peerId);
    }
}