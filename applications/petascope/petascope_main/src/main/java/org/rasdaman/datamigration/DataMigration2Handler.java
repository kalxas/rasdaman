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
 * Copyright 2003 - 2020 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package org.rasdaman.datamigration;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.rasdaman.domain.cis.AxisExtent;
import org.rasdaman.domain.cis.Coverage;
import org.rasdaman.domain.cis.CoveragePyramid;
import static org.rasdaman.domain.cis.CoveragePyramid.NON_XY_AXIS_SCALE_FACTOR;
import org.rasdaman.domain.cis.EnvelopeByAxis;
import org.rasdaman.domain.cis.GeneralGridCoverage;
import org.rasdaman.domain.cis.GeoAxis;
import org.rasdaman.domain.cis.IndexAxis;
import org.rasdaman.domain.cis.IrregularAxis;
import org.rasdaman.domain.cis.RasdamanDownscaledCollection;
import org.rasdaman.repository.service.CoverageRepositoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.util.BigDecimalUtil;
import petascope.util.CrsUtil;
import petascope.util.JSONUtil;
import static petascope.util.ras.RasConstants.RASQL_BOUND_SEPARATION;
import static petascope.util.ras.RasConstants.RASQL_INTERVAL_SEPARATION;
import petascope.util.ras.RasUtil;

