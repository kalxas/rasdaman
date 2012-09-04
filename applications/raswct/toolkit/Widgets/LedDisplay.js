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
 * @class LedDisplay extends OutputWidget
 *
 * Defines a Led Display widget.
 *
 * @author Mircea Dumitru <m.dumitru@jacobs-university.de>
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @package raswct
 * @version 1.0
 */

Rj.namespace('Rj.Widget');

Rj.Widget.LedDisplay = new JS.Class(Rj.Widget.OutputWidget, {

  /**
   * Standard class constructor
   * @param <float> value - the initial value displayed
   * @param <int> numIntegralDigits - the number of digits of the display
   * @param <float> numFractionalDigits - the number of fractional digits to display
   */
  initialize:function (value, numIntegralDigits, numFractionalDigits) {
    this.query = query;
    this.numIntegralDigits = numIntegralDigits || 4;
    this.numFractionalDigits = numFractionalDigits || 2;
    this.id = "";
    this.callSuper();
  },

  /**
   * Getter for the value attribute
   */
  getValue:function () {
    return this.value;
  },

  /**
   * Setter for the value attribute
   * @param <float> value - the new value of the counter, with 2 digits precision
   * @event ledchange - fires when the values of the display changes
   */
  setValue:function (value) {
    value = parseInt(value, 10);
    this.value = value;
    jQuery("#" + this.id).flipCounter("setNumber", value);
    this.fireEvent("changevalue", this.value);
  },

  /**
   * @override Rj.Widget.BaseWidget.renderTo
   */
  renderTo:function (selector) {
    this.id = Rj.getId(selector);
    jQuery("#" + this.id).flipCounter({
      number             :this.value, // the initial number the counter should display, overrides the hidden field
      numIntegralDigits  :this.numIntegralDigits, // number of places left of the decimal point to maintain
      numFractionalDigits:this.numFractionalDigits, // number of places right of the decimal point to maintain
      digitClass         :"counter-digit", // class of the counter digits
      counterFieldName   :"counter-value", // name of the hidden field
      digitHeight        :40, // the height of each digit in the flipCounter-medium.png sprite image
      digitWidth         :30, // the width of each digit in the flipCounter-medium.png sprite image
      imagePath          :"../../" + TOOLKIT_PATH + "bin/img/flipCounter-medium.png", // the path to the sprite image relative to your html document
      easing             :jQuery.easing.easeOutCubic, // the easing function to apply to animations, you can override this with a jQuery.easing method
      duration           :5000, // duration of animations
      onAnimationStarted :false, // call back for animation upon starting
      onAnimationStopped :false, // call back for animation upon stopping
      onAnimationPaused  :false, // call back for animation upon pausing
      onAnimationResumed :false // call back for animation upon resuming from pause
    });
  }

})