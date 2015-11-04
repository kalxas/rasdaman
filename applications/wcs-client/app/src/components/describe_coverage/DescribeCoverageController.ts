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

module rasdaman {
    export class DescribeCoverageController {
        //Makes the controller work as a tab.
        private static selectedCoverageId:string;

        public static $inject = [
            "$scope",
            "$rootScope",
            "$log",
            "rasdaman.WCSService",
            "Notification"
        ];

        public constructor($scope:DescribeCoverageControllerScope,
                           $rootScope:angular.IRootScopeService,
                           $log:angular.ILogService,
                           wcsService:rasdaman.WCSService,
                           alertService:any) {

            $scope.SelectedCoverageId = null;
            $scope.IsCoverageDescriptionsDocumentOpen = false;

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
                    },
                    (...args:any[])=> {
                        $scope.CoverageDescriptionsDocument = null;
                        $scope.CoverageDescriptions = null;

                        alertService.error("Failed to retrieve the description for coverage with ID " + $scope.SelectedCoverageId + ". Check the log for more details.");
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

        CoverageDescriptionsDocument:rasdaman.common.ResponseDocument;
        CoverageDescriptions:wcs.CoverageDescriptions;

        AvailableCoverageIds:string[];
        SelectedCoverageId:string;

        isCoverageIdValid():void;
        describeCoverage():void;
    }
}