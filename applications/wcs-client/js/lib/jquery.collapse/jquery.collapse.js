/*
 * Collapse plugin for jQuery
 * --
 * source: http://github.com/danielstocks/jQuery-Collapse/
 * site: http://webcloud.se/jQuery-Collapse
 *
 * @author Daniel Stocks (http://webcloud.se)
 * Copyright 2013, Daniel Stocks
 * Released under the MIT, BSD, and GPL Licenses.
 */

(function(jQuery) {

  // Constructor
  function Collapse (el, options) {
    options = options || {};
    var _this = this,
      query = options.query || "> :even";

    jQuery.extend(_this, {
      $el: el,
      options : options,
      sections: [],
      isAccordion : options.accordion || false,
      db : options.persist ? jQueryCollapseStorage(el.get(0).id) : false
    });

    // Figure out what sections are open if storage is used
    _this.states = _this.db ? _this.db.read() : [];

    // For every pair of elements in given
    // element, create a section
    _this.$el.find(query).each(function() {
      new jQueryCollapseSection(jQuery(this), _this);
    });

    // Capute ALL the clicks!
    (function(scope) {
      _this.$el.on("click", "[data-collapse-summary] " + (scope.options.clickQuery || ""),
        jQuery.proxy(_this.handleClick, scope));

      _this.$el.bind("toggle close open",
        jQuery.proxy(_this.handleEvent, scope));

    }(_this));
  }

  Collapse.prototype = {
    handleClick: function(e, state) {
      e.preventDefault();
      var state = state || "toggle"
      var sections = this.sections,
        l = sections.length;
      while(l--) {
        if(jQuery.contains(sections[l].$summary[0], e.target)) {
          sections[l][state]();
          break;
        }
      }
    },
    handleEvent: function(e) {
      if(e.target == this.$el.get(0)) return this[e.type]();
      this.handleClick(e, e.type);
    },
    open: function(eq) {
      if(isFinite(eq)) return this.sections[eq].open();
      jQuery.each(this.sections, function(i, section) {
        section.open();
      })
    },
    close: function(eq) {
      if(isFinite(eq)) return this.sections[eq].close();
      jQuery.each(this.sections, function(i, section) {
        section.close();
      })
    },
    toggle: function(eq) {
      if(isFinite(eq)) return this.sections[eq].toggle();
      jQuery.each(this.sections, function(i, section) {
        section.toggle();
      })
    }
  };

  // Section constructor
  function Section($el, parent) {

    if(!parent.options.clickQuery) $el.wrapInner('<a href="#"/>');

    jQuery.extend(this, {
      isOpen : false,
      $summary : $el.attr("data-collapse-summary",""),
      $details : $el.next(),
      options: parent.options,
      parent: parent
    });
    parent.sections.push(this);

    // Check current state of section
    var state = parent.states[this._index()];

    if(state === 0) {
      this.close(true)
    }
    else if(this.$summary.is(".open") || state === 1) {
      this.open(true);
    } else {
      this.close(true)
    }
  }

  Section.prototype = {
    toggle : function() {
      this.isOpen ? this.close() : this.open();
    },
    close: function(bypass) {
      this._changeState("close", bypass);
    },
    open: function(bypass) {
      var _this = this;
      if(_this.options.accordion && !bypass) {
        jQuery.each(_this.parent.sections, function(i, section) {
          section.close()
        });
      }
      _this._changeState("open", bypass);
    },
    _index: function() {
      return jQuery.inArray(this, this.parent.sections);
    },
    _changeState: function(state, bypass) {

      var _this = this;
      _this.isOpen = state == "open";
      if(jQuery.isFunction(_this.options[state]) && !bypass) {
        _this.options[state].apply(_this.$details);
      } else {
        _this.$details[_this.isOpen ? "show" : "hide"]();
      }

      _this.$summary.toggleClass("open", state != "close")
      _this.$details.attr("aria-hidden", state == "close");
      _this.$summary.attr("aria-expanded", state == "open");
      _this.$summary.trigger(state == "open" ? "opened" : "closed", _this);
      if(_this.parent.db) {
        _this.parent.db.write(_this._index(), _this.isOpen);
      }
    }
  };

  // Expose in jQuery API
  jQuery.fn.extend({
    collapse: function(options, scan) {
      var nodes = (scan) ? jQuery("body").find("[data-collapse]") : jQuery(this);
      return nodes.each(function() {
        var settings = (scan) ? {} : options,
          values = jQuery(this).attr("data-collapse") || "";
        jQuery.each(values.split(" "), function(i,v) {
          if(v) settings[v] = true;
        });
        new Collapse(jQuery(this), settings);
      });
    }
  });

  //jQuery DOM Ready
  jQuery(function() {
    jQuery.fn.collapse(false, true);
  });

  // Expose constructor to
  // global namespace
  jQueryCollapse = Collapse;
  jQueryCollapseSection = Section;

})(window.jQuery);
