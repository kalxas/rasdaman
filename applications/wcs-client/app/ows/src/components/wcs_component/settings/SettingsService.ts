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
    export class WCSSettingsService {
        public wcsEndpoint:string;
	    public wcsServiceNameVersion:string; 
        public static $inject = ["$window"];
        // context path set at the login form
        public contextPath:string;  

        // context path of the petascope requested to download WSClient
        public defaultContextPath:string;

        public wcsFullEndpoint:string;

        public constructor($window:angular.IWindowService) {  
            // In case of Petascope is deployed to an URI without /rasdaman/ows
            this.wcsEndpoint = $window.location.href.replace("wcs-client/index.html", "ows");           
            
            // In case of only WSClient is deployed to external web server (e.g: Tomcat) and this Tomcat has Petascope
            // This is used for development only.
            this.wcsEndpoint = this.wcsEndpoint.replace("wcs-client/app/", "rasdaman/ows");
            this.wcsServiceNameVersion = "SERVICE=WCS&VERSION=2.0.1";

            this.setWCSEndPoint(this.wcsEndpoint);
            this.defaultContextPath = this.contextPath;      
            
            this.wcsFullEndpoint = this.wcsEndpoint + "?" + this.wcsServiceNameVersion;
        }

        public setWCSEndPoint(petascopeEndPoint) {
            this.wcsEndpoint = petascopeEndPoint;
            this.wcsEndpoint = this.wcsEndpoint.split("#")[0];
            
            if (!this.wcsEndpoint.endsWith("ows")) {
                this.wcsEndpoint = this.wcsEndpoint + "ows";
            }
            
            this.contextPath = this.wcsEndpoint.replace("/rasdaman/ows", "/rasdaman");
        }
    }
}
