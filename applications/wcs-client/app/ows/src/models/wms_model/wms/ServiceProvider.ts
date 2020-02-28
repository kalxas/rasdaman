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

module wms {
    export class ServiceProvider {        
        public providerName:string;
        public providerSite:string;
        public contactPerson:string;
        public positionName:string;
        public email:string;
        public voicePhone:string;

        public address:string;
        public city:string;
        public postCode:string;
        public country:string;
        
        public constructor(providerName:string, providerSite:string, contactPerson:string, positionName:string,
                          email:string, voicePhone:string, address:string, city:string, postCode:string, country:string) {
            this.providerName = providerName;
            this.providerSite = providerSite;
            this.contactPerson = contactPerson;
            this.positionName = positionName;
            this.email = email;
            this.voicePhone = voicePhone;
            this.address = address;
            this.city = city;
            this.postCode = postCode;
            this.country = country;
        }
    }
}