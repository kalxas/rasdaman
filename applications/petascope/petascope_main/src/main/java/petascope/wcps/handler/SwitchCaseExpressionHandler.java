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

import org.apache.commons.lang3.StringUtils;
import petascope.wcps.result.WcpsResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import petascope.exceptions.PetascopeException;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;
import petascope.wcps.result.VisitorResult;

/**
 * Translation switch case class which returns range constructor expressions  <code>
 * for c in (mr) return encode(
 *  switch case c > 1000 return (char)5
 *  default return (char)6 , "png")
 * </code> returns  <code>
 * SELECT encode(case
 *  WHEN ((c)>(1000)) THEN ((octet)(2))
 *                     ELSE ((octet)(6))
 *  END, "GTiff", "xmin=0.0;xmax=255.0;ymin=0.0;ymax=210.0")
 *  from mr AS c where oid(c)=1025
 * </code>
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.de">Bang Pham Huu</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
@Service
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class SwitchCaseExpressionHandler extends Handler {
    
    public SwitchCaseExpressionHandler() {
        
    }
    
    public SwitchCaseExpressionHandler create(Handler switchCaseElementListHandler, Handler switchCaseDefaultValueHandler) {
        SwitchCaseExpressionHandler result = new SwitchCaseExpressionHandler();
        result.setChildren(Arrays.asList(switchCaseElementListHandler, switchCaseDefaultValueHandler));
        
        return result;
    }
    
    public WcpsResult handle() throws PetascopeException {
        WcpsResult switchCaseElementListResult = (WcpsResult)(this.getFirstChild().handle());
        WcpsResult returnValueElementResult = (WcpsResult)(this.getSecondChild().handle());
        
        WcpsResult result = this.handle(switchCaseElementListResult, returnValueElementResult);
        return result;
    }

    public WcpsResult handle(WcpsResult switchCaseElementListResult, WcpsResult returnValueElementResult) {
        String rasql = " CASE " + switchCaseElementListResult.getRasql() + " " + returnValueElementResult.getRasql() + " END ";
        
        WcpsCoverageMetadata metadata = switchCaseElementListResult.getMetadata();
        return new WcpsResult(metadata, rasql);
    }
}
