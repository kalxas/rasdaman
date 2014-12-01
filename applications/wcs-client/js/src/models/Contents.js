/**
 * Created by Alexandru on 17.10.2014.
 */

define(["src/models/ContentsBaseType", "src/models/Inherit", "src/models/CoverageSummary", "src/models/Extension"], function (ContentsBaseType, Inherit, CoverageSummary, Extension) {
    function Contents(json, isKVP) {
        ContentsBaseType.call(this, json, isKVP);

        var i = 0;
        this.coverageSummary = [];
        if (isKVP) {

        } else {
            for (i = 0; json.CoverageSummary && i < json.CoverageSummary.length; i++) {
                this.coverageSummary.push(new CoverageSummary(json.CoverageSummary[i]));
            }
            this.coverageSummary.sort(compareCoverageSummary);
            this.extension = json.Extension ? new Extension(json.Extension[0]) : null;
        }
    }

    function compareCoverageSummary(a, b) {
        return a.coverageId.localeCompare(b.coverageId);
    }

    Inherit(Contents, ContentsBaseType);

    return Contents;
});