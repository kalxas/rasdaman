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
 * The GlobalState Singleton provides a common area for defining shared
 * static information across modules
 * @module {Rj.util}
 * @author Alex Dumitru <m.dumitru@jacobs-university.de>
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @version 3.0.0
 */

FlancheJs.defineClass("Rj.util.GlobalState", {
  init   :function () {
    throw Error("This object is a singleton and should not be initialized");
  },
  statics:{
    selectorIdCounter:1
  }
})/*
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
 * This file extends the functionality of the underscore library to utilities that are needed
 * across the project
 * @author Alex Dumitru <m.dumitru@jacobs-university.de>
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @version 3.0.0
 */

_.mixin({
  exists: function(variable){
    return (!_.isUndefined(variable) && !_.isNull(variable));
  },

  arrayEquals: function(arr1, arr2, compareFunction){
    if(!_.exists(compareFunction)){
      compareFunction = function(elem1, elem2){
        return elem1 == elem2;
      }
    }
    if(!_.exists(arr1) || !_.exists(arr2) || !(arr1 instanceof Array) || !(arr2 instanceof Array) ||
      arr1.length != arr2.length){
      return false
    }
    for(var i = 0; i < arr1.length; i++){
      if(compareFunction(arr1[i], arr2[i]) === false){
        return false;
      }
    }
    return true;
  },

  mapToKVP: function(map){
    var kvps = [];
    for(var index in map){
      kvps.push(index.toString() + "=" + map[index].toString());
    }
    return kvps.join("&");
  },

  __prepareAjaxRequest: function(request, config){
    var async = _.exists(config.async) ? config.async : true;
    var error = function(){
      Rj.util.ErrorManager.reportError(Rj.util.Constants.ajaxErrorMessage, true);
    };
    if(_.isFunction(config.error)){
      error = config.error;
    }
    if(!_.isFunction(config.success)){
      throw Error("You must provide an ajax success handler.");
    }
    var url = config.url;
    //if the method is get, add the params to url, reset the post params
    if(config.method === Rj.query.Transport.HttpMethod.GET){
      url += '?' + _.mapToKVP(config.data);
    }
    return {
      method    : config.method,
      url       : url,
      async     : async,
      success   : config.success,
      error     : error,
      dataToSend: _.mapToKVP(config.data)
    }
  },

  ajax: function(config){
    if(config.binary === true){
      _.binaryAjax(config);
    }
    else{
      _.stringAjax(config);
    }
  },

  stringAjax: function(config){
    var request = new XMLHttpRequest();
    var newCfg = _.__prepareAjaxRequest(request, config);
    request.addEventListener("error", newCfg.error, false);
    request.open(newCfg.method, newCfg.url, newCfg.async);
    request.onreadystatechange = function(){
      if(request.readyState == 4){
        newCfg.success.call(request, request.responseText, request.status);
      }
    }
    _.requestDelayer(request, newCfg);
  },

  binaryAjax: function(config){
    var xhr = new XMLHttpRequest();
    var newCfg = _.__prepareAjaxRequest(xhr, config);
    xhr.open(newCfg.method, newCfg.url, newCfg.async);
    xhr.responseType = 'arraybuffer';
    xhr.onload = function(event){
      newCfg.success.call(this, this.response, this.status);
    }
    _.requestDelayer(xhr, newCfg);
  },

  /**
   * Delays the ajax requests
   */
  requestDelayer: function(request, newCfg){
    var arrivalTime = new Date().getTime();
    if(!Rj.util.GlobalState.RjRequestTime){
      Rj.util.GlobalState.RjRequestTime = arrivalTime;
      request.send(newCfg.dataToSend);
    }
    else{
      //some request arrived already
      var timeDiff = arrivalTime - Rj.util.GlobalState.RjRequestTime;
      console.log(timeDiff, Rj.util.ConfigManager.getRequestDelay()-timeDiff);
      if(timeDiff >= Rj.util.ConfigManager.getRequestDelay()){
        request.send(newCfg.dataToSend);
        Rj.util.GlobalState.RjRequestTime = arrivalTime;
      }
      else{
        Rj.util.GlobalState.RjRequestTime = arrivalTime + (Rj.util.ConfigManager.getRequestDelay()-timeDiff);
        setTimeout(function(){
          request.send(newCfg.dataToSend);
        }, (Rj.util.ConfigManager.getRequestDelay() - timeDiff));
      }
    }
  },
  /**
   * Returns the id of a DOM Element. If the element does not
   * have an id, one will be generated for it
   * @param {String} selector any jQuery compatible selector expression
   * @return {String} the id of the elements
   */
  getId: function(selector){
    var id = jQuery(selector).attr('id');
    if(!id){
      var id = "rj-" + Rj.util.GlobalState.selectorIdCounter.toString();
      Rj.util.GlobalState.selectorIdCounter += 1;
      jQuery(selector).attr('id', id);
    }
    return id;
  },

  /**
   * Returns the path to the raswct dir.
   * It requires that raswct.js script name stays unchanged.
   * In case raswct.js name is changed the global variable RASWCT_PATH
   * needs to be manually set.
   */
  getToolkitPath: function(){
    if(_.exists(window.RASWCT_PATH)){
      return window.RASWCT_PATH;
    }
    else{
      if(_.exists(Rj.util.GlobalState.RASWCT_PATH)){
        return Rj.util.GlobalState.RASWCT_PATH;
      }
      var scripts = document.getElementsByTagName('script');
      var path;
      $(scripts).each(function(){
        if($(this).attr('src').match('raswct.js')){
          var src = $(this).attr('src');
          path = src.replace('raswct.js', '');
          return false;
        }
      })
      Rj.util.GlobalState.RASWCT_PATH = path;
      return path;
    }
  },

  arrayBufferToBase64: function(arrayBuffer){
    var base64 = ''
    var encodings = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/'
    var bytes = new Uint8Array(arrayBuffer);
    var byteLength = bytes.byteLength;
    var byteRemainder = byteLength % 3;
    var mainLength = byteLength - byteRemainder;
    var a, b, c, d;
    var chunk;
    // Main loop deals with bytes in chunks of 3
    for(var i = 0; i < mainLength; i = i + 3){
      // Combine the three bytes into a single integer
      chunk = (bytes[i] << 16) | (bytes[i + 1] << 8) | bytes[i + 2]

      // Use bitmasks to extract 6-bit segments from the triplet
      a = (chunk & 16515072) >> 18 // 16515072 = (2^6 - 1) << 18
      b = (chunk & 258048) >> 12 // 258048   = (2^6 - 1) << 12
      c = (chunk & 4032) >> 6 // 4032     = (2^6 - 1) << 6
      d = chunk & 63               // 63       = 2^6 - 1

      // Convert the raw binary segments to the appropriate ASCII encoding
      base64 += encodings[a] + encodings[b] + encodings[c] + encodings[d]
    }

    // Deal with the remaining bytes and padding
    if(byteRemainder == 1){
      chunk = bytes[mainLength]

      a = (chunk & 252) >> 2 // 252 = (2^6 - 1) << 2

      // Set the 4 least significant bits to zero
      b = (chunk & 3) << 4 // 3   = 2^2 - 1

      base64 += encodings[a] + encodings[b] + '=='
    } else if(byteRemainder == 2){
      chunk = (bytes[mainLength] << 8) | bytes[mainLength + 1]

      a = (chunk & 64512) >> 10 // 64512 = (2^6 - 1) << 10
      b = (chunk & 1008) >> 4 // 1008  = (2^6 - 1) << 4

      // Set the 2 least significant bits to zero
      c = (chunk & 15) << 2 // 15    = 2^4 - 1

      base64 += encodings[a] + encodings[b] + encodings[c] + '='
    }

    return base64
  },

  stringToHashCode: function(str){
    var hash = 0, i, char;
    if(str.length != 0){
      for(i = 0; i < str.length; i++){
        char = str.charCodeAt(i);
        hash = ((hash << 5) - hash) + char;
        hash = hash & hash; // Convert to 32bit integer
      }
    }
    return hash;
  },
  getRandomInt    : function(min, max){
    return Math.floor(Math.random() * (max - min + 1)) + min
  }
})/*
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
 * This file contains all the constants needed across the toolkit
 * @module {Rj.util}
 * @author Alex Dumitru <m.dumitru@jacobs-university.de>
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @version 3.0.0
 */

