SRC = src
BIN = bin
LIB = lib
CP = cp -r
RM = rm -rf
MKDIR = mkdir
INSTALL_DIR = $(DESTDIR)$(datadir)/rasdaman/raswct

all: css libs src img
	@cat $(BIN)/raswct_lib.js $(BIN)/raswct_src.js > $(BIN)/raswct.js
	@echo "Build Process sucessfully. You can now install the toolkit by running $ make install"

doBin:
	$(MKDIR) -p $(BIN)
	$(MKDIR) -p $(BIN)/img

img: doBin
	@$(CP) $(LIB)/Gauges/img/* $(LIB)/TwitterBootstrap/img/* $(BIN)/img

css: doBin
	@cat $(LIB)/Dojo/dojo.css $(LIB)/jqPlot/jquery.jqplot.css               \
	     $(LIB)/jqTip/jqtip.css                                             \
	     $(LIB)/openlayers/style.css                                        \
	     $(LIB)/Gauges/css/jgauge.css                                       \
	     $(LIB)/TwitterBootstrap/bootstrap.css                              \
	     $(LIB)/Gauges/css/styles.css                                       \
	     $(LIB)/Gauges/css/knobKnob.css > $(BIN)/raswct.css


libs: doBin
	@cat $(LIB)/Flanche/FlancheJs.js                                 \
	     $(LIB)/Underscore/underscore.min.js                         \
	     $(LIB)/LawnChair/lawnchair.min.js                           \
	     $(LIB)/jQuery/jQuery.min.js                                 \
	     $(LIB)/Dojo/dojo.js                                         \
	     $(LIB)/jQuery/jQueryRotate.min.js                           \
	     $(LIB)/jqTip/jqTip.min.js                                   \
	     $(LIB)/openlayers/OpenLayers.js                             \
	     $(LIB)/jqPlot/jquery.jqplot.min.js                          \
	     $(LIB)/jqPlot/plugins/jqplot.canvasAxisLabelRenderer.min.js \
	     $(LIB)/jqPlot/plugins/jqplot.canvasAxisTickRenderer.min.js  \
	     $(LIB)/jqPlot/plugins/jqplot.canvasOverlay.js               \
	     $(LIB)/jqPlot/plugins/jqplot.categoryAxisRenderer.min.js    \
	     $(LIB)/jqPlot/plugins/jqplot.canvasTextRenderer.min.js      \
	     $(LIB)/jqPlot/plugins/jqplot.cursor.min.js                  \
	     $(LIB)/jqPlot/plugins/jqplot.barRenderer.min.js             \
	     $(LIB)/jqPlot/plugins/jqplot.categoryAxisRenderer.min.js    \
	     $(LIB)/jqPlot/plugins/jqplot.dateAxisRenderer.min.js        \
	     $(LIB)/jqPlot/plugins/jqplot.dragable.min.js                \
	     $(LIB)/jqPlot/plugins/jqplot.enhancedLegendRenderer.min.js  \
	     $(LIB)/jqPlot/plugins/jqplot.highlighter.min.js             \
	     $(LIB)/jqPlot/plugins/jqplot.logAxisRenderer.min.js         \
	     $(LIB)/jqPlot/plugins/jqplot.meterGaugeRenderer.min.js      \
	     $(LIB)/jqPlot/plugins/jqplot.pieRenderer.min.js             \
	     $(LIB)/jqPlot/plugins/jqplot.pointLabels.min.js             \
	     $(LIB)/Gauges/raphael.js                                    \
	     $(LIB)/Gauges/jgauge-0.3.0.a3.js                            \
	     $(LIB)/Gauges/justgage.1.0.1.min.js                         \
	     $(LIB)/Gauges/transform.js                                  \
	     $(LIB)/Gauges/knobKnob.jquery.js                            \
	     $(LIB)/Gauges/flipCounter.js                                > $(BIN)/raswct_lib.js

src: doBin
	@cat $(SRC)/util/GlobalState.js                       \
	     $(SRC)/util/Util.js                              \
	     $(SRC)/util/Constants.js                         \
	     $(SRC)/util/Observable.js                        \
	     $(SRC)/util/Bindable.js                          \
             $(SRC)/util/DataSeries.js                        \
         $(SRC)/util/ConfigManager.js                     \
         $(SRC)/util/ErrorManager.js                      \
         $(SRC)/util/NotificationManager.js               \
         $(SRC)/util/XMLDoc.js                            \
         $(SRC)/util/MultiDimArray.js                     \
         $(SRC)/util/CSVParser.js                         \
         $(SRC)/util/CacheEngine.js                       \
         $(SRC)/util/MapLayer.js                          \
         $(SRC)/query/Transport.js                        \
         $(SRC)/query/Executable.js                       \
         $(SRC)/query/GMLParser.js                        \
         $(SRC)/query/LiteralQuery.js                     \
         $(SRC)/query/RasQuery.js                         \
         $(SRC)/query/WCPSQuery.js                        \
         $(SRC)/query/WCSQuery.js                         \
         $(SRC)/widget/BaseWidget.js                      \
	     $(SRC)/widget/InputWidget.js                     \
	     $(SRC)/widget/OutputWidget.js                    \
	     $(SRC)/widget/Knob.js                            \
	     $(SRC)/widget/Text.js                            \
	     $(SRC)/widget/SpeedoMeter.js                     \
	     $(SRC)/widget/Gauge.js                           \
	     $(SRC)/widget/ToolTip.js                         \
	     $(SRC)/widget/Slider.js                          \
	     $(SRC)/widget/HorizontalSlider.js                \
	     $(SRC)/widget/VerticalSlider.js                  \
	     $(SRC)/widget/BinaryImage.js                     \
	     $(SRC)/widget/Diagram.js                         \
	     $(SRC)/widget/BarDiagram.js                      \
	     $(SRC)/widget/AreaDiagram.js                     \
	     $(SRC)/widget/LinearDiagram.js                   \
	     $(SRC)/widget/ScatterDiagram.js                  \
	     $(SRC)/widget/Map.js                             \
	     $(SRC)/widget/Led.js                             > $(BIN)/raswct_src.js

install:
	@$(MKDIR) -p $(INSTALL_DIR)
	@$(CP) $(BIN)/* $(INSTALL_DIR)
	@echo "The rasdaman web client toolkit has been installed. You can start using it by including raswct.js and raswct.css in your html file"

uninstall:
	@$(RM) $(INSTALL_DIR)
	@echo "The rasdaman web client toolkit was uninstalled"

clean:
	@$(RM) $(BIN)/raswct.js $(BIN)/raswct_src.js $(BIN)/raswct_lib.js $(BIN)/raswct.css
	@echo "The compiled files were removed"

EXTRA_DIST = *
