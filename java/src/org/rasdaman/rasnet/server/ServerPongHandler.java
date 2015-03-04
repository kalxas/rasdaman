package org.rasdaman.rasnet.server;

import com.google.protobuf.InvalidProtocolBufferException;
import org.rasdaman.rasnet.exception.UnsupportedMessageType;
import org.rasdaman.rasnet.message.Communication;
import org.rasdaman.rasnet.util.ContainerMessage;
import org.rasdaman.rasnet.util.ProtoZmq;
import org.zeromq.ZMQ;

/**
 * Created by rasdaman on 2/19/15.
 */
public class ServerPongHandler {
    private String acceptedMessageType;
    private ClientPool clientPool;

    public ServerPongHandler(ClientPool clientPool) {
        this.clientPool = clientPool;
        this.acceptedMessageType = ProtoZmq.getType(Communication.AlivePong.getDefaultInstance());
    }

    public boolean canHandle(ContainerMessage message) {
        return (this.acceptedMessageType.equals(message.getType()));
    }

    public void handle(ContainerMessage message, ZMQ.Socket responseDestination, String peerId) throws UnsupportedMessageType {
        //Sanity check.
        //Parse the message data to make sure that no invalid message arrives here
        if (this.canHandle(message)) {
            this.clientPool.resetClientStatus(peerId);
        } else {
            throw new UnsupportedMessageType(message.getType());
        }
    }
}
