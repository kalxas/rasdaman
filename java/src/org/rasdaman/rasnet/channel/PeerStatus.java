package org.rasdaman.rasnet.channel;

import org.rasdaman.rasnet.util.Timer;

public class PeerStatus {

    private int retries;
    private int retriesBackup;
    private Timer timer;

    public PeerStatus(int retries, long period) {
        this.retries = retries;
        this.retriesBackup = retries;
        this.timer = new Timer(period);
    }

    public boolean isAlive() {
        return this.retries > 0;
    }

    public boolean decreaseLiveliness() {
        if (this.timer.hasExpired()) {
            if (this.retries > 0) {
                this.retries--;
                this.timer.reset();
            }

            return true;
        }
        return false;
    }

    public void reset() {
        this.timer.reset();
        this.retries = this.retriesBackup;
    }
}
