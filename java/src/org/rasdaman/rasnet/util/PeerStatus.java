package org.rasdaman.rasnet.util;

public class PeerStatus {
    private final int retriesBackup;
    private int retries;
    private Timer timer;

    public PeerStatus(int retries, int lifetime) {
        if (retries < 0 || lifetime < 0) {
            throw new IllegalArgumentException("The number of retries and the lifetime must be positive.");
        }

        this.timer = new Timer(lifetime);
        this.retries = retries;
        this.retriesBackup = retries;
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
        } else {
            return false;
        }
    }

    public void reset() {
        this.retries = this.retriesBackup;
        this.timer.reset();
    }
}
