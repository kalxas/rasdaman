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
///<reference path="../../models/wcs/Capabilities.ts"/>
///<reference path="../../models/wcs/CoverageDescriptions.ts"/>

module rasdaman {
    /**
     * The MainController holds the state of the WCS client and orchestrates
     * interaction between the tabs and state transfer.
     * All the other controllers will inherit this controller's scope.
     */
    export class MainController {
        public static $inject = ["$scope", "$rootScope", "$state"];

        public constructor(private $scope:MainControllerScope, $rootScope:angular.IRootScopeService, $state:any) {
            this.initializeTabs($scope);

            $scope.$watch("StateInformation.ServerCapabilities", (newValue:wcs.Capabilities, oldValue:wcs.Capabilities)=> {
                if (newValue) {
                    $scope.DescribeCoverageTab.Disabled = false;
                    $scope.GetCoverageTab.Disabled = false;
                    $scope.ProcessCoverageTab.Disabled = !MainController.isProcessCoverageEnabled(newValue);
                    $scope.InsertCoverageTab.Disabled = !MainController.isCoverageTransactionEnabled(newValue);
                    $scope.DeleteCoverageTab.Disabled = !MainController.isCoverageTransactionEnabled(newValue);
                } else {
                    this.resetState();
                }
            });

            $scope.$watch("StateInformation.SelectedCoverageDescriptions", (newValue:wcs.CoverageDescriptions, oldValue:wcs.CoverageDescriptions)=> {
                $scope.GetCoverageTab.Disabled = newValue ? false : true;
            });


            $scope.Tabs = [$scope.GetCapabilitiesTab, $scope.DescribeCoverageTab, $scope.GetCoverageTab, $scope.ProcessCoverageTab, $scope.DeleteCoverageTab, $scope.InsertCoverageTab];

            $scope.StateInformation = {
                ServerCapabilities: null,
                SelectedCoverageDescriptions: null,
                SelectedGetCoverageId: null
            };

            $scope.describeCoverage = function (coverageId:string) {
                $scope.DescribeCoverageTab.Active = true;
                $rootScope.$broadcast("SelectedCoverageId", coverageId);
            };
        }

        private initializeTabs($scope:MainControllerScope) {
            $scope.GetCapabilitiesTab = {
                Heading: "GetCapabilities",
                View: "get_capabilities",
                Active: true,
                Disabled: false
            };

            $scope.DescribeCoverageTab = {
                Heading: "DescribeCoverage",
                View: "describe_coverage",
                Active: false,
                Disabled: false
            };

            $scope.GetCoverageTab = {
                Heading: "GetCoverage",
                View: "get_coverage",
                Active: false,
                Disabled: false
            };

            $scope.ProcessCoverageTab = {
                Heading: "ProcessCoverages",
                View: "process_coverages",
                Active: false,
                Disabled: false
            };

            $scope.InsertCoverageTab = {
                Heading: "InsertCoverage",
                View: "insert_coverage",
                Active: false,
                Disabled: false
            };
            $scope.DeleteCoverageTab = {
                Heading: "DeleteCoverage",
                View: "delete_coverage",
                Active: false,
                Disabled: false
            };
        }

        private resetState() {
            this.$scope.DescribeCoverageTab.Disabled = true;
            this.$scope.GetCoverageTab.Disabled = true;
            this.$scope.ProcessCoverageTab.Disabled = true;
            this.$scope.DeleteCoverageTab.Disabled = true;
            this.$scope.InsertCoverageTab.Disabled = true;
        }

        private static isProcessCoverageEnabled(serverCapabilities:wcs.Capabilities) {
            var processExtensionUri = rasdaman.Constants.PROCESSING_EXT_URI;

            return serverCapabilities.ServiceIdentification.Profile.indexOf(processExtensionUri) != -1;
        }

        private static  isCoverageTransactionEnabled(serverCapabilities:wcs.Capabilities) {
            var transactionExtensionUri = rasdaman.Constants.TRANSACTION_EXT_URI;

            return serverCapabilities.ServiceIdentification.Profile.indexOf(transactionExtensionUri) != -1;
        }
    }

    export interface MainControllerScope extends angular.IScope {
        StateInformation:{
            ServerCapabilities:wcs.Capabilities,
            SelectedCoverageDescriptions:wcs.CoverageDescriptions,
            SelectedGetCoverageId:string
        };

        Tabs:TabState[];
        GetCapabilitiesTab:TabState;
        DescribeCoverageTab:TabState;
        GetCoverageTab:TabState;
        ProcessCoverageTab:TabState;
        InsertCoverageTab:TabState;
        DeleteCoverageTab:TabState;

        //Implement a better way to navigate between tabs
        describeCoverage(coverageId:string);
    }

    interface TabState {
        Heading:string;
        View:string;
        Active:boolean;
        Disabled:boolean;
    }
}
