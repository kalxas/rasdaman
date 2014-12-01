/**
 * Created by Alexandru on 14.10.2014.
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
