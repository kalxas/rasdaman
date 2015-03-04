package org.rasdaman.rasnet.server;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Service;
import org.rasdaman.rasnet.common.Constants;
import org.rasdaman.rasnet.exception.AlreadyRunningException;
import org.rasdaman.rasnet.exception.DuplicateService;
import org.rasdaman.rasnet.exception.NetworkingException;
import org.rasdaman.rasnet.message.Communication;
import org.rasdaman.rasnet.message.Internal;
import org.rasdaman.rasnet.message.Service.ServiceResponse;
import org.rasdaman.rasnet.util.ContainerMessage;
import org.rasdaman.rasnet.util.ProtoZmq;
import org.rasdaman.rasnet.util.ZmqUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ServiceManagerCommunication implements Runnable, AutoCloseable {
    private static final  Logger LOG = LoggerFactory.getLogger(ServiceManagerCommunication.class);

    private final ZMQ.Context context;
    private final ZMQ.Socket controlSocket;
    private final long linger;
    private final ServiceRequestHandler serviceRequestHandler;
    private final ServiceManagerConfig config;
    private final String controlSocketAddress;
    private final String bridgeAddress;
    private final String identity;
    private final ExecutorService executorService;

    private String endpoint;

    private volatile boolean running;
    private volatile boolean closed;

    public ServiceManagerCommunication(ServiceManagerConfig config) {
        this.config = config;
        this.linger = 0;
        this.running = false;
        this.closed = false;
        this.identity = UUID.randomUUID().toString();
        this.endpoint = null;
        this.executorService = Executors.newFixedThreadPool(config.getCpuThreadNo());

        this.context = ZMQ.context(config.getIoThreadNo());
        this.context.setMaxSockets(this.config.getMaxOpenSockets());

        this.controlSocketAddress = ZmqUtil.toInprocAddress(String.format("control-%s}", this.identity));
        this.bridgeAddress = ZmqUtil.toInprocAddress(String.format("bridge-%s}", this.identity));

        this.controlSocket = this.context.socket(ZMQ.PAIR);
        this.controlSocket.setLinger(this.linger);
        this.controlSocket.bind(this.controlSocketAddress);

        this.serviceRequestHandler = new ServiceRequestHandler(this.context, this.bridgeAddress);
    }

    void addService(Service service) throws DuplicateService {
        this.serviceRequestHandler.addService(service);
    }

    void setHost(String endpoint) throws AlreadyRunningException {
        if (!this.running) {
            this.endpoint = endpoint;
        } else {
            throw new AlreadyRunningException("The ServiceManagerCommunication instance is already running.");
        }
    }

    public void close() throws InterruptedException {
        if (this.running) {
            ContainerMessage reply = new ContainerMessage();
            String expectedReplyType = ProtoZmq.getType(Communication.InternalDisconnectReply.getDefaultInstance());
            Communication.InternalDisconnectRequest disconnectRequest = Communication.InternalDisconnectRequest.getDefaultInstance();

            try {
                ProtoZmq.send(controlSocket, disconnectRequest);
                ProtoZmq.receive(controlSocket, reply);

                if (expectedReplyType.equals(reply.getType())) {
                    this.running = false;
                } else {
                    LOG.error("Received invalid reply to internal disconnect request.");
                }
            } catch (NetworkingException e) {
                LOG.error(e.getMessage());
            } catch (InvalidProtocolBufferException e) {
                LOG.error(e.getMessage());
            }
        }

        if (!this.closed) {
            this.closed = true;
            executorService.shutdown();
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

//            this.controlSocket.unbind(this.controlSocketAddress);
            this.controlSocket.close();

            this.context.close();
        }
    }

    @Override
    public void run() {
        String responseAvailableType = ProtoZmq.getType(Internal.ResponseAvailable.getDefaultInstance());
        ClientPool clientPool = new ClientPool();
        ConnectRequestHandler connectRequestHandler = new ConnectRequestHandler(clientPool, this.config.getAliveRetryNo(), this.config.getAliveTimeout());
        ServerPingHandler serverPingHandler = new ServerPingHandler(clientPool);
        ServerPongHandler serverPongHandler = new ServerPongHandler(clientPool);

        this.running = true;

        ZMQ.Socket control = this.context.socket(ZMQ.PAIR);
        control.setLinger(this.linger);
        control.connect(this.controlSocketAddress);

        ZMQ.Socket fromClient = this.context.socket(ZMQ.ROUTER);
        fromClient.setLinger(this.linger);
        fromClient.bind(this.endpoint);

        ZMQ.Socket bridge = this.context.socket(ZMQ.ROUTER);
        bridge.setLinger(this.linger);
        bridge.bind(this.bridgeAddress);

        ZMQ.PollItem[] items = new ZMQ.PollItem[]{
                new ZMQ.PollItem(bridge, ZMQ.Poller.POLLIN),
                new ZMQ.PollItem(fromClient, ZMQ.Poller.POLLIN),
                new ZMQ.PollItem(control, ZMQ.Poller.POLLIN)
        };


        while (this.running) {
            ZMQ.poll(items, items.length, clientPool.getMinimumPollPeriod());

            if (items[0].isReadable()) {
                try {
                    //The first string is the identity of the other socket
                    //so we can ignore it
                    bridge.recvStr(Constants.DEFAULT_ENCODING);

                    if (bridge.hasReceiveMore()) {
                        ContainerMessage message = new ContainerMessage();
                        //In this case, the peerId is the id of the client that initially made the request
                        String peerId = ProtoZmq.receiveFromPeer(bridge, message);

                        if (responseAvailableType.equals(message.getType())) {

                            //We ignore the retrieved message, but we use the peerId to forward the message;
                            ServiceResponse serviceResponse = this.serviceRequestHandler.getTaskResult();
                            if (serviceResponse != null) {
                                if (clientPool.isClientAlive(peerId)) {
                                    //Forward the message to the peer
                                    ProtoZmq.sendToPeer(fromClient, peerId, serviceResponse);
                                } else {
                                    LOG.error("Client with ID:" + peerId + " deceased before being sent the response.");
                                }
                            } else {
                                LOG.error("Tried to retrieve inexistent ServiceResponse object.");
                            }
                        }
                    } else {
                        LOG.error("Invalid message from bridge.");
                    }
                } catch (Exception ex) {
                    LOG.error(ex.getMessage());
                }
            }

            if (items[1].isReadable()) {
                try {
                    final ContainerMessage message = new ContainerMessage();
                    final String peerId = ProtoZmq.receiveFromPeer(fromClient, message);

                    if (connectRequestHandler.canHandle(message)) {
                        connectRequestHandler.handle(message, fromClient, peerId);
                    } else if (serverPingHandler.canHandle(message)) {
                        serverPingHandler.handle(message, fromClient, peerId);
                    } else if (serverPongHandler.canHandle(message)) {
                        serverPongHandler.handle(message, fromClient, peerId);
                    } else if (this.serviceRequestHandler.canHandle(message)) {
                        //Reset the status for this client.
                        clientPool.resetClientStatus(peerId);

                        this.executorService.execute(new Runnable() {
                            @Override
                            public void run() {
                                serviceRequestHandler.handle(message, peerId);
                            }
                        });
                    } else {
                        LOG.error("Received invalid message of type:" + message.getType());
                    }

                } catch (Exception ex) {
                    LOG.error(ex.getMessage());
                }
            } else {
                clientPool.removeDeadClients();
                clientPool.pingAllClients(fromClient);
            }

            if (items[2].isReadable()) {
                try {
                    //The only message that is sent through the control socket at this stage
                    //is a request to shutdown the thread.
                    ContainerMessage message = new ContainerMessage();
                    Communication.InternalDisconnectReply reply = Communication.InternalDisconnectReply.getDefaultInstance();

                    ProtoZmq.receive(control, message);
                    if (ProtoZmq.getType(Communication.InternalDisconnectRequest.getDefaultInstance()).equals(message.getType())) {
                        ProtoZmq.send(control, reply);
                    } else {
                        LOG.error("Invalid message received from control socket.");
                    }

                    this.running = false;
                } catch (Exception ex) {
                    LOG.error(ex.getMessage());
                }
            }
        }

        //Close the sockets
        control.disconnect(this.controlSocketAddress);
        control.close();

        fromClient.unbind(this.endpoint);
        fromClient.close();

       /** TODO-GM There is a bug in libzmq whe unbind is used on inporc address. To be uncommented
        * when libzmq is updated to 4.1+.
           libzmq bug: https://github.com/zeromq/libzmq/issues/949
        */
        //bridge.unbind(this.bridgeAddress);
        bridge.close();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();

        if (!this.closed || this.running) {
            LOG.error("The " + this.getClass().getName() + " was not closed.");
            this.close();
        }
    }
}