FlancheJs.defineClass("Rj.util.Constants", {
  init   :function () {
    throw Error("This should not be initialized");
  },
  statics:{
    requestDelay: 200,
    errorMessageTitle             :"Error",
    errorMessageButtonText        :"Dismiss",
    serviceUnavailableErrorMessage:"The request was unsuccessful. Please check that the service that was accessed is up and running.",
    serviceErrorMessage:"The request was unsuccessful. The server detected an error. Server Response: ",
    ajaxErrorMessage              :"The ajax request failed. Please contact a developer",
    textRows : 5,
    textCols : 15,
    textSubmitTex : 'Submit',
    wcpsQueryPlaceHolder: "{RaswctQueryPlaceholder}",
    templates                          :{
      wcpsRequestTemplate:'<?xml version="1.0" encoding="UTF-8" ?>\
<ProcessCoveragesRequest xmlns="http://www.opengis.net/wcps/1.0" service="WCPS" version="1.0.0">\
  <query>\
    <abstractSyntax>\
      {RaswctQueryPlaceholder}\
    </abstractSyntax>\
  </query>\
</ProcessCoveragesRequest>'
    },
    knobColors:  [
    '26e000','2fe300','37e700','45ea00','51ef00',
    '61f800','6bfb00','77ff02','80ff05','8cff09',
    '93ff0b','9eff09','a9ff07','c2ff03','d7ff07',
    'f2ff0a','fff30a','ffdc09','ffce0a','ffc30a',
    'ffb509','ffa808','ff9908','ff8607','ff7005',
    'ff5f04','ff4f03','f83a00','ee2b00','e52000'
    ],
    knobMin: 0,
    knobMax: 100,
    knobSnap: 1,
    knobReverse: false,
    sliderVertical: "vertical",
    sliderHorizontal: "horizontal",
    sliderMin: 0,
    sliderMax: 1,
    sliderStep: 1,
    sliderRange: false,
    sliderLabel: '',
    ledValue: 0,
    ledIntDigits: 4,
    ledFracDigits: 2,
    ledDigitClass: "counter-digit",
    ledCounterFieldName: "counter-value",
    ledDigitHeight: 40,
    ledDigitWidth: 30,
    ledImagePath:_.getToolkitPath() + "img/flipCounter-medium.png",
    ledDuration: 5000,
    ledChangeDuration: 300,
    speedoMeterValue: 0,
    speedoMeterLabelSuffix: '',
    speedoMeterImage: _.getToolkitPath() + 'img/jgauge_face_default.png',
    speedoMeterNeedleImage: _.getToolkitPath() + 'img/jgauge_needle_default.png',
    gaugeValue: 0,
    gaugeMin: 0,
    gaugeMax: 100,
    gaugeLabel: ' ',
    gaugeTitle: ' ',
    gaugeWidthScale: 1,
    gaugeTitleColor: '#999999',
    gaugeValueColor: '#999999',
    gaugeLabelColor: '#999999',
    gaugeColor: '#edebeb',
    gaugeShowMinMax: true,
    gaugeShadowOpacity: 1,
    gaugeShadowSize: 0,
    gaugeShadowOffset: 10,
    gaugeHeight: 200,
    toolTipValue: ' ',
    toolTipPretext: ' ',
    toolTipPostext: ' ',
    toolTipAdjust: {},
    toolTipPlace: "bottom",
    toolTipMouse: false,
    toolTipDelay: 1000,
    dataSeriesColors: [ 
    '#0000AA', '#00AA00', '#AA0000', '#A0A0A0', '#CCBBAA',
    '#26e000', '#2fe300', '#37e700', '#45ea00', '#51ef00',
    '#61f800', '#6bfb00', '#77ff02', '#80ff05', '#8cff09',
    '#93ff0b', '#9eff09', '#a9ff07', '#c2ff03', '#d7ff07',
    ],
    diagramTitle: '',
    diagramXlabel: 'X',
    diagramYlabel: 'Y',
    diagramTooltip: false,
    diagramTipTitle: 'Diagram Tip',
    diagramTipText: 'You can restore the zoom level to its initial value by double clicking inside the diagram.',
    barDiagramLineWidth: 1,
    diagramWidth: '600',
    diagramHeight: '300',
    mapWidth: '600',
    mapHeight: '300',
    imgPath: _.getToolkitPath() + 'img/'
  }
})/*
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
 * Interface for event communication
 * @module {Rj.util}
 * @author Alex Dumitru <m.dumitru@jacobs-university.de>
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @version 3.0.0
 */

FlancheJs.defineTrait("Rj.util.Observable", {

  methods:{
    /**
     * Adds a listener to the events fired by this object
     * @param <string> caller - a identifier for the object that is requesting the addon
     * @param <string> event - the name of the event you want to listen to
     * @param <function> callback - a callback function that will be executed once the event is fired
     */
    addListener:function (caller, event, callback) {
      if (!this._listeners[event]) {
        this._listeners[event] = {};
      }
      this._listeners[event][caller] = callback;
    },

    /**
     * Removes a listnener from the list
     * @param <string> caller - @see addListener
     * @param <string> event - @see addListener
     */
    removeListener:function (caller, event) {
      delete(this._listeners[event][caller]);
    },

    /**
     * Fires an event associated with the widgets. All objects that registered for this event
     * will be notified
     * @param <string> event - the name of the event to be fired
     * @param <object> args - any aditional parameters you might pass to the handlers
     */
    fireEvent:function (event, args) {
      var callers = this._listeners[event];
      var status = true;
      for (var callerId in callers) {
        var currentCaller = callers[callerId];
        var currentStatus = currentCaller.call(this, args);
        if (currentStatus === false) {
          status = false;
        }
        else {
          status = (currentStatus !== null && currentStatus !== undefined && currentStatus !== false) ? currentStatus : status;
        }
      }
      return status;
    }
  },

  internals:{
    listeners:{}
  }
  
})
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

})/*
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
 * The ConfigManager class acts as a singleton to store the configuration data used across raswct modules
 * @module {Rj.util}
 * @author Alex Dumitru <m.dumitru@jacobs-university.de>
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @version 3.0.0
 */

FlancheJs.defineClass("Rj.util._ConfigManager", {
  init:function () {

  },

  properties:{
    RasdamanServiceUrl:{
      value:"http://example.org/rasdaman/"
    },
    WCSService        :{
      value:{
        baseUrl:"http://example.org/",
        name   :"WCS",
        version:"2.0.1"
      }
    },
    WCPSService       :{
      value:{
        url           :"http://example.org/wcps/1.0/",
        queryParameter:"request"
      }
    },
    RequestDelay: {
      value: Rj.util.Constants.requestDelay
    }
  }

});

Rj.util.ConfigManager = new Rj.util._ConfigManager();/*
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
 * The ErrorManager is a singleton that manage the error messages, displaying them to the user
 * or just reporting them in the dev console
 * @module {Rj.util}
 * @author Alex Dumitru <m.dumitru@jacobs-university.de>
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @version 3.0.0
 */

FlancheJs.defineClass("Rj.util._ErrorManager", {

  init:function () {

  },

  properties:{
    warnings:{
      value:true
    },
    errors  :{
      value:true
    }
  },

  methods:{
    reportError  :function (errorMsg, throwable) {
      if (!this.getErrors()) {
        return;
      }
      if (throwable) {
        Rj.util.NotificationManager.alert(Rj.util.Constants.errorMessageTitle, errorMsg,
          Rj.util.Constants.errorMessageButtonText, "raswct-error-message");
        throw Error(errorMsg);
      }
      else {
        this._consoleErr(errorMsg);
      }
    },
    reportWarning:function (errorMsg, ui) {
      if (!this.getWarnings()) {
        return;
      }
      Rj.util.NotificationManager.alert(errorMsg);
      this._consoleWarn(err);
    }
  },

  internals:{
    consoleErr:function (err) {
      if (_.exists(window.console)) {
        console.error(err)
      }
    },

    consoleWarn:function (err) {
      if (_.exists(window.console)) {
        console.warn(err)
      }
    }
  }

});

Rj.util.ErrorManager = new Rj.util._ErrorManager();/**
 * @brief Description
 * @author: Alex Dumitru <alex@flanche.net>
 * @package pack
 */

FlancheJs.defineClass("Rj.util._NotificationManager", {

  init:function () {

  },

  properties:{
    enabled:{
      value:true
    }
  },

  methods:{
    disable:function () {
      this.setEnabled(true)
    },
    enable :function () {
      this.setEnabled(false);
    },

    notify:function (title, body, persistent) {
      this._createNotification(title, body, persistent);
    },

    alert:function (title, message, buttonText, extraClasses) {
      buttonText = buttonText || "Ok";
      extraClasses = extraClasses || "";
      this._alert(title, message, buttonText, extraClasses);
    }
  },

  internals:{
    createNotification:function (title, body, persistent) {
      var $ = jQuery;
      // Make it a window property see we can call it outside via updateGrowls() at any point
      var updateGrowls = function () {
        // Loop over each jGrowl qTip
        var each = $('.qtip.jgrowl'),
          width = each.outerWidth(),
          height = each.outerHeight(),
          gap = each.eq(0).qtip('option', 'position.adjust.y'),
          pos;

        each.each(function (i) {
          var api = $(this).data('qtip');

          // Set target to window for first or calculate manually for subsequent growls
          api.options.position.target = !i ? $(window) : [
            pos.left + width, pos.top + (height * i) + Math.abs(gap * (i - 1))
          ];
          api.set('position.at', 'top right');

          // If this is the first element, store its finak animation position
          // so we can calculate the position of subsequent growls above
          if (!i) {
            pos = api.cache.finalPos;
          }
        });
      };

      // Setup our timer function
      var timer = function (event) {
        var api = $(this).data('qtip'),
          lifespan = 5000; // 5 second lifespan

        // If persistent is set to true, don't do anything.
        if (api.get('show.persistent') === true) {
          return;
        }

        // Otherwise, start/clear the timer depending on event type
        clearTimeout(api.timer);
        if (event.type !== 'mouseover') {
          api.timer = setTimeout(api.hide, lifespan);
        }
      }
      // Use the last visible jGrowl qtip as our positioning target
      var target = $('.qtip.jgrowl:visible:last');

      // Create your jGrowl qTip...
      $(document.body).qtip({
        // Any content config you want here really.... go wild!
        content :{
          text :body,
          title:{
            text  :title,
            button:true
          }
        },
        position:{
          my    :'top right',
          // Not really important...
          at    :(target.length ? 'bottom' : 'top') + ' right',
          // If target is window use 'top right' instead of 'bottom right'
          target:target.length ? target : $(window),
          // Use our target declared above
          adjust:{ y:5 },
          effect:function (api, newPos) {
            // Animate as usual if the window element is the target
            $(this).animate(newPos, {
              duration:200,
              queue   :false
            });

            // Store the final animate position
            api.cache.finalPos = newPos;
          }
        },
        show    :{
          event     :false,
          // Don't show it on a regular event
          ready     :true,
          // Show it when ready (rendered)
          effect    :function () {
            $(this).stop(0, 1).fadeIn(400);
          },
          // Matches the hide effect
          delay     :0,
          // Needed to prevent positioning issues
          // Custom option for use with the .get()/.set() API, awesome!
          persistent:persistent
        },
        hide    :{
          event :false,
          // Don't hide it on a regular event
          effect:function (api) {
            // Do a regular fadeOut, but add some spice!
            $(this).stop(0, 1).fadeOut(400).queue(function () {
              // Destroy this tooltip after fading out
              api.destroy();

              // Update positions
              updateGrowls();
            })
          }
        },
        style   :{
          classes:'jgrowl ui-tooltip-dark ui-tooltip-rounded',
          // Some nice visual classes
          tip    :false // No tips for this one (optional ofcourse)
        },
        events  :{
          render:function (event, api) {
            // Trigger the timer (below) on render
            timer.call(api.elements.tooltip, event);
          }
        }
      }).removeData('qtip');

      $(document).delegate('.qtip.jgrowl', 'mouseover mouseout', timer);

    },

    alert:function (title, message, buttonText, extraClasses) {
      var $ = jQuery;
      /*
       * Common dialogue() function that creates our dialogue qTip.
       * We'll use this method to create both our prompt and confirm dialogues
       * as they share very similar styles, but with varying content and titles.
       */
      var dialogue = function (content, title) {
        /*
         * Since the dialogue isn't really a tooltip as such, we'll use a dummy
         * out-of-DOM element as our target instead of an actual element like document.body
         */
        $('<div />').qtip(
          {
            content :{
              text :content,
              title:title
            },
            position:{
              my    :'center', at:'center', // Center it...
              target:$(window) // ... in the window
            },
            show    :{
              ready:true, // Show it straight away
              modal:{
                on  :true, // Make it modal (darken the rest of the page)...
                blur:false // ... but don't close the tooltip when clicked
              }
            },
            hide    :false, // We'll hide it maunally so disable hide events
            style   :'ui-tooltip-light ui-tooltip-rounded ui-tooltip-dialogue raswct-alert-message ' + extraClasses, // Add a few styles
            events  :{
              // Hide the tooltip when any buttons in the dialogue are clicked
              render:function (event, api) {
                $('button', api.elements.content).click(api.hide);
              },
              // Destroy the tooltip once it's hidden as we no longer need it!
              hide  :function (event, api) {
                api.destroy();
              }
            }
          });
      }

      var Alert = function (title, message, buttonText) {
        // Content will consist of the message and an ok button
        var message = $('<p />', { text:message }),
          ok = $('<button />', { text:buttonText, 'class':'full' });
        dialogue(message.add(ok), title);
      }

      Alert(title, message, buttonText);
    }
  }
})

