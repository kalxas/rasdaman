/**
 * Created by Alexandru on 25.10.2014.
 */

define(["knockout", "src/viewmodels/GetCapabilitiesTab", "src/viewmodels/DescribeCoverageTab", "src/models/CoverageDescriptions", "src/viewmodels/GetCoverageTab"], function (ko, GetCapabilitiesTab, DescribeCoverageTab, WCS, xmlToJSON, Capabilities, CoverageDescriptions, GetCoverageTab) {
    function MainViewModel() {
        var self = this;
        self.errorMessage = ko.observable(null);
        self.wcsInstance = ko.observable(null);

        self.getCapabilitiesTab = ko.observable(new GetCapabilitiesTab(self));
        self.describeCoverageTab = ko.observable(new DescribeCoverageTab(self));
        self.processCoveragesTab = ko.observable(null);
        self.getCoverageTab = ko.observable(null);


        self.isDescribeCoverageEnabled = ko.observable(false);
        self.isGetCoverageEnabled = ko.observable(false);
        self.isProcessCoveragesEnabled = ko.observable(false);

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