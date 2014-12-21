package org.rasdaman.rasnet.util;

public class Timer {
    private long start;
    private long period;

    public Timer(long period) {
        this.start = System.currentTimeMillis();
        this.period = period;
    }

    public boolean hasExpired() {
        return (System.currentTimeMillis() > start + period);
    }

    public void reset() {
        this.start = System.currentTimeMillis();
    }
}
