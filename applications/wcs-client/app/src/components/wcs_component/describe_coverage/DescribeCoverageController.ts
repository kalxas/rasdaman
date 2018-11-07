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

///<reference path="../../../../assets/typings/tsd.d.ts"/>
///<reference path="../../wcs_component/WCSService.ts"/>
///<reference path="../../../models/wcs_model/wcs/Capabilities.ts"/>
///<reference path="../../main/WCSMainController.ts"/>
///<reference path="../../web_world_wind/WebWorldWindService.ts"/>

module rasdaman {
    export class WCSDescribeCoverageController {
        //Makes the controller work as a tab.
        private static selectedCoverageId:string;

        public static $inject = [
            "$scope",
            "$rootScope",
            "$log",
            "rasdaman.WCSService",
            "rasdaman.WCSSettingsService",
            "Notification",
            "rasdaman.ErrorHandlingService",
            "rasdaman.WebWorldWindService"
        ];

        public constructor($scope:WCSDescribeCoverageControllerScope,
                           $rootScope:angular.IRootScopeService,
                           $log:angular.ILogService,
                           wcsService:rasdaman.WCSService,
                           settings:rasdaman.WCSSettingsService,
                           alertService:any,
                           errorHandlingService:rasdaman.ErrorHandlingService,
                           webWorldWindService:rasdaman.WebWorldWindService) {            

            $scope.selectedCoverageId = null;
            $scope.isCoverageDescriptionsDocumentOpen = false;
            // default hide the div containing the Globe
            $scope.isCoverageDescriptionsHideGlobe = true;

            $scope.isCoverageIdValid = ()=> {
                if ($scope.wcsStateInformation.serverCapabilities) {
                    var coverageSummaries = $scope.wcsStateInformation.serverCapabilities.contents.coverageSummary;
                    for (var i = 0; i < coverageSummaries.length; ++i) {
                        if (coverageSummaries[i].coverageId == $scope.selectedCoverageId) {
                            return true;
                        }
                    }
                }

                return false;
            };

            $rootScope.$on("wcsSelectedGetCoverageId", (event:angular.IAngularEvent, coverageId:string)=> {                
                $scope.selectedCoverageId = coverageId;
                $scope.describeCoverage();
            });

            $scope.$watch("wcsStateInformation.serverCapabilities", (capabilities:wcs.Capabilities)=> {
                if (capabilities) {
                    $scope.availableCoverageIds = [];
                    capabilities.contents.coverageSummary.forEach((coverageSummary:wcs.CoverageSummary)=> {
                        $scope.availableCoverageIds.push(coverageSummary.coverageId);
                    });
                }                
            });


            // when GetCoverage triggers get coverage id, this function will be called to fill data for both DescribeCoverage and GetCoverage tabs
            $scope.$watch("wcsStateInformation.selectedGetCoverageId", (getCoverageId:string)=> {
                if (getCoverageId) {
                    $scope.selectedCoverageId = getCoverageId;
                    $scope.describeCoverage();
                }
            });

            $scope.describeCoverage = function () {                
                if (!$scope.isCoverageIdValid()) {
                    alertService.error("The entered coverage ID is invalid.");
                    return;
                }

                //Create describe coverage request
                var coverageIds:string[] = [];
                coverageIds.push($scope.selectedCoverageId);

                var describeCoverageRequest = new wcs.DescribeCoverage(coverageIds);
                $scope.requestUrl = settings.wcsEndpoint + "?" + describeCoverageRequest.toKVP();
                
                            
                //Retrieve coverage description
                wcsService.getCoverageDescription(describeCoverageRequest)
                    .then(
                        (response:rasdaman.common.Response<wcs.CoverageDescriptions>)=> {
                            //Success handler
                            $scope.coverageDescriptionsDocument = response.document;
                            $scope.coverageDescriptions = response.value;
                            $scope.metaDataPrint = ' ';

                            var rawCoverageDescription = $scope.coverageDescriptionsDocument.value;

                            // Extract the metadata from the coverage document (inside <rasdaman:covMetadata></rasdaman:covMetadata>)
                            var parser = new DOMParser();
                            var xmlDoc = parser.parseFromString(rawCoverageDescription,"text/xml"); 
                            var elements = xmlDoc.getElementsByTagName("rasdaman:covMetadata");

                            var metadataContent = "";
                            if (elements.length > 0) {
                                metadataContent = elements[0].innerHTML;
                            }

                            if (metadataContent != "") {  
                                $scope.metaDataPrint = metadataContent;
                                //Define the characters that indicates if the metadata string represents JSON code.
                                var ch = /{/gi;

                                //Checks if the metadata is written in JSON.
                                if ($scope.metaDataPrint.search(ch) != -1) {
                                    $scope.typeMetadata = 'json';
                                } else {
                                    $scope.typeMetadata = 'xml';
                                }
                            }                                

                            // Fetch the coverageExtent by coverageId to display on globe if possible
                            var coverageExtentArray = webWorldWindService.getCoveragesExtentsByCoverageId($scope.selectedCoverageId);
                            if (coverageExtentArray == null) {
                                $scope.isCoverageDescriptionsHideGlobe = true;
                            } else {
                                // Show coverage's extent on the globe
                                var canvasId = "wcsCanvasDescribeCoverage";
                                $scope.isCoverageDescriptionsHideGlobe = false;
                                // Also prepare for DescribeCoverage's globe with only 1 coverageExtent                                
                                webWorldWindService.prepareCoveragesExtentsForGlobe(canvasId, coverageExtentArray);
                                // Then, load the footprint of this coverage on the globe
                                webWorldWindService.showHideCoverageExtentOnGlobe(canvasId, $scope.selectedCoverageId);
                                // And look at the coverage's center on globe
                                webWorldWindService.gotoCoverageExtentCenter(canvasId, coverageExtentArray);
                            }
                        },
                        (...args:any[])=> {
                            $scope.coverageDescriptionsDocument = null;
                            $scope.coverageDescriptions = null;

                            errorHandlingService.handleError(args);
                            $log.error(args);
                        })
                    .finally(()=> {
                        $scope.wcsStateInformation.selectedCoverageDescriptions = $scope.coverageDescriptions;
                    });
            };

            $scope.isCoverageDescriptionsDocumentOpen = false;
        }
    }

    interface WCSDescribeCoverageControllerScope extends WCSMainControllerScope {
        isCoverageDescriptionsDocumentOpen:boolean;
        // Not show the globe when coverage cannot reproject to EPSG:4326
        isCoverageDescriptionsHideGlobe:boolean;

        coverageDescriptionsDocument:rasdaman.common.ResponseDocument;
        coverageDescriptions:wcs.CoverageDescriptions;

        availableCoverageIds:string[];
        selectedCoverageId:string;

        requestUrl:string;

        isCoverageIdValid():void;
        describeCoverage():void;    
        metaDataPrint:string;
        typeMetadata:string;
    }
}
