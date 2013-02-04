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
$(document).ready(function(){
  CodeMirror.commands.autocomplete = function(cm){
    CodeMirror.simpleHint(cm, CodeMirror.javascriptHint);
  }
  var jsEditor = CodeMirror(document.getElementById("js-editor"), {
    value        : "alert('Hello all');\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n",
    mode         : "javascript",
    smartIndent  : true,
    lineNumbers  : true,
    matchBrackets: true,
    extraKeys    : {"Ctrl-Space": "autocomplete"}
  });
  jsEditor.on("cursorActivity", function(){
    jsEditor.matchHighlight("CodeMirror-matchhighlight");
  });

  var cssEditor = CodeMirror(document.getElementById("css-editor"), {
    value        : "div{color: blue}\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n",
    mode         : "css",
    smartIndent  : true,
    lineNumbers  : true,
    matchBrackets: true,
    extraKeys    : {"Ctrl-Space": "autocomplete"}
  })

  var htmlEditor = CodeMirror(document.getElementById("html-editor"), {
    value        : "<div>Hello World</div>\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n",
    mode         : "htmlmixed",
    smartIndent  : true,
    lineNumbers  : true,
    matchBrackets: true
  })

  $("#button-run").click(function(e){
    var html = htmlEditor.getValue();
    var js = jsEditor.getValue();
    var css = cssEditor.getValue();
    $.post("processor.php?action=run", {
      html: html,
      js  : js,
      css : css
    }, function(response){
      console.log(response);
      if(response.search("http") != -1){
        $("#result-iframe").html('<iframe src="' + response + '" width="100%" height="100%"></iframe>');
      }
    });
  });

  $("#button-save").click(function(e){
    var html = htmlEditor.getValue();
    var js = jsEditor.getValue();
    var css = cssEditor.getValue();
    $.post("processor.php?action=save", {
        html: html,
        js  : js,
        css : css
      },
      function(response){
        console.log(response);
        window.open(response)
      });
  });

  $("#button-share").click(function(e){
    var html = htmlEditor.getValue();
    var js = jsEditor.getValue();
    var css = cssEditor.getValue();
    $.post("processor.php?action=share", {
      html: html,
      js  : js,
      css : css
    }, function(response){
      var url = document.URL;
      var qind = url.search("\\?");
      if(qind !== -1){
        url = url.substring(0, qind);
      }
      url += "?id=" + response;
      $("#share-modal").remove();
      $(document.body).append('<div id="share-modal" class="modal hide fade">' +
        '<div class="modal-header">' +
        '<button type="button" class="close" data-dismiss="modal" aria-hidden="true">x</button>' +
        '<h3>Share your code</h3>' +
        '</div> ' +
        '<div class="modal-body"><p>Your code can be found at this URL:</p><a href="' + url + '"> ' + url + ' </a></div>' +
        '<div class="modal-footer"><button class="btn" data-dismiss="modal" aria-hidden="true">Close</button></div>' +
        '</div>');
      $("#share-modal").modal('show');
    });
  });

  (function(){
    var url = document.URL;
    var qind = url.search("\\?");
    if(qind !== -1){
      var id = url.substring(qind+4);
      $.get("processor.php", {
        action: 'get',
        id    : id
      }, function(response){
        var contents = JSON.parse(response);
        jsEditor.setValue(contents.js);
        htmlEditor.setValue(contents.html);
        cssEditor.setValue(contents.css);
      })
    }
  })()
})