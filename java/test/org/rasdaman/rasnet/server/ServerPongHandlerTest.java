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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.rasdaman.rasnet.exception.UnsupportedMessageType;
import org.rasdaman.rasnet.message.Communication;

import java.util.ArrayList;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.rasdaman.rasnet.message.Communication.MessageType.Types.ALIVE_PING;
import static org.rasdaman.rasnet.message.Communication.MessageType.Types.ALIVE_PONG;

public class ServerPongHandlerTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private ClientPool clientPool;
    private ServerPongHandler pongHandler;

    @Before
    public void setUp() throws Exception {
        this.clientPool = mock(ClientPool.class);
        this.pongHandler = new ServerPongHandler(clientPool);
    }

    @Test
    public void testCanHandle() throws Exception {
        byte[] pong = Communication.MessageType.newBuilder().setType(ALIVE_PONG).build().toByteArray();
        ArrayList<byte[]> goodMessage = new ArrayList<>();
        goodMessage.add(pong);

        assertTrue(pongHandler.canHandle(goodMessage));

        byte[] ping = Communication.MessageType.newBuilder().setType(ALIVE_PING).build().toByteArray();
        ArrayList<byte[]> badMessage = new ArrayList<>();
        badMessage.add(ping);

        assertFalse(pongHandler.canHandle(badMessage));
    }

    @Test
    public void testHandleFailure() throws Exception {
        thrown.expect(UnsupportedMessageType.class);

        String peerId = "peerId";
        byte[] ping = Communication.MessageType.newBuilder().setType(ALIVE_PING).build().toByteArray();
        ArrayList<byte[]> badMessage = new ArrayList<>();
        badMessage.add(ping);

        pongHandler.handle(badMessage, peerId);
    }

    @Test
    public void testHandle() throws Exception {
        String peerId = "peerId";
        byte[] pong = Communication.MessageType.newBuilder().setType(ALIVE_PONG).build().toByteArray();
        ArrayList<byte[]> goodMessage = new ArrayList<>();
        goodMessage.add(pong);

        pongHandler.handle(goodMessage, peerId);

        verify(this.clientPool).resetClientStatus(peerId);
    }
}