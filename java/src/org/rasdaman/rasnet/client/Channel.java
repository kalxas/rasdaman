package org.rasdaman.rasnet.client;

import com.google.protobuf.*;
import org.rasdaman.rasnet.common.Constants;
import org.rasdaman.rasnet.exception.ConnectionTimeoutException;
import org.rasdaman.rasnet.exception.UnsupportedMessageType;
import org.rasdaman.rasnet.util.MessageContainer;
import org.rasdaman.rasnet.util.PeerMessage;
import org.rasdaman.rasnet.util.PeerStatus;
import org.rasdaman.rasnet.util.ZmqUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.rasdaman.rasnet.message.Communication.*;
import org.rasdaman.rasnet.message.Communication.MessageType.*;
import org.rasdaman.rasnet.message.internal.Internal.*;

public class Channel implements BlockingRpcChannel, AutoCloseable {
    private static Logger LOG = LoggerFactory.getLogger(Channel.class);

    volatile boolean closed;
    volatile boolean running;

    private int linger;
    private String serverAddress;
    private ChannelConfig config;
    private String identity;
    private final String controlAddress;
    private final String bridgeAddress;

    private ConcurrentHashMap<String, Map.Entry<String, Message>> serviceRequests;
    private ConcurrentHashMap<String, Map.Entry<Boolean, byte[]>> serviceResponses;

    private ZMQ.Context context;
    private ZMQ.Socket controlSocket;

    private Thread handlerThread;

