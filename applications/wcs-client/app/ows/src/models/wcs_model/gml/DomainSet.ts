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

    export class DomainSet {
        
        public abstractGridCoverage:AbstractGridCoverage;

        public constructor(source:rasdaman.common.ISerializedObject) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");

            if (source.doesElementExist("gml:Grid")) {
                // Grid Coverage
                this.abstractGridCoverage = new GridCoverage(source);
            } else if (source.doesElementExist("gml:RectifiedGrid")) {
                // Rectified Grid Coverage
                this.abstractGridCoverage = new RectifiedGridCoverage(source);
            } else if (source.doesElementExist("gmlrgrid:ReferenceableGridByVectors")) {
                // Referenceable Grid Coverage
                this.abstractGridCoverage = new ReferenceableGridCoverage(source);
            }

            this.abstractGridCoverage.buildObj();
        }
    }
}