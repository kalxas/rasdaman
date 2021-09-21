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
 * Copyright 2003 - 2018 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcps.metadata.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import petascope.util.BigDecimalUtil;

/**
 * Class for storing numerical slicing (lower is equal with upper)
 * @author <a href="merticariu@rasdaman.com">Vlad Merticariu</a>
 */
public class NumericSlicing extends NumericSubset {

    private BigDecimal bound;
    
    public NumericSlicing() {
        
    }

    public NumericSlicing(BigDecimal bound) {
        this.bound =  BigDecimalUtil.stripDecimalZeros(bound);
    }

    public BigDecimal getBound() {
        return this.bound;
    }

    public void setBound(BigDecimal bound) {
        this.bound = BigDecimalUtil.stripDecimalZeros(bound);
    }

    @Override
    @JsonIgnore
    public String getStringRepresentation() {
        return bound.toPlainString();
    }

    @Override
    @JsonIgnore
    public String getStringRepresentationInInteger() {
        return bound.toBigInteger().toString();
    }

    @Override
    public BigDecimal getLowerLimit() {
        return bound;
    }

    @Override
    public BigDecimal getUpperLimit() {
        return bound;
    }

    @Override
    public void setLowerLimit(BigDecimal value) {
        this.bound = value;
    }

    @Override
    public void setUpperLimit(BigDecimal value) {
        this.bound = value;
    }
}
