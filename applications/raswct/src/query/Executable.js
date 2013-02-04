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
 * The Executable Trait should be used in the composition of any query classes that can
 * be evaluated by a services.
 * @provides {evaluate}
 * @needs {transport : Function} - a function that returns a Rj.query.Transport object.
 * @module {Rj.query}
 * @author Alex Dumitru <m.dumitru@jacobs-university.de>
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @version 3.0.0
 */
FlancheJs.defineTrait("Rj.query.Executable", {

  methods: {
    /**
     * Evaluates the query and calls a corresponding callback function
     * @param {Function} callback - a function to process the response.
     * @param {Boolean} persistent - if set to true the callback will be called each
     * time the query will be re-executed
     * @todo decide if persistent is relevant
     */
    evaluate: function(callback, persistent){
      if(persistent === true){
        this._callbacks.push(callback);
      }
      var self = this;
      var transport = this.transport();
      if(this.getCached() === true){
        this._evaluateCached(transport, callback);
      }
      else{
        this._evaluateRaw(transport, callback);
      }
    }
  },

  properties: {
    cached: {
      value: false
    }
  },

  internals: {
    callbacks    : [],
    fireCallbacks: function(response, httpStatus){
      for(var i = 0; i < this._callbacks.length; i++){
        this._callbacks[i].call(self, response, httpStatus);
      }
    },

    evaluateCached: function(transport, callback){
      var self = this;
      var key = transport.toHashCode();
      Rj.util.CacheEngine.exists(key, function(exists){
        if(exists){
          Rj.util.CacheEngine.get(key, function(key, value){
            if(_.exists(value)){
              //object in cache, apply the callback
              self._handleCallback(callback, value, 200);
            }
          })
        }
        else{
          //Object not in cache, first add the response in cache and then apply callback
          self._evaluateRaw(transport, function(response, httpStatus){
            Rj.util.CacheEngine.set(transport.toHashCode(), response);
            self._handleCallback(callback, response, httpStatus);
          })
        }
      });
    },

    evaluateRaw: function(transport, callback){
      var self = this;
      _.ajax({
        url    : transport.getServiceUrl(),
        method : transport.getServiceHttpMethod(),
        data   : transport.getParams(),
        binary : transport.getBinary(),
        success: function(response, httpStatus){
          if(httpStatus == 404){
            Rj.util.ErrorManager.reportError(Rj.util.Constants.serviceUnavailableErrorMessage, true);
          }
          else if(httpStatus == 500){
            Rj.util.ErrorManager.reportError(Rj.util.Constants.serviceErrorMessage + response, true);
          }
          else{
            self._handleCallback(callback, response, httpStatus);
          }
        },
        error  : function(){
          Rj.util.ErrorManager.reportError(Rj.util.Constants.serviceUnavailableErrorMessage, true);
        }
      });
    },

    handleCallback: function(callback, response, httpStatus){
      //if a callback is provided return response to it
      if(_.exists(callback)){
        callback.call(this, response, httpStatus);
      }
      //otherwise call all registered callbacks
      else{
        this._fireCallbacks(response, httpStatus);
      }
      this.fireEvent("evaluated", response);
    }
  },

  needs: {
    /**
     * Requires a function that returns a transport object
     */
    transport: Function
  }
})



