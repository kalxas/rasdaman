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

        // EX_GeographicBoundingBox (reprojected native bounding box to EPSG:4326)        
        public coverageExtent:CoverageExtent;
                
        // native CRS (e.g: EPGS:4326)
        public crs:string;        
        // native bounding box (NOTE: it can be not EPSG:4326)
        public minx:Number;
        public miny:Number;
        public maxx:Number;
        public maxy:Number;
        // display layer's footprint on the globe
        public displayFootprint:boolean;
        
        // layer's styles
        public styles:Style[];
        
        public constructor(gmlDocument:string, name:string, title:string, abstract:string, westBoundLongitude:Number, eastBoundLongitude:Number, 
                           southBoundLatitude:Number, northBoundLatitude:Number, crs:string,
                           minx:Number, miny:Number, maxx:Number, maxy:Number) {
            this.gmlDocument = gmlDocument;
            this.name = name;            
            this.title = title;
            this.abstract = abstract;
            this.coverageExtent = new CoverageExtent(name, westBoundLongitude, southBoundLatitude, eastBoundLongitude, northBoundLatitude);
            this.crs = crs;
            this.minx = minx;
            this.miny = miny;
            this.maxx = maxx;
            this.maxy = maxy;             
            // NOTE: not as WCS, all WMS layers can display on the globe, so no need to set it to null and hide the checkbox when coverage is not geo-referenced.           
            this.displayFootprint = true;

            // build styles from gmlDocument of this layer
            this.buildStylesFromGMLDocument();
        }

        // Extract the Style element of layer to an array
        private buildStylesFromGMLDocument() {
            this.styles = [];
            var tmpXML = $.parseXML(this.gmlDocument);
            var totalStyles = $(tmpXML).find("Style").length;
            for (var i = 0; i < totalStyles; i++) {
                var styleXML = $(tmpXML).find("Style").eq(i);
                var name = styleXML.find("Name").text();
                var abstract = styleXML.find("Abstract").text();
                            
                // parse abstract to know it is rasql transform or wcps fragment query and get the query inside abstract also
                var queryType = 0;
                var query = "";
                var tmp = "";
                if (abstract.indexOf("Rasql transform fragment: ") == -1) {
                    // wcps query fragment
                    queryType = 0;                    
                    tmp = "WCPS query fragment: ";
                } else {
                    // rasql query fragment                    
                    queryType = 1;
                    tmp = "Rasql transform fragment: ";                    
                } 

                // the query (rasql/wcps) for the style
                query = abstract.substring(abstract.indexOf(tmp) + tmp.length, abstract.length);
                var styleAbstract = abstract.substring(0, abstract.indexOf(tmp) - 2).trim();

                this.styles.push(new Style(name, styleAbstract, queryType, query));
            }
        }
    }
}