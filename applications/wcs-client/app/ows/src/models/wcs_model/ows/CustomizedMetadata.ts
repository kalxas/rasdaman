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
 * Copyright 2003 - 2019 Peter Baumann /
 rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */

///<reference path="../../../common/_common.ts"/>

module ows {
    // Used only for result of WCS GetCapabilities (not coverage's real metadata)
    export class CustomizedMetadata {
        public hostname:String;
        public petascopeEndPoint:String;

        // Convert value of element coverageSizeInBytes to a human-readable value (e.g: 1000 -> 1KB)
        // "N/A" if size doesn't exist from GetCapabilities result
        public coverageSize:String;
        public localCoverageSizeInBytes:number = 0;
        public remoteCoverageSizeInBytes:number = 0;
        public isBlackedList:boolean;

        public constructor(source:rasdaman.common.ISerializedObject) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");            

            this.parseCoverageLocation(source);
            this.parseCoverageSizeInBytes(source);
            this.parseBlackListed(source);
        }

        /**
         * If rasdaman:blackListed exists, then get it as true or false
         * 
        <ows:AdditionalParameter>
            <ows:Name>blackListed</ows:Name>
            <ows:Value>false</ows:Value>
        </ows:AdditionalParameter>
         */
        private parseBlackListed(source:rasdaman.common.ISerializedObject):void {            
            this.isBlackedList = JSON.parse(this.parseAdditionalElementValueByName(source, "blackListed"));            
        }

        /**
         If in customized metadata it exists, then, get hostname and endpoint from location element.

        <ows:AdditionalParameter>
            <ows:Name>hostname</ows:Name>
            <ows:Value>mundi.earthserver.xyz</ows:Value>
        </ows:AdditionalParameter>
        <ows:AdditionalParameter>
            <ows:Name>endpoint</ows:Name>
            <ows:Value>http://mundi.earthserver.xyz:8080/rasdaman/ows</ows:Value>
        </ows:AdditionalParameter>

          */        
        private parseCoverageLocation(source:rasdaman.common.ISerializedObject):void {           
            this.hostname = this.parseAdditionalElementValueByName(source, "hostname");            
            this.petascopeEndPoint = this.parseAdditionalElementValueByName(source, "endpoint");
        }

        /**
         * If rasdaman:sizeInbytes exists, then parse it and convert to human-readable value.
         * 
        <ows:AdditionalParameter>
            <ows:Name>sizeInBytes</ows:Name>
            <ows:Value>224775000</ows:Value>
        </ows:AdditionalParameter>
         */
        private parseCoverageSizeInBytes(source:rasdaman.common.ISerializedObject):void {
            let sizeInBytesValue = this.parseAdditionalElementValueByName(source, "sizeInBytes");
            if (sizeInBytesValue === null) {
                this.coverageSize = "N/A";
            } else {
                let number = parseInt(sizeInBytesValue);
                this.coverageSize = CustomizedMetadata.convertNumberOfBytesToHumanReadable(number);

                if (this.hostname === null) {
                    // local node
                    this.localCoverageSizeInBytes = number;
                } else {
                    // remote node
                    this.remoteCoverageSizeInBytes = number;
                }
            }
        }

        /**
         * Convert a number of bytes to human readable string.
         * e.g: 1000 -> 1 KB         
         */
        public static convertNumberOfBytesToHumanReadable(numberOfBytes):String {
            if (numberOfBytes == 0) {
                return "0 B";
            }
            const k = 1000;            
            const sizes = ['B', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];
            let i = Math.floor(Math.log(numberOfBytes) / Math.log(k));
            let result = parseFloat((numberOfBytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];           

            return result;
        }

        /**
         * Given AdditionalParameters element, return the nested child AdditionalParameter element's value
         * which has Name element matches with the input name          
         */
        private parseAdditionalElementValueByName(source:rasdaman.common.ISerializedObject, inputNameElement:string): string {
            let additionalElements = source.getChildrenAsSerializedObjects("AdditionalParameter");

            for (let i = 0; i < additionalElements.length; i++) {
                let nameElement = additionalElements[i].getChildAsSerializedObject("Name");
                let name = nameElement.getValueAsString();                

                if (name === inputNameElement) {
                    let valueElement = additionalElements[i].getChildAsSerializedObject("Value");                    
                    return valueElement.getValueAsString();
                }                
            }

            return null;
        }
    }
}
