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
 * The ConfigManager class acts as a singleton to store the configuration data used across raswct modules
 * @module {Rj.util}
 * @author Alex Dumitru <m.dumitru@jacobs-university.de>
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @version 3.0.0
 */

FlancheJs.defineClass("Rj.util._ConfigManager", {
  init:function () {

  },

  properties:{
    RasdamanServiceUrl:{
      value:"http://example.org/rasdaman/"
    },
    WCSService        :{
      value:{
        baseUrl:"http://example.org/",
        name   :"WCS",
        version:"2.0.1"
      }
    },
    WCPSService       :{
      value:{
        url           :"http://example.org/wcps/1.0/",
        queryParameter:"request"
      }
    },
    RequestDelay: {
      value: Rj.util.Constants.requestDelay
    }
  }

});

Rj.util.ConfigManager = new Rj.util._ConfigManager();