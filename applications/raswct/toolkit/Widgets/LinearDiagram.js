/* 
 * Defines an a widget used for displaying linear graphs.
 * 
 * @author Alex Dumitru <m.dumitru@jacobs-university.de>
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @package raswct
 * @version 1.0.0
 */

Rj.namespace('Rj.Widget');

Rj.Widget.LinearDiagram = new JS.Class(Rj.Widget.BaseChart, {
  
  /**
   * @param <string> title - the title of this diagram
   * @param <string> xAxisTitle - the title of the x Axis
   * @param <string> yAxisTitle - the title of the y Axis
   * @param <array> seriesColors - an array of colors of the series. The colors are assigned in the order given
   */
  initialize: function(title, xAxisTitle, yAxisTitle, seriesColors){
    this.callSuper(title, xAxisTitle, yAxisTitle, seriesColors);
    this.processed = false;
  },
  
  configure : function(cfg){
    this.callSuper();
    this.cfg.seriesDefaults = this.cfg.seriesDefaults || {};
    this.cfg.seriesDefaults.renderer = jQuery.jqplot.LineRenderer;    
    this.cfg.seriesDefaults.lineWidth = 1;    
    
    if(!cfg){
      cfg = {};
    }
    
    this.cfg = jQuery.extend(this.cfg, cfg);
    return this.cfg
  }
    
});
