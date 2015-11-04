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

module rasdaman {
    export class GetCoverageController {
        public static $inject = [
            "$scope",
            "$log",
            "rasdaman.WCSService",
            "Notification"
        ];

        public constructor($scope:GetCoverageControllerScope,
                           $log:angular.ILogService,
                           wcsService:rasdaman.WCSService,
                           alertService:any) {
            $scope.$watch("StateInformation.SelectedCoverageDescriptions",
                (coverageDescriptions:wcs.CoverageDescriptions)=> {
                    if (coverageDescriptions && coverageDescriptions.CoverageDescription) {
                        $scope.CoverageDescription = $scope.StateInformation.SelectedCoverageDescriptions.CoverageDescription[0];

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
                        };

                        var numberOfAxis = $scope.CoverageDescription.BoundedBy.Envelope.LowerCorner.Values.length;
                        for (var i = 0; i < numberOfAxis; ++i) {
                            var dimension = $scope.CoverageDescription.BoundedBy.Envelope.AxisLabels[i];
                            var min = $scope.CoverageDescription.BoundedBy.Envelope.LowerCorner.Values[i];
                            var max = $scope.CoverageDescription.BoundedBy.Envelope.UpperCorner.Values[i];

                            $scope.Core.Slices.push(new wcs.DimensionSlice(dimension, "" + Math.round((min + max) / 2)));
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
                                    if ($scope.Core.Slices[i].SlicePoint != "" + Math.round((min + max) / 2)) {
                                        dimensionSubset.push($scope.Core.Slices[i]);
                                    }
                                } else {
                                    if ($scope.Core.Trims[i].TrimLow != min + ""
                                        && $scope.Core.Trims[i].TrimHigh != max + "") {
                                        dimensionSubset.push($scope.Core.Trims[i]);
                                    }
                                }
                            }

                            var getCoverageRequest = new wcs.GetCoverage($scope.CoverageDescription.CoverageId, dimensionSubset, $scope.Core.SelectedCoverageFormat, $scope.Core.IsMultiPartFormat);
                            getCoverageRequest.RangeSubset = $scope.RangeSubsettingExtension.RangeSubset;
                            getCoverageRequest.Scaling = $scope.ScalingExtension.getScaling();
                            getCoverageRequest.Interpolation = $scope.InterpolationExtension.getInterpolation();

                            wcsService.getCoverage(getCoverageRequest)
                                .then(
                                    (data:any)=> {

                                        $log.log(data);
                                    },
                                    (...args:any[])=> {
                                        alertService.error("Failed to execute GetCoverage operation. Check the log for additional information.");
                                        $log.error(args);
                                    });
                        };
                    }
                });
        }

        private static isRangeSubsettingSupported(serverCapabilities:wcs.Capabilities):boolean {
            var rangeSubsettingUri = "http://www.opengis.net/spec/WCS_service-extension_range-subsetting/1.0/conf/record-subsetting";

            return serverCapabilities.ServiceIdentification.Profile.indexOf(rangeSubsettingUri) != -1;
        }

        private static isScalingSupported(serverCapabilities:wcs.Capabilities):boolean {
            var scalingUri = "http://www.opengis.net/spec/WCS_service-extension_scaling/1.0/conf/scaling";

            return serverCapabilities.ServiceIdentification.Profile.indexOf(scalingUri) != -1;
        }

        private static isInterpolationSupported(serverCapabilities:wcs.Capabilities):boolean {
            var interpolationUri = "http://www.opengis.net/spec/WCS_service-extension_interpolation/1.0/conf/interpolation";

            return serverCapabilities.ServiceIdentification.Profile.indexOf(interpolationUri) != -1;
        }
    }


    interface GetCoverageControllerScope extends MainControllerScope {
        CoverageDescription:wcs.CoverageDescription;

        Core:GetCoverageCoreModel;
        RangeSubsettingExtension:RangeSubsettingModel;
        ScalingExtension:ScalingExtensionModel;
        InterpolationExtension:InterpolationExtensionModel;

        GetCoverageTabStates:GetCoverageTabStates;

        getCoverage():void;
    }

    interface GetCoverageCoreModel {
        Slices:wcs.DimensionSlice[];
        Trims:wcs.DimensionTrim[];
        IsTrimSelected:boolean[];
        IsMultiPartFormat:boolean;
        SelectedCoverageFormat:string;
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
