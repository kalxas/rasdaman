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
///<reference path="../../models/wcs/GetCapabilities.ts"/>
///<reference path="../shared/WCSService.ts"/>
///<reference path="../settings/SettingsService.ts"/>
///<reference path="../main/MainController.ts"/>

module rasdaman {
    export class GetCapabilitiesController {

        public static $inject = [
            "$scope",
            "$log",
            "rasdaman.WCSService",
            "rasdaman.SettingsService",
            "Notification",
            "rasdaman.WCSErrorHandlingService"
        ];

        public constructor(private $scope:CapabilitiesControllerScope,
                           private $log:angular.ILogService,
                           private wcsService:rasdaman.WCSService,
                           private settings:rasdaman.SettingsService,
                           private alertService:any,
                           private errorHandlingService:WCSErrorHandlingService) {
            $scope.IsAvailableCoveragesOpen = false;
            $scope.IsServiceIdentificationOpen = false;
            $scope.IsServiceProviderOpen = false;
            $scope.IsCapabilitiesDocumentOpen = false;

            $scope.WcsServerEndpoint = settings.WCSEndpoint;

            $scope.getServerCapabilities = ()=> {
                if (!$scope.WcsServerEndpoint) {
                    alertService.error("The entered WCS endpoint is invalid.");
                    return;
                }

                //Update settings:
                settings.WCSEndpoint = $scope.WcsServerEndpoint;

                //Create capabilities request
                var capabilitiesRequest = new wcs.GetCapabilities();

                wcsService.getServerCapabilities(capabilitiesRequest)
                    .then((response:rasdaman.common.Response<wcs.Capabilities>)=> {
                            //Success handler
                            $scope.CapabilitiesDocument = response.Document;
                            $scope.Capabilities = response.Value;

                            $scope.IsAvailableCoveragesOpen = true;
                            $scope.IsServiceIdentificationOpen = true;
                            $scope.IsServiceProviderOpen = true;
                        },
                        (...args:any[])=> {
                            //Success handler
                            $scope.CapabilitiesDocument = null;
                            $scope.Capabilities = null;

                            $scope.IsAvailableCoveragesOpen = false;
                            $scope.IsServiceIdentificationOpen = false;
                            $scope.IsServiceProviderOpen = false;

                            errorHandlingService.handleError(args);
                            $log.error(args);
                        })
                    .finally(()=> {
                        $scope.StateInformation.ServerCapabilities = $scope.Capabilities;
                    });
            };

            // When the constructor is called, make a call to retrieve the server capabilities.
            $scope.getServerCapabilities();
        }
    }

    interface CapabilitiesControllerScope extends rasdaman.MainControllerScope {
        WcsServerEndpoint:string;

        IsAvailableCoveragesOpen:boolean;
        IsServiceIdentificationOpen:boolean;
        IsServiceProviderOpen:boolean;
        IsCapabilitiesDocumentOpen:boolean;
        CapabilitiesDocument:rasdaman.common.ResponseDocument;
        Capabilities:wcs.Capabilities;

        getServerCapabilities():void;
    }
}