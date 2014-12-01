/**
 * Created by Alexandru on 19.10.2014.
 */
define(["src/models/GridEnvelope"], function (GridEnvelope) {

    function DomainSet(json, isKVP) {
        if (isKVP) {

        } else {

            if (json.MultiPoint) {
                json = json.MultiPoint[0];

            } else if (json.Grid) {
                json = json.Grid[0];
            } else if (json.RectifiedGrid) {
                json = json.RectifiedGrid[0];
            } else if( json.ReferenceableGridByVectors){
                json = json.ReferenceableGridByVectors[0];
            }

            this.limits = json.limits && json.limits[0].GridEnvelope ? new GridEnvelope(json.limits[0].GridEnvelope[0]) : null;
            this.axisLabels = json.axisLabels[0]._text.split(" ");
            this.dimension = parseInt(json._attr.dimension._value);
            this.id = json._attr.id._value;
        }
    }

    return DomainSet;
});