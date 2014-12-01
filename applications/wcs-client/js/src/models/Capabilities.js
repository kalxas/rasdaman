/**
 * Created by Alexandru on 14.10.2014.
 */

define([ "src/models/Inherit", "src/models/WCSServiceMetadata", "src/models/CapabilitiesBaseType", "src/models/Contents"], function (Inherit, WCSServiceMetadata, CapabilitiesBaseType, Contents) {

    function Capabilities(json, isKVP) {
        CapabilitiesBaseType.call(this, json, isKVP);

        if (isKVP) {

        } else {
            if (!json.ServiceMetadata) {
                throw new Error("Invalid json" + JSON.stringify(json));
            }

            this.serviceMetadata = new WCSServiceMetadata(json.ServiceMetadata[0]);
            this.contents = json.Contents ? new Contents(json.Contents[0], isKVP) : null;
        }
    }

    Inherit(Capabilities, CapabilitiesBaseType);

    return Capabilities;
});