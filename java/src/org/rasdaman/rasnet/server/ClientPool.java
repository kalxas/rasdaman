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

public class ClientPool {
    private static Logger LOG = LoggerFactory.getLogger(ClientPool.class);
    private HashMap<String, PeerStatus> clients;
    private int minimumPollPeriod;

    public ClientPool() {
        this.clients = new HashMap<String, PeerStatus>();
        this.minimumPollPeriod = Integer.MAX_VALUE;
    }

    void addClient(String clientId, int lifetime, int retries) throws InvalidClient {
        if (clients.containsKey(clientId)) {
            throw new InvalidClient("There already is a client with the given ID.");
        } else {
            PeerStatus status = new PeerStatus(retries, lifetime);
            this.clients.put(clientId, status);
            this.minimumPollPeriod = Math.min(this.minimumPollPeriod, lifetime);
        }
    }

    public int getMinimumPollPeriod() {
        return this.minimumPollPeriod;
    }

    public void resetClientStatus(String clientId) {
        PeerStatus status = this.clients.get(clientId);
        if (status != null) {
            status.reset();
        }
    }

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

    public void removeClient(String client) {
        this.clients.remove(client);
    }

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

    public boolean isClientAlive(String clientId) {
        boolean result = false;

        PeerStatus peerStatus = this.clients.get(clientId);
        if (peerStatus != null) {
            peerStatus.decreaseLiveliness();
            result = peerStatus.isAlive();
        }

        return result;
    }

    void removeAllClients() {
        this.clients.clear();
    }

}
