/**
 * Created by Alexandru on 19.10.2014.
 */
define(["src/models/GMLBoundedBy"], function (GMLBoundedBy) {
    function GMLAbstractFeature(json, isKVP) {
        if (isKVP) {

        } else {
            this.boundedBy = json.boundedBy ? new GMLBoundedBy(json.boundedBy[0]) : null;
        }
    }

    return GMLAbstractFeature;
});