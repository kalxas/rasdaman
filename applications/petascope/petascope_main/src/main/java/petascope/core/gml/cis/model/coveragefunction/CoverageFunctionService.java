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
package petascope.core.gml.cis.model.coveragefunction;

import java.util.List;
import org.springframework.stereotype.Service;
import petascope.wcps.metadata.model.Axis;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;

/**
 * Service class to build CoverageFunction.
 * 
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */

@Service
public class CoverageFunctionService {
    
    // ################## Build CoverageFunction
    
        /**
     * If a coverage imported with this sdom [0:35,0:17,0:3] with crs axes order (EPSG:4326&AnsiDate): lat (+1), long (+2), time (+3) 
     * and grid axes order (rasdaman order) as [long (+2), lat (+1), time (+3)]
     * then the result of WCS GetCoverage in GML will return tupleList element which contains the result of: encode(c, "JSON")
     * which iterate the sdom in grid axes order: from outer time axis to lat axis then inner long axis.
     * 
     * Hence, the sequenceRule must follow the way Rasdaman iterates and it should return: +3 +1 +2 (reversed order from sdom)
     */
    private SequenceRule buildSequenceRuleForGridFunction(WcpsCoverageMetadata wcpsCoverageMetadata) {

        String axisOrder = "";
        
        for (int i = wcpsCoverageMetadata.getSortedAxesByGridOrder().size() - 1; i >= 0; i--) {
            int sequenceNumber = 0;
            // Iterate by grid axes order reversed
            Axis axis = wcpsCoverageMetadata.getSortedAxesByGridOrder().get(i);
            for (int j = 0; j < wcpsCoverageMetadata.getAxes().size(); j++) {
                // Iterate by crs axes order
                if (wcpsCoverageMetadata.getAxes().get(j).getLabel().equals(axis.getLabel())) {
                    sequenceNumber = j + 1;
                    break;
                }
            }
            // e.g: +3 +1 +2 (for 3D coverage imported with grid axes order: long, lat, time)            
            axisOrder += "+" + sequenceNumber + " ";
        }
        
        SequenceRule sequenceRule = new SequenceRule(axisOrder.trim());
        return sequenceRule;
    }
    
    /**
     * Build GridFunction for CoverageFunction.
     */
    private GridFunction buildGridFunction(WcpsCoverageMetadata wcpsCoverageMetadata) {
        SequenceRule sequenceRule = this.buildSequenceRuleForGridFunction(wcpsCoverageMetadata);
        
        List<Axis> axes = wcpsCoverageMetadata.getAxes();
        String startPoint = "";
        for (Axis axis : axes) {
            startPoint += axis.getGridBounds().getLowerLimit().toBigInteger() + " ";
        }
        
        GridFunction gridFunction = new GridFunction(sequenceRule, startPoint.trim());
        return gridFunction;
    }
    

    /**
     * Build CoverageFunction for GMLCore.
     */
    public CoverageFunction buildCoverageFunction(WcpsCoverageMetadata wcpsCoverageMetadata) {
        GridFunction gridFunction = this.buildGridFunction(wcpsCoverageMetadata);
        
        CoverageFunction coverageFunction = new CoverageFunction(gridFunction);
        return coverageFunction;
    }
    
}
