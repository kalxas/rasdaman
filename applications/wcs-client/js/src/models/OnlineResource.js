/**
 * Created by Alexandru on 13.10.2014.
 */

define(function () {
    function OnlineResource(json, isKVP) {
        //TODO:Implement this
        if (isKVP) {

        } else {
            this.value = json._attr && json._attr.href ? json._attr.href._value : null;
        }
    }

    return OnlineResource;
});