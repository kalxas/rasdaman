package org.rasdaman.rasnet.server;

import org.rasdaman.rasnet.exception.InvalidClient;
import org.rasdaman.rasnet.exception.NetworkingException;
import org.rasdaman.rasnet.message.Communication;
import org.rasdaman.rasnet.util.PeerStatus;
import org.rasdaman.rasnet.util.ProtoZmq;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by rasdaman on 2/19/15.
 */
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
            throw new InvalidClient("There already is a client.");
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
        Communication.AlivePing ping = Communication.AlivePing.getDefaultInstance();

        Iterator<Map.Entry<String, PeerStatus>> iter = this.clients.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, PeerStatus> entry = iter.next();

            if (entry.getValue().decreaseLiveliness()) {
                try {
                    ProtoZmq.sendToPeer(socket, entry.getKey(), ping);
                } catch (NetworkingException e) {
                    LOG.error(e.getMessage());
                }
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
