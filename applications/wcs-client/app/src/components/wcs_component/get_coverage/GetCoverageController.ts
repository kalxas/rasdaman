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
///<reference path="../../main/WCSMainController.ts"/>
///<reference path="../../../models/wcs_model/wcs/_wcs.ts"/>
///<reference path="../../web_world_wind/WebWorldWindService.ts"/>

module rasdaman {
    export class WCSGetCoverageController {
        //Makes the controller work as a tab.
        private static selectedCoverageId:string;

        public static $inject = [
            "$scope",
            "$rootScope",
            "$log",
            "rasdaman.WCSService",
            "Notification",
            "rasdaman.WebWorldWindService"
        ];

        public constructor($scope:WCSGetCoverageControllerScope,
                           $rootScope:angular.IRootScopeService,
                           $log:angular.ILogService,
                           wcsService:rasdaman.WCSService,
                           alertService:any,
                           webWorldWindService:rasdaman.WebWorldWindService) {
            $scope.selectedCoverageId = null;

            $scope.isGlobeOpen = false;
            $scope.isGetCoverageHideGlobe = true;

            $scope.isCoverageIdValid = ()=> {
                if ($scope.wcsStateInformation.serverCapabilities) {
                    var coverageSummaries = $scope.wcsStateInformation.serverCapabilities.contents.coverageSummary;
                    for (var i = 0; i < coverageSummaries.length; ++i) {
                        if (coverageSummaries[i].coverageId == $scope.selectedCoverageId) {
                            return true;
                        }
                    }
                }

                return false;
            };

            /*
            $rootScope.$on("SelectedCoverageId", (event:angular.IAngularEvent, coverageId:string)=> {
                $scope.SelectedCoverageId = coverageId;
                $scope.describeCoverage();
            }); */

            $scope.$watch("wcsStateInformation.serverCapabilities", (capabilities:wcs.Capabilities)=> {
                if (capabilities) {
                    $scope.availableCoverageIds = [];
                    capabilities.contents.coverageSummary.forEach((coverageSummary:wcs.CoverageSummary)=> {
                        $scope.availableCoverageIds.push(coverageSummary.coverageId);
                    });
                }
            });

            $scope.loadCoverageExtentOnGlobe = function() {
                // Fetch the coverageExtent by coverageId to display on globe if possible
                var coverageExtentArray = webWorldWindService.getCoveragesExtentsByCoverageId($scope.selectedCoverageId);                            
                if (coverageExtentArray == null) {
                    $scope.isGetCoverageHideGlobe = true;
                } else {
                    // Show covearge's extent on the globe
                    var canvasId = "wcsCanvasGetCoverage";
                    $scope.isGetCoverageHideGlobe = false;
                    // Also prepare for GetCoverage's globe with only 1 coverageExtent                    
                    webWorldWindService.prepareCoveragesExtentsForGlobe(canvasId, coverageExtentArray);
                    // Then, load the footprint of this coverage on the globe
                    webWorldWindService.showHideCoverageExtentOnGlobe(canvasId, $scope.selectedCoverageId);
                    // And look at the coverage's center on globe
                    webWorldWindService.gotoCoverageExtentCenter(canvasId, coverageExtentArray);
                }                                
            }

            $scope.getCoverageClickEvent = function () {
                if (!$scope.isCoverageIdValid()) {
                    alertService.error("The entered coverage ID is invalid.");
                    return;
                }
                // trigger the DescribeCoverage in DescribeCoverageController to fill the data to both DescribeCoverage and GetCoverage tabs
                $scope.wcsStateInformation.selectedGetCoverageId = $scope.selectedCoverageId;                
                // $scope.$digest();

                // load the coverage extent on the globe
                $scope.loadCoverageExtentOnGlobe();
            }
           

            $scope.$watch("wcsStateInformation.selectedCoverageDescriptions",
                (coverageDescriptions:wcs.CoverageDescriptions)=> {
                    if (coverageDescriptions && coverageDescriptions.coverageDescription) {
                        $scope.coverageDescription = $scope.wcsStateInformation.selectedCoverageDescriptions.coverageDescription[0];
                        $scope.selectedCoverageId = $scope.coverageDescription.coverageId;

                        $scope.getCoverageTabStates = {                            
                            isCoreOpen: true,
                            isRangeSubsettingOpen: false,
                            isRangeSubsettingSupported: WCSGetCoverageController.isRangeSubsettingSupported($scope.wcsStateInformation.serverCapabilities),
                            isScalingOpen: false,
                            isScalingSupported: WCSGetCoverageController.isScalingSupported($scope.wcsStateInformation.serverCapabilities),
                            isInterpolationOpen: false,
                            isInterpolationSupported: WCSGetCoverageController.isInterpolationSupported($scope.wcsStateInformation.serverCapabilities),
                            isCRSOpen: false,
                            isCRSSupported: WCSGetCoverageController.isCRSSupported($scope.wcsStateInformation.serverCapabilities),
                            isClippingOpen: false,
                            // TODO: when clipping is accepted from OGC, get an URI to schema from WCS GetCapabilities.
                            isClippingSupported: true
                        };

                        $scope.core = {
                            slices: [],
                            trims: [],
                            isTrimSelected: [],
                            isMultiPartFormat: false,
                            selectedCoverageFormat: $scope.wcsStateInformation.serverCapabilities.serviceMetadata.formatSupported[0],
                            requestUrl: null
                        };

                        var numberOfAxis = $scope.coverageDescription.boundedBy.envelope.lowerCorner.values.length;
                        for (var i = 0; i < numberOfAxis; ++i) {
                            var dimension = $scope.coverageDescription.boundedBy.envelope.axisLabels[i];
                            var min = $scope.coverageDescription.boundedBy.envelope.lowerCorner.values[i];
                            var max = $scope.coverageDescription.boundedBy.envelope.upperCorner.values[i];

                            $scope.core.slices.push(new wcs.DimensionSlice(dimension, min + ""));
                            $scope.core.trims.push(new wcs.DimensionTrim(dimension, min + "", max + ""));
                            $scope.core.isTrimSelected.push(true);
                        }

                        if ($scope.getCoverageTabStates.isRangeSubsettingSupported) {
                            $scope.rangeSubsettingExtension = new RangeSubsettingModel($scope.coverageDescription);
                        }

                        if ($scope.getCoverageTabStates.isScalingSupported) {
                            $scope.scalingExtension = new WCSScalingExtensionModel($scope.coverageDescription);
                        }

                        if ($scope.getCoverageTabStates.isInterpolationSupported) {
                            $scope.interpolationExtension = new WCSInterpolationExtensionModel($scope.wcsStateInformation.serverCapabilities);
                        }

                        if ($scope.getCoverageTabStates.isCRSSupported) {
                            $scope.crsExtension = new WCSCRSExtensionModel($scope.wcsStateInformation.serverCapabilities);
                        }

                        if ($scope.getCoverageTabStates.isClippingSupported) {
                            $scope.clippingExtension = new WCSClippingExtensionModel($scope.wcsStateInformation.serverCapabilities);
                        }

                        $scope.getCoverage = function ():void {
                            var dimensionSubset:wcs.DimensionSubset[] = [];
                            for (var i = 0; i < numberOfAxis; ++i) {
                                var min = $scope.coverageDescription.boundedBy.envelope.lowerCorner.values[i];
                                var max = $scope.coverageDescription.boundedBy.envelope.upperCorner.values[i];

                                if ($scope.core.isTrimSelected[i]) {
                                    if ($scope.core.trims[i].trimLow != min.toString()
                                        || $scope.core.trims[i].trimHigh != max.toString()) {
                                        dimensionSubset.push($scope.core.trims[i]);
                                    }
                                } else {
                                        dimensionSubset.push($scope.core.slices[i]);
                                }
                            }

                            var getCoverageRequest = new wcs.GetCoverage($scope.coverageDescription.coverageId, dimensionSubset, $scope.core.selectedCoverageFormat, $scope.core.isMultiPartFormat);
                            getCoverageRequest.rangeSubset = $scope.rangeSubsettingExtension.rangeSubset;
                            getCoverageRequest.scaling = $scope.scalingExtension.getScaling();
                            getCoverageRequest.interpolation = $scope.interpolationExtension.getInterpolation();
                            getCoverageRequest.crs = $scope.crsExtension.getCRS();
                            getCoverageRequest.clipping = $scope.clippingExtension.getClipping();

                            wcsService.getCoverage(getCoverageRequest)
                                .then(
                                    (requestUrl:string)=> {
                                        $scope.core.requestUrl = requestUrl;                                        
                                    },
                                    (...args:any[])=> {
                                        $scope.core.requestUrl = null;

                                        alertService.error("Failed to execute GetCoverage operation.");
                                        $log.error(args);
                                    });                            
                        };

                        // Load the coverage extent on globe
                        $scope.loadCoverageExtentOnGlobe();
                    }
                });
        }

