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
 * Copyright 2003 - 2014 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcps2.metadata.model;

import java.math.BigDecimal;
import petascope.core.CrsDefinition;

/**
 * @author <a href="merticariu@rasdaman.com">Vlad Merticariu</a>
 */
public class RegularAxis extends Axis {
    private final BigDecimal resolution;

    public RegularAxis(String label, NumericSubset geoBounds, NumericSubset gridBounds, AxisDirection direction,
                       String crsUri, CrsDefinition crsDefinition, BigDecimal resolution, String axisType, String axisUoM,
                       BigDecimal scalarResoultion, int rasdamanOrder) {
        super(label, geoBounds, gridBounds, direction, crsUri, crsDefinition, axisType, axisUoM, scalarResoultion, rasdamanOrder);
        this.resolution = resolution;
    }

    public BigDecimal getResolution() {
        return resolution;
    }
}
