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

import java.util.Arrays;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import petascope.exceptions.PetascopeException;
import petascope.wcps.exception.processing.RangeFieldNotFound;
import static petascope.wcps.handler.AbstractOperatorHandler.checkOperandIsCoverage;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;
import petascope.wcps.metadata.service.WcpsCoverageMetadataGeneralService;
import petascope.wcps.result.WcpsResult;

/**
 * Translation node from wcps to rasql for range subsetting. Example:  <code>
 * $c1.red
 * </code> translates to  <code>
 * c1.red
 * </code> select encode(scale( ((c[*:*,*:*,0:0]).0) [*:*,*:*,0], [0:2,0:1] ),
 * "csv") from irr_cube_2 AS c SELECT encode(SCALE( ((c[*:*,*:*,0:0]).0)
 * [*:*,*:*,0:0], [0:2,0:1]), "csv" ) FROM irr_cube_2 AS c
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
@Service
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class RangeSubsettingHandler extends Handler {

    @Autowired
    private WcpsCoverageMetadataGeneralService wcpsCoverageMetadataService;
    
    public static final String OPERATOR = "range subset";
    
    public RangeSubsettingHandler() {
        
    }
    
    public RangeSubsettingHandler create(Handler coverageExpressionHandler, StringScalarHandler fieldNameHandler) {
        RangeSubsettingHandler result = new RangeSubsettingHandler();
        result.wcpsCoverageMetadataService = wcpsCoverageMetadataService;
        result.setChildren(Arrays.asList(coverageExpressionHandler, fieldNameHandler));
        
        return result;
    }
    
    public WcpsResult handle() throws PetascopeException {
        WcpsResult coverageExpression = (WcpsResult)this.getFirstChild().handle();
        String fieldName = ((WcpsResult)this.getSecondChild().handle()).getRasql();
        
        WcpsResult result = this.handle(fieldName, coverageExpression);
        return result;
    }

    private WcpsResult handle(String fieldName, WcpsResult coverageExpression) {
        
        checkOperandIsCoverage(coverageExpression, OPERATOR); 

        WcpsCoverageMetadata metadata = coverageExpression.getMetadata();

        String rangeField = fieldName.trim();
        if (!NumberUtils.isNumber(rangeField)) {
            if (!wcpsCoverageMetadataService.checkIfRangeFieldExists(metadata, rangeField)) {
                throw new RangeFieldNotFound(rangeField);
            }
        } else {
            int intRangeField;
            try {
                intRangeField = Integer.parseInt(rangeField);
            } catch (NumberFormatException ex) {
                //only ints supported for range subsetting
                throw new RangeFieldNotFound(rangeField);
            }
            if (!wcpsCoverageMetadataService.checkRangeFieldNumber(coverageExpression.getMetadata(), intRangeField)) {
                throw new RangeFieldNotFound(rangeField);
            }
        }

        // use rangeIndex instead of rangeName
        int rangeFieldIndex = wcpsCoverageMetadataService.getRangeFieldIndex(metadata, rangeField);

        String coverageExprStr = coverageExpression.getRasql().trim();
        String rasql = TEMPLATE.replace("$coverageExp", coverageExprStr)
                .replace("$rangeFieldIndex", String.valueOf(rangeFieldIndex));

        wcpsCoverageMetadataService.removeUnusedRangeFields(metadata, rangeFieldIndex);

        // NOTE: we need to remove all the un-used range fields in coverageExpression's metadata
        // or it will add to netCDF extra metadata and have error in Rasql encoding.
        return new WcpsResult(coverageExpression.getMetadata(), rasql);
    }

    private final String TEMPLATE = "$coverageExp.$rangeFieldIndex";
}
