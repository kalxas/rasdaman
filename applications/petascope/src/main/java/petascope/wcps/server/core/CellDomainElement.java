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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003 - 2010 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcps.server.core;

import java.math.BigInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.WCPSException;
import petascope.util.WCPSConstants;
import petascope.wcs2.parsers.GetCoverageRequest;

//A coverage axis in pixel coordinates. See the WCPS standard.
public class CellDomainElement implements Cloneable {
    
    private static Logger log = LoggerFactory.getLogger(CellDomainElement.class);

    GetCoverageRequest.DimensionSubset subsetElement;

    private BigInteger minValue;
    private BigInteger maxValue;
    private int iOrder;
   
    public CellDomainElement(BigInteger lo, BigInteger hi, int order) throws WCPSException {        
        if ((lo == null) || (hi == null)) {
            throw new WCPSException(ExceptionCode.InvalidMetadata, 
                    WCPSConstants.ERRTXT_INVALID_CELL_DOMAIN);
        }
        if (lo.compareTo(hi) == 1) {
            throw new WCPSException(ExceptionCode.InvalidMetadata, 
                    WCPSConstants.ERRTXT_INVALID_CELL_DOMAIN_LOWER + " " + lo + " " + WCPSConstants.ERRTXT_CANNOT_BE_LARGER + " " + hi);
        }
        log.trace(WCPSConstants.MSG_CELL_DOMAIN + " " + lo + ":" + hi);

        minValue = lo;
        maxValue = hi;
        iOrder = order;
    }

    @Override
    public CellDomainElement clone() {
        try {
            return new CellDomainElement(BigInteger.ZERO.add(minValue),
                    BigInteger.ZERO.add(maxValue), new Integer(iOrder));
        } catch (WCPSException ime) {
            throw new RuntimeException(
                    WCPSConstants.ERRTXT_INVALID_METADATA,
                    ime);
        }
    }

    public boolean equals(CellDomainElement cde) {
        return minValue.equals(cde.getLo()) 
                && maxValue.equals(cde.getHi());
    }

    public BigInteger getLo() {
        return minValue;
    }

    public void setLo(BigInteger lo){
        minValue = lo;
    }
    
    public BigInteger getHi() {
        return maxValue;
    }

    public void setHi(BigInteger hi){
        maxValue = hi;
    }
    
    public int getOrder() {
        return iOrder;
    }
    
    @Override
    public String toString() {
        return WCPSConstants.MSG_CELL_DOMAIN_ELEMENT + "#" + iOrder + " [" + minValue + ", " + maxValue + "]";
    }

    public GetCoverageRequest.DimensionSubset getSubsetElement() {
        return subsetElement;
    }

    public void setSubsetElement(GetCoverageRequest.DimensionSubset subsetEl) {
        subsetElement = subsetEl;
    }
}
