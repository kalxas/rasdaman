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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import petascope.exceptions.PetascopeException;
import static petascope.util.CrsUtil.GRID_CRS;
import petascope.wcps.exception.processing.InvalidScaleExtentException;
import petascope.wcps.metadata.model.Axis;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;
import petascope.wcps.result.WcpsResult;
import petascope.wcps.subset_axis.model.WcpsScaleDimensionIntevalList;
import petascope.wcps.subset_axis.model.AbstractWcpsScaleDimension;
import petascope.wcps.subset_axis.model.DimensionIntervalList;
import petascope.wcps.subset_axis.model.WcpsSliceScaleDimension;
import petascope.wcps.subset_axis.model.WcpsSubsetDimension;
import petascope.wcps.subset_axis.model.WcpsTrimScaleDimension;
import petascope.wcps.subset_axis.model.WcpsTrimSubsetDimension;

/**
 * Class to translate a scale wcps expression by scaleSize into rasql  <code>
 *    SCALE_EXTENT($coverageExpression, [$scaleDimensionInteverlList]) *
 * </code>
 *
 * e.g: scale_extent(c, [Lat(10:20), Lon(100:200)]) then number of grid points for i
 * is 26 and j is 26 in the output
 * The result to be correct with OGC CITE test is with grid domains:
 * Lat(0:10) and Long(0:100)
 *
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
@Service
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class WcsScaleExpressionByScaleExtentHandler extends AbstractWcsScaleHandler {

    @Autowired
    private ScaleExpressionByDimensionIntervalsHandler scaleExpressionByDimensionIntervalsHandler;
    
    public WcsScaleExpressionByScaleExtentHandler() {
        
    }
    
    public WcsScaleExpressionByScaleExtentHandler create(Handler coverageExpressionHandler, Handler scaleAxesDimensionListHandler) {
        WcsScaleExpressionByScaleExtentHandler result = new WcsScaleExpressionByScaleExtentHandler();
        result.setChildren(Arrays.asList(coverageExpressionHandler, scaleAxesDimensionListHandler));
        
        result.scaleExpressionByDimensionIntervalsHandler = this.scaleExpressionByDimensionIntervalsHandler;
        
        return result;
    }
    
    public WcpsResult handle() throws PetascopeException {
        WcpsResult coverageExpression = (WcpsResult)this.getFirstChild().handle();
        WcpsScaleDimensionIntevalList scaleAxesDimensionListHandler = (WcpsScaleDimensionIntevalList)this.getSecondChild().handle();
        
        WcpsResult result = this.handle(coverageExpression, scaleAxesDimensionListHandler);
        return result;
    }

    private WcpsResult handle(WcpsResult coverageExpression, WcpsScaleDimensionIntevalList scaleAxesDimensionList) throws PetascopeException {
        // SCALE_EXTENT LEFT_PARENTHESIS
        //        coverageExpression COMMA scaleDimensionIntervalList
        // RIGHT_PARENTHESIS
        // e.g: scaleextent(c[t(0)], [Lat(25:30), Long(25:30)]) with c is 3D coverage which means 2D output will have grid domain: 25:30, 25:30 (6 pixesl for each dimension)        

        WcpsCoverageMetadata metadata = coverageExpression.getMetadata();
        List<WcpsSubsetDimension> wcpsSubsetDimensions = new ArrayList<>();
        
        for (AbstractWcpsScaleDimension dimension : scaleAxesDimensionList.getIntervals()) {
            Axis axis = metadata.getAxisByName(dimension.getAxisName());
            // NOTE: scaleextent must be low:hi
            if (dimension instanceof WcpsSliceScaleDimension) {
                throw new InvalidScaleExtentException(axis.getLabel(), ((WcpsSliceScaleDimension) dimension).getBound());
            }
            
            WcpsTrimScaleDimension trimScaleDimension = ((WcpsTrimScaleDimension)dimension);
            // e.g scale_extent(Lat, [10:20]) -> output in grid domain of Lat is: [0:10]
            Long lowerBound = 0l;
            Long upperBound = Long.valueOf(trimScaleDimension.getUpperBound()) - Long.valueOf(trimScaleDimension.getLowerBound());
            WcpsTrimSubsetDimension trimSubsetDimension = new WcpsTrimSubsetDimension(axis.getLabel(), GRID_CRS,
                                                                                    lowerBound.toString(), upperBound.toString());
            
            wcpsSubsetDimensions.add(trimSubsetDimension);
        }

        
        DimensionIntervalList dimensionIntervalList = new DimensionIntervalList(wcpsSubsetDimensions);
        WcpsResult wcpsResult = this.scaleExpressionByDimensionIntervalsHandler.handle(coverageExpression, dimensionIntervalList, false);
        
        return wcpsResult;
    }
}
