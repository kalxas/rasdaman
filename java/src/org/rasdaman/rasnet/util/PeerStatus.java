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

/**
 * @brief The PeerStatus class
 *  This class is used to maintain information about the state of a network peer.
 */
public class PeerStatus {
    /**
     *  The original number of retries the peer has until it is declared dead
     */
    private final int retriesBackup;

    /**
     * The number of retries left before the peer is declared dead*
     */
    private int retries;

    /**
     *  Timer used to keep track of the period between two signals from the peer
     */
    private Timer timer;

    /**
     * Initialize the PeerStatus object with a number of attempts to detect if the peer is alive
     * and a period after which, if a signal is not received from the peer, the number of retries is decreased.
     * @param retries The number of times we try to determine if the peer is alive
     * @param lifetime The period in milliseconds after which, if a signal is not received, we decrease the number of retries
     */
    public PeerStatus(int retries, int lifetime) {
        if (retries < 0 || lifetime < 0) {
            throw new IllegalArgumentException("The number of retries and the lifetime must be positive.");
        }

        this.timer = new Timer(lifetime);
        this.retries = retries;
        this.retriesBackup = retries;
    }

    /**
     * Check if the peer is alive.
     * @return true if the peer is alive i.e. the number of retries has not reached 0, false otherwise
     */
    public boolean isAlive() {
        return this.retries > 0;
    }

    /**
     * Decrease the number of retries by 1 if a time larger than the lifetime
     * has passed from the last signal from the peer.
     * @return true if the number of retries has been decremented, false otherwise
     */
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

    /**
     * Reset the status of the peer .
     */
    public void reset() {
        this.retries = this.retriesBackup;
        this.timer.reset();
    }
}
