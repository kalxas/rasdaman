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
<%@page import="secore.util.StringUtil"%>
<%@page import="org.basex.core.cmd.CreateDB"%>
<%@page import="org.basex.core.cmd.Close"%>
<%@page import="org.basex.core.cmd.Open"%>
<%@page import="org.basex.core.cmd.DropDB"%>
<%@page import="secore.db.BaseX"%>
<%@page import="secore.db.Database"%>
<%-- 
    Document   : updatedb
    Created on : Oct 18, 2013, 13:34:30
    Author     : Dimitar Misev
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.Set"%>
<%@page import="java.io.*"%>
<%@page import="java.util.TreeSet"%>
<%@page import="secore.db.DbManager" %>
<%@page import="secore.util.Constants" %>
<!DOCTYPE html>
<html>  
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Update EPSG database</title>
  </head>
  <body>
<%
  out.println("<span style=\"font-size:x-large;\"><a href='" + Constants.INDEX_FILE + "'>Index</a></span><br/><br/>");
  
  String contentType = request.getContentType();
  if ((contentType == null) || (contentType.indexOf("multipart/form-data") == -1)) {
%>
<form enctype="multipart/form-data" action="updatedb.jsp" method="POST">
  <table border="0" >
    <tr>
      <td><b>Choose the new EPSG GML dictionary file:</b></td>
    </tr>
    <tr>
      <td><input name="gmlfile" type="file"></td>
    </tr>
    <tr>
      <td colspan="2">
        <p align="right"><input type="submit" value="Update Database"></p>
      </td>
    </tr>
  </table>
</form>

<%
  } else {
    DataInputStream in = new DataInputStream(request.getInputStream());

    // check data upload size
    int formDataLength = request.getContentLength();
    if (formDataLength > Constants.MAX_UPLOAD_FILE_SIZE) {
      out.println("File size exceeds " + (Constants.MAX_UPLOAD_FILE_SIZE / 1000000) + "MB, update aborted.");
      return;
    }
    
    byte dataBytes[] = new byte[formDataLength];
    int byteRead = 0;
    int totalBytesRead = 0;
    //this loop converting the uploaded file into byte code
    while (totalBytesRead < formDataLength) {
      byteRead = in.read(dataBytes, totalBytesRead, formDataLength);
      totalBytesRead += byteRead;
    }
    
    String file = new String(dataBytes);
    file = StringUtil.fixLinks(file, StringUtil.SERVICE_URI);
    
    // figure out file name
    String saveFile = file.substring(file.indexOf("filename=\"") + 10);
    saveFile = saveFile.substring(0, saveFile.indexOf("\n"));
    saveFile = saveFile.substring(saveFile.lastIndexOf("\\") + 1, saveFile.indexOf("\""));
    
    int lastIndex = contentType.lastIndexOf("=");
    String boundary = contentType.substring(lastIndex + 1, contentType.length());
    
    // extracting the index of file
    int pos;
    pos = file.indexOf("filename=\"");
    pos = file.indexOf("\n", pos) + 1;
    pos = file.indexOf("\n", pos) + 1;
    pos = file.indexOf("\n", pos) + 1;
    
    int boundaryLocation = file.indexOf(boundary, pos) - 4;
    file = file.substring(pos, boundaryLocation);
    dataBytes = file.getBytes();
    
    // creating a new file with the same name and writing the content in new file
    try {
      FileOutputStream fileOut = new FileOutputStream(saveFile);
      fileOut.write(dataBytes, 0, dataBytes.length);
      fileOut.flush();
      fileOut.close();
    } catch (Exception ex) {
      out.println("Can not update database due to internal server error: " + ex.getMessage());
      return;
    }

    // get BaseX database instance
    Database tmpDb = DbManager.getInstance().getDb();
    if (!(tmpDb instanceof BaseX)) {
      out.println("Can not update database of type: " + tmpDb.getClass().getSimpleName());
      return;
    }
    BaseX db = (BaseX) tmpDb;
    
    // drop db
    try {
      db.executeCommand(new Open(DbManager.EPSG_DB));
      db.executeCommand(new DropDB(DbManager.EPSG_DB));
      db.close();
    } catch (Exception ex) {
      out.println("Failed dropping old database due to error: " + ex.getMessage());
    }
    
    // create from new file
    try {
      db.executeCommand(new CreateDB(DbManager.EPSG_DB, saveFile));
      db.close();
      DbManager.clearCache();
    } catch (Exception ex) {
      out.println("Failed creating new database due to error: " + ex.getMessage());
    }
    
    // remove temporary file
    File fileToDelete = new File(saveFile);
    fileToDelete.delete();
    
    // figure out version
    int releasePos = file.indexOf("release-");
    int endReleasePos = file.indexOf("\"", releasePos);
    String release = null;
    if (releasePos != -1 && endReleasePos != -1) {
      release = file.substring(releasePos + "release-".length(), endReleasePos);
    }
%>
    <p>Ok, database updated<%if (release != null) out.print(" to version " + release);%>.</p>
<%
  }
%>
  </body>
</html>

