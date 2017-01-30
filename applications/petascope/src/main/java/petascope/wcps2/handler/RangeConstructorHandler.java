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
 * Copyright 2003 - 2016 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcps2.handler;

import org.apache.commons.lang3.StringUtils;
import petascope.wcps2.result.WcpsResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import petascope.wcps2.metadata.model.WcpsCoverageMetadata;

/**
 * Translation class for the range constructor expressions
 * <code>
 * for c in (COV) return encode( {red: c.red;    green: c.green;    blue: c.blue }, "png")
 * </code>
 * returns
 * <code>
 * select  { c.red, c.green, c.blue } from COV as c
 * </code>
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class RangeConstructorHandler {

    public static WcpsResult handle(Map<String, WcpsResult> fieldStructure) {
        List<String> translatedFields = new ArrayList();
        // NOTE: if range is single scalar value then metadata is NULL. If any range has a not-null metadata, the metadata is from this range.
        WcpsCoverageMetadata metadata = null;
        for (Map.Entry<String, WcpsResult> entry : fieldStructure.entrySet()) {
            translatedFields.add(entry.getValue().getRasql());
            WcpsCoverageMetadata metadataTmp = entry.getValue().getMetadata();            
            if (metadataTmp != null) {
                // coverage must contain at least 1 range and when in range expression only 1 range can be used.
                // e.g: test_mr has 1 range (band) and can be used as { red: c }
                // e.g: test_rgb has 3 ranges (bands) and can be used as { red: c.red } "not" { red: c }
                // NOTE: in case of coverage constructor, it also has only 1 range
                metadataTmp.getRangeFields().get(0).setName(entry.getKey());
                
                metadata = metadataTmp;
            }
        }

        String rasql = null;
        if (translatedFields.size() == 1) {
            // if encode only 1 range, then it does not need to be braced
            rasql = ONE_RANGE_TEMPLATE.replace("$fieldDefinitions", StringUtils.join(translatedFields, ","));
        } else {
            rasql = MULTIPLE_RANGE_TEMPLATE.replace("$fieldDefinitions", StringUtils.join(translatedFields, ","));
        }

        return new WcpsResult(metadata, rasql);
    }

    private static final String ONE_RANGE_TEMPLATE = "$fieldDefinitions";
    private static final String MULTIPLE_RANGE_TEMPLATE = "{$fieldDefinitions}";
}
