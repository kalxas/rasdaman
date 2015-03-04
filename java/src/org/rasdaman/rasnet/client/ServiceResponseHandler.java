package org.rasdaman.rasnet.client;

import com.google.protobuf.InvalidProtocolBufferException;
import org.rasdaman.rasnet.exception.NetworkingException;
import org.rasdaman.rasnet.exception.UnsupportedMessageType;
import org.rasdaman.rasnet.message.Internal;
import org.rasdaman.rasnet.message.Service;
import org.rasdaman.rasnet.util.ContainerMessage;
import org.rasdaman.rasnet.util.ProtoZmq;
import org.zeromq.ZMQ;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceResponseHandler {
    private final ConcurrentHashMap<Long, Service.ServiceResponse> serviceResponses;
    private ZMQ.Socket bridgeSocket;
    private HashMap<Long, String> outstandingRequests;
    private String acceptedMessageType;

    public ServiceResponseHandler(HashMap<Long, String> outstandingRequests, ConcurrentHashMap<Long, Service.ServiceResponse> serviceResponses, ZMQ.Socket bridgeSocket) {
        this.bridgeSocket = bridgeSocket;
        this.outstandingRequests = outstandingRequests;
        this.serviceResponses = serviceResponses;
        this.acceptedMessageType = ProtoZmq.getType(Service.ServiceResponse.getDefaultInstance());
    }

    public boolean canHandle(ContainerMessage message) {
        return this.acceptedMessageType.equals(message.getType());
    }

    public void handle(ContainerMessage message, ZMQ.Socket serverSocket) throws UnsupportedMessageType, InvalidProtocolBufferException, NetworkingException {
        if (this.canHandle(message)) {
            //Parse the service response and add it to the collection of responses
            Service.ServiceResponse response = Service.ServiceResponse.parseFrom(message.getData());
            String peerId = this.outstandingRequests.get(response.getId());

            if (peerId != null) {
                this.serviceResponses.put(response.getId(), response);
                //Notify the CallMethod thread that the response is available
                Internal.ResponseAvailable responseAvailable = Internal.ResponseAvailable.getDefaultInstance();
                ProtoZmq.sendToPeer(this.bridgeSocket, peerId, responseAvailable);
            }

            this.outstandingRequests.remove(response.getId());
        } else {
            throw new UnsupportedMessageType(message.getType());
        }
    }
}
