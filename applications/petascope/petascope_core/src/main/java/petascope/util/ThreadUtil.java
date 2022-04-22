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
 * Copyright 2003 - 2020 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.util;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.LoggerFactory;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;

/**
 *
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
public class ThreadUtil {
    
    private static final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(ThreadUtil.class);    
    
    /**
     * Run a list of tasks in parallel in batch mode (max = number of CPU cores)
     */
    public static void executeMultipleTasksInParallel(List<Callable<Object>> tasks) throws PetascopeException {
        try {
            executorService.invokeAll(tasks);
        } catch (InterruptedException ex) {
            throw new PetascopeException(ExceptionCode.RuntimeError, 
                      "Error while running multiple threads. Reason: " + ex.getMessage(), ex);
        }
    }
    
    /**
     * Given a list of tasks, run them in a separated thread
     */
    public static void executeTasksInParallelInBackground(final String tasksDescription, final List<Callable<Object>> tasks) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    ThreadUtil.executeMultipleTasksInParallel(tasks);
                } catch (PetascopeException ex) {
                    log.warn("Failed to run tasks to " + tasksDescription + ". Reason: " + ex.getMessage(), ex);
                }
            }
        };
        
        Thread thread = new Thread(runnable);
        thread.start();
    }
}
