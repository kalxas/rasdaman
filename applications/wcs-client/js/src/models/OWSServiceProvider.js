/**
 * Created by Alexandru on 13.10.2014.
 */

define(["src/models/OnlineResource", "src/models/ResponsibleParty"], function (OnlineResource, ResponsibleParty) {
    function OWSServiceProvider(json, isKVP) {

        if (isKVP) {

        } else {
            if (!json.ProviderName) {
                throw new Error("Invalid json" + json);
            }

            this.providerName = json.ProviderName ? json.ProviderName[0]._text : null;
            this.providerSite = json.ProviderSite ? new OnlineResource(json.ProviderSite[0], isKVP) : null;
            this.serviceContact = json.ServiceContact ? new ResponsibleParty(json.ServiceContact[0], isKVP) : null;
        }
    }

    return OWSServiceProvider;
});