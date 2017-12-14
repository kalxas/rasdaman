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
 * Testing Suite for MultiDimArray
 * @see Rj.util.MultiDimArray
 * @author Alex Dumitru <m.dumitru@jacobs-university.de>
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @version 3.0.0
 */

var assert = buster.assert;

buster.testCase("Rj.util.MultiDimArray Tests", {
  setUp:function () {
    this.m1d = new Rj.util.MultiDimArray([
      {low:0, high:4}
    ], [10, 11, 17, 23, 25]);
    this.m2d = new Rj.util.MultiDimArray([
      {low:0, high:2},
      {low:0, high:2}
    ], [1, -1, 2, 3, 5, 7, 12, 19, 31]);
    this.m3d = new Rj.util.MultiDimArray([
      {low:0, high:1},
      {low:0, high:1},
      {low:0, high:2}
    ], [1, -1, 2, 3, 5, 7, 12, 19, 31, 50, 81, 131]);
  },

  "1D Array: ":{
    "get method should return the correct point":function () {
      assert.equals(this.m1d.get(0), 10);
      assert.equals(this.m1d.get(2), 17);
      assert.equals(this.m1d.get(4), 25);
    }
  },

  "2D Array: ":{
    "get method should return the correct point"                   :function () {
      assert.equals(this.m2d.get(0, 0), 1);
      assert.equals(this.m2d.get(0, 1), -1);
      assert.equals(this.m2d.get(0, 2), 2);
      assert.equals(this.m2d.get(1, 2), 7);
      assert.equals(this.m2d.get(2, 1), 19);
    },
    "get method should return the correct multiDimArray (degree 1)":function () {
      var resp = new Rj.util.MultiDimArray([
        {low:0, high:2}
      ], [1, -1, 2]);
      assert(this.m2d.get(0).equals(resp));
      var resp = new Rj.util.MultiDimArray([
        {low:0, high:2}
      ], [12, 19, 31]);
      assert(this.m2d.get(2).equals(resp));
    }
  },

  "3D Array: ":{
    "get method should return the correct point":function () {
      assert.equals(this.m3d.get(0, 0, 0), 1);
      assert.equals(this.m3d.get(0, 1, 0), 3);
      assert.equals(this.m3d.get(0, 1, 2), 7);
      assert.equals(this.m3d.get(1, 1, 2), 131);
      assert.equals(this.m3d.get(1, 1, 0), 50);
    },

    "get method should return the correct multiDimArray (degree 1 & 2)":function () {
      var resp = new Rj.util.MultiDimArray([
        {low:0, high:2}
      ], [3, 5, 7]);
      assert(this.m3d.get(0, 1).equals(resp));

      var resp = new Rj.util.MultiDimArray([
        {low:0, high:2}
      ], [50, 81, 131]);
      assert(this.m3d.get(1, 1).equals(resp));

      var resp = new Rj.util.MultiDimArray([
        {low:0, high:1},
        {low:0, high:2}
      ], [1, -1, 2, 3, 5, 7]);
      assert(this.m3d.get(0).equals(resp));

      var resp = new Rj.util.MultiDimArray([
        {low:0, high:1},
        {low:0, high:2}
      ], [12, 19, 31, 50, 81, 131]);
      assert(this.m3d.get(1).equals(resp));
    }
  },

  "Equals: ":{
    "two identical MultiDimArrays should be equal"    :function () {
      var m1 = new Rj.util.MultiDimArray([
        {low:0, high:1},
        {low:0, high:1}
      ], [1, 2, 3, 4]);
      var m2 = new Rj.util.MultiDimArray([
        {low:0, high:1},
        {low:0, high:1}
      ], [1, 2, 3, 4]);
      assert(m1.equals(m2));
    },
    "two different MultiDimArrays should not be equal":function () {
      var m1 = new Rj.util.MultiDimArray([
        {low:0, high:1},
        {low:0, high:1}
      ], [1, 2, 3, 4]);
      var m2 = new Rj.util.MultiDimArray([
        {low:0, high:3}
      ], [1, 2, 3, 4]);
      assert(m1.equals(m2) === false);

      var m3 = new Rj.util.MultiDimArray([
        {low:0, high:1},
        {low:0, high:1}
      ], [1, 2, 3, 4]);
      var m4 = new Rj.util.MultiDimArray([
        {low:0, high:1},
        {low:0, high:1}
      ], [1, 2, 3, 1]);
      assert(m3.equals(m4) === false);
    }
  }

});