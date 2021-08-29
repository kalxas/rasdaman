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

/**
 * e.g: <swe:nilValue reason="http://www.opengis.net/def/nil/OGC/0/BelowDetectionRange">-INF</swe:nilValue>
 */
module swe {
    export class NilValue {
        public reason:string;
        public value:string;

        public constructor(source:rasdaman.common.ISerializedObject) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");

            let elements = source.getChildrenAsSerializedObjects("swe:nilValue");
            let reasons = [];
            let values = [];

            elements.forEach(element => {
                if (element.doesAttributeExist("reason")) {
                    let reasonTmp = element.getAttributeAsString("reason");
                    reasons.push(reasonTmp);
                }
                
                let valueTmp = element.getValueAsString();
                values.push(valueTmp);
            });

            this.reason = reasons.join(", ");
            this.value = values.join(", ");
        }
    }
}
