/**
 * Created by Alexandru on 19.10.2014.
 */
define(["src/models/GMLAbstractFeature", "src/models/Inherit", "src/models/ServiceParameters", "src/models/Extension", "src/models/CoverageFunction", "src/models/GMLMetadata", "src/models/DomainSet", "src/models/RangeType"], function (GMLAbstractFeature, Inherit, ServiceParameters, Extension, CoverageFunction, GMLMetadata, DomainSet, RangeType) {
    function CoverageDescription(json, isKVP) {
        GMLAbstractFeature.call(this, json, isKVP);
        if (isKVP) {

        } else {
            if (!json.CoverageId || !json.domainSet || !json.rangeType || !json.ServiceParameters) {
                throw new Error("Invalid json:" + JSON.stringify(json));
            }
            this.coverageId = json.CoverageId[0]._text;
            this.coverageFunction = json.coverageFunction ? new CoverageFunction(json.coverageFunction[0]) : null;

            this.metadata = [];
            for (var i = 0; json.metadata && i < json.metadata.length; i++) {
                this.metadata.push(new GMLMetadata(json.metadata[i], isKVP));
            }

            this.domainSet = new DomainSet(json.domainSet[0]);
            this.rangeType = new RangeType(json.rangeType[0]);
            this.serviceParameters = new ServiceParameters(json.ServiceParameters[0]);
        }
    }

    Inherit(CoverageDescription, GMLAbstractFeature);

    return CoverageDescription;
});