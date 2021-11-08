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
 * Copyright 2003 - 2020 Peter Baumann /
 rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */

module wms {
    export class CreatePyramidMember implements rasdaman.common.ISerializable {
        public baseCoverageId:string;

        // e.g. scaleVector={2,2,1}
        public scaleFactors:string;
        public pyramidMemberCoverageId:string;

        public constructor(baseCoverageId:string, scaleFactors:string, pyramidMemberCoverageId:string,) {
            this.baseCoverageId = baseCoverageId;
            this.scaleFactors = scaleFactors;
            this.pyramidMemberCoverageId = pyramidMemberCoverageId;     
        }

        public toKVP():string {
            return "&COVERAGEID=" + this.baseCoverageId +
                "&MEMBER=" + this.pyramidMemberCoverageId +
                "&SCALEVECTOR=" + this.scaleFactors;
        }
    }
}
