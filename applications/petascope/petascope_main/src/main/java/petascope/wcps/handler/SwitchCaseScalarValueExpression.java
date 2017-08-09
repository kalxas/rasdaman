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
import petascope.wcps.metadata.model.WcpsCoverageMetadata;

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
public class SwitchCaseScalarValueExpression {

    public WcpsResult handle(List<WcpsResult> booleanResults, List<WcpsResult> scalarResults) {
        List<String> translatedFields = new ArrayList();

        for (int i = 0; i < booleanResults.size(); i++) {
            String booleanResult = booleanResults.get(i).getRasql();
            String scalarResult = scalarResults.get(i).getRasql();

            String result = TEMPLATE_WHEN_THEN.replace("$booleanExpr", booleanResult)
                    .replace("$scalarExpr", scalarResult);
            translatedFields.add(result);
        }

        // combine switch case to a string
        String whenThenStr = StringUtils.join(translatedFields, " ");
        String elseStr = "";

        // switch case has default also
        String scalarResult = scalarResults.get(scalarResults.size() - 1).getRasql();
        elseStr = TEMPLATE_ELSE.replace("$scalarExpr", scalarResult);

        String rasql = TEMPLATE.replace("$whenThenExpr", whenThenStr)
                .replace("$elseExpr", elseStr);

        // This is needed a coverage metadata from boolean coverage epxression
        WcpsCoverageMetadata metadata = booleanResults.get(0).getMetadata();
        return new WcpsResult(metadata, rasql);
    }

    // it can have multiple cases
    private final String TEMPLATE_WHEN_THEN = "WHEN ( $booleanExpr ) THEN ( $scalarExpr )";
    // but only one default
    private final String TEMPLATE_ELSE = "ELSE ( $scalarExpr )";
    // the Rasql query template for this switch case
    private final String TEMPLATE = "CASE $whenThenExpr $elseExpr END";
}
