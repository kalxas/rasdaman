/*
* This file is part of rasdaman community.
*
* Rasdaman community is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Rasdaman community is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU  General Public License for more details.
*
* You should have received a copy of the GNU  General Public License
* along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
*
* Copyright 2003 - 2014 Peter Baumann / rasdaman GmbH.
*
* For more information please see <http://www.rasdaman.org>
* or contact Peter Baumann via <baumann@rasdaman.com>.
*/
package petascope.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.WCPSException;
import petascope.wcps.server.core.CoverageExpr;
import petascope.wcps.server.core.CoverageInfo;
import petascope.wcps.server.core.ExtendCoverageExpr;
import petascope.wcps.server.core.IRasNode;
import petascope.wcps.server.core.TrimCoverageExpr;

/**
 *
 * @author pcampa
 */
/**
 * Inner class which gathers the required geo-parameters for GTiff/JPEG2000 encoding.
 */
public class GdalParameters {

    private static final Logger log = LoggerFactory.getLogger(GdalParameters.class);

    /* Encoding parameters */
    private static final String CRS_PARAM  = "crs";
    private static final String XMAX_PARAM = "xmax";
    private static final String XMIN_PARAM = "xmin";
    private static final String YMAX_PARAM = "ymax";
    private static final String YMIN_PARAM = "ymin";
    private static final String CONFIG_PARAM = "config";
    private static final char PS    = ';'; // parameter separator
    private static final char PKVS  = '='; // parameter key-value separator
    private static final char COS   = ','; // configuration options separator
    private static final char COKVS = ' '; // configuration options key-value separator

    /* Members */
    // Creation options (extraParams)
    /**
     * GDAL creation option for the codec.
     * In JP2OpenJPEG there are two alternatives: JP2/J2K, the latter not supporting GMLJP2.
     */
    public static final String GDAL_CODEC_PARAM = "CODEC";
    // ...
    // vars
    private double lowX;
    private double highX;
    private double lowY;
    private double highY;
    private String crs;
    private Map<String,String> configOptions;
    private String extraParams; // any other param GDAL supports

    /* Constructors */
    // Unreferenced gml:Grid
    public GdalParameters() {
        lowX  = 0.0D;
        highX = 0.0D;
        lowY  = 0.0D;
        highY = 0.0D;
        crs   = "";
        configOptions = new HashMap<String,String>();
    }
    // Georeferenced gml:RectifiedGrid
    public GdalParameters(double xMin, double xMax, double yMin, double yMax, String crs) {
        lowX  = xMin;
        highX = xMax;
        lowY  = yMin;
        highY = yMax;
        this.crs = crs;
        configOptions = new HashMap<String,String>();
    }
    public GdalParameters(String xMin, String xMax, String yMin, String yMax, String crs) {
        this(Double.parseDouble(xMin), Double.parseDouble(xMax),
                Double.parseDouble(yMin), Double.parseDouble(yMax), crs);
    }

    /**
     * Returns the bounds of the requested coverage with trim-updates and
     * letting out sliced dimensions.
     * Dimensionality of the bounds is checked against the DIM argument.
     *
     * @param queryRoot The root node of the XML query, used to fetch trims
     * and slices
     * @param expectedDim expected dimensionality of queryRoot
     * @throws WCPSException
     */
    public GdalParameters(CoverageExpr queryRoot, Integer expectedDim) throws WCPSException {
        CoverageInfo info = queryRoot.getCoverageInfo();

        if (info != null) {
            // (order of subset) not necessarily = (order of coverage axes)
            Map<Integer, String> orderToName = new HashMap<Integer, String>();
            // axis name -> geo bounds
            Map<String, Double[]> nameToBounds = new HashMap<String, Double[]>();

            // Fetch the operations which change the geo bounding box (trim/extend)
            List<IRasNode> subsets = MiscUtil.childrenOfTypes(queryRoot, TrimCoverageExpr.class, ExtendCoverageExpr.class);

            // Check each dimension: slice->discard, trim/extend->setBounds, otherwise set bbox bounds
            for (int i = 0; i < info.getNumDimensions(); i++) {
                String dimName = info.getDomainElement(i).getLabel();

                // The dimension is surely in the output
                if (!queryRoot.isSlicedAxis(dimName)) {
                    orderToName.put(info.getDomainIndexByName(dimName), dimName);

                    // Set the bounds of this dimension: total bbox first, then update in case of trims in the request
                    nameToBounds.put(dimName, new Double[] {
                        info.getDomainElement(info.getDomainIndexByName(dimName)).getMinValue().doubleValue(),
                        info.getDomainElement(info.getDomainIndexByName(dimName)).getMaxValue().doubleValue()
                    });

                    // reduce or extend the bbox according to the subset ops applied to the coverage
                    for (IRasNode subset : subsets) {
                        Double[] subsetBounds = null;
                        if (subset instanceof TrimCoverageExpr) {
                            if (((TrimCoverageExpr)subset).trimsDimension(dimName)) {
                                // Set bounds specified in the trim (themselves trimmed by bbox values)
                                subsetBounds = ((TrimCoverageExpr)subset).trimmingValues(dimName);
                            }
                        }
                        if (subset instanceof ExtendCoverageExpr) {
                            if (((ExtendCoverageExpr)subset).extendsDimension(dimName)) {
                                // Set bounds specified in the trim (themselves trimmed by bbox values)
                                subsetBounds = ((ExtendCoverageExpr)subset).extendingValues(dimName);
                            }
                        }
                        if (subsetBounds != null && subsetBounds.length > 0) {
                            nameToBounds.remove(dimName);
                            nameToBounds.put(dimName, subsetBounds);
                        }
                    }
                }
            }

            // Check dimensions is exactly 2:
            if (orderToName.size() != expectedDim) {
                String message = "The number of output dimensions " + orderToName.size()
                        + " does not match the expected dimensionality: " + expectedDim;
                log.error(message);
                throw new WCPSException(ExceptionCode.InvalidRequest, message);
            }

            // Set the bounds in the proper order (according to the order of the axes in the coverage
            Double[] dom1 = nameToBounds.get(orderToName.get(Collections.min(orderToName.keySet())));
            Double[] dom2 = nameToBounds.get(orderToName.get(Collections.max(orderToName.keySet())));

            // Output: min1, max1, min2, max2
            lowX = dom1[0];
            highX = dom1[1];
            lowY = dom2[0];
            highY = dom2[1];

            // init other params
            crs = "";
            configOptions = new HashMap<String,String>();
        }
    }

