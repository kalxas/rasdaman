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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import petascope.exceptions.PetascopeException;
import petascope.wcps.metadata.model.CrsTransformTargetGeoXYResolutions;
import petascope.wcps.result.VisitorResult;
import petascope.wcps.result.WcpsMetadataResult;
import static petascope.wcps.result.WcpsResult.getResult;

/**
Handler for the target resolutions in crsTransform expression:

//  crsTransform($c, "EPSG:4326", {interpolation},
*      { Lat:0.5, Lon:domain($c, Lon).resolution) } // this part
 */
@Service
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class CrsTransformTargetGeoXYResolutionsHandler extends Handler {
    
    public CrsTransformTargetGeoXYResolutionsHandler() {
        
    }
    
    public CrsTransformTargetGeoXYResolutionsHandler create(StringScalarHandler geoResolutionAxisLabelX, Handler scalarExpressionGeoResolutionAxisX,
                                    StringScalarHandler geoResolutionAxisLabelY, Handler scalarExpressionGeoResolutionAxisY
                                    ) {
        CrsTransformTargetGeoXYResolutionsHandler result = new CrsTransformTargetGeoXYResolutionsHandler();
        result.setChildren(Arrays.asList(geoResolutionAxisLabelX, scalarExpressionGeoResolutionAxisX,
                                        geoResolutionAxisLabelY, scalarExpressionGeoResolutionAxisY));
        return result;
    }    

    @Override
    public VisitorResult handle() throws PetascopeException {
        String geoResolutionAxisLabelX = getResult(this.getFirstChild().handle());
        String geoResolutionX = getResult(this.getSecondChild().handle());
        
        String geoResolutionAxisLabelY = getResult(this.getThirdChild().handle());
        String geoResolutionY = getResult(this.getFourthChild().handle());
        
        CrsTransformTargetGeoXYResolutions tmpObject = new CrsTransformTargetGeoXYResolutions(geoResolutionAxisLabelX, geoResolutionX, geoResolutionAxisLabelY, geoResolutionY);
               
        WcpsMetadataResult result = new WcpsMetadataResult(tmpObject);
        return result;
    }
}
