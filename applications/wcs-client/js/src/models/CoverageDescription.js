/*
 * This file is part of rasdaman community.
 *
 * Rasdaman community is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Rasdaman community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009,2010,2011,2012,2013,2014 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
define(["src/models/GMLAbstractFeature", "src/models/Inherit", "src/models/ServiceParameters", "src/models/Extension", "src/models/CoverageFunction", "src/models/GMLMetadata", "src/models/DomainSet", "src/models/RangeType"], function (GMLAbstractFeature, Inherit, ServiceParameters, Extension, CoverageFunction, GMLMetadata, DomainSet, RangeType) {
    function CoverageDescription(json, isKVP) {
        GMLAbstractFeature.call(this, json, isKVP);
        if (isKVP) {

        } else {
            if (!json.CoverageId || !json.domainSet || !json.rangeType || !json.ServiceParameters) {
                throw new Error("Invalid json:" + JSON.stringify(json));
            }
            this.coverageId = json.CoverageId[0]._text;
            this.coverageFunction = json.coverageFunction ? new CoverageFunction(json.coverageFunction[0]) : null;

            this.metadata = [];
            for (var i = 0; json.metadata && i < json.metadata.length; i++) {
                this.metadata.push(new GMLMetadata(json.metadata[i], isKVP));
            }

            this.domainSet = new DomainSet(json.domainSet[0]);
            this.rangeType = new RangeType(json.rangeType[0]);
            this.serviceParameters = new ServiceParameters(json.ServiceParameters[0]);
        }
    }

    Inherit(CoverageDescription, GMLAbstractFeature);

    return CoverageDescription;
});