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
 * Testing Suite for the CacheEngine
 * @see Rj.util.CacheEngine
 * @author Alex Dumitru <m.dumitru@jacobs-university.de>
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @version 3.0.0
 */
buster.testCase("Rj.query.CacheEngine tests", {
  setUp: function(){
    this.timeout = 1000;
    Rj.util.CacheEngine.set("test2", "initialval");
  },

  "CacheEngine initial set works": function(done){
    Rj.util.CacheEngine.set("test", "somevalue");
    Rj.util.CacheEngine.get("test", function(key, value){
      if(value == "somevalue"){
        buster.assert(true);
      }
      else{
        buster.assert(false);
      }
      done();
    })
  },

  "CacheEngine update works": function(done){
    var k = "test2", val = "somevalue2"
    Rj.util.CacheEngine.set(k, val);
    Rj.util.CacheEngine.get(k, function(key, value){
      if(value == val){
        buster.assert(true);
      }
      else{
        buster.assert(false);
      }
      done();
    })
  },

  "CacheEngine remove works": function(done){
    var k = "test3", val = "somevalue3"
    Rj.util.CacheEngine.set(k, val);
    Rj.util.CacheEngine.remove(k);
    Rj.util.CacheEngine.get(k, function(key, val){
      if(!_.exists(val)){
        buster.assert(true);
      }
      else{
        buster.assert(false);
      }
      done();
    });
  }

})