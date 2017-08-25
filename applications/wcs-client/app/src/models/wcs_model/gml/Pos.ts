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

module gml {
    /**
     * Extend this class so that it fully complies with the OGC GML specification if the need arises.
     */
    export class Pos {
        public srsName:string;
        public srsDimension:number;
        public axisLabels:string[];
        public uomLabels:string[];
        public values:number[];

        public constructor(source:rasdaman.common.ISerializedObject) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");

            if (source.doesAttributeExist("srsName")) {
                this.srsName = source.getAttributeAsString("srsName");
            }

            if (source.doesAttributeExist("srsDimension")) {
                this.srsDimension = source.getAttributeAsNumber("srsDimension");
            }

            if (source.doesAttributeExist("axisLabels")) {
                this.axisLabels = source.getAttributeAsString("axisLabels").split(" ");
            }

            if (source.doesAttributeExist("uomLabels")) {
                this.uomLabels = source.getAttributeAsString("uomLabels").split(" ");
            }

            this.values = [];
            var stringValues:string[] = source.getValueAsString().split(" ");
            stringValues.forEach(o=> {
                this.values.push(parseFloat(o));
            });
        }
    }
}