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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003 - 2010 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */

package petascope.wcs2;

import javax.servlet.ServletException;
import junit.framework.Assert;
import org.junit.Test;
import petascope.BaseTestCase;
import petascope.wcs2.extensions.*;
import petascope.core.DbMetadataSource;
import petascope.ConfigManager;
import petascope.exceptions.RasdamanException;

/**
 * This a test class used to test if petascope can
 * properly parse multiple requests that require
 * access to a rasdaman database.
 *
 * @author Ernesto Rodriguez <ernesto4160@gmail.com>
 */

public class ParseMultipleRasdamanQueryTest extends BaseTestCase{

    //The number of concurrent requests that will be preformed.
    private final int NUM_REQUESTS = 5;

    /**
     * Test case for parsing multiple queries that require rasdaman.
     * Multiple queries will be simultaneously generated
     * this method should complete without exceptions. If an
     * exception results the test case fails. The exception
     * will result if a parser is invoked multiple times
     * from different threads.
     * @throws Exception
     */
    @Test
    public void concurrentRasdamanRequests() throws Exception {

        /**
         * Configuration directory containing "petascope.properties" setting file.
         * If @confDir@ is not default, manually edit this variable to your local configuration.
         */
        String confDir = ConfigManager.CONF_DIR_DEFAULT;
        try {
            // Initialize the singleton configuration manager. Now all classes can read the settings.
            ConfigManager.getInstance(confDir);
        } catch (IllegalArgumentException ex) {
            throw new ServletException(ex.getMessage());
        } catch (RasdamanException ex) {
            throw ex;
        }

        DbMetadataSource meta = new DbMetadataSource(
                ConfigManager.METADATA_DRIVER,
                ConfigManager.METADATA_URL,
                ConfigManager.METADATA_USER,
                ConfigManager.METADATA_PASS, false);


        PerformWCS2RasdamanQuery requests[] = new PerformWCS2RasdamanQuery[NUM_REQUESTS];
        KVPProtocolExtension pext = new KVPProtocolExtension();

        for(int i = 0; i < NUM_REQUESTS; i++){

            requests[i] = new PerformWCS2RasdamanQuery(pext, meta);
            (new Thread(requests[i])).start();
        }

        boolean isDone = false;

        while(!isDone) {
            isDone = true;
            for (int i = 0; i < NUM_REQUESTS; i++) {
                if(!requests[i].isDone())
                    isDone = false;
            }
        }

        for (int i = 0; i < NUM_REQUESTS; i++) {
            Assert.assertNull("request n." + i, requests[i].exception());
        }
    }
}
