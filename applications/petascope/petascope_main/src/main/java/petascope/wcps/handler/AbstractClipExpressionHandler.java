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
import petascope.core.Pair;
import petascope.exceptions.PetascopeException;
import petascope.util.BigDecimalUtil;
import petascope.util.CrsUtil;
import petascope.util.ListUtil;
import static petascope.util.ras.RasConstants.RASQL_BOUND_SEPARATION;
import static petascope.util.ras.RasConstants.RASQL_CLOSE_SUBSETS;
import static petascope.util.ras.RasConstants.RASQL_OPEN_SUBSETS;
import petascope.util.ras.RasUtil;
import petascope.wcps.exception.processing.InvalidCoordinatesForClippingException;
import petascope.wcps.exception.processing.WcpsRasqlException;
import petascope.wcps.metadata.model.Axis;
import petascope.wcps.metadata.model.NumericSlicing;
import petascope.wcps.metadata.model.NumericSubset;
import petascope.wcps.metadata.model.NumericTrimming;
import petascope.wcps.metadata.model.ParsedSubset;
import petascope.wcps.metadata.model.Subset;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;
import petascope.wcps.metadata.service.CoverageAliasRegistry;
import petascope.wcps.metadata.service.SubsetParsingService;
import petascope.wcps.metadata.service.WcpsCoverageMetadataGeneralService;
import petascope.wcps.result.WcpsResult;
import petascope.wcps.subset_axis.model.AbstractWKTShape;
import petascope.wcps.subset_axis.model.WKTCompoundPoint;
import petascope.wcps.subset_axis.model.WKTCompoundPoints;
import petascope.wcps.subset_axis.model.WcpsSliceSubsetDimension;
import petascope.wcps.subset_axis.model.WcpsSubsetDimension;

