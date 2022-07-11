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

import petascope.wcps.result.WcpsResult;

import java.util.Arrays;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import petascope.exceptions.PetascopeException;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;

/**
 * Translation class for the range constructor expressions  <code>
 * for c in (COV) return encode( {red: c.red;    green: c.green;    blue: c.blue }, "png")
 * </code> returns  <code>
 * select  { c.red, c.green, c.blue } from COV as c
 * </code>
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
@Service
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class RangeConstructorHandler extends Handler {
    
    public RangeConstructorHandler() {
        
    }
    
    public RangeConstructorHandler create(Handler rangeConstructorElementListHandler) {
        RangeConstructorHandler result = new RangeConstructorHandler();
        result.setChildren(Arrays.asList(rangeConstructorElementListHandler));
        
        return result;
    }
    
    public WcpsResult handle() throws PetascopeException {
        WcpsResult coverageExpression = (WcpsResult) this.getFirstChild().handle();
        WcpsResult result = this.handle(coverageExpression);
        
        return result;
    }

    private WcpsResult handle(WcpsResult coverageExpression) {
        WcpsCoverageMetadata metadata = coverageExpression.getMetadata();
        
        String rasql = "{ " +  coverageExpression.getRasql() + " }";
        return new WcpsResult(metadata, rasql);
    }
}
