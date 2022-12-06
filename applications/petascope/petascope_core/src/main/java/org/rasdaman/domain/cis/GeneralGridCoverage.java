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
 * Copyright 2003 - 2017 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package org.rasdaman.domain.cis;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import petascope.core.AxisTypes;
import petascope.core.Pair;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.util.CrsUtil;

/**
 *
 * CIS 1.1
 *
 * With the introduction of the CIS GeneralGridCoverage type and its unified
 * modeling of all grid types, the grid types of GMLCOV 1.0 , GML 3.3 , and
 * ReferenceableGrid- Coverage Extension are likely to get deprecated in future.
 */
@Entity
@Table(name = GeneralGridCoverage.TABLE_NAME)
@PrimaryKeyJoinColumn(name = GeneralGridCoverage.COLUMN_ID, referencedColumnName = Coverage.COLUMN_ID)
public class GeneralGridCoverage extends Coverage implements Serializable {

    public static final String TABLE_NAME = "general_grid_coverage";
    public static final String COLUMN_ID = TABLE_NAME + "_id";

    public GeneralGridCoverage() {

    }
    
        
    public GeneralGridCoverage(BaseLocalCoverage baseCoverage) {
        this.id = baseCoverage.getId();
        this.coverageId = baseCoverage.getCoverageId();
        this.envelope = baseCoverage.getEnvelope();
        this.coverageType = baseCoverage.getCoverageType();
        this.coverageSizeInBytes = baseCoverage.getCoverageSizeInBytes();
        this.pyramid = baseCoverage.getPyramid();
        this.rasdamanRangeSet = baseCoverage.getRasdamanRangeSet();
        
        this.inspireMetadataURL = baseCoverage.getInspireMetadataURL();
        this.coverageSizeInBytesWithPyramid = baseCoverage.getCoverageSizeInBytesWithPyramid();
    }

    public GeneralGridCoverage(String coverageId, String coverageType, long coverageSizeInBytes, 
                               Envelope envelope, DomainSet domainSet, RangeType rangeType, RasdamanRangeSet rasdamanRangeSet, String metadata, 
                               long coverageSizeInBytesWithPyramid) {
        super(coverageId, coverageType, coverageSizeInBytes, envelope, domainSet, rangeType, rasdamanRangeSet, metadata, coverageSizeInBytesWithPyramid);
    }
    
    // DomainSet here is GeneralGridDomainSet
    // Helper Methods
    /**
     * Return the list of geo axes from current coverage
     *
     * @return
     */
    @JsonIgnore
    public List<GeoAxis> getGeoAxes() {
        List<GeoAxis> geoAxes = ((GeneralGridDomainSet) this.getDomainSet()).getGeneralGrid().getGeoAxes();

        return geoAxes;
    }

    /**
     *
     * Return the list IndexAxis of coverage's grid domain (cellDomainElements)
     *
     * @return
     */
    @JsonIgnore
    public List<IndexAxis> getIndexAxes() {
        List<IndexAxis> indexAxes = ((GeneralGridDomainSet) this.getDomainSet()).getGeneralGrid().getGridLimits().getIndexAxes();

        return indexAxes;
    }

    /**
     * Return the IndexAxis from the list of index axes by the grid axisOrder
     *
     * @param axisOrder
     * @return
     */
    @JsonIgnore
    public IndexAxis getIndexAxisByOrder(int axisOrder) {
        List<IndexAxis> indexAxes = this.getIndexAxes();

        for (IndexAxis indexAxis : indexAxes) {
            if (indexAxis.getAxisOrder() == axisOrder) {
                return indexAxis;
            }
        }

        return null;
    }

    /**
     * Return the IndexAxis from the list of index axes by the grid axis name as
     * grid index axis has the same name as geo axis, but the grid axes order
     * could be different with geo CRS axes order
     */
    @JsonIgnore
    public IndexAxis getIndexAxisByName(String axisLabel) {
        for (IndexAxis indexAxis : this.getIndexAxes()) {
            if (CrsUtil.axisLabelsMatch(indexAxis.getAxisLabel(), axisLabel)) {
                return indexAxis;
            }
        }
        return null;
    }
    
    /**
     *
     * Return the geoAxis's CRS order from the list of geo axes by axis label
     * e.g: the list of geo axes is: Lat, Long, time, then Long's order is 1
     *
     * @param axisLabel
     * @return
     */
    @JsonIgnore
    public Integer getGeoAxisOrderByName(String axisLabel) {
        List<GeoAxis> geoAxes = ((GeneralGridDomainSet) this.getDomainSet()).getGeneralGrid().getGeoAxes();
        int i = 0;
        for (GeoAxis geoAxis : geoAxes) {
            if (CrsUtil.axisLabelsMatch(geoAxis.getAxisLabel(), axisLabel)) {
                return i;
            }

            i++;
        }

        return null;
    }
    
