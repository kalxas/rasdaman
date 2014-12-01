/**
 * Created by Alexandru on 13.10.2014.
 */
define(function () {
    function Address(json, isKVP) {
        var i;
        if (isKVP) {

        } else {
            this.deliveryPoint = [];
            this.postalCode = [];
            this.country = [];
            this.electronicMailAddress = [];

            for (i = 0; json.DeliveryPoint && i < json.DeliveryPoint.length; i++) {
                this.deliveryPoint.push(json.DeliveryPoint[i]._text);
            }

            for (i = 0; json.PostalCode && i < json.PostalCode.length; i++) {
                this.postalCode.push(json.PostalCode[i]._text);
            }

            for (i = 0; json.Country && i < json.Country.length; i++) {
                this.country.push(json.Country[i]._text);
            }

            for (i = 0; json.ElectronicMailAddress && i < json.ElectronicMailAddress.length; i++) {
                this.electronicMailAddress.push(json.ElectronicMailAddress[i]._text);
            }

            this.city = json.City ? json.City[0]._text : null;
            this.administrativeArea = json.AdministrativeArea ? json.AdministrativeArea[0]._text : null;
        }
    }

    return Address;

});