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
* Copyright 2003 - 2020 Peter Baumann / rasdaman GmbH.
*
* For more information please see <http://www.rasdaman.org>
* or contact Peter Baumann via <baumann@rasdaman.com>.
*/

var ENDPOINT = PETASCOPE_ENDPOINT.replace("rasdaman/ows", "rasdaman/rasql");

// Load queries from queries.json to example queries dropdown.
var populateQueriesDropdown = function() {

    var queries = queriesJSON.queries;
    queries.forEach(function(queryObj) {
        var option = $("<option></option>").attr("value", queryObj.query).text(queryObj.description);
        $("#query-populate").append(option);
    });
}

populateQueriesDropdown();

Rj.util.ConfigManager.setRasdamanServiceUrl({
    // rasql servlet
    serviceUrl: ENDPOINT,
    username: "rasguest",
    password: "rasguest"
})

 /**
 * 
 * Rasql is a class that helps better define the relationships 
 * needed for the rasql web console
 * @module {Rj.widget}
 * @author Dragi Kamov <d.kamov@jacobs-university.de>
 * @version 2.0.0
 */
FlancheJs.defineClass("Rj.widget.Rasql", {

    implements: [Rj.query.LiteralQuery, Rj.query.Executable, Rj.util.Observable],

    /**
     * Constructor
     * @param query
     * @param params
     */
    init: function (query, params) {
        if (_.exists(params)) {
            for (var param in params) {
                this.setVariable(param, undefined);
            }
        }
        this.setQuery(query)
        this._determineFormat(query);
    },

    properties: {
        /**
         * @property {String} format the format of the result of the query
         */
        format: {},
        service: { value: Rj.util.ConfigManager.getRasdamanServiceUrl() }
    },

    methods: {
        transport: function () {
            var params = {
                username: this.getService().username,
                password: this.getService().password,
                query: encodeURIComponent(this.getQuery().toString())
            }

            params["request"] = "?username=" + params.username + "&password=" + params.password + "&query=" + params.query;
            var transport = new Rj.query.Transport(
                this.getService().serviceUrl,
                params,
                Rj.query.Transport.HttpMethod.GET
            );
            transport.setBinary(this._isBinaryResult());
            return transport;
        }

    },

    internals: {
        /**
         * Returns true if the result will be binary, false otherwise
         * @returns {boolean}
         */
        isBinaryResult: function () {
            if (this.getFormat() == Rj.widget.Rasql.FORMAT_IMAGE_JPG ||
                this.getFormat() == Rj.widget.Rasql.FORMAT_IMAGE_PNG) {
                return true;
            }
            return false;
        },

        determineFormat: function (stringQuery) {
            if (stringQuery.contains("png") || stringQuery.contains("PNG")) {
                this.setFormat(Rj.widget.Rasql.FORMAT_IMAGE_PNG);
            }
            else if (stringQuery.contains("jpg") || stringQuery.contains("JPG")) {
                this.setFormat(Rj.widget.Rasql.FORMAT_IMAGE_JPG);
            }
            else if (stringQuery.contains("csv") || stringQuery.contains("CSV")) {
                this.setFormat(Rj.widget.Rasql.FORMAT_CSV);
            }
            else if (stringQuery.contains("json") || stringQuery.contains("JSON")) {
                this.setFormat(Rj.widget.Rasql.FORMAT_JSON);
            }
            else {
                this.setFormat(Rj.widget.Rasql.FORMAT_TEXT);
            }
        }
    },

    statics: {
        FORMAT_IMAGE_PNG: "png",
        FORMAT_IMAGE_JPG: "jpg",
        FORMAT_CSV: "csv",
        FORMAT_JSON: "json",
        FORMAT_TEXT: "txt"
    }
})

var diagramBinder = function (widget, query) {
    query.addListener("listen", "evaluated", function (response) {
        checkForErrors(response, false)
        var parser = new Rj.util.CSVParser(response, function (e) {
            return parseInt(e, 10);
        });
        var dataSeries = new Rj.util.DataSeries(parser.toNativeJsArray());
        widget.setDataSeries(dataSeries);
    })
};

