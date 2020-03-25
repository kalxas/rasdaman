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

module wms {
    export class Layer {        
        // the gml of this layer as a part of result from GetCapabilities request
        // NOTE: WMS does not have a request to describe layer as WCS DescribeCoverage request
        public gmlDocument:string;
        public name:string;
        public title:string;
        public abstract:string;
        public customizedMetadata:ows.CustomizedMetadata;

        // EX_GeographicBoundingBox (reprojected native bounding box to EPSG:4326)        
        public coverageExtent:CoverageExtent;
                
        // native CRS (e.g: EPGS:4326)
        public crs:string;        
        // native bounding box (NOTE: it can be not EPSG:4326)
        public minx:Number;
        public miny:Number;
        public maxx:Number;
        public maxy:Number;
        //native dimensions array
        public layerDimensions:dimension[];
        // display layer's footprint on the globe
        public displayFootprint:boolean;
        
        // layer's styles
        public styles:Style[];

        // Default layer imported locally
        public importedType:String;

        // List of downscaled collection levels
        public downscaledCollectionLevels:String[];
        
        public constructor(gmlDocument:string, name:string, title:string, abstract:string, customizedMetadata:ows.CustomizedMetadata,
                           westBoundLongitude:Number, eastBoundLongitude:Number, 
                           southBoundLatitude:Number, northBoundLatitude:Number, crs:string,
                           minx:Number, miny:Number, maxx:Number, maxy:Number) {
            this.gmlDocument = gmlDocument;
            this.name = name;            
            this.title = title;
            this.abstract = abstract;
            this.customizedMetadata = customizedMetadata;
            this.coverageExtent = new CoverageExtent(name, westBoundLongitude, southBoundLatitude, eastBoundLongitude, northBoundLatitude);
            this.crs = crs;
            this.minx = minx;
            this.miny = miny;
            this.maxx = maxx;
            this.maxy = maxy;             
            // NOTE: not as WCS, all WMS layers can display on the globe, so no need to set it to null and hide the checkbox when coverage is not geo-referenced.           
            this.displayFootprint = true;
            
            this.layerDimensions = [];
            
            for(var j = 0; j < 3; ++j) {
                this.layerDimensions.push(dimen);
            }
            // build the dimension axis from gmlDocument of this layer
            j = 3;
            var dimen = this.initialiseDimenison();

            while(this.buildDimensionAxisFromGMLDocumet(dimen) != false) {
                
                this.layerDimensions.push(null);
                this.layerDimensions[j] = dimen;

                var dimen = this.initialiseDimenison();
                dimen.startPos = this.layerDimensions[j].startPos;
                j++;
            }

            this.importedType = "local";
            if (this.customizedMetadata != null && this.customizedMetadata.hostname != null) {            
                this.importedType = "remote";
            }
           
            // build styles from gmlDocument of this layer
            this.buildStylesFromGMLDocument();

            // get list of downscaled collection levels of this layer
            this.getDownscaledCollectionLevelsFromGMLDocument();
        }

        private initialiseDimenison() {
            return {
                name: '',
                array: [],
                startPos: 0,
                isTemporal: false
            }
        }

