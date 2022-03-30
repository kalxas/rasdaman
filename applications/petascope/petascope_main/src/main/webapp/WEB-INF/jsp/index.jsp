<%--
 * This file is part of rasdaman community.
 *
 * Rasdaman community is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Rasdaman community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003 - 2017 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
--%>
<%@page import="org.rasdaman.secore.ConfigManager"%>
<%@page import="java.util.regex.Matcher"%>
<%@page import="java.util.regex.Pattern"%>
<%--
    Document   : index
    Created on : Oct 1, 2011, 12:18:10 PM
    Author     : Dimitar Misev
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="org.rasdaman.secore.db.DbManager" %>
<%@page import="org.rasdaman.secore.Constants" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>SECORE Home Page</title>
    </head>
    <body>
        <% // Handle the XQuery sent from same page after clicking on submit button
            String query = request.getParameter("runquery");
            if (null != query) {
                out.println("<br/><span style=\"font-size:x-large;\"><a href='" + Constants.INDEX_JSP + "'>Index</a></span><br/>");
                if (!query.equals(Constants.EMPTY)) {
                    String versionNumber = DbManager.FIX_GML_VERSION_NUMBER;
                    // NOTE: not every query will need to query in both epsg and userdb (only select will need to do this).
                    String patternStr = "\\s(insert|update|delete)\\s";
                    Pattern pattern = Pattern.compile(patternStr);
                    Matcher matcher = pattern.matcher(query);
                    String result = null;
                    
                    try {
                        // If query is used to change userdb, then it should only query in userdb
                        if (matcher.find()) {
                            result = DbManager.getInstance().getDb().queryUser(query, true);
                        } else {
                            // non-update userdb query (like select can query in both collections: userdb and epsg db).
                            result = DbManager.getInstance().getDb().queryBothDB(query, versionNumber);
                        }
                        out.println("<br/><span>Result:</span><br/>");
                        out.println("<form name=mf><textarea name=mt cols=150 rows=30 readonly>" + result + "</textarea></form>");
                    } catch (Exception ex) {
                        out.println("<br/><span><span style=\"color:red\">Error when querying BaseX database '" + ex.getMessage() + "'.<span></span><br/>");
                    }
                } else {
                    out.println("<br/><span><span style=\"color:red\">Empty query submitted.<span></span><br/>");
                }
            } else {

                String toquery = request.getParameter("query");
                if (null != toquery && toquery.equals("true")) {
                    out.println("<br/><span style=\"font-size:x-large;\">Please write the XQuery to be executed on the database in the space below:</span>");
                    out.println("<br/><span style=\"font-size:x-large;\"><a href='" + Constants.INDEX_JSP + "'>Back</a></span><br/>");
        %>
        <form action="<%=Constants.INDEX_JSP%>" method="post" name="queryform">
            <textarea cols="150" rows="30" name="runquery" wrap="virtual"></textarea><br/>
            <input type="submit" name="Execute" value="Execute" />
        </form>
        <%
            } else {%>
        <h1>Coordinate Reference System Resolver</h1>
        <span style="font-size:x-large;"><%out.print("<a href='" + Constants.BROWSE_JSP + "'>Browse the definitions tree</a><br/>");%></span>        
        <span style="font-size:x-large;"><%out.print("<a href='" + Constants.INDEX_JSP + "?query=true'>Query the database directly</a><br/>");%></span>
        <span style="font-size:x-large;"><%out.print("<a href='" + Constants.DEMO_JSP + "'>View examples</a><br/>");%></span> <br/><br/>
        <%
            // NOTE: only when secore admin username/password in secore.properties then logged in user will see log out link
            if (ConfigManager.getInstance().showLoginPage()) { %>
            <span style="font-size:x-large;"><%out.print("<b><a href='" + ConfigManager.getInstance().getServerContextPath() + "/" + Constants.LOGOUT_JSP + "'>Log out</a></b><br/>");%></span>
            <% }              
          }
        }%>
    </body>
</html>
