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
/// <reference path="../../../../assets/typings/tsd.d.ts"/>
///<reference path="../../../common/_common.ts"/>

module rasdaman {
    export class WMSSettingsService {
        public wmsEndpoint:string;
        public wmsServiceNameVersion:string; 
        public wmsFullEndpoint:string;
        public static $inject = ["$window"];
        public static version:string = "1.3.0";

        public contextPath:string; 
        public adminEndpoint:string;

        public constructor($window:angular.IWindowService) {
            // e.g: http://localhost:8080/rasdaman/ows
            this.wmsEndpoint = $window.location.href.replace("wcs-client/index.html", "ows");
            // In case of only WSClient is deployed to external web server (e.g: Tomcat) and this Tomcat has Petascope
            // This is used for development only.
            this.wmsEndpoint = this.wmsEndpoint.replace("wcs-client/app/", "rasdaman/ows");

            // #TESTING
            this.wmsEndpoint = "http://localhost:8080/rasdaman/ows";
                                   
            this.setWMSEndPoint(this.wmsEndpoint);
            
            // e.g: service=WMS&version=1.3.0
            this.wmsServiceNameVersion = "service=WMS&version=" + WMSSettingsService.version;
            // e.g: http://localhost:8080/rasdaman/ows?service=WMS&version=1.3.0
            this.setWMSFullEndPoint();
        }

        public setWMSFullEndPoint() {
            this.wmsFullEndpoint = this.wmsEndpoint + "?" + this.wmsServiceNameVersion;
        }

        public setWMSEndPoint(petascopeEndPoint) {
            this.wmsEndpoint = petascopeEndPoint;
            this.wmsEndpoint = this.wmsEndpoint.split("#")[0];            

            if (!this.wmsEndpoint.endsWith("ows")) {
                this.wmsEndpoint = this.wmsEndpoint + "ows";
            }

            this.contextPath = this.wmsEndpoint.replace("/rasdaman/ows", "/rasdaman");
            this.adminEndpoint = this.contextPath + "/admin";
        }
    }
}
