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
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.rasdaman.domain.cis.CoveragePyramid;
import org.rasdaman.domain.cis.NilValue;
import org.slf4j.LoggerFactory;
import petascope.core.AxisTypes;
import petascope.core.BoundingBox;
import petascope.core.Pair;
import petascope.core.gml.metadata.model.CoverageMetadata;
import petascope.core.gml.metadata.service.CoverageMetadataService;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.WCPSException;
import petascope.util.BigDecimalUtil;
import petascope.util.CrsUtil;
import petascope.util.JSONUtil;
import petascope.util.ListUtil;
import petascope.wcps.exception.processing.CoverageAxisNotFoundExeption;
import petascope.wcps.metadata.service.AxesOrderComparator;
import petascope.wcps.metadata.service.CrsUtility;

/**
 * Class that keeps information about the coverages (such as domains, CRSs etc.)
 * in the WCPS tree.
 *
 * @author <a href="merticariu@rasdaman.com">Vlad Merticariu</a>
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class WcpsCoverageMetadata {
    
    CoverageMetadataService coverageMetadataService = new CoverageMetadataService();

    protected String coverageName;   
    // NOTE: rasdaman collection name can be different from coverageName (in case of import a coverageName which is duplicate to an existing collectionName)
    // then coverage will create a new collectionName_datetime to store data.
    protected String rasdamanCollectionName;
    protected BigDecimal downscaledLevel;
    // In case a coverage is created temporarily from uploaded file path
    protected String decodedFilePath;
    protected String coverageType;
    // List of axes after coverage expression (it will be stripped when there is a slicing expression, 
    // e.g: c[Lat(20)] then output axes are Long and t with c is a 3D coverages (CRS: EPSG:4326&AnsiDate)

    protected List<Axis> axes;
    protected String crsUri;
    // use in crsTransform()
    protected String outputCrsUri;
    protected List<RangeField> rangeFields;
    protected List<List<NilValue>> nilValues;
    protected String metadata;
    protected CoverageMetadata coverageMetadata;
    
    // NOTE: By default, original axes are the coverage's axes persisted in database and not used anywhere (use axes instead!)
    // But in case WCPS coverage changes completely e.g: clip(c, linestring()) from 2D geo coverage -> 1D grid coverage
    // Then, axes are changed to contain only 1D grid axis and original axes is updated to previous axes (geo coverage).
    protected List<Axis> originalAxes;

    // To mark this object is created from a condenser
    private boolean condenserResult = false;
    
    private CoveragePyramid coveragePyramid;
    
    
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(WcpsCoverageMetadata.class);
    
    public WcpsCoverageMetadata() {
        
    }
    
    
    public WcpsCoverageMetadata(String coverageName, String rasdamanCollectionName, String coverageType, List<Axis> axes, String crsUri,
            List<RangeField> rangeFields, List<List<NilValue>> nilValues, String metadata, List<Axis> originalAxes) throws PetascopeException {
        this.crsUri = crsUri;
        // this axes could be stripped when a slicing expression is processed
        this.axes = axes;
        this.coverageName = coverageName;
        this.rasdamanCollectionName = rasdamanCollectionName;
        this.rangeFields = rangeFields;
        this.nilValues = nilValues;
        this.metadata = (metadata == null ? "" : metadata);
        this.coverageType = coverageType;
        this.coverageMetadata = this.buildCoverageMetadata(metadata, axes);
        this.originalAxes = originalAxes;
    }
    
    /**
     * Build CoverageMetadata object from coverage's metadata in string and coverage's axes.
     */
    private CoverageMetadata buildCoverageMetadata(String metadata, List<Axis> axes) {
        CoverageMetadata covMetadata = new CoverageMetadata();
        try {
            covMetadata = this.coverageMetadataService.deserializeCoverageMetadata(metadata);            
        } catch (Exception ex) {
            log.warn("Cannot deserialize coverage's metadata string to CoverageMetadata object. Reason: " + ex.getMessage(), ex);
        }
        
        try {
            if (covMetadata.getLocalMetadata() != null) {
                covMetadata.getLocalMetadata().buildEnvelopeSubsetsForChildList(axes);
            }
        } catch (PetascopeException ex) {
            log.warn("Cannot build envelope subsets from coverage's local metadata. Reason: " + ex.getMessage(), ex);
        }
        
        return covMetadata;
    }

    @JsonIgnore
    public Integer getGridDimension() {
        return axes.size();
    }

    public void setAxes(List<Axis> axes) {
        this.axes = axes;
    }
    
    /**
     * Replace an existing axis with new axis by index in axis list.
     */
    public void updateAxisByIndex(int index, Axis axis) {
        this.axes.set(index, axis);
    }

    /**
     * Return the list of axes by the CRS order e.g: EPSG:4326&AnsiDate, then
     * order is Lat, Long, Ansi
     *
     * @return
     */
    public List<Axis> getAxes() {
        if (this.axes == null) {
            return new ArrayList<>();
        }
        
        return this.axes;
    }
    
    /**
     * Find the geo order of an original axis by name.
     */
    @JsonIgnore
    public int getOriginalAxisGeoOrder(String axisName) throws PetascopeException {
        int i = 0;
        for (Axis originalAxis : this.originalAxes) {
            if (originalAxis.getLabel().equals(axisName)) {
                return i;
            }
            
            i++;
        }
        
        throw new PetascopeException(ExceptionCode.InvalidRequest, "Cannot find original axis '" + axisName + "' from WCPS coverage metadata.");
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
    @JsonIgnore
    public List<Axis> getSortedAxesByGridOrder() {
        List<Axis> sortedAxis = new ArrayList<>();
        // create a copy of the original list
        for (Axis axis : this.axes) {
            if (axis instanceof RegularAxis) {
                sortedAxis.add((RegularAxis) axis);
            } else {
                sortedAxis.add((IrregularAxis) axis);
            }
        }

        // then sort this list by the grid order
        Collections.sort(sortedAxis, new AxesOrderComparator());

        return sortedAxis;
    }
    
    /**
     * Get geo order for input axis
     */
    @JsonIgnore
    public int getAxisGeoOrder(String axisName) {
        int i = 0;
        for (Axis axis : this.axes) {
            if (CrsUtil.axisLabelsMatch(axis.getLabel(), axisName)) {
                return i;
            }
            
            i++;
        }
        throw new CoverageAxisNotFoundExeption(axisName);
    }
    
    /**
     * Get grid order (rasdaman order) for input axis
     */
    @JsonIgnore
    public int getAxisGridOrder(String axisName) {
        List<Axis> axesTmp = this.getSortedAxesByGridOrder();
        for (int i = 0; i < axesTmp.size(); i++) {
            if (CrsUtil.axisLabelsMatch(axesTmp.get(i).getLabel(), axisName)) {
                return i;
            }
        }
        throw new CoverageAxisNotFoundExeption(axisName);
    }
    
    public List<Axis> getOriginalAxes() {
        return this.originalAxes;
    }
    
    @JsonIgnore
    public int getNumberOfOriginalAxes() {
        return this.originalAxes.size();
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
    @JsonIgnore
    public List<Axis> getSortedOriginalAxesByGridOrder() {
        List<Axis> sortedAxis = new ArrayList<>();
        // create a copy of the original list
        for (Axis axis : this.originalAxes) {
            if (axis instanceof RegularAxis) {
                sortedAxis.add((RegularAxis) axis);
            } else {
                sortedAxis.add((IrregularAxis) axis);
            }
        }

        // then sort this list by the grid order
        Collections.sort(sortedAxis, new AxesOrderComparator());

        return sortedAxis;
    }
    
    /**
     * Find original axis by name.
     */
    @JsonIgnore
    public Axis getOriginalAxisByName(String axisName) {
        for (int i = 0; i < originalAxes.size(); i++) {
            Axis axis = originalAxes.get(i);
            if (axis.getLabel().equals(axisName)) {
                return axis;
            }
        }
        throw new CoverageAxisNotFoundExeption(axisName);
    }
    
    /**
     * Find original axis by grid order (rasdaman order).
     */
    @JsonIgnore
    public Axis getOriginalAxisByGridOrder(int index) {
        List<Axis> axesTmp = this.getSortedOriginalAxesByGridOrder();
        Axis originalAxis = axesTmp.get(index);
        
        return originalAxis;
    }

    public String getCrsUri() {
        return this.crsUri;
    }

    public void setCrsUri(String crsUri) {
        this.crsUri = crsUri;
    }

    /**
     * Update coverage's native CRS URIs based on current axes
     */
    public void updateCrsUri() {
        
        List<String> axisCrsUris = new ArrayList<>();
        for (Axis axis : this.axes) {
            axisCrsUris.add(axis.getNativeCrsUri());
        }
        
        String newCrsUri = CrsUtil.CrsUri.createCompound(axisCrsUris);
        this.crsUri = newCrsUri;
    }

    public String getCoverageName() {
        return this.coverageName;
    }
    
    public String getRasdamanCollectionName() {
        return this.rasdamanCollectionName;
    }

    public void setRasdamanCollectionName(String rasdamanCollectionName) {
        this.rasdamanCollectionName = rasdamanCollectionName;
    }
    
    public BigDecimal getDownscaledLevel() {
        return downscaledLevel;
    }

    public void setDownscaledLevel(BigDecimal downscaledLevel) {
        this.downscaledLevel = downscaledLevel;
    }
    
    public String getDecodedFilePath() {
        return decodedFilePath;
    }

    public void setDecodedFilePath(String decodedFilePath) {
        this.decodedFilePath = decodedFilePath;
    }
    
    public void setRangeFields(List<RangeField> rangeFields) {
        this.rangeFields = rangeFields;
    }

    public List<RangeField> getRangeFields() {
        return this.rangeFields;
    }

    public boolean axisExists(String axisName) {
        for (Axis axis : this.axes) {
            if (axis.getLabel().equals(axisName)) {
                return true;
            }
        }
        return false;
    }

    @JsonIgnore
    public Axis getAxisByName(String axisName) {
        for (Axis axis : this.axes) {
            if (CrsUtil.axisLabelsMatch(axis.getLabel(), axisName)) {
                return axis;
            }
        }
        
        throw new CoverageAxisNotFoundExeption(axisName);
    }

    public String getGridId() {
        return getCoverageName() + "-grid";
    }
    
    public String getPointId() {
        return getCoverageName() + "-point";
    }
    
    public boolean isCondenserResult() {
        return this.condenserResult;
    }
    
    public void setCondenserResult(boolean value) {
        this.condenserResult = value;
    }
    
    /**
     * To support WMS the axis order is needed to swap correctly in the bounding box
     * @return 
     */
    @JsonIgnore
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
     * Check if this coverage object contains only 2 axes and they are XY axes, e.g: Long Lat.
     */
    @JsonIgnore
    public boolean containsOnlyXYAxes() {
        return this.axes.size() == 2 && this.getXYAxes().size() == 2;
    }

    /**
     * Return the XY axes from coverage (e.g: 3D x,y,t then axes are x,y) by
     * grid axis order (NOTE: not by CRS order (so EPSG:4326 returns Long (X), Lat (Y)).
     *
     * @return
     */
    @JsonIgnore
    public List<Axis> getXYAxes() {
        Map<Integer, Axis> map = new HashMap<>();
        for (Axis axis : this.getAxes()) {
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
     * Return axis which is not X or Y type
     */
    @JsonIgnore
    public List<Axis> getNonXYAxes() {
        List<Axis> nonXYAxes = new ArrayList<>(); 
        for (Axis axis : this.axes) {
            if (!(axis.getAxisType().equals(AxisTypes.X_AXIS) || axis.getAxisType().equals(AxisTypes.Y_AXIS))) {
                nonXYAxes.add(axis);
            }
        }
        
        return nonXYAxes;
    }

    /**
     * Get the geo-reference CRS which is used for X, Y axes only
     *
     * @return
     */
    @JsonIgnore
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
    @JsonIgnore
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
    @JsonIgnore
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
        
        if (this.rangeFields != null) {
            // NOTE: null values are the same for all bands
            List<NilValue> nilValuesTmp = this.rangeFields.get(0).getNodata();
            if (nilValuesTmp != null) {
                for (NilValue nilValue : this.rangeFields.get(0).getNodata()) {
                    nodataValues.add(nilValue);
                }
            }
        }
        
        return nodataValues;
    }

    public String getMetadata() {        
        return metadata;
    }
    
    public CoverageMetadata getCoverageMetadata() {
        return this.coverageMetadata;
    }

    public String getCoverageType() {
        return coverageType;
    }

    public void setCoverageType(String coverageType) {
        this.coverageType = coverageType;
    }

    public List<List<NilValue>> getNilValues() {
        return nilValues;
    }

    public void setNilValues(List<List<NilValue>> nilValues) {
        this.nilValues = nilValues;
    }

    public CoveragePyramid getCoveragePyramid() {
        return coveragePyramid;
    }

    public void setCoveragePyramid(CoveragePyramid coveragePyramid) {
        this.coveragePyramid = coveragePyramid;
    }
    
    /**
     * Return the offset vector of an axis in coverage.
     * e.g: Offset vector of Lat axis is: -20 0 0
     */
    public String getOffsetVectorByAxisLabel(String axisLabel) {
        String offsetVector = "";
        
        for (Axis axis : this.axes) {
            if (axisLabel.equals(axis.getLabel())) {
                offsetVector += BigDecimalUtil.stripDecimalZeros(axis.getResolution()).toPlainString() + " ";
            } else {
                offsetVector += BigInteger.ZERO + " ";
            }
        }
        
        return offsetVector.trim();
    }

    /**
     * Return a concatenated string for geo axis names (e.g: Lat Long time)
     */
    @JsonIgnore
    public String getGeoAxisNames() {
        
        String geoAxisNames = "";
        
        for (Axis axis : this.axes) {
            geoAxisNames += axis.getLabel() + " ";
        }
        
        return geoAxisNames.trim();
    }
    
    @JsonIgnore
    public List<String> getGeoAxisNamesAsList() {
        List<String> results = new ArrayList<>();
        
        for (Axis axis : this.axes) {
            results.add(axis.getLabel());
        }
        
        return results;
    }
    
    /**
     * Return a concatenated string for grid axis names (e.g: i j k), start from ASCII i (65).
     */
    @JsonIgnore
    public String getGridAxisNames() {
        String gridAxisNames = "";
        
        for (int i = 0; i < this.axes.size(); i++) {
            String gridAxisName = Axis.createAxisLabelByIndex(i);
            gridAxisNames += " " + gridAxisName;
        }
        
        return gridAxisNames.trim();
    }
    
    @JsonIgnore
    public List<String> getGridAxisNamesAsList() {
        List<String> results = new ArrayList<>();
        
        for (int i = 0; i < this.axes.size(); i++) {
            String gridAxisName = Axis.createAxisLabelByIndex(i);
            results.add(gridAxisName);
        }
        
        return results;
    }
    
    /**
     * Return IndexND CRS from number of axes in coverage.
     * e.g: opengis.net/def/crs/OGC/0/Index3D
     */
    @JsonIgnore
    public String getIndexCrsUri() {
        String indexCRS = CrsUtility.createIndexNDCrsUri(axes);
        
        return indexCRS;
    }
    
    /**
     * Get current grid domains of coverage with rasdaman oder.
     */
    @JsonIgnore
    public String getGridDomainsRepresentation() {
        List<Axis> gridAxes = this.getSortedAxesByGridOrder();
        List<String> temp = new ArrayList<>();
        for (Axis axis : gridAxes) {
            temp.add(axis.getGridBounds().getStringRepresentation());
        }
        
        String result = "[" + ListUtil.join(temp, ",") + "]";
        return result;
    }
    
    /**
     * Check if coverage is sliced on X or Y axis.
     */
    @JsonIgnore
    public boolean isSlicingOnXYAxis() {
        if (this.getXYAxes().size() > 0) {
            Axis axisX = this.getXYAxes().get(0);
            Axis axisY = this.getXYAxes().get(1);
            
            if ((axisX.getGridBounds() instanceof NumericSlicing)
                || (axisY.getGridBounds() instanceof NumericSlicing)) {
                return true;
            }            
        }
        
        return false;
    }
    
    /**
     * Return a BoundingBox object with original lower/upper bounds 
     * on XY axes of a coverage.
     */
    @JsonIgnore
    public BoundingBox getOrginalGeoXYBoundingBox() {
        List<Axis> xyAxes = this.getXYAxes();
        Axis axisX = xyAxes.get(0);
        Axis axisY = xyAxes.get(1);
        
        BoundingBox bbox = new BoundingBox(axisX.getOriginalGeoBounds().getLowerLimit(), axisY.getOriginalGeoBounds().getLowerLimit(),
                                           axisX.getOriginalGeoBounds().getUpperLimit(), axisY.getOriginalGeoBounds().getUpperLimit(), axisX.getNativeCrsUri());
        return bbox;
    }
    
    /**
     * Return the original grid bounding box for XY axes
     */
    @JsonIgnore
    public BoundingBox getOriginalGridXYBoundingBox() {
        List<Axis> xyAxes = this.getXYAxes();
        Axis axisX = xyAxes.get(0);
        Axis axisY = xyAxes.get(1);
        
        BoundingBox bbox = new BoundingBox(axisX.getOriginalGridBounds().getLowerLimit(), axisY.getOriginalGridBounds().getLowerLimit(),
                                           axisX.getOriginalGridBounds().getUpperLimit(), axisY.getOriginalGridBounds().getUpperLimit());
        
        return bbox;
    }
    
    /**
     * Check if this object has invalid geo /grid domains
     */
    @JsonIgnore
    public boolean isValidXYGeoGridDomains() {
        if (this.hasXYAxes()) {
            Axis axisX = this.getXYAxes().get(0);
            Axis axisY = this.getXYAxes().get(1);
            
            if ((axisX.getGeoBounds().getLowerLimit().compareTo(axisX.getGeoBounds().getUpperLimit()) > 0)
                || (axisY.getGeoBounds().getLowerLimit().compareTo(axisY.getGeoBounds().getUpperLimit()) > 0)
                || (axisX.getGridBounds().getLowerLimit().compareTo(axisX.getGridBounds().getUpperLimit()) > 0)
                || (axisY.getGridBounds().getLowerLimit().compareTo(axisY.getGridBounds().getUpperLimit()) > 0)
                ) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Return the single null value from the first band of coverage
     * e.g: list of null values ["9.96921e+35:*","9.96921E+36", "30"]
     * then returns 9.96921E+36.
     * 
     * If coverage has no null value, then return 0
     */
    public String getFirstBandSingleNullValue() {
        if (this.rangeFields != null) {
            RangeField firstRangeField = this.rangeFields.get(0);
            if (firstRangeField.getNodata() != null) {
                for (NilValue nilValue : firstRangeField.getNodata()) {
                    String nullValue = nilValue.getValue();
                    if (!nullValue.contains(":")) {
                        // e.g: 30;
                        String value = BigDecimalUtil.stripDecimalZeros(new BigDecimal(nullValue)).toPlainString();
                        return value;
                    }
                }
            }
        }
        
        return "0";
    }
    
    @Override
    public String toString() {
        return "Coverage Id '" + this.coverageName  + "'"; 
    }
    
    @JsonIgnore 
    /**
     * e.g. coverage has 2 axes -> Lat(20.3:30.5),Long(50.5:62.5)
     */
    public String getCoverageGeoBoundsRepresentation() throws PetascopeException {
        List<String> list = new ArrayList<>();
        for (Axis geoAxis : this.getAxes()) {
            String tmp = geoAxis.getLabel() + "(" + geoAxis.getLowerGeoBoundRepresentation() + ":" + geoAxis.getUpperGeoBoundRepresentation() + ")";
            list.add(tmp);
        }
        
        String result = ListUtil.join(list, ",");
        return result;
    }
    
}
