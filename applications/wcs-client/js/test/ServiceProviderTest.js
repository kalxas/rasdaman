/*
 * This file is part of rasdaman community.
 *
 * Rasdaman community is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Rasdaman community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009,2010,2011,2012,2013,2014 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
define(["../src/models/Address", "../src/models/Telephone" , "../src/models/Contact", "../src/models/ResponsibleParty", "../src/models/OWSServiceProvider"], function (Address, Telephone, Contact, ResponsibleParty, OWSServiceProvider) {

   describe("Address", function () {
        it("Address", function () {
            var data = '<Address xmlns:ows="http://www.opengis.net/ows/2.0">' +
                '<ows:DeliveryPoint>Jacobs</ows:DeliveryPoint>' +
                ' <ows:City>Bremen</ows:City>' +
                '  <ows:AdministrativeArea>Bremen</ows:AdministrativeArea>' +
                ' <ows:PostalCode>28759</ows:PostalCode>' +
                ' <ows:Country>Germany</ows:Country>' +
                ' <ows:ElectronicMailAddress>a.t@j.de</ows:ElectronicMailAddress>' +
                '  </Address>';

            var address = new Address(xmlToJSON.parseString(data).Address[0]);
            expect(address.deliveryPoint[0]).toBe("Jacobs");
            expect(address.city).toBe("Bremen");
            expect(address.administrativeArea).toBe("Bremen");
            expect(address.postalCode[0] == "28759").toBeTruthy();
            expect(address.country[0]).toBe("Germany");
            expect(address.electronicMailAddress[0]).toBe("a.t@j.de");
        });
    });

    describe("Telephone", function () {
        it("Telephone", function () {
            var data = '<Phone  xmlns:ows="http://www.opengis.net/ows/2.0">' +
                '<ows:Voice>112233</ows:Voice>' +
                '<ows:Facsimile>bla</ows:Facsimile>' +
                '</Phone>';

            var phone = new Telephone(xmlToJSON.parseString(data).Phone[0]);
            expect(phone.voice[0]).toBe("112233");
            expect(phone.facsimile[0]).toBe("bla");
        });
    });

    describe("Contact", function () {
        it("Contact", function () {
            var data = ' <ContactInfo xmlns:ows="http://www.opengis.net/ows/2.0">' +
                '    <ows:Phone>' +
                '       <ows:Voice>111</ows:Voice>' +
                '      <ows:Facsimile>lala</ows:Facsimile>' +
                ' </ows:Phone>' +
                '<ows:Address>' +
                '   <ows:DeliveryPoint>jacobs</ows:DeliveryPoint>' +
                '  <ows:City>bremen</ows:City>' +
                ' <ows:AdministrativeArea>bremen</ows:AdministrativeArea>' +
                '<ows:PostalCode>28759</ows:PostalCode>' +
                ' <ows:Country>germany</ows:Country>' +
                ' <ows:ElectronicMailAddress>string</ows:ElectronicMailAddress>' +
                '</ows:Address>' +
                '<ows:OnlineResource/>' +
                '<ows:HoursOfService>12-14</ows:HoursOfService>' +
                '<ows:ContactInstructions>call</ows:ContactInstructions>' +
                ' </ContactInfo>';

            var contact = new Contact(xmlToJSON.parseString(data).ContactInfo[0]);

            expect(contact.phone.voice[0]).toBe("111");
            expect(contact.phone.facsimile[0]).toBe("lala");
            expect(contact.address.deliveryPoint[0]).toBe("jacobs");
            expect(contact.address.city).toBe("bremen");
            expect(contact.address.administrativeArea).toBe("bremen");
            expect(contact.address.postalCode[0]).toBe("28759");
            expect(contact.address.country[0]).toBe("germany");
            expect(contact.address.electronicMailAddress[0]).toBe("string");
            expect(contact.hoursOfService).toBe("12-14");
            expect(contact.contactInstructions).toBe("call");
            expect(contact.onlineResource).toBeTruthy();
        });
    });


    describe("ResponsibleParty", function () {
        it("ResponsibleParty", function () {
            var data = '<ServiceContact xmlns:ows="http://www.opengis.net/ows/2.0">' +
                '<ows:IndividualName>Alex</ows:IndividualName>' +
                '<ows:PositionName>Boss</ows:PositionName>' +
                '<ows:ContactInfo>' +
                '<ows:Phone>' +
                '<ows:Voice>string</ows:Voice>' +
                '<ows:Facsimile>string</ows:Facsimile>' +
                '</ows:Phone>' +
                '<ows:Address>' +
                '<ows:DeliveryPoint>string</ows:DeliveryPoint>' +
                '<ows:City>string</ows:City>' +
                '<ows:AdministrativeArea>string</ows:AdministrativeArea>' +
                '<ows:PostalCode>string</ows:PostalCode>' +
                '<ows:Country>string</ows:Country>' +
                '<ows:ElectronicMailAddress>string</ows:ElectronicMailAddress>' +
                '</ows:Address>' +
                '<ows:OnlineResource/>' +
                '<ows:HoursOfService>string</ows:HoursOfService>' +
                '<ows:ContactInstructions>string</ows:ContactInstructions>' +
                '</ows:ContactInfo>' +
                '<ows:Role>test</ows:Role>' +
                '</ServiceContact>';

            var responsibleParty = new ResponsibleParty(xmlToJSON.parseString(data).ServiceContact[0]);
            expect(responsibleParty.individualName).toBe("Alex");
            expect(responsibleParty.positionName).toBe("Boss");
            expect(responsibleParty.role).toBe("test");
            expect(responsibleParty.contactInfo).toBeTruthy();
        });
    });

    describe("ServiceProvider", function () {
        it("ServiceProvider", function () {
            var data = '<ServiceProvider>' +
                '  <ows:ProviderName>ME</ows:ProviderName>' +
                ' <ows:ProviderSite/>' +
                ' <ows:ServiceContact>' +
                ' </ows:ServiceContact>' +
                '</ServiceProvider>';

            var serviceProvider = new OWSServiceProvider(xmlToJSON.parseString(data).ServiceProvider[0]);
            expect(serviceProvider.providerName).toBe("ME");
            expect(serviceProvider.serviceContact).toBeTruthy(null);
            expect(serviceProvider.providerSite).toBeTruthy(null);
        });
    });
});
