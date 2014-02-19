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
 * Copyright 2003 - 2014 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.util;

import java.util.ArrayList;
import java.util.List;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import petascope.exceptions.PetascopeException;

/**
 * Test class for Time-related utilities.
 * Target is petascope.util.TimeUtil class, in particular its countOffsets() method.
 *
 * @author <a href="mailto:p.campalani@jacobs-university.de">Piero Campalani</a>
 */
public class TimeUtilTest {

    /**
     * Triple of i) label of the test -- included in case of error, ii) TimeUtil.countOffsets() inputs, iii) expected output}.
     */
    private List<Triple<String,countOffsetInput,Double>> assertsInOut;

    public TimeUtilTest() {}

    @Before
    public void setUpData() {

        assertsInOut = new ArrayList<Triple<String,countOffsetInput,Double>>();

        // NOTE: Z time zone = +00
        // Omitting time zone goes to default..

        // fill the input/expected-output
        // 1) "Different resolutions"
        //    2014-01-01 + 4*(5 days) = 2014-01-21
        assertsInOut.add(Triple.of(
                "Different-resolutions test",
                new countOffsetInput("2014-01-01T00:00:00.000", "2014-01-11", TimeUtil.UCUM_DAY, 5D),
                Double.valueOf(2D)));
        // 2) "Year only"
        //    2014-01-01 + 4*(5 days) = 2014-01-21
        assertsInOut.add(Triple.of(
                "Year-only test",
                new countOffsetInput("2014", "2014-01-11", TimeUtil.UCUM_DAY, 5D),
                Double.valueOf(2D)));
        // 3) "Time Zones"
        //    2014-01-01T12:45Z + 2*(3 hours) = 2014-01-01T12:45:00-06
        assertsInOut.add(Triple.of(
                "Time-zones test",
                new countOffsetInput("2014-01-01T12:45Z", "2014-01-01T12:45:00-06", TimeUtil.UCUM_HOUR, 3D),
                Double.valueOf(2D)));
        // 4) "Fractional offset vector"
        //    2014-01-01T00:00:00 + 2*(25.5 seconds) = 2014-01-01T00:00:51
        assertsInOut.add(Triple.of(
                "Fractional offset vector",
                new countOffsetInput("2014-01-01T00:00:00", "2014-01-01T00:00:51", TimeUtil.UCUM_SECOND, 25.5),
                Double.valueOf(2D)));
        // 5) "Fractional time coordinate"
        //    2014-01-01 + 2.4*(10 hours) = 2014-01-02
        // NOTE: 2014-01-01 is turned to absolute instant 2014-01-01T00:00:00.000.
        assertsInOut.add(Triple.of(
                "Fractional time coordinate",
                new countOffsetInput("2014-01-01", "2014-01-02", TimeUtil.UCUM_HOUR, 10D),
                Double.valueOf(2.4)));
        // 6) "Milliseconds test"
        //    2014-01-01T00:00:00.014 + 2*(500 milliseconds) = 2014-01-01T00:00:01.014
        assertsInOut.add(Triple.of(
                "Milliseconds test",
                new countOffsetInput("2014-01-01T00:00:00.014", "2014-01-01T00:00:01.014", TimeUtil.UCUM_MILLIS, 500D),
                Double.valueOf(2D)));
        // 7) "Seconds test"
        //    2014-01-01T00:00:00.014 + 2*(8 seconds) = 2014-01-01T00:00:16.014
        assertsInOut.add(Triple.of(
                "Seconds test",
                new countOffsetInput("2014-01-01T00:00:00.014", "2014-01-01T00:00:16.014", TimeUtil.UCUM_SECOND, 8D),
                Double.valueOf(2D)));
        // 8) "Minutes test"
        //    2014-01-01T00:57:00.014 + 2*(1 minute) = 2014-01-01T00:59:00.014
        assertsInOut.add(Triple.of(
                "Minutes test",
                new countOffsetInput("2014-01-01T00:57:00.014", "2014-01-01T00:59:00.014", TimeUtil.UCUM_MINUTE, 1D),
                Double.valueOf(2D)));
        // 9) "Hours test"
        //    2014-01-01T10:20 + 2*(4 hours) = 2014-01-01T18:20
        assertsInOut.add(Triple.of(
                "Hours test",
                new countOffsetInput("2014-01-01T10:20", "2014-01-01T18:20", TimeUtil.UCUM_HOUR, 4D),
                Double.valueOf(2D)));
        // 10) "Days test (with leap)"
        //    2012-01-11T10:20 + 2*(25 days) = 2012-03-01T10:20  ((2012 is leap year))
        assertsInOut.add(Triple.of(
                "Days test (with leap)",
                new countOffsetInput("2012-01-11T10:20", "2012-03-01T10:20", TimeUtil.UCUM_DAY, 25D),
                Double.valueOf(2D)));
        // 11) "Weeks test (future)"
        //    3012-01-11 + 2*(2 weeks) = 3012-02-08
        assertsInOut.add(Triple.of(
                "Weeks test (future)",
                new countOffsetInput("3012-01-11", "3012-02-08", TimeUtil.UCUM_WEEK, 2D),
                Double.valueOf(2D)));
        // 12) "Mean Julian month test"
        //    i. D1 = date -d '2014-03-20 21:50 Z' +%s             = 1395352200 seconds from Unix epoch
        //   ii. D2 = echo $(( 1395352200 + 2629800 ))             = 1397982000 seconds from Unix epoch = D1 + (1 mo_j)
        //          = date -d @1397982000 +"%Y-%m-%d %R:%S %:::z" = 2014-04-20 10:20:00 +02
        assertsInOut.add(Triple.of(
                "Mean Julian month test",
                new countOffsetInput("2014-03-20T21:50", "2014-04-20T10:20+02", TimeUtil.UCUM_MEAN_JULIAN_MONTH, 1D),
                Double.valueOf(1D)));
        // 13) "Mean Gregorian month test"
        //    i. D1 = date -d '2014-03-01 Z' +%s  = 1393632000 seconds from Unix epoch
        //   ii. D2 = date -d '2014-04-01 Z' +%s  = 1396310400 seconds from Unix epoch
        //  iii. (1396310400-1393632000)/2629746 = (seconds/seconds_greg_month) = 1.0171324530962307 [mo_g]
        assertsInOut.add(Triple.of(
                "Mean Gregorian month test",
                new countOffsetInput("2014-03-01", "2014-04-01", TimeUtil.UCUM_MEAN_GREGORIAN_MONTH, 1D),
                Double.valueOf(1.0185014065997249)));
        // 14) "Synodal month test"
        //    i. D1 = date -d '2014-03-20 21:50 Z' +%s                   = 1395352200.000 seconds from Unix epoch
        //   ii. D2 = (1395352200000 + 2551442976)/1000
        //          = (seconds_D1 + seconds_synodal_month)               = 1397903642.976 seconds from Unix epoch
        //          = date -d @1397903642.976 +"%Y-%m-%d %R:%S.%N %:::z" = 2014-04-19 12:34:02.976000000 +02
        assertsInOut.add(Triple.of(
                "Synodal month test",
                new countOffsetInput("2014-03-20T21:50:00.000", "2014-04-19T10:34:02.976", TimeUtil.UCUM_SYNODAL_MONTH, 1D),
                Double.valueOf(1D)));
        // 15) "Month test" (month = meanJulian month = 365.25)
        assertsInOut.add(Triple.of(
                "Month test",
                new countOffsetInput("2014", "2018", TimeUtil.UCUM_MONTH, 12D),
                Double.valueOf(4D)));
        // 16) "Mean Julian year test"
        //    2014-06 + 1*(4*365.25 days) = 2018-06
        assertsInOut.add(Triple.of(
                "Mean Julian year test",
                new countOffsetInput("2014-06", "2018-06", TimeUtil.UCUM_MEAN_JULIAN_YEAR, 1D),
                Double.valueOf(4D)));
        // 17) "Mean Gregorian year test"
        //    2000 + 4*(100*365.2425 days) = 2400
        assertsInOut.add(Triple.of(
                "Mean Gregorian year test",
                new countOffsetInput("2000", "2400", TimeUtil.UCUM_MEAN_GREGORIAN_YEAR, 100D),
                Double.valueOf(4D)));
        // 18) "Tropical year test"
        //    (milliseconds_2001)/(miiliseconds tropical year) = (365*24*60*60*1000)/31556925216 = 0.9993369057391754
        assertsInOut.add(Triple.of(
                "Tropical year test",
                new countOffsetInput("2001", "2002", TimeUtil.UCUM_TROPICAL_YEAR, 1D),
                Double.valueOf(0.9993369057391754)));
        // 19) "Year test" (year = mean Julian year)
        //
        assertsInOut.add(Triple.of(
                "Year test",
                new countOffsetInput("2001", "2005", TimeUtil.UCUM_YEAR, 2D),
                Double.valueOf(2D)));
    }

