<?php
/*
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
 * Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
require_once("src/server.php");
require_once("src/utils.php");
cors();
session_start();
$server = new Server(__DIR__, $_POST, $_GET, session_id());

//Normal requests
if ($_GET['action'] === "run") {

  $server->handleRequest();
  print $server->getPathToUserBoxHtml();

} else if ($_GET['action'] === "save") {

  $server->handleRequest();
  print $server->getPathToZippedBox();

} else if ($_GET['action'] === "share") {

  $server->handleRequest();
  print session_id();

} else if ($_GET['action'] === "get") {

  $id = $_GET['id'];
  print json_encode(Server::getUserBoxContents(__DIR__, $id));

} else if ($_GET['action'] === "example") {

  $server->handleRequest();
  print("index.html?id=" . session_id());

} else {

  print "No action specified.";

}