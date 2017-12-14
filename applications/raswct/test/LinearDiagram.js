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
 * Testing Suite for LinearDiagram widgets
 * @see Rj.widget.LinearDiagram
 * @author Alex Dumitru <m.dumitru@jacobs-university.de>
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @version 3.0.0
 */

buster.testCase("Rj.widget.LinearDiagram tests", {
  setUp:function () {
    $("body").append("<div id='linear-widget'></div>");
  },

  "LinearDiagram widget ok":function () {
/**
 
408 KB & 0.133 & 0.089 & 1.5x\\ \hline
3.2 MB & 0.396 & 0.209 & 1.89x\\ \hline
6.2 MB & 0.923 & 0.450 & 2.11x\\ \hline
12.8 MB & 1.661 & 0.781 & 2.12x\\ \hline
52 MB & 15.89 & 6.85 & 2.31x\\ \hline
106 MB & 28.60  & 12.27 & 2.33x\\ \hline
211 MB & 54.34  & 23.42 & 2.32x\\ \hline
403 MB & 140.2  & 62.88 & 2.32x\\ \hline
1.4 GB & 462.66  & 193.58 & 2.39x\\ \hline
2.1 GB & 693.99  & 287.96 & 2.41x
**/
  var series = new Rj.util.DataSeries([ [2.61, 1.5], [3.5, 1.89], [3.8, 2.11], [4.1, 2.12], [4.71, 2.31],[5.02, 2.33],[5.32, 2.32],[5.6, 2.32],[6.14,2.39],[6.32, 2.41]]);
  series.setName("Performance improvement/array size");
    var widget = new Rj.widget.BarDiagram("#linear-widget", series);
    buster.assert(true);
  },

  tearDown:function () {
    //$("#text-widget").remove();
  }
})



