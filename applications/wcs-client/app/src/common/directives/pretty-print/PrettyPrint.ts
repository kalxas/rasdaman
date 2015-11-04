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

    declare function prettyPrint(callBack:any, htmlElement:HTMLElement):string;

    /**
     * Directive that allows to pretty print XML code. This can be extended with support for multiple languages if the need arises.
     * @returns {{restrict: string, scope: {code: string}, template: string, link: (function(any, JQuery, any): undefined)}}
     * @constructor
     */
    export function PrettyPrint():angular.IDirective {
        return {
            restrict: "E",
            scope: {
                code: "="
            },
            template: '<?prettify lang=xml?><pre class="prettyprint">{{code}}<pre>',
            link: function ($scope:any, element:JQuery, attributes:any) {
                $scope.$watch("code", (newValue:any, oldValue:any)=> {
                    //Optimize this and remove the timeout.
                    if (newValue) {
                        window.setTimeout(()=> {
                            prettyPrint(()=> {
                            }, element.get(0));
                        }, 500);
                    }
                });
            }
        };
    }
}