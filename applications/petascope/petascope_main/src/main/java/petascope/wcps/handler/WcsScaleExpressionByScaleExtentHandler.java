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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.core.Pair;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.util.CrsUtil;
import petascope.wcps.exception.processing.InvalidScaleExtentException;
import petascope.wcps.metadata.model.Axis;
import petascope.wcps.metadata.model.NumericSubset;
import petascope.wcps.metadata.model.NumericTrimming;
import petascope.wcps.metadata.model.Subset;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;
import petascope.wcps.metadata.service.RasqlTranslationService;
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
 * e.g: scale_extent(c, [i(25:50), j(25:50)]) then number of grid points for i
 * is 26 and j is 26 in the output
 *
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
@Service
public class WcsScaleExpressionByScaleExtentHandler extends AbstractWcsScaleHandler {

    @Autowired
    private ScaleExpressionByDimensionIntervalsHandler scaleExpressionByDimensionIntervalsHandler;

    public WcpsResult handle(WcpsResult coverageExpression, WcpsScaleDimensionIntevalList scaleAxesDimensionList) throws PetascopeException {

        WcpsCoverageMetadata metadata = coverageExpression.getMetadata();
        List<WcpsSubsetDimension> wcpsSubsetDimensions = new ArrayList<>();
        
        for (AbstractWcpsScaleDimension dimension : scaleAxesDimensionList.getIntervals()) {
            Axis axis = metadata.getAxisByName(dimension.getAxisName());
            // NOTE: scaleextent must be low:hi
            if (dimension instanceof WcpsSliceScaleDimension) {
                throw new InvalidScaleExtentException(axis.getLabel(), ((WcpsSliceScaleDimension) dimension).getBound());
            }
            
            WcpsTrimScaleDimension trimScaleDimension = ((WcpsTrimScaleDimension)dimension);
            WcpsTrimSubsetDimension trimSubsetDimension = new WcpsTrimSubsetDimension(axis.getLabel(), axis.getNativeCrsUri(),
                                                                                    trimScaleDimension.getLowerBound(), trimScaleDimension.getUpperBound());
            
            wcpsSubsetDimensions.add(trimSubsetDimension);
        }

        
        DimensionIntervalList dimensionIntervalList = new DimensionIntervalList(wcpsSubsetDimensions);
        WcpsResult wcpsResult = this.scaleExpressionByDimensionIntervalsHandler.handle(coverageExpression, dimensionIntervalList, false);
        
        return wcpsResult;
    }
}
