/**
 * Created by Alexandru on 19.10.2014.
 */
define(function () {
    function GMLEnvelope(json, isKVP) {
        if (isKVP) {

        } else {
            if (json._attr) {
                this.srsName = json._attr.srsName ? json._attr.srsName._value : null;
                this.srsDimension = json._attr.srsDimension ? parseInt(json._attr.srsDimension._value) : null;

                this.axisLabels = [];
                if (json._attr.axisLabels && json._attr.axisLabels._value) {
                    this.axisLabels = json._attr.axisLabels._value.match(/\S+/g);
                }

                this.uomLabels = [];
                if (json._attr.uomLabels && json._attr.uomLabels._value) {
                    this.uomLabels = json._attr.uomLabels._value.match(/\S+/g);
                }

                this.lowerCorner = [];
                this.upperCorner = [];

                if (json.lowerCorner && json.upperCorner) {
                    this.lowerCorner = json.lowerCorner[0]._text.match(/\S+/g);
                    this.upperCorner = json.upperCorner[0]._text.match(/\S+/g);
                    for(var i=0; i<this.lowerCorner.length; i++){
                        this.lowerCorner[i]=parseFloat(this.lowerCorner[i]);
                        this.upperCorner[i]=parseFloat(this.upperCorner[i]);
                    }
                }
            }
        }
    }

    return GMLEnvelope;
});