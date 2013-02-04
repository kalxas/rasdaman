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
 * Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */

/**
 * Testing Suite for WCS Queries
 * @see Rj.query.wcs
 * @author Alex Dumitru <m.dumitru@jacobs-university.de>
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @version 3.0.0
 */

buster.testCase("Rj.query.wcs Tests", {
  setUp:function () {
    this.timeout = 10000;
    Rj.util.ConfigManager.setWCSService({
      baseUrl:"http://flanche.net:8080/petascope/",
      name   :"WCS",
      version:"2.0.0"
    })
  },

  "WCS is accessible":function (done) {
    var query = new Rj.query.wcs.GetCoverage("mean_summer_airtemp");
    query.evaluate(function (response) {
      buster.assert(response !== "");
      done.call(null);
    });
  },

  "WCS request returns correct string":function (done) {
    var query = new Rj.query.wcs.GetCoverage("mean_summer_airtemp");
    query.addSubset(new Rj.query.wcs.Subset("x", null, 120, 120));
    query.addSubset(new Rj.query.wcs.Subset("y", null, -23, -23));
    query.evaluate(function (response) {
      buster.assert(response.search("113") !== -1);
      done.call(null);
    })
  },

  "WCS FML response can be parsed into 1D Array":function (done) {
    var query = new Rj.query.wcs.GetCoverage("mean_summer_airtemp");
    query.addSubset(new Rj.query.wcs.Subset("x", null, 120, 120));
    query.evaluate(function (response) {
      var gmlParser = new Rj.query.GMLParser(response, function (elem) {
        return parseInt(elem, 10);
      });
      var result = gmlParser.getTupleListAsNativeArray();
      buster.assert(result.length === 711);
      done();
    })
  },

  "WCS GML response can be parsed into  MultiDimArray":function (done) {
    var query = new Rj.query.wcs.GetCoverage("mean_summer_airtemp");
    query.addSubset(new Rj.query.wcs.Subset("x", null, 120, 120));
    query.evaluate(function (response) {
      var gmlParser = new Rj.query.GMLParser(response, function (elem) {
        return parseInt(elem, 10);
      });
      var result = gmlParser.getTupleListAsMultiDimArray();
      buster.assert(_.arrayEquals(result.getDimensions(), [
        {low:160, high:160},
        {low:0, high:710}
      ], function(elem1, elem2){
        return elem1.low == elem2.low && elem1.high == elem2.high;
      }));
      done();
    })
  }
})