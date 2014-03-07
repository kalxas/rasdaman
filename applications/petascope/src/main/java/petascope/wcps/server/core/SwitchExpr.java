package petascope.wcps.server.core;

import java.util.Iterator;
import java.util.LinkedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCPSException;
import petascope.util.WcpsConstants;
import static petascope.util.ras.RasConstants.*;

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
/**
 * Class for translating the swith expression from xml to rasql.
 * @author Vlad Merticariu <vlad@flanche.net>
 */

public class SwitchExpr extends AbstractRasNode implements ICoverageInfo{
    
    private static Logger log = LoggerFactory.getLogger(SwitchExpr.class);
    
    private LinkedList<CoverageExpr> argsList;
    
    public SwitchExpr(Node node, XmlQuery xq) throws WCPSException, SecoreException {
        int defaultOk = 0;
        argsList = new LinkedList<CoverageExpr>();
        String nodeName = node.getNodeName();
        if(nodeName.equals(WcpsConstants.MSG_SWITCH)){
            NodeList childNodes = node.getChildNodes();
            for(int i = 0; i < childNodes.getLength(); i++){
                Node currentNode = childNodes.item(i);
                if(currentNode.getNodeName().equals(WcpsConstants.MSG_CASE)){
                    //get the condition and the result
                    NodeList caseChildren = currentNode.getChildNodes();
                    for(int j = 0; j < caseChildren.getLength(); j++){
                        Node caseNode = caseChildren.item(j);
                        if(caseNode.getNodeName().equals(WcpsConstants.MSG_CONDITION) ||
                                caseNode.getNodeName().equals(WcpsConstants.MSG_RESULT)){
                            CoverageExpr expr = new CoverageExpr(caseNode.getFirstChild(), xq);
                            argsList.add(expr);
                        }
                        else{
                            throw new WCPSException(WcpsConstants.ERRTXT_UNEXPETCTED_NODE + ": " + caseNode);
                        }
                    }
                }
                else if(currentNode.getNodeName().equals(WcpsConstants.MSG_DEFAULT)){
                    //now get the default result
                    defaultOk = 1;
                    NodeList defChildren = currentNode.getChildNodes();
                    for(int j = 0; j < defChildren.getLength(); j++){
                        Node defNode = defChildren.item(j);
                        if(defNode.getNodeName().equals(WcpsConstants.MSG_RESULT)){
                            argsList.add(new CoverageExpr(defNode.getFirstChild(), xq));
                        }
                        else{
                            throw new WCPSException(WcpsConstants.ERRTXT_UNEXPETCTED_NODE + ": " + defNode);
                        }
                    }
                }
            }
            if(defaultOk == 0){
                throw new WCPSException(WcpsConstants.ERRTXT_MISSING_SWITCH_DEFAULT);
            }
        }
        else{
            throw new WCPSException(WcpsConstants.ERRTXT_UNEXPETCTED_NODE + ": " + nodeName);
        }
    }

    public String toRasQL() {
        String result = RASQL_CASE + " ";
        int pos = 0;
        Iterator<CoverageExpr> it = argsList.iterator();
        while(it.hasNext()){
            String currentChild = it.next().toRasQL();
            if(pos == argsList.size() - 1){
                //we got the default result here
                result += RASQL_ELSE + " " + currentChild + " " + RASQL_END;
            }
            else{
                if(pos % 2 == 0){
                    //we got conditions here
                    result += RASQL_WHEN + " " + currentChild + " ";
                }
                else{
                    //we got results here
                    result += RASQL_THEN + " " + currentChild + " ";
                }
            }
            pos++;
        }
        return result;
    }

    public CoverageInfo getCoverageInfo() {
        return argsList.getFirst().getCoverageInfo();
    }
    
}
