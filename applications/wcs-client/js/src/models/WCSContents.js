/**
 * Created by Alexandru on 13.10.2014.
 */

define(["src/models/CoverageSummary"], function (CoverageSummary) {
    function WCSContents(json, isKVP) {
        var i;
        this.coverageSummary = [];
        this.supportedCRS = [];
        this.supportedFormat = [];
        this.otherSource = [];

        if (isKVP) {

        } else {
            for (i = 0; json.coverageSummary && i < json.converageSummary.length; i++) {
                this.coverageSummary.push(new CoverageSummary(json.coverageSummary[i], isKVP));
            }

            for (i = 0; json.supportedCRS && i < json.supportedCRS.length; i++) {
                this.supportedCRS.push(json.supportedCRS[i]._text);
            }

            for (i = 0; json.supportedFormat && i < json.supportedFormat.length; i++) {
                this.supportedFormat.push(json.supportedFormat[i]._text);
            }

            //TODO: Get OtherSource
        }
    }

    return WCSContents;
});