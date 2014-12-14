/**
 * Handles the deletion of a coverage.
 */
define(["knockout", "lib/xmlToJSON/xmlToJSON"], function (ko, xmlToJSON) {
    function DeleteCoverageTab(mainViewModel) {
        var self = this;

        self.mainViewModel = mainViewModel;

        self.coverageId = ko.observable(null);
        self.requestInProgress = ko.observable(false);

        self.deleteCoverage = function () {
            try {
                self.coverageId($("#delete-coverage-id").val());

                var insertCoverageUrl = self.mainViewModel.wcsInstance().deleteCoverage(self.coverageId());

                self.requestInProgress(true);
                $.ajax(insertCoverageUrl)
                    .done(function (data, textStatus, jqXHR) {
                        self.mainViewModel.errorMessage({
                            message: "Successfully deleted coverage with id <b>" + self.coverageId() + "<b/>",
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

    return DeleteCoverageTab;
});