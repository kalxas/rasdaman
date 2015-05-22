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

import org.rasdaman.rasnet.exception.InvalidClient;
import org.rasdaman.rasnet.message.Communication;
import org.rasdaman.rasnet.util.PeerStatus;
import org.rasdaman.rasnet.util.ZmqUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * @brief The ClientPool class Keeps track of the status of a list of clients.
 */
public class ClientPool {
    private static Logger LOG = LoggerFactory.getLogger(ClientPool.class);
    /**
     * Map between the client's ids and their status
     */
    private HashMap<String, PeerStatus> clients;

    /**
     * Map between period values and the number of clients with that period
     */
    private TreeMap<Integer, Integer> periods;

    public static int DEFAULT_MINIMUM_POLL_PERIOD = -1;

    public ClientPool() {
        this.clients = new HashMap<String, PeerStatus>();
        this.periods = new TreeMap<Integer, Integer>();
    }

    /**
     * Add the client with the given ID and connection parameters to the pool.
     *
     * @param clientId The ID uniquely represents the client with respect to the
     *                 server socket through which it sent the message.
     * @param lifetime The number of milliseconds after which the client's liveliness decreases
     * @param retries  The number of times the client's liveliness can decrease before declaring it dead
     *                 and removing it from the pool.
     */
    void addClient(String clientId, int lifetime, int retries) throws InvalidClient {
        if (clients.containsKey(clientId)) {
            throw new InvalidClient("There already is a client with the given ID.");
        } else {
            PeerStatus status = new PeerStatus(retries, lifetime);
            this.clients.put(clientId, status);

            Integer howManyClientsWithPeriod = this.periods.get(lifetime);
            if (howManyClientsWithPeriod == null) {
                this.periods.put(lifetime, 1);
            } else {
                this.periods.put(lifetime, howManyClientsWithPeriod + 1);
            }
        }
    }

    /**
     * @return The minimum value of the lifetimes of all the clients in the pool.
     * If no client is present in the pool, -1 is returned
     * @brief getMinimumPollPeriod Get the minimum number of milliseconds
     * between each check of the liveliness of the clients that would allow
     * for the status of each client to be up to date.
     */
    public int getMinimumPollPeriod() {
        if (this.periods.isEmpty()) {
            return DEFAULT_MINIMUM_POLL_PERIOD;
        } else {
            return periods.firstKey();
        }
    }

    /**
     * Reset the status of the client with the given id. This method should be used
     * when the client has sent a message to the server
     *
     * @param clientId The ID uniquely represents the client with respect to the
     *                 server socket through which it sent the message.
     */
    public void resetClientStatus(String clientId) {
        PeerStatus status = this.clients.get(clientId);
        if (status != null) {
            status.reset();
        }
    }

    /**
     * Send a ping to all the registered clients that have not had any activity.
     *
     * @param socket Socket through which to send the message
     */
    public void pingAllClients(ZMQ.Socket socket) {
        //Ping to send to all the clients
        Iterator<Map.Entry<String, PeerStatus>> iter = this.clients.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, PeerStatus> entry = iter.next();

            if (entry.getValue().decreaseLiveliness()) {
                ZmqUtil.sendCompositeMessageToPeer(socket, entry.getKey(), Communication.MessageType.Types.ALIVE_PING);
            }
        }
    }

    /**
     * Remove all the clients who are declared dead from the pool
     */
    public void removeDeadClients() {
        Iterator<Map.Entry<String, PeerStatus>> iter = this.clients.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, PeerStatus> entry = iter.next();

            entry.getValue().decreaseLiveliness();
            if (!entry.getValue().isAlive()) {
                iter.remove();
            }
        }
    }

    /**
     * Check if the client with the given ID is alive
     *
     * @param clientId
     * @return TRUE if the client is alive, FALSE otherwise
     */
    public boolean isClientAlive(String clientId) {
        boolean result = false;

        PeerStatus peerStatus = this.clients.get(clientId);
        if (peerStatus != null) {
            peerStatus.decreaseLiveliness();
            result = peerStatus.isAlive();
        }

        return result;
    }

    /**
     * Remove all the clients in the pool.
     */
    void removeAllClients() {
        this.clients.clear();
    }
}
