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

///<reference path="../../common/_common.ts"/>

///<reference path="BoundedBy.ts"/>

module gml {
    /**
     * Extend this class so that it fully complies with the OGC GML specification if the need arises.
     */
    export class AbstractFeature {
        public Id:string;
        public Description:string;
        public DescriptionReference:any;
        public Identifier:string;
        public Name:string[];
        public BoundedBy:gml.BoundedBy;

        public constructor(source:rasdaman.common.ISerializedObject) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");

            this.Id = source.getAttributeAsString("gml:id");

            if (source.doesElementExist("gml:description")) {
                this.Description = source.getChildAsSerializedObject("gml:description").getValueAsString();
            }

            if (source.doesElementExist("gml:identifier")) {
                this.Identifier = source.getChildAsSerializedObject("gml:identifier").getValueAsString();
            }

            this.Name = [];
            source.getChildrenAsSerializedObjects("gml:name").forEach(o=> {
                this.Name.push(o.getValueAsString());
            });

            if (source.doesElementExist("gml:boundedBy")) {
                this.BoundedBy = new gml.BoundedBy(source.getChildAsSerializedObject("gml:boundedBy"));
            }
        }
    }
}