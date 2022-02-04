/*
 * This file is part of rasdaman community.
 *
 * Rasdaman community is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Rasdaman community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU  General Public License for more details.
 *
 * You should have received a copy of the GNU  General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003 - 2021 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.util;

/**
 *
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */

enum WaitingIncreasePolicy {
    NONE,
    DOUBLE_WAIT_TIME
}

public class WaitingTimer {

    private float initialWaitInSeconds = 10;
    private float maximumWaitInSeconds = 3600;
    private WaitingIncreasePolicy waitingIncreasePolicy = WaitingIncreasePolicy.DOUBLE_WAIT_TIME;
    private float waitingIncreaseFactor = 1; // only considered if waitingIncreasePolicy is NONE
    private float currentWaitInSeconds = initialWaitInSeconds;
    private long timeAnchorMillis = System.currentTimeMillis();

    public WaitingTimer(float initialWaitInSeconds) {
        this.initialWaitInSeconds = initialWaitInSeconds;
    }

    public WaitingTimer(WaitingIncreasePolicy waitingIncreasePolicy,
                        float initialWaitInSeconds, float maximumWaitInSeconds,
                        float waitingIncreaseFactor, float currentWaitInSeconds) {
        this.initialWaitInSeconds = initialWaitInSeconds;
        this.maximumWaitInSeconds = maximumWaitInSeconds;
        this.waitingIncreasePolicy = waitingIncreasePolicy;
        this.currentWaitInSeconds = currentWaitInSeconds;
        this.waitingIncreaseFactor = waitingIncreaseFactor;        
    }
    
    private void reset() {
        // 10 seconds
        this.currentWaitInSeconds = initialWaitInSeconds;
        this.timeAnchorMillis = System.currentTimeMillis();
    }

    public void reportFailedRequest() {
        
        // everytime the request is failed, then double the waiting time for this task
        if (waitingIncreasePolicy == WaitingIncreasePolicy.DOUBLE_WAIT_TIME) {
            currentWaitInSeconds *= 2;
        } else if (waitingIncreasePolicy == WaitingIncreasePolicy.NONE) {
            currentWaitInSeconds *= waitingIncreaseFactor;
        }
        if (currentWaitInSeconds > maximumWaitInSeconds) {
            this.reset();
        }
    }
    
    public void reportSuccessRequest() {
        this.reset();
    }
    
    /**
     * Check if the task should run by the current time and its stored time in milliseconds
     */
    public boolean shouldRun(long currentTimeMillis) {
        // By default, this task should run, when it doesn't have any failed request
        if (currentWaitInSeconds == initialWaitInSeconds) {
            return true;
        } else {
            // this task had at least a failed request before, it must wait for time penalty
            return (currentTimeMillis - this.timeAnchorMillis) / 1000 > currentWaitInSeconds;
        }
    }

    public float getCurrentWaitInSeconds() {
        return currentWaitInSeconds;
    }
}
