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
import org.springframework.stereotype.Service;
import petascope.wcps.metadata.model.RangeField;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;

/**
 * Translation switch case class which returns range constructor expressions  <code>
 * for c in (mr) return encode(switch
 * case c > 1000 return {red: 107; green:17; blue:68, r1:30, r2:50}
 * default return {red: 150; green:103; blue:14, r1:20, r2:50}
 * , "png")
 * </code> returns  * <code>
SELECT encode(case
 * WHEN ((c)>(1000)) THEN ((107) * {1c,0c,0c,0c,0c} + (17) * {1c,0c,0c,0c,0c} + (68) * {1c,0c,0c,0c,0c})
 * ELSE ((150) * {1c,0c,0c,0c,0c} + (103) * {1c,0c,0c,0c,0c} + (14) * {1c,0c,0c,0c,0c})
 * END,
 * "GTiff", "xmin=0.0;xmax=255.0;ymin=0.0;ymax=210.0")
 * from mr AS c where oid(c)=1025
 * </code>
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.de">Bang Pham Huu</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
@Service
public class SwitchCaseRangeConstructorExpression extends AbstractOperatorHandler {

    public WcpsResult handle(List<WcpsResult> booleanResults, List<WcpsResult> rangeResults) {
        List<String> translatedFields = new ArrayList();
        // NOTE: if booleanResults = rangeResults -> no use of "default"
        //          booleanResults = rangeResults - 1 -> use of "default" (default return { Range })

        for (int i = 0; i < booleanResults.size(); i++) {
            String booleanResult = booleanResults.get(i).getRasql();
            String rangeResult = rangeResults.get(i).getRasql();

            String result = TEMPLATE_WHEN_THEN.replace("$booleanExpr", booleanResult)
                    .replace("$rangeExpr", rangeResult);
            translatedFields.add(result);
        }

        // combine switch case to a string
        String whenThenStr = StringUtils.join(translatedFields, " ");
        String elseStr = "";

        // switch case has default also
        String rangeResult = rangeResults.get(rangeResults.size() - 1).getRasql();
        elseStr = TEMPLATE_ELSE.replace("$rangeExpr", rangeResult);

        String rasql = TEMPLATE.replace("$whenThenExpr", whenThenStr)
                .replace("$elseExpr", elseStr);

        // This is needed a coverage metadata from boolean coverage epxression
        WcpsCoverageMetadata metadata = booleanResults.get(0).getMetadata();
        // NOTE: it will use the range fields from the band names of case expression, not the original range fields from coverage
        metadata.getRangeFields().clear();
        List<RangeField> rangeFields = rangeResults.get(0).getMetadata().getRangeFields();
        metadata.getRangeFields().addAll(rangeFields);
        return new WcpsResult(metadata, rasql);
    }

    // it can have multiple cases
    private final String TEMPLATE_WHEN_THEN = "WHEN ( $booleanExpr ) THEN ( $rangeExpr )";
    // but only one default
    private final String TEMPLATE_ELSE = "ELSE ( $rangeExpr )";
    // the Rasql query template for this switch case
    private final String TEMPLATE = "CASE $whenThenExpr $elseExpr END";
}
