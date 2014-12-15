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
 * Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009,2010,2011,2012,2013,2014 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */

define(function () {
    function DataRecord(json, isKVP) {
        if (isKVP) {

        } else {
            this.field = [];
            this.quantity = [];
            this.code = [];
            for (var i = 0; json.field && i < json.field.length; i++) {
                this.field.push(json.field[i]._attr.name._value);
                if (json.field[i].Quantity
                    && json.field[i].Quantity[0]
                    && json.field[i].Quantity[0]._attr
                    && json.field[i].Quantity[0]._attr.definition) {

                    this.quantity.push(json.field[i].Quantity[0]._attr.definition._value);

                } else if (json.field[i].Quantity
                    && json.field[i].Quantity[0]
                    && json.field[i].Quantity[0].uom
                    && json.field[i].Quantity[0].uom[0]._attr
                    && json.field[i].Quantity[0].uom[0]._attr.code) {

                    this.code.push(json.field[i].Quantity[0].uom[0]._attr.code._value);
                }
            }
        }
    }

    return DataRecord;
});