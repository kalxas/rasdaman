/**
 * Created by Alexandru on 14.10.2014.
 */

define(["src/models/OWSServiceIdentification", "src/models/OWSServiceProvider", "src/models/OWSOperationsMetadata" ], function (OWSServiceIdentification, OWSServiceProvider, OWSOperationsMetadata) {
    function CapabilitiesBaseType(/*JSObject*/json, /*bool*/isKVP) {
        if (isKVP) {

        } else {
            if (!json._attr || (json._attr && !json._attr.version)) {
                throw new Error("Invalid json:" + JSON.stringify(json));
            }
            this.version = json._attr.version._value;
            this.updateSequence = json._attr.updateSequence ? json._attr.updateSequence._value : null;

            this.serviceIdentification = json.ServiceIdentification ? new OWSServiceIdentification(json.ServiceIdentification[0]) : null;
            this.serviceProvider = json.ServiceProvider ? new OWSServiceProvider(json.ServiceProvider[0]) : null;
            this.operationsMetadata = json.OperationsMetadata ? new OWSOperationsMetadata(json.OperationsMetadata[0]) : null;
        }
    }

    return CapabilitiesBaseType;
});