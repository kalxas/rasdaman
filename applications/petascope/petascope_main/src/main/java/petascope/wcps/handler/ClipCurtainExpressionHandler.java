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
package petascope.wcps.handler;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import petascope.core.Pair;
import petascope.exceptions.PetascopeException;
import petascope.wcps.metadata.model.Axis;
import petascope.wcps.metadata.model.NumericTrimming;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;
import petascope.wcps.result.WcpsResult;
import petascope.wcps.subset_axis.model.AbstractWKTShape;

/**
 *
 * Handler for the clip curtain expression to crop a coverage by a curtain with a WKT (polygon,
 * linestring,...) with coverage expression (c) must be 3D+.
 * e.g: clip( c, curtain( projection(Lat, Lon), Polygon((0 20, 20 20, 20 10, 0 20)) ),
 *            "http://opengis.net/def/CRS/EPSG/0/4326")
 *
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
@Service
public class ClipCurtainExpressionHandler extends AbstractClipExpressionHandler {
    
    private static final String TRANSLATED_CURTAIN_PROJECTION_AXIS_LABEL1 = "$TRANSLATED_CURTAIN_PROJECTION_AXIS_LABEL1";
    private static final String TRANSLATED_CURTAIN_PROJECTION_AXIS_LABEL2 = "$TRANSLATED_CURTAIN_PROJECTION_AXIS_LABEL2";
    // e.g: clip( curtain(c, projection(0, 1), POLYGON((...)) ) )
    private final String RASQL_TEMPLATE = this.OPERATOR 
                                    + "( "
                                        + this.TRANSLATED_COVERAGE_EXPRESSION_RASQL_TEMPLATE + ", "
                                    +   " curtain( "                                          
                                          + "projection("
                                              + TRANSLATED_CURTAIN_PROJECTION_AXIS_LABEL1 + ", "  
                                              + TRANSLATED_CURTAIN_PROJECTION_AXIS_LABEL2 + "), "
                                          +  this.TRANSLATED_WKT_EXPRESSION_RASQL_TEMPLATE
                                    + " ) )";

    /**
     * Handle the clip operator from current collection and input WKT to be
     * applied on it.
     *
     * @param coverageExpression a coverage expression valid to clip
     * @param curtainProjectionAxisLabel1 first axis parameter in curtain's projection()
     * @param curtainProjectionAxisLabel2 second axis parameter curtain's projection()
     * @param wktShape an abstract class for all parsable WKT
     * @param crs input CRS of wktShape (polygon, multipolygon)
     * @return WcpsResult an object to be used in upper parsing tree.
     */
    public WcpsResult handle(WcpsResult coverageExpression, String curtainProjectionAxisLabel1, String curtainProjectionAxisLabel2,
                             AbstractWKTShape wktShape, String crs) throws PetascopeException {
        
        WcpsCoverageMetadata metadata = coverageExpression.getMetadata();        
        // NOTE: translate geo coordinates attending in WKT for these 2 axes only.
        List<String> axisNames = new ArrayList<>();
        axisNames.add(curtainProjectionAxisLabel1);
        axisNames.add(curtainProjectionAxisLabel2);
        WcpsResult result = this.mainHandle(coverageExpression, axisNames, wktShape, crs, RASQL_TEMPLATE);
        
        String rasqlTmp = result.getRasql();
        Integer gridOrderAxis1 = result.getMetadata().getAxisGridOrder(curtainProjectionAxisLabel1);
        Integer gridOrderAxis2 = result.getMetadata().getAxisGridOrder(curtainProjectionAxisLabel2);
        
        String rasql = rasqlTmp.replace(TRANSLATED_CURTAIN_PROJECTION_AXIS_LABEL1, gridOrderAxis1.toString())
                               .replace(TRANSLATED_CURTAIN_PROJECTION_AXIS_LABEL2, gridOrderAxis2.toString());
        result.setRasql(rasql);
        
        // NOTE: as this curtain operator is a complex query so we must use rasql to get the grid domains for 3d+ axes.
        List<Pair<String, String>> gridDomains = this.getSdomOfClippedOutput(rasql);
        for (int i = 0; i < metadata.getSortedAxesByGridOrder().size(); i++) {
            Axis axis = coverageExpression.getMetadata().getSortedAxesByGridOrder().get(i);
            BigDecimal lowerGridBound = new BigDecimal(gridDomains.get(i).fst);
            BigDecimal upperGridBound = new BigDecimal(gridDomains.get(i).snd);
            NumericTrimming numericTrimming = new NumericTrimming(lowerGridBound, upperGridBound);
            axis.setGridBounds(numericTrimming);
        }
        
        // However, the geo domains just be the reduced ones from original coverage's expression if axes joint in curtain's projection() expression.
        this.updateOuputCoverageGeoAxesDomains(metadata);
        
        return result;
    }
}
