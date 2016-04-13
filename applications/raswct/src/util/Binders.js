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
})()