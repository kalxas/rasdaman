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
 * Copyright 2003 - 2019 Peter Baumann /
 rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */

///<reference path="../../_all.ts"/>

module rasdaman {
    /**
     * This class holds the state of the OWS client and orchestrates
     * interaction between the tabs and state transfer.
     * All the other Admin controllers will inherit 
     * this ****controller's scope****.
     */
    export class RootController {
        public static $inject = ["$http", "$q", "$scope", "$rootScope",
            "$state", "rasdaman.WCSSettingsService", "rasdaman.ErrorHandlingService",
            "rasdaman.CredentialService"
        ];

        public constructor(private $http: angular.IHttpService,
            private $q: angular.IQService,
            private $scope: RootControllerScope,
            private $rootScope: angular.IRootScopeService,
            private $state: any,
            private settings: rasdaman.WCSSettingsService,
            private errorHandlingService: rasdaman.ErrorHandlingService,
            private credentialService: rasdaman.CredentialService) {

            this.initializeViews($scope);

            $rootScope.homeLoggedIn = false;
            $rootScope.usernameLoggedIn = "";

            // When logged in in the first page, then shows the main WSClient
            $rootScope.$watch("homeLoggedIn", (newValue: boolean, oldValue: boolean) => {
                if (newValue === true) {                    
                    $scope.showView($scope.wsclient, "services");
                }
            }); 


            // --------- defined functions -------------

            /**
             * If petascope enable basic authentication header then it needs to show login form
             * if user has not authenticated
             */
            $scope.checkPetascopeEnableAuthentication = function(): angular.IPromise<any> {

                var result = $q.defer();
                var requestUrl = settings.contextPath + "/admin/authisactive";

                $http.get(requestUrl)
                    .then(function(dataObj: any) {
                        var data = JSON.parse(dataObj.data);
                        result.resolve(data);
                    }, function(errorObj) {
                        // Petascope Community
                        if (errorObj.status == 404) {
                            result.resolve(false);
                        } else {
                            errorHandlingService.handleError(errorObj);
                        }
                    });

                return result.promise;
            }

            /**
             * Check if username and password of rasdaman user are valid
             */
            $scope.checkRadamanCredentials = function(): void {
                  
                // check if stored credentials are usable                        
                var credentialsDict = credentialService.credentialsDict;
                if (credentialsDict != null) {
                    var obj = credentialsDict[settings.wcsEndpoint];
                    if (obj != null) { 
                        var credential = new login.Credential(obj["username"], obj["password"]);                                                
                        var requestUrl = settings.contextPath + "/login"; 

                        $http.get(requestUrl, {
                                headers: credentialService.createBasicAuthenticationHeader(credential.username, credential.password)
                            }).then(function(dataObj: any) {
                                var data = JSON.parse(dataObj.data);                                
                                if (data) {                                    
                                    // Valid stored credentials
                                    $rootScope.homeLoggedIn = true;
                                    $rootScope.usernameLoggedIn = credential.username;

                                    $scope.showView($scope.wsclient, "services");
                                    return;
                                }                                    
                            }, function(errorObj) {
                                errorHandlingService.handleError(errorObj);
                            });
                    }
                }                    

                // username and password changed, need to reauthenticate
                $scope.showView($scope.login, "login");
            }

            // Show target view
            $scope.showView = function(viewState:ViewState, stateName:string): void {                
                $scope.selectedView = viewState;
                $state.go(stateName);
            }

            // Logout and go to login page, clear storage
            $scope.homeLogOutEvent = function() {
                credentialService.clearStorage();
                $rootScope.homeLoggedIn = false;
                // $scope.showView($scope.login, "login");
                location.reload();
            }

            // -------------------- invoke functions -------------------
            
            // Check which form should be shown
            $scope.checkPetascopeEnableAuthentication()
            .then(function(data) {
                if (data) {
                    // Petascope with enabled authentication
                    $scope.checkRadamanCredentials();
                } else {
                    // no need to authenticate if authentication not enabled                    
                    $rootScope.homeLoggedIn = false;
                    $scope.showView($scope.wsclient, "services");
                }
            });
        }

        private initializeViews($scope: RootControllerScope) {
            $scope.login = {
                view: "login"
            };
            $scope.wsclient = {
                view: "wsclient"
            };
        }

    }

    export interface RootControllerScope extends angular.IScope {
        homeLoggedIn: false;
        login: ViewState;
        wsclient: ViewState;
        selectedView: ViewState;
        
        checkPetascopeEnableAuthentication(): angular.IPromise<any>;
        checkRadamanCredentials(): void;
        showView(view:ViewState, stateName:string): void;
        homeLogOutEvent(): void;
    }

    interface ViewState {
        view: string;
    }
}
