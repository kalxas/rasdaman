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

/// <reference path="../../../common/_common.ts"/>
/// <reference path="../../../models/wms_model/wms/_wms.ts"/>
///<reference path="../../../../assets/typings/tsd.d.ts"/>
/// <reference path="../../wms_component/settings/SettingsService.ts"/>
///<reference path="../../wms_component/WMSService.ts"/>
///<reference path="../../wcs_component/WCSService.ts"/>
///<reference path="../../../models/wms_model/wms/Capabilities.ts"/>
///<reference path="../../main/WMSMainController.ts"/>
///<reference path="../../web_world_wind/WebWorldWindService.ts"/>

module rasdaman {
    export class WMSDescribeLayerController {
        //Makes the controller work as a tab.
        private static selectedCoverageId:string;

        public static $inject = [
            "$scope",
            "$rootScope",
            "$log",            
            "rasdaman.WMSSettingsService",
            "rasdaman.WMSService",
            "rasdaman.WCSService",
            "Notification",
            "rasdaman.ErrorHandlingService",
            "rasdaman.WebWorldWindService"
        ];

        public constructor($scope:WMSDescribeLayerControllerScope,
                           $rootScope:angular.IRootScopeService,
                           $log:angular.ILogService,                           
                           settings:rasdaman.WMSSettingsService,
                           wmsService:rasdaman.WMSService,
                           wcsService:rasdaman.WCSService,
                           alertService:any,
                           errorHandlingService:rasdaman.ErrorHandlingService,
                           webWorldWindService:rasdaman.WebWorldWindService) {    
                               
            $scope.getMapRequestURL = null;                            
                               
            $scope.layerNames = [];
            $scope.layers = [];   
            
            $scope.displayWMSLayer = false;

            $scope.timeString = null;
            $scope.coverageDescription = null;

            var canvasId = "wmsCanvasDescribeLayer";

            var WCPS_QUERY_FRAGMENT = 0;
            var RASQL_QUERY_FRAGMENT = 1;            
          
            // When clicking on the layername from the table of GetCapabilities tab, it will change to DescribeLayer tab and load metadata for this selected layer.
            $rootScope.$watch("wmsSelectedLayerName", (layerName:string)=> {
                if (layerName != null) {
                    $scope.selectedLayerName = layerName;                
                    $scope.describeLayer();
                }
            });

            // Only allow to click on DescribeCoverage when layer name exists in the available list.
            $scope.isLayerNameValid = ():boolean => {                
                for (var i = 0; i < $scope.layers.length; i++) {
                    if ($scope.layers[i].name == $scope.selectedLayerName) {
                        return true;
                    }
                }                                    

                return false;
            };
            
            // When GetCapabilities is requested, also update the available layers to be used in DescribeLayer controller.
            $scope.$watch("wmsStateInformation.serverCapabilities", (capabilities:wms.Capabilities)=> {                
                if (capabilities) {                                  
                    // NOTE: Clear the layers array first to get new valus from GetCapabilities
                    $scope.layers = [];  
                    $scope.layerNames = [];
                    $scope.display3DLayerNotification = false;
                    $scope.display4BandsExclamationMark = false;

                    capabilities.layers.forEach((layer:wms.Layer)=> {                        
                        $scope.layerNames.push(layer.name);
                        $scope.layers.push(layer);                                                
                    });

                    // Describe the current selected layer
                    $scope.describeLayer();
                }
            });

            // When petascope admin user logged in, show insert/update/delete styles and insert/delete pyramid members features
            $rootScope.$watch("adminStateInformation.loggedIn", (newValue:boolean, oldValue:boolean)=> {
                if (newValue) {
                    // Admin logged in
                    $scope.adminUserLoggedIn = true;

                    $scope.hasInsertStyleRole = AdminService.hasRole($rootScope.adminStateInformation.roles, AdminService.PRIV_OWS_WMS_INSERT_STYLE);
                    $scope.hasUpdateStyleRole = AdminService.hasRole($rootScope.adminStateInformation.roles, AdminService.PRIV_OWS_WMS_UPDATE_STYLE);
                    $scope.hasDeleteStyleRole = AdminService.hasRole($rootScope.adminStateInformation.roles, AdminService.PRIV_OWS_WMS_DELETE_STYLE);

                    $scope.hasInsertCoverageRole = AdminService.hasRole($rootScope.adminStateInformation.roles, AdminService.PRIV_OWS_WCS_INSERT_COV);
                    $scope.hasDeleteCoverageRole = AdminService.hasRole($rootScope.adminStateInformation.roles, AdminService.PRIV_OWS_WCS_DELETE_COV);
                } else {
                    // Admin logged out
                    $scope.adminUserLoggedIn = false;
                }
            });
           
            // Describe the content (children elements of this selected layer) of a selected WMS layer
            $scope.describeLayer = function() {

                $scope.displayWMSLayer = false;
                $scope.selectedStyleName = "";
                $("#styleName").val("");
                $("#styleAbstract").val("");

                for (var i = 0; i < $scope.layers.length; i++) {
                    if ($scope.layers[i].name == $scope.selectedLayerName) {                        

                        // Fetch the layer's metadata from the available layers
                        $scope.layer = $scope.layers[i];
                        $scope.isLayerDocumentOpen = true;

                        $scope.firstChangedSlider = [];
                        
                        // Fetch the coverageExtent by layerName to display on globe if possible
                        // as WMS layer name is as same as WCS coverageId
                        var coveragesExtents = [{"bbox": {"xmin": $scope.layer.coverageExtent.bbox.xmin,
                                                          "ymin": $scope.layer.coverageExtent.bbox.ymin,
                                                          "xmax": $scope.layer.coverageExtent.bbox.xmax,
                                                          "ymax": $scope.layer.coverageExtent.bbox.ymax}
                                               }];

                        // And load the layer as footprint on the globe
                        // Show coverage's extent on the globe                        
                        $scope.isCoverageDescriptionsHideGlobe = false;
                                                
                        // Check if coverage is 2D and has <= 4 bands then send a GetMap request to petascope and display result on globe
                        // Create describe coverage request
                        var coverageIds:string[] = [];
                        coverageIds.push($scope.layer.name);
                        var describeCoverageRequest = new wcs.DescribeCoverage(coverageIds);
                        // Also prepare for DescribeLayer's globe with only 1 coverageExtent
                        var coverageExtentArray = [];

                        coverageExtentArray.push($scope.layer.coverageExtent);
                        
                        wcsService.getCoverageDescription(describeCoverageRequest)
                            .then(
                                (response:rasdaman.common.Response<wcs.CoverageDescription>)=> {
                                    //Success handler                                    
                                    $scope.coverageDescription = response.value;
                                    var dimensions = $scope.coverageDescription.boundedBy.envelope.srsDimension;

                                    addSliders(dimensions, coveragesExtents);                
                                    
                                    selectOptionsChange();
                                   
                                    // Also prepare for GetCoverage's globe with only 1 coverageExtent                    
                                    webWorldWindService.prepareCoveragesExtentsForGlobe(canvasId, coverageExtentArray);
                                    // Then, load the footprint of layer on the globe
                                    webWorldWindService.showCoverageExtentOnGlobe(canvasId, $scope.layer.name);
                                },
                                (...args:any[])=> {                                    
                                    errorHandlingService.handleError(args);
                                    $log.error(args);
                                })

                        var listPyramidMembersRequest = new wms.ListPyramidMembers($scope.selectedLayerName);
                        
                        // get the pyramid members of this selected layer
                        wmsService.listPyramidMembersRequest(listPyramidMembersRequest).then(                            
                            (arrayData:[])=> {
                                var pyramidCoverageMembers = [];
                                arrayData.forEach((element:any) => {
                                    var coverageName = element["coverage"];
                                    var scaleFactors = element["scale"].join(",");
                                    var pyramidCoverageMember = new wms.PyramidCoverageMember(coverageName, scaleFactors);
                                    
                                    pyramidCoverageMembers.push(pyramidCoverageMember);
                                });

                                $scope.layers[i].pyramidCoverageMembers = pyramidCoverageMembers;
                            }, (...args:any[])=> {                                    
                                errorHandlingService.handleError(args);
                                $log.error(args);
                            });
                        

                        return;
                    }
                }                
                
            };

            /**
             * When sliders change, renew values to be displayed for WMS GetMap URL
             */
            function renewDisplayedWMSGetMapURL(url) {                
                var tmpURL = url + $scope.selectedStyleName;
                // Push the url to the view
                $( '#getMapRequestURL' ).text(tmpURL);
                $( '#getMapRequestURL' ).attr('href', tmpURL);
                $( '#secGetMap' ).attr('href', tmpURL);
            }

            /**
             * Add axis sliders for selected WMS layer             
             */
            function addSliders(dimensions, coveragesExtents) {

                for(var j = 0; j <= dimensions; ++j) {
                    $scope.firstChangedSlider.push(false);
                }

                // Clear the content displayed in the info boxes of the sliders
                $("#sliders").empty();

                // Display a message to user about the last slice on non spatial axis is selected if layer is 3D+
                $scope.display3DLayerNotification = dimensions > 2 ? true : false;
                $scope.display4BandsExclamationMark = false;

                var showGetMapURL = false;
                var bands = $scope.coverageDescription.rangeType.dataRecord.fields.length;
                var bbox = coveragesExtents[0].bbox; 
                $scope.bboxLayer = bbox;  
                
                if (bands == 2 || bands > 4) {
                    $scope.display4BandsExclamationMark = true;
                }

                // As PNG can only support maximum 4 bands
                showGetMapURL = true;
                // send a getmap request in EPSG:4326 to server                                         
                var minLat = bbox.ymin;
                var minLong = bbox.xmin;
                var maxLat = bbox.ymax;
                var maxLong = bbox.xmax;
                
                $scope.timeString = null;

                // WMS 1.3 requires axes order by CRS (EPSG:4326 is lat, long order)
                var bboxStr = minLat + "," + minLong + "," + maxLat + "," + maxLong;   
                var urlDimensions = bboxStr;
                
                // Prepare the array to store the information for the 3D+ dimensions
                var dimStr = [];
                for(var j = 0; j < 3; ++j){
                    dimStr.push('');
                }

                // Create the string used for the GetMap request in the 3D+ case
                for(var j = 3; j <= dimensions; j++) {
                    if($scope.layer.layerDimensions[j].isTemporal == true) {
                        dimStr.push('&' + $scope.layer.layerDimensions[j].name + '="' + $scope.layer.layerDimensions[j].array[0] + '"');
                        $scope.timeString = $scope.layer.layerDimensions[j].array[0];
                    }
                    else {
                        dimStr.push('&' + $scope.layer.layerDimensions[j].name + '=' + $scope.layer.layerDimensions[j].array[0]);
                    }
                }
                for(var j = 3; j <= dimensions; j++) {
                    urlDimensions += dimStr[j];
                }

                var getMapRequest = new wms.GetMap($scope.layer.name, urlDimensions, 800, 600, $scope.selectedStyleName);
                var url = settings.wmsFullEndpoint + "&" + getMapRequest.toKVP();
                $scope.getMapRequestURL = url;

                $( '#getMapRequestURL' ).text($scope.getMapRequestURL);
                // Then, let webworldwind shows the result of GetMap on the globe
                // Default layer is not shown
                webWorldWindService.loadGetMapResultOnGlobe(canvasId, $scope.selectedLayerName, null, $scope.bboxLayer, $scope.displayWMSLayer,
                                                                $scope.timeString);
                

                if (!showGetMapURL) {
                    // Coverage cannot show GetMap on globe
                    $scope.getMapRequestURL = null;
                }  


                // Initialise auxbBox that can be modified in WebWorldWindService and dosen't change the initial values of the bbox
                var auxbBox = {
                    xmin:Number,
                    xmax:Number,
                    ymin:Number,
                    ymax:Number
                };
                auxbBox.xmax = $scope.bboxLayer.xmax;
                auxbBox.xmin = $scope.bboxLayer.xmin;
                auxbBox.ymax = $scope.bboxLayer.ymax;
                auxbBox.ymin = $scope.bboxLayer.ymin;

                var stepSize = 0.01;
                var numberStepsLat = ($scope.bboxLayer.ymax - $scope.bboxLayer.ymin) / stepSize;
                var numberStepsLong = ($scope.bboxLayer.xmax - $scope.bboxLayer.xmin) / stepSize;

                var stepLat = ($scope.bboxLayer.ymax - $scope.bboxLayer.ymin) / numberStepsLat;
                var stepLong = ($scope.bboxLayer.xmax - $scope.bboxLayer.xmin) / numberStepsLong;

                // Latitude slider
                $("#latSlider").slider({
                    max: numberStepsLat,
                    range: true,
                    values: [0, numberStepsLat],
                    slide: function(event, slider) {
                        // Get max/min values of the lat bbox
                        var sliderMin = slider.values[0];
                        var sliderMax = slider.values[1];

                        // Set the slider as changed, compute what means one step on the slider
                        $scope.firstChangedSlider[1] = true;
                                         
                        // Compute the new values of the lat bbox, setted using the sliders
                        minLat = bbox.ymin;
                        maxLat = bbox.ymax;
                        minLat += stepLat * sliderMin;
                        maxLat -= stepLat * (numberStepsLat - sliderMax);

                        // Update auxbBox, push the change to the bboxLayer
                        auxbBox.ymin = minLat;
                        auxbBox.ymax = maxLat;
                        $scope.bboxLayer = auxbBox;

                        // Update the lat info tooltip of the sliders
                        var tooltip = minLat + ':' + maxLat;                        
                        $("#latSlider").attr('data-original-title', tooltip);
                        $("#latSlider").tooltip('show');
                    
                        // Update the GetMap url
                        var bboxStr = 'bbox=' + minLat + "," + minLong + "," + maxLat + "," + maxLong;
                        var pos1 = url.indexOf('&bbox=');
                        var pos2 = url.indexOf('&', pos1 + 1);
                        url = url.substr(0, pos1 + 1) + bboxStr + url.substr(pos2, url.length - pos2);
                        $scope.getMapRequestURL = url;
                        
                        renewDisplayedWMSGetMapURL(url);

                        // Load the changed footprint of the layer on the globe
                        webWorldWindService.loadGetMapResultOnGlobe(canvasId, $scope.selectedLayerName, $scope.selectedStyleName, auxbBox, $scope.displayWMSLayer, $scope.timeString);
                    }
                });

                $("#latSlider").tooltip();
                $("#latSlider").attr('data-original-title', $scope.bboxLayer.ymin + ':' + $scope.bboxLayer.ymax);

                // If the lat slider hasn't yet been moved set it to the initial position
                if ($scope.firstChangedSlider[1] == false) {
                    $("#latSlider").slider('values', [0, numberStepsLat]);
                }
                
                $("#longSlider").slider({
                    max: numberStepsLong,
                    range: true,
                    values: [0, numberStepsLong],
                    slide: function(event, slider) {
                        // Get max/min values of the long bbox
                        var sliderMin = slider.values[0];
                        var sliderMax = slider.values[1];

                        // Set the slider as changed, compute what means one step on the slider
                        $scope.firstChangedSlider[2] = true;
                                                
                        // Compute the new values of the long bbox, setted using the sliders
                        minLong = bbox.xmin;
                        maxLong = bbox.xmax;
                        minLong += stepLong * sliderMin;
                        maxLong -= stepLong * (numberStepsLong - sliderMax)

                        // Update auxbBox, push the change to the bboxLayer
                        auxbBox.xmin = minLong;
                        auxbBox.xmax = maxLong;
                        $scope.bboxLayer = auxbBox;

                        // Update the long info tooltip of the sliders
                        var tooltip = minLong + ':' + maxLong;                        
                        $("#longSlider").attr('data-original-title', tooltip);
                        $("#longSlider").tooltip('show');

                        // Update the GetMap url
                        var bboxStr = 'bbox=' + minLat + "," + minLong + "," + maxLat + "," + maxLong;
                        var pos1 = url.indexOf('&bbox=');
                        var pos2 = url.indexOf('&', pos1 + 1);
                        url = url.substr(0, pos1 + 1) + bboxStr + url.substr(pos2, url.length - pos2);
                        $scope.getMapRequestURL = url;

                        renewDisplayedWMSGetMapURL(url);
                        
                        // Load the changed footprint of the layer on the globe
                        webWorldWindService.loadGetMapResultOnGlobe(canvasId, $scope.selectedLayerName, $scope.selectedStyleName, auxbBox, $scope.displayWMSLayer, $scope.timeString);
                    }
                });

                $("#longSlider").tooltip();
                $("#longSlider").attr('data-original-title', $scope.bboxLayer.xmin + ':' + $scope.bboxLayer.xmax);

                // If the long slider hasn't yet been moved set it to the initial position
                if ($scope.firstChangedSlider[2] == false) {
                    $("#longSlider").slider('values', [0, numberStepsLong]);
                }

                var sufixSlider = "d";

                for (var j = 3; j <= dimensions; j++) {
                    // Create for each dimension the view components for its corresponding slider 
                    $("<div />", { class:"containerSliders", id:"containerSlider"+j+sufixSlider})
                        .appendTo( $("#sliders"));

                    $("<label />", { class:"sliderLabel", id:"label"+j+sufixSlider})
                        .appendTo( $("#containerSlider"+j+sufixSlider));
                    $("#label"+j+sufixSlider).text($scope.layer.layerDimensions[j].name + ':');

                    $("<div />", { class:"slider", id:"slider"+j+sufixSlider})
                        .appendTo( $("#containerSlider"+j+sufixSlider));

                    let sliderId = "#slider" + j + sufixSlider;
                  
                    // Controler of the slider
                    $( function() {
                        $(sliderId).slider({
                            // Set for each dimension the number of steps on its corresponding the slider
                            max: $scope.layer.layerDimensions[j].array.length - 1,
                            // Initialisations for each slider
                            create: function(event, slider) {
                                // Define the variables such that they can be seen inside the slider code
                                this.sliderObj = $scope.layer.layerDimensions[j];                                
                                var sizeSlider = $scope.layer.layerDimensions[j].array.length - 1;
                                
                                // Add the index lines below the slider
                                for (var it = 1; it < sizeSlider; ++it) {
                                    $("<label>|</label>").css('left', (it/sizeSlider*100)+'%')
                                        .appendTo($(sliderId));
                                }
                                
                            },

                            slide: function(event, slider) {
                                // Set the slider as changed
                                $scope.firstChangedSlider[this.sliderPos] = true;

                                // Update the GetMap url
                                if (this.sliderObj.isTemporal == true) {
                                    dimStr[j] = this.sliderObj.name + '="' + this.sliderObj.array[slider.value] + '"';
                                    $scope.timeString = this.sliderObj.array[slider.value];
                                } else {
                                    dimStr[j] = this.sliderObj.name + '=' + this.sliderObj.array[slider.value];
                                }

                                var pos1 = url.indexOf('&' + this.sliderObj.name + '=');
                                var pos2 = url.indexOf('&', pos1 + 1);
                                url = url.substr(0, pos1 + 1) + dimStr[j] + url.substr(pos2, url.length - pos2);
                                $scope.getMapRequestURL = url;
                                
                                // Update the dimenitional info tooltip of the slider
                                var tooltip = this.sliderObj.array[slider.value];                                
                                $(sliderId).attr('data-original-title', tooltip);
                                $(sliderId).tooltip('show');

                                renewDisplayedWMSGetMapURL(url);

                                // Load the changed footprint of the layer on the globe
                                webWorldWindService.loadGetMapResultOnGlobe(canvasId, $scope.selectedLayerName, 
                                                                            $scope.selectedStyleName, auxbBox, $scope.displayWMSLayer, $scope.timeString);
                            }
                        });
                    } );

                    $(sliderId).tooltip();
                    $(sliderId).attr('data-original-title', $scope.layer.layerDimensions[j].array[0]);
                    

                    // If the i-th dimentional slider hasn't yet been moved set it to the initial position
                    if ($scope.firstChangedSlider[j] == false) {
                        $(sliderId).slider('value', 0);
                    }
                }
            }

            $scope.isLayerDocumentOpen = false;
            
            // Load/Unload WMSLayer on WebWorldWind globe from the checkbox user selected
            $scope.showWMSLayerOnGlobe = (styleName:string)=> {
                $scope.selectedStyleName = styleName;
                $scope.displayWMSLayer = true;          

                renewDisplayedWMSGetMapURL($scope.getMapRequestURL);
                webWorldWindService.loadGetMapResultOnGlobe(canvasId, $scope.selectedLayerName, styleName, $scope.bboxLayer, true, $scope.timeString);
            }

            $scope.hideWMSLayerOnGlobe = ()=> {                
                $scope.displayWMSLayer = false;          
                webWorldWindService.loadGetMapResultOnGlobe(canvasId, $scope.selectedLayerName, $scope.selectedStyleName, $scope.bboxLayer, false, $scope.timeString);
            }

            // ********** Layer's downscaled coverages management **************

            // Create a pyramid member coverage as downscaled level coverage of this selected layer
            $scope.createPyramidMember = () => {                
                let scaleFactors = $("#scaleFactorsValue").val();
                let pyramidMemberCoverageId = $("#pyramidMemberCoverageIdValue").val();

                var createPyramidMember = new wms.CreatePyramidMember($scope.layer.name, scaleFactors, pyramidMemberCoverageId);
                wmsService.createPyramidMemberRequest(createPyramidMember).then(
                    (...args:any[])=> {
                        alertService.success("Successfully created pyramid member coverage <b>" + pyramidMemberCoverageId 
                                           + "</b> with scalefactors <b>" + scaleFactors + "</b> of layer  <b>" + $scope.layer.name + "</b>.");
                        // reload WMS GetCapabilities 
                        $scope.wmsStateInformation.reloadServerCapabilities = true;

                        $("#scaleFactorsValue").val("");
                        $("#pyramidMemberCoverageIdValue").val("");
                    }, (...args:any[])=> {
                        errorHandlingService.handleError(args);                            
                    }).finally(function () {                        
                });

            }

            // Remove a pyramid member coverage from the base coverage (selected layer)
            $scope.removePyramidMember = (pyramidMemberCoverageId:string) => {
                // Then, send the delete layer's downscaled collection level request to server
                var removePyramidMemberRequest = new wms.RemovePyramidMember($scope.layer.name, pyramidMemberCoverageId);
                wmsService.removePyramidMemberRequest(removePyramidMemberRequest).then(
                    (...args:any[])=> {
                        alertService.success("Successfully remove pyramid member <b>" + pyramidMemberCoverageId + "</b> from layer <b>" + $scope.layer.name + "</b>");
                        // reload WMS GetCapabilities 
                        $scope.wmsStateInformation.reloadServerCapabilities = true;                    
                    }, (...args:any[])=> {
                        errorHandlingService.handleError(args);                            
                    }).finally(function () {                        
                });                                
            }


            // ********** Layer's styles management **************

            // Show/hide query/table color definitions if not needed
            function selectOptionsChange() {
                
                $("#styleQueryType").val("none").change();

                $("#styleQueryType").change(function() {
                    if (this.value !== "none") {
                        $("#divStyleQuery").show();
                    } else {
                        $("#divStyleQuery").hide();
                    }
                });

                $("#styleColorTableType").val("none").change();               

                $("#styleColorTableType").change(function() {                    
                    if (this.value !== "none") {
                        $("#divStyleColorTableDefinition").show();
                    } else {
                        $("#divStyleColorTableDefinition").hide();
                    }
                });

                $("#colorTableDefinitionStyleFileInput").change(function() {                 
                    const reader = new FileReader();
                    reader.onload = function fileReadCompleted() {
                        $("#styleColorTableDefinition").val(reader.result as string);
                    };
                    reader.readAsText(this.files[0]);
                });
            }
            
            $scope.isStyleNameValid = (styleName:string)=> {                
                for (var i = 0; i < $scope.layer.styles.length; ++i) {
                    if ($scope.layer.styles[i].name == styleName) {
                        return true;
                    }
                }                                    

                return false;
            };

            // Display the selected style's metadata to the form for updating
            $scope.describeStyleToUpdate = (styleName:string)=> {
                for (var i = 0; i < $scope.layer.styles.length; i++) {
                    var styleObj = $scope.layer.styles[i];
                    if (styleObj.name == styleName) {
                        $("#styleName").val(styleObj.name);                        
                        $("#styleAbstract").val(styleObj.abstract);

                        var styleQueryType = styleObj.queryType;
                        if (styleQueryType === "") {
                            styleQueryType = "none";
                        }
                        $("#styleQueryType").val(styleQueryType);
                        $("#styleQuery").val(styleObj.query);
                        
                        var colorTableType = styleObj.colorTableType;
                        if (colorTableType === "") {
                            colorTableType = "none";
                        }
                        $("#styleColorTableType").val(colorTableType);
                        $("#styleColorTableDefinition").val(styleObj.colorTableDefinition);

                        // Show/hide query/color table defintiion divs
                        $("#styleQueryType").change();
                        $("#styleColorTableType").change();

                        break;
                    }
                }
            }

            // validate the style's data before insert/update to database
            $scope.validateStyle = ()=> {
                var styleName = $("#styleName").val();
                var styleAbstract = $("#styleAbstract").val();
                var styleQueryType = $("#styleQueryType").val();
                var styleQuery = $("#styleQuery").val();
                var styleColorTableType = $("#styleColorTableType").val();
                var styleColorTableDefintion = $("#styleColorTableDefinition").val();
                

                if (styleName.trim() === "") {
                    alertService.error("Style name cannot be empty.");
                    return;
                } else if (styleAbstract.trim() === "") {
                    alertService.error("Style abstract cannot be empty.");
                    return;
                }

                if (styleQueryType == "none" && styleColorTableType == "none") {
                    alertService.error("A style must contain at least a query fragment or a color table definition.");
                    return;
                }

                if (styleQuery.trim() === "" && styleColorTableDefintion.trim() === "") {
                    alertService.error("Style query or color table definition must have value.");
                    return;
                }                  
                
                return true;
            }

            // update WMS style to database
            $scope.updateStyle = ()=> {
                // first validate the style's data
                if ($scope.validateStyle()) {
                    var styleName = $("#styleName").val();
                    var styleAbstract = $("#styleAbstract").val();
                    var styleQueryType = $("#styleQueryType").val();
                    var styleQuery = $("#styleQuery").val();
                    var styleColorTableType = $("#styleColorTableType").val();
                    var styleColorTableDefintion = $("#styleColorTableDefinition").val();

                    // Check if style of current layer exists
                    if (!$scope.isStyleNameValid(styleName)) {
                        alertService.error("Style name '" + styleName + "' does not exist to update.");
                        return;
                    }

                    // Then, send the update layer's style request to server
                    var updateLayerStyle = new wms.UpdateLayerStyle($scope.layer.name, styleName, styleAbstract, styleQueryType, styleQuery, styleColorTableType, styleColorTableDefintion);
                    wmsService.updateLayerStyleRequest(updateLayerStyle).then(
                        (...args:any[])=> {
                            alertService.success("Successfully update style with name <b>" + styleName + "</b> of layer with name <b>" + $scope.layer.name + "</b>");                            
                            // reload WMS GetCapabilities 
                            $scope.wmsStateInformation.reloadServerCapabilities = true;
                        }, (...args:any[])=> {
                            errorHandlingService.handleError(args);                            
                        }).finally(function () {                        
                    });
                }                
            }

            // insert WMS style to database
            $scope.insertStyle = ()=> {
                // first validate the style's data
                if ($scope.validateStyle()) {
                    var styleName = $("#styleName").val();
                    var styleAbstract = $("#styleAbstract").val();
                    var styleQueryType = $("#styleQueryType").val();
                    var styleQuery = $("#styleQuery").val();
                    var styleColorTableType = $("#styleColorTableType").val();
                    var styleColorTableDefintion = $("#styleColorTableDefinition").val();

                    // Check if style of current layer exists
                    if ($scope.isStyleNameValid(styleName)) {
                        alertService.error("Style name '" + styleName + "' already exists, cannot insert same name.");
                        return;
                    }

                    // Then, send the insert layer's style request to server
                    var insertLayerStyle = new wms.InsertLayerStyle($scope.layer.name, styleName, styleAbstract, styleQueryType, styleQuery, styleColorTableType, styleColorTableDefintion);
                    wmsService.insertLayerStyleRequest(insertLayerStyle).then(
                        (...args:any[])=> {
                            alertService.success("Successfully insert style with name <b>" + styleName + "</b> of layer with name <b>" + $scope.layer.name + "</b>");
                            // reload WMS GetCapabilities 
                            $scope.wmsStateInformation.reloadServerCapabilities = true;
                        }, (...args:any[])=> {
                            errorHandlingService.handleError(args);
                        }).finally(function () {                        
                    });
                }                
            }

            // delete WMS style from database
            $scope.deleteStyle = (styleName:string)=> {                
                // Then, send the delete layer's style request to server
                var deleteLayerStyle = new wms.DeleteLayerStyle($scope.layer.name, styleName);                    
                wmsService.deleteLayerStyleRequest(deleteLayerStyle).then(
                    (...args:any[])=> {
                        alertService.success("Successfully delete style with name <b>" + styleName + "</b> of layer with name <b>" + $scope.layer.name + "</b>");
                        // reload WMS GetCapabilities 
                        $scope.wmsStateInformation.reloadServerCapabilities = true;
                    }, (...args:any[])=> {
                        errorHandlingService.handleError(args);                            
                    }).finally(function () {                        
                });                                
            }
        }
    }

