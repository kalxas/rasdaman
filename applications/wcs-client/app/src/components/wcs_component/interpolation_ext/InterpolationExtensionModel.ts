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
///<reference path="../../../models/wcs_model/wcs/_wcs.ts"/>

module rasdaman {
    export class WCSInterpolationExtensionModel {
        public selectedInterpolationMethod:InterpolationMethod;
        public availableInterpolationMethods:InterpolationMethod[];

        public constructor(serverCapabilities:wcs.Capabilities) {
            this.availableInterpolationMethods = [];

            for (var i = 0; i < serverCapabilities.serviceMetadata.extension.length; ++i) {
                if (serverCapabilities.serviceMetadata.extension[i].interpolationMetadata) {
                    var arr = serverCapabilities.serviceMetadata.extension[i].interpolationMetadata.interpolationSupported;
                    for (var j = 0; j <arr.length; j++) {
                        var interpolationUri = arr[j];
                        this.availableInterpolationMethods.push({name: interpolationUri, uri: interpolationUri});
                    }                    
                }
            }
        }

        public getInterpolation():wcs.Interpolation {
            var interpolationUri = "";
            if (this.selectedInterpolationMethod) {
                interpolationUri = this.selectedInterpolationMethod.uri;
            }

            return new wcs.Interpolation(interpolationUri);
        }
    }

    interface InterpolationMethod {
        name:string;
        uri:string;
    }
}