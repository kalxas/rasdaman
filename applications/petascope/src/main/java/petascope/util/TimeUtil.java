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
package petascope.util;
import java.util.HashMap;
import java.util.Map;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.wcps.server.core.DomainElement;

/**
 * Class for handling timestamps formatting and elaborations.
 * It relies on the Joda-Time library (http://www.joda.org/joda-time/).
 *
 * @author <a href="mailto:p.campalani@jacobs-university.de">Piero Campalani</a>
 */
public class TimeUtil {

    // Standard codes for supported temporal Unit of Measures (which provide ISO8601 interface to the user)
    // http://unitsofmeasure.org/ucum.html
    public static final String UCUM_MILLIS               = "ms";
    public static final String UCUM_SECOND               = "s";
    public static final String UCUM_MINUTE               = "min";
    public static final String UCUM_HOUR                 = "h";
    public static final String UCUM_DAY                  = "d";
    public static final String UCUM_WEEK                 = "wk";
    public static final String UCUM_MEAN_JULIAN_MONTH    = "mo_j";
    public static final String UCUM_MEAN_GREGORIAN_MONTH = "mo_g";
    public static final String UCUM_SYNODAL_MONTH        = "mo_s";
    public static final String UCUM_MONTH                = "mo";
    public static final String UCUM_MEAN_JULIAN_YEAR     = "a_j";
    public static final String UCUM_MEAN_GREGORIAN_YEAR  = "a_g";
    public static final String UCUM_TROPICAL_YEAR        = "a_t";
    public static final String UCUM_YEAR                 = "a";

    // Milliseconds associated to each supported temporal UoM:
    public static final Long MILLIS_MILLIS               = 1L;
    public static final Long MILLIS_SECOND               = 1000L;
    public static final Long MILLIS_MINUTE               = MILLIS_SECOND * 60;
    public static final Long MILLIS_HOUR                 = MILLIS_MINUTE * 60;
    public static final Long MILLIS_DAY                  = MILLIS_HOUR * 24;
    public static final Long MILLIS_WEEK                 = MILLIS_DAY * 7;
    public static final Long MILLIS_MEAN_JULIAN_YEAR     = (long) (MILLIS_DAY * 365.25);
    public static final Long MILLIS_MEAN_GREGORIAN_YEAR  = (long) (MILLIS_DAY * 365.2425);
    public static final Long MILLIS_TROPICAL_YEAR        = (long) (MILLIS_DAY * 365.24219);
    public static final Long MILLIS_YEAR                 = MILLIS_MEAN_JULIAN_YEAR;
    public static final Long MILLIS_MEAN_JULIAN_MONTH    = MILLIS_MEAN_JULIAN_YEAR / 12;
    public static final Long MILLIS_MEAN_GREGORIAN_MONTH = MILLIS_MEAN_GREGORIAN_YEAR / 12;
    public static final Long MILLIS_SYNODAL_MONTH        = (long) (MILLIS_DAY * 29.53059);
    public static final Long MILLIS_MONTH                = MILLIS_MEAN_JULIAN_MONTH;

    // Logger
    private static Logger log = LoggerFactory.getLogger(TimeUtil.class);

    /**
     * Registry of Temporal UoMs which support ISO timestamp conversion.
     */
    private static final Map<String, Long> timeUomsRegistry = new HashMap<String, Long>();

    /**
     * ISO datetime formatter.
     * Valid formats and other ISO formatters: http://www.joda.org/joda-time/apidocs/org/joda/time/format/ISODateTimeFormat.html
     *
     * The default chronology in Joda-Time is ISO (ISO8601).
     * For other Joda-Time Chronologies see http://www.joda.org/joda-time/apidocs/org/joda/time/Chronology.html.
     * For a description of the ISO8601 calendar system see http://www.joda.org/joda-time/cal_iso.html.
     *
     * "For most applications, the Chronology can be ignored as it will default to the ISOChronology.
     *  This is suitable for most uses. You would change it if you need accurate dates before October 15, 1582,
     *  or whenever the Julian calendar ceased in the territory you're interested in).
     *  You'd also change it if you need a specific calendar like the Coptic calendar illustrated earlier". (Joda-Time)
     */
    private static final DateTimeFormatter isoFmt;

