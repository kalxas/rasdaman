/**
 * Created by Alexandru on 19.10.2014.
 */

define(["CoverageDescriptions", "WCS", "Capabilities", "Util", "jQuery", "jQueryUI"], function (CoverageDescriptions, WCS, Capabilities, Util) {
    function UIControl() {
        this.wcsInstance = null;
        this.serverCapabilities = null;
        var self = this;

        //Initialize the get capabilities button
        $("#get-capabilities-button").click(function () {
            //Clear the previous data
            self.hideDescribeCoverageSection();
            self.hideGetCoverageSection();

            //Get the capabilities for the new server
            var address = $("#wcs-server-address").val();
            if (address.charAt(address.length - 1) != "?") {
                address = address.concat("?");
            }

            self.wcsInstance = new WCS(address);
            var getCapabilitiesUrl = self.wcsInstance.getCapabilities();

            $.ajax(getCapabilitiesUrl)
                .done(function (data, textStatus, jqXHR) {
                    var parsedXML = xmlToJSON.parseXML(data);
                    self.serverCapabilities = new Capabilities(parsedXML.Capabilities[0]);
                    self.showDescribeCoverageSection(self.serverCapabilities);
                })
                .fail(function (jqXHR, textStatus, errorThrown) {
                    alert(jqXHR);
                }
            );
        });
    }

    UIControl.prototype.showDescribeCoverageSection = function (/*Capabilities*/capabilities) {
        var self = this;
        console.log("Show describe coverage section");
        console.log(capabilities);

        var autocompleteTerms = [];
        for (var i = 0; i < capabilities.contents.coverageSummary.length; i++) {
            autocompleteTerms.push(capabilities.contents.coverageSummary[i].coverageId);
        }

        //Create the autocomplete list
        $("#coverage-id-list").autocomplete({
            source: Util.createAutocompleteFilter(autocompleteTerms)
        });

        $("#describe-coverage-row").css("display", "block");

        $("#providerName").text(capabilities.serviceProvider.providerName).css("display", "block");
        $("#providerWebsite").attr("href", capabilities.serviceProvider.providerSite.value).text(capabilities.serviceProvider.providerSite.value).css("display", "block");
        $("#contactPerson").text(capabilities.serviceProvider.serviceContact.individualName).css("display", "block");
        $("#contactRole").text(capabilities.serviceProvider.serviceContact.role).css("display", "block");
        $("#city").text(capabilities.serviceProvider.serviceContact.contactInfo.address.city).css("display", "block");
        $("#country").text(capabilities.serviceProvider.serviceContact.contactInfo.address.country[0]).css("display", "block");
        $("#postal-code").text(capabilities.serviceProvider.serviceContact.contactInfo.address.postalCode[0]).css("display", "block");
        $("#email-address").attr("href", "mailto:" + capabilities.serviceProvider.serviceContact.contactInfo.address.electronicMailAddress[0]).text(capabilities.serviceProvider.serviceContact.contactInfo.address.electronicMailAddress[0]).css("display", "block");


        //Initialize the describe coverage button
        $("#describe-coverage-button").click(function () {
            var coverageId = $("#coverage-id-list").val();
            var describeCoverageUrl = self.wcsInstance.describeCoverage(coverageId);
            $.ajax(describeCoverageUrl)
                .done(function (data, textStatus, jqXHR) {
                    var parsedXML = xmlToJSON.parseXML(data);
                    var coverageDescriptions = new CoverageDescriptions(parsedXML.CoverageDescriptions[0]);
                    self.hideGetCoverageSection();
                    self.showGetCoverageSection(coverageDescriptions);

                })
                .fail(function (jqXHR, textStatus, errorThrown) {
                    alert(jqXHR);
                }
            );
        });


    };

    UIControl.prototype.hideDescribeCoverageSection = function () {
        $("#describe-coverage-row").css("display", "none");
    };

    UIControl.prototype.showGetCoverageSection = function (coverageDescriptions) {
        console.log(coverageDescriptions);
        var self = this;

        function createSliderClosure(label) {
            return function (event, ui) {

                $("#" + label).val(ui.values[ 0 ] + " - " + ui.values[ 1 ]);
            }
        }

        var downloadURL = function downloadURL(url) {
            var hiddenIFrameID = 'hiddenDownloader',
                iframe = document.getElementById(hiddenIFrameID);
            if (iframe === null) {
                iframe = document.createElement('iframe');
                iframe.id = hiddenIFrameID;
                iframe.style.display = 'none';
                document.body.appendChild(iframe);
            }
            iframe.src = url;
        };

        if (coverageDescriptions.coverageDescription.length > 0) {
            var coverageDomain = coverageDescriptions.coverageDescription[0].boundedBy.envelope;

            var title = '<h3>Trimming dimensions</h3>';
            $("#trimming-column").append(title);

            for (var i = 0; i < coverageDomain.srsDimension; i++) {

                var labelId = coverageDomain.axisLabels[i] + '-label';
                var sliderId = coverageDomain.axisLabels[i] + '-slider';
                var element = '<p><label for="' + labelId + '" style="margin-top: 10px;">' + coverageDomain.axisLabels[i] + ':</label>';
                element += ' <input type="text" id="' + labelId + '" readonly style="border:0; color:#f6931f; font-weight:bold;"></p>';
                element += '<div id="' + sliderId + '"></div>';

                $("#trimming-column").append(element);

                $("#" + sliderId).slider({
                    range: true,
                    step: (coverageDomain.upperCorner[i] - coverageDomain.lowerCorner[i]) / 10,
                    min: coverageDomain.lowerCorner[i],
                    max: coverageDomain.upperCorner[i],
                    values: [ coverageDomain.lowerCorner[i], coverageDomain.upperCorner[i] ],
                    slide: createSliderClosure(labelId)
                });

                $("#" + labelId).val(coverageDomain.lowerCorner[i] + " - " + coverageDomain.upperCorner[i]);

            }


            //Display available formats
            var formats = this.serverCapabilities.serviceMetadata.formatSupported;
            for (i = 0; i < formats.length; i++) {
                var el = '<label for="' + formats[i] + '">' + formats[i] + '</label>';
                el += '<input type="radio" id="' + formats[i] + '" name="radio">';
                $("#coverage-format").append(el);
            }
            var children = $("#coverage-format").children();
            $(children[children.length - 1]).attr("checked", "checked");

            $("#coverage-format").buttonset();

            $("#get-coverage-row").css("display", "block");
            $("#trimming-column").css("display", "block");

            $("#get-coverage-button").click(function () {
                var selectedVal = "";
                var selected = $("#coverage-format input[type='radio']:checked");
                var lowerBounds = [];
                var upperBounds = [];
                var axes = [];
                for (var i = 0; i < coverageDomain.srsDimension; i++) {
                    var sliderValues = $("#" + coverageDomain.axisLabels[i] + '-slider').slider("values");
                    lowerBounds.push(sliderValues[0]);
                    upperBounds.push(sliderValues[1]);
                    axes.push(coverageDomain.axisLabels[i]);
                }

                var url = self.wcsInstance.getCoverage(coverageDescriptions.coverageDescription[0].coverageId, selected[0].id, axes, lowerBounds, upperBounds);
                downloadURL(url);
                console.log(url);
            });
        }
        else {

        }
    };

    UIControl.prototype.hideGetCoverageSection = function () {
        $("#coverage-format").empty();
        $("#trimming-column").empty().css("display", "none");
        $("#get-coverage-row").css("display", "none");

    };

    return UIControl;
});