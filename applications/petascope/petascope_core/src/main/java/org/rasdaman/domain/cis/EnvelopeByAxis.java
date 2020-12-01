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
 * Copyright 2003 - 2017 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package org.rasdaman.domain.cis;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.rasdaman.config.ConfigManager;
import petascope.core.AxisTypes;
import petascope.core.BoundingBox;
import petascope.core.CrsDefinition;
import petascope.core.Pair;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.util.BigDecimalUtil;
import petascope.util.CrsUtil;
import petascope.util.ListUtil;
import petascope.util.StringUtil;
import petascope.util.TimeUtil;

/**
 * CIS 1.1
 *
 * If present, the envelope of a coverage instantiating class coverage shall
 * consist of a CIS::EnvelopeByAxis when envelope is minimum bounding box of the
 * coverage, as specified in GML (it shows valuable information in MultiPoint
 * when GeneralGrid does not exist)
 *
 *
 *
 */
@Entity
@Table(name = EnvelopeByAxis.TABLE_NAME)
public class EnvelopeByAxis implements Serializable {

    public static final String TABLE_NAME = "envelope_by_axis";
    public static final String COLUMN_ID = TABLE_NAME + "_id";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = COLUMN_ID)
    private long id;

    @Column(name = "srs_name", length=10000)
    // NOTE: As this could be long text, so varchar(255) is not enough
    private String srsName;

    @Column(name = "srs_dimension")
    private int srsDimension;

    @Column(name = "axis_Labels")
    private String axisLabels;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = EnvelopeByAxis.COLUMN_ID)
    @OrderColumn
    private List<AxisExtent> axisExtents;
    
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = Wgs84BoundingBox.COLUMN_ID)
    private Wgs84BoundingBox wgs84BBox;

    public EnvelopeByAxis() {

    }

    public EnvelopeByAxis(String srsName, int srsDimension, String axisLabels, List<AxisExtent> axisExtents) {
        this.srsName = srsName;
        this.srsDimension = srsDimension;
        this.axisLabels = axisLabels;
        this.axisExtents = axisExtents;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List<AxisExtent> getAxisExtents() {
        return axisExtents;
    }

    public void setAxisExtents(List<AxisExtent> axisExtents) {
        this.axisExtents = axisExtents;
    }

    public String getSrsName() {
        return srsName;
    }

    public void setSrsName(String srsName) {
        this.srsName = srsName;
    }

    public int getSrsDimension() {
        return srsDimension;
    }

    public void setSrsDimension(int srsDimension) {
        this.srsDimension = srsDimension;
    }

    public String getAxisLabels() {
        return axisLabels;
    }

    public void setAxisLabels(String axisLabels) {
        this.axisLabels = axisLabels;
    }

    // Helper methods
    /**
     * Return the axisExtent (geoDomains) from list of axis extents
     *
     * @param axisLabel
     * @return
     */
    public AxisExtent getAxisExtentByLabel(String axisLabel) {
        for (AxisExtent axisExtent : this.axisExtents) {
            if (axisExtent.getAxisLabel().equals(axisLabel)) {
                return axisExtent;
            }
        }

        return null;
    }
    
    /**
     * Return the axisExtent (geoDomains) from the list of axis extents by index of element
     */
    public AxisExtent getAxisExtentByIndex(int index) {
        AxisExtent axisExtent = this.axisExtents.get(index);
        return axisExtent;
    }
    
    public Wgs84BoundingBox getWgs84BBox() {
        return wgs84BBox;
    }

    public void setWgs84BBox(Wgs84BoundingBox wgs84BBox) {
        this.wgs84BBox = wgs84BBox;
    }

    /**
     * Return the concatenation of axis labels (e.g: "lat lon")
     *
     * @return
     * @throws petascope.exceptions.PetascopeException
     * @throws petascope.exceptions.SecoreException
     */
    public String getAxisLabelsRepresentation() throws PetascopeException, SecoreException {
        String axisLabels = "";

        // Decompose the axis labels with correct order in URI definition (i.e: epsg 4326: lat lon)
        List<String> crsAxisLabels = CrsUtil.getAxesLabels(CrsUtil.CrsUri.decomposeUri(this.srsName)); // full list of CRS axis label
        for (String label : crsAxisLabels) {
            for (AxisExtent axisExtent : this.axisExtents) {
                if (label.equals(axisExtent.getAxisLabel())) {
                    axisLabels += label + " ";
                    break;
                }
            }
        }

        return axisLabels.trim();
    }

    /**
     * Return the String contains all the lower corners of axes extent e.g:
     * <LowerCorner>111.975 -44.525</LowerCorner>
     *
     * NOTE: although display DateTime for LowerCorner, UpperCorner of
     * coverage's boundingbox is nice, but it is not valid Schema, so just
     * display the raw numbers when OGC CITE testing is not enabled.
     *
     * @return
     * @throws petascope.exceptions.PetascopeException
     * @throws petascope.exceptions.SecoreException
     */
    public String getLowerCornerRepresentation() throws PetascopeException, SecoreException {

        String lowerCorner = "";
        for (AxisExtent axisExtent : this.axisExtents) {
            if (!ConfigManager.OGC_CITE_OUTPUT_OPTIMIZATION) {
                lowerCorner += axisExtent.getLowerBound() + " ";
            } else {
                // Returns the bound as number as OGC CITE requires value in number
                lowerCorner += axisExtent.getLowerBoundNumber() + " ";
            }
        }

        return lowerCorner.trim();
    }
    
    /**
     * Return the list of axes lower bounds
     */
    public List<String> getLowerBoundValues() {
        List<String> results = new ArrayList<>();
        
        for (AxisExtent axisExtent : this.axisExtents) {
            String lowerBound = StringUtil.stripQuotes(axisExtent.getLowerBound());
            results.add(lowerBound);
        }
        
        return results;
    }
    
    /**
     * Return the list of axes upper bounds
     */
    public List<String> getUpperBoundValues() {
        List<String> results = new ArrayList<>();
        
        for (AxisExtent axisExtent : this.axisExtents) {
            String upperBound = StringUtil.stripQuotes(axisExtent.getUpperBound());
            results.add(upperBound);
        }
        
        return results;
    }

    /**
     * Return the String contains all the lower corners of axes extent e.g:
     * <UpperCorner>156.275 -8.975</UpperCorner>
     *
     * NOTE: although display DateTime for LowerCorner, UpperCorner of
     * coverage's boundingbox is nice, but it is not valid Schema, so just
     * display the raw numbers when OGC CITE testing is not enabled.
     *
     * @return
     * @throws petascope.exceptions.PetascopeException
     * @throws petascope.exceptions.SecoreException
     */
    public String getUpperCornerRepresentation() throws PetascopeException, SecoreException {

        String upperCorner = "";
        for (AxisExtent axisExtent : this.axisExtents) {
            if (!ConfigManager.OGC_CITE_OUTPUT_OPTIMIZATION) {
                upperCorner += axisExtent.getUpperBound() + " ";
            } else {
                // Returns the bound as number as OGC CITE requires value in number
                upperCorner += axisExtent.getUpperBoundNumber() + " ";
            }
        }

        return upperCorner.trim();
    }
    
    /**
     * e.g: return Lat -> Y, Long -> X type
     */
    public Map<String, String> getAxisLabelsTypesMap() throws PetascopeException, SecoreException {
        Map<String, String> map = new LinkedHashMap<>();
        
        for (int i = 0; i < this.axisExtents.size(); i++) {
            String axisType = CrsUtil.getAxisTypeByIndex(this.srsName, i);
            map.put(this.axisExtents.get(i).getAxisLabel(), axisType);
        }
        
        return map;
    }
    
    /**
     * Return the comma separated list of axis names
     * e.g: Lat,Long,time
     */
    public String getAxisNamesRepresentation() {
        List<String> results = new ArrayList<>();
        
        for (AxisExtent axisExtent : this.axisExtents) {
            results.add(axisExtent.getAxisLabel());
        }
        
        return ListUtil.join(results, ",");
    }
    
    /**
     * If this coverage has a time axis, then returns it lower and uppe bounds in date time format
     */
    public List<AxisExtent> getTimeAxisExtents() throws PetascopeException, SecoreException {
        List<AxisExtent> axisExtents = new ArrayList<>();
        
        for (int i = 0; i < this.axisExtents.size(); i++) {
            String axisType = CrsUtil.getAxisTypeByIndex(this.srsName, i);
            if (axisType.equals(AxisTypes.T_AXIS)) {
                AxisExtent axisExtent = this.axisExtents.get(i);
                CrsDefinition crsDefinition = CrsUtil.getCrsDefinition(axisExtent.getSrsName());
                if (!axisExtent.getLowerBound().contains("\"")) {
                    axisExtent.setLowerBound(TimeUtil.valueToISODateTime(BigDecimal.ZERO, axisExtent.getLowerBoundNumber(), crsDefinition));
                } 
                if (!axisExtent.getUpperBound().contains("\"")) {
                    axisExtent.setUpperBound(TimeUtil.valueToISODateTime(BigDecimal.ZERO, axisExtent.getUpperBoundNumber(), crsDefinition));
                }
                
                axisExtents.add(axisExtent);
            }
        }
        
        return axisExtents;
    }
    
    /**
     * Return the list of crss of a coverage
     */
    public List<String> getCrsList() {
        Set<String> results = new LinkedHashSet<>();
        
        for (AxisExtent axisExtent : this.axisExtents) {
            String shortenedCrs = CrsUtil.getAuthorityCodeFormat(axisExtent.getSrsName());
            results.add(shortenedCrs);
        }
        
        return new ArrayList<>(results);
    }
    
    /**
     * Check if this coverage has geo XY axes and returns list of minX, minY, maxX, maxY
     */
    public BoundingBox getGeoXYBoundingBox() throws PetascopeException {
        List<AxisExtent> axisExtents = this.axisExtents;
        boolean foundX = false, foundY = false;
        String xyAxesCRS = null;
        String coverageCRS = this.srsName;
        BigDecimal xMin = null, yMin = null, xMax = null, yMax = null;
        
        BoundingBox result = null;
        
        int i = 0;
        for (AxisExtent axisExtent : axisExtents) {
            String axisExtentCrs = axisExtent.getSrsName();
            // NOTE: the basic coverage metadata can have the abstract SECORE URL, so must replace it first
            axisExtentCrs = CrsUtil.CrsUri.fromDbRepresentation(axisExtentCrs);
            
            if (axisExtentCrs.contains(CrsUtil.EPSG_AUTH)) {
                // x, y
                String axisType = CrsUtil.getAxisTypeByIndex(coverageCRS, i);
                if (axisType.equals(AxisTypes.X_AXIS)) {
                    foundX = true;
                    xMin = new BigDecimal(axisExtent.getLowerBound());
                    xMax = new BigDecimal(axisExtent.getUpperBound());
                    xyAxesCRS = axisExtentCrs;
                } else if (axisType.equals(AxisTypes.Y_AXIS)) {
                    foundY = true;
                    yMin = new BigDecimal(axisExtent.getLowerBound());
                    yMax = new BigDecimal(axisExtent.getUpperBound());
                }
                if (foundX && foundY) {
                    break;
                }
            }
            
            i++;
        }
        
        if (foundX && foundY && CrsUtil.isValidTransform(xyAxesCRS)) {
            result = new BoundingBox(xMin, yMin, xMax, yMax, xyAxesCRS);
        }
        
        return result;
    }
}
