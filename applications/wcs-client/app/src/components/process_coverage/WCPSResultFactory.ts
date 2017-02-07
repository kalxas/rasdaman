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

///<reference path="../../common/_common.ts"/>
///<reference path="WCPSQueryResult.ts"/>
///<reference path="RawWCPSResult.ts"/>
///<reference path="ImageWCPSResult.ts"/>
///<reference path="DiagramWCPSResult.ts"/>

module rasdaman {
	//Declare the fileSaver function so that typescript does not complain.
    declare var saveAs:any;
    export class WCPSResultFactory {
        public static getResult(errorHandlingService:any, command:WCPSCommand, data:any, mimeType:string, fileName:string):WCPSQueryResult {
            if (command.WidgetConfiguration == null) {
                // if mimeType is text then return raw data
                if (mimeType == "application/json" || mimeType == "text/plain" || mimeType == "application/gml+xml") {
                    return new RawWCPSResult(command, data);
                } else {
                    // if mimeType is image/binary file then download file
                    var blob = new Blob([data], {type: "application/octet-stream"});
                    saveAs(blob, fileName);
                    return null;
                }
            } else if (command.WidgetConfiguration.Type == "diagram") {
                // validate result for diagram widget (only 1D encoding in csv, json is supported)
                this.validateResult(errorHandlingService, command.WidgetConfiguration.Type, mimeType);
                return new DiagramWCPSResult(command, data);
            } else if (command.WidgetConfiguration.Type == "image") {
                // validate result for image widget (only 2D encoding in png, jpeg is supported)
                this.validateResult(errorHandlingService, command.WidgetConfiguration.Type, mimeType);
                return new ImageWCPSResult(command, data);
            } else {
                errorHandlingService.notificationService.error("The input widget: " + command.WidgetConfiguration.Type + " does not exist");
            }
        }     

        
        /**
        * @widgetType: the requested widget in WCPS query from WCS client (e.g: image>>, diagram>>)
        * @mimeType: the returned mimeType from Petascope (e.g: image/tiff, image/png,...)
        */
        public static validateResult(errorHandlingService:any, widgetType:any, mimeType:any) {
            // Check if diagram widget is requested with image/binary mime
            if (widgetType == "diagram" && ! (mimeType == "application/json" || mimeType == "text/plain")) {
                errorHandlingService.notificationService.error("Diagram widget can only be used with encoding 1D result in json or csv.");          
            } else if (widgetType == "image" && ! (mimeType == "image/png" || mimeType == "image/jpeg")) {
                // Check if image widget is requested with text or non-displayable mime
                errorHandlingService.notificationService.error("Image widget can only be used with encoding 2D result in png or jpeg.");
            }
             
        }        
   
    }
}
