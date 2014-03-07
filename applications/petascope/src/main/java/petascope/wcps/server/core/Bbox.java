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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package petascope.wcps.server.core;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCPSException;
import petascope.exceptions.WCSException;
import petascope.util.AxisTypes;
import petascope.util.CrsUtil;

/**
 * NOTE: the WGS84 bounding needs to take care to transform only the /spatial/ axes,
 * whereas the other extents don't have to change.
 *
 * @author <a href="mailto:p.campalani@jacobs-university.de">Piero Campalani</a>
 */
public class Bbox implements Cloneable {

    private static Logger log = LoggerFactory.getLogger(Bbox.class);

    private String crsName;
    private String coverageName;
    private List<BigDecimal> minValues;
    private List<BigDecimal> maxValues;
    private List<String> types;
    private List<String> names;
    private double wgs84minLon;
    private double wgs84maxLon;
    private double wgs84minLat;
    private double wgs84maxLat;
    private Boolean hasWgs84Bbox = false;
    private List<DomainElement> domains; // Cloning

    // spatial bbox
    private Double minX, maxX, minY, maxY, minZ, maxZ;

    public Bbox(String crs, List<DomainElement> domains, String coverage)
            throws WCPSException, WCSException, PetascopeException, SecoreException {

        this.domains = new ArrayList();
        this.domains.addAll(domains);
        coverageName = coverage;
        minValues = new ArrayList<BigDecimal>();
        maxValues = new ArrayList<BigDecimal>();
        types     = new ArrayList<String>();
        names     = new ArrayList<String>();

        // Raw Bounding-box
        for (DomainElement el : domains) {
            if ((el.getMinValue() == null) || (el.getMaxValue() == null)) {
            throw new WCPSException(ExceptionCode.InvalidMetadata,
                    "Invalid bounding box: null element encountered.");
            }
            minValues.add(el.getMinValue());
            maxValues.add(el.getMaxValue());
            types.add(el.getType());
            names.add(el.getLabel());
        }

        if (crs == null) {
            throw new WCPSException(ExceptionCode.InvalidMetadata, "Invalid CRS name: null element encountered.");
        } else {
            // Store the native (C)CRS from the list of single CRSs associated to the coverage
            crsName = crs;
        }

        /* Get WGS84 bounding box if the non-WGS84 coverage has X and Y axis from which to convert.
         * (NOTE1) Simple copy of coordinates for WGS84 coverages: Capabilities response to list,
         * though redundant, the Wgs84Bbox of WGS84 coverages as well (uniformity).
         * (NOTE2) Keep WGS84 bbox for planetary CRSs as well? It can be useful, but they need
         * a special treatment since the CRS must be loaded via WKT, URI is unknown for GeoTools.
         */
        double lowX  = 0D;
        double lowY  = 0D;
        double highX = 0D;
        double highY = 0D;
        String crsSourceX="", crsSourceY="";
        for (DomainElement el : domains) {
            Double min = el.getMinValue().doubleValue();
            Double max = el.getMaxValue().doubleValue();
            // X AXIS
            if (el.getType().equals(AxisTypes.X_AXIS)) {
                crsSourceX = el.getNativeCrs();
                if (CrsUtil.CrsUri.areEquivalent(crsSourceX, CrsUtil.CrsUri(CrsUtil.EPSG_AUTH, CrsUtil.WGS84_EPSG_CODE))) {
                    wgs84minLon = min;
                    wgs84maxLon = max;
                } else {
                    lowX  = min;
                    highX = max;
                }
                minX = min;
                maxX = max;
            // Y AXIS
            } else if (el.getType().equals(AxisTypes.Y_AXIS)) {
                crsSourceY = el.getNativeCrs();
                if (CrsUtil.CrsUri.areEquivalent(crsSourceY, CrsUtil.CrsUri(CrsUtil.EPSG_AUTH, CrsUtil.WGS84_EPSG_CODE))) {
                    wgs84minLat = min;
                    wgs84maxLat = max;
                } else {
                    lowY  = min;
                    highY = max;
                }
                minY = min;
                maxY = max;
            } else if (el.getType().equals(AxisTypes.ELEV_AXIS)) {
                minZ = min;
                maxZ = max;
            }
        }

        // Consistency checks
        if (!crsSourceX.isEmpty() && !crsSourceY.isEmpty()) {
            if (!CrsUtil.CrsUri.areEquivalent(crsSourceX, crsSourceY)) {
                throw new WCPSException(ExceptionCode.InvalidMetadata,
                        "Invalid bounding box: X and Y axis have different CRS:" + crsSourceX + "<->" + crsSourceY);
            }
        }

        log.trace(toString());
    }

    @Override
    public Bbox clone() {
        try {
            return new Bbox(crsName, domains, coverageName);
        } catch (WCPSException e) {
            log.warn(e.getMessage());
            return null;
        } catch (WCSException e) {
            log.warn(e.getMessage());
            return null;
        } catch (PetascopeException e) {
            log.warn(e.getMessage());
            return null;
        } catch (SecoreException e) {
            log.warn(e.getMessage());
            return null;
        }
    }

