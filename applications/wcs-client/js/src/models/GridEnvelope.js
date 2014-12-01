/**
 * Created by Alexandru on 19.10.2014.
 */

define(function () {
    function GridEnvelope(json, isKVP) {
        var i;
        var auxVect;
        if (isKVP) {

        } else {
            if (!json.low || !json.high) {
                throw new Error("Invalid json" + JSON.stringify(json));
            }
            this.low = [];
            this.high = [];

            auxVect = json.low[0]._text.split(" ");
            for (i = 0; i < auxVect.length; i++) {
                this.low.push(parseInt(auxVect[i]));
            }

            auxVect = json.high[0]._text.split(" ");
            for (i = 0; i < auxVect.length; i++) {
                this.high.push(parseInt(auxVect[i]));
            }
        }
    }

    return GridEnvelope;
});