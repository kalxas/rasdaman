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
 * Copyright 2003 - 2022 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcps.handler;

import java.util.Arrays;
import java.util.List;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import petascope.exceptions.PetascopeException;
import petascope.util.StringUtil;
import petascope.wcps.metadata.model.CrsTransformTargetGeoXYBoundingBox;
import petascope.wcps.result.VisitorResult;
import petascope.wcps.result.WcpsMetadataResult;
import petascope.wcps.subset_axis.model.DimensionIntervalList;
import petascope.wcps.subset_axis.model.WcpsSubsetDimension;

/**
Handler for the target resolutions in crsTransform expression:

//  crsTransform($c, "EPSG:4326", {interpolation},
*      { Lat:0.5, Lon:domain($c, Lon).resolution) },
*      { Lat (0:3), Lon (30:50 } or { domain ($c) } // this part
 */
@Service
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class CrsTransformTargetGeoXYBoundingBoxHandler extends Handler {
    
    public CrsTransformTargetGeoXYBoundingBoxHandler() {
        
    }
    
    public CrsTransformTargetGeoXYBoundingBoxHandler create(Handler dimensionIntervalListHandler, Handler domainExpressionHandler) {
        CrsTransformTargetGeoXYBoundingBoxHandler result = new CrsTransformTargetGeoXYBoundingBoxHandler();
        result.setChildren(Arrays.asList(dimensionIntervalListHandler, domainExpressionHandler));
        return result;
    }    

    @Override
    public VisitorResult handle() throws PetascopeException {
        // CrsTransform target bbox can be fetched from:
        // - {Lat(30:50), Lon(60:70)}
        // - {domain($c)}
        DimensionIntervalList dimensionIntervalListExpression = null;
        WcpsMetadataResult domainExpressionResult = null;
        if (this.getFirstChild() != null) {
            dimensionIntervalListExpression = (DimensionIntervalList) this.getFirstChild().handle();
        } else if (this.getSecondChild() != null) {
            domainExpressionResult = (WcpsMetadataResult)this.getSecondChild().handle();
        }
        
        String axisLabelX = null, trimSubsetX = null, axisLabelY = null, trimSubsetY = null;
        if (domainExpressionResult != null) {
            // output of domain($c)
            // e.g. Lat(30:50),Lon(60:70)
            String[] tmps = domainExpressionResult.getResult().split(",");
            
            axisLabelX = tmps[0].split("\\(")[0];
            trimSubsetX = StringUtil.extractStringsBetweenParentheses(tmps[0]).get(0);
            
            axisLabelY = tmps[1].split("\\(")[0];
            trimSubsetY = StringUtil.extractStringsBetweenParentheses(tmps[1]).get(0);
        } else if (dimensionIntervalListExpression != null) {
            // it comes from specified intervals
            // e.g. {Lat(30:50), Lon(50:60)}
            List<WcpsSubsetDimension> geoXYSubsetDimensions = dimensionIntervalListExpression.getIntervals();
            axisLabelX = geoXYSubsetDimensions.get(0).getAxisName();
            trimSubsetX = geoXYSubsetDimensions.get(0).getStringBounds();
            
            axisLabelY = geoXYSubsetDimensions.get(1).getAxisName();
            trimSubsetY = geoXYSubsetDimensions.get(1).getStringBounds();
        }
        
        CrsTransformTargetGeoXYBoundingBox tmpObject = new CrsTransformTargetGeoXYBoundingBox(axisLabelX, trimSubsetX, axisLabelY, trimSubsetY);
               
        WcpsMetadataResult result = new WcpsMetadataResult(tmpObject);
        return result;
    }
}
