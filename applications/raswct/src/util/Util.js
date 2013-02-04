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
})