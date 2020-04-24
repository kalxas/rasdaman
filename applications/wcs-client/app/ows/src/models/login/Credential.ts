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
 * Copyright 2003 - 2019 Peter Baumann /
 rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
module login {
    export class Credential {
        public username:string;
        public password:string;

        public constructor(username, password) {            
            this.username = username;
            this.password = password;
        }        

        public toKVP():string {
            return "username=" + this.username +
                "&password=" + this.password;
        }

        public toString():string {
            return this.username + ":" + this.password;
        }

        /**
         * Convert a persisted username:password in local storage to an object         
         */
        public static fromString(input:string):Credential {
            // e.g: username:password
            var tmp:string[] = input.split(":");
            var username:string = tmp[0];
            var password:string = tmp[1];

            return new Credential(username, password);    
        }
    }
}
