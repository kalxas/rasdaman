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
 * Base class for widgets, exposing methods for easy communication between the current
 * widget and other widgets on the page.
 * All widgets also contain a descendant of BaseQuery, be it SelectQuery or TransportQuery
 * which it can use to receive information from ther server
 * @module {Rj.widget}
 * @author Alex Dumitru <m.dumitru@jacobs-university.de>
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @version 3.0.0
 */

FlancheJs.defineClass("Rj.widget._BaseWidget", {
  
  implements: [Rj.util.Observable, Rj.util.Bindable],

  /**
   * @param <string> selector - a valid CSS3 or xPath selector or any other selectors
   * that work with the jQuery library
   */
  init:function (selector) {
    this.setSelector(selector);
  },

  methods:{
    /**
     * Shows the widget if it is hidden
     */
    show:function () {
      if (this.getSelector() !== null) {
        $(this.getSelector()).show();
      }
    },

    /**
     * Hides the widget
     */
    hide:function () {
      if (this.getSelector() !== null) {
        $(this.getSelector()).hide();
      }
    },

    /**
     * Destroys the widget
     */
    destroy:function () {
      this._clear();
    },

    /**
     * Binds the widget toa Rj.query object
     * @param <Rj.query> query - the query object to which the widget is binded
     */
    bind:function (query) {

    }
  },

  properties:{
    selector:{
      value:null,
      set  :function (selector) {
        this.$selector = selector;
        this.fireEvent("selectorchanged", selector);
        this._refresh();
      }
    }
  },

  internals:{

    /**
     * Placeholder function that should be extended by any showing widget
     */
    render   :function () {
      $(this.getSelector()).html("Loading...");
    },

    /**
     * Removes the widget from its container
     */
    clear:function () {
      $(this.getSelector()).html("");
    },

    /**
     * Removes the widget from the container and re-renders it
     */
    refresh:function () {
      this._clear();
      this._render();
    }
  }
})
