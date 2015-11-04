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
///<reference path="DCP.ts"/>
///<reference path="Parameter.ts"/>
///<reference path="Constraint.ts"/>
///<reference path="Metadata.ts"/>

module ows {
    export class Operation {
        public Name:string;
        public DCP:DCP[];
        public Parameter:Parameter[];
        public Constraint:Constraint[];
        public Metadata:Metadata[];

        public constructor(source:rasdaman.common.ISerializedObject) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");

            this.Name = source.getAttributeAsString("name");

            this.DCP = [];
            source.getChildrenAsSerializedObjects("ows:DCP").forEach(o=> {
                this.DCP.push(new DCP(o));
            });

            this.Parameter = [];
            source.getChildrenAsSerializedObjects("ows:Parameter").forEach(o=> {
                this.Parameter.push(new Parameter(o));
            });

            this.Constraint = [];
            source.getChildrenAsSerializedObjects("ows:Constraint").forEach(o=> {
                this.Constraint.push(new Constraint(o));
            });

            this.Metadata = [];
            source.getChildrenAsSerializedObjects("ows:Metadata").forEach(o=> {
                this.Metadata.push(new Metadata(o));
            });
        }
    }
}