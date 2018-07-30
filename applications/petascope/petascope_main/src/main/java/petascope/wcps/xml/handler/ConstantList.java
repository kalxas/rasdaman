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

import petascope.exceptions.WCPSException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import petascope.util.ListUtil;

/**
 * Class to translate list element of constant coverage from XML syntax to
 * abstract syntax
 *
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
//  list <0;1>
//  <value>0</value>
//  <value>1</value>
public class ConstantList extends AbstractRasNode {

    private static final Logger log = LoggerFactory.getLogger(ConstantList.class);

    private final List<String> list;
    private String value;
    private Node lastNode;

    public ConstantList(Node node, WCPSXmlQueryParsingService xq) throws WCPSException {
        list = new ArrayList<>();

        while (node != null) {
            String nodeName = node.getNodeName();
            log.trace("node name: " + nodeName);

            if (nodeName.equals(WcpsConstants.MSG_VALUE)) {
                value = node.getTextContent();
                checkConstantValue(value);
                log.trace("Adding value: " + value);
                list.add(value);
                lastNode = node;
            } else {
                log.error("Unknown node in constant list: " + nodeName);
                throw new WCPSException("Unknown node in constant list '" + nodeName + "'.");
            }

            node = node.getNextSibling();
        }

        log.trace("Parsed constant list with " + list.size() + " elements.");
    }

    /**
     * Check if value of list should be number
     *
     * @param value
     * @throws WCPSException
     */
    private void checkConstantValue(String value) throws WCPSException {
        boolean ok = false;
        try {
            Integer.parseInt(value);
            ok = true;
        } catch (NumberFormatException e1) {
        }
        try {
            Float.parseFloat(value);
            ok = true;
        } catch (NumberFormatException e2) {
        }
        try {
            new ComplexConstant(value);
            ok = true;
        } catch (WCPSException e1) {
        }

        if (ok == false) {
            throw new WCPSException("'" + value + "' is not an integer, float, or complex constant.");
        }
    }

    @Override
    public String toAbstractSyntax() {
        // list <0;1>
        String result = ListUtil.join(list, ";");

        return result;
    }

    /**
     * Size of all elements in the constant list
     *
     * @return *
     */
    public int getSize() {
        return list.size();
    }

    /**
     * Return the last node of the constant list.
     *
     * @return *
     */
    public Node getLastNode() {
        return lastNode;
    }
}
