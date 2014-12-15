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
define(function () {
    function Address(json, isKVP) {
        var i;
        if (isKVP) {

        } else {
            this.deliveryPoint = [];
            this.postalCode = [];
            this.country = [];
            this.electronicMailAddress = [];

            for (i = 0; json.DeliveryPoint && i < json.DeliveryPoint.length; i++) {
                this.deliveryPoint.push(json.DeliveryPoint[i]._text);
            }

            for (i = 0; json.PostalCode && i < json.PostalCode.length; i++) {
                this.postalCode.push(json.PostalCode[i]._text);
            }

            for (i = 0; json.Country && i < json.Country.length; i++) {
                this.country.push(json.Country[i]._text);
            }

            for (i = 0; json.ElectronicMailAddress && i < json.ElectronicMailAddress.length; i++) {
                this.electronicMailAddress.push(json.ElectronicMailAddress[i]._text);
            }

            this.city = json.City ? json.City[0]._text : null;
            this.administrativeArea = json.AdministrativeArea ? json.AdministrativeArea[0]._text : null;
        }
    }

    return Address;

});