/**
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 */

Rj.util.ConfigManager.setWCSService({
  service: "WCS",
  version: "2.0.1",
  baseUrl: "http://kahlua.eecs.jacobs-university.de:8080/petascope_earthlook"
});

FlancheJs.defineClass("Rj.query.wcs.Coverage", {

  init: function (name, boundingBox, gridLimits, axisLabels, rangeFields) {
    this.setName(name);
    this.setBoundingBox(boundingBox);
    this.setGridLimits(gridLimits);
    this.setAxisLabels(axisLabels);
    this.setRangeFields(rangeFields);
  },

  properties: {
    name: {
      value: ""
    },

    boundingBox: {
      value: {}
    },

    gridLimits: {
      value: {}
    },

    axisLabels: {
      value: []
    },

    rangeFields: {
      value: []
    }
  }

})

FlancheJs.defineClass("Rj.query.wcs.GetCapabilities", {

  implements: [Rj.query.Executable, Rj.util.Observable],

  init: function () {
  },

  properties: {
    service: {
      value: {
        service: Rj.util.ConfigManager.getWCSService().service,
        version: Rj.util.ConfigManager.getWCSService().version,
        baseUrl: Rj.util.ConfigManager.getWCSService().baseUrl
      }
    }
  },

  methods: {
    getCoverages: function (callback, scope) {
      this.evaluate(function (data) {
        var xmlDoc = new Rj.util.XMLDoc(data);
        var coverages = [];
        _.map(xmlDoc.getXmlDoc().getElementsByTagName("CoverageId"), function (value) {
          coverages.push(value.textContent.trim());
        });
        callback.call(scope, coverages);
      })
    },

    transport: function () {
      var params = {
        service: this.getService().service,
        version: this.getService().version,
        request: "GetCapabilities"
      };
      var transport = new Rj.query.Transport(
        this.getService().baseUrl,
        params,
        Rj.query.Transport.HttpMethod.GET
      );
      transport.setBinary(false);
      return transport;
    }
  }
});

FlancheJs.defineClass("Rj.query.wcs.DescribeCoverage", {

  implements: [Rj.query.Executable, Rj.util.Observable],

  init: function (coverageId) {
    this.setCoverageId(coverageId);
  },

  properties: {
    service: {
      value: {
        service: Rj.util.ConfigManager.getWCSService().service,
        version: Rj.util.ConfigManager.getWCSService().version,
        baseUrl: Rj.util.ConfigManager.getWCSService().baseUrl
      }
    },

    coverageId: {
      value: null
    }
  },

  methods: {
    getCoverage: function (callback, scope) {
      var self = this;
      this.evaluate(function (data) {
        var xmlDoc = new Rj.util.XMLDoc(data);
        var boundedBy = {
          low : xmlDoc.filter("//gml:boundedBy//gml:lowerCorner")[0].getXmlDoc().textContent,
          high: xmlDoc.filter("//gml:boundedBy//gml:upperCorner")[0].getXmlDoc().textContent
        };
        var grid = {
          low : xmlDoc.filter("//gml:GridEnvelope//gml:low")[0].getXmlDoc().textContent,
          high: xmlDoc.filter("//gml:GridEnvelope//gml:high")[0].getXmlDoc().textContent
        };
        var axisLabels = xmlDoc.filter("//gml:axisLabels")[0].getXmlDoc().textContent.split(" ")
        var fields = _.map(xmlDoc.filter("//*[local-name() = 'field']"),function (value) {
          if (value) {
            return value.getXmlDoc().getAttribute("name");
          }
        }).slice(0, -1);
        var coverage = new Rj.query.wcs.Coverage(self.getCoverageId(), boundedBy, grid, axisLabels, fields);
        callback.call(scope, coverage, xmlDoc);
      })
    },

    transport: function () {
      var params = {
        service   : this.getService().service,
        version   : this.getService().version,
        request   : "DescribeCoverage",
        coverageId: this.getCoverageId()
      };
      var transport = new Rj.query.Transport(
        this.getService().baseUrl,
        params,
        Rj.query.Transport.HttpMethod.GET
      );
      transport.setBinary(false);
      return transport;
    }
  }
});

FlancheJs.defineClass("Rj.query.wcs.GetCoverage", {

  init: function (coverageId) {
    this.setCoverageId(coverageId);
    this.setSubsets([]);
  },

  implements: [Rj.query.Executable, Rj.util.Observable],

  properties: {
    coverageId  : {},
    subsets     : {value: []},
    binaryFormat: {value: false},
    rangeSubsets: {value: []},
    format      : {value: "application/gml+xml"}
  },

  methods: {
    transport: function () {
      var params = {
        service   : Rj.util.ConfigManager.getWCSService().service,
        version   : Rj.util.ConfigManager.getWCSService().version,
        request   : "GetCoverage",
        coverageId: this.getCoverageId()
      };

      var subsets = this.getSubsets();
      for (var i = 0; i < subsets.length; i++) {
        params['subset' + i.toString()] = subsets[i].transport();
      }

      if (this.getFormat() !== "application/gml+xml") {
        params['format'] = this.getFormat();
        this.setBinaryFormat(true);
      }

      if (this.getRangeSubsets().length > 0) {
        params['rangesubset'] = this.getRangeSubsets().join(",");
      }

      var transport = new Rj.query.Transport(
        Rj.util.ConfigManager.getWCSService().baseUrl,
        params,
        Rj.query.Transport.HttpMethod.GET
      );
      transport.setBinary(this.getBinaryFormat());
      return transport;
    },

    addSubset: function (subsetDef) {
      this.$subsets.push(subsetDef);
    },

    removeSubset: function (subsetDef) {
      this.$subsets = this.$subsets.filter(function (iterSubset) {
        return !subsetDef.equals(iterSubset);
      })
    },

    addRangeSubset: function (rangeSubset) {
      this.$rangeSubsets.push(rangeSubset);
    },

    removeRangeSubset: function (rangeSubset) {
      this.$rangeSubsets = _.without(this.$rangeSubsets, rangeSubset);
    },

    toString: function () {
      return this.transport().toString();
    }
  }
})

