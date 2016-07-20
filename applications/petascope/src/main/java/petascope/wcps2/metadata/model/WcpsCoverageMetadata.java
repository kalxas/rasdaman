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

import java.util.ArrayList;
import petascope.wcps2.error.managed.processing.InvalidAxisNameException;

import java.util.List;
import petascope.core.CrsDefinition;
import petascope.util.AxisTypes;
import petascope.wcps2.metadata.service.CrsUtility;

/**
 * Class that keeps information about the coverages (such as domains, crses
 * etc.) in the WCPS tree.
 *
 * @author <a href="merticariu@rasdaman.com">Vlad Merticariu</a>
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
public class WcpsCoverageMetadata {

    private final String coverageName;
    private String coverageType;
    private final List<Axis> axes;
    // output bounding box of coverage, used in case of scale, extend
    // e.g: scale(c, imageCrsdomain(c[Lat(0:20)], Long(0:30)])) then it has 2 axes (Lat(0:20), Long(0:30))
    private final List<Axis> axesBBox = new ArrayList<Axis>();
    private final String crsUri;
    // use in crsTransform()
    private String outputCrsUri;
    private final List<RangeField> rangeFields;

    // based on number of axes to create a grid crs (e.g: Index2D)
    private final String gridCrsUri;
    private List<Double> nodata;
    private String metadata;

    public WcpsCoverageMetadata(String coverageName, String coverageType, List<Axis> axes, String crsUri, String gridCrsUri,
                                List<RangeField> rangeFields, String metadata, List<Double> nodata) {
        this.crsUri = crsUri;
        this.axes = axes;
        this.coverageName = coverageName;
        this.rangeFields = rangeFields;
        this.gridCrsUri = CrsUtility.stripBoundingQuotes(gridCrsUri);
        this.metadata = metadata;
        this.nodata = nodata;
        this.coverageType = coverageType;
    }

    public List<Axis> getAxes() {
        return this.axes;
    }

    public String getCrsUri() {
        return this.crsUri;
    }

    public String getGridCrsUri() {
        return this.gridCrsUri;
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
     * add the axes for the bounding box (then can use later with set
     * xmin,xmax,ymin,ymax)
     *
     * @return
     */
    public List<Axis> getAxesBBox() {
        return this.axesBBox;
    }

    public List<Double> getNodata() {
        return nodata;
    }

    public void setNodata(List<Double> nodata) {
        this.nodata = nodata;
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
