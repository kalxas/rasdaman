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

///<reference path="../../../common/_common.ts"/>
///<reference path="WCPSQueryResult.ts"/>
///<reference path="RawWCPSResult.ts"/>
///<reference path="ImageWCPSResult.ts"/>
///<reference path="DiagramWCPSResult.ts"/>
///<reference path="WebWorldWindWCPSResult.ts"/>
///<reference path="NotificationWCPSResult.ts"/>

module rasdaman {
	//Declare the fileSaver function so that typescript does not complain.
    declare var saveAs:any;
    export class WCPSResultFactory {
        public static getResult(errorHandlingService:any, command:WCPSCommand, data:any, mimeType:string, fileName:string):WCPSQueryResult {

            var validationResult = this.validateResult(errorHandlingService, command, mimeType);
            if (command.widgetConfiguration == null) {
                // if mimeType is text then return raw data
                if (mimeType == "" || mimeType == "application/json" || mimeType == "text/csv" || mimeType == "text/xml" || mimeType == "text/plain" || mimeType == "application/gml+xml") {
                    return new RawWCPSResult(command, data);
                } else {
                    // if mimeType is image/binary file then download file
                    var blob = new Blob([data], {type: "application/octet-stream"});
                    saveAs(blob, fileName);
                    return null;
                }
            } else if (command.widgetConfiguration.type == "diagram") {
                // validate result for diagram widget (only 1D encoding in csv, json is supported)
                if (validationResult == null) {
                    return new DiagramWCPSResult(command, data);
                } else {
                    return new NotificationWCPSResult(command, validationResult);
                }                
            } else if (command.widgetConfiguration.type == "image") {
                // validate result for image widget (only 2D encoding in png, jpeg is supported)
                if (validationResult == null) {
                    return new ImageWCPSResult(command, data);
                } else {
                    return new NotificationWCPSResult(command, validationResult);
                }
            } else if (command.widgetConfiguration.type == "wwd") {
                // valid result to display in WebWorldWind as 2D png, jpeg
                if (validationResult == null) {
                    return new WebWorldWindWCPSResult(command, data);
                } else {
                    return new NotificationWCPSResult(command, validationResult);
                }
            } else {
                errorHandlingService.notificationService.error("The input widget: " + command.widgetConfiguration.type + " does not exist");
            }
        }     

        
        /**
        * @widgetType: the requested widget in WCPS query from WCS client (e.g: image>>, diagram>>)
        * @mimeType: the returned mimeType from Petascope (e.g: image/tiff, image/png,...)
        */
        public static validateResult(errorHandlingService:any, command:WCPSCommand, mimeType:any) {
            var errorMessage = null;            

            // In case WCPS query doesn't have any widget prefix, no point to validate
            if (command.widgetConfiguration == null) {
                return errorMessage;
            }

            var widgetType = command.widgetConfiguration.type;
            // Check if diagram widget is requested with image/binary mime
            if (widgetType == "diagram" && ! (mimeType == "application/json" || mimeType == "text/plain" || mimeType == "text/csv")) {
                errorMessage = "Diagram widget can only be used with encoding 1D result in json or csv."
                errorHandlingService.notificationService.error(errorMessage);
            } else if (widgetType == "image" && ! (mimeType == "image/png" || mimeType == "image/jpeg")) {
                // Check if image widget is requested with text or non-displayable mime
                errorMessage = "Image widget can only be used with encoding 2D result in png or jpeg."
                errorHandlingService.notificationService.error(errorMessage);
            } else if (widgetType == "wwd" && ! (mimeType == "image/png" || mimeType == "image/jpeg")) {
                // Check if wwd widget is requested with text or non-displayable mime
                errorMessage = "WebWorldWind widget can only be used with encoding 2D result in png or jpeg.";
                errorHandlingService.notificationService.error(errorMessage);
            } else if (widgetType == "wwd" && command.widgetParameters.length > 0) {
                // Check if wwd widget if parameters specified then it must have 4 values: minLat,minLong,maxLat,maxLong with lat(-90:90), long(-180:180)
                if (command.widgetParameters.length != 4) {
                    errorMessage = "WebWorldWind widget with input parameters needs to follow this pattern: wwd(MIN_LAT,MIN_LONG,MAX_LAT,MAX_LONG).";
                    errorHandlingService.notificationService.error(errorMessage);
                } else {
                    // Validate all the min/max Lat, Long before displaying the result on WebWorldWind
                    var minLat = parseFloat(command.widgetParameters[0]);
                    var minLong = parseFloat(command.widgetParameters[1]);
                    var maxLat = parseFloat(command.widgetParameters[2]);
                    var maxLong = parseFloat(command.widgetParameters[3]);
                    if (minLat < -90 || minLat > 90) {
                        errorMessage = "WebWorldWind widget min Lat value is not within (-90:90), given: " + minLat + ".";
                        errorHandlingService.notificationService.error(errorMessage);
                    } else if (minLong < -180 || minLat > 180) {
                        errorMessage = "WebWorldWind widget min Long value is not within (-180:180), given: " + minLong + ".";
                        errorHandlingService.notificationService.error(errorMessage);
                    } else if (maxLat < -90 || maxLat > 90) {
                        errorMessage = "WebWorldWind widget max Lat value is not within (-90:90), given: " + maxLat + ".";
                        errorHandlingService.notificationService.error(errorMessage);
                    } else if (maxLong < -180 || maxLong > 180) {
                        errorMessage = "WebWorldWind widget max Long value is not within (-180:180), given: " + maxLong + "."
                        errorHandlingService.notificationService.error(errorMessage);
                    } else if (minLat > maxLat) {
                        errorMessage = "WebWorldWind widget min Lat cannot greater than max Lat, given: minLat: " + minLat + ", maxLat: " + maxLat + ".";
                        errorHandlingService.notificationService.error(errorMessage);
                    } else if (minLong > maxLong) {
                        errorMessage = "WebWorldWind widget min Long cannot greater than max Long, given: minLong: " + minLong + ", maxLong: " + maxLong + ".";
                        errorHandlingService.notificationService.error(errorMessage);
                    }                    
                }              
            }
            
            return errorMessage;
        }      
    }
}
