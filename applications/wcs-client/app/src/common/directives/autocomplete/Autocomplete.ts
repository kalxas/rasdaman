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
     * Simple directive that wraps JQuery UI Autocomplete plugin.
     * @param $timeout
     * @returns {{scope: {source: string}, link: (function(angular.IScope, any, any): undefined)}}
     * @constructor
     */
    export function Autocomplete($timeout):angular.IDirective {
        return {
            restrict: "A",
            scope: {
                source: "=source"
            },
            link: function (scope:angular.IScope, elem:any, attributes:any) {
                scope.$watch("source", (newValue, oldValue)=> {
                    //Remove the old instance.
                    if (elem.autocomplete("instance")) {
                        elem.autocomplete("destroy");
                    }

                    if (newValue) {
                        elem.autocomplete({
                            source: newValue,
                            select: function () {
                                $timeout(function () {
                                    elem.trigger('input');
                                }, 100);
                            }
                        });
                    }
                });
            }
        };
    }

    Autocomplete.$inject = ["$timeout"];
}