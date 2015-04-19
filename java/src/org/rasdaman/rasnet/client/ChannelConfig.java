package org.rasdaman.rasnet.client;

import org.rasdaman.rasnet.common.Constants;

public class ChannelConfig {
    private int channelTimeout;
    private int aliveTimeout;
    private int aliveRetryNo;
    private int maxOpenSockets;
    private int ioThreadsNo;

    public ChannelConfig() {
        //TODO-GM: Maybe read from .properties file?
        this.channelTimeout = Constants.DEFAULT_CHANNEL_TIMEOUT;
        this.aliveTimeout = Constants.DEFAULT_CLIENT_ALIVE_TIMEOUT;
        this.aliveRetryNo = Constants.DEFAULT_CLIENT_ALIVE_RETRIES;
        this.ioThreadsNo = Constants.DEFAULT_CHANNEL_IO_THREAD_NO;
        this.maxOpenSockets = Constants.DEFAULT_CHANNEL_MAX_OPEN_SOCKETS;
    }

    public int getAliveRetryNo() {
        return aliveRetryNo;
    }

    public void setAliveRetryNo(int aliveRetryNo) {
        this.aliveRetryNo = aliveRetryNo;
    }

    public int getAliveTimeout() {
        return aliveTimeout;
    }

    public int getIoThreadsNo() {
        return ioThreadsNo;
    }

    public void setIoThreadsNo(int ioThreadsNo) {
        this.ioThreadsNo = ioThreadsNo;
    }

    public int getChannelTimeout() {
        return channelTimeout;
    }

    public void setChannelTimeout(int channelTimeout) {
        this.channelTimeout = channelTimeout;
    }

    public int getMaxOpenSockets() {
        return maxOpenSockets;
    }

    public void setMaxOpenSockets(int maxOpenSockets) {
        this.maxOpenSockets = maxOpenSockets;
    }

}
