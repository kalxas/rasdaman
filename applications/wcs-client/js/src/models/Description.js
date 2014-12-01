/**
 * Created by Alexandru on 14.10.2014.
 */

define(["src/models/Keywords", "src/models/LanguageString"], function (Keywords, LanguageString) {
    function Description(json, isKVP) {
        if (isKVP) {

        } else {
            this.title = [];
            this.abstract = [];

            for (var i = 0; json.Title && i < json.Title.length; i++) {
                this.title.push(new LanguageString(json.Title[i], isKVP));
            }

            for (var j = 0; json.Abstract && j < json.Abstract.length; j++) {
                this.abstract.push(new LanguageString(json.Abstract[j], isKVP));
            }

            this.keywords = json.Keywords ? new Keywords(json.Keywords[0]) : null;
        }
    }

    return Description;
});