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
package petascope.wcps.subset_axis.model;

import petascope.wcps.metadata.model.Axis;

/**
 * Translation node from wcps axis iterator to rasql
 * Example:
 * <code>
 * $px x(0:100)
 * </code>
 * translates to
 * <code>
 * x in [0:100]
 * </code>
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class AxisIterator extends AxisSpec {
    /**
     * Constructor for the class
     *
     * @param axisIteratorName the name of the variable used to iterate
     * @param subsetDimension      the interval on which to iterate
     */
    public AxisIterator(String axisIteratorName, String axisName, WcpsSubsetDimension subsetDimension, Axis axis) {
        super(subsetDimension);
        this.aliasName = axisIteratorName;
        this.axisName = axisName;
        this.axis = axis;
    }

    /**
     * Constructor for the class
     * @param axisIteratorName the name of the variable used to iterate
     * @param subsetDimension the interval on which to iterate
     * @param axisIteratorOrder the order of the axis iterator in the coverage
     * @param rasqlAliasName the rasql alias name of multiple axis iterators
     */
    public AxisIterator(String axisIteratorName, String axisName, WcpsSubsetDimension subsetDimension, int axisIteratorOrder, String rasqlAliasName) {
        super(subsetDimension);
        this.aliasName = axisIteratorName;
        this.axisName = axisName;
        this.axisIteratorOrder = axisIteratorOrder;
        this.rasqlAliasName = rasqlAliasName;
        this.axis = null;
    }

    /**
     * Returns the iterator name
     *
     * @return
     */
    public String getAliasName() {
        return aliasName;
    }

    public int getAxisIteratorOrder() {
        return axisIteratorOrder;
    }

    public String getRasqlAliasName() {
        return rasqlAliasName;
    }

    public void setRasqlAliasName(String rasqlAliasName) {
        this.rasqlAliasName = rasqlAliasName;
    }

    /**
     * Set the order of axis iterator inside coverage constructor / coverage condenser
     * @param axisIteratorOrder the index of axis iterator inside a coverage (e.g: $px x(0:20): order: 0, $py y(0:20): order: 1)
    */
    public void setAxisIteratorOrder(int axisIteratorOrder) {
        this.axisIteratorOrder = axisIteratorOrder;
    }
    
    public String getAxisName() {
        return this.axisName;
    }
    
    /**
     * In case axisIterator from $px Y(domain(c, Lat)), then this.axis returns Lat axis
     */
    public Axis getAxis() {
        return this.axis;
    }
    
    /**
     * Current only support Axis Iterator on 1D interval which already translated to grid interval.
     */
    public static final String AXIS_NAME_DEAULT = "x";
    public static final String CRS_DEFAULT = "Index1D";

    /**
     * This is the alias name of the axis iterator (e.g: $px)
     */
    private final String aliasName;
    
    // e.g. X
    private final String axisName;
    
    // Source axis is used from domain($c, axisLabel)
    private final Axis axis;

    /**
     * This is the alias name of axis iterator in Rasql (e.g: px [0:20,0:30:0:50])
     * all of axis iterators which are belonged to same coverage, use the same rasqlAliasName but different orders (e.g: px[0], px[1], px[2])
     */
    private String rasqlAliasName;
    private int axisIteratorOrder;
}
