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
import java.util.ArrayList;
import java.util.List;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import petascope.exceptions.PetascopeException;
import petascope.wcps.result.VisitorResult;
import petascope.wcps.result.WcpsResult;

/**
 * Translation node from wcps coverage list to rasql for the FOR clause.
 * Example:  <code>
 * for $c1 in COL1
 * for $c2 in COL2
 * for $c3 in COL3
 * </code> translates to  <code>
 * FROM COL1 as c1, COL2 as c2, COL3 as c3
 * </code>
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
@Service
// Create a new instance of this bean for each request (so it will not use the old object with stored data)
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class LetClauseListHandler extends Handler {
    
    public LetClauseListHandler() {
        
    }
    
    public LetClauseListHandler create(List<Handler> childHandlers) {
        LetClauseListHandler result = new LetClauseListHandler();
        result.setChildren(childHandlers);
        return result;
    }
    
    
    public WcpsResult handle() throws PetascopeException {
        WcpsResult result = null;
        for (Handler childHandler : this.getChildren()) {
            result = (WcpsResult) childHandler.handle();
        }
        
        return result;
    }
}
