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