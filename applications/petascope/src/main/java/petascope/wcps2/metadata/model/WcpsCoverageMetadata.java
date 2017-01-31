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
package petascope.wcps2.metadata.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import petascope.wcps2.error.managed.processing.InvalidAxisNameException;

import java.util.List;
import java.util.Map;
import petascope.core.CrsDefinition;
import petascope.swe.datamodel.NilValue;
import petascope.util.AxisTypes;
import petascope.util.CrsUtil;

/**
 * Class that keeps information about the coverages (such as domains, CRSs
 * etc.) in the WCPS tree.
 *
 * @author <a href="merticariu@rasdaman.com">Vlad Merticariu</a>
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
public class WcpsCoverageMetadata {

    private final String coverageName;
    private String coverageType;
    private List<Axis> axes;
    private final String crsUri;
    // use in crsTransform()
    private String outputCrsUri;
    private List<RangeField> rangeFields;        
    private String metadata;

    public WcpsCoverageMetadata(String coverageName, String coverageType, List<Axis> axes, String crsUri,
                                List<RangeField> rangeFields, String metadata) {
        this.crsUri = crsUri;
        this.axes = axes;
        this.coverageName = coverageName;
        this.rangeFields = rangeFields;
        this.metadata = metadata;        
        this.coverageType = coverageType;

    }

    public Integer getGridDimension() {
        return axes.size();
    }

    public void setAxes(List<Axis> axes) {
        this.axes = axes;
    }

    public List<Axis> getAxes() {
        return this.axes;
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
        throw new InvalidAxisNameException(axisName);
    }

    public String getGridId(){
        return getCoverageName() + "-grid";
    }

    /**
     * Return the XY axes from coverage (e.g: 3D x,y,t then axes is x,y)
     * @return
     */
    public List<Axis> getXYAxes() {
        List<Axis> axisList = new ArrayList<Axis>();
        for (Axis axis : this.axes) {
            if (CrsDefinition.getAxisTypeByName(axis.getLabel()).equals(AxisTypes.X_AXIS)
                    || CrsDefinition.getAxisTypeByName(axis.getLabel()).equals(AxisTypes.Y_AXIS)) {
                axisList.add(axis);
            }
        }
        return axisList;
    }
    
    /**
     * Get the geo-reference CRS which is used for X, Y axes only
     * @return 
     */
    public String getXYCrs() {
        // NOTE: cannot combine CRS from 1 axis with geo-referenced CRS and 1 axis is time (or IndexND)
        // so if coverage returns with 1 axis is Lat and 1 axis is AnsiDate so the CRS for the coverage will be Index2D
        if (this.getXYAxes().size() < 2) {            
            return CrsUtil.INDEX_CRS_PREFIX;
        }
        
        // X, Y axes have same CRS
        return this.getXYAxes().get(0).getCrsUri();
    }

    /**
     * Get nodata values from Range fields to be consistent
     * @return 
     */
    public List<NilValue> getNodata() {
        List<NilValue> nodataValues = new ArrayList<NilValue>();
        for (RangeField rangeField: this.rangeFields) {
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

}
