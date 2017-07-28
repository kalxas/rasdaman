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
///<reference path="../shared/WCSService.ts"/>
///<reference path="../main/MainController.ts"/>
///<reference path="../../models/wcs/_wcs.ts"/>
///<reference path="../web_world_wind/WebWorldWindService.ts"/>

module rasdaman {
    export class GetCoverageController {
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

        public constructor($scope:GetCoverageControllerScope,
                           $rootScope:angular.IRootScopeService,
                           $log:angular.ILogService,
                           wcsService:rasdaman.WCSService,
                           alertService:any,
                           webWorldWindService:rasdaman.WebWorldWindService) {
            $scope.SelectedCoverageId = null;

            $scope.IsGlobeOpen = false;
            $scope.IsGetCoverageHideGlobe = true;

            $scope.isCoverageIdValid = ()=> {
                if ($scope.StateInformation.ServerCapabilities) {
                    var coverageSummaries = $scope.StateInformation.ServerCapabilities.Contents.CoverageSummary;
                    for (var i = 0; i < coverageSummaries.length; ++i) {
                        if (coverageSummaries[i].CoverageId == $scope.SelectedCoverageId) {
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

            $scope.$watch("StateInformation.ServerCapabilities", (capabilities:wcs.Capabilities)=> {
                if (capabilities) {
                    $scope.AvailableCoverageIds = [];
                    capabilities.Contents.CoverageSummary.forEach((coverageSummary:wcs.CoverageSummary)=> {
                        $scope.AvailableCoverageIds.push(coverageSummary.CoverageId);
                    });
                }
            });

            $scope.loadCoverageExtentOnGlobe = function() {
                // Fetch the coverageExtent by coverageId to display on globe if possible
                var coveragesExtents = webWorldWindService.getCoveragesExtentsByCoverageId($scope.SelectedCoverageId);                            
                if (coveragesExtents == null) {
                    $scope.IsGetCoverageHideGlobe = true;
                } else {
                    // Show covearge's extent on the globe
                    var canvasId = "canvasGetCoverage";
                    $scope.IsGetCoverageHideGlobe = false;
                    webWorldWindService.loadCoveragesExtentsOnGlobe(canvasId, coveragesExtents);
                    // NOTE: Without the time interval, Globe in DescribeCoverage/GetCoverage will hang up in some cases when it goes to the center of current coverage's extent
                    // If the globe hangs up, click on the button GetCoverage one more time.
                    webWorldWindService.gotoCoverageExtentCenter(canvasId, coveragesExtents);
                }                                
            }

            $scope.getCoverageClickEvent = function () {
                if (!$scope.isCoverageIdValid()) {
                    alertService.error("The entered coverage ID is invalid.");
                    return;
                }
                // trigger the DescribeCoverage in DescribeCoverageController to fill the data to both DescribeCoverage and GetCoverage tabs
                $scope.StateInformation.SelectedGetCoverageId = $scope.SelectedCoverageId;                
                // $scope.$digest();

                // load the coverage extent on the globe
                $scope.loadCoverageExtentOnGlobe();
            }
           

            $scope.$watch("StateInformation.SelectedCoverageDescriptions",
                (coverageDescriptions:wcs.CoverageDescriptions)=> {
                    if (coverageDescriptions && coverageDescriptions.CoverageDescription) {
                        $scope.CoverageDescription = $scope.StateInformation.SelectedCoverageDescriptions.CoverageDescription[0];
                        $scope.SelectedCoverageId = $scope.CoverageDescription.CoverageId;

                        $scope.GetCoverageTabStates = {                            
                            IsCoreOpen: true,
                            IsRangeSubsettingOpen: false,
                            IsRangeSubsettingSupported: GetCoverageController.isRangeSubsettingSupported($scope.StateInformation.ServerCapabilities),
                            IsScalingOpen: false,
                            IsScalingSupported: GetCoverageController.isScalingSupported($scope.StateInformation.ServerCapabilities),
                            IsInterpolationOpen: false,
                            IsInterpolationSupported: GetCoverageController.isInterpolationSupported($scope.StateInformation.ServerCapabilities)
                        };

                        $scope.Core = {
                            Slices: [],
                            Trims: [],
                            IsTrimSelected: [],
                            IsMultiPartFormat: false,
                            SelectedCoverageFormat: $scope.StateInformation.ServerCapabilities.ServiceMetadata.FormatSupported[0],
                            RequestUrl: null
                        };

                        var numberOfAxis = $scope.CoverageDescription.BoundedBy.Envelope.LowerCorner.Values.length;
                        for (var i = 0; i < numberOfAxis; ++i) {
                            var dimension = $scope.CoverageDescription.BoundedBy.Envelope.AxisLabels[i];
                            var min = $scope.CoverageDescription.BoundedBy.Envelope.LowerCorner.Values[i];
                            var max = $scope.CoverageDescription.BoundedBy.Envelope.UpperCorner.Values[i];

                            $scope.Core.Slices.push(new wcs.DimensionSlice(dimension, min + ""));
                            $scope.Core.Trims.push(new wcs.DimensionTrim(dimension, min + "", max + ""));
                            $scope.Core.IsTrimSelected.push(true);
                        }

                        if ($scope.GetCoverageTabStates.IsRangeSubsettingSupported) {
                            $scope.RangeSubsettingExtension = new RangeSubsettingModel($scope.CoverageDescription);
                        }

                        if ($scope.GetCoverageTabStates.IsScalingSupported) {
                            $scope.ScalingExtension = new ScalingExtensionModel($scope.CoverageDescription);
                        }

                        if ($scope.GetCoverageTabStates.IsInterpolationSupported) {
                            $scope.InterpolationExtension = new InterpolationExtensionModel($scope.StateInformation.ServerCapabilities);
                        }

                        $scope.getCoverage = function ():void {
                            var dimensionSubset:wcs.DimensionSubset[] = [];
                            for (var i = 0; i < numberOfAxis; ++i) {
                                var min = $scope.CoverageDescription.BoundedBy.Envelope.LowerCorner.Values[i];
                                var max = $scope.CoverageDescription.BoundedBy.Envelope.UpperCorner.Values[i];

                                if ($scope.Core.IsTrimSelected[i]) {
                                    if ($scope.Core.Trims[i].TrimLow != min.toString()
                                        || $scope.Core.Trims[i].TrimHigh != max.toString()) {
                                        dimensionSubset.push($scope.Core.Trims[i]);
                                    }
                                } else {
                                        dimensionSubset.push($scope.Core.Slices[i]);
                                }
                            }

                            var getCoverageRequest = new wcs.GetCoverage($scope.CoverageDescription.CoverageId, dimensionSubset, $scope.Core.SelectedCoverageFormat, $scope.Core.IsMultiPartFormat);
                            getCoverageRequest.RangeSubset = $scope.RangeSubsettingExtension.RangeSubset;
                            getCoverageRequest.Scaling = $scope.ScalingExtension.getScaling();
                            getCoverageRequest.Interpolation = $scope.InterpolationExtension.getInterpolation();

                            wcsService.getCoverage(getCoverageRequest)
                                .then(
                                    (requestUrl:string)=> {
                                        $scope.Core.RequestUrl = requestUrl;                                        
                                    },
                                    (...args:any[])=> {
                                        $scope.Core.RequestUrl = null;

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
            return serverCapabilities.ServiceIdentification.Profile.indexOf(Constants.RANGE_SUBSETTING_EXT_URI) != -1;
        }

        private static isScalingSupported(serverCapabilities:wcs.Capabilities):boolean {
            return serverCapabilities.ServiceIdentification.Profile.indexOf(Constants.SCALING_EXT_URI) != -1;
        }

        private static isInterpolationSupported(serverCapabilities:wcs.Capabilities):boolean {
            return serverCapabilities.ServiceIdentification.Profile.indexOf(Constants.INTERPOLATION_EXT_URI) != -1;
        }
    }


    interface GetCoverageControllerScope extends MainControllerScope {
        // Is the dropdown for Globe open
        IsGlobeOpen:boolean;
        // Is the div containing Globe show/hide
        IsGetCoverageHideGlobe:boolean;

        AvailableCoverageIds:string[];
        SelectedCoverageId:string;
        isCoverageIdValid():void;

        CoverageDescription:wcs.CoverageDescription;

        Core:GetCoverageCoreModel;
        RangeSubsettingExtension:RangeSubsettingModel;
        ScalingExtension:ScalingExtensionModel;
        InterpolationExtension:InterpolationExtensionModel;

        GetCoverageTabStates:GetCoverageTabStates;

        getCoverageClickEvent():void;

        getCoverage():void;
    }

    interface GetCoverageCoreModel {

        Slices:wcs.DimensionSlice[];
        Trims:wcs.DimensionTrim[];
        IsTrimSelected:boolean[];
        IsMultiPartFormat:boolean;
        SelectedCoverageFormat:string;
        RequestUrl:string;
    }

    interface GetCoverageTabStates {        
        //Is the core tab open
        IsCoreOpen:boolean;

        //Is the range subsetting tab open
        IsRangeSubsettingOpen:boolean;
        IsRangeSubsettingSupported:boolean;

        //Is the scaling extenstion tab open
        IsScalingOpen:boolean;
        IsScalingSupported:boolean;

        //Is the interpolation tab open
        IsInterpolationOpen:boolean;
        IsInterpolationSupported:boolean;
    }
}
