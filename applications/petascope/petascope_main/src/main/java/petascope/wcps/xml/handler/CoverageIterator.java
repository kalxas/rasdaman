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
package petascope.wcps.xml.handler;

import petascope.exceptions.PetascopeException;
import petascope.exceptions.WCPSException;
import org.w3c.dom.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petascope.util.ListUtil;

/**
 * Class to translate coverageIterator element from XML syntax to abstract syntax
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */

// for d in (test_eobstest, test_eobstests1)
//<coverageIterator>
//    <iteratorVar>d</iteratorVar>
//    <coverageName>test_eobstest</coverageName>
//    <coverageName>test_eobstest1</coverageName>
//</coverageIterator>
public class CoverageIterator extends AbstractRasNode {

    private static Logger log = LoggerFactory.getLogger(CoverageIterator.class);

    // ``for c in (foo, zoo)'' --> "c" isiterator, "foo" and "zoo" are (compatible) coverages
    private String iteratorVar;
    private List<String> coverageNames;    

    public CoverageIterator(Node node) throws WCPSException, PetascopeException {
        log.trace(node.getNodeName());
        coverageNames = new ArrayList<>(); 
        if (!node.getNodeName().equals(WcpsConstants.MSG_COVERAGE_ITERATOR)) {
            log.error("Invalid cast from '" + node.getNodeName() + "' XML node to CoverageIterator node");
            throw new WCPSException("Invalid cast from '" + node.getNodeName() + "' XML node to CoverageIterator node");
        }

        Node child = node.getFirstChild();
        while (child != null) {
            if (child.getNodeName().equals("#" + WcpsConstants.MSG_TEXT)) {
                child = child.getNextSibling();
                continue;
            }

            if (child.getNodeName().equals(WcpsConstants.MSG_ITERATORVAR)) {
                // e.g: <iteratorVar>c</iteratorVar>
                iteratorVar = child.getFirstChild().getNodeValue();
                log.trace("Iterator variable: " + iteratorVar);
            } else if (child.getNodeName().equals(WcpsConstants.MSG_COVERAGE_NAME)) {
                // e.g: <coverageName>test_mr</coverageName>
                String coverageName = child.getFirstChild().getNodeValue();
                log.trace("Coverage name : " + coverageName);

                coverageNames.add(coverageName);
            }

            child = child.getNextSibling();
        }
    }

    public CoverageIterator(String iterator, String coverage) throws WCPSException {
        log.trace("Iterator: " + iterator + ", for coverage: " + coverage);
        coverageNames = new ArrayList<>();
        iteratorVar = iterator;
        coverageNames.add(coverage);
    }

    public Iterator<String> getCoverages() {
        return coverageNames.iterator();
    }
    
    public String getIteratorName() {
        return this.iteratorVar;
    }


    @Override
    public String toAbstractSyntax() {
        // e.g: c in (test_mr, test_rgb)
        String result = iteratorVar + " in ( " + ListUtil.join(coverageNames, ", ") + " )";
        return result;
    }
}
