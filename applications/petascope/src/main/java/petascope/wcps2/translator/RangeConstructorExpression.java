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
package petascope.wcps2.translator;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Translation class fo the range constructor expressions
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class RangeConstructorExpression extends CoverageExpression {
    /**
     * Constructor for the class
     *
     * @param fieldStructure the structure of the range fields
     */
    public RangeConstructorExpression(Map<String, CoverageExpression> fieldStructure) {
        this.fieldStructure = fieldStructure;
        setCoverage(fieldStructure.values().iterator().next().getCoverage());
    }

    @Override
    public String toRasql() {
        List<String> translatedFields = new ArrayList<String>();
        int index = 0;
        for (Map.Entry<String, CoverageExpression> entry : fieldStructure.entrySet()) {
            translatedFields.add(entry.getValue().toRasql() + " * " + generateIdentityStruct(index, fieldStructure.entrySet().size()));
            index++;
        }
        return TEMPLATE.replace("$fieldDefinitions", StringUtils.join(translatedFields, " + "));
    }

    private String generateIdentityStruct(int position, int size) {
        List<String> parts = new ArrayList<String>(size);
        for (int j = 0; j < size; j++) {
            if (j == position) {
                parts.add("1c");
            } else {
                parts.add("0c");
            }
        }
        return "{" + StringUtils.join(parts, ",") + "}";
    }

    private final Map<String, CoverageExpression> fieldStructure;
    private final String TEMPLATE = "($fieldDefinitions)";
}
