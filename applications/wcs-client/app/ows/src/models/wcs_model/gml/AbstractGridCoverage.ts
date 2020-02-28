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

module gml {

    /**
     * Abstract class for 3 supported Grid Coverage types
     */
    export abstract class AbstractGridCoverage {
        
        public gridEnvelope:GridEnvelope;
        public offsetVectors:String[] = [];
        public axisTypes:String[] = [];

        protected REGULAR_AXIS:String = "Regular Axis";
        protected IRREGULAR_AXIS:String = "Irregular Axis";
        protected IRREGULAR_AXIS_RESOLUTION = "N/A";

        protected currentSource:rasdaman.common.ISerializedObject;

        public constructor(source:rasdaman.common.ISerializedObject) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");
        }

        /**
         * Build properties of this object
         */
        public buildObj(): void {
            this.parseGridEnvelope();
            this.parseAxisTypesAndOffsetVectors();
        }

        /**
         * Parse GridEnvelope objects containing Grid Axes Extents         
         */
        private parseGridEnvelope(): void {
            this.gridEnvelope = new GridEnvelope(this.currentSource.getChildAsSerializedObject("gml:limits"));
        }

        /**
         Parse a list of axes resolutions (offset vectors)
         */
        protected abstract parseAxisTypesAndOffsetVectors():void;

    }
}