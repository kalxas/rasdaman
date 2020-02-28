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
///<reference path="NilValues.ts"/>

/**
 e.g:
 
 <swe:nilValues>
    <swe:NilValues>
        <swe:nilValue reason="http://www.opengis.net/def/nil/OGC/0/BelowDetectionRange">-INF</swe:nilValue>
        <swe:nilValue reason="http://www.opengis.net/def/nil/OGC/0/AboveDetectionRange">INF</swe:nilValue>
    </swe:NilValues>
</swe:nilValues>
 */
module swe {
    export class NilValuesWrapper {
        public nilValues:NilValues;

        public constructor(source:rasdaman.common.ISerializedObject) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
            this.nilValues = new NilValues(source);
        }

        /** 
         * Get all avaiable null values of this band and concatenate as a string
        */
        public getNullValues():String {
            let values = [];

            this.nilValues.nilValues.forEach(obj => {
                values.push(obj.value);
            });

            let result = values.join(", ");
            return result;
        }
    }
}