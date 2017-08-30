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

            // Load all coverages's extents on current page
            $scope.loadCoverageExtentsByPageNumber = (newPage:number)=> {
                var selectedPage = newPage - 1;
                // e.g: newPage is 1 then selectedPage is 0 and startIndex is 0 and endIndex is 10 (non inclusive in slice method)
                var startIndex = $scope.rowPerPageSmartTable * selectedPage;
                var endIndex = $scope.rowPerPageSmartTable * selectedPage + $scope.rowPerPageSmartTable;

                var coveragesExtentsCurrentPage = $scope.selectCoveragesExtentsCurrentPage(startIndex, endIndex);
                webWorldWindService.loadCoveragesExtentsOnGlobe(canvasId, coveragesExtentsCurrentPage);
                
                $("#wmsDisplayAllFootprintsCheckbox").prop('checked', false);
            }

            // From the WMS's EX_GeographicBoundingBox
            // NOTE: not like WCS, all layers can be display on the globe as they are geo-referenced.            
            $scope.selectCoveragesExtentsCurrentPage = (startIndex:number, endIndex:number) => {
                var layersCurrentPage = $scope.capabilities.layers.slice(startIndex, endIndex);
                var coverageExtentsCurrentPage = [];
                for (var i = 0; i < layersCurrentPage.length; i++) {
                    layersCurrentPage[i].displayFootprint = true;
                    coverageExtentsCurrentPage.push(layersCurrentPage[i].coverageExtent);                    
                }

                return coverageExtentsCurrentPage;
            }    

            // If a coverage can be displayed on globe, user can show/hide it's footprint by changing checkbox of current page
            $scope.displayFootprintOnGlobe = (coverageId:string)=> {     
                webWorldWindService.showHideCoverageExtentOnGlobe(canvasId, coverageId);                
            }

            // Load/Unload all coverages's extents on globe
            $scope.displayAllFootprintsOnGlobe = (status:boolean)=> {
                // Array of coverageExtents belong to WMS layers
                var coveragesExtentsArray = [];
                for (var i = 0; i < $scope.capabilities.layers.length; i++) {
                    $scope.capabilities.layers[i].displayFootprint = true;
                    coveragesExtentsArray.push($scope.capabilities.layers[i].coverageExtent);
                }

                $scope.showAllFootprints = status;
                if (status == true) {
                    // load all footprints from all pages on globe
                    webWorldWindService.loadCoveragesExtentsOnGlobe(canvasId, coveragesExtentsArray);
                } else {
                    // only load all footprint of current page
                    $scope.loadCoverageExtentsByPageNumber(currentPageNumber);
                }                
            }

            $rootScope.$on("wcsSelectedGetCoverageId", (event:angular.IAngularEvent, coverageId:string)=> {                
                $scope.selectedCoverageId = coverageId;
                $scope.describeCoverage();
            });

            // When WCS GetCapabilities button is clicked, then WMS also needs to reload its GetCapabilities
            $rootScope.$on("reloadServerCapabilities", (event:angular.IAngularEvent, value:boolean)=> {                
                $scope.getServerCapabilities();
            });

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
                            var coveragesExtentsFirstPage = $scope.selectCoveragesExtentsCurrentPage(0, $scope.rowPerPageSmartTable);                                                               
                            webWorldWindService.loadCoveragesExtentsOnGlobe(canvasId, coveragesExtentsFirstPage);
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
