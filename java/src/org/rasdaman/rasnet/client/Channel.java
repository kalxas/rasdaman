package org.rasdaman.rasnet.client;

import com.google.protobuf.*;
import org.rasdaman.rasnet.common.Constants;
import org.rasdaman.rasnet.exception.ConnectionTimeoutException;
import org.rasdaman.rasnet.exception.NetworkingException;
import org.rasdaman.rasnet.exception.UnsupportedMessageType;
import org.rasdaman.rasnet.message.Communication;
import org.rasdaman.rasnet.message.Internal;
import org.rasdaman.rasnet.message.Service;
import org.rasdaman.rasnet.util.ContainerMessage;
import org.rasdaman.rasnet.util.PeerStatus;
import org.rasdaman.rasnet.util.ProtoZmq;
import org.rasdaman.rasnet.util.ZmqUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Channel implements RpcChannel, AutoCloseable {
    private static Logger LOG = LoggerFactory.getLogger(Channel.class);

    volatile boolean closed;
    volatile boolean running;
    private ConcurrentLinkedQueue<Service.ServiceRequest> serviceRequests;
    private ConcurrentHashMap<Long, Service.ServiceResponse> serviceResponses;
    private int linger;
    private long counter;
    private String serverEndpoint;
    private ChannelConfig config;
    private ZMQ.Context context;
    private ZMQ.Socket controlSocket;

    private String identity;
    private String controlSocketAddress;
    private String bridgeAddress;
    private Thread handlerThread;

    public Channel(String serverEndpoint, ChannelConfig config) throws ConnectionTimeoutException {
        this.serverEndpoint = serverEndpoint;
        this.config = config;
        this.linger = 0;
        this.counter = 0;
        this.identity = UUID.randomUUID().toString();
        this.controlSocketAddress = ZmqUtil.toInprocAddress(String.format("control-%s}", this.identity));
        this.bridgeAddress = ZmqUtil.toInprocAddress(String.format("bridge-%s}", this.identity));

        this.context = ZMQ.context(this.config.getIoThreadsNo());
        this.context.setMaxSockets(this.config.getMaxOpenSockets());

        this.controlSocket = this.context.socket(ZMQ.PAIR);
        this.controlSocket.setLinger(this.linger);
        this.controlSocket.bind(this.controlSocketAddress);

        this.closed = false;
        this.serviceRequests = new ConcurrentLinkedQueue<Service.ServiceRequest>();
        this.serviceResponses = new ConcurrentHashMap<Long, Service.ServiceResponse>();

        this.handlerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                communicationHandler();
            }
        });

        this.handlerThread.start();
        this.connectToServer();
    }


    @Override
    public void callMethod(Descriptors.MethodDescriptor method, RpcController controller, Message request, Message responsePrototype, RpcCallback<Message> done) {
        if (this.closed || !this.running) {
            controller.setFailed("Channel is closed. No further calls are possible.");
            return;
        }

        try {
            long counterValue;

            synchronized (this) {
                counterValue = this.counter;
                this.counter++;
            }

            //Service request
            Service.ServiceRequest serviceRequest = Service.ServiceRequest
                    .newBuilder()
                    .setId(counterValue)
                    .setMethodName(method.getFullName())
                    .setInputValue(request.toByteString())
                    .build();

            //Add the request to the list of pending request calls.
            this.serviceRequests.add(serviceRequest);

            String socketID = UUID.randomUUID().toString();

            ZMQ.Socket bridgeSocket = this.context.socket(ZMQ.DEALER);
            bridgeSocket.setLinger(this.linger);
            bridgeSocket.setIdentity(socketID.getBytes(Constants.DEFAULT_ENCODING));
            bridgeSocket.connect(this.bridgeAddress);


            ContainerMessage message = new ContainerMessage();
            Internal.RequestAvailable requestAvailable = Internal.RequestAvailable.getDefaultInstance();

            //Notify the worker thread that a new request is pending
            ProtoZmq.send(bridgeSocket, requestAvailable);

            //Wait for a response for the service call.
            ProtoZmq.receive(bridgeSocket, message);

            Service.ServiceResponse response = null;
            if (ProtoZmq.getType(Internal.ResponseAvailable.getDefaultInstance()).equals(message.getType())) {
                response = this.serviceResponses.get(counterValue);
                if (response != null && done != null) {
                    //Run the callback
                    Message responseMessage = responsePrototype.getParserForType().parseFrom(response.getOutputValue());
                    done.run(responseMessage);
                } else {
                    String errorMessage = "Implementation error. Invalid service response.";
                    controller.setFailed(errorMessage);
                    LOG.error(errorMessage);
                }
            } else {
                String errorMessage = "Invalid inter-thread message. Expected ResponseAvailable message.";
                controller.setFailed(errorMessage);
                LOG.error(errorMessage);
            }

            bridgeSocket.disconnect(this.bridgeAddress);
            bridgeSocket.close();

        } catch (NetworkingException e) {
            controller.setFailed(e.getMessage());
        } catch (InvalidProtocolBufferException e) {
            controller.setFailed(e.getMessage());
        } catch (Exception e) {
            controller.setFailed(e.getMessage());
        }
    }

    @Override
    public void close() throws Exception {
        if (this.running) {
            this.running = false;
            ContainerMessage connectReply = new ContainerMessage();
            Communication.InternalDisconnectRequest disconnectRequest = Communication.InternalDisconnectRequest.getDefaultInstance();
            String expectedReplyType = ProtoZmq.getType(Communication.InternalDisconnectReply.getDefaultInstance());

            try {
                ProtoZmq.send(controlSocket, disconnectRequest);
                ProtoZmq.receive(controlSocket, connectReply);
                if (connectReply.getType() != expectedReplyType) {
                    LOG.error("Invalid internal disconnect reply.");
                }
            } catch (NetworkingException e) {
                LOG.error(e.getMessage());
            } catch (InvalidProtocolBufferException e) {
                LOG.error(e.getMessage());
            }
        }

        if (!this.closed) {
            this.closed = true;

            handlerThread.join();

//            this.controlSocket.unbind(this.controlSocketAddress);
            this.controlSocket.close();

            this.context.close();
        }
    }

    private void communicationHandler() {
        try {
            //Ping message to send to the server
            Communication.AlivePing ping = Communication.AlivePing.getDefaultInstance();
            //Map between the call ids and the
            HashMap<Long, String> outstandingRequests = new HashMap<Long, String>();
            //The status of the server
            PeerStatus serverStatus = null;
            //Poll timeout
            int pollTimeout = -1;

            //Used for inter-thread communication
            ZMQ.Socket threadControl = this.context.socket(ZMQ.PAIR);
            threadControl.setLinger(this.linger);
            threadControl.connect(this.controlSocketAddress);

            //Socket that communicates with the server
            ZMQ.Socket server = this.context.socket(ZMQ.DEALER);
            server.setIdentity(this.identity.getBytes(Constants.DEFAULT_ENCODING));
            server.setLinger(this.linger);
            server.connect(this.serverEndpoint);

            //Socket for sending messages about requests
            ZMQ.Socket threadBridge = this.context.socket(ZMQ.ROUTER);
            threadBridge.setLinger(this.linger);
            threadBridge.bind(this.bridgeAddress);

            Communication.ExternalConnectReply connectReply = tryConnectToServer(threadControl, server);

            if (connectReply != null) {
                serverStatus = new PeerStatus(connectReply.getRetries(), connectReply.getPeriod());
                pollTimeout = connectReply.getPeriod();
                this.running = true;
            } else {
                this.running = false;
            }

            Communication.InternalConnectReply internalConnectReply = Communication.InternalConnectReply.
                    newBuilder().
                    setSuccess(this.running)
                    .build();
            //Send internal connect reply
            ProtoZmq.send(threadControl, internalConnectReply);

            ZMQ.PollItem[] items = new ZMQ.PollItem[]{
                    new ZMQ.PollItem(threadControl, ZMQ.Poller.POLLIN),
                    new ZMQ.PollItem(server, ZMQ.Poller.POLLIN),
                    new ZMQ.PollItem(threadBridge, ZMQ.Poller.POLLIN)
            };

            ClientPingHandler clientPingHandler = new ClientPingHandler(serverStatus);
            ClientPongHandler clientPongHandler = new ClientPongHandler(serverStatus);
            ServiceResponseHandler serviceResponseHandler = new ServiceResponseHandler(outstandingRequests, this.serviceResponses, threadBridge);

            while (this.running) {
                ZMQ.poll(items, items.length, pollTimeout);

                if (items[0].isReadable()) {
                    try {
                        //The only message that is sent through the control socket at this stage
                        //is a request to shutdown the thread.
                        ContainerMessage message = new ContainerMessage();
                        Communication.InternalDisconnectReply reply = Communication.InternalDisconnectReply.getDefaultInstance();

                        ProtoZmq.receive(threadControl, message);
                        if (ProtoZmq.getType(Communication.InternalDisconnectRequest.getDefaultInstance()).equals(message.getType())) {
                            ProtoZmq.send(threadControl, reply);
                        } else {
                            LOG.error("Invalid message received from control socket.");
                        }

                        this.running = false;
                    } catch (Exception ex) {
                        LOG.error(ex.getMessage());
                    }
                }

                if (items[1].isReadable()) {
                    try {
                        final ContainerMessage message = new ContainerMessage();
                        ProtoZmq.receive(server, message);

                        if (clientPingHandler.canHandle(message)) {
                            clientPingHandler.handle(message, server);
                        } else if (clientPongHandler.canHandle(message)) {
                            clientPongHandler.handle(message, server);
                        } else if (serviceResponseHandler.canHandle(message)) {
                            serviceResponseHandler.handle(message, server);
                        }

                    } catch (UnsupportedMessageType unsupportedMessageType) {
                        unsupportedMessageType.printStackTrace();
                    }
                } else {
                    if (serverStatus.decreaseLiveliness()) {
                        ProtoZmq.send(server, ping);
                    }
                }

                if (items[2].isReadable()) {
                    ContainerMessage message = new ContainerMessage();
                    Internal.RequestAvailable requestAvailable = Internal.RequestAvailable.getDefaultInstance();
                    String peerId = ProtoZmq.receiveFromPeer(threadBridge, message);

                    if (ProtoZmq.getType(requestAvailable).equals(message.getType())) {

                        Service.ServiceRequest request = this.serviceRequests.poll();
                        outstandingRequests.put(request.getId(), peerId);

                        if (request != null) {
                            ProtoZmq.send(server, request);
                        } else {
                            LOG.error("Invalid internal service request.");
                        }
                    }
                }

                if (!serverStatus.isAlive()) {
                    for (Map.Entry<Long, String> request : outstandingRequests.entrySet()) {
                        Service.ServiceResponse failureResponse = Service.ServiceResponse.newBuilder()
                                .setError("The service call failed because the server is not responding.")
                                .setId(request.getKey())
                                .build();
                        this.serviceResponses.put(request.getKey(), failureResponse);

                        Internal.ResponseAvailable responseAvailable = Internal.ResponseAvailable.getDefaultInstance();
                        ProtoZmq.sendToPeer(threadBridge, request.getValue(), responseAvailable);
                    }

                    outstandingRequests.clear();
                }
            }

            threadControl.disconnect(this.controlSocketAddress);
            threadControl.close();

            server.disconnect(this.serverEndpoint);
            server.close();

//            threadBridge.unbind(this.bridgeAddress);
            threadBridge.close();

        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        } catch (NetworkingException e) {
            e.printStackTrace();
        }
    }

    private Communication.ExternalConnectReply tryConnectToServer(ZMQ.Socket threadControl, ZMQ.Socket toServer) {
        Communication.ExternalConnectReply connectReply = null;

        try {
            ContainerMessage controlMessage = new ContainerMessage();
            String internalConnectRequestType = ProtoZmq.getType(Communication.InternalConnectRequest.getDefaultInstance());

            ProtoZmq.receive(threadControl, controlMessage);
            if (internalConnectRequestType.equals(controlMessage.getType())) {
                Communication.ExternalConnectRequest connectionReq = Communication.ExternalConnectRequest
                        .newBuilder()
                        .setPeriod(this.config.getAliveTimeout())
                        .setRetries(this.config.getAliveRetryNo())
                        .build();

                ProtoZmq.send(toServer, connectionReq);

                ZMQ.PollItem[] items = new ZMQ.PollItem[]{
                        new ZMQ.PollItem(toServer, ZMQ.Poller.POLLIN),
                };

                ZMQ.poll(items, items.length, this.config.getConnectionTimeout());

                if (items[0].isReadable()) {
                    Communication.ExternalConnectReply reply = Communication.ExternalConnectReply.getDefaultInstance();
                    ContainerMessage message = new ContainerMessage();

                    ProtoZmq.receive(toServer, message);
                    if (ProtoZmq.getType(reply).equals(message.getType())) {
                        connectReply = Communication.ExternalConnectReply.parseFrom(message.getData());
                    }
                }

            } else {
                LOG.error("Invalid internal connect request:" + controlMessage.getData());
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        } catch (NetworkingException e) {
            e.printStackTrace();
        }

        return connectReply;
    }


    private void connectToServer() throws ConnectionTimeoutException {
        Communication.InternalConnectRequest connectRequest = Communication.InternalConnectRequest.getDefaultInstance();
        Communication.InternalConnectReply expectedConnectReply = Communication.InternalConnectReply.getDefaultInstance();
        ContainerMessage connectReply = new ContainerMessage();

        try {
            ProtoZmq.send(this.controlSocket, connectRequest);
            ProtoZmq.receive(this.controlSocket, connectReply);

            if (ProtoZmq.getType(expectedConnectReply).equals(connectReply.getType())) {
                expectedConnectReply = Communication.InternalConnectReply.parseFrom(connectReply.getData());
                if (!expectedConnectReply.getSuccess()) {
                    throw new ConnectionTimeoutException("Connection to server time out.");
                }
            }
        } catch (NetworkingException e) {
            throw new ConnectionTimeoutException(e.getMessage());
        } catch (InvalidProtocolBufferException e) {
            throw new ConnectionTimeoutException(e.getMessage());
        }
    }

}
