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
module admin {
    export class ServiceProvider {
        
        private providerName:string;
        private providerSite:string;
        private individualName:string;
        private positionName:string;
        private role:string;
        private email:string;
        private voicePhone:string;
        private facsimilePhone:string;
        private hoursOfService:string;
        private contactInstructions:string;
        private city:string;
        private administrativeArea:string;
        private postalCode:string;
        private country:string;

        public constructor(providerName, providerSite, individualName, positionName, role, email, voicePhone, facsimilePhone,
                        hoursOfService, contactInstructions, city, administrativeArea, postalCode, country) {            
            this.providerName = providerName;
            this.providerSite = providerSite;
            this.individualName = individualName;
            this.positionName = positionName;
            this.role = role;
            this.email = email;
            this.voicePhone = voicePhone;
            this.facsimilePhone = facsimilePhone;
            this.hoursOfService = hoursOfService;
            this.contactInstructions = contactInstructions;
            this.city = city;
            this.administrativeArea = administrativeArea;
            this.postalCode = postalCode;
            this.country = country;
        }        

        public toKVP():string {
            return "providerName=" + this.providerName +
                "&providerSite=" + this.providerSite +
                "&individualName=" + this.individualName +
                "&positionName=" + this.positionName +
                "&role=" + this.role +
                "&email=" + this.email +
                "&voicePhone=" + this.voicePhone +
                "&facsimilePhone=" + this.facsimilePhone +
                "&hoursOfService=" + this.hoursOfService +
                "&contactInstructions=" + this.contactInstructions +
                "&city=" + this.city + 
                "&administrativeArea=" + this.administrativeArea +
                "&postalCode=" + this.postalCode +
                "&country=" + this.country;
        }
    }
}
