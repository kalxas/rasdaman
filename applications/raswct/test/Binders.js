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
 * Testing Suite for Binders
 * @see Rj.util.Bindable, Rj.util.BinderManager, Rj.util.Binders
 * @author Alex Dumitru <m.dumitru@jacobs-university.de>
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @version 3.0.0
 */

buster.testCase("Binder tests", {
  setUp: function(){
    $("body").append('<div id="slider"></div>');
    $("body").append('<div id="slider2"></div>');
    $("body").append('<div id="text"></div>');
    $("body").append('<div id="text2"></div>');
    $("body").append('<div id="diagram"></div>');
    $("body").append('<div id="diagram2"></div>');
    $("body").append('<div id="diagram3"></div>');
    Rj.util.ConfigManager.setWCPSService({
      url           : "http://flanche.net:8080/petascope/",
      queryParameter: "request"
    })
  },

  "Binder for WCPS ok": function(){
    var slider = new Rj.widget.HorizontalSlider("#slider", 124, 1000);
    var slider2 = new Rj.widget.VerticalSlider("#slider2", 124, 2000)
    slider.setWidth(200);
    slider2.setHeight(200);
    var text = new Rj.widget.Text("#text", "yahoo");
    var text2 = new Rj.widget.Text("#text2");
    var query = new Rj.query.WCPSQuery('for t1 in (mean_summer_airtemp) return encode (t1[ x($x:$y) ], "csv")', ['$x', '$y']);
    query.bind(slider, '$x');
    query.bind(slider2, '$y');
    text.bind(query, function(text, query){
      text.setValue(query.toString());
      query.addListener("myapp","evaluated", function(response){
        text.setValue(query.toString());
      });
    });
    text2.bind(query);
    var diagram = new Rj.widget.LinearDiagram('#diagram');
    diagram.bind(query);
    var diagram2 = new Rj.widget.LinearDiagram('#diagram2');
    diagram2.bind(query);
    var query2 = new Rj.query.WCPSQuery('for t1 in (mean_summer_airtemp) return encode (t1[ x(126:126) ], "csv")');
    var diagram3 = new Rj.widget.LinearDiagram('#diagram3');
    diagram3.bind(query2);
    query.evaluate();
    query.evaluate();
    query.evaluate();
    query2.evaluate()
    buster.assert(true);
  }
})