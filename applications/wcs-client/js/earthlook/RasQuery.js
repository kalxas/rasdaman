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
 * A RasQuery is a query to the rasdaman server. All queries defined in rasql can be used with this class.
 * @module {Rj.query}
 * @author Alex Dumitru <m.dumitru@jacobs-university.de>
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @version 3.0.0
 */

FlancheJs.defineClass("Rj.query.RasQuery", {

  /**
   * Constructor for the class
   * @param {String} query - the query string containing one or multiple parameters
   * @param {Array} vars - the query variables (e.g. $domain)
   */
  init: function(query, vars){
    this.setQuery(query);
    if(_.exists(vars)){
      for(var i = 0; i < vars.length; i++){
        this.setVariable(vars[i], undefined);
      }
    }
  },

  implements: [Rj.query.LiteralQuery, Rj.query.Executable],

  methods: {
    /**
     * Implementation of the transport function, needed by {Rj.query.Executable}
     */
    transport: function(){
      var transport = new Rj.query.Transport(
        Rj.util.ConfigManager.getRasdamanServiceUrl(),
        Rj.query.Transport.HttpMethod.POST,
        {
          query: this.toString()
        },
        this._parseResponse
      );
      return transport;
    }
  },

  internal: {
    parseResponse: function(response){
      return JSON.parse(response);
    }
  }

});