FlancheJs.defineClass("Demo.wcs.Browser", {

  init: function () {

  },

  methods: {
    run: function () {
      var self = this;
      $(document).ready(function () {
        self._populateCoverages();
      })
    }
  },

  internals: {
    sliders: [],

    populateCoverages: function () {
      $("#coverage-list-loading").show();
      var coveragesRequest = new Rj.query.wcs.GetCapabilities();
      coveragesRequest.getCoverages(function (coverages) {
        var coverageList = _.map(["NIR", "lena", "visible_human", "climate_clouds", "eobs"],function (coverage) {
          return "<li><a href='#'>" + coverage + "</a></li>"
        }).join("")
        var template = "<ul>" + coverageList + "</ul>";
        $("#coverage-list-loading").hide();
        $("#coverage-list-container").html(template);
        this._registerCoverageSelectorHandler();
      }, this);
    },

    populateCoverageDescription: function (coverageId) {
      var describeCoverageRequest = new Rj.query.wcs.DescribeCoverage(coverageId);
      $("#coverage-description-loading").show();
      $(".coverage-not-selected-mess").hide();
      describeCoverageRequest.getCoverage(function (coverage, xmlDoc) {
        $("#coverage-description-loading").hide();
        $("#describe-coverage-xml-box").html(xmlDoc.toString().replace(/</g, "&lt;").replace(/>/g, "&gt;"));
        $("#describe-coverage-xml-box").show();
        this._createSliders(coverage);
        this._createRangeSubsets(coverage);
        this._createFormatters();
        this._registerExecuteHandler(coverage);
        prettyPrint();
      }, this);
    },

    registerCoverageSelectorHandler: function () {
      var self = this;
      $("#coverage-list-container li a").on('click', function (event) {
        event.preventDefault();
        self._populateCoverageDescription($(this).text());
      })
    },

    createSliders: function (coverage) {
      var $sliders = $("#sliders");
      $sliders.html("");
      this._sliders = [];
      for (var i = 0; i < coverage.getAxisLabels().length; i++) {
        var bboxLow = parseFloat(coverage.getBoundingBox().low.split(" ")[i]);
        var bboxHigh = parseFloat(coverage.getBoundingBox().high.split(" ")[i]);
        $sliders.append("<div id='slider-" + coverage.getAxisLabels()[i] + "'></div>");
        var slider = new Rj.widget.HorizontalSlider("#slider-" + coverage.getAxisLabels()[i], bboxLow, bboxHigh);
        slider.setWidth(250);
        slider.setStep((bboxHigh - bboxLow) / 10);
        slider.setRange(true);
        slider.setLabel(coverage.getAxisLabels()[i]);
        this._sliders.push(slider);
      }
    },

    createRangeSubsets: function (coverage) {
      var subsetButtons = _.map(coverage.getRangeFields(), function (field) {
        return "<button class='btn btn-primary'>" + field + "</button>";
      });
      $("#range-subsets").html(subsetButtons.join(""));
      $("#range-subsets button").on("click", function (event) {
        event.preventDefault();
        $(this).toggleClass("disabled");
      })
    },

    getSelectedRanges: function () {
      var ranges = []
      $("#range-subsets button.disabled").each(function (index, element) {
        ranges.push($(element).html());
      })
      return ranges;
    },

    createFormatters: function () {
      $("#formatters").show();
      $("#formatters button").on("click", function (event) {
        event.preventDefault();
        $("#formatters button").removeClass("disabled");
        $(this).toggleClass("disabled");

      })
    },

    getSelectedFormatter: function () {
      return $("#formatters button.disabled").text();
    },

    registerExecuteHandler: function (coverage) {
      var self = this;
      $("#execute").off('click.raswct');
      $("#execute").on('click.raswct', function (event) {
        event.preventDefault();
        $("#result").html("");
        $("#constructed-query-container").show();
        var getCoverageRequest = new Rj.query.wcs.GetCoverage(coverage.getName());

        _.each(self._sliders, function (slider, index) {
          if (slider.getValue()[0] != slider.getMin() || slider.getValue() != slider.getMax()) {
            var subset = new Rj.query.wcs.Subset(coverage.getAxisLabels()[index], null, slider.getValue()[0], slider.getValue()[1]);
            getCoverageRequest.addSubset(subset);
          }
        });

        _.each(self._getSelectedRanges(), function (rangeSubset) {
          getCoverageRequest.addRangeSubset(rangeSubset);
        })

        getCoverageRequest.setFormat(self._getSelectedFormatter());

        $("#constructed-query").html(getCoverageRequest.toString());
        $("#query-evaluation-loading").show();
        if (getCoverageRequest.getFormat() == "application/gml+xml") {
          getCoverageRequest.evaluate(function (data) {
            $(".prettyprint").removeClass("prettyprint")
            $("#result").html("<pre style='height:250px;overflow-y: scroll;' class='prettyprint linenums'>" + data.replace(/</g, "&lt;").replace(/>/g, "&gt;") + "</pre>")
            $("#query-evaluation-loading").hide();
            prettyPrint();
          })
        }
        else{
          $("#query-evaluation-loading").hide();
          window.open(getCoverageRequest.toString().replace("GET ", ""));
          $("#result").append("<span class='label label-inverse'>The selected resource was downloaded successfully.</span> ")
        }
      })
    }
  }
});

var demo = new Demo.wcs.Browser();
demo.run();
