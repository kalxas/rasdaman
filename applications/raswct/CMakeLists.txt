# This file is part of rasdaman community.
#
# Rasdaman community is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# Rasdaman community is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
#
# Copyright 2003-2016 Peter Baumann /
# rasdaman GmbH.
#
# For more information please see <http://www.rasdaman.org>
# or contact Peter Baumann via <baumann@rasdaman.com>.
#
###################################################################

set(SRC "${CMAKE_CURRENT_SOURCE_DIR}/src")
set(BIN "${CMAKE_CURRENT_SOURCE_DIR}/bin")
set(LIB "${CMAKE_CURRENT_SOURCE_DIR}/lib")
set(INSTALL_DIR "${SHARE_DATA_DIR}/raswct")
set(RASQL_SHARE_DIR "${SHARE_DATA_DIR}/www/rasql-web-console")

set(BUILD_DIR "${CMAKE_CURRENT_BINARY_DIR}/build")
set(BUILD_IMG_DIR "${BUILD_DIR}/img")

# Create the directory into which to place the built files.
file(MAKE_DIRECTORY ${BUILD_DIR})
file(MAKE_DIRECTORY ${BUILD_IMG_DIR})

set(CSS_SOURCES "${LIB}/jqPlot/jquery.jqplot.css"
        "${LIB}/openlayers/style.css"
        "${LIB}/Gauges/css/jgauge.css"
        "${LIB}/TwitterBootstrap/bootstrap.css"
        "${LIB}/Gauges/css/styles.css"
        "${LIB}/Gauges/css/knobKnob.css")

set(LIB_SOURCES "${LIB}/Flanche/FlancheJs.js"
        "${LIB}/Underscore/underscore.min.js"
        "${LIB}/jQuery/jQuery.min.js"
        "${LIB}/TwitterBootstrap/Bootstrap.js"
        "${LIB}/EarthServer/EarthServerGenericClient.js"
        "${LIB}/X3DOM/x3dom.js"
        "${LIB}/jqueryui/jquery.ui.js"
        "${LIB}/openlayers/OpenLayers.js"
        "${LIB}/jqPlot/jquery.jqplot.min.js"
        "${LIB}/jqPlot/plugins/jqplot.canvasAxisLabelRenderer.min.js"
        "${LIB}/jqPlot/plugins/jqplot.canvasAxisTickRenderer.min.js"
        "${LIB}/jqPlot/plugins/jqplot.canvasOverlay.js"
        "${LIB}/jqPlot/plugins/jqplot.categoryAxisRenderer.min.js"
        "${LIB}/jqPlot/plugins/jqplot.canvasTextRenderer.min.js"
        "${LIB}/jqPlot/plugins/jqplot.cursor.min.js"
        "${LIB}/jqPlot/plugins/jqplot.barRenderer.min.js"
        "${LIB}/jqPlot/plugins/jqplot.categoryAxisRenderer.min.js"
        "${LIB}/jqPlot/plugins/jqplot.dateAxisRenderer.min.js"
        "${LIB}/jqPlot/plugins/jqplot.dragable.min.js"
        "${LIB}/jqPlot/plugins/jqplot.enhancedLegendRenderer.min.js"
        "${LIB}/jqPlot/plugins/jqplot.highlighter.min.js"
        "${LIB}/jqPlot/plugins/jqplot.logAxisRenderer.min.js"
        "${LIB}/jqPlot/plugins/jqplot.meterGaugeRenderer.min.js"
        "${LIB}/jqPlot/plugins/jqplot.pieRenderer.min.js"
        "${LIB}/jqPlot/plugins/jqplot.pointLabels.min.js"
        "${LIB}/Gauges/raphael.js"
        "${LIB}/Gauges/jgauge-0.3.0.a3.js"
        "${LIB}/Gauges/justgage.1.0.1.min.js"
        "${LIB}/Gauges/transform.js"
        "${LIB}/Gauges/knobKnob.jquery.js"
        "${LIB}/Gauges/flipCounter.js")

