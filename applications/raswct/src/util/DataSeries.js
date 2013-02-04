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
 * A DataSeries object is an discrete indexed array object, similar to how a 2D diagram is represented
 * e.g. [[1, 2], [3, 6]] where the domain is {1,3} and the codomain is {2,6}
 * @module {Rj.util}
 * @author Alex Dumitru <m.dumitru@jacobs-university.de>
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @version 3.0.0
 */

FlancheJs.defineClass("Rj.util.DataSeries", {

  implements : [Rj.util.Observable],

  /**
   * Class constructor.
   * @param <array> series - Can be of 2 types:
   * 1. DataSeries series array (e.g. [[1,2], [3, 6]] where the domain is {1,3} and the codomain is {2,6}).
   * 2. Unidimensional array (e.g. [4,9,10] where the domain is implicitely {0,1,2} and the codomain {4,9,10}).
   */
  init : function(series){
    if(_.exists(series)){
      if(!(series instanceof Array)){
        var error = "In Rj.Util.DataSeries, init(): ";
        error += "The given series must be an array!";
        Rj.util.ErrorManager.reportError(error, true);
      }
      else{
        if(series[0] instanceof Array){
          //we are on case 1
          var newSeries = series;
        }
        else{
          //we are on case 2
          var i = -1; //counter for positions
          newSeries = series.map(function(element){
            i++;
            return [i, element];
          });
        }
        this.setSeries(newSeries);
      }
    }
  },

  properties : {
    name   : {
      set : function(name, doNotFireEvent){
        this.$name = name;
        if(!doNotFireEvent){
          this.fireEvent("namechanged", name);
        }
      }
    },
    color  : {
      set : function(color){
        this.$color = color;
        this.fireEvent("colorchanged", color);
      }
    },
    series : {
      value : [],
      set   : function(series){
        this.$series = series;
        this.fireEvent("serieschanged", series);
      }
    }
  },

  internals : {
    getRealIndex : function(x){
      var series = this.getSeries();
      if(series.length){
        for(var i = 0; i < series.length; i++){
          if(series[i][0] == x){
            return i;
          }
        }
      }
      return null;
    },
    getNextIndex : function(){
      var series = this.getSeries();
      if(series.length){
        return series[series.length - 1][0] + 1;
      }
      return 0;
    }
  },

  methods : {
    pointAt     : function(x){
      var series = this.getSeries();
      for(var i = 0; i < series.length; i++){
        if(series[i][0] == x){
          return series[i][1];
        }
      }
      return undefined;
    },
    addPoint    : function(x, y){
      var series = this.getSeries();
      if(_.exists(y)){
        //remove point if already exists
        if(this.pointAt(x) != undefined){
          this.removePoint(x);
        }
        series.push([x, y]);
        this.setSeries(series);
      }
      else{
        series.push([this._getNextIndex(), x]);
        this.setSeries(series);
      }
    },
    removePoint : function(x){
      //check if point exists
      if(this.pointAt(x) != undefined){
        var index = this._getRealIndex(x);
        var series = this.getSeries();
        series.splice(index, 1);
        this.setSeries(series);
      }
      else{
        Rj.util.ErrorManager.reportError("In Rj.util.DataSeries, removePoint(): Point at index " + x + " doesn't exist. " +
          "Nothing removed.", false);
      }
    }
  }

})