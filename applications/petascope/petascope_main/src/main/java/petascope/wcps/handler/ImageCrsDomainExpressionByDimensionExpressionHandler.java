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
package petascope.wcps.handler;

import org.springframework.stereotype.Service;
import petascope.wcps.metadata.model.Axis;
import petascope.wcps.metadata.model.NumericSlicing;
import petascope.wcps.metadata.model.NumericTrimming;
import petascope.wcps.result.WcpsMetadataResult;
import petascope.wcps.result.WcpsResult;

/**
 * Translator class for the imageCrsDomain(coverageExpression, axisLabel)
 * operation in wcps  <code>
 * for c in (eobstest) return imageCrsDomain(c[Lat(20:30)], Lat)
 * </code> returns [120:170] in grid-coordinate
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
@Service
public class ImageCrsDomainExpressionByDimensionExpressionHandler extends AbstractOperatorHandler {
    
    public static final String OPERATOR = "imageCrsDomain";

    public WcpsMetadataResult handle(WcpsResult coverageExpression, String axisName) {
        
        checkOperandIsCoverage(coverageExpression, OPERATOR); 
        
        // just iterate the axes and get the grid bound for each axis
        String rasql = "";
        String tmp = "";

        Axis axis = coverageExpression.getMetadata().getAxisByName(axisName);
        if (axis.getGeoBounds() instanceof NumericTrimming) {
            // Trimming
            String lowBound = ((NumericTrimming) axis.getGridBounds()).getLowerLimit().toPlainString();
            String highBound = ((NumericTrimming) axis.getGridBounds()).getUpperLimit().toPlainString();

            tmp = TRIMMING_TEMPLATE.replace("$lowBound", lowBound)
                                    .replace("$highBound", highBound);
        } else {
            // Slicing
            String bound = ((NumericSlicing) axis.getGridBounds()).getBound().toPlainString();
            tmp = SLICING_TEMPLATE.replace("$lowBound", bound);
        }

        // (0:5)
        rasql = "(" + tmp + ")";
        
        WcpsMetadataResult metadataResult = new WcpsMetadataResult(null, rasql);
        return metadataResult;
    }

    private final String TRIMMING_TEMPLATE = "$lowBound:$highBound";
    private final String SLICING_TEMPLATE = "$lowBound";
}
