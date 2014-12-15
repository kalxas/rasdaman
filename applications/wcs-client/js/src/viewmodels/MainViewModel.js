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
 * Created by Alexandru on 25.10.2014.
 */

define(["knockout", "src/viewmodels/GetCapabilitiesTab", "src/viewmodels/DescribeCoverageTab", "src/viewmodels/InsertCoverageTab",
        "src/viewmodels/DeleteCoverageTab", "src/models/CoverageDescriptions", "src/viewmodels/GetCoverageTab"],
    function (ko, GetCapabilitiesTab, DescribeCoverageTab, InsertCoverageTab, DeleteCoverageTab, WCS, xmlToJSON, Capabilities, CoverageDescriptions, GetCoverageTab) {
    function MainViewModel() {
        var self = this;
        self.errorMessage = ko.observable(null);
        self.wcsInstance = ko.observable(null);

        self.getCapabilitiesTab = ko.observable(new GetCapabilitiesTab(self));
        self.describeCoverageTab = ko.observable(new DescribeCoverageTab(self));
        self.processCoveragesTab = ko.observable(null);
        self.getCoverageTab = ko.observable(null);
        self.insertCoverageTab = ko.observable(new InsertCoverageTab(self));
        self.deleteCoverageTab = ko.observable(new DeleteCoverageTab(self));

        self.isDescribeCoverageEnabled = ko.observable(false);
        self.isGetCoverageEnabled = ko.observable(false);
        self.isProcessCoveragesEnabled = ko.observable(false);
        self.isTransactionExtensionEnabled = ko.observable(false);

        self.processCoverageFrameUrl = ko.computed(function () {
            if (this.wcsInstance()) {
                return "../external/wcps/demo/demo-frames/wcps-console/index.html?endpoint=" + this.wcsInstance().WCSPUrl();
            } else {
                return "../external/wcps/demo/demo-frames/wcps-console/index.html";
            }

        }, this);
    }

    return MainViewModel;
});