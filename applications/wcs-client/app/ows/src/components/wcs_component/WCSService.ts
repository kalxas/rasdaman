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
/// <reference path="../../models/wcs_model/wcs/_wcs.ts"/>
/// <reference path="../wcs_component/settings/SettingsService.ts"/>

module rasdaman {
    export class WCSService {

        public static $inject = ["$http", "$q", "rasdaman.WCSSettingsService", 
                                 "rasdaman.common.SerializedObjectFactory", "$window",
                                 "rasdaman.CredentialService", "$state",
                                 "rasdaman.AdminService"];

        public constructor(private $http:angular.IHttpService,
                           private $q:angular.IQService,
                           private settings:rasdaman.WCSSettingsService,
                           private serializedObjectFactory:rasdaman.common.SerializedObjectFactory,
                           private $window:angular.IWindowService,
                           private credentialService:rasdaman.CredentialService,
                           private $state:any,
                           private adminService:rasdaman.AdminService) {
        }


        public getServerCapabilities(request:wcs.GetCapabilities):angular.IPromise<any> {
            var result = this.$q.defer();
            var self = this;

            var requestHeaders = {};
            var credentials:login.Credential = this.adminService.getPersistedAdminUserCredentials();
            if (credentials != null) {
                // If petascope admin user logged in, then use its credentials for GetCapabilities intead to view blacklisted coverages
                requestHeaders = this.adminService.getAuthenticationHeaders();
            } else {
                requestHeaders = this.credentialService.createRequestHeader(this.settings.wcsEndpoint, {});
            }

            var requestUrl = this.settings.wcsEndpoint + "?" + request.toKVP();
            this.$http.get(requestUrl, {
                    headers: requestHeaders
                }).then(function (data:any) {
                    try {
                        var doc = new rasdaman.common.ResponseDocument(data.data, rasdaman.common.ResponseDocumentType.XML);
                        var serializedResponse = self.serializedObjectFactory.getSerializedObject(doc);
                        var capabilities = new wcs.Capabilities(serializedResponse);
                        var response = new rasdaman.common.Response<wcs.Capabilities>(doc, capabilities);
                        result.resolve(response);
                    } catch (err) {
                        result.reject(err);
                    }
                }, function (error) {
                    result.reject(error);
                });

            return result.promise;
        }

        public getCoverageDescription(request:wcs.DescribeCoverage):angular.IPromise<any> {
            var result = this.$q.defer();
            var self = this;

            var currentHeaders = {};

            var requestUrl = this.settings.wcsEndpoint + "?" + request.toKVP();
            this.$http.get(requestUrl, {
                headers: this.credentialService.createRequestHeader(this.settings.wcsEndpoint, currentHeaders)
                }).then(function (data:any) {
                    try {
                        var doc = new rasdaman.common.ResponseDocument(data.data, rasdaman.common.ResponseDocumentType.XML);
                        var serializedResponse = self.serializedObjectFactory.getSerializedObject(doc);
                        var description = new wcs.CoverageDescription(serializedResponse);
                        var response = new rasdaman.common.Response<wcs.CoverageDescription>(doc, description);

                        result.resolve(response);
                    } catch (err) {
                        result.reject(err);
                    }
                }, function (error) {
                    result.reject(error);
                });

            return result.promise;
        }

        // Store these keys values to be reused in result.html page
        public storeKVPParametersToLocalStorage(petascopeEndPoint:string, keysValuesStr:string) {
           
            var getCoverageKVPParameters = {"PetascopeEndPoint": petascopeEndPoint};
            getCoverageKVPParameters["request"] = keysValuesStr;

            if (this.credentialService.hasStoredCredentials()) {
                var authorizationObj = this.credentialService.getAuthorizationHeader(petascopeEndPoint);
                getCoverageKVPParameters = (<any>Object).assign(getCoverageKVPParameters, authorizationObj);
            }

            window.localStorage.setItem("GetcoverageKVPParameters", JSON.stringify(getCoverageKVPParameters));
        }

        // Send a GetCoverage KVP request in HTTP GET
        public getCoverageHTTPGET(request:wcs.GetCoverage):angular.IPromise<any> {
            var result = this.$q.defer();
            // Build the request URL
            var requestUrl = this.settings.wcsEndpoint + "?" + request.toKVP();
            var url = this.settings.defaultContextPath + "/ows/result.html";
                        
            this.storeKVPParametersToLocalStorage(this.settings.wcsEndpoint, request.toKVP());            
            window.open(url, '_blank');            

            // Return the URL as the result
            result.resolve(requestUrl);

            return result.promise;
        }

        // Send a GetCoverage KVP request in HTTP POST
        public getCoverageHTTPPOST(request:wcs.GetCoverage) {
            return this.getCoverageHTTPGET(request);
        }