Rj.util.NotificationManager = new Rj.util._NotificationManager();/*
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
 * XMLDoc is a class that provides a series of utility functions for easier parsing of XML docs using XPath
 * @module {Rj.util}
 * @author Alex Dumitru <m.dumitru@jacobs-university.de>
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @version 3.0.0
 */


FlancheJs.defineClass("Rj.util.XMLDoc", {

  init:function (xml) {
    var resXml;
    if ((xml instanceof Document) || (xml instanceof Element)) {
      resXml = xml;
    }
    else {
      var parser = new DOMParser();
      resXml = parser.parseFromString(xml, "application/xml");
    }
    this.setXmlDoc(resXml);
    this._setup();
  },

  properties:{
    xmlDoc:{}
  },

  methods:{
    filter:function (xPath) {
      var doc = this.getXmlDoc();
      var iter = doc.evaluate(xPath, doc, this._resolver, XPathResult.ANY_TYPE, null);
      var resultSet = [];
      do {
        var next = iter.iterateNext();
        if ((next instanceof Document) || (next instanceof Element)) {
          next = new Rj.util.XMLDoc(next);
        }
        resultSet.push(next);
      } while (next != null);
      return resultSet;
    },

    contains:function (xPath) {
      var doc = this.getXmlDoc();
      var iter = doc.evaluate(xPath, doc, this._resolver, XPathResult.ANY_TYPE, null);
      return _.exists(iter.iterateNext())
    },

    toString:function () {
      var serializer = new XMLSerializer();
      return serializer.serializeToString(this.getXmlDoc());
    },

    getTextContents:function () {
      return this.getXmlDoc().textContent;
    }
  },

  internals:{
    resolver:null,

    setup:function () {
      var doc = this.getXmlDoc();
      this._resolver = document.createNSResolver(doc.ownerDocument == null ? doc.documentElement : doc.ownerDocument.documentElement);
    }
  }

})/*
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
 * @class MultiDimArray
 * @description This class is a representation of a multidimensional array that has easy to use accessor methods.
 * @module {Rj.query}
 * @author Alex Dumitru <m.dumitru@jacobs-university.de>
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @version 3.0.0
 * @example var m = new MultiDimArray([{low: 0, high:1}, {low:0, high:1}], [0,1,2,3]);
 * console.log(m.get(0,1)); //prints 1
 * console.log(m.get(0)); //prints new MultiDimArray([{low:0, high:1}], [0,1]);
 * m.set(0,1,42);//array is now [0,42,2,3]
 */

FlancheJs.defineClass("Rj.util.MultiDimArray", {

  /**
   * Constructor for the classe
   * @param {Array} dimensions - the dimension specifications of the array in the following format:
   * [{low: Number, high: Number}, {low: Number, high:Number}, ...]
   * @param {Array} values - the values as a 1d array
   */
  init:function (dimensions, values) {
    this._noOfDims = dimensions.length;
    this._dimensions = dimensions;
    this._values = values;
  },

  methods:{
    /**
     * Accessor for the elements of the array
     * @param {Number} the index for the first dimension
     * @param {Number} the index for the second dimension
     * @param {Number} the index for the n-th dimension
     * @return {Number | Rj.util.MultiDimArray}
     */
    get:function () {
      var result = undefined;
      var args = Array.prototype.slice.call(arguments);
      if (args.length === this._noOfDims) {      //point request
        result = this._getExactPoint(args);
      }
      else if (args.length > 0) {
        var dimSpaces = this._getDimensionality();
        var dimProduct = 1;
        var addedSum = 0;
        var retDims = [];
        for (var i = args.length; i < this._noOfDims; i++) {
          retDims.push(this._dimensions[i]);
        }
        var intermResults = this._getMultiDim(args, []);
        result = new Rj.util.MultiDimArray(retDims, intermResults);
      }
      else {
        return this;
      }
      return result;
    },

    /**
     * Returns a 1D array representation of the MultiDimArray
     * @return {Array}
     */
    getAsNativeArray:function () {
      return this._values;
    },

    /**
     * Returns the dimensions of the MultiDimArray
     * @return {Array}
     */
    getDimensions:function () {
      return this._dimensions;
    },

    /**
     * Determines if the MultiDimArray is equal to another. Equality is defined by equality between their 1D representations
     * if the dimensions are identical.
     * @param {Rj.util.MultiDimArray} the MultiDimArray to compare with.
     * @return {Boolean} true if equal, false otherwise
     */
    equals:function (other) {
      var dimensionsAreEqual = true;
      var otherDimensions = other.getDimensions();
      this._dimensions.forEach(function (item, index) {
        if (!_.exists(otherDimensions[index])) {
          dimensionsAreEqual = false;
        }
        else if (item.low !== otherDimensions[index].low ||
          item.high !== otherDimensions[index].high) {
          dimensionsAreEqual = false;
        }
      });
      return dimensionsAreEqual && _.arrayEquals(this.getAsNativeArray(), other.getAsNativeArray());
    }
  },

  internals:{
    noOfDims         :null,
    values           :null,
    dimensions       :null,
    getDimensionality:function () {
      var dimSpaces = this._dimensions.map(function (elem) {
        return elem.high - elem.low + 1;
      });
      return dimSpaces;
    },
    getExactPoint    :function (args) {
      args = args.reverse();
      var dimSpaces = this._getDimensionality().reverse();
      var offset = args[0];
      for (var i = 1; i < args.length; i++) {
        var dimOff = args[i];
        for (var j = 0; j < i; j++) {
          dimOff *= dimSpaces[j];
        }
        offset += dimOff;
      }
      return this._values[offset];
    },
    getMultiDim      :function (args, marr) {
      var dims = this._getDimensionality();
      if (args.length == dims.length) {
        marr.push(this._getExactPoint(args));
      }
      else {
        var dimension = this._dimensions[args.length];
        for (var i = dimension.low; i <= dimension.high; i++) {
          var newArgs = function (gs, i) {
            var newArgs = ([]).concat(args);
            newArgs.push(i);
            return newArgs;
          }
          marr = this._getMultiDim(newArgs(args, i), marr);
        }
      }
      newArgs = [];
      return marr;
    }
  }

})/*
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
 * A CSVParser object is designed to help with the parsing of CSV data
 * produced by rasdaman server or petascope wcps services to
 * native javascript objects
 * @module {Rj.query}
 * @author Alex Dumitru <m.dumitru@jacobs-university.de>
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @version 3.0.0
 */

FlancheJs.defineClass("Rj.util.CSVParser", {
  init:function (csvString, elementParser) {
    this._csvString = csvString;
    this._elementParser = _.exists(elementParser) ? elementParser : _.identity;
    this.rethrowErrors();
  },

  methods:{
    /**
     * This method checks if any Exceptions were returned and if so reports them
     * as errors through the default mechanism
     */
    rethrowErrors:function () {
      var doc = this._csvString;
      if (doc.search("ows:ExceptionReport") != -1) {
        var xml = new Rj.util.XMLDoc(doc);
        var error = "The server returned an error: " + xml.filter("//ows:ExceptionText")[0].getTextContents();
        Rj.util.ErrorManager.reportError(error, true);
      }
    },
    toOriginalCSVString:function () {
      return this.csvString;
    },

    toNativeJsArray:function () {
      csvArray = this._csvString.replace("{", "")
        .replace("}", "")
        .replace('"', "")
        .split(",")
        .map(this._elementParser);
      return csvArray;
    }
  },

  internals:{
    csvString    :null,
    elementParser:null
  }
})/*
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
 * This singleton acts as a key value store for caching misc data directly
 * in the browser.
 * @author Alex Dumitru <m.dumitru@jacobs-university.de>
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @version 3.0.0
 */

