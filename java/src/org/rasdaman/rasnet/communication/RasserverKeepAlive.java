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

package org.rasdaman.rasnet.communication;

import io.grpc.StatusRuntimeException;
import org.rasdaman.rasnet.service.ClientRasServerService;
import org.rasdaman.rasnet.service.ClientRassrvrServiceGrpc;
import org.rasdaman.rasnet.util.Constants;
import org.rasdaman.rasnet.util.GrpcUtils;
import rasj.global.Debug;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class RasserverKeepAlive {
    private volatile boolean isKeepAliveRunning = false;
    private final Lock keepAliveLock = new ReentrantLock();
    private final Condition isKeepAliveRunningCond = keepAliveLock.newCondition();
    private final ClientRassrvrServiceGrpc.ClientRassrvrServiceBlockingStub rasserverService;
    private final String clientUUID;
    private final String sessionId;
    private final long aliveIntervalMilliseconds;
    private Thread runnerThread;

    /**
     * Create a new RasserverKeepAlive object that, when started, connects throw the given
     * ClientRassrvrServiceGrpc to the rasserver and sends keep alive messages.
     *
     * @param rasserverService GRPC service that is connected to a rasserver
     * @param clientUUID       String that uniquely identifies the client on the server
     * @param sessionId        String that uniquely identifies the database session on the server
     */
    public RasserverKeepAlive(ClientRassrvrServiceGrpc.ClientRassrvrServiceBlockingStub rasserverService,
                              String clientUUID,
                              String sessionId,
                              long aliveIntervalMilliseconds) {
        this.rasserverService = rasserverService;
        this.clientUUID = clientUUID;
        this.sessionId = sessionId;
        this.aliveIntervalMilliseconds = aliveIntervalMilliseconds;

    }

    /**
     * Creates and starts a thread that will send KeepAlive messages to the server.
     * After this method is called, stop must be called.
     */
    public void start() {
        this.keepAliveLock.lock();

        try {
            if (this.isKeepAliveRunning) {
                throw new AssertionError("The " + this.getClass().getName() + " must be started only once.");
            }

            this.isKeepAliveRunning = true;
            //Start the keep alive
            this.runnerThread = new Thread(new KeepAliveRunner(this.clientUUID, this.sessionId));
            this.runnerThread.start();

        } catch (Exception ex) {
            Debug.talkCritical("Failed to start keep alive thread:" + ex.getMessage());
            //If an exception is throw, we must restore the invariants
            this.isKeepAliveRunning = false;
            this.runnerThread = null;
        } finally {
            this.keepAliveLock.unlock();
        }
    }

    /**
     * Check if the worker thread is running.
     *
     * @return
     */
    public boolean isRunning() {
        this.keepAliveLock.lock();

        try {
            return this.isKeepAliveRunning;
        } finally {
            this.keepAliveLock.unlock();
        }
    }

    /**
     * Stop the thread that is sending KeepAlive messages to the server.
     */
    public void stop() {
        this.keepAliveLock.lock();
        try {
            this.isKeepAliveRunning = false;
            this.isKeepAliveRunningCond.signal();
        } finally {
            this.keepAliveLock.unlock();

            try {
                this.runnerThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private class KeepAliveRunner implements Runnable {
        /**
         * Client unique ID
         */
        private final String clientUUID;

        /**
         * Session unqiue ID
         */
        private final String sessionId;

        public KeepAliveRunner(String clientUUID, String sessionId) {
            this.clientUUID = clientUUID;
            this.sessionId = sessionId;
        }

        @Override
        public void run() {
            keepAliveLock.lock();

            try {
                while (isKeepAliveRunning) {
                    if (!isKeepAliveRunningCond.await(aliveIntervalMilliseconds, TimeUnit.MILLISECONDS)) {
                        //The time expired
                        ClientRasServerService.KeepAliveRequest keepAliveReq = ClientRasServerService.KeepAliveRequest.newBuilder()
                                .setClientUuid(this.clientUUID)
                                .setSessionId(this.sessionId)
                                .build();

                        //If this throws an exception, let it fail.
                        rasserverService.withDeadlineAfter(Constants.SERVICE_CALL_TIMEOUT, TimeUnit.MILLISECONDS)
                                .keepAlive(keepAliveReq);
                    }
                }
            } catch (InterruptedException ex) {
                Debug.talkCritical("Keep alive thread has failed:" + ex.getMessage());
            } catch (StatusRuntimeException ex) {
                Exception actualException = GrpcUtils.convertStatusToRuntimeException(ex.getStatus());
                Debug.talkCritical("Keep alive thread has failed:" + actualException.getMessage());
            } finally {
                isKeepAliveRunning = false;
                keepAliveLock.unlock();
            }
        }
    }

    /**
     * If the stop method was not called, this method will call it.
     *
     * @throws Throwable
     */
    @Override
    protected void finalize() throws Throwable {
        keepAliveLock.lock();
        try {
            if (this.isKeepAliveRunning) {
                Debug.talkCritical("The " + this.getClass().getName() + " was not stopped.");
                this.stop();
            }
        } finally {
            this.keepAliveLock.unlock();
        }
        super.finalize();
    }
}
