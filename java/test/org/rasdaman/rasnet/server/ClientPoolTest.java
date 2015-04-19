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
import org.junit.Test;
import org.zeromq.ZMQ;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ClientPoolTest {
    private ClientPool pool;
    private int retries = 1;
    private int lifetime = 1;

    @Before
    public void setUp() {
        this.pool = new ClientPool();
    }

    @Test
    public void testAddClient() throws Exception {
        String clientId = "test";
        pool.addClient(clientId, this.lifetime, this.retries);

        //The client is alive
        assertTrue(pool.isClientAlive(clientId));
        Thread.sleep(lifetime);

        pool.removeDeadClients();

        assertFalse(pool.isClientAlive(clientId));
    }

    @Test
    public void testGetMinimumPollPeriod() throws Exception {
        String clientId = "test";

        assertEquals(Integer.MAX_VALUE, pool.getMinimumPollPeriod());
        pool.addClient(clientId, this.lifetime, this.retries);
        assertEquals(this.lifetime, pool.getMinimumPollPeriod());
    }

    @Test
    public void testResetClientStatus() throws Exception {
        String clientId = "test";
        pool.addClient(clientId, this.lifetime, this.retries + 1);

        assertTrue(pool.isClientAlive(clientId));
        //Sleep so that the client's life counter decreases by one.
        Thread.sleep(lifetime);
        assertTrue(pool.isClientAlive(clientId));

        pool.resetClientStatus(clientId);
        Thread.sleep(lifetime);
        assertTrue(pool.isClientAlive(clientId));

        Thread.sleep(lifetime);
        assertFalse(pool.isClientAlive(clientId));
    }

    @Test
    public void testPingAllClients() throws Exception {
        ZMQ.Socket socket = mock(ZMQ.Socket.class);
        when(socket.send(any(byte[].class), anyInt())).thenReturn(true);
        when(socket.send(anyString())).thenReturn(true);

        pool.pingAllClients(socket);
    }

    @Test
    public void testRemoveClient() throws Exception {
        String clientId = "test";
        pool.addClient(clientId, this.lifetime, this.retries);

        pool.removeClient(clientId);

        assertFalse(pool.isClientAlive(clientId));
    }

    @Test
    public void testRemoveDeadClients() throws Exception {
        String clientId = "test";
        pool.addClient(clientId, this.lifetime, this.retries);

        assertTrue(pool.isClientAlive(clientId));
        Thread.sleep(this.lifetime);
        assertFalse(pool.isClientAlive(clientId));

        pool.removeDeadClients();

        //The client was removed so this will have no effect
        pool.resetClientStatus(clientId);
        assertFalse(pool.isClientAlive(clientId));
    }

    @Test
    public void testIsClientAlive() throws Exception {
        String clientId = "test";
        pool.addClient(clientId, this.lifetime, this.retries);

        assertTrue(pool.isClientAlive(clientId));

        Thread.sleep(this.lifetime);
        pool.removeDeadClients();
        assertFalse(pool.isClientAlive(clientId));
    }

    @Test
    public void testRemoveAllClients() throws Exception {
        String clientId = "test";
        pool.addClient(clientId, this.lifetime, this.retries);

        assertTrue(pool.isClientAlive(clientId));
        pool.removeAllClients();
        assertFalse(pool.isClientAlive(clientId));
    }
}