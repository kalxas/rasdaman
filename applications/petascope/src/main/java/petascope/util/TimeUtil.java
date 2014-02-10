/*
 * This file is part of rasdaman community.
 *
 * Rasdaman community is free software: you can redistribute it and/or modify
 * it under the terms of the GNU  General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Rasdaman community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU  General Public License for more details.
 *
 * You should have received a copy of the GNU  General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003 - 2010 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.util;

import hirondelle.date4j.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that offers several utilities to handle timestamps formatting and elaborations.
 * Purpose: hide the library-specific details (DateTime) and let the client work with ISO strings.
 *
 * @author <a href="mailto:p.campalani@jacobs-university.de">Piero Campalani</a>
 */
public class TimeUtil {

    public static final String ISO8601_T_KEY = "T";
    public static final String TIME_ZONE_KEY = "Z";

    // http://aurora.regenstrief.org/~ucum/ucum.html#iso1000
    public static final String ISO_YEAR_UOM   = "a";
    public static final String ISO_MONTH_UOM  = "mo";
    public static final String ISO_WEEK_UOM   = "wk";
    public static final String ISO_DAY_UOM    = "d";
    public static final String ISO_HOUR_UOM   = "h";
    public static final String ISO_MINUTE_UOM = "min";
    public static final String ISO_SECOND_UOM = "s";

    private static final int DATETIME_YEAR_DEFAULT       = 1;
    private static final int DATETIME_MONTH_DEFAULT      = 1;
    private static final int DATETIME_DAY_DEFAULT        = 1;
    private static final int DATETIME_HOUR_DEFAULT       = 0;
    private static final int DATETIME_MINUTE_DEFAULT     = 0;
    private static final int DATETIME_SECOND_DEFAULT     = 0;
    private static final int DATETIME_NANOSECOND_DEFAULT = 0;

    @Deprecated private static final int DELTA_ANSI_MJD = 94188; // =(ANSI Day)-(Modified Julian Day)

    private static Logger log = LoggerFactory.getLogger(TimeUtil.class);

    /**
     * @param   timestamp    Timestamp string requested by the client
     * @return  boolean             True if valid ISO timestamp.
     */
    public static boolean isValidTimestamp (String timestamp) {

        timestamp = fix(timestamp);

        try {
            DateTime trial = new DateTime(timestamp);
            trial.lteq(trial); // whatever
        } catch (Exception ex) {
            log.debug(timestamp + " format is invalid or unsupported: " + ex.getMessage());
            return false;
        }

        return true;
    }

    /**
     * @param   timestampLo    timestamp
     * @param   timestampHi    timestamp
     * @return  boolean               Is Lo (*strictly*) lower than Hi ?
     */
    public static boolean isOrderedTimeSubset (String timestampLo, String timestampHi) {

        timestampLo = fix(timestampLo);
        timestampHi = fix(timestampHi);

        DateTime dtLo = new DateTime(timestampLo);
        DateTime dtHi = new DateTime(timestampHi);

        return dtLo.lteq(dtHi);
    }

    /**
     * @param   timestamp    A timestamp
     * @return  int                 ANSI day number.
     */
    @Deprecated
    public static int convert2AnsiDay (String timestamp) throws Exception {
        timestamp = fix(timestamp);
        DateTime dt = new DateTime(timestamp);
        return dt.getModifiedJulianDayNumber() + DELTA_ANSI_MJD;
    }

    /**
     * Remove apixes (-> more flexibility) and replace "T" ISO:8601 separator with a white space.
     * @param isoTimestamp   ISO:8601 timestamp (possibly quoted)
     * @return String        PSQL/DateTime compatible timestamp
     */
    private static String fix(String isoTimestamp) {

        // Replace T key with spaces, see: http://www.date4j.net/javadoc/hirondelle/date4j/DateTime.html
        String psqlTimestamp = isoTimestamp.replaceFirst(ISO8601_T_KEY, " ");

        // No time zone in the Date String: support of time zones is not out-of-the-box: future dev.
        psqlTimestamp = isoTimestamp.replaceFirst(TIME_ZONE_KEY, "");

        // Remove quotes
        psqlTimestamp = psqlTimestamp.replaceAll("%22", "");
        psqlTimestamp = psqlTimestamp.replaceAll("'"  , "");
        psqlTimestamp = psqlTimestamp.replaceAll("\""  , "");

        return psqlTimestamp;
    }

