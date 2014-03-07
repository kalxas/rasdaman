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
 * Copyright 2003 - 2014 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcps.server.cli;

import petascope.core.DbMetadataSource;
import petascope.wcps.server.core.ProcessCoveragesRequest;
import petascope.wcps.server.core.Wcps;
import java.io.File;
import java.io.FileInputStream;
import java.io.StringReader;
import java.util.Properties;
import org.xml.sax.InputSource;
import petascope.util.WcpsConstants;

/**
 * This is a small application around the WCPS core. It takes XML requests as files and runs them
 * through WCPS. The resulting radaman queries are displayed, but not executed. This is very useful
 * for testing whether your metadata is valid.
 */
public class xml {

    private static Wcps wcps;
    private static DbMetadataSource metadataSource;
    private static String rasserviceURL;


    private static void initMetadata() {
        File cwd = new File(".");
        System.out.println("Working in " + cwd.getAbsolutePath());
        String pcSchemaFileName =
                //		"src/conf/" +
                WcpsConstants.MSG_XML + File.separator + WcpsConstants.MSG_OGC + File.separator + WcpsConstants.MSG_WCPS
                + File.separator + "1.0.0" + File.separator + WcpsConstants.MSG_WCPS_COVERAGES;
        File pcSchemaFile = new File(pcSchemaFileName);

        if (!pcSchemaFile.exists()) {
            System.err.println("WCPS: could not find the WCPS ProcessCoverage schema ("
                    + pcSchemaFileName + ")");
            System.exit(1);
        }

        metadataSource = null;

        try {
            Properties dbParams = new Properties();

            dbParams.load(new FileInputStream(WcpsConstants.DBPARAM_SETTING_PROPERTIES));
            metadataSource =
                    new DbMetadataSource(dbParams.getProperty(WcpsConstants.DBPARAM_METADATA_DRIVER),
                    dbParams.getProperty(WcpsConstants.DBPARAM_METADATA_URL),
                    dbParams.getProperty(WcpsConstants.DBPARAM_METADATA_USER),
                    dbParams.getProperty(WcpsConstants.DBPARAM_METADATA_PASS), false);
            wcps = new Wcps(pcSchemaFile, metadataSource);
        } catch (Exception e) {
            System.err.println("WCPS: could not initialize WCPS:");
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        if (args.length > 2) {
            System.err.println("WCPS: no input files");
            System.exit(1);
        }

        initMetadata();
        rasserviceURL = args[0]; 
        for (int i = 1; i < args.length; i++) {
            File fileIn = null;
            InputSource is = null;

            try {
                fileIn = new File(args[i]);
                is = new InputSource(new FileInputStream(fileIn));
            } catch (Exception fnfe) {
                System.err.println("WCPS: " + args[i]
                        + ": no such file or directory" + fnfe);
                System.exit(1);
            }


            String result = processCoverage(is, i);
            if (result != null) {
                System.out.println(result);
            } else {
                System.err.println("WCPS: " + args[i] + " failed");
                System.exit(1);
            }
        }

        metadataSource.close();
        System.exit(0);

    }

    private static String processCoverage(InputSource is, int i) {
        String result = null;

        try {
            ProcessCoveragesRequest r = wcps.pcPrepare(rasserviceURL,
                    WcpsConstants.MSG_RASSERVICE, is);
            System.err.println("Request " + i);
            String rasql = r.getRasqlQuery();
            String mime = r.getMime();
//			result = "[" + mime + "] " + rasql;
            result = rasql;
        } catch (Exception e) {
            System.err.println("WCPS: request " + i
                    + " failed with the following exception:");
            e.printStackTrace(System.err);
        }

        return result;
    }

    /** Converts a WCPS XML query into a RasQL query string **/
    public static String convertXmlToRasql(String query) {
        String rasql = null;
        if (metadataSource == null) {
            initMetadata();
        }
        InputSource is = new InputSource(new StringReader(query));
        rasql = processCoverage(is, 1);
        return rasql;
    }
}
