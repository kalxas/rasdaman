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
package petascope.core.gml.cis11;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import static petascope.core.XMLSymbols.LABEL_RANGE_TYPE_CIS11;
import static petascope.core.XMLSymbols.PREFIX_CIS11;
import petascope.core.gml.cis.model.coveragefunction.CoverageFunction;
import petascope.core.gml.cis.model.coveragefunction.CoverageFunctionService;
import petascope.core.gml.cis.model.rangetype.RangeType;
import petascope.core.gml.cis.model.rangetype.RangeTypeService;
import petascope.core.gml.cis11.model.domainset.DomainSet;
import petascope.core.gml.cis11.model.domainset.GeneralGrid;
import petascope.core.gml.cis11.model.domainset.GridLimits;
import petascope.core.gml.cis11.model.domainset.IndexAxis;
import petascope.core.gml.cis11.model.domainset.IrregularAxis;
import petascope.core.gml.cis11.model.domainset.RegularAxis;
import petascope.core.gml.cis11.model.envelope.AxisExtent;
import petascope.core.gml.cis11.model.envelope.Envelope;
import petascope.core.gml.cis11.model.metadata.Metadata;
import petascope.core.gml.metadata.service.CoverageMetadataService;
import petascope.exceptions.PetascopeException;
import petascope.util.StringUtil;
import petascope.wcps.metadata.model.Axis;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;
import static petascope.core.XMLSymbols.NAMESPACE_CIS_11;

