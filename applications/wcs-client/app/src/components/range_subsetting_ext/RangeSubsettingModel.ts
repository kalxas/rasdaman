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

///<reference path="../../models/wcs/_wcs.ts"/>

module rasdaman {
    export class RangeSubsettingModel {
        private AvailableRanges:string[];
        private IsInterval:boolean[];

        public RangeSubset:wcs.RangeSubset;

        public constructor(coverageDescription:wcs.CoverageDescription) {
            this.RangeSubset = new wcs.RangeSubset();
            this.AvailableRanges = [];
            this.IsInterval = [];

            coverageDescription.RangeType.DataRecord.Field.forEach(field=> {
                this.AvailableRanges.push(field.Name);
            });
        }

        public addRangeComponent():void {
            this.RangeSubset.RangeItem.push(new wcs.RangeComponent(this.AvailableRanges[0]));
            this.IsInterval.push(false);
        }

        public addRangeComponentInterval():void {
            var begin = new wcs.RangeComponent(this.AvailableRanges[0]);
            var end = new wcs.RangeComponent(this.AvailableRanges[this.AvailableRanges.length - 1]);

            this.RangeSubset.RangeItem.push(new wcs.RangeInterval(begin, end));
            this.IsInterval.push(true);
        }

        public deleteRangeComponent(index:number):void {
            this.RangeSubset.RangeItem.splice(index, 1);
        }
    }
}