/**
 * Abstract class for all clip() epxression handlers.
 * 
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
public abstract class AbstractClipExpressionHandler extends AbstractOperatorHandler {
    
    @Autowired
    protected WcpsCoverageMetadataGeneralService wcpsCoverageMetadataGeneralService;
    @Autowired
    protected SubsetParsingService subsetParsingService;
    @Autowired
    protected CoverageAliasRegistry coverageAliasRegistry;

    // Store the calculated bounding box of clipped output from a coverage and a WKT shape
    protected final Map<String, Pair<BigDecimal, BigDecimal>> clippedCoverageAxesGeoBounds = new HashMap<>();
    
    public static final String OPERATOR = "clip";
    
    protected static final String TRANSLATED_COVERAGE_EXPRESSION_RASQL_TEMPLATE = "$TRANSLATED_COVERAGE_EXPRESSION_RASQL";
    protected static final String TRANSLATED_WKT_EXPRESSION_RASQL_TEMPLATE = "$TRANSLATED_WKT_EXPRESSION_RASQL";
    
     /**
     * Convert a geoCoordinate for an axis to a numeric BigDecimal value
     * (especially for DateTime coordinate).
     *
     * @param axis axis containing input coordinate
     * @param geoCoordinate coordinate in string
     * @return BigDecimal numeric geo coordinate
     */
    protected BigDecimal getNumericGeoCoordinate(WcpsCoverageMetadata metadata, Axis axis, String geoCoordinate) throws PetascopeException {
        WcpsSubsetDimension wcpsSubsetDimension = new WcpsSliceSubsetDimension(axis.getLabel(), axis.getNativeCrsUri(), geoCoordinate);
        List<WcpsSubsetDimension> wcpsSubsetDimensions = new ArrayList<>();
        wcpsSubsetDimensions.add(wcpsSubsetDimension);
        List<Subset> subsets = this.subsetParsingService.convertToNumericSubsets(wcpsSubsetDimensions, metadata.getAxes());
        // this can never throw an exception, because the list is not empty
        BigDecimal numericGeoCoordinate = subsets.get(0).getNumericSubset().getLowerLimit();

        return numericGeoCoordinate;
    }

    /**
     * Translate a geoPointCoordinate of an axis to grid pixel coordinate.
     *
     * @return a grid coordinate
     * @throws PetascopeException
     */
    protected String translateGeoToGridPointCoordinate(Axis axis, BigDecimal geoPointCoordinate) throws PetascopeException {
        // add/substract epsilon as the coordinate from crs transform can be approximately
        if (geoPointCoordinate.add(BigDecimalUtil.COEFFICIENT_DECIMAL_EPSILON).compareTo(axis.getGeoBounds().getLowerLimit()) < 0 ||
            geoPointCoordinate.subtract(BigDecimalUtil.COEFFICIENT_DECIMAL_EPSILON).compareTo(axis.getGeoBounds().getUpperLimit()) > 0) {
            String errorMessage = "Coordinate is not within domain ["
                    + axis.getGeoBounds().getLowerLimit().toPlainString() + RASQL_BOUND_SEPARATION + axis.getGeoBounds().getUpperLimit().toPlainString() + "] of axis '" + axis.getLabel() + "'.";
            throw new InvalidCoordinatesForClippingException(geoPointCoordinate, errorMessage);
        }

        ParsedSubset<BigDecimal> parsedSubset = new ParsedSubset<>(geoPointCoordinate, geoPointCoordinate);
        // Translate geo coordinate of a slice point to a grid point
        ParsedSubset<Long> gridSubset = wcpsCoverageMetadataGeneralService.translateGeoToGridCoordinates(parsedSubset, axis,
                axis.getGeoBounds().getLowerLimit(),
                axis.getGeoBounds().getUpperLimit(),
                axis.getGridBounds().getLowerLimit(),
                axis.getGridBounds().getUpperLimit());
        String gridPoint = gridSubset.getLowerLimit().toString();

        return gridPoint;
    }

    /**
     * The clipped output coverage needs to have new geo bounds after it is
     * clipped by WKT shape.
     *
     * @param index the axis index to update bound
     * @param bound the numeric geo value to be consider to be update as min/max
     * of current axis's geo domain.
     */
    private void updateGeoBoundsClippedOutput(int totalAxes, String axisName, BigDecimal bound) {
        if (clippedCoverageAxesGeoBounds.size() < totalAxes) {
            clippedCoverageAxesGeoBounds.put(axisName, new Pair<>(bound, bound));
        } else {
            Pair<BigDecimal, BigDecimal> pair = clippedCoverageAxesGeoBounds.get(axisName);
            BigDecimal newLowerBound = pair.fst;
            BigDecimal newUpperBound = pair.snd;
            // lowerBound of axis
            if (newLowerBound.compareTo(bound) > 0) {
                newLowerBound = bound;
            } else if (newUpperBound.compareTo(bound) < 0) {
                newUpperBound = bound;
            }
            clippedCoverageAxesGeoBounds.put(axisName, new Pair<>(newLowerBound, newUpperBound));
        }
    }

    /**
     * Translate from the coordinates with geo-axes order (e.g: Time, Lat, Long
     * with values: "1950-01-01T20:30:30Z" 25.25 35.67) to grid coordinates with
     * grid-axes order (e.g: Long, Lat, Time with values: 30 40 0)
     *
     * @param metadata WcpsCoverageMetadata object
     * @param axisNames: list of axes attending to WKT coordinates
     * @param geoCoordinateArray Array of geo coordinates in geo-axes order
     * @param wktCRS optional parameter to transform XY coordinates in WKT from
     * input CRS to coverage's native XY axes CRS.
     * @return A string representing translated coordinates in grid-axes order
     */
    protected String translateGeoToGridCoorinates(WcpsCoverageMetadata metadata, List<String> axisNames, 
                                                  String[] geoCoordinateArray, String wktCRS) throws PetascopeException {
        // First, determine the XY axes from this coverage.
        Integer xOrder = -1;
        Integer yOrder = -1;
        if (metadata.hasXYAxes()) {
            Pair<Integer, Integer> xyOrder = metadata.getXYAxesOrder();
            xOrder = xyOrder.fst;
            yOrder = xyOrder.snd;
        }
        
        // Check if WKT contains XY axes from input specific axisNames
        boolean hasXYAxesInWKT = false;
        if (metadata.hasXYAxes()) {
            List<Axis> axes = metadata.getXYAxes();
            Axis axisX = axes.get(0);
            Axis axisY = axes.get(1);
            boolean hasXAxisInWKT = false;
            boolean hasYAxisInWKT = false;
            
            for (String axisName : axisNames) {
                if (axisName.equals(axisX.getLabel())) {
                    hasXAxisInWKT = true;
                } else if (axisName.equals(axisY.getLabel())) {
                    hasYAxisInWKT = true;
                }
                if (hasXAxisInWKT && hasYAxisInWKT) {
                    hasXYAxesInWKT = true;
                    break;
                }
                
            }
        }

        List<Pair<String, Integer>> translatedGridCoordinates = new ArrayList<>();
        // Then, translate non XY-axes geo coordinate as single coordinate
        for (int i = 0; i < axisNames.size(); i++) {
            String axisName = axisNames.get(i);
            
            if (!hasXYAxesInWKT || (i != xOrder && i != yOrder)) {
                // Non XY-axes, translate as 1 point in 1 axis normally                
                Axis axis = metadata.getAxisByName(axisName);
                String geoCoordinate = geoCoordinateArray[i];
                BigDecimal numericGeoCoordinate = this.getNumericGeoCoordinate(metadata, axis, geoCoordinate);
                String gridCoordinate = this.translateGeoToGridPointCoordinate(axis, numericGeoCoordinate);
                translatedGridCoordinates.add(new Pair<>(gridCoordinate, axis.getRasdamanOrder()));

                // update geo bound for the clipped output
                this.updateGeoBoundsClippedOutput(geoCoordinateArray.length, axisName, numericGeoCoordinate);
            } else {
                // XY axes, translate as a pair of geo coordinates in 2 axes (e.g: Lat, Long)
                Axis axisX = metadata.getAxes().get(xOrder);
                Axis axisY = metadata.getAxes().get(yOrder);
                String geoCoordinateX = geoCoordinateArray[xOrder];
                String geoCoordinateY = geoCoordinateArray[yOrder];
                BigDecimal numericGeoCoordinateX = this.getNumericGeoCoordinate(metadata, axisX, geoCoordinateX);
                BigDecimal numericGeoCoordinateY = this.getNumericGeoCoordinate(metadata, axisY, geoCoordinateY);
                // (e.g: WKT in EPSG:4326, native coverage XY axes is EPSG:3857, then XY coordinates in WKT are translated to EPSG:3857)
                if (wktCRS == null) {
                    wktCRS = axisX.getNativeCrsUri();
                }

                Subset subsetX = new Subset(new NumericSlicing(numericGeoCoordinateX), wktCRS, axisX.getLabel());
                Subset subsetY = new Subset(new NumericSlicing(numericGeoCoordinateY), wktCRS, axisY.getLabel());

                List<Subset> subsets = new ArrayList<>();
                subsets.add(subsetX);
                subsets.add(subsetY);
                // Transform from subsettingCRS of WKT to native coverage CRS if necessary
                this.wcpsCoverageMetadataGeneralService.transformSubsettingCrsXYSubsets(metadata, subsets);

                BigDecimal newGeoCoordinateX = subsets.get(0).getNumericSubset().getLowerLimit();
                BigDecimal newGeoCoordinateY = subsets.get(1).getNumericSubset().getLowerLimit();

                // Update the geo bounds for clipped output coverage
                if (xOrder < yOrder) {
                    // XY Axes order
                    
                    this.updateGeoBoundsClippedOutput(geoCoordinateArray.length, axisX.getLabel(), newGeoCoordinateX);
                    this.updateGeoBoundsClippedOutput(geoCoordinateArray.length, axisY.getLabel(), newGeoCoordinateY);
                } else {
                    // YX Axes order
                    this.updateGeoBoundsClippedOutput(geoCoordinateArray.length, axisY.getLabel(), newGeoCoordinateY);
                    this.updateGeoBoundsClippedOutput(geoCoordinateArray.length, axisX.getLabel(), newGeoCoordinateX);
                }

                // Now, translate coordinate for XY axes normally
                String gridCoordinateX = this.translateGeoToGridPointCoordinate(axisX, newGeoCoordinateX);
                String gridCoordinateY = this.translateGeoToGridPointCoordinate(axisY, newGeoCoordinateY);
                translatedGridCoordinates.add(new Pair<>(gridCoordinateX, axisX.getRasdamanOrder()));
                translatedGridCoordinates.add(new Pair<>(gridCoordinateY, axisY.getRasdamanOrder()));
                i++;
            }
        }
        // NOTE: input WKT with coverage's geo axes order (e.g: Lat, Long, time), output with rasdaman's grid axes order (time, Lat, Long).
        // e.g: with geo axes order is: 300 400 500, output in grid axes order is: 500 400 300.
        String output = this.gridCoordinatesToString(translatedGridCoordinates);

        return output;
    }

    /**
     * From a List of translated grid coordinates but in geo axes order, sort
     * this list by grid axes order and return a string // e.g: geo-axes order
     * is: Lat, Long, Time (0, 20, 30) // grid-axes oder is: Time, Lat, Long
     * (30, 0, 20)
     *
     * @return coordinates in grid-axes order
     */
    protected String gridCoordinatesToString(List<Pair<String, Integer>> translatedGridCoordinatesList) {
        for (int i = 0; i < translatedGridCoordinatesList.size(); i++) {
            for (int j = 0; j < translatedGridCoordinatesList.size(); j++) {
                Pair<String, Integer> ai = translatedGridCoordinatesList.get(i);
                Pair<String, Integer> aj = new Pair<>(translatedGridCoordinatesList.get(j).fst, translatedGridCoordinatesList.get(j).snd);
                if (ai.snd < aj.snd) {
                    Pair<String, Integer> temp = new Pair<>(translatedGridCoordinatesList.get(j).fst, translatedGridCoordinatesList.get(j).snd);
                    translatedGridCoordinatesList.set(j, ai);
                    translatedGridCoordinatesList.set(i, temp);
                }
            }
        }

        List<String> tmpList = new ArrayList<>();
        for (int i = 0; i < translatedGridCoordinatesList.size(); i++) {
            tmpList.add(translatedGridCoordinatesList.get(i).fst);
        }

        String result = ListUtil.join(tmpList, " ").trim();

        return result;
    }

    /**
     * Used only to get the sdom() of a clip query (e.g: oblique polygon,
     * linestring) which is impossible to determine the domains of the output.
     * 
     * @TODO: It is hard to get the sdom() of Linestring of oblique polygon in Petascope but 
     * if it can be then it is better than send a query to Rasql to get this value. Need more discussions about it.
     *
     * @return the domains (lowerBound, upperBound) for each grid axis of
     * clipped output.
     */
    protected List<Pair<String, String>> getSdomOfClippedOutput(String clipRasqlQuery) {
        // First, create a full Rasql query from the input clipping main part 
        // e.g: clip(c, POLYGON(( ... )) ) -> select sdom(clip(c, POLYGON(( ... )) ) from test_mean_summer_airtemp as c
        List<Pair<String, String>> pairs = new ArrayList<>();
        
        // all the possible coverages and alias comes from FOR clause of WCPS query
        String aliasRasdamanCollectionNames = this.coverageAliasRegistry.getRasqlFromClause();
        String rasqlQuery = "Select sdom(" + clipRasqlQuery + ") FROM " + aliasRasdamanCollectionNames;        
        try {
            // e.g: [0:20,0:50]
            String sdom = new String(RasUtil.getRasqlResultAsBytes(rasqlQuery));
            sdom = sdom.replace(RASQL_OPEN_SUBSETS, "").replace(RASQL_CLOSE_SUBSETS, "");
            String[] tmpArray = sdom.split(",");
            for (String tmp : tmpArray) {
                String[] domain = tmp.split(RASQL_BOUND_SEPARATION);
                Pair<String, String> pair = new Pair<>(domain[0], domain[1]);
                pairs.add(pair);
            }
        } catch (PetascopeException ex) {
            throw new WcpsRasqlException(rasqlQuery, ex.getExceptionText(), ex);
        }
        return pairs;
    }
    
    /**
     * After clipping, the axes' domains are reduced not as before, so it needs to update them correspondingly.
     */
    protected void updateOuputCoverageGeoAxesDomains(WcpsCoverageMetadata metadata) throws PetascopeException {
        // e.g: clip 2D polygon on a 2D coverage, 
        // it needs to update the bounding box of output coverage based on the bounding box of clipping polygon.
        List<Subset> subsets = new ArrayList<>();
        
        // NOTE: in case of clipping with curtain, only 2 axes from 3D+ coverage will attend to WKT's vertices' coordinates
        // not like plain clipping with WKT when a vertex's coordinate requires all axes of coverage's expression to compound.
        for (String axisName : this.clippedCoverageAxesGeoBounds.keySet()) {            
            BigDecimal minBound = this.clippedCoverageAxesGeoBounds.get(axisName).fst;
            BigDecimal maxBound = this.clippedCoverageAxesGeoBounds.get(axisName).snd;
            NumericSubset numericSubset = new NumericTrimming(minBound, maxBound);
            Axis axis = metadata.getAxisByName(axisName);

            Subset subset = new Subset(numericSubset, axis.getNativeCrsUri(), axis.getLabel());
            subsets.add(subset);
        }
        // Update clipped coverage expression with the new subsets from WKT shape
        // e.g: original coverage has axis with geo bounds: Lat(0, 20), Long(0, 30) and WKT polygon has a bounding box is Lat(0:5), Long(20:25)
        // then output is a coverage with bounding box in geo bounds: Lat(0:5), Long(20:25)
        this.wcpsCoverageMetadataGeneralService.applySubsets(true, metadata, subsets);
    }
    
    /**
     * Handle the clip operator and input WKT to be
     * applied on current coverage expression and generate Rasql result for it.
     */
    protected WcpsResult mainHandle(WcpsResult coverageExpression, List<String> axisNames, 
                                    AbstractWKTShape wktShape, String wktCRS, String rasqlTemplate) throws PetascopeException {
        checkOperandIsCoverage(coverageExpression, OPERATOR);
        
        // Clear stored data from last request
        this.clippedCoverageAxesGeoBounds.clear();

        WcpsCoverageMetadata metadata = coverageExpression.getMetadata();
        // Translate geo coordinates to grid coordinates for XY axes in pair and other axis individually
        List<String> finalTranslatedWKTCompoundPointsList = new ArrayList<>();

        List<WKTCompoundPoints> wktCompoundPointsList = wktShape.getWktCompoundPointsList();
        for (int i = 0; i < wktCompoundPointsList.size(); i++) {
            WKTCompoundPoints wktCompoundPoints = wktCompoundPointsList.get(i);
            List<String> translatedWKTCompoudPointsList = new ArrayList<>();
            
            for (int j = 0; j < wktCompoundPoints.getWKTCompoundPoints().size(); j++) {
                // e.g: 20.5 30.5 "2008-01-01T02:01:20.000Z",40.5 50.5 "2008-01-03T23:59:55.000Z"
                WKTCompoundPoint wktCompoundPoint = wktCompoundPoints.getWKTCompoundPoints().get(j);
                // e.g: geoPoints[0] = 20.5 30.5 "2008-01-01T02:01:20.000Z", geoPoints[1] = 40.5 50.5 "2008-01-03T23:59:55.000Z"
                String[] geoPoints = wktCompoundPoint.getPoint().split(",");
                List<String> translatedGeoPointsList = new ArrayList<>();

                String previousTranslatedGridPointCoordinates = "";
                // each pointCoordinate belongs to one coverage's axis
                for (int k = 0; k < geoPoints.length; k++) {
                    // e.g: geoPointCoordinates = [20, 30, "2008-01-01T02:01:20.000Z"]
                    String[] geoPointCoordinates = geoPoints[k].split(" ");
                    String translatedGridPointCoordinates;
                    if (wktCRS != null && wktCRS.equals(CrsUtil.GRID_CRS)) {
                        // e.g: clip(c, POLYGON((...)), "CRS:1"), then no need to translate any coordinates in WKT
                        translatedGridPointCoordinates = geoPoints[k];
                    } else {
                        // e.g: clip(c, POLYGON((...)) ) or clip(c, POLYGON((...)), "http://opengis.net/def/crs/EPSG/0/3857")
                        // then need to translate geo coordinates in WKT to grid coordinates accordingly
                        // This is the tranlsated grid coordinates in grid-axes order to query in rasql
                        translatedGridPointCoordinates = this.translateGeoToGridCoorinates(metadata, axisNames, geoPointCoordinates, wktCRS);
                    }
                    if (!previousTranslatedGridPointCoordinates.equals(translatedGridPointCoordinates)) {
                        // NOTE: don't add the duplicate grid coordinates to Rasql as they are redundant and cause significant slow in rasserver
                        // e.g: 20 30, 20 30, 20 30, 30 40 then, only add 20 30, 30 40 as grid coordinates
                        translatedGeoPointsList.add(translatedGridPointCoordinates);
                    }
                    previousTranslatedGridPointCoordinates = translatedGridPointCoordinates;
                }
                // e.g: 20 30 40,30 40 50,60 70 80
                String translatedGeoPoints = ListUtil.join(translatedGeoPointsList, ",");
                translatedWKTCompoudPointsList.add("(" + translatedGeoPoints + ")");
            }
            String tmp = ListUtil.join(translatedWKTCompoudPointsList, ",");
            finalTranslatedWKTCompoundPointsList.add("(" + tmp + ")");
        }

        // e.g: (20 30 40,30 40 50,60 70 80),(20 50 60,70 80 90,50 60 70)
        String translatedCoordinates = ListUtil.join(finalTranslatedWKTCompoundPointsList, ",");
        String translatedWKT = wktShape.getStringRepresentation(translatedCoordinates);

        // e.g: clip( c, POLYGON((30 50, 60 70, 80 90),(120 150, 160 170, 190 220)) )
        String rasql = rasqlTemplate.replace(TRANSLATED_COVERAGE_EXPRESSION_RASQL_TEMPLATE, coverageExpression.getRasql())
                                    .replace(TRANSLATED_WKT_EXPRESSION_RASQL_TEMPLATE, translatedWKT);

        // NOTE: it just finished the main task, subclasses needs to finish the rests for different clipping types.
        WcpsResult result = new WcpsResult(metadata, rasql);

        return result;
    }
}