    @Override
    public String toString() {
        String extents = "";
        for (int i=0; i<minValues.size(); i++) {
            if (i > 0) extents += ", ";
            extents += types.get(i) + "(" + getMinValue(i) + ", " + getMaxValue(i) + ")";
        }
        return "CRS '" + getCrsName() + "' { Bounding Box [" + extents + "] }";
    }

    /**
     * @return the minValues of a specified axis.
     */
    public BigDecimal getMinValue(String axisName) {
        for (int i=0; i<names.size(); i++) {
            if (names.get(i).equals(axisName)) {
                return getMinValue(i);
            }
        }
        return null;
    }
    public BigDecimal getMinValue(int index) {
        return minValues.get(index);
    }

    /**
     * @return the maxValue of a specified axis.
     */
    public BigDecimal getMaxValue(String axisName) {
        for (int i=0; i<names.size(); i++) {
            if (names.get(i).equals(axisName)) {
                return getMaxValue(i);
            }
        }
        return null;
    }
    public BigDecimal getMaxValue(int index) {
        return maxValues.get(index);
    }

    /**
     * Returns the i-order of an axis.
     * @param axisName
     * @return the i-order of axisName
     */
    public Integer getIndex(String axisName) {
        for (int i=0; i<names.size(); i++) {
            if (names.get(i).equals(axisName)) {
                return i;
            }
        }
        return null;
    }

    /**
     * @return the CRS name
     */
    public String getCrsName() {
        return crsName;
    }

    /**
     * @return the CRS name where the spatial CRS is replaced by WGS84 URI.
     */
    public String getWgs84CrsName() {
        List<String> crsUris; // avoid CRS duplication if some axes share the CRS; keep order.
        if (CrsUtil.CrsUri.isCompound(crsName)) {
            // Extract the involved atomic CRSs:
            /* Assumption: suppose that CRS involving height are 2D+1D, not 3D: still not considering
             * 3D->WGS84 transforms.
             */
            crsUris = new ArrayList<String>();
            for (DomainElement dom : domains) {
                if (dom.getType().equals(AxisTypes.X_AXIS) ||
                        dom.getType().equals(AxisTypes.Y_AXIS)) {
                    crsUris.add(CrsUtil.CrsUri(CrsUtil.EPSG_AUTH, CrsUtil.WGS84_EPSG_CODE));
                } else
                    crsUris.add(dom.getNativeCrs());
            }

            // Build the compound CRS:
            String ccrs = CrsUtil.CrsUri.createCompound(crsUris);
            return ccrs;

        } else
            return hasWgs84Bbox ? CrsUtil.CrsUri(CrsUtil.EPSG_AUTH, CrsUtil.WGS84_EPSG_CODE) : "";
    }

    /**
     * @return the coverage name
     */
    public String getCoverageName() {
        return coverageName;
    }

    /**
     * @return The lower-corner longitude of the WGS bounding box.
     */
    public Double getWgs84minLon() {
        return wgs84minLon;
    }

    /**
     * @return The upper-corner longitude of the WGS bounding box.
     */
    public Double getWgs84maxLon() {
        return wgs84maxLon;
    }

    /**
     * @return The lower-corner latitude of the WGS bounding box.
     */
    public Double getWgs84minLat() {
        return wgs84minLat;
    }

    /**
     * @return The upper-corner latitude of the WGS bounding box.
     */
    public Double getWgs84maxLat() {
        return wgs84maxLat;
    }

   /**
     * @return Whether a WGS84 bounding box has been computed for this object.
     */
    public Boolean hasWgs84Bbox() {
        return hasWgs84Bbox;
    }

    /**
     * @return The dimensionality of the bounding box (namely of the coverage).
     */
    public int getDimensionality() {
        return domains.size();
    }

    /**
     * @return The number of the spatial dimensions in the bounding box (for CRS transform purposes)
     */
    private int getSpatialDimensionality() {
        int counter = 0;
        for (DomainElement domain : domains) {
            if (domain.getType().equals(AxisTypes.X_AXIS)
                    || domain.getType().equals(AxisTypes.Y_AXIS)
                    || domain.getType().equals(AxisTypes.ELEV_AXIS))
                counter += 1;
        }
        return counter;
    }

    /**
     * @return The type of the specified axis of the bounding box.
     */
    public String getType(int i) {
        return domains.get(i).getType();
    }

