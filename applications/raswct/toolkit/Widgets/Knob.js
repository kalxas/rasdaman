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
 * @class Knob extends InputWidget
 *
 * Defines a knob widget.
 *
 * @author Mircea Dumitru <m.dumitru@jacobs-university.de>
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @package raswct
 * @version 1.0
 */

Rj.namespace('Rj.Widget');

Rj.Widget.Knob = new JS.Class(Rj.Widget.InputWidget, {
    
  /**
     * Standard class constructor
     * @param <int> min - the starting point of the knob
     * @param <int> max - the ending point of the knob
     * @param <int> value - the initial value of the knob
     * @param <bool> reverse - the values will be in reversed order
     * @param <int> snap - the number of degrees at which the knob is snapped to 0
     */
  initialize: function(min, max, value, reverse, snap){
    this.min = min || 0;
    this.max = max || 100;
    this.snap = snap || 1;
    this.reverse = reverse || false;
    this.id = '';
    this.callSuper();
    this.value = value || 0;
    console.log(this.value);
  },
    
  /**
     * Standard getter for the value attribute
     */
  getValue: function(){
    return this.value;
  },
    
  /**
     * @override Rj.Widget.BaseWidget.renderTo
     * @event knobchange - fires when the knob value is changed
     */
  renderTo: function(selector){
    this.id = selector;
    var self = this;
    var colors = [
    '26e000','2fe300','37e700','45ea00','51ef00',
    '61f800','6bfb00','77ff02','80ff05','8cff09',
    '93ff0b','9eff09','a9ff07','c2ff03','d7ff07',
    'f2ff0a','fff30a','ffdc09','ffce0a','ffc30a',
    'ffb509','ffa808','ff9908','ff8607','ff7005',
    'ff5f04','ff4f03','f83a00','ee2b00','e52000'
    ];
	
    var rad2deg = 180/Math.PI;
    var deg = 0;
    var bars = $('#' + this.id);
    bars.addClass('knobBars');
    bars.append("<div id = 'knobControl' class = 'knobControl'></div>");
        
    for(var i=0;i<colors.length;i++){
      deg = i*12;
      // Create the colorbars
      $('<div class="colorBar">').css({
        backgroundColor: '#'+colors[i],
        transform:'rotate('+deg+'deg)',
        top: -Math.sin(deg/rad2deg)*80+100,
        left: Math.cos((180 - deg)/rad2deg)*80+100
      }).appendTo(bars);
    }
    $('<div class="knobLabel">').css({
      padding: '200px 0 0 100px'
    }).appendTo(bars);
    $('.knobLabel').html(self.value);
    var colorBars = bars.find('.colorBar');
    var numBars = 0, lastNum = -1;
    if(self.reverse){
      var transVal = 360 - Math.round((self.value-self.min)*360/(self.max - self.min))%360;
    }
    else{
      transVal = Math.round((self.value-self.min)*360/(self.max - self.min))%360;
    }
    console.log(self);
    $('#knobControl').knobKnob({
      snap : self.snap,
      value: transVal,
      turn : function(ratio){
        numBars = Math.round(colorBars.length*ratio);
        this.value = Math.round(ratio*360);        
        if(self.reverse){          
          self.value = self.max - Math.round(this.value*(self.max-self.min)/360);
        }
        else{
          self.value = Math.round(this.value*(self.max-self.min)/360) + self.min;
        }
        $('.knobLabel').html(self.value);
        // Update the dom only when the number of active bars
        // changes, instead of on every move			
        if(numBars == lastNum){
          return false;
        }
        lastNum = numBars;		
        colorBars.removeClass('active').slice(0, numBars).addClass('active');
        self.fireEvent("knobchange", self.value);
      }
    });
  }
})