    public Channel(String serverAddress, ChannelConfig config) throws ConnectionTimeoutException {
        this.closed = false;
        this.running = false;
        this.linger = 0;
        this.serverAddress = serverAddress;
        this.config = config;


        this.serviceRequests = new ConcurrentHashMap<String, Map.Entry<String, Message>>();
        this.serviceResponses = new ConcurrentHashMap<String, Map.Entry<Boolean, byte[]>>();

        this.identity = UUID.randomUUID().toString();
        this.controlAddress = ZmqUtil.toInprocAddress(String.format("control-%s}", this.identity));
        this.bridgeAddress = ZmqUtil.toInprocAddress(String.format("bridge-%s}", this.identity));

        this.context = ZMQ.context(this.config.getIoThreadsNo());
        this.context.setMaxSockets(this.config.getMaxOpenSockets());

        this.controlSocket = this.context.socket(ZMQ.PAIR);
        this.controlSocket.setLinger(this.linger);
        this.controlSocket.bind(this.controlAddress);

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
    public void close() throws Exception {
        if (this.running) {
            InternalDisconnectRequest disconnectRequest = InternalDisconnectRequest.getDefaultInstance();
            MessageContainer replyEnvelope = new MessageContainer();
            String expectedReplyType = ZmqUtil.getType(InternalDisconnectReply.getDefaultInstance());

            if (!ZmqUtil.isSocketWritable(controlSocket, 0)) {
                LOG.error("Unable to write internal disconnect request to control socket");
            } else if (!ZmqUtil.send(controlSocket, disconnectRequest)) {
                LOG.error("Unable to send internal disconnect request to control socket");
            } else {
                if (!ZmqUtil.isSocketReadable(controlSocket, this.config.getChannelTimeout())
                        || !ZmqUtil.receive(controlSocket, replyEnvelope)) {
                    LOG.error("Failed to receive internal disconnect reply.");
                } else if (!replyEnvelope.getType().equals(expectedReplyType)) {
                    LOG.error("Received invalid internal disconnect reply.");
                }
            }

            //Set running to false independent of the result of the above call.
            //This means that the worker thread will eventually shutdown.
            this.running = false;
        }

        if (!this.closed) {
            this.closed = true;
            handlerThread.join();

            /** TODO-GM There is a bug in libzmq whe unbind is used on inporc address. To be uncommented
             * when libzmq is updated to 4.1+. libzmq bug: https://github.com/zeromq/libzmq/issues/949
             */
//            this.controlSocket.unbind(this.controlSocketAddress);
            this.controlSocket.close();

            this.context.close();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();

        if (!this.closed) {
            LOG.error("Programming error. The Channel must be closed before being garbage collected.");
            this.close();
        }
    }

    @Override
    public Message callBlockingMethod(Descriptors.MethodDescriptor method, RpcController controller, Message request, Message responsePrototype) throws ServiceException {
        Message result = null;
        if (this.closed || !this.running) {
            controller.setFailed("Channel is closed. No further calls are possible.");
            return null;
        }

        if (method == null || controller == null || request == null
                || responsePrototype == null) {
            throw new IllegalArgumentException("The input parameters must be non-null.");
        }

        //1. Create DEALER socket for inter-thread communication
        String callId = UUID.randomUUID().toString();
        ZMQ.Socket bridge = this.context.socket(ZMQ.DEALER);
        bridge.setIdentity(callId.getBytes(Constants.DEFAULT_ENCODING));
        bridge.setLinger(linger);
        bridge.connect(bridgeAddress);

        //2. Add the request to the list of pending requests
        this.serviceRequests.put(callId, new AbstractMap.SimpleEntry<String, Message>(ZmqUtil.getMethodName(method), request));

        ServiceRequestAvailable requestAvailable = ServiceRequestAvailable.getDefaultInstance();
        ServiceResponseAvailable responseAvailable = ServiceResponseAvailable.getDefaultInstance();
        MessageContainer internalResponseEnvelope = new MessageContainer();

        //3. Send request and wait for response
        if (!ZmqUtil.send(bridge, requestAvailable) || !ZmqUtil.receive(bridge, internalResponseEnvelope)) {
            controller.setFailed("Internal communication failure.");
        } else if (!internalResponseEnvelope.getType().equals(ZmqUtil.getType(responseAvailable))) {
            throw new RuntimeException("Received invalid message from bridge socket.");
        } else {
            Map.Entry<Boolean, byte[]> response = this.serviceResponses.get(callId);
            if (response != null) {
                boolean success = response.getKey();
                if (success) {
                    try {
                        //If the call was successful, parse the response
                        result = responsePrototype.getParserForType().parseFrom(response.getValue());

                    } catch (InvalidProtocolBufferException e) {
                        controller.setFailed("Failed to parse response message.");
                    }

                } else {
                    //If the call was not successful, the bytes represent an error message
                    controller.setFailed(new String(response.getValue()));
                }

                this.serviceResponses.remove(callId);
            } else {
                throw new RuntimeException("The list of service responses is empty");
            }
        }

        //4. Cleanup
        bridge.disconnect(bridgeAddress);
        bridge.close();

        return result;
    }

    private void communicationHandler() {
        //The status of the server
        PeerStatus serverStatus = null;
        //Poll timeout
        int pollTimeout = -1;
        try {
            ZMQ.Socket server = this.context.socket(ZMQ.DEALER);
            server.setIdentity(identity.getBytes(Constants.DEFAULT_ENCODING));
            server.setLinger(linger);
            server.connect(serverAddress);

            ZMQ.Socket control = this.context.socket(ZMQ.PAIR);
            control.setLinger(linger);
            control.connect(controlAddress);

            ZMQ.Socket bridge = this.context.socket(ZMQ.ROUTER);
            bridge.setLinger(linger);
            bridge.bind(bridgeAddress);

            ConnectReply connectReply = this.tryConnectToServer(control, server);
            if (connectReply != null) {
                serverStatus = new PeerStatus(connectReply.getRetries(), connectReply.getLifetime());
                pollTimeout = connectReply.getLifetime();
                this.running = true;
            } else {
                this.running = false;
            }

            InternalConnectReply internalConnectReply = InternalConnectReply.newBuilder().setSuccess(this.running).build();
            ZmqUtil.send(control, internalConnectReply);

            ZMQ.PollItem[] items = new ZMQ.PollItem[]{
                    new ZMQ.PollItem(control, ZMQ.Poller.POLLIN),
                    new ZMQ.PollItem(server, ZMQ.Poller.POLLIN),
                    new ZMQ.PollItem(bridge, ZMQ.Poller.POLLIN)
            };

            ClientPingHandler clientPingHandler = new ClientPingHandler(server, serverStatus);
            ClientPongHandler clientPongHandler = new ClientPongHandler(serverStatus);
            ServiceResponseHandler serviceResponseHandler = new ServiceResponseHandler(bridge, serverStatus, serviceRequests, serviceResponses);

            while (this.running) {
                ZMQ.poll(items, items.length, pollTimeout);

                if (items[0].isReadable()) {
                    //The only message that is sent through the control socket at this stage
                    //is a request to shutdown the thread.
                    MessageContainer message = new MessageContainer();
                    InternalDisconnectReply reply = InternalDisconnectReply.getDefaultInstance();

                    if (!ZmqUtil.receive(control, message)) {
                        LOG.error("Failed to receive message from control socket.");
                    } else if (!ZmqUtil.getType(InternalDisconnectRequest.getDefaultInstance()).equals(message.getType())) {
                        LOG.error("Invalid message received from control socket.");
                    } else {
                        ZmqUtil.send(control, reply);
                    }

                    this.running = false;
                }

                if (items[1].isReadable()) {
                    ArrayList<byte[]> message = new ArrayList<byte[]>();
                    ZmqUtil.receiveCompositeMessage(server, message);

                    try {
                        if (serviceResponseHandler.canHandle(message)) {
                            serviceResponseHandler.handle(message);
                        } else if (clientPingHandler.canHandle(message)) {
                            clientPingHandler.handle(message);
                        } else if (clientPongHandler.canHandle(message)) {
                            clientPongHandler.handle(message);
                        } else {
                            LOG.error("Received unknown message type from server.");
                        }

                    } catch (UnsupportedMessageType unsupportedMessageType) {
                        unsupportedMessageType.printStackTrace();
                    }
                } else {
                    if (serverStatus.decreaseLiveliness()) {
                        if (!ZmqUtil.sendCompositeMessage(server, Types.ALIVE_PING)) {
                            LOG.error("Failed to send ping message to server.");
                        }
                    }
                }

                if (items[2].isReadable()) {
                    PeerMessage message = new PeerMessage();
                    ServiceRequestAvailable requestAvailable = ServiceRequestAvailable.getDefaultInstance();

                    if (!ZmqUtil.receiveFromPeer(bridge, message)) {
                        LOG.error("Failed to receive message from bridge.");
                    } else if (!ZmqUtil.getType(requestAvailable).equals(message.getMessage().getType())) {
                        LOG.error("Type of message received from bridge has invalid type.");
                    } else {
                        Map.Entry<String, Message> request = this.serviceRequests.get(message.getPeerId());
                        if (request != null) {
                            ZmqUtil.sendServiceRequest(server, message.getPeerId(), request.getKey(), request.getValue());
                        } else {
                            LOG.error("There is no service request in the map from the peer:" + message.getPeerId());
                        }
                    }
                }

                if (!serverStatus.isAlive()) {
                    byte[] errorMessage = (new String("Connection to server timeout.")).getBytes(Constants.DEFAULT_ENCODING);
                    AbstractMap.SimpleEntry<Boolean, byte[]> responseEntry = new AbstractMap.SimpleEntry<>(false, errorMessage);
                    ServiceResponseAvailable responseAvailable = ServiceResponseAvailable.getDefaultInstance();

                    for (Map.Entry<String, Map.Entry<String, Message>> request : serviceRequests.entrySet()) {
                        String callId = request.getKey();
                        this.serviceResponses.put(callId, responseEntry);
                        ZmqUtil.sendToPeer(bridge, callId, responseAvailable);
                    }

                    this.serviceRequests.clear();
                }
            }

            //bridge.unbind(bridgeAddress)
            bridge.close();

            control.disconnect(controlAddress);
            control.close();

            server.disconnect(serverAddress);
            server.close();
        } catch (org.zeromq.ZMQException ex) {
            this.running = false;
            LOG.error(ex.getLocalizedMessage());
        }
    }

    private ConnectReply tryConnectToServer(ZMQ.Socket controlSocket, ZMQ.Socket server) {
        ConnectReply connectReply = null;

        try {
            String expectedInternalRequestType = ZmqUtil.getType(InternalConnectRequest.getDefaultInstance());
            MessageContainer controlMessage = new MessageContainer();

            //Wait for the request from the main thread.
            if (ZmqUtil.receive(controlSocket, controlMessage)
                    && controlMessage.getType().equals(expectedInternalRequestType)) {

                //Build the connect request for the server
                ConnectRequest connectRequest = ConnectRequest
                        .newBuilder().setLifetime(this.config.getAliveTimeout())
                        .setRetries(this.config.getAliveRetryNo())
                        .build();

                //Send message connect request to server
                ZmqUtil.sendCompositeMessage(server, Types.CONNECT_REQUEST, connectRequest);

                //Wait for the response for a predefined period
                if (ZmqUtil.isSocketReadable(server, this.config.getChannelTimeout())) {

                    ArrayList<byte[]> messageFromServer = new ArrayList<byte[]>();
                    ZmqUtil.receiveCompositeMessage(server, messageFromServer);

                    if (messageFromServer.size() == 2) {

                        MessageType responseFromServerType = MessageType.parseFrom(messageFromServer.get(0));
                        if (responseFromServerType.getType() == Types.CONNECT_REPLY) {
                            connectReply = ConnectReply.parseFrom(messageFromServer.get(1));
                        } else {
                            LOG.error("The message received from the server has an incorrect type.");
                        }
                    } else {
                        LOG.error("The message received from the server has the wrong number of parts.");
                    }
                } else {
                    LOG.error("Server did not reply within the maximum time window.");
                }
            } else {
                LOG.error("Failed to receive valid message from control socket.");
            }

        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
            LOG.error("Failed to parse message received from the server. Reason:" + e.getLocalizedMessage());
        }

        return connectReply;
    }


    private void connectToServer() throws ConnectionTimeoutException {
        InternalConnectRequest connectRequest = InternalConnectRequest.getDefaultInstance();
        MessageContainer responseEnvelope = new MessageContainer();
        String expectedReplyType = ZmqUtil.getType(InternalConnectReply.getDefaultInstance());

        if (!ZmqUtil.isSocketWritable(controlSocket, this.config.getChannelTimeout())
                || !ZmqUtil.send(controlSocket, connectRequest)) {
            LOG.error("Could not write InternalConnectRequest to control socket.");
            throw new ConnectionTimeoutException("Failed to send inter-thread connection request.");

        } else if (!ZmqUtil.isSocketReadable(controlSocket, 2 * this.config.getChannelTimeout())
                || !ZmqUtil.receive(controlSocket, responseEnvelope)) {

            LOG.error("Could not receive InternalConnectReply from control socket.");
            throw new ConnectionTimeoutException("Failed to receive inter-thread connection reply.");

        } else if (!responseEnvelope.getType().equals(expectedReplyType)) {

            throw new RuntimeException("Received invalid message from internal connection reply");

        } else {
            try {
                InternalConnectReply reply = InternalConnectReply.parseFrom(responseEnvelope.getData());
                if (!reply.getSuccess()) {
                    throw new ConnectionTimeoutException("Unable to establish connection to server.");
                }
            } catch (InvalidProtocolBufferException e) {
                LOG.error(e.getLocalizedMessage());
                throw new RuntimeException("Failed to parse internal connect reply. Reason:" + e.getLocalizedMessage());
            }
        }
    }
}
