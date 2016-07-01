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
 * Copyright 2003 - 2016 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcps2.cli;

import petascope.exceptions.rasdaman.RasdamanException;
import petascope.util.ras.RasQueryResult;
import petascope.util.ras.RasUtil;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * This class provides a method to run wcps queries directly from the cli. Useful for testing and debugging
 * of parsers and translators
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class Wcps {

    /**
     * Main method of the program
     *
     * @param args input arguments:
     *             - a file containing the query to be executed
     *             - a path where the result will be written
     * @throws IOException
     * @throws RasdamanException
     */
    public static void main(String[] args) throws IOException, RasdamanException {
//        FileInputStream inputStream = new FileInputStream(args[0]);
//        CoordinateTranslationService coordinateTranslationService = new CoordinateTranslationService();
//        WcpsCoverageMetadataService wcpsCoverageMetadataService = new WcpsCoverageMetadataService();
//        String query;
//        try {
//            query = IOUtils.toString(inputStream);
//            System.out.println("Executing query: " + query);
//        } finally {
//            inputStream.close();
//        }
//        try {
//            executeQuery(run(query), args[1]);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

    }


    /**
     * Runs the translation process and returns the rasql query.
     *
     * @param query
     */
//    public static String run(String query) throws WCPSProcessingError {
//        long startTime = System.nanoTime();
//        WcpsTranslator translator = new WcpsTranslator(query);
//        WcpsResult wcpsResult = translator.translate();
//        // Check if wcpsResult return directly meta value then should not execute the result
//        if(wcpsResult instanceof WCPSMetaResult) {
//            throw new NotRasqlException(query, wcpsResult.getResult());
//        }
//        long endTime = System.nanoTime();
//        // Return Rasql query
//        return wcpsResult.getResult();
//    }

    /**
     * Executes a rasql query and dumps the results into a file
     *
     * @param query the query to be executed
     * @param file  the file where the results will be dumped
     * @throws IOException
     * @throws RasdamanException
     */
    public static void executeQuery(String query, String file) throws IOException, RasdamanException {
        DataOutputStream printer = null;
        try {
            if (file != null) {
                printer = new DataOutputStream(new FileOutputStream(file));
            }
            RasQueryResult res = new RasQueryResult(RasUtil.executeRasqlQuery(query));
            if (!res.getMdds().isEmpty() || !res.getScalars().isEmpty()) {
                for (String s : res.getScalars()) {
                    if (file == null) {
                        System.out.print(s);
                    } else {
                        printer.write(s.getBytes());
                        printer.flush();
                    }
                }
                for (byte[] bs : res.getMdds()) {
                    if (file == null) {
                        System.out.write(bs);
                    } else {
                        printer.write(bs);
                        printer.flush();
                    }
                }
                if (file != null) {
                    printer.flush();
                }
            } else {
                System.out.println("WCPS: Warning! No result returned from rasql query.");
            }
        } finally {
            if (file == null) {
                printer.close();
            }
        }
    }

}
