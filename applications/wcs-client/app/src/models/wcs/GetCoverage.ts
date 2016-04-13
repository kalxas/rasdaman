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

///<reference path="RequestBase.ts"/>
///<reference path="DimensionSubset.ts"/>
///<reference path="RangeSubset.ts"/>
///<reference path="Scaling.ts"/>

module wcs {
    export class GetCoverage extends RequestBase {
        public CoverageId:string;
        public DimensionSubset:DimensionSubset[];
        public Format:string;
        public MediaType:boolean;

        //Find a more elegant way to extend GetCoverage with these members.
        public RangeSubset:wcs.RangeSubset;
        public Scaling:wcs.Scaling;
        public Interpolation:wcs.Interpolation;

        public constructor(coverageId:string, dimensionSubset:DimensionSubset[], format?:string, mediaType?:boolean) {
            super();

            this.CoverageId = coverageId;

            this.DimensionSubset = [];
            dimensionSubset.forEach(o=> {
                this.DimensionSubset.push(o);
            });

            this.Format = format;
            this.MediaType = mediaType;
        }

        public toKVP():string {
            var serialization:string = super.toKVP();
            serialization += "&REQUEST=GetCoverage";
            serialization += "&COVERAGEID=" + this.CoverageId;

            this.DimensionSubset.forEach((subset:DimensionSubset)=> {
                serialization += "&SUBSET=" + subset.toKVP();
            });

            if (this.RangeSubset) {
                serialization += this.RangeSubset.toKVP();
            }

            if (this.Scaling) {
                serialization += this.Scaling.toKVP();
            }

            if (this.Interpolation) {
                serialization += this.Interpolation.toKVP();
            }

            if (this.Format) {
                serialization += "&FORMAT=" + this.Format;
            }

            if (this.MediaType) {
                serialization += "&MEDIATYPE=multipart/related";
            }

            return serialization;
        }
    }
}