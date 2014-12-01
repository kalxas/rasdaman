/**
 * Created by Alexandru on 17.10.2014.
 */

define(["src/models/DatasetDescriptionSummary", "src/models/OtherSource"], function (DatasetDescriptionSummary, OtherSource) {
    function ContentsBaseType(json, isKVP) {
        var i = 0;
        this.datasetDescriptionSummary = [];
        this.otherSource = [];
        if (isKVP) {

        } else {
            for (i = 0; json.DatasetDescriptionSummary && i < json.DatasetDescriptionSummary.length; i++) {
                this.datasetDescriptionSummary.push(new DatasetDescriptionSummary(json.DatasetDescriptionSummary[i], isKVP));
            }

            for (i = 0; json.OtherSource && i < json.OtherSource.length; i++) {
                this.otherSource.push(new OtherSource(json.OtherSource[i]));
            }
        }
    }

    return ContentsBaseType;
});