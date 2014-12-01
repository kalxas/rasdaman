/**
 * Created by Alexandru on 15.10.2014.
 */

define(["src/models/CoverageSubtypeParent", "src/models/Extension", "src/models/Description", "src/models/Inherit", "src/models/Metadata"], function (CoverageSubtypeParent, Extension, Description, Inherit, Metadata) {

    function CoverageSummary(json, isKVP) {
        var i = 0;
        var box;

        Description.call(this, json, isKVP);

        if (isKVP) {

        } else {
            if (!json.CoverageId || !json.CoverageSubtype) {
                throw new Error("Invalid json" + JSON.stringify(json));
            }

            this.coverageId = json.CoverageId[0]._text;
            this.coverageSubtype = json.CoverageSubtype[0]._text;
            this.coverageSubtypeParent = json.CoverageSubtypeParent ? new CoverageSubtypeParent(json.CoverageSubtypeParent[0]) : null;

            //TODO:The extension does not appear in the schema
            this.extension = json.Extension ? new Extension(json.Extension[0]) : null;

            /* for (j = 0; json.wgs84BoundingBox && j < json.wgs84BoundingBox.length; j++) {
             box = json.wgs84BoundingBox[j];
             //TODO Something
             }

             for (j = 0; json.boundingBox && j < json.boundingBox.length; j++) {
             box = json.boundingBox[j];
             //TODO Something
             }
             */

            this.metadata = [];

            for (j = 0; json.Metadata && j < json.Metadata.length; j++) {
                this.metadata.push(new Metadata(json.Metadata[j]));
            }
        }

    }

    Inherit(CoverageSummary, Description);

    return CoverageSummary;
});