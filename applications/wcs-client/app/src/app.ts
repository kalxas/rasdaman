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

/// <reference path="_all.ts" />

// NOTE: When creating new Controller, Service, Directive classes, remember to register it to module.
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

            $stateProvider.state('wcs', {
                url: "/wcs",
                views: {
                    'get_capabilities': {
                        url: "get_capabilities",
                        templateUrl: 'src/components/get_capabilities/GetCapabilitiesView.html',
                        controller: rasdaman.GetCapabilitiesController
                    },
                    'describe_coverage': {
                        url: "describe_coverage",
                        templateUrl: 'src/components/describe_coverage/DescribeCoverageView.html',
                        controller: rasdaman.DescribeCoverageController
                    },
                    'get_coverage': {
                        templateUrl: 'src/components/get_coverage/GetCoverageView.html',
                        controller: rasdaman.GetCoverageController
                    },
                    'process_coverages': {
                        templateUrl: 'src/components/process_coverage/ProcessCoverageView.html',
                        controller: rasdaman.ProcessCoverageController
                    },
                    'insert_coverage': {
                        templateUrl: 'src/components/insert_coverage/InsertCoverageView.html',
                        controller: rasdaman.InsertCoverageController
                    },
                    'delete_coverage': {
                        templateUrl: 'src/components/delete_coverage/DeleteCoverageView.html',
                        controller: rasdaman.DeleteCoverageController
                    }
                }
            });

            $urlRouterProvider.otherwise('/wcs');

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
        .service("rasdaman.SettingsService", rasdaman.SettingsService)
        .service("rasdaman.common.SerializedObjectFactory", rasdaman.common.SerializedObjectFactory)
        .service("rasdaman.WCSService", rasdaman.WCSService)
        .service("rasdaman.WebWorldWindService", rasdaman.WebWorldWindService)
        .service("rasdaman.WCSErrorHandlingService", rasdaman.WCSErrorHandlingService)
        .controller("rasdaman.SettingsController", rasdaman.SettingsController)
        .controller("rasdaman.GetCapabilitiesController", rasdaman.GetCapabilitiesController)
        .controller("rasdaman.DescribeCoverageController", rasdaman.DescribeCoverageController)
        .controller("rasdaman.DeleteCoverageController", rasdaman.DeleteCoverageController)
        .controller("rasdaman.GetCoverageController", rasdaman.GetCoverageController)
        .controller("rasdaman.ProcessCoverageController", rasdaman.ProcessCoverageController)
        .controller("rasdaman.MainController", rasdaman.MainController)
        .directive("rangeSubsettingExtension", rasdaman.RangeSubsettingExtension)
        .directive("scalingExtension", rasdaman.ScalingExtension)
        .directive("interpolationExtension", rasdaman.InterpolationExtension)
        .directive("rasPrettyPrint", rasdaman.common.PrettyPrint)
        .directive("stringToNumberConverter", rasdaman.common.StringToNumberConverter)
        .directive("autocomplete", rasdaman.common.Autocomplete);
}
