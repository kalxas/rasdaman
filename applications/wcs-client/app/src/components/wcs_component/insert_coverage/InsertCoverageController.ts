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
///<reference path="../../main/WCSMainController.ts"/>

module rasdaman {
    export class WCSInsertCoverageController {
        public static $inject = [
            "$scope",
            "$log",
            "Notification",
            "rasdaman.WCSService",
            "rasdaman.ErrorHandlingService"
        ];

        public constructor(private $scope:WCSInsertCoverageControllerScope,
                           private $log:angular.ILogService,
                           private alertService:any,
                           private wcsService:rasdaman.WCSService,
                           private errorHandlingService:ErrorHandlingService) {
            $scope.urlOfCoverageToInsert = null;
            $scope.requestInProgress = false;
            $scope.useGeneratedCoverageId = false;

            $scope.insertCoverage = ()=> {
                if ($scope.requestInProgress) {
                    this.alertService.error("Cannot insert a coverage while another insert request is in progress.");
                } else {
                    $scope.requestInProgress = true;

                    this.wcsService.insertCoverage($scope.urlOfCoverageToInsert, $scope.useGeneratedCoverageId).then(
                        (...args:any[])=> {
                            this.alertService.success("Successfully inserted coverage.");
                            this.$log.info(args);
                            // after insert coverage Id, it should reload GetCapabilities to add the id to the list
                            $scope.wcsStateInformation.reloadServerCapabilities = true;
                        },
                        (...args:any[])=> {
                            this.errorHandlingService.handleError(args);
                            this.$log.error(args);
                        }).finally(function () {
                        $scope.requestInProgress = false;
                    });
                }
            };
        }
    }

    interface WCSInsertCoverageControllerScope extends WCSMainControllerScope {
        urlOfCoverageToInsert:string;
        requestInProgress:boolean;
        useGeneratedCoverageId:boolean;

        insertCoverage():void;
    }
}