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
package petascope.wcs2.wcst;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import petascope.core.CoverageMetadata;
import petascope.exceptions.WCPSException;
import petascope.exceptions.wcst.WCSTRangeFieldNameMismatchException;
import petascope.exceptions.wcst.WCSTRangeFieldNumberMismatchException;
import petascope.util.WcpsConstants;
import petascope.wcps.server.core.RangeElement;
import petascope.wcs2.handlers.wcst.UpdateCoverageValidator;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Tests the validation of rangeType for coverages having varying numbers of bands.
 *
 * @author <a href="merticariu@rasdaman.com">Vlad Merticariu</a>
 */
public class RangeTypeValidatorTest {

    private CoverageMetadata inputCoverage;
    private CoverageMetadata existingCoverage;

    private List<RangeElement> inputCoverageBandList;
    private List<RangeElement> existingCoverageBandList;

    private void populateBandListWithSameBands(int noOfBands) {
        inputCoverageBandList = new ArrayList<RangeElement>();
        existingCoverageBandList = new ArrayList<RangeElement>();
        for (int i = 0; i < noOfBands; i++) {
            try {
                RangeElement rangeElement = new RangeElement(RandomStringUtils.random(10), WcpsConstants.MSG_BOOLEAN, RandomStringUtils.random(2));
                inputCoverageBandList.add(rangeElement);
                existingCoverageBandList.add(rangeElement);
            } catch (WCPSException e) {
                e.printStackTrace();
            }
        }
    }

    private void populateBandListWithDifferentBandNames(int noOfBands) {
        inputCoverageBandList = new ArrayList<RangeElement>();
        existingCoverageBandList = new ArrayList<RangeElement>();
        for (int i = 0; i < noOfBands; i++) {
            try {
                RangeElement rangeElement = new RangeElement(RandomStringUtils.random(10), WcpsConstants.MSG_BOOLEAN, RandomStringUtils.random(2));
                RangeElement anotherRangeElement = new RangeElement(RandomStringUtils.random(10), WcpsConstants.MSG_BOOLEAN, RandomStringUtils.random(2));
                inputCoverageBandList.add(rangeElement);
                existingCoverageBandList.add(anotherRangeElement);
            } catch (WCPSException e) {
                e.printStackTrace();
            }
        }
    }

    private void populateBandListWithDifferentNumberOfBands(int noOfBands) {
        populateBandListWithSameBands(noOfBands);
        try {
            existingCoverageBandList.add(new RangeElement(RandomStringUtils.random(10), WcpsConstants.MSG_BOOLEAN, RandomStringUtils.random(2)));
        } catch (WCPSException e) {
            e.printStackTrace();
        }
    }

    @Before
    public void setUp() {
        inputCoverage = Mockito.mock(CoverageMetadata.class);
        existingCoverage = Mockito.mock(CoverageMetadata.class);

        Mockito.when(inputCoverage.getNumberOfBands()).then(new Answer<Integer>() {
            @Override
            public Integer answer(InvocationOnMock invocation) throws Throwable {
                return inputCoverageBandList.size();
            }
        });

        Mockito.when(existingCoverage.getNumberOfBands()).then(new Answer<Integer>() {
            @Override
            public Integer answer(InvocationOnMock invocation) throws Throwable {
                return existingCoverageBandList.size();
            }
        });

        Mockito.when(inputCoverage.getRangeIterator()).then(new Answer<Iterator<RangeElement>>() {
            @Override
            public Iterator<RangeElement> answer(InvocationOnMock invocation) throws Throwable {
                return inputCoverageBandList.iterator();
            }
        });

        Mockito.when(existingCoverage.getRangeIterator()).then(new Answer<Iterator<RangeElement>>() {
            @Override
            public Iterator<RangeElement> answer(InvocationOnMock invocation) throws Throwable {
                return existingCoverageBandList.iterator();
            }
        });

    }

    public int testBandsValidation() {
        try {
            UpdateCoverageValidator updateCoverageValidator = new UpdateCoverageValidator(existingCoverage, inputCoverage, null, null);
            Method validateMethod = UpdateCoverageValidator.class.getDeclaredMethod("validateRangeType", CoverageMetadata.class, CoverageMetadata.class, List.class);
            validateMethod.setAccessible(true);
            validateMethod.invoke(updateCoverageValidator, existingCoverage, inputCoverage, null);
        } catch (NoSuchMethodException e) {
            return 3;
        } catch (InvocationTargetException e) {
            if(e.getTargetException() instanceof WCSTRangeFieldNumberMismatchException){
                return 1;
            }
            if(e.getTargetException() instanceof WCSTRangeFieldNameMismatchException){
                return 2;
            }
            return 3;
        } catch (IllegalAccessException e) {
            return 3;
        }

        return 0;
    }

    @Test
    public void testSameBands() {
        populateBandListWithSameBands(0);
        Assert.assertEquals(0, testBandsValidation());

        populateBandListWithSameBands(3);
        Assert.assertEquals(0, testBandsValidation());

        populateBandListWithSameBands(32);
        Assert.assertEquals(0, testBandsValidation());

        populateBandListWithSameBands(128);
        Assert.assertEquals(0, testBandsValidation());

        populateBandListWithSameBands(1025);
        Assert.assertEquals(0, testBandsValidation());

        populateBandListWithSameBands(1000001);
        Assert.assertEquals(0, testBandsValidation());
    }

    @Test
    public void testDifferentNumberOfBands() {
        populateBandListWithDifferentNumberOfBands(0);
        Assert.assertEquals(1, testBandsValidation());

        populateBandListWithDifferentNumberOfBands(32);
        Assert.assertEquals(1, testBandsValidation());

        populateBandListWithDifferentNumberOfBands(128);
        Assert.assertEquals(1, testBandsValidation());

        populateBandListWithDifferentNumberOfBands(1025);
        Assert.assertEquals(1, testBandsValidation());

        populateBandListWithDifferentNumberOfBands(1000001);
        Assert.assertEquals(1, testBandsValidation());
    }

    @Test
    public void testDifferentBandNames() {
        populateBandListWithDifferentBandNames(32);
        Assert.assertEquals(2, testBandsValidation());

        populateBandListWithDifferentBandNames(128);
        Assert.assertEquals(2, testBandsValidation());

        populateBandListWithDifferentBandNames(1025);
        Assert.assertEquals(2, testBandsValidation());

        populateBandListWithDifferentBandNames(1000001);
        Assert.assertEquals(2, testBandsValidation());
    }
}