    /**
     * Count how many temporal units (i.e. offset vectors) fit inside a time interval.
     * @param timestampLo       Lower bound of time interval
     * @param timestampHi       Upper bound of time interval
     * @param timeResolution    Temporal resolution of the unit
     * @return How many *full* time units fit into the time interval (timestampHi-timestampLo).
     */
    public static Double countOffsets(String timestampLo, String timestampHi, String timeResolution) {

        int  sign = 1;

        // if Hi is /before/ Lo, than return 0
        if (isOrderedTimeSubset(timestampHi, timestampLo)) {
            // -> Lo>Hi: swap the interval bounds and return a negative offset
            String buf = timestampLo;
            timestampLo = timestampHi;
            timestampHi = buf;
            sign = -1;
        }

        Double offsetsCounter = .0;

        timestampLo = fix(timestampLo);
        timestampHi = fix(timestampHi);

        DateTime dtSubsetLo   = fillAllUnits(new DateTime(timestampLo));
        DateTime dtSubsetHi   = fillAllUnits(new DateTime(timestampHi));
        DateTime dtResolution = fillAllUnits(isoInterval2dateTime(timeResolution));

        // Start from lowerbound and increase until I exceed the upper bound
        DateTime dtHead = new DateTime(dtSubsetLo.toString());

        // Decompose resolution
        int yearRes   = (dtResolution.getYear()   == DATETIME_YEAR_DEFAULT )   ? 0 : (dtResolution.getYear()   - DATETIME_YEAR_DEFAULT);
        int monthRes  = (dtResolution.getMonth()  == DATETIME_MONTH_DEFAULT )  ? 0 : (dtResolution.getMonth()  - DATETIME_MONTH_DEFAULT);
        int dayRes    = (dtResolution.getDay()    == DATETIME_DAY_DEFAULT )    ? 0 : (dtResolution.getDay()    - DATETIME_DAY_DEFAULT);
        int hourRes   = (dtResolution.getHour()   == DATETIME_HOUR_DEFAULT )   ? 0 : (dtResolution.getHour()   - DATETIME_HOUR_DEFAULT);
        int minuteRes = (dtResolution.getMinute() == DATETIME_MINUTE_DEFAULT ) ? 0 : (dtResolution.getMinute() - DATETIME_MINUTE_DEFAULT);
        int secondRes = (dtResolution.getSecond() == DATETIME_SECOND_DEFAULT ) ? 0 : (dtResolution.getSecond() - DATETIME_SECOND_DEFAULT);

        DateTime dtLastHead = dtHead;
        dtHead = dtHead.plus(yearRes, monthRes, dayRes, hourRes, minuteRes, secondRes, DATETIME_NANOSECOND_DEFAULT, DateTime.DayOverflow.FirstDay);
        while (dtHead.lteq(dtSubsetHi)) {
            // Increment
            // FIXME: performance killer! Go with binary search.
            offsetsCounter += 1;
            dtLastHead = dtHead;
            dtHead = dtHead.plus(yearRes, monthRes, dayRes, hourRes, minuteRes, secondRes, DATETIME_NANOSECOND_DEFAULT, DateTime.DayOverflow.FirstDay);
            log.debug("dtHead#" + offsetsCounter + ": " + dtHead);
        }

        // Add fractional part to avoid integer resolution on temporal subsets and miss out-of-bound subsets
        offsetsCounter += 1 - (double)dtHead.numSecondsFrom(dtSubsetHi) / dtHead.numSecondsFrom(dtLastHead);

        return sign*offsetsCounter;
    }

