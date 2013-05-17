/*
 * This file is part of rasdaman community.
 *
 * Rasdaman community is free software: you can redistribute it and/or modify
 * it under the terms of the GNU  General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Rasdaman community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU  General Public License for more details.
 *
 * You should have received a copy of the GNU  General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003 - 2010 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.core;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.util.AxisTypes;
import petascope.wcps.server.core.Bbox;
import petascope.wcps.server.core.CellDomainElement;
import petascope.wcps.server.core.DomainElement;
import petascope.wcps.server.core.InterpolationMethod;
import petascope.wcps.server.core.RangeElement;
import petascope.wcps.server.core.SDU;
import petascope.wcs.server.core.TimeString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements WCPS metadata. For information on what each field
 * means, see the WCPS standard. This class provides extensive error checking
 * and well as various utility functions.
 */
public class CoverageMetadata implements Cloneable {

    private static Logger log = LoggerFactory.getLogger(CoverageMetadata.class);
    
    private List<CellDomainElement> cellDomain;
    private List<DomainElement> domain;
    private String coverageName;
    private String coverageType;
    private int coverageId = -1;    // Used when reading metadata from the DB
    private InterpolationMethod interpolationDefault;
    private Set<InterpolationMethod> interpolationSet;
    private String nullDefault;
    private Set<String> nullSet;
    private List<RangeElement> range;
    private String titleStr = "";
    private String abstractStr = "";
    private String keywordsStr = "";
    private Bbox bbox = null;
    private String crsUri;
    private String metadata;

    public CoverageMetadata(
            String                   coverageName, 
            String                   coverageType,
            String                   crs,
            List<DomainElement>      domain, 
            List<CellDomainElement>  cellDomain, 
            List<RangeElement>       range,
            Set<String>              nullSet, 
            String                   nullDefault, 
            Set<InterpolationMethod> interpolationSet,
            InterpolationMethod      interpolationDefault, 
            String          title, 
            String          abstr, 
            String          keywords
            ) throws PetascopeException {
        
        this(coverageName, coverageType, crs, domain, cellDomain, range, nullSet, nullDefault, interpolationSet, interpolationDefault);
        titleStr = title;
        abstractStr = abstr;
        keywordsStr = keywords;
    }

