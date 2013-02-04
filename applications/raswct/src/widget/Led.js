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
 * Defines a Led Display widget.
 * @module {Rj.widget}
 * @author Alex Dumitru <m.dumitru@jacobs-university.de>
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @version 3.0.0
 */

FlancheJs.defineClass('Rj.widget.Led', {

  extends:Rj.widget._OutputWidget,

  init:function (selector) {
    Rj.widget.Led.prototype.parent.call(this, selector);
  },

  properties:{
    value     :{
      value:Rj.util.Constants.ledValue,
      set  :function (value) {
        var oldValue = this.$value;
        this.$value = value;
        this.fireEvent("valuechanged", value);
        var id = _.getId(this.getSelector());
        jQuery("#" + id).flipCounter("startAnimation", {
          number: oldValue,
          end_number: value,
          duration: Rj.util.Constants.ledChangeDuration
        });
      }
    },
    intDigits :{
      value:Rj.util.Constants.ledIntDigits,
      set  :function (intDigits) {
        this.$intDigits = intDigits;
        this._refresh();
      }
    },
    fracDigits:{
      value:Rj.util.Constants.ledFracDigits,
      set  :function (fracDigits) {
        this.$fracDigits = fracDigits;
        this._refresh();
      }
    }
  },

  internals:{
    render:function () {
      this.fireEvent("beforerender");
      var id = _.getId(this.getSelector());
      jQuery("#" + id).flipCounter({
        number             :this.getValue(), // the initial number the counter should display, overrides the hidden field
        numIntegralDigits  :this.getIntDigits(), // number of places left of the decimal point to maintain
        numFractionalDigits:this.getFracDigits(), // number of places right of the decimal point to maintain
        digitClass         :Rj.util.Constants.ledDigitClass, // class of the counter digits
        counterFieldName   :Rj.util.Constants.ledCounterFieldName, // name of the hidden field
        digitHeight        :Rj.util.Constants.ledDigitHeight, // the height of each digit in the flipCounter-medium.png sprite image
        digitWidth         :Rj.util.Constants.ledDigitWidth, // the width of each digit in the flipCounter-medium.png sprite image
        imagePath          :Rj.util.Constants.ledImagePath, // the path to the sprite image relative to your html document
        easing             :jQuery.easing.easeOutCubic, // the easing function to apply to animations, you can override this with a jQuery.easing method
        duration           :Rj.util.Constants.ledDuration, // duration of animations
        onAnimationStarted :false, // call back for animation upon starting
        onAnimationStopped :false, // call back for animation upon stopping
        onAnimationPaused  :false, // call back for animation upon pausing
        onAnimationResumed :false // call back for animation upon resuming from pause
      });
      this.fireEvent("afterrender");
    }
  }
})
