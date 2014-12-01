/**
 * Created by Alexandru on 13.10.2014.
 */

define(["src/models/Inherit", "src/models/Description", ], function (Inherit, Description) {
    function OWSServiceIdentification(json, isKVP) {
        Description.call(this, json, isKVP);

        if (isKVP) {

        } else {
            if (!json.ServiceType || !json.ServiceTypeVersion) {
                throw new Error("Invalid json object" + JSON.stringify(json));
            }
            this.serviceType = json.ServiceType[0]._text;

            this.serviceTypeVersion = [];
            for (i = 0; i < json.ServiceTypeVersion.length; i++) {
                this.serviceTypeVersion.push(json.ServiceTypeVersion[i]._text);
            }

            this.fees = json.Fees ? json.Fees[0]._text : null;
            this.accessConstraints = json.AccessConstraints ? json.AccessConstraints[0]._text : null;

            this.profile = [];
            for (i = 0; json.Profile && i < json.Profile.length; i++) {
                this.profile.push(json.Profile[i]._text);
            }
        }
    }

    Inherit(OWSServiceIdentification, Description);

    return OWSServiceIdentification;

});