    public CoverageMetadata(
            String                   coverageName, 
            String                   coverageType, 
            String                   crs,
            List<DomainElement>      domain, 
            List<CellDomainElement>  cellDomain, 
            List<RangeElement>       range,
            Set<String>              nullSet, 
            String                   nullDefault, 
            Set<InterpolationMethod> interpolationSet,
            InterpolationMethod      interpolationDefault
            ) throws PetascopeException {
        
        // Check if some value is missing
        if ( (cellDomain == null) || (range == null)            || (coverageName == null) ||
                (nullSet == null) || (interpolationSet == null) || (crs == null)) {
            throw new PetascopeException(ExceptionCode.InvalidMetadata, "Cell domain, range list, "
                    + "coverage name, null set, and interpolation set cannot be null for coverage " + coverageName);
        }

        // CellDomain
        if (cellDomain.isEmpty()) {
            throw new PetascopeException(ExceptionCode.InvalidMetadata, "Invalid cell domain: At least "
                    + "one element is required for coverage " + coverageName);
        }
        this.cellDomain = cellDomain;

        // Range
        if (range.isEmpty()) {
            throw new PetascopeException(ExceptionCode.InvalidMetadata, "At least one range element is "
                    + "required for coverage " + coverageName);
        }
        this.range = new ArrayList<RangeElement>(range.size());
        Iterator<RangeElement> ir = range.iterator();
        while (ir.hasNext()) {
            RangeElement next = ir.next();
            Iterator<RangeElement> j = this.range.iterator();

            while (j.hasNext()) {
                if (j.next().getName().equals(next.getName())) {
                    throw new PetascopeException(ExceptionCode.InvalidMetadata, "Duplicate range element"
                            + " name encountered for coverage " + coverageName);
                }
            }

            this.range.add(next);
        }

        // Null handlers
        if (nullSet.isEmpty()) {
                throw new PetascopeException(ExceptionCode.InvalidMetadata, "Invalid null set: At least one "
                    + "null value is required for coverage " + coverageName);
        }       
        if (nullDefault == null) {
            throw new PetascopeException(ExceptionCode.InvalidMetadata, "Invalid null default: Null "
                    + "default cannot be null for coverage " + coverageName);
        }
        if (!nullSet.contains(nullDefault)) {
            throw new PetascopeException(ExceptionCode.InvalidMetadata, "Invalid null default: Default "
                    + "null value " + nullDefault + " is not part of the null set"
                    + " for coverage " + coverageName);
        }
        Iterator<String> ns = nullSet.iterator();
        while (ns.hasNext()) {
            String nullVal = ns.next();
            List<String> nulls = SDU.str2string(nullVal);
            if (nulls.size() != range.size()) {
                throw new PetascopeException(ExceptionCode.InvalidMetadata, "Invalid null value: "
                        + nullVal + " must have " + range.size() + " axes "
                        + "according to the range specified for coverage " + coverageName);
            }
            Iterator<String> i = nulls.iterator();
            Iterator<RangeElement> j = range.iterator();
            while (j.hasNext()) {
                RangeElement re = j.next();
                if (re.isBoolean()) {
                    SDU.str2boolean(i.next());
                } else if (re.isIntegral()) {
                    SDU.str2integer(i.next());
                } else if (re.isFloating()) {
                    SDU.str2double(i.next());
                } else if (re.isComplex()) {
                    SDU.str2complex(i.next());
                }
            }
        }
        this.nullSet = nullSet;
        this.nullDefault = nullDefault;

        // Interpolation handlers
        if (interpolationSet.isEmpty()) {
            throw new PetascopeException(ExceptionCode.InvalidMetadata, "Invalid interpolation set: "
                    + "At least one interpolation method is required for "
                    + "coverage " + coverageName);
        }
        if (interpolationDefault == null) {
            interpolationDefault = new InterpolationMethod("none", "none");
        }
        boolean defaultContainedInSet = false;
        Iterator<InterpolationMethod> is = interpolationSet.iterator();
        while (is.hasNext()) {
            if (interpolationDefault.equals(is.next())) {
                defaultContainedInSet = true;
            }
        }
        if (!defaultContainedInSet) {
            throw new PetascopeException(ExceptionCode.InvalidMetadata, "Invalid interpolation default:"
                    + " Default interpolation method ("
                    + interpolationDefault.getInterpolationType() + ","
                    + interpolationDefault.getNullResistance() + ") is not part "
                    + "of the interpolation set for coverage " + coverageName);
        }
        this.interpolationSet = interpolationSet;
        this.interpolationDefault = interpolationDefault;

        this.coverageName = coverageName;
        this.coverageType = coverageType;
        crsUri = crs;

        // Domain axes: consitency checks
        if (domain != null) {

            if (domain.size() != cellDomain.size()) {
                throw new PetascopeException(ExceptionCode.InvalidMetadata, "Domain and cell domain "
                        + "must have equal number of elements for coverage " + coverageName);
            }

            Iterator<DomainElement>     i  = domain.iterator();
            Iterator<CellDomainElement> ci = cellDomain.iterator();
            while (i.hasNext() && ci.hasNext()) {
                DomainElement           next = i.next();
                CellDomainElement       cell = ci.next();
                Iterator<DomainElement> j    = domain.iterator();
                while (j.hasNext()) {
                    DomainElement previous = j.next();
                    // don't compare same objects
                    if (next == previous) {
                        continue;
                    }
                    // TODO: use .contains() in place of .equals to let aliases on axis types.
                    if (previous.getName().equals(next.getName())) {
                        throw new PetascopeException(ExceptionCode.InvalidMetadata, "Duplicate domain "
                                + "element name encountered for coverage " + coverageName);
                    }
                    if (previous.getType().equals(AxisTypes.T_AXIS) && next.getType().equals(AxisTypes.T_AXIS)) {
                        throw new PetascopeException(ExceptionCode.InvalidMetadata, "Domain can contain"
                                + " at most one temporal axis for coverage " + coverageName);
                    }
                    if (previous.getType().equals(AxisTypes.ELEV_AXIS) && next.getType().equals(AxisTypes.ELEV_AXIS)) {
                        throw new PetascopeException(ExceptionCode.InvalidMetadata, "Domain can contain"
                                + " at most one elevation axis for coverage " + coverageName);
                    }
                    if (previous.getType().equals(AxisTypes.X_AXIS) && next.getType().equals(AxisTypes.X_AXIS)) {
                        throw new PetascopeException(ExceptionCode.InvalidMetadata, "Domain can contain"
                                + " at most one x axis for coverage " + coverageName);
                    }
                    if (previous.getType().equals(AxisTypes.Y_AXIS) && next.getType().equals(AxisTypes.Y_AXIS)) {
                        throw new PetascopeException(ExceptionCode.InvalidMetadata, "Domain can contain"
                                + " at most one y axis for coverage " + coverageName);
                    }
                }
            }
        }
        
        this.domain = domain;
        this.cellDomain = cellDomain;
               
        // Create Bbox object (WCPS)
        bbox = new Bbox(crsUri, domain, coverageName);        
    }

