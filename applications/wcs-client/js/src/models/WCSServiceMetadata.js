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

define(["src/models/Extension"], function (Extension) {
    function WCSServiceMetadata(json, isKVP) {
        if (isKVP) {

        } else {
            if (!json.formatSupported) {
                throw new Error("Invalid json object" + JSON.stringify(json));
            }

            this.formatSupported = [];
            for (var i = 0; i < json.formatSupported.length; i++) {
                this.formatSupported.push(json.formatSupported[i]._text);
            }

            this.extension = json.Extension ? new Extension(json.Extension[0]) : null;
        }
    }

    return WCSServiceMetadata;

});