Rj.util.BinderManager.setBinder('Rj.widget.LinearDiagram', 'Rj.widget.Rasql', diagramBinder);
Rj.util.BinderManager.setBinder('Rj.widget.AreaDiagram', 'Rj.widget.Rasql', diagramBinder);
Rj.util.BinderManager.setBinder('Rj.widget.ScatterDiagram', 'Rj.widget.Rasql', diagramBinder);
Rj.util.BinderManager.setBinder('Rj.widget.BinaryImage', 'Rj.widget.Rasql', function (widget, query) {
    query.evaluate(function (response) {
        checkForErrors(response, false, query)
        widget.setBinaryData(response);
    });
    query.addListener("listen", "evaluated", function (response) {
        checkForErrors(response, false, query)
        widget.setBinaryData(response);
    });
});


function formatTime(miliesconds) {
    if (miliesconds < 1000) {
        return miliesconds + " miliseconds"
    }
    else {
        var time = parseInt(miliesconds / 1000, 10) + " seconds"
        if (miliesconds % 1000) {
            time += " " + miliesconds % 1000 + " miliseconds"
        }
        return time;
    }
}

function checkForErrors(response, useErrorManager) {
    if (typeof response == "string" && response.contains("An error has occured")) {
        var error = response.split("<small>")[1].split("</small>")[0].split("Caused by: org.odmg.QueryException: ")[1].split("at rasj")[0];
        if (useErrorManager === true) {
            Rj.util.ErrorManager.reportError(error, true);
        }
        return "<span class='label label-important'>" + error + "</span>"
    }
    else if (response instanceof ArrayBuffer) {
        var strResponse = String.fromCharCode.apply(null, new Uint8Array(response));
        checkForErrors(strResponse, useErrorManager);
    }
    return response;
}

function formatRasdamanString(result) {
    var repl = String.fromCharCode(0);
    if (result.contains(repl)) {
        var resultAr = result.split(repl);
        var stringData = "<ul>";
        for (var i = 0; i < resultAr.length - 1; i++) {
            stringData += "<li><span class='label label-success'>" + resultAr[i] + "</span></li>"
        }
        stringData += '</ul>'
        return stringData;
    }
    else {
        result = checkForErrors(result)
        return result;
    }
}

// Scroll to bottom of Output console
function scrollToBottomOfOutputConsole() {
    var objDiv = $("#output");
    objDiv.scrollTop(objDiv.prop("scrollHeight"));
}

 /**
 * 
 * Class that defines a rasql console that can submit queries and 
 * retrieve results from a rasql endpoint
 * @module {Rj.widget}
 * @author Dragi Kamov <d.kamov@jacobs-university.de>
 * @version 2.0.0
 */
