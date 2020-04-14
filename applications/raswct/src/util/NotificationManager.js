/**
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

    notify: function (cfg) {
      this._createNotification(cfg);
    },

    alert: function (title, message, type) {
      this._alert(title, message, type);
    }
  },

  internals: {
    createNotification: function (cfg) {
      var $ = jQuery;
      $.pnotify({
        title  : cfg.title,
        text   : cfg.body,
        hide   : !cfg.persistent,
        icon   : 'icon-envelope',
        styling: 'bootstrap',
        type   : cfg.type || 'info',
        delay  : 7000,
        hide: true
      })
    },

    alert: function (title, message, type) {
      var $ = jQuery;
      $.pnotify({
        title  : title,
        text   : message,
        type   : type,
        hide   : false,
        icon   : 'icon-exclamation-sign',
        styling: 'bootstrap',
        delay  : 7000,
        hide: true
      });
    }
  }
})

Rj.util.NotificationManager = new Rj.util._NotificationManager();