    @Override
    public CoverageMetadata clone() {
        try {
            List<CellDomainElement> cd = new ArrayList<CellDomainElement>(cellDomain.size());
            Iterator<CellDomainElement> i = cellDomain.iterator();

            while (i.hasNext()) {
                cd.add(i.next().clone());
            }

            List<RangeElement> r = new ArrayList<RangeElement>(range.size());
            Iterator<RangeElement> j = range.iterator();

            while (j.hasNext()) {
                r.add(j.next().clone());
            }

            List<DomainElement> d = new ArrayList<DomainElement>(domain.size());
            Iterator<DomainElement> k = domain.iterator();

            while (k.hasNext()) {
                d.add(k.next().clone());
            }

            Set<String> ns = new HashSet<String>(nullSet.size());
            Iterator<String> l = nullSet.iterator();

            while (l.hasNext()) {
                ns.add(new String(l.next()));
            }

            Set<InterpolationMethod> is = new HashSet<InterpolationMethod>(interpolationSet.size());
            Iterator<InterpolationMethod> m = interpolationSet.iterator();

            while (m.hasNext()) {
                is.add(m.next().clone());
            }
            
            return new CoverageMetadata(new String(coverageName), new String(coverageType), new String(crsUri), 
                    d, cd, r, ns, new String(nullDefault), is, interpolationDefault.clone(), getAbstract(), getTitle(), getKeywords());
            
        } catch (PetascopeException ime) {
            throw new RuntimeException("Invalid metadata while cloning "
                    + "Metadata. This is a software bug in WCPS.", ime);
        }

    }

//  public CellDomainElement getCellDomain( int index ) {
//
//      return cellDomain.get( index );
//
//  }
    protected void setCoverageId(int id) {
        this.coverageId = id;
    }

    public int getCoverageId() {
        return coverageId;
    }

    public String getCoverageName() {
        return coverageName;
    }

    public String getCoverageType() {
        return coverageType;
    }

    public String getAbstract() {
        return abstractStr;
    }

    public String getTitle() {
        return titleStr;
    }

    public String getKeywords() {
        return keywordsStr;
    }

    public Iterator<CellDomainElement> getCellDomainIterator() {
        return cellDomain.iterator();
    }

    public List<CellDomainElement> getCellDomainList() {
        return cellDomain;
    }

    public Iterator<DomainElement> getDomainIterator() {
        return domain.iterator();
    }

    public Iterator<RangeElement> getRangeIterator() {
        return range.iterator();
    }

    public Iterator<InterpolationMethod> getInterpolationMethodIterator() {
        return interpolationSet.iterator();
    }
    
    public Iterator<String> getNullSetIterator() {
        return nullSet.iterator();
    }

    public int getDimension() {
        return cellDomain.size();
    }