    static {
        //Create an ISO formatter with optional time and UTC default time zone
        isoFmt = ISODateTimeFormat.dateOptionalTimeParser().withZoneUTC();
        // NOTE: if zone is not assigned, the default time zone of the JVM will be picked: no-no.

        // Fill the registry of time UoMs:
        timeUomsRegistry.put(UCUM_MILLIS, MILLIS_MILLIS);
        timeUomsRegistry.put(UCUM_SECOND, MILLIS_SECOND);
        timeUomsRegistry.put(UCUM_MINUTE, MILLIS_MINUTE);
        timeUomsRegistry.put(UCUM_HOUR, MILLIS_HOUR);
        timeUomsRegistry.put(UCUM_DAY, MILLIS_DAY);
        timeUomsRegistry.put(UCUM_WEEK, MILLIS_WEEK);
        timeUomsRegistry.put(UCUM_MEAN_JULIAN_MONTH, MILLIS_MEAN_JULIAN_MONTH);
        timeUomsRegistry.put(UCUM_MEAN_GREGORIAN_MONTH, MILLIS_MEAN_GREGORIAN_MONTH);
        timeUomsRegistry.put(UCUM_SYNODAL_MONTH, MILLIS_SYNODAL_MONTH);
        timeUomsRegistry.put(UCUM_MONTH, MILLIS_MONTH);
        timeUomsRegistry.put(UCUM_MEAN_JULIAN_YEAR, MILLIS_MEAN_JULIAN_YEAR);
        timeUomsRegistry.put(UCUM_MEAN_GREGORIAN_YEAR, MILLIS_MEAN_GREGORIAN_YEAR);
        timeUomsRegistry.put(UCUM_TROPICAL_YEAR, MILLIS_TROPICAL_YEAR);
        timeUomsRegistry.put(UCUM_YEAR, MILLIS_YEAR);
    }

    /**
     * Check if the input timestamp is in an understandable format.
     *
     * @param   timestamp    Timestamp string requested by the client
     * @return  True if valid ISO timestamp.
     */
    public static boolean isValidTimestamp (String timestamp) {
        boolean isValid = true;
        try {
            DateTime dt = isoFmt.parseDateTime(fix(timestamp));
        } catch (IllegalArgumentException ex) {
            log.debug(timestamp + " format is invalid or unsupported: " + ex.getMessage());
            isValid = false;
        }
        return isValid;
    }

    /**
     * Verifies that the two input timestamps define a valid interval.
     *
     * @param   timestampLo    timestamp
     * @param   timestampHi    timestamp
     * @return  True if Lo is lower or equal than Hi
     * @throws PetascopeException
     */
    public static boolean isOrderedTimeSubset (String timestampLo, String timestampHi) throws PetascopeException {

        DateTime dtLo = isoFmt.parseDateTime(fix(timestampLo));
        DateTime dtHi = isoFmt.parseDateTime(fix(timestampHi));
        Duration millis;
        try {
            millis = new Duration(dtLo, dtHi);
        } catch (ArithmeticException ex) {
            log.error("Error while computing milliseconds between " + dtLo + " and " + dtHi + ".");
            throw new PetascopeException(ExceptionCode.InternalComponentError,
                    "Cannot convert input datetimes to numeric time coordinates: duration exceeds a 64 bit long.", ex);
        }
        return (millis.getMillis() >= 0);
    }

