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
///<reference path="ServiceIdentification.ts"/>

module wms {
    export class Capabilities {        
        // WMS does not have the ServiceIdentification element as WCS, but they contain almost same content.
        public serviceIdentification:ServiceIdentification;
        // WMS does not have the ServiceProrivder element as WCS, but they contain almost same content.
        public serviceProvider:ServiceProvider;
        // 2D output format of GetMap request (png/jpeg/tiff)
        public getMapFormat:string[];
        public layers:Layer[];
        public gmlDocument:string;

        // If at least 1 coverage is remote then show Layer location in WMS GetCapabilities layers table
        public showLayerLocationsColumn:boolean;
        public showLayerSizesColumn:boolean;
        // If a layer is blacklisted, only petascope admin user can see it from GetCapabilities
        public showBlackListedColumn:boolean;

        public totalLocalLayerSizes:String;
        public totalRemoteLayerSizes:String;
        public totalLayerSizes:String;
        public numberOfLayers:String;

        // source is the JSON object parsed from gmlDocument (a full XML result of WMS GetCapabilities request)
        public constructor(source:rasdaman.common.ISerializedObject, gmlDocument:string) {
            this.gmlDocument = gmlDocument;

            rasdaman.common.ArgumentValidator.isNotNull(source, "source");       
            
            if (source.doesElementExist("Service")) {
                var serviceObj = source.getChildAsSerializedObject("Service");

                // First, build serviceIdentification object                
                var title = serviceObj.getChildAsSerializedObject("Title").getValueAsString();
                var abstract = serviceObj.getChildAsSerializedObject("Abstract").getValueAsString();

                this.serviceIdentification = new wms.ServiceIdentification(title, abstract);        
                
                // Then, build serviceProvider object
                var onlineResourceObj = serviceObj.getChildAsSerializedObject("OnlineResource");
                var contactInformationObj = serviceObj.getChildAsSerializedObject("ContactInformation");
                var contactPersonPrimaryObj = contactInformationObj.getChildAsSerializedObject("ContactPersonPrimary");
                var contactAdressObj = contactInformationObj.getChildAsSerializedObject("ContactAddress");

                var providerName = contactPersonPrimaryObj.getChildAsSerializedObject("ContactOrganization").getValueAsString();
                var providerSite = onlineResourceObj.getAttributeAsString("href");
                var contactPersion = contactPersonPrimaryObj.getChildAsSerializedObject("ContactPerson").getValueAsString();
                var positionName = contactInformationObj.getChildAsSerializedObject("ContactPosition").getValueAsString();
                var email = contactInformationObj.getChildAsSerializedObject("ContactElectronicMailAddress").getValueAsString();
                var voicePhone = contactInformationObj.getChildAsSerializedObject("ContactVoiceTelephone").getValueAsString();
                
                var address = contactAdressObj.getChildAsSerializedObject("Address").getValueAsString();
                var city = contactAdressObj.getChildAsSerializedObject("City").getValueAsString();
                var postCode = contactAdressObj.getChildAsSerializedObject("PostCode").getValueAsString();
                var country = contactAdressObj.getChildAsSerializedObject("Country").getValueAsString();

                this.serviceProvider = new wms.ServiceProvider(providerName, providerSite, contactPersion,
                                                               positionName, email, voicePhone, address, city, postCode, country);

                // Then, get the supported format for output of GetMap request
                var capabilityObj = source.getChildAsSerializedObject("Capability");
                var getMapObj = capabilityObj.getChildAsSerializedObject("Request").getChildAsSerializedObject("GetMap");

                this.getMapFormat = [];
                getMapObj.getChildrenAsSerializedObjects("Format").forEach(obj => {
                    this.getMapFormat.push(obj.getValueAsString());
                });

                // Then, get all the WMS layers
                var layerObjs = capabilityObj.getChildAsSerializedObject("Layer").getChildrenAsSerializedObjects("Layer");
                this.layers = [];

                let totalLocalLayerSizesInBytes = 0;
                let totalRemoteLayerSizesInBytes = 0;
                let totalLayerSizesInBytes = 0;

                layerObjs.forEach(obj => {
                    var name = obj.getChildAsSerializedObject("Name").getValueAsString();
                    var title = obj.getChildAsSerializedObject("Title").getValueAsString();
                    var abstract = obj.getChildAsSerializedObject("Abstract").getValueAsString();

                    var customizedMetadata = this.parseLayerCustomizedMetadata(obj);

                    if (customizedMetadata != null) {
                        if (customizedMetadata.hostname != null) {
                            this.showLayerLocationsColumn = true;
                        }

                        if (customizedMetadata.coverageSize != null) {
                            this.showLayerSizesColumn = true;
                        }

                        if (customizedMetadata.localCoverageSizeInBytes > 0) {
                            totalLocalLayerSizesInBytes += customizedMetadata.localCoverageSizeInBytes;
                        } else {
                            totalRemoteLayerSizesInBytes += customizedMetadata.remoteCoverageSizeInBytes;
                        }     

                        if (customizedMetadata.isBlackedList != null) {
                            this.showBlackListedColumn = true;
                        }
                    }
                    
                    // native CRS of layer
                    var crs = obj.getChildAsSerializedObject("CRS").getValueAsString();

                    // NOTE: WMS already reprojected bounding boxes to EPSG:4326 with lat, long order for EX_GeographicBoundingBox element
                    var exBBox = obj.getChildAsSerializedObject("EX_GeographicBoundingBox");
                    var westBoundLongitude = exBBox.getChildAsSerializedObject("westBoundLongitude").getValueAsNumber();
                    var eastBoundLongitude = exBBox.getChildAsSerializedObject("eastBoundLongitude").getValueAsNumber();
                    var southBoundLatitude = exBBox.getChildAsSerializedObject("southBoundLatitude").getValueAsNumber();
                    var northBoundLatitude = exBBox.getChildAsSerializedObject("northBoundLatitude").getValueAsNumber();

                    // The bounding box in native CRS (maybe not EPSG:4326)
                    var bboxObj = obj.getChildAsSerializedObject("BoundingBox");
                    // epsg code (e.g: EPSG:4326)
                    var crs = bboxObj.getAttributeAsString("CRS");
                    var minx = bboxObj.getAttributeAsNumber("minx");
                    var miny = bboxObj.getAttributeAsNumber("miny");
                    var maxx = bboxObj.getAttributeAsNumber("maxx");
                    var maxy = bboxObj.getAttributeAsNumber("maxy");
                    
                    var layerGMLDocument = this.extractLayerGMLDocument(name);

                    this.layers.push(new wms.Layer(layerGMLDocument, name, title, abstract, customizedMetadata,
                                                   westBoundLongitude, eastBoundLongitude, southBoundLatitude, northBoundLatitude,
                                                   crs, minx, miny, maxx, maxy));
                });

                totalLayerSizesInBytes += totalLocalLayerSizesInBytes + totalRemoteLayerSizesInBytes;

                // Convert Bytes to GBs for total sizes of layers
                this.totalLocalLayerSizes = ows.CustomizedMetadata.convertNumberOfBytesToHumanReadable(totalLocalLayerSizesInBytes);
                this.totalRemoteLayerSizes = ows.CustomizedMetadata.convertNumberOfBytesToHumanReadable(totalRemoteLayerSizesInBytes);
                this.totalLayerSizes = ows.CustomizedMetadata.convertNumberOfBytesToHumanReadable(totalLayerSizesInBytes);
                this.numberOfLayers = layerObjs.length.toString();
            }
        }

        /**
         * Parse layer's customized metadata (if any)
         */
        private parseLayerCustomizedMetadata(source:rasdaman.common.ISerializedObject) {
            let childElement = "ows:AdditionalParameters";
            let customizedMetadata:ows.CustomizedMetadata = null;

            if (source.doesElementExist(childElement)) {
                customizedMetadata = new ows.CustomizedMetadata(source.getChildAsSerializedObject(childElement));
            }

            return customizedMetadata;            
        }

        // extract the specific GML by layer name from the full GML result of GetCapabilities request
        private extractLayerGMLDocument(layerName:string) {
            var regex = /<Layer \S+[\s\S]*?<\/Layer>/g;
            var match = regex.exec(this.gmlDocument);
            
            // Iterate all the matching layers until it find the specific layer
            while (match != null) {                
                if (match[0].indexOf("<Name>" + layerName + "</Name>") !== -1) {
                    return match[0];
                }
                match = regex.exec(this.gmlDocument);
            }    
            
            return null;
        }
    }
}
