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
 * A CSVParser object is designed to help with the parsing of CSV data
 * produced by rasdaman server or petascope wcps services to
 * native javascript objects
 * @module {Rj.query}
 * @author Alex Dumitru <m.dumitru@jacobs-university.de>
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @version 3.0.0
 */

FlancheJs.defineClass("Rj.util.CSVParser", {
  init:function (csvString, elementParser) {
    this._csvString = csvString;
    this._elementParser = _.exists(elementParser) ? elementParser : _.identity;
    this.rethrowErrors();
  },

  methods:{
    /**
     * This method checks if any Exceptions were returned and if so reports them
     * as errors through the default mechanism
     */
    rethrowErrors:function () {
      var doc = this._csvString;
      if (doc.search("ows:ExceptionReport") != -1) {
        var xml = new Rj.util.XMLDoc(doc);
        var error = "The server returned an error: " + xml.filter("//ows:ExceptionText")[0].getTextContents();
        Rj.util.ErrorManager.reportError(error, true);
      }
    },
    toOriginalCSVString:function () {
      return this.csvString;
    },

    toNativeJsArray:function () {
      // Support result in both CSV and JSON (only 1D now for diagram widget)
      // e.g: diagram>>SELECT encode(c[1888,369,1:12] + 273.15, "json") FROM AvgLandTemp AS c   
      csvArray = this._csvString.replace("{", "")
        .replace("}", "")
        .replace("[", "")
        .replace("]", "")
        .replace('"', "")
        .split(",")
        .map(this._elementParser);
      return csvArray;
    }
  },

  internals:{
    csvString    :null,
    elementParser:null
  }
})
