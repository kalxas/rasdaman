/**
 * Created by Alexandru on 14.10.2014.
 */

define(function () {
    function Keywords(json, isKVP) {

        this.keyword = [];
        this.type = null;
        if (isKVP) {

        } else {
            if (!json.Keyword) {
                throw new Error("Invalid object" + JSON.stringify(json));
            }
            for (i = 0; i < json.Keyword.length; i++) {
                this.keyword.push(json.Keyword[i]._text);
            }

            this.type = json.Type ? json.Type[0]._text : null;
        }
    }

    return Keywords;
});