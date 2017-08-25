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
/// <reference path="../../common/_common.ts"/>
/// <reference path="../../models/wcs_model/wcs/_wcs.ts"/>
/// <reference path="../wcs_component/settings/SettingsService.ts"/>

module rasdaman {
    export class ErrorHandlingService {
        public static $inject = ["Notification", "rasdaman.common.SerializedObjectFactory", "$log"];

        public constructor(private notificationService:any,
                           private serializedObjectFactory:rasdaman.common.SerializedObjectFactory,
                           private $log:angular.ILogService) {
        }

        public handleError(...args:any[]) {
            //Network error
            if (args.length == 1) {
                var errorInformation:any = args[0][0];

                if (errorInformation.status == 404 || errorInformation.status == -1) {
                    // No error in data and HTTP code 404 or -1 then, Petascope cannot connect.
                    this.notificationService.error("Cannot connect to petascope, please check if petascope is running.");
                } else {
                    this.notificationService.error("The request failed with HTTP code:" + errorInformation.status + "(" + errorInformation.statusText + ")");
                }                

                if (errorInformation.data != null && errorInformation.data != "") {
                    try {
                        var responseDocument = new rasdaman.common.ResponseDocument(errorInformation.data, rasdaman.common.ResponseDocumentType.XML);
                        var serializedResponse = this.serializedObjectFactory.getSerializedObject(responseDocument);
                        var exceptionReport = new ows.ExceptionReport(serializedResponse);

                        this.notificationService.error(exceptionReport.exception.exceptionText + "</br> Exception code: " + exceptionReport.exception.exceptionCode);
                    } catch (err) {
                        ///This means that the exception is not a valid WCS exception
                        this.$log.error(err);
                    }
                }
            }
        }
    }
}