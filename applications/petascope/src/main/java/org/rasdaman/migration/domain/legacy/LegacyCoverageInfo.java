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
package org.rasdaman.migration.domain.legacy;

import org.rasdaman.migration.domain.legacy.LegacyCoverageMetadata;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petascope.util.CrsUtil;
import org.rasdaman.migration.domain.legacy.LegacyWcsUtil;
import org.rasdaman.migration.domain.legacy.LegacyWcpsConstants;

public class LegacyCoverageInfo {

    private static Logger log = LoggerFactory.getLogger(LegacyCoverageInfo.class);

    private final List<LegacyCellDomainElement> cellDomains;
    private final List<LegacyDomainElement> domains;
    private final String coverageName;
    private String coverageCrs;
    private final LegacyBbox bbox;
    private final boolean gridded;
    //empty constructor
    public LegacyCoverageInfo() {
        cellDomains = null;
        domains = null;
        coverageName = null;
        coverageCrs = null;
        bbox = null;
        gridded = false;
    }

    public LegacyCoverageInfo(LegacyCoverageInfo other) {
        cellDomains = new ArrayList<LegacyCellDomainElement>();
        domains = new ArrayList<LegacyDomainElement>();
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

    public LegacyCoverageInfo(LegacyCoverageMetadata m) {
        cellDomains = new ArrayList<LegacyCellDomainElement>();
        domains = new ArrayList<LegacyDomainElement>();
        Iterator<LegacyCellDomainElement> itcde = m.getCellDomainIterator();

        while (itcde.hasNext()) {
            cellDomains.add(itcde.next());
        }

        Iterator<LegacyDomainElement> itde = m.getDomainIterator();

        while (itde.hasNext()) {
            domains.add(itde.next());
        }

        coverageName = m.getCoverageName();
        coverageCrs = CrsUtil.CrsUri.createCompound(m.getCrsUris());
        bbox = m.getBbox();
        // is the coverage gridded or e.g. multipoint?
        gridded = LegacyWcsUtil.isGrid(m.getCoverageType());
    }

    public boolean isCompatible(LegacyCoverageInfo other) {
        if (getNumDimensions() != other.getNumDimensions()) {
            log.error("The number of dimensions does not match.");
            return false;
        }

        {
            Iterator<LegacyCellDomainElement> it = cellDomains.iterator();
            int index = 0;

            while (it.hasNext()) {
                LegacyCellDomainElement me;
                LegacyCellDomainElement you;

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
        
        return true;
    }

    public int getNumDimensions() {
        return cellDomains.size();
    }

    public LegacyCellDomainElement getCellDomainElement(int dim) {
        return cellDomains.get(dim);
    }

    public LegacyDomainElement getDomainElement(int dim) {
        return domains.get(dim);
    }

    public String getCoverageName() {
        return coverageName;
    }

    public String getCoverageCrs() {
        return coverageCrs;
    }

    public void setCoverageCrs(String coverageCrs) {
        this.coverageCrs = coverageCrs;
    }

    public LegacyBbox getBbox() {
        return bbox;
    }

    public void removeDimension(int dim) {
        cellDomains.remove(dim);
        domains.remove(dim);
    }

    public void setDimension(int dim, LegacyCellDomainElement cde, LegacyDomainElement de) {
        cellDomains.set(dim, cde);
        domains.set(dim, de);
    }

    public void setCellDimension(int dim, LegacyCellDomainElement cde) {
        cellDomains.set(dim, cde);
    }

    public int getDomainIndexByType(String type) throws Exception {
        Iterator<LegacyDomainElement> it = domains.iterator();
        int index = 0;

        while (it.hasNext()) {
            if (type.equals(it.next().getType())) {
                return index;
            }

            index++;
        }

        log.error("Axis name not found: " + type);
        throw new Exception("Domain name not found: " + type);
    }

    public int getDomainIndexByName(String name) throws Exception {
        Iterator<LegacyDomainElement> it = domains.iterator();
        int index = 0;

        while (it.hasNext()) {
            if (name.equals(it.next().getLabel())) {
                return index;
            }

            index++;
        }

        log.error("Axis name not found: " + name);
        throw new Exception("Domain name not found: " + name);
    }

    /**
     * Returns the cell domains for this coverage
     *
     * @return the cell domain elements
     */
    public List<LegacyCellDomainElement> getCellDomains() {
        return cellDomains;
    }

    /**
     * Returns the domain elements for this coverage
     *
     * @return the domain elements
     */
    public List<LegacyDomainElement> getDomains() {
        return domains;
    }

    /**
     * Returns the domain element based on its name
     *
     * @param name the name of the domain element
     * @return the domain element or null if no domain is found
     */
    public LegacyDomainElement getDomainByName(String name) {
        try {
            return domains.get(getDomainIndexByName(name));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Return a cell domain based on the axis name
     *
     * @param name the name of the cell domain axis
     * @return the cell domain or null if none is found
     */
    public LegacyCellDomainElement getCellDomainByName(String name) {
        for (LegacyDomainElement axis : domains) {
            if (axis.getLabel().equalsIgnoreCase(name)) {
                return cellDomains.get(axis.getOrder());
            }
        }
        return null;
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
