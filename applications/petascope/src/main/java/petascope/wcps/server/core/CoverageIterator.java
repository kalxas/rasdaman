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
 * Copyright 2003 - 2014 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcps.server.core;

import petascope.exceptions.PetascopeException;
import petascope.exceptions.WCPSException;
import org.w3c.dom.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petascope.util.WcpsConstants;
import static petascope.util.ras.RasConstants.*;

public class CoverageIterator extends AbstractRasNode {

    private static Logger log = LoggerFactory.getLogger(CoverageIterator.class);

    // ``for c in (foo, zoo)'' --> "c" isiterator, "foo" and "zoo" are (compatible) coverages
    private List<String> coverageNames;
    private String iteratorName;
    private boolean dynamic = false;    // created from a Construct Coverage expr?

    public CoverageIterator(Node x, XmlQuery xq) throws WCPSException, PetascopeException {
        log.trace(x.getNodeName());
        coverageNames = new ArrayList<String>();
        if (!x.getNodeName().equals(WcpsConstants.MSG_COVERAGE_ITERATOR)) {
            log.error("Invalid cast from " + x.getNodeName() + " XML node to CoverageIterator node");
            throw new WCPSException("Invalid cast from " + x.getNodeName() + " XML node to CoverageIterator node");
        }

        Node it = x.getFirstChild();
        while (it != null) {
            if (it.getNodeName().equals("#" + WcpsConstants.MSG_TEXT)) {
                it = it.getNextSibling();
                continue;
            }

            if (it.getNodeName().equals(WcpsConstants.MSG_ITERATORVAR)) {
                iteratorName = it.getFirstChild().getNodeValue();
                log.trace("Iterator variable: " + iteratorName);
            } else if (it.getNodeName().equals(WcpsConstants.MSG_COVERAGE_NAME)) {
                String cn = it.getFirstChild().getNodeValue();
                log.trace("Coverage reference : " + cn);
                if (!xq.getMetadataSource().coverages().contains(cn)) {
                    log.error("Unknown coverage " + cn);
                    throw new WCPSException("Unknown coverage " + cn);
                }

                coverageNames.add(cn);
            }

            it = it.getNextSibling();
        }
    }

    public CoverageIterator(String iterator, String coverage) throws WCPSException {
        log.trace("Iterator: " + iterator + ", for coverage: " + coverage);
        coverageNames = new ArrayList<String>();
        iteratorName = iterator;
        coverageNames.add(coverage);
        this.dynamic = true;
    }

    public Iterator<String> getCoverages() {
        return coverageNames.iterator();
    }

    // WCPS iterator is used as RasQL alias (...coverageName AS iteratorName...)
    public String getIteratorName() {
        return iteratorName;
    }

    public String toRasQL() {
        // TODO(andreia) : How to translate multiple coverages?
        return coverageNames.get(0) + " " + RASQL_AS + " " + iteratorName;
        // FIXME : toRasQL here is more complex: coverageName = collectionName+OID
    }
}
