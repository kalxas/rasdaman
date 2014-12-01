/**
 * Created by Alexandru on 19.10.2014.
 */

define(function () {
    function DataRecord(json, isKVP) {
        if (isKVP) {

        } else {
            this.field = [];
            this.quantity = [];
            this.code = [];
            for (var i = 0; json.field && i < json.field.length; i++) {
                this.field.push(json.field[i]._attr.name._value);
                if (json.field[i].Quantity
                    && json.field[i].Quantity[0]
                    && json.field[i].Quantity[0]._attr
                    && json.field[i].Quantity[0]._attr.definition) {

                    this.quantity.push(json.field[i].Quantity[0]._attr.definition._value);

                } else if (json.field[i].Quantity
                    && json.field[i].Quantity[0]
                    && json.field[i].Quantity[0].uom
                    && json.field[i].Quantity[0].uom[0]._attr
                    && json.field[i].Quantity[0].uom[0]._attr.code) {

                    this.code.push(json.field[i].Quantity[0].uom[0]._attr.code._value);
                }
            }
        }
    }

    return DataRecord;
});