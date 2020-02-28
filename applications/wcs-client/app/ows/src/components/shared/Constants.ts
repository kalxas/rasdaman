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

module rasdaman {
    export class Constants {
        public static APP_NAME:string = "wcsClient";
        public static PROCESSING_EXT_URI:string = "http://www.opengis.net/spec/WCS_service-extension_processing/2.0/conf/processing";
        public static TRANSACTION_EXT_URI:string = "http://www.opengis.net/spec/WCS_service-extension_transaction/2.0/conf/insert+delete";
        public static RANGE_SUBSETTING_EXT_URI:string = "http://www.opengis.net/spec/WCS_service-extension_range-subsetting/1.0/conf/record-subsetting";
        public static SCALING_EXT_URI:string = "http://www.opengis.net/spec/WCS_service-extension_scaling/1.0/conf/scaling";
        public static INTERPOLATION_EXT_URI:string = "http://www.opengis.net/spec/WCS_service-extension_interpolation/1.0/conf/interpolation";
        public static CRS_EXT_URI:string = "http://www.opengis.net/spec/WCS_service-extension_crs/1.0/conf/crs";
    }
}