        public deleteCoverage(coverageId:string):angular.IPromise<any> {
            var result = this.$q.defer();

            if (!coverageId) {
                result.reject("You must specify at least one coverage ID.");
            }
            
            var currentHeaders = {};
            var requestUrl = this.settings.wcsEndpoint + "?" + this.settings.wcsServiceNameVersion + "&REQUEST=DeleteCoverage&COVERAGEID=" + coverageId;

            this.$http.get(requestUrl, {
                    headers: this.credentialService.createRequestHeader(this.settings.wcsEndpoint, currentHeaders)
                }).then(function (data:any) {
                    result.resolve(data);
                }, function (error) {
                    result.reject(error);
                });

            return result.promise;
        }

        public insertCoverage(coverageUrl:string, useGeneratedId:boolean):angular.IPromise<any> {
            var result = this.$q.defer();

            if (!coverageUrl) {
                result.reject("You must indicate a coverage source.");
            }

            var currentHeaders = {};
            var requestUrl = this.settings.wcsEndpoint + "?" + this.settings.wcsServiceNameVersion + "&REQUEST=InsertCoverage&coverageRef=" + encodeURI(coverageUrl);
            if (useGeneratedId) {
                requestUrl += "&useId=new";
            }

            this.$http.get(requestUrl, {
                    headers: this.credentialService.createRequestHeader(this.settings.wcsEndpoint, currentHeaders)
                }).then(function (data:any) {
                    result.resolve(data);
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
        public processCoverages(query:String):angular.IPromise<any> {
            var result = this.$q.defer();            
            // Use POST request to POST long WCPS query to server as form-data (as same as Web page /ows/wcps)
            var queryStr = 'query=' + query;
                        
            var requestUrl = this.settings.wcsEndpoint;
            var currentHeaders = {"Content-Type": "application/x-www-form-urlencoded"};

            var request:angular.IRequestConfig = {
                method: 'POST',
                url: requestUrl,
                headers: this.credentialService.createRequestHeader(this.settings.wcsEndpoint, currentHeaders),
                //Removed the transformResponse to prevent angular from parsing non-JSON objects.
                transformResponse: null,                
                data: queryStr
            };

            // TODO: if have new supported binary encodings, add them here.
            if (queryStr.indexOf("png") >= 0 || queryStr.indexOf("jpeg") >= 0 || queryStr.indexOf("jpeg2000") >= 0 || queryStr.indexOf("tiff") >= 0 || queryStr.indexOf("netcdf") >= 0)  {            
                // This is needed to save binary file correctly
                request.responseType = "arraybuffer"; 
            }

            // send request to Petascope and get response (headers and contents)
            this.$http(request).then(function (data:any) {
                result.resolve(data);
            }, function (error) {
                result.reject(error);
            });

            return result.promise;
        }

        
        // Update coverage's metadata from a text file (formData is FormData object containing the file to be uploaded)
        public updateCoverageMetadata(formData):angular.IPromise<any> {
            var result = this.$q.defer();                                               
            var requestUrl = this.settings.adminEndpoint + "/coverage/update"; 
            
            var requestHeaders = this.adminService.getAuthenticationHeaders();
            requestHeaders["Content-Type"] = undefined;

            var request:angular.IRequestConfig = {
                method: 'POST',
                url: requestUrl,
                //Removed the transformResponse to prevent angular from parsing non-JSON objects.
                transformResponse: null,                
                headers: requestHeaders,
                data: formData
            };

            // send request to Petascope and get response (headers and contents)
            this.$http(request).then(function (data:any) {
                result.resolve(data);
            }, function (error) {
                result.reject(error);
            });

            return result.promise;
        }    

        // --------------- black list

        // Set a coverage id to the blacklist
        public blackListOneCoverage(coverageId:string[]):angular.IPromise<any> {
            var result = this.$q.defer();
            
            var requestUrl = this.settings.adminEndpoint + "/wcs/blacklist?COVERAGELIST=" + coverageId;
            var requestHeaders = this.adminService.getAuthenticationHeaders();

            this.$http.get(requestUrl, {
                    headers: requestHeaders
                }).then(function (data:any) {
                    result.resolve(data);
                }, function (error) {
                    result.reject(error);
                });

            return result.promise;
        }

        // Set all coverages to the blacklist
        public blackListAllCoverages():angular.IPromise<any> {
            var result = this.$q.defer();
            
            var requestUrl = this.settings.adminEndpoint + "/wcs/blacklistall";
            var requestHeaders = this.adminService.getAuthenticationHeaders();

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

        // Remove a coverage from the whitelist
        public whiteListOneCoverage(coverageId:string):angular.IPromise<any> {
            var result = this.$q.defer();
          
            var requestUrl = this.settings.adminEndpoint + "/wcs/whitelist?COVERAGELIST=" + coverageId;
            var requestHeaders = this.adminService.getAuthenticationHeaders();

            this.$http.get(requestUrl, {
                    headers: requestHeaders
                }).then(function (data:any) {
                    result.resolve(data);
                }, function (error) {
                    result.reject(error);
                });

            return result.promise;
        }

        // Remove all coverages from the blacklist
        public whiteListAllCoverages():angular.IPromise<any> {
            var result = this.$q.defer();
            
            var requestUrl = this.settings.adminEndpoint + "/wcs/whitelistall";
            var requestHeaders = this.adminService.getAuthenticationHeaders();

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
