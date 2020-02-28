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

module wms {
    export class UpdateLayerStyle implements rasdaman.common.ISerializable {
        public request:string;
        public layerName:string;
        public name:string;
        public abstract:string;                
        public queryFragmentType:string;
        public query:string;
        public colorTableType:string;
        public colorTableDefinition:string;

        public constructor(layerName:string, name:string, abstract:string, queryType:string, query:string,
                           colorTableType:string, colorTableDefintion:string) {  
            this.request = "UpdateStyle";
            this.layerName = layerName;
            this.name = name;
            this.abstract = abstract;
            this.queryFragmentType = queryType;      
            this.query = query;
            this.colorTableType = colorTableType;
            this.colorTableDefinition = colorTableDefintion;
        }

        public toKVP():string {
            var result = "&request=" + this.request +
                    "&name=" + this.name +
                    "&layer=" + this.layerName +
                    "&abstract=" + this.abstract;

            result += "&" + this.queryFragmentType + "=" + this.query;
                        
            result += "&ColorTableType=" + this.colorTableType + 
                        "&ColorTableDefinition=" + this.colorTableDefinition;         
            
            return result;                
        }
    }
}
