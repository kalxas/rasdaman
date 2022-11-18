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
package petascope.wcps.handler;

import petascope.util.CrsUtil;

import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.WCPSException;
import petascope.wcps.metadata.model.Axis;
import petascope.wcps.metadata.model.CrsTransformTargetGeoXYBoundingBox;
import petascope.wcps.metadata.model.CrsTransformTargetGeoXYResolutions;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;
import petascope.wcps.result.WcpsMetadataResult;
import petascope.wcps.result.WcpsResult;
import static petascope.wcps.result.WcpsResult.getResult;

/**
 * Class to handle an crsTransform coverage expression  <code>
// Handle crsTransform($COVERAGE_EXPRESSION, "CRS", {$INTERPOLATION})
// e.g: crsTransform(c, "EPSG:4326", { near })
 *
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
@Service
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class CrsTransformShorthandHandler extends Handler {
    
    public static final String OPERATOR = "crsTransform";
    @Autowired
    private CrsTransformHandler crsTransformHandler;
    
    public CrsTransformShorthandHandler() {
        
    }
    
    public CrsTransformShorthandHandler create(Handler coverageExpressionHandler,
                                    StringScalarHandler crsXYHandler,
                                    StringScalarHandler interpolationTypeHandler,
                                    CrsTransformTargetGeoXYResolutionsHandler targetGeoXYResolutionsHandler,
                                    CrsTransformTargetGeoXYBoundingBoxHandler targetGeoXYBoundingBoxHandler
                                    ) {
        CrsTransformShorthandHandler result = new CrsTransformShorthandHandler();
        result.setChildren(Arrays.asList(coverageExpressionHandler, crsXYHandler, interpolationTypeHandler, 
                                        targetGeoXYResolutionsHandler,
                                        targetGeoXYBoundingBoxHandler));
        result.crsTransformHandler = crsTransformHandler;
        
        return result;
    }
    
    @Override
    public WcpsResult handle() throws PetascopeException {
        WcpsResult coverageExpression = ((WcpsResult)this.getFirstChild().handle());
        WcpsCoverageMetadata metadata = coverageExpression.getMetadata();
        
        String errorMessage = "The coverage operand of crsTransform() must be 2D and has spatial X and Y geo axes.";
        if (!metadata.hasXYAxes()) {
            throw new WCPSException(ExceptionCode.InvalidRequest, errorMessage);
        } else if (metadata.getAxes().size() != 2) {
            throw new WCPSException(ExceptionCode.InvalidRequest, errorMessage);
        }
        
        List<Axis> geoXYAxes = metadata.getXYAxes();
        String axisLabelX = geoXYAxes.get(0).getLabel();
        String axisLabelY = geoXYAxes.get(1).getLabel();
        
        String crsXY = ((WcpsResult)this.getSecondChild().handle()).getRasql();
        String outputCRS = "";
        if (crsXY.contains("/")) {
            // e.g. http://localhost:8080/def/crs/EPSG/0/4326
            outputCRS = crsXY;
        } else {
            // e.g. EPSG:4326
            outputCRS = CrsUtil.getFullCRSURLByAuthorityCode(crsXY);

        }        
        
        String interpolationType = ((WcpsResult)this.getThirdChild().handle()).getRasql();
        
        
        CrsTransformTargetGeoXYResolutions targetGeoXYResolutions = null;
        
        if (this.getFourthChild() != null) {
            WcpsMetadataResult wcpsMetadataResult = (WcpsMetadataResult) this.getFourthChild().handle();
            if (wcpsMetadataResult.getTmpObject() instanceof CrsTransformTargetGeoXYResolutions) {
                targetGeoXYResolutions = (CrsTransformTargetGeoXYResolutions) wcpsMetadataResult.getTmpObject();
            }
        }
        
        CrsTransformTargetGeoXYBoundingBox targetGeoXYBBox = null;
        if (this.getFifthChild()!= null) {
            WcpsMetadataResult wcpsMetadataResult = (WcpsMetadataResult) this.getFifthChild().handle();
            if (wcpsMetadataResult.getTmpObject() instanceof CrsTransformTargetGeoXYBoundingBox) {
                targetGeoXYBBox = (CrsTransformTargetGeoXYBoundingBox) wcpsMetadataResult.getTmpObject();
            }
        }        
        
        
        WcpsResult result = this.crsTransformHandler.handle(coverageExpression, axisLabelX, outputCRS, axisLabelY, outputCRS, interpolationType,
                                                            targetGeoXYResolutions,
                                                            targetGeoXYBBox);
        return result;
    }

}
