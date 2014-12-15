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
 * Created by rasdaman on 22.11.14.
 */
define(["knockout", "src/models/CoverageDescriptions", "src/viewmodels/GetCoverageTab", "lib/xmlToJSON/xmlToJSON"], function (ko, CoverageDescriptions, GetCoverageTab, xmlToJSON) {
    function DescribeCoverageTab(mainViewModel) {
        var self = this;

        self.mainViewModel = mainViewModel;

        self.activeCoverageId = ko.observable(null);
        self.coverageDescriptions = ko.observable(null);
        self.describeCoverageDocument = ko.observable(null);

        self.describeCoverage = function () {
            //TODO Make this work with knocout js
            try {
                self.activeCoverageId($("#coverage-id-list").val());
                var describeCoverageUrl = self.mainViewModel.wcsInstance().describeCoverage(self.activeCoverageId());

                $.ajax(describeCoverageUrl)
                    .done(function (data, textStatus, jqXHR) {
                        var parsedXML = xmlToJSON.parseXML(data);
                        try {
                            self.coverageDescriptions(new CoverageDescriptions(parsedXML.CoverageDescriptions[0]));
                            console.log(self.coverageDescriptions());
                            self.describeCoverageDocument(xmlToJSON.xmlToString(data));

                            self.mainViewModel.getCoverageTab(new GetCoverageTab(self.mainViewModel.wcsInstance(), self.mainViewModel.getCapabilitiesTab().serverCapabilities(), self.coverageDescriptions().coverageDescription[0]), self.mainViewModel);
                            self.mainViewModel.isGetCoverageEnabled(true);
                        } catch (err) {
                            console.log(err);
                            console.log(describeCoverageUrl);
                            self.describeCoverageDocument(xmlToJSON.xmlToString(data));
                            self.mainViewModel.isGetCoverageEnabled(false);
                            self.mainViewModel.isGetCoverageEnabled(null);

                            self.mainViewModel.errorMessage({
                                message: "Could not retrieve the coverage description.",
                                level: "danger"
                            });
                            throw  err;
                        }
                    })
                    .fail(function (jqXHR, textStatus, errorThrown) {
                        console.log(jqXHR);
                        console.log(textStatus);
                        console.log(errorThrown);
                        console.log(describeCoverageUrl);

                        self.mainViewModel.isGetCoverageEnabled(false);
                        self.mainViewModel.isGetCoverageEnabled(null);

                        self.mainViewModel.errorMessage({
                            message: "Could not retrieve the coverage description.",
                            level: "danger"
                        });
                    }
                );
            } catch (err) {
                self.mainViewModel.errorMessage({
                    message: err,
                    level: "danger"
                });
            }
        };
    }

    return DescribeCoverageTab;
});