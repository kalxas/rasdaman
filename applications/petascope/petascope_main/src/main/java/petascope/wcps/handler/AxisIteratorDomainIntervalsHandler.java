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
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import petascope.exceptions.PetascopeException;
import petascope.util.CrsUtil;
import petascope.util.StringUtil;
import petascope.wcps.metadata.model.Axis;
import petascope.wcps.result.VisitorResult;
import petascope.wcps.result.WcpsMetadataResult;
import petascope.wcps.result.WcpsResult;
import petascope.wcps.subset_axis.model.AxisIterator;
import petascope.wcps.subset_axis.model.WcpsSubsetDimension;
import petascope.wcps.subset_axis.model.WcpsTrimSubsetDimension;

/** 
 * Class to handler WCPS AxisIterator:
 * 
// coverageVariableName axisName LEFT_PARENTHESIS  domainIntervals RIGHT_PARENTHESIS
// e.g: $px x (imageCrsdomain(c[Lat(0:20)]), Lat)
// e.g: $px x (imageCrsdomain(c[Long(0)], Lat[(0:20)]))
// e.g: $px x (domain(c[Lat(0:20)], Lat, "http://.../4326"))
// return x in (50:80)
 * 
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@Service
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class AxisIteratorDomainIntervalsHandler extends Handler {
    
    public AxisIteratorDomainIntervalsHandler() {
        
    }
    
    public AxisIteratorDomainIntervalsHandler create(Handler axisIteratorNameHandler, 
                                                    Handler axisNameHandler,
                                                    Handler domainIntervalsHandler) {
        AxisIteratorDomainIntervalsHandler result = new AxisIteratorDomainIntervalsHandler();
        result.setChildren(Arrays.asList(axisIteratorNameHandler, axisNameHandler, domainIntervalsHandler));
        
        return result;
    }
    
    public VisitorResult handle() throws PetascopeException {
        // e.g. $px
        String axisIteratorName = ((WcpsResult)this.getFirstChild().handle()).getRasql();
        // .e.g X
        String axisName = ((WcpsResult)this.getSecondChild().handle()).getRasql();
        WcpsMetadataResult domainIntervalsMetadataResult = (WcpsMetadataResult)this.getThirdChild().handle();
        
        VisitorResult result = this.handle(axisIteratorName, axisName, domainIntervalsMetadataResult);
        return result;
    }

    private VisitorResult handle(String axisIteratorName, String axisName, WcpsMetadataResult domainIntervalsMetadataResult) throws PetascopeException {
        // coverageVariableName axisName LEFT_PARENTHESIS  domainIntervals RIGHT_PARENTHESIS
        // e.g: $px x (imageCrsdomain(c[Lat(0:20)]), Lat)
        // e.g: $px x (imageCrsdomain(c[Long(0)], Lat[(0:20)]))
        // e.g: $px x (domain(c[Lat(0:20)], Lat, "http://.../4326"))
        // return x in (50:80)
        Axis axis = domainIntervalsMetadataResult.getMetadata().getAxes().get(0);
        
        String rasqlInterval = domainIntervalsMetadataResult.getResult();
        // remove the () from (50:80) and extract lower and upper grid bounds
        String[] gridBounds = StringUtil.stripParentheses(rasqlInterval).split(":");

        // NOTE: it expects that "domainIntervals" will only return 1 trimming domain in this case (so only 1D)

        WcpsSubsetDimension trimSubsetDimension = new WcpsTrimSubsetDimension(axis.getLabel(), CrsUtil.GRID_CRS,
                        gridBounds[0], gridBounds[1]);

        AxisIterator axisIterator = new AxisIterator(axisIteratorName, axisName, trimSubsetDimension);
        return axisIterator;
    }
    
}
