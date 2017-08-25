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

///<reference path="../gml/_gml.ts"/>
///<reference path="../gmlcov/_gmlcov.ts"/>
///<reference path="ServiceParameters.ts"/>

module wcs {
    export class CoverageDescription extends gml.AbstractFeature {
        public coverageId:string;
        public coverageFunction:gml.CoverageFunction;
        public metadata:gmlcov.Metadata[];
        public domainSet:gml.DomainSet;
        public rangeType:gmlcov.RangeType;
        public serviceParameters:wcs.ServiceParameters;


        public constructor(source:rasdaman.common.ISerializedObject) {
            super(source);

            rasdaman.common.ArgumentValidator.isNotNull(source, "source");

            this.coverageId = source.getChildAsSerializedObject("wcs:CoverageId").getValueAsString();

            if (source.doesElementExist("gml:coverageFunction")) {
                this.coverageFunction = new gml.CoverageFunction(source.getChildAsSerializedObject("gml:coverageFunction"));
            }

            this.metadata = [];
            source.getChildrenAsSerializedObjects("gmlcov:metadata").forEach(o=> {
                this.metadata.push(new gmlcov.Metadata(o));
            });

            this.domainSet = new gml.DomainSet(source.getChildAsSerializedObject("gml:domainSet"));

            this.rangeType = new gmlcov.RangeType(source.getChildAsSerializedObject("gmlcov:rangeType"));

            this.serviceParameters = new wcs.ServiceParameters(source.getChildAsSerializedObject("wcs:ServiceParameters"));
        }
    }
}