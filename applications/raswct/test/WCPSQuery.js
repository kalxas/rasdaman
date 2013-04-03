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
 * Testing Suite for WCPS Queries
 * @see Rj.query.WCPSQuery
 * @author Alex Dumitru <m.dumitru@jacobs-university.de>
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @version 3.0.0
 */

buster.testCase("Rj.query.WCPSQuery tests", {
  setUp: function () {
    this.timeout = 20000;
    Rj.util.ConfigManager.setWCPSService({
      url           : "http://flanche.net:8080/petascope/",
      queryParameter: "request"
    })
  },

  "WCPS service is available": function (done) {
    var query = new Rj.query.WCPSQuery('for t2 in (mean_summer_airtemp) return encode (t2, "csv")');
    query.evaluate(function (response) {
      buster.assert(response !== null);
      var parser = new Rj.util.CSVParser(response, function (e) {
        return parseInt(e, 10);
      });
      done();
    })
  },

  "WCPS can register multiple queries": function (done) {
    var query = new Rj.query.WCPSQuery('for t2 in (mean_summer_airtemp) return encode (t2, "csv")');
    query.setCached(true);
    query.evaluate(function (response) {
      console.log("1");
    }, true);
    query.evaluate(function (response) {
      console.log("2");
    }, true);
    query.evaluate();
    buster.assert(true);
    done();
  },

  "WCPS query works with specified service(not global)": function (done) {
    "use strict";
    var query = new Rj.query.WCPSQuery('for t2 in (mean_summer_airtemp) return encode (t2, "csv")');
    query.setWCPSService({
      url           : "http://flanche.net:8080/restPatch",
      queryParameter: "request"
    });
    query.evaluate(function (response) {
      buster.assert(_.exists(response));
      done();
    });
  }
})