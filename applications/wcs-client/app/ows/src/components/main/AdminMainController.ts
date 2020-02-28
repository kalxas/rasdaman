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

///<reference path="../../../assets/typings/tsd.d.ts"/>

module rasdaman {
    /**
     * This class holds the state of the OWS client and orchestrates
     * interaction between the tabs and state transfer.
     * All the other Admin controllers will inherit 
     * this ****controller's scope****.
     */
    export class AdminMainController {
        public static $inject = ["$scope", "$rootScope", "$state"];

        public constructor(private $scope:AdminMainControllerScope, $rootScope:angular.IRootScopeService, $state:any) {
            this.initializeTabs($scope);

            $rootScope.adminStateInformation = {
                loggedIn:false
            }
           
            $rootScope.loggedIn = false;
            // default show only login tab
            $scope.tabs = [$scope.adminLogin];
            
            $rootScope.$watch("adminStateInformation.loggedIn", (newValue:boolean, oldValue:boolean)=> {
                // logged in, show other tabs
                if (newValue) {
                    $scope.tabs = [$scope.adminOWSMetadataManagement];
                } else {
                    // logged out, show login tab
                    $scope.tabs = [$scope.adminLogin];
                }
            });
        }

        private initializeTabs($scope:AdminMainControllerScope) {     
            $scope.adminLogin = {
                heading: "Login",
                view: "admin_login",
                active: true,
                disabled: false
            }
            $scope.adminOWSMetadataManagement = {
                heading: "OWS Metadata Management",
                view: "admin_ows_metadata_management",
                active: true,
                disabled: false
            };
        }
    }

    export interface AdminMainControllerScope extends angular.IScope {
        adminStateInformation:{
            loggedIn:boolean
        };
        loggedIn:boolean;
        tabs:TabState[];
        adminLogin:TabState;
        adminOWSMetadataManagement:TabState;
    }

    interface TabState {
        heading:string;
        view:string;
        active:boolean;
        disabled:boolean;
    }    
}
