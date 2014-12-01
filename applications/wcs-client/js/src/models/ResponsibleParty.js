/**
 * Created by Alexandru on 13.10.2014.
 */
define(["src/models/Contact"], function (Contact) {

    function ResponsibleParty(json, isKVP) {
        if (isKVP) {

        } else {
            this.individualName = json.IndividualName ? json.IndividualName[0]._text : null;
            this.positionName = json.PositionName ? json.PositionName[0]._text : null;
            this.role = json.Role ? json.Role[0]._text : null;
            this.contactInfo = json.ContactInfo ? new Contact(json.ContactInfo[0]) : null;
        }
    }

    return ResponsibleParty;
});