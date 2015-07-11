/**
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 */
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
 /

 /**
 * A WCPSQuery object can send wcps queries to a service that can process them and parse the result
 * to obtain meaningul data for Widgets.
 * @module {Rj.query}
 * @implements Rj.query.LiteralQuery, Rj.query.Executable
 * @author Alex Dumitru <m.dumitru@jacobs-university.de>
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @version 3.0.0
 */

FlancheJs.defineClass("Rj.query.WCPSQuery", {

  /**
   * Constructor for the class
   * @param query a string query containing 0 or more parameterized variables
   * @param {Array} vars - the query variables (e.g. $domain)
   */
  init: function (query, vars) {
    this.setQuery(query);
    if (_.exists(vars)) {
      for (var i = 0; i < vars.length; i++) {
        this.setVariable(vars[i], undefined);
      }
    }
  },

  implements: [Rj.query.Executable, Rj.query.LiteralQuery, Rj.util.Bindable, Rj.util.Observable],

  properties: {
    /**
     * Should be set to true if the query returns a binary format (e.g. image)
     * instead of UTF-8 / ASCII format
     */
    binaryFormat: {
      value: false
    },
    WCPSService : {
      value: null
    }
  },

  methods: {
    /**
     * Returns a transport object that can be used internally by
     * the Executable trait                                                $
     * @return {Rj.query.Transport}
     */
    transport: function () {
      var queryParameter = _.exists(this.getWCPSService()) ? this.getWCPSService().queryParameter : Rj.util.ConfigManager.getWCPSService().queryParameter;
      var serviceUrl = window.location.protocol + "//" + window.location.host + "/rasdaman/ows"
      var params = {};

      params["query"] = this.getQuery();
      var transport = new Rj.query.Transport(
          serviceUrl,
          params,
          Rj.query.Transport.HttpMethod.GET
      );
      transport.setBinary(this.getBinaryFormat());
      return transport;
    }
  }

});
FlancheJs.defineClass("Demo.wcps.Console", {
  init: function () {

  },

  methods: {
    run: function () {
      var self = this;
      this.addRichEditor();
      $("#run").on('click', function (event) {
        event.preventDefault();
        var query = self._editor.getValue();
        self.runQuery(query);
      })
      $("#query-populate").change(function () {
        var val = $(this).val();
        if (val == 1) {
          self._editor.setValue('image>>for t1 in ( lena ) return encode( t1, "png" )');
        }
        else if (val == 2) {
	  self._editor.setValue('for t1 in ( NN3_1 ) return encode( t1[t(0:49)], "csv" )');
        }
        else if(val == 3){
          self._editor.setValue('diagram>>for t1 in ( NN3_1 ) return encode( t1[t(0:49)], "csv" )');
        }
      })
    },

    addRichEditor: function () {
      CodeMirror.commands.autocomplete = function (cm) {
        CodeMirror.showHint(cm, CodeMirror.javascriptHint);
      }
      window.edtiorial = this._editor = CodeMirror(document.getElementById("query"), {
        lineNumbers: true,
        extraKeys  : {"Ctrl-Space": "autocomplete"},
        theme      : 'eclipse',
        mode       : 'xquery'
      });
    },


    runQuery: function (query) {
      var interval = this.addLoadHandlers();
      var queryParts = this.parseInput(query);
      this.dispatchRenderer(queryParts, interval);
    },

    dispatchRenderer: function (queryParts, interval) {
      var wcpsQuery = new Rj.query.WCPSQuery(queryParts.query, []);
      if (queryParts.widget == null) {
        this.createRawOutput(queryParts.query, wcpsQuery, interval)
        return;
      }
      if (queryParts.widget.type == "diagram") {
        this.createDiagramQuery(queryParts, wcpsQuery, interval);
        return;
      }
      if (queryParts.widget.type == "image") {
        this.createImageQuery(queryParts, wcpsQuery, interval);
        return;
      }
    },

    addLoadHandlers: function () {
      var self = this;
      var time = 0;
      var id = "img-" + Math.floor(Math.random() * 10000000).toString(10);
      self.appendToOutput("<div id='" + id + "'></div>", "", "Status")
      $("#" + id).html("Executing Query. <span class='label label-warning'>" + time + " seconds</span> passed. Please wait...")
      var interval = setInterval(function () {
        time += 1;
        $("#" + id).html("Executing Query. <span class='label label-warning'>" + time + " seconds</span> passed. Please wait...")
        $('#output').scrollTop("2000000")
      }, 1000);
      window.onerror = function () {
        clearInterval(interval);
      }
      return interval;
    },

    parseInput: function (input) {
      if (input.search(">>") == -1) {
        return {widget: null, query: input}
      }
      var inputParts = input.split(">>");
      var widget = {
        type      : inputParts[0],
        parameters: null
      }
      if (inputParts[0].indexOf("(") != -1) {
        var widgetParams = inputParts[0].substring(inputParts[0].indexOf("(") + 1, inputParts[0].indexOf(")")).split(",")
        var params = {};
        _.each(widgetParams, function (value) {
          var parts = value.split("=");
          params[parts[0]] = parts[1];
        });
        widget.type = inputParts[0].substring(0, inputParts[0].indexOf("("));
        widget.parameters = params;
      }
      return {widget: widget, query: inputParts[1]};
    },

    createDiagramQuery: function (queryParts, wcpsQuery, interval, type) {
      var self = this;
      var id = "img-" + Math.floor(Math.random() * 10000000).toString(10);
      self.appendToOutput(queryParts.query, "<div id='" + id + "'></div>", "Query");
      var type = "Linear";
      if (queryParts.widget.parameters && queryParts.widget.parameters.type) {
        type = queryParts.widget.parameters.type.charAt(0).toUpperCase() + queryParts.widget.parameters.type.slice(1);
      }
      var diagramType = type + "Diagram";
      var diagram = new Rj.widget[diagramType]("#" + id);
      this.addPropertiesToObject(diagram, queryParts.widget.parameters);
      diagram.bind(wcpsQuery)
      diagram.addListener("wcpsConsole", "afterrender", function () {
        clearInterval(interval);
        setTimeout(function () {
          $('#output').scrollTop("200000")
        }, 200);
      })
      wcpsQuery.evaluate();
    },

    createImageQuery: function (queryParts, wcpsQuery, interval) {
      var self = this;
      var type = queryParts.query.search(/jpeg/g) === -1 ? "image/png" : "image/jpeg";
      var id = "img-" + Math.floor(Math.random() * 10000000).toString(10);
      self.appendToOutput(queryParts.query, "<div id='" + id + "'></div>", "Query");
      wcpsQuery.setBinaryFormat(true);
      var image = new Rj.widget.BinaryImage("#" + id, type);
      image.bind(wcpsQuery);
      this.addPropertiesToObject(image, queryParts.widget.parameters);
      image.addListener("wcpsConsole", "afterrender", function () {
        clearInterval(interval);
        setTimeout(function () {
          $('#output').scrollTop("2000000")
        }, 200);
      });
    },

    createRawOutput: function (query, wcpsQuery, interval) {
      var self = this;
      wcpsQuery.evaluate(function (data) {
        clearInterval(interval);
        var stringData = data.toString();
        self.appendToOutput(query, stringData, "Query");
        clearInterval(interval);
        setTimeout(function () {
          $('#output').scrollTop("2000000")
        }, 200);
      });
    },

    addPropertiesToObject: function (obj, params) {
      console.log(obj, params);
      for (var index in params) {
        var property = index.charAt(0).toUpperCase() + index.slice(1);
        if (obj["set" + property]) {
          obj["set" + property].call(obj, params[index])
        }
      }
    },

    appendToOutput: function (query, value, type) {
      var actType = type || "Query";
      $("#output").append("<p><span class='label label-info'>you@wcps>" + actType + ":</span> " + query +
        (type != "Status" ?
          ( "<br/> <span class='label label-info'>Result:</span><br/>" + value)
          : ""
          ) + "</p>");
      $('#output').scrollTop("200000000000");
    }
  }
})

jQuery(document).ready(function () {
  var demo = new Demo.wcps.Console();
  demo.run();
})
