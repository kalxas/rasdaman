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

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.rasdaman.rasnet.common.Constants;
import org.rasdaman.rasnet.exception.UnsupportedMessageType;
import org.rasdaman.rasnet.util.PeerStatus;
import org.rasdaman.rasnet.util.ZmqUtil;
import org.zeromq.ZMQ;

import java.util.ArrayList;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.rasdaman.rasnet.message.Communication.MessageType;
import static org.rasdaman.rasnet.message.Communication.MessageType.Types.ALIVE_PING;
import static org.rasdaman.rasnet.message.Communication.MessageType.Types.ALIVE_PONG;

public class ClientPingHandlerTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private ZMQ.Context context;
    private ZMQ.Socket server;
    private ZMQ.Socket client;
    private String address;
    private ClientPingHandler pingHandler;
    private String clientId;
    private PeerStatus serverStatus;

    @Before
    public void setUp() throws Exception {
        this.address = "inproc://clientping";
        this.clientId = "clientId";
        this.context = ZMQ.context(1);

        this.serverStatus = mock(PeerStatus.class);

        this.server = this.context.socket(ZMQ.ROUTER);
        this.server.bind(address);

        this.client = this.context.socket(ZMQ.DEALER);
        this.client.setIdentity(clientId.getBytes(Constants.DEFAULT_ENCODING));
        this.client.connect(address);

        PeerStatus status = new PeerStatus(3, 1000);
        pingHandler = new ClientPingHandler(client, serverStatus);
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
        byte[] ping = MessageType.newBuilder().setType(ALIVE_PING).build().toByteArray();
        ArrayList<byte[]> goodMessage = new ArrayList<>();
        goodMessage.add(ping);

        assertTrue(pingHandler.canHandle(goodMessage));

        byte[] pong = MessageType.newBuilder().setType(ALIVE_PONG).build().toByteArray();
        ArrayList<byte[]> badMessage = new ArrayList<>();
        badMessage.add(pong);

        assertFalse(pingHandler.canHandle(badMessage));
    }

    @Test
    public void testHandleFailure() throws Exception {
        thrown.expect(UnsupportedMessageType.class);

        byte[] pong = MessageType.newBuilder().setType(ALIVE_PONG).build().toByteArray();
        ArrayList<byte[]> badMessage = new ArrayList<>();
        badMessage.add(pong);

        pingHandler.handle(badMessage);
    }

    @Test
    public void testHandle() throws Exception {
        byte[] ping = MessageType.newBuilder().setType(ALIVE_PING).build().toByteArray();
        ArrayList<byte[]> goodMessage = new ArrayList<>();
        goodMessage.add(ping);

        pingHandler.handle(goodMessage);

        ArrayList<byte[]> message = new ArrayList<>();
        String peerId = ZmqUtil.receiveCompositeMessageFromPeer(server, message);

        assertEquals(1, message.size());
        MessageType type = MessageType.parseFrom(message.get(0));
        assertEquals(ALIVE_PONG, type.getType());

        verify(this.serverStatus).reset();
    }
}