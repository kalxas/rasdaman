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

///<reference path="../../../assets/typings/tsd.d.ts"/>
///<reference path="../shared/WCSService.ts"/>
///<reference path="../../models/wcs/Capabilities.ts"/>
///<reference path="../main/MainController.ts"/>
///<reference path="../web_world_wind/WebWorldWindService.ts"/>

module rasdaman {
    export class DescribeCoverageController {
        //Makes the controller work as a tab.
        private static selectedCoverageId:string;

        public static $inject = [
            "$scope",
            "$rootScope",
            "$log",
            "rasdaman.WCSService",
            "Notification",
            "rasdaman.WCSErrorHandlingService",
            "rasdaman.WebWorldWindService"
        ];

        public constructor($scope:DescribeCoverageControllerScope,
                           $rootScope:angular.IRootScopeService,
                           $log:angular.ILogService,
                           wcsService:rasdaman.WCSService,
                           alertService:any,
                           wcsErrorHandlingService:rasdaman.WCSErrorHandlingService,
                           webWorldWindService:rasdaman.WebWorldWindService) {

            $scope.SelectedCoverageId = null;
            $scope.IsCoverageDescriptionsDocumentOpen = false;
            // default hide the div containing the Globe
            $scope.IsCoverageDescriptionsHideGlobe = true;

            $scope.isCoverageIdValid = ()=> {
                if ($scope.StateInformation.ServerCapabilities) {
                    var coverageSummaries = $scope.StateInformation.ServerCapabilities.Contents.CoverageSummary;
                    for (var i = 0; i < coverageSummaries.length; ++i) {
                        if (coverageSummaries[i].CoverageId == $scope.SelectedCoverageId) {
                            return true;
                        }
                    }
                }

                return false;
            };

            $rootScope.$on("SelectedCoverageId", (event:angular.IAngularEvent, coverageId:string)=> {
                $scope.SelectedCoverageId = coverageId;
                $scope.describeCoverage();
            });

            $scope.$watch("StateInformation.ServerCapabilities", (capabilities:wcs.Capabilities)=> {
                if (capabilities) {
                    $scope.AvailableCoverageIds = [];
                    capabilities.Contents.CoverageSummary.forEach((coverageSummary:wcs.CoverageSummary)=> {
                        $scope.AvailableCoverageIds.push(coverageSummary.CoverageId);
                    });
                }
            });


            // when GetCoverage triggers get coverage id, this function will be called to fill data for both DescribeCoverage and GetCoverage tabs
            $scope.$watch("StateInformation.SelectedGetCoverageId", (getCoverageId:string)=> {
                if (getCoverageId) {
                    $scope.SelectedCoverageId = getCoverageId;
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
                coverageIds.push($scope.SelectedCoverageId);

                var describeCoverageRequest = new wcs.DescribeCoverage(coverageIds);

                //Retrieve coverage description
                wcsService.getCoverageDescription(describeCoverageRequest)
                    .then(
                        (response:rasdaman.common.Response<wcs.CoverageDescriptions>)=> {
                            //Success handler
                            $scope.CoverageDescriptionsDocument = response.Document;
                            $scope.CoverageDescriptions = response.Value;

                            // Fetch the coverageExtent by coverageId to display on globe if possible
                            var coveragesExtents = webWorldWindService.getCoveragesExtentsByCoverageId($scope.SelectedCoverageId);                            
                            if (coveragesExtents == null) {
                                $scope.IsCoverageDescriptionsHideGlobe = true;
                            } else {
                                // Show coverage's extent on the globe
                                var canvasId = "canvasDescribeCoverage";
                                $scope.IsCoverageDescriptionsHideGlobe = false;
                                webWorldWindService.loadCoveragesExtentsOnGlobe(canvasId, coveragesExtents);
                                // NOTE: Without the time interval, Globe in DescribeCoverage/GetCoverage will hang up in some cases when it goes to the center of current coverage's extent
                                // If the globe hangs up, click on the button DescribeCoverage one more time.
                                webWorldWindService.gotoCoverageExtentCenter(canvasId, coveragesExtents);
                            }                            
                        },
                        (...args:any[])=> {
                            $scope.CoverageDescriptionsDocument = null;
                            $scope.CoverageDescriptions = null;

                            wcsErrorHandlingService.handleError(args);
                            $log.error(args);
                        })
                    .finally(()=> {
                        $scope.StateInformation.SelectedCoverageDescriptions = $scope.CoverageDescriptions;
                    });
            };

            $scope.IsCoverageDescriptionsDocumentOpen = false;
        }
    }

    interface DescribeCoverageControllerScope extends MainControllerScope {
        IsCoverageDescriptionsDocumentOpen:boolean;
        // Not show the globe when coverage cannot reproject to EPSG:4326
        IsCoverageDescriptionsHideGlobe:boolean;

        CoverageDescriptionsDocument:rasdaman.common.ResponseDocument;
        CoverageDescriptions:wcs.CoverageDescriptions;

        AvailableCoverageIds:string[];
        SelectedCoverageId:string;

        isCoverageIdValid():void;
        describeCoverage():void;
    }
}
