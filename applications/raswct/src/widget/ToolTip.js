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
 * Defines a tooltip widget
 * @module {Rj.widget}
 * @author Mircea Dumitru <m.dumitru@jacobs-university.de>
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @version 3.0.0
 */

FlancheJs.defineClass('Rj.widget.ToolTip', {

  extends : Rj.widget._OutputWidget,

  init : function(selector){
    Rj.widget.ToolTip.prototype.parent.call(this, selector);
  },

  properties : {
    value   : {
      value : Rj.util.Constants.toolTipValue,
      set   : function(value){
        this.$value = value;
        if(this._tip){
          this._tip.qtip('option', 'content.text', this.getPretext() + value);
        }
        this.fireEvent("valuechanged", value);
      }
    },
    pretext : {
      value : Rj.util.Constants.toolTipPretext,
      set   : function(pretext){
        this.$pretext = pretext;
        this._refresh();
      }
    },
    postext : {
      value : Rj.util.Constants.toolTipPostext,
      set   : function(postext){
        this.$postext = postext;
        this._refresh();
      }
    },
    adjust  : {
      value : {},
      set   : function(adjust){
        this.$adjust = adjust;
        this._refresh();
      }
    },
    place   : {
      value : Rj.util.Constants.toolTipPlace,
      set   : function(place){
        this.$place = place;
        this._refresh();
      }
    },
    mouse   : {
      value : Rj.util.Constants.toolTipMouse,
      set   : function(mouse){
        this.$mouse = mouse;
        this._refresh();
      }
    },
    delay   : {
      value : Rj.util.Constants.toolTipDelay,
      set   : function(delay){
        this.$delay = delay;
        this._refresh();
      }
    }
  },

  internals : {
    tip : null,

    render : function(){
      this.fireEvent("beforerender");
      var id = _.getId(this.getSelector());
      this._tip = $('#' + id);
      var target = this.getMouse() ? 'mouse' : $('#' + id);
      this._tip.qtip({
        content  : this.getPretext() + this.getValue() + this.getPostext(),
        position : {
          my     : this.getPlace() + ' center',
          at     : 'top center',
          target : target,
          adjust : this.getAdjust()
        },
        hide     : {
          delay : this.getDelay()
        },
        style    : 'qtip-shadow'
      });
      this.fireEvent("afterrender");
    },

    clear: function(){
      this._qtip = null;
    }
  }
})