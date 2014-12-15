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
define(["../src/models/Keywords", "../src/models/LanguageString", "../src/models/Description", "../src/models/OWSServiceIdentification", "../src/models/Address", "../src/models/Telephone"], function (Keywords, LanguageString, Description, OWSServiceIdentification, Address, Telephone) {
    describe("Keywords", function () {
        it("Keywords", function () {
            var data = '<Keywords>' +
                '<Keyword>string</Keyword>' +
                '<Type>string</Type>' +
                '</Keywords>';

            var keywords = new Keywords(xmlToJSON.parseString(data).Keywords[0]);
            expect(keywords.keyword.length).toBe(1);
            expect(keywords.keyword[0]).toBe("string");
            expect(keywords.type).toBe("string");
        });
    });

    describe("LanguageString", function () {
        it("LanguageString", function () {
            var data = '<Title lang="en">Acme Corp. Map Server</Title>';

            var languageString = new LanguageString(xmlToJSON.parseString(data).Title[0]);
            expect(languageString.lang).toBe("en");
            expect(languageString.value).toBe("Acme Corp. Map Server");
        });
    });

    describe("Description", function () {
        it("Description", function () {
            var data = '<Description><Title lang="en">Acme Corp. Map Server</Title>' +
                '<Title lang="fr">Serveur de Carte par Acme Corp.</Title>' +
                '<Abstract>Abstract</Abstract>' +
                '<Keywords>' +
                '<Keyword>bird</Keyword>' +
                '<Keyword>roadrunner</Keyword>' +
                '<Keyword>ambush</Keyword>' +
                '</Keywords></Description>';
            var doc = xmlToJSON.parseString(data);
            var description = new Description(doc.Description[0]);

            expect(description.abstract[0].value).toBe("Abstract");
            expect(description.abstract[0].lang).toBe(null);
            expect(description.title[0].value).toBe("Acme Corp. Map Server");
            expect(description.title[0].lang).toBe("en");
            expect(description.title[1].value).toBe("Serveur de Carte par Acme Corp.");
            expect(description.title[1].lang).toBe("fr");
            expect(description.keywords.keyword[0]).toBe("bird");
            expect(description.keywords.keyword[1]).toBe("roadrunner");
            expect(description.keywords.keyword[2]).toBe("ambush");
        });
    });

    describe("OWSServiceIdentification", function () {
        it("OWSServiceIdentification", function () {
            var data =
                '<ServiceIdentification  xmlns:ows="http://www.opengis.net/ows/2.0">' +
                ' <ows:Title>string</ows:Title>' +
                '<ows:Abstract>string</ows:Abstract>' +
                ' <ows:Keywords>' +
                '     <ows:Keyword>string</ows:Keyword>' +
                '     <ows:Type>string</ows:Type>' +
                ' </ows:Keywords>' +
                ' <ows:ServiceType>serviceType</ows:ServiceType>' +
                ' <ows:ServiceTypeVersion>serviceTypeVersion</ows:ServiceTypeVersion>' +
                ' <ows:Fees>fee</ows:Fees>' +
                ' <ows:AccessConstraints>constraint</ows:AccessConstraints>' +
                '</ServiceIdentification>';

            var doc = xmlToJSON.parseString(data);
            var servideIdentification = new OWSServiceIdentification(doc.ServiceIdentification[0]);

            expect(servideIdentification.fees).toBe("fee");
            expect(servideIdentification.accessConstraints).toBe("constraint");
            expect(servideIdentification.serviceType).toBe("serviceType");
            expect(servideIdentification.serviceTypeVersion[0]).toBe("serviceTypeVersion");

        });
    });
});
