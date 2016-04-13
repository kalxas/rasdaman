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

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;

/**
 * Some utilities for nD vector algebra.
 * Useful for handling offset-vectors and non-aligned operations in the CRS geometric space.
 *
 * @author <a href="mailto:p.campalani@jacobs-university.de">Piero Campalani</a>
 */
public class Vectors {

    private static final Logger log = LoggerFactory.getLogger(Vectors.class);

    // Methods
    /**
     * Calculates the dot-product of two vectors.
     * It is zero for orthogonal vectors.
     *
     * @param <T> The generic numeric type (no primitives)
     * @param v1  First vector
     * @param v2  Second vector
     * @return The sum of the products of the corresponding entries.
     */
    public static <T extends Number> Number dotProduct(T[] v1, T[] v2) throws PetascopeException {

        // check dimensions
        if (v1.length != v2.length) {
            throw new PetascopeException(ExceptionCode.InternalComponentError,
                    "Trying to compute a dot-product on vector of different dimensions.");
        }
        // check same class
        if (!v1.getClass().getName().equals(v2.getClass().getName())) {
            throw new PetascopeException(ExceptionCode.InternalComponentError,
                    "Trying to compute a dot-product on vector of different numeric types.");
        }

        Number dotProduct = v1.getClass().equals(BigDecimal.class) ? new BigDecimal(0) : new Double(0D);

        for (int i=0; i<v1.length; i++) {
            if (v1.getClass().equals(BigDecimal.class)) {
                dotProduct = ((BigDecimal)dotProduct).add((BigDecimal)v1[i]).multiply((BigDecimal)v2[i]);
            } else {
                dotProduct = dotProduct.doubleValue() + v1[i].doubleValue() * v2[i].doubleValue();
            }
        }

        log.debug("dot-product between " + toString(v1) + " and " + toString(v2) + " is: " + dotProduct);
        return dotProduct;
    }

    /**
     * Calculates the scalar multiplication of a vector by a scalar.
     * T is BigDecimal for either both or none (cast error otherwise).
     * @param <T> The generic numeric type (no primitives)
     * @param s  The scalar
     * @param v  The vector
     * @return The same vector with magnitude multiplied by the scalar factor.
     */
    public static <T extends Number> Number[] scalarMultiplication(T s, T[] v) {
        // init
        Number[] vOut = s.getClass().equals(BigDecimal.class) ? new BigDecimal[v.length] : new Double[v.length];

        // mulitply each component
        for (int i=0; i<v.length; i++) {
            if (s.getClass().equals(BigDecimal.class)) {
                // Force explicitly to 0 (if v[i]==0) otherwise 0*<non-zero scalenumber> = "0.0"
                vOut[i] = ((BigDecimal)v[i]).doubleValue() == 0 ? BigDecimal.ZERO : ((BigDecimal)v[i]).multiply((BigDecimal)s);
            } else {
                vOut[i] = v[i].doubleValue() * s.doubleValue();
            }
        }

        log.debug("scalar-multiplication of " + toString(v) + " by " + s + " is: " + toString(vOut));
        return vOut;
    }

    /**
     * Verifies if 2 or more vectors are pairwise orthogonal.
     *
     * @param <T>     The generic numeric type (no primitives)
     * @param vectors A sequence of generic vectors
     * @return True if the set of vectors form an orthogonal set.
     * @throws PetascopeException
     */
    public static <T extends Number> boolean areOrthogonal(List<List<T>> vectors) throws PetascopeException {

        // At least 2 vectors
        if (vectors.size() < 2) {
            log.warn("Evaluating orthogonality of less than 2 vectors.");
            return true;
        }

        int i; // to step through the args
        int j; //

        // pair-wise check on each combination of input vectors
        for (i=0; i<vectors.size()-1; i++) {
            for (j=i+1; j<vectors.size(); j++) {

                // List to array
                Number[] v1 = vectors.get(i).get(0).getClass().equals(BigDecimal.class)
                        ? ((ArrayList<BigDecimal>)vectors.get(i)).toArray(new BigDecimal[vectors.get(i).size()])
                        :     ((ArrayList<Double>)vectors.get(i)).toArray(new Double[vectors.get(i).size()]);
                Number[] v2 = vectors.get(j).get(0).getClass().equals(BigDecimal.class)
                        ? ((ArrayList<BigDecimal>)vectors.get(j)).toArray(new BigDecimal[vectors.get(j).size()])
                        :     ((ArrayList<Double>)vectors.get(j)).toArray(new Double[vectors.get(j).size()]);

                if (dotProduct(v1, v2).doubleValue() != 0) {
                    log.debug("Vectors " + toString(v1) + " and " + toString(v2) + " are not orthogonal.");
                    return false;
                }
            }
        }
        // None of the pairwise dot-products was != 0, the vectors form an orthogonal set
        return true;
    }

