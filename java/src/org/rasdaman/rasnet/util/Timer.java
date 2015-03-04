package org.rasdaman.rasnet.util;

import java.util.concurrent.TimeUnit;

public class Timer {
    private long deadline;
    private long period;

    /**
     *
     * @param period - measured in milliseconds.
     */
    public Timer(long period) {

        this.period = TimeUnit.NANOSECONDS.convert(period, TimeUnit.MILLISECONDS);
        this.deadline = System.nanoTime() + this.period;
    }

    public boolean hasExpired() {
        return (System.nanoTime() > this.deadline);
    }

    public void reset() {
        this.deadline = System.nanoTime() + this.period;
    }
}
