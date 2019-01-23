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
package petascope.wcps.metadata.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import petascope.util.CrsUtil;
import petascope.wcps.exception.processing.InvalidExpressionSubsetException;
import petascope.wcps.subset_axis.model.WcpsSubsetDimension;
import petascope.wcps.metadata.model.Axis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import petascope.wcps.metadata.model.NumericSlicing;
import petascope.wcps.metadata.model.NumericSubset;
import petascope.wcps.metadata.model.NumericTrimming;
import petascope.wcps.metadata.model.Subset;
import petascope.wcps.subset_axis.model.WcpsSliceSubsetDimension;
import petascope.wcps.subset_axis.model.WcpsTrimSubsetDimension;
import static petascope.util.ras.RasConstants.RASQL_BOUND_SEPARATION;

/**
 * @author <a href="merticariu@rasdaman.com">Vlad Merticariu</a>
 */
@Service
public class RasqlTranslationService {

    @Autowired
    private AxisIteratorAliasRegistry axisIteratorAliasRegistry;

    /**
     * Constructs the rasql domain corresponding to the current list of axes.
     *
     * @param axes list of axes of coverage sorted by grid order
     * @param nonNumericSubsets list of subset dimensions which contains "$" as axis iterator
     * @return
     */
    public String constructRasqlDomain(List<Axis> axes, List<WcpsSubsetDimension> nonNumericSubsets) {
        
        String rasqlDomain = "";
        String result = "";
        List<String> translatedDomains = new ArrayList<>();
        for (Axis axis : axes) {
            //we should use the grid bounds, unless we have a dollar subset on this axis
            boolean nonNumericSubsetFound = false;
            for (WcpsSubsetDimension nonNumericSubset : nonNumericSubsets) {
                if (CrsUtil.axisLabelsMatch(nonNumericSubset.getAxisName(), axis.getLabel())) {
                    // found subset containing axis iterator alias or expression
                    //only allow this kind of subsets for index and grid axes
                    boolean isSubsetValid = validateExpressionSubset(axis.getNativeCrsUri(), nonNumericSubset);
                    if (!isSubsetValid) {
                        throw new InvalidExpressionSubsetException(nonNumericSubset);
                    }
                    String subsetDimensionStr = nonNumericSubset.getStringBounds();

                    result = subsetDimensionStr;
                    nonNumericSubsetFound = true;
                    break;
                }
            }
            if (!nonNumericSubsetFound) {
                //ok, regular grid domain
                NumericSubset gridBounds = axis.getGridBounds();
                result = gridBounds.getStringRepresentationInInteger();
            }

            translatedDomains.add(result);
        }

        rasqlDomain = StringUtils.join(translatedDomains, ",");
        return rasqlDomain;
    }

    /**
     * Checks whether a subset specified as expression is valid.
     * An expression subset is valid if:
     *  - the axis on which it is specified is a grid axis
     *  - the axis on which it is specified is an index axis
     *  - for backwards compatibility, the subset expression references an iterator in a coverage constructor or condenser
     * @param axisCrs: the crs of the axis on which the subset is applied.
     * @param subset: the WCPS subset.
     * @return true if the subset is valid, false otherwise.
     */
    private boolean validateExpressionSubset(String axisCrs, WcpsSubsetDimension subset){
        String subsetCrs = subset.getCrs();
        //in case no subset crs is indicated, check if the axis' crs is index or grid
        if(StringUtils.isEmpty(subsetCrs) && (CrsUtil.isGridCrs(axisCrs) || CrsUtil.isIndexCrs(axisCrs))){
            return true;
        }
        //in case a subset crs is indicated, check if it is index or grid
        if(!StringUtils.isEmpty(subsetCrs) && (CrsUtil.isGridCrs(subsetCrs) || CrsUtil.isIndexCrs(subsetCrs))){
            return true;
        }
        //check whether the subset expression contains a rasql axis iterator
        for(String rasqlIter: axisIteratorAliasRegistry.getRasqlAxisIterators()){
            if(subset.getStringBounds().contains(rasqlIter)){
                return true;
            }
        }
        return false;
    }

    /**
     * This function will construct the rasql domain corresponding to the
     * *specific* list of axis used in extend and scale e.g: extend(c[t(0)], {
     * Lat(0:70), Long(0:150) }) with c is 3D return Rasql: c[0, 0:20, 0:30],
     * [0:200, 0:300]
     *
     * @param axes
     * @param subsets
     * @return
     */
    public String constructSpecificRasqlDomain(List<Axis> axes, List<Subset> subsets) {
        //sort the axes after the rasdaman order
        Collections.sort(axes, new AxesOrderComparator());
        String rasqlDomain = "";
        String result = "";
        List<String> translatedDomains = new ArrayList<>();
        for (Axis axis : axes) {
            // Only add the axis if it is defined in the subsets
            if (isNeededAxis(axis.getLabel(), subsets)) {
                NumericSubset gridBounds = axis.getGridBounds();
                if (gridBounds instanceof NumericSlicing) {
                    result = ((NumericSlicing) gridBounds).getBound().toBigInteger().toString();
                } else {
                    result = ((NumericTrimming) gridBounds).getLowerLimit().toBigInteger() + ":"
                             + ((NumericTrimming) gridBounds).getUpperLimit().toBigInteger();
                }
                translatedDomains.add(result);
            }
        }

        rasqlDomain = StringUtils.join(translatedDomains, ",");
        return rasqlDomain;
    }

    /**
     * Constructs the rasql domain corresponding to a list of subsets
     *
     * @param subsetDimensions
     * @return
     */
    public String constructRasqlDomainFromSubsets(List<WcpsSubsetDimension> subsetDimensions) {
        List<String> results = new ArrayList();
        for (WcpsSubsetDimension subsetDimension : subsetDimensions) {
            if (subsetDimension instanceof WcpsTrimSubsetDimension) {
                results.add(((WcpsTrimSubsetDimension) subsetDimension).getLowerBound()
                            + RASQL_BOUND_SEPARATION + ((WcpsTrimSubsetDimension) subsetDimension).getUpperBound());
            } else {
                results.add(((WcpsSliceSubsetDimension) subsetDimension).getBound());
            }

        }
        return StringUtils.join(results, ",");
    }


    /**
     * check if axisName is in the list of specific subsets, it not then don't
     * need to construct Rasql for this axis
     *
     * @param axisName
     * @param subsets
     */
    private boolean isNeededAxis(String axisName, List<Subset> subsets) {
        for (Subset subset : subsets) {
            if (CrsUtil.axisLabelsMatch(subset.getAxisName(), axisName)) {
                return true;
            }
        }
        return false;
    }
}
