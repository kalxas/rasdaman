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
 * Defines a knob widget.
 * @module {Rj.widget}
 * @author Alex Dumitru <m.dumitru@jacobs-university.de>
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @version 3.0.0
 */

FlancheJs.defineClass("Rj.widget.Knob", {

  extends: Rj.widget._InputWidget,

  init: function(selector, min, max){
    if(_.exists(min)){
      this.$min = min;
      this.$value = min;
    }
    else{
      this.$value = Rj.util.Constants.knobMin;
    }
    if(_.exists(max)){
      this.$max = max;
    }

    Rj.widget.Knob.prototype.parent.call(this, selector);
    //this._render();
  },

  properties: {
    min    : {
      value: Rj.util.Constants.knobMin,
      set  : function(min){
        this.$min = min;
        this._refresh();
      }
    },
    max    : {
      value: Rj.util.Constants.knobMax,
      set  : function(max){
        this.$max = max;
        this._refresh()
      }
    },
    snap   : {
      value: Rj.util.Constants.knobSnap,
      set  : function(snap){
        this.$snap = snap;
        this._refresh();
      }
    },
    reverse: {
      value: Rj.util.Constants.knobReverse,
      set  : function(reverse){
        this.$reverse = reverse;
        this._refresh();
      }
    }
  },

  internals: {

    /**
     * @override Rj.widget.BaseWidget.render
     */
    render: function(){
      this.fireEvent('beforerender');
      var id = _.getId(this.getSelector());
      var self = this;
      var colors = Rj.util.Constants.knobColors;
      var rad2deg = 180 / Math.PI;
      var deg = 0;
      var bars = $('#' + id);
      bars.addClass('knobBars');
      bars.append("<div id = 'knobControl' class = 'knobControl'></div>");

      for(var i = 0; i < colors.length; i++){
        deg = i * 12;
        // Create the colorbars
        $('<div class="colorBar">').css({
          backgroundColor: '#' + colors[i],
          transform      : 'rotate(' + deg + 'deg)',
          top            : -Math.sin(deg / rad2deg) * 80 + 100,
          left           : Math.cos((180 - deg) / rad2deg) * 80 + 100
        }).appendTo(bars);
      }
      $('<div class="knobLabel">').css({
        padding: '200px 0 0 100px'
      }).appendTo(bars);
      $('.knobLabel').html(self.getValue());
      var colorBars = bars.find('.colorBar');
      var numBars = 0, lastNum = -1;
      if(self.getReverse()){
        var transVal = 360 - Math.round((self.getValue() - self.getMin()) * 360 / (self.getMax() - self.getMin())) % 360;
      }
      else{
        transVal = Math.round((self.getValue() - self.getMin()) * 360 / (self.getMax() - self.getMin())) % 360;
      }
      $('#knobControl').knobKnob({
        snap : self.getSnap(),
        value: transVal,
        turn : function(ratio){
          numBars = Math.round(colorBars.length * ratio);
          this.value = Math.round(ratio * 360);
          var value;
          if(self.getReverse()){
            value = self.getMax() - Math.round(this.value * (self.getMax() - self.getMin()) / 360);
          }
          else{
            value = Math.round(this.value * (self.getMax() - self.getMin()) / 360) + self.getMin();
          }
          $('.knobLabel').html(value);
          // Update the dom only when the number of active bars
          // changes, instead of on every move			
          if(numBars == lastNum){
            return false;
          }
          lastNum = numBars;
          colorBars.removeClass('active').slice(0, numBars).addClass('active');
          self._setValue(value);
          //set value when releasing
//          $("#" + id).mouseup(function(){
//            self._setValue(value);
//          })
        }
      });
      self.fireEvent('afterrender');
    },

    setValue: function(value){
      this.$value = value;
      this.fireEvent("valuechanged", this.getValue());
    }
  }

})