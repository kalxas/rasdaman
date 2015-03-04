package org.rasdaman.rasnet.server;

import com.google.protobuf.InvalidProtocolBufferException;
import org.rasdaman.rasnet.exception.NetworkingException;
import org.rasdaman.rasnet.exception.UnsupportedMessageType;
import org.rasdaman.rasnet.message.Communication;
import org.rasdaman.rasnet.util.ContainerMessage;
import org.rasdaman.rasnet.util.ProtoZmq;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;

public class ServerPingHandler {
    private static Logger LOG = LoggerFactory.getLogger(ClientPool.class);
    private String acceptedMessageType;
    private Communication.AlivePong pong;
    private ClientPool clientPool;

    public ServerPingHandler(ClientPool clientPool) {
        this.clientPool = clientPool;
        this.pong = Communication.AlivePong.getDefaultInstance();
        this.acceptedMessageType = Communication.AlivePing.getDefaultInstance().getDescriptorForType().getFullName();
    }

    public boolean canHandle(ContainerMessage message) {
        return (this.acceptedMessageType.equals(message.getType()));
    }

    public void handle(ContainerMessage message, ZMQ.Socket responseDestination, String peerId) throws InvalidProtocolBufferException, UnsupportedMessageType {
        //Parse the message to make sure the correct message was passed in.
        Communication.AlivePing.parseFrom(message.getData());

        if (this.canHandle(message)) {
            try {
                this.clientPool.resetClientStatus(peerId);
                ProtoZmq.sendToPeer(responseDestination, peerId, this.pong);
            } catch (NetworkingException e) {
                LOG.error(e.getMessage());
            }
        } else {
            throw new UnsupportedMessageType(message.getType());
        }
    }
}
