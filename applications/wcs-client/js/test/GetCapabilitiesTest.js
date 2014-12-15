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
