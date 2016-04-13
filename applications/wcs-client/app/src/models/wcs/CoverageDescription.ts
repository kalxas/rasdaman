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

///<reference path="../gml/_gml.ts"/>
///<reference path="../gmlcov/_gmlcov.ts"/>
///<reference path="ServiceParameters.ts"/>

module wcs {
    export class CoverageDescription extends gml.AbstractFeature {
        public CoverageId:string;
        public CoverageFunction:gml.CoverageFunction;
        public Metadata:gmlcov.Metadata[];
        public DomainSet:gml.DomainSet;
        public RangeType:gmlcov.RangeType;
        public ServiceParameters:wcs.ServiceParameters;


        public constructor(source:rasdaman.common.ISerializedObject) {
            super(source);

            rasdaman.common.ArgumentValidator.isNotNull(source, "source");

            this.CoverageId = source.getChildAsSerializedObject("wcs:CoverageId").getValueAsString();

            if (source.doesElementExist("gml:coverageFunction")) {
                this.CoverageFunction = new gml.CoverageFunction(source.getChildAsSerializedObject("gml:coverageFunction"));
            }

            this.Metadata = [];
            source.getChildrenAsSerializedObjects("gmlcov:metadata").forEach(o=> {
                this.Metadata.push(new gmlcov.Metadata(o));
            });

            this.DomainSet = new gml.DomainSet(source.getChildAsSerializedObject("gml:domainSet"));

            this.RangeType = new gmlcov.RangeType(source.getChildAsSerializedObject("gmlcov:rangeType"));

            this.ServiceParameters = new wcs.ServiceParameters(source.getChildAsSerializedObject("wcs:ServiceParameters"));
        }
    }
}