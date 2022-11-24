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
///<reference path="../../wcs_component/WCSService.ts"/>
///<reference path="../../../models/wcs_model/wcs/Capabilities.ts"/>
///<reference path="../../main/WCSMainController.ts"/>
///<reference path="../../web_world_wind/WebWorldWindService.ts"/>

module rasdaman {
    export class WCSDescribeCoverageController {
        //Makes the controller work as a tab.
        private static selectedCoverageId:string;

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

        public constructor($scope:WCSDescribeCoverageControllerScope,
                           $rootScope:angular.IRootScopeService,
                           $log:angular.ILogService,
                           wcsService:rasdaman.WCSService,
                           settings:rasdaman.WCSSettingsService,
                           alertService:any,
                           errorHandlingService:rasdaman.ErrorHandlingService,
                           webWorldWindService:rasdaman.WebWorldWindService) {            

            $scope.selectedCoverageId = null;
            $scope.newCoverageId = null;
            $scope.REGULAR_AXIS = "regular";
            $scope.IRREGULAR_AXIS = "irregular";
            $scope.NOT_AVALIABLE = "N/A";

            // default hide the div containing the Globe
            $scope.hideWebWorldWindGlobe = true;

            $scope.isCoverageIdValid = function():boolean {
                if ($scope.wcsStateInformation.serverCapabilities) {
                    var coverageSummaries = $scope.wcsStateInformation.serverCapabilities.contents.coverageSummaries;
                    for (var i = 0; i < coverageSummaries.length; i++) {
                        if (coverageSummaries[i].coverageId == $scope.selectedCoverageId) {                            
                            return true;
                        }
                    }
                }

                return false;
            };

            $rootScope.$watch("wcsSelectedGetCoverageId", (coverageId:string)=> {
                if (coverageId != null) {
                    $scope.selectedCoverageId = coverageId;
                    $scope.describeCoverage();
                }
            });

            $scope.$watch("wcsStateInformation.serverCapabilities", (capabilities:wcs.Capabilities)=> {
                if (capabilities) {
                    $scope.availableCoverageIds = [];
                    $scope.coverageCustomizedMetadatasDict = {};
                    
                    capabilities.contents.coverageSummaries.forEach((coverageSummary:wcs.CoverageSummary)=> {
                        let coverageId = coverageSummary.coverageId;
                        $scope.availableCoverageIds.push(coverageId);

                        // coverage location, size,...
                        if (coverageSummary.customizedMetadata != null) {
                            $scope.coverageCustomizedMetadatasDict[coverageId] = coverageSummary.customizedMetadata;
                        }
                    });
                }                
            });


            // when GetCoverage triggers get coverage id, this function will be called to fill data for both DescribeCoverage and GetCoverage tabs
            $scope.$watch("wcsStateInformation.selectedGetCoverageId", (getCoverageId:string)=> {
                if (getCoverageId) {
                    $scope.selectedCoverageId = getCoverageId;
                    $scope.describeCoverage();
                }
            });

            // When petascope admin user logged in, show the update coverage's metadata feature
            $rootScope.$watch("adminStateInformation.loggedIn", (newValue:boolean, oldValue:boolean)=> {
                if (newValue) {
                    // Admin logged in
                    $scope.adminUserLoggedIn = true;

                    $scope.hasRole = AdminService.hasRole($rootScope.adminStateInformation.roles, AdminService.PRIV_OWS_WCS_UPDATE_COV);
                } else {
                    // Admin logged out
                    $scope.adminUserLoggedIn = false;
                }
            });

            /**
             * Update coverage's metadata from a text file
             */
            $scope.updateCoverageMetadata = () => {
                // Get browsed file to upload
                var fileInput:any = document.getElementById("coverageMetadataUploadFile");
                var mimeType = fileInput.files[0].type;
                var requiredMimeTypes:any = ["", "text/xml", "", "application/json", "text/plain"];
                if (!requiredMimeTypes.includes(mimeType)) {
                    alertService.error("Coverage's metadata file to update must be <b>xml/json/text</b> format. Given: <b>'" + mimeType + "'</b>.");
                    return;
                }

                var formData = new FormData();
                formData.append("coverageId", $scope.selectedCoverageId);
                formData.append("fileName", fileInput.files[0]);          

                wcsService.updateCoverageMetadata(formData).then(
                    response => {
                        alertService.success("Successfully update coverage's metadata from file.");
                        // Reload DescribeCoverage to see new changes
                        $scope.describeCoverage();
                    }, (...args:any[])=> {                            
                        errorHandlingService.handleError(args);
                        $log.error(args);
                    }
                );
            }

            /**
             * Rename coverage's id
             */
            $scope.renameCoverageId = () => {    
                if ($scope.newCoverageId == null || $scope.newCoverageId.trim() == "") {
                    alertService.error("New coverage id cannot be empty.");
                    return;
                }

                var formData = new FormData();
                formData.append("coverageId", $scope.selectedCoverageId);
                formData.append("newCoverageId", $scope.newCoverageId);

                wcsService.updateCoverageMetadata(formData).then(
                    response => {
                        alertService.success("Successfully rename coverage's id.");
                        // Reload DescribeCoverage to see new changes
                        $scope.selectedCoverageId = $scope.newCoverageId;
                        $scope.newCoverageId = null;
                        $scope.describeCoverage();
                    }, (...args:any[])=> {                            
                        errorHandlingService.handleError(args);
                        $log.error(args);
                    }
                );
            }            

            /**
             * Parse coverage metadata as string and show it to a dropdown
             */
            $scope.parseCoverageMetadata = () => {
                $scope.metadata = null;
                
                // Extract the metadata from the coverage document (inside <rasdaman:covMetadata></rasdaman:covMetadata>)
                var parser = new DOMParser();
                var xmlDoc = parser.parseFromString($scope.rawCoverageDescription, "text/xml");

                var elements = xmlDoc.getElementsByTagName("rasdaman:covMetadata");
                if (elements.length == 0) {
                    // In case coverage's metadata is not created by wcst_import (e.g: INSPIRE metadata)
                    elements = xmlDoc.getElementsByTagName("gmlcov:Extension");
                }
                if (elements.length > 0) {
                    $scope.metadata = elements[0].innerHTML;

                    // Check if coverage metadata is XML / JSON format
                    for (let i = 0; i < $scope.metadata.length; i++) {
                        if ($scope.metadata[i] === "{") {
                            $scope.typeMetadata = "json";
                            break;
                        } else {
                            $scope.typeMetadata = "xml";
                            break;
                        }
                    }
                }

                // As coverage contains no metadata
                if ($scope.metadata == null) {
                    // To display empty in extra metadata dropdown
                    $scope.metadata = " ";
                    $("#btnUpdateCoverageMetadata").text("Insert metadata");
                } else {
                    // Coverage has existing metadata
                    $("#btnUpdateCoverageMetadata").text("Update metadata");
                }
            }

            $scope.describeCoverage = function () {                

                //Create describe coverage request
                var coverageIds:string[] = [];
                coverageIds.push($scope.selectedCoverageId);

                var describeCoverageRequest = new wcs.DescribeCoverage(coverageIds);
                $scope.requestUrl = settings.wcsEndpoint + "?" + describeCoverageRequest.toKVP();
                $scope.coverageBBox = "";
                $scope.axes = [];                

                // Clear selected file to upload
                $("#coverageMetadataUploadFile").val("");
                $("#uploadFileName").html("");
                $("#btnUpdateCoverageMetadata").hide();
                            
                //Retrieve coverage description
                wcsService.getCoverageDescription(describeCoverageRequest)
                    .then(
                        (response:rasdaman.common.Response<wcs.CoverageDescription>)=> {
                            // //Success handler                            
                            $scope.coverageDescription = response.value;                           
                            $scope.rawCoverageDescription = response.document.value;

                            $scope.parseCoverageMetadata();
                    
                            // Fetch the coverageExtent by coverageId to display on globe if possible
                            var coverageExtentArray = webWorldWindService.getCoveragesExtentsByCoverageId($scope.selectedCoverageId);
                            if (coverageExtentArray == null) {
                                $scope.hideWebWorldWindGlobe = true;
                            } else {
                                let bbox = "minLon=" + coverageExtentArray[0].bbox.xmin.toFixed(2) + ", minLat=" + coverageExtentArray[0].bbox.ymin.toFixed(2)
                                        +  ", maxLon=" + coverageExtentArray[0].bbox.xmax.toFixed(2) + ", maxLat=" + coverageExtentArray[0].bbox.ymax.toFixed(2);
                                $scope.coverageBBox = bbox;
                                console.log(coverageExtentArray[0]);
                                // Show coverage's extent on the globe
                                var canvasId = "wcsCanvasDescribeCoverage";
                                $scope.hideWebWorldWindGlobe = false;
                                // Also prepare for DescribeCoverage's globe with only 1 coverageExtent                                
                                webWorldWindService.prepareCoveragesExtentsForGlobe(canvasId, coverageExtentArray);
                                // Then, load the footprint of this coverage on the globe
                                webWorldWindService.showCoverageExtentOnGlobe(canvasId, $scope.selectedCoverageId);
                            }
                        },
                        (...args:any[])=> {                            
                            $scope.coverageDescription = null;

                            errorHandlingService.handleError(args);
                            $log.error(args);
                        })
                    .finally(()=> {
                        $scope.wcsStateInformation.selectedCoverageDescription = $scope.coverageDescription;
                    });
            };           
        }
    }

    interface WCSDescribeCoverageControllerScope extends WCSMainControllerScope {        
        // Not show the globe when coverage cannot reproject to EPSG:4326
        isCoverageDescriptionsDocumentOpen:boolean;
        hideWebWorldWindGlobe:boolean;

        hasRole:boolean;

        coverageDescription:wcs.CoverageDescription;
        rawCoverageDescription:string;

        availableCoverageIds:string[];
        coverageCustomizedMetadatasDict:any;
        selectedCoverageId:string;
        newCoverageId:string;

        // Array of objects
        axes:any[];

        requestUrl:string;

        coverageBBox:string

        metadata:string;
        typeMetadata:string;


        isCoverageIdValid():boolean;
        describeCoverage():void;
        getAxisResolution(number, any):string;
        getAxisType(number, any):string;
        parseCoverageMetadata():void;

        adminUserLoggedIn:boolean;
        metadataFileToUpload:string;
        updateCoverageMetadata():void;
    }
}
