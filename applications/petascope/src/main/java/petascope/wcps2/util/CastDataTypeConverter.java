package petascope.wcps2.util;

import petascope.util.WcpsConstants;

/**
 * Utility class to convert a petascope data type to a rasdaman data type
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class CastDataTypeConverter {

    /**
     * This is only a static class so make sure no one instantiates it
     */
    private CastDataTypeConverter() {
        throw new AssertionError("This class should not be instantiated");
    }

    /**
     * Converts a petascope data type to a rasdaman data type
     *
     * @param dataTypeToBeConverted the data type to be converted
     * @return the rasdaman data type
     */
    public static String convert(String dataTypeToBeConverted) {
        String result = dataTypeToBeConverted.toLowerCase();
        if (result.equals(WcpsConstants.MSG_BOOLEAN)) {
            result = WcpsConstants.MSG_BOOL;
        } else if (result.equals(WcpsConstants.MSG_CHAR)) {
            result = WcpsConstants.MSG_OCTET;
        } else if (result.equals(WcpsConstants.MSG_UNSIGNED_CHAR)) {
            result = WcpsConstants.MSG_CHAR;
        } else if (result.equals(WcpsConstants.MSG_INT)) {
            result = WcpsConstants.MSG_LONG;
        } else if (result.equals(WcpsConstants.MSG_UNSIGNED_INT)) {
            result = WcpsConstants.MSG_UNSIGNED_LONG;
        } else if (result.equals(WcpsConstants.MSG_UNSIGNED_LONG)) {
            result = WcpsConstants.MSG_LONG;
        } else if (result.equals(WcpsConstants.MSG_COMPLEX + "2")) {
            result = WcpsConstants.MSG_COMPLEX + "d";
        }
        //short, unsigned short and complex have identity mapping
        return result;
    }
}
