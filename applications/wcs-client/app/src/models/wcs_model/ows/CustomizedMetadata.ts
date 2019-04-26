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
 * Copyright 2003 - 2019 Peter Baumann /
 rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */

///<reference path="../../../common/_common.ts"/>

module ows {
    // Used only for result of WCS GetCapabilities (not coverage's real metadata)
    export class CustomizedMetadata {
        public hostname:String;
        public petascopeEndPoint:String;

        public constructor(source:rasdaman.common.ISerializedObject) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");

            this.parseCoverageLocation(source);
        }

        /**
         If in customized metadata it exists, then, get hostname and endpoint from location element.

        <rasdaman:location>
            <rasdaman:hostname>locahost</rasdaman:hostname>
            <rasdaman:endpoint>http://localhost:8080/rasdaman/ows</rasdaman:endpoint>
        </rasdaman:location>
          */        
        private parseCoverageLocation(source:rasdaman.common.ISerializedObject):void {
            let childElement = "rasdaman:location";
            if (source.doesElementExist(childElement)) {
                let locationElement = source.getChildAsSerializedObject(childElement);
                this.hostname = locationElement.getChildAsSerializedObject("rasdaman:hostname").getValueAsString();
                this.petascopeEndPoint = locationElement.getChildAsSerializedObject("rasdaman:endpoint").getValueAsString();
            }
        }
    }
}