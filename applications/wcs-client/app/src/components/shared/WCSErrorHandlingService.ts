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
 * Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2014, 2015 Peter Baumann /
 rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
/// <reference path="../../common/_common.ts"/>
/// <reference path="../../models/wcs/_wcs.ts"/>
/// <reference path="../settings/SettingsService.ts"/>

module rasdaman {
    export class WCSErrorHandlingService {
        public static $inject = ["Notification", "rasdaman.common.SerializedObjectFactory", "$log"];

        public constructor(private notificationService:any,
                           private serializedObjectFactory:rasdaman.common.SerializedObjectFactory,
                           private $log:angular.ILogService) {
        }

        public handleError(...args:any[]) {
            //Network error
            if (args.length == 1) {
                var errorInformation:any = args[0][0];

                this.notificationService.error("The request failed with status:" + errorInformation.status + "(" + errorInformation.statusText + ")");

                if (errorInformation.data) {
                    try {
                        var responseDocument = new rasdaman.common.ResponseDocument(errorInformation.data, rasdaman.common.ResponseDocumentType.XML);
                        var serializedResponse = this.serializedObjectFactory.getSerializedObject(responseDocument);
                        var exceptionReport = new ows.ExceptionReport(serializedResponse);

                        this.notificationService.error(exceptionReport.Exception.ExceptionText + "</br> Exception code: " + exceptionReport.Exception.ExceptionCode);
                    } catch (err) {
                        ///This means that the exception is not a valid WCS exception
                        this.$log.error(err);
                    }
                }
            }
        }
    }
}