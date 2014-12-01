/**
 * Created by Alexandru on 17.10.2014.
 */

define(function () {

    var Util = {};

    Util.loadXMLDoc = function (filename) {
        if (window.XMLHttpRequest) {
            xhttp = new XMLHttpRequest();
        }
        else // code for IE5 and IE6
        {
            xhttp = new ActiveXObject("Microsoft.XMLHTTP");
        }
        xhttp.open("GET", filename, false);
        xhttp.send();
        return xhttp.responseXML;
    };

    Util.createGuid = (function() {
        function s4() {
            return Math.floor((1 + Math.random()) * 0x10000)
                .toString(16)
                .substring(1);
        }
        return function() {
            return s4() + s4() + '-' + s4() + '-' + s4() + '-' +
                s4() + '-' + s4() + s4() + s4();
        };
    })();

    Util.createAutocompleteFilter = function (/*string[]*/ items) {
        return function (request, response) {
            var matches, substrRegex;
            var query = request.term;
            //http://stackoverflow.com/questions/2593637/how-to-escape-regular-expression-in-javascript
            query = (query + '').replace(/([.?*+^$[\]\\(){}|-])/g, "\\$1");

            // an array that will be populated with substring matches
            matches = [];

            // regex used to determine if a string contains the substring `q`=
            substrRegex = new RegExp(query, 'i');

            // iterate through the pool of strings and for any string that
            // contains the substring `q`, add it to the `matches` array
            $.each(items, function (i, str) {
                if (substrRegex.test(str)) {
                    // the typeahead jQuery plugin expects suggestions to a
                    // JavaScript object, refer to typeahead docs for more info
                    matches.push({ value: str });
                }
            });

            response(matches);
        };
    };

    return Util;

});