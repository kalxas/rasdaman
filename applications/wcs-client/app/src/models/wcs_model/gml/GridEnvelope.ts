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

module gml {

    /*
    e.g: 
    <gml:GridEnvelope>
        <gml:low>0 0 0</gml:low>
        <gml:high>3 62 35</gml:high>
    </gml:GridEnvelope>
    */
    export class GridEnvelope {

        public gridLows:string[]
        public gridHighs:string[]

        public constructor(source:rasdaman.common.ISerializedObject) {            
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");

            let obj = source.getChildAsSerializedObject("gml:GridEnvelope");
            this.gridLows = obj.getChildAsSerializedObject("low").getValueAsString().split(" ");
            this.gridHighs = obj.getChildAsSerializedObject("high").getValueAsString().split(" ");
        }
    }
}