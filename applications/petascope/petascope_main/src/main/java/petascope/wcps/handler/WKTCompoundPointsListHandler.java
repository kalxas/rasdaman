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

import java.util.ArrayList;
import java.util.List;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import petascope.exceptions.PetascopeException;
import petascope.wcps.result.VisitorResult;
import petascope.wcps.subset_axis.model.WKTCompoundPoint;
import petascope.wcps.subset_axis.model.WKTCompoundPoints;

/**
// Handle LEFT_PARENTHESIS wktPoints RIGHT_PARENTHESIS (COMMA LEFT_PARENTHESIS wktPoints RIGHT_PARENTHESIS)*
// e.g: (20 30, 40 50), (40 60, 70 80) are considered as 2 WKTCompoundPoints (1 is 20 30 40 50, 2 is 40 60 70 80)
 */
@Service
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class WKTCompoundPointsListHandler extends Handler {
    
    public WKTCompoundPointsListHandler() {
        
    }
    
    public WKTCompoundPointsListHandler create(List<Handler> compoundPointHandlers) {
        WKTCompoundPointsListHandler result = new WKTCompoundPointsListHandler();
        result.setChildren(compoundPointHandlers);
        return result;
    }
    
    @Override
    public VisitorResult handle() throws PetascopeException {
        List<WKTCompoundPoint> compoundPoints = new ArrayList<>();
        
        for (Handler compoundPointHandler : this.getChildren()) {
            WKTCompoundPoint compoundPoint = (WKTCompoundPoint) compoundPointHandler.handle();
            compoundPoints.add(compoundPoint);
        }
        
        WKTCompoundPoints result = new WKTCompoundPoints(compoundPoints);
        return result;
    }
}