FlancheJs.defineClass("Rj.util._CacheEngine", {

  init: function(){
    this._storage = Lawnchair({name: Rj.util._CacheEngine.MainKey}, function(e){
      //Nothing to do, we just want to initialize the database
    });
  },

  methods: {
    set: function(key, value){
      var serializedValue = this._serialize(value)
      this._storage.save({key: key, value: serializedValue});
    },

    get: function(key, callback){
      var self = this;
      this._storage.get(key, function(keyVal){
        var unserializedValue = null;
        if(_.exists(keyVal)){
          unserializedValue = self._unserialize(keyVal.value);
        }
        callback.call(null, key, unserializedValue);
      });
    },

    exists : function(key, callback){
      this._storage.exists(key, callback);
    },

    remove: function(key){
      this._storage.remove(key);
    },

    clearCache: function(){
      this._storage.nuke();
    }
  },

  internals: {
    storage  : null,
    serialize: function(data){
      var serialized = null;
      if(_.exists(data) && data !== ""){
        serialized = JSON.stringify(data)
      }
      return serialized;
    },

    unserialize: function(data){
      var unserialized = null;
      if(_.exists(data) && data !== ""){
        unserialized = JSON.parse(data);
      }
      return unserialized;
    }
  },

  statics: {
    MainStore: "RaswctCache"
  }

})

Rj.util.CacheEngine = new Rj.util._CacheEngine();/*
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

 /*
 * Defines a layer used as an abstraction for map layers that can be added
 * to any Rj.widget.Map
 * @module {Rj.util}
 * @author Alex Dumitru <m.dumitru@jacobs-university.de>
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @version 3.0.0
 */

FlancheJs.defineClass('Rj.util.MapLayer', {

  /**
   * Constructor for layers
   * @param <string> id - a unique identifier for this layer
   * @param <string> url - an url pointing to an appropriate service(e.g WMS)
   * @param <object> serviceOptions - any parameters to be passed to the service
   * @param <object> options - additional options to be passed to the layer
   */
  init: function(id, url, serviceOptions, options){
    this.$layer = new OpenLayers.Layer.WMS(id, url, serviceOptions, options);
  },

  properties: {
    layer: {
      value: null
    }
  }

});

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
 * Interface for binding objects
 * @module {Rj.util}
 * @author Alex Dumitru <m.dumitru@jacobs-university.de>
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @version 3.0.0
 */

FlancheJs.defineTrait("Rj.util.Bindable", {

  methods: {
    bind: function(){
      if(arguments.length){
        var bindTo = arguments[0];
        var binder = null;
        var args = Array.prototype.slice.call(arguments);
        if(args[args.length - 1] instanceof Function){
          binder = args[args.length - 1];
        }
        else{
          binder = Rj.util.BinderManager.getBinder(this.__meta__.name, bindTo.__meta__.name);
        }

        if(binder){
          binder.apply(null, [this].concat(args));
        }
        else{
          Rj.util.ErrorManager.reportError("In Rj.util.Bindable, bind(): No binders found for the given objects.", true);
        }
      }
      else{
        Rj.util.ErrorManager.reportError("In Rj.util.Bindable, bind(): At least one argument is needed for the binder function.", true);
      }
    }
  }

})
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
 * Manages the binders between Rj objects
 * @module {Rj.util}
 * @author Alex Dumitru <m.dumitru@jacobs-university.de>
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @version 3.0.0
 */

FlancheJs.defineClass("Rj.util._BinderManager", {

  init: function(){

  },

  methods: {
    setBinder: function(name1, name2, fun){
      this._binders[name1 + '_' + name2] = fun;
    },
    getBinder: function(name1, name2){
      return this._binders[name1 + '_' + name2];
    }
  },

  internals: {
    binders: {}
  }

});

Rj.util.BinderManager = new Rj.util._BinderManager();
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
 * Binders
 * @module {Rj.util}
 * @author Alex Dumitru <m.dumitru@jacobs-university.de>
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @version 3.0.0
 */

(function(){

  var internalListenerName = "RaswctInternalBinder";
  /**
   * General binder for queries and input widgets
   * The value of the widget is directly sent to the query
   */
  var queryToInputWidget = function(query, widget, queryVariable){
    query.setVariable(queryVariable, widget.getValue());
//    if(query.isReady()){
//      query.evaluate();
//    }
    widget.addListener(internalListenerName, "valuechanged", function(value){
      query.setVariable(queryVariable, value);
      if(query.isReady()){
        query.evaluate();
      }
    });
  }

  /**
   * Binder for queris and text widgets
   * Query is set to the text in the
   */
  var queryToTextWidget = function(query, widget){
    query.setQuery(widget.getValue());
    if(query.isReady()){
      query.evaluate();
    }
    widget.addListener(internalListenerName, "valuechanged", function(value){
      query.setQuery(value);
      if(query.isReady()){
        query.evaluate();
      }
    });
  }

  /**
   * Binder for image widgets and queries
   */
  var ImageWidgetToQuery = function(widget, query){
    query.evaluate(function(response){
      widget.setBinaryData(response);
    });
    query.addListener(internalListenerName, "evaluated", function(response){
      widget.setBinaryData(response);
    })
  }

  /**
   * Binder for text widgets and queries
   * Text widgets' text is set to the query value
   */
  var TextWidgetToQuery = function(widget, query){
//    query.evaluate(function(response){
//      widget.setValue(response);
//    }, true)
    query.addListener(internalListenerName, "evaluated", function(response){
      widget.setValue(response);
    })
  }

  /**
   * Binder for diagram widgets and wcps queries
   * Diagram's series is set to the query response
   */
  var DiagramToQuery = function(widget, query){
//    query.evaluate(function(response){
//      var parser = new Rj.util.CSVParser(response, function(e){
//        return parseInt(e, 10);
//      });
//      var dataSeries = new Rj.util.DataSeries(parser.toNativeJsArray());
//      widget.setDataSeries(dataSeries);
//    }, true);
    query.addListener(internalListenerName, "evaluated", function(response){
      var parser = new Rj.util.CSVParser(response, function(e){
        return parseInt(e, 10);
      });
      var dataSeries = new Rj.util.DataSeries(parser.toNativeJsArray());
      widget.setDataSeries(dataSeries);
    })
  }

  /**
   * Set the binders
   */

  /**
   * WCPSQuery
   */

  /**
   * WCPSQuery changed by HorizontalSlider
   */
  Rj.util.BinderManager.setBinder('Rj.query.WCPSQuery', 'Rj.widget.HorizontalSlider', queryToInputWidget);

  /**
   * WCPSQuery changed by VerticalSlider
   */
  Rj.util.BinderManager.setBinder('Rj.query.WCPSQuery', 'Rj.widget.VerticalSlider', queryToInputWidget);

  /**
   * WCPSQuery changed by Knob
   */
  Rj.util.BinderManager.setBinder('Rj.query.WCPSQuery', 'Rj.widget.Knob', queryToInputWidget);

  /**
   * WCPSQuery changed by Text
   */
  Rj.util.BinderManager.setBinder('Rj.util.WCPSQuery', 'Rj.widget.Text', queryToTextWidget);

  /**
   * RasQuery
   */

  /**
   * RasQuery changed by HorizontalSlider
   */
  Rj.util.BinderManager.setBinder('Rj.query.RasQuery', 'Rj.widget.HorizontalSlider', queryToInputWidget);

  /**
   * RasQuery changed by VerticalSlider
   */
  Rj.util.BinderManager.setBinder('Rj.query.RasQuery', 'Rj.widget.VerticalSlider', queryToInputWidget);

  /**
   * RasQuery changed by Knob
   */
  Rj.util.BinderManager.setBinder('Rj.query.RasQuery', 'Rj.widget.Knob', queryToInputWidget);

  /**
   * RasQuery changed by Text
   */
  Rj.util.BinderManager.setBinder('Rj.util.RasQuery', 'Rj.widget.Text', queryToTextWidget);


  /**
   * BinaryImage changed by WCPSQuery
   */
  Rj.util.BinderManager.setBinder('Rj.widget.BinaryImage', 'Rj.query.WCPSQuery', ImageWidgetToQuery);

  /**
   * BinaryImage changed by RasQuery
   */
  Rj.util.BinderManager.setBinder('Rj.widget.BinaryImage', 'Rj.query.RasQuery', ImageWidgetToQuery);

  /**
   * TextWidget changed by WCPSQuery
   */
  Rj.util.BinderManager.setBinder('Rj.widget.Text', 'Rj.query.WCPSQuery', TextWidgetToQuery);

  /**
   * LinearDiagram changed by WCPSQuery
   */
  Rj.util.BinderManager.setBinder('Rj.widget.LinearDiagram', 'Rj.query.WCPSQuery', DiagramToQuery);
})()/*
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
 * The transport objects are used by the Executable trait to send the queries to the server to be evaluated
 * @module {Rj.query}
 * @author Alex Dumitru <m.dumitru@jacobs-university.de>
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @version 3.0.0
 * @todo change name to _Transport
 */
FlancheJs.defineClass("Rj.query.Transport", {

  /**
   * Constructor for the Transport function
   * @param {String} serviceUrl - an url to a service that can process the query
   * @param {String} params - an object containing the parameters to the service e.g. {query : "select ..."}
   * @param {Rj.query.Transport.HttpMethod} serviceHttpMethod - the http method e.g. POST / GET
   * @param {Function} parseResponse - a function to parse the response from the server e.g. function(response){return response;}
   */
  init: function(serviceUrl, params, serviceHttpMethod, parseResponse){
    this.setServiceUrl(serviceUrl);
    this.setParams(params);
    if(serviceHttpMethod){
      this.setServiceHttpMethod(serviceHttpMethod);
    }
    if(parseResponse){
      this.setParseResponse(parseResponse);
    }
  },

  statics: {
    /**
     * Common HTTP Methods recognized by the Executable trait
     */
    HttpMethod: {
      POST  : "post",
      GET   : "get",
      PUT   : "put",
      DELETE: "delete"
    }
  },

  methods: {
    toHashCode: function(){
      var str = this.getServiceHttpMethod() + "__" +
        this.getServiceUrl() + "__" +
        this.getBinary() + "__" +
        JSON.stringify(this.getParams());
      return _.stringToHashCode(str);
    }
  },

  properties: {
    serviceUrl       : {},
    serviceHttpMethod: {
      value: "post"
    },
    params           : {},
    parseResponse    : {
      value: function(response){
        return response;
      }
    },
    binary           : {
      value: true
    }
  }

})/*
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
 * The Executable Trait should be used in the composition of any query classes that can
 * be evaluated by a services.
 * @provides {evaluate}
 * @needs {transport : Function} - a function that returns a Rj.query.Transport object.
 * @module {Rj.query}
 * @author Alex Dumitru <m.dumitru@jacobs-university.de>
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @version 3.0.0
 */
