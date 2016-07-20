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
public class Axis<T> {

    private final String label;
    private NumericSubset geoBounds;
    private NumericSubset gridBounds;
    private BigDecimal origin;
    private final AxisDirection direction;
    private String crsUri;
    private final CrsDefinition crsDefinition;
    // e.g: x, y, t, ...
    private final String axisType;
    private final String axisUoM;
    private final BigDecimal scalarResolution;
    private final int rasdamanOrder;

    public Axis(String label, NumericSubset geoBounds, NumericSubset gridBounds,
                              AxisDirection direction, String crsUri, CrsDefinition crsDefinition,
                              String axisType, String axisUoM, BigDecimal scalarResolution, int rasdamanOrder, BigDecimal origin) {
        this.label = label;
        this.geoBounds = geoBounds;
        this.gridBounds = gridBounds;
        this.direction = direction;
        this.crsUri = crsUri;
        this.crsDefinition = crsDefinition;
        this.axisType = axisType;
        this.axisUoM = axisUoM;
        this.scalarResolution = scalarResolution;
        this.rasdamanOrder = rasdamanOrder;
        this.origin = origin;
    }

    public void setCrsUri(String crsUri) {
        this.crsUri = crsUri;
    }

    public String getCrsUri() {
        return crsUri;
    }

    public CrsDefinition getCrsDefinition() {
        return crsDefinition;
    }

    public NumericSubset getGeoBounds() {
        return geoBounds;
    }

    public void setGeoBounds(NumericSubset geoBounds) {
        this.geoBounds = geoBounds;
    }

    public AxisDirection getDirection() {
        return direction;
    }

    public String getLabel() {
        return label;
    }

    public NumericSubset getGridBounds() {
        return gridBounds;
    }

    public void setGridBounds(NumericSubset gridBounds) {
        this.gridBounds = gridBounds;
    }

    public String getAxisType() {
        return axisType;
    }

    public String getAxisUoM() {
        return axisUoM;
    }

    public BigDecimal getScalarResolution() {
        return scalarResolution;
    }

    public int getRasdamanOrder() {
        return rasdamanOrder;
    }

    public BigDecimal getOrigin() {
        return origin;
    }

    public void setOrigin(BigDecimal origin) {
        this.origin = origin;
    }
}
