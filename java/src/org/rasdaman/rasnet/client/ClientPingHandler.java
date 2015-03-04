package org.rasdaman.rasnet.client;

import org.rasdaman.rasnet.exception.NetworkingException;
import org.rasdaman.rasnet.exception.UnsupportedMessageType;
import org.rasdaman.rasnet.message.Communication;
import org.rasdaman.rasnet.util.ContainerMessage;
import org.rasdaman.rasnet.util.PeerStatus;
import org.rasdaman.rasnet.util.ProtoZmq;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;

import static org.rasdaman.rasnet.message.Communication.AlivePing;

public class ClientPingHandler {
    private static Logger LOG = LoggerFactory.getLogger(ClientPingHandler.class);
    private PeerStatus serverStatus;
    private String acceptedMessageType;
    private Communication.AlivePong pong;

    public ClientPingHandler(PeerStatus status) {
        this.serverStatus = status;
        this.pong = Communication.AlivePong.getDefaultInstance();
        this.acceptedMessageType = ProtoZmq.getType(AlivePing.getDefaultInstance());
    }

    public boolean canHandle(ContainerMessage message) {
        return (acceptedMessageType.equals(message.getType()));
    }

    public void handle(ContainerMessage message, ZMQ.Socket socket) throws UnsupportedMessageType {
        //Parse the message to make sure the correct message was passed in.
        if (this.canHandle(message)) {
            try {
                this.serverStatus.reset();
                ProtoZmq.send(socket, this.pong);
            } catch (NetworkingException e) {
                LOG.error(e.getMessage());
            }
        } else {
            throw new UnsupportedMessageType(message.getType());
        }
    }
}
