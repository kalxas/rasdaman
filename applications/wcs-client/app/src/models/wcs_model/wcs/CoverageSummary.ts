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
        public wgs84BoundingBox:ows.WGS84BoundingBox[];
        public boundingBox:ows.BoundingBox[];
        public metadata:ows.Metadata[];
        public displayFootprint:boolean;

        public constructor(source:rasdaman.common.ISerializedObject) {
            super(source);

            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            // Don't display checkbox to show/hide coverages's footprints if they are not displayable.
            this.displayFootprint = null;

            this.coverageId = source.getChildAsSerializedObject("wcs:CoverageId").getValueAsString();
            this.coverageSubtype = source.getChildAsSerializedObject("wcs:CoverageSubtype").getValueAsString();

            if (source.doesElementExist("wcs:CoverageSubtypeParent")) {
                this.coverageSubtypeParent = new CoverageSubtypeParent(source.getChildAsSerializedObject("wcs:CoverageSubtypeParent"));
            }

            this.wgs84BoundingBox = [];
            source.getChildrenAsSerializedObjects("ows:WGS84BoundingBox").forEach(o=> {
                this.wgs84BoundingBox.push(new ows.WGS84BoundingBox(o));
            });

            this.boundingBox = [];
            source.getChildrenAsSerializedObjects("ows:BoundingBox").forEach(o=> {
                this.boundingBox.push(new ows.BoundingBox(o));
            });

            this.metadata = [];
            source.getChildrenAsSerializedObjects("ows:Metadata").forEach(o=> {
                this.metadata.push(new ows.Metadata(o));
            });
        }
    }
}