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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petascope.core.Pair;

/**
 * Utilities for handling BigDecimals computations and scales.
 * @author <a href="mailto:p.campalani@jacobs-university.de">Piero Campalani</a>
 */
public class BigDecimalUtil {
    
     // To support calculation for TimeAxis (double) before and now using BigDecimal for wcst_import
    // we also add a very small acceptable epsilon to avoid case when coefficient does not exist
    // e.g: coeffcient (double) in database is 0.0, calculated coefficient (BigDecimal) is 0.0000000001
    public static final BigDecimal COEFFICIENT_DECIMAL_EPSILON = new BigDecimal("0.0000000001");    
    // The resolution from files can be different but only within the [axis resolution - epsilon, axis resolution + epsilon]
    public static final BigDecimal AXIS_RESOLUTION_EPSILION = new BigDecimal("0.00001");

    // The total digit numbers of BigDecimal number (before and after the ".")
    public static final int MAX_PRECISION = 128;
    // The total digit numbers of BigDecimal number (after the ".")
    public static final int MAX_SCALE = 64;
    
    // e.g: 10.66666666666666667 and 10.666666666666666666666666666666 are equal 
    public static final int MIN_SCALE_TO_CHECK_EPSILON = 10;
    
    private static final Logger log = LoggerFactory.getLogger(BigDecimalUtil.class);
    
    /**
     * Scale of a quotient between two BigDecimals.
     *
     * Java API on BigDecimal:
     *   Immutable, arbitrary-precision signed decimal numbers.
     *   A BigDecimal consists of an arbitrary precision integer unscaled value and a 32-bit integer scale.
     *   If zero or positive, the scale is the number of digits to the right of the decimal point.
     *   If negative, the unscaled value of the number is multiplied by ten to the power of the negation of the scale.
     *   The value of the number represented by the BigDecimal is therefore (unscaledValue × 10-scale).
     */
    private static final int DIVISION_SCALE = 50;

    /**
     * Converts BigDecimal value minimum scale equivalent representation.
     * Java bug #6480539 is taken into account.
     * @param bd
     * @return bd where decimal zeros have been stripped.
     */
    public static BigDecimal stripDecimalZeros(BigDecimal bd) {
        BigDecimal bdOut = bd.stripTrailingZeros();
        try {
            if (bdOut.scale() < 0) {
                bdOut = new BigDecimal(bdOut.longValueExact());
            }
            if (bdOut.compareTo(BigDecimal.ZERO) == 0) {
                bdOut = BigDecimal.ZERO;
            }
        } catch (ArithmeticException ex) {
            log.trace(bd + " exceeds the capacity of long integers: leaving its own representation.");
        }
        return bdOut;
    }
    
    /**
     * e.: 1 to 1.0
     */
    public static BigDecimal addTrailingZero(BigDecimal bigDecimal) {
        if (!bigDecimal.toPlainString().contains(".")) {
            return new BigDecimal(bigDecimal.toPlainString() + ".0");
        }
        
        return bigDecimal;
    }

    /**
     * Computes the division between two BigDecimals with the specified max significant figures.
     * Setting a scale cuts the precision of the quotient to that number of significant figures
     * (negligible when high scale value), but avoids dangerous truncations which are dynamically
     * set depending on the scale of divisor and dividend.
     * Setting no RoundingMode on the other hand can throw exception since exact precision is assumed
     * but sometimes cannot be achieved (eg 1/3).
     * @param dividend
     * @param divisor
     * @return Zeros-stripped quotient of dividend/divisor division.
     */
    public static BigDecimal divide(BigDecimal dividend, BigDecimal divisor) {
        BigDecimal quotient = dividend.divide(divisor, DIVISION_SCALE, RoundingMode.UP);
        return BigDecimalUtil.stripDecimalZeros(quotient);
    }

    /**
     * Computes the multiply between two BigDecimals number
     * @param numberA
     * @param numberB
     * @return
     */
    public static BigDecimal multiple(BigDecimal numberA, BigDecimal numberB) {
        BigDecimal result = numberA.multiply(numberB);
        return BigDecimalUtil.stripDecimalZeros(result);
    }

    /**
     * Check if a big decimal value is inside the list
     * NOTE: we use a very small epsilon to add, subtract to the checking value
     * as coefficients which was persisted in double is not as same as in BigDecimal
     * especially for time axis:
     * Double:     148654.0842592477
     * BigDecimal: 148654.08425924768518518518518518518518518518518518518519        
     * @param list
     * @param value
     * @return 
     */
    public static int listContainsCoefficient(List<BigDecimal> list, BigDecimal value) {
        int counter = 0;
        for (BigDecimal coeff : list) {
            // if value is within [coefficient - epsilon, coefficient + epsilon], then value is considered the coefficient
            // e.g: 
            if ((coeff.subtract(COEFFICIENT_DECIMAL_EPSILON).compareTo(value) <= 0)
                &&(coeff.add(COEFFICIENT_DECIMAL_EPSILON).compareTo(value) >= 0)) {
                return counter;
            }            
            counter++;
        }
        return -1;
    }

