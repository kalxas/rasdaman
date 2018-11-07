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
 * Copyright 2003 - 2018 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.core.gml.cis10;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import static petascope.core.XMLSymbols.LABEL_GRID_COVERAGE;
import static petascope.core.XMLSymbols.LABEL_RECTIFIED_GRID_COVERAGE;
import static petascope.core.XMLSymbols.LABEL_REFERENCEABLE_GRID_COVERAGE;
import static petascope.core.XMLSymbols.NAMESPACE_GMLCOV;
import static petascope.core.XMLSymbols.PREFIX_GMLCOV;
import static petascope.core.XMLSymbols.PREFIX_GMLRGRID;
import petascope.core.gml.cis10.model.boundedby.BoundedBy;
import petascope.core.gml.cis10.model.boundedby.Envelope;
import petascope.core.gml.cis.model.coveragefunction.CoverageFunction;
import petascope.core.gml.cis.model.coveragefunction.CoverageFunctionService;
import petascope.core.gml.cis10.model.domainset.DomainSet;
import petascope.core.gml.cis10.model.domainset.grid.Grid;
import petascope.core.gml.cis10.model.domainset.grid.GridEnvelope;
import petascope.core.gml.cis10.model.domainset.grid.Limits;
import petascope.core.gml.cis10.model.domainset.rectifiedgrid.OffsetVector;
import petascope.core.gml.cis10.model.domainset.rectifiedgrid.Origin;
import petascope.core.gml.cis10.model.domainset.rectifiedgrid.Point;
import petascope.core.gml.cis10.model.domainset.rectifiedgrid.RectifiedGrid;
import petascope.core.gml.cis10.model.domainset.referenceablegridbyvectors.GeneralGridAxis;
import petascope.core.gml.cis10.model.domainset.referenceablegridbyvectors.ReferenceableGridByVectors;
import petascope.core.gml.cis10.model.metadata.Metadata;
import petascope.core.gml.cis.model.rangetype.RangeType;
import petascope.core.gml.cis.model.rangetype.RangeTypeService;
import petascope.core.gml.metadata.service.CoverageMetadataService;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.wcps.metadata.model.Axis;
import petascope.wcps.metadata.model.IrregularAxis;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;

