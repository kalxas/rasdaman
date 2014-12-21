package org.rasdaman.rasnet.channel;

import com.google.protobuf.*;
import org.rasdaman.rasnet.exception.RasnetException;
import org.rasdaman.rasnet.util.ProtoZMQ;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;
import org.rasdaman.rasnet.message.Communication.*;
import org.rasdaman.rasnet.message.Base.*;
import org.rasdaman.rasnet.message.Service.*;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Channel implements RpcChannel, Closeable {

    public static final String ENCODING = "UTF-8";

    private Context context;
    private Socket controlSocket;

    private String controlPipeAddr;
    private String messagePipeAddr;

    private String serverAddr;
    private String socketIdentity;

    private long counter;

    private long linger;

    private ChannelConfig channelConfig;
    private ChannelListener channelListener;

    private AlivePing ping;
    private AlivePong pong;

    public Channel(String host, int port) {
        //TODO-GM
        channelConfig = new ChannelConfig();
        channelConfig.setAliveRetryNo(3);
        channelConfig.setAliveTimeout(1000);
        channelConfig.setNumberOfIoThreads(1);
        channelConfig.setConnectionTimeout(1000);

        this.context = ZMQ.context(1);
        this.init(host, port);
    }

    public Channel(String host, int port, ChannelConfig channelConfig) {
        this.channelConfig = channelConfig;
        this.context = ZMQ.context(channelConfig.getNumberOfIoThreads());

        this.init(host, port);
    }

    @Override
    public void callMethod(Descriptors.MethodDescriptor methodDescriptor,
                           RpcController rpcController,
                           Message request,
                           Message response,
                           RpcCallback<Message> rpcCallback) {

        if (methodDescriptor == null || rpcController == null || request == null ||
                response == null || rpcCallback == null) {
            throw new RasnetException("Invalid input parameters.");
        }

        try {
            //1. Increase the counter value
            long counterValue;
            synchronized (this) {
                counterValue = this.counter;
                this.counter++;
            }

            //2. Prepare the request
            ServiceRequest serviceRequest = ServiceRequest.newBuilder()
                    .setInputValue(request.toByteString())
                    .setId(counterValue)
                    .setMethodName(ProtoZMQ.getMethodName(methodDescriptor))
                    .build();

            // create dealer socket
            Socket channelSocket = this.context.socket(ZMQ.DEALER);

            // set socket identity and connect
            String identity = UUID.randomUUID().toString();
            channelSocket.setIdentity(identity.getBytes());
            channelSocket.setLinger(this.linger);
            channelSocket.connect(this.messagePipeAddr);

            try {
                // send the id of the call
                channelSocket.send(String.valueOf(counterValue), ZMQ.SNDMORE);

                // send the serialized request
                channelSocket.send(serviceRequest.toByteArray(), ProtoZMQ.ZMQ_NO_FLAG);

                try {
                    byte[] data = channelSocket.recv();
                    ServiceResponse serviceResponse = ServiceResponse.parseFrom(data);
                    if (serviceRequest.getId() != serviceResponse.getId()) {
                        throw new RasnetException("Invalid request id.");
                    }

                    if (serviceResponse.hasError()) {
                        rpcController.setFailed(serviceResponse.getError());
                    } else if (serviceResponse.hasOutputValue()) {
                        try {
                            rpcCallback.run(response.toBuilder().mergeFrom(serviceResponse.getOutputValue()).build());
                        } catch (InvalidProtocolBufferException e) {
                            rpcController.setFailed("Service call received unparsable response.");
                        }
                    } else {
                        rpcController.setFailed("Service call failed for an unknown reason.");
                    }
                } catch (InvalidProtocolBufferException e) {
                    throw new RasnetException(e);
                }
            } finally {
                channelSocket.disconnect(this.messagePipeAddr);
                channelSocket.close();
            }
        } catch (Exception ex) {
            rpcController.setFailed(ex.getMessage());
        }
    }

    private void init(String host, int port) {
        this.context.setMaxSockets(50000);
        this.linger = 0;
        this.counter = 1;
        this.serverAddr = String.format("%s:%d", host, port);
        this.socketIdentity = UUID.randomUUID().toString();
        this.controlPipeAddr = String.format("inproc://control-%s}", this.socketIdentity);
        this.messagePipeAddr = String.format("inproc://message-%s}", this.socketIdentity);

        this.controlSocket = this.context.socket(ZMQ.PAIR);
        this.controlSocket.setLinger(linger);
        this.controlSocket.bind(this.controlPipeAddr);

        this.ping = AlivePing.newBuilder().build();
        this.pong = AlivePong.newBuilder().build();

        this.channelListener = new ChannelListener();
        this.channelListener.start();

        this.connectToServer();
    }

    private void connectToServer() {
        InternalConnectRequest request = InternalConnectRequest.newBuilder().build();
        if (!ProtoZMQ.zmqSend(this.controlSocket, request)) {
            throw new RasnetException("Connection to server timed out.");
        }
        try {
            BaseMessage baseReply = ProtoZMQ.zmqRecv(this.controlSocket);

            if (baseReply.getType().equals(InternalConnectReply.getDefaultInstance().getDescriptorForType().getFullName())) {
                InternalConnectReply reply = InternalConnectReply.parseFrom(baseReply.getData());
                if (!reply.getSuccess()) {
                    throw new RasnetException("Connection to server timed out.");
                }
            } else {
                throw new RasnetException("Connection to server timed out.");
            }
        } catch (InvalidProtocolBufferException e) {
            throw new RasnetException("Connection to server timed out.");
        }
    }

    private void disconnectFromServer() {
        InternalDisconnectRequest internalDisconnectRequest = InternalDisconnectRequest.getDefaultInstance();
        ProtoZMQ.zmqSend(this.controlSocket, internalDisconnectRequest);
        try {
            BaseMessage baseReply = ProtoZMQ.zmqRecv(this.controlSocket);
            if (baseReply.getType() != InternalDisconnectReply.getDefaultInstance().getDescriptorForType().getFullName()) {
                //TODO-GM: logging
            }
        } catch (InvalidProtocolBufferException e) {
            //TODO-GM: logging
        }
    }

    @Override
    public void close() throws IOException {
        this.disconnectFromServer();
        this.controlSocket.unbind(this.controlPipeAddr);
        this.controlSocket.close();
        try {
            this.channelListener.stopListening();
            this.channelListener.join();
        } catch (InterruptedException e) {
            //TODO-GM: logging
        }
        this.context.close();
    }

    private class ChannelListener extends Thread {

        private boolean keepRunning;

        @Override
        public void run() {
            keepRunning = false;
            listen();
        }

        private void listen() {
            /**
             * 1. Wait for connect request from the constructor.
             * 2. Connect to the server by sending a ConnectRequest
             * 3. Forward the ConnectReply to the controlSocket
             * 4. Find a way to gracefully exit if the server has died.
             */
            //TODO-AT: What should we do if any of the connect calls fail?

            //Map between call ids and socket identities
            PeerStatus serverStatus = null;
            Map<Long, String> requests = new HashMap<Long, String>();
            String serviceRequestType = ServiceRequest.getDefaultInstance().getDescriptorForType().getFullName();

            BaseMessage controlMessage;
            BaseMessage frontMessage;

            // The number of milliseconds to poll before giving up
            // -1 means that the poll will block until there is something on
            // the polled sockets
            int timeout = -1;
            this.keepRunning = false;

            //Socket through which we pass control messages from the client to this thread
            Socket internalChannelSocket = context.socket(ZMQ.ROUTER);
            internalChannelSocket.setLinger(linger);
            internalChannelSocket.bind(messagePipeAddr);

            //Socket through which we pass control messages from the client to this thread
            Socket control = context.socket(ZMQ.PAIR);
            control.setLinger(linger);
            control.connect(controlPipeAddr);

            //Socket connected to the server
            Socket frontend = context.socket(ZMQ.DEALER);
            //Set the identity for the DEALER socket
            frontend.setIdentity(socketIdentity.getBytes());
            frontend.setLinger(linger);
            frontend.connect(serverAddr);
            try {
                //Connect to the server
                try {
                    controlMessage = ProtoZMQ.zmqRecv(control);
                    if (controlMessage.getType().equals(InternalConnectRequest.getDefaultInstance().getDescriptorForType().getFullName())) {
                        ExternalConnectRequest connectRequest = ExternalConnectRequest.newBuilder()
                                .setPeriod(channelConfig.getAliveTimeout())
                                .setRetries(channelConfig.getAliveRetryNo())
                                .build();

                        ProtoZMQ.zmqSend(frontend, connectRequest);
                        ZMQ.PollItem[] items = new ZMQ.PollItem[]{new ZMQ.PollItem(frontend, ZMQ.Poller.POLLIN)};
                        //Wait for a predefined number of milliseconds before giving up and closing this
                        ZMQ.poll(items, items.length, channelConfig.getConnectionTimeout());

                        if (items[0].isReadable()) {
                            BaseMessage mess = ProtoZMQ.zmqRecv(frontend);
                            if (mess.getType().equals(ExternalConnectReply.getDescriptor().getFullName())) {
                                ExternalConnectReply connectReply = ExternalConnectReply.parseFrom(mess.getData());
                                serverStatus = new PeerStatus(connectReply.getRetries(), connectReply.getPeriod());
                                timeout = connectReply.getPeriod();
                                this.keepRunning = true;
                            }
                        }

                    }
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }

                InternalConnectReply internalConnectReply = InternalConnectReply.newBuilder()
                        .setSuccess(keepRunning)
                        .build();

                //Send the status to the controlling thread.
                ProtoZMQ.zmqSend(control, internalConnectReply);

                ZMQ.PollItem[] items = new ZMQ.PollItem[]{
                        new ZMQ.PollItem(control, ZMQ.Poller.POLLIN),
                        new ZMQ.PollItem(frontend, ZMQ.Poller.POLLIN),
                        new ZMQ.PollItem(internalChannelSocket, ZMQ.Poller.POLLIN)
                };

                // if the connection to server fails, then the loop is not started
                while (keepRunning) {
                    ZMQ.poll(items, items.length, timeout);
                    if (items[0].isReadable()) {
                        try {
                            controlMessage = ProtoZMQ.zmqRecv(control);
                            if (controlMessage.getType().equals(InternalDisconnectRequest.getDefaultInstance().getDescriptorForType().getFullName())) {
                                keepRunning = false;
                                InternalDisconnectReply disconnectReply = InternalDisconnectReply.newBuilder().build();
                                ProtoZMQ.zmqSend(control, disconnectReply);
                            }
                        } catch (InvalidProtocolBufferException e) {
                            e.printStackTrace();
                        }
                    }

                    if (items[1].isReadable()) {
                        //If we receive a message from the server, reset its status
                        serverStatus.reset();

                        try {
                            frontMessage = ProtoZMQ.zmqRecv(frontend);
                            String messageType = frontMessage.getType();

                            if (messageType.equals(ping.getDescriptorForType().getFullName())) {
                                ProtoZMQ.zmqSend(frontend, pong);
                            } else if (messageType.equals(ServiceResponse.getDescriptor().getFullName())) {
                                ServiceResponse serviceResponse = ServiceResponse.parseFrom(frontMessage.getData());
                                String socketIdentity = requests.get(serviceResponse.getId());
                                if (socketIdentity != null) {
                                    internalChannelSocket.sendMore(socketIdentity);
                                    internalChannelSocket.send(serviceResponse.toByteArray(), 0);
                                    requests.remove(serviceResponse.getId());
                                }
                            } else if (messageType.equals(AlivePong.getDescriptor().getFullName())) {
                                // Ignore it
                            }
                        } catch (InvalidProtocolBufferException e) {
                            //TODO-GM: what should I do if a message is invalid?
                        }
                    } else {
                        //Decrease liveliness and send out a ping to see if the server is alive
                        if (serverStatus.decreaseLiveliness()) {
                            ProtoZMQ.zmqSend(frontend, ping);
                        }
                    }

                    if (items[2].isReadable()) {
                        //We will only receive service request and we need to forward
                        //them to the appropriate server and keep track of
                        //Receive an integer representing the call id

                        String identity = internalChannelSocket.recvStr(Charset.forName(Channel.ENCODING));
                        if (internalChannelSocket.hasReceiveMore()) {

                            //TODO-GM: Should I handle the case when the received string is not a valid long?
                            long callId = Long.parseLong(internalChannelSocket.recvStr(Charset.forName(Channel.ENCODING)));

                            if (internalChannelSocket.hasReceiveMore()) {
                                byte[] content = internalChannelSocket.recv();
                                if (requests.put(callId, identity) == null) {
                                    BaseMessage internalReq = BaseMessage.newBuilder()
                                            .setData(ByteString.copyFrom(content))
                                            .setType(serviceRequestType)
                                            .build();

                                    ProtoZMQ.zmqRawSend(frontend, internalReq);
                                }
                            }
                        }
                    }

                    if (!serverStatus.isAlive()) {
                        for (Map.Entry<Long, String> entry : requests.entrySet()) {
                            ServiceResponse failureResponse = ServiceResponse.newBuilder()
                                    .setId(entry.getKey())
                                    .setError("The service call failed")
                                    .build();
                            ProtoZMQ.zmqSend(internalChannelSocket, failureResponse);
                        }
                        requests.clear();
                    }
                }
            } finally {
                //TODO-GM: disconnect the other two sockets?
                internalChannelSocket.unbind(messagePipeAddr);
                internalChannelSocket.close();

                control.disconnect(controlPipeAddr);
                control.close();

                frontend.disconnect(serverAddr);
                frontend.close();
            }


        }

        protected void stopListening() {
            this.keepRunning = false;
        }
    }
}
