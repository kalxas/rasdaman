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

module ows {
    export class Address {
        public DeliveryPoint:string[];
        public City:string;
        public AdministrativeArea:string;
        public PostalCode:string;
        public Country:string;
        public ElectronicMailAddress:string[];

        public constructor(source:rasdaman.common.ISerializedObject) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");

            this.DeliveryPoint = [];
            source.getChildrenAsSerializedObjects("ows:DeliveryPoint").forEach(o=> {
                this.DeliveryPoint.push(o.getValueAsString());
            });

            if (source.doesElementExist("ows:City")) {
                this.City = source.getChildAsSerializedObject("ows:City").getValueAsString();
            }

            if (source.doesElementExist("ows:AdministrativeArea")) {
                this.AdministrativeArea = source.getChildAsSerializedObject("ows:AdministrativeArea").getValueAsString();
            }

            if (source.doesElementExist("ows:PostalCode")) {
                this.PostalCode = source.getChildAsSerializedObject("ows:PostalCode").getValueAsString();
            }

            if (source.doesElementExist("ows:Country")) {
                this.Country = source.getChildAsSerializedObject("ows:Country").getValueAsString();
            }

            this.ElectronicMailAddress = [];
            source.getChildrenAsSerializedObjects("ows:ElectronicMailAddress").forEach(o=> {
                this.ElectronicMailAddress.push(o.getValueAsString());
            });
        }
    }
}