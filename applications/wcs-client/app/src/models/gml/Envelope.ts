/*
 * This file is part of rasdaman community.
 *
 * Rasdaman community is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Rasdaman community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2014, 2015 Peter Baumann /
 rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */

///<reference path="../../common/_common.ts"/>
///<reference path="Pos.ts"/>

///<reference path="LowerCorner.ts"/>
///<reference path="UpperCorner.ts"/>


module gml {
    /**
     * Extend this class so that it fully complies with the OGC GML specification if the need arises.
     */
    export class Envelope {
        public SrsName:string;
        public SrsDimension:Number;
        public AxisLabels:string[];
        public UomLabels:string[];
        public Frame:string;
        public Pos:Pos;
        public LowerCorner:LowerCorner;
        public UpperCorner:UpperCorner;

        public constructor(source:rasdaman.common.ISerializedObject) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");

            if (source.doesAttributeExist("srsName")) {
                this.SrsName = source.getAttributeAsString("srsName");
            }

            if (source.doesAttributeExist("srsDimension")) {
                this.SrsDimension = source.getAttributeAsNumber("srsDimension");
            }

            if (source.doesAttributeExist("axisLabels")) {
                this.AxisLabels = source.getAttributeAsString("axisLabels").split(" ");
            }

            if (source.doesAttributeExist("uomLabels")) {
                this.UomLabels = source.getAttributeAsString("uomLabels").split(" ");
            }

            if (source.doesAttributeExist("frame")) {
                this.Frame = source.getAttributeAsString("frame");
            }

            if (source.doesElementExist("gml:lowerCorner")) {
                this.LowerCorner = new LowerCorner(source.getChildAsSerializedObject("gml:lowerCorner"));
            }

            if (source.doesElementExist("gml:upperCorner")) {
                this.UpperCorner = new UpperCorner(source.getChildAsSerializedObject("gml:upperCorner"));
            }

            if (source.doesElementExist("gml:pos")) {
                this.Pos = new Pos(source.getChildAsSerializedObject("gml:pos"));
            }
        }
    }
}
