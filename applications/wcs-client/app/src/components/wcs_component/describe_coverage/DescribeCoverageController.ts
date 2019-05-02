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
            $scope.REGULAR_AXIS = "regular";
            $scope.IRREGULAR_AXIS = "irregular";
            $scope.NOT_AVALIABLE = "N/A";

            // default hide the div containing the Globe
            $scope.hideWebWorldWindGlobe = true;

            $scope.isCoverageIdValid = ()=> {
                if ($scope.wcsStateInformation.serverCapabilities) {
                    var coverageSummaries = $scope.wcsStateInformation.serverCapabilities.contents.coverageSummaries;
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
                    $scope.coverageCustomizedMetadatasDict = {};
                    
                    capabilities.contents.coverageSummaries.forEach((coverageSummary:wcs.CoverageSummary)=> {
                        let coverageId = coverageSummary.coverageId;
                        $scope.availableCoverageIds.push(coverageId);

                        // coverage location, size,...
                        if (coverageSummary.customizedMetadata != null) {
                            $scope.coverageCustomizedMetadatasDict[coverageId] = coverageSummary.customizedMetadata;
                        }
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

            /**
             * Parse coverage metadata as string and show it to a dropdown
             */
            $scope.parseCoverageMetadata = () => {
                $scope.metadata = null;
                
                // Extract the metadata from the coverage document (inside <rasdaman:covMetadata></rasdaman:covMetadata>)
                var parser = new DOMParser();
                var xmlDoc = parser.parseFromString($scope.rawCoverageDescription, "text/xml");

                var elements = xmlDoc.getElementsByTagName("rasdaman:covMetadata");
                if (elements.length > 0) {
                    $scope.metadata = elements[0].innerHTML;

                    // Check if coverage metadata is XML / JSON format
                    for (let i = 0; i < $scope.metadata.length; i++) {
                        if ($scope.metadata[i] === "{") {
                            $scope.typeMetadata = "json";
                            break;
                        } else {
                            $scope.typeMetadata = "xml";
                            break;
                        }
                    }
                }
            }

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
                $scope.axes = [];                
                            
                //Retrieve coverage description
                wcsService.getCoverageDescription(describeCoverageRequest)
                    .then(
                        (response:rasdaman.common.Response<wcs.CoverageDescription>)=> {
                            // //Success handler                            
                            $scope.coverageDescription = response.value;                           
                            $scope.rawCoverageDescription = response.document.value;

                            $scope.parseCoverageMetadata();
                    
                            // Fetch the coverageExtent by coverageId to display on globe if possible
                            var coverageExtentArray = webWorldWindService.getCoveragesExtentsByCoverageId($scope.selectedCoverageId);
                            if (coverageExtentArray == null) {
                                $scope.hideWebWorldWindGlobe = true;
                            } else {
                                // Show coverage's extent on the globe
                                var canvasId = "wcsCanvasDescribeCoverage";
                                $scope.hideWebWorldWindGlobe = false;
                                // Also prepare for DescribeCoverage's globe with only 1 coverageExtent                                
                                webWorldWindService.prepareCoveragesExtentsForGlobe(canvasId, coverageExtentArray);
                                // Then, load the footprint of this coverage on the globe
                                webWorldWindService.showHideCoverageExtentOnGlobe(canvasId, $scope.selectedCoverageId);
                                // And look at the coverage's center on globe
                                webWorldWindService.gotoCoverageExtentCenter(canvasId, coverageExtentArray);
                            }
                        },
                        (...args:any[])=> {                            
                            $scope.coverageDescription = null;

                            errorHandlingService.handleError(args);
                            $log.error(args);
                        })
                    .finally(()=> {
                        $scope.wcsStateInformation.selectedCoverageDescription = $scope.coverageDescription;
                    });
            };           
        }
    }

    interface WCSDescribeCoverageControllerScope extends WCSMainControllerScope {        
        // Not show the globe when coverage cannot reproject to EPSG:4326
        isCoverageDescriptionsDocumentOpen:boolean;
        hideWebWorldWindGlobe:boolean;

        coverageDescription:wcs.CoverageDescription;
        rawCoverageDescription:string;

        availableCoverageIds:string[];
        coverageCustomizedMetadatasDict:any;
        selectedCoverageId:string;

        // Array of objects
        axes:any[];

        requestUrl:string;

        metadata:string;
        typeMetadata:string;


        isCoverageIdValid():void;
        describeCoverage():void;
        getAxisResolution(number, any):string;
        getAxisType(number, any):string;
        parseCoverageMetadata():void;
    }
}
