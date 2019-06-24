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
///<reference path="../../../models/wcs_model/wcs/GetCapabilities.ts"/>
///<reference path="../../wcs_component/WCSService.ts"/>
///<reference path="../AdminService.ts"/>
///<reference path="../../main/WCSMainController.ts"/>
///<reference path="../../../_all.ts"/>
///<reference path="../../wcs_component/settings/SettingsService.ts"/>

module rasdaman {
    export class AdminOWSMetadataManagementController {

        public static $inject = [    
            "$scope",
            "$rootScope",
            "$log",
            "rasdaman.WCSService",
            "rasdaman.WCSSettingsService",           
            "rasdaman.AdminService",
            "Notification",
            "rasdaman.ErrorHandlingService"        
        ];

        

        public constructor(private $scope:AdminOWSMetadataManagementControllerScope,
                           private $rootScope:angular.IRootScopeService,
                           private $log:angular.ILogService,                           
                           private wcsService:rasdaman.WCSService,                           
                           private settings:rasdaman.WCSSettingsService,                           
                           private adminService:rasdaman.AdminService,
                           private alertService:any,
                           private errorHandlingService:ErrorHandlingService) {

            // When WCS GetCapabilities button is clicked, then OWS Metadata also needs to reload its GetCapabilities
            $rootScope.$on("reloadServerCapabilities", (event:angular.IAngularEvent, value:boolean)=> {         
                $scope.getServerCapabilities();
            });

            // When logged in, load Capabilities for OWS metadata
            $rootScope.$watch("adminStateInformation.loggedIn", (newValue:boolean, oldValue:boolean)=> {
                $scope.getServerCapabilities();
            });

            $scope.getServerCapabilities = (...args: any[])=> {                 

                //Create capabilities request
                var capabilitiesRequest = new wcs.GetCapabilities();
                
                wcsService.getServerCapabilities(capabilitiesRequest)
                    .then((response:rasdaman.common.Response<wcs.Capabilities>)=> {
                            //Success handler
                            // This is output from GetCapabilities request in XML
                            $scope.capabilitiesDocument = response.document;                            
                            // This is the parsed object from XML output by wmsService
                            var capabilities = response.value;       
                            
                            // Service Identification
                            var serviceTitle = capabilities.serviceIdentification.title[0].value;
                            var abstract = capabilities.serviceIdentification.abstract[0].value;

                            $scope.serviceIdentification = new admin.ServiceIdentification(serviceTitle, abstract);

                            // Service Provider
                            var providerName = capabilities.serviceProvider.providerName;
                            var providerSite = capabilities.serviceProvider.providerSite.href;
                            var individualName = capabilities.serviceProvider.serviceContact.individualName;
                            var positionName = capabilities.serviceProvider.serviceContact.positionName;
                            var role = capabilities.serviceProvider.serviceContact.role.code;
                            var email = capabilities.serviceProvider.serviceContact.contactInfo.address.electronicMailAddress[0];
                            var voicePhone = capabilities.serviceProvider.serviceContact.contactInfo.phone.voice[0];
                            var facsimilePhone = capabilities.serviceProvider.serviceContact.contactInfo.phone.facsimile[0];
                            var hoursOfService = capabilities.serviceProvider.serviceContact.contactInfo.hoursOfService;
                            var contactInstructions = capabilities.serviceProvider.serviceContact.contactInfo.contactInstructions;
                            var city = capabilities.serviceProvider.serviceContact.contactInfo.address.city;
                            var administrativeArea = capabilities.serviceProvider.serviceContact.contactInfo.address.administrativeArea;
                            var postalCode = capabilities.serviceProvider.serviceContact.contactInfo.address.postalCode;
                            var country = capabilities.serviceProvider.serviceContact.contactInfo.address.country;

                            $scope.serviceProvider = new admin.ServiceProvider(providerName, providerSite, individualName, positionName, role,
                                                                            email, voicePhone, facsimilePhone, hoursOfService, contactInstructions,
                                                                            city, administrativeArea, postalCode, country);
                        },
                        (...args:any[])=> {
                            errorHandlingService.handleError(args);
                            $log.error(args);
                        })
                    .finally(()=> {
                    });
            };

            // Update service identification to database
            $scope.updateServiceIdentification = (...args: any[])=> {                
                adminService.updateServiceIdentification($scope.serviceIdentification).then(
                    (...args:any[])=> {
                        alertService.success("Successfully update Service Identifcation to Petascope database.");                       
                    }, (...args:any[])=> {
                        errorHandlingService.handleError(args);                            
                    }).finally(function () {                        
                });
            }

            // Update service provider to database
            $scope.updateServiceProvider = (...args: any[])=> {                
                adminService.updateServiceProvider($scope.serviceProvider).then(
                    (...args:any[])=> {
                        alertService.success("Successfully update Service Provider to Petascope database.");                       
                    }, (...args:any[])=> {
                        errorHandlingService.handleError(args);                            
                    }).finally(function () {                        
                });
            }
            
            // Logout, just show the loggin form in WSClient and hide other admin tabs
            $scope.logOut = (...args: any[])=> {
                $rootScope.adminStateInformation.loggedIn = false;
            }           
        }
    }

    interface AdminOWSMetadataManagementControllerScope extends rasdaman.AdminMainControllerScope {        
        serviceIdentification:admin.ServiceIdentification;
        serviceProvider:admin.ServiceProvider;

        getServerCapabilities():void;
        updateServiceIdentification():void;
        updateServiceProvider():void;
        logOut():void;
	    capabilitiesDocument:any;
    }
}
