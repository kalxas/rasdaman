/**
 * Created by Alexandru on 14.10.2014.
 */
define(["../src/models/CapabilitiesBaseType", "../src/models/Util","../src/models/Capabilities"], function (CapabilitiesBaseType, Util, Capabilities) {
    describe("CapabilitiesBaseType", function () {
        it("CapabilitiesBaseType", function () {

            var document = Util.loadXMLDoc("base/test/mock/responseGetCapabilities.xml");
            var parsedXML = xmlToJSON.parseXML(document);
            var capabilitiesBaseType = new CapabilitiesBaseType(parsedXML.Capabilities[0]);

            expect(capabilitiesBaseType.version).toBe("2.0.1");
            expect(capabilitiesBaseType.updateSequence).toBe(null);
        });
    });

    describe("Capabilities", function () {
        it("Capabilities", function () {

            var document = Util.loadXMLDoc("base/test/mock/responseGetCapabilities.xml");
            var parsedXML = xmlToJSON.parseXML(document);
            var capabilitiesBaseType = new Capabilities(parsedXML.Capabilities[0]);

            expect(capabilitiesBaseType.version).toBe("2.0.1");
            expect(capabilitiesBaseType.updateSequence).toBe(null);
            expect(capabilitiesBaseType.contents.coverageSummary[0].coverageId).toBe("C0001");
            expect(capabilitiesBaseType.contents.coverageSummary[1].coverageId).toBe("C0002");
            expect(capabilitiesBaseType.contents.coverageSummary[2].coverageId).toBe("C0003");
        });
    });

});