FlancheJs.defineTrait("Rj.query.Executable", {

  methods: {
    /**
     * Evaluates the query and calls a corresponding callback function
     * @param {Function} callback - a function to process the response.
     * @param {Boolean} persistent - if set to true the callback will be called each
     * time the query will be re-executed
     * @todo decide if persistent is relevant
     */
    evaluate: function(callback, persistent){
      if(persistent === true){
        this._callbacks.push(callback);
      }
      var self = this;
      var transport = this.transport();
      if(this.getCached() === true){
        this._evaluateCached(transport, callback);
      }
      else{
        this._evaluateRaw(transport, callback);
      }
    }
  },

  properties: {
    cached: {
      value: false
    }
  },

  internals: {
    callbacks    : [],
    fireCallbacks: function(response, httpStatus){
      for(var i = 0; i < this._callbacks.length; i++){
        this._callbacks[i].call(self, response, httpStatus);
      }
    },

    evaluateCached: function(transport, callback){
      var self = this;
      var key = transport.toHashCode();
      Rj.util.CacheEngine.exists(key, function(exists){
        if(exists){
          Rj.util.CacheEngine.get(key, function(key, value){
            if(_.exists(value)){
              //object in cache, apply the callback
              self._handleCallback(callback, value, 200);
            }
          })
        }
        else{
          //Object not in cache, first add the response in cache and then apply callback
          self._evaluateRaw(transport, function(response, httpStatus){
            Rj.util.CacheEngine.set(transport.toHashCode(), response);
            self._handleCallback(callback, response, httpStatus);
          })
        }
      });
    },

    evaluateRaw: function(transport, callback){
      var self = this;
      _.ajax({
        url    : transport.getServiceUrl(),
        method : transport.getServiceHttpMethod(),
        data   : transport.getParams(),
        binary : transport.getBinary(),
        success: function(response, httpStatus){
          if(httpStatus == 404){
            Rj.util.ErrorManager.reportError(Rj.util.Constants.serviceUnavailableErrorMessage, true);
          }
          else if(httpStatus == 500){
            Rj.util.ErrorManager.reportError(Rj.util.Constants.serviceErrorMessage + response, true);
          }
          else{
            self._handleCallback(callback, response, httpStatus);
          }
        },
        error  : function(){
          Rj.util.ErrorManager.reportError(Rj.util.Constants.serviceUnavailableErrorMessage, true);
        }
      });
    },

    handleCallback: function(callback, response, httpStatus){
      //if a callback is provided return response to it
      if(_.exists(callback)){
        callback.call(this, response, httpStatus);
      }
      //otherwise call all registered callbacks
      else{
        this._fireCallbacks(response, httpStatus);
      }
      this.fireEvent("evaluated", response);
    }
  },

  needs: {
    /**
     * Requires a function that returns a transport object
     */
    transport: Function
  }
})



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
 * A GMLParser object is designed to help with the parsing of GML to
 * native javascript objects
 * @module {Rj.query}
 * @author Alex Dumitru <m.dumitru@jacobs-university.de>
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @version 3.0.0
 */

FlancheJs.defineClass("Rj.query.GMLParser", {

  /**
   * Constructor for the function
   * @param {Rj.util.XMLDoc} doc the gml document
   * @param {Function} elemParser a function to parse individual elements from the tuple list
   * if none are provided.
   * @example var parser = new Rj.query.GMLParser(doc, function(element){
   *   components = element.split(" ");
   *   var rgb = {
   *    red : components[0],
   *    green: components[1],
   *    blue: components[2]
   *   }
   *   return rgb;
   * });
   */
  init:function (doc, elemParser) {
    if (doc instanceof Rj.util.XMLDoc) {
      this._doc = doc;
    }
    else {
      this._doc = new Rj.util.XMLDoc(doc);
    }
    if (_.exists(elemParser)) {
      this._elemParser = elemParser;
    }
    else {
      this._elemParser = _.identity;
    }
  },

  methods:{
    /**
     * This method checks if any Exceptions were returned and if so reports them
     * as errors through the default mechanism
     */
    rethrowErrors:function () {
      var doc = this._doc;
      if (doc.contains("ows:ExceptionReport")) {
        var error = "The server returned an error: " + doc.filter("//ows:ExceptionText")[0].getTextContents();
        Rj.util.ErrorManager.reportError(error, true);
      }
    },

    /**
     * Returns the number of dimensions
     * @return {Number}
     */
    getNoOfDimensions:function () {
      var noOfDims = this._doc.filter("//gml:RectifiedGrid/@dimension")[0].value;
      return noOfDims;
    },

    /**
     * Returns the dimensions of the GML coverage as objects of form:
     * {
     *  low : 0,
     *  high: 1,
     * }
     * @return {Array}
     */
    getDimensions:function () {
      var low = this._doc.filter("//gml:RectifiedGrid/gml:limits/gml:GridEnvelope/gml:low")[0].getTextContents().split(" ");
      var high = this._doc.filter("//gml:RectifiedGrid/gml:limits/gml:GridEnvelope/gml:high")[0].getTextContents().split(" ");
      var dims = [];
      for (var i = 0; i < low.length; i++) {
        dims.push({low:parseInt(low[i], 10), high:parseInt(high[i], 10)});
      }
      return dims;
    },

    /**
     * Returns the tuple list element from the GML as a string
     * @return {String}
     */
    getTupleListAsString:function () {
      var ts = this._doc.filter("//gml:tupleList")[0].getTextContents();
      return ts;
    },

    /**
     * Returns the tuple list as a one dimensional native js array
     * with the elements processed
     * @return {Array}
     */
    getTupleListAsNativeArray:function () {
      var ts = this.getTupleListAsString();
      unprocessedTs = ts.split(",");
      if (!_.exists(this._elemParser)) {
        return unprocessedTs;
      }
      var proccessedTs = [];
      for (var i = 0; i < unprocessedTs.length; i++) {
        proccessedTs.push(this._elemParser.call(null, unprocessedTs[i], i));
      }
      return proccessedTs;
    },

    /**
     * Returns the tuple list as a multi dimensional array
     * with the elements processed
     * @return {Rj.util.MultiDimArray}
     */
    getTupleListAsMultiDimArray:function () {
      var ts = this.getTupleListAsNativeArray();
      var dims = this.getDimensions();
      var multiDimArr = new Rj.util.MultiDimArray(dims, ts);
      return multiDimArr;
    }
  },

  internals:{
    doc       :null,
    elemParser:null
  }

})/*
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
 * A literal query trait provides functionality for defining string queries containing parameters that can be changed.
 * e.g "SELECT @col FROM @col WHERE @cond"
 * @module {Rj.query}
 * @author Alex Dumitru <m.dumitru@jacobs-university.de>
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @version 3.0.0
 */
FlancheJs.defineTrait("Rj.query.LiteralQuery", {

  properties:{
    /**
     * @property {String} query - the literal query e.g. "SELECT @col FROM @col"
     */
    query    :{
      value:""
    },
    /**
     * @property {Object} variables - an object of form {variable : value} e.g. {"@col" : "mr"}
     */
    variables:{
      value:{}
    }
  },

  methods:{
    /**
     * Sets a variable to a certain value
     * @param {String} varName
     * @param {String} value
     */
    setVariable:function (varName, value) {
      this.$variables[varName] = value;
    },

    /**
     * Returns a variable's value.
     * @param {String} varName
     * @return {String}
     */
    getVariable:function (varName) {
      return this.getVariables()[varName];
    },

    /**
     * Returns the string representation after the query is expanded by replacing the vars
     * @return {String}
     */
    toString:function () {
      return this._expand();
    },
    /**
     * Indicates if all the variables in the query are set
     * @return {Boolean}
     */
    isReady: function(){
      var variables = this.getVariables();
      for(var variable in variables){
        if(variables[variable] == undefined){
          return false;
        }
      }
      return true;
    }
  },

  internals:{
    expand:function () {
      var query = this.getQuery();
      var variables = this.getVariables();
      for (var variable in variables) {
        if(variables[variable] == undefined){
          Rj.util.ErrorManager.reportError('In Rj.query.LiteralQuery, expand(): Variables must be defined!', true);
        }
        var current = variables[variable].toString();
        query = query.split(variable).join(current);
      }
      return query;
    }
  }
});/*
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
 * A RasQuery is a query to the rasdaman server. All queries defined in rasql can be used with this class.
 * @module {Rj.query}
 * @author Alex Dumitru <m.dumitru@jacobs-university.de>
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @version 3.0.0
 */

FlancheJs.defineClass("Rj.query.RasQuery", {

  /**
   * Constructor for the class
   * @param {String} query - the query string containing one or multiple parameters
   * @param {Array} vars - the query variables (e.g. $domain)
   */
  init: function(query, vars){
    this.setQuery(query);
    if(_.exists(vars)){
      for(var i = 0; i < vars.length; i++){
        this.setVariable(vars[i], undefined);
      }
    }
  },

  implements: [Rj.query.LiteralQuery, Rj.query.Executable],

  methods: {
    /**
     * Implementation of the transport function, needed by {Rj.query.Executable}
     */
    transport: function(){
      var transport = new Rj.query.Transport(
        Rj.util.ConfigManager.getRasdamanServiceUrl(),
        Rj.query.Transport.HttpMethod.POST,
        {
          query: this.toString()
        },
        this._parseResponse
      );
      return transport;
    }
  },

  internal: {
    parseResponse: function(response){
      return JSON.parse(response);
    }
  }

});/*
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
 * A WCPSQuery object can send wcps queries to a service that can process them and parse the result
 * to obtain meaningul data for Widgets.
 * @module {Rj.query}
 * @implements Rj.query.LiteralQuery, Rj.query.Executable
 * @author Alex Dumitru <m.dumitru@jacobs-university.de>
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @version 3.0.0
 */
