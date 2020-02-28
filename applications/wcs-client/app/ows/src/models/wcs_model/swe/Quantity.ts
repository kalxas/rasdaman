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

///<reference path="NilValuesWrapper.ts"/>
///<reference path="Uom.ts"/>

/**
 * e.g:
 * 
<swe:Quantity>
    <swe:label>Gray</swe:label>
    <swe:description/>
    <swe:nilValues>
        <swe:NilValues>
            <swe:nilValue reason="">9999</swe:nilValue>
        </swe:NilValues>
    </swe:nilValues>
    <swe:uom code="10^0"/>
    <swe:constraint/>
</swe:Quantity>
 */
module swe {
    export class Quantity {
        public nilValuesWrapper:NilValuesWrapper;
        public uom:swe.Uom;

        public constructor(source:rasdaman.common.ISerializedObject) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");

            if (source.doesElementExist("swe:nilValues")) {
                this.nilValuesWrapper = new NilValuesWrapper(source.getChildAsSerializedObject("swe:nilValues"));
            }

            if (source.doesElementExist("swe:uom")) {
                this.uom = new Uom(source.getChildAsSerializedObject("swe:uom"));
            }
        }
    }
}