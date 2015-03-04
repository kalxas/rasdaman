package org.rasdaman.rasnet.server;

import org.rasdaman.rasnet.common.Constants;

/**
 * Created by rasdaman on 2/19/15.
 */
public class ServiceManagerConfig {
    private int ioThreadNo;
    private int cpuThreadNo;
    private int aliveTimeout;
    private int aliveRetryNo;
    private int maxOpenSockets;

    private ServiceManagerConfig(int ioThreadNo, int cpuThreadNo, int aliveTimeout, int aliveRetryNo, int maxOpenSockets) {
        this.ioThreadNo = ioThreadNo;
        this.cpuThreadNo = cpuThreadNo;
        this.aliveTimeout = aliveTimeout;
        this.aliveRetryNo = aliveRetryNo;
        this.maxOpenSockets = maxOpenSockets;
    }

    public static ServiceManagerConfig defaultInstance() {
        return new ServiceManagerConfig(
                Constants.SERVER_IO_THREAD_NO,
                Constants.CPU_THREAD_NO,
                Constants.SERVER_ALIVE_TIMEOUT,
                Constants.SERVER_ALIVE_RETRIES,
                Constants.MAX_OPEN_SOCKETS);
    }

    public int getIoThreadNo() {
        return ioThreadNo;
    }

    public void setIoThreadNo(int ioThreadNo) {
        this.ioThreadNo = ioThreadNo;
    }

    public int getCpuThreadNo() {
        return cpuThreadNo;
    }

    public void setCpuThreadNo(int cpuThreadNo) {
        this.cpuThreadNo = cpuThreadNo;
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