FlancheJs.defineClass("Rj.widget.RasqlConsole", {
    init: function () {
    },

    methods: {
        run: function () {
            this.addRichEditor();
            this.addQuerySelectorListener();
            this.addExecuteQueryListener();
        },

        addQuerySelectorListener: function () {
            var self = this;
            $("#query-populate").change(function () {
                var query = $(this).val();
		        // Add Rasql queries here for Execute
                self._editor.setValue(query);
            });
        },

        addExecuteQueryListener: function () {
            var self = this;
            $("#run").on('click', function (event) {
                event.preventDefault();
                var query = self._editor.getValue();
                self.runQuery(query);
            })
        },

        addRichEditor: function () {
            window.edtiorial = this._editor = CodeMirror(document.getElementById("query"), {
                lineNumbers: true,
                mode: 'text/rasql'
            });
            this._editor.setValue('');
        },


        runQuery: function (query) {
            var interval = this.addLoadHandlers();
            var queryParts = this.parseInput(query);
            this.dispatchRenderer(queryParts, interval);
        },

        dispatchRenderer: function (queryParts, interval) {
            var rasqlQuery = new Rj.widget.Rasql(queryParts.query);
            if (queryParts.widget == null) {
                this.createFileOutput(queryParts.query, rasqlQuery, interval);
                return;
            }
            else if(queryParts.widget.type == "text") {
                this.createRawOutput(queryParts.query, rasqlQuery, interval);
                return;
            }
            else if (queryParts.widget.type == "diagram") {
                this.createDiagramQuery(queryParts, rasqlQuery, interval);
                return;
            }
            else if (queryParts.widget.type == "image") {
                this.createImageQuery(queryParts, rasqlQuery, interval);
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
                time += 10;
                $("#" + id).html("Executing Query. <span class='label label-warning'>" + formatTime(time) + "</span> passed. Please wait...")
                scrollToBottomOfOutputConsole();
            }, 10);
            window.onerror = function () {
                clearInterval(interval);
            }
            return interval;
        },

        parseInput: function (input) {
            if (input.search(">>") == -1) {
                return { widget: null, query: input }
            }
            var inputParts = input.split(">>");
            var widget = {
                type: inputParts[0],
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
            return { widget: widget, query: inputParts[1] };
        },

        createDiagramQuery: function (queryParts, rasqlQuery, interval, type) {
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
            diagram.bind(rasqlQuery)
            diagram.addListener("wcpsConsole", "afterrender", function () {
                clearInterval(interval);
                setTimeout(function () {
                    $('#output').scrollTop("200000")
                }, 200);
            })
            rasqlQuery.evaluate();
        },

        createImageQuery: function (queryParts, rasqlQuery, interval) {
            var self = this;
            window.w = rasqlQuery
            var id = "img-" + Math.floor(Math.random() * 10000000).toString(10);

            var query = encodeURI(rasqlQuery.toString());

            self.appendToOutput(queryParts.query, "<div id='" + id + "'><img src='" + ENDPOINT + "?username=rasguest&password=rasguest&query=" + query + "' /></div>", "Query");
            $("#" + id + " img").on('load', function () {
                clearInterval(interval);
                setTimeout(function () {
                    scrollToBottomOfOutputConsole();
                }, 200);
            });
            $("#" + id + " img").on('error', function () {
                $(this).remove();
                clearInterval(interval);
                rasqlQuery.evaluate(function (data) {
                    clearInterval(interval);
                    $("#output").append(checkForErrors(data));
                })

            });

        },

        createRawOutput: function (query, rasqlQuery, interval) {
            var self = this;
            rasqlQuery.evaluate(function (data) {
                clearInterval(interval);

                if (data instanceof ArrayBuffer) {
                    data = formatRasdamanString(String.fromCharCode.apply(null, new Uint8Array(data)));
                }
                else {
                    data = formatRasdamanString(data.toString());
                }

                data = data.replace(/(\s)+(--End--)(\s)+/, "");
                data = data.replace(/((\s)(--End)(\s)*|(Content-type: text\/plain)(\s)+)/g, "");
                
                self.appendToOutput(query, data, "Query");
                clearInterval(interval);
                setTimeout(function () {
                    scrollToBottomOfOutputConsole();
                }, 200);
            });
        },

        createFileOutput: function (query, rasqlQuery, interval) {
            var self = this;
            rasqlQuery.evaluate(function (data) {
                clearInterval(interval);

                var fileType = this.getFormat();

                if (!(fileType == "png" || fileType == "jpg")) {
                    if (data instanceof ArrayBuffer) {
                        data = String.fromCharCode.apply(null, new Uint8Array(data));
                    }
                    else {
                        data = data.toString();
                    }
    
                    data = data.replace(/(\s)+(--End--)(\s)+/, "");
                    data = data.replace(/((\s)(--End)(\s)*|(Content-type: text\/plain)(\s)+)/g, "");
                }
                
                if (data) {
                    var blob = new Blob([data], {type: "application/octet-stream"});
                    var fileName = "rasql." + fileType;
                    saveAs(blob, fileName);

                    self.appendToOutput(query, "Downloading...", "Query");
                    clearInterval(interval);
                    setTimeout(function () {
                        scrollToBottomOfOutputConsole();
                    }, 200);
                }
            });
        },

        addPropertiesToObject: function (obj, params) {
            for (var index in params) {
                var property = index.charAt(0).toUpperCase() + index.slice(1);
                if (obj["set" + property]) {
                    obj["set" + property].call(obj, params[index])
                }
            }
        },

        appendToOutput: function (query, value, type) {
            var actType = type || "Query";
            $("#output").append("<p><span class='label label-info'>you@rasql>" + actType + ":</span> " + query +
                (type != "Status" ?
                    ("<br> <span class='label label-info'>Result:</span><br><span style=\"white-space: pre-wrap;\">" + value + "</span>")
                    : ""
                ) + "</p>");
            scrollToBottomOfOutputConsole();
        }
    }
});
