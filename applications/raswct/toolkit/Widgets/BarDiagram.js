/* 
 * Defines an a widget used for displaying bar charts.
 * 
 * @author Alex Dumitru <m.dumitru@jacobs-university.de>
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @package raswct
 * @version 2.0.0
 */

Rj.namespace('Rj.Widget');

Rj.Widget.BarDiagram = new JS.Class(Rj.Widget.BaseChart, {
  
  /**
   * @param <string> title - the title of this diagram
   * @param <string> xAxisTitle - the title of the x Axis
   * @param <string> yAxisTitle - the title of the y Axis
   * @param <array> seriesColors - an array of colors of the series. The colors are assigned in the order given
   */
  initialize: function(title, xAxisTitle, yAxisTitle, seriesColors){
    this.callSuper(title, xAxisTitle, yAxisTitle, seriesColors);
    this.processed = false;
    this.ticks = [];
    $.jqplot.config.enablePlugins = true;
  },
  
  /**
   * Set the ticks for this BarChart. A tick is defined as the label of one bar.
   * @param ticks - e.g. ["Option A", "B", ...]
   */
  setTicks : function(ticks){
    this.ticks = ticks;
  },
  
  configure : function(cfg){
    this.callSuper();
    this.cfg.seriesDefaults = this.cfg.seriesDefaults || {};
    this.cfg.seriesDefaults.renderer = jQuery.jqplot.BarRenderer;    
    this.cfg.seriesDefaults.lineWidth = 1;    
    this.cfg.axes.xaxis.renderer = jQuery.jqplot.CategoryAxisRenderer;
    this.cfg.axes.xaxis.ticks = this.ticks;
    this.cfg.seriesDefaults.pointLabels = {
      show : true
    };
    this.cfg.highlighter={
      show: false
    }
    if(!cfg){
      cfg = {};
    }
    this.cfg = jQuery.extend(this.cfg, cfg);
    return this.cfg
  }
    
});


