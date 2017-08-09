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
package petascope.wcps.metadata.service;

import org.apache.commons.lang3.StringUtils;
import petascope.wcps.subset_axis.model.WcpsSubsetDimension;
import petascope.wcps.metadata.model.Axis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import petascope.wcps.metadata.model.NumericSlicing;
import petascope.wcps.metadata.model.NumericSubset;
import petascope.wcps.metadata.model.NumericTrimming;
import petascope.wcps.metadata.model.Subset;
import petascope.wcps.result.WcpsMetadataResult;
import petascope.wcps.subset_axis.model.AxisIterator;
import petascope.wcps.subset_axis.model.WcpsSliceSubsetDimension;
import petascope.wcps.subset_axis.model.WcpsTrimSubsetDimension;

/**
 * @author <a href="merticariu@rasdaman.com">Vlad Merticariu</a>
 */
@Service
public class RasqlTranslationService {

    /**
     * Constructs the rasql domain corresponding to the current list of axes.
     *
     * @param axes list of axes of coverage sorted by grid order
     * @param subsetAxisIteratorDimensions list of subset dimensions which contains "$" as axis iterator
     * @param axisIteratorAliasRegistry list of axis iterator alias and their subset dimensions
     * @return
     */
    public String constructRasqlDomain(List<Axis> axes, List<WcpsSubsetDimension> subsetAxisIteratorDimensions, AxisIteratorAliasRegistry axisIteratorAliasRegistry) {        
        
        String rasqlDomain = "";
        String result = "";
        List<String> translatedDomains = new ArrayList<>();
        for (Axis axis : axes) {
            //we should use the grid bounds, unless we have a dollar subset on this axis
            boolean dollarSubsetFound = false;
            for (WcpsSubsetDimension dollarSubset : subsetAxisIteratorDimensions) {
                if (dollarSubset.getAxisName().equals(axis.getLabel())) {
                    // found this subset containing axis iterator alias (e.g: c[i($px), j($px+$py)]
                    // NOTE: need to change axis iterator alias with the correct order inside a coverage
                    // e.g: Marray px in [0:20,0:20] values c[ px[0],px[0] + px[1] ]
                    String subsetDimensionStr = dollarSubset.getStringRepresentation();
                    result = this.replaceAxisIteratorAliasNames(subsetDimensionStr, axisIteratorAliasRegistry);
                    dollarSubsetFound = true;
                    break;
                }
            }
            if (!dollarSubsetFound) {
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
                            + ":" + ((WcpsTrimSubsetDimension) subsetDimension).getUpperBound());
            } else {
                results.add(((WcpsSliceSubsetDimension) subsetDimension).getBound());
            }

        }
        return StringUtils.join(results, ",");
    }

    /**
     * Constructs a TrimmingSubsetDimension from result of WcpsMetadataResult
     * such as imageCrsdomain(), domain(). Used in axis iterator, e.g: $px x
     * (imageCrsdomain(c[Lat(0:30)], Lat)) -> i_i in (80:120) in Rasql
     *
     * @param wcpsMetadataResult
     * @return SubsetDimension
     */
    public WcpsSubsetDimension constructRasqlDomainFromWcpsMetadataDomainInterval(WcpsMetadataResult wcpsMetadataResult) {
        // NOTE: it only support 1D domain for the axis iterator
        Axis axis = wcpsMetadataResult.getMetadata().getAxes().get(0);
        String axisName = axis.getLabel();
        String crsUri = axis.getNativeCrsUri();
        String lowerBound = ((NumericTrimming) axis.getGridBounds()).getLowerLimit().toPlainString();
        String upperBound = ((NumericTrimming) axis.getGridBounds()).getUpperLimit().toPlainString();
        WcpsSubsetDimension trimSubsetDimension = new WcpsTrimSubsetDimension(axisName, crsUri, lowerBound, upperBound);

        return trimSubsetDimension;
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
            if (subset.getAxisName().equals(axisName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Extract all the axis iterator alias names in the subset dimension
     * e.g: i($px + $py) or j($px)
     * then replace with the rasql axis iterator (e.g: px[0])
     * @param subsetDimensionStr the subset of interval in string
     * @return the subsetDimensionStr which has replaced axis iterator alias name with the rasql alias name
     */
    private String replaceAxisIteratorAliasNames(String subsetDimensionStr, AxisIteratorAliasRegistry axisIteratorAliasRegistry) {
        List<String> axisIteratorAlias = new ArrayList<String>();

        Pattern pattern = Pattern.compile("\\$.*?(?=(\\+|-|\\*|/|\\)|:|\\s|$))");
        Matcher matcher = pattern.matcher(subsetDimensionStr);
        while (matcher.find()) {
            // Add the found axis iterator alias name to the list
            axisIteratorAlias.add(matcher.group(0));
        }

        // replace all the axis iterator alias name with the translated rasql alias name
        // e.g: $px -> px[0]
        for (String alias : axisIteratorAlias) {
            AxisIterator axisIterator = axisIteratorAliasRegistry.getAxisIterator(alias);
            // e.g: px[0]
            String rasqlAlias = axisIterator.getRasqlAliasName() + "[" + axisIterator.getAxisIteratorOrder() + "]";
            // Replace the alias name with rasql alias
            subsetDimensionStr = subsetDimensionStr.replace(alias, rasqlAlias);
        }

        return subsetDimensionStr;
    }
}
