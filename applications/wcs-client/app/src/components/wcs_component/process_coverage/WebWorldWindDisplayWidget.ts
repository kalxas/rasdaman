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

module rasdaman {
    //Declare the WorldWind object so that typescript does not complain.
    declare var WorldWind:any;
    export function WebWorldWindDisplayWidget():angular.IDirective {        
        return {            
            // Link passed arguments from directive to template URL (HTML)
            // NOTE: attributes are converted to lower case internally (e.g: minLat -> minlat)
            link: function (scope:angular.IScope, elem:any, attributes:any) {
                console.log('attributes: ', attributes);
                var index = attributes.index;
                var minLat = attributes.minlat;                                
                var minLong = attributes.minlong;
                var maxLat = attributes.maxlat;
                var maxLong = attributes.maxlong;

                 // // NOTE: all these angular scope values are passed from directive <wwwd-display> via WebWorldWindDisplayWidget
                // Each canvas to draw WebWorldWind globe needs to have different id or only the first one is drawn.
                var canvas = document.createElement("canvas");
                canvas.id = "canvas" + Math.random().toString();
                canvas.width = 500;
                canvas.height = 500;

                // Each row contains different WCPS result
                var divContainerId = document.getElementById("resultRow_" + attributes.index);
                divContainerId.appendChild(canvas);

                WorldWind.Logger.setLoggingLevel(WorldWind.Logger.LEVEL_WARNING);

                var wwd = new WorldWind.WorldWindow(canvas.id);

                var layers = [
                    {layer: new WorldWind.BMNGOneImageLayer(), enabled: true},
                    {layer: new WorldWind.BingAerialWithLabelsLayer(null), enabled: true},
                    {layer: new WorldWind.CompassLayer(), enabled: true},
                    {layer: new WorldWind.CoordinatesDisplayLayer(wwd), enabled: true},
                    {layer: new WorldWind.ViewControlsLayer(wwd), enabled: true}
                ];

                for (var l = 0; l < layers.length; l++) {
                    layers[l].layer.enabled = layers[l].enabled;
                    wwd.addLayer(layers[l].layer);
                }

                var image = new Image();
                image.src = "data:image/png;base64," + attributes.data;
                
                var surfaceImage = new WorldWind.SurfaceImage(new WorldWind.Sector(minLat, maxLat, minLong, maxLong), new WorldWind.ImageSource(image));
                var surfaceImageLayer = new WorldWind.RenderableLayer();
                surfaceImageLayer.displayName = "Surface Images";
                surfaceImageLayer.addRenderable(surfaceImage);
                wwd.addLayer(surfaceImageLayer);

                // Move to the center of the image (Lat, Long order)
                var xcenter = (parseFloat(minLong) + parseFloat(maxLong)) / 2;
                var ycenter = (parseFloat(minLat) + parseFloat(maxLat)) / 2;
                wwd.navigator.lookAtLocation = new WorldWind.Location(ycenter, xcenter);
                wwd.redraw();   
            }            
        };
    }
}