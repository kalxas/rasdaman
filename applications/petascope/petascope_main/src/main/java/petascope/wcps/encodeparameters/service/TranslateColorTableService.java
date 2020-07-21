/*
  *  This file is part of rasdaman community.
  * 
  *  Rasdaman community is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  * 
  *  Rasdaman community is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  *  See the GNU  General Public License for more details.
  * 
  *  You should have received a copy of the GNU  General Public License
  *  along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
  * 
  *  Copyright 2003 - 2019 Peter Baumann / rasdaman GmbH.
  * 
  *  For more information please see <http://www.rasdaman.org>
  *  or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcps.encodeparameters.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import nu.xom.Element;
import org.rasdaman.domain.wms.Style;
import static petascope.core.XMLSymbols.ATT_COLOR;
import static petascope.core.XMLSymbols.ATT_OPACITY;
import static petascope.core.XMLSymbols.ATT_QUANTITY;
import static petascope.core.XMLSymbols.ATT_TYPE;
import static petascope.core.XMLSymbols.LABEL_COLOR_MAP;
import static petascope.core.XMLSymbols.LABEL_COLOR_MAP_ENTRY;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.util.XMLUtil;
import petascope.wcps.encodeparameters.model.ColorMap;
import petascope.wcps.encodeparameters.model.ColorPalette;
import petascope.wcps.encodeparameters.model.JsonExtraParams;

/**
 * Translate ColorTable (e.g: SLD, GDAL) formats to rasql ColorMap format
 * which can be used as extra parameter in rasql's encode().
 * 
 * http://doc.rasdaman.org/04_ql-guide.html#coloring-arrays
 * 
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
public class TranslateColorTableService {
    
    private static ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Translate a style's color table definition to rasdaman ColorMap format as extra parameter     
     */
    public static void translate(Byte colorTableTypeCode, String colorTableDefinition, JsonExtraParams jsonExtraParams) throws PetascopeException {
        try {
            if (colorTableTypeCode.equals(Style.ColorTableType.ColorMap.getTypeCode())) {
                handleRasdamanColorMap(colorTableDefinition, jsonExtraParams);
            } else if (colorTableTypeCode.equals(Style.ColorTableType.GDAL.getTypeCode())) {
                handleGDALFormat(colorTableDefinition, jsonExtraParams);
            } else if (colorTableTypeCode.equals(Style.ColorTableType.SLD.getTypeCode())) {
                handleSLDFormat(colorTableDefinition, jsonExtraParams);
            }
        } catch (Exception ex) {
            throw new PetascopeException(ExceptionCode.InternalComponentError, 
                                        "Cannot translate color table's definition: " + colorTableDefinition + " to rasdaman ColorMap extra parameter. "
                                        + "Reason: " + ex.getMessage(), ex);
        }
    }
    
    /**
     * Translate a style in WMS SLD format in XML which contains ColorMap element to rasdaman ColorMap extra parameter.
     * 
     * https://docs.geoserver.org/stable/en/user/styling/sld/reference/rastersymbolizer.html#colormap
     */
    private static void handleSLDFormat(String colorTableDefinition, JsonExtraParams jsonExtraParams) throws PetascopeException {
        Element rootElement = XMLUtil.parseXML(colorTableDefinition);
        Element colorMapElement = XMLUtil.firstChildRecursive(rootElement, LABEL_COLOR_MAP);
        
        if (colorMapElement == null) {
            throw new PetascopeException(ExceptionCode.InvalidRequest, "SLD style must contain one " + LABEL_COLOR_MAP + " XML element.");
        }
        
        ColorMap colorMap = new ColorMap();
        
        // e.g: ramp
        String colorMapType = colorMapElement.getAttributeValue(ATT_TYPE);
        if (colorMapType == null) {
            colorMapType = "ramp";
        }
        colorMap.setType(colorMapType);
        
        Map<String, List<Integer>> colorMapValuesMap = new LinkedHashMap<>();
        colorMap.setColorTable(colorMapValuesMap);
        
        List<Element> colorMapEntryElements = XMLUtil.getChildElements(colorMapElement, LABEL_COLOR_MAP_ENTRY);
        int i = 1;
        for (Element colorMapEntryElement : colorMapEntryElements) {
            String color = colorMapEntryElement.getAttributeValue(ATT_COLOR);
            if (color == null) {
                throw new PetascopeException(ExceptionCode.InvalidRequest, LABEL_COLOR_MAP_ENTRY + " " + i + "-th element must contain attribute " + ATT_COLOR);
            }
            String quantity = colorMapEntryElement.getAttributeValue(ATT_QUANTITY);
            if (quantity == null) {
                throw new PetascopeException(ExceptionCode.InvalidRequest, LABEL_COLOR_MAP_ENTRY + " " + i + "-th element must contain attribute " + ATT_QUANTITY);
            }
            
            String opacity = colorMapEntryElement.getAttributeValue(ATT_OPACITY);
            int alphaBandValue = 255;
            if (opacity != null) {
                // e.g: 0.3 * 255
                alphaBandValue = (new BigDecimal(opacity).multiply(new BigDecimal("255"))).intValue();
            }
            
            List<Integer> rgbaColors = convertHexToRGBAColor(color, alphaBandValue);
            colorMapValuesMap.put(quantity, rgbaColors);
            
            i++;
        }
        
        jsonExtraParams.setColorMap(colorMap);
    }
    
    /**
     * Convert a hex color to a RGB color (array with 3 values)
     * e.g: #FF000 -> [255, 0, 0]
     */
    private static List<Integer> convertHexToRGBAColor(String colorStr, int alphaBandValue) {
        List<Integer> results = new ArrayList<>();
        results.add(Integer.valueOf(colorStr.substring(1, 3), 16));
        results.add(Integer.valueOf(colorStr.substring(3, 5), 16));
        results.add(Integer.valueOf(colorStr.substring(5, 7), 16));
        results.add(alphaBandValue);

        return results;        
    }
    
    /**
     * Add a style in GDAL colorPalette format as extra parameter
     * 
     * http://doc.rasdaman.org/04_ql-guide.html#encode (check "colorPalette")
     * 
     * The style's definition must be an JSON object (!), e.g::
     * 
     * { 
     *   "paletteInterp": "RGB",
     *   "colorTable": [[255,0,0,255],[216,31,30,255],...,[43,131,186,255]]
     * }
     */
    private static void handleGDALFormat(String colorTableDefinition, JsonExtraParams jsonExtraParams) throws IOException {
        ColorPalette colorPalette = objectMapper.readValue(colorTableDefinition, ColorPalette.class);
        jsonExtraParams.setColorPalette(colorPalette);
    }
    
    /**
     * Add a style in rasdaman ColorMap format as extra parameter
     * 
     * http://doc.rasdaman.org/04_ql-guide.html#encode (check "colorMap")
     * 
     * The style's definition must be an JSON object (!), e.g::
     * 
     * { 
     *   "type": "values",
     *   "colorTable": {
            "-1": [255, 255, 255, 0],
            "-0.5": [125, 125, 125, 255],
            "1": [0, 0, 0, 255]
          }
     * }
     */
    private static void handleRasdamanColorMap(String colorTableDefinition, JsonExtraParams jsonExtraParams) throws IOException {
        ColorMap colorMap = objectMapper.readValue(colorTableDefinition, ColorMap.class);
        jsonExtraParams.setColorMap(colorMap);
    }
}
