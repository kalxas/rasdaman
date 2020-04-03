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
 * Defines a horizontal slider that can be used to slide through an interval
 * of numerical values.
 * @module {Rj.widget}
 * @author Alex Dumitru <m.dumitru@jacobs-university.de>
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @version 3.0.0
 */

FlancheJs.defineClass('Rj.widget.HorizontalSlider', {

  extends: Rj.widget._Slider,

  /**
   * Constructor for the slider widget
   * @param {String} selector any CSS3 or XPath selector
   * @param {Number} min the minimum value the slider can take
   * @param {Number} max the maximum value the slider can take
   * @see Rj.widget._Slider
   */
  init: function(selector, min, max){
    this.$height = Rj.widget.HorizontalSlider.HorizontalDefaultHeight
    Rj.widget.HorizontalSlider.prototype.parent.call(this,
      selector,
      Rj.widget._Slider.Orientation.HORIZONTAL,
      min, max);
  },

  statics : {
    HorizontalDefaultHeight : 5
  }
})