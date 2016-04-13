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
package petascope.wcps.grammar;

import java.util.*;
import petascope.util.WcpsConstants;

/**
 * Switch Expression
 * @author Vlad Merticariu
 */

public class SwitchExpr implements IParseTreeNode{
    LinkedList<CoverageExpr> argsList;

    public SwitchExpr(){
        argsList = new LinkedList<CoverageExpr>();
    }

    public void add(CoverageExpr e){
        argsList.add(e);
    }

    @Override
    public String toXML() {
        String result = "<" + WcpsConstants.MSG_SWITCH + ">";

        Iterator<CoverageExpr> it = argsList.iterator();
        int pos = 0;
        while(it.hasNext()){
            String currentChild = it.next().toXML();
            if(pos == argsList.size() - 1){
                //the default result sits here
                result += "<" + WcpsConstants.MSG_DEFAULT + ">";
                result += "<" + WcpsConstants.MSG_RESULT + ">";
                result += currentChild;
                result += "</" + WcpsConstants.MSG_RESULT + ">";
                result += "</" + WcpsConstants.MSG_DEFAULT + ">";
            }
            else{
                if(pos % 2 == 0){
                    //conditions sit here
                    result += "<" + WcpsConstants.MSG_CASE + ">";
                    result += "<" + WcpsConstants.MSG_CONDITION + ">";
                    result += currentChild;
                    result += "</" + WcpsConstants.MSG_CONDITION + ">";
                }
                else{
                    //results sit here
                    result += "<" + WcpsConstants.MSG_RESULT + ">";
                    result += currentChild;
                    result += "</" + WcpsConstants.MSG_RESULT + ">";
                    result += "</" + WcpsConstants.MSG_CASE + ">";
                }
            }
            pos++;
        }
        result += "</" + WcpsConstants.MSG_SWITCH + ">";
        return result;
    }

}
