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
 * A GMLParser object is designed to help with the parsing of GML to
 * native javascript objects
 * @module {Rj.query}
 * @author Alex Dumitru <m.dumitru@jacobs-university.de>
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @version 3.0.0
 */

FlancheJs.defineClass("Rj.query.GMLParser", {

  /**
   * Constructor for the function
   * @param {Rj.util.XMLDoc} doc the gml document
   * @param {Function} elemParser a function to parse individual elements from the tuple list
   * if none are provided.
   * @example var parser = new Rj.query.GMLParser(doc, function(element){
   *   components = element.split(" ");
   *   var rgb = {
   *    red : components[0],
   *    green: components[1],
   *    blue: components[2]
   *   }
   *   return rgb;
   * });
   */
  init:function (doc, elemParser) {
    if (doc instanceof Rj.util.XMLDoc) {
      this._doc = doc;
    }
    else {
      this._doc = new Rj.util.XMLDoc(doc);
    }
    if (_.exists(elemParser)) {
      this._elemParser = elemParser;
    }
    else {
      this._elemParser = _.identity;
    }
  },

  methods:{
    /**
     * This method checks if any Exceptions were returned and if so reports them
     * as errors through the default mechanism
     */
    rethrowErrors:function () {
      var doc = this._doc;
      if (doc.contains("ows:ExceptionReport")) {
        var error = "The server returned an error: " + doc.filter("//ows:ExceptionText")[0].getTextContents();
        Rj.util.ErrorManager.reportError(error, true);
      }
    },

    /**
     * Returns the number of dimensions
     * @return {Number}
     */
    getNoOfDimensions:function () {
      var noOfDims = this._doc.filter("//gml:RectifiedGrid/@dimension")[0].value;
      return noOfDims;
    },

    /**
     * Returns the dimensions of the GML coverage as objects of form:
     * {
     *  low : 0,
     *  high: 1,
     * }
     * @return {Array}
     */
    getDimensions:function () {
      var low = this._doc.filter("//gml:RectifiedGrid/gml:limits/gml:GridEnvelope/gml:low")[0].getTextContents().split(" ");
      var high = this._doc.filter("//gml:RectifiedGrid/gml:limits/gml:GridEnvelope/gml:high")[0].getTextContents().split(" ");
      var dims = [];
      for (var i = 0; i < low.length; i++) {
        dims.push({low:parseInt(low[i], 10), high:parseInt(high[i], 10)});
      }
      return dims;
    },

    /**
     * Returns the tuple list element from the GML as a string
     * @return {String}
     */
    getTupleListAsString:function () {
      var ts = this._doc.filter("//gml:tupleList")[0].getTextContents();
      return ts;
    },

    /**
     * Returns the tuple list as a one dimensional native js array
     * with the elements processed
     * @return {Array}
     */
    getTupleListAsNativeArray:function () {
      var ts = this.getTupleListAsString();
      unprocessedTs = ts.split(",");
      if (!_.exists(this._elemParser)) {
        return unprocessedTs;
      }
      var proccessedTs = [];
      for (var i = 0; i < unprocessedTs.length; i++) {
        proccessedTs.push(this._elemParser.call(null, unprocessedTs[i], i));
      }
      return proccessedTs;
    },

    /**
     * Returns the tuple list as a multi dimensional array
     * with the elements processed
     * @return {Rj.util.MultiDimArray}
     */
    getTupleListAsMultiDimArray:function () {
      var ts = this.getTupleListAsNativeArray();
      var dims = this.getDimensions();
      var multiDimArr = new Rj.util.MultiDimArray(dims, ts);
      return multiDimArr;
    }
  },

  internals:{
    doc       :null,
    elemParser:null
  }

})