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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU  General Public License for more details.
 *
 * You should have received a copy of the GNU  General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003 - 2017 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
module rasdaman {
    // NOTE: remember to register Service, Controller, Directive classes to app/src/app.ts
    // or it will have this error: $injector:unpr
    // https://docs.angularjs.org/error/$injector/unpr?p0=rasdaman.WebWorldWindServiceProvider%20%3C-%20rasdaman.WebWorldWindService
    export class WebWorldWindService {                    
        // Array of object for each WebWorldWind in each canvas (GetCapabilities, DescribeCoverage, GetCoverage)        
        private webWorldWindModels: WebWorldWindModel[] = [];         
        private coveragesExtents: any = null;

        public static $inject = [];

        public constructor() {            
        }

        public setCoveragesExtents(coveragesExtents: any) {
            this.coveragesExtents = coveragesExtents;
        }

        // Return an array of all CoveragesExtents
        public getCoveragesExtents() {
            return this.coveragesExtents;
        }

        // Return an array containing only one CoverageExtent if coverageId exists
        public getCoveragesExtentsByCoverageId(coverageId: string) {
            var result = [];
            for (var i = 0; i < this.coveragesExtents.length; i++) {
                if (this.coveragesExtents[i].coverageId === coverageId) {
                    result.push(this.coveragesExtents[i]);
                    return result;
                }
            }

            // CoverageExtent does not exist which means coverage cannot reproject to EPSG:4326
            return null;
        }
        
        // Init the WebWorldWind on the canvasId HTML element
        private initWebWorldWind(canvasId: string) {
            // Create a WorldWindow for the canvas.                
            var wwd = new WorldWind.WorldWindow(canvasId);    
            // Create a layer to hold the polygons.
            var polygonLayer = new WorldWind.RenderableLayer();         
            
            var layers = [
                {layer: new WorldWind.BMNGLayer(), enabled: true},
                {layer: new WorldWind.BMNGLandsatLayer(), enabled: false},
                {layer: new WorldWind.BingAerialLayer(null), enabled: false},
                {layer: new WorldWind.BingAerialWithLabelsLayer(null), enabled: true},
                {layer: new WorldWind.BingRoadsLayer(null), enabled: false},
                {layer: new WorldWind.CompassLayer(), enabled: true},
                {layer: new WorldWind.CoordinatesDisplayLayer(wwd), enabled: true},
                {layer: new WorldWind.ViewControlsLayer(wwd), enabled: true}
            ];

            // Bing layers
            for (var i = 0; i < layers.length; i++) {
                layers[i].layer.enabled = layers[i].enabled;
                wwd.addLayer(layers[i].layer);
            }       

            // Coverage's extent as a text when hovering mouse over
            var textLayer = new WorldWind.RenderableLayer("Screen Text");
            wwd.addLayer(textLayer);
            
            // Callback function on mouse hover event
            var handlePick = function (o) {
                // Clear the displayed screen text
                textLayer.removeAllRenderables();
                var pickPoint = wwd.canvasCoordinates(o.clientX, o.clientY);                    
                var pickList = wwd.pick(pickPoint);
                if (pickList.objects.length > 0) {
                    for (var p = 0; p < pickList.objects.length; p++) {
                        var pickedObject = pickList.objects[p];
                        if (!pickedObject.isTerrain) {
                            if (pickedObject.userObject instanceof WorldWind.SurfacePolygon) {
                                var screenText = new WorldWind.ScreenText(
        new WorldWind.Offset(WorldWind.OFFSET_FRACTION, 0.5, WorldWind.OFFSET_FRACTION, 0.5), pickedObject.userObject.userProperties);
                                var textAttributes = new WorldWind.TextAttributes(null);
                                textAttributes.color = WorldWind.Color.YELLOW;
                                screenText.attributes = textAttributes;
                                
                                textLayer.addRenderable(screenText);
                                break;
                            }
                        }
                    }
                }
            }
            
            // Listen for mouse moves and highlight the placemarks that the cursor rolls over.
            wwd.addEventListener("mousemove", handlePick);

            // Now set up to handle highlighting.
            var highlightController = new WorldWind.HighlightController(wwd);  

            // Create a new WebWorldWindModel and add to the array
            var webWorldWindModel: WebWorldWindModel = {
                canvasId: canvasId,
                wwd: wwd,
                polygonLayer: polygonLayer
            }

            this.webWorldWindModels.push(webWorldWindModel);

            // Then return the WebWorldWindModel object to be used later
            return webWorldWindModel;
        }

