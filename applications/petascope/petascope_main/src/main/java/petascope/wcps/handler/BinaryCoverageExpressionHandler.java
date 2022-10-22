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

import java.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.WCPSException;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;
import petascope.wcps.metadata.service.WcpsCoverageMetadataGeneralService;
import petascope.wcps.result.VisitorResult;
import petascope.wcps.result.WcpsResult;

/**
 * Translation node from wcps coverage list to rasql for the
 * LogicalCOverageExpressions Example:  <code>
 * $c1 OR $c2
 * </code> translates to  <code>
 * c1 OR c2
 * </code>
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
@Service
// Create a new instance of this bean for each request (so it will not use the old object with stored data)
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class BinaryCoverageExpressionHandler extends Handler {

    @Autowired
    private WcpsCoverageMetadataGeneralService wcpsCoverageMetadataService;
    
    public BinaryCoverageExpressionHandler() {
        
    }

    public BinaryCoverageExpressionHandler create(Handler firstCoverageExpressionChildHandler, Handler operatorStringScalarHandler, Handler secondCoverageExpressionChildHandler) {
        BinaryCoverageExpressionHandler result = new BinaryCoverageExpressionHandler();
        result.wcpsCoverageMetadataService = this.wcpsCoverageMetadataService;
        result.setChildren(Arrays.asList(firstCoverageExpressionChildHandler, operatorStringScalarHandler, secondCoverageExpressionChildHandler));
        
        return result;
    }
    
    public WcpsResult handle() throws PetascopeException {
        VisitorResult firstCoverageExpressionVisitorResult = this.getFirstChild().handle();
        String operator = ((WcpsResult)this.getSecondChild().handle()).getRasql();
        VisitorResult secondCoverageExpressionVisitorResult = this.getThirdChild().handle();
        
        WcpsResult result = this.handle(firstCoverageExpressionVisitorResult, operator, secondCoverageExpressionVisitorResult);
        return result;
    }

    private WcpsResult handle(VisitorResult firstCoverage, String operator, VisitorResult secondCoverage) throws WCPSException {
        String firstCoverageReturnedValue = "";
        String secondCoverageReturnedValue = "";
        
        if (firstCoverage instanceof WcpsResult) {
            firstCoverageReturnedValue = ((WcpsResult) firstCoverage).getRasql();
        } else {
            firstCoverageReturnedValue = firstCoverage.getResult();
        }
        
        if (secondCoverage instanceof WcpsResult) {
            secondCoverageReturnedValue = ((WcpsResult) secondCoverage).getRasql();
        } else {
            secondCoverageReturnedValue = secondCoverage.getResult();
        }
        
        
        
        //create the resulting rasql string
        String rasql = firstCoverageReturnedValue + " " + operator + " " + secondCoverageReturnedValue;
        //create the resulting metadata
        WcpsCoverageMetadata metadata = wcpsCoverageMetadataService.getResultingMetadata(firstCoverage.getMetadata(), secondCoverage.getMetadata(), 
                                                                                         firstCoverageReturnedValue, secondCoverageReturnedValue);
        
        WcpsResult result = new WcpsResult(metadata, rasql);
        return result;
    }
}
