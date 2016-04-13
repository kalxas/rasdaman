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
 * Defines a widget used for displaying linear diagrams.
 * @module {Rj.widget}
 * @author Alex Dumitru <m.dumitru@jacobs-university.de>
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @version 3.0.0
 */

FlancheJs.defineClass('Rj.widget.LinearDiagram', {

  extends: Rj.widget._Diagram,

  init: function(selector, dataSeries){
    Rj.widget.AreaDiagram.prototype.parent.call(this, selector, dataSeries);
  },

  internals: {
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
          showMarker     : true,
          renderer : jQuery.jqplot.LineRenderer
        },
        legend        : {
          show    : true,
          location: 'ne',
          xoffset : 12,
          yoffset : 12
        },
        seriesColors  : this._getSeriesColors(),
	series: this._getSeriesNames()

      };
      return cfg;
    }
  }
})
