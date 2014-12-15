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
 * Handles the insertion of a new coverage.
 */
define(["knockout", "lib/xmlToJSON/xmlToJSON"], function (ko, xmlToJSON) {
    function InsertCoverageTab(mainViewModel) {
        var self = this;

        self.mainViewModel = mainViewModel;

        self.coverageRef = ko.observable(null);
        self.useNewId = ko.observable(false);
        self.requestInProgress = ko.observable(false);

        self.insertCoverage = function () {
            try {
                self.coverageRef($("#coverage-ref").val());
                self.useNewId($("#use-new-id").is(":checked"));

                var insertCoverageUrl = self.mainViewModel.wcsInstance().insertCoverage(self.coverageRef(), self.useNewId());

                self.requestInProgress(true);
                $.ajax(insertCoverageUrl)
                    .done(function (data, textStatus, jqXHR) {
                        var parsedXML = xmlToJSON.parseXML(data);
                        self.mainViewModel.errorMessage({
                            message: "Successfully inserted coverage with id <b>" + parsedXML["coverageId"][0]["_text"] + "<b/>",
                            level: "success"
                        })
                        self.requestInProgress(false);
                    })
                    .fail(function (jqXHR, textStatus, errorThrown) {
                        var parsedXML = xmlToJSON.parseXML(jqXHR.responseXML);
                        var exceptionCode = parsedXML["ExceptionReport"][0]["Exception"][0]["_attr"]["exceptionCode"]["_value"];
                        var exceptionText = parsedXML["ExceptionReport"][0]["Exception"][0]["ExceptionText"][0]["_text"];
                        self.mainViewModel.errorMessage({
                            message: "Could not delete the coverage. Server responded with:<br/><b><i> " + exceptionCode + ": " + exceptionText + "</i></b>",
                            level: "danger"
                        });
                        self.requestInProgress(false);
                    }
                );
            } catch (err) {
                self.mainViewModel.errorMessage({
                    message: err,
                    level: "danger"
                });
                self.requestInProgress(false);
            }
        };
    }

    return InsertCoverageTab;
});