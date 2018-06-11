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
                               
            $scope.layerNames = [];
            $scope.layers = [];   
            
            $scope.displayWMSLayer = false;

            var canvasId = "wmsCanvasDescribeLayer";

            var WCPS_QUERY_FRAGMENT = 0;
            var RASQL_QUERY_FRAGMENT = 1;            
          
            // When clicking on the layername from the table of GetCapabilities tab, it will change to DescribeLayer tab and load metadata for this selected layer.
            $rootScope.$on("wmsSelectedLayerName", (event:angular.IAngularEvent, layerName:string)=> {                            
                $scope.selectedLayerName = layerName;                
                $scope.describeLayer();
            });

            // Only allow to click on DescribeCoverage when layer name exists in the available list.
            $scope.isLayerNameValid = ()=> {                
                for (var i = 0; i < $scope.layers.length; ++i) {
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

                    capabilities.layers.forEach((layer:wms.Layer)=> {                        
                        $scope.layerNames.push(layer.name);
                        $scope.layers.push(layer);                                                
                    });

                    // Describe the current selected layer
                    $scope.describeLayer();
                }
            });
           
            // Describe the content (children elements of this selected layer) of a selected WMS layer
            $scope.describeLayer = function() {

                $scope.displayWMSLayer = false;
                $scope.selectedStyleName = "";

                for (var i = 0; i < $scope.layers.length; i++) {
                    if ($scope.layers[i].name == $scope.selectedLayerName) {
                        // Fetch the layer's metadata from the available layers
                        $scope.layer = $scope.layers[i];
                        $scope.isLayerDocumentOpen = true;

                        // Fetch the coverageExtent by layerName to display on globe if possible
                        // as WMS layer name is as same as WCS coverageId
                        var coveragesExtents = webWorldWindService.getCoveragesExtentsByCoverageId($scope.selectedLayerName);

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
                        webWorldWindService.prepareCoveragesExtentsForGlobe(canvasId, coverageExtentArray);
                        wcsService.getCoverageDescription(describeCoverageRequest)
                            .then(
                                (response:rasdaman.common.Response<wcs.CoverageDescriptions>)=> {
                                    //Success handler                                    
                                    var coverageDescriptions = response.value;
                                    var dimensions = coverageDescriptions.coverageDescription[0].boundedBy.envelope.srsDimension;

                                    // Display a message to user about the last slice on non spatial axis is selected if layer is 3D+
                                    $scope.display3DLayerNotification = dimensions > 2 ? true : false;

                                    var showGetMapURL = false;
                                    var bands = coverageDescriptions.coverageDescription[0].rangeType.dataRecord.field.length;
                                    // As PNG can only support maximum 4 bands
                                    if (bands <= 4) {
                                        showGetMapURL = true;
                                        // send a getmap request in EPSG:4326 to server
                                        var bbox = coveragesExtents[0].bbox;       
                                        $scope.bboxLayer = bbox;                                     
                                        var minLat = bbox.ymin;
                                        var minLong = bbox.xmin;
                                        var maxLat = bbox.ymax;
                                        var maxLong = bbox.xmax;
                                        // WMS 1.3 requires axes order by CRS (EPSG:4326 is lat, long order)
                                        var bboxStr = minLat + "," + minLong + "," + maxLat + "," + maxLong;                                            
                                        var getMapRequest = new wms.GetMap($scope.layer.name, bboxStr, 800, 600);
                                        var url = settings.wmsFullEndpoint + "&" + getMapRequest.toKVP();
                                        this.getMapRequestURL = url;
                                        // Then, let webworldwind shows the result of GetMap on the globe
                                        // Default layer is not shown
                                        webWorldWindService.loadGetMapResultOnGlobe(canvasId, $scope.selectedLayerName, null, $scope.bboxLayer, false);
                                    }
                                    
                                    if (!showGetMapURL) {
                                        // Coverage cannot show GetMap on globe
                                        this.getMapRequestURL = null;
                                    }                                                                        

                                    // Then, load the footprint of layer on the globe
                                    webWorldWindService.showHideCoverageExtentOnGlobe(canvasId, $scope.layer.name);
                                },
                                (...args:any[])=> {                                    
                                    errorHandlingService.handleError(args);
                                    $log.error(args);
                                })

                        return;
                    }
                }                
                
            };

            $scope.isLayerDocumentOpen = false;
            
            // Load/Unload WMSLayer on WebWorldWind globe from the checkbox user selected
            $scope.showWMSLayerOnGlobe = (styleName:string)=> {
                $scope.selectedStyleName = styleName;
                $scope.displayWMSLayer = true;          
                webWorldWindService.loadGetMapResultOnGlobe(canvasId, $scope.selectedLayerName, styleName, $scope.bboxLayer, true);
            }

            $scope.hideWMSLayerOnGlobe = ()=> {                
                $scope.displayWMSLayer = false;          
                webWorldWindService.loadGetMapResultOnGlobe(canvasId, $scope.selectedLayerName, $scope.selectedStyleName, $scope.bboxLayer, false);
            }


            // ********** Layer's styles management **************
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
                        $("#styleQueryType").val(styleObj.queryType.toString());
                        $("#styleQuery").val(styleObj.query);
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

                if (styleName.trim() === "") {
                    alertService.error("Style name cannot be empty.");
                    return;
                } else if (styleAbstract.trim() === "") {
                    alertService.error("Style abstract cannot be empty.");
                    return;
                } else if (styleQuery.trim() === "") {
                    alertService.error("Style query cannot be empty.");
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

                    // Check if style of current layer exists
                    if (!$scope.isStyleNameValid(styleName)) {
                        alertService.error("Style name '" + styleName + "' does not exist to update.");
                        return;
                    }

                    // Then, send the update layer's style request to server
                    var updateLayerStyle = new wms.UpdateLayerStyle($scope.layer.name, styleName, styleAbstract, styleQueryType, styleQuery);                    
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

                    // Check if style of current layer exists
                    if ($scope.isStyleNameValid(styleName)) {
                        alertService.error("Style name '" + styleName + "' already exists, cannot insert same name.");
                        return;
                    }

                    // Then, send the insert layer's style request to server
                    var insertLayerStyle = new wms.InsertLayerStyle($scope.layer.name, styleName, styleAbstract, styleQueryType, styleQuery);                    
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
	    isLayerNameValid():void;
	    validateStyle():void;
	    insertStyle():void;
	    updateStyle():void;
	    describeStyleToUpdate(styleName:string):void;
    }
}
