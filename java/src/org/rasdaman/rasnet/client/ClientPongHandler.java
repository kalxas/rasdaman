package org.rasdaman.rasnet.client;

import org.rasdaman.rasnet.exception.UnsupportedMessageType;
import org.rasdaman.rasnet.message.Communication;
import org.rasdaman.rasnet.util.ContainerMessage;
import org.rasdaman.rasnet.util.PeerStatus;
import org.rasdaman.rasnet.util.ProtoZmq;
import org.zeromq.ZMQ;

public class ClientPongHandler {
    private PeerStatus serverStatus;
    private String acceptedMessageType;

    public ClientPongHandler(PeerStatus serverStatus) {
        this.serverStatus = serverStatus;
        this.acceptedMessageType = ProtoZmq.getType(Communication.AlivePong.getDefaultInstance());
    }

    public boolean canHandle(ContainerMessage message) {
        return (this.acceptedMessageType.equals(message.getType()));
    }

    public void handle(ContainerMessage message, ZMQ.Socket responseDestination) throws UnsupportedMessageType {
        if (this.canHandle(message)) {
            this.serverStatus.reset();
        } else {
            throw new UnsupportedMessageType(message.getType());
        }
    }
}
