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
package petascope.wcps.metadata.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import java.util.List;
import java.util.Map;
import org.rasdaman.domain.cis.NilValue;
import petascope.core.AxisTypes;
import petascope.core.Pair;
import petascope.util.CrsUtil;
import petascope.wcps.exception.processing.CoverageAxisNotFoundExeption;
import petascope.wcps.metadata.service.AxesOrderComparator;

/**
 * Class that keeps information about the coverages (such as domains, CRSs etc.)
 * in the WCPS tree.
 *
 * @author <a href="merticariu@rasdaman.com">Vlad Merticariu</a>
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
public class WcpsCoverageMetadata {

    private final String coverageName;   
    // NOTE: rasdaman collection name can be different from coverageName (in case of import a coverageName which is duplicate to an existing collectionName)
    // then coverage will create a new collectionName_datetime to store data.
    private final String rasdamanCollectionName;
    private String coverageType;
    // List of axes after coverage expression (it will be stripped when there is a slicing expression, 
    // e.g: c[Lat(20)] then output axes are Long and t with c is a 3D coverages (CRS: EPSG:4326&AnsiDate)
    private List<Axis> axes;
    private final String crsUri;
    // use in crsTransform()
    private String outputCrsUri;
    private List<RangeField> rangeFields;
    private List<NilValue> nilValues;
    private String metadata;

    public WcpsCoverageMetadata(String coverageName, String rasdamanCollectionName, String coverageType, List<Axis> axes, String crsUri,
            List<RangeField> rangeFields, List<NilValue> nilValues, String metadata) {
        this.crsUri = crsUri;
        // this axes could be stripped when a slicing expression is processed
        this.axes = axes;
        this.coverageName = coverageName;
        this.rasdamanCollectionName = rasdamanCollectionName;
        this.rangeFields = rangeFields;
        this.nilValues = nilValues;
        this.metadata = (metadata == null ? "" : metadata);
        this.coverageType = coverageType;
    }

    public Integer getGridDimension() {
        return axes.size();
    }

    public void setAxes(List<Axis> axes) {
        this.axes = axes;
    }

    /**
     * Return the list of axes by the CRS order e.g: EPSG:4326&AnsiDate, then
     * order is Lat, Long, Ansi
     *
     * @return
     */
    public List<Axis> getAxes() {
        return this.axes;
    }

    /**
     * Return the list of axes by grid Order e.g: EPSG:4326&AnsiDate, the grid
     * axes order is: ansi, Long, Lat
     *
     * NOTE: used only when writing the rasql domains for each axis by grid
     * order
     *
     * @return
     */
    public List<Axis> getSortedAxesByGridOrder() {
        List<Axis> sortedAxis = new ArrayList<>();
        // create a copy of the original list
        for (Axis axis : this.axes) {
            if (axis instanceof RegularAxis) {
                sortedAxis.add(((RegularAxis) axis).clone());
            } else {
                sortedAxis.add(((IrregularAxis) axis).clone());
            }
        }

        // then sort this list by the grid order
        Collections.sort(sortedAxis, new AxesOrderComparator());

        return sortedAxis;
    }

    public String getCrsUri() {
        return this.crsUri;
    }

    public void setOutputCrsUri(String outputCrsUri) {
        this.outputCrsUri = outputCrsUri;
    }

    public String getOutputCrsUri() {
        return this.outputCrsUri;
    }

    public String getCoverageName() {
        return this.coverageName;
    }
    
    public String getRasdamanCollectionName() {
        return this.rasdamanCollectionName;
    }

    public void setRangeFields(List<RangeField> rangeFields) {
        this.rangeFields = rangeFields;
    }

    public List<RangeField> getRangeFields() {
        return this.rangeFields;
    }

    public Axis getAxisByName(String axisName) {
        for (Axis axis : this.axes) {
            if (axis.getLabel().equals(axisName)) {
                return axis;
            }
        }
        throw new CoverageAxisNotFoundExeption(axisName);
    }

    public String getGridId() {
        return getCoverageName() + "-grid";
    }
    
    /**
     * To support WMS the axis order is needed to swap correctly in the bounding box
     * @return 
     */
    public boolean isXYOrder() {        
        // e.g: 4326 in WMS is YX order (Lat, Long)        
        int xGridOrder = -1;
        int yGridOrder = -1;
        int i = 0;
        for (Axis axis : this.axes) {
            if (axis.getAxisType().equals(AxisTypes.X_AXIS)) {
                xGridOrder = i;
            } else if (axis.getAxisType().equals(AxisTypes.Y_AXIS)) {
                yGridOrder = i;
            }     
            i++;
        }        
        
        if (xGridOrder < yGridOrder) {
            return true;
        }
        
        return false;
    }

    /**
     * Return the XY axes from coverage (e.g: 3D x,y,t then axes are x,y) by
     * grid axis order (NOTE: not by CRS order (so EPSG:4326 returns Long (X), Lat (Y)).
     *
     * @return
     */
    public List<Axis> getXYAxes() {
        Map<Integer, Axis> map = new HashMap<>();
        for (Axis axis : this.axes) {
            // NOTE: the order must be XY if the coverage has X-Y axes, or only X or only Y when the coverage has CRS combination (e.g: Lat and Time axes)
            if (axis.getAxisType().equals(AxisTypes.X_AXIS)) {
                map.put(0, axis);
            } else if (axis.getAxisType().equals(AxisTypes.Y_AXIS)) {
                map.put(1, axis);
            }
        }

        return new ArrayList<>(map.values());
    }

    /**
     * Get the geo-reference CRS which is used for X, Y axes only
     *
     * @return
     */
    public String getXYCrs() {
        // NOTE: cannot combine CRS from 1 axis with geo-referenced CRS and 1 axis is time (or IndexND)
        // so if coverage returns with 1 axis is Lat and 1 axis is AnsiDate so the CRS for the coverage will be Index2D
        if (this.getXYAxes().size() < 2) {
            return CrsUtil.INDEX_CRS_PREFIX;
        }

        // X, Y axes have same CRS
        return this.getXYAxes().get(0).getNativeCrsUri();
    }
    
    /**
     * Return the geo-order of XY axes in coverage.
     * 
     * @return Pair of geo-order of XY axes
     */
    public Pair<Integer, Integer> getXYAxesOrder() {
        int xOrder = -1;
        int yOrder = -1;
        int index = 0;
        for (Axis axis : this.getAxes()) {
            if (axis.getAxisType().equals(AxisTypes.X_AXIS)) {
                xOrder = index;
            } else if (axis.getAxisType().equals(AxisTypes.Y_AXIS)) {
                yOrder = index;
            }
            if (xOrder != -1 && yOrder != -1) {
                return new Pair<>(xOrder, yOrder);
            }
            index++;
        }
        return null;      
    }
    
    /**
     * Check if coverage contains XY axes (e.g: Lat, Long).
     * With a sliced coverage on a Lat/Long axis, it doesn't contain XY axes.
     * @return true if coverage has XY axes or false
     */
    public boolean hasXYAxes() {
        if (this.getXYAxesOrder() != null) {
            return true;
        }
        return false;
    }

    /**
     * Get nodata values from Range fields to be consistent
     *
     * @return
     */
    public List<NilValue> getNodata() {
        List<NilValue> nodataValues = new ArrayList<>();
        for (RangeField rangeField : this.rangeFields) {
            // NOTE: current only support 1 range with 1 no data value
            if (rangeField.getNodata().size() > 0) {
                nodataValues.add(rangeField.getNodata().get(0));
            }
        }
        return nodataValues;
    }

    public String getMetadata() {        
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public String getCoverageType() {
        return coverageType;
    }

    public void setCoverageType(String coverageType) {
        this.coverageType = coverageType;
    }

    public List<NilValue> getNilValues() {
        return nilValues;
    }

    public void setNilValues(List<NilValue> nilValues) {
        this.nilValues = nilValues;
    }
}
