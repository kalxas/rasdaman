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
 * Defines a binary image widget that can consume uint8 data and transform it into
 * a image that can be displayed in the browser.
 * @module {Rj.widget}
 * @author Alex Dumitru <m.dumitru@jacobs-university.de>
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @version 3.0.0
 */

FlancheJs.defineClass("Rj.widget.BinaryImage", {

  extends: Rj.widget._OutputWidget,

  /**
   * Class constructor
   * @param {String} selector - A valid cs3 selector.
   * @param {String} format - The data format (e.g. 'image/png').
   * @param {ArrayBuffer} arrayBufData - The data stream.
   */
  init: function(selector, format, arrayBufData){
    this._generateBase64Data(arrayBufData)
    this._format = format;
    Rj.widget.BinaryImage.prototype.parent.call(this, selector);
  },

  properties: {
    width : {
      value: null,
      set  : function(width){
        this.$width = width;
        this._refresh();
      }
    },
    height: {
      value: null,
      set  : function(height){
        this.$height = height;
        this._refresh();
      }
    },
    binaryData : {
      value : null,
      set : function(data){
        this.$binaryData = data;
        this._generateBase64Data(this.$binaryData);
        this._refresh();
      }
    }
  },

  internals: {
    base64Data: null,
    format    : null,
    generateBase64Data : function(arrayBufferData){
      this._base64Data = _.arrayBufferToBase64(arrayBufferData);
    },
    render    : function(){
      var id = _.getId(this.getSelector());
      var style = "style=\"";
      if(this.getWidth()){
        style += "width: " + this.getWidth() + "px;";
      }
      if(this.getHeight()){
        style += "height: " + this.getHeight() + "px;";
      }
      style += '"';
      this.fireEvent("beforerender");
      jQuery("#" + id).append('<img src="data:' + this._format + ';base64,' + this._base64Data + '" ' + style + ' />');
      this.fireEvent("afterrender");
    }
  }
})
