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
///<reference path="Envelope.ts"/>
///<reference path="EnvelopeWithTimePeriod.ts"/>

module gml {
    /**
     * Extend this class so that it fully complies with the OGC GML specification if the need arises.
     */
    export class BoundedBy {
        public envelope:Envelope;
        public envelopeWithTimePeriod:EnvelopeWithTimePeriod;

        public constructor(source:rasdaman.common.ISerializedObject) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");

            if (source.doesElementExist("gml:Envelope")) {
                this.envelope = new Envelope(source.getChildAsSerializedObject("gml:Envelope"));
            }

            if (source.doesElementExist("gml:EnvelopeWithTimePeriod")) {
                this.envelopeWithTimePeriod = new EnvelopeWithTimePeriod(source.getChildAsSerializedObject("gml:EnvelopeWithTimePeriod"));
                this.envelope = this.envelopeWithTimePeriod;
            }
        }
    }
}