/**
 * Created by Alexandru on 13.10.2014.
 */

define(function () {
    function GetCapabilities(service, request, acceptVersions, sections, acceptFormats, updateSequence, acceptLanguage) {
        this.service = service;
        this.request = request;
        this.acceptVersions = acceptVersions;
        this.acceptFormats = acceptFormats;
        this.updateSequence = updateSequence;
        this.sections = sections;
    }

    GetCapabilities.prototype.toXML = function(){

    };

    GetCapabilities.prototype.toKVP = function(){

    };

    return GetCapabilities;

});