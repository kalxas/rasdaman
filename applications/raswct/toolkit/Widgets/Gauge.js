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
 * @class Gauge extends OutputWidget
 *
 * Defines a Gauge widget.
 *
 * @author Mircea Dumitru <m.dumitru@jacobs-university.de>
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @package raswct
 * @version 1.0
 */

Rj.namespace('Rj.Widget');

Rj.Widget.Gauge = new JS.Class(Rj.Widget.OutputWidget, {
    
    /**
     * Standard class constructor
     * @param <BaseQuery> query - the query that this widget will be able to modify
     * @param <int> value - the initial value displayed
     * @param <string> labelSuffix - the string displayed after the label value
     * @param <bool> taco - sets a custom display
     */
    initialize: function(query, value, labelSuffix, taco ){
        this.query = query;
        this.taco = taco || false;
        this.labelSuffix = labelSuffix || "";
        this.id = '';
        this.value = value || 0;
        this.initGauge = null;
        this.callSuper();
    },
    
    /**
     * Getter for the value attribute
     */
    getValue: function(){
        return this.value;
    },
    
    /**
     * Setter for the value attribute
     * @param <float> value - the new value of the counter, with 2 digits precision
     * @event gaugechange - fires when the values of the display changes
     */
    setValue: function(value){
        this.value = value;  
        //this.fireEvent("gaugechange", this.value);
        if(this.initGauge){
            this.initGauge.setValue(value);
        }
    },
    
    /**
     * @override Rj.Widget.BaseWidget.renderTo
     */
    renderTo: function(selector){
        this.id = selector;
        $("#" + this.id).addClass("jgauge");
        this.initGauge = new jGauge();
        if(this.taco){
            this.initGauge.label.suffix = this.labelSuffix; 
            this.initGauge.autoPrefix = autoPrefix.si; // Use SI prefixing (i.e. 1k = 1000).
            this.initGauge.imagePath = '../../../' + TOOLKIT_PATH + 'raswct/bin/img/jgauge_face_taco.png';
            this.initGauge.segmentStart = -225
            this.initGauge.segmentEnd = 45
            this.initGauge.width = 170;
            this.initGauge.height = 170;
            this.initGauge.needle.imagePath = '../../../' + TOOLKIT_PATH + 'raswct/bin/img/jgauge_needle_taco.png';
            this.initGauge.needle.xOffset = 0;
            this.initGauge.needle.yOffset = 0;
            this.initGauge.label.yOffset = 55;
            this.initGauge.label.color = '#fff';
            this.initGauge.label.precision = 0; // 0 decimals (whole numbers).
            this.initGauge.ticks.labelRadius = 45;
            this.initGauge.ticks.labelColor = '#0ce';
            this.initGauge.ticks.start = 200;
            this.initGauge.ticks.end = 800;
            this.initGauge.ticks.count = 7;
            this.initGauge.ticks.color = 'rgba(0, 0, 0, 0)';
            this.initGauge.range.color = 'rgba(0, 0, 0, 0)';
        }
        else{
            this.initGauge.label.suffix =this.labelSuffix;
        }
        this.initGauge.id = this.id;
        this.initGauge.init();
        this.initGauge.setValue(this.value);
    }
    
})