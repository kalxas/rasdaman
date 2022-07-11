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
import petascope.util.ListUtil;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;
import petascope.wcps.result.VisitorResult;
import petascope.wcps.result.WcpsResult;

/**
 * Handler for expression
    switchCaseElementList: switchCaseElement (switchCaseElement)*;
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@Service
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class SwitchCaseElementListHandler extends Handler {
    
    public SwitchCaseElementListHandler() {
        
    }
    
    public SwitchCaseElementListHandler create(List<Handler> switchCaseElementHandlers) {
        SwitchCaseElementListHandler result = new SwitchCaseElementListHandler();
        result.setChildren(switchCaseElementHandlers);
        
        return result;
    }

    @Override
    public VisitorResult handle() throws PetascopeException {
        VisitorResult result = this.handle(this.getChildren());
        return result;
    }
    
    private VisitorResult handle(List<Handler> switchCaseElementHandlers) throws PetascopeException {
        List<String> rasqlParts = new ArrayList<>();
        WcpsCoverageMetadata metadata = null;
        
        for (Handler handler : switchCaseElementHandlers) {
            WcpsResult result = ((WcpsResult) handler.handle());
            String rasqlPart = result.getRasql();
            
            WcpsCoverageMetadata metadataTmp = result.getMetadata();
            if (metadataTmp != null) {
                metadata = metadataTmp;
            }
            
            rasqlParts.add(rasqlPart);
        }
        
        // e.g. WHEN ... RETURN {0,1,2} WHEN ... RETURN 1
        String rasql = ListUtil.join(rasqlParts, " ");
        WcpsCoverageMetadata metadataResult = metadata;
        
        return new WcpsResult(metadataResult, rasql);
    }
    
}
