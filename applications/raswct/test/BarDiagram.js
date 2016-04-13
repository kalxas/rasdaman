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
 * Testing Suite for BarDiagram widgets
 * @see Rj.widget.BarDiagram
 * @author Alex Dumitru <m.dumitru@jacobs-university.de>
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @version 3.0.0
 */

buster.testCase("Rj.widget.BarDiagram tests", {
  setUp:function () {
    $("body").append("<div id='bar-widget'></div>");
  },

  "BarDiagram widget ok":function () {
    var dataSeries = new Rj.util.DataSeries([[4,1],[5,2]]);
    var widget = new Rj.widget.BarDiagram("#bar-widget", dataSeries);
    //widget.setXLabel("Laala");
    //widget.setYLabel("Bla");
    //dataSeries.setName("vasile");
    //var sSeries = new Rj.util.DataSeries([4,5,6])
    //widget.addDataSeries(sSeries);
    //widget.addDataSeries(sSeries);

    //dataSeries.setSeries([1,2,3,4]);
    console.log(dataSeries);
    //sSeries.setName("bazinga");
    buster.assert(true);
  },

  tearDown:function () {
    //$("#text-widget").remove();
  }
})



