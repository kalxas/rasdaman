/**
 * Created by Alexandru on 19.10.2014.
 */

define(["../src/models/GridEnvelope", "../src/models/DomainSet", "../src/models/Util", "../src/models/CoverageDescriptions"], function (GridEnvelope, DomainSet, Util, CoverageDescriptions) {
    describe("GridEnvelope", function () {
        it("GridEnvelope", function () {

            var data = '<GridEnvelope>' +
                '<low>1 2</low>' +
                '<high>3 4</high>' +
                '</GridEnvelope>';

            var parsedXML = xmlToJSON.parseString(data);
            var envelope = new GridEnvelope(parsedXML.GridEnvelope[0]);

            expect(envelope.low[0]).toBe(1);
            expect(envelope.low[1]).toBe(2);
            expect(envelope.high[0]).toBe(3);
            expect(envelope.high[1]).toBe(4);
        });
    });

    describe("DomainSet", function () {
        it("RectifiedGrid", function () {

            var document = Util.loadXMLDoc("base/test/mock/rectifiedGridDomainSet.xml");
            var parsedXML = xmlToJSON.parseXML(document);
            var domainSet = new DomainSet(parsedXML.domainSet[0]);
            expect(domainSet.limits.low[0]).toBe(0);
            expect(domainSet.limits.low[1]).toBe(0);
            expect(domainSet.limits.low[2]).toBe(0);

            expect(domainSet.limits.high[0]).toBe(95);
            expect(domainSet.limits.high[1]).toBe(3083);
            expect(domainSet.limits.high[2]).toBe(1265);

            expect(domainSet.id).toBe("OIL_SPILL_MONTEREY_BAY-grid");
            expect(domainSet.axisLabels[0]).toBe("t");
            expect(domainSet.axisLabels[1]).toBe("x");
            expect(domainSet.axisLabels[2]).toBe("y");
        });

        it("Grid", function () {

            var document = Util.loadXMLDoc("base/test/mock/gridDomainSet.xml");
            var parsedXML = xmlToJSON.parseXML(document);
            var domainSet = new DomainSet(parsedXML.domainSet[0]);
            expect(domainSet.limits.low[0]).toBe(0);
            expect(domainSet.limits.low[1]).toBe(0);
            expect(domainSet.limits.low[2]).toBe(0);
            expect(domainSet.limits.low[3]).toBe(0);
            expect(domainSet.limits.low[3]).toBe(0);

            expect(domainSet.limits.high[0]).toBe(2);
            expect(domainSet.limits.high[1]).toBe(4);
            expect(domainSet.limits.high[2]).toBe(19);
            expect(domainSet.limits.high[3]).toBe(151);
            expect(domainSet.limits.high[4]).toBe(113);

            expect(domainSet.id).toBe("Test_5D-grid");
            expect(domainSet.dimension).toBe(5);

            expect(domainSet.axisLabels[0]).toBe("modelTime");
            expect(domainSet.axisLabels[1]).toBe("t");
            expect(domainSet.axisLabels[2]).toBe("pressure");
            expect(domainSet.axisLabels[3]).toBe("x");
            expect(domainSet.axisLabels[4]).toBe("y");
        });
    });

    describe("CoverageDescriptions", function () {
        it("CoverageDescriptions", function () {

            var document = Util.loadXMLDoc("base/test/mock/coverageDescriptions.xml");
            var parsedXML = xmlToJSON.parseXML(document);
            var domainSet = new CoverageDescriptions(parsedXML.CoverageDescriptions[0]);
            expect(domainSet.coverageDescription.length).toBe(1);

        });
    });
});
