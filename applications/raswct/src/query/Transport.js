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
 /

 /**
 * The transport objects are used by the Executable trait to send the queries to the server to be evaluated
 * @module {Rj.query}
 * @author Alex Dumitru <m.dumitru@jacobs-university.de>
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @version 3.0.0
 * @todo change name to _Transport
 */
FlancheJs.defineClass("Rj.query.Transport", {

  /**
   * Constructor for the Transport function
   * @param {String} serviceUrl - an url to a service that can process the query
   * @param {String} params - an object containing the parameters to the service e.g. {query : "select ..."}
   * @param {Rj.query.Transport.HttpMethod} serviceHttpMethod - the http method e.g. POST / GET
   * @param {Function} parseResponse - a function to parse the response from the server e.g. function(response){return response;}
   */
  init: function(serviceUrl, params, serviceHttpMethod, parseResponse){
    this.setServiceUrl(serviceUrl);
    this.setParams(params);
    if(serviceHttpMethod){
      this.setServiceHttpMethod(serviceHttpMethod);
    }
    if(parseResponse){
      this.setParseResponse(parseResponse);
    }
  },

  statics: {
    /**
     * Common HTTP Methods recognized by the Executable trait
     */
    HttpMethod: {
      POST  : "post",
      GET   : "get",
      PUT   : "put",
      DELETE: "delete"
    }
  },

  methods: {
    toHashCode: function(){
      var str = this.getServiceHttpMethod() + "__" +
        this.getServiceUrl() + "__" +
        this.getBinary() + "__" +
        JSON.stringify(this.getParams());
      return _.stringToHashCode(str);
    }
  },

  properties: {
    serviceUrl       : {},
    serviceHttpMethod: {
      value: "post"
    },
    params           : {},
    parseResponse    : {
      value: function(response){
        return response;
      }
    },
    binary           : {
      value: true
    }
  }

})