    /**
     * XML-related utilities: list the bbox corners, separated each other with a white space.
     * For WGS84 corners: if X or Y domains then put the WGS84 value, otherwise leave it as is.
     * @return The String of concatenated corner coordinates.
     * NOTE: redundant with GetCoverageMetadata fields, but here can manage WGS84 equivalent corners.
     */
    public String getLowerCorner() {
        String output = "";
        BigDecimal tmp;
        // Loop through the N dimensions
        for (int i = 0; i < getDimensionality(); i++) {
            if (i>0) output += " ";
            tmp = getMinValue(i);
            // Fill possible space between date and time in timestamp with "T" (ISO:8601)
            // Disable: breaks XML schema (requires xs:double)
            //if (getType(i).equals(AxisTypes.T_AXIS)) {
            //    tmp = tmp.replaceFirst(" ", TimeUtil.ISO8601_T_KEY);
            //}
            output += tmp.toPlainString();
        }
        return output;
    }
    // Used when get the corner only for a subset of the whole available axes (eg. gml:origin after slicing)
    // Convert numbers to plain Strings: double get `E' formatting otherwise.
    public String getLowerCorner(String[] axisLabels) {
        String output = "";
        BigDecimal tmp;
        // Loop through the N dimensions
        for (int i = 0; i < axisLabels.length; i++) {
            if (i>0) output += " ";
            tmp = getMinValue(axisLabels[i]);
            // Fill possible space between date and time in timestamp with "T" (ISO:8601)
            // Disable: breaks XML schema (requires xs:double)
            //if (getType(i).equals(AxisTypes.T_AXIS)) {
            //    tmp = tmp.replaceFirst(" ", TimeUtil.ISO8601_T_KEY);
            //}
            output += tmp.toPlainString();
        }
        return output;
    }
    public String getWgs84LowerCorner() {
        String output = "";
        BigDecimal tmp;
        // Loop through the N dimensions
        for (int i = 0; i < getDimensionality(); i++) {
            if (i>0) output += " ";
            if (getType(i).equals(AxisTypes.X_AXIS)) output += getWgs84minLon(); else
            if (getType(i).equals(AxisTypes.Y_AXIS)) output += getWgs84minLat();
            else {
                tmp = getMinValue(i);
                // Fill possible space between date and time in timestamp with "T" (ISO:8601)
                // Disable: breaks XML schema (requires xs:double)
                //if (getType(i).equals(AxisTypes.T_AXIS))
                //    tmp = tmp.replaceFirst(" ", TimeUtil.ISO8601_T_KEY);
                output += tmp.toPlainString();
            }
        }
        return output;
    }
    public String getUpperCorner() {
        String output = "";
        BigDecimal tmp;
        // Loop through the N dimensions
        for (int i = 0; i < getDimensionality(); i++) {
            if (i>0) output += " ";
            tmp = getMaxValue(i);
            // Fill possible space between date and time in timestamp with "T" (ISO:8601)
            // Disable: breaks XML schema (requires xs:double)
            //if (getType(i).equals(AxisTypes.T_AXIS))
            //    tmp = tmp.replaceFirst(" ", TimeUtil.ISO8601_T_KEY);
            output += tmp.toPlainString();
        }
        return output;
    }
    // Used when get the corner only for a subset of the whole available axes
    public String getUpperCorner(String[] axisLabels) {
        String output = "";
        BigDecimal tmp;
        // Loop through the N dimensions
        for (int i = 0; i < axisLabels.length; i++) {
            if (i>0) output += " ";
            tmp = getMaxValue(axisLabels[i]);
            // Fill possible space between date and time in timestamp with "T" (ISO:8601)
            // Disable: breaks XML schema (requires xs:double)
            //if (getType(i).equals(AxisTypes.T_AXIS)) {
            //    tmp = tmp.replaceFirst(" ", TimeUtil.ISO8601_T_KEY);
            //}
            output += tmp.toPlainString();
        }
        return output;
    }
    public String getWgs84UpperCorner() {
        String output = "";
        BigDecimal tmp;
        // Loop through the N dimensions
        for (int i = 0; i < getDimensionality(); i++) {
            if (i>0) output += " ";
            if (getType(i).equals(AxisTypes.X_AXIS)) output += getWgs84maxLon(); else
            if (getType(i).equals(AxisTypes.Y_AXIS)) output += getWgs84maxLat();
            else {
                tmp = getMaxValue(i);
                // Fill possible space between date and time in timestamp with "T" (ISO:8601)
                // Disable: breaks XML schema (requires xs:double)
                //if (getType(i).equals(AxisTypes.T_AXIS))
                //    tmp = tmp.replaceFirst(" ", TimeUtil.ISO8601_T_KEY);
                output += tmp.toPlainString();
            }
        }
        return output;
    }

    /**
     * @return minimum X coordinate (spatial), or null if non is set.
     */
    public Double getMinX() {
        return minX;
    }

    /**
     * @return maximum X coordinate (spatial), or null if non is set.
     */
    public Double getMaxX() {
        return maxX;
    }
    /**
     * @return minimum Y coordinate (spatial), or null if non is set.
     */
    public Double getMinY() {
        return minY;
    }

    /**
     * @return maximum Y coordinate (spatial), or null if non is set.
     */
    public Double getMaxY() {
        return maxY;
    }

    /**
     * @return minimum Z coordinate (spatial), or null if non is set.
     */
    public Double getMinZ() {
        return minZ;
    }

    /**
     * @return maximum Z coordinate (spatial), or null if non is set.
     */
    public Double getMaxZ() {
        return maxZ;
    }
}
