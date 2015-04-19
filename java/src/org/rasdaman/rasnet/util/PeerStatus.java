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

public class PeerStatus {
    private final int retriesBackup;
    private int retries;
    private Timer timer;

    public PeerStatus(int retries, int lifetime) {
        if (retries < 0 || lifetime < 0) {
            throw new IllegalArgumentException("The number of retries and the lifetime must be positive.");
        }

        this.timer = new Timer(lifetime);
        this.retries = retries;
        this.retriesBackup = retries;
    }

    public boolean isAlive() {
        return this.retries > 0;
    }

    public boolean decreaseLiveliness() {
        if (this.timer.hasExpired()) {
            if (this.retries > 0) {
                this.retries--;
                this.timer.reset();
            }
            return true;
        } else {
            return false;
        }
    }

    public void reset() {
        this.retries = this.retriesBackup;
        this.timer.reset();
    }
}
