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
    },
    submitButton: {
      value: true,
      set  : function (submitButton) {
        this.$submitButton = submitButton;
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
      if (this.getSubmitButton()) {
        htmlStr += this._createSubmitButton();
      }
      htmlStr += "</div></form>";


      //render the html
      this.fireEvent('beforerender');
      jQuery('#' + id).html(htmlStr);
      this._addSubmitListener();
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
      var htmlStr = '<button class="' + this._genericClasses + ' raswct-widget-text-submit btn btn-primary" type = "submit" value = "' +
        this.getSubmitText() + '" id = "textarea-' + id + '-submit" >'+this.getSubmitText()+'</button>';
      return htmlStr;
    },

    /**
     * Listener for the submit action
     */
    addSubmitListener:function () {
      var id = _.getId(this.getSelector());
      var self = this;
      jQuery("#textarea-" + id + '-submit').off('click.raswct');
      jQuery("#textarea-" + id + '-submit').on('click.raswct', function (event) {
        event.preventDefault();
        self.setValue(jQuery("#textarea-" + id).val());
        self.fireEvent("submitted", self.getValue());
      })
    },

    /**
     * Generic classes to be added to the HTML elements
     */
    genericClasses:"raswct raswct-widget raswct-widget-text"
  }
})

