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
import java.util.List;
import java.util.Map;
import org.rasdaman.domain.cis.NilValue;
import org.springframework.stereotype.Service;
import petascope.exceptions.PetascopeException;
import petascope.wcps.metadata.model.Axis;
import petascope.wcps.metadata.model.RangeField;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;

/**
 * Translation class for the range constructor expressions which is used inside
 * switch-case expression  <code>
 * switch
 * case c > 1000 return """{red: 107; green:17; blue:68}"""
 * default return {red: 150; green:103; blue:14}
 * </code> returns <code>
 * {107c, 17c, 68c}
 * </code>
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.de">Bang Pham Huu</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
@Service
public class RangeConstructorSwitchCaseHandler extends AbstractOperatorHandler {
    
    public WcpsResult handle(Map<String, WcpsResult> fieldStructure) throws PetascopeException {
        List<String> translatedFields = new ArrayList();
        
        List<RangeField> rangeFields = new ArrayList<>();
        for (Map.Entry<String, WcpsResult> entry : fieldStructure.entrySet()) {
            String scalarValue = entry.getValue().getRasql();

            String result = scalarValue;
            
            translatedFields.add(result);

            // we create range field for the coverage metadata
            RangeField rangeField = new RangeField(RangeField.DATA_TYPE, entry.getKey(), "", new ArrayList<NilValue>(),
                    RangeField.UOM_CODE, "", null);
            rangeFields.add(rangeField);
        }

        List<List<NilValue>> nilValues = new ArrayList<>();

        //for now no metadata is forwarded, but it can be constructed from the fields (we need this to set extrametadata with netcdf)
        WcpsCoverageMetadata metadata = new WcpsCoverageMetadata(null, null, null, new ArrayList<Axis>(), "", rangeFields, nilValues, "", new ArrayList<Axis>());
        
        // {red: 100, green: 100, blue: 20} -> {100c, 100c, 20c}
        String rasql = "{" + StringUtils.join(translatedFields, ", ") + "}";
        
        return new WcpsResult(metadata, rasql);
    }
}