        private static isRangeSubsettingSupported(serverCapabilities:wcs.Capabilities):boolean {
            return serverCapabilities.serviceIdentification.profile.indexOf(Constants.RANGE_SUBSETTING_EXT_URI) != -1;
        }

        private static isScalingSupported(serverCapabilities:wcs.Capabilities):boolean {
            return serverCapabilities.serviceIdentification.profile.indexOf(Constants.SCALING_EXT_URI) != -1;
        }

        private static isInterpolationSupported(serverCapabilities:wcs.Capabilities):boolean {
            return serverCapabilities.serviceIdentification.profile.indexOf(Constants.INTERPOLATION_EXT_URI) != -1;
        }

        private static isCRSSupported(serverCapabilities:wcs.Capabilities):boolean {
            return serverCapabilities.serviceIdentification.profile.indexOf(Constants.CRS_EXT_URI) != -1;
        }
    }


    interface WCSGetCoverageControllerScope extends WCSMainControllerScope {
        // Is the dropdown for Globe open
        isGlobeOpen:boolean;
        // Is the div containing Globe show/hide
        isGetCoverageHideGlobe:boolean;

        availableCoverageIds:string[];
        selectedCoverageId:string;
        isCoverageIdValid():void;

        coverageDescription:wcs.CoverageDescription;

