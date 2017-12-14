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
 * This singleton acts as a key value store for caching misc data directly
 * in the browser.
 * @author Alex Dumitru <m.dumitru@jacobs-university.de>
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @version 3.0.0
 */

FlancheJs.defineClass("Rj.util._CacheEngine", {

  init: function(){
    this._storage = Lawnchair({name: Rj.util._CacheEngine.MainKey}, function(e){
      //Nothing to do, we just want to initialize the database
    });
  },

  methods: {
    set: function(key, value){
      var serializedValue = this._serialize(value)
      this._storage.save({key: key, value: serializedValue});
    },

    get: function(key, callback){
      var self = this;
      this._storage.get(key, function(keyVal){
        var unserializedValue = null;
        if(_.exists(keyVal)){
          unserializedValue = self._unserialize(keyVal.value);
        }
        callback.call(null, key, unserializedValue);
      });
    },

    exists : function(key, callback){
      this._storage.exists(key, callback);
    },

    remove: function(key){
      this._storage.remove(key);
    },

    clearCache: function(){
      this._storage.nuke();
    }
  },

  internals: {
    storage  : null,
    serialize: function(data){
      var serialized = null;
      if(_.exists(data) && data !== ""){
        serialized = JSON.stringify(data)
      }
      return serialized;
    },

    unserialize: function(data){
      var unserialized = null;
      if(_.exists(data) && data !== ""){
        unserialized = JSON.parse(data);
      }
      return unserialized;
    }
  },

  statics: {
    MainStore: "RaswctCache"
  }

})

Rj.util.CacheEngine = new Rj.util._CacheEngine();