    public int getDomainIndexByType(String type) {
        Iterator<DomainElement> i = domain.iterator();
        for (int index = 0; i.hasNext(); index++) {
            if (i.next().getType().equals(type)) {
                return index;
            }
        }
        return -1;
    }

    public DomainElement getDomainByType(String type) {
        Iterator<DomainElement> i = domain.iterator();
        for (int index = 0; i.hasNext(); index++) {
            DomainElement dom = i.next();
            if (dom.getType().equals(type)) {
                return dom;
            }
        }
        return null;
    }
    
        public int getDomainIndexByName(String name) {
        Iterator<DomainElement> i = domain.iterator();
        for (int index = 0; i.hasNext(); index++) {
            if (i.next().getName().equals(name)) {
                return index;
            }
        }
        return -1;
    }

    public DomainElement getDomainByName(String name) {
        Iterator<DomainElement> i = domain.iterator();
        for (int index = 0; i.hasNext(); index++) {
            DomainElement dom = i.next();
            if (dom.getName().equals(name)) {
                return dom;
            }
        }
        return null;
    }
    
    public CellDomainElement getCellDomainByName(String name) {
        Iterator<CellDomainElement> i = cellDomain.iterator();
        for (int index = 0; i.hasNext(); index++) {
            CellDomainElement dom = i.next();
            if (dom.getName().equalsIgnoreCase(name)) {
                return dom;
            }
        }
        return null;
    }
    
    public List<DomainElement> getDomainList() {
        return domain;
    }
    
    public String getNullDefault() {
        return nullDefault;
    }

    public Set<String> getNullSet() {
        return nullSet;
    }

    public boolean isRangeBoolean() {
        Iterator<RangeElement> i = range.iterator();
        while (i.hasNext()) {
            if (!i.next().isBoolean()) {
                return false;
            }
        }
        return true;
    }

    public boolean isRangeComplex() {
        Iterator<RangeElement> i = range.iterator();
        while (i.hasNext()) {
            if (!i.next().isComplex()) {
                return false;
            }
        }
        return true;
    }

    public boolean isRangeIntegral() {
        Iterator<RangeElement> i = range.iterator();
        while (i.hasNext()) {
            if (!i.next().isIntegral()) {
                return false;
            }
        }
        return true;
    }

    public boolean isRangeFloating() {
        Iterator<RangeElement> i = range.iterator();
        while (i.hasNext()) {
            if (!i.next().isFloating()) {
                return false;
            }
        }
        return true;
    }

    public boolean isRangeNumeric() {
        Iterator<RangeElement> i = range.iterator();

        while (i.hasNext()) {
            if (!i.next().isNumeric()) {
                return false;
            }
        }

        return true;
    }

    public void setCoverageName(String coverageName) throws PetascopeException {
        if (coverageName == null) {
            throw new PetascopeException(ExceptionCode.InvalidMetadata, "Metadata transformation: Coverage name cannot be null");
        }

        this.coverageName = coverageName;
    }

    public void setRangeType(String type) throws PetascopeException {
        Iterator<RangeElement> i = range.iterator();

        while (i.hasNext()) {
            i.next().setType(type);
        }
    }

    public void updateNulls(Set<String> nullSet, String nullDefault) throws PetascopeException {
        if (nullSet.size() == 0) {
            throw new PetascopeException(ExceptionCode.InvalidMetadata, "Invalid null set: At least one null value is required");
        }

        if (nullDefault == null) {
            nullDefault = "0";
        }

        if (!nullSet.contains(nullDefault)) {
            throw new PetascopeException(ExceptionCode.InvalidMetadata, "Invalid null default: Default null value " + nullDefault + " is not part of the null set");
        }

        this.nullSet = nullSet;
        this.nullDefault = nullDefault;
    }

    public String getInterpolationDefault() {
        return interpolationDefault.getInterpolationType();
    }

    public String getNullResistanceDefault() {
        return interpolationDefault.getNullResistance();
    }

    public Bbox getBbox() {
        return bbox;
    }
    
