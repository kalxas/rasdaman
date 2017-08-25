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

/**
 * Extend this class so that it fully complies with the OGC WMS specification if the need arises.
 */
module wms {
    export class GetCapabilities implements rasdaman.common.ISerializable {
        public Service:string;
        public Request:string;
        public version:string[];                

        public constructor() {            
            this.Service = "WMS";
            this.version = ["1.3.0"];
        }

        public toKVP():string {
            return "&service=" + this.Service +
                "&version=" + this.version[0] +
                "&request=" + "GetCapabilities";
        }
    }
}
