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
///<reference path="ScaleAxis.ts"/>
///<reference path="Scaling.ts"/>

module wcs {
    export class ScaleAxesByFactor extends Scaling {
        public scaleAxis:ScaleAxis[];

        public constructor(scaleAxis:ScaleAxis[]) {
            super();
            this.scaleAxis = angular.copy(scaleAxis);
        }

        public toKVP():string {
            var serializedAxes:string[] = [];
            this.scaleAxis.forEach((axis:ScaleAxis)=> {
                serializedAxes.push(axis.toKVP());
            });

            return "&SCALEAXES=" + serializedAxes.join(",");
        }
    }
}