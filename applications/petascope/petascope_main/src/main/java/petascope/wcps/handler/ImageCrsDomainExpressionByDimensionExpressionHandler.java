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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import petascope.exceptions.PetascopeException;
import petascope.util.CrsUtil;
import static petascope.wcps.handler.AbstractOperatorHandler.checkOperandIsCoverage;
import petascope.wcps.metadata.model.Axis;
import petascope.wcps.metadata.model.NumericSlicing;
import petascope.wcps.metadata.model.NumericTrimming;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;
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
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ImageCrsDomainExpressionByDimensionExpressionHandler extends Handler {
    
    public static final String OPERATOR = "imageCrsDomain";
    
    
    public ImageCrsDomainExpressionByDimensionExpressionHandler() {
        
    }
    
    public ImageCrsDomainExpressionByDimensionExpressionHandler create(Handler coverageExpressionHandler, StringScalarHandler axisNameHandler) {
        ImageCrsDomainExpressionByDimensionExpressionHandler result = new ImageCrsDomainExpressionByDimensionExpressionHandler();
        result.setChildren(Arrays.asList(coverageExpressionHandler, axisNameHandler));
        
        return result;
    }
    
    public WcpsMetadataResult handle() throws PetascopeException {
        WcpsResult coverageExpression = (WcpsResult) this.getFirstChild().handle();
        String axisName = ((WcpsResult)this.getSecondChild().handle()).getRasql();
        
        WcpsMetadataResult result = this.handle(coverageExpression, axisName);
        return result;
    }

    private WcpsMetadataResult handle(WcpsResult coverageExpression, String axisName) throws PetascopeException {
        
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
        
        String coverageId = coverageExpression.getMetadata().getCoverageName();
        WcpsCoverageMetadata tmpMetadata = this.generateWcpsMetadataWithOneGridAxis(coverageId, axis);
        
        WcpsMetadataResult metadataResult = new WcpsMetadataResult(tmpMetadata, rasql);
        return metadataResult;
    }
    
        
    /**
     * Given one input axis, create a WCPS metadata object with one axis as grid domains and CRS:1 CRS
     * used for imageCrsdomain() handler result
     * NOTE: this is used to determine in the case of axis iterator in condenser over $pt t (imageCrsdomain(c[time("2015":"2015")], t))
     */
    private WcpsCoverageMetadata generateWcpsMetadataWithOneGridAxis(String coverageId, Axis axis) throws PetascopeException {
        List<Axis> axesTmp = new ArrayList<>();
        axesTmp.add(axis);
        
        String crs = CrsUtil.GRID_CRS;
        if (!axis.getNativeCrsUri().equals(CrsUtil.GRID_CRS)) {
            crs = axis.getNativeCrsUri();
        }
        
        // NOTE: this is used to determine in the case of axis iterator in condenser over $pt t (imageCrsdomain(c[time("2015":"2015")], t))
        WcpsCoverageMetadata tmpMetadata = new WcpsCoverageMetadata(coverageId, null, null, axesTmp, crs, null, null, null, null);
        return tmpMetadata;
    }

    private final String TRIMMING_TEMPLATE = "$lowBound:$highBound";
    private final String SLICING_TEMPLATE = "$lowBound";
}
