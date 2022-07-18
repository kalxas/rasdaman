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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import petascope.core.Pair;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.WCPSException;
import petascope.wcps.result.VisitorResult;
import petascope.wcps.result.WcpsResult;
import petascope.wcps.subset_axis.model.DimensionIntervalList;

/**
Handler for this expression:

//  coverageExpression LEFT_BRACKET dimensionIntervalList RIGHT_BRACKET
// e.g: c[Lat(0:20)] - Trim
 */
@Service
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ShorthandSubsetHandler extends Handler {
    
    @Autowired
    private SubsetExpressionHandler subsetExpressionHandler;
    
    public ShorthandSubsetHandler() {
        
    }
    
    public ShorthandSubsetHandler create(Handler coverageExpressionHandler, Handler dimensionIntervalListHandler) {
        ShorthandSubsetHandler result = new ShorthandSubsetHandler();
        result.setChildren(Arrays.asList(coverageExpressionHandler, dimensionIntervalListHandler));
        result.subsetExpressionHandler = subsetExpressionHandler;
        
        return result;
    }
    
    @Override
    public WcpsResult handle() throws PetascopeException {
        if (!this.getChildren().isEmpty()) {
            this.updateQueryTree(this.getParent(), this.getFirstChild(), this.getSecondChild());
        }
        
        if (this.getChildren().size() == 2) {
            WcpsResult coverageExpression = (WcpsResult) this.getFirstChild().handle();
            DimensionIntervalList dimensionIntervalListExpression = (DimensionIntervalList) this.getSecondChild().handle();

            return this.handle(coverageExpression, dimensionIntervalListExpression);
        } else {
            // Here, the current node is removed, parent node of the current node has a new child instead
            WcpsResult coverageExpressionResult = (WcpsResult) this.getFirstChild().handle();
            return coverageExpressionResult;
        }
    }

    private WcpsResult handle(WcpsResult coverageExpression, VisitorResult dimensionIntervalListExpression) throws WCPSException, PetascopeException {
        //  coverageExpression LEFT_BRACKET dimensionIntervalList RIGHT_BRACKET
        // e.g: c[Lat(0:20), Long(0:30)] - Trim
        DimensionIntervalList dimensionIntervalList = (DimensionIntervalList)dimensionIntervalListExpression;
        WcpsResult wcpsResult = subsetExpressionHandler.handle(coverageExpression, dimensionIntervalList);
        return wcpsResult;
    }
    
    /**
     * Traverse the children tree nodes and update the non-updated coverage variable name handler nodes with subset domains
     * e.g. (c + d)[Lat(0:30)] is translated to (c[Lat(0:30)] + d[Lat(0:30)]
     */
    private void updateQueryTree(Handler parentNode, Handler childCoverageExpressionHandler, Handler childDimensionIntervalListHandler) throws PetascopeException {
        boolean updated = false;
        Queue<Pair<Handler, Integer>> queue = new ArrayDeque<>();
        queue.add(new Pair<>(childCoverageExpressionHandler, 0));
        
        if (childCoverageExpressionHandler.getClass().getName().equals(this.getClass().getName())
            || childCoverageExpressionHandler.getClass().getName().equals(ReduceExpressionHandler.class.getName())) {
            return;
        }
        
        while (!queue.isEmpty()) {
            Pair<Handler, Integer> currentNodePair = queue.remove();
            Handler currentNode = currentNodePair.fst;
            Integer currentNodeIndex = currentNodePair.snd;
            
            if (currentNode instanceof CoverageVariableNameHandler && currentNode.isUpdatedHandlerAlready(this) == false) {
                Handler parentNodeTmp = currentNode.getParent();
                Handler shortHandSubsetHandler = this.create(currentNode, childDimensionIntervalListHandler);
                
                parentNodeTmp.getChildren().set(currentNodeIndex, shortHandSubsetHandler);
                shortHandSubsetHandler.setParent(parentNodeTmp);
                
                currentNode.addUpdatedHandler(this);
                updated = true;
            }
            
            if (currentNode != null && currentNode.getChildren() != null) {
                List<Pair<Handler, Integer>> childNodes = new ArrayList<>();
                int i = 0;
                for (Handler childHandler : currentNode.getChildren()) {
                    if (childHandler != null && 
                           (childHandler.getClass().getName().equals(this.getClass().getName())
                            || childHandler.getClass().getName().equals(ReduceExpressionHandler.class.getName()))
                        ) {
                        // If the child handler of this node is shorthandsubset then do nothing
                        // e.g. c[ansi("2008-01-03T23:59:55.000Z":"2008-01-08T00:02:58.000Z"), E:"CRS:1"(0:0), N:"CRS:1"(0:0)] [ansi("2008-01-08T00:02:58.000Z")]

                        return;
                    }
                    childNodes.add(new Pair<>(childHandler, i));
                    i++;
                }

                queue.addAll(childNodes);
            }
        }
        
        if (updated) {
//            parentNode.getChildren().set(0, coverageExpressionHandler);
//            coverageExpressionHandler.setParent(parentNode);
            int i = 0;
            for (Handler childNodeHandler : parentNode.getChildren()) {
                if (childNodeHandler != null && childNodeHandler.getClass().getName().equals(this.getClass().getName())) {
                    break;
                }
                i++;
                    
            }
            
            Handler pushedUpHandlerNode = this.getChildren().get(0);
            parentNode.getChildren().set(i, pushedUpHandlerNode);
            pushedUpHandlerNode.setParent(parentNode);
            this.getChildren().clear();
            this.getChildren().add(0, pushedUpHandlerNode);
        }
        
    }

}
