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
 * Interface for event communication
 * @module {Rj.util}
 * @author Alex Dumitru <m.dumitru@jacobs-university.de>
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @version 3.0.0
 */

FlancheJs.defineTrait("Rj.util.Observable", {

  methods:{
    /**
     * Adds a listener to the events fired by this object
     * @param <string> caller - a identifier for the object that is requesting the addon
     * @param <string> event - the name of the event you want to listen to
     * @param <function> callback - a callback function that will be executed once the event is fired
     */
    addListener:function (caller, event, callback) {
      if (!this._listeners[event]) {
        this._listeners[event] = {};
      }
      this._listeners[event][caller] = callback;
    },

    /**
     * Removes a listnener from the list
     * @param <string> caller - @see addListener
     * @param <string> event - @see addListener
     */
    removeListener:function (caller, event) {
      delete(this._listeners[event][caller]);
    },

    /**
     * Fires an event associated with the widgets. All objects that registered for this event
     * will be notified
     * @param <string> event - the name of the event to be fired
     * @param <object> args - any aditional parameters you might pass to the handlers
     */
    fireEvent:function (event, args) {
      var callers = this._listeners[event];
      var status = true;
      for (var callerId in callers) {
        var currentCaller = callers[callerId];
        var currentStatus = currentCaller.call(this, args);
        if (currentStatus === false) {
          status = false;
        }
        else {
          status = (currentStatus !== null && currentStatus !== undefined && currentStatus !== false) ? currentStatus : status;
        }
      }
      return status;
    }
  },

  internals:{
    listeners:{}
  }
  
})
