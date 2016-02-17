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

import org.apache.commons.lang3.math.NumberUtils;
import petascope.exceptions.PetascopeException;
import petascope.wcps2.error.managed.processing.RangeFieldNotFound;

/**
 * Translation node from wcps to rasql for range subsetting.
 * Example:
 * <code>
 * $c1.red
 * </code>
 * translates to
 * <code>
 * c1.red
 * </code>
 * select encode(scale( ((c[*:*,*:*,0:0]).1) [*:*,*:*,0], [0:2,0:1] ), "csv") from irr_cube_2 AS c
 * SELECT encode(SCALE( ((c[*:*,*:*,0:0]).1) [*:*,*:*,0:0], [0:2,0:1]), "csv" ) FROM irr_cube_2 AS c
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class RangeSubsetting extends CoverageExpression {

    public RangeSubsetting(String rangeType, CoverageExpression coverageExp) {
        this.rangeType = rangeType;
        this.coverageExp = coverageExp;
        addChild(coverageExp);
        setCoverage(coverageExp.getCoverage());
    }

    @Override
    public String toRasql() {
        String rangeField = this.rangeType.trim();
        if (!NumberUtils.isNumber(rangeField)) {
            try {
                rangeField = getCoverage().getCoverageMetadata().getRangeIndexByName(this.rangeType.trim()).toString();
            } catch (PetascopeException e) {
                throw new RangeFieldNotFound(this.rangeType.trim());
            }
        }
        String template = TEMPLATE.replace("$coverageExp", this.coverageExp.toRasql().trim()).replace("$rangeType", rangeField);
        return template;
    }

    private String rangeType;
    private IParseTreeNode coverageExp;
    private final String TEMPLATE = "$coverageExp.$rangeType";
}
