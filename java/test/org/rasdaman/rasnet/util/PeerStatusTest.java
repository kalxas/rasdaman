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

import org.junit.Before;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PeerStatusTest {
    private int lifetime;
    private int retries;
    private PeerStatus status;

    @Before
    public void setUp(){
        lifetime = 10;
        retries = 3;
        status = new PeerStatus(retries, lifetime);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor() throws Exception {
        Random r = new Random();

        int retries = -Math.abs(r.nextInt());
        int lifetime = -Math.abs(r.nextInt());

        PeerStatus st = new PeerStatus(retries, lifetime);
    }

    @Test
    public void testIsAlive() throws Exception {
        assertTrue(this.status.isAlive());
    }

    @Test
    public void testDecreaseLiveliness() throws Exception {
        assertTrue(this.status.isAlive());

        for(int i=0; i<retries; i++){
            Thread.sleep(this.lifetime);
            this.status.decreaseLiveliness();
        }

        assertFalse(this.status.isAlive());
    }

    @Test
    public void testReset() throws Exception {
        assertTrue(this.status.isAlive());

        for(int i=0; i<retries; i++){
            Thread.sleep(this.lifetime);
            this.status.decreaseLiveliness();
        }

        assertFalse(this.status.isAlive());
        this.status.reset();
        assertTrue(this.status.isAlive());
    }
}