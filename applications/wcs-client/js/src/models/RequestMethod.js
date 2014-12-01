/**
 * Created by Alexandru on 13.10.2014.
 */
define(function () {
    function RequestMethod(linkage, url, constraint) {
        this.linkage = linkage;
        this.url = url;
        this.constraint = constraint;
    }

    RequestMethod.prototype.toXML = function () {

    };

    RequestMethod.prototype.toKVP = function () {

    };

    return RequestMethod;
});