    @JsonIgnore
    public int getGridAxisOrderByName(String axisLabel) throws PetascopeException {
        List<IndexAxis> indexAxes = this.getIndexAxes();
        for (IndexAxis indexAxis : indexAxes) {
            if (CrsUtil.axisLabelsMatch(indexAxis.getAxisLabel(), axisLabel)) {
                return indexAxis.getAxisOrder();
            }
        }
        
        throw new PetascopeException(ExceptionCode.InvalidRequest, "Index axis does not exist. Given name: " + axisLabel);
    }
    
    /**
     * Check if coverage contains an axis label
     */
    @JsonIgnore
    public boolean containsGeoAxisName(String axisLabel) {
        GeoAxis geoAxis = this.getGeoAxisByName(axisLabel);
        
        return geoAxis != null;
    }

    /**
     * Return the geo axis by it's name from the list of geo axes
     *
     * @param axisLabel
     * @return
     */
    @JsonIgnore
    public GeoAxis getGeoAxisByName(String axisLabel) {
        List<GeoAxis> geoAxes = ((GeneralGridDomainSet) this.getDomainSet()).getGeneralGrid().getGeoAxes();
        for (GeoAxis geoAxis : geoAxes) {
            if (CrsUtil.axisLabelsMatch(geoAxis.getAxisLabel(), axisLabel)) {
                return geoAxis;
            }
        }

        return null;
    }
    
    /**
     * Return the XY geo axes of a coverage if possible
     * @return 
     */
    @JsonIgnore
    public Pair<GeoAxis, GeoAxis> getXYGeoAxes() throws PetascopeException {
        GeoAxis geoAxisX = null, geoAxisY = null;
        
        List<GeoAxis> geoAxes = ((GeneralGridDomainSet) this.getDomainSet()).getGeneralGrid().getGeoAxes();
        
        for (GeoAxis geoAxis : geoAxes) {
            // x, y, t,...
            String axisType = geoAxis.getAxisType();

            if (axisType == null) {
                // NOTE: in some rare cases when DataMigration2Handler.java failed, 
                // this attribute is null, then calculate it now and persist Coverage object to database
                GeneralGridCoverage.setAxisType(this);
                
                axisType = geoAxis.getAxisType();
            }
            
            if (axisType.equals(AxisTypes.X_AXIS)) {
                geoAxisX = geoAxis;
            } else if (axisType.equals(AxisTypes.Y_AXIS)) {
                geoAxisY = geoAxis;
            }
        }
        
        // Coverage has XY geo axes
        if (geoAxisX != null && geoAxisY != null) {
            return new Pair<>(geoAxisX, geoAxisY);
        }
        
        return null;        
    }
    
    /**
     * Return list of non XY geo axes
     */
    @JsonIgnore
    public List<GeoAxis> getNonXYGeoAxes() throws PetascopeException {
        Pair<GeoAxis, GeoAxis> xyGeoAxesPair = this.getXYGeoAxes();
        
        List<GeoAxis> nonXYGeoAxes = new ArrayList<>();
        for (GeoAxis axis : this.getGeoAxes()) {
            if (!(axis.getAxisLabel().equals(xyGeoAxesPair.fst.getAxisLabel())
                || axis.getAxisLabel().equals(xyGeoAxesPair.snd.getAxisLabel()))) {
                nonXYGeoAxes.add(axis);
            }
        } 
        
        return nonXYGeoAxes;
    }
    
    /**
     * Check if a GeoAxis is X/Y type (e.g: Long or Lat)
     */
    @JsonIgnore
    public boolean isXYAxis(GeoAxis geoAxis) throws PetascopeException, SecoreException {
        Pair<GeoAxis, GeoAxis> pair = this.getXYGeoAxes();
        
        return CrsUtil.axisLabelsMatch(pair.fst.getAxisLabel(), geoAxis.getAxisLabel())
              || CrsUtil.axisLabelsMatch(pair.snd.getAxisLabel(), geoAxis.getAxisLabel());
    }
    
    @JsonIgnore
    /**
     * Set the missing axis type for each axis in the envelope and geo axis
     */
    public static void setAxisType(GeneralGridCoverage baseCoverage) throws PetascopeException {
        
        EnvelopeByAxis envelopeByAxis = baseCoverage.getEnvelope().getEnvelopeByAxis();
        String coverageCRS = envelopeByAxis.getSrsName();
        int numberOfAxes = envelopeByAxis.getAxisExtents().size();
        
        for (int i = 0; i < numberOfAxes; i++) {
            String axisType = CrsUtil.getAxisTypeByIndex(coverageCRS, i);
            AxisExtent axisExtent = envelopeByAxis.getAxisExtents().get(i);
            axisExtent.setAxisType(axisType);
            
            GeoAxis geoAxis = baseCoverage.getGeoAxes().get(i);
            geoAxis.setAxisType(axisType);
        }
    }
    
    
    @Override
    public String toString() {
        return "Coverage id '" + this.coverageId + "'.";
    }
}
