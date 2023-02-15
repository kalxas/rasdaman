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
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import petascope.exceptions.PetascopeException;
import petascope.util.StringUtil;
import static petascope.wcps.handler.CoverageConstantHandler.updateAxisNamesFromAxisIterators;
import static petascope.wcps.handler.CoverageConstructorHandler.validateAxisIteratorSubsetWithQuote;
import petascope.wcps.metadata.model.Axis;
import petascope.wcps.metadata.model.RegularAxis;
import petascope.wcps.metadata.model.Subset;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;
import petascope.wcps.metadata.service.AxisIteratorAliasRegistry;
import petascope.wcps.metadata.service.RasqlTranslationService;
import petascope.wcps.metadata.service.SubsetParsingService;
import petascope.wcps.metadata.service.UsingCondenseRegistry;
import petascope.wcps.metadata.service.WcpsCoverageMetadataGeneralService;
import petascope.wcps.result.VisitorResult;
import petascope.wcps.result.WcpsResult;
import petascope.wcps.subset_axis.model.AxisIterator;
import petascope.wcps.subset_axis.model.WcpsSubsetDimension;

/**
 * Translation node from wcps coverage list to rasql for the general condenser
 * Example:  <code>
 * CONDENSE +
 * OVER x x(0:100)
 * WHERE true
 * USING 2
 * </code> translates to  <code>
 * CONDENSE +
 * OVER x in [0:100]
 * WHERE true
 * USING 2
 * </code>
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
@Service
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class GeneralCondenserHandler extends Handler {

    @Autowired
    private WcpsCoverageMetadataGeneralService wcpsCoverageMetadataService;
    @Autowired
    private SubsetParsingService subsetParsingService;
    @Autowired
    private RasqlTranslationService rasqlTranslationService;
    @Autowired
    private AxisIteratorAliasRegistry axisIteratorAliasRegistry;
    @Autowired
    private UsingCondenseRegistry usingCondenseRegistry;
    
    public GeneralCondenserHandler() {
        
    }
    
    public GeneralCondenserHandler create(Handler operatorHandler, List<Handler> axisIteratorHandlers,
                                          Handler whereClauseHandler, Handler usingClauseHandler) {
        GeneralCondenserHandler result = new GeneralCondenserHandler();
        List<Handler> childHandlers = new ArrayList<>();
        childHandlers.add(operatorHandler);
        childHandlers.addAll(axisIteratorHandlers);
        childHandlers.add(whereClauseHandler);
        childHandlers.add(usingClauseHandler);
        
        result.setChildren(childHandlers);
        
        result.wcpsCoverageMetadataService = this.wcpsCoverageMetadataService;
        result.subsetParsingService = this.subsetParsingService;
        result.rasqlTranslationService = this.rasqlTranslationService;
        result.axisIteratorAliasRegistry = this.axisIteratorAliasRegistry;
        result.usingCondenseRegistry = this.usingCondenseRegistry;
        
        return result;
    }

    @Override
    public WcpsResult handle() throws PetascopeException {
        String operator = ((WcpsResult)this.getFirstChild().handle()).getRasql();
        // NOTE: this is important to handle general condenser for virtual coverage properly 
        // e.g. over $pt t (imageCrsdomain(c[unix("2011-01-01":"2012-01-01")], unix)) 
        //      using scale(c[unix($pt)] , {Lat:"CRS:1"(0:5)})
        this.usingCondenseRegistry.setOperator(operator);
        
        List<Handler> axisIteratorHandlers = this.getChildren().subList(1, this.getChildren().size() - 2);
        
        String rasqlAliasName = "";
        String aliasName = "";
        int count = 0;        
        
        List<AxisIterator> axisIterators = new ArrayList<>();
        for (Handler axisIteratorHandler : axisIteratorHandlers) {
            AxisIterator axisIterator = (AxisIterator)axisIteratorHandler.handle();
            aliasName = axisIterator.getAliasName();
            axisIteratorAliasRegistry.addAxisIteratorAliasMapping(aliasName, axisIterator);
            
            if (rasqlAliasName.isEmpty()) {
                rasqlAliasName = StringUtil.stripDollarSign(aliasName);
            }
            
            axisIterator.setRasqlAliasName(rasqlAliasName);
            axisIterator.setAxisIteratorOrder(count);            
            
            axisIterators.add(axisIterator);
            count++;
        }
        
        Handler whereClauseHandler = this.getChildren().get(this.getChildren().size() - 2);
        WcpsResult whereClause = null;
        if (whereClauseHandler != null) {
            whereClause = (WcpsResult) whereClauseHandler.handle(); 
        }
        
        Handler usingClauseHandler = this.getChildren().get(this.getChildren().size() - 1);
        WcpsResult usingExpressionResult = (WcpsResult) usingClauseHandler.handle();
        if (usingExpressionResult.getMetadata() != null) {
            usingExpressionResult.getMetadata().setCondenserResult(true);
        }
        
        WcpsResult result;
        
        try {
            result = this.handle(operator, axisIterators, whereClause, usingExpressionResult);
        } finally {

        }
        
        return result;
    }

    private WcpsResult handle(String operator, List<AxisIterator> axisIterators, WcpsResult whereClauseExpression,
            WcpsResult usingCoverageExpression) throws PetascopeException {
        
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
            
            validateAxisIteratorSubsetWithQuote(null, alias, subsetDimension);
            
            if (rasqlAliasName.isEmpty()) {
                rasqlAliasName = alias.replace(WcpsSubsetDimension.AXIS_ITERATOR_DOLLAR_SIGN, "");
            }
            // Check if axis iterator's subset dimension which has the "$"
            
            String bounds = axisIterator.getSubsetDimension().getStringBounds();
            if (bounds.contains(WcpsSubsetDimension.AXIS_ITERATOR_DOLLAR_SIGN) || bounds.contains("[")) {
                // e.g. axis iterator in rasql: pt[0] is used as lower / upper bound
                axisIteratorSubsetDimensions.add(subsetDimension);
                axes.add(new RegularAxis(subsetDimension.getAxisName(), null, null, null, null, null, null, null, 0, BigDecimal.ZERO, BigDecimal.ONE, null));
            } else {
                pureSubsetDimensions.add(subsetDimension);
                axes.add(axisIterator.getAxis());
            }
        }

        //create a coverage with the domain expressed in the condenser
        List<Subset> numericSubsets = subsetParsingService.convertToRawNumericSubsets(pureSubsetDimensions, axes);
        WcpsCoverageMetadata metadata = wcpsCoverageMetadataService.createCoverage(CONDENSER_TEMP_NAME, usingCoverageExpression.getMetadata(), numericSubsets, axes);
        
        updateAxisNamesFromAxisIterators(metadata, axisIterators);

        String rasqlDomain = rasqlTranslationService.constructRasqlDomain(metadata.getSortedAxesByGridOrder(), axisIteratorSubsetDimensions);
        String template = TEMPLATE.replace("$operation", operator)
                .replace("$iter", rasqlAliasName)
                .replace("$intervals", rasqlDomain)
                .replace("$using", usingCoverageExpression.getRasql());
        

        if (whereClauseExpression != null) {
            template = template.replace("$whereClause", whereClauseExpression.getRasql());
        } else {
            template = template.replace("$whereClause", "");
        }

        return new WcpsResult(usingCoverageExpression.getMetadata(), template);
    }

    public static final String USING = "USING";
    private final String CONDENSER_TEMP_NAME = "CONDENSE_TEMP";
    private final String TEMPLATE = "CONDENSE $operation OVER $iter in [$intervals] $whereClause " + USING + " $using";
}
