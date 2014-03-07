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

import petascope.core.CoverageMetadata;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petascope.exceptions.WCPSException;
import petascope.util.CrsUtil;
import petascope.util.WcpsConstants;
import petascope.util.WcsUtil;
import petascope.util.XMLSymbols;

public class CoverageInfo {

    private static Logger log = LoggerFactory.getLogger(CoverageInfo.class);

    private final List<CellDomainElement> cellDomains;
    private final List<DomainElement> domains;
    private final String coverageName;
    private final String coverageCrs;
    private final Bbox bbox;
    private final boolean gridded;

    public CoverageInfo(CoverageInfo other) {
        cellDomains = new ArrayList<CellDomainElement>();
        domains = new ArrayList<DomainElement>();
        int N = other.getNumDimensions();

        for (int i = 0; i < N; ++i) {
            cellDomains.add(other.getCellDomainElement(i));
            domains.add(other.getDomainElement(i));
        }

        coverageName = other.getCoverageName();
        coverageCrs = other.getCoverageCrs();
        bbox = other.getBbox();
        gridded = other.isGridded();
    }

    public CoverageInfo(CoverageMetadata m) {
        cellDomains = new ArrayList<CellDomainElement>();
        domains = new ArrayList<DomainElement>();
        Iterator<CellDomainElement> itcde = m.getCellDomainIterator();

        while (itcde.hasNext()) {
            cellDomains.add(itcde.next());
        }

        Iterator<DomainElement> itde = m.getDomainIterator();

        while (itde.hasNext()) {
            domains.add(itde.next());
        }

        coverageName = m.getCoverageName();
        coverageCrs= CrsUtil.CrsUri.createCompound(m.getCrsUris());
        bbox = m.getBbox();
        // is the coverage gridded or e.g. multipoint?
        gridded = WcsUtil.isGrid(m.getCoverageType());
    }

    public boolean isCompatible(CoverageInfo other) {
        if (getNumDimensions() != other.getNumDimensions()) {
            log.error("The number of dimensions does not match.");
            return false;
        }

        {
            Iterator<CellDomainElement> it = cellDomains.iterator();
            int index = 0;

            while (it.hasNext()) {
                CellDomainElement me, you;

                me = it.next();
                you = other.getCellDomainElement(index++);

                if (!me.getHi().equals(you.getHi())) {
                    log.error("High values don't match: "
                            + me.getHi().toString() + ", "
                            + you.getHi().toString());
                    return false;
                }

                if (!me.getLo().equals(you.getLo())) {
                    log.error("Low values do not match: "
                            + me.getLo().toString() + ", "
                            + you.getLo().toString());
                    return false;
                }
            }
        }
        {
            Iterator<DomainElement> it = domains.iterator();
            int index = 0;

            while (it.hasNext()) {
                DomainElement me, you;

                me = it.next();
                you = other.getDomainElement(index++);

                if (!me.getLabel().equals(you.getLabel())) {
                    log.error("Domain element names don't match: '"
                            + me.getLabel() + "' " + WcpsConstants.MSG_AND + " '"
                            + you.getLabel() + "'.");
                    return false;
                }

                // if (me.getName() != you.getName()) return false;
            }
        }
        return true;
    }

    public int getNumDimensions() {
        return cellDomains.size();
    }

    public CellDomainElement getCellDomainElement(int dim) {
        return cellDomains.get(dim);
    }

    public DomainElement getDomainElement(int dim) {
        return domains.get(dim);
    }

    public String getCoverageName() {
        return coverageName;
    }

    public String getCoverageCrs() {
        return coverageCrs;
    }

    public Bbox getBbox() {
        return bbox;
    }

    public void removeDimension(int dim) {
        cellDomains.remove(dim);
        domains.remove(dim);
    }

    public void setDimension(int dim, CellDomainElement cde, DomainElement de) {
        cellDomains.set(dim, cde);
        domains.set(dim, de);
    }

    public void setCellDimension(int dim, CellDomainElement cde) {
        cellDomains.set(dim, cde);
    }

    public int getDomainIndexByType(String type) throws WCPSException {
        Iterator<DomainElement> it = domains.iterator();
        int index = 0;

        while (it.hasNext()) {
            if (type.equals(it.next().getType())) {
                return index;
            }

            index++;
        }

        log.error("Axis name not found: " + type);
        throw new WCPSException("Domain name not found: " + type);
    }

    public int getDomainIndexByName(String name) throws WCPSException {
        Iterator<DomainElement> it = domains.iterator();
        int index = 0;

        while (it.hasNext()) {
            if (name.equals(it.next().getLabel())) {
                return index;
            }

            index++;
        }

        log.error("Axis name not found: " + name);
        throw new WCPSException("Domain name not found: " + name);
    }

    /**
     * @return True if the coverage is gridded, false otherwise.
     */
    public boolean isGridded() {
        return this.gridded;
    }

    @Override
    public String toString() {
        return coverageName;
    }

}
