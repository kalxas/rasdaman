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

///<reference path="../../../models/wcs_model/wcs/_wcs.ts"/>

module rasdaman {
    export class RangeSubsettingModel {
        private availableRanges:string[];
        private isInterval:boolean[];

        public rangeSubset:wcs.RangeSubset;

        public constructor(coverageDescription:wcs.CoverageDescription) {
            this.rangeSubset = new wcs.RangeSubset();
            this.availableRanges = [];
            this.isInterval = [];

            coverageDescription.rangeType.dataRecord.fields.forEach(field => {
                this.availableRanges.push(field.name);
            });
        }

        public addRangeComponent():void {
            this.rangeSubset.rangeItem.push(new wcs.RangeComponent(this.availableRanges[0]));
            this.isInterval.push(false);
        }

        public addRangeComponentInterval():void {
            var begin = new wcs.RangeComponent(this.availableRanges[0]);
            var end = new wcs.RangeComponent(this.availableRanges[this.availableRanges.length - 1]);

            this.rangeSubset.rangeItem.push(new wcs.RangeInterval(begin, end));
            this.isInterval.push(true);
        }

        public deleteRangeComponent(index:number):void {
            this.rangeSubset.rangeItem.splice(index, 1);
        }
    }
}