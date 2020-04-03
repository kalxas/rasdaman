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

  extends: Rj.widget._InputWidget,

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
    min: {
      value: Rj.util.Constants.sliderMin,
      set  : function(min){
        this.$min = min;
        this._refresh();
      }
    },

    /**
     * The max value the slider can take
     */
    max: {
      value: Rj.util.Constants.sliderMax,
      set  : function(max){
        this.$max = max;
        this._refresh();
      }
    },

    /**
     * The orientation of the slider, either vertical or horizontal
     */
    orientation: {
      value: null,
      set  : function(orientation){
        this.$orientation = orientation;
        this._refresh();
      }
    },

    /**
     * If set to true it will create a range slider with two handles
     * The getValue function will now return an array with two elements
     */
    range: {
      value: false,
      set  : function (range) {
        this.$range = range;
        this._refresh();
      }
    },

    /**
     * The step size to which the slider should be increased on slide action
     */
    step: {
      value: Rj.util.Constants.sliderStep,
      set  : function(step){
        this.$step = step;
        this._refresh();
      }
    },

    /**
     * The value of the slider incrementor
     */
    value: {
      value: Rj.util.Constants.sliderMin,
      set  : function(value, doNotRefresh){
        if(!(value instanceof Array)){ // asurre it doesn't exit the bounds
          if(value < this.getMin()) value = this.getMin();
          if(value > this.getMax()) value = this.getMax();
        }
        else{
          value = value.map(function (val){
            if(val < this.getMin()) return this.getMin();
            if(val > this.getMax()) return this.getMax();
            return val;
          }, this)
        }
        this.$value = value;
        this.fireEvent("valuechanged", value);
        if(!doNotRefresh){
          if(value instanceof Array){
            this._jQuerySlider.slider("values", value);
          }
          else{
            this._jQuerySlider.slider("value", value);
          }
        }
      }
    },

    /**
     * True if the slider should have a tooltip, false otherwise
     */
    tooltip: {
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
    tooltip       : null,
    slider        : null,
    sliderId      : null,
    jQuerySliderId: null,
    jQuerySlider  : null,
    /**
     * This is the current value to which the slider points to.
     * Note that this will not always be the same with getValue() as
     * getValue() can react to mouse release events if set so.
     */
    slideValue    : null,
    handleIds     : null,
    mainHandleId  : null,

    /**
     * Creates a tooltip attached to the slider
     */
    createTooltip: function(){
      this._handleIds = _.getId("#" + this._sliderId + " a.ui-slider-handle");
      this._mainHandleId = this._handleIds instanceof Array ? this._handleIds[0] : this._handleIds;
      this._tooltip = new Rj.widget.ToolTip("#" + this._mainHandleId);
      this._tooltip.setPlace(this.getOrientation() == Rj.widget._Slider.Orientation.HORIZONTAL ? "top" : "left");
      if(this.getLabel()){
        this._tooltip.setPreText(this.getLabel() + ": ");
      }
      this._tooltip.setValue(this.getValue());
    },

    /**
     * Update the tooltip with a new value
     * @param value
     */
    updateTooltip: function (value) {
      if (this._tooltip) {
        this._tooltip.setValue(value);
        this._tooltip.show();
      }
    },

    /**
     * Prepares the rendering process for the jqueryui renderer
     */
    prepareRendering: function(){
      //create a slider container
      var id = _.getId(this.getSelector());
      this._sliderId = id + "-slider";
      this._jQuerySliderId = "jquery-" + this._sliderId;
      jQuery("#" + id).html(
        Rj.widget._Slider.sliderHTMLString
          .replace("{orientation}", this.getOrientation())
          .replace("{widthFull}", this.getWidth() + 20)
          .replace("{heightFull}", this.getHeight() + 20)
          .replace("{sliderId}", this._sliderId)
          .replace("{jQuerySliderId}", this._jQuerySliderId)
          .replace("{height}", this.getHeight())
          .replace("{width}", this.getWidth())
      );
      this.fireEvent("beforerender");
    },

    /**
     * Finishing touches to the slider
     */
    finishRendering: function () {
      var self = this;
      //create the incr/decr buttons
      self._createButtons();
      //create the tooltip with a delay of 10ms to be sure that the handler is already rendered
      _.delay(function () {
        self._createTooltip();
        //add a handler so the tooltip shows for the second handle as well if ranged
        if (self.getRange()) {
          jQuery("#" + self._handleIds[1]).on('hover', function () {
            self._tooltip.show();
          });
        }
        self.fireEvent("afterrender");
      }, 10);
      //if ranged user the [min,max] interval for initial values
      if (this.getRange()) {
        this.setValue([this.getMin(), this.getMax()], true)
      }
    },

    /**
     * Creates the buttons for the incrementing and decrementing
     */
    createButtons: function () {
      var self = this;
      //common helper for listeners below
      var update = function (incr) {
        //if ranged apply op on all values
        if (self.getRange()) {
          self._jQuerySlider.slider("values", self.getValue().map(function (value) {
            return value + incr;
          }))
        } else {//else only on one
          self._jQuerySlider.slider("value", self.getValue() + incr)
        }
        self._updateTooltip(self.getValue());
      }

      var decreasing = this.getOrientation() === Rj.widget._Slider.Orientation.HORIZONTAL ? this.getStep() : -1 * this.getStep();
      //make the buttons functional, first decrements, second increments
      jQuery("#" + this._sliderId + " .slider-btn-left ").on('click', function () {
        update(decreasing * -self.getStep());
      });
      jQuery("#" + this._sliderId + " .slider-btn-right ").on('click', function () {
        update(decreasing * self.getStep());
      });
    },

    /**
     * Render the slider using a jQueryUI widget
     */
    renderJquerySlider: function(){
      var self = this;
      var $slider = this._jQuerySlider = jQuery("#" + this._jQuerySliderId);
      var config = {
        orientation: this.getOrientation(),
        min        : this.getMin(),
        max        : this.getMax(),
        step       : this.getStep(),
        value      : this.getValue(),
        range      : this.getRange() ? true : "min",
        slide      : function (event, ui) {
          if (self.getRange()) {
            self._slideValue = ui.values;
            if (self.getInstantChange()) self.setValue(ui.values, true);
            self._updateTooltip(ui.values.toString())
          }
          else {
            self._slideValue = ui.value;
            if (self.getInstantChange()) self.setValue(ui.value, true);
            self._updateTooltip(ui.value.toString())
          }
          self._tooltip.show();
        },
        change     : function (event, ui) {
          if (self.getRange()) {
            self.setValue(ui.values, true);
          }
          else {
            self.setValue(ui.value, true);
          }
        }
      };
      if (this.getRange()) {
        config.values = [this.getMin(), this.getMax()];
      }
      $slider.slider(config);
    },

    /**
     * Renders the slider with all its components
     */
    render: function(){
      this._prepareRendering();
      this._renderJquerySlider();
      this._finishRendering();
    },

    clear: function(){
      jQuery(this.getSelector()).html("");
    },

    refresh: function(){
      this._clear();
      this._render();
    }
  },

  methods: {

  },

  statics: {
    RenderingTimeout: 250,
    Orientation     : {
      HORIZONTAL: "horizontal",
      VERTICAL  : "vertical"
    },
    sliderHTMLString: "<div id='{sliderId}' style='height:{heightFull}px;width:{widthFull}px' class='raswct-slider raswct-{orientation}-slider'> <button class='btn btn-extra-small slider-btn-left' type='button'><i class='icon-chevron-left'></i></button><div id='{jQuerySliderId}' style='height:{height}px;width:{width}px'></div><button class='btn btn-extra-small slider-btn-right' type='button'><i class='icon-chevron-right'></i></button></div>"
  }
});