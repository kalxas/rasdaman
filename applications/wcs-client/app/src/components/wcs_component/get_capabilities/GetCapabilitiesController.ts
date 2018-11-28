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
///<reference path="../../web_world_wind/WebWorldWindService.ts"/>
///<reference path="../../main/WCSMainController.ts"/>
///<reference path="../../../_all.ts"/>
///<reference path="../settings/SettingsService.ts"/>

module rasdaman {
    export class WCSGetCapabilitiesController {

        public static $inject = [            
            "$scope",
            "$rootScope",
            "$log",
            "rasdaman.WCSService",
            "rasdaman.WCSSettingsService",            
            "Notification",
            "rasdaman.ErrorHandlingService",
            "rasdaman.WebWorldWindService"           
        ];

        public constructor(private $scope:WCSCapabilitiesControllerScope,
                           private $rootScope:angular.IRootScopeService,
                           private $log:angular.ILogService,                           
                           private wcsService:rasdaman.WCSService,
                           private settings:rasdaman.WCSSettingsService,                           
                           private alertService:any,
                           private errorHandlingService:ErrorHandlingService,
                           private webWorldWindService:rasdaman.WebWorldWindService
                           ) {
            $scope.isAvailableCoveragesOpen = false;
            $scope.isCoveragesExtentsOpen = false;
            $scope.isServiceIdentificationOpen = false;
            $scope.isServiceProviderOpen = false;
            $scope.isCapabilitiesDocumentOpen = false;
            // Only display 10 rows in a smart table's page
            $scope.rowPerPageSmartTable = 10;

            // Shown a warning about WebWorldWind if firefox version is 63 or 64
            $scope.showFirefoxWebWorldWindWarning = false;
            var userAgent = navigator.userAgent;
            if (navigator.userAgent.indexOf("Firefox") > 0) {                
                
                var startIndex = userAgent.indexOf("rv:");
                var endIndex = userAgent.indexOf(")");
                if (startIndex > 0 && endIndex > 0) {
                    var version = parseInt(userAgent.substring(startIndex, endIndex).split(":")[1]);

                    if (version == 63 || version == 64) {
                        $scope.showFirefoxWebWorldWindWarning = true;
                    }
                }
            }

            $scope.wcsServerEndpoint = settings.wcsEndpoint;
            // To init the Globe on this canvas           
            var canvasId = "wcsCanvasGetCapabilities";

            // NOTE: not all coverages could be loaded as geo-referenced, only possible coverages will have checkboxes nearby coveargeId
            $scope.initCheckboxesForCoverageIds = () => {
                // all coverages
                var coverageSummaryArray = $scope.capabilities.contents.coverageSummary;
                for (var i = 0; i < coverageSummaryArray.length; i++) {
                    // only geo-referenced coverages
                    for (var j = 0; j < $scope.coveragesExtents.length; j++) {
                        if ($scope.coveragesExtents[j].coverageId === coverageSummaryArray[i].coverageId) {
                            coverageSummaryArray[i].displayFootprint = false;
                            break;
                        }
                    }
                }                     
            }

            // Return a coverage's summary by coverageId
            $scope.getCoverageSummaryByCoverageId = (coverageId:string) => {
                // all coverages
                var coverageSummaryArray = $scope.capabilities.contents.coverageSummary;
                for (var i = 0; i < coverageSummaryArray.length; i++) {
                    if (coverageSummaryArray[i].coverageId == coverageId) {
                        return coverageSummaryArray[i];
                    }
                }
            }

            // If a coverage can be displayed on globe, user can show/hide it's footprint by changing checkbox of current page
            $scope.displayFootprintOnGlobe = (coverageId:string)=> {     
                webWorldWindService.showHideCoverageExtentOnGlobe(canvasId, coverageId);
            }

            // Load/Unload all coverages's extents on globe
            $scope.displayAllFootprintsOnGlobe = (status:boolean)=> {
                $scope.showAllFootprints = status;
                if (status == true) {
                    // load all unloaded footprints from all pages on globe                    
                    for (var i = 0; i < $scope.coveragesExtents.length; i++) {
                        var coverageId = $scope.coveragesExtents[i].coverageId;
                        if ($scope.coveragesExtents[i].displayFootprint == false) {
                            // checkbox is checked
                            $scope.getCoverageSummaryByCoverageId(coverageId).displayFootprint = true;
                            webWorldWindService.showHideCoverageExtentOnGlobe(canvasId, coverageId);
                        }                     
                    }                    
                } else {
                    // unload all loaded footprints from all pages on globe
                    for (var i = 0; i < $scope.coveragesExtents.length; i++) {
                        var coverageId = $scope.coveragesExtents[i].coverageId;                    
                        if ($scope.coveragesExtents[i].displayFootprint == true) {
                            // checkbox is unchecked
                            $scope.getCoverageSummaryByCoverageId(coverageId).displayFootprint = false;
                            webWorldWindService.showHideCoverageExtentOnGlobe(canvasId, coverageId);
                        }                        
                    }
                }                
            }

            // When deleteCoverage, insertCoverage is called sucessfully, it should reload the new capabilities
            $scope.$watch("wcsStateInformation.reloadServerCapabilities", (capabilities:wcs.Capabilities)=> {
                $scope.getServerCapabilities();
                // It already reloaded, then set to false.
                $scope.wcsStateInformation.reloadServerCapabilities = false;
            });

            // Handle the click event on Get Capabilities button
            $scope.getServerCapabilities = (...args: any[])=> {                            
                if (!$scope.wcsServerEndpoint) {
                    alertService.error("The entered WCS endpoint is invalid.");
                    return;
                }

                //Update settings:
                settings.wcsEndpoint = $scope.wcsServerEndpoint;

                //Create capabilities request
                var capabilitiesRequest = new wcs.GetCapabilities();

                wcsService.getServerCapabilities(capabilitiesRequest)
                    .then((response:rasdaman.common.Response<wcs.Capabilities>)=> {
                            //Success handler
                            $scope.capabilitiesDocument = response.document;
                            $scope.capabilities = response.value;

                            $scope.isAvailableCoveragesOpen = true;
                            $scope.isServiceIdentificationOpen = true;
                            $scope.isServiceProviderOpen = true;

                            // also, make another request to GetCoveragesExtents in EPSG:4326 to display on Globe
                            wcsService.getCoveragesExtents()
                                .then((response:rasdaman.common.Response<any>)=> {
                                        //Success handler
                                        $scope.coveragesExtents = response.value;
                                        // default don't load any coverage's on globe, just prepare them
                                        for (var i = 0; i < $scope.coveragesExtents.length; i++) {
                                            $scope.coveragesExtents[i].displayFootprint = false;
                                        }
                                        // Also, store the CoveragesExtents to Service class then can be used later
                                        webWorldWindService.setCoveragesExtentsArray($scope.coveragesExtents);
                                        $scope.isCoveragesExtentsOpen = true;                                    

                                        // Init all possible checkboxes for geo-reference coverages and set to false
                                        $scope.initCheckboxesForCoverageIds();
                                        // Prepare all coverage's extents but does not load it on WebWorldWind
                                        webWorldWindService.prepareCoveragesExtentsForGlobe(canvasId, $scope.coveragesExtents);
                                        // Then, load the footprint of this coverage on the globe
                                        webWorldWindService.showHideCoverageExtentOnGlobe(canvasId, $scope.selectedCoverageId);
                                    },
                                    (...args:any[])=> {
                                        //UnSuccess handler
                                        $scope.coveragesExtents = null;
                                        $scope.isCoveragesExtentsOpen = false;

                                        errorHandlingService.handleError(args);
                                        $log.error(args);
                                    })
                                .finally(()=> {
                                    $scope.wcsStateInformation.getCoveragesExtents = $scope.coveragesExtents;
                                });
                        },
                        (...args:any[])=> {
                            //Error handler
                            $scope.capabilitiesDocument = null;
                            $scope.capabilities = null;

                            $scope.isAvailableCoveragesOpen = false;
                            $scope.isServiceIdentificationOpen = false;
                            $scope.isServiceProviderOpen = false;

                            errorHandlingService.handleError(args);
                            $log.error(args);
                        })
                    .finally(()=> {
                        $scope.wcsStateInformation.serverCapabilities = $scope.capabilities;
                        // Broadcast to WMS controller to reload its server capabilities when WCS GetCapabitlies button is clicked
                        $rootScope.$broadcast("reloadServerCapabilities", true);                        
                    });
            };            

            // When the constructor is called, make a call to retrieve the server capabilities.
            $scope.getServerCapabilities();
        }
    }

    interface WCSCapabilitiesControllerScope extends rasdaman.WCSMainControllerScope {
        wcsServerEndpoint:string;
        isAvailableCoveragesOpen:boolean;
        isCoveragesExtentsOpen:boolean;
        isServiceIdentificationOpen:boolean;
        isServiceProviderOpen:boolean;
        isCapabilitiesDocumentOpen:boolean;
        capabilitiesDocument:rasdaman.common.ResponseDocument;
        capabilities:wcs.Capabilities;
        // An array to store the list of CoverageExtent objects
        coveragesExtents:any;        
        rowPerPageSmartTable:number;

        showAllFootprints:boolean;
        // Show/Hide the checked coverage extent on globe of current page
        displayFootprintOnGlobe(coverageId:string):void;
        // Load all the coverages's extents on globe from all pages
        displayAllFootprintsOnGlobe(status:boolean):void;

        getServerCapabilities():void;
	
	    selectedCoverageId:string;
	    initCheckboxesForCoverageIds():void;
        getCoverageSummaryByCoverageId(coverageId):{displayFootprint:boolean};
        
        // In Firefox version 63, WebWorldWind not working
        showFirefoxWebWorldWindWarning:boolean;
	
    }




}