        core:GetCoverageCoreModel;
        rangeSubsettingExtension:RangeSubsettingModel;
        scalingExtension:WCSScalingExtensionModel;
        crsExtension:WCSCRSExtensionModel;
        clippingExtension:WCSClippingExtensionModel;
        interpolationExtension:WCSInterpolationExtensionModel;

        getCoverageTabStates:GetCoverageTabStates;

        getCoverageClickEvent():void;

        getCoverage():void;
    }

    interface GetCoverageCoreModel {

        slices:wcs.DimensionSlice[];
        trims:wcs.DimensionTrim[];
        isTrimSelected:boolean[];
        isMultiPartFormat:boolean;
        selectedCoverageFormat:string;
        requestUrl:string;
    }

    interface GetCoverageTabStates {        
        //Is the core tab open
        isCoreOpen:boolean;

        //Is the range subsetting tab open
        isRangeSubsettingOpen:boolean;
        isRangeSubsettingSupported:boolean;

        //Is the scaling extenstion tab open
        isScalingOpen:boolean;
        isScalingSupported:boolean;

        //Is the interpolation tab open
        isInterpolationOpen:boolean;
        isInterpolationSupported:boolean;

        //Is the CRS tab open
        isCRSOpen:boolean;
        isCRSSupported:boolean;

        //Is the Clipping tab open
        isClippingOpen:boolean;
        isClippingSupported:boolean;
    }
}