    interface WMSDescribeLayerControllerScope extends WMSMainControllerScope {           
        isLayerDocumentOpen:boolean;
        // Only with 2D coverage (bands <=4) can show GetMap
        getMapRequestURL:string;        
        bboxLayer:any;
        displayWMSLayer:boolean;
        display4BandsNotification:boolean;
        display4BandsExclamationMark:boolean;

        timeString:string;

        firstChangedSlider:boolean[];

        display3DLayerNotification:boolean;

        // Show the WMSLayer on WebWorldWind globe (default doesn't show)
        showWMSLayerOnGlobe(styleName:string):void;
        // Hide the WMSLayer on WebWorldWind globe
        hideWMSLayerOnGlobe():void;

        // Model of text box to search layer by name
        layerNames:string[];
        // Contain all the layers's metadata
        layers:wms.Layer[];        
        // Selected layer to describe
        layer:wms.Layer;
        selectedLayerName:string;       
        selectedStyleName:string; 
        describeLayer():void;
	    deleteStyle(styleName:string):void;
	    isStyleNameValid(styleName:string):boolean;
	    isCoverageDescriptionsHideGlobe:boolean;
	    isLayerNameValid():boolean;
	    validateStyle():boolean;
	    insertStyle():void;
	    updateStyle():void;
        describeStyleToUpdate(styleName:string):void;
        
        coverageDescription:wcs.CoverageDescription;

        hasInsertStyleRole: boolean;
        hasUpdateStyleRole: boolean;
        hasDeleteStyleRole: boolean;

        hasInsertCoverageRole: boolean;
        hasDeleteCoverageRole: boolean;
    }
}
