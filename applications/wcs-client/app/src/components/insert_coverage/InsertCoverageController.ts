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
///<reference path="../main/MainController.ts"/>

module rasdaman {
    export class InsertCoverageController {
        public static $inject = [
            "$scope",
            "$log",
            "Notification",
            "rasdaman.WCSService",
            "rasdaman.WCSErrorHandlingService"
        ];

        public constructor(private $scope:InsertCoverageControllerScope,
                           private $log:angular.ILogService,
                           private alertService:any,
                           private wcsService:rasdaman.WCSService,
                           private errorHandlingService:WCSErrorHandlingService) {
            $scope.UrlOfCoverageToInsert = null;
            $scope.RequestInProgress = false;
            $scope.UseGeneratedCoverageId = false;

            $scope.insertCoverage = ()=> {
                if ($scope.RequestInProgress) {
                    this.alertService.error("Cannot insert a coverage while another insert request is in progress.");
                } else {
                    $scope.RequestInProgress = true;

                    this.wcsService.insertCoverage($scope.UrlOfCoverageToInsert, $scope.UseGeneratedCoverageId).then(
                        (...args:any[])=> {
                            this.alertService.success("Successfully inserted coverage.");
                            this.$log.info(args);
                        },
                        (...args:any[])=> {
                            this.errorHandlingService.handleError(args);
                            this.$log.error(args);
                        }).finally(function () {
                        $scope.RequestInProgress = false;
                    });
                }
            };
        }
    }

    interface InsertCoverageControllerScope extends MainControllerScope {
        UrlOfCoverageToInsert:string;
        RequestInProgress:boolean;
        UseGeneratedCoverageId:boolean;

        insertCoverage():void;
    }
}