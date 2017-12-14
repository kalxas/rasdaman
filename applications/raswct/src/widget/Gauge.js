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
 * Defines a Gauge widget.
 * @module {Rj.widget}
 * @author Alex Dumitru <m.dumitru@jacobs-university.de>
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @version 3.0.0
 */

FlancheJs.defineClass('Rj.widget.Gauge', {

  extends:Rj.widget._OutputWidget,

  init:function (selector, min, max) {
    if(_.exists(min)){
      this.$min = min;
    }
    if(_.exists(max)){
      this.$max = max;
    }
    Rj.widget.Gauge.prototype.parent.call(this, selector);
  },

  properties:{
    value     :{
      value:Rj.util.Constants.gaugeValue,
      set  :function (value) {
        this.$value = value;
        if (this._gauge) {
          this._gauge.refresh(value);
        }
        this.fireEvent("valuechanged", value);
      }
    },
    min       :{
      value:Rj.util.Constants.gaugeMin,
      set  :function (min) {
        this.$min = min;
        this._refresh();
      }
    },
    max       :{
      value:Rj.util.Constants.gaugeMax,
      set  :function (max) {
        this.$max = max;
        this._refresh();
      }
    },
    title     :{
      value:Rj.util.Constants.gaugeTitle,
      set  :function (title) {
        this.$title = title;
        this._refresh();
      }
    },
    label     :{
      value:Rj.util.Constants.gaugeLabel,
      set  :function (label) {
        this.$label = label;
        this._refresh();
      }
    },
    widthScale:{
      value:Rj.util.Constants.gaugeWidthScale,
      set  :function (widthScale) {
        this.$widthScale = widthScale;
        this._refresh();
      }
    },
    showMinMax:{
      value:Rj.util.Constants.gaugeShowMinMax,
      set  :function (showMinMax) {
        this.$showMinMax = showMinMax;
        this._refresh();
      }
    }
  },

  internals:{
    gauge:null,

    render:function () {
      this.fireEvent("beforerender");
      var id = _.getId(this.getSelector());
      //height needs to be specified, if not set default
      if(!$("#" + id).height()){
        $("#" + id).height(Rj.util.Constants.gaugeHeight);
      }
      //making sure width/height ration is kept
      var width = $("#" + id).css("width");
      $("#" + id).css("height", width * 16 / 20);
      //rendering the gauge
      this._gauge = new JustGage({
        id                  :id,
        value               :this.getValue(),
        min                 :this.getMin(),
        max                 :this.getMax(),
        title               :this.getTitle(),
        titleFontColor      :Rj.util.Constants.gaugeTitleColor,
        valueFontColor      :Rj.util.Constants.gaugeValueColor,
        showMinMax          :this.getShowMinMax(),
        gaugeWidthScale     :this.getWidthScale(),
        gaugeColor          :Rj.util.Constants.gaugeColor,
        labelFontColor      :Rj.util.Constants.gaugeLabelColor,
        label               :this.getLabel(),
        shadowOpacity       :Rj.util.Constants.gaugeShadowOpacity,
        shadowSize          :Rj.util.Constants.gaugeShadowSize,
        shadowVerticalOffset:Rj.util.Constants.gaugeShadowOffset
      });
      this.fireEvent("afterrender");
    }
  }
})
