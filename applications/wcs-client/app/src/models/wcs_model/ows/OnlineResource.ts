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

module ows {
    export class OnlineResource {
        public actuate:string;
        public acrole:string;
        public href:string;
        public role:string;
        public show:string;
        public title:string;
        public type:string;

        public constructor(source:rasdaman.common.ISerializedObject) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");

            if (source.doesAttributeExist("xlink:actuate")) {
                this.actuate = source.getAttributeAsString("xlink:actuate");
            }

            if (source.doesAttributeExist("xlink:acrole")) {
                this.acrole = source.getAttributeAsString("xlink:acrole");
            }

            if (source.doesAttributeExist("xlink:href")) {
                this.href = source.getAttributeAsString("xlink:href");
            }

            if (source.doesAttributeExist("xlink:role")) {
                this.role = source.getAttributeAsString("xlink:role");
            }

            if (source.doesAttributeExist("xlink:show")) {
                this.show = source.getAttributeAsString("xlink:show");
            }

            if (source.doesAttributeExist("xlink:title")) {
                this.title = source.getAttributeAsString("xlink:title");
            }

            if (source.doesAttributeExist("xlink:type")) {
                this.type = source.getAttributeAsString("xlink:type");
            }
        }
    }
}