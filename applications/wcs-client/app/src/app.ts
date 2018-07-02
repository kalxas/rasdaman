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
            "$urlRouterProvider",
            "$stateProvider",
            "NotificationProvider"
        ];

        constructor($httpProvider:any, $urlRouterProvider:any, $stateProvider:any, NotificationProvider:any) {
            //Enable cross domain calls
            $httpProvider.defaults.useXDomain = true;

            // Routing for WCS, WMS controllers, views
            $stateProvider.state('services', {
                url: "",
                views: {
                    // WCS
                    'get_capabilities': {
                        url: "get_capabilities",
                        templateUrl: 'src/components/wcs_component/get_capabilities/GetCapabilitiesView.html',
                        controller: rasdaman.WCSGetCapabilitiesController
                    },
                    'describe_coverage': {
                        url: "describe_coverage",
                        templateUrl: 'src/components/wcs_component/describe_coverage/DescribeCoverageView.html',
                        controller: rasdaman.WCSDescribeCoverageController
                    },
                    'get_coverage': {
                        templateUrl: 'src/components/wcs_component/get_coverage/GetCoverageView.html',
                        controller: rasdaman.WCSGetCoverageController
                    },
                    'process_coverages': {
                        templateUrl: 'src/components/wcs_component/process_coverage/ProcessCoverageView.html',
                        controller: rasdaman.WCSProcessCoverageController
                    },
                    'insert_coverage': {
                        templateUrl: 'src/components/wcs_component/insert_coverage/InsertCoverageView.html',
                        controller: rasdaman.WCSInsertCoverageController
                    },
                    'delete_coverage': {
                        templateUrl: 'src/components/wcs_component/delete_coverage/DeleteCoverageView.html',
                        controller: rasdaman.WCSDeleteCoverageController
                    },

                    // WMS
                    'wms_get_capabilities': {
                        url: "wms_get_capabilities",
                        templateUrl: 'src/components/wms_component/get_capabilities/GetCapabilitiesView.html',
                        controller: rasdaman.WMSGetCapabilitiesController
                    },
                    'wms_describe_layer': {
                        url: "wms_describe_layer",
                        templateUrl: 'src/components/wms_component/describe_layer/DescribeLayerView.html',
                        controller: rasdaman.WMSDescribeLayerController
                    },

                    // Admin
                    'admin_login': {
                        url: "admin_login",
                        templateUrl: 'src/components/admin_component/login/AdminLoginView.html',
                        controller: rasdaman.AdminLoginController
                    }, 
                    'admin_ows_metadata_management': {
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
        .service("rasdaman.WCSService", rasdaman.WCSService)
        .service("rasdaman.WCSSettingsService", rasdaman.WCSSettingsService)
        .service("rasdaman.WMSService", rasdaman.WMSService)
        .service("rasdaman.WMSSettingsService", rasdaman.WMSSettingsService)
        .service("rasdaman.AdminService", rasdaman.AdminService)
        .service("rasdaman.WebWorldWindService", rasdaman.WebWorldWindService)
        .service("rasdaman.ErrorHandlingService", rasdaman.ErrorHandlingService)
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
        .directive("scrollToBottom", rasdaman.common.scrollToBottom)
        .directive("autocomplete", rasdaman.common.Autocomplete);
}
