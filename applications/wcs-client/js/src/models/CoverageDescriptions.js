/**
 * Created by Alexandru on 19.10.2014.
 */

define(["src/models/CoverageDescription"], function (CoverageDescription) {
    function CoverageDescriptions(json, isKVP) {
        if (isKVP) {

        } else {
            this.coverageDescription = [];
            for (var i = 0; json.CoverageDescription && i < json.CoverageDescription.length; i++) {
                this.coverageDescription.push(new CoverageDescription(json.CoverageDescription[i]));
            }
        }
    }

    return CoverageDescriptions;
});