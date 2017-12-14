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

/* 
 * Defines an a widget used for displaying maps composed
 * of several layers
 * The implementation is based on the OpenLayers library <http://http://openlayers.org/>
 * @module {Rj.widget}
 * @author Alex Dumitru <m.dumitru@jacobs-university.de>
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @version 3.0.0
 */

FlancheJs.defineClass('Rj.widget.Map', {

  extends: Rj.widget._OutputWidget,

  /**
   * Constructor for the map widget
   * @param <object> options - an OpenLayers configuration object
   */
  init: function(selector, options){
    if(_.exists(options)){
      if(!_.exists(options.theme)){
        options.theme = null;
      }
      this.$map = new OpenLayers.Map(options);
    }
    OpenLayers.ImgPath = Rj.util.Constants.imgPath;
    Rj.widget.Map.prototype.parent.call(this, selector);
  },

  properties: {
    map   : {
      value: null
    },
    layers: {
      value: []
    },
    width : {
      value: Rj.util.Constants.mapWidth,
      set  : function(width){
        this.$width = width;
        this._refresh();
      }
    },
    height: {
      value: Rj.util.Constants.mapHeight,
      set  : function(height){
        this.$height = height;
        this._refresh();
      }
    }
  },

  methods: {
    /**
     * Adds layers to the map
     * @param <array> layers - Rj.util.Layer objects
     */
    addLayers: function(layers){
      var rawLayers = [];
      for(var i = 0; i < layers.length; i++){
        rawLayers.push(layers[i].getLayer());
        this.$layers.push(layers[i]);
      }
      this.$map.addLayers(rawLayers);
      this._refresh();
    }
  },

  internals: {
    render: function(){
      if(this.$map){
        if(this.getLayers().length){
          this.fireEvent('beforerender');
          $('#' + _.getId(this.getSelector())).width(this.getWidth());
          $('#' + _.getId(this.getSelector())).height(this.getHeight());
          this._rendered = true;
          this.$map.addControl(new OpenLayers.Control.LayerSwitcher());
          this.$map.addControl(new OpenLayers.Control.MousePosition());
          this.$map.addControl(new OpenLayers.Control.OverviewMap());
          this.$map.addControl(new OpenLayers.Control.KeyboardDefaults());
          this.$map.zoomToMaxExtent();
          this.$map.render(_.getId(this.getSelector()));
          this.fireEvent('afterrender');
        }
      }
    },

    clear: function(){
      if(this._redered){
        this.$map.destroy();
        $('#' + _.getId(this.getSelector())).html('');
      }
    },

    rendered: false
  }
});
