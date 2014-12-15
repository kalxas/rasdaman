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

define(["src/models/CoverageSubtypeParent", "src/models/Extension", "src/models/Description", "src/models/Inherit", "src/models/Metadata"], function (CoverageSubtypeParent, Extension, Description, Inherit, Metadata) {

    function CoverageSummary(json, isKVP) {
        var i = 0;
        var box;

        Description.call(this, json, isKVP);

        if (isKVP) {

        } else {
            if (!json.CoverageId || !json.CoverageSubtype) {
                throw new Error("Invalid json" + JSON.stringify(json));
            }

            this.coverageId = json.CoverageId[0]._text;
            this.coverageSubtype = json.CoverageSubtype[0]._text;
            this.coverageSubtypeParent = json.CoverageSubtypeParent ? new CoverageSubtypeParent(json.CoverageSubtypeParent[0]) : null;

            //TODO:The extension does not appear in the schema
            this.extension = json.Extension ? new Extension(json.Extension[0]) : null;

            /* for (j = 0; json.wgs84BoundingBox && j < json.wgs84BoundingBox.length; j++) {
             box = json.wgs84BoundingBox[j];
             //TODO Something
             }

             for (j = 0; json.boundingBox && j < json.boundingBox.length; j++) {
             box = json.boundingBox[j];
             //TODO Something
             }
             */

            this.metadata = [];

            for (j = 0; json.Metadata && j < json.Metadata.length; j++) {
                this.metadata.push(new Metadata(json.Metadata[j]));
            }
        }

    }

    Inherit(CoverageSummary, Description);

    return CoverageSummary;
});