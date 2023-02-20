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
import org.rasdaman.rasnet.service.RasmgrClientServiceGrpc;
import org.rasdaman.rasnet.service.RasmgrClientServiceOuterClass;
import org.rasdaman.rasnet.util.Constants;
import org.rasdaman.rasnet.util.GrpcUtils;
import rasj.global.Debug;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class RasmgrKeepAlive {
    private volatile boolean isKeepAliveRunning = false;
    private final Lock keepAliveLock = new ReentrantLock();
    private final Condition isKeepAliveRunningCond = keepAliveLock.newCondition();
    private final RasmgrClientServiceGrpc.RasmgrClientServiceBlockingStub rasmgrService;
    private final int clientID;
    private final long aliveIntervalMilliseconds;
    private Thread runnerThread;

    /**
     * Create a new RasmgrKeepAlive object that, when started, connects throw the given
     * RasmgrClientServiceGrpc to the rasmgr and sends keep alive messages.
     *
     * @param rasmgrService             GRPC service that is connected to a rasserver
     * @param clientID                String that uniquely identifies the client on the server
     * @param aliveIntervalMilliseconds The number of milliseconds between each heart beat.
     */
    public RasmgrKeepAlive(RasmgrClientServiceGrpc.RasmgrClientServiceBlockingStub rasmgrService,
                           int clientID, long aliveIntervalMilliseconds) {
        this.rasmgrService = rasmgrService;
        this.clientID = clientID;
        this.aliveIntervalMilliseconds = aliveIntervalMilliseconds;
    }

    /**
     * Creates and starts a thread that will send KeepAlive messages to the server.
     * After this method is called, stop must be called.
     * This method can be called only once.
     */
    public void start() {
        this.keepAliveLock.lock();

        try {
            if (this.isKeepAliveRunning) {
                throw new AssertionError("The " + this.getClass().getName() + " must be started only once.");
            }

            this.isKeepAliveRunning = true;
            //Start the keep alive
            this.runnerThread = new Thread(new KeepAliveRunner(this.clientID));
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
        private final int clientUUID;

        public KeepAliveRunner(int clientUUID) {
            this.clientUUID = clientUUID;
        }

        @Override
        public void run() {
            keepAliveLock.lock();

            try {
                while (isKeepAliveRunning) {
                    if (!isKeepAliveRunningCond.await(aliveIntervalMilliseconds, TimeUnit.MILLISECONDS)) {
                        //The time expired
                        RasmgrClientServiceOuterClass.KeepAliveReq keepAliveReq = RasmgrClientServiceOuterClass.KeepAliveReq.newBuilder()
                                .setClientId(clientID)
                                .build();

                        //If this throws an exception, let it fail.
                        rasmgrService.withDeadlineAfter(Constants.SERVICE_CALL_TIMEOUT, TimeUnit.MILLISECONDS)
                        .keepAlive(keepAliveReq);
                    }
                }
            } catch (InterruptedException ex) {
                Debug.talkCritical("The worker thread was interrupted:" + ex.getMessage());
            } catch (StatusRuntimeException ex) {
                Exception actualException = GrpcUtils.convertStatusToRuntimeException(ex.getStatus());
                Debug.talkCritical(actualException.getMessage());
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
