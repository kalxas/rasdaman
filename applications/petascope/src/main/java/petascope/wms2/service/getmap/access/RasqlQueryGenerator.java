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

package petascope.wms2.service.getmap.access;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petascope.wms2.metadata.RasdamanLayer;
import petascope.wms2.service.exception.error.WMSInvalidBbox;
import petascope.wms2.service.exception.error.WMSInvalidDimensionValue;
import petascope.wms2.service.getmap.MergedLayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class generates a rasql query based on a merged layer. The exact mechanism in described inside the class.
 *
 * @author <a href="mailto:dumitru@rasdaman.com">Alex Dumitru</a>
 */
public class RasqlQueryGenerator {

    public RasqlQueryGenerator(MergedLayer mergedLayer) {
        this.mergedLayer = mergedLayer;
    }

    /**
     * Generates the rasql query for the given merged layer
     *
     * @return the rasql query
     * @throws WMSInvalidDimensionValue
     * @throws WMSInvalidBbox
     */
    public String generateQuery() throws WMSInvalidDimensionValue, WMSInvalidBbox {
        String query = QUERY_TEMPLATE;
        query = query.replace("$Select", generateEncodeClause(generateSelectClause()))
                     .replace("$From", generateFromClause())
                     .replace("$Where", generateWhereClause());
        logger.info("Executing rasql query: {}", query);
        return query;
    }

    /**
     * Generates the encode clause of the query
     *
     * @param selectClause the select clause
     * @return the encode clause
     */
    private String generateEncodeClause(@NotNull String selectClause) {
        String encode = ENCODE_CLAUSE;
        encode = encode.replace("$Select", selectClause)
                 .replace("$Format", mergedLayer.getFormat().getRasdamanFormat());
        if (mergedLayer.isTransparent()) {
            encode = encode.replace("$nodata", "nodata=0");
        } else {
            encode = encode.replace("$nodata", "");
        }
        return encode;
    }

    /**
     * Generates the where clause of the query
     *
     * @return the where clause of the query
     */
    private String generateWhereClause() {
        List<String> oidWheres = new ArrayList<String>();
        for (RasdamanLayer layer : mergedLayer.getRasdamanLayers()) {
            String oidWhere = OID_FILTER.replace("$Col", layer.getCollectionName())
                              .replace("$Oid", layer.getOid().toString());
            oidWheres.add(oidWhere);
        }
        return StringUtils.join(oidWheres, OID_FILTER_JOINER);
    }

    /**
     * Generates the from clause of the query
     *
     * @return the from clause
     */
    private String generateFromClause() {
        List<String> colFroms = new ArrayList<String>();
        for (RasdamanLayer layer : mergedLayer.getRasdamanLayers()) {
            colFroms.add(layer.getCollectionName());
        }
        return StringUtils.join(colFroms, COLLECTION_JOINER);
    }

    /**
     * Generates the extend clause of the rasql query
     *
     * @return the extend clause of the rasql query
     * @throws WMSInvalidDimensionValue
     * @throws WMSInvalidBbox
     */
    private String generateExtendClause() throws WMSInvalidDimensionValue, WMSInvalidBbox {
        String extendClause  = EXTEND_CLAUSE;
        String subsets = generateSubsetClause(mergedLayer.getRasdamanExtendSubsets());
        extendClause = extendClause.replace("$Subsets", subsets);
        return extendClause;
    }

    /**
     * Generates the scale clause of the query
     *
     * @param layer the merged layer containing information about output size and requested bounding box
     * @return the scale query
     * @throws WMSInvalidDimensionValue
     * @throws WMSInvalidBbox
     */
    private String generateScaleClause(RasdamanLayer layer) throws WMSInvalidDimensionValue, WMSInvalidBbox {
        String scaleClause  = SCALE_CLAUSE;
        scaleClause = scaleClause.replace("$Extend", generateExtendClause());
        if (layer.getXOrder() == 0) {
            scaleClause = scaleClause.replace("$1Axis", String.valueOf(mergedLayer.getWidth() - 1));
            scaleClause = scaleClause.replace("$2Axis", String.valueOf(mergedLayer.getHeight() - 1));
        } else {
            scaleClause = scaleClause.replace("$1Axis", String.valueOf(mergedLayer.getHeight() - 1));
            scaleClause = scaleClause.replace("$2Axis", String.valueOf(mergedLayer.getWidth() - 1));
        }
        return scaleClause;
    }

