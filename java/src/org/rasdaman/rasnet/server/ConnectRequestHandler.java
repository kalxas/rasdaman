package org.rasdaman.rasnet.server;

import com.google.protobuf.InvalidProtocolBufferException;
import org.rasdaman.rasnet.exception.InvalidClient;
import org.rasdaman.rasnet.exception.NetworkingException;
import org.rasdaman.rasnet.exception.UnsupportedMessageType;
import org.rasdaman.rasnet.message.Communication;
import org.rasdaman.rasnet.util.ContainerMessage;
import org.rasdaman.rasnet.util.ProtoZmq;
import org.zeromq.ZMQ;

public class ConnectRequestHandler {
    private ClientPool clientPool;
    private Communication.ExternalConnectReply reply;
    private String acceptedMessageType;

    public ConnectRequestHandler(ClientPool clientPool, int serverRetries, int serverLifetime) {
        this.clientPool = clientPool;
        this.reply = Communication.ExternalConnectReply.newBuilder()
                .setRetries(serverRetries)
                .setPeriod(serverLifetime)
                .build();
        this.acceptedMessageType = ProtoZmq.getType(Communication.ExternalConnectRequest.getDefaultInstance());
    }

    public boolean canHandle(ContainerMessage message) {
        return (this.acceptedMessageType.equals(message.getType()));
    }

    public void handle(ContainerMessage message, ZMQ.Socket replyDestination, String peerId) throws UnsupportedMessageType, InvalidProtocolBufferException, InvalidClient {
        if (this.canHandle(message)) {
            Communication.ExternalConnectRequest request = Communication.ExternalConnectRequest.parseFrom(message.getData());
            this.clientPool.addClient(peerId, request.getPeriod(), request.getRetries());

            try {
                ProtoZmq.sendToPeer(replyDestination, peerId, this.reply);
            } catch (NetworkingException e) {
                //TODO-GM: Log error
            }
        } else {
            throw new UnsupportedMessageType(message.getType());
        }
    }
}
