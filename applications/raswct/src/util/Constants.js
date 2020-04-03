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
 * Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */

/**
 * This file contains all the constants needed across the toolkit
 * @module {Rj.util}
 * @author Alex Dumitru <m.dumitru@jacobs-university.de>
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @version 3.0.0
 */

FlancheJs.defineClass("Rj.util.Constants", {
  init   :function () {
    throw Error("This should not be initialized");
  },
  statics:{
    requestDelay: 200,
    errorMessageTitle             :"Error",
    errorMessageButtonText        :"Dismiss",
    serviceUnavailableErrorMessage:"The request was unsuccessful. Please check that the service that was accessed is up and running.",
    serviceErrorMessage:"The request was unsuccessful. The server detected an error. Server Response: ",
    ajaxErrorMessage              :"The ajax request failed. Please contact a developer",
    textRows : 5,
    textCols : 200,
    textSubmitTex : 'Submit',
    wcpsQueryPlaceHolder: "{RaswctQueryPlaceholder}",
    templates                          :{
      wcpsRequestTemplate:'<?xml version="1.0" encoding="UTF-8" ?>\
<ProcessCoveragesRequest xmlns="http://www.opengis.net/wcps/1.0" service="WCPS" version="1.0.0">\
  <query>\
    <abstractSyntax>\
      {RaswctQueryPlaceholder}\
    </abstractSyntax>\
  </query>\
</ProcessCoveragesRequest>'
    },
    knobColors:  [
    '26e000','2fe300','37e700','45ea00','51ef00',
    '61f800','6bfb00','77ff02','80ff05','8cff09',
    '93ff0b','9eff09','a9ff07','c2ff03','d7ff07',
    'f2ff0a','fff30a','ffdc09','ffce0a','ffc30a',
    'ffb509','ffa808','ff9908','ff8607','ff7005',
    'ff5f04','ff4f03','f83a00','ee2b00','e52000'
    ],
    knobMin: 0,
    knobMax: 100,
    knobSnap: 1,
    knobReverse: false,
    sliderVertical: "vertical",
    sliderHorizontal: "horizontal",
    sliderMin: 0,
    sliderMax: 1,
    sliderStep: 1,
    sliderRange: false,
    sliderLabel: '',
    ledValue: 0,
    ledIntDigits: 4,
    ledFracDigits: 2,
    ledDigitClass: "counter-digit",
    ledCounterFieldName: "counter-value",
    ledDigitHeight: 40,
    ledDigitWidth: 30,
    ledImagePath:_.getToolkitPath() + "img/flipCounter-medium.png",
    ledDuration: 5000,
    ledChangeDuration: 300,
    speedoMeterValue: 0,
    speedoMeterLabelSuffix: '',
    speedoMeterImage: _.getToolkitPath() + 'img/jgauge_face_default.png',
    speedoMeterNeedleImage: _.getToolkitPath() + 'img/jgauge_needle_default.png',
    gaugeValue: 0,
    gaugeMin: 0,
    gaugeMax: 100,
    gaugeLabel: ' ',
    gaugeTitle: ' ',
    gaugeWidthScale: 1,
    gaugeTitleColor: '#999999',
    gaugeValueColor: '#999999',
    gaugeLabelColor: '#999999',
    gaugeColor: '#edebeb',
    gaugeShowMinMax: true,
    gaugeShadowOpacity: 1,
    gaugeShadowSize: 0,
    gaugeShadowOffset: 10,
    gaugeHeight: 200,
    toolTipValue: ' ',
    toolTipPretext: ' ',
    toolTipPostext: ' ',
    toolTipAdjust: {},
    toolTipPlace: "bottom",
    toolTipMouse: false,
    toolTipDelay: 10,
    dataSeriesColors: [ 
    '#0000AA', '#00AA00', '#AA0000', '#A0A0A0', '#CCBBAA',
    '#26e000', '#2fe300', '#37e700', '#45ea00', '#51ef00',
    '#61f800', '#6bfb00', '#77ff02', '#80ff05', '#8cff09',
    '#93ff0b', '#9eff09', '#a9ff07', '#c2ff03', '#d7ff07',
    ],
    diagramTitle: '',
    diagramXlabel: 'X',
    diagramYlabel: 'Y',
    diagramTooltip: false,
    diagramTipTitle: 'Diagram Tip',
    diagramTipText: 'You can restore the zoom level to its initial value by double clicking inside the diagram.',
    barDiagramLineWidth: 1,
    diagramWidth: '600',
    diagramHeight: '300',
    mapWidth: '600',
    mapHeight: '300',
    imgPath: _.getToolkitPath() + 'img/'
  }
})