    /**
     * Generates the select clause and returns it
     *
     * @return the select clause
     * @throws WMSInvalidDimensionValue
     * @throws WMSInvalidBbox
     */
    private String generateSelectClause() throws WMSInvalidDimensionValue, WMSInvalidBbox {
        List<String> selects = new ArrayList<String>();
        List<RasdamanSubset> subsets = mergedLayer.getRasdamanSubsets();
        String subset = generateSubsetClause(subsets);
        int layerPosition = 0;
        for (RasdamanLayer layer : mergedLayer.getRasdamanLayers()) {
            String select = generateScaleClause(layer);
            String col = layer.getCollectionName() + subset;
            select = select.replace("$Col", col);

            select = addStyleToSelectClause(select, layerPosition);
            layerPosition += 1;
            selects.add(select);
        }
        String selectStr = StringUtils.join(selects, SELECT_JOINER);
        if (mergedLayer.projection()) {
            selectStr = PROJECTION + "(" + selectStr + "," + mergedLayer.getProjectionStr() + ")";
        }
        return selectStr;
    }

    /**
     * Adds the style rasql fragment to the select clause
     *
     * @param select the select clause of the rasql query
     * @param stylePosition the position of the style in the list of layer styles
     * @return the styles select clause
     */
    private String addStyleToSelectClause(String select, int stylePosition) {
        if (mergedLayer.getStyles().size() > stylePosition) {
            String rasqlStylePart = mergedLayer.getStyles().get(stylePosition).getRasqlQueryTransformer();
            if (rasqlStylePart.equals("")) {
                return select;
            }
            return rasqlStylePart.replace(STYLE_SELECT_TOKEN, select);
        }
        return select;
    }

    /**
     * Generates the subset clause of the query e.g. [0:100,0:20]
     *
     * @return the subset clause
     * @throws WMSInvalidDimensionValue
     * @throws WMSInvalidBbox
     */
    private String generateSubsetClause(List<RasdamanSubset> subsets) throws WMSInvalidDimensionValue, WMSInvalidBbox {
        String subset = SUBSET_START;
        List<String> dimSubsets = new ArrayList<String>();
        Collections.sort(subsets);
        for (RasdamanSubset rasdamanSubset : subsets) {
            dimSubsets.add(String.valueOf(rasdamanSubset.getMin()) + SUBSET_DIMENSION_INTERVAL_SPLIT + String.valueOf(rasdamanSubset.getMax()));
        }
        subset += StringUtils.join(dimSubsets, SUBSET_JOINER);
        subset += SUBSET_END;
        return subset;
    }


    private final MergedLayer mergedLayer;
    private final static String OID_FILTER = "oid($Col) = $Oid";
    private final static String OID_FILTER_JOINER = " AND ";
    private final static String COLLECTION_JOINER = ",";
    private final static String SELECT_JOINER = " OVERLAY ";
    private final static String EXTEND_CLAUSE = "EXTEND($Col, $Subsets)";
    private final static String SCALE_CLAUSE = "SCALE($Extend, [0:$1Axis, 0:$2Axis])";
    private final static String ENCODE_CLAUSE = "ENCODE($Select, \"$Format\", \"$nodata\")";
    private final static String SUBSET_JOINER = ",";
    private final static String SUBSET_DIMENSION_INTERVAL_SPLIT = ":";
    private final static String SUBSET_START = "[";
    private final static String SUBSET_END = "]";
    private final static String PROJECTION = "project";
    private final static String QUERY_TEMPLATE = "SELECT $Select FROM $From WHERE $Where";
    private static final Logger logger = LoggerFactory.getLogger(RasqlQueryGenerator.class);
    private static final String STYLE_SELECT_TOKEN = "$Iterator";
}
