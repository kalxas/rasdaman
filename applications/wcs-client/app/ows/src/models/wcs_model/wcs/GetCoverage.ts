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

///<reference path="RequestBase.ts"/>
///<reference path="DimensionSubset.ts"/>
///<reference path="RangeSubset.ts"/>
///<reference path="Scaling.ts"/>

module wcs {
    export class GetCoverage extends RequestBase {
        public coverageId:string;
        public dimensionSubset:DimensionSubset[];
        public format:string;
        public mediaType:boolean;

        //Find a more elegant way to extend GetCoverage with these members.
        public rangeSubset:wcs.RangeSubset;
        public scaling:wcs.Scaling;
        public interpolation:wcs.Interpolation;
        public crs:wcs.CRS;
        public clipping:wcs.Clipping;

        public constructor(coverageId:string, dimensionSubset:DimensionSubset[], format?:string, mediaType?:boolean) {
            super();

            this.coverageId = coverageId;

            this.dimensionSubset = [];
            dimensionSubset.forEach(o=> {
                this.dimensionSubset.push(o);
            });

            this.format = format;
            this.mediaType = mediaType;
        }

        public toKVP():string {
            var serialization:string = super.toKVP();
            serialization += "&REQUEST=GetCoverage";
            serialization += "&COVERAGEID=" + this.coverageId;

            this.dimensionSubset.forEach((subset:DimensionSubset)=> {
                serialization += "&SUBSET=" + subset.toKVP();
            });

            if (this.rangeSubset) {
                serialization += this.rangeSubset.toKVP();
            }

            if (this.scaling) {
                serialization += this.scaling.toKVP();
            }

            if (this.interpolation) {
                serialization += this.interpolation.toKVP();
            }

            if (this.crs) {
                serialization += this.crs.toKVP();
            }

            if (this.clipping) {
                serialization += this.clipping.toKVP();
            }

            if (this.format) {
                serialization += "&FORMAT=" + this.format;
            }

            if (this.mediaType) {
                serialization += "&MEDIATYPE=multipart/related";
            }

            return serialization;
        }
    }
}