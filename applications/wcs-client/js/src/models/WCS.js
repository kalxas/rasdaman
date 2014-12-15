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

define(function () {
    "use strict";

    function WCS(/*string*/ endpoint) {
        this.endpoint = endpoint;
        this.service = "WCS";
        this.version = "2.0.1";
    }

    WCS.prototype.getCapabilities = function (/*string[]*/acceptVersions, /*Section[]*/ sections, /*string[]*/ acceptFormats, /*string*/updateSequence, /*string[]*/acceptLanguages) {

        this.request = "GetCapabilities";
        this.acceptVersions = acceptVersions;
        this.sections = sections;
        this.acceptFormats = acceptFormats;
        this.updateSequence = updateSequence;
        this.acceptLanguages = acceptLanguages;

        var url = this.endpoint + "SERVICE=" + this.service + "&VERSION=" + this.version + "&REQUEST=" + this.request;
        if (this.acceptVersions && $.isArray(this.acceptVersions)) {
            url += "&ACCEPTVERSIONS=" + this.acceptVersions.join(",");
        }
        if (this.sections) {
            url += "&SECTIONS=" + this.sections
        }
        if (this.updateSequence) {
            url += "&UPDATESEQUENCE=" + this.updateSequence;
        }
        if (this.acceptFormats && $.isArray(this.acceptFormats)) {
            url += "&ACCEPTFORMATS=" + this.acceptFormats.join(",");
        }
        if (this.acceptLanguages && $.isArray(this.acceptLanguages)) {
            url += "&ACCEPTLANGUAGES=" + this.acceptLanguages.join(" ");
        }

        return url;
    };

    WCS.prototype.describeCoverage = function (/*string*/coverageId) {
        this.request = "DescribeCoverage";
        var url = this.endpoint + "SERVICE=" + this.service + "&VERSION=" + this.version + "&REQUEST=" + this.request;

        if (!coverageId || typeof(coverageId) != "string") {
            throw new Error("You must select at least one coverage.");
        }

        url += "&COVERAGEID=" + coverageId;
        return url;
    };

    WCS.prototype.getCoverage = function (/*string*/coverageId, /*string*/format, /*bool*/multipart) {
        this.request = "GetCoverage";
        var url = this.endpoint + "SERVICE=" + this.service + "&VERSION=" + this.version + "&REQUEST=" + this.request;
        if (!coverageId || typeof(coverageId) != "string") {
            throw new Error("You must select at least one coverage.");
        }
        url += "&COVERAGEID=" + coverageId;

        if (format) {
            url += "&FORMAT=" + format;
        }

        if (multipart) {
            url += "&MEDIATYPE=multipart/related";
        }

        return url;
    };

    WCS.prototype.WCSPUrl = function () {
        return this.endpoint.substr(0, this.endpoint.lastIndexOf('?')) + "/" + "SERVICE=" + this.service + "&VERSION=" + this.version + "&REQUEST=ProcessCoverages";
    };

    WCS.prototype.insertCoverage = function (/*string*/coverageRef, /*bool*/useNewId) {
        this.request = "InsertCoverage";
        var url = this.endpoint + "SERVICE=" + this.service + "&VERSION=" + this.version + "&request=" + this.request;
        if (!coverageRef || typeof(coverageRef) != "string") {
            throw new Error("You must indicate a coverage source.");
        }
        url += "&coverageRef=" + encodeURI(coverageRef);
        if(useNewId){
            url += "&useId=new";
        }
        return url;
    }

    WCS.prototype.deleteCoverage = function(/*string*/coverageId) {
        this.request = "DeleteCoverage";
        var url = this.endpoint + "SERVICE=" + this.service + "&VERSION=" + this.version + "&request=" + this.request;
        if (!coverageId || typeof(coverageId) != "string") {
            throw new Error("You must indicate at least one coverage id.");
        }
        url += "&coverageId=" + coverageId;
        return url;
    }

    return WCS;
});