    /**
     * Convert list of String to list of BigDecimal values
     * @param values
     * @return 
     */
    public static List<BigDecimal> convertListString(List<String> values) {
        List<BigDecimal> convertedValues = new ArrayList<>();
        for (String value : values) {
            convertedValues.add(new BigDecimal(value));
        }
        
        return convertedValues;
    }
    
    /**
     * Check if a big decimal is an integer value (e.g: 2.00000000000000, 2)
     */
    public static boolean integer(BigDecimal number) {
        return number.signum() == 0 || number.scale() <= 0 || number.stripTrailingZeros().scale() <= 0;
    }
    
    /**
     * From a BigDecimal, return a rounded up to 3 decimal places.
     */
    public static String roundUp3DecimalPlaces(BigDecimal number) {

        // e.g: 3.0 -> 3
        if (number.stripTrailingZeros().scale() <= 0) {
            return stripDecimalZeros(number).toPlainString();
        }
        
        // e.g: 3.23456 -> 3.234
        String result = stripDecimalZeros(number.setScale(3, BigDecimal.ROUND_HALF_EVEN)).toPlainString();
        
        return result;
    }
    
    /**
     * Swap values of two inputs
     */
    public static Pair<BigDecimal, BigDecimal> swap(BigDecimal firstValue, BigDecimal secondValue) {
        BigDecimal temp = secondValue;
        secondValue = firstValue;
        firstValue = temp;
        
        return new Pair<>(firstValue, secondValue);
    }
    
    
    /**
     * Check if number A >= number B
     */
    public static boolean greaterThanOrEqual(BigDecimal firstValue, BigDecimal secondValue) {        
        return firstValue.compareTo(secondValue.subtract(COEFFICIENT_DECIMAL_EPSILON)) >= 0;
    }
    
    public static boolean greaterThan(BigDecimal firstValue, BigDecimal secondValue) {        
        return firstValue.compareTo(secondValue.subtract(COEFFICIENT_DECIMAL_EPSILON)) > 0;
    }
    
    /**
     * Check if number A <= number B
     */
    public static boolean smallerThanOrEqual(BigDecimal firstValue, BigDecimal secondValue) {        
        return firstValue.compareTo(secondValue.add(COEFFICIENT_DECIMAL_EPSILON)) <= 0;
    }
    
    public static boolean smallerThan(BigDecimal firstValue, BigDecimal secondValue) {        
        return firstValue.compareTo(secondValue.add(COEFFICIENT_DECIMAL_EPSILON)) < 0;
    }
   
    public static boolean isNumber(String value) {
        try {
            new BigDecimal(value);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
    
    public static List<String> toStringList(List<BigDecimal> list) {
        List<String> results = new ArrayList<>();
        
        for (BigDecimal value : list) {
            results.add(BigDecimalUtil.stripDecimalZeros(value).toPlainString());
        }
        
        return results;
    }
    
    /**
     * e.g: 9.5 -> 9_5
     */
    public static String replacePointByUnderscore(BigDecimal number) {
        String result = BigDecimalUtil.stripDecimalZeros(number).toPlainString().replace(".", "_");
        return result;
    }
    
    /**
     * Return the product of all numbers
     */
    public static BigDecimal calculateProduct(List<BigDecimal> list) {
        BigDecimal result = BigDecimal.ONE;
        
        for (BigDecimal value : list) {
            result = result.multiply(value);
        }
        
        return result;
    }
    
    /**
     * Divide first by second and round up the result as long value
     */
    public static long divideToLong(BigDecimal first, BigDecimal second) {
        long result = BigDecimalUtil.divide(first, second).longValue();
        return result;
    }
    
    public static long multipleToLong(BigDecimal first, BigDecimal second) {
        long result = first.multiply(second).longValue();
        return result;
    }
    
    public static class BigDecimalComparator implements Comparator<BigDecimal> {

        @Override
        public int compare(BigDecimal firstValue, BigDecimal secondValue) {
            if (firstValue.compareTo(secondValue.subtract(COEFFICIENT_DECIMAL_EPSILON)) < 0) {
                return -1;
            } else if (firstValue.compareTo(secondValue.add(COEFFICIENT_DECIMAL_EPSILON)) > 0) {
                return 1;
            }

            return 0;
        }

    }

}
