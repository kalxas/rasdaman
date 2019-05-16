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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.core.AxisTypes;
import petascope.core.Pair;
import petascope.exceptions.PetascopeException;
import petascope.wcps.metadata.model.Axis;
import petascope.wcps.metadata.model.NumericTrimming;
import petascope.wcps.metadata.model.RegularAxis;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;
import petascope.wcps.metadata.service.WcpsCoverageMetadataGeneralService;
import petascope.wcps.result.WcpsResult;
import petascope.wcps.subset_axis.model.AbstractWKTShape;

/**
 *
 * Handler for the clip corridor expression to crop a coverage by a LineString (trackline) with a WKT (polygon,
 * linestring,...) with coverage expression (c) must be 3D+.
 * e.g: clip( c, corridor( projection(Lat, Lon), LineString("1950-01-01" 1 1, "1950-01-02" 5 5),
 *                         Polygon((0 20, 20 20, 20 10, 0 20)), discrete ), "http://opengis.net/def/CRS/EPSG/0/4326")
 *
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
@Service
public class ClipCorridorExpressionHandler extends AbstractClipExpressionHandler {
    
    @Autowired
    WcpsCoverageMetadataGeneralService wcpsCoverageMetadataGeneralService;
    
    private static final String TRANSLATED_CURTAIN_PROJECTION_AXIS_LABEL1 = "$TRANSLATED_CURTAIN_PROJECTION_AXIS_LABEL1";
    private static final String TRANSLATED_CURTAIN_PROJECTION_AXIS_LABEL2 = "$TRANSLATED_CURTAIN_PROJECTION_AXIS_LABEL2";
    private static final String TRANSLATED_TRACKLINE_EXPRESSION_RASQL_TEMPLATE = "$TRANSLATED_TRACKLINE_EXPRESSION_RASQL";
    private static final String DISCRETE_TEMPLATE = "$DISCRETE";
    private static final String DISCRETE = "discrete";
            
    // e.g: clip( curtain(c, projection(0, 1), POLYGON((...)) ) )
    private final String RASQL_TEMPLATE = this.OPERATOR 
                                    + "( "
                                        + this.TRANSLATED_COVERAGE_EXPRESSION_RASQL_TEMPLATE + ", "
                                    +   " corridor( "                                          
                                          + "projection("
                                              + TRANSLATED_CURTAIN_PROJECTION_AXIS_LABEL1 + ", "  
                                              + TRANSLATED_CURTAIN_PROJECTION_AXIS_LABEL2 + "), "
                                          + TRANSLATED_TRACKLINE_EXPRESSION_RASQL_TEMPLATE + ", "
                                          + this.TRANSLATED_WKT_EXPRESSION_RASQL_TEMPLATE
                                          + DISCRETE_TEMPLATE  
                                    + " ) )";

    /**
     * Handle the clip operator from current collection and input WKT to be
     * applied on it.
     *
     * @param coverageExpression a coverage expression valid to clip
     * @param corridorProjectionAxisLabel1 first axis parameter in curtain's projection()
     * @param corridorProjectionAxisLabel2 second axis parameter curtain's projection()
     * @param wktShape an abstract class for all parsable WKT
     * @param crs input CRS of wktShape (polygon, multipolygon)
     * @return WcpsResult an object to be used in upper parsing tree.
     */
    public WcpsResult handle(WcpsResult coverageExpression, String corridorProjectionAxisLabel1, String corridorProjectionAxisLabel2,
                             AbstractWKTShape trackLineShape,
                             AbstractWKTShape wktShape, boolean discrete, String crs) throws PetascopeException {
        
        WcpsCoverageMetadata metadata = coverageExpression.getMetadata();
        // Store the calculated bounding box of clipped output from a coverage and a WKT shape
        Map<String, Pair<BigDecimal, BigDecimal>> clippedCoverageAxesGeoBounds = new HashMap<>();
        
        // First, translate the LineString (trackline) to grid coordinates
        String trackLineRasqlTemplate = this.TRANSLATED_WKT_EXPRESSION_RASQL_TEMPLATE;
        List<String> trackLineAxisNames = new ArrayList<>();
        for (Axis axis : coverageExpression.getMetadata().getAxes()) {
            trackLineAxisNames.add(axis.getLabel());
        }
        WcpsResult trackLineResult = this.mainHandle(clippedCoverageAxesGeoBounds, coverageExpression, trackLineAxisNames, trackLineShape, crs, trackLineRasqlTemplate);
        String trackLineRasql = trackLineResult.getRasql();

        // Then, translate the WKT (polygon, linestring) to grid coordinates based on specifed axes in projection()
        // NOTE: translate geo coordinates attending in WKT for these 2 axes only.
        List<String> projecttionAxisNames = new ArrayList<>();
        projecttionAxisNames.add(corridorProjectionAxisLabel1);
        projecttionAxisNames.add(corridorProjectionAxisLabel2);
        WcpsResult result = this.mainHandle(clippedCoverageAxesGeoBounds, coverageExpression, projecttionAxisNames, wktShape, crs, RASQL_TEMPLATE);
        
        String rasqlTmp = result.getRasql();
        Integer gridOrderAxis1 = result.getMetadata().getAxisGridOrder(corridorProjectionAxisLabel1);
        Integer gridOrderAxis2 = result.getMetadata().getAxisGridOrder(corridorProjectionAxisLabel2);
        
        String rasql = rasqlTmp.replace(TRANSLATED_CURTAIN_PROJECTION_AXIS_LABEL1, gridOrderAxis1.toString())
                               .replace(TRANSLATED_CURTAIN_PROJECTION_AXIS_LABEL2, gridOrderAxis2.toString())
                               .replace(TRANSLATED_TRACKLINE_EXPRESSION_RASQL_TEMPLATE, trackLineRasql);
        if (discrete) {
            // clipping corridor with discrete parameter (i.e: only select points in trackline for the clipped output)
            rasql = rasql.replace(DISCRETE_TEMPLATE, ", " + DISCRETE);
        } else {
            rasql = rasql.replace(DISCRETE_TEMPLATE, "");
        }
        result.setRasql(rasql);
        
        // The geo domains just be the reduced ones from original coverage's expression if axes joint in corridor's projection() expression.
        this.updateOuputCoverageGeoAxesDomains(clippedCoverageAxesGeoBounds, metadata);
        
        // NOTE: as this corridor operator is a complex query so we must use rasql to get the grid domains for 3D output coverage.
        List<Pair<String, String>> gridDomains = this.getSdomOfClippedOutput(rasql);
        
        // The first axis is Index1D CRS and depend on discrete parameter is specified or not, it returns trackline's length or the number of points in trackline.
        BigDecimal lowerGridBoundTrackLineAxis = new BigDecimal(gridDomains.get(0).fst);
        BigDecimal upperGridBoundTrackLineAxis = new BigDecimal(gridDomains.get(0).snd);
        NumericTrimming numericTrimmingTrackLineAxis = new NumericTrimming(lowerGridBoundTrackLineAxis, upperGridBoundTrackLineAxis);
        Axis trackLineAxis = wcpsCoverageMetadataGeneralService.createAxisByGridBounds(0, numericTrimmingTrackLineAxis);
        
        Axis projectionAxis1 = coverageExpression.getMetadata().getAxisByName(corridorProjectionAxisLabel1);
        BigDecimal lowerGridBoundAxis1 = new BigDecimal(gridDomains.get(1).fst);
        BigDecimal upperGridBoundAxis1 = new BigDecimal(gridDomains.get(1).snd);
        NumericTrimming numericTrimmingAxis1 = new NumericTrimming(lowerGridBoundAxis1, upperGridBoundAxis1);
        projectionAxis1.setGridBounds(numericTrimmingAxis1);
        
        Axis projectionAxis2 = coverageExpression.getMetadata().getAxisByName(corridorProjectionAxisLabel2);
        BigDecimal lowerGridBoundAxis2 = new BigDecimal(gridDomains.get(2).fst);
        BigDecimal upperGridBoundAxis2 = new BigDecimal(gridDomains.get(2).snd);
        NumericTrimming numericTrimmingAxis2 = new NumericTrimming(lowerGridBoundAxis2, upperGridBoundAxis2);
        projectionAxis2.setGridBounds(numericTrimmingAxis2);
        
        // After clipping by corridor, output coverage is always in 3D, regardless it was 3D+ before.
        List<Axis> axes = new ArrayList<>();
        axes.add(trackLineAxis);
        axes.add(projectionAxis1);
        axes.add(projectionAxis2);
        
        metadata.setAxes(axes);
        
        // Update coverag's native CRS after subsetting (e.g: 3D -> 2D, then CRS=compound?time&4326 -> 4326)
        metadata.updateCrsUri();

        return result;
    }
}