    /**
     * Test countOffsets() method.
     * It computes the vector-normalized distance between two instants along a time dimension.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testCountOffsets() throws Exception {

        Double givenOffset;

        for (Triple<String,countOffsetInput,Double> input : assertsInOut) {
            // lo < hi
            givenOffset = TimeUtil.countOffsets(input.snd.timestampLo, input.snd.timestampHi, input.snd.timeUom, input.snd.timeVector);
            assertEquals(input.fst, input.trd, givenOffset);
            // lo > hi : negative offsets
            givenOffset = TimeUtil.countOffsets(input.snd.timestampHi, input.snd.timestampLo, input.snd.timeUom, input.snd.timeVector);
            assertEquals(input.fst, new Double((-1.0)*input.trd), givenOffset);
        }
    }

    /**
     * Test plus() method.
     * It computes the vector-normalized distance between two instants along a time dimension.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testPlus() throws Exception {

        String givenTimestamp;
        DateTimeFormatter isoFmt = ISODateTimeFormat.dateOptionalTimeParser().withZoneUTC();
        DateTime dtExcepted;
        DateTime dtGiven;

        for (Triple<String,countOffsetInput,Double> input : assertsInOut) {

            // lo < hi
            givenTimestamp = TimeUtil.plus(input.snd.timestampLo, input.trd*input.snd.timeVector, input.snd.timeUom);
            // need to check equivalence of instants
            dtExcepted = isoFmt.parseDateTime(input.snd.timestampHi);
            dtGiven    = isoFmt.parseDateTime(givenTimestamp);
            // assert duration between two instants is zero
            assertEquals(input.fst, (new Duration(dtGiven, dtExcepted)).getMillis(), 0L);

            // lo > hi : negative offsets
            givenTimestamp = TimeUtil.plus(input.snd.timestampHi, (-1.0)*input.trd*input.snd.timeVector, input.snd.timeUom);
            // need to check equivalence of instants
            dtExcepted = isoFmt.parseDateTime(input.snd.timestampLo);
            dtGiven    = isoFmt.parseDateTime(givenTimestamp);
            // assert duration between two instants is zero
            assertEquals(input.fst, (new Duration(dtGiven, dtExcepted)).getMillis(), 0L);
        }
    }

    /**
     * Test handling of overflow arithmetic exception on wide interval/resolution.
     * Note: Year must be in the range [-292275054,292278993].
     * @throws Exception
     */
    @Test(expected=PetascopeException.class)
    public void testOverflowException() throws Exception {
        TimeUtil.countOffsets("1000", "292278993", TimeUtil.UCUM_MILLIS, 1D);
        assertTrue(true);
    }


