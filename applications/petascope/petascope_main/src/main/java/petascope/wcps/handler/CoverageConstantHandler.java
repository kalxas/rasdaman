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

import org.apache.commons.lang3.StringUtils;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;
import petascope.wcps.result.WcpsResult;
import petascope.wcps.subset_axis.model.AxisIterator;
import petascope.wcps.subset_axis.model.WcpsSubsetDimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import petascope.exceptions.PetascopeException;
import petascope.wcps.metadata.model.Axis;
import petascope.wcps.metadata.model.Subset;
import petascope.wcps.metadata.service.RasqlTranslationService;
import petascope.wcps.metadata.service.SubsetParsingService;
import petascope.wcps.metadata.service.WcpsCoverageMetadataGeneralService;

/**
 * Translation node from wcps coverageConstant to rasql Example:  <code>
 * COVERAGE m
 * OVER x(0:1), y(2:4)
 * VALUES <1;2;3;4;5>
 * </code> translates to
 * <code>
 * <[0:1,2:4] 1, 2; 3,4,5>
 * </code>
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
@Service
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class CoverageConstantHandler extends Handler {

    @Autowired
    private WcpsCoverageMetadataGeneralService wcpsCoverageMetadataService;
    @Autowired
    private RasqlTranslationService rasqlTranslationService;
    @Autowired
    private SubsetParsingService subsetParsingService;
    
    public CoverageConstantHandler() {
        
    }
    
    public CoverageConstantHandler create(Handler coverageNameHandler, List<Handler> axisIteratorHandlers, Handler constantListHandler) {
        CoverageConstantHandler result = new CoverageConstantHandler();
        List<Handler> childHandlers = new ArrayList<>();
        childHandlers.add(coverageNameHandler);
        childHandlers.addAll(axisIteratorHandlers);
        childHandlers.add(constantListHandler);
        
        result.setChildren(childHandlers);
        
        result.wcpsCoverageMetadataService = this.wcpsCoverageMetadataService;
        result.rasqlTranslationService = this.rasqlTranslationService;
        result.subsetParsingService = this.subsetParsingService;
        
        return result;
    }
    
    @Override
    public WcpsResult handle() throws PetascopeException {
        String coverageName = ((WcpsResult)this.getFirstChild().handle()).getRasql();
        List<Handler> axisIteratorHandlers = this.getChildren().subList(1, this.getChildren().size() - 1);
        List<AxisIterator> axisIterators = new ArrayList<>();
        
        for (Handler handler : axisIteratorHandlers) {
            AxisIterator axisIterator = (AxisIterator)handler.handle();
            axisIterators.add(axisIterator);
        }
        
        Handler constantValuesListHandler = this.getChildren().get(this.getChildren().size() - 1);
        String rasql = ((WcpsResult)constantValuesListHandler.handle()).getRasql();
        List<String> constantValues = Arrays.asList(rasql.split(","));
        
        WcpsResult result = this.handle(coverageName, axisIterators, constantValues);
        return result;        
    }
    
    public static void updateAxisNamesFromAxisIterators(WcpsCoverageMetadata metadata, List<AxisIterator> axisIterators) {
        for (int i = 0; i < metadata.getAxes().size(); i++) {
            // e.g. in axis iterator is is called X from $px X(...)
            String axisName = axisIterators.get(i).getAxisName();
            Axis axis = metadata.getAxes().get(i);
            axis.setLabel(axisName);
        }        
    }

    private WcpsResult handle(String coverageName, List<AxisIterator> axisIterators,
            List<String> constantList) throws PetascopeException {

        List<WcpsSubsetDimension> subsetDimensions = new ArrayList();
        List<Axis> axes = new ArrayList<>();
        for (AxisIterator axisIterator : axisIterators) {
            subsetDimensions.add(axisIterator.getSubsetDimension());
            axes.add(axisIterator.getAxis());
        }
        String intervals = rasqlTranslationService.constructRasqlDomainFromSubsets(axes, subsetDimensions);
        ArrayList<String> constantsByDimension = new ArrayList<>();

        for (String constant : constantList) {
            constantsByDimension.add(constant);
        }
        String rasql = TEMPLATE.replace("$intervals", intervals).replace("$constants", StringUtils.join(constantsByDimension, ","));
        List<Subset> subsets = subsetParsingService.convertToRawNumericSubsets(subsetDimensions);
        WcpsCoverageMetadata metadata = wcpsCoverageMetadataService.createCoverage(coverageName, null, subsets, axes);
        
        updateAxisNamesFromAxisIterators(metadata, axisIterators);
                
        WcpsResult result = new WcpsResult(metadata, rasql);
        return result;
    }
    
    private final String TEMPLATE = "<[$intervals] $constants>";
}
