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

///<reference path="../../../common/_common.ts"/>
///<reference path="../ows/ows_all.ts"/>
///<reference path="CoverageSubtypeParent.ts"/>

module wcs {
    export class CoverageSummary extends ows.Description {
        public coverageId:string;
        public coverageSubtype:string;
        public coverageSubtypeParent:CoverageSubtypeParent;
        public wgs84BoundingBox:ows.WGS84BoundingBox;
        public boundingBox:ows.BoundingBox;
        public displayFootprint:boolean;

        public constructor(source:rasdaman.common.ISerializedObject) {
            super(source);

            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            // Don't display checkbox to show/hide coverages's footprints if they are not displayable.
            this.displayFootprint = null;

            this.coverageId = source.getChildAsSerializedObject("wcs:CoverageId").getValueAsString();
            this.coverageSubtype = source.getChildAsSerializedObject("wcs:CoverageSubtype").getValueAsString();

            let childElement = "wcs:CoverageSubtypeParent";
            if (source.doesElementExist(childElement)) {
                this.coverageSubtypeParent = new CoverageSubtypeParent(source.getChildAsSerializedObject(childElement));
            }

            childElement = "ows:WGS84BoundingBox";
            // optional parameter if coverage's CRS can project to EPSG:4326
            if (source.doesElementExist(childElement)) {
                this.wgs84BoundingBox = new ows.WGS84BoundingBox(source.getChildAsSerializedObject(childElement));
            }

            childElement = "ows:BoundingBox";
            if (source.doesElementExist(childElement)) {
                this.boundingBox = new ows.BoundingBox(source.getChildAsSerializedObject(childElement));
            }
        }
    }
}