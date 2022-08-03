package petascope.wcps.handler;

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


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.rasdaman.domain.cis.NilValue;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.WCPSException;
import petascope.util.ListUtil;
import petascope.wcps.metadata.model.RangeField;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;
import petascope.wcps.result.VisitorResult;
import petascope.wcps.result.WcpsResult;

/**
 * Handler for expression:
    rangeConstructorElement (COMMA rangeConstructorElement)*
    red: $c.red + 30,
    green: $c.green
   
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@Service
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class RangeConstructorElementListHandler extends Handler {
    
    public RangeConstructorElementListHandler() {
        
    }
    
    public RangeConstructorElementListHandler create(List<Handler> childHandlers) {
        RangeConstructorElementListHandler result = new RangeConstructorElementListHandler();
        result.setChildren(childHandlers);
        
        return result;
    }

    @Override
    public VisitorResult handle() throws PetascopeException {
        VisitorResult result = this.handle(this.getChildren());
        return result; 
    }
    
    private VisitorResult handle(List<Handler> childHandlers) throws PetascopeException {
        List<String> rasqlParts = new ArrayList<>();
        WcpsCoverageMetadata metadata = null;
        WcpsResult rangeFieldCoverageExpression = null;
        
        List<RangeField> rangeFields = new ArrayList<>();
        List<String> rangeFieldNames = new ArrayList<>();
        
        for (Handler childHandler : childHandlers) {
            rangeFieldCoverageExpression = (WcpsResult)childHandler.handle();
            WcpsCoverageMetadata rangeFieldMetadata = rangeFieldCoverageExpression.getMetadata();
            if (rangeFieldMetadata.getAxes().size() > 0 && metadata == null) {
                metadata = rangeFieldMetadata;
            }
            
            String rangeFieldName = rangeFieldMetadata.getRangeFields().get(0).getName();
            if (rangeFieldNames.contains(rangeFieldName)) {
                throw new WCPSException(ExceptionCode.InvalidRequest, "Range field: " + rangeFieldName + " is duplicated in a range constructor.");
            } else {
                rangeFieldNames.add(rangeFieldName);
            }
            
            rangeFields.addAll(rangeFieldMetadata.getRangeFields());
            
            String rasql = rangeFieldCoverageExpression.getRasql();
            rasqlParts.add(rasql);
        }
        
        if (metadata == null) {
            metadata = new WcpsCoverageMetadata();
        }
        metadata.setRangeFields(rangeFields);
        
        String rasql = ListUtil.join(rasqlParts, ", ");
        
        WcpsResult wcpsResult = new WcpsResult(metadata, rasql);
        return wcpsResult;
    }
    
}
