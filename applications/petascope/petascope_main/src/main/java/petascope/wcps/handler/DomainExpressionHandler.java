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
package petascope.wcps.handler;

import java.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import petascope.core.AxisTypes;
import petascope.exceptions.PetascopeException;
import petascope.util.CrsUtil;
import petascope.wcps.exception.processing.InvalidAxisInDomainExpressionException;
import static petascope.wcps.handler.AbstractOperatorHandler.checkOperandIsCoverage;
import petascope.wcps.metadata.model.Axis;
import petascope.wcps.metadata.model.NumericSlicing;
import petascope.wcps.metadata.model.NumericTrimming;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;
import petascope.wcps.metadata.service.CrsUtility;
import petascope.wcps.metadata.service.WcpsCoverageMetadataGeneralService;
import petascope.wcps.result.VisitorResult;
import petascope.wcps.result.WcpsMetadataResult;
import petascope.wcps.result.WcpsResult;

/**
 * Translator class for the domain(coverageExpression, axisLabel, CRS) operation
 * in wcps
 *
 * <code>
 * Return intervals in nativeCrs
 *      for c in (eobstest) return domain(c[Lat(20:30)], Lat, "http://localhost:8080/def/crs/EPSG/0/4326")
 * returns (20:30)
 * </code>
 *
 * <code>
 * Return intervals in gridCrs
 *      for c in (eobstest) return domain(c[Lat(20:30)], Lat, "CRS:1")
 * returns (91:110)
 * </code>
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
@Service
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class DomainExpressionHandler extends Handler {
    
    public static final String OPERATOR = "domain";
    
    @Autowired
    private WcpsCoverageMetadataGeneralService generateWcpsMetadataWithOneGridAxis;
    
    public DomainExpressionHandler() {
        
    }
    
    public DomainExpressionHandler create(Handler coverageExpressionHandler, StringScalarHandler axisNameHandler, StringScalarHandler axisCrsHandler) {
        DomainExpressionHandler result = new DomainExpressionHandler();
        result.setChildren(Arrays.asList(coverageExpressionHandler, axisNameHandler, axisCrsHandler));
        result.generateWcpsMetadataWithOneGridAxis = this.generateWcpsMetadataWithOneGridAxis;
        
        return result;
    }
    
    public VisitorResult handle() throws PetascopeException {
        WcpsResult coverageExpression = (WcpsResult) this.getFirstChild().handle();
        String axisName = ((WcpsResult)this.getSecondChild().handle()).getRasql();
        String axisCrs = ((WcpsResult)this.getThirdChild().handle()).getRasql();
        
        VisitorResult result = this.handle(coverageExpression, axisName, axisCrs);
        return result;       
    }
    

    /**
     * Constructor for the class
     *
     * @param coverageExpression the coverage to get ($c,axisName, CRS)
     * @param axisName the name of axis (e.g Lat, Long,...)
     * @param axisCrs
     * @return
     */
    private VisitorResult handle(WcpsResult coverageExpression, String axisName, String axisCrs) throws PetascopeException {
        
        checkOperandIsCoverage(coverageExpression, OPERATOR);
        
        WcpsMetadataResult metadataResult = null;
        if (axisName != null) {
            if (axisCrs == null) {
                axisCrs = coverageExpression.getMetadata().getAxisByName(axisName).getNativeCrsUri();
            }
            
            // if axisName and axisCrs is belonge to coverageExpression then can just get the bounding of axis from coverageExpression
            if (isValid(coverageExpression, axisName, axisCrs)) {
                String result = getDomainByAxisCrs(coverageExpression, axisName, axisCrs);

                String coverageId = coverageExpression.getMetadata().getCoverageName();
                Axis axis = coverageExpression.getMetadata().getAxisByName(axisName);
                WcpsCoverageMetadata tmpMetadata = this.generateWcpsMetadataWithOneGridAxis.generateWcpsMetadataWithOneGridAxis(coverageId, axis);

                metadataResult = new WcpsMetadataResult(tmpMetadata, result);            
            } else {
                throw new InvalidAxisInDomainExpressionException(axisName, axisCrs);
            }
        } else {
            WcpsCoverageMetadata metadata = coverageExpression.getMetadata();
            String result = metadata.getCoverageGeoBoundsRepresentation();
            metadataResult = new WcpsMetadataResult(metadata, result);
        }
        
        return metadataResult;
    }
    
    /**
     * Get the domain for the axis with the correct crsURI from
     * coverageExpression
     *
     * @param coverageExpression
     * @param axisName
     * @param crsUri
     * @return
     */
    private String getDomainByAxisCrs(WcpsResult coverageExpression, String axisName, String axisCrs) throws PetascopeException {
        String result = "";

        Axis axis = coverageExpression.getMetadata().getAxisByName(axisName);

        if (axis.getGridBounds() instanceof NumericTrimming) {
            // Trimming
            String lowBound = "";
            String highBound = "";

            // Rasql axis CRS
            if (CrsUtil.isGridCrs(axisCrs)) {
                lowBound = ((NumericTrimming) axis.getGridBounds()).getLowerLimit().toPlainString();
                highBound = ((NumericTrimming) axis.getGridBounds()).getUpperLimit().toPlainString();
            } else if (axis.getAxisType().equals(AxisTypes.T_AXIS)) {
                lowBound = axis.getLowerGeoBoundRepresentation();
                highBound = axis.getUpperGeoBoundRepresentation();
            } else if (axis.getAxisType().equals(AxisTypes.X_AXIS) 
                    || axis.getAxisType().equals(AxisTypes.Y_AXIS)) {
                // geo-referenced axis , e.g: Lat, Long or Index2D(*)
                lowBound = ((NumericTrimming) axis.getGeoBounds()).getLowerLimit().toPlainString();
                highBound = ((NumericTrimming) axis.getGeoBounds()).getUpperLimit().toPlainString();
            } else {
                // Unknow axisType
                lowBound = ((NumericTrimming) axis.getGeoBounds()).getLowerLimit().toPlainString();
                highBound = ((NumericTrimming) axis.getGeoBounds()).getUpperLimit().toPlainString();
            }

            result = TRIMMING_TEMPLATE.replace("$lowBound", lowBound).replace("$highBound", highBound);
        } else {
            // Slicing
            String bound = "";

            // Grid axis CRS
            if (CrsUtil.isGridCrs(axisCrs)) {
                bound = ((NumericSlicing) axis.getGridBounds()).getBound().toPlainString();
            } else if (axis.getAxisType().equals(AxisTypes.T_AXIS)) {
                // Time - now only in grid axis
                bound = ((NumericSlicing) axis.getGridBounds()).getBound().toPlainString();
            } else if (axis.getAxisType().equals(AxisTypes.X_AXIS)
                    || axis.getAxisType().equals(AxisTypes.Y_AXIS)) {
                // geo-referenced axis (geoBounds), e.g: Lat, Long or Index2D(*)
                bound = ((NumericSlicing) axis.getGeoBounds()).getBound().toPlainString();
            } else {
                // Unknow axisType, use grid bounds
                bound = ((NumericSlicing) axis.getGridBounds()).getBound().toPlainString();
            }

            result = SLICING_TEMPLATE.replace("$lowBound", bound);
        }

        return result;
    }

    /**
     * check if axisCRS is belonged to axisName (e.g Lat is "4326" and "CRS:1")
     *
     * @param coverageExpression
     * @param axisName
     * @param crsUri
     * @return
     */
    private boolean isValid(WcpsResult coverageExpression, String axisName, String crsUri) {
        // check if axisName belonged to coverageExpression first
        for (Axis axis : coverageExpression.getMetadata().getAxes()) {
            // if coverage contains axisName then check the crsUri belonged to axis also
            if (CrsUtil.axisLabelsMatch(axis.getLabel(), axisName)) {
                String axisCrsCode = CrsUtil.CrsUri.getCode(axis.getNativeCrsUri());
                String inputCrsCode = CrsUtil.CrsUri.getCode(crsUri);

                if (CrsUtil.isGridCrs(crsUri)) {
                    // CRS:1 always belonged to axis
                    return true;
                } else if (axisCrsCode.equals(inputCrsCode)) {
                    // e.g: 4326
                    return true;
                } else {
                    // e.g: Lat:"4326" and Lat:"3857" is not identical
                    return false;
                }
            }
        }
        return false;
    }

    private final String TRIMMING_TEMPLATE = "($lowBound:$highBound)";
    private final String SLICING_TEMPLATE = "($lowBound)";
}