    /**
     * Returns the indices of non-zero components in the input vector.
     * If more there are more than 1 non-zero components, than the vector
     * is not aligned to an axis of the geometric space (CRS).
     *
     * @param <T> The generic numeric type (no primitives)
     * @param v   The vector
     * @return The list of indices of non-zero components in v.
     */
    public static <T extends Number> List<Integer> nonZeroComponentsIndices(T[] v) {
        // init
        List<T> vList = Arrays.asList(v);
        List<Integer> indices = new ArrayList<Integer>();

        // loop through the components of the vector
        for (T vEl : vList) {
            if (vEl.doubleValue() != 0) {
                indices.add(vList.indexOf(vEl));
            }
        }

        return indices;
    }

    /**
     * Build a nD unit vector along a specified axis (eg [0,0,_,0,1,0,_,0])
     * @param dimension The dimensionality of the vector
     * @param unitIndex The position of the non-zero component (first is 0)
     * @return An array of `dimension` components, with `unitIndex` component equal to 1 (0 otherwise).
     * @throws PetascopeException
     */
    public static BigDecimal[] unitVector(int dimension, int unitIndex) throws PetascopeException {

        // check consistency
        if (unitIndex >= dimension) {
            throw new PetascopeException(ExceptionCode.InternalComponentError,
                    "Trying to create a " + dimension + "D unit-vector with " + unitIndex + " as non-zero component.");
        }

        BigDecimal[] unitVector = new BigDecimal[dimension];

        // build the unit vector
        for (int i=0; i<dimension; i++) {
            unitVector[i] = (i==unitIndex) ? BigDecimal.ONE : BigDecimal.ZERO;
        }

        return unitVector;
    }

    /**
     * Readable visualization of vector components.
     *
     * @param <T> The generic numeric type (no primitives)
     * @param v   The vector
     * @return Vector components, enclosed by square brackets and separated by commas.
     */
    public static <T extends Number> String toString(T[] v) {
        List<T> vList = Arrays.asList(v);
        return vList.toString();
    }

    /**
     * Add a scalar to each component of a numeric vector.
     *
     * @param <T>  The generic numeric type (no primitives)
     * @param a    The numeric vector
     * @param b    The scalar
     * @return [a0+b,a1+b,__,aN+b]
     */
    public static <T extends Number> T[] add(T[] a, T b) {
        // init
        Number[] vOut = new Number[a.length];

        for (int i=0; i<a.length; i++) {
            if (b.getClass().equals(BigDecimal.class)) {
                vOut[i] = ((BigDecimal)a[i]).add((BigDecimal)b);
            } else {
                vOut[i] = a[i].doubleValue() + b.doubleValue();
            }
        }

        return (T[])vOut;
    }
    // Overload for lists
    public static <T extends Number> List<T> add(List<T> a, T b) {
        Number[] aa = a.toArray((T[]) Array.newInstance(b.getClass(),0));
        List<Number> aaa = Arrays.asList(aa);
        Number[] aaaa =  add((T[])aaa.toArray(),b);
        return Arrays.asList((T[])aaaa);
        //return Arrays.asList((add((T[])a.toArray(), b));
    }
}
