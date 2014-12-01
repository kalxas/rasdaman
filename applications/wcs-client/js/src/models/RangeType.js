/**
 * Created by Alexandru on 19.10.2014.
 */
define(["src/models/DataRecord"], function (DataRecord) {
    function RangeType(json, isKVP) {
        if (isKVP) {

        } else {
            this.dataRecord = json.DataRecord ? new DataRecord(json.DataRecord[0]) : null;
        }
    }

    return RangeType;
});