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
 * Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2014, 2015 Peter Baumann /
 rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
/// <reference path="../../common/_common.ts"/>
/// <reference path="../../models/wcs/_wcs.ts"/>
/// <reference path="../settings/SettingsService.ts"/>

module rasdaman {
    export class WCSService {
        public static $inject = ["$http", "$q", "rasdaman.SettingsService", "rasdaman.common.SerializedObjectFactory", "$window"];

        public constructor(private $http:angular.IHttpService,
                           private $q:angular.IQService,
                           private settings:rasdaman.SettingsService,
                           private serializedObjectFactory:rasdaman.common.SerializedObjectFactory,
                           private $window:angular.IWindowService) {
        }


        public getServerCapabilities(request:wcs.GetCapabilities):angular.IPromise<rasdaman.common.Response<wcs.Capabilities> > {
            var result = this.$q.defer();
            var self = this;

            var requestUrl = this.settings.WCSEndpoint + "?" + request.toKVP();
            this.$http.get(requestUrl)
                .then(function (data:any) {
                    var doc = new rasdaman.common.ResponseDocument(data.data, rasdaman.common.ResponseDocumentType.XML);
                    var serializedResponse = self.serializedObjectFactory.getSerializedObject(doc);
                    var capabilities = new wcs.Capabilities(serializedResponse);
                    var response = new rasdaman.common.Response<wcs.Capabilities>(doc, capabilities);

                    result.resolve(response);
                }, function (error) {
                    result.reject(error);
                });

            return result.promise;
        }

        public getCoverageDescription(request:wcs.DescribeCoverage):angular.IPromise<rasdaman.common.Response<wcs.CoverageDescriptions> > {
            var result = this.$q.defer();
            var self = this;

            var requestUrl = this.settings.WCSEndpoint + "?" + request.toKVP();
            this.$http.get(requestUrl)
                .then(function (data:any) {
                    var doc = new rasdaman.common.ResponseDocument(data.data, rasdaman.common.ResponseDocumentType.XML);
                    var serializedResponse = self.serializedObjectFactory.getSerializedObject(doc);
                    var capabilities = new wcs.CoverageDescriptions(serializedResponse);
                    var response = new rasdaman.common.Response<wcs.CoverageDescriptions>(doc, capabilities);

                    result.resolve(response);
                }, function (error) {
                    result.reject(error);
                });

            return result.promise;
        }

        public getCoverage(request:wcs.GetCoverage):angular.IPromise<any> {
            var result = this.$q.defer();

            // For get coverage, open a new window.
            var requestUrl = this.settings.WCSEndpoint + "?" + request.toKVP();
            this.$window.open(requestUrl);

            result.resolve();
            return result.promise;
        }

        public deleteCoverage(coverageId:string):angular.IPromise<rasdaman.common.Response<any> > {
            var result = this.$q.defer();

            if (!coverageId) {
                result.reject("You must specify at least one coverage ID.");
            }
            var requestUrl = this.settings.WCSEndpoint + "?SERVICE=WCS&VERSION=2.0.1&REQUEST=DeleteCoverage&COVERAGEID=" + coverageId;

            this.$http.get(requestUrl)
                .then(function (data:any) {
                    result.resolve(data);
                }, function (error) {
                    result.reject(error);
                });

            return result.promise;
        }

        public insertCoverage(coverageUrl:string, useGeneratedId:boolean):angular.IPromise<rasdaman.common.Response<any> > {
            var result = this.$q.defer();

            if (!coverageUrl) {
                result.reject("You must indicate a coverage source.");
            }
            var requestUrl = this.settings.WCSEndpoint + "?SERVICE=WCS&VERSION=2.0.1&REQUEST=InsertCoverage&coverageRef=" + encodeURI(coverageUrl);
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
         * @param binaryFormat
         * @returns {IPromise<T>}
         */
        public processCoverages(query:wcs.ProcessCoverages, binaryFormat?:boolean):angular.IPromise<rasdaman.common.Response<any> > {
            var result = this.$q.defer();

            var requestUrl = this.settings.WCSEndpoint + "?" + query.toKVP();
            var request:angular.IRequestConfig = {
                method: 'GET',
                url: requestUrl,
                //Removed the transformResponse to prevent angular from parsing non-JSON objects.
                transformResponse: null
            };

            if (binaryFormat) {
                request.responseType = "arraybuffer";
            }

            this.$http(request).then(function (data:any) {
                result.resolve(data);
            }, function (error) {
                result.reject(error);
            });

            return result.promise;
        }
    }
}