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
 * The ErrorManager is a singleton that manage the error messages, displaying them to the user
 * or just reporting them in the dev console
 * @module {Rj.util}
 * @author Alex Dumitru <m.dumitru@jacobs-university.de>
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @version 3.0.0
 */

FlancheJs.defineClass("Rj.util._ErrorManager", {

  init:function () {

  },

  properties:{
    warnings:{
      value:true
    },
    errors  :{
      value:true
    }
  },

  methods:{
    reportError  :function (errorMsg, throwable) {

      // Only get the error message, not the XML wrapper
      if (errorMsg.includes("ows:ExceptionText")) {
        errorMsg = errorMsg.substring(errorMsg.indexOf("<ows:ExceptionText>") + 19);
        errorMsg = errorMsg.substring(errorMsg.indexOf("</ows:ExceptionText>"), 0);
      }

      if (!this.getErrors()) {
        return;
      }
      if (throwable) {
        Rj.util.NotificationManager.alert(Rj.util.Constants.errorMessageTitle, errorMsg, "error");
        throw Error(errorMsg);
      }
      else {
        this._consoleErr(errorMsg);
      }
    },
    reportWarning:function (errorMsg) {
      if (!this.getWarnings()) {
        return;
      }
      Rj.util.NotificationManager.alert("Warning", errorMsg, "warning");
      this._consoleWarn(errorMsg);
    }
  },

  internals:{
    consoleErr:function (err) {
      if (_.exists(window.console)) {
        console.error(err);
      }
    },

    consoleWarn:function (err) {
      if (_.exists(window.console)) {
        console.warn(err)
      }
    }
  }

});

Rj.util.ErrorManager = new Rj.util._ErrorManager();