        // To get the coverageIds of other coverages in the current page which have same extents.
        // As in the Globe, only the upper coverage's polygon can be hovered, so need to add these coverageIds to the text layer
        // to let user know how many coverages in this polygon.
        // return: array[string] coverageIds
        private getCoverageIdsSameExtent(coverageExtent: any, coveragesExtentsArray: any) {
            var coveragedIds = [];            
            var xmin = coverageExtent.bbox.xmin;
            var ymin = coverageExtent.bbox.ymin;
            var xmax = coverageExtent.bbox.xmax;
            var ymax = coverageExtent.bbox.ymax;

            for (var i = 0; i < coveragesExtentsArray.length; i++) {
                var coverageIdTmp = coveragesExtentsArray[i].coverageId;
                var bboxTmp = coveragesExtentsArray[i].bbox;
                var xminTmp = bboxTmp.xmin;
                var yminTmp = bboxTmp.ymin;
                var xmaxTmp = bboxTmp.xmax;
                var ymaxTmp = bboxTmp.ymax;

                if (xmin == xminTmp && ymin == yminTmp && xmax == xmaxTmp && ymax == ymaxTmp) {                    
                    // add the coverages with same extent with input coverage (incldue itself)
                    coveragedIds.push("Coverage Id: " + coverageIdTmp + "\n");
                }
            }

            return coveragedIds;
        }

        // coveragesExtentsArray is an array of CoveragesExtents
        // Then load this array on the Globe on a HTML element canvas
        public loadCoveragesExtentsOnGlobe(canvasId: string, coveragesExtentsArray: any) {    
            var exist = false;
            var webWorldWindModel = null;            
            for (var i = 0; i < this.webWorldWindModels.length; i++) {
                if (this.webWorldWindModels[i].canvasId === canvasId) {
                    exist = true;
                    webWorldWindModel = this.webWorldWindModels[i];
                    break;
                }
            }

            // Init the WebWorldWindModel for the canvasId if it does not exist
            if (!exist) {
                webWorldWindModel = this.initWebWorldWind(canvasId);
            }                        

            var wwd = webWorldWindModel.wwd;
            var polygonLayer = webWorldWindModel.polygonLayer;

            // Remove the rendered polygon layer and replace it with new layer
            wwd.removeLayer(polygonLayer);
            polygonLayer = new WorldWind.RenderableLayer();
            webWorldWindModel.polygonLayer = polygonLayer;     
            wwd.addLayer(polygonLayer);                            
                    
            var polygonAttributes = new WorldWind.ShapeAttributes(null);
            polygonAttributes.drawInterior = true;
            polygonAttributes.drawOutline = true;
            polygonAttributes.outlineColor = WorldWind.Color.BLUE;
            polygonAttributes.interiorColor = new WorldWind.Color(0, 1, 1, 0.2);
            polygonAttributes.applyLighting = true;

            // Create and assign the polygon's highlight attributes.
            var highlightAttributes = new WorldWind.ShapeAttributes(polygonAttributes);
            highlightAttributes.outlineColor = WorldWind.Color.RED;
            highlightAttributes.interiorColor = new WorldWind.Color(1, 1, 1, 0.2);        
                      
            for (var i = 0; i < coveragesExtentsArray.length; i++) {
                var coverageExtent = coveragesExtentsArray[i];
                var coverageId = coverageExtent.coverageId;
                var bbox = coverageExtent.bbox;

                var boundaries = [];
                boundaries[0] = []; // outer boundary
                boundaries[0].push(new WorldWind.Location(bbox.ymin, bbox.xmin));
                boundaries[0].push(new WorldWind.Location(bbox.ymin, bbox.xmax));
                boundaries[0].push(new WorldWind.Location(bbox.ymax, bbox.xmax));
                boundaries[0].push(new WorldWind.Location(bbox.ymax, bbox.xmin));                                       

                var polygon = new WorldWind.SurfacePolygon(boundaries, polygonAttributes);                                                            
                polygon.highlightAttributes = highlightAttributes;

                // as it can have multiple coverageIds share same extent
                var coverageIds = this.getCoverageIdsSameExtent(coverageExtent, coveragesExtentsArray);
                var coverageIdsStr = "";

                for (var j = 0; j < coverageIds.length; j++) {
                    coverageIdsStr += coverageIds[j];
                }

                var userProperties = coverageIdsStr + "Coverage Extent: lat_min=" + bbox.ymin + ", lon_min=" + bbox.xmin + ", lat_max=" + bbox.ymax + ", lon_max=" + bbox.xmax;
                polygon.userProperties = userProperties;

                // Add the polygon to the layer and the layer to the World Window's layer list.
                polygonLayer.addRenderable(polygon);                
            }                                                                                       
        }

        // Go to the center of the first coverage extent of the input array on Globe
        public gotoCoverageExtentCenter(canvasId: string, coverageExtents: any) {
            var webWorldWindModel = null;            
            for (var i = 0; i < this.webWorldWindModels.length; i++) {
                if (this.webWorldWindModels[i].canvasId === canvasId) {                    
                    webWorldWindModel = this.webWorldWindModels[i];
                    break;
                }
            }
            var coverageExtent = coverageExtents[0];
            var xcenter = (coverageExtent.bbox.xmin + coverageExtent.bbox.xmax) / 2;
            var ycenter = (coverageExtent.bbox.ymin + coverageExtent.bbox.ymax) / 2;
            var wwd = webWorldWindModel.wwd;            

            // NOTE: using wwd.goTo() will make the Globe hang up
            wwd.navigator.lookAtLocation = new WorldWind.Location(ycenter, xcenter);
            wwd.redraw();                                                                   
        }
    }

    interface WebWorldWindModel {
        canvasId: string,
        wwd: any,
        polygonLayer: any
    }
}