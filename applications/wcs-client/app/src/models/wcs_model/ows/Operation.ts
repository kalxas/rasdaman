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
 * Copyright 2003 - 2017 Peter Baumann /
 rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */

///<reference path="../../../common/_common.ts"/>
///<reference path="DCP.ts"/>
///<reference path="Parameter.ts"/>
///<reference path="Constraint.ts"/>
///<reference path="Metadata.ts"/>

module ows {
    export class Operation {
        public name:string;
        public dcp:DCP[];
        public parameter:Parameter[];
        public constraint:Constraint[];
        public metadata:Metadata[];

        public constructor(source:rasdaman.common.ISerializedObject) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");

            this.name = source.getAttributeAsString("name");

            this.dcp = [];
            source.getChildrenAsSerializedObjects("ows:DCP").forEach(o=> {
                this.dcp.push(new DCP(o));
            });

            this.parameter = [];
            source.getChildrenAsSerializedObjects("ows:Parameter").forEach(o=> {
                this.parameter.push(new Parameter(o));
            });

            this.constraint = [];
            source.getChildrenAsSerializedObjects("ows:Constraint").forEach(o=> {
                this.constraint.push(new Constraint(o));
            });

            this.metadata = [];
            source.getChildrenAsSerializedObjects("ows:Metadata").forEach(o=> {
                this.metadata.push(new Metadata(o));
            });
        }
    }
}