/**
 * Build GMLCore as main part of WCS DescribeCoverage/GetCoverage in
 * application/gml+xml result for CIS 1.0 coverages.
 *
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@Service
public class GMLCoreCIS10Builder {
    
    @Autowired
    private CoverageFunctionService coverageFunctionService;
    
    @Autowired
    private RangeTypeService rangeTypeService;
    
    @Autowired
    private CoverageMetadataService coverageMetadataService;
    
    // ################## Build BoundedBy
    /**
     * Build Envelope for BoundedBy.
     */
    private Envelope buildEnvelope(WcpsCoverageMetadata wcpsCoverageMetadata) throws PetascopeException {        
        String axisLabels = "";
        String srsDimension = "";
        String srsName = "";
        String uomLabels = "";
    
        String lowerCorner = "";
        String upperCorner = "";
        
        List<Axis> axes = wcpsCoverageMetadata.getAxes();
        srsDimension = String.valueOf(axes.size());
        srsName = wcpsCoverageMetadata.getCrsUri();
        
        for (Axis geoAxis : axes) {
            axisLabels += geoAxis.getLabel() + " ";
            uomLabels += geoAxis.getAxisUoM() + " ";
            lowerCorner += geoAxis.getLowerGeoBoundRepresentation() + " ";
            upperCorner += geoAxis.getUpperGeoBoundRepresentation() + " ";
        }
        
        Envelope envelope = new Envelope();
        envelope.setAxisLabels(axisLabels.trim());
        envelope.setSrsName(srsName);
        envelope.setSrsDimension(srsDimension.trim());
        envelope.setUomLabels(uomLabels.trim());
        envelope.setLowerCorner(lowerCorner.trim());
        envelope.setUpperCorner(upperCorner.trim());
        
        return envelope;
    }
    
    /**
     * Build BoundedBy for GMLCore.
     */
    private BoundedBy buildBoundedBy(WcpsCoverageMetadata wcpsCoverageMetadata) throws PetascopeException {
        Envelope envelope = this.buildEnvelope(wcpsCoverageMetadata);
        BoundedBy boundedBy = new BoundedBy(envelope);
        
        return boundedBy;
    }
    
    // ################## Build Grid(s)
    
    /**
     * Build GridEnvelope for Limits.
     */
    private GridEnvelope buildGridEnvelope(List<Axis> axes) {

        String low = "";
        String high = "";
        
        for (Axis axis : axes) {
            low += axis.getGridBounds().getLowerLimit().toBigInteger().toString() + " ";
            high += axis.getGridBounds().getUpperLimit().toBigInteger().toString() + " ";
        }
        
        GridEnvelope gridEnvelope = new GridEnvelope(low.trim(), high.trim());

        return gridEnvelope;
    }
    
    /**
     * Build Limits for CIS 1.0 coverage types's domainSet.
     */
    private Limits buildLimits(List<Axis> axes) {
        GridEnvelope gridEnvelope = this.buildGridEnvelope(axes);
        
        Limits limits = new Limits(gridEnvelope);
        return limits;
    }
    
    /**
     * Build Grid for domainSet.
     */
    private Grid buildGrid(WcpsCoverageMetadata wcpsCoverageMetadata) {
        List<Axis> axes = wcpsCoverageMetadata.getAxes();
        
        String gridId = wcpsCoverageMetadata.getGridId();
        String dimension = String.valueOf(axes.size());
        String axisLabels = "";
        Limits limits = this.buildLimits(axes);
        
        for (Axis axis : axes) {
            axisLabels += axis.getLabel() + " ";
        }
        
        Grid grid = new Grid(gridId, dimension, limits, axisLabels.trim());
        return grid;
    }
    
    /**
     * Build Point for Origin.
     */
    private Point buildPoint(WcpsCoverageMetadata wcpsCoverageMetadata) throws PetascopeException {
        String id = wcpsCoverageMetadata.getPointId();
        String srsName = wcpsCoverageMetadata.getCrsUri();
        
        String coordinates = "";
        
        List<Axis> axes = wcpsCoverageMetadata.getAxes();
        for (Axis axis : axes) {
            coordinates += axis.getOriginRepresentation() + " ";
        }
        
        Point point = new Point(id, srsName, coordinates.trim());
        return point;        
    }
    
    /**
     * Build Origin for RectifiedGrid.
     */
    private Origin buildOrigin(WcpsCoverageMetadata wcpsCoverageMetadata) throws PetascopeException {
        Point point = this.buildPoint(wcpsCoverageMetadata);
        
        Origin origin = new Origin(point);
        
        return origin;        
    }
    
    /**
     * Build OffsetVector
     */   
    private OffsetVector buildOffsetVector(WcpsCoverageMetadata wcpsCoverageMetadata, Axis axis) {
        String srsName = wcpsCoverageMetadata.getCrsUri();
        String coordinates = wcpsCoverageMetadata.getOffsetVectorByAxisLabel(axis.getLabel());
            
        OffsetVector offsetVector = new OffsetVector(srsName, coordinates);
        return offsetVector;
    }
    
    /**
     * Build List<OffsetVector> for RectifiedGrid.
     */
    private List<OffsetVector> buildOffsetVectors(WcpsCoverageMetadata wcpsCoverageMetadata) {
        List<Axis> axes = wcpsCoverageMetadata.getAxes();
        List<OffsetVector> offsetVectors = new ArrayList<>();
        
        for (Axis axis : axes) {
            OffsetVector offsetVector = this.buildOffsetVector(wcpsCoverageMetadata, axis);
            offsetVectors.add(offsetVector);
        }
        
        return offsetVectors;
    }
    
    /**
     * Build RectifiedGrid for domainSet.
    */
    private RectifiedGrid buildRectifiedGrid(WcpsCoverageMetadata wcpsCoverageMetadata) throws PetascopeException {
        Grid grid = this.buildGrid(wcpsCoverageMetadata);
        RectifiedGrid rectifiedGrid = new RectifiedGrid(grid);
        
        Origin origin = this.buildOrigin(wcpsCoverageMetadata);
        List<OffsetVector> offsetVectors = this.buildOffsetVectors(wcpsCoverageMetadata);
        
        rectifiedGrid.setOrigin(origin);
        rectifiedGrid.setOffsetVectors(offsetVectors);
        
        return rectifiedGrid;
    }
    
    /**
     * Build GeneralGridAxis for ReferenceableGridByVectors
     */
    private List<GeneralGridAxis> buildGeneralGridAxes(WcpsCoverageMetadata wcpsCoverageMetadata) throws PetascopeException {
        
        List<GeneralGridAxis> generalGridAxes = new ArrayList<>();
        List<Axis> axes = wcpsCoverageMetadata.getAxes();
        
        for (Axis axis : axes) {  
            OffsetVector offsetVector = this.buildOffsetVector(wcpsCoverageMetadata, axis);
            String coefficients = "";
            if (axis instanceof IrregularAxis) {
                coefficients = ((IrregularAxis) axis).getRepresentationCoefficients();
            }
            String gridAxesSpanned = axis.getLabel();
            
            GeneralGridAxis generalGridAxis = new GeneralGridAxis(offsetVector, coefficients, gridAxesSpanned);
            generalGridAxes.add(generalGridAxis);
        }
        
        return generalGridAxes;
    }
    
    /**
     * Build ReferenceableGridByVectors for domainSet.
     */
    private ReferenceableGridByVectors buildReferenceableGridByVectors(WcpsCoverageMetadata wcpsCoverageMetadata) throws PetascopeException {
        RectifiedGrid rectifiedGrid = this.buildRectifiedGrid(wcpsCoverageMetadata);
        
        ReferenceableGridByVectors referenceableGridByVectors = new ReferenceableGridByVectors(rectifiedGrid);
        Origin origin = referenceableGridByVectors.getOrigin();
        origin.setPrefixLabelXML(PREFIX_GMLRGRID);
        
        List<GeneralGridAxis> generalGridAxes = this.buildGeneralGridAxes(wcpsCoverageMetadata);
        for (GeneralGridAxis generalGridAxis : generalGridAxes) {
            // Update prefix for offsetVector (from gml -> gmlrgrid) for irregular axes
            OffsetVector offsetVector = generalGridAxis.getOffsetVector();
            offsetVector.setPrefixLabelXML(PREFIX_GMLRGRID);
        }
         
        referenceableGridByVectors.setGeneralGridAxes(generalGridAxes);
        
        return referenceableGridByVectors;
    }
    
    // ################## Build DomainSet

    /**
     * Build DomainSet for Grid Coverage in CIS 1.0.
     */
    private DomainSet buildGridDomainSet(WcpsCoverageMetadata wcpsCoverageMetadata) {
        Grid grid = this.buildGrid(wcpsCoverageMetadata);
        
        DomainSet domainSet = new DomainSet(grid);
        return domainSet;        
    }
    
    /**
     * Build DomainSet for RectifiedGrid Coverage in CIS 1.0.
     */
    private DomainSet buildRectifiedGridDomainSet(WcpsCoverageMetadata wcpsCoverageMetadata) throws PetascopeException {
        RectifiedGrid rectifiedGrid = this.buildRectifiedGrid(wcpsCoverageMetadata);
       
        DomainSet domainSet = new DomainSet(rectifiedGrid);
        return domainSet;        
    }
    
    /**
     * Build DomainSet for ReferenceableGrid Coverage in CIS 1.0.
     */
    private DomainSet buildReferenceableGridDomainSet(WcpsCoverageMetadata wcpsCoverageMetadata) throws PetascopeException {
        ReferenceableGridByVectors referenceableGridByVectors = this.buildReferenceableGridByVectors(wcpsCoverageMetadata);
        
        DomainSet domainSet = new DomainSet(referenceableGridByVectors);
        return domainSet;
    }
    
    /**
     * Build DomainSet for GMLCore.
     */
    private DomainSet buildDomainSet(WcpsCoverageMetadata wcpsCoverageMetadata) throws PetascopeException {
        String coverageType = wcpsCoverageMetadata.getCoverageType();
        
        DomainSet domainSet = null;
        
        if (coverageType.equals(LABEL_GRID_COVERAGE)) {
            domainSet = this.buildGridDomainSet(wcpsCoverageMetadata);
        } else if (coverageType.equals(LABEL_RECTIFIED_GRID_COVERAGE)) {
            domainSet = this.buildRectifiedGridDomainSet(wcpsCoverageMetadata);
        } else if (coverageType.equals(LABEL_REFERENCEABLE_GRID_COVERAGE)) {
            domainSet = this.buildReferenceableGridDomainSet(wcpsCoverageMetadata);
        } else {
            throw new PetascopeException(ExceptionCode.NoApplicableCode, "Cannot build DomainSet for coverage type '" + coverageType + "'.");
        }
        
        return domainSet;
    }
    
    // ################## Build GMLCore

    /**
     * Build GMLCore for 1 input coverage.
     */
    public GMLCoreCIS10 build(WcpsCoverageMetadata wcpsCoverageMetadata) throws PetascopeException {
        BoundedBy boundedBy = this.buildBoundedBy(wcpsCoverageMetadata);
        CoverageFunction coverageFunction = this.coverageFunctionService.buildCoverageFunction(wcpsCoverageMetadata);
        Metadata metadata = new Metadata(this.coverageMetadataService.getMetadataContent(wcpsCoverageMetadata));
        DomainSet domainSet = this.buildDomainSet(wcpsCoverageMetadata);
        RangeType rangeType = this.rangeTypeService.buildRangeType(PREFIX_GMLCOV, NAMESPACE_GMLCOV, wcpsCoverageMetadata);
        
        GMLCoreCIS10 gmlCore = new GMLCoreCIS10(boundedBy, coverageFunction, metadata, domainSet, rangeType);

        return gmlCore;
    }
}