        // Extract the dimension's name and the possible values from which the client can select
        private buildDimensionAxisFromGMLDocumet(dim:dimension) {

            // search for the position of the beginning and of the end of the name
            var posNameStart = this.gmlDocument.indexOf('<Dimension name="', dim.startPos);
            if(posNameStart != -1){
                posNameStart += 17;
                var posNameEnd = this.gmlDocument.indexOf('">', posNameStart);
                
                // extract the name of the dimenisonal axis
                dim.name = this.gmlDocument.substr(posNameStart, posNameEnd - posNameStart);
                
                // search for the end of the elements of the dimensioal axis
                var posElementsStart = posNameEnd + 2;
                var posElementsEnd = this.gmlDocument.indexOf('</Dimension>', posElementsStart);
                dim.startPos = posElementsEnd;
                
                // extract the string that contains the elements
                var rawElementsString = this.gmlDocument.substr(posElementsStart, posElementsEnd - posElementsStart);
                
                // then the dimension is datetime
                if(rawElementsString[0] == '"') {

                    dim.isTemporal = true;
                    var positionEndMinElement = rawElementsString.indexOf('/');

                    // then the dimension is a datetime regular axis
                    if(positionEndMinElement != -1) {

                        // extract the first element of the axis as string
                        var minElementAsString = rawElementsString.substr(0, positionEndMinElement - 1);
                        minElementAsString = minElementAsString.substr(1, minElementAsString.length);

                        // search for the maximum element and extract the element as string
                        // NOTE: here to avoid searching again we can assume that the datetime has the same format so the same lenght, but I am not sure if this is always the case.
                        var positionEndMaxElement = rawElementsString.indexOf('/', positionEndMinElement + 1);
                        var maxElementAsString = rawElementsString.substr(positionEndMinElement + 1, positionEndMaxElement - positionEndMinElement - 2);
                        maxElementAsString = maxElementAsString.substr(1, maxElementAsString.length);

                        // extract the step as string and convert it into number
                        var stepAsString = rawElementsString.substr(positionEndMaxElement + 1, rawElementsString.length - positionEndMaxElement - 2);
                        var stepAsNumber = +stepAsString;

                        // convert the step from days into milliseconds
                        stepAsNumber *= 86400000;

                        // convert the min/max values from string to Date
                        var minElementAsDate = new Date(minElementAsString);
                        var maxElementAsDate = new Date(maxElementAsString);

                        // push all the possible values into the array of elements as Strings because it will be used after
                        // NOTE: here I also used the assumption that all the dates are in ISO format
                        for(var i = minElementAsDate; i <= maxElementAsDate; i.setMilliseconds(i.getMilliseconds() + stepAsNumber)) {
                            dim.array.push(i.toISOString());
                        }
                    }
                    else {
                        // then the dimension is a datetime irregular axis

                        var startCurrentElement = 1; 
                        var endCurrentElement = rawElementsString.indexOf('"', startCurrentElement);
                        endCurrentElement -= 1;

                        while(startCurrentElement < endCurrentElement) {

                            dim.array.push(rawElementsString.substr(startCurrentElement, endCurrentElement - startCurrentElement + 1));

                            startCurrentElement = endCurrentElement + 4;
                            endCurrentElement = rawElementsString.indexOf('"', startCurrentElement);
                            endCurrentElement -= 1;
                        }
                    }
                }
                else {
                    // then the dimension is numerical
                    var positionEndMinElement = rawElementsString.indexOf('/');
                    dim.isTemporal = false;

                    // then the dimension is a numerical regular axis 
                    if(positionEndMinElement != -1) {
                        var minElementAsString = rawElementsString.substr(0, positionEndMinElement);

                        positionEndMaxElement = rawElementsString.indexOf('/', positionEndMinElement + 1);
                        var maxElementAsString = rawElementsString.substr(positionEndMinElement + 1, positionEndMaxElement - positionEndMinElement - 1);
                        
                        var stepAsString = rawElementsString.substr(positionEndMaxElement + 1, rawElementsString.length - positionEndMaxElement);
                        

                        // converting the max, min element and the step from string into number
                        if(minElementAsString[0] == '-') {
                            minElementAsString = minElementAsString.substr(1, minElementAsString.length);
                            var minElementAsNumber = -minElementAsString;
                        }
                        else {
                            minElementAsNumber = +minElementAsString;
                        }

                        if(maxElementAsString[0] == '-') {
                            maxElementAsString = maxElementAsString.substr(1, maxElementAsString.length);
                            var maxElementAsNumber = -maxElementAsString;
                        }
                        else {
                            maxElementAsNumber = +maxElementAsString;
                        }

                        var rg = /[^a-zA-Z]/g;
                        stepAsString = ""+stepAsString.match(rg);

                        if(stepAsString[0] == '-') {
                            stepAsString = stepAsString.substr(1, stepAsString.length);
                            var stepAsNumber = -stepAsString;
                        }
                        else {
                            stepAsNumber = +stepAsString;
                        }

                        // push all the possible values into the array of elements as String because it will be used after
                        for(var it = minElementAsNumber; it <= maxElementAsNumber; it += stepAsNumber) {
                            dim.array.push((""+it));
                        }
                    }
                    else {
                        // then the dimension is a numerical irregular axis

                        var startCurrentElement = 0; 
                        var endCurrentElement = rawElementsString.indexOf(',', startCurrentElement);
                        if(endCurrentElement == -1) {
                            endCurrentElement = rawElementsString.length;
                        }

                        while(startCurrentElement < endCurrentElement) {
                            dim.array.push(rawElementsString.substr(startCurrentElement, endCurrentElement - startCurrentElement));

                            startCurrentElement = endCurrentElement + 1;
                            endCurrentElement = rawElementsString.indexOf(',', startCurrentElement);

                            if(endCurrentElement == -1) {
                                endCurrentElement = rawElementsString.length;
                            }
                        }

                    }
                }

                return true;
            }
            else {
                // else there is no dimentioal axis
                return false;
            }
            
        }

