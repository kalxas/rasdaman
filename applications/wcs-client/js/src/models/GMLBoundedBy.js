/**
 * Created by Alexandru on 19.10.2014.
 */
define(["src/models/GMLEnvelope"], function (GMLEnvelope) {
    function GMLBoundedBy(json, isKVP) {
        if (isKVP) {

        } else {
            this.nilReason = json._attr && json._attr.nilReason ? json._attr.nilReason._value : null;

            if (json.Envelope) {
                this.envelope = new GMLEnvelope(json.Envelope[0]);
            } else if (json.EnvelopeWithTimePeriod) {

            } else if (json.Null) {

            }
        }
    }

    return GMLBoundedBy;
});