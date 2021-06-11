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
/// <reference path="../../models/wms_model/wms/_wms.ts"/>
/// <reference path="../wms_component/settings/SettingsService.ts"/>

module rasdaman {
    export class WMSService {
        public static $inject = ["$http", "$q", "rasdaman.WMSSettingsService", "rasdaman.WCSSettingsService", 
                                 "rasdaman.common.SerializedObjectFactory", "$window",
                                 "rasdaman.CredentialService",
                                 "rasdaman.AdminService"];

        public constructor(private $http:angular.IHttpService,
                           private $q:angular.IQService,
                           private settings:rasdaman.WMSSettingsService,
                           private wcsSettings:rasdaman.WCSSettingsService,
                           private serializedObjectFactory:rasdaman.common.SerializedObjectFactory,
                           private $window:angular.IWindowService,
                           private credentialService:rasdaman.CredentialService,
                           private adminService:rasdaman.AdminService) {
        }


        public getServerCapabilities(request:wms.GetCapabilities):angular.IPromise<any> {
            var result = this.$q.defer();
            var self = this;

            var requestHeaders = {};
            var credentials:login.Credential = this.adminService.getPersistedAdminUserCredentials();
            if (credentials != null) {
                // If petascope admin user logged in, then use its credentials for GetCapabilities intead to view blacklisted coverages
                requestHeaders = this.adminService.getAuthentcationHeaders();
            } else {
                requestHeaders = this.credentialService.createRequestHeader(this.settings.wmsEndpoint, {});
            }

            var requestUrl = this.settings.wmsFullEndpoint + "&" + request.toKVP();
            this.$http.get(requestUrl, {
                    headers: requestHeaders
                }).then(function (data:any) {
                    try {
                        var gmlDocument = new rasdaman.common.ResponseDocument(data.data, rasdaman.common.ResponseDocumentType.XML);
                        var serializedResponse = self.serializedObjectFactory.getSerializedObject(gmlDocument);
                        var capabilities = new wms.Capabilities(serializedResponse, gmlDocument.value);                       

                        var response = new rasdaman.common.Response<wms.Capabilities>(gmlDocument, capabilities);
                        result.resolve(response);
                    } catch (err) {
                        result.reject(err);
                    }
                }, function (error) {
                    result.reject(error);
                });

            return result.promise;
        }      

        /**
         *
         * @param query wcs.ProcessCoverages query that will be serialized and sent to the server.
         * @returns {IPromise<T>}
         */
        public updateLayerStyleRequest(updateLayerStyle:wms.UpdateLayerStyle):angular.IPromise<any> {
            var result = this.$q.defer();                                               
            var requestUrl = this.settings.wmsEndpoint;
            var currentHeaders = {"Content-Type": "application/x-www-form-urlencoded"};

            var request:angular.IRequestConfig = {
                method: 'POST',
                url: requestUrl,
                //Removed the transformResponse to prevent angular from parsing non-JSON objects.
                transformResponse: null,
                headers: this.credentialService.createRequestHeader(this.settings.wmsEndpoint, currentHeaders),
                data: this.settings.wmsServiceNameVersion + "&" + updateLayerStyle.toKVP()
            };

            // send request to Petascope and get response (headers and contents)
            this.$http(request).then(function (data:any) {
                result.resolve(data);
            }, function (error) {
                result.reject(error);
            });

            return result.promise;
        }        
     
        // ******** Layer's style management ********

        // Insert the specified style's data to databasee
        public insertLayerStyleRequest(insertLayerStyle:wms.InsertLayerStyle):angular.IPromise<any> {
            var result = this.$q.defer();                                               
            var requestUrl = this.settings.wmsEndpoint;
            var currentHeaders = {"Content-Type": "application/x-www-form-urlencoded"};

            var request:angular.IRequestConfig = {
                method: 'POST',
                url: requestUrl,
                //Removed the transformResponse to prevent angular from parsing non-JSON objects.
                transformResponse: null,
                headers: this.credentialService.createRequestHeader(this.settings.wmsEndpoint, currentHeaders),
                data: this.settings.wmsServiceNameVersion + "&" + insertLayerStyle.toKVP()
            };

            // send request to Petascope and get response (headers and contents)
            this.$http(request).then(function (data:any) {
                result.resolve(data);
            }, function (error) {
                result.reject(error);
            });

            return result.promise;
        }

        // Delete the specified style's data from databasee
        public deleteLayerStyleRequest(request:wms.DeleteLayerStyle):angular.IPromise<any> {
            var result = this.$q.defer();
            // Build the request URL
            var requestUrl = this.settings.wmsFullEndpoint + "&" + request.toKVP();
            var currentHeaders = {};

            this.$http.get(requestUrl, {
                headers: this.credentialService.createRequestHeader(this.settings.wmsEndpoint, currentHeaders),
            }).then(function (data:any) {
                    try {                                                
                        result.resolve("");
                    } catch (err) {
                        result.reject(err);
                    }
                }, function (error) {
                    result.reject(error);
                });

            return result.promise;
        }