set(SRC_SOURCES "${SRC}/util/GlobalState.js"
        "${SRC}/util/Util.js"
        "${SRC}/util/Constants.js"
        "${SRC}/util/Observable.js"
        "${SRC}/util/Bindable.js"
        "${SRC}/util/DataSeries.js"
        "${SRC}/util/ConfigManager.js"
        "${SRC}/util/ErrorManager.js"
        "${SRC}/util/NotificationManager.js"
        "${SRC}/util/XMLDoc.js"
        "${SRC}/util/MultiDimArray.js"
        "${SRC}/util/CSVParser.js"
        "${SRC}/util/CacheEngine.js"
        "${SRC}/util/MapLayer.js"
        "${SRC}/query/Transport.js"
        "${SRC}/query/Executable.js"
        "${SRC}/query/GMLParser.js"
        "${SRC}/query/LiteralQuery.js"
        "${SRC}/query/RasQuery.js"
        "${SRC}/query/WCPSQuery.js"
        "${SRC}/query/WCSQuery.js"
        "${SRC}/widget/BaseWidget.js"
        "${SRC}/widget/InputWidget.js"
        "${SRC}/widget/OutputWidget.js"
        "${SRC}/widget/Knob.js"
        "${SRC}/widget/Text.js"
        "${SRC}/widget/SpeedoMeter.js"
        "${SRC}/widget/Gauge.js"
        "${SRC}/widget/ToolTip.js"
        "${SRC}/widget/Slider.js"
        "${SRC}/widget/HorizontalSlider.js"
        "${SRC}/widget/VerticalSlider.js"
        "${SRC}/widget/BinaryImage.js"
        "${SRC}/widget/Diagram.js"
        "${SRC}/widget/BarDiagram.js"
        "${SRC}/widget/AreaDiagram.js"
        "${SRC}/widget/LinearDiagram.js"
        "${SRC}/widget/ScatterDiagram.js"
        "${SRC}/widget/Map.js"
        "${SRC}/util/BinderManager.js"
        "${SRC}/util/Binders.js"
        "${SRC}/widget/Led.js"
        "${SRC}/widget/RasqlConsole.js")

# Get all the images in the bin directory

file(GLOB IMAGE_FILES_PATH ${BIN}/img/*)

set(IMAGE_FILES ${IMAGE_FILES_PATH}
        "${LIB}/Gauges/img/jgauge_face_default.png"
        "${LIB}/Gauges/img/jgauge_needle_default.png"
        "${LIB}/Gauges/img/knob.png")

set(IMAGE_FILES ${IMAGE_FILES_PATH}
        "${LIB}/TwitterBootstrap/img/glyphicons-halflings.png"
        "${LIB}/TwitterBootstrap/img/glyphicons-halflings-white.png")

set(BUILT_IMAGES "")
foreach (IMAGE_FILE ${IMAGE_FILES_PATH})
    get_filename_component(IMAGE_FILE_NAME ${IMAGE_FILE} NAME)

    add_custom_command(OUTPUT "${BUILD_IMG_DIR}/${IMAGE_FILE_NAME}"
            COMMAND ${CMAKE_COMMAND} -E copy ${IMAGE_FILE} "${BUILD_IMG_DIR}/${IMAGE_FILE_NAME}"
            DEPENDS ${IMAGE_FILE}
            VERBATIM)

    set(BUILT_IMAGES ${BUILT_IMAGES} "${BUILD_IMG_DIR}/${IMAGE_FILE_NAME}")
endforeach ()

find_package(CAT REQUIRED)

add_custom_command(OUTPUT "${BUILD_DIR}/raswct.css"
        COMMAND ${CAT_EXECUTABLE}
        ARGS ${CSS_SOURCES} > "${BUILD_DIR}/raswct.css"
        DEPENDS ${CSS_SOURCES}
        VERBATIM)

add_custom_command(OUTPUT "${BUILD_DIR}/raswct_lib.js"
        COMMAND ${CAT_EXECUTABLE}
        ARGS ${LIB_SOURCES} > "${BUILD_DIR}/raswct_lib.js"
        DEPENDS ${LIB_SOURCES}
        VERBATIM)

add_custom_command(OUTPUT "${BUILD_DIR}/raswct_src.js"
        COMMAND ${CAT_EXECUTABLE}
        ARGS ${SRC_SOURCES} > "${BUILD_DIR}/raswct_src.js"
        DEPENDS ${SRC_SOURCES}
        VERBATIM)

add_custom_command(OUTPUT "${BUILD_DIR}/raswct.js"
        COMMAND ${CAT_EXECUTABLE}
        ARGS "${BUILD_DIR}/raswct_lib.js" "${BUILD_DIR}/raswct_src.js" > "${BUILD_DIR}/raswct.js"
        DEPENDS "${BUILD_DIR}/raswct_lib.js" "${BUILD_DIR}/raswct_src.js"
        VERBATIM)

add_custom_target(raswct ALL
        DEPENDS "${BUILD_DIR}/raswct.css"
        "${BUILD_DIR}/raswct_lib.js"
        "${BUILD_DIR}/raswct_src.js"
        "${BUILD_DIR}/raswct.js"
        "${BUILT_IMAGES}")

# Install the contents of the build directory to the destination.

install(DIRECTORY "${BUILD_DIR}/"
        DESTINATION ${INSTALL_DIR})

install(FILES     "${BUILD_DIR}/raswct.css"
        DESTINATION "${RASQL_SHARE_DIR}/css")

install(FILES   "${BUILD_DIR}/raswct.js"
                "${BUILD_DIR}/raswct_src.js"
                "${BUILD_DIR}/raswct_lib.js"
        DESTINATION "${RASQL_SHARE_DIR}/js")
