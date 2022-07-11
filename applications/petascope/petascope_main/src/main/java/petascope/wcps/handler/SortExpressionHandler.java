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
 * Copyright 2003 - 2022 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcps.handler;

import java.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.WCPSException;
import petascope.util.CrsUtil;
import petascope.wcps.exception.processing.CoverageAxisNotFoundExeption;
import petascope.wcps.metadata.model.Axis;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;
import petascope.wcps.metadata.service.SortedAxisIteratorAliasRegistry;
import petascope.wcps.result.WcpsResult;

/**
 *
 * Handler for the sort expression to sort values along an axis by a ranking function
 * e.g. SORT $c + 30 ALONG ansi DESC BY $c[Long(30:30)] with $c is a 3D coverage (time, long, lat grid axes order).
 * 
 * generated rasql:
 *      SORT c ALONG 0 AS i DESC BY min_cells(c[i[0], 0:0, *:*])
 *
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
@Service
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class SortExpressionHandler extends Handler {
    
    // e.g: SORT ... ALONG ... AS ... BY ...
    private final String COVERAGE_EXPRESSION_TEMPLATE = "$coverageExpression";
    private final String AXIS_LABEL_INDEX_TEMPLATE = "$axisLabelIndex";
    private final String AXIS_ITERATOR_TEMPLATE = "$axisIterator";
    private final String SORTING_ORDER = "$sortingOrder";
    private final String CELL_EXPRESSION_TEMPLATE = "$cellExpression";
    
    private final String RASQL_TEMPLATE = "SORT " + COVERAGE_EXPRESSION_TEMPLATE + " ALONG " + AXIS_LABEL_INDEX_TEMPLATE                                         
                                        + " AS " + AXIS_ITERATOR_TEMPLATE
                                        + " " + SORTING_ORDER
                                        + " BY " + CELL_EXPRESSION_TEMPLATE;
    
    public static final String DEFAULT_SORTING_ORDER = "ASC";
    
    @Autowired 
    private SortedAxisIteratorAliasRegistry sortedAxisIteratorAliasRegistry;
    
    public SortExpressionHandler() {
        
    }
    
    public SortExpressionHandler create(Handler coverageExpressionHandler, StringScalarHandler sortedAxisLabelHandler,
                                        StringScalarHandler sortingOrderHandler, Handler cellExpressionHandler) {
        SortExpressionHandler result = new SortExpressionHandler();
        result.sortedAxisIteratorAliasRegistry = this.sortedAxisIteratorAliasRegistry;
        result.setChildren(Arrays.asList(coverageExpressionHandler, sortedAxisLabelHandler, sortingOrderHandler, cellExpressionHandler));
        
        return result;
    }
    
    public WcpsResult handle() throws PetascopeException {
        
        WcpsResult coverageExpression = (WcpsResult) this.getFirstChild().handle();
        // e.g. sorted by Long axis
        String sortedAxisLabel = ((WcpsResult) this.getSecondChild().handle()).getRasql();
        
        int sortedAxisLabelIndex = -1;
        int i = 0;
        
        for (Axis axis : coverageExpression.getMetadata().getAxes()) {
            if (CrsUtil.axisLabelsMatch(axis.getLabel(), sortedAxisLabel)) {
                sortedAxisLabelIndex = i;
                break;
            }
            
            i++;
        }
        
        if (sortedAxisLabelIndex == -1) {
            throw new CoverageAxisNotFoundExeption(sortedAxisLabel);
        }
        
        String sortingOrder = ((WcpsResult) this.getThirdChild().handle()).getRasql();
        
        this.sortedAxisIteratorAliasRegistry.add(sortedAxisLabel);
        
        // e.g. i
        String sortedAxisIteratorLabel = this.sortedAxisIteratorAliasRegistry.getIteratorLabel(sortedAxisLabel);
        
        WcpsResult cellExpression = (WcpsResult) this.getFourthChild().handle();
        
        this.sortedAxisIteratorAliasRegistry.remove(sortedAxisLabel);        
        
        WcpsResult result = this.handle(coverageExpression, sortedAxisLabel, i, sortedAxisIteratorLabel, sortingOrder, cellExpression);
        return result;
    }

    /**
     * Handle the sort operator
     */
    private WcpsResult handle(WcpsResult coverageExpression, String sortedAxisLabel, int sortedAxisGridIndex, String sortedAxisIteratorLabel, String sortingOrder, WcpsResult cellExpression) {
        WcpsResult result = coverageExpression;
        
        WcpsCoverageMetadata coverageExpressionMetadata = coverageExpression.getMetadata();
        WcpsCoverageMetadata cellExpressionMetadata = cellExpression.getMetadata();
        
        if (cellExpressionMetadata != null) {
            Axis coverageExpressionSortedAxis = coverageExpressionMetadata.getAxisByName(sortedAxisLabel);
            Axis cellExpressionSortedAxis = null;
            try {
                cellExpressionSortedAxis = cellExpressionMetadata.getAxisByName(sortedAxisLabel);
            } catch (Exception ex) {
                if (!(ex instanceof CoverageAxisNotFoundExeption)) {
                    throw ex;
                }
            }

            if (cellExpressionSortedAxis == null) {
                throw new WCPSException(ExceptionCode.InvalidRequest, "Sorted axis '" + sortedAxisLabel + "' does not exist in the ranking cellExpr; "
                        + "you have to remove any slicing on '" + sortedAxisLabel + "' from the cellExpr.");
            } else {
                if ((coverageExpressionSortedAxis.getGeoBounds().getLowerLimit().compareTo(cellExpressionSortedAxis.getGeoBounds().getLowerLimit()) != 0)
                    || (cellExpressionSortedAxis.getGeoBounds().getUpperLimit().compareTo(cellExpressionSortedAxis.getGeoBounds().getUpperLimit()) != 0)
                    ) {
                    throw new WCPSException(ExceptionCode.InvalidRequest, 
                            "Sorted axis '"  + sortedAxisLabel + "' must have the same geo bounds between coverageExpr and ranking cellExpr; "
                          + "you have to remove any subsets on '" + sortedAxisLabel + "' in the cellExpr, or add an equivalent subset on '" + sortedAxisLabel + "'  in the coverageExpr.");
                }
            }
        }
        
        
        
        String rasql = this.RASQL_TEMPLATE
                                          // e.g. c + 30
                                          .replace(COVERAGE_EXPRESSION_TEMPLATE, coverageExpression.getRasql())
                                          // e.g. 0
                                          .replace(AXIS_LABEL_INDEX_TEMPLATE, String.valueOf(sortedAxisGridIndex))
                                          // e.g. i
                                          .replace(AXIS_ITERATOR_TEMPLATE, sortedAxisIteratorLabel)
                                          // e.g desc
                                          .replace(SORTING_ORDER, sortingOrder)
                                           // e.g. min_cells(c[i[0], 0:0])
                                          .replace(CELL_EXPRESSION_TEMPLATE, cellExpression.getRasql());
        result.setRasql(rasql);
        
        return result;
    }
}
