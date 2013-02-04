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
class Server {

  private $postData;
  private $getData;
  private $sessionId;
  private $jsContents;
  private $htmlContents;
  private $cssContents;
  private $serverPath;

  function __construct($serverPath, $postData, $getData, $sessionId) {
    $this->serverPath = $serverPath;
    $this->postData = $postData;
    $this->getData = $getData;
    $this->sessionId = $sessionId;
  }

  function extractCode() {
    $this->jsContents = $this->postData['js'];
    $this->htmlContents = $this->postData['html'];
    $this->cssContents = $this->postData['css'];
  }

  private function userBoxExists() {
    return file_exists("$this->serverPath/files/$this->sessionId");
  }

  private function createUserBox() {
    shell_exec("mkdir -p $this->serverPath/files/" . $this->sessionId);
    shell_exec("cp -r $this->serverPath/raswct/ $this->serverPath/files/$this->sessionId/raswct");
  }

  private function destroyUserBox() {
    shell_exec("rm -rf $this->serverPath/files/" . $this->sessionId);
  }

  private function createJsFile() {
    file_put_contents("$this->serverPath/files/$this->sessionId/userCode.js", $this->jsContents);
  }

  private function  createCSSFile() {
    file_put_contents("$this->serverPath/files/$this->sessionId/userCode.css", $this->cssContents);
  }

  private function createHTMLFile() {
    $html = Server::HTMLTemplate;
    $html = str_replace("{userHtmlCode}", $this->htmlContents, $html);
    file_put_contents("$this->serverPath/files/$this->sessionId/userCode.html", $html);
  }

  private function createZipArchive() {
    shell_exec("rm -f files/$this->sessionId.zip");
    shell_exec("zip -r files/$this->sessionId.zip files/$this->sessionId/ ");
  }

  public function handleRequest() {
    $this->extractCode();
    if (!$this->userBoxExists()) {
      $this->createUserBox();
    }
    $this->createCSSFile();
    $this->createJsFile();
    $this->createHTMLFile();
  }

  private function  getPathToUserBox() {
    $path = "http://$_SERVER[HTTP_HOST]$_SERVER[REQUEST_URI]";
    if (strstr($path, ".php")) {
      $pathParts = explode("/", $path);
      array_pop($pathParts);
      $path = implode($pathParts, "/");
    }
    $path .= "/files/" . $this->sessionId;
    return $path;
  }

  public function getPathToUserBoxHtml() {
    return $this->getPathToUserBox() . "/userCode.html";
  }

  public function getPathToZippedBox() {
    $this->createZipArchive();
    return $this->getPathToUserBox() . ".zip";
  }

  public static function getUserBoxContents($serverPath, $id) {
    $html = file_get_contents("$serverPath/files/$id/userCode.html");
    $fInd = strpos($html, "<body>") + strlen("<body>");
    $sInd = strpos($html, "</body>");
    $html = substr($html, $fInd, $sInd);
    $html = str_replace(array("</html>", "</body>", '<script type="text/javascript" src="userCode.js"></script>'), array("", "", ""), $html);
    return array(
      "html" => $html,
      "js"   => file_get_contents("$serverPath/files/$id/userCode.js"),
      "css"  => file_get_contents("$serverPath/files/$id/userCode.css")
    );
  }

  const HTMLTemplate = <<<HEREDOC
<!DOCTYPE html>
<html>
<head>
<link href="raswct/raswct.css" rel="stylesheet" type="text/css"/>
<link href="userCode.css" rel="stylesheet" type="text/css"/>
<script type="text/javascript" src="raswct/raswct.js"></script>
</head>
<body>
{userHtmlCode}
<script type="text/javascript" src="userCode.js"></script>
</body>
</html>
HEREDOC;

}