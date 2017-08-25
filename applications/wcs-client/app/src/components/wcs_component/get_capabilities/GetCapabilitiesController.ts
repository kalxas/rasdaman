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
            "$log",
            "rasdaman.WCSService",
            "rasdaman.WCSSettingsService",            
            "Notification",
            "rasdaman.ErrorHandlingService",
            "rasdaman.WebWorldWindService"           
        ];

        public constructor(private $scope:WCSCapabilitiesControllerScope,
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

            $scope.wcsServerEndpoint = settings.wcsEndpoint;
            // To init the Globe on this canvas           
            var canvasId = "wcsCanvasGetCapabilities";

            // to know which page are selected
            var currentPageNumber = 1;
                        
            // A callback method is called when the page button of paginator of smart table is clicked            
            // newPage starts from 1
            $scope.pageChanged = (newPage: any) => {                                
                var selectedPage = newPage - 1;
                // e.g: newPage is 1 then selectedPage is 0 and startIndex is 0 and endIndex is 10 (non inclusive in slice method)
                var startIndex = $scope.rowPerPageSmartTable * selectedPage;
                var endIndex = $scope.rowPerPageSmartTable * selectedPage + $scope.rowPerPageSmartTable;

                var coveragesExtentsCurrentPage = $scope.selectCoveragesExtentsCurrentPage(startIndex, endIndex);
                webWorldWindService.loadCoveragesExtentsOnGlobe(canvasId, coveragesExtentsCurrentPage);

                currentPageNumber = newPage;
                $scope.loadCoverageExtentsByPageNumber(currentPageNumber);
            }            


            // Load all coverages's extents on current page
            $scope.loadCoverageExtentsByPageNumber = (newPage:number)=> {
                var selectedPage = newPage - 1;
                // e.g: newPage is 1 then selectedPage is 0 and startIndex is 0 and endIndex is 10 (non inclusive in slice method)
                var startIndex = $scope.rowPerPageSmartTable * selectedPage;
                var endIndex = $scope.rowPerPageSmartTable * selectedPage + $scope.rowPerPageSmartTable;

                var coveragesExtentsCurrentPage = $scope.selectCoveragesExtentsCurrentPage(startIndex, endIndex);
                webWorldWindService.loadCoveragesExtentsOnGlobe(canvasId, coveragesExtentsCurrentPage);

                // When changing to another page, uncheck the display all footprints checkbox if it is checked
                // and only load the coverages's extents of current page      
                $scope.showAllFootprints = false;          
                $("#wcsDisplayAllFootprintsCheckbox").prop('checked', false);
            }

            // Select coverages's extents from the list of WCS Coverages on the current page.
            // NOTE: not all coverages in a page can be displayable, so they need to be filtered.
            $scope.selectCoveragesExtentsCurrentPage = (startIndex: number, endIndex: number) => {
                var coveragesCurrentPage = $scope.capabilities.contents.coverageSummary.slice(startIndex, endIndex);
                var coveragesExtentsCurrentPage = [];
                // Fetch the coverages's extents of current page by coverage Ids
                for (var i = 0; i < coveragesCurrentPage.length; i++) {
                    for (var j = 0; j < $scope.coveragesExtents.length; j++) {
                        if ($scope.coveragesExtents[j].coverageId === coveragesCurrentPage[i].coverageId) {
                            coveragesCurrentPage[i].displayFootprint = true;
                            var coverageExtent = $scope.coveragesExtents[j];
                            coverageExtent.index = j;
                            coveragesExtentsCurrentPage.push(coverageExtent);
                            break;
                        }
                    }
                }                

                // Sort the coveragesExtents in current page by the original order (area descending)
                coveragesExtentsCurrentPage.sort(function(a, b) {
                    return parseFloat(a.index) - parseFloat(b.index);
                });

                return coveragesExtentsCurrentPage;
            }

            // If a coverage can be displayed on globe, user can show/hide it's footprint by changing checkbox of current page
            $scope.displayFootprintOnGlobe = (coverageId:string)=> {     
                webWorldWindService.showHideCoverageExtentOnGlobe(canvasId, coverageId);                
            }

            // Load/Unload all coverages's extents on globe
            $scope.displayAllFootprintsOnGlobe = (status:boolean)=> {
                $scope.showAllFootprints = status;
                if (status == true) {
                    // load all footprints from all pages on globe
                    webWorldWindService.loadCoveragesExtentsOnGlobe(canvasId, $scope.coveragesExtents);
                } else {
                    // only load all footprint of current page
                    $scope.loadCoverageExtentsByPageNumber(currentPageNumber);
                }                
            }

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
                                        $scope.coveragesExtents = response.data;
                                        // Also, store the CoveragesExtents to Service class then can be used later
                                           webWorldWindService.setCoveragesExtentsArray($scope.coveragesExtents);
                                        $scope.isCoveragesExtentsOpen = true;

                                        // Load coveragesExtents of first page to WebWorldWind                                                                                
                                        var coveragesExtentsFirstPage = $scope.selectCoveragesExtentsCurrentPage(0, $scope.rowPerPageSmartTable);                                                               
                                        webWorldWindService.loadCoveragesExtentsOnGlobe(canvasId, coveragesExtentsFirstPage);
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
        // load all the coverages's extents on a specified page
        loadCoverageExtentsByPageNumber(pageNumber:number):void;
        // Show/Hide the checked coverage extent on globe of current page
        displayFootprintOnGlobe(coverageId:string):void;
        // Load all the coverages's extents on globe from all pages
        displayAllFootprintsOnGlobe(status:boolean):void;

        getServerCapabilities():void;
    }




}
