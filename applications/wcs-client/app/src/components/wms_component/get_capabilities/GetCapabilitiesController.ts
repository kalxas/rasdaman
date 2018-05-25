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
///<reference path="../../../models/wms_model/wms/GetCapabilities.ts"/>
///<reference path="../../wms_component/WMSService.ts"/>
///<reference path="../../web_world_wind/WebWorldWindService.ts"/>
///<reference path="../../main/WMSMainController.ts"/>
///<reference path="../../../_all.ts"/>
///<reference path="../settings/SettingsService.ts"/>

module rasdaman {
    export class WMSGetCapabilitiesController {

        public static $inject = [
            "$rootScope",
            "$scope",
            "$log",
            "rasdaman.WMSSettingsService",       
            "rasdaman.WMSService",                 
            "Notification",
            "rasdaman.ErrorHandlingService",
            "rasdaman.WebWorldWindService"           
        ];

        public constructor(private $rootScope:angular.IRootScopeService,
                           private $scope:WMSCapabilitiesControllerScope,
                           private $log:angular.ILogService,
                           private settings:rasdaman.WMSSettingsService,                           
                           private wmsService:rasdaman.WMSService,                           
                           private alertService:any,
                           private errorHandlingService:ErrorHandlingService,
                           private webWorldWindService:rasdaman.WebWorldWindService) {
            $scope.isAvailableLayersOpen = false;            
            $scope.isServiceIdentificationOpen = false;
            $scope.isServiceProviderOpen = false;
            $scope.isCapabilitiesDocumentOpen = false;                               
            // Only display 10 rows in a smart table's page
            $scope.rowPerPageSmartTable = 10;

            $scope.wmsServerEndpoint = settings.wmsEndpoint;
            // To init the Globe on this canvas           
            var canvasId = "wmsCanvasGetCapabilities";          
            // to know which page are selected
            var currentPageNumber = 1;
            
            // A callback method is called when the page button of paginator of smart table is clicked            
            // newPage starts from 1
            $scope.pageChanged = (newPage: any) => {                                
                currentPageNumber = newPage;
                $scope.loadCoverageExtentsByPageNumber(currentPageNumber);                
            }

            $scope.display = true;

            // From the WMS's EX_GeographicBoundingBox
            // NOTE: not like WCS, all layers can be display on the globe as they are geo-referenced.            
            $scope.initCheckboxesForCoverageIds = () => {
                // all coverages
                var layerArray = $scope.capabilities.layers;
                for (var i = 0; i < layerArray.length; i++) {                        
                    layerArray[i].displayFootprint = false;
                }                     
            }


            // If a coverage can be displayed on globe, user can show/hide it's footprint by changing checkbox of current page
            $scope.displayFootprintOnGlobe = (coverageId:string)=> {     
                webWorldWindService.showHideCoverageExtentOnGlobe(canvasId, coverageId);                
            }

            // Load/Unload all coverages's extents on globe
            $scope.displayAllFootprintsOnGlobe = (status:boolean)=> {
                // Array of coverageExtents belong to WMS layers
                $scope.showAllFootprints = status;
                if (status == true) {
                    // load all unloaded footprints from all pages on globe                    
                    for (var i = 0; i < $scope.capabilities.layers.length; i++) {
                        var coverageExtent = $scope.capabilities.layers[i].coverageExtent;
                        var coverageId = coverageExtent.coverageId;
                        if (coverageExtent.displayFootprint == false) {
                            // checkbox is checked
                            $scope.capabilities.layers[i].displayFootprint = true;
                            webWorldWindService.showHideCoverageExtentOnGlobe(canvasId, coverageId);
                        }
                    }
                } else {
                    // unload all loaded footprints from all pages on globe
                    for (var i = 0; i <  $scope.capabilities.layers.length; i++) {
                        var coverageExtent = $scope.capabilities.layers[i].coverageExtent;
                        var coverageId = coverageExtent.coverageId;
                        if (coverageExtent.displayFootprint == true) {
                            // checkbox is unchecked
                            $scope.capabilities.layers[i].displayFootprint = false;
                            webWorldWindService.showHideCoverageExtentOnGlobe(canvasId, coverageId);
                        }
                    }
                }
            }            

            // When WMS insertStyle, updateStyle, deleteStyle is called sucessfully, it should reload the new capabilities            
            $scope.$watch("wmsStateInformation.reloadServerCapabilities", (capabilities:wms.Capabilities)=> {
                $scope.getServerCapabilities();
                // It already reloaded, then set to false.
                $scope.wmsStateInformation.reloadServerCapabilities = false;
            });            
            
            // Handle the click event on GetCoverage button
            $scope.getServerCapabilities = (...args: any[])=> {                
                if (!$scope.wmsServerEndpoint) {
                    alertService.error("The entered WMS endpoint is invalid.");
                    return;
                }

                //Update settings:
                settings.wmsEndpoint = $scope.wmsServerEndpoint;
                //Reload the full WMS URL
                settings.setWMSFullEndPoint();   

                //Create capabilities request
                var capabilitiesRequest = new wms.GetCapabilities();

                wmsService.getServerCapabilities(capabilitiesRequest)
                    .then((response:rasdaman.common.Response<wms.Capabilities>)=> {
                            //Success handler
                            // This is output from GetCapabilities request in XML
                            $scope.capabilitiesDocument = response.document;                            
                            // This is the parsed object from XML output by wmsService
                            $scope.capabilities = response.value;

                            // If a GetCapabilities succeeds, all dropdown boxes should open
                            $scope.isAvailableLayersOpen = true;
                            $scope.isServiceIdentificationOpen = true;
                            $scope.isServiceProviderOpen = true;                            
                                                        
                            // NOTE: WMS does not have the request GetCoverageExtents to fetch the reprojected coverages's extents in EPSG:4326
                            // It already has the EX_GeographicBoundingBox element of each layer from GetCapabilities request.
                            // But, WMS still needs to convert the EX_GeographicBoundingBox the same outcome (CoverageExtent) to be displayable on globe.                            
                            $scope.initCheckboxesForCoverageIds();
                            
                            var coverageExtentArray = [];
                            for (var i = 0; i < $scope.capabilities.layers.length; i++) {
                                coverageExtentArray.push($scope.capabilities.layers[i].coverageExtent);
                            }
                            webWorldWindService.prepareCoveragesExtentsForGlobe(canvasId, coverageExtentArray);                            
                        },
                        (...args:any[])=> {
                            //Error handler
                            $scope.capabilitiesDocument = null;
                            $scope.capabilities = null;

                            // If a GetCapabilities failed, all dropdown boxes should close
                            $scope.isAvailableLayersOpen = false;
                            $scope.isServiceIdentificationOpen = false;
                            $scope.isServiceProviderOpen = false;

                            errorHandlingService.handleError(args);
                            $log.error(args);
                        })
                    .finally(()=> {
                        $scope.wmsStateInformation.serverCapabilities = $scope.capabilities;
                    });
            };            

            // When the constructor is called, make a call to retrieve the server capabilities.
            $scope.getServerCapabilities();
        }
    }

    interface WMSCapabilitiesControllerScope extends rasdaman.WMSMainControllerScope {
        wmsServerEndpoint:string;
        capabilitiesDocument:rasdaman.common.ResponseDocument;
        capabilities:wms.Capabilities;                            
        rowPerPageSmartTable:number;
        showAllFootprints:boolean;
        
        isAvailableLayersOpen:boolean;        
        isServiceIdentificationOpen:boolean;
        isServiceProviderOpen:boolean;
        isCapabilitiesDocumentOpen:boolean;
        
        // load all the coverages's extents on a specified page
        loadCoverageExtentsByPageNumber(pageNumber:number):void;

        // Show/Hide the checked coverage extent on globe of current page
        displayFootprintOnGlobe(coverageId:string):void;

        // Load all the coverages's extents on globe from all pages
        displayAllFootprints(status:boolean):void;

        getServerCapabilities():void;

        display:boolean;
    }
}
