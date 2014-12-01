/**
 * Created by Alexandru on 13.10.2014.
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

    return WCS;
});