FlancheJs.defineClass("Rj.query.WCPSQuery", {

  /**
   * Constructor for the class
   * @param query a string query containing 0 or more parameterized variables
   * @param {Array} vars - the query variables (e.g. $domain)
   */
  init: function(query, vars){
    this.setQuery(query);
    if(_.exists(vars)){
      for(var i = 0; i < vars.length; i++){
        this.setVariable(vars[i], undefined);
      }
    }
  },

  implements: [Rj.query.Executable, Rj.query.LiteralQuery, Rj.util.Bindable, Rj.util.Observable],

  properties: {
    /**
     * Should be set to true if the query returns a binary format (e.g. image)
     * instead of UTF-8 / ASCII format
     */
    binaryFormat: {
      value: false
    }
  },

  methods: {
    /**
     * Returns a transport object that can be used internally by
     * the Executable trait
     * @return {Rj.query.Transport}
     */
    transport: function(){
      var params = {};
      var tpl = Rj.util.Constants.templates.wcpsRequestTemplate.replace(Rj.util.Constants.wcpsQueryPlaceHolder, this._expand());
      params[Rj.util.ConfigManager.getWCPSService().queryParameter] = tpl;
      var transport = new Rj.query.Transport(
        Rj.util.ConfigManager.getWCPSService().url,
        params,
        Rj.query.Transport.HttpMethod.POST
      )
      transport.setBinary(this.getBinaryFormat());
      return transport;
    }
  }

})/*
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
 * A GetCoverage object can send WCS getCoverage requests to a service that can process them and parse the result
 * to obtain meaningful data for Widgets.
 * @module {Rj.query}
 * @implements Rj.query.Executable
 * @author Alex Dumitru <m.dumitru@jacobs-university.de>
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @version 3.0.0
 */

FlancheJs.defineClass("Rj.query.wcs.Subset", {

  init:function (dimension, crs, low, high) {
    this.setDimension(dimension);
    this.setCrs(crs);
    this.setLow(low);
    this.setHigh(high);
  },

  properties:{
    dimension:{},
    crs      :{},
    low      :{},
    high     :{}
  },

  methods:{
    transport:function () {
      var dimvs = "(" + this.getLow() + (this.getHigh() ? ("," + this.getHigh()) : "") + ")";
      var transport;
      if (_.exists(this.getCrs())) {
        transport = this.getDimension().toString() + "," + this.getCrs().toString() + dimvs;
      } else {
        transport = this.getDimension().toString() + dimvs;
      }
      return transport;
    },

    equals:function (other) {
      return (
        this.getDimension() == other.getDimension() &&
          this.getCrs() == other.getCrs() &&
          this.getLow() == other.getLow() &&
          this.getHigh() == other.getHigh()
        );
    }
  }
});

FlancheJs.defineClass("Rj.query.wcs.GetCoverage", {

  init:function (coverageId) {
    this.setCoverageId(coverageId);
    this.setSubsets([]);
  },

  implements:[Rj.query.Executable, Rj.util.Observable],

  properties:{
    coverageId:{},
    subsets   :{value:[]}
  },

  methods:{
    transport:function () {
      var params = {
        service   :Rj.util.ConfigManager.getWCSService().name,
        version   :Rj.util.ConfigManager.getWCSService().version,
        request   :"GetCoverage",
        coverageId:this.getCoverageId()
      };
      var subsets = this.getSubsets();
      for(var i =0; i < subsets.length; i++){
        params['subset'+ i.toString()] = subsets[i].transport();
      }
      var transport = new Rj.query.Transport(
        Rj.util.ConfigManager.getWCSService().baseUrl,
        params,
        Rj.query.Transport.HttpMethod.GET
      );
      return transport;
    },

    addSubset:function (subsetDef) {
      this.$subsets.push(subsetDef);
    },

    removeSubset:function (subsetDef) {
      this.$subsets = this.$subsets.filter(function (iterSubset) {
        return !subsetDef.equals(iterSubset);
      })
    }
  }
})/*
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
 * Base class for widgets, exposing methods for easy communication between the current
 * widget and other widgets on the page.
 * All widgets also contain a descendant of BaseQuery, be it SelectQuery or TransportQuery
 * which it can use to receive information from ther server
 * @module {Rj.widget}
 * @author Alex Dumitru <m.dumitru@jacobs-university.de>
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @version 3.0.0
 */

FlancheJs.defineClass("Rj.widget._BaseWidget", {
  
  implements: [Rj.util.Observable, Rj.util.Bindable],

  /**
   * @param <string> selector - a valid CSS3 or xPath selector or any other selectors
   * that work with the jQuery library
   */
  init:function (selector) {
    this.setSelector(selector);
  },

  methods:{
    /**
     * Shows the widget if it is hidden
     */
    show:function () {
      if (this.getSelector() !== null) {
        $(this.getSelector()).show();
      }
    },

    /**
     * Hides the widget
     */
    hide:function () {
      if (this.getSelector() !== null) {
        $(this.getSelector()).hide();
      }
    },

    /**
     * Destroys the widget
     */
    destroy:function () {
      this._clear();
    },

    /**
     * Binds the widget toa Rj.query object
     * @param <Rj.query> query - the query object to which the widget is binded
     */
    bind:function (query) {

    }
  },

  properties:{
    selector:{
      value:null,
      set  :function (selector) {
        this.$selector = selector;
        this.fireEvent("selectorchanged", selector);
        this._refresh();
      }
    }
  },

  internals:{

    /**
     * Placeholder function that should be extended by any showing widget
     */
    render   :function () {
      $(this.getSelector()).html("Loading...");
    },

    /**
     * Removes the widget from its container
     */
    clear:function () {
      $(this.getSelector()).html("");
    },

    /**
     * Removes the widget from the container and re-renders it
     */
    refresh:function () {
      this._clear();
      this._render();
    }
  }
})
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
 * 
 * InputWidget is a simple grouper class that helps better define the 
 * relationships between widgets
 * @module {Rj.widget}
 * @author Alex Dumitru <m.dumitru@jacobs-university.de>
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @version 3.0.0
 */

FlancheJs.defineClass('Rj.widget._InputWidget', {

  extends:Rj.widget._BaseWidget,

  init:function (selector) {
    Rj.widget._InputWidget.prototype.parent.call(this, selector);
  },

  properties:{
    value:{
      value:null,
      set  :function (value) {
        this.$value = value;
        this.fireEvent("valuechanged", value);
        this._refresh();
      }
    }
  }
})


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
 * OutputWidget is a simple grouper class that helps better define the 
 * relationships between widgets
 * @module {Rj.widget}
 * @author Alex Dumitru <m.dumitru@jacobs-university.de>
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @version 3.0.0
 */

FlancheJs.defineClass("Rj.widget._OutputWidget", {
    
    extends : Rj.widget._BaseWidget,
    
    init : function(selector){
      Rj.widget._OutputWidget.prototype.parent.call(this, selector);
    }
    
})

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
 * Defines a knob widget.
 * @module {Rj.widget}
 * @author Alex Dumitru <m.dumitru@jacobs-university.de>
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @version 3.0.0
 */

FlancheJs.defineClass("Rj.widget.Knob", {

  extends: Rj.widget._InputWidget,

  init: function(selector, min, max){
    if(_.exists(min)){
      this.$min = min;
      this.$value = min;
    }
    else{
      this.$value = Rj.util.Constants.knobMin;
    }
    if(_.exists(max)){
      this.$max = max;
    }

    Rj.widget.Knob.prototype.parent.call(this, selector);
    //this._render();
  },

  properties: {
    min    : {
      value: Rj.util.Constants.knobMin,
      set  : function(min){
        this.$min = min;
        this._refresh();
      }
    },
    max    : {
      value: Rj.util.Constants.knobMax,
      set  : function(max){
        this.$max = max;
        this._refresh()
      }
    },
    snap   : {
      value: Rj.util.Constants.knobSnap,
      set  : function(snap){
        this.$snap = snap;
        this._refresh();
      }
    },
    reverse: {
      value: Rj.util.Constants.knobReverse,
      set  : function(reverse){
        this.$reverse = reverse;
        this._refresh();
      }
    }
  },

  internals: {

    /**
     * @override Rj.widget.BaseWidget.render
     */
    render: function(){
      this.fireEvent('beforerender');
      var id = _.getId(this.getSelector());
      var self = this;
      var colors = Rj.util.Constants.knobColors;
      var rad2deg = 180 / Math.PI;
      var deg = 0;
      var bars = $('#' + id);
      bars.addClass('knobBars');
      bars.append("<div id = 'knobControl' class = 'knobControl'></div>");

      for(var i = 0; i < colors.length; i++){
        deg = i * 12;
        // Create the colorbars
        $('<div class="colorBar">').css({
          backgroundColor: '#' + colors[i],
          transform      : 'rotate(' + deg + 'deg)',
          top            : -Math.sin(deg / rad2deg) * 80 + 100,
          left           : Math.cos((180 - deg) / rad2deg) * 80 + 100
        }).appendTo(bars);
      }
      $('<div class="knobLabel">').css({
        padding: '200px 0 0 100px'
      }).appendTo(bars);
      $('.knobLabel').html(self.getValue());
      var colorBars = bars.find('.colorBar');
      var numBars = 0, lastNum = -1;
      if(self.getReverse()){
        var transVal = 360 - Math.round((self.getValue() - self.getMin()) * 360 / (self.getMax() - self.getMin())) % 360;
      }
      else{
        transVal = Math.round((self.getValue() - self.getMin()) * 360 / (self.getMax() - self.getMin())) % 360;
      }
      $('#knobControl').knobKnob({
        snap : self.getSnap(),
        value: transVal,
        turn : function(ratio){
          numBars = Math.round(colorBars.length * ratio);
          this.value = Math.round(ratio * 360);
          var value;
          if(self.getReverse()){
            value = self.getMax() - Math.round(this.value * (self.getMax() - self.getMin()) / 360);
          }
          else{
            value = Math.round(this.value * (self.getMax() - self.getMin()) / 360) + self.getMin();
          }
          $('.knobLabel').html(value);
          // Update the dom only when the number of active bars
          // changes, instead of on every move			
          if(numBars == lastNum){
            return false;
          }
          lastNum = numBars;
          colorBars.removeClass('active').slice(0, numBars).addClass('active');
          self._setValue(value);
          //set value when releasing
//          $("#" + id).mouseup(function(){
//            self._setValue(value);
//          })
        }
      });
      self.fireEvent('afterrender');
    },

    setValue: function(value){
      this.$value = value;
      this.fireEvent("valuechanged", this.getValue());
    }
  }

})/*
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
 * Defines a widget which allows the user to input text into a textarea.
 * @module {Rj.widget}
 * @author Alex Dumitru <m.dumitru@jacobs-university.de>
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @version 3.0.0
 */

