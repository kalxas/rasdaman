/**
 * Created by Alexandru on 15.10.2014.
 */

define(function () {
    function CoverageSubtypeParent(json, isKVP) {
        if (isKVP) {

        } else {
            if (!json.CoverageSubtype) {
                throw new Error("Invalid json" + JSON.stringify(json));
            }

            this.coverageSubtype = json.CoverageSubtype[0]._text;
            this.coverageSubtypeParent = json.CoverageSubtypeParent ? new CoverageSubtypeParent(json.CoverageSubtypeParent[0], isKVP) : null;
        }
    }

    return CoverageSubtypeParent;
});