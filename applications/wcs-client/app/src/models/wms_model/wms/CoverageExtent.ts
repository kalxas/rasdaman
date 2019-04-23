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

/**
 * WMS already has the Ex_GeographicBoundingBox element to have XY bounding box in EPSG:4326.
 * However, the outcome of them should be the same object (CoverageExtent) and WMS needs to translate
 * EX_GeographicBoundingBox to CoverageExtent before WebWorldWind can process and load this bbox on globe.
 */
///<reference path="BBox.ts"/>

module wms {
    export class CoverageExtent {
        // NOTE: in WMS, a layer name is equivalent to a coverageId.
        public coverageId:string;
        public bbox:BBox;
        public displayFootprint:boolean;

        public constructor(coverageId, xmin, ymin, xmax, ymax) {            
            this.coverageId = coverageId;
            this.bbox = new BBox(xmin, ymin, xmax, ymax);
            // all layer in WMS is geo-referenced but not load footprint on globe by default
            this.displayFootprint = false;
        }        
    }
}
