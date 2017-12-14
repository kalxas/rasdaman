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

///<reference path="../ows/ows_all.ts"/>

/**
 * Extend this class so that it fully complies with the OGC WCS specification if the need arises.
 */
module wcs {
    export class GetCapabilities extends ows.GetCapabilities implements rasdaman.common.ISerializable {
        public constructor() {
            super();

            this.service = "WCS";
            this.acceptVersions = ["2.0.1"];
        }

        public toKVP():string {
            return "&SERVICE=" + this.service +
                "&ACCEPTVERSIONS=" + this.acceptVersions[0] +
                "&REQUEST=" + this.request;
        }
    }
}
