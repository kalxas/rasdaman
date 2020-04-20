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
            "$http",
            "$scope",
            "$rootScope",
            "$log",
            "rasdaman.WCSService",
            "Notification",
            "rasdaman.WebWorldWindService"
        ];

        public constructor($http:angular.IHttpService,
                           $scope:WCSGetCoverageControllerScope,
                           $rootScope:angular.IRootScopeService,
                           $log:angular.ILogService,
                           wcsService:rasdaman.WCSService,
                           alertService:any,
                           webWorldWindService:rasdaman.WebWorldWindService) {
            $scope.selectedCoverageId = null;

            $scope.isGlobeOpen = false;
            $scope.isGetCoverageHideGlobe = true;

            $scope.isCoverageIdValid = function():boolean {
                if ($scope.wcsStateInformation.serverCapabilities) {
                    var coverageSummaries = $scope.wcsStateInformation.serverCapabilities.contents.coverageSummaries;
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
                    // Supported HTTP request type for GetCoverage KVP request
                    $scope.avaiableHTTPRequests = ["GET", "POST"];
                    $scope.selectedHTTPRequest = $scope.avaiableHTTPRequests[0];
                    
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

            $scope.loadCoverageExtentOnGlobe = function () {
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
                    webWorldWindService.showCoverageExtentOnGlobe(canvasId, $scope.selectedCoverageId);
                }                                
            }

            // Select a coverage to show WCS core and extensions form
            $scope.selectCoverageClickEvent = function() {
                if (!$scope.isCoverageIdValid()) {
                    alertService.error("The entered coverage ID is invalid.");
                    return;
                } else {
                    // trigger the DescribeCoverage in DescribeCoverageController to fill the data to both DescribeCoverage and GetCoverage tabs
                    $scope.wcsStateInformation.selectedGetCoverageId = $scope.selectedCoverageId;                
                    // $scope.$digest();
                    
                    // load the coverage extent on the globe
                    $scope.loadCoverageExtentOnGlobe();
                }
            }

            // Send a GetCoverage request to get result
            $scope.getCoverageClickEvent = function () {

                
                var numberOfAxis = $scope.coverageDescription.boundedBy.envelope.lowerCorner.values.length;
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

                if ($scope.selectedHTTPRequest == "GET") {
                    // GET KVP request which open a new Window to show the result
                    wcsService.getCoverageHTTPGET(getCoverageRequest)
                    .then(
                        (requestUrl:string)=> {                                        
                            $scope.core.requestUrl = requestUrl;                                        
                        },
                        (...args:any[])=> {
                            $scope.core.requestUrl = null;

                            alertService.error("Failed to execute GetCoverage operation in HTTP GET.");
                            $log.error(args);
                        });
                } else {
                    $scope.core.requestUrl = null;
                    // POST KVP request which open a new Window to show the result
                    wcsService.getCoverageHTTPPOST(getCoverageRequest);
                }
                
            }

            // Set the output format according to number of axes
            $scope.setOutputFormat = function(numberOfDimensions) {
                var result = "application/netcdf";

                if (numberOfDimensions == 2) {
                    result = "image/tiff";
                } else if (numberOfDimensions == 1) {
                    result = "application/json";
                }

                return result;
            }

            $scope.$watch("wcsStateInformation.selectedCoverageDescription",
                (coverageDescription:wcs.CoverageDescription)=> {
                    if (coverageDescription) {
                        $scope.coverageDescription = $scope.wcsStateInformation.selectedCoverageDescription;
                        $scope.selectedCoverageId = $scope.coverageDescription.coverageId;

                        // NOTE: this one is important to make "Select Coverage" on a same coverage can trigger DescribeCoverage
                        // to refresh description for current selected coverage on GetCoverage tab.
                        // without it, in DescribeCoverage controller, the watch event listener for "selectedGetCoverageId"
                        //  from GetCoverage does not work in this case.
                        $scope.wcsStateInformation.selectedGetCoverageId = null;

                        $scope.typeOfAxis = [];
                        $scope.isTemporalAxis = [];

                        var coverageIds:string[] = [];
                        coverageIds.push($scope.selectedCoverageId);
                        var describeCoverageRequest = new wcs.DescribeCoverage(coverageIds);
                        var numberOfAxis = $scope.coverageDescription.boundedBy.envelope.lowerCorner.values.length;

                        var rawCoverageDescription;
                        var regularAxis = 'regular';
                        var irregularAxis = 'irregular';

                        for (var i = 0; i < numberOfAxis; ++i) {
                            var el = +$scope.coverageDescription.boundedBy.envelope.upperCorner.values[i];

                                if (isNaN(el)) {
                                    $scope.isTemporalAxis[i] = true;
                                }
                                else {
                                    $scope.isTemporalAxis[i] = false;
                                }
                        }
                        
                        wcsService.getCoverageDescription(describeCoverageRequest)
                        .then(
                            (response:rasdaman.common.Response<wcs.CoverageDescription>)=> {
                                //Success handler
                                $scope.coverageDescriptionsDocument = response.document;

                                rawCoverageDescription = $scope.coverageDescriptionsDocument.value;

                                // Set each axis if it is regular of irregular

                                var startPos = rawCoverageDescription.indexOf("<gmlrgrid:coefficients>");
                                var endPos;
                                            
                                if (startPos != -1) {
                                    // then the coverage has irregular axis
                                    for (var it1 = 0; it1 < numberOfAxis; ++it1) {
                                        startPos = 0;
                                        $("#sliceIrrValues" + it1).empty();
                                        $("#trimmIrrValuesMin" + it1).empty();
                                        $("#trimmIrrValuesMax" + it1).empty();

                                        for (var it2 = 0; it2 <= it1; ++it2) {
                                            startPos = rawCoverageDescription.indexOf("<gmlrgrid:generalGridAxis>", startPos);
                                            startPos = rawCoverageDescription.indexOf(">", startPos + 1);
                                            endPos = rawCoverageDescription.indexOf("</gmlrgrid:generalGridAxis>", startPos);
                                        }

                                        startPos = rawCoverageDescription.indexOf("<gmlrgrid:coefficients>", startPos);

                                        if (startPos != -1 && startPos < endPos) {
                                            // then this is an irregular axis
                                            $scope.typeOfAxis.push(irregularAxis);
                                            endPos = rawCoverageDescription.indexOf("</gmlrgrid:coefficients>", startPos);
                                            startPos  = rawCoverageDescription.indexOf(">", startPos + 1);
                                            startPos++;
                                            // get elements of irregular axis
                                            var rawIrrElements = rawCoverageDescription.substring(startPos, endPos);

                                            var st = rawIrrElements.indexOf(' ');
                                            var element;
                                            var noEl = 0;

                                            while (st != -1) {
                                                //create the select elements for the view
                                                var element = rawIrrElements.substring(0, st);
                                                $("#sliceIrrValues" + it1).append(
                                                    $('<option id="' + noEl + '"/>').attr("value", element)
                                                );
                                                $("#trimmIrrValuesMin" + it1).append(
                                                    $('<option id="' + noEl + '"/>').attr("value", element)
                                                );
                                                $("#trimmIrrValuesMax" + it1).append(
                                                    $('<option id="' + noEl + '"/>').attr("value", element)
                                                );
                                                rawIrrElements = rawIrrElements.substring(st + 1, rawIrrElements.length);
                                                st = rawIrrElements.indexOf(' ');
                                                noEl++;
                                            }

                                            element = rawIrrElements;
                                            $("#trimmIrrValuesMin" + it1).append(
                                                $('<option id="' + noEl + '"/>').attr("value", element)
                                            );
                                            $("#trimmIrrValuesMax" + it1).append(
                                                $('<option id="' + noEl + '"/>').attr("value", element)
                                            );
                                            $("#sliceIrrValues" + it1).append(
                                                $('<option id="' + noEl + '"/>').attr("value", element)
                                            );
                                        }
                                        else {
                                            // then this axis is regular
                                            $scope.typeOfAxis.push(regularAxis);
                                        }
                                    }
                                }
                                else {
                                    // then there is no irregular axis
                                    for (var it = 0; it < numberOfAxis; ++it) {
                                        $scope.typeOfAxis.push(regularAxis);
                                    }
                                }

                                // Set default values (low, high) in dropdown boxes for irregular axes from collected values
                                for (var i = 0; i < $scope.typeOfAxis.length; i++) {
                                    if ($scope.typeOfAxis[i] == irregularAxis) {
                                        var trimLow = $scope.core.trims[i].trimLow;
                                        var trimHigh = $scope.core.trims[i].trimHigh;
                                        $("#trimmIrrMin" + i).val(trimLow);
                                        $("#trimmIrrMax" + i).val(trimHigh);

                                        var slicePoint = $scope.core.slices[i].slicePoint;
                                        $("#sliceIrr" + i).val(slicePoint);
                                    }
                                }

                        });

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
                            selectedCoverageFormat: $scope.setOutputFormat(numberOfAxis),
                            requestUrl: null
                        };

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

                        $scope.typeOfInputIsNotValid = function(isTemporalAxis:boolean, value:any):boolean {
                            if (isTemporalAxis) {
                                value = value.substr(1, value.length - 2);
                                value = new Date(value);
                                if (isNaN(value.getTime())) {
                                    return true;
                                }
                            }
                            else {
                                if (isNaN(value)) {
                                    return true;
                                }
                            }

                            return false;
                        }

                        $scope.trimValidator = function(i:number, min:any, max:any):void {
                            $scope.core.trims[i].trimLowNotValid = false;
                            $scope.core.trims[i].trimHighNotValid = false;
                            $scope.core.trims[i].trimLowerUpperBoundNotInOrder = false;
                            $scope.core.trims[i].typeOfTrimUpperNotValidDate = false;
                            $scope.core.trims[i].typeOfTrimUpperNotValidNumber = false;
                            $scope.core.trims[i].typeOfTrimLowerNotValidDate = false;
                            $scope.core.trims[i].typeOfTrimLowerNotValidNumber = false;

                            var minTrimSelected:any;
                            var maxTrimSelected:any;

                                if ($scope.typeOfInputIsNotValid($scope.isTemporalAxis[i], $scope.core.trims[i].trimLow)) {
                                    if($scope.isTemporalAxis[i]) {
                                        $scope.core.trims[i].typeOfTrimLowerNotValidDate = true;
                                    }
                                    else {
                                        $scope.core.trims[i].typeOfTrimLowerNotValidNumber = true;
                                    }
                                }
                                if ($scope.typeOfInputIsNotValid($scope.isTemporalAxis[i], $scope.core.trims[i].trimHigh)) {
                                    if($scope.isTemporalAxis[i]) {
                                        $scope.core.trims[i].typeOfTrimUpperNotValidDate = true;
                                    }
                                    else {
                                        $scope.core.trims[i].typeOfTrimUpperNotValidNumber = true;
                                    }
                                }

                                if ($scope.isTemporalAxis[i]) {

                                    minTrimSelected = $scope.core.trims[i].trimLow;
                                    minTrimSelected = minTrimSelected.substr(1, minTrimSelected.length - 2);
                                    minTrimSelected = new Date(minTrimSelected);

                                    maxTrimSelected = $scope.core.trims[i].trimHigh;
                                    maxTrimSelected = maxTrimSelected.substr(1, maxTrimSelected.length - 2);
                                    maxTrimSelected = new Date(maxTrimSelected);
                                }
                                else {
                                    minTrimSelected = +$scope.core.trims[i].trimLow;
                                    maxTrimSelected = +$scope.core.trims[i].trimHigh;
                                }
                                    
                                if (minTrimSelected < min) {
                                    $scope.core.trims[i].trimLowNotValid = true;
                                }

                                if (maxTrimSelected > max) {
                                    $scope.core.trims[i].trimHighNotValid = true;
                                }

                                if (minTrimSelected > maxTrimSelected) {
                                    $scope.core.trims[i].trimLowerUpperBoundNotInOrder = true;
                                }
                        }

                        $scope.sliceValidator = function (i:number, min:any, max:any):void {
                            $scope.core.slices[i].sliceRegularNotValid = false;
                            $scope.core.slices[i].typeOfSliceNotValidDate = false;
                            $scope.core.slices[i].typeOfSliceNotValidNumber = false;

                            var sliceSelected:any; 

                            if ($scope.typeOfInputIsNotValid($scope.isTemporalAxis[i], $scope.core.slices[i].slicePoint)) {
                                if($scope.isTemporalAxis[i]) {
                                    $scope.core.slices[i].typeOfSliceNotValidDate = true;
                                }
                                else {
                                    $scope.core.slices[i].typeOfSliceNotValidNumber = true;
                                }
                            }

                            if ($scope.isTemporalAxis[i]) {
                                sliceSelected = $scope.core.slices[i].slicePoint;
                                sliceSelected = sliceSelected.substr(1, sliceSelected.length - 2);
                                sliceSelected = new Date(sliceSelected);
                            }
                            else {
                                sliceSelected = +$scope.core.slices[i].slicePoint;
                            }

                            if (sliceSelected < min || sliceSelected > max) {
                                $scope.core.slices[i].sliceRegularNotValid = true;
                            }
                        }

                        $scope.inputValidator = function (i:number):void {

                                var min:any;
                                var max:any;

                                min = +$scope.coverageDescription.boundedBy.envelope.lowerCorner.values[i];
                                max = +$scope.coverageDescription.boundedBy.envelope.upperCorner.values[i];

                                if ($scope.isTemporalAxis[i]) {
                                    // The axis is temporal
                                    min = $scope.coverageDescription.boundedBy.envelope.lowerCorner.values[i];
                                    min = min.substr(1, min.length - 2);
                                    min = new Date(min);

                                    max = $scope.coverageDescription.boundedBy.envelope.upperCorner.values[i];
                                    max = max.substr(1, max.length - 2);
                                    max = new Date(max);
                                }

                                if ($scope.core.isTrimSelected[i]) {

                                    $scope.trimValidator(i, min, max);
                                }
                                else {

                                    $scope.sliceValidator(i, min, max);
                                }
                            
                        };

                        $scope.selectSliceIrregular = function (i:number) {
                            $scope.core.slices[i].typeOfSliceNotValidDate = false;
                            $scope.core.slices[i].typeOfSliceNotValidNumber = false;

                            var id = "#sliceIrr" + i;
                            var selectedValue = $(id).val();
                            if ($scope.typeOfInputIsNotValid($scope.isTemporalAxis[i], selectedValue)) {
                                if($scope.isTemporalAxis[i]) {
                                    $scope.core.slices[i].typeOfSliceNotValidDate = true;
                                }
                                else {
                                    $scope.core.slices[i].typeOfSliceNotValidNumber = true;
                                }
                            }
                            $scope.core.slices[i].slicePoint = selectedValue;
                        }

                        var operationLess = function (a:number, b:number):boolean {
                            return a < b;
                        }

                        var operationMore = function (a:number, b:number):boolean {
                            return a > b;
                        }

                        $scope.selectTrimIrregularMin = function (i:number) {
                            $scope.core.trims[i].typeOfTrimLowerNotValidDate = false;
                            $scope.core.trims[i].typeOfTrimLowerNotValidNumber = false;

                            var id = "#trimmIrrMin" + i;
                            var selectedValue = $(id).val();
                            if ($scope.typeOfInputIsNotValid($scope.isTemporalAxis[i], selectedValue)) {
                                if($scope.isTemporalAxis[i]) {
                                    $scope.core.trims[i].typeOfTrimLowerNotValidDate = true;
                                }
                                else {
                                    $scope.core.trims[i].typeOfTrimLowerNotValidNumber = true;
                                }
                            }
                            console.log(selectedValue);

                            $scope.core.trims[i].trimLow = selectedValue;
                            $scope.disableUnwantedValues("#trimmIrrValuesMin" + i, '#trimmIrrValuesMax' + i, selectedValue, operationLess);
                        }

                        $scope.selectTrimIrregularMax = function (i:number) {
                            $scope.core.trims[i].typeOfTrimUpperNotValidDate = false;
                            $scope.core.trims[i].typeOfTrimUpperNotValidNumber = false;

                            var id = "#trimmIrrMax" + i;
                            var selectedValue = $(id).val();
                            if ($scope.typeOfInputIsNotValid($scope.isTemporalAxis[i], selectedValue)) {
                                if($scope.isTemporalAxis[i]) {
                                    $scope.core.trims[i].typeOfTrimUpperNotValidDate = true;
                                }
                                else {
                                    $scope.core.trims[i].typeOfTrimUpperNotValidNumber = true;
                                }
                            }

                            $scope.core.trims[i].trimHigh = selectedValue;

                            $scope.disableUnwantedValues("#trimmIrrValuesMax" + i, '#trimmIrrValuesMin' + i, selectedValue, operationMore);
                        }

                        $scope.disableUnwantedValues = function (firstId:string, secondId:string, selectedValue:string, op:any) {
                            var id = firstId;
                            var idSelectedOption:any;
                            var idOptionSecondSelect:number;
                            var wrongSelection = false;

                            idSelectedOption = $(id).find("option[value='" + selectedValue +"']").attr("id");
                            idSelectedOption = +idSelectedOption;
                            $(secondId).find('option').each(function() {
                                idOptionSecondSelect = +$(this).attr("id");
                                
                                if (op(idOptionSecondSelect, idSelectedOption)) {
                                    if($(this).prop('selected') == true) {
                                        wrongSelection = true;
                                    }
                                    
                                    $(this).prop('disabled', true);
                                }
                                else {
                                    $(this).removeAttr('disabled');

                                    if (wrongSelection == true) {
                                        $(this).prop('selected', true);
                                    }
                                }
                            });
                        }

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

        inputValidator(i:number):void;
        trimValidator(i:number, min:any, max:any):void;
        sliceValidator(i:number, min:any, max:any):void;
        typeOfInputIsNotValid(isTemporalAxis:boolean, value:any):boolean;
        isSliceInIrrData(axis:number, value:any):boolean;
        selectSliceIrregular(i:number):void;
        selectTrimIrregularMin(i:number):void;
        selectTrimIrregularMax(i:number):void;
        disableUnwantedValues(firstId:string, secondId:string, selectedValue:string, op:any);
        showIrrAxisValues:boolean[];
        typeOfAxis:string[];
        isTemporalAxis:boolean[];

        availableCoverageIds:string[];
        coverageCustomizedMetadatasDict:any;
        selectedCoverageId:string;
        isCoverageIdValid():boolean;

        coverageDescription:wcs.CoverageDescription;
        // GET, POST
        avaiableHTTPRequests:string[];
        selectedHTTPRequest:string;

        core:GetCoverageCoreModel;
        rangeSubsettingExtension:RangeSubsettingModel;
        scalingExtension:WCSScalingExtensionModel;
        crsExtension:WCSCRSExtensionModel;
        clippingExtension:WCSClippingExtensionModel;
        interpolationExtension:WCSInterpolationExtensionModel;

        getCoverageTabStates:GetCoverageTabStates;

        // Based on the number of dimensions to set the output format accordingly
        setOutputFormat(numberOfDimensions:number):string;

        // select a coverage to show the form
        selectCoverageClickEvent():void;
        // send a GetCoverage request to get result
        getCoverageClickEvent():void;

        getCoverage():void;

	    loadCoverageExtentOnGlobe():void;
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
