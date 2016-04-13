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
///<reference path="../main/MainController.ts"/>
///<reference path="../shared/WCSService.ts"/>

module rasdaman {
    export class DeleteCoverageController {

        public static $inject = [
            "$scope",
            "$log",
            "Notification",
            "rasdaman.WCSService",
            "rasdaman.WCSErrorHandlingService"
        ];

        public constructor(private $scope:DeleteCoverageControllerScope,
                           private $log:angular.ILogService,
                           private alertService:any,
                           private wcsService:rasdaman.WCSService,
                           private errorHandlingService:WCSErrorHandlingService) {
            function isCoverageIdValid(coverageId:string):boolean {
                if ($scope.StateInformation.ServerCapabilities) {
                    var coverageSummaries = $scope.StateInformation.ServerCapabilities.Contents.CoverageSummary;
                    for (var i = 0; i < coverageSummaries.length; ++i) {
                        if (coverageSummaries[i].CoverageId == coverageId) {
                            return true;
                        }
                    }
                }

                return false;
            }

            $scope.$watch("IdOfCoverageToDelete", (newValue:string, oldValue:string)=> {
                $scope.IsCoverageIdValid = isCoverageIdValid(newValue);
            });

            $scope.$watch("StateInformation.ServerCapabilities", (capabilities:wcs.Capabilities)=> {
                if (capabilities) {
                    $scope.AvailableCoverageIds = [];
                    capabilities.Contents.CoverageSummary.forEach((coverageSummary:wcs.CoverageSummary)=> {
                        $scope.AvailableCoverageIds.push(coverageSummary.CoverageId);
                    });
                }
            });

            $scope.deleteCoverage = ()=> {
                if ($scope.RequestInProgress) {
                    this.alertService.error("Cannot delete a coverage while another delete request is in progress.");
                } else if (!isCoverageIdValid($scope.IdOfCoverageToDelete)) {
                    this.alertService.error("The coverage ID <b>" + $scope.IdOfCoverageToDelete + "</b> is not valid.");
                } else {
                    $scope.RequestInProgress = true;

                    this.wcsService.deleteCoverage($scope.IdOfCoverageToDelete).then(
                        (...args:any[])=> {
                            this.alertService.success("Successfully deleted coverage with ID <b>" + $scope.IdOfCoverageToDelete + "<b/>");
                            this.$log.log(args);
                        }, (...args:any[])=> {
                            this.errorHandlingService.handleError(args);
                            this.$log.error(args);
                        }).finally(function () {
                        $scope.RequestInProgress = false;
                    });
                }
            };

            $scope.IdOfCoverageToDelete = null;
            $scope.RequestInProgress = false;
            $scope.IsCoverageIdValid = false;
        }
    }

    interface DeleteCoverageControllerScope extends MainControllerScope {
        IdOfCoverageToDelete:string;
        AvailableCoverageIds:string[];
        RequestInProgress:boolean;
        IsCoverageIdValid:boolean;

        deleteCoverage():void;
    }
}