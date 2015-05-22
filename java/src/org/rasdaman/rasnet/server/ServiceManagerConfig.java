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

import org.rasdaman.rasnet.common.Constants;

public class ServiceManagerConfig {
    private int numberOfIoThreads;
    private int numberOfCPUThreads;
    private int maxOpenSockets;
    private int aliveTimeout;
    private int aliveRetryNo;

    public ServiceManagerConfig() {
        this.numberOfIoThreads = Constants.DEFAULT_SERVER_IO_THREADS_NO;
        this.numberOfCPUThreads = Constants.DEFAULT_SERVER_CPU_THREADS_NO;
        this.aliveTimeout = Constants.DEFAULT_SERVER_ALIVE_TIMEOUT;
        this.aliveRetryNo = Constants.DEFAULT_SERVER_LIVES;
        this.maxOpenSockets = Constants.DEFAULT_SERVER_MAX_OPEN_SOCKETS;
    }

    public int getNumberOfIoThreads() {
        return numberOfIoThreads;
    }

    public void setNumberOfIoThreads(int numberOfIoThreads) {
        this.numberOfIoThreads = numberOfIoThreads;
    }

    public int getNumberOfCPUThreads() {
        return numberOfCPUThreads;
    }

    public void setNumberOfCPUThreads(int numberOfCPUThreads) {
        this.numberOfCPUThreads = numberOfCPUThreads;
    }

    public int getAliveTimeout() {
        return aliveTimeout;
    }

    public void setAliveTimeout(int aliveTimeout) {
        this.aliveTimeout = aliveTimeout;
    }

    public int getAliveRetryNo() {
        return aliveRetryNo;
    }

    public void setAliveRetryNo(int aliveRetryNo) {
        this.aliveRetryNo = aliveRetryNo;
    }

    public int getMaxOpenSockets() {
        return maxOpenSockets;
    }

    public void setMaxOpenSockets(int maxOpenSockets) {
        this.maxOpenSockets = maxOpenSockets;
    }
}
