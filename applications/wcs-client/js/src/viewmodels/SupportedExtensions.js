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

/**
 * Created by rasdaman on 23.11.14.
 */
define(function () {
    return {
        processingExtension: "http://www.opengis.net/spec/WCS_service-extension_processing/2.0/conf/processing",
        interpolationCore: "http://www.opengis.net/spec/WCS_service-extension_interpolation/1.0/conf/interpolation",
        recordSubsetting: "http://www.opengis.net/spec/WCS_service-extension_range-subsetting/1.0/conf/record-subsetting",
        recordSubsettingBackup: "http://www.opengis.net/spec/WCS_service-extension_range-subsetting/1.0/conf/",
        scalingExtension: "http://www.opengis.net/spec/WCS_service-extension_scaling/1.0/conf/scaling",
        crs: "http://www.opengis.net/spec/WCS_service-extension_crs/1.0/conf/crs",
        transactionExtension: "http://www.opengis.net/spec/WCS_service-extension_transaction/2.0/conf/insert+delete"
    };
});