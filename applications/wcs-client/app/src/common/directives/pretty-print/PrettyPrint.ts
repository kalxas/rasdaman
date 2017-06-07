/*
 * This file is part of rasdaman community.
 *
 * Rasdaman community is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Rasdaman community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2014, 2015 Peter Baumann /
 rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */

///<reference path="../../../../assets/typings/tsd.d.ts"/>
module rasdaman.common {

    /**
     * @param sourceCodeHtml {string} The HTML to pretty print.
     * @param opt_langExtension {string} The language name to use.
     *     Typically, a filename extension like 'cpp' or 'java'.
     * @param opt_numberLines {number|boolean} True to number lines,
     *     or the 1-indexed number of the first line in sourceCodeHtml.
     */
    declare function prettyPrintOne(sourceCodeHtml:string, opt_langExtension:string, opt_numberLines:boolean):string;

    function escapeXml(unsafe) {
        return unsafe.replace(/[<>&'"]/g, function (c) {
            switch (c) {
                case '<':
                    return '&lt;';
                case '>':
                    return '&gt;';
                case '&':
                    return '&amp;';
                case '\'':
                    return '&apos;';
                case '"':
                    return '&quot;';
            }
        });
    }

    /**
     * Directive that allows to pretty print XML code. This can be extended with support for multiple languages if the need arises.
     * @returns {{restrict: string, scope: {code: string}, template: string, link: (function(any, JQuery, any): undefined)}}
     * @constructor
     */
    export function PrettyPrint($sanitize, $sce:angular.ISCEService):angular.IDirective {
        var MAXIMUM_TEXT_LENGTH = 300000;
        return {
            restrict: 'EC',
            scope: {
                data: "="
            },

            templateUrl: "src/common/directives/pretty-print/PrettyPrintTemplate.html",
            link: function (scope:any, element:JQuery, attrs:any) {
                scope.$watch("data", (newData:PrettyPrintObject, oldValue:PrettyPrintObject)=> {
                    //Only update the document if the value changes.
                    if (newData && newData.Value) {
                        if (newData.Value.length > MAXIMUM_TEXT_LENGTH) {
                            newData.Value = newData.Value.substr(0, MAXIMUM_TEXT_LENGTH);
                            newData.Value += "\n The text content is too long to display, only first " + MAXIMUM_TEXT_LENGTH + " characters are shown.";
                        }
                        var escapedHtml = prettyPrintOne(escapeXml(newData.Value), newData.Type, true);
                        scope.document = $sce.trustAsHtml(escapedHtml);
                    }
                }, true);
            }
        };
    }

    PrettyPrint.$inject = ["$sanitize", "$sce"];

    export interface PrettyPrintObject {
        Value:string;
        Type:string;
    }
}
