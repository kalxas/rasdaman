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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author <a href="mailto:p.campalani@jacobs-university.de">Piero Campalani</a>
 */
public class BigDecimalUtilTest {

    /**
     * Triple of i) label of the test -- included in case of error, ii) BigDecimalUtil.divide() inputs, iii) expected output}.
     */
    private List<Triple<String,bdDivideInputs,BigDecimal>> assertEqualsInOut;

    public BigDecimalUtilTest() {}

    @Before
    public void setUp() {

        // init
        assertEqualsInOut = new ArrayList<Triple<String,bdDivideInputs,BigDecimal>>();

        // asserts
        assertEqualsInOut.add(Triple.of(
                "integer/integer = integer",
                new bdDivideInputs(BigDecimal.valueOf(3), BigDecimal.valueOf(1)),
                BigDecimal.valueOf(3)));
        assertEqualsInOut.add(Triple.of(
                "integer/integer = decimal",
                new bdDivideInputs(BigDecimal.valueOf(3), BigDecimal.valueOf(2)),
                BigDecimal.valueOf(1.5)));
        assertEqualsInOut.add(Triple.of(
                "integer/decimal = integer",
                new bdDivideInputs(BigDecimal.valueOf(3), BigDecimal.valueOf(1.5)),
                BigDecimal.valueOf(2)));
                assertEqualsInOut.add(Triple.of(
                "integer/decimal = decimal",
                new bdDivideInputs(BigDecimal.valueOf(3), BigDecimal.valueOf(2)),
                BigDecimal.valueOf(1.5)));
        assertEqualsInOut.add(Triple.of(
                "decimal/decimal = decimal",
                new bdDivideInputs(BigDecimal.valueOf(3.46), BigDecimal.valueOf(1.73)),
                BigDecimal.valueOf(2)));
        assertEqualsInOut.add(Triple.of(
                "decimal/decimal = integer",
                new bdDivideInputs(BigDecimal.valueOf(3.6), BigDecimal.valueOf(0.5)),
                BigDecimal.valueOf(7.2)));
        assertEqualsInOut.add(Triple.of(
                "ZERO/decimal",
                new bdDivideInputs(BigDecimal.ZERO, BigDecimal.valueOf(0.12345678987654321)),
                BigDecimal.ZERO));
    }

    /**
     * Test BigDecimal division utility.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testDivide() throws Exception {

        BigDecimal givenQuotient;

        for (Triple<String,bdDivideInputs,BigDecimal> input : assertEqualsInOut) {
            // lo < hi
            givenQuotient = BigDecimalUtil.divide(input.snd.dividend, input.snd.divisor);
            assertEquals(input.fst, input.trd, givenQuotient);
        }
    }

    /**
     * Holds the set of inputs for BigDecimalUtil.divide()
     */
    private class bdDivideInputs {
        BigDecimal dividend;
        BigDecimal divisor;

        bdDivideInputs(BigDecimal bdDividend, BigDecimal bdDivisor) {
            dividend = bdDividend;
            divisor = bdDivisor;
        }
    }
}
