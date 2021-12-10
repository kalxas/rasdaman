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
        public static $inject = ["$rootScope", "$http", "$q", "rasdaman.WCSSettingsService",                                
                                "rasdaman.CredentialService"];

        public static RW_RIGHTS_COMMUNITY:string = "RW";

        public static PRIV_OWS_UPDATE_SRV:string = "PRIV_OWS_UPDATE_SRV";
        public static PRIV_OWS_WCS_INSERT_COV:string = "PRIV_OWS_WCS_INSERT_COV";
        public static PRIV_OWS_WCS_UPDATE_COV:string = "PRIV_OWS_WCS_UPDATE_COV";
        public static PRIV_OWS_WCS_DELETE_COV:string = "PRIV_OWS_WCS_DELETE_COV";

        public static PRIV_OWS_WMS_INSERT_LAYER:string = "PRIV_OWS_WMS_INSERT_LAYER";
        public static PRIV_OWS_WMS_UPDATE_LAYER:string = "PRIV_OWS_WMS_UPDATE_LAYER";
        public static PRIV_OWS_WMS_DELETE_LAYER:string = "PRIV_OWS_WMS_DELETE_LAYER";

        public static PRIV_OWS_WMS_INSERT_STYLE:string = "PRIV_OWS_WMS_INSERT_STYLE";
        public static PRIV_OWS_WMS_UPDATE_STYLE:string = "PRIV_OWS_WMS_UPDATE_STYLE";
        public static PRIV_OWS_WMS_DELETE_STYLE:string = "PRIV_OWS_WMS_DELETE_STYLE";

        public static PRIV_OWS_WCS_BLACKWHITELIST_COV = "PRIV_OWS_WCS_BLACKWHITELIST_COV";
        public static PRIV_OWS_WMS_BLACKWHITELIST_LAYER = "PRIV_OWS_WMS_BLACKWHITELIST_LAYER";


        public static adminRoles = [AdminService.PRIV_OWS_UPDATE_SRV, AdminService.PRIV_OWS_WCS_INSERT_COV, AdminService.PRIV_OWS_WCS_UPDATE_COV, AdminService.PRIV_OWS_WCS_DELETE_COV,
                        AdminService.PRIV_OWS_WMS_INSERT_LAYER, AdminService.PRIV_OWS_WMS_UPDATE_LAYER, AdminService.PRIV_OWS_WMS_DELETE_LAYER,
                        AdminService.PRIV_OWS_WMS_INSERT_STYLE, AdminService.PRIV_OWS_WMS_UPDATE_STYLE, AdminService.PRIV_OWS_WMS_DELETE_STYLE,
                        AdminService.PRIV_OWS_WCS_BLACKWHITELIST_COV, AdminService.PRIV_OWS_WMS_BLACKWHITELIST_LAYER,
                        AdminService.RW_RIGHTS_COMMUNITY // -- special rights for community
        ];                                

        public constructor(private $rootScope:angular.IRootScopeService,
                           private $http:angular.IHttpService,
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
                // NOTE: petauser was petascope admin user and since v10+, 
                // it has no use anymore in petascope (user doesn't exist in rasdaman)
                //  and user must login with different user in admin's tab
                if (credentials["username"] == "petauser") {
                    this.persitLoggedOut();
                    return null;
                }

                return credentials;
            }

            // petascope admin user didn't login yet
            return null;
        }

        // Fetch the stored petascope admin user credentials from local storage
        // and create a basic authentication headers for them
        public getAuthenticationHeaders = ():{} => {
            var credentials:login.Credential = this.getPersistedAdminUserCredentials();
            var headers = {};
            if (credentials != null) {
                headers = this.credentialService.createBasicAuthenticationHeader(credentials.username, credentials.password);
            }

            return headers;
        }

        // Login

        public login(inputCredentials:login.Credential):angular.IPromise<any> {
            var result = this.$q.defer();                                               
            var requestUrl = this.settings.contextPath + "/login";            
            var success = false;

            let adminRolesObj = AdminService.adminRoles;
            
            // send request to Petascope and get response (headers and contents)
            this.$http.get(requestUrl, {
                headers: this.credentialService.createBasicAuthenticationHeader(inputCredentials.username, inputCredentials.password)
            }).then(function (response:any) {
                let roles = response.data.split(",");
                let hasAdminRole = false;

                for (let i = 0; i < adminRolesObj.length; i++){
                    let adminRole = adminRolesObj[i];
                    if (roles.includes(adminRole)) {
                        hasAdminRole = true;
                        break;
                    }
                }

                if (hasAdminRole) {                    
                    result.resolve(roles);
                } else {
                    result.reject("Given credentials are not valid for an admin user with granted adequate roles.");
                }
            }, function (error) {
                result.reject(error);
            });

            return result.promise;
        }

        /**
            Check from the list of granted roles of an admin user, if a given role exists
         */
        public static hasRole(roles:any[], role:string) {
            return roles.includes(AdminService.RW_RIGHTS_COMMUNITY)
               || roles.includes(role);
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
