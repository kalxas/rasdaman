/**
 * Created by Alexandru on 14.10.2014.
 */

define(function () {
    return function (subType, SuperType) {
        var prototype = Object.create(SuperType.prototype);
        prototype.constructor = subType;
        subType.prototype = prototype;
    };
});