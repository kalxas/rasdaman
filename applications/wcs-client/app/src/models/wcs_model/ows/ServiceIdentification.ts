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
///<reference path="Description.ts"/>
///<reference path="Code.ts"/>

module ows {
    export class ServiceIdentification extends Description {
        public serviceType:Code;
        public serviceTypeVersion:string[];
        public profile:string[];
        public fees:string;
        public accessConstraints:string;

        public constructor(source:rasdaman.common.ISerializedObject) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");

            super(source);

            this.serviceType = new Code(source.getChildAsSerializedObject("ServiceType"));

            this.serviceTypeVersion = [];
            source.getChildrenAsSerializedObjects("ows:ServiceTypeVersion").forEach(s=> {
                this.serviceTypeVersion.push(s.getValueAsString());
            });

            this.profile = [];
            source.getChildrenAsSerializedObjects("ows:Profile").forEach(s=> {
                this.profile.push(s.getValueAsString());
            });

            if (source.doesElementExist("ows:Fees")) {
                this.fees = source.getChildAsSerializedObject("ows:Fees").getValueAsString();
            }

            if (source.doesElementExist("ows:AccessConstraints")) {
                this.accessConstraints = source.getChildAsSerializedObject("ows:AccessConstraints").getValueAsString();
            }
        }
    }
}