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
/// <reference path="../../common/_common.ts"/>
/// <reference path="../../models/login/_login.ts"/>
/// <reference path="../wcs_component/settings/SettingsService.ts"/>

module rasdaman {
    export class AdminService {
        public static $inject = ["$http", "$q", "rasdaman.WCSSettingsService", "rasdaman.common.SerializedObjectFactory", "$window"];

        public constructor(private $http:angular.IHttpService,
                           private $q:angular.IQService,
                           private settings:rasdaman.WCSSettingsService,
                           private serializedObjectFactory:rasdaman.common.SerializedObjectFactory,
                           private $window:angular.IWindowService) {
        }


        // Login

        public login(credential:login.Credential):angular.IPromise<any> {
            var result = this.$q.defer();                                               
            var requestUrl = this.settings.wcsEndpoint + "/admin/Login";

            var request:angular.IRequestConfig = {
                method: 'POST',
                url: requestUrl,
                //Removed the transformResponse to prevent angular from parsing non-JSON objects.
                transformResponse: null,                
                headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                data: credential.toKVP()
            };

            // send request to Petascope and get response (headers and contents)
            this.$http(request).then(function (data:any) {
                result.resolve(data);
            }, function (error) {
                result.reject(error);
            });

            return result.promise;
        }


        // OWS Metadata Management

        public updateServiceIdentification(serviceIdentification:admin.ServiceIdentification):angular.IPromise<any> {
            var result = this.$q.defer();                                               
            var requestUrl = this.settings.wcsEndpoint + "/admin/UpdateServiceIdentification";

            var request:angular.IRequestConfig = {
                method: 'POST',
                url: requestUrl,
                //Removed the transformResponse to prevent angular from parsing non-JSON objects.
                transformResponse: null,                
                headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                // NOTE: Without this property value, Petascope will create new session for each request and the logged in user session doesn't exist -> invalid request.
                withCredentials: true,
                data: serviceIdentification.toKVP()
            };

            // send request to Petascope and get response (headers and contents)
            this.$http(request).then(function (data:any) {
                result.resolve(data);
            }, function (error) {
                result.reject(error);
            });

            return result.promise;
        }      
        
        public updateServiceProvider(serviceProvider:admin.ServiceProvider):angular.IPromise<any> {
            var result = this.$q.defer();                                               
            var requestUrl = this.settings.wcsEndpoint + "/admin/UpdateServiceProvider";

            var request:angular.IRequestConfig = {
                method: 'POST',
                url: requestUrl,
                //Removed the transformResponse to prevent angular from parsing non-JSON objects.
                transformResponse: null,        
                headers: {'Content-Type': 'application/x-www-form-urlencoded'},        
                // NOTE: Without this property value, Petascope will create new session for each request and the logged in user session doesn't exist -> invalid request.
                withCredentials: true,
                data: serviceProvider.toKVP()
            };

            // send request to Petascope and get response (headers and contents)
            this.$http(request).then(function (data:any) {
                result.resolve(data);
            }, function (error) {
                result.reject(error);
            });

            return result.promise;
        }  
    }
}
