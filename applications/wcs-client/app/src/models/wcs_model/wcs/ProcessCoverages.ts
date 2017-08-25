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

///<reference path="RequestBase.ts"/>
///<reference path="../../../common/_common.ts"/>

module wcs {
    export class ProcessCoverages extends RequestBase {
        public request:string;
        public query:string;
        public extraParameters:string[];

        public constructor(query:string, extraParams:string[]) {
            super();
            rasdaman.common.ArgumentValidator.isNotNull(query, "query");
            rasdaman.common.ArgumentValidator.isNotNull(extraParams, "extraParams");
            rasdaman.common.ArgumentValidator.isArray(extraParams, "extraParams");

            this.request = "ProcessCoverages";
            this.query = query;
            this.extraParameters = angular.copy(extraParams);
        }


        public toKVP():string {
            var serializedParams = "";
            for (var i = 0; i < this.extraParameters.length; ++i) {
                serializedParams += ("&" + i + "=" + encodeURI(this.extraParameters[i]));
            }

            // Fix the serialization super.toKVP() +
            return "&REQUEST=" + this.request
                + "&QUERY=" + encodeURI(this.query)
                + serializedParams;
        }
    }
}