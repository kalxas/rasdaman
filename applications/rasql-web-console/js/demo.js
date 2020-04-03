/**
 * Class that defines a rasql console that can submit queries and retrieve results from a rasql endpoint
 */

var ENDPOINT = PETASCOPE_ENDPOINT.replace("rasdaman/ows", "rasdaman/rasql");

Rj.util.ConfigManager.setRasdamanServiceUrl({
    // rasql servlet
    serviceUrl: ENDPOINT,
    username: "rasguest",
    password: "rasguest"
})

FlancheJs.defineClass("Rj.query.Rasql", {

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
            if (this.getFormat() == Rj.query.Rasql.FORMAT_IMAGE_JPG ||
                this.getFormat() == Rj.query.Rasql.FORMAT_IMAGE_PNG) {
                return true;
            }
            return false;
        },

        determineFormat: function (stringQuery) {
            if (stringQuery.contains("png")) {
                this.setFormat(Rj.query.Rasql.FORMAT_IMAGE_PNG);
            }
            else if (stringQuery.contains("jpg")) {
                this.setFormat(Rj.query.Rasql.FORMAT_IMAGE_JPG);
            }
            else {
                this.setFormat(Rj.query.Rasql.FORMAT_CSV);
            }
        }
    },

    statics: {
        FORMAT_IMAGE_PNG: "png",
        FORMAT_IMAGE_JPG: "jpg",
        FORMAT_CSV: "csv"
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

Rj.util.BinderManager.setBinder('Rj.widget.LinearDiagram', 'Rj.query.Rasql', diagramBinder);
Rj.util.BinderManager.setBinder('Rj.widget.AreaDiagram', 'Rj.query.Rasql', diagramBinder);
Rj.util.BinderManager.setBinder('Rj.widget.ScatterDiagram', 'Rj.query.Rasql', diagramBinder);

Rj.util.BinderManager.setBinder('Rj.widget.BinaryImage', 'Rj.query.Rasql', function (widget, query) {
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
        var returnString = "<ul>";
        for (var i = 0; i < resultAr.length - 1; i++) {
            returnString += "<li><span class='label label-success'>" + resultAr[i] + "</span></li>"
        }
        returnString += '</ul>'
        return returnString;
    }
    else {
        result = checkForErrors(result)
        return result;
    }
}

/**
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 */
FlancheJs.defineClass("Demo.enterprise.RasqlConsole", {
    init: function () {

    },

    methods: {
        run: function () {
            this.addRichEditor();
            this.addRasdamanVersionListener()
            this.addQuerySelectorListener();
            this.addExecuteQueryListener();
        },

        addQuerySelectorListener: function () {
            var self = this;
            $("#query-populate").change(function () {
                // Edit queries bellow
                var val = $(this).val();
                if (val == 0) {
                    self._editor.setValue('SELECT dbinfo(m) FROM mr AS m')
                }
                if (val == 1) {
                    self._editor.setValue('SELECT sdom(m) FROM mr AS m')
                }
                else if (val == 2) {
                    self._editor.setValue('SELECT c[18,50] FROM mr AS c');
                }
                else if (val == 3) {
                    self._editor.setValue('diagram>>SELECT encode(c[25,35], "csv") FROM mr AS c');
                }
                else if (val == 4) {
                    self._editor.setValue('image>>SELECT encode(c[*:*,*:*], "PNG") FROM mr AS c');
                }
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
            this._editor.setValue('SELECT dbinfo(m) FROM mr AS m');
        },


        runQuery: function (query) {
            var interval = this.addLoadHandlers();
            var queryParts = this.parseInput(query);
            this.dispatchRenderer(queryParts, interval);
        },

        dispatchRenderer: function (queryParts, interval) {
            var wcpsQuery = new Rj.query.Rasql(queryParts.query);
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
                time += 10;
                $("#" + id).html("Executing Query. <span class='label label-warning'>" + formatTime(time) + "</span> passed. Please wait...")
                $('#output').scrollTop("2000000")
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
            window.w = wcpsQuery
            var type = queryParts.query.search(/jpeg/g) === -1 ? "image/png" : "image/jpeg";
            var id = "img-" + Math.floor(Math.random() * 10000000).toString(10);

            var query = encodeURI(wcpsQuery.toString());

            self.appendToOutput(queryParts.query, "<div id='" + id + "'><img src='" + ENDPOINT + "?username=rasguest&password=rasguest&query=" + query + "' /></div>", "Query");
            $("#" + id + " img").on('load', function () {
                clearInterval(interval);
                setTimeout(function () {
                    $('#output').scrollTop("2000000")
                }, 200);
            });
            $("#" + id + " img").on('error', function () {
                $(this).remove();
                clearInterval(interval);
                wcpsQuery.evaluate(function (data) {
                    clearInterval(interval);
                    $("#output").append(checkForErrors(data));
                })

            });

        },

        createRawOutput: function (query, wcpsQuery, interval) {
            var self = this;
            wcpsQuery.evaluate(function (data) {
                clearInterval(interval);
                var stringData;
                if (data instanceof ArrayBuffer) {
                    stringData = formatRasdamanString(String.fromCharCode.apply(null, new Uint8Array(data)));
                }
                else {
                    stringData = formatRasdamanString(data.toString());
                }
                self.appendToOutput(query, stringData, "Query");
                clearInterval(interval);
                setTimeout(function () {
                    $('#output').scrollTop("2000000")
                }, 200);
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
                    ("<br/> <span class='label label-info'>Result:</span><br/>" + value)
                    : ""
                ) + "</p>");
            $('#output').scrollTop("200000000000");
        },

        addRasdamanVersionListener: function () {
            $("#rasdaman-type").change(function () {
                Rj.util.ConfigManager.getRasdamanServiceUrl().serviceUrl = "http://flanche.com:9090/rasdaman/rasql"
            })
        }
    }
});

jQuery(document).ready(function () {
    var demo = new Demo.enterprise.RasqlConsole()
    demo.run()
});