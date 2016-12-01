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

import junit.framework.Assert;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import petascope.BaseTestCase;
import petascope.ConfigManager;
import petascope.core.DbMetadataSource;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.util.IOUtil;
import petascope.wcs2.handlers.Response;
import petascope.wcs2.handlers.wcst.DeleteCoverageHandler;
import petascope.wcs2.handlers.wcst.InsertCoverageHandler;
import petascope.wcs2.handlers.wcst.UpdateCoverageHandler;
import petascope.wcs2.parsers.subsets.DimensionSlice;
import petascope.wcs2.parsers.subsets.DimensionSubset;
import petascope.wcs2.parsers.subsets.SubsetParser;
import petascope.wcs2.parsers.wcst.DeleteCoverageRequest;
import petascope.wcs2.parsers.wcst.InsertCoverageRequest;
import petascope.wcs2.parsers.wcst.UpdateCoverageRequest;
import petascope.wms2.metadata.Dimension;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Integration test verifying that metadata remains unchanged for coverages where an update adding a new slice to an irregular axis
 * fails at rasdaman level.
 *
 * Requires rasdaman to be running at http://localhost:7001.
 */
public class MetadataRollbackTest {

    private static InputStream TEST_DATASET = MetadataRollbackTest.class.getResourceAsStream("../../../testdata/wcst/irreg_3d_slice.gml");
    private static InputStream TEST_DATASET_UPDATE = MetadataRollbackTest.class.getResourceAsStream("../../../testdata/wcst/2d_slice.gml");
    private int IRREG_AXIS_ORDER = 2;

    private DbMetadataSource metadataSource;
    private String coverageName;
    private List<BigDecimal> initialCoefficientList;

    /**
     * Creates a coverage containing an irregular axis.
     */
    @Before
    public void setUp() throws IOException, PetascopeException, SecoreException {
        this.metadataSource = new DbMetadataSource(
            ConfigManager.METADATA_DRIVER,
            ConfigManager.METADATA_URL,
            ConfigManager.METADATA_USER,
            ConfigManager.METADATA_PASS, false);

        //insert the coverage
        String gmlCov = IOUtils.toString(TEST_DATASET);
        InsertCoverageRequest insertCoverageRequest = new InsertCoverageRequest(gmlCov, null, true, "Byte", null);
        InsertCoverageHandler insertCoverageHandler = new InsertCoverageHandler(this.metadataSource);
        Response response = insertCoverageHandler.handle(insertCoverageRequest);
        this.coverageName = parseCoverageName(response);
        this.initialCoefficientList = this.metadataSource.getAllCoefficients(this.coverageName, IRREG_AXIS_ORDER);
    }

    @After
    public void tearDown() throws PetascopeException, SecoreException {
        //delete the coverage
        DeleteCoverageRequest deleteCoverageRequest = new DeleteCoverageRequest(this.coverageName);
        DeleteCoverageHandler deleteCoverageHandler = new DeleteCoverageHandler(this.metadataSource);
        deleteCoverageHandler.handle(deleteCoverageRequest);
    }

    @Test
    public void testMetadataRollback() throws IOException, PetascopeException, SecoreException {
        //update the coverage with a wrong data type
        String gmlCov = IOUtils.toString(TEST_DATASET_UPDATE);
        //create the time point where to add the data
        DimensionSlice dimensionSlice = new DimensionSlice("ansi", "\"2002-11-01T00:00:00+00:00\"");
        List<DimensionSubset> subsets = new ArrayList<DimensionSubset>();
        subsets.add(dimensionSlice);
        UpdateCoverageRequest updateCoverageRequest = new UpdateCoverageRequest(this.coverageName, gmlCov, null, null,
                null, subsets, null, null, "Float64");
        UpdateCoverageHandler updateCoverageHandler = new UpdateCoverageHandler(this.metadataSource);

        //try to make the failing update
        try {
            updateCoverageHandler.handle(updateCoverageRequest);
        } catch (PetascopeException e) {
            //ok, we expected this, check if the coefficients stayed the same
            List<BigDecimal> newCoefficients = this.metadataSource.getAllCoefficients(this.coverageName, IRREG_AXIS_ORDER);
            Assert.assertTrue(newCoefficients.equals(this.initialCoefficientList));
            return;
        } catch (SecoreException e) {
            //problem
            throw e;
        }

        //request didn't fail and it should have, so smth went wrong
        Assert.assertTrue(false);
    }

    private String parseCoverageName(Response response) {
        return response.getXml()[0].split("<coverageId>")[1].replace("</coverageId>", "").trim();
    }
}