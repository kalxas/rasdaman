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
 * Copyright 2003 - 2017 Peter Baumann /
 rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */

 ///<reference path="../../../../assets/typings/tsd.d.ts"/>

module rasdaman.common {
    /**
     * Directive used to scroll to bottom a div which contains list of row elements.
     */
    export function scrollToBottom($timeout, $window):angular.IDirective {
        return {
            scope: {
                scrollToBottom: "="
            },
            restrict: 'A',
            link: function(scope, element, attr) {
                scope.$watchCollection('scrollToBottom', function(newVal) {
                    if (newVal) {
                        $timeout(function() {
                            element[0].scrollTop = element[0].scrollHeight;
                        }, 0);
                    }
    
                });
            }
        };
    }
}
