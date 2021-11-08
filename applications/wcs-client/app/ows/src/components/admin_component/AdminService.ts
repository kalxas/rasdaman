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
/// <reference path="../../../assets/typings/tsd.d.ts"/>
/// <reference path="../../common/_common.ts"/>
/// <reference path="../../models/login/_login.ts"/>
/// <reference path="../wcs_component/settings/SettingsService.ts"/>

module rasdaman {
    export class AdminService {
        public static $inject = ["$http", "$q", "rasdaman.WCSSettingsService",                                
                                "rasdaman.CredentialService"];

        public constructor(private $http:angular.IHttpService,
                           private $q:angular.IQService,
                           private settings:rasdaman.WCSSettingsService,
                           private credentialService:rasdaman.CredentialService) {

        }
     
        // After login succesfully, persist petauser admin credentials to local storage to reuse
        public persitAdminUserCredentials = (credentials:login.Credential) => {            
            window.localStorage.setItem("petascopeAdminUserCredentials", credentials.toString());
        }

        // Mark as petascope admin user logged out
        public persitLoggedOut = () => {    
            window.localStorage.removeItem("petascopeAdminUserCredentials");
        }

        // Check if petascope admin user logged in
        public getPersistedAdminUserCredentials = ():login.Credential => {            
            let persistedCredentialsString = window.localStorage.getItem("petascopeAdminUserCredentials");
            if (persistedCredentialsString != null) {
                var credentials:login.Credential = login.Credential.fromString(persistedCredentialsString);
                return credentials;
            }

            // petascope admin user didn't login yet
            return null;
        }

        // Fetch the stored petascope admin user credentials from local storage
        // and create a basic authentication headers for them
        public getAuthentcationHeaders = ():{} => {
            var credentials:login.Credential = this.getPersistedAdminUserCredentials();
            var headers = this.credentialService.createBasicAuthenticationHeader(credentials.username, credentials.password);

            return headers;
        }

        // Login

        public login(inputCredentials:login.Credential):angular.IPromise<any> {
            var result = this.$q.defer();                                               
            var requestUrl = this.settings.contextPath + "/login";            
            var success = false;
            // send request to Petascope and get response (headers and contents)
            this.$http.get(requestUrl, {
                headers: this.credentialService.createBasicAuthenticationHeader(inputCredentials.username, inputCredentials.password)
            }).then(function (response:any) {
                if (response.data.includes("admin")) {
                    result.resolve(inputCredentials);
                } else {
                    result.reject("Given credentials are not valid for admin user.");
                }
            }, function (error) {
                result.reject(error);
            });

            return result.promise;
        }


        // OWS Metadata Management

        public updateServiceIdentification(serviceIdentification:admin.ServiceIdentification):angular.IPromise<any> {
            var result = this.$q.defer();                                               
            var requestUrl = this.settings.adminEndpoint + "/ows/serviceinfo";

            var credentials = this.getPersistedAdminUserCredentials();
            var requestHeaders = this.credentialService.createBasicAuthenticationHeader(credentials.username, credentials.password);

            var request:angular.IRequestConfig = {
                method: 'POST',
                url: requestUrl,
                //Removed the transformResponse to prevent angular from parsing non-JSON objects.
                transformResponse: null,                
                headers: requestHeaders,
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
            var requestUrl = this.settings.adminEndpoint + "/ows/serviceinfo";

            var credentials = this.getPersistedAdminUserCredentials();
            var requestHeaders = this.credentialService.createBasicAuthenticationHeader(credentials.username, credentials.password);

            var request:angular.IRequestConfig = {
                method: 'POST',
                url: requestUrl,
                //Removed the transformResponse to prevent angular from parsing non-JSON objects.
                transformResponse: null,        
                headers: requestHeaders,        
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
