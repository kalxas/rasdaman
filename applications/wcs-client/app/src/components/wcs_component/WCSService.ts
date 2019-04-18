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

        public static $inject = ["$http", "$q", "rasdaman.WCSSettingsService", "rasdaman.common.SerializedObjectFactory", "$window"];

        public constructor(private $http:angular.IHttpService,
                           private $q:angular.IQService,
                           private settings:rasdaman.WCSSettingsService,
                           private serializedObjectFactory:rasdaman.common.SerializedObjectFactory,
                           private $window:angular.IWindowService) {
        }


        public getServerCapabilities(request:wcs.GetCapabilities):angular.IPromise<any> {
            var result = this.$q.defer();
            var self = this;

            var requestUrl = this.settings.wcsEndpoint + "?" + request.toKVP();
            this.$http.get(requestUrl)
                .then(function (data:any) {
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

        // When sending request GetCapabilities, also make a request to a made up GetCoveragesExtents to get all the reprojected CoveragesExtents in ESPG:4326        
        public getCoveragesExtents():angular.IPromise<any> {
            var result = this.$q.defer();

            var requestUrl = this.settings.wcsEndpoint + "/GetCoveragesExtents";

            this.$http.get(requestUrl)
                .then(function (data:any) {
                    var response = new rasdaman.common.Response<any>(null, data.data);
                    result.resolve(response);
                }, function (error) {
                    result.reject(error);
                });

            return result.promise;
        }

        public getCoverageDescription(request:wcs.DescribeCoverage):angular.IPromise<any> {
            var result = this.$q.defer();
            var self = this;

            var requestUrl = this.settings.wcsEndpoint + "?" + request.toKVP();
            this.$http.get(requestUrl)
                .then(function (data:any) {
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

        // Send a GetCoverage KVP request in HTTP GET
        public getCoverageHTTPGET(request:wcs.GetCoverage):angular.IPromise<any> {
            var result = this.$q.defer();
            // Build the request URL
            var requestUrl = this.settings.wcsEndpoint + "?" + request.toKVP();

            // For get coverage, open a new window.
            this.$window.open(requestUrl);

            // Return the URL as the result
            result.resolve(requestUrl);

            return result.promise;
        }

        // Send a GetCoverage KVP request in HTTP POST
        public getCoverageHTTPPOST(request:wcs.GetCoverage) {
            var result = this.$q.defer();

            var requestUrl = this.settings.wcsEndpoint;
            var keysValues = request.toKVP();
            var arrayTmp = keysValues.split("&");

            // Simulate the same behavior as HTTP GET, open a new window to see the result from Petascope
            // by submitting form input elements.
            var formId = "getCoverageHTTPPostForm";     
            var formTmp = <HTMLFormElement>(document.getElementById(formId)); 
            // remove the old one if exists      
            if (formTmp) {
                document.body.removeChild(formTmp);
            }            

            formTmp = document.createElement("form");
            formTmp.id = "getCoverageHTTPPostForm";
            formTmp.target = "_blank";
            formTmp.method = "POST";
            formTmp.action = requestUrl;

            for (var i = 0; i < arrayTmp.length;i ++) {
                if (arrayTmp[i].trim() != "") {
                    var inputTmp = document.createElement("input");
                    inputTmp.type = "hidden";
                    // e.g: service=WCS
                    var keyValue = arrayTmp[i].split("=");
                    inputTmp.name = keyValue[0];
                    inputTmp.value = keyValue[1];
                    formTmp.appendChild(inputTmp);
                }                
            }

            document.body.appendChild(formTmp);            
            formTmp.submit();
        }

        public deleteCoverage(coverageId:string):angular.IPromise<any> {
            var result = this.$q.defer();

            if (!coverageId) {
                result.reject("You must specify at least one coverage ID.");
            }
            var requestUrl = this.settings.wcsEndpoint + "?" + this.settings.wcsServiceNameVersion + "&REQUEST=DeleteCoverage&COVERAGEID=" + coverageId;

            this.$http.get(requestUrl)
                .then(function (data:any) {
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
            var requestUrl = this.settings.wcsEndpoint + "?" + this.settings.wcsServiceNameVersion + "&REQUEST=InsertCoverage&coverageRef=" + encodeURI(coverageUrl);
            if (useGeneratedId) {
                requestUrl += "&useId=new";
            }

            this.$http.get(requestUrl)
                .then(function (data:any) {
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

            var request:angular.IRequestConfig = {
                method: 'POST',
                url: requestUrl,
                //Removed the transformResponse to prevent angular from parsing non-JSON objects.
                transformResponse: null,
                headers: {'Content-Type': 'application/x-www-form-urlencoded'},
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
    }
}
