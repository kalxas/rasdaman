package org.rasdaman.rasnet.server;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Service;
import org.rasdaman.rasnet.exception.AlreadyRunningException;
import org.rasdaman.rasnet.exception.DuplicateService;
import org.rasdaman.rasnet.exception.NetworkingException;
import org.rasdaman.rasnet.exception.UnsupportedMessageType;
import org.rasdaman.rasnet.message.Communication;
import org.rasdaman.rasnet.message.internal.Internal;
import org.rasdaman.rasnet.util.MessageContainer;
import org.rasdaman.rasnet.util.PeerMessage;
import org.rasdaman.rasnet.util.ZmqUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ServiceManager implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(ServiceManager.class);

    private volatile boolean running;
    private volatile boolean closed;

    //Number of milliseconds a socket should wait for
    //queued messages to be delivered
    private final long linger;

    private final ServiceManagerConfig config;

    //The zeromq context that contains all the sockets
    private final ZMQ.Context context;

    //Address of the bridge socket
    private final String bridgeAddress;
    //Address of the control socket
    private final String controlSocketAddress;
    private String bindAddress;

    //Socket through which to send messages from the main thread
    //to the worker thread
    private final ZMQ.Socket controlSocket;

    //object used to handle requests
    private final ServiceRequestHandler serviceRequestHandler;

    private final ExecutorService executorService;

    public ServiceManager(ServiceManagerConfig config) {
        if(config==null){
            throw new IllegalArgumentException("config");
        }
        this.running = false;
        this.closed = false;
        this.linger = 0;
        this.config = config;
        this.executorService = Executors.newSingleThreadExecutor();

        this.context = ZMQ.context(config.getIoThreadNo());
        this.context.setMaxSockets(this.config.getMaxOpenSockets());

        String identity = UUID.randomUUID().toString();
        this.controlSocketAddress = ZmqUtil.toInprocAddress(String.format("control-%s}", identity));
        this.bridgeAddress = ZmqUtil.toInprocAddress(String.format("bridge-%s}", identity));
        this.bindAddress = null;

        this.controlSocket = this.context.socket(ZMQ.PAIR);
        this.controlSocket.setLinger(this.linger);
        this.controlSocket.bind(this.controlSocketAddress);

        this.serviceRequestHandler = new ServiceRequestHandler(this.context, this.bridgeAddress);
    }


    public void addService(Service service) throws DuplicateService {
        this.serviceRequestHandler.addService(service);
    }

    //TODO:In the future, make this fail if the address is incorrect
    public void serve(String address) throws AlreadyRunningException {
        if (this.running) {
            throw new AlreadyRunningException();
        } else {
            this.running = true;
            this.bindAddress = address;
            this.executorService.submit(new Runnable() {
                @Override
                public void run() {
                    workerThread();
                }
            });
        }
    }

    public void close() throws InterruptedException {
        if (this.running) {
            Internal.InternalDisconnectRequest disconnectRequest = Internal.InternalDisconnectRequest.getDefaultInstance();
            MessageContainer replyEnvelope = new MessageContainer();
            String expectedReplyType = ZmqUtil.getType(Internal.InternalDisconnectReply.getDefaultInstance());

            if (!ZmqUtil.isSocketWritable(controlSocket, 1000)) {
                LOG.error("Unable to write internal disconnect request to control socket");
            } else if (!ZmqUtil.send(controlSocket, disconnectRequest)) {
                LOG.error("Unable to send internal disconnect request to control socket");
            } else {
                //TODO:refactor
                //We wait 1000 milliseconds for the reply message so that
                //we don't block on receive
                if (!ZmqUtil.isSocketReadable(controlSocket, 1000)
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

            executorService.shutdown();
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

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

        if (!this.closed || this.running) {
            LOG.error("The " + this.getClass().getName() + " was not closed.");
            this.close();
        }
    }

    public void workerThread() {
        ZMQ.Socket client = null;
        ZMQ.Socket bridge = null;
        ZMQ.Socket control = null;
        try {
            this.running = true;

            client = this.context.socket(ZMQ.ROUTER);
            client.setLinger(this.linger);
            client.bind(this.bindAddress);

            bridge = this.context.socket(ZMQ.ROUTER);
            bridge.setLinger(this.linger);
            bridge.bind(this.bridgeAddress);

            control = this.context.socket(ZMQ.PAIR);
            control.setLinger(this.linger);
            control.connect(this.controlSocketAddress);

            ClientPool clientPool = new ClientPool();
            ConnectRequestHandler connectRequestHandler = new ConnectRequestHandler(client, clientPool, this.config.getAliveRetryNo(), this.config.getAliveTimeout());
            ServerPingHandler serverPingHandler = new ServerPingHandler(client, clientPool);
            ServerPongHandler serverPongHandler = new ServerPongHandler(clientPool);
            ExecutorService threadPool = Executors.newFixedThreadPool(config.getCpuThreadNo());

            ZMQ.PollItem[] items = new ZMQ.PollItem[]{
                    new ZMQ.PollItem(client, ZMQ.Poller.POLLIN),
                    new ZMQ.PollItem(bridge, ZMQ.Poller.POLLIN),
                    new ZMQ.PollItem(control, ZMQ.Poller.POLLIN)
            };

            while (this.running) {
                ZMQ.poll(items, items.length, clientPool.getMinimumPollPeriod());

                if (items[0].isReadable()) {
                    final ArrayList<byte[]> message = new ArrayList<>();
                    final String peerId = ZmqUtil.receiveCompositeMessageFromPeer(client, message);

                    try {
                        if (serverPingHandler.canHandle(message)) {
                            serverPingHandler.handle(message, peerId);
                        } else if (serverPongHandler.canHandle(message)) {
                            serverPongHandler.handle(message, peerId);
                        } else if (connectRequestHandler.canHandle(message)) {
                            connectRequestHandler.handle(message, peerId);
                        } else if (serviceRequestHandler.canHandle(message)) {
                            threadPool.submit(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        serviceRequestHandler.handle(message, peerId);
                                    } catch (UnsupportedMessageType unsupportedMessageType) {
                                        unsupportedMessageType.printStackTrace();
                                    }
                                }
                            });
                        } else {
                            LOG.error("Received invalid message from client with peer ID:" + peerId);
                        }
                    } catch (InvalidProtocolBufferException e) {
                        e.printStackTrace();
                    } catch (UnsupportedMessageType unsupportedMessageType) {
                        unsupportedMessageType.printStackTrace();
                    }
                } else {
                    clientPool.pingAllClients(client);
                    clientPool.removeDeadClients();
                }

                if (items[1].isReadable()) {
                    PeerMessage message = new PeerMessage();
                    String expectedMessageType = ZmqUtil.getType(Internal.ServiceResponseAvailable.getDefaultInstance());

                    if (ZmqUtil.receiveFromPeer(bridge, message) &&
                            message.getMessage().getType().equals(expectedMessageType)) {

                        try {

                            Internal.ServiceResponseAvailable responseAvailable = Internal.ServiceResponseAvailable.parseFrom(message.getMessage().getData());
                            ServiceResponse response = serviceRequestHandler.getTaskResult();
                            ZmqUtil.sendServiceResponse(client, message.getPeerId(), response);

                        } catch (InvalidProtocolBufferException e) {
                            e.printStackTrace();
                            LOG.error("Logic error. Received invalid internal message.");
                        } catch (NetworkingException e) {
                            e.printStackTrace();
                            LOG.error("Failed to send service response to peer." + e.getLocalizedMessage());
                        }

                    } else {
                        LOG.error("Received invalid message from bridge socket.");
                    }
                }

                if (items[2].isReadable()) {
                    //The only message that is sent through the control socket at this stage
                    //is a request to shutdown the thread.
                    MessageContainer message = new MessageContainer();
                    Internal.InternalDisconnectReply reply = Internal.InternalDisconnectReply.getDefaultInstance();

                    if (!ZmqUtil.receive(control, message)) {
                        LOG.error("Failed to receive message from control socket.");
                    } else if (!ZmqUtil.getType(Internal.InternalDisconnectRequest.getDefaultInstance()).equals(message.getType())) {
                        LOG.error("Invalid message received from control socket.");
                    } else {
                        ZmqUtil.send(control, reply);
                    }

                    this.running = false;
                }
            }

        } catch (org.zeromq.ZMQException ex) {
            this.running = false;
            LOG.error(ex.getLocalizedMessage());
        }finally {
            if(control!=null){
                try{
                    //Close the sockets
                    control.disconnect(this.controlSocketAddress);
                }catch (org.zeromq.ZMQException ex) {
                    LOG.error(ex.getLocalizedMessage());
                }finally {
                    control.close();
                }
            }

            if(bridge!=null){
                try{
                    /** TODO-GM There is a bug in libzmq whe unbind is used on inporc address. To be uncommented
                     * when libzmq is updated to 4.1+.
                     libzmq bug: https://github.com/zeromq/libzmq/issues/949
                     */
                    //bridge.unbind(this.bridgeAddress);
                }catch (org.zeromq.ZMQException ex) {
                    LOG.error(ex.getLocalizedMessage());
                }finally {
                    bridge.close();
                }
            }

            if(client!=null){
                try{
                    client.unbind(this.bindAddress);
                }catch (org.zeromq.ZMQException ex) {
                    LOG.error(ex.getLocalizedMessage());
                }finally {
                    client.close();
                }
            }
        }
    }
}
