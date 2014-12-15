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
define(["src/models/OWSServiceIdentification", "src/models/OWSServiceProvider", "src/models/OWSOperationsMetadata" ], function (OWSServiceIdentification, OWSServiceProvider, OWSOperationsMetadata) {
    function CapabilitiesBaseType(/*JSObject*/json, /*bool*/isKVP) {
        if (isKVP) {

        } else {
            if (!json._attr || (json._attr && !json._attr.version)) {
                throw new Error("Invalid json:" + JSON.stringify(json));
            }
            this.version = json._attr.version._value;
            this.updateSequence = json._attr.updateSequence ? json._attr.updateSequence._value : null;

            this.serviceIdentification = json.ServiceIdentification ? new OWSServiceIdentification(json.ServiceIdentification[0]) : null;
            this.serviceProvider = json.ServiceProvider ? new OWSServiceProvider(json.ServiceProvider[0]) : null;
            this.operationsMetadata = json.OperationsMetadata ? new OWSOperationsMetadata(json.OperationsMetadata[0]) : null;
        }
    }

    return CapabilitiesBaseType;
});