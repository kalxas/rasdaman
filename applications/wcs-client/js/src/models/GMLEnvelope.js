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
    function GMLEnvelope(json, isKVP) {
        if (isKVP) {

        } else {
            if (json._attr) {
                this.srsName = json._attr.srsName ? json._attr.srsName._value : null;
                this.srsDimension = json._attr.srsDimension ? parseInt(json._attr.srsDimension._value) : null;

                this.axisLabels = [];
                if (json._attr.axisLabels && json._attr.axisLabels._value) {
                    this.axisLabels = json._attr.axisLabels._value.match(/\S+/g);
                }

                this.uomLabels = [];
                if (json._attr.uomLabels && json._attr.uomLabels._value) {
                    this.uomLabels = json._attr.uomLabels._value.match(/\S+/g);
                }

                this.lowerCorner = [];
                this.upperCorner = [];

                if (json.lowerCorner && json.upperCorner) {
                    this.lowerCorner = json.lowerCorner[0]._text.match(/\S+/g);
                    this.upperCorner = json.upperCorner[0]._text.match(/\S+/g);
                    for(var i=0; i<this.lowerCorner.length; i++){
                        this.lowerCorner[i]=parseFloat(this.lowerCorner[i]);
                        this.upperCorner[i]=parseFloat(this.upperCorner[i]);
                    }
                }
            }
        }
    }

    return GMLEnvelope;
});