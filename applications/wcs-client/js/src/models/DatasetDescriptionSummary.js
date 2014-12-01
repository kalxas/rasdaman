/**
 * Created by Alexandru on 17.10.2014.
 */
define(["src/models/Description", "src/models/Inherit"], function (Description, Inherit) {

    function DatasetDescriptionSummary(json, isKVP) {
        Description.call(this, json, isKVP);

        if (isKVP) {

        } else {
            //TODO
        }
    }

    Inherit(DatasetDescriptionSummary, Description);

    return DatasetDescriptionSummary;
});