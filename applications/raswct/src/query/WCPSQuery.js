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
 * A WCPSQuery object can send wcps queries to a service that can process them and parse the result
 * to obtain meaningul data for Widgets.
 * @module {Rj.query}
 * @implements Rj.query.LiteralQuery, Rj.query.Executable
 * @author Alex Dumitru <m.dumitru@jacobs-university.de>
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @version 3.0.0
 */
FlancheJs.defineClass("Rj.query.WCPSQuery", {

  /**
   * Constructor for the class
   * @param query a string query containing 0 or more parameterized variables
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

  implements: [Rj.query.Executable, Rj.query.LiteralQuery, Rj.util.Bindable, Rj.util.Observable],

  properties: {
    /**
     * Should be set to true if the query returns a binary format (e.g. image)
     * instead of UTF-8 / ASCII format
     */
    binaryFormat: {
      value: false
    }
  },

  methods: {
    /**
     * Returns a transport object that can be used internally by
     * the Executable trait
     * @return {Rj.query.Transport}
     */
    transport: function(){
      var params = {};
      var tpl = Rj.util.Constants.templates.wcpsRequestTemplate.replace(Rj.util.Constants.wcpsQueryPlaceHolder, this._expand());
      params[Rj.util.ConfigManager.getWCPSService().queryParameter] = tpl;
      var transport = new Rj.query.Transport(
        Rj.util.ConfigManager.getWCPSService().url,
        params,
        Rj.query.Transport.HttpMethod.POST
      )
      transport.setBinary(this.getBinaryFormat());
      return transport;
    }
  }

})