/**
 * Class to handle data migration version number 1
 * 
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@Service
public class DataMigration2Handler extends AbstractDataMigrationHandler {
    
    private static final Logger log = LoggerFactory.getLogger(AbstractDataMigrationHandler.class);
    
    @Autowired
    private CoverageRepositoryService coverageRepositoryService;

    public DataMigration2Handler() {
        // NOTE: update this by one for new handler class
        this.migrationVersion = 2;        
        this.handlerId = "ff392cdc-8348-11eb-9c35-509a4cb4e064";
    }

    @Override
    public void migrate() throws PetascopeException, IOException, SecoreException {
        
        // Task: migrate all rasdaman downscaled collections to coverages
        
        for (String coverageId : this.coverageRepositoryService.readAllLocalCoverageIds()) {
            Coverage baseCoverage = this.coverageRepositoryService.readCoverageFullMetadataByIdFromCache(coverageId);
            
            // Get the deprecated downscaled collections objects of a base coverage
            List<RasdamanDownscaledCollection> rasdamanDownscaledCollections = baseCoverage.getRasdamanRangeSet().getRasdamanDownscaledCollections();
            
            // If coverage has downscaled collections
            if (rasdamanDownscaledCollections != null && rasdamanDownscaledCollections.size() > 0) {
                baseCoverage = this.coverageRepositoryService.readCoverageByIdFromDatabase(coverageId);
                
                for (RasdamanDownscaledCollection rasdamanDownscaledCollection : rasdamanDownscaledCollections) {
                    try {
                        this.createDownscaledLevelCoverage(rasdamanDownscaledCollection, (GeneralGridCoverage) baseCoverage);
                    } catch(Exception ex) {
                        log.warn("Cannot migrate coverage '" + coverageId + "'. Reason: " + ex.getMessage(), ex);
                    }
                }
                
                baseCoverage.getRasdamanRangeSet().setRasdamanDownscaledCollections(new ArrayList<RasdamanDownscaledCollection>());                
            }
            
            this.setAxisType((GeneralGridCoverage) baseCoverage);
            this.coverageRepositoryService.save(baseCoverage);
        }
        
    }
    
    /**
     * Set the missing axis type for each axis in the envelope and geo axis
     */
    private void setAxisType(GeneralGridCoverage baseCoverage) throws PetascopeException {
        
        EnvelopeByAxis envelopeByAxis = baseCoverage.getEnvelope().getEnvelopeByAxis();
        String coverageCRS = envelopeByAxis.getSrsName();
        int numberOfAxes = envelopeByAxis.getAxisExtents().size();
        
        for (int i = 0; i < numberOfAxes; i++) {
            String axisType = CrsUtil.getAxisTypeByIndex(coverageCRS, i);
            AxisExtent axisExtent = envelopeByAxis.getAxisExtents().get(i);
            axisExtent.setAxisType(axisType);
            
            if (axisType == null) {
                System.out.println("");
            }
            
            GeoAxis geoAxis = baseCoverage.getGeoAxes().get(i);
            geoAxis.setAxisType(axisType);
        }
        
    }
    
    /**
     * From a rasdaman downscaled collection and and input base coverage,
     * create a new coverage (a pyramid member coverage) of the base coverage
     */
    private void createDownscaledLevelCoverage(RasdamanDownscaledCollection rasdamanDownscaledCollection, GeneralGridCoverage baseCoverage)
                                                            throws IOException, PetascopeException, SecoreException {
        
        // e.g: 0:5,0:30,-50:60
        String sdomSourceRasdamanDownscaledCollection = RasUtil.retrieveSdomInfo(rasdamanDownscaledCollection.getCollectionName());
        String[] gridIntervals = sdomSourceRasdamanDownscaledCollection.split(RASQL_INTERVAL_SEPARATION);
        
        // Clone base coverage to pyramid member coverage
        GeneralGridCoverage pyramidMemberCoverage = (GeneralGridCoverage) JSONUtil.clone(baseCoverage);
        
        // e.g: test_cov_2 (downscaled level 2)
        String pyramidMemberCoverageId = baseCoverage.getCoverageId() + "_" + rasdamanDownscaledCollection.getLevel().toPlainString();
        String pyramidMemberRasdamanCollectionName = rasdamanDownscaledCollection.getCollectionName();
        pyramidMemberCoverage.setCoverageId(pyramidMemberCoverageId);
        pyramidMemberCoverage.getRasdamanRangeSet().setCollectionName(pyramidMemberRasdamanCollectionName);
        
        // update the grid bounds for downscaled level coverage
        for (int i = 0; i < gridIntervals.length; i++) {
            String[] bounds = gridIntervals[i].split(RASQL_BOUND_SEPARATION);
            Long lowerBound = Long.valueOf(bounds[0]);
            Long upperBound = Long.valueOf(bounds[1]);
            
            IndexAxis pyramidMemberIndexAxis = pyramidMemberCoverage.getIndexAxisByOrder(i);
            String axisLabel = pyramidMemberIndexAxis.getAxisLabel();
            
            pyramidMemberIndexAxis.setLowerBound(lowerBound);
            pyramidMemberIndexAxis.setUpperBound(upperBound);
            
            // set the resolutions for axis extents in envelope and regular geo axes
            BigDecimal axisResolution = IrregularAxis.DEFAULT_RESOLUTION;
            GeoAxis baseGeoAxis = baseCoverage.getGeoAxisByName(axisLabel);
            GeoAxis pyramidMemberGeoAxis = pyramidMemberCoverage.getGeoAxisByName(axisLabel);
            if (!pyramidMemberGeoAxis.isIrregular()) {
                BigDecimal numberOfGridPixels = new BigDecimal(upperBound - lowerBound + 1);
                BigDecimal geoDistance = pyramidMemberGeoAxis.getUpperBoundNumber().subtract(pyramidMemberGeoAxis.getLowerBoundNumber());
                axisResolution = BigDecimalUtil.divide(geoDistance, numberOfGridPixels);
                
                if (baseGeoAxis.getResolution().compareTo(BigDecimal.ZERO) < 0) {
                    axisResolution = axisResolution.multiply(new BigDecimal("-1"));
                }
            }

            pyramidMemberGeoAxis.setResolution(axisResolution);
            pyramidMemberCoverage.getEnvelope().getEnvelopeByAxis().getAxisExtentByLabel(axisLabel).setResolution(axisResolution);
        }
        
        // e.g: 2.33
        String level = BigDecimalUtil.stripDecimalZeros(rasdamanDownscaledCollection.getLevel()).toPlainString();

        // e.g: [1, 2, 2] with geo crs order: time, Lat, Long
        List<String> scaleFactors = new ArrayList<>();        
        for (GeoAxis geoAxis : pyramidMemberCoverage.getGeoAxes()) {
            
            String scaleFactor = level;
            if (!pyramidMemberCoverage.isXYAxis(geoAxis)) {
                scaleFactor = NON_XY_AXIS_SCALE_FACTOR;
            }
            
            scaleFactors.add(scaleFactor);
        }
        
        
        pyramidMemberCoverage.getRasdamanRangeSet().setRasdamanDownscaledCollections(null);        
        // persist newly created pyramid memer coverage to database
        this.coverageRepositoryService.save(pyramidMemberCoverage);

        // add pyramid member to the base coverage
        CoveragePyramid coveragePyramid = new CoveragePyramid(pyramidMemberCoverageId, scaleFactors, true);
        baseCoverage.getPyramid().add(coveragePyramid);
    }
    
}
