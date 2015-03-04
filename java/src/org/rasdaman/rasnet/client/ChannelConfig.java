package org.rasdaman.rasnet.client;

/**
 * Created by rasdaman on 2/24/15.
 */
public class ChannelConfig {
    private String serverEndpoint;
    private int channelTimeout;
    private int ioThreadsNo;
    private int aliveTimeout;
    private int aliveRetryNo;
    private int connectionTimeout;
    private int maxOpenSockets;

    public ChannelConfig() {
        //TODO:Refactor
        //TODO-GM: Maybe read from .properties file?
        //TODO-GM: which fields are immutable? => make them final
        this.channelTimeout = 1;
        this.ioThreadsNo = 1;
        this.aliveTimeout = 1000;
        this.aliveRetryNo = 1;
        this.connectionTimeout = 1000;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
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

    public String getServerEndpoint() {
        return serverEndpoint;
    }

    public void setServerEndpoint(String serverEndpoint) {
        this.serverEndpoint = serverEndpoint;
    }
}
