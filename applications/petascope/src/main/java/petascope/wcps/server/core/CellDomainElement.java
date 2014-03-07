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
package petascope.wcps.server.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.WCPSException;
import petascope.util.WcpsConstants;
import petascope.wcs2.parsers.GetCoverageRequest;

//A coverage axis in pixel coordinates. See the WCPS standard.
public class CellDomainElement implements Cloneable {

    private static Logger log = LoggerFactory.getLogger(CellDomainElement.class);

    GetCoverageRequest.DimensionSubset subsetElement;

    private String hi;                      //FIXME: should be double
    private String lo;                      //FIXME: should be double
    private int iOrder;

    public CellDomainElement(String lo, String hi, int order) throws WCPSException {
        if ((lo == null) || (hi == null)) {
            throw new WCPSException(ExceptionCode.InvalidMetadata,
                    "Invalid cell domain element: Bounds may not be null.");
        }
        log.trace(WcpsConstants.MSG_CELL_DOMAIN + " " + lo + ":" + hi);

        this.lo = lo;
        this.hi = hi;
        iOrder = order;
    }

    @Override
    public CellDomainElement clone() {
        try {
            return new CellDomainElement(lo, hi, new Integer(iOrder));
        } catch (WCPSException ime) {
            throw new RuntimeException("Invalid metadata while cloning CellDomainElement. This is a software bug in WCPS.", ime);
        }
    }

    public boolean equals(CellDomainElement cde) {
        return lo.equals(cde.getLo())
                && hi.equals(cde.getHi());
    }

    public String getLo() {
        return lo;
    }

    public String getHi() {
        return hi;
    }

    public int getHiInt() {
        try {
            return Integer.valueOf(hi);
        } catch (NumberFormatException ex) {
            log.error("Lower bound of interval is not a number: " + hi);
            throw new RuntimeException("Lower bound of interval is not a number: " + hi);
        }
    }

    public void setLo(String lo) {
        this.lo = lo;
    }

    public void setHi(String hi) {
        this.hi = hi;
    }

    public int getLoInt() {
        try {
            return Integer.valueOf(lo);
        } catch (NumberFormatException ex) {
            log.error("Lower bound of interval is not a number: " + lo);
            throw new RuntimeException("Lower bound of interval is not a number: " + lo);
        }
    }

    public int getOrder() {
        return iOrder;
    }

    @Override
    public String toString() {
        return WcpsConstants.MSG_CELL_DOMAIN_ELEMENT + "#" + iOrder + " [" + lo + ", " + hi + "]";
    }

    public GetCoverageRequest.DimensionSubset getSubsetElement() {
        return subsetElement;
    }

    public void setSubsetElement(GetCoverageRequest.DimensionSubset subsetEl) {
        subsetElement = subsetEl;
    }
}
