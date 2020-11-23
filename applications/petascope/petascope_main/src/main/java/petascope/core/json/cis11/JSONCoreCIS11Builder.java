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
package petascope.core.json.cis11;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.core.gml.metadata.model.CoverageMetadata;
import petascope.core.json.cis11.model.domainset.AbstractAxis;
import petascope.core.json.cis11.model.rangetype.RangeType;
import petascope.core.json.cis11.model.rangetype.JSONRangeTypeService;
import petascope.core.json.cis11.model.domainset.DomainSet;
import petascope.core.json.cis11.model.domainset.GeneralGrid;
import petascope.core.json.cis11.model.domainset.GridLimits;
import petascope.core.json.cis11.model.domainset.IndexAxis;
import petascope.core.json.cis11.model.domainset.IrregularAxis;
import petascope.core.json.cis11.model.domainset.RegularAxis;
import petascope.core.json.cis11.model.envelope.AxisExtent;
import petascope.core.json.cis11.model.envelope.Envelope;
import petascope.core.json.cis11.model.metadata.Metadata;
import petascope.exceptions.PetascopeException;
import petascope.util.BigDecimalUtil;
import petascope.util.ListUtil;
import petascope.util.StringUtil;
import petascope.wcps.metadata.model.Axis;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;

/**
 * Build GMLCore as main part of WCS DescribeCoverage/GetCoverage in
 * application/json result for CIS 1.1 coverages
 * 
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@Service
public class JSONCoreCIS11Builder {

    @Autowired
    private JSONRangeTypeService jsonRangeTypeService;
    
    /**
     * Build JsonCore for 1 input coverage.
     */
    public JSONCoreCIS11 build(WcpsCoverageMetadata wcpsCoverageMetadata) throws PetascopeException {
        
        Envelope envelope = this.buildEnvelope(wcpsCoverageMetadata);
        RangeType rangeType = this.jsonRangeTypeService.buildRangeType(wcpsCoverageMetadata);
        DomainSet domainSet = this.buildDomainSet(wcpsCoverageMetadata);
        
        CoverageMetadata coverageMetadata = wcpsCoverageMetadata.getCoverageMetadata();
        coverageMetadata.stripEmptyProperties();
        
        Metadata metadata = new Metadata(coverageMetadata);
        if (coverageMetadata.isIsNotDeserializable()) {
            metadata = null;
        } 
        
        JSONCoreCIS11 jsonCore = new JSONCoreCIS11(envelope, domainSet, rangeType, metadata);

        return jsonCore;
    }
    
    
    // ################## Build Envelope
    
    private Envelope buildEnvelope(WcpsCoverageMetadata wcpsCoverageMetadata) throws PetascopeException {
        
        String geoSrsName = wcpsCoverageMetadata.getCrsUri();
        List<String> axisLabels = wcpsCoverageMetadata.getGeoAxisNamesAsList();
        List<AxisExtent> axisExtents = new ArrayList<>(); 

        for (Axis axis : wcpsCoverageMetadata.getAxes()) {
            String axisLabel = axis.getLabel();
            String uomLabel = axis.getAxisUoM();
            String geoLowerBound = StringUtil.stripFirstAndLastQuotes(axis.getLowerGeoBoundRepresentation());
            String geoUpperBound = StringUtil.stripFirstAndLastQuotes(axis.getUpperGeoBoundRepresentation());
            
            AxisExtent axisExtent = new AxisExtent(axisLabel, uomLabel, geoLowerBound, geoUpperBound);
            if (!axis.isTimeAxis()) {
                BigDecimal geoLowerBoundNumber = new BigDecimal(geoLowerBound);
                BigDecimal geoUpperBoundNumber = new BigDecimal(geoUpperBound);
                
                axisExtent = new AxisExtent(axisLabel, uomLabel, geoLowerBoundNumber, geoUpperBoundNumber);
            }
            axisExtents.add(axisExtent);
        }

        Envelope envelope = new Envelope(geoSrsName, axisLabels, axisExtents);
        
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
            String geoLowerBound = StringUtil.stripFirstAndLastQuotes(axis.getLowerGeoBoundRepresentation());
            String geoUpperBound = StringUtil.stripFirstAndLastQuotes(axis.getUpperGeoBoundRepresentation());
            BigDecimal axisResolution = axis.getResolution();

            if (axis instanceof petascope.wcps.metadata.model.RegularAxis) {
                RegularAxis regularAxis = new RegularAxis(axisLabel, uomLabel, geoLowerBound, geoUpperBound, axisResolution);
                if (!axis.isTimeAxis()) {
                    regularAxis = new RegularAxis(axisLabel, uomLabel, new BigDecimal(geoLowerBound), new BigDecimal(geoUpperBound), axisResolution);
                }
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
                List<String> coefficientsTmp = ((petascope.wcps.metadata.model.IrregularAxis) axis).getRepresentationCoefficientsList();
                List<String> coefficients = ListUtil.stripQuotes(coefficientsTmp);
                
                List<Object> coefficientObjects = new ArrayList<>();
                for (String value : coefficients) {
                    if (axis.isTimeAxis()) {
                        coefficientObjects.add(value);
                    } else {
                        coefficientObjects.add(new BigDecimal(value));
                    }
                }
                
                IrregularAxis irregularAxis = new IrregularAxis(axisLabel, uomLabel, coefficientObjects);
                
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
            long gridLowerBound = axis.getGridBounds().getLowerLimit().longValue();
            long gridUpperBound = axis.getGridBounds().getUpperLimit().longValue();
            
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
        List<String> axisLabels = wcpsCoverageMetadata.getGridAxisNamesAsList();
        List<IndexAxis> indexAxes = this.buildIndexAxes(wcpsCoverageMetadata);
        
        GridLimits gridLimits = new GridLimits(gridSrsName, axisLabels, indexAxes);
        
        return gridLimits;
    }
    
    /**
     * Build GeneralGrid for DomainSet.
     */
    private GeneralGrid buildGeneralGrid(WcpsCoverageMetadata wcpsCoverageMetadata) throws PetascopeException {
        
        String geoSrsName = wcpsCoverageMetadata.getCrsUri();
        List<String> geoAxisLabels = wcpsCoverageMetadata.getGeoAxisNamesAsList();
        List<RegularAxis> regularAxes = this.buildRegularAxes(wcpsCoverageMetadata);
        List<IrregularAxis> irregularAxes = this.buildIrregularAxes(wcpsCoverageMetadata);
        GridLimits gridLimits = this.buildGridLimits(wcpsCoverageMetadata);
        
        List<AbstractAxis> geoAxes = new ArrayList<>();
        for (Axis axis : wcpsCoverageMetadata.getAxes()) {
            
            for (RegularAxis regularAxis : regularAxes) {
                if (axis.getLabel().equals(regularAxis.getAxisLabel())) {
                    geoAxes.add(regularAxis);
                    break;
                }
            }
            
            for (IrregularAxis irregularAxis : irregularAxes) {
                if (axis.getLabel().equals(irregularAxis.getAxisLabel())) {
                    geoAxes.add(irregularAxis);
                    break;
                }
            }
        }
        
        GeneralGrid generalGrid = new GeneralGrid(geoSrsName, geoAxisLabels, geoAxes, gridLimits);
        
        return generalGrid;
    }
    
    private DomainSet buildDomainSet(WcpsCoverageMetadata wcpsCoverageMetadata) throws PetascopeException {
        
        GeneralGrid generalGrid = this.buildGeneralGrid(wcpsCoverageMetadata);
        DomainSet domainSet = new DomainSet(generalGrid);
        
        return domainSet;
    }

}
