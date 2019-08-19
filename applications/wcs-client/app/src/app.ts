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

/// <reference path="_all.ts" />

// NOTE: When creating new Controller, Service, Directive classes, remember to register it to module.
// to avoid error: Error: $injector:unpr
module rasdaman {
    "use strict";
    export class AngularConfig {
        public static $inject = [
            "$httpProvider",            
            "$stateProvider",
            "$locationProvider",
            "$urlRouterProvider",
            "NotificationProvider"
        ];

        constructor($httpProvider:any, $stateProvider:any, $locationProvider:any, $urlRouterProvider:any, NotificationProvider:any) {
            //Enable cross domain calls
            $httpProvider.defaults.useXDomain = true;

            // NOTE about nested views: @services https://stackoverflow.com/a/21050093
            // and multiple view routers: https://jsfiddle.net/awolf2904/Lmsumk2v/
       
            // Routing for WCS, WMS controllers, views
            $stateProvider
                .state("login", {
                    url: "",
                    views: {

                        "login": {
                            templateUrl: "src/components/login_component/Login.html",
                            controller: rasdaman.LoginController
                        }

                    }
                })

                .state("services", {
                    url: "services",
                    views: {                   

                        // Main view 
                        "wsclient": {
                            // NOTE: don't add RootController here as it is added already in index.html)
                            // Otherwise constructor of RootController will be invoked twice
                            templateUrl: "wsclient.html"
                        },

                        // WCS
                        'get_capabilities@services': {
                            url: "get_capabilities",
                            templateUrl: 'src/components/wcs_component/get_capabilities/GetCapabilitiesView.html',
                            controller: rasdaman.WCSGetCapabilitiesController
                        },
                        'describe_coverage@services': {
                            url: "describe_coverage",
                            templateUrl: 'src/components/wcs_component/describe_coverage/DescribeCoverageView.html',
                            controller: rasdaman.WCSDescribeCoverageController
                        },
                        'get_coverage@services': {
                            templateUrl: 'src/components/wcs_component/get_coverage/GetCoverageView.html',
                            controller: rasdaman.WCSGetCoverageController
                        },
                        'process_coverages@services': {
                            templateUrl: 'src/components/wcs_component/process_coverage/ProcessCoverageView.html',
                            controller: rasdaman.WCSProcessCoverageController
                        },
                        'insert_coverage@services': {
                            templateUrl: 'src/components/wcs_component/insert_coverage/InsertCoverageView.html',
                            controller: rasdaman.WCSInsertCoverageController
                        },
                        'delete_coverage@services': {
                            templateUrl: 'src/components/wcs_component/delete_coverage/DeleteCoverageView.html',
                            controller: rasdaman.WCSDeleteCoverageController
                        },

                        // WMS
                        'wms_get_capabilities@services': {
                            url: "wms_get_capabilities",
                            templateUrl: 'src/components/wms_component/get_capabilities/GetCapabilitiesView.html',
                            controller: rasdaman.WMSGetCapabilitiesController
                        },
                        'wms_describe_layer@services': {
                            url: "wms_describe_layer",
                            templateUrl: 'src/components/wms_component/describe_layer/DescribeLayerView.html',
                            controller: rasdaman.WMSDescribeLayerController
                        },

                        // Admin
                        'admin_login@services': {
                            url: "admin_login",
                            templateUrl: 'src/components/admin_component/login/AdminLoginView.html',
                            controller: rasdaman.AdminLoginController
                        }, 
                        'admin_ows_metadata_management@services': {
                            url: "admin_ows_metadata_management",
                            templateUrl: 'src/components/admin_component/ows_metadata_management/AdminOWSMetadataManagementView.html',
                            controller: rasdaman.AdminOWSMetadataManagementController
                        }
                        
                    }                
                });

            NotificationProvider.setOptions({
                delay: 10000,
                startTop: 20,
                startRight: 10,
                verticalSpacing: 20,
                horizontalSpacing: 20,
                positionX: 'right',
                positionY: 'top'
            });

            $.fn.followTo = function ( pos ) {
                var $window = $(Window);
                
                $window.scroll(function(e){
                    
                    if ($window.scrollTop() > pos) {
                        $('body').css('background-attachment', 'fixed');
                        $('body').css('background-position', 'top -201px center');
                            
                    } else {
                        $('body').css('background-attachment', 'absolute');
                        $('body').css('background-position', 'top ' + -$window.scrollTop() + 'px center');
                    }
                });
            };

           // $('body').followTo(210);
        }
    }

    // Register Service, Controller, Directive classes to module
    var wcsClient = angular
        .module(rasdaman.Constants.APP_NAME, ["ngRoute",
            "ngAnimate",
            "ngSanitize",
            "ui.bootstrap",
            "smart-table",
            "ui.router",
            "ui-notification",
            "ui.codemirror",
            "luegg.directives",
            "nvd3"])
        .config(AngularConfig)        
        // NOTE: remember to add these types in app/src/components/_component.ts or here will have error not found type
        .service("rasdaman.common.SerializedObjectFactory", rasdaman.common.SerializedObjectFactory)
        .service("rasdaman.CredentialService", rasdaman.CredentialService)
        .service("rasdaman.WCSService", rasdaman.WCSService)
        .service("rasdaman.WCSSettingsService", rasdaman.WCSSettingsService)
        .service("rasdaman.WMSService", rasdaman.WMSService)
        .service("rasdaman.WMSSettingsService", rasdaman.WMSSettingsService)
        .service("rasdaman.AdminService", rasdaman.AdminService)
        .service("rasdaman.WebWorldWindService", rasdaman.WebWorldWindService)
        .service("rasdaman.ErrorHandlingService", rasdaman.ErrorHandlingService)
        .controller("rasdaman.RootController", rasdaman.RootController)
        .controller("rasdaman.LoginController", rasdaman.LoginController)        
        .controller("rasdaman.WCSMainController", rasdaman.WCSMainController)
        .controller("rasdaman.WCSSettingsController", rasdaman.WCSSettingsController)
        .controller("rasdaman.WCSGetCapabilitiesController", rasdaman.WCSGetCapabilitiesController)
        .controller("rasdaman.WCSDescribeCoverageController", rasdaman.WCSDescribeCoverageController)
        .controller("rasdaman.WCSDeleteCoverageController", rasdaman.WCSDeleteCoverageController)
        .controller("rasdaman.WCSGetCoverageController", rasdaman.WCSGetCoverageController)
        .controller("rasdaman.WCSProcessCoverageController", rasdaman.WCSProcessCoverageController)        
        .controller("rasdaman.WMSMainController", rasdaman.WMSMainController)
        .controller("rasdaman.AdminMainController", rasdaman.AdminMainController)
        .directive("rangeSubsettingExtension", rasdaman.WCSRangeSubsettingExtension)
        .directive("scalingExtension", rasdaman.WCSScalingExtension)
        .directive("interpolationExtension", rasdaman.WCSInterpolationExtension)
        .directive("crsExtension", rasdaman.WCSCRSExtension)
        .directive("clippingExtension", rasdaman.WCSClippingExtension)
        .directive("wwdDisplay", rasdaman.WebWorldWindDisplayWidget)
        .directive("rasPrettyPrint", rasdaman.common.PrettyPrint)
        .directive("stringToNumberConverter", rasdaman.common.StringToNumberConverter)
        .directive("autocomplete", rasdaman.common.Autocomplete)
        .directive("scrollToBottom", rasdaman.common.scrollToBottom);
}
