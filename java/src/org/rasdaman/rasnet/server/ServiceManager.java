/*
 * This file is part of rasdaman community.
 *
 * Rasdaman community is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Rasdaman community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2014 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */

package org.rasdaman.rasnet.server;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Service;
import org.rasdaman.rasnet.common.Constants;
import org.rasdaman.rasnet.exception.AlreadyRunningException;
import org.rasdaman.rasnet.exception.DuplicateService;
import org.rasdaman.rasnet.exception.NetworkingException;
import org.rasdaman.rasnet.exception.UnsupportedMessageType;
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
    /**
     * This flag is true if the serve method was called successfully,
     * false otherwise
     */
    private volatile boolean serving;

    /**
     * This flag is true if the closed method was called, false otherwise
     */
    private volatile boolean closed;

    /**
     * Number of milliseconds a socket should wait for queued messages to be delivered
     */
    private final long linger;

    private final ServiceManagerConfig config;

    /**
     * ZMQ context that hosts all the sockets used with this object for inter-thread communication
     * and communication with the clients
     */
    private final ZMQ.Context context;

    /**
     * Address of the ZMQ socket used for sending messages from threads handling a request
     * to the worker thread in this object.
     */
    private final String bridgeAddress;

    /**
     * Address of the ZMQ.PAIR socket used for sending messages from the main thread to the worker thread
     */
    private final String controlSocketAddress;

    /**
     * Address to which this ServiceManager will bind and where it will be accessible from the outside.
     */
    private String bindAddress;

    /**
     * Socket through which to send control messages from the main thread to the worker thread
     */
    private final ZMQ.Socket controlSocket;

    /**
     * Object used to handle service requests
     */
    private final ServiceRequestHandler serviceRequestHandler;

    /**
     * Executor service used to contain the worker thread.
     */
    private Thread workerThread;

    /**
     * @param config
     */
    public ServiceManager(ServiceManagerConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("config");
        }

        this.serving = false;
        this.closed = false;
        this.linger = 0;
        this.config = config;
        this.workerThread = null;

        this.context = ZMQ.context(config.getNumberOfIoThreads());
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

    /**
     * Add the service to the list of services provided by the ServiceManager.
     * Adding the service multiple times will throw an exception.
     *
     * @param service Service provided by the ServiceManager
     */
    public void addService(Service service) throws DuplicateService {
        this.serviceRequestHandler.addService(service);
    }

    /**
     * Serve all the services added so far on the given host and port.
     *
     * @param address The host parameters specifies the interface and the protocol on
     *                which to listen for incoming connections
     *                This method will block for INTER_THREAD_COMMUNICATION_TIMEOUT milliseconds
     *                to perform a timed_wait on the worker thread and catch any exception
     *                throw during the binding of the communication sockets
     */
    public void serve(String address) throws AlreadyRunningException, NetworkingException, InterruptedException {
        if (this.serving) {
            throw new AlreadyRunningException();
        } else {
            this.serving = true;
            this.bindAddress = address;

            this.workerThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    communicationThread();
                }
            });

            this.workerThread.start();

            this.workerThread.join(Constants.INTER_THREAD_COMMUNICATION_TIMEOUT);

            if (!this.workerThread.isAlive()) {
                //If the executor terminates, the thread failed to start, so we throw an exception
                throw new NetworkingException("Failed to start ServiceManager on endpoint:" + address
                        + ". Check the logs for more details.");
            }
        }
    }

    /**
     * Close the ServiceManager and release any resources.
     * If this method is not called, there is the risk of resource leaks.
     *
     * @throws InterruptedException
     */
    public void close() throws InterruptedException {
        if (this.serving) {
            Internal.InternalDisconnectRequest disconnectRequest = Internal.InternalDisconnectRequest.getDefaultInstance();
            MessageContainer replyEnvelope = new MessageContainer();
            String expectedReplyType = ZmqUtil.getType(Internal.InternalDisconnectReply.getDefaultInstance());

            if (!ZmqUtil.isSocketWritable(controlSocket, 0)) {
                LOG.error("Unable to write internal disconnect request to control socket");
            } else if (!ZmqUtil.send(controlSocket, disconnectRequest)) {
                LOG.error("Unable to send internal disconnect request to control socket");
            } else {
                //We wait for the reply message so that we don't block on receive
                if (!ZmqUtil.isSocketReadable(controlSocket, Constants.INTER_THREAD_COMMUNICATION_TIMEOUT)
                        || !ZmqUtil.receive(controlSocket, replyEnvelope)) {
                    LOG.error("Failed to receive internal disconnect reply.");
                } else if (!replyEnvelope.getType().equals(expectedReplyType)) {
                    LOG.error("Received invalid internal disconnect reply.");
                }
            }

            //Set serving to false independent of the result of the above call.
            //This means that the worker thread will eventually shutdown.
            this.serving = false;
        }

        if (!this.closed) {
            this.closed = true;

            if (this.workerThread != null) {
                this.workerThread.join();
            }

            /** TODO-GM There is a bug in libzmq whe unbind is used on inporc address. To be uncommented
             * when libzmq is updated to 4.1+. libzmq bug: https://github.com/zeromq/libzmq/issues/949
             */
//            this.controlSocket.unbind(this.controlSocketAddress);
            this.controlSocket.close();

            this.context.close();
        }
    }

    /**
     * This method is called if the ServiceManager was not closed and the object is being garbage collected
     *
     * @throws Throwable
     */
    @Override
    protected void finalize() throws Throwable {
        super.finalize();

        if (!this.closed || this.serving) {
            LOG.error("The " + this.getClass().getName() + " was not closed.");
            this.close();
        }
    }

    public void communicationThread() {
        ZMQ.Socket client = null;
        ZMQ.Socket bridge = null;
        ZMQ.Socket control = null;

        try {
            this.serving = true;

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
            ExecutorService threadPool = Executors.newFixedThreadPool(config.getNumberOfCPUThreads());

            ZMQ.PollItem[] items = new ZMQ.PollItem[]{
                    new ZMQ.PollItem(client, ZMQ.Poller.POLLIN),
                    new ZMQ.PollItem(bridge, ZMQ.Poller.POLLIN),
                    new ZMQ.PollItem(control, ZMQ.Poller.POLLIN)
            };

            while (this.serving) {
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

                    this.serving = false;
                }
            }

        } catch (org.zeromq.ZMQException ex) {
            this.serving = false;
            LOG.error(ex.getLocalizedMessage());
        } finally {
            if (control != null) {
                try {
                    //Close the sockets
                    control.disconnect(this.controlSocketAddress);
                } catch (org.zeromq.ZMQException ex) {
                    LOG.error(ex.getLocalizedMessage());
                } finally {
                    control.close();
                }
            }

            if (bridge != null) {
                try {
                    /** TODO-GM There is a bug in libzmq whe unbind is used on inporc address. To be uncommented
                     * when libzmq is updated to 4.1+.
                     libzmq bug: https://github.com/zeromq/libzmq/issues/949
                     */
                    //bridge.unbind(this.bridgeAddress);
                } catch (org.zeromq.ZMQException ex) {
                    LOG.error(ex.getLocalizedMessage());
                } finally {
                    bridge.close();
                }
            }

            if (client != null) {
                try {
                    client.unbind(this.bindAddress);
                } catch (org.zeromq.ZMQException ex) {
                    LOG.error(ex.getLocalizedMessage());
                } finally {
                    client.close();
                }
            }
        }
    }
}
