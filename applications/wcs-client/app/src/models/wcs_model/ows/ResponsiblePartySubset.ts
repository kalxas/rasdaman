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
///<reference path="ContactInfo.ts"/>
///<reference path="Code.ts"/>

module ows {
    export class ResponsiblePartySubset {
        public individualName:string;
        public positionName:string;
        public contactInfo:ContactInfo;
        public role:Code;

        public constructor(source:rasdaman.common.ISerializedObject) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");

            if (source.doesElementExist("ows:IndividualName")) {
                this.individualName = source.getChildAsSerializedObject("ows:IndividualName").getValueAsString();
            }

            if (source.doesElementExist("ows:PositionName")) {
                this.positionName = source.getChildAsSerializedObject("ows:PositionName").getValueAsString();
            }

            if (source.doesElementExist("ows:Role")) {
                this.role = new Code(source.getChildAsSerializedObject("ows:Role"));
            }

            if (source.doesElementExist("ows:ContactInfo")) {
                this.contactInfo = new ContactInfo(source.getChildAsSerializedObject("ows:ContactInfo"));
            }
        }
    }
}