/**
 * Build GMLCore as main part of WCS DescribeCoverage/GetCoverage in
 * application/gml+xml result for CIS 1.1 coverages (or CIS 1.0 coverages but outputType=GeneralGridCoverage).
 * 
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@Service
public class GMLCoreCIS11Builder {

    @Autowired
    private CoverageFunctionService coverageFunctionService;
    
    @Autowired
    private CoverageMetadataService coverageMetadataService;
    
    @Autowired
    private RangeTypeService rangeTypeService;
    
    
    // ################## Build Envelope
    
    private Envelope buildEnvelope(WcpsCoverageMetadata wcpsCoverageMetadata) throws PetascopeException {
        
        String geoSrsName = wcpsCoverageMetadata.getCrsUri();
        Integer srsDimension = wcpsCoverageMetadata.getAxes().size();
        String axisLabels = wcpsCoverageMetadata.getGeoAxisNames();
        List<AxisExtent> axisExtents = new ArrayList<>(); 

        for (Axis axis : wcpsCoverageMetadata.getAxes()) {
            String axisLabel = axis.getLabel();
            String uomLabel = axis.getAxisUoM();
            String geoLowerBound = StringUtil.stripQuotes(axis.getLowerGeoBoundRepresentation());
            String geoUpperBound = StringUtil.stripQuotes(axis.getUpperGeoBoundRepresentation());
            
            AxisExtent axisExtent = new AxisExtent(axisLabel, uomLabel, geoLowerBound, geoUpperBound);
            axisExtents.add(axisExtent);
        }

        Envelope envelope = new Envelope(geoSrsName, axisLabels, srsDimension.toString(), axisExtents);
        
        return envelope;
    }
    
    // ################## Build DomainSet
    
    /**
     * Build list of RegularAxis for GeneralGrid.
     */
    private List<RegularAxis> buildRegularAxes(WcpsCoverageMetadata wcpsCoverageMetadata) throws PetascopeException {
        List<RegularAxis> regularAxes = new ArrayList<>();
        
        for (int i = 0; i < wcpsCoverageMetadata.getAxes().size(); i++) {
            Axis axis = wcpsCoverageMetadata.getAxes().get(i);
            String axisLabel = axis.getLabel();
            String uomLabel = axis.getAxisUoM();
            String geoLowerBound = StringUtil.stripQuotes(axis.getLowerGeoBoundRepresentation());
            String geoUpperBound = StringUtil.stripQuotes(axis.getUpperGeoBoundRepresentation());
            String axisResolution = axis.getResolution().toPlainString();

            if (axis instanceof petascope.wcps.metadata.model.RegularAxis) {
                RegularAxis regularAxis = new RegularAxis(axisLabel, uomLabel, geoLowerBound, geoUpperBound, axisResolution);
                regularAxes.add(regularAxis);
            }
        }
        
        return regularAxes;
    }
    
    /**
     * Build list of IrregularAxis for GeneralGrid.
     */
    private List<IrregularAxis> buildIrregularAxes(WcpsCoverageMetadata wcpsCoverageMetadata) throws PetascopeException {
        List<IrregularAxis> irregularAxes = new ArrayList<>();
        
        for (int i = 0; i < wcpsCoverageMetadata.getAxes().size(); i++) {
            Axis axis = wcpsCoverageMetadata.getAxes().get(i);
            String axisLabel = axis.getLabel();
            String uomLabel = axis.getAxisUoM();

            if (axis instanceof petascope.wcps.metadata.model.IrregularAxis) {
                List<String> coefficients = ((petascope.wcps.metadata.model.IrregularAxis) axis).getRepresentationCoefficientsList();
                IrregularAxis irregularAxis = new IrregularAxis(axisLabel, uomLabel, coefficients);
                irregularAxes.add(irregularAxis);
            }
        }
        
        return irregularAxes;
    }
    
    /**
     * Build List of IndexAxis for GeneralGrid.
     */
    private List<IndexAxis> buildIndexAxes(WcpsCoverageMetadata wcpsCoverageMetadata) {
        List<IndexAxis> indexAxes = new ArrayList<>();
        
        for (int i = 0; i < wcpsCoverageMetadata.getAxes().size(); i++) {
            Axis axis = wcpsCoverageMetadata.getAxes().get(i);
            String gridAxisLabel = Axis.createAxisLabelByIndex(i);
            String gridLowerBound = axis.getGridBounds().getLowerLimit().toPlainString();
            String gridUpperBound = axis.getGridBounds().getUpperLimit().toPlainString();
            
            IndexAxis indexAxis = new IndexAxis(gridAxisLabel, gridLowerBound, gridUpperBound);
            indexAxes.add(indexAxis);
        }
        
        return indexAxes;
    }
    
    /**
     * Build GridLimits for GeneralGrid.
     */
    private GridLimits buildGridLimits(WcpsCoverageMetadata wcpsCoverageMetadata) {
        
        String gridSrsName = wcpsCoverageMetadata.getIndexCrsUri();
        String axisLabels = wcpsCoverageMetadata.getGridAxisNames();
        List<IndexAxis> indexAxes = this.buildIndexAxes(wcpsCoverageMetadata);
        
        GridLimits gridLimits = new GridLimits(gridSrsName, axisLabels, indexAxes);
        
        return gridLimits;
    }
    
    /**
     * Build GeneralGrid for DomainSet.
     */
    private GeneralGrid buildGeneralGrid(WcpsCoverageMetadata wcpsCoverageMetadata) throws PetascopeException {
        
        String geoSrsName = wcpsCoverageMetadata.getCrsUri();
        String axisLabels = wcpsCoverageMetadata.getGeoAxisNames();
        List<RegularAxis> regularAxes = this.buildRegularAxes(wcpsCoverageMetadata);
        List<IrregularAxis> irregularAxes = this.buildIrregularAxes(wcpsCoverageMetadata);
        GridLimits gridLimits = this.buildGridLimits(wcpsCoverageMetadata);
        
        GeneralGrid generalGrid = new GeneralGrid(geoSrsName, axisLabels, regularAxes, irregularAxes, gridLimits);
        
        return generalGrid;
    }
    
    private DomainSet buildDomainSet(WcpsCoverageMetadata wcpsCoverageMetadata) throws PetascopeException {
        
        GeneralGrid generalGrid = this.buildGeneralGrid(wcpsCoverageMetadata);
        DomainSet domainSet = new DomainSet(generalGrid);
        
        return domainSet;
    }

    /**
     * Build GMLCore for 1 input coverage.
     */
    public GMLCoreCIS11 build(WcpsCoverageMetadata wcpsCoverageMetadata) throws PetascopeException {
        
        Envelope envelope = this.buildEnvelope(wcpsCoverageMetadata);
        CoverageFunction coverageFunction = this.coverageFunctionService.buildCoverageFunction(wcpsCoverageMetadata);
        RangeType rangeType = this.rangeTypeService.buildRangeType(PREFIX_CIS11, LABEL_RANGE_TYPE_CIS11, NAMESPACE_CIS_11, wcpsCoverageMetadata);
        DomainSet domainSet = this.buildDomainSet(wcpsCoverageMetadata);
        Metadata metadata = new Metadata(this.coverageMetadataService.getMetadataContent(wcpsCoverageMetadata));
        
        GMLCoreCIS11 gmlCore = new GMLCoreCIS11(envelope, coverageFunction, domainSet, rangeType, metadata);

        return gmlCore;
    }
}
