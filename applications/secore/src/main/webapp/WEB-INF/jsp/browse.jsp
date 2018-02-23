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
<%@page import="org.rasdaman.SecoreFilter"%>
<%@page import="org.rasdaman.secore.ConfigManager"%>
<%@page import="org.rasdaman.secore.util.ExceptionCode"%>
<%@page import="org.rasdaman.secore.db.DbCollection"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%--
    Document   : browse
    Created on : Dec 21, 2011, 10:06:30 AM
    Author     : Mihaela Rusu, Dimitar Misev
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.Set"%>
<%@page import="java.util.TreeSet"%>
<%@page import="org.rasdaman.secore.db.DbManager" %>
<%@page import="org.rasdaman.secore.Constants" %>
<%@page import="org.rasdaman.secore.util.Pair"%>
<%@page import="org.rasdaman.secore.util.XMLUtil"%>
<%@page import="org.rasdaman.secore.util.StringUtil"%>
<%@page import="org.rasdaman.secore.util.SecoreUtil"%>
<%@page import="org.rasdaman.secore.util.SecoreException"%>
<%@page import="java.util.Comparator"%>
<%@page import="org.rasdaman.secore.gml_validate.GMLValidator" %>
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
    <title>SECORE - Browse Definitions Page</title>

  </head>
  <body>


    <%

      // NOTE: It only allow to add/update/delete definition on userdb (User Dictionary)
      // Then it will not change in GML Dictionary versions.
      String versionNumber = DbManager.FIX_GML_VERSION_NUMBER;

      // NOTE: If not ?add=true then need to load any entries in list (and handle up one level when add new definition also)
      String toadd = request.getParameter("add");

      // Future work: assure a smooth transition between URNs and URLs for the new identifiers
      String url = request.getAttribute(SecoreFilter.CLIENT_REQUEST_URI_ATTRIBUTE).toString().replace(Constants.BROWSE_JSP, "");
      // return the string without first "/", e.g: def/crs/EPSG/
      String servletContextPath = ConfigManager.getInstance().getServerContextPath().replace("/", "");
      String defURL = url.substring(url.lastIndexOf(servletContextPath));

      // when use with browse page, the URL is followed by URN format with maximum 4 parameters (/def/crs/EPSG/0/4326).
      // try if request URI has the version number value
      String urlTmp = "";
      String[] tmp = defURL.split("/");

      // NOTE: in case of version number is missing but has authorityName (e.g: /def/crs/EPSG), it need to load all the versions
      // e.g: http://localhost:8088/def/crs/EPSG/8.5/browse.jsp
      //      http://localhost:8088/def/crs/EPSG/8.6/browse.jsp
      boolean isShowAllVersions = false;

      // Normally, only add definition to userdb, only when request to a definition from userdb, the button will be changed to update
      boolean isAddOnUserDB = true;

      if (tmp.length > 3) {
        versionNumber = tmp[3];
        
        // then create a urlTmp with versionNumber will be changed later when query in user and gml dictionaries.
        // due to before both User and Gml dictionaries were used version "0".
        tmp[3] = Constants.VERSION_NUMBER;
        for (int i = 0; i < tmp.length; i++) {
          urlTmp += tmp[i] + "/";
        }

      } else {
        // if URI don't have *versionNumber* then it is as same as url (/def/crs/EPSG/)
        urlTmp = url;

        if(tmp.length == 3) {
          isShowAllVersions = true;
        }
      }


      // -------------------------------- Page Header --------------------------------
      // ------ Go to index.jsp -----
      out.println("<span style='font-size:large;'>"
          + "<a href='" + ConfigManager.getInstance().getServerContextPath() + "/" + Constants.INDEX_JSP + "'>Index</a></span>");

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
            + "<a href='" + up + "/" + Constants.BROWSE_JSP + "'>Up one level</a></span> ");
      }
      
      // NOTE: only when secore admin username/password in secore.properties then logged in user will see log out link
      if (ConfigManager.getInstance().showLoginPage()) {
        out.println(" | <span style='font-size:large; margin-left: 60px;'>"
            + "<b><a href='" + ConfigManager.getInstance().getServerContextPath() + "/" + Constants.LOGOUT_JSP + "'>Log out</a></b></span> ");
      }

      //  ------ Navigation in entries list --------
      out.println("<br/><span style='font-size:large;'>Nodes at prefix: " + url + "</span>");

      // ---------- Add new definition -----------
      out.println("<br/><span style='font-size:large;'><a href='" + url
          + Constants.BROWSE_JSP + Constants.QUERY_SEPARATOR
          + "add=true'>Add new definition</a>");
      out.print("<span style='color:red;'>&nbsp;&nbsp;&nbsp;Note: you can only add, update or remove 'user defined entries'.</span><br/></span><hr/>");

      // ------------------------------ List all entries ----------------------------
      // This variable to let know when is viewing the entries list or the update definition
      String result = Constants.EMPTY;
      // Check if not ?add = true then load all the entries of list
      if (toadd == null) {

        // Query all the definition which contains the id.
        List<String> versionNumbers = new ArrayList<String>();
        // if the version number is specific then just query with this version.
        if(isShowAllVersions == false) {
          versionNumbers.add(versionNumber);
        } else {
          // if ther version number is not specific then query all the versions
          // e.g(8.5, gml_85)
          for (DbCollection coll: DbManager.collections.keySet()) {
            if (!coll.getCollectionName().equals(DbManager.USER_DB)) {
              // Only add the gml dictionary version numbers.
              versionNumbers.add(coll.getVersionNumber());
            }
          }
        }

        // Query with all the needed GML version numbers.
        for (String verNum: versionNumbers) {
          result += SecoreUtil.queryDef(urlTmp, false, true, true, verNum);
        }

        // Note: if it is max level of URN then it will change to view definition instead of entries list
        // sort elements at this level in a tree set
        Set<Pair<String, Boolean>> res = SecoreUtil.sortElements(urlTmp, versionNumber, result);
        // If res is Empty mean this is max level of entries then go to view definition detail
        if (res.isEmpty()) {
          // NOTE: the definition can be from userdb or gml_*
          // Because of user can insert a definition /def/crs/EPSG/0/2000 into userdb
          // and it support backwards with /def/crs/EPSG/0/2000 as gmldb in /def/crs/EPSG/8.5/2000
          // then in this case if userdb has this id (/crs/EPSG/0/2000) then it will show its definition, otherwise show the original from gml_db.
          String retUserDB = SecoreUtil.queryDef(urlTmp, true, true, false, versionNumber);
          if (!retUserDB.equals(Constants.EMPTY_XML)) {
            // Get definition from userdb
            result = retUserDB;
          } else {
            // Get definition from gml_db*
            try {
                result = SecoreUtil.queryDef(urlTmp, true, false, true, versionNumber);
            } catch (SecoreException e) {
                // When remove the last definition of a URN (e.g: remove: /def/axis/0GC/123/test from /def/axis/OGC/123/) from userdb, 
                // it will reload the page and could not found any definition in /def/axis/OGC/123 from both userdb (check here first, if it cannot find then check 
                // gml_db (which throw exception if versionNumber does not exist.)
                if (e.getExceptionCode().equals(ExceptionCode.VersionNotFoundException)) {
                    // Set the result to empty then it will redirect to the upper parent i.e: /def/axis/OGC/) which has children definitions.
                    result = Constants.EMPTY_XML;
                } else {
                    throw e;
                }
            }
            isAddOnUserDB = false;
          }

          // This mean definition has been deleted and does not exist in DB then go to upper level (Use when user delete a parent entry)
          // NOTE: if countSeperator = 1 (def/) then don't need to go upper as it is root entry
          if (result.equals(Constants.EMPTY_XML)) {
            response.sendRedirect(up + "/" + Constants.BROWSE_JSP);
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
                + "<a href='" + Constants.BROWSE_JSP + Constants.QUERY_SEPARATOR + "delete=" + l + "'"
                + " onclick='return confirm(\" Do you really want to delete entry " + l + " with all of its *user defined entries children* ? \");'>Remove</a>"
                + "</td>";
          }
          // If definition cannot de removed, still need to print it link with remove = ""
          out.print("<tr>"
              + "<td><a href='" + l + "/" + Constants.BROWSE_JSP + "'>" + l + "</a></td>" + remove + " </tr>");
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
            String db = DbManager.USER_DB; // to updateQuery with the userdb

            // Check if the identifier does exist in userdb, if not then insert else update.
            retUser = SecoreUtil.queryDef(newUrl, true, true, false, versionNumber);

            String error = Constants.EMPTY;
            boolean isInsert = true;

            // get gml:identifier to check that user is used the correct version of userdb for definition
            String identifierUri = StringUtil.getElementValue(mod, Constants.IDENTIFIER_LABEL);
            //e.g: 8.5 or empty if it is URN (e.g: urn:org:def:axis:EPSG::9902)
            String identifierVersionNumber = StringUtil.getVersionNumber(identifierUri);
            // Add new CRS definition by URN to userdb.
            if (identifierVersionNumber.equals("")) {
              identifierVersionNumber = DbManager.FIX_USER_VERSION_NUMBER;
            }

            if (!StringUtil.isValidIdentifierURI(identifierUri)) {
              errorUpdate = "<span style='color:red'>GML Identifier URI: <b> $URI </b> is not valid.</span><br/>";
              errorUpdate = errorUpdate.replace("$URI", identifierUri);
            } else {
              // the GML Identifier is correct can add/update in userdb
              // If it does exist in userDictionary then should update it
              if (!retUser.equals(Constants.EMPTY_XML)) {
                error = SecoreUtil.updateDef(mod, newUrl, db);
                isInsert = false;
              } else {
                // or insert to userDictionary
                error = SecoreUtil.insertDef(mod, newUrl);
              }

              // Check if update/insert sucessfully
              if (error.equals(Constants.EMPTY)) {
                // return success
                errorUpdate = "<span style='font-size:large; color:green;'>The definiton has been <u>$MODIFICATION</u>. The database has been updated successfully.</span><br/><br/>";
                if(isInsert) {
                  errorUpdate = errorUpdate.replace("$MODIFICATION", "inserted");
                } else {
                  errorUpdate = errorUpdate.replace("$MODIFICATION", "updated");
                }
              } else {
                // return error
                errorUpdate = "<span style='color:red;'>Error: " + error + " when update, see log file for more detail. The database remains unchanged.</span><br/><br/>";
              }
            }  // end check identifier has correct userdb version number
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

    <form action="<%=url + Constants.BROWSE_JSP + "?update=true"%>" method="post" name="gmlform">
      <textarea cols="150" rows="25" name="changedef" wrap="virtual"><%=mod%></textarea><br/>
      <span style="color: blue">Note: Check definition valid before submitting. If identifier does exist then update, else insert. Only add/update this definition to <b>user dictionary</b>.<br/><br/></span>
      <input type="submit" style="margin-right:20px; float:left;" name="checkValidUpdate" value="Valid GML" onclick="return checkTextEmpty(this)" />
      <%
        // if definition is from userdb, then it is update
        if(isAddOnUserDB) {
          out.print("<input type='submit' name='Update' onclick='return checkTextEmpty(this);' value='Update' />");
        } else {
          // if defintion is from gml_db*, then it is add
          out.print("<input type='submit' name='Update' onclick='return checkTextEmpty(this);' value='Add' />");
        }

      %>
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
          // get gml:identifier to check that user is used the correct version of userdb for definition
          String identifierUri = StringUtil.getElementValue(newd, Constants.IDENTIFIER_LABEL);
          //e.g: 8.5 or empty if it is URN (e.g: urn:org:def:axis:EPSG::9902)
          String identifierVersionNumber = StringUtil.getVersionNumber(identifierUri);
          // Add new CRS definition by URN to userdb.
          if (identifierVersionNumber.equals("")) {
            identifierVersionNumber = DbManager.FIX_USER_VERSION_NUMBER;
          }

          if (!StringUtil.isValidIdentifierURI(identifierUri)) {
            errorAdd = "<span style='color:red'>GML Identifier URI: <b> $URI </b> is not valid.</span><br/>";
            errorAdd = errorAdd.replace("$URI", identifierUri);
          } else { 
            // It is up to user to decide to insert definition to User Dictionary (so just insert definition)
            errorAdd = SecoreUtil.insertDef(newd, url);
            if (errorAdd.equals(Constants.EMPTY)) {
              errorAdd = "<span style='font-size: large; color:green;'>The database has been updated successfully.</span><br/><br/>";
            } else {
              errorAdd = "<span style='color:red;'>Error: " + errorAdd + " when insert, see log file for more detail. The database remains unchanged.</span><br/><br/>";
            }
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


    <form action="<%=url + Constants.BROWSE_JSP + "?add=true"%>" method="post" name="gmlform">
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
      // NOTE: only delete in userdb (even if versionNumber is not 0 as it can be any number but it is stored in userdb).
      errorDel = SecoreUtil.deleteDef(url, todel);

      if (errorDel.equals(Constants.EMPTY)) {
        //errorDel = "<span style='font-size: large; color:green;'>The database has been updated sucessfully.</span><br/><br/>";
        //Note: After removing, need to load all the entries again by reloading the current page as it is success
        response.sendRedirect(url + "/" + Constants.BROWSE_JSP);
      } else {
           errorDel = "<span style='color:red;'>Error: " + errorDel + " when delete, see log file for more detail. The database remains unchanged.</span><br/><br/>";
           // print the error not reload the page
           %>           
           <div style="width: 1024px; height: 150px; overflow-y: auto; margin-top:15px;">
           <%        
              out.print(errorDel);        
           %>
           </div>
        <%
      }   
    }
  %>
</body>
</html>

