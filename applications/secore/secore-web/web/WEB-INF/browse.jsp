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
 * Copyright 2003 - 2012 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
--%>
<%-- 
    Document   : browse
    Created on : Dec 21, 2011, 10:06:30 AM
    Author     : Mihaela Rusu, Dimitar Misev
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.Set"%>
<%@page import="java.util.TreeSet"%>
<%@page import="secore.db.DbManager" %>
<%@page import="secore.util.Constants" %>
<%@page import="secore.util.Pair"%>
<%@page import="secore.util.XMLUtil"%>
<%@page import="secore.util.StringUtil"%>
<%@page import="secore.util.SecoreUtil"%>
<%@page import="secore.util.SecoreException"%>
<%@page import="java.util.Comparator"%>
<!DOCTYPE html>
<script>

  function checkTextEmpty(e) {
    var text = "";
    if (e.name === "Save")
    {
      text = document.gmlform.changedef.value.trim();
    }
    else if (e.name === "Add")
    {
      text = document.gmlform.adddef.value.trim();
    }

    if (!text || 0 === text.length)
    {
      alert("Definition can not be blank.");
      return false;
    }
    return true;
  }
</script>


<html>  
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Browse</title>
  </head>
  <body>


    <%
      out.println("<span style=\"font-size:large;\">"
          + "<a href='" + StringUtil.SERVLET_CONTEXT + "/" + Constants.INDEX_FILE + "'>Index</a></span>");
      // Future work: assure a smooth transition between URNs and URLs for the new identifiers
      String url = (String) request.getAttribute("url");

      String up = url.substring(0, url.lastIndexOf(Constants.REST_SEPARATOR, url.length() - 2));
      if (!up.isEmpty()) {
        out.println(" | <span style=\"font-size:large;\">"
            + "<a href='" + up + "/" + Constants.ADMIN_FILE + "'>Up one level</a></span> ");
      }

      out.println("<br/><span style=\"font-size:large;\">Nodes at prefix: " + url + "</span>");
      out.println("<br/><span style=\"font-size:large;\"><a href='" + url
          + Constants.ADMIN_FILE + Constants.QUERY_SEPARATOR
          + "add=true'>Add new definition?</a></span><br/><hr/>");
      out.println("<br/><span style='color:red;'>Note: you can only add or remove 'user defined entries'.</span><br/><br/>");

      // Handle changed GML deffinitions
      String mod = request.getParameter("changedef");
      if (null != mod) {
        if (!mod.equals(Constants.EMPTY)) {
          String newUrl = StringUtil.getElementValue(mod, Constants.IDENTIFIER_LABEL);

          if (newUrl == null) {
            // need to validate GML in next version
            out.println("<span style=\"font-size:large; color:red;\">Definition is not valid. The database remains unchanged.</span><br/><br/>");
          } else {
            // assum definition is valid then check it does exist in DB.
            // Should check newUrl does exist in userDictionary or GmlDictionary or does not exist in both (return error)
            String retUser = Constants.EMPTY;
            String retEPSG = Constants.EMPTY;
            String db = Constants.EMPTY; // to updateQuery with right DB

            retUser = SecoreUtil.queryDef(newUrl, true, true, false);
            // If it is exist in userDictionary then it should not be in GmlDictionary
            if (!retUser.equals(Constants.EMPTY_XML)) {
              db = DbManager.USER_DB;
            } else // try to check it is exist in EPSG
            {
              retEPSG = SecoreUtil.queryDef(newUrl, true, false, true);
              if (!retEPSG.equals(Constants.EMPTY_XML)) {
                db = DbManager.EPSG_DB;
              }
            }

            // If identifier does not exist in User, Gml Dictionary then it is not valid to update
            if (db.equals(Constants.EMPTY)) {
              out.println("<span style=\"font-size:large; color:red;\">GML identifier does not exist in "
                  + DbManager.USER_DB + " or " + DbManager.EPSG_DB + " dictionaries. The database remains unchanged.</span><br/><br/>");
            } else {
              String error = Constants.EMPTY;
              // NOTE: If definition does exist in User Dictionary then go and update normally (only change anything inside User Dictionary)
              if (db.equals(DbManager.USER_DB)) {
                error = SecoreUtil.updateDef(mod, newUrl, db);
              } else {
                // If definition does exist in Gml Dictionary but not in User Dictionary then *have to* add it (don't update to Gml Dictionary)
                error = SecoreUtil.insertDef(mod, newUrl);
              }
              if (error.equals(Constants.EMPTY)) {
                out.println("<span style=\"font-size:large; color:green;\">The database has been updated.</span><br/><br/>");
              } else {
                out.println("<span style=\"font-size:large; color:red;\">Error: " + error + " when update, see log file for more detail. The database remains unchanged.</span><br/><br/>");
              }
            } // end check GML Identifier exist in User or Gml Dictionaries
          } // end check newURL is null
        } else {
          out.println("<br/><span style=\"font-size:large;\"><span style=\"color:red\">"
              + "Empty definition submitted. The database remains unchanged.<span></span><br/>");
        }
      }

      // Handle newly added GML deffinitions
      String newd = request.getParameter("adddef");
      if (null != newd) {
        if (!newd.equals(Constants.EMPTY)) {
          String error = SecoreUtil.insertDef(newd, url);
          if (error.equals(Constants.EMPTY)) {
            out.println("<span style=\"font-size:large; color:green;\">The database has been updated.</span><br/><br/>");
          } else {
            out.println("<span style=\"font-size:large; color:red;\">Error: " + error + " when insert, see log file for more detail. The database remains unchanged.</span><br/><br/>");
          }
        } else {
          out.println("<br/><span style=\"font-size:large;\"><span style=\"color:red\">"
              + "Empty definition submitted. The database remains unchanged.<span></span><br/>");
        }
      }

      // Decide whether to display the form for a new definition or just the list
      String toadd = request.getParameter("add");
      if (null != toadd && toadd.equals("true")) {
    %>
    <span style="font-size:large;">Add a new GML definition in the space below:</span><br/>
    <form action="<%=url + Constants.ADMIN_FILE%>" method="post" name="gmlform">
      <textarea cols="150" rows="35" name="adddef" wrap="virtual"></textarea><br/>
      <input type="submit" name="Add" onclick="return checkTextEmpty(this)" value="Add" />
    </form>
    <%
    } else {
      // Handles removal of definitions
      String todel = request.getParameter("delete");
      if (null != todel) {
        String error = SecoreUtil.deleteDef(url, todel);

        if (error.equals(Constants.EMPTY)) {
          out.println("<span style=\"font-size:large; color:green;\">The database has been updated.</span><br/><br/>");
        } else {
          out.println("<span style=\"font-size:large; color:red;\">Error: " + error + " when delete, see log file for more detail. The database remains unchanged.</span><br/><br/>");
        }
      }

      // Query for the list of all definitions by category
      String result = SecoreUtil.queryDef(url, false, true, true);

      // Query for individual GML definitions
      if (StringUtil.emptyQueryResult(result)) {
        result = SecoreUtil.queryDef(url, true, false, false);
    %>
    <span style="font-size:large;">The definition below will be replaced by your submission:</span><br/>
    <form action="<%=url + Constants.ADMIN_FILE%>" method="post" name="gmlform">
      <textarea cols="150" rows="35" id="changedef" name="changedef" wrap="virtual"><%out.print(result);%></textarea><br/>
      <input type="submit" onclick="return checkTextEmpty(this)" name="Save" value="Save" />
    </form>
    <%
    } else {
      // sort elements at this level in a tree set
      Set<Pair<String, Boolean>> res = SecoreUtil.sortElements(url, result);
      if (res.isEmpty()) {
        result = SecoreUtil.queryDef(url, true, false, false);
    %>
    <span style="font-size:large;">The definition below will be replaced by your submission:</span><br/>
    <form action="<%=url + Constants.ADMIN_FILE%>" method="post" name="gmlform">
      <textarea cols="150" rows="35" id="changedef" name="changedef" wrap="virtual"><%out.print(result);%></textarea><br/>
      <input type="submit" onclick="return checkTextEmpty(this)" name="Save" value="Save" />
    </form>
    <%
    } else {
    %>
    <table>
      <%
        // Display the list in a table
        for (Pair<String, Boolean> p : res) {
          String l = p.fst;
          String remove = Constants.EMPTY;
          if (p.snd) {
            // only allow removal of user definitions
            remove = "<td><a href='" + Constants.ADMIN_FILE + Constants.QUERY_SEPARATOR + "delete=" + l
                + "' onclick='javascript:return confirm(\"Do you really want to delete the node " + l
                + " and all entries under the path " + url + l + "?\")'>Remove</a>"
                + "</td>";
          }
          out.print("<tr>"
              + "<td><a href='" + l + "/" + Constants.ADMIN_FILE + "'>" + l + "</a></td>"
              + remove
              + "</tr>");
        }
      %>
    </table>
    <%
          }
        }
      }
    %>
  </body>
</html>