FlancheJs.defineClass('Rj.widget.Text', {

  extends:Rj.widget._InputWidget,

  init:function (selector, value) {
    this.$value = value;
    Rj.widget.Text.prototype.parent.call(this, selector);
  },

  properties:{

    rows      :{
      value:Rj.util.Constants.textRows,
      set  :function (rows) {
        this.$rows = rows;
        this._refresh();
      }
    },
    cols      :{
      value:Rj.util.Constants.textCols,
      set  :function (cols) {
        this.$cols = cols;
        this._refresh();
      }
    },
    submitText:{
      value:Rj.util.Constants.textSubmitTex,
      set  :function (submitText) {
        this.$submitText = submitText;
        this._refresh();
      }
    },
    label     :{
      value:"",
      set  :function (submitText) {
        this.$label = submitText;
        this._refresh();
      }
    }
  },

  internals:{

    /**
     * @override Rj.widget.BaseWidget.render
     */
    render:function () {
      var id = _.getId(this.getSelector());

      //prepare the html for rendering
      var htmlStr = '<form id="form-horizontal' + id + '"><div class="control-group">';
      htmlStr += this._createLabel();
      htmlStr += this._createTextArea();
      htmlStr += this._createSubmitButton();
      htmlStr += "</div></form>"
      this._addSubmitListener();


      //render the html
      this.fireEvent('beforerender');
      jQuery('#' + id).html(htmlStr);
      this.fireEvent('afterrender');
    },

    /**
     * Creates the html for the textarea
     * @return {String}
     */
    createTextArea:function () {
      var id = _.getId(this.getSelector());
      var htmlStr = '<div class="controls"><textarea class="" id="textarea-' + id +
        '" rows = "' + this.getRows() + '" cols = "' + this.getCols() + '">';
      htmlStr += _.exists(this.getValue()) ? this.getValue() : "";
      htmlStr += '</textarea></div>';
      return htmlStr;
    },

    /**
     * Adds a label to the the textarea if one is set
     * @return {String}
     */
    createLabel:function () {
      var id = _.getId(this.getSelector());
      var htmlStr = "";
      if (this.getLabel() != "") {
        htmlStr = '<label for="textarea-' + id + '" class="' + this._genericClasses + ' raswct-widget-text-label control-label">' +
          this.getLabel() + '</label>';
      }
      return htmlStr;
    },

    /**
     * Adds the submit button html
     * @return {String}
     */
    createSubmitButton:function () {
      var id = _.getId(this.getSelector());
      var htmlStr = '<input class="' + this._genericClasses + ' raswct-widget-text-submit btn" type = "submit" value = "' +
        this.getSubmitText() + '" id = "textarea-' + id + '-submit" />';
      return htmlStr;
    },

    /**
     * Listener for the submit action
     */
    addSubmitListener:function () {
      var id = _.getId(this.getSelector());
      var self = this;
      jQuery("#textarea-" + id + '-submit').click(function (event) {
        this.setValue(jQuery("#textarea-" + id).val());
        event.preventDefault();
      })
    },

    /**
     * Generic classes to be added to the HTML elements
     */
    genericClasses:"raswct raswct-widget raswct-widget-text"
  }
})

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
 * Defines a SpeedoMeter widget.
 * @module {Rj.widget}
 * @author Alex Dumitru <m.dumitru@jacobs-university.de>
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @version 3.0.0
 */

FlancheJs.defineClass('Rj.widget.SpeedoMeter', {

  extends:Rj.widget._OutputWidget,

  init:function (selector) {
    Rj.widget.SpeedoMeter.prototype.parent.call(this, selector);
  },

  properties:{
    value      :{
      value:Rj.util.Constants.speedoMeterValue,
      set  :function (value) {
        this.$value = value;
        if (_.exists(this._gauge)) {
          this._gauge.setValue(value);
        }
        this.fireEvent("valuechange", value);
      }
    },
    labelSuffix:{
      value:Rj.util.Constants.speedoMeterLabelSuffix,
      set  :function (labelSuffix) {
        this.$labelSuffix = labelSuffix;
        this._refresh();
      }
    }
  },

  internals:{
    gauge:null,

    render:function () {
      this.fireEvent("beforerender");
      var id = _.getId(this.getSelector());
      $("#" + id).addClass("jgauge");
      this._gauge = new jGauge();
      this._gauge.imagePath = Rj.util.Constants.speedoMeterImage;
      this._gauge.needle.imagePath = Rj.util.Constants.speedoMeterNeedleImage;
      this._gauge.label.suffix = this.getLabelSuffix();
      this._gauge.id = id;
      this._gauge.init();
      this._gauge.setValue(this.getValue());
      this.fireEvent("afterrender");
    }
  }

})
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
 * Defines a Gauge widget.
 * @module {Rj.widget}
 * @author Alex Dumitru <m.dumitru@jacobs-university.de>
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @version 3.0.0
 */

FlancheJs.defineClass('Rj.widget.Gauge', {

  extends:Rj.widget._OutputWidget,

  init:function (selector, min, max) {
    if(_.exists(min)){
      this.$min = min;
    }
    if(_.exists(max)){
      this.$max = max;
    }
    Rj.widget.Gauge.prototype.parent.call(this, selector);
  },

  properties:{
    value     :{
      value:Rj.util.Constants.gaugeValue,
      set  :function (value) {
        this.$value = value;
        if (this._gauge) {
          this._gauge.refresh(value);
        }
        this.fireEvent("valuechanged", value);
      }
    },
    min       :{
      value:Rj.util.Constants.gaugeMin,
      set  :function (min) {
        this.$min = min;
        this._refresh();
      }
    },
    max       :{
      value:Rj.util.Constants.gaugeMax,
      set  :function (max) {
        this.$max = max;
        this._refresh();
      }
    },
    title     :{
      value:Rj.util.Constants.gaugeTitle,
      set  :function (title) {
        this.$title = title;
        this._refresh();
      }
    },
    label     :{
      value:Rj.util.Constants.gaugeLabel,
      set  :function (label) {
        this.$label = label;
        this._refresh();
      }
    },
    widthScale:{
      value:Rj.util.Constants.gaugeWidthScale,
      set  :function (widthScale) {
        this.$widthScale = widthScale;
        this._refresh();
      }
    },
    showMinMax:{
      value:Rj.util.Constants.gaugeShowMinMax,
      set  :function (showMinMax) {
        this.$showMinMax = showMinMax;
        this._refresh();
      }
    }
  },

  internals:{
    gauge:null,

    render:function () {
      this.fireEvent("beforerender");
      var id = _.getId(this.getSelector());
      //height needs to be specified, if not set default
      if(!$("#" + id).height()){
        $("#" + id).height(Rj.util.Constants.gaugeHeight);
      }
      //making sure width/height ration is kept
      var width = $("#" + id).css("width");
      $("#" + id).css("height", width * 16 / 20);
      //rendering the gauge
      this._gauge = new JustGage({
        id                  :id,
        value               :this.getValue(),
        min                 :this.getMin(),
        max                 :this.getMax(),
        title               :this.getTitle(),
        titleFontColor      :Rj.util.Constants.gaugeTitleColor,
        valueFontColor      :Rj.util.Constants.gaugeValueColor,
        showMinMax          :this.getShowMinMax(),
        gaugeWidthScale     :this.getWidthScale(),
        gaugeColor          :Rj.util.Constants.gaugeColor,
        labelFontColor      :Rj.util.Constants.gaugeLabelColor,
        label               :this.getLabel(),
        shadowOpacity       :Rj.util.Constants.gaugeShadowOpacity,
        shadowSize          :Rj.util.Constants.gaugeShadowSize,
        shadowVerticalOffset:Rj.util.Constants.gaugeShadowOffset
      });
      this.fireEvent("afterrender");
    }
  }
})
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
 * Defines a tooltip widget
 * @module {Rj.widget}
 * @author Mircea Dumitru <m.dumitru@jacobs-university.de>
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @version 3.0.0
 */

