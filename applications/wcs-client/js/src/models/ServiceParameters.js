/**
 * Created by Alexandru on 19.10.2014.
 */

define(["src/models/CoverageSubtypeParent", "src/models/Extension"], function (CoverageSubtypeParent, Extension) {
    function ServiceParameters(json, isKVP) {
        if (isKVP) {

        } else {
            this.coverageSubtype = json.CoverageSubtype[0]._text;
            this.coverageSubtypeParent = json.CoverageSubtypeParent ? new CoverageSubtypeParent(json.CoverageSubtypeParent[0]) : null;
            this.nativeFormat = json.nativeFormat[0]._text;
            this.extension = json.Extension ? new Extension(json.Extension[0]) : null;
        }
    }

    return ServiceParameters;
});