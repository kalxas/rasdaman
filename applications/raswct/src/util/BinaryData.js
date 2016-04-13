/**
 * @brief Description
 * @author: Alex Dumitru <alex@flanche.net>
 * @package pack
 */

FlancheJs.defineClass("Rj.util.BinaryLoader", {
  init     : function(transport, callback){
    this._base64Resource;
  },
  internals: {
    sendRequest: function(){
      var xhr = new XMLHttpRequest();
      xhr.open('GET', 'http://www.celticfc.net/images/doc/celticcrest.png', true);
      xhr.responseType = 'arraybuffer';
      xhr.onload = function(e){
        if(this.status == 200){
          var uInt8Array = new Uint8Array(this.response); // Note:not xhr.responseText
          for(var i = 0, len = uInt8Array.length; i < len; ++i){
            uInt8Array[i] = this.response[i];
          }
          var byte3 = uInt8Array[4]; // byte at offset 4
        }
      }

      xhr.send();
    }
  }
});

