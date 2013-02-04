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
 * Defines a SpeedoMeter widget.
 * @module {Rj.widget}
 * @author Alex Dumitru <m.dumitru@jacobs-university.de>
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @version 3.0.0
 */

FlancheJs.defineClass('Rj.widget.SpeedoMeter', {

  extends:Rj.widget._OutputWidget,

  init:function (selector) {
    Rj.widget.SpeedoMeter.prototype.parent.call(this, selector);
  },

  properties:{
    value      :{
      value:Rj.util.Constants.speedoMeterValue,
      set  :function (value) {
        this.$value = value;
        if (_.exists(this._gauge)) {
          this._gauge.setValue(value);
        }
        this.fireEvent("valuechange", value);
      }
    },
    labelSuffix:{
      value:Rj.util.Constants.speedoMeterLabelSuffix,
      set  :function (labelSuffix) {
        this.$labelSuffix = labelSuffix;
        this._refresh();
      }
    }
  },

  internals:{
    gauge:null,

    render:function () {
      this.fireEvent("beforerender");
      var id = _.getId(this.getSelector());
      $("#" + id).addClass("jgauge");
      this._gauge = new jGauge();
      this._gauge.imagePath = Rj.util.Constants.speedoMeterImage;
      this._gauge.needle.imagePath = Rj.util.Constants.speedoMeterNeedleImage;
      this._gauge.label.suffix = this.getLabelSuffix();
      this._gauge.id = id;
      this._gauge.init();
      this._gauge.setValue(this.getValue());
      this.fireEvent("afterrender");
    }
  }

})
