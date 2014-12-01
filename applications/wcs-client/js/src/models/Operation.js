/**
 * Created by Alexandru on 13.10.2014.
 */

define(function () {
    function Operation(name, parameter, constraint, DCP, metadata) {
        this.name = name;
        this.parameter = parameter;
        this.constraint = constraint;
        this.DCP = DCP;
        this.metadata = metadata;
    }

    Operation.prototype.toXML = function () {

    };

    Operation.prototype.toKVP = function () {

    };

    return Operation;
});