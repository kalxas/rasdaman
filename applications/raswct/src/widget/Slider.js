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
 * Defines a slider widget. This class is private and should be instatiated on its
 * own. Please see Rj.widget.HorizontalSlider and Rj.widget.VerticalSlider if you
 * need to create a slider
 * @module {Rj.widget}
 * @author Alex Dumitru <m.dumitru@jacobs-university.de>
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @version 3.0.0
 */

FlancheJs.defineClass("Rj.widget._Slider", {

  extends   : Rj.widget._InputWidget,

  /**
   * Constructor for the slider widget
   * @param {String} selector any CSS3 or XPath selector
   * @param {Rj.widget._Slider.Orientation} orientation VERTICAL or HORZIONTAL
   * @param {Number} min the minimum value the slider can take
   * @param {Number} max the maximum value the slider can take
   * @param {Number} step the step size of each slide action
   */
  init: function(selector, orientation, min, max, step){
    if(!selector){
      //Do not do anything
      //This is needed because JS calls empty constructors
      //to set the prototypal chain
    }
    else{
      this.$orientation = orientation;
      this.$min = min || 0;
      this.$max = max || 100;
      this.$step = step || 1;
      this.$value = this._slideValue = min;
      Rj.widget._Slider.prototype.parent.call(this, selector);
      this._render();
    }
  },

  properties: {
    /**
     * The min value the slider can take
     */
    min          : {
      value: Rj.util.Constants.sliderMin,
      set  : function(min){
        this.$min = min;
        this._refresh();
      }
    },
    /**
     * The max value the slider can take
     */
    max          : {
      value: Rj.util.Constants.sliderMax,
      set  : function(max){
        this.$max = max;
        this._refresh();
      }
    },
    /**
     * The orientation of the slider, either vertical or horizontal
     */
    orientation  : {
      value: null,
      set  : function(orientation){
        this.$orientation = orientation;
        this._refresh();
      }
    },
    /**
     * The step size to which the slider should be increased on slide action
     */
    step         : {
      value: Rj.util.Constants.sliderStep,
      set  : function(step){
        this.$step = step;
        this._refresh();
      }
    },
    /**
     * The value of the slider incrementor
     */
    value        : {
      value: Rj.util.Constants.sliderMin,
      set  : function(value, doNotRefresh){
        this.$value = value;
        this.fireEvent("valuechanged", value);
        if(!doNotRefresh){
          this._refresh();
        }
      }
    },
    /**
     * True if the slider should have a tooltip, false otherwise
     */
    tooltip      : {
      value: true
    },
    /**
     * The label shown in the tooltip
     */
    label        : {
      value: Rj.util.Constants.sliderLabel,
      set  : function(label){
        this.$label = label;
        this._refresh();
      }
    },
    /**
     * The height of the slider
     */
    height       : {
      value: 100,
      set  : function(height){
        this.$height = height;
        this._refresh();
      }
    },
    /**
     * The width of the slider
     */
    width        : {
      value: 100,
      set  : function(width){
        this.$width = width;
        this._refresh();
      }
    },
    /**
     * If true the slider will react (change the value) to the slide movement of the incrementor,
     * otherwise it will react only to the mouse up movement
     */
    instantChange: {
      value: false,
      set  : function(instantChange){
        this.$instantChange = instantChange;
        this._refresh();
      }
    }
  },

  internals: {
    tooltip              : null,
    slider               : null,
    rendered             : false,
    //below private values needed to deal with the async way dojo does its rendering
    isRendering          : false,
    softRefreshInProgress: false,
    sliderId             : null,
    /**
     * This is the current value to which the slider points to.
     * Note that this will not always be the same with getValue() as
     * getValue() can react to mouse release events if set so.
     */
    slideValue           : null,
    /**
     * Creates a tooltip attached to the slider
     */
    createTooltip        : function(){
      var id = _.getId(this.getSelector());
      this._tooltip = new Rj.widget.ToolTip("#" + id + ' .dijitSliderImageHandle');
      this._tooltip.setValue(this.getValue().toString());
      if(this.getLabel()){
        this._tooltip.setPretext(this.getLabel() + ": ");
      }
      this._tooltip.setDelay(Rj.widget._Slider.TooltipDelay);
      var self = this;
      jQuery('.dijitSliderIncrementIconH, .dijitSliderDecrementIconH, .dijitSliderIncrementIconV, .dijitSliderDecrementIconV').click(function(){
        self._tooltip.show();
      });
      this._tooltip.setValue(this.getValue().toString());
    },

    /**
     * Prepares the rendering process for the dojoRenderer
     */
    prepareRendering: function(){
      //create a slider container
      var id = _.getId(this.getSelector());
      this._sliderId = id + "-slider";
      jQuery("#" + id).append("<div id=\"" + this._sliderId + '"></div>');
      //add the dojo theme class to the body
      jQuery("body").addClass(Rj.widget._Slider.DojoInternalThemeClass);
      this._isRendering = true;
      this.fireEvent("beforerender");
    },

    /**
     * Finishing touches to the slider
     */
    finishRendering: function(){
      //create the tooltip associated with the slider
      this._createTooltip();
      //Let the other methods know that refresh is possible now
      this._rendered = true;
      this._isRendering = false;
      this.fireEvent("afterrender");
    },

    /**
     * Renders the slider using the dojo library widget
     */
    renderDojoSlider: function(){
      var self = this;
      require([
        "dojo/ready",
        "dijit/form/" + self._getDojoClass()
      ], function(ready, slider){
        var context = {}
        context[self._getDojoClass()] = slider;
        ready(context, function(){
          self._slider = new this[self._getDojoClass()]({
            name               : "slider",
            value              : self.getValue(),
            minimum            : self.getMin(),
            maximum            : self.getMax(),
            discreteValues     : parseInt(self.getMax() - self.getMin() / self.getStep()) + 1,
            intermediateChanges: true,
            style              : "width:" + self.getWidth() + "px; height: " + self.getHeight() + "px;",
            onChange           : function(value){
              self._tooltip.setValue(value);
              self.fireEvent("slided", value);
              self._slideValue = value;
              //if the slider should be updated on slide set value here
              //otherwise set it on mouse up
              if(self.getInstantChange()){
                self.setValue(value, true);
              }
            },
            onMouseUp          : function(){
              if(!self.getInstantChange()){
                self.setValue(self._slideValue, true);
              }
            }
          }, self._sliderId);
          self._finishRendering();
        });
      });
    },

    /**
     * Renders the slider with all its components
     */
    render: function(){
      this._prepareRendering();
      this._renderDojoSlider();
    },

    getDojoClass: function(){
      return Rj.widget._Slider.DojoSliderClasses[this.getOrientation()];
    },

    clear: function(){
      this._slider.destroyRecursive();
      jQuery(this.getSelector()).html("");
    },

    refresh: function(){
      if(!this._softRefreshInProgress){
        if(this._isRendering){
          var self = this;
          this._softRefreshInProgress = true;
          setTimeout(function(){
            self._softRefresh();
          }, Rj.widget._Slider.RenderingTimeout)
        }
        if(this._rendered){
          this._softRefresh();
        }
      }
    },

    softRefresh: function(){
      this._clear();
      this._render();
      this._softRefreshInProgress = false;
    }
  },

  methods: {

  },

  statics: {
    DojoInternalThemeClass: "claro",
    TooltipDelay          : 1000,
    RenderingTimeout      : 250,
    Orientation           : {
      HORIZONTAL: "horizontal",
      VERTICAL  : "vertical"
    },
    DojoSliderClasses     : {
      "horizontal": "HorizontalSlider",
      "vertical"  : "VerticalSlider"
    }
  }
});