        // Extract the list of downscaled collection levels of this layer
        private getDownscaledCollectionLevelsFromGMLDocument() {
            this.downscaledCollectionLevels = [];
            
            var tmpXML = $.parseXML(this.gmlDocument);
            var text = $(tmpXML).find("rasdaman\\:downscaledCollectionLevels").text();
            if (text !== "") {
                this.downscaledCollectionLevels = text.split(",");                
            }
        }

        // Extract the Style element of layer to an array
        private buildStylesFromGMLDocument() {
            this.styles = [];
            var tmpXML = $.parseXML(this.gmlDocument);
            var totalStyles = $(tmpXML).find("Style").length;
            for (var i = 0; i < totalStyles; i++) {
                var styleXML = $(tmpXML).find("Style").eq(i);
                var name = styleXML.find("Name").text();
                var abstractContent = styleXML.find("Abstract").text();
                var userAbstract = abstractContent.substring(0, abstractContent.indexOf("<rasdaman>")).trim();
                var rasdamanAbstract = abstractContent.substring(abstractContent.indexOf("<rasdaman>"), abstractContent.length).trim();

                // Parse element values from rasdaman
                var rasdamanXML = $.parseXML(rasdamanAbstract);
                var queryType = "none";
                var query = "";

                if ($(rasdamanXML).find("WcpsQueryFragment").text() != "") {
                    queryType = "wcpsQueryFragment";
                    query = $(rasdamanXML).find("WcpsQueryFragment").text();
                } else if ($(rasdamanXML).find("RasqlTransformFragment").text() != "") {
                    queryType = "rasqlTransformFragment";
                    query = $(rasdamanXML).find("RasqlTransformFragment").text();
                }

                query = query.replace(/&amp;lt;/g, "<").replace(/&amp;gt;/g, ">");

                var colorTableType = "";
                var colorTableDefinition = "";

                if ($(rasdamanXML).find("ColorTableType").text() != "") {
                    colorTableType = $(rasdamanXML).find("ColorTableType").text();
                }
                if ($(rasdamanXML).find("ColorTableDefinition").text() != "") {
                    // as the content as SLD format is XML as well, it needs to show the raw text
                    colorTableDefinition = rasdamanAbstract.match(/<ColorTableDefinition>([\s\S]*?)<\/ColorTableDefinition>/im)[1];
                }

                this.styles.push(new Style(name, userAbstract, queryType, query, colorTableType, colorTableDefinition));
            }
        }
    }

    interface dimension {

        name:string;
        array:string[];
        startPos:number;
        isTemporal:boolean;
    }
}