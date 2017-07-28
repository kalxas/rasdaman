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

///<reference path="../../../assets/typings/tsd.d.ts"/>
///<reference path="../../models/wcs/GetCapabilities.ts"/>
///<reference path="../shared/WCSService.ts"/>
///<reference path="../settings/SettingsService.ts"/>
///<reference path="../web_world_wind/WebWorldWindService.ts"/>
///<reference path="../main/MainController.ts"/>
///<reference path="../../_all.ts"/>

module rasdaman {
    export class GetCapabilitiesController {

        public static $inject = [
            "$scope",
            "$log",
            "rasdaman.WCSService",
            "rasdaman.SettingsService",            
            "Notification",
            "rasdaman.WCSErrorHandlingService",
            "rasdaman.WebWorldWindService"           
        ];

        public constructor(private $scope:CapabilitiesControllerScope,
                           private $log:angular.ILogService,
                           private wcsService:rasdaman.WCSService,
                           private settings:rasdaman.SettingsService,                           
                           private alertService:any,
                           private errorHandlingService:WCSErrorHandlingService,
                           private webWorldWindService:rasdaman.WebWorldWindService
                           ) {
            $scope.IsAvailableCoveragesOpen = false;
            $scope.IsCoveragesExtentsOpen = false;
            $scope.IsServiceIdentificationOpen = false;
            $scope.IsServiceProviderOpen = false;
            $scope.IsCapabilitiesDocumentOpen = false;
            // Only display 10 rows in a smart table's page
            $scope.rowPerPageSmartTable = 10;

            $scope.WcsServerEndpoint = settings.WCSEndpoint;
            // To init the Globe on this canvas           
            var canvasId = "canvasGetCapabilities";
                        
            // A callback method is called when the page button of paginator of smart table is clicked            
            // newPage starts from 1
            $scope.pageChanged = (newPage: any) => {                                
                var selectedPage = newPage - 1;
                // e.g: newPage is 1 then selectedPage is 0 and startIndex is 0 and endIndex is 10 (non inclusive in slice method)
                var startIndex = $scope.rowPerPageSmartTable * selectedPage;
                var endIndex = $scope.rowPerPageSmartTable * selectedPage + $scope.rowPerPageSmartTable;

                var coveragesExtentsCurrentPage = $scope.selectCoveragesExtentsCurrentPage(startIndex, endIndex);
                webWorldWindService.loadCoveragesExtentsOnGlobe(canvasId, coveragesExtentsCurrentPage);
            }            

            // Select coverages's extents from the list of WCS Coverages on the current page.
            // NOTE: not all coverages in a page can be displayable, so they need to be filtered.
            $scope.selectCoveragesExtentsCurrentPage = (startIndex: number, endIndex: number) => {
                var coveragesCurrentPage = $scope.Capabilities.Contents.CoverageSummary.slice(startIndex, endIndex);
                var coveragesExtentsCurrentPage = [];
                // Fetch the coverages's extents of current page by coverage Ids
                for (var i = 0; i < coveragesCurrentPage.length; i++) {
                    for (var j = 0; j < $scope.coveragesExtents.length; j++) {
                        if ($scope.coveragesExtents[j].coverageId === coveragesCurrentPage[i].CoverageId) {
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

            $scope.getServerCapabilities = (...args: any[])=> {                
                if (!$scope.WcsServerEndpoint) {
                    alertService.error("The entered WCS endpoint is invalid.");
                    return;
                }

                //Update settings:
                settings.WCSEndpoint = $scope.WcsServerEndpoint;

                //Create capabilities request
                var capabilitiesRequest = new wcs.GetCapabilities();

                wcsService.getServerCapabilities(capabilitiesRequest)
                    .then((response:rasdaman.common.Response<wcs.Capabilities>)=> {
                            //Success handler
                            $scope.CapabilitiesDocument = response.Document;
                            $scope.Capabilities = response.Value;

                            $scope.IsAvailableCoveragesOpen = true;
                            $scope.IsServiceIdentificationOpen = true;
                            $scope.IsServiceProviderOpen = true;
                            
                            // also, make another request to GetCoveragesExtents in EPSG:4326 to display on Globe
                            wcsService.getCoveragesExtents()
                                .then((response:rasdaman.common.Response<any>)=> {
                                        //Success handler                                                                                                                                                            
                                        $scope.coveragesExtents = response.data;
                                        // Also, store the CoveragesExtents to Service class then can be used later
                                        webWorldWindService.setCoveragesExtents($scope.coveragesExtents);
                                        $scope.IsCoveragesExtentsOpen = true;

                                        // Load coveragesExtents of first page to WebWorldWind                                                                                
                                        var coveragesExtentsFirstPage = $scope.selectCoveragesExtentsCurrentPage(0, $scope.rowPerPageSmartTable);                                                               
                                        webWorldWindService.loadCoveragesExtentsOnGlobe(canvasId, coveragesExtentsFirstPage);
                                    },
                                    (...args:any[])=> {
                                        //UnSuccess handler
                                        $scope.coveragesExtents = null;
                                        $scope.IsCoveragesExtentsOpen = false;

                                        errorHandlingService.handleError(args);
                                        $log.error(args);
                                    })
                                .finally(()=> {
                                    $scope.StateInformation.GetCoveragesExtents = $scope.coveragesExtents;
                                });
                        },
                        (...args:any[])=> {
                            //Success handler
                            $scope.CapabilitiesDocument = null;
                            $scope.Capabilities = null;

                            $scope.IsAvailableCoveragesOpen = false;
                            $scope.IsServiceIdentificationOpen = false;
                            $scope.IsServiceProviderOpen = false;

                            errorHandlingService.handleError(args);
                            $log.error(args);
                        })
                    .finally(()=> {
                        $scope.StateInformation.ServerCapabilities = $scope.Capabilities;
                    });

                
            };            

            // When the constructor is called, make a call to retrieve the server capabilities.
            $scope.getServerCapabilities();
        }
    }

    interface CapabilitiesControllerScope extends rasdaman.MainControllerScope {
        WcsServerEndpoint:string;
        IsAvailableCoveragesOpen:boolean;
        IsCoveragesExtentsOpen:boolean;
        IsServiceIdentificationOpen:boolean;
        IsServiceProviderOpen:boolean;
        IsCapabilitiesDocumentOpen:boolean;
        CapabilitiesDocument:rasdaman.common.ResponseDocument;
        Capabilities:wcs.Capabilities;
        // An array to store the list of CoverageExtent objects
        coveragesExtents:any;        
        rowPerPageSmartTable:number;

        getServerCapabilities():void;
    }




}
