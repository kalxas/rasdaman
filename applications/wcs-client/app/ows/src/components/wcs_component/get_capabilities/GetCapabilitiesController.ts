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
            "$window",
            "$scope",
            "$rootScope",
            "$log",
            "rasdaman.WCSService",
            "rasdaman.WCSSettingsService",            
            "Notification",
            "rasdaman.ErrorHandlingService",
            "rasdaman.WebWorldWindService"           
        ];

        public constructor(private $window,
                           private $scope:WCSCapabilitiesControllerScope,
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

            $scope.displayCoveragesDropdownItems = [{"name": "Display all coverages", "value": ""},
                                                    {"name": "Display local coverages", "value": "local"},
                                                    {"name": "Display remote coverages", "value": "remote"}
                                                ];
            $scope.selectedDisplayCoveragesByTypeDropdown = "all";                               
                            
            $scope.coveragesExtents = [];

            $scope.showAllFootprints = {isChecked: false};

            // Only display 10 rows in a smart table's page
            $scope.rowPerPageSmartTable = 10;

            $scope.wcsServerEndpoint = settings.wcsEndpoint;
            // To init the Globe on this canvas           
            var canvasId = "wcsCanvasGetCapabilities";

            // When petascope admin user logged in, show the blacklist / whitelist buttons
            $rootScope.$watch("adminStateInformation.loggedIn", (newValue:boolean, oldValue:boolean)=> {
                if (newValue) {
                    // Admin logged in
                    $scope.adminUserLoggedIn = true;
                } else {
                    // Admin logged out
                    $scope.adminUserLoggedIn = false;
                }
            });

            // NOTE: not all coverages could be loaded as geo-referenced, only possible coverages will have checkboxes nearby coveargeId
            $scope.initCheckboxesForCoverageIds = () => {
                // all coverages
                var coverageSummaryArray = $scope.capabilities.contents.coverageSummaries;
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
            $scope.getCoverageSummaryByCoverageId = (coverageId:string):wcs.CoverageSummary => {
                // all coverages
                var coverageSummaryArray = $scope.capabilities.contents.coverageSummaries;
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

                if (status == true) {
                    // Get filtered rows from smart table
                    let filteredRows = JSON.parse($window.wcsGetCapabilitiesFilteredRows);
                    $scope.hideAllFootprintsOnGlobe();

                    for (var i = 0; i < filteredRows.length; i++) {
                        var obj = filteredRows[i];
                        var covId = obj["coverageId"];
                        // load all unloaded footprints from all pages on globe                    
                        for (var j = 0; j < $scope.coveragesExtents.length; j++) {                        
                            var coverageId = $scope.coveragesExtents[j].coverageId;                        
                            if (covId === coverageId) {
                                // checkbox is checked
                                $scope.getCoverageSummaryByCoverageId(coverageId).displayFootprint = true;
                                webWorldWindService.showHideCoverageExtentOnGlobe(canvasId, coverageId);
                                break;
                            }                     
                        }                    
                    }
                } else {
                    // unload all loaded footprints from all pages on globe
                    $scope.hideAllFootprintsOnGlobe();
                }                
            }

            $scope.hideAllFootprintsOnGlobe = () => {
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

            // If a coverage is checked as blacklist, no one, except petascope admin user can see it from GetCapabilities
            $scope.handleBlackListOneCoverage = (coverageId:string) => {
                var status = $scope.getCoverageSummaryByCoverageId(coverageId).customizedMetadata.isBlackedList;
                if (status == true) {
                    // coverage is added to blacklist

                    this.wcsService.blackListOneCoverage(coverageId).then(
                        (...args:any[]) => {
                            this.alertService.success("Blacklisted coverage <b>" + coverageId + "</b>");
                        }, (...args:any[]) => {
                            this.errorHandlingService.handleError(args);
                            this.$log.error(args);
                        }).finally(function () {

                        });

                } else {
                    // coverage is removed from blacklist (whitelisted)
                    
                    this.wcsService.whiteListOneCoverage(coverageId).then(
                        (...args:any[]) => {
                            this.alertService.success("Whitelisted coverage <b>" + coverageId + "</b>");
                        }, (...args:any[]) => {
                            this.errorHandlingService.handleError(args);
                            this.$log.error(args);
                        }).finally(function () {

                        });
                }
            }

            // Handle black list all coverages button
            $scope.handleBlackListAllCoverages = () => {

                this.wcsService.blackListAllCoverages().then(
                    (...args:any[]) => {
                        this.alertService.success("Blacklisted <b>all coverages</b>");

                        // Check all checkboxes in blacklist column
                        var coverageSummaryArray = $scope.capabilities.contents.coverageSummaries;
                        for (var i = 0; i < coverageSummaryArray.length; i++) {
                            coverageSummaryArray[i].customizedMetadata.isBlackedList = true;
                        }
                    }, (...args:any[]) => {
                        this.errorHandlingService.handleError(args);
                        this.$log.error(args);
                    }).finally(function () {

                    });
                
            }

            // Handle white list all coverages button
            $scope.handleWhiteListAllCoverages = () => {

                this.wcsService.whiteListAllCoverages().then(
                    (...args:any[]) => {
                        this.alertService.success("Whitelisted <b>all coverages</b>");

                        // Uncheck all checkboxes in blacklist column
                        var coverageSummaryArray = $scope.capabilities.contents.coverageSummaries;
                        for (var i = 0; i < coverageSummaryArray.length; i++) {
                            coverageSummaryArray[i].customizedMetadata.isBlackedList = false;
                        }
                    }, (...args:any[]) => {
                        this.errorHandlingService.handleError(args);
                        this.$log.error(args);
                    }).finally(function () {

                    });
            }

            // rootScope broadcasts an event to all children controllers
            $scope.$on("reloadWCSServerCapabilities", function(event, b) {                
                $scope.getServerCapabilities();
            });

            // When deleteCoverage, insertCoverage is called sucessfully, it should reload the new capabilities
            $scope.$watch("wcsStateInformation.reloadServerCapabilities", (capabilities:wcs.Capabilities)=> {
                if ($scope.wcsStateInformation.reloadServerCapabilities == true) {
                    $scope.getServerCapabilities();
                }
                // It already reloaded, then set to false.
                $scope.wcsStateInformation.reloadServerCapabilities = false;
            });

            /**
             * From WGS84BoundingBox elements, parse them to get xmin, ymin, xmax, ymax.
             */
            $scope.parseCoveragesExtents = () => {
                let coverageSummaries = $scope.capabilities.contents.coverageSummaries;
                coverageSummaries.forEach((coverageSummary) => {
                    let coverageId = coverageSummary.coverageId;
                    
                    let wgs84BoundingBox = coverageSummary.wgs84BoundingBox;
                    // Only parse possible coverage extents to WGS84 CRS
                    if (wgs84BoundingBox != null) {
                        let lowerArrayTmp = wgs84BoundingBox.lowerCorner.split(" ");
                        let xMin = parseFloat(lowerArrayTmp[0]);
                        let yMin = parseFloat(lowerArrayTmp[1]);

                        let upperArrayTmp = wgs84BoundingBox.upperCorner.split(" ");
                        let xMax = parseFloat(upperArrayTmp[0]);
                        let yMax = parseFloat(upperArrayTmp[1]);

                        let bboxObj = {
                            "coverageId": coverageId,
                            "bbox": {
                                "xmin": xMin,
                                "ymin": yMin,
                                "xmax": xMax,
                                "ymax": yMax
                            },
                            "displayFootprint": false
                        };

                        $scope.coveragesExtents.push(bboxObj);
                    }
                });

                // Also, store the CoveragesExtents to Service class then can be used later
                webWorldWindService.setCoveragesExtentsArray($scope.coveragesExtents);
                $scope.isCoveragesExtentsOpen = true;                                    

                // Init all possible checkboxes for geo-reference coverages and set to false
                $scope.initCheckboxesForCoverageIds();

                // Prepare all coverage's extents but does not load it on WebWorldWind
                webWorldWindService.prepareCoveragesExtentsForGlobe(canvasId, $scope.coveragesExtents);              
            }

            // Handle the click event on GetCapabilities button
            $scope.handleGetServerCapabilities = () => {              
                $scope.getServerCapabilities();
                
                $scope.showAllFootprints.isChecked = false;
            }

            // Handle server capabilities request
            $scope.getServerCapabilities = (...args: any[])=> {                            
                if (!$scope.wcsServerEndpoint) {
                    alertService.error("The entered WCS endpoint is invalid.");
                    return;
                }

                // Load new coverage extents
                $scope.coveragesExtents = [];     

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

                            // for displaying coverages' footprints on WebWorldWind
                            $scope.parseCoveragesExtents();
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
                    });
            };            
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
        coveragesExtents:any[];        
        rowPerPageSmartTable:number;

        showAllFootprints:any;
        adminUserLoggedIn:boolean;

        parseCoveragesExtents():void;

        // Show/Hide the checked coverage extent on globe of current page
        displayFootprintOnGlobe(coverageId:string):void;
        // Load all the coverages's extents on globe from all pages
        displayAllFootprintsOnGlobe(status:boolean):void;

        // Blacklist / whitelist a specific coverage when admin changes on the checkbox for it
        handleBlackListOneCoverage(coverageId:string):void;

        // Handle blacklist / whiltelist all coverages buttons' events
        handleBlackListAllCoverages():void;
        handleWhiteListAllCoverages():void;

        getServerCapabilities():void;
	
	    initCheckboxesForCoverageIds():void;
        getCoverageSummaryByCoverageId(coverageId):wcs.CoverageSummary;
	
    }




}
