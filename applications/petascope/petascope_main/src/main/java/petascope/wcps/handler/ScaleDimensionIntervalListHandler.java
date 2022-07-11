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

import java.util.ArrayList;
import java.util.List;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import petascope.exceptions.PetascopeException;
import petascope.wcps.result.VisitorResult;
import petascope.wcps.subset_axis.model.AbstractWcpsScaleDimension;
import petascope.wcps.subset_axis.model.WcpsScaleDimensionIntevalList;

/**
 Handler for expression:
// scaleDimensionIntervalElement (COMMA scaleDimensionIntervalElement)* 
// e.g: [i(0.5),j(0.5)]
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@Service
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ScaleDimensionIntervalListHandler extends Handler {
    
    public ScaleDimensionIntervalListHandler() {
        
    }
    
    public ScaleDimensionIntervalListHandler create(List<Handler> childHandlers) {
        ScaleDimensionIntervalListHandler result = new ScaleDimensionIntervalListHandler();
        result.setChildren(childHandlers);
        
        return result;
    }

    @Override
    public VisitorResult handle() throws PetascopeException {
        VisitorResult result = this.handle(this.getChildren());
        return result;
    }
    
    private VisitorResult handle(List<Handler> handlers) throws PetascopeException {
        // scaleDimensionIntervalElement (COMMA scaleDimensionIntervalElement)* 
        // e.g: [i(0.5),j(0.5)]
        List<AbstractWcpsScaleDimension> intervalList = new ArrayList<>();
        
        for (Handler handler : handlers) {
            AbstractWcpsScaleDimension scaleDimension = (AbstractWcpsScaleDimension) handler.handle();
            intervalList.add(scaleDimension);
        }
        
        WcpsScaleDimensionIntevalList result = new WcpsScaleDimensionIntevalList(intervalList);
        return result;
    }
    
}
