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
