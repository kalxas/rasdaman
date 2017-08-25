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
///<reference path="WCPSQueryResult.ts"/>

module rasdaman {
    export class ImageWCPSResult extends WCPSQueryResult {
        public base64ImageData:string;
        public imageType:string;

        public constructor(command:WCPSCommand, rawImageData:ArrayBuffer) {
            super(command);
            this.base64ImageData = rasdaman.common.ImageUtilities.arrayBufferToBase64(rawImageData);
            this.imageType = (command.query.search(/jpeg/g) === -1 ? "image/png" : "image/jpeg");
        }
    }
}
