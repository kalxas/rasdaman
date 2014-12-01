/**
 * Created by Alexandru on 26.10.2014.
 */
define(["knockout", "src/viewmodels/SupportedExtensions"], function (ko, SupportedExtensions) {

        function WCSCore(coverageDescription) {
            var self = this;
            this.coverageSize = ko.observableArray([]);

            this.subsets = [];
            var bounds = coverageDescription.boundedBy.envelope;
            for (var i = 0; i < bounds.srsDimension; i++) {
                this.subsets.push(
                    {
                        label: bounds.axisLabels[i],
                        lowerCorner: bounds.lowerCorner[i],
                        upperCorner: bounds.upperCorner[i],
                        sliderMin: ko.observable(bounds.lowerCorner[i]),
                        sliderMax: ko.observable(bounds.upperCorner[i]),
                        isTrim: ko.observable("trim")
                    }
                );
            }

            this.getKVPRequest = function () {
                var result = "";

                for (var i = 0; i < self.subsets.length; i++) {
                    if (self.subsets[i].isTrim() == "trim") {
                        result += "&SUBSET=" + self.subsets[i].label + "(" + self.subsets[i].sliderMin() + "," + self.subsets[i].sliderMax() + ")";
                    } else {
                        result += "&SUBSET=" + self.subsets[i].label + "(" + self.subsets[i].sliderMin() + ")"
                    }
                }

                return result;
            };

            this.getCoverageSize = ko.computed(function () {
                var sizes = [];
                for (var i = 0; i < self.subsets.length; i++) {
                    if (self.subsets[i].isTrim() == "trim") {
                        sizes.push((coverageDescription.domainSet.limits.high[i] - coverageDescription.domainSet.limits.low[i]) * (self.subsets[i].sliderMax() - self.subsets[i].sliderMin()) / (self.subsets[i].upperCorner - self.subsets[i].lowerCorner));
                    } else {
                        sizes.push(1);
                    }
                }
                return sizes;

            }, this);
        }

        function ScalingExtension(coverageDescription) {
            var self = this;

            var errors;
            this.scalingMode = ko.observable("scaleByFactor");

            this.isScaleByFactor = ko.computed(function () {
                return this.scalingMode() == "scaleByFactor";
            }, this);

            this.isScaleAxesByFactor = ko.computed(function () {
                return this.scalingMode() == "scaleAxesByFactor";
            }, this);


            this.isScaleToSize = ko.computed(function () {
                return this.scalingMode() == "scaleToSize";
            }, this);

            this.isScaleToExtent = ko.computed(function () {
                return this.scalingMode() == "scaleToExtent";
            }, this);

            this.scaleByFactorValue = ko.observable(1.0).extend({number: true, min: 0});
            this.scaleAxesByFactorValues = [];
            this.scaleToSizeValues = [];
            this.scaleToExtentValues = [];

            this.scalingFactors = ko.computed(function () {
                var scalingFactors = [];
                var doNothing = this.scaleByFactorValue();
                var i;
                var axisNo = self.scaleToSizeValues.length;
                for (i = 0; i < axisNo; i++) {
                    scalingFactors.push(1);
                }

                if (self.isScaleByFactor()) {
                    for (i = 0; i < axisNo; i++) {
                        scalingFactors[i] = (+this.scaleByFactorValue());
                    }
                } else if (self.isScaleAxesByFactor()) {
                    for (i = 0; i < axisNo; i++) {
                        scalingFactors[i] = +this.scaleAxesByFactorValues[i].value();
                    }
                } else if (self.isScaleToExtent()) {
                    for (i = 0; i < axisNo; i++) {
                        scalingFactors[i] = (this.scaleToExtentValues[i].max() - this.scaleToExtentValues[i].min()) / (bounds.upperCorner[i] - bounds.lowerCorner[i]);
                    }
                } else if (self.isScaleToSize()) {
                    for (i = 0; i < axisNo; i++) {
                        scalingFactors[i] = (this.scaleToSizeValues[i].value()) / (bounds.upperCorner[i] - bounds.lowerCorner[i]);
                    }
                }
                return scalingFactors;
            }, this);

            var bounds = coverageDescription.boundedBy.envelope;
            for (var i = 0; i < bounds.srsDimension; i++) {
                this.scaleAxesByFactorValues.push(
                    {
                        label: bounds.axisLabels[i],
                        value: ko.observable(1.0).extend({number: true, min: 0, required: true})
                    }
                );

                this.scaleToSizeValues.push(
                    {
                        label: bounds.axisLabels[i],
                        value: ko.observable(bounds.upperCorner[i]).extend({number: true, min: 0, required: true})
                    }
                );
                this.scaleToExtentValues.push(
                    {
                        label: bounds.axisLabels[i],
                        min: ko.observable(bounds.lowerCorner[i]).extend({number: true, required: true}),
                        max: ko.observable(bounds.upperCorner[i]).extend({number: true, required: true})
                    }
                );

            }

            this.getKVPRequest = function () {
                var result = "";
                var i = 0;

                if (self.isScaleByFactor()) {
                    result = "&SCALEFACTOR=" + self.scaleByFactorValue();
                } else if (self.isScaleAxesByFactor()) {
                    result = "&SCALEAXES=";

                    for (i = 0; i < self.scaleAxesByFactorValues.length; i++) {
                        result += self.scaleAxesByFactorValues[i].label + "(" + self.scaleAxesByFactorValues[i].value() + "),";
                    }

                    result = result.substring(0, result.length - 1);
                } else if (self.isScaleToExtent()) {
                    result = "&SCALEEXTENT=";

                    for (i = 0; i < self.scaleToExtentValues.length; i++) {
                        result += self.scaleToExtentValues[i].label + "(" + self.scaleToExtentValues[i].min() + ":" + self.scaleToExtentValues[i].max() + "),";
                    }

                    result = result.substring(0, result.length - 1);
                } else if (self.isScaleToSize()) {
                    result = "&SCALESIZE=";

                    for (i = 0; i < self.scaleToSizeValues.length; i++) {
                        result += self.scaleToSizeValues[i].label + "(" + self.scaleToSizeValues[i].value() + "),";
                    }

                    result = result.substring(0, result.length - 1);
                }

                return result;
            }
        }


        function RangeComponent(start, end) {
            this.start = ko.observable(start);
            this.end = ko.observable(end);
        }

        function RangeSubsettingExtention(coverageDescription) {
            var self = this;
            this.availableRangeComponents = [];
            if (coverageDescription.rangeType && coverageDescription.rangeType.dataRecord && coverageDescription.rangeType.dataRecord.field.length > 0) {
                this.availableRangeComponents = coverageDescription.rangeType.dataRecord.field.slice(0);
            }

            this.selectedRangeComponents = ko.observableArray([]);

            this.addRangeComponent = function () {
                self.selectedRangeComponents.push(new RangeComponent(self.availableRangeComponents[0], null));
            };

            this.addRangeInterval = function () {
                self.selectedRangeComponents.push(new RangeComponent(self.availableRangeComponents[0], self.availableRangeComponents[0]));
            };

            this.removeRange = function (range) {
                self.selectedRangeComponents.remove(range);
            };

            this.getKVPRequest = function () {
                var result = "";

                var data = self.selectedRangeComponents();
                if (data.length > 0) {
                    result = "&RANGESUBSET=";
                }

                for (var i = 0; i < data.length; i++) {
                    if (data[i].end() == null) {
                        result += data[i].start() + ",";
                    } else {
                        var startIndex = self.availableRangeComponents.indexOf(data[i].start());
                        var endIndex = self.availableRangeComponents.indexOf(data[i].end());
                        if (startIndex <= endIndex) {
                            result += data[i].start() + ":" + data[i].end() + ",";
                        } else {
                            throw new Error("Invalid subset range selection");
                        }
                    }
                }
                result = result.substring(0, result.length - 1);

                return result;

            };

            this.getSubsetsPerAxis = function () {
                var axes = coverageDescription.rangeType.dataRecord.field;
                var subsets = [];
                var data = self.selectedRangeComponents();

                for (var j = 0; j < axes.length; j++) {
                    subsets.push(0);
                }

                for (var i = 0; i < data.length; i++) {
                    if (data[i].end() == null) {
                        subsets[axes.indexOf(data[i].start())]++;
                    } else {
                        subsets[axes.indexOf(data[i].start())]++;
                        subsets[axes.indexOf(data[i].end())]++;
                    }
                }

                return subsets;
            };
        }

        function InterpolationExtension(capabilitiesDocument) {
            this.supportedInterpolations = ko.observableArray([]);
            var self = this;

            var ext = capabilitiesDocument.serviceMetadata.extension;

            if (ext.data && ext.data.InterpolationMetadata && ext.data.InterpolationMetadata[0] && ext.data.InterpolationMetadata[0].InterpolationSupported) {
                var supported = ext.data.InterpolationMetadata[0].InterpolationSupported;
                for (var i = 0; i < supported.length; i++) {
                    this.supportedInterpolations.push({
                        "url": supported[i]._text,
                        "text": supported[i]._text.substring(supported[i]._text.lastIndexOf("/") + 1).replace("-", " ")
                    });
                }

                this.selectedInterpolationMethod = ko.observable(null);
            }

            this.getKVPRequest = function () {
                if (self.selectedInterpolationMethod()) {
                    return "&INTERPOLATION=" + this.selectedInterpolationMethod().url;
                } else {
                    return "";
                }
            }
        }

        function CRSExtension(capabilitiesDocument) {
            var self = this;

            this.supportedCrs = ko.observableArray([]);

            var ext = capabilitiesDocument.serviceMetadata.extension;

            if (ext.data && ext.data.CrsMetadata && ext.data.CrsMetadata[0] && ext.data.CrsMetadata[0].supportedCrs) {
                var supported = ext.data.CrsMetadata[0].supportedCrs;
                for (var i = 0; i < supported.length; i++) {
                    this.supportedCrs.push({
                        "url": supported[i]._text
                    });
                }

                this.subsettingCrs = ko.observable(null);
                this.outputCrs = ko.observable(null);
            }

            this.getKVPRequest = function () {
                var result = "";

                if (self.subsettingCrs() != null) {
                    result += "&SUBSETTINGCRS=" + self.subsettingCrs().url;
                }

                if (self.outputCrs() != null) {
                    result += "&OUTPUTCRS=" + self.outputCrs().url;
                }
                return result;
            }
        }

        function GetCoverageTab(wcs, capabilitiesDocument, coverageDescription, mainViewModel) {
            var self = this;
            this.mainViewModel=mainViewModel;
            var extension = capabilitiesDocument.serviceMetadata.extension;

            this.request = ko.observable(null);
            this.core = new WCSCore(coverageDescription);
            this.selectedFormat = ko.observable(null);
            this.multipart = ko.observable(false);


            if (capabilitiesDocument.serviceIdentification.profile.indexOf(SupportedExtensions.scalingExtension) != -1) {
                this.scalingExtension = ko.observable(new ScalingExtension(coverageDescription));
            } else {
                this.scalingExtension = ko.observable(null);
            }

            if (capabilitiesDocument.serviceIdentification.profile.indexOf(SupportedExtensions.recordSubsetting) != -1 ||
                capabilitiesDocument.serviceIdentification.profile.indexOf(SupportedExtensions.recordSubsettingBackup) != -1) {

                this.rangeSubsettingExtension = new RangeSubsettingExtention(coverageDescription);
            } else {
                this.rangeSubsettingExtension = null;
            }

            if (capabilitiesDocument.serviceIdentification.profile.indexOf(SupportedExtensions.interpolationCore) != -1 &&
                extension && extension.data && extension.data.InterpolationMetadata) {
                this.interpolationExtension = new InterpolationExtension(capabilitiesDocument);
            } else {
                this.interpolationExtension = null;
            }

            if (capabilitiesDocument.serviceIdentification.profile.indexOf(SupportedExtensions.crs) != -1 &&
                extension && extension.data && extension.data.CrsMetadata) {
                this.crsExtension = new CRSExtension(capabilitiesDocument);
            } else {
                this.crsExtension = null;
            }

            var validatedItems = [];
            if (this.scalingExtension()) {
                validatedItems.push(this.scalingExtension());
            }

            this.errors = ko.validation.group(validatedItems, {deep: true});

            this.coverageSize = ko.computed(function () {
                var auxSize= this.core.getCoverageSize();
                var coreSizes = [];
                var i = 0;

                //Copy to prevent a weird updating bug
                for(i=0; i<auxSize.length; i++){
                    coreSizes.push(auxSize[i]);
                }
                console.log(coreSizes);
                //if (self.rangeSubsettingExtension) {
                //    var ranges = self.rangeSubsettingExtension.getSubsetsPerAxis();
                //    console.log(ranges);
                //    var s = 0;
                //    for (i = 0; i < ranges.length; i++) {
                //        s += ranges[i];
                //
                //    }
                //    s = s / ranges.length;
                //
                //    for (i = 0; i < coreSizes.length; i++) {
                //        coreSizes[i] *= s;
                //    }
                //}

                if (self.scalingExtension()) {
                    var scalings = self.scalingExtension().scalingFactors();
                    console.log(scalings);
                    for (i = 0; i < scalings.length; i++) {
                        coreSizes[i] *=  self.scalingExtension().scalingFactors()[i];
                    }
                }

                var size = 1;

                for (i = 0; i < coreSizes.length; i++) {
                    size *= coreSizes[i];
                }

                return size;
            }, this);

            this.getKVPRequest = function () {
                var url = wcs.getCoverage(coverageDescription.coverageId, self.selectedFormat(), self.multipart());

                if (self.core) {
                    url += self.core.getKVPRequest();
                }

                if (self.rangeSubsettingExtension) {
                    url += self.rangeSubsettingExtension.getKVPRequest();
                }

                if (self.scalingExtension) {
                    url += self.scalingExtension().getKVPRequest();
                }

                if (self.interpolationExtension) {
                    url += self.interpolationExtension.getKVPRequest();
                }

                if (self.crsExtension) {
                    url += self.crsExtension.getKVPRequest();
                }

                return url;
            };

            this.getCoverage = function () {
                if (self.errors().length == 0) {
                    try {
                        self.request(self.getKVPRequest());
                        window.open(self.request(), "_blank");
                    } catch (err) {
                        console.log(err);
                        self.mainViewModel.errorMessage({
                            message: "Could not retrieve the coverage.",
                            level: "danger"
                        });
                    }
                } else {
                    alert('Please check your query and remove any error.');
                    self.errors.showAllMessages();
                }
            }
        }

        return GetCoverageTab;
    }
)
;