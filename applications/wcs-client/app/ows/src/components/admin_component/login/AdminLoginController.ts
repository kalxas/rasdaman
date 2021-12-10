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
///<reference path="../AdminService.ts"/>
///<reference path="../../../_all.ts"/>
///<reference path="../../wcs_component/settings/SettingsService.ts"/>

module rasdaman {
    export class AdminLoginController {

        public static $inject = [    
            "$scope",
            "$rootScope",
            "$log",
            "rasdaman.WCSSettingsService",           
            "rasdaman.AdminService",
            "Notification",
            "rasdaman.ErrorHandlingService"        
        ];

        public constructor(private $scope:AdminLoginControllerScope,
                           private $rootScope:angular.IRootScopeService,
                           private $log:angular.ILogService,                          
                           private settings:rasdaman.WCSSettingsService,                           
                           private adminService:rasdaman.AdminService,
                           private alertService:any,
                           private errorHandlingService:ErrorHandlingService) {

            $scope.credential = new login.Credential("", "");

            // When opening web page, check if there are stored credentials for petascope admin user
            var persitedAdminUserCredentials = adminService.getPersistedAdminUserCredentials();

            if (persitedAdminUserCredentials != null) {
                adminService.login(persitedAdminUserCredentials).then(
                    (data:any) => {                        
                        // store the list of granted roles for this admin user
                        $rootScope.adminStateInformation.roles = data;                        
                        // Stored credentials for petascope admin user still correct
                        // no need to show petascope admin user login form                        
                        $rootScope.adminStateInformation.loggedIn = true;
                    },  (...args:any[])=> {
                        
                    }
                )
            }

            // Login with Petascope admin credentials by clicking on button on admin's tab: login form
            $scope.login = (...args: any[])=> {
                adminService.login($scope.credential).then(
                    (data:any) => {
                        alertService.success("Successfully logged in.");
                        $rootScope.adminStateInformation.loggedIn = true;
                        $rootScope.adminStateInformation.roles = data;
                    
                        // store to local storage as admin logged in
                        adminService.persitAdminUserCredentials($scope.credential);
                       
                    }, (...args:any[])=> {                        
                        errorHandlingService.handleError(args);                            
                    }).finally(function () {                        
                });
            }
        }
    }

    interface AdminLoginControllerScope extends rasdaman.AdminMainControllerScope {        
        credential:login.Credential;
        login(...args: any[]):void;
        
        checkLoggedIn():void;
    }
}
