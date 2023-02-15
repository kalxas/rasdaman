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

import petascope.wcps.metadata.model.WcpsCoverageMetadata;
import petascope.wcps.result.WcpsResult;
import petascope.wcps.subset_axis.model.AxisIterator;
import petascope.wcps.subset_axis.model.WcpsSubsetDimension;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.WCPSException;
import petascope.util.StringUtil;
import static petascope.wcps.handler.CoverageConstantHandler.updateAxisNamesFromAxisIterators;
import petascope.wcps.metadata.model.Axis;
import petascope.wcps.metadata.model.Subset;
import petascope.wcps.metadata.service.AxisIteratorAliasRegistry;
import petascope.wcps.metadata.service.RasqlTranslationService;
import petascope.wcps.metadata.service.SubsetParsingService;
import petascope.wcps.metadata.service.WcpsCoverageMetadataGeneralService;
import petascope.wcps.result.VisitorResult;
import petascope.wcps.subset_axis.model.WcpsTrimSubsetDimension;

/**
 * Handler for WCPS:
 
 // COVERAGE IDENTIFIER  
 // OVER axisIterator (COMMA axisIterator)*
 // VALUES coverageExpression
 
 // e.g: coverage cov 
 //      over $px x(0:20), 
 //           $px y(0:20)
 //      values avg(c)
 
 * 
 * Translation node from wcps coverage list to rasql for the coverage
 * constructor Example:  <code>
 * COVERAGE myCoverage
 * OVER x x(0:100)
 * VALUES 200
 * 
 * </code> translates to  <code>
 * MARRAY x in [0:100]
 * VALUES 200
 * </code>
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
@Service
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class CoverageConstructorHandler extends Handler {

    @Autowired
    private WcpsCoverageMetadataGeneralService wcpsCoverageMetadataService;
    @Autowired
    private SubsetParsingService subsetParsingService;
    @Autowired
    private RasqlTranslationService rasqlTranslationService;
    @Autowired
    private AxisIteratorAliasRegistry axisIteratorAliasRegistry;
    
    public CoverageConstructorHandler() {
        
    }
    
    public CoverageConstructorHandler create(Handler coverageVariableNameHandler, List<Handler> axisIteratorHandlers, Handler valuesCoverageExpressionHandler) {
        CoverageConstructorHandler result = new CoverageConstructorHandler();
        
        List<Handler> childHandlers = new ArrayList<>();
        childHandlers.add(coverageVariableNameHandler);
        childHandlers.addAll(axisIteratorHandlers);
        childHandlers.add(valuesCoverageExpressionHandler);
        
        result.setChildren(childHandlers);
        
        result.wcpsCoverageMetadataService = this.wcpsCoverageMetadataService;
        result.subsetParsingService = this.subsetParsingService;
        result.rasqlTranslationService = this.rasqlTranslationService;
        result.axisIteratorAliasRegistry = this.axisIteratorAliasRegistry;
        
        return result;
    }
    
    public VisitorResult handle() throws PetascopeException {
        String coverageName = ((WcpsResult)this.getFirstChild().handle()).getRasql();
        
        List<AxisIterator> axisIterators = new ArrayList<>();
        List<Handler> axisIteratorHandlers = this.getChildren().subList(1, this.getChildren().size() - 1);
        
        String rasqlAliasName = "";
        String aliasName = "";
        int count = 0;
        
        for (Handler axisIteratorHandler : axisIteratorHandlers) {
            AxisIterator axisIterator = (AxisIterator) axisIteratorHandler.handle();
            
            aliasName = axisIterator.getAliasName();
            if (rasqlAliasName.isEmpty()) {
                rasqlAliasName = StringUtil.stripDollarSign(aliasName);
            }
            
            axisIterator.setRasqlAliasName(rasqlAliasName);
            axisIterator.setAxisIteratorOrder(count);
            
            this.axisIteratorAliasRegistry.addAxisIteratorAliasMapping(aliasName, axisIterator);
            
            axisIterators.add(axisIterator);
            count++;
        }
         
        Handler valuesCoverageExpressionHandler = this.getChildren().get(this.getChildren().size() - 1);
        WcpsResult valuesCoverageExpression = (WcpsResult)valuesCoverageExpressionHandler.handle();
        
        WcpsResult result = this.handle(coverageName, axisIterators, valuesCoverageExpression);
        return result;
    }

    public WcpsResult handle(String coverageName, List<AxisIterator> axisIterators, WcpsResult valuesCoverageExpression) throws PetascopeException {

        // contains subset dimension without "$"
        List<WcpsSubsetDimension> pureSubsetDimensions = new ArrayList<>();
        // contains subset dimension with "$"
        List<WcpsSubsetDimension> axisIteratorSubsetDimensions = new ArrayList<>();

        // All of the axis iterators uses the same rasql alias name (e.g: px)
        String rasqlAliasName = "";
        
        List<Axis> axes = new ArrayList<>();

        for (AxisIterator axisIterator : axisIterators) {
            String alias = axisIterator.getAliasName();
            WcpsSubsetDimension subsetDimension = axisIterator.getSubsetDimension();
            
            validateAxisIteratorSubsetWithQuote(coverageName, alias, subsetDimension);

            if (rasqlAliasName.isEmpty()) {
                rasqlAliasName = alias.replace(WcpsSubsetDimension.AXIS_ITERATOR_DOLLAR_SIGN, "");
            }
            // Check if axis iterator's subset dimension which has the "$"
            if (axisIterator.getSubsetDimension().getStringBounds().contains(WcpsSubsetDimension.AXIS_ITERATOR_DOLLAR_SIGN)) {
                axisIteratorSubsetDimensions.add(subsetDimension);
            } else {
                pureSubsetDimensions.add(subsetDimension);
            }
            
            axes.add(axisIterator.getAxis());
        }
        
        List<Subset> numericSubsets = subsetParsingService.convertToRawNumericSubsets(pureSubsetDimensions, axes);
        WcpsCoverageMetadata metadata = wcpsCoverageMetadataService.createCoverage(coverageName, valuesCoverageExpression.getMetadata(), numericSubsets, axes);
        
        updateAxisNamesFromAxisIterators(metadata, axisIterators);
        
        String rasqlDomain = rasqlTranslationService.constructRasqlDomain(metadata.getSortedAxesByGridOrder(), axisIteratorSubsetDimensions);
        if (valuesCoverageExpression.getMetadata() != null) {
            for (Axis axis : valuesCoverageExpression.getMetadata().getAxes()) {
                metadata.getAxes().add(axis);
            }
        }
        
        String template = TEMPLATE.replace("$iter", rasqlAliasName)
                                  .replace("$intervals", rasqlDomain)
                                  .replace("$values", valuesCoverageExpression.getRasql());
        return new WcpsResult(metadata, template);
    }
    
    public static void validateAxisIteratorSubsetWithQuote(String coverageName, String axisIteratorAlias, 
                                                           WcpsSubsetDimension subsetDimension) {
    }

    private final String TEMPLATE = "MARRAY $iter in [$intervals] VALUES $values";
}
