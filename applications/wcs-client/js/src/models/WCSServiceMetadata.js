/**
 * Created by Alexandru on 13.10.2014.
 */
define(["src/models/Extension"], function (Extension) {
    function WCSServiceMetadata(json, isKVP) {
        if (isKVP) {

        } else {
            if (!json.formatSupported) {
                throw new Error("Invalid json object" + JSON.stringify(json));
            }

            this.formatSupported = [];
            for (var i = 0; i < json.formatSupported.length; i++) {
                this.formatSupported.push(json.formatSupported[i]._text);
            }

            this.extension = json.Extension ? new Extension(json.Extension[0]) : null;
        }
    }

    return WCSServiceMetadata;

});