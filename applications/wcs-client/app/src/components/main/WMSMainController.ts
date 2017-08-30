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
///<reference path="../../models/wms_model/wms/Capabilities.ts"/>

module rasdaman {
    /**
     * This class holds the state of the OWS client and orchestrates
     * interaction between the tabs and state transfer.
     * All the other WMS controllers will inherit 
     * this ****controller's scope****.
     */
    export class WMSMainController {
        public static $inject = ["$scope", "$rootScope", "$state"];

        public constructor(private $scope:WMSMainControllerScope, $rootScope:angular.IRootScopeService, $state:any) {
            this.initializeTabs($scope);
           
            $scope.tabs = [$scope.wmsGetCapabilitiesTab, $scope.wmsDescribeLayerTab];
                        
            // When click on the layerName in the table of GetCapabilities tab,
            // it will change to DescribeLayer tab and get metadata for this layer.
            $scope.describeLayer = function(layerName:string) {
                $scope.wmsDescribeLayerTab.active = true;
                $rootScope.$broadcast("wmsSelectedLayerName", layerName);
            };

            // NOTE: must initialize wmsStateInformation first or watcher for serverCapabilities in GetCapabilities
            // from DescribeLayer controller will not work and return null.
            $scope.wmsStateInformation = {
                serverCapabilities: null,
                reloadServerCapabilities: null
            };
        }

        private initializeTabs($scope:WMSMainControllerScope) {            
            $scope.wmsGetCapabilitiesTab = {
                heading: "GetCapabilities",
                view: "wms_get_capabilities",
                active: true,
                disabled: false
            };

            $scope.wmsDescribeLayerTab = {
                heading: "DescribeLayer",
                view: "wms_describe_layer",
                active: false,
                disabled: false
            };
        }
    }

    export interface WMSMainControllerScope extends angular.IScope {
        wmsStateInformation:{
            serverCapabilities: wms.Capabilities,
            reloadServerCapabilities: boolean
        };

        tabs:TabState[];
        wmsGetCapabilitiesTab:TabState;
        wmsDescribeLayerTab:TabState;
        wmsGetLayerTab:TabState;        
    }

    interface TabState {
        heading:string;
        view:string;
        active:boolean;
        disabled:boolean;
    }    
}
