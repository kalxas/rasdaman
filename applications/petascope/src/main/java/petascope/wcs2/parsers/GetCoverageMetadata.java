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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003 - 2010 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcs2.parsers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import petascope.core.CoverageMetadata;
import petascope.core.DbMetadataSource;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.WCSException;
import petascope.util.CrsUtil;
import petascope.util.ListUtil;
import petascope.util.Pair;
import petascope.util.WcsUtil;
import petascope.util.XMLSymbols;
import petascope.wcps.server.core.Bbox;
import petascope.wcps.server.core.CellDomainElement;
import petascope.wcps.server.core.DomainElement;
import petascope.wcps.server.core.RangeElement;
import petascope.wcs2.extensions.GmlFormatExtension;

/**
 * This class holds the GetCoverage response data.
 *
 * @author <a href="mailto:d.misev@jacobs-university.de">Dimitar Misev</a>
 */
public class GetCoverageMetadata {

    private final CoverageMetadata metadata;
    private final String coverageId;
    private String coverageType;
    private String axisLabels;
    private String uomLabels;
    private String low;     // Grid lower bound (px)
    private String high;    // Grid upper bound (px)
    private String domLow;  // Domain request lower bound
    private String domHigh; // Domain request upper bound
    private String gridType;
    private String gridId;
    private Integer gridDimension;
    private List<RangeField> rangeFields;
    private String crs;     // dynamically adapt to coverage slicing
    private Bbox bbox;

    public GetCoverageMetadata(GetCoverageRequest request, DbMetadataSource meta) throws WCSException {
        coverageId = request.getCoverageId();
        axisLabels = uomLabels = "";
        low = high = "";
        domLow = domHigh = "";
        gridType = gridId = "";

        if (!meta.existsCoverageName(coverageId)) {
            throw new WCSException(ExceptionCode.NoSuchCoverage.locator(coverageId),
                    "One of the identifiers passed does not match with any of the coverages offered by this server");
        }
        metadata = WcsUtil.getMetadata(meta, coverageId) ;
        
        coverageType = metadata.getCoverageType();
        
        // Analyze the grid components and their values
        Iterator<DomainElement> dit = metadata.getDomainIterator();
        Iterator<CellDomainElement> cdit = metadata.getCellDomainIterator();
        while (dit.hasNext() && cdit.hasNext()) {
            DomainElement dom = dit.next();
            CellDomainElement cell = cdit.next();
            axisLabels += dom.getLabel() + " ";
            low  += cell.getLo() + " ";
            high += cell.getHi() + " ";
            domLow  += dom.getMinValue().toPlainString() + " ";
            domHigh += dom.getMaxValue().toPlainString() + " ";
            if (dom.getUom() != null) {
                uomLabels += dom.getUom() + " ";
            }
        }
        
        gridType = coverageType.replace("Coverage", "");
        if (coverageType.equals(XMLSymbols.LABEL_REFERENCEABLE_GRID_COVERAGE)) {
            gridType = XMLSymbols.LABEL_RGBV; // No other grid implementations supported currently
        }
        gridDimension = metadata.getDimension();
        gridId = coverageId + "-grid";
        
        rangeFields = new ArrayList<RangeField>();
        Iterator<RangeElement> rit = metadata.getRangeIterator();
        int i = -1;
        while (rit.hasNext()) {
            RangeElement range = rit.next();
            rangeFields.add(new RangeField(metadata, range, ++i));
        }
        bbox = metadata.getBbox();        
        crs = CrsUtil.CrsUri.createCompound(metadata.getCrsUris());
    }

    public String getAxisLabels() {
        return axisLabels.trim();
    }

    public String getCoverageId() {
        return coverageId;
    }

    public String getCoverageType() {
        return coverageType;
    }

    public Integer getGridDimension() {
        return gridDimension;
    }

    public String getGridId() {
        return gridId;
    }

    public String getGridType() {
        return gridType;
    }

    public String getHigh() {
        return high.trim();
    }

    public String getLow() {
        return low.trim();
    }

    public String getDomHigh() {
        return domHigh.trim();
    }

    public String getDomLow() {
        return domLow.trim();
    }
    public List<RangeField> getRangeFields() {
        return rangeFields;
    }

    public String getUomLabels() {
        return uomLabels.trim();
    }

    public CoverageMetadata getMetadata() {
        return metadata;
    }

    public Bbox getBbox() {
        return bbox;
    }
    
    public String getCrs() {
        return crs;
    }

    public void setAxisLabels(String axisLabels) {
        this.axisLabels = axisLabels;
        setGridDimension(axisLabels.split(" +").length);
    }

    // Update pixel bounds of the grid (upon trimming and slicing)
    public void setHigh(String high) {
        this.high = high;
    }
    public void setLow(String low) {
        this.low = low;
    }
    
    // Update bounds of coverage (upon trimming and slicing)
    public void setDomHigh(String high) {
        domHigh = high;
    }
    public void setDomLow(String low) {
        domLow = low;
    }
    
    public void setGridDimension(Integer gridDimension) {
        this.gridDimension = gridDimension;
    }
    
    public void setCrs(String newUri) {
        crs = newUri;
    }
    
    public void setUomLabels(String newUoms) {
        uomLabels = newUoms;
    }
    
    /**
     * Check if the *output* coverage is an irregular grid.
     * @return True if at least one of the non-sliced axes are irregular.
     */
    public boolean hasIrregularAxis() {
        List<String> labels = new ArrayList<String>();
        labels.addAll(Arrays.asList(axisLabels.split(" +")));
        
        for (String axisLabel : labels) {
            DomainElement domEl = metadata.getDomainByName(axisLabel);
            if (domEl.isIrregular()) {
                return true;
            }
        }
        return false;
    }
    
    public static class RangeField {
        
        private String fieldName;
        private String componentName;
        private String nilValues;
        private String uomCode;
        private String type;
        private String description;

        public RangeField(CoverageMetadata cov, RangeElement range, int i) {
            
            fieldName = range.getName();
            componentName = range.getName();        
            
            Set<String> nullSet = new HashSet<String>();
            nilValues = ListUtil.ltos(nullSet, " ");
            
            type = range.getType();
            uomCode = range.getUom();
            if (uomCode == null) {
                uomCode = CrsUtil.PURE_UOM;
            }
            description = "";
            range.isBoolean();
        }

        public String getDatatype() {
            return GmlFormatExtension.DATATYPE_URN_PREFIX + type;
        }
        
        public String getAllowedValues() {
            if (type.equals("boolean")) {
                return "<swe:value>true</swe:value><swe:value>false</swe:value>";
            } else {
                Pair<String, String> p = WcsUtil.toInterval(type);
                return "<swe:interval>" + p.fst + " " + p.snd + "</swe:interval>";
            }
        }

        public String getFieldName() {
            String ret = fieldName;
            if (fieldName == null) {
                ret = "field";
            } else if (!fieldName.matches("^[a-zA-Z]+.*")) {
                // field name has to start with a letter
                ret = "field" + fieldName;
            }
            return ret;
        }

        public String getDescription() {
            return description;
        }

        public String getUomCode() {
            return uomCode;
        }

        public String getComponentName() {
            return getFieldName();
        }

        public String getNilValues() {
            return nilValues;
        }
    }
}