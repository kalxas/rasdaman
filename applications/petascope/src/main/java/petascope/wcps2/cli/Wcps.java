package petascope.wcps2.cli;

import org.apache.commons.io.IOUtils;
import petascope.exceptions.RasdamanException;
import petascope.util.ras.RasQueryResult;
import petascope.util.ras.RasUtil;
import petascope.wcps2.error.managed.processing.WCPSProcessingError;
import petascope.wcps2.translator.WcpsTranslator;

import java.io.DataOutputStream;
import java.io.FileInputStream;
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
     *   - a file containing the query to be executed
     *   - a path where the result will be written
     * @throws IOException
     * @throws RasdamanException
     */
    public static void main(String[] args) throws IOException, RasdamanException {
        FileInputStream inputStream = new FileInputStream(args[0]);

        String query;
        try {
            query = IOUtils.toString(inputStream);
            System.out.println("Executing query: " + query);
        } finally {
            inputStream.close();
        }
        try {
            executeQuery(run(query), args[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Runs the translation process and returns the rasql query.
     *
     * @param query
     */
    public static String run(String query) throws WCPSProcessingError {
        long startTime = System.nanoTime();
        WcpsTranslator translator = new WcpsTranslator(query);
        String rasqlQuery = translator.translate();
        long endTime = System.nanoTime();
        System.out.println(query + " -> " + rasqlQuery + ": " + (endTime-startTime)/1000000L);
        return rasqlQuery;
    }

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