    /**
     * @return the specified cellDomain
     */
    public CellDomainElement getCellDomain(String type) {
        // Extract the specified domain from cellDomain object, if it exists
        for (CellDomainElement axis : cellDomain) {
            if (axis.getName().equals(type)) 
                return axis;
        }
        log.warn("Requesting axis " + type + " from coverage " + coverageName + ", which is missing.");
        return null;
    }
    
    /**
     * @return the CRS URI of the whole coverage.
     */
    public String getCrsUri() {
        return crsUri;
    }
    
    /**
     * @param titleStr the titleStr to set
     */
    public void setTitle(String titleStr) {
        this.titleStr = titleStr;
    }

    /**
     * @param abstractStr the abstractStr to set
     */
    public void setAbstract(String abstractStr) {
        this.abstractStr = abstractStr;
    }

    /**
     * @param keywordsStr the keywordsStr to set
     */
    public void setKeywords(String keywordsStr) {
        this.keywordsStr = keywordsStr;
    }

    /**
     * @param cellDomain the cellDomain to set
     */
    public void setCellDomain(List<CellDomainElement> cellDomain) {
        this.cellDomain = cellDomain;
    }

    /**
     * @param domain the domain to set
     */
    public void setDomain(List<DomainElement> domain) {
        this.domain = domain;
    }

    /**
     * @param range the range to set
     */
    public void setRange(List<RangeElement> range) {
        this.range = range;
    }

    /**
     * @param interpolationSet the interpolationSet to set
     */
    public void setInterpolationSet(Set<InterpolationMethod> interpolationSet) {
        this.interpolationSet = interpolationSet;
    }

    /**
     * @param interpolationDefault the interpolationDefault to set
     */
    public void setDefaultInterpolation(InterpolationMethod interpolationDefault) {
        this.interpolationDefault = interpolationDefault;
    }
    
    /**
     *  Returns the maximal time position of the current coverage in ISO 8601 format, as string.
     * If there is no time-axis, returns null
     */
    public String getTimePeriodBeginning() {
        // Extract the specified domain from cellDomain object, if it exists
        for (DomainElement axis : domain) {
            if (AxisTypes.T_AXIS.equals(axis.getType())) 
                return axis.getMinValue();
        }
        log.warn("Requesting time info on " + coverageName + ", which is missing.");
        return null;
    }

    /**
     *  Returns the minimal time position of the current coverage in ISO 8601 format, as string.
     * If there is no time-axis, returns null
     */
    public String getTimePeriodEnd() {
        // Extract the specified domain from cellDomain object, if it exists
        for (DomainElement axis : domain) {
            if (AxisTypes.T_AXIS.equals(axis.getType())) 
                return axis.getMaxValue();
        }
        log.warn("Requesting time info on " + coverageName + ", which is missing.");
        return null;
    }

    /**
     * Returns the time span of the current coverage, as described in the metadata (in miliseconds).
     * If there is no metadata, returns -1.
     * Note that this function returns the absolute difference. It is the administrator's
     * responsibility to make sure that the metadata values are correct.
     */
    public long getTimeSpan() {
        // Extract the specified domain from cellDomain object, if it exists
        for (DomainElement axis : domain) {
            if (AxisTypes.T_AXIS.equals(axis.getType())) {
                long result = TimeString.difference(getTimePeriodEnd(), getTimePeriodBeginning());
                return Math.abs(result);
            }
        }
        log.warn("Requesting time info on " + coverageName + ", which is missing.");
        return -1;
    }

    /* Returns the difference between the maximum and the minimum time axis index.
    Returns -1 if there is no metadata. */
    public long getTimeIndexesSpan() {
        // Extract the specified domain from cellDomain object, if it exists
        for (DomainElement axis : domain) {
            if (AxisTypes.T_AXIS.equals(axis.getType())) {
                BigInteger big = getCellDomain(AxisTypes.T_AXIS).getHi().subtract(getCellDomain(AxisTypes.T_AXIS).getLo());
                return big.longValue();
            }
        }
        log.warn("Requesting time info on " + coverageName + ", which is missing.");
        return -1;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }
}
