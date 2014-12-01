/**
 * Created by Alexandru on 13.10.2014.
 */

define(["src/models/Address", "src/models/Telephone", "src/models/OnlineResource"], function (Address, Telephone, OnlineResource) {
    function Contact(json, isKVP) {
        if (isKVP) {

        } else {
            this.hoursOfService = json.HoursOfService ? json.HoursOfService[0]._text : null;
            this.contactInstructions = json.ContactInstructions ? json.ContactInstructions[0]._text : null;
            this.phone = json.Phone ? new Telephone(json.Phone[0], isKVP) : null;
            this.address = json.Address ? new Address(json.Address[0], isKVP) : null;
            this.onlineResource = json.OnlineResource ? new OnlineResource(json.OnlineResource[0]) : null;
        }
    }

    return Contact;
});