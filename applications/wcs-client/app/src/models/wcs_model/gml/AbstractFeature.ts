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

///<reference path="BoundedBy.ts"/>

module gml {
    /**
     * Extend this class so that it fully complies with the OGC GML specification if the need arises.
     */
    export class AbstractFeature {
        public id:string;
        public description:string;
        public descriptionReference:any;
        public identifier:string;
        public name:string[];
        public boundedBy:gml.BoundedBy;

        public constructor(source:rasdaman.common.ISerializedObject) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");

            this.id = source.getAttributeAsString("gml:id");

            if (source.doesElementExist("gml:description")) {
                this.description = source.getChildAsSerializedObject("gml:description").getValueAsString();
            }

            if (source.doesElementExist("gml:identifier")) {
                this.identifier = source.getChildAsSerializedObject("gml:identifier").getValueAsString();
            }

            this.name = [];
            source.getChildrenAsSerializedObjects("gml:name").forEach(o=> {
                this.name.push(o.getValueAsString());
            });

            if (source.doesElementExist("gml:boundedBy")) {
                this.boundedBy = new gml.BoundedBy(source.getChildAsSerializedObject("gml:boundedBy"));
            }
        }
    }
}