        // ******** Layer's downscaled levels coverages (pyramid members) management ********

        // List all downscaled levels coverages (pyramid memebers) of a base coverage
        public listPyramidMembersRequest(request:wms.ListPyramidMembers):angular.IPromise<any> {
            var result = this.$q.defer();
            // Build the request URL
            var requestUrl = this.wcsSettings.adminEndpoint + "?" + request.toKVP();
            var currentHeaders = {};

            this.$http.get(requestUrl, {
                headers: this.credentialService.createRequestHeader(this.settings.wmsEndpoint, currentHeaders),
            }).then(function (response:any) {
                try {                                                
                    result.resolve(response.data);
                } catch (err) {
                    result.reject(err);
                }
            }, function (error) {
                result.reject(error);
            });

            return result.promise;
        }

        // Create a pyramid member coverage for a layer
        public createPyramidMemberRequest(request:wms.CreatePyramidMember):angular.IPromise<any> {
            var result = this.$q.defer();
            // Build the request URL
            var requestUrl = this.wcsSettings.adminEndpoint + "?" + request.toKVP();
            var currentHeaders = {};

            this.$http.get(requestUrl, {
                headers: this.credentialService.createRequestHeader(this.settings.wmsEndpoint, currentHeaders),
            }).then(function (data:any) {
                try {                                                
                    result.resolve("");
                } catch (err) {
                    result.reject(err);
                }
            }, function (error) {
                result.reject(error);
            });

            return result.promise;
        }

        // Remove the pyramid member coverage from a base coverage's pyramid
        public removePyramidMemberRequest(request:wms.RemovePyramidMember):angular.IPromise<any> {
            var result = this.$q.defer();
            // Build the request URL
            var requestUrl = this.wcsSettings.adminEndpoint + "?" + request.toKVP();
            var currentHeaders = {};

            this.$http.get(requestUrl, {
                headers: this.credentialService.createRequestHeader(this.settings.wmsEndpoint, currentHeaders),
            }).then(function (data:any) {
                    try {                                                
                        result.resolve("");
                    } catch (err) {
                        result.reject(err);
                    }
                }, function (error) {
                    result.reject(error);
                });

            return result.promise;
        }

        // --------------- black list

        // Set a layer to the blacklist
        public blackListOneLayer(layerName:string):angular.IPromise<any> {
            var result = this.$q.defer();
            
            var requestUrl = this.wcsSettings.adminEndpoint + "?SERVICE=WMS&REQUEST=BlackList&LAYERS=" + layerName;
            var requestHeaders = this.adminService.getAuthentcationHeaders();

            this.$http.get(requestUrl, {
                    headers: requestHeaders
                }).then(function (data:any) {
                    result.resolve(data);
                }, function (error) {
                    result.reject(error);
                });

            return result.promise;
        }

        // Set all layers to the blacklist
        public blackListAllLayers():angular.IPromise<any> {
            var result = this.$q.defer();
            
            var requestUrl = this.wcsSettings.adminEndpoint + "?SERVICE=WMS&REQUEST=BlackListAll";
            var requestHeaders = this.adminService.getAuthentcationHeaders();

            this.$http.get(requestUrl, {
                    headers: requestHeaders
                }).then(function (data:any) {
                    result.resolve(data);
                }, function (error) {
                    result.reject(error);
                });

            return result.promise;
        }

        // --------------- white list

        // Remove a layer from the whitelist
        public whiteListOneLayer(layerName:string):angular.IPromise<any> {
            var result = this.$q.defer();
          
            var requestUrl = this.wcsSettings.adminEndpoint + "?SERVICE=WMS&REQUEST=WhiteList&LAYERS=" + layerName;
            var requestHeaders = this.adminService.getAuthentcationHeaders();

            this.$http.get(requestUrl, {
                    headers: requestHeaders
                }).then(function (data:any) {
                    result.resolve(data);
                }, function (error) {
                    result.reject(error);
                });

            return result.promise;
        }

        // Remove all layers from the blacklist
        public whiteListAllLayers():angular.IPromise<any> {
            var result = this.$q.defer();
            
            var requestUrl = this.wcsSettings.adminEndpoint + "?SERVICE=WMS&REQUEST=WhiteListAll";
            var requestHeaders = this.adminService.getAuthentcationHeaders();

            this.$http.get(requestUrl, {
                    headers: requestHeaders
                }).then(function (data:any) {
                    result.resolve(data);
                }, function (error) {
                    result.reject(error);
                });

            return result.promise;
        }

    }
}
