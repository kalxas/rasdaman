/**
 * Created by Alexandru on 14.10.2014.
 */

define(function () {
    function LanguageString(json, isKVP) {
        this.value = null;
        this.lang = null;
        if (isKVP) {

        } else {
            this.value = json._text ? json._text : null;
            this.lang = json._attr && json._attr.lang ? json._attr.lang._value : null;
        }
    }

    return LanguageString;
});