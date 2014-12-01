/**
 * Created by Alexandru on 25.10.2014.
 */
define(["src/viewmodels/SupportedExtensions", "src/models/WCS", "src/models/Capabilities", "lib/xmlToJSON/xmlToJSON", "knockout"], function (SupportedExtensions, WCS, Capabilities, xmlToJSON, ko) {

    function GetCapabilitiesTab(mainViewModel) {
        var self = this;
        this.mainViewModel = mainViewModel;
//http://kahlua.eecs.jacobs-university.de:8080/petascope_earthlook?
        self.wcsEndpoint = ko.observable("http://wcs.rasdaman.flanche.net:8080/rasdaman/ows?");//http://localhost:8080/rasdaman/ows?");

        self.serverCapabilities = ko.observable(null);
        self.serverCapabilitiesDocument = ko.observable(null);

        self.openDescription = function (data) {
            $("#coverage-id-list").val(data.coverageId);
            $("#describe-coverage-tab-header").trigger("click");
            $("#describe-coverage-button").trigger("click");
        };

        self.getCapabilities = function () {
            //Get the capabilities for the new server
            var address = self.wcsEndpoint();
            var capabilitiesResult = null;
            if (address.charAt(address.length - 1) != "?") {
                address = address.concat("?");
            }

            self.wcsEndpoint(address);

            self.mainViewModel.wcsInstance(new WCS(self.wcsEndpoint()));
            var getCapabilitiesUrl = self.mainViewModel.wcsInstance().getCapabilities();

            $.ajax(getCapabilitiesUrl)
                .done(function (data, textStatus, jqXHR) {
                    var parsedXML = xmlToJSON.parseXML(data);
                    try {
                        capabilitiesResult = new Capabilities(parsedXML.Capabilities[0]);
                        self.serverCapabilities(capabilitiesResult);
                        self.serverCapabilitiesDocument(xmlToJSON.xmlToString(data));

                        self.mainViewModel.isDescribeCoverageEnabled(true);
                        self.mainViewModel.isGetCoverageEnabled(false);

                        if (capabilitiesResult.serviceIdentification.profile.indexOf(SupportedExtensions.processingExtension) != -1) {
                            self.mainViewModel.isProcessCoveragesEnabled(true);
                        } else {
                            self.mainViewModel.isProcessCoveragesEnabled(false);
                        }
                    }
                    catch
                        (err) {
                        console.log(err);

                        self.serverCapabilitiesDocument(xmlToJSON.xmlToString(data));
                        self.mainViewModel.isDescribeCoverageEnabled(false);
                        self.mainViewModel.isGetCoverageEnabled(false);
                        self.mainViewModel.isProcessCoveragesEnabled(false);

                        self.mainViewModel.errorMessage({
                            message: "The server's capabilities document contains an error.",
                            level: "danger"
                        });
                    }
                }
            )
                .
                fail(function (jqXHR, textStatus, errorThrown) {
                    console.log(jqXHR);
                    console.log(textStatus);
                    console.log(errorThrown);

                    self.mainViewModel.isDescribeCoverageEnabled(false);
                    self.mainViewModel.isGetCoverageEnabled(false);
                    self.mainViewModel.isProcessCoveragesEnabled(false);

                    self.mainViewModel.errorMessage({
                        message: "The server's capabilities could not be retrieved",
                        level: "danger"
                    });
                }
            );
        };
    }


    return GetCapabilitiesTab;
})
;