    /**
     * Validity check on input time subsets.
     * format follows the ISO standard, with compulsory date and optional time.
     * @throws Exception
     */
    @Test
    public void testIsValidTimestamp() throws Exception {
        for (Triple<String,countOffsetInput,Double> input : assertsInOut) {
            assertTrue( input.fst, TimeUtil.isValidTimestamp(input.snd.timestampLo));
            assertTrue( input.fst, TimeUtil.isValidTimestamp(input.snd.timestampHi));
        }
    }

    /**
     * Test that all correct WCS subsets are accepted, and all wrong are rejected.
     * A subset is wrong when lower bound is strictly higher than the upper bound.
     * @throws Exception
     */
    @Test
    public void testIsOrderedTimeSubset() throws Exception {
        for (Triple<String,countOffsetInput,Double> input : assertsInOut) {
            // lo < hi
            assertTrue( input.fst, TimeUtil.isOrderedTimeSubset(input.snd.timestampLo, input.snd.timestampHi));
            // lo = hi
            assertTrue( input.fst, TimeUtil.isOrderedTimeSubset(input.snd.timestampLo, input.snd.timestampLo));
            // lo > hi
            assertFalse(input.fst, TimeUtil.isOrderedTimeSubset(input.snd.timestampHi, input.snd.timestampLo));
        }
    }

    /**
     * Holds the set of inputs for TimeUtil.countOffsets()
     */
    private class countOffsetInput {
        String timestampLo;
        String timestampHi;
        String timeUom;
        Double timeVector;

        countOffsetInput(String tLo, String tHi, String tUom, Double tVector) {
            timestampLo = tLo;
            timestampHi = tHi;
            timeUom = tUom;
            timeVector = tVector;
        }
    }
}
