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
 * @class MultiDimArray
 * @description This class is a representation of a multidimensional array that has easy to use accessor methods.
 * @module {Rj.query}
 * @author Alex Dumitru <m.dumitru@jacobs-university.de>
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @version 3.0.0
 * @example var m = new MultiDimArray([{low: 0, high:1}, {low:0, high:1}], [0,1,2,3]);
 * console.log(m.get(0,1)); //prints 1
 * console.log(m.get(0)); //prints new MultiDimArray([{low:0, high:1}], [0,1]);
 * m.set(0,1,42);//array is now [0,42,2,3]
 */

FlancheJs.defineClass("Rj.util.MultiDimArray", {

  /**
   * Constructor for the classe
   * @param {Array} dimensions - the dimension specifications of the array in the following format:
   * [{low: Number, high: Number}, {low: Number, high:Number}, ...]
   * @param {Array} values - the values as a 1d array
   */
  init:function (dimensions, values) {
    this._noOfDims = dimensions.length;
    this._dimensions = dimensions;
    this._values = values;
  },

  methods:{
    /**
     * Accessor for the elements of the array
     * @param {Number} the index for the first dimension
     * @param {Number} the index for the second dimension
     * @param {Number} the index for the n-th dimension
     * @return {Number | Rj.util.MultiDimArray}
     */
    get:function () {
      var result = undefined;
      var args = Array.prototype.slice.call(arguments);
      if (args.length === this._noOfDims) {      //point request
        result = this._getExactPoint(args);
      }
      else if (args.length > 0) {
        var dimSpaces = this._getDimensionality();
        var dimProduct = 1;
        var addedSum = 0;
        var retDims = [];
        for (var i = args.length; i < this._noOfDims; i++) {
          retDims.push(this._dimensions[i]);
        }
        var intermResults = this._getMultiDim(args, []);
        result = new Rj.util.MultiDimArray(retDims, intermResults);
      }
      else {
        return this;
      }
      return result;
    },

    /**
     * Returns a 1D array representation of the MultiDimArray
     * @return {Array}
     */
    getAsNativeArray:function () {
      return this._values;
    },

    /**
     * Returns the dimensions of the MultiDimArray
     * @return {Array}
     */
    getDimensions:function () {
      return this._dimensions;
    },

    /**
     * Determines if the MultiDimArray is equal to another. Equality is defined by equality between their 1D representations
     * if the dimensions are identical.
     * @param {Rj.util.MultiDimArray} the MultiDimArray to compare with.
     * @return {Boolean} true if equal, false otherwise
     */
    equals:function (other) {
      var dimensionsAreEqual = true;
      var otherDimensions = other.getDimensions();
      this._dimensions.forEach(function (item, index) {
        if (!_.exists(otherDimensions[index])) {
          dimensionsAreEqual = false;
        }
        else if (item.low !== otherDimensions[index].low ||
          item.high !== otherDimensions[index].high) {
          dimensionsAreEqual = false;
        }
      });
      return dimensionsAreEqual && _.arrayEquals(this.getAsNativeArray(), other.getAsNativeArray());
    }
  },

  internals:{
    noOfDims         :null,
    values           :null,
    dimensions       :null,
    getDimensionality:function () {
      var dimSpaces = this._dimensions.map(function (elem) {
        return elem.high - elem.low + 1;
      });
      return dimSpaces;
    },
    getExactPoint    :function (args) {
      args = args.reverse();
      var dimSpaces = this._getDimensionality().reverse();
      var offset = args[0];
      for (var i = 1; i < args.length; i++) {
        var dimOff = args[i];
        for (var j = 0; j < i; j++) {
          dimOff *= dimSpaces[j];
        }
        offset += dimOff;
      }
      return this._values[offset];
    },
    getMultiDim      :function (args, marr) {
      var dims = this._getDimensionality();
      if (args.length == dims.length) {
        marr.push(this._getExactPoint(args));
      }
      else {
        var dimension = this._dimensions[args.length];
        for (var i = dimension.low; i <= dimension.high; i++) {
          var newArgs = function (gs, i) {
            var newArgs = ([]).concat(args);
            newArgs.push(i);
            return newArgs;
          }
          marr = this._getMultiDim(newArgs(args, i), marr);
        }
      }
      newArgs = [];
      return marr;
    }
  }

})