    // Interface (read/write)
    public double getXmin() {
        return lowX;
    }
    public double getXmax() {
        return highX;
    }
    public double getYmin() {
        return lowY;
    }
    public double getYmax() {
        return highY;
    }
    public String getCrs() {
        return (null==crs) ? "" : crs;
    }
    public void setCrs(String crs) {
        this.crs = crs;
    }
    public Iterator<String> getConfigOptionsKeysIterator() {
        return configOptions.keySet().iterator();
    }
    public String fetchConfigValue(String key) {
        return configOptions.containsKey(key) ? configOptions.get(key) : "";
    }
    public String getExtraParams() {
        return extraParams;
    }
    public void addExtraParams(String extraParameters) {
        extraParams = extraParameters;
    }
    /**
     * Add configuration KV-pair for GDAL.
     * Unset a configuration option by specifying null or empty value.
     * All values in the map are ensured not to be null or empty.
     * @param key
     * @param value
     * @return The previous value associated with key, or null if there was no mapping for key.
     */
    public String setConfigKeyValue(String key, String value) {
        // NOTE: existing KV pairs will be overwritten on multiple calls
        // If value is empty, the associated key is removed
        if (null == key || !key.isEmpty()) {
            if (null == value || !value.isEmpty()) {
                return configOptions.put(key, value);
            } else {
                return configOptions.remove(key);
            }
        }
        return null;
    }

    @Override
    public String toString() {
        String ret = null;
        if (crs != null && !CrsUtil.GRID_CRS.equals(crs)) {
            ret = appendToParamList(appendToParamList(appendToParamList(appendToParamList(appendToParamList(
                    extraParams,
                    XMIN_PARAM, lowX + ""),
                    XMAX_PARAM, highX + ""),
                    YMIN_PARAM, lowY + ""),
                    YMAX_PARAM, highY + ""),
                    CRS_PARAM, CrsUtil.CrsUri.getAuthority(crs) + ":" + CrsUtil.CrsUri.getCode(crs));
                    // FIXME: pass on the WKT form of CRS definition (not AUTH:CODE) otherwise non-conventional CRSs are skipped by GDAL.
        } else {
            // return empty in case of CRS:1
            ret = appendToParamList(extraParams, null, null);
        }

        // config options
        String cOptionKVPairsString = printConfigOptions();
        if (!cOptionKVPairsString.isEmpty()) {
            ret = appendToParamList(ret, CONFIG_PARAM, cOptionKVPairsString);
        }
        return ret;
    }

    /**
     * Append key-value pair to existing extra parameters.
     * Takes into account if any argument is null.
     *
     * @return extraParams with key-value accordingly appended.
     */
    private String appendToParamList(String currentParamList, String key, String value) {
        String newParam = (key == null || value == null) ? "" : key + PKVS + value;
        String ret = null;

        if (currentParamList == null || currentParamList.length() == 0) {
            ret = newParam;
        } else {
            String uniformCurrentParamList = PS + currentParamList + PKVS;
            if (uniformCurrentParamList.toLowerCase().contains(PS + key + PKVS)) {
                ret = currentParamList; // don't override if key is already supplied by user
            } else {
                ret = currentParamList + PS + newParam;
            }
        }

        return ret;
    }

    /**
     * Print configuration options as understood by RasQL encode() function.
     * @see http://rasdaman.org/wiki/RasqlEncodeFunction
     * @return The list of configuration options: "config=key1 value1, key2 value2, __, keyN valueN".
     */
    private String printConfigOptions() {
        if (configOptions.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (String key : configOptions.keySet()) {
            sb.append(key);
            sb.append(COKVS);
            sb.append(configOptions.get(key));
            sb.append(COS);
        }
        sb = sb.deleteCharAt(sb.lastIndexOf(String.valueOf(COS)));
        return sb.toString();
    }
}
