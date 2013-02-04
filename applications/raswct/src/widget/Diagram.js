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
 * Defines a widget used as a base for all charts.
 * @module {Rj.widget}
 * @author Alex Dumitru <m.dumitru@jacobs-university.de>
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @version 3.0.0
 */

FlancheJs.defineClass('Rj.widget._Diagram', {

  extends: Rj.widget._OutputWidget,

  /**
   * Class constructor
   * @param selector {string} - A valid cs3 selector where the widget will be displayed.
   * @param [optional] dataSeries {Rj.util.DataSeries} - The series to be plotted.
   */
  init: function(selector, dataSeries){
    if(selector){
      if(_.exists(dataSeries)){
        this._bindSeries(dataSeries);
        this.$dataSeries = [dataSeries];
      }
      Rj.widget._Diagram.prototype.parent.call(this, selector);
    }
  },

  properties: {
    /**
     * The title displayed above the diagram.
     */
    title     : {
      value: Rj.util.Constants.diagramTitle,
      set  : function(title){
        this.$title = title;
        this._refresh();
      }
    },
    /**
     * The label of the x axis.
     */
    xLabel    : {
      value: Rj.util.Constants.diagramXlabel,
      set  : function(xLabel){
        this.$xLabel = xLabel;
        this._refresh();
      }
    },
    /**
     * The label of the y axis.
     */
    yLabel    : {
      value: Rj.util.Constants.diagramYlabel,
      set  : function(yLabel){
        this.$yLabel = yLabel;
        this._refresh();
      }
    },
    /**
     * Indicates whether a Tooltip with tips about how the diagram works should be shown.
     */
    tooltip   : {
      value: Rj.util.Constants.diagramTooltip,
      set  : function(tooltip){
        this.$tooltip = tooltip;
        this._referesh();
      }
    },
    /**
     * The series to be plotted.
     */
    dataSeries: {
      value: [],
      set  : function(dataSeries){
        this.$dataSeries = [dataSeries];
        this._refresh();
      }
    },
    /**
     * The width of the diagram.
     */
    width: {
      value: Rj.util.Constants.diagramWidth,
      set: function(width){
        this.$width = width;
        this._refresh();
      }
    },
    /**
     * The height of the diagram.
     */
    height: {
      value: Rj.util.Constants.diagramHeight,
      set: function(height){
        this.$height = height;
        this._refresh();
      }
    }
  },

  methods: {
    /**
     * Adds a data series to the diagram
     * @param {Rj.util.DataSeries} series - the series object
     */
    addDataSeries: function(series){
      var seriesArray = this.getDataSeries();
      seriesArray.push(series);
      this.setDataSeries(seriesArray);
      this._bindSeries(series);
    },

    /**
     * Removes a data series from the diagram
     * @param {string} seriesName - the name of the data series to be removed
     */
    removeDataSeries: function(seriesName){
      var series = this.getDataSeries();
      for(var i = 0; i < series.length; i++){
        if(series[i].getName() == seriesName.toString()){
          series.splice(i, 1);
        }
      }
      this.setDataSeries(series);
    },

    /**
     * Returns the data series in the format that is sent to the plot
     */
    getData: function(){
      var result = [];
      var series = this.getDataSeries();
      for(var i = 0; i < series.length; i++){
        result.push(series[i].getSeries());
      }
      return result;
    }
  },

  internals: {
    widget: null,

    configure: function(){
      var cfg = {
        title: this.getTitle(),

        axes          : {
          xaxis: {
            label: this.getXLabel()
          },
          yaxis: {
            label: this.getYLabel()
          }
        },
        cursor        : {
          show: true,
          zoom: true
        },
        highlighter   : {
          show: true
        },
        seriesDefaults: {
          rendererOptions: {
            smooth     : true,
            varyByColor: true,
            animation  : {
              show: true
            }
          },
          showMarker     : true
        },
        legend        : {
          show    : true,
          location: 'ne',
          xoffset : 12,
          yoffset : 12
        },
        seriesColors  : this._getSeriesColors()

      };
      return cfg;
    },

    getSeriesColors: function(){
      var series = this.getDataSeries();
      var colors = [];
      var defaultColors = Rj.util.Constants.dataSeriesColors;
      for(var i = 0; i < series.length; i++){
        if(_.exists(series[i].getColor())){
          colors.push(series[i].getColor());
        }
        else{
          colors.push(defaultColors[i]);
        }
      }
      return colors;
    },

    getSeriesNames: function(){
      var series = this.getDataSeries();
      var names = [];
      for(var i = 0; i < series.length; i++){
        if(_.exists(series[i].getName())){
          names.push({label: series[i].getName()});
        }
        else{
          names.push({label: "Series " + (i + 1).toString()});
          //set the series name for making it possible to remove it
          series[i].setName("Series " + (i + 1).toString(), true);
        }
      }
      return names;
    },

    render: function(){
      if(this.getDataSeries().length){
        var self = this;
        this.fireEvent("beforerender");
        var id = _.getId(this.getSelector());
        jQuery("#" + id).html("");
        jQuery("#" + id).height(this.getHeight());
        jQuery("#" + id).width(this.getWidth());
        this._widget = jQuery.jqplot(id, this.getData(), this._configure());
        jQuery.jqplot.preDrawHooks.push(function(plot){
          if(window.gritterZoomMessage !== true && self.getTooltip()){
            // Rj.util._NotificationManager.notify("", "", false);
            window.gritterZoomMessage = true;
          }
        })
        this.fireEvent("afterrender");
      }
    },

    bindSeries: function(series){
      var self = this;
      series.addListener("serieschanged", "serieschanged", function(){
         self._refresh();
      });
      series.addListener("namechanged", "namechanged", function(){
        self._refresh();
      });
      series.addListener("colorchanged", "colorchanged", function(){
        self._refresh();
      });
    }
  }
})

