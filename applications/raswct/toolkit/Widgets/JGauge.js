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
 * @class JGauge extends OutputWidget
 *
 * Defines a JGauge widget.
 *
 * @author Mircea Dumitru <m.dumitru@jacobs-university.de>
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @package raswct
 * @version 1.0
 */

Rj.namespace('Rj.Widget');

Rj.Widget.JGauge = new JS.Class(Rj.Widget.OutputWidget, {
    
    /**
     * Standard class constructor
     * @param <BaseQuery> query - the query that this widget will be able to modify
     * @param <string> title - the title displayed above the gauge
     * @param <string> label - the label displayed under the gauge
     * @param <int> min - the starting point
     * @param <int> max - the ending point
     * @param <bool> showMinMax - determines whether the starting and ending points are displayed as numbers
     * @param <int> value - the initial value of the gauge
     * @param <float> width - the scale of the width of the gauge (1 - default width. e.g. 0.2 is 20% of the default width) 
     * @param <bool> shadow - adds a shadow effect to the gauge. Defaults to true.
     * @param <string> color - the hex color of the gauge
     * @param <string> titleColor - the hex color of the title
     * @param <string> valueColor - the hex color of the value
     * @param <string> labelColor - the hex color of the label
     */
    initialize: function(query, title, label, min, max, showMinMax, value, width, shadow, color, titleColor, valueColor, labelColor){
        this.callSuper();
        this.query = query;
        this.title = title || '';
        this.label = label || '';
        this.min = min || 0;
        this.max = max || 100;
        this.width = width || 1;
        this.color = color || '#edebeb';
        this.titleColor = titleColor || '#999999';
        this.valueColor = valueColor || '#999999';
        if(showMinMax == undefined){
            this.showMinMax = true;
        }
        else{
            this.showMinMax = showMinMax;
        }
        this.labelColor = labelColor || '#999999';
        this.id = '';
        this.value = value || 0;
        if(shadow == undefined){
            this.shadow = true;
        }
        else{
            this.shadow = shadow;
        }
        this.initGauge = null;
    },
    
    /**
     * Getter for the value attribute
     */
    getValue: function(){
        return this.value;
    },
    
    /**
     * Setter for the value attribute
     * @param <int> value - the new value of the gauge
     * @event jgaugechange - fires when the values of the gauge changes
     */
    setValue: function(value){
        this.value = value;
        if(this.initGauge){
            this.initGauge.refresh(value);
        }
      //  this.fireEvent("jgaugechange", this.value);
    },
    
    /**
     * @override Rj.Widget.BaseWidget.renderTo
     */
    renderTo: function(selector){
        this.id = selector;
        //making sure width/height ration is kept
        var width = $("#" + this.id).css("width");
        $("#" + this.id).css("height", width*16/20);
        //rendering the gauge
        if(this.shadow){
            this.initGauge = new JustGage({
                id: this.id,
                value: this.value,
                min: this.min,
                max: this.max,
                title: this.title,
                titleFontColor: this.titleColor,
                valueFontColor: this.valueColor,
                showMinMax: this.showMinMax,
                gaugeWidthScale: this.width,
                gaugeColor: this.color,
                labelFontColor: this.labelColor,
                label: this.label,
                shadowOpacity: 1,
                shadowSize: 0,
                shadowVerticalOffset: 10           
            });
        }
        else{
            this.initGauge = new JustGage({
                id: this.id,
                value: this.value,
                min: this.min,
                max: this.max,
                title: this.title,
                titleFontColor: this.titleColor,
                valueFontColor: this.valueColor,
                showMinMax: this.showMinMax,
                gaugeWidthScale: this.width,
                gaugeColor: this.color,
                labelFontColor: this.labelColor,
                label: this.label
            });
        }
    }
    
})