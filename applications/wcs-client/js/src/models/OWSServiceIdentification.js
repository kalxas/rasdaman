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

define(["src/models/Inherit", "src/models/Description", ], function (Inherit, Description) {
    function OWSServiceIdentification(json, isKVP) {
        Description.call(this, json, isKVP);

        if (isKVP) {

        } else {
            if (!json.ServiceType || !json.ServiceTypeVersion) {
                throw new Error("Invalid json object" + JSON.stringify(json));
            }
            this.serviceType = json.ServiceType[0]._text;

            this.serviceTypeVersion = [];
            for (i = 0; i < json.ServiceTypeVersion.length; i++) {
                this.serviceTypeVersion.push(json.ServiceTypeVersion[i]._text);
            }

            this.fees = json.Fees ? json.Fees[0]._text : null;
            this.accessConstraints = json.AccessConstraints ? json.AccessConstraints[0]._text : null;

            this.profile = [];
            for (i = 0; json.Profile && i < json.Profile.length; i++) {
                this.profile.push(json.Profile[i]._text);
            }
        }
    }

    Inherit(OWSServiceIdentification, Description);

    return OWSServiceIdentification;

});