    /**
     * @param   DateTime dt         DateTime object with incomplete units
     * @return  DateTime            DateTime object filled with defaults values.
     */
    private static DateTime fillAllUnits(DateTime dt) {
        DateTime dtOut = new DateTime(
                dt.unitsAllAbsent(DateTime.Unit.YEAR)   ? DATETIME_YEAR_DEFAULT   : dt.getYear(),
                dt.unitsAllAbsent(DateTime.Unit.MONTH)  ? DATETIME_MONTH_DEFAULT  : dt.getMonth(),
                dt.unitsAllAbsent(DateTime.Unit.DAY)    ? DATETIME_DAY_DEFAULT    : dt.getDay(),
                dt.unitsAllAbsent(DateTime.Unit.HOUR)   ? DATETIME_HOUR_DEFAULT   : dt.getHour(),
                dt.unitsAllAbsent(DateTime.Unit.MINUTE) ? DATETIME_MINUTE_DEFAULT : dt.getMinute(),
                dt.unitsAllAbsent(DateTime.Unit.SECOND) ? DATETIME_SECOND_DEFAULT : dt.getSecond(),
                dt.unitsAllAbsent(DateTime.Unit.NANOSECONDS) ? DATETIME_NANOSECOND_DEFAULT : dt.getNanoseconds());
        return dtOut;
    }

    /**
     * @param   String isoInterval    ISO8601 interval string to be translated
     * @return  DateTime              Corresponding DateTime object
     */
    private static DateTime isoInterval2dateTime(String isoInterval) {
        // NOTE: e.g. resolution of 12 month must be set as 1 year, otherwise overflow when creating the DateTime object.
        DateTime  dt;
        Integer iBuf;
        String  sBuf = "";
        char  keyBuf = ' ';

        // Init
        int year       = 0;
        int month      = 0;
        int day        = 0;
        int hour       = 0;
        int minute     = 0;
        int second     = 0;

        // Transalte ISO resolution
        if      (isoInterval.equals(ISO_YEAR_UOM))   year   = 1;
        else if (isoInterval.equals(ISO_MONTH_UOM))  month  = 1;
        else if (isoInterval.equals(ISO_WEEK_UOM))   day    = 7;
        else if (isoInterval.equals(ISO_DAY_UOM))    day    = 1;
        else if (isoInterval.equals(ISO_HOUR_UOM))   hour   = 1;
        else if (isoInterval.equals(ISO_MINUTE_UOM)) minute = 1;
        else if (isoInterval.equals(ISO_SECOND_UOM)) second = 1;
        else {
            log.error(isoInterval + " is unknown.");
            return null;
        }

        // Create output object
        dt = new DateTime(
                year   + DATETIME_YEAR_DEFAULT,
                month  + DATETIME_MONTH_DEFAULT,
                day    + DATETIME_DAY_DEFAULT,
                hour   + DATETIME_HOUR_DEFAULT,
                minute + DATETIME_MINUTE_DEFAULT,
                second + DATETIME_SECOND_DEFAULT,
                DATETIME_NANOSECOND_DEFAULT);

        return dt;
    }

    /**
     * @param timestamp1  ISO:8601 timestamp
     * @param timestamp2  ISO:8601 timestamp
     * @return String     The minimum value.
     */
    public static String min(String timestamp1, String timestamp2) {
        if (isOrderedTimeSubset(timestamp1, timestamp2))
             return timestamp1;
        else return timestamp2;
    }

    /**
     * @param timestamp1  ISO:8601 timestamp
     * @param timestamp2  ISO:8601 timestamp
     * @return String     The maximum value.
     */
    public static String max(String timestamp1, String timestamp2) {
        if (!isOrderedTimeSubset(timestamp1, timestamp2))
             return timestamp1;
        else return timestamp2;
    }

    /**
     * When translating to cell indexes, there is a particular case:
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
     * @param timestamp
     * @return String     1 nanosecond before timestamp.
     */
    public static String minusEpsilon(String timestamp) {

        timestamp   = fix(timestamp);
        DateTime dt = fillAllUnits(new DateTime(timestamp));

        dt = dt.minus(0, 0, 0, 0, 0, 0, 1, DateTime.DayOverflow.LastDay);

        return dt.toString();
    }
}