FlancheJs.defineClass('Rj.widget.ToolTip', {

  extends : Rj.widget._OutputWidget,

  init : function(selector){
    Rj.widget.ToolTip.prototype.parent.call(this, selector);
  },

  properties : {
    value   : {
      value : Rj.util.Constants.toolTipValue,
      set   : function(value){
        this.$value = value;
        if(this._tip){
          this._tip.qtip('option', 'content.text', this.getPretext() + value);
        }
        this.fireEvent("valuechanged", value);
      }
    },
    pretext : {
      value : Rj.util.Constants.toolTipPretext,
      set   : function(pretext){
        this.$pretext = pretext;
        this._refresh();
      }
    },
    postext : {
      value : Rj.util.Constants.toolTipPostext,
      set   : function(postext){
        this.$postext = postext;
        this._refresh();
      }
    },
    adjust  : {
      value : {},
      set   : function(adjust){
        this.$adjust = adjust;
        this._refresh();
      }
    },
    place   : {
      value : Rj.util.Constants.toolTipPlace,
      set   : function(place){
        this.$place = place;
        this._refresh();
      }
    },
    mouse   : {
      value : Rj.util.Constants.toolTipMouse,
      set   : function(mouse){
        this.$mouse = mouse;
        this._refresh();
      }
    },
    delay   : {
      value : Rj.util.Constants.toolTipDelay,
      set   : function(delay){
        this.$delay = delay;
        this._refresh();
      }
    }
  },

  internals : {
    tip : null,

    render : function(){
      this.fireEvent("beforerender");
      var id = _.getId(this.getSelector());
      this._tip = $('#' + id);
      var target = this.getMouse() ? 'mouse' : $('#' + id);
      this._tip.qtip({
        content  : this.getPretext() + this.getValue() + this.getPostext(),
        position : {
          my     : this.getPlace() + ' center',
          at     : 'top center',
          target : target,
          adjust : this.getAdjust()
        },
        hide     : {
          delay : this.getDelay()
        },
        style    : 'qtip-shadow'
      });
      this.fireEvent("afterrender");
    },

    clear: function(){
      this._qtip = null;
    }
  }
})/*
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
});/*
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
 * Defines a horizontal slider that can be used to slide through an interval
 * of numerical values.
 * @module {Rj.widget}
 * @author Alex Dumitru <m.dumitru@jacobs-university.de>
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @version 3.0.0
 */

FlancheJs.defineClass('Rj.widget.HorizontalSlider', {

  extends: Rj.widget._Slider,

  /**
   * Constructor for the slider widget
   * @param {String} selector any CSS3 or XPath selector
   * @param {Number} min the minimum value the slider can take
   * @param {Number} max the maximum value the slider can take
   * @see Rj.widget._Slider
   */
  init: function(selector, min, max){
    this.$height = Rj.widget.HorizontalSlider.HorizontalDefaultHeight
    Rj.widget.HorizontalSlider.prototype.parent.call(this,
      selector,
      Rj.widget._Slider.Orientation.HORIZONTAL,
      min, max);
  },

  statics : {
    HorizontalDefaultHeight : 10
  }
})/*
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
 * Defines a VerticalSlider that can be used to slide through
 * an interval of numerical values
 * @module {Rj.widget}
 * @author Alex Dumitru <m.dumitru@jacobs-university.de>
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @version 3.0.0
 */

FlancheJs.defineClass('Rj.widget.VerticalSlider', {

  extends: Rj.widget._Slider,
  /**
   * Constructor for the widget
   * @param {String} selector any CSS3 or XPath selector
   * @param {Number} min the minimum value the slider can take
   * @param {Number} max the maximum value the slider can take
   * @see Rj.widget._Slider
   */
  init: function(selector, min, max){
    this.$width = Rj.widget.VerticalSlider.VerticalDefaultWidth;
    Rj.widget.VerticalSlider.prototype.parent.call(this,
      selector,
      Rj.widget._Slider.Orientation.VERTICAL,
      min, max);
  },

  statics : {
    VerticalDefaultWidth: 10
  }

})
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
 * Defines a binary image widget that can consume uint8 data and transform it into
 * a image that can be displayed in the browser.
 * @module {Rj.widget}
 * @author Alex Dumitru <m.dumitru@jacobs-university.de>
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @version 3.0.0
 */

FlancheJs.defineClass("Rj.widget.BinaryImage", {

  extends: Rj.widget._OutputWidget,

  /**
   * Class constructor
   * @param {String} selector - A valid cs3 selector.
   * @param {String} format - The data format (e.g. 'image/png').
   * @param {ArrayBuffer} arrayBufData - The data stream.
   */
  init: function(selector, format, arrayBufData){
    this._generateBase64Data(arrayBufData)
    this._format = format;
    Rj.widget.BinaryImage.prototype.parent.call(this, selector);
  },

  properties: {
    width : {
      value: null,
      set  : function(width){
        this.$width = width;
        this._refresh();
      }
    },
    height: {
      value: null,
      set  : function(height){
        this.$height = height;
        this._refresh();
      }
    },
    binaryData : {
      value : null,
      set : function(data){
        this.$binaryData = data;
        this._generateBase64Data(this.$binaryData);
        this._refresh();
      }
    }
  },

  internals: {
    base64Data: null,
    format    : null,
    generateBase64Data : function(arrayBufferData){
      this._base64Data = _.arrayBufferToBase64(arrayBufferData);
    },
    render    : function(){
      var id = _.getId(this.getSelector());
      var style = "style=\"";
      if(this.getWidth()){
        style += "width: " + this.getWidth() + "px;";
      }
      if(this.getHeight()){
        style += "height: " + this.getHeight() + "px;";
      }
      style += '"';
      this.fireEvent("beforerender");
      jQuery("#" + id).append('<img src="data:' + this._format + ';base64,' + this._base64Data + '" ' + style + ' />');
      this.fireEvent("afterrender");
    }
  }
})
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
 * Defines an a widget used for displaying bar charts.
 * @module {Rj.widget}
 * @author Alex Dumitru <m.dumitru@jacobs-university.de>
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @version 3.0.0
 */

FlancheJs.defineClass("Rj.widget.BarDiagram", {

  extends: Rj.widget._Diagram,

  init: function(selector, dataSeries){
    Rj.widget.BarDiagram.prototype.parent.call(this, selector, dataSeries);
  },


  internals: {

    labeled: false,

    configure: function(){
      var cfg = {
        title: this.getTitle(),

        axes          : {
          xaxis: {
            label   : this.getXLabel(),
            renderer: jQuery.jqplot.CategoryAxisRenderer
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
          show: false
        },
        seriesDefaults: {
          rendererOptions: {
            smooth     : true,
            varyByColor: true,
            animation  : {
              show: true
            },
            fillToZero : true
          },
          showMarker     : true,
          renderer       : jQuery.jqplot.BarRenderer,
          lineWidth      : Rj.util.Constants.barDiagramLineWidth,
          pointLabels    : {
            show: true
          }
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
});

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
 * Defines a widget used for displaying area diagrams.
 * @module {Rj.widget}
 * @author Alex Dumitru <m.dumitru@jacobs-university.de>
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @version 3.0.0
 */

FlancheJs.defineClass('Rj.widget.AreaDiagram', {

  extends: Rj.widget._Diagram,

  /**
   * Class constructor
   * @param selector <string> - A valid cs3 selector where the widget will be displayed.
   * @param [optional] dataSeries <Rj.util.DataSeries> - The series to be plotted.
   */
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
          renderer : jQuery.jqplot.LineRenderer,
          fill: true
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
    }
  }
})
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
        seriesColors  : this._getSeriesColors()

      };
      return cfg;
    }
  }
})
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
 * Defines a widget used for displaying scatter diagrams.
 * @module {Rj.widget}
 * @author Alex Dumitru <m.dumitru@jacobs-university.de>
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @version 3.0.0
 */

FlancheJs.defineClass('Rj.widget.ScatterDiagram', {

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
          renderer       : jQuery.jqplot.LineRenderer,
          showLine       : false
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
    }
  }
})
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
 * Defines a Led Display widget.
 * @module {Rj.widget}
 * @author Alex Dumitru <m.dumitru@jacobs-university.de>
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @version 3.0.0
 */

FlancheJs.defineClass('Rj.widget.Led', {

  extends:Rj.widget._OutputWidget,

  init:function (selector) {
    Rj.widget.Led.prototype.parent.call(this, selector);
  },

  properties:{
    value     :{
      value:Rj.util.Constants.ledValue,
      set  :function (value) {
        var oldValue = this.$value;
        this.$value = value;
        this.fireEvent("valuechanged", value);
        var id = _.getId(this.getSelector());
        jQuery("#" + id).flipCounter("startAnimation", {
          number: oldValue,
          end_number: value,
          duration: Rj.util.Constants.ledChangeDuration
        });
      }
    },
    intDigits :{
      value:Rj.util.Constants.ledIntDigits,
      set  :function (intDigits) {
        this.$intDigits = intDigits;
        this._refresh();
      }
    },
    fracDigits:{
      value:Rj.util.Constants.ledFracDigits,
      set  :function (fracDigits) {
        this.$fracDigits = fracDigits;
        this._refresh();
      }
    }
  },

  internals:{
    render:function () {
      this.fireEvent("beforerender");
      var id = _.getId(this.getSelector());
      jQuery("#" + id).flipCounter({
        number             :this.getValue(), // the initial number the counter should display, overrides the hidden field
        numIntegralDigits  :this.getIntDigits(), // number of places left of the decimal point to maintain
        numFractionalDigits:this.getFracDigits(), // number of places right of the decimal point to maintain
        digitClass         :Rj.util.Constants.ledDigitClass, // class of the counter digits
        counterFieldName   :Rj.util.Constants.ledCounterFieldName, // name of the hidden field
        digitHeight        :Rj.util.Constants.ledDigitHeight, // the height of each digit in the flipCounter-medium.png sprite image
        digitWidth         :Rj.util.Constants.ledDigitWidth, // the width of each digit in the flipCounter-medium.png sprite image
        imagePath          :Rj.util.Constants.ledImagePath, // the path to the sprite image relative to your html document
        easing             :jQuery.easing.easeOutCubic, // the easing function to apply to animations, you can override this with a jQuery.easing method
        duration           :Rj.util.Constants.ledDuration, // duration of animations
        onAnimationStarted :false, // call back for animation upon starting
        onAnimationStopped :false, // call back for animation upon stopping
        onAnimationPaused  :false, // call back for animation upon pausing
        onAnimationResumed :false // call back for animation upon resuming from pause
      });
      this.fireEvent("afterrender");
    }
  }
})