    /**
     * Count how many temporal units (i.e. offset vectors) fit inside a time interval.
     *
     * @param timestampLo       Lower ISO timestamp
     * @param timestampHi       Upper ISO timestamp
     * @param timeResolution    Temporal UoM of the CRS
     * @param timeVector        Length of the offset vector along time [TIME(n) := timeEpoch + n*(timeResolution*timeVector)]
     * @return How many time units (with fractional resolution) fit into the time interval [timestampHi-timestampLo]
     * @throws PetascopeException
     */
    public static Double countOffsets(String timestampLo, String timestampHi, String timeResolution, Double timeVector)
            throws PetascopeException {

        // local variables
        Double fractionalTimeSteps;

        DateTime dtLo = isoFmt.parseDateTime(fix(timestampLo));
        DateTime dtHi = isoFmt.parseDateTime(fix(timestampHi));

        // Determine milliseconds between these two datetimes
        Duration millis;
        try {
            millis = new Duration(dtLo, dtHi);
        } catch (ArithmeticException ex) {
            log.error("Error while computing milliseconds between " + dtLo + " and " + dtHi + ".");
            throw new PetascopeException(ExceptionCode.InternalComponentError,
                    "Cannot convert input datetimes to numeric time coordinates: duration exceeds a 64 bit long.", ex);
        }

        // Compute how many vectors of distance are there between dtLo and dtHi along this time CRS
        // NOTE: not necessarily an integer number of vectors will fit in the interval, clearly.
        // Formula:
        //               fractionalTimeSteps := milliseconds(lo,hi) / [milliseconds(offset_vector)]
        // WHERE milliseconds(offset_vector) := milliseconds(timeResolution) * vector_length
        Long vectorMillis = (long) (getMillis(timeResolution) * timeVector);
        fractionalTimeSteps = 1D*millis.getMillis() / vectorMillis;

        log.debug("Computed " + fractionalTimeSteps + " offset-vectors between " + dtLo + " and " + dtHi + ".");
        return fractionalTimeSteps;
    }

    /**
     * Removes an epsilon to the input timestamp.
     *
     * When translating to cell indexes, there is a particular case:
     *
     *    9h00     10h00     11h00     12h00     13h00
     *     o---------o---------o---------o---------o
     *        cell0     cell1     cell2     cell3
     *
     * E.g. subset=t(10h00:18h00)
     * cellLo = cellMin + countPixels( 9h00:10h00) = cellMin + 1
     * cellHi = cellLo  + countPixels(10h00:13h00) = cellLo  + 3 --> overflow
     *
     * Whereas subset=t(10h01:18h00) would work fine: things work when
     * excluding the maximum, i.e. < instead of <=.
     * @param timestamp  ISO:8601 timestamp
     * @return 1 nanosecond before timestamp.
     */
    public static String minusEpsilon(String timestamp) {
        DateTime dt = isoFmt.parseDateTime(fix(timestamp));
        DateTime dtEps = dt.minusMillis(1);
        return dtEps.toString();
    }

    /**
     * Add a certain time duration to a timestamp.
     * @param timestamp The starting time instant.
     * @param coefficient The coefficient of the second addend.
     * @param timeResolution The time resolution to be added to timestamp ("c" times).
     * @return The ISO representation of timestamp+c*timeResolution.
     * @throws PetascopeException
     */
    public static String plus(String timestamp, Double coefficient, String timeResolution) throws PetascopeException {
        DateTime dt = isoFmt.parseDateTime(fix(timestamp));
        DateTime outDt = dt.plus((long) (getMillis(timeResolution)*coefficient));
        return outDt.toString();
    }

    /**
     * Retrieves the time instant of a numeric time coordinate.
     * @param numCoordinate
     * @param datumOrigin
     * @param timeResolution
     * @return The time instant correspondent to the input time coordinate.
     * @throws PetascopeException
     */
    public static String coordinate2timestamp(Double numCoordinate, String datumOrigin, String timeResolution) throws PetascopeException {
            return TimeUtil.plus(datumOrigin, numCoordinate, timeResolution);
    }

    /**
     * Get the number of milliseconds fitting in the provided UoM (UCUM abbreviation).
     * @param ucumAbbreviation See c/s symbols in http://unitsofmeasure.org/ucum.html
     * @return How many milliseconds [ms] are in the specified duration.
     * @throws PetascopeException
     */
    private static Long getMillis(String ucumAbbreviation) throws PetascopeException {
        Long millis = timeUomsRegistry.get(ucumAbbreviation);
        if (null==millis) {
            throw new PetascopeException(ExceptionCode.InvalidCoverageConfiguration,
                    "Unsupported temporal Unit of Measure [" + ucumAbbreviation + "].");
        }
        return millis;
    }

    /**
     * Remove quotes from a timestamp, if present.
     * @param timestamp   ISO:8601 timestamp (possibly quoted)
     * @return String        PSQL/DateTime compatible timestamp
     */
    private static String fix(String timestamp) {
        String unquotedTimestamp;

        unquotedTimestamp = timestamp.replaceAll("%22", "");
        unquotedTimestamp = unquotedTimestamp.replaceAll("'"  , "");
        unquotedTimestamp = unquotedTimestamp.replaceAll("\""  , "");

        return unquotedTimestamp;
    }
}
