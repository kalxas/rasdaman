package org.rasdaman.rasnet.channel;

public class ChannelConfig {

    private int numberOfIoThreads;
    private int aliveTimeout;
    private int aliveRetryNo;
    private int connectionTimeout;

    public ChannelConfig() {
    }

    public int getNumberOfIoThreads() {
        return numberOfIoThreads;
    }

    public void setNumberOfIoThreads(int numberOfIoThreads) {
        this.numberOfIoThreads = numberOfIoThreads;
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

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }
}
