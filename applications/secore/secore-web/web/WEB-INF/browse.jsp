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
<%@page import="secore.gml.GMLValidator" %>
<!DOCTYPE html>
<script>

  function checkTextEmpty(e) {
    var text = "";
    if (e.name === "Update" || e.name === "checkValidUpdate")
    {
      text = document.gmlform.changedef.value.trim();
    }
    else if (e.name === "Add" || e.name === "checkValidAdd")
    {
      text = document.gmlform.adddef.value.trim();
    }

    if (!text || 0 === text.length)
    {
      alert("Definition can not be blank.");
      return false;
    }

    // Check if user Save or Add then show a confirm
    if (e.name === "Update" || e.name === "Add")
    {
      var r = confirm("Do you really want to submit this definition? \n(Note: it is up to you after validating definition manually).");
      // If ok then submit to change definition
      if (r === true)
      {
        return true;
      }
      else
      {
        return false;
      }
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

      // NOTE: If not ?add=true then need to load any entries in list (and handle up one level when add new definition also)
      String toadd = request.getParameter("add");

      // Future work: assure a smooth transition between URNs and URLs for the new identifiers
      String url = (String) request.getAttribute("url");
      String defURL = url.substring(url.lastIndexOf("def"));

      // -------------------------------- Page Header --------------------------------
      // ------ Go to index.jsp -----
      out.println("<span style='font-size:large;'>"
          + "<a href='" + StringUtil.SERVLET_CONTEXT + "/" + Constants.INDEX_FILE + "'>Index</a></span>");

      // ----- Go to up one level in entries list -------
      String up = url.substring(0, url.lastIndexOf(Constants.REST_SEPARATOR, url.length() - 2));

      // Count how many "/" from "def/", if it is 5 and add=true then go to upper normally
      int countSeprator = defURL.length() - defURL.replace("/", "").length();
      // if it is < 5 then it is go back not go to upper
      if (toadd != null && countSeprator < 5) {
        up = url.substring(0, url.lastIndexOf(Constants.REST_SEPARATOR, url.length()));
      }
      if (!up.isEmpty()) {
        out.println(" | <span style='font-size:large;'>"
            + "<a href='" + up + "/" + Constants.ADMIN_FILE + "'>Up one level</a></span> ");
      }

      //  ------ Navigation in entries list --------
      out.println("<br/><span style='font-size:large;'>Nodes at prefix: " + url + "</span>");

      // ---------- Add new definition -----------
      out.println("<br/><span style='font-size:large;'><a href='" + url
          + Constants.ADMIN_FILE + Constants.QUERY_SEPARATOR
          + "add=true'>Add new definition</a>");
      out.print("<span style='color:red;'>&nbsp;&nbsp;&nbsp;Note: you can only add, update or remove 'user defined entries'.</span><br/></span><hr/>");

      // ------------------------------ List all entries ----------------------------
      // This variable to let know when is viewing the entries list or the update definition
      String result = Constants.EMPTY;
      // Check if not ?add = true then load all the entries of list
      if (toadd == null) {

        result = SecoreUtil.queryDef(url, false, true, true);

        // Note: if it is max level of URN then it will change to view definition instead of entries list
        // sort elements at this level in a tree set
        Set<Pair<String, Boolean>> res = SecoreUtil.sortElements(url, result);
        // If res is Empty mean this is max level of entries then go to view definition detail
        if (res.isEmpty()) {
          result = SecoreUtil.queryDef(url, true, false, false);

          // This mean definition has been deleted and does not exist in DB then go to upper level (Use when user delete a parent entry)
          // NOTE: if countSeperator = 1 (def/) then don't need to go upper as it is root entry
          if (result.equals(Constants.EMPTY_XML)) {
            response.sendRedirect(up + "/browse.jsp");
          }
        } // This is not max level of entries, then load the list of entries
        else {
    %>
    <table>
      <%
        // Display the list in a table with the Remove link if it is or has "user defined entry(s)"
        for (Pair<String, Boolean> p : res) {
          String l = p.fst;
          String remove = Constants.EMPTY;
          if (p.snd) {
            // only allow removal of user definitions
            remove = "<td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
                + "<a href='" + Constants.ADMIN_FILE + Constants.QUERY_SEPARATOR + "delete=" + l + "'"
                + " onclick='return confirm(\" Do you really want to delete entry " + l + " with all of its *user defined entries children* ? \");'>Remove</a>"
                + "</td>";
          }
          // If definition cannot de removed, still need to print it link with remove = ""
          out.print("<tr>"
              + "<td><a href='" + l + "/" + Constants.ADMIN_FILE + "'>" + l + "</a></td>" + remove + " </tr>");
        } // end for each definition
      %>
    </table>
    <%
        } // end load entries list
      } // end check access ?add=true

      // ------------------------------------ Update Definition -----------------------------------------
      String mod = request.getParameter("changedef");
      String errorCheckValidUpdate = ""; // get value from error when validate GML
      // 1. Check Valid when update new definition
      String checkValidUpdate = request.getParameter("checkValidUpdate"); // check parameter to know which page is accessed
      String errorUpdate = Constants.EMPTY; // only get error when updateDef

      // If user chose valid then checkValidUpdate = "Valid"
      if (null != checkValidUpdate) {
        errorCheckValidUpdate = GMLValidator.parseAndValidateGMLFile(mod);
        if (!errorCheckValidUpdate.equals(Constants.EMPTY)) {
          errorCheckValidUpdate = "<span style='color:red;'>Error when valid definition, you can ignore and submit if you don't need to fix: <br/> " + "</span>" + errorCheckValidUpdate;
        } else if (errorCheckValidUpdate.equals(Constants.EMPTY)) {
          errorCheckValidUpdate = ("<span style='color:green;'>GML definition is well-formed and validated correctly.</span>");
        }
      }

      // 2. Handle newly updated GML deffinitions  (when only ?update=true and user has submitted updated definition and not "checkValidUpdate")
      String toupdate = request.getParameter("update");
      if (null != toupdate && toupdate.equals("true") && checkValidUpdate == null && mod != null) {
        if (!mod.equals(Constants.EMPTY)) {
          String newUrl = StringUtil.getElementValue(mod, Constants.IDENTIFIER_LABEL);

          // NOTE: this can be error and most is not valid in GML definition
          if (newUrl == null) {
            errorUpdate = "<span style='color:red;'>Error: Definition is not valid, please check valid before submitting. The database remains unchanged.</span><br/><br/>";
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
              errorUpdate = "<span style='color:red;'>GML identifier does not exist in "
                  + DbManager.USER_DB + " or " + DbManager.EPSG_DB + " dictionaries. The database remains unchanged.</span><br/><br/>";
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
                errorUpdate = "<span style='font-size:large; color:green;'>The database has been updated successfully.</span><br/><br/>";
              } else {
                errorUpdate = "<span style='color:red;'>Error: " + error + " when update, see log file for more detail. The database remains unchanged.</span><br/><br/>";
              }
            } // end check GML Identifier exist in User or Gml Dictionaries
          } // end check newURL is null
        } else {
          errorUpdate = "<span style='color:red'>Empty definition submitted. The database remains unchanged.<span><br/>";
        }
      }

      // 3. Decide which time needed to view and update document detail
      // If click on entries list then can view normally as ?update=true
      // NOTE: entries list using "<el></el>" tag, then if result doesn't have this tag then it is viewing definition data.
      if ((null != toupdate && toupdate.equals("true")) || ((!result.contains("<el>")) && (!result.equals(Constants.EMPTY)))) {
        if (!result.equals(Constants.EMPTY) && (mod == null)) {
          // first time access to view definition, then just get the data from result in DB
          mod = result;
        }

    %>

    <!-- View and Update definition form !-->
    <span style="font-size:large;">The definition below will be replaced by your submission:</span><br/>

    <form action="<%=url + Constants.ADMIN_FILE + "?update=true"%>" method="post" name="gmlform">
      <textarea cols="150" rows="25" name="changedef" wrap="virtual"><%=mod%></textarea><br/>
      <span style="color: blue">Note: you should check definition valid before submitting.<br/><br/></span>
      <input type="submit" style="margin-right:20px; float:left;" name="checkValidUpdate" value="Valid GML" onclick="return checkTextEmpty(this)" />
      <input type="submit" name="Update" onclick="return checkTextEmpty(this);" value="Update" />
    </form>


    <div style="width: 1024px; height: 150px; overflow-y: auto; margin-top:15px;">
      <%
        // If error when valid  != "" or does exist always print
        if (!errorCheckValidUpdate.equals(Constants.EMPTY)) {
          errorCheckValidUpdate = errorCheckValidUpdate.replace("\n", "<br/>");
          out.print(errorCheckValidUpdate);
        } // If error when add != "" or does exist always print
        else if (!errorUpdate.equals(Constants.EMPTY)) {
          errorUpdate = errorUpdate.replace("\n", "<br/>");
          out.print(errorUpdate);
        }
      %>
    </div>



    <%
      } // end display update detail

      // ------------------------- Add Definition ------------------------------------
      String newd = request.getParameter("adddef"); // get value from TextArea
      String errorCheckValidAdd = ""; // get value from error when validate GML
      // 1. Check Valid when add new definition
      String checkValidAdd = request.getParameter("checkValidAdd"); // check parameter to know which page is accessed
      String errorAdd = Constants.EMPTY; // only get error when insertDef

      // If user chose valid then checkValidAdd = "Valid"
      if (null != checkValidAdd) {
        errorCheckValidAdd = GMLValidator.parseAndValidateGMLFile(newd);
        if (!errorCheckValidAdd.equals(Constants.EMPTY)) {
          errorCheckValidAdd = "<span style='color:red;'>Error when valid definition, you can ignore and submit if you don't need to fix: <br/> " + "</span>" + errorCheckValidAdd;
        } else if (errorCheckValidAdd.equals(Constants.EMPTY)) {
          errorCheckValidAdd = ("<span style='color:green;'>GML definition is well-formed and validated correctly.</span>");
        }
      }

      // 2. Handle newly added GML deffinitions  (when only ?add=true and user has submitted new definition and not "checkValidAdd")
      if (null != toadd && toadd.equals("true") && checkValidAdd == null && newd != null) {
        if (!newd.equals(Constants.EMPTY)) {
          // It is up to user to decide to insert definition to User Dictionary (so just insert definition)
          errorAdd = SecoreUtil.insertDef(newd, url);
          if (errorAdd.equals(Constants.EMPTY)) {
            errorAdd = "<span style='font-size: large; color:green;'>The database has been updated successfully.</span><br/><br/>";
          } else {
            errorAdd = "<span style='color:red;'>Error: " + errorAdd + " when insert, see log file for more detail. The database remains unchanged.</span><br/><br/>";
          }

        } else {
          errorAdd = "<br/><span style='color:red'>"
              + "Empty definition submitted. The database remains unchanged.</span><br/>";
        }
      }

      // 3. Decide whether to display the form for a new definition or just the list (?add = true)
      if (null != toadd && toadd.equals("true")) {
        if (newd == null) {
          newd = Constants.EMPTY; // as when first load page newd = ""
        }
    %>

    <!-- Add new definition form !-->
    <span style="font-size:large;">Add a new GML definition in the space below:</span><br/>


    <form action="<%=url + Constants.ADMIN_FILE + "?add=true"%>" method="post" name="gmlform">
      <textarea cols="150" rows="25" name="adddef" wrap="virtual"><%=newd%></textarea><br/>
      <span style="color: blue">Note: you should check definition valid before submitting.<br/><br/></span>
      <input type="submit" style="margin-right:20px; float:left;" name="checkValidAdd" value="Valid GML" onclick="return checkTextEmpty(this)" />
      <input type="submit" name="Add" onclick="return checkTextEmpty(this);" value="Add" />
    </form>


    <div style="width: 1024px; height: 150px; overflow-y: auto; margin-top:15px;">
      <%
        // If error when valid  != "" or does exist always print
        if (!errorCheckValidAdd.equals(Constants.EMPTY)) {
          errorCheckValidAdd = errorCheckValidAdd.replace("\n", "<br/>");
          out.print(errorCheckValidAdd);
        } // If error when add != "" or does exist always print
        else if (!errorAdd.equals(Constants.EMPTY)) {
          errorAdd = errorAdd.replace("\n", "<br/>");
          out.print(errorAdd);
        }
      %>
    </div>

  </form>
  <%
    } // end check add definition
    // ----------------------------------- Remove Definition -------------------------------
    String errorDel = Constants.EMPTY;
    // 2. Handles removal of definitions
    String todel = request.getParameter("delete");
    if (null != todel) {
      errorDel = SecoreUtil.deleteDef(url, todel);

      if (errorDel.equals(Constants.EMPTY)) {
        errorDel = "<span style='font-size: large; color:green;'>The database has been updated sucessfully.</span><br/><br/>";
      } else {
        errorDel = "<span style='color:red;'>Error: " + errorDel + " when delete, see log file for more detail. The database remains unchanged.</span><br/><br/>";
      }

      // Note: After removing, need to load all the entries again by reloading the current page
      response.sendRedirect(url + "/browse.jsp");

    }


  %>
</body>
</html>

