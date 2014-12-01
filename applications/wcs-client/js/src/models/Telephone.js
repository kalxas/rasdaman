/**
 * Created by Alexandru on 14.10.2014.
 */

define(function () {
    function Telephone(json, isKVP) {
        var i;
        if (isKVP) {

        } else {
            this.voice = [];
            this.facsimile = [];

            for (i = 0; json.Voice && i < json.Voice.length; i++) {
                this.voice.push(json.Voice[i]._text);
            }

            for (i = 0; json.Facsimile && i < json.Facsimile.length; i++) {
                this.facsimile.push(json.Facsimile[i]._text);
            }
        }
    }

    return Telephone;
});