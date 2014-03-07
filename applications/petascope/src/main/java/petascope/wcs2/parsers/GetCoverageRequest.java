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
package petascope.wcs2.parsers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.WCSException;
import petascope.util.ListUtil;
import petascope.util.Pair;
import petascope.util.TimeUtil;
import petascope.wcs2.extensions.FormatExtension;
import petascope.wcs2.helpers.rangesubsetting.RangeSubset;

/**
 * A GetCoverage request object, populated by a parser.
 *
 * <element name="GetCoverage" type="wcs:GetCoverageType"> <annotation>
 * <documentation>Request to a WCS to perform the GetCoverage operation. This
 * operation allows a client to retrieve a subset of one coverage.
 * </documentation> </annotation> </element> <complexType
 * name="GetCoverageType"> <complexContent> <extension
 * base="wcs:RequestBaseType"> <sequence> <element ref="wcs:CoverageId">
 * <annotation> <documentation>Identifier of the coverage that this GetCoverage
 * operation request shall draw from. </documentation> </annotation> </element>
 * <element ref="wcs:DimensionSubset" minOccurs="0" maxOccurs="unbounded"/>
 * </sequence> </extension> </complexContent> </complexType>
 *
 * <!-- ======================================================= --> <!-- Domain
 * subset types and elements --> <!--
 * ======================================================= --> <element
 * name="DimensionSubset" type="wcs:DimensionSubsetType" abstract="true">
 * <annotation> <documentation>Definition of the desired subset of the domain of
 * the coverage. This is either a Trim operation, or a Slice
 * operation.</documentation> </annotation> </element> <complexType
 * name="DimensionSubsetType" abstract="true"> <sequence> <element
 * name="Dimension" type="NCName"/> </sequence> </complexType> <!--
 * ======================================================= --> <element
 * name="DimensionTrim" type="wcs:DimensionTrimType"
 * substitutionGroup="wcs:DimensionSubset"> <annotation>
 * <documentation>Describes the trimming of a coverage's domain axis, between
 * two values.</documentation> </annotation> </element> <complexType
 * name="DimensionTrimType"> <complexContent> <extension
 * base="wcs:DimensionSubsetType"> <sequence> <element name="TrimLow"
 * type="double" minOccurs="0"/> <element name="TrimHigh" type="double"
 * minOccurs="0"/> </sequence> </extension> </complexContent> </complexType>
 * <!-- ======================================================= --> <element
 * name="DimensionSlice" type="wcs:DimensionSliceType"
 * substitutionGroup="wcs:DimensionSubset"> <annotation>
 * <documentation>Describes the slicing of a coverage's domain axis at a
 * particular point.</documentation> </annotation> </element> <complexType
 * name="DimensionSliceType"> <complexContent> <extension
 * base="wcs:DimensionSubsetType"> <sequence> <element name="SlicePoint"
 * type="double"/> </sequence> </extension> </complexContent> </complexType>
 *
 * @author <a href="mailto:d.misev@jacobs-university.de">Dimitar Misev</a>
 */
public class GetCoverageRequest extends BaseRequest {

    public static final String ASTERISK = "*";
    public static final String QUOTED_SUBSET = "^\".*\"$"; // switch from numeric to ISO8601 coordinates for time

    private final String coverageId;
    private final String format;
    private final boolean multipart;
    private final List<DimensionSubset> subsets;
    private final RangeSubset rangeSubset;
    private final Scaling scale;
    private final CrsExt crsExt;

    public GetCoverageRequest(String coverageId) {
        this(coverageId, FormatExtension.MIME_GML, false); // GML is default
    }

    public GetCoverageRequest(String coverageId, String format, boolean multipart) {
        this.coverageId = coverageId;
        this.format = (null == format || format.isEmpty()) ? FormatExtension.MIME_GML : format ; // GML is default
        this.multipart = multipart;
        this.subsets = new ArrayList<DimensionSubset>();
        this.rangeSubset = new RangeSubset();
        this.scale = new Scaling();
        crsExt = new CrsExt();
    }

    public String getCoverageId() {
        return coverageId;
    }

    public List<DimensionSubset> getSubsets() {
        return subsets;
    }

    public void addSubset(DimensionSubset sub) {
        subsets.add(sub);
    }

    public DimensionSubset getSubset(String dim) {
        ListIterator<DimensionSubset> it = subsets.listIterator();
        while (it.hasNext()) {
            if (dim.equals(it.next().getDimension())) {
                it.previous();
                return it.next();
            }
        }
        return null;
    }

    public String getFormat() {
        return format;
    }

    public boolean isMultipart() {
        return multipart;
    }

    public CrsExt getCrsExt() {
        return crsExt;
    }

    public RangeSubset getRangeSubset() {
        return rangeSubset;
    }

    public Scaling getScaling() {
        return scale;
    }

    public boolean isScaled() {
        return scale.isSet();
    }

    public boolean hasRangeSubsetting(){
        return !this.rangeSubset.isEmpty();
    }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder();
        ret.append("Coverage: ").append(coverageId).append(", Subsets: ").append(ListUtil.ltos(subsets, ", "));
        if(!this.rangeSubset.isEmpty()){
            ret.append(this.rangeSubset.toString());
        }
        return ret.toString();
    }

    public class DimensionSubset {

        protected final String dimension;
        //protected final String crs;
        protected String crs;
        protected boolean isNumeric = true;

        public DimensionSubset(String dimension) {
            this(dimension, null);
        }

        public DimensionSubset(String dimension, String crs) {
            this.dimension = dimension;
            this.crs = crs;
        }

        public DimensionSubset(String dimension, String crs, boolean isNumeric) {
            this.dimension = dimension;
            this.crs = crs;
            this.isNumeric = isNumeric;
        }

        public String getDimension() {
            return dimension;
        }

        public String getCrs() {
            return crs;
        }

        // When transforming a subset, change crs accordingly
        public void setCrs(String value) {
            crs = value;
        }

        public boolean isNumeric() {
            return isNumeric;
        }

        @Override
        public String toString() {
            return dimension + ((crs != null) ? "," + crs : "");
        }
    }

    public class DimensionTrim extends DimensionSubset {

        //private final String trimLow;
        //private final String trimHigh;
        private String trimLow;
        private String trimHigh;

        public DimensionTrim(String dimension, String trimLow, String trimHigh) {
            this(dimension, null, trimLow, trimHigh);
        }

        public DimensionTrim(String dimension, String crs, String trimLow, String trimHigh) {
            super(dimension, crs);
            this.trimLow = trimLow;
            this.trimHigh = trimHigh;
            isNumeric = !trimLow.matches(QUOTED_SUBSET) && !trimHigh.matches(QUOTED_SUBSET);
        }

        public String getTrimHigh() {
            return trimHigh;
        }

        public String getTrimLow() {
            return trimLow;
        }

        /**
         * @param value Set new lower bound to 1D domain (due to a CrsExt transformation).
         */
        public void setTrimLow(String value) {
            trimLow = value;
            isNumeric = !trimLow.matches(QUOTED_SUBSET);
        }

        public void setTrimLow(Double value) {
            setTrimLow(value.toString());
            isNumeric = true;
        }

        public void setTrimLow(Integer value) {
            setTrimLow(value.toString());
            isNumeric = true;
        }

        /**
         * @param value Set new upper bound to 1D domain (due to a CrsExt transformation).
         */
        public void setTrimHigh(String value) {
            trimHigh = value;
            isNumeric = !trimHigh.matches(QUOTED_SUBSET);
        }

        public void setTrimHigh(Double value) {
            setTrimHigh(value.toString());
            isNumeric = true;
        }

        public void setTrimHigh(Integer value) {
            setTrimHigh(value.toString());
            isNumeric = true;
        }

        /**
         * Integrity of time subsets (quoted subsets): valid and ordered bounds.
         * @throws WCSException
         */
        public void timestampSubsetCheck() throws WCSException {
            if (null != getTrimLow() && getTrimLow().matches(QUOTED_SUBSET)) {
                if (!TimeUtil.isValidTimestamp(getTrimLow())) {
                    throw new WCSException(ExceptionCode.InvalidParameterValue, "Timestamp \"" + getTrimLow() + "\" is not valid or supported.");
                }
            }
            if (null != getTrimHigh() && getTrimHigh().matches(QUOTED_SUBSET)) {
                if (!TimeUtil.isValidTimestamp(getTrimHigh())) {
                    throw new WCSException(ExceptionCode.InvalidParameterValue, "Timestamp \"" + getTrimHigh() + "\" is not valid or supported.");
                }
            }
            if (null != getTrimLow() && null != getTrimHigh() && getTrimLow().matches(QUOTED_SUBSET) && getTrimHigh().matches(QUOTED_SUBSET)) {
                // Check low<high
                try {
                    if (!TimeUtil.isOrderedTimeSubset(getTrimLow(), getTrimHigh())) {
                        throw new WCSException(ExceptionCode.InvalidParameterValue, "Temporal subset \"" + getTrimLow() + ":" + getTrimHigh() + "\" is invalid: check order.");
                    }
                } catch (PetascopeException ex) {
                    throw new WCSException(ex.getExceptionCode(), ex);
                }
            }
        }

        @Override
        public String toString() {
            return super.toString() + "(" + trimLow + "," + trimHigh + ")";
        }
    }

    public class DimensionSlice extends DimensionSubset {

        private String slicePoint;

        public DimensionSlice(String dimension, String slicePoint) {
            this(dimension, null, slicePoint);
        }

        public DimensionSlice(String dimension, String crs, String slicePoint) {
            super(dimension, crs);
            this.slicePoint = slicePoint;
            isNumeric = !slicePoint.matches(QUOTED_SUBSET);
        }

        public String getSlicePoint() {
            return slicePoint;
        }

        public void setSlicePoint(String value) {
            slicePoint = value;
            isNumeric = !slicePoint.matches(QUOTED_SUBSET);
        }

        public void setSlicePoint(Double value) {
            setSlicePoint(value.toString());
            isNumeric = true;
        }

        /**
         * Integrity of time subsets (quoted subsets): valid and ordered bounds.
         * @throws WCSException
         */
        public void timestampSubsetCheck() throws WCSException {
            if (null != getSlicePoint() && getSlicePoint().matches(QUOTED_SUBSET)) {
                if (!TimeUtil.isValidTimestamp(getSlicePoint())) {
                    throw new WCSException(ExceptionCode.InvalidParameterValue, "Timestamp \"" + getSlicePoint() + "\" is not valid or supported.");
                }
            }
        }

        @Override
        public String toString() {
            return super.toString() + "(" + slicePoint + ")";
        }
    }

    // CrsExt-extension additional (optional) parameters
    public class CrsExt {
        private String subsettingCrs;
        private String outputCrs;
        // Dictionary <axis;outpurCrs> to build up the WCPS query
        // only containing the *reprojection* needed (see CRSExtension.java)
        private HashMap<String, String> outputAxisCrsMap;

        {
            outputAxisCrsMap = new HashMap<String, String>();
        }

        // Interface
        public String getSubsettingCrs() {
            return subsettingCrs;
        }

        public String getOutputCrs() {
            return outputCrs;
        }

        // Returns axis name(s) associated to a certain CrsExt among the subsets
        // 1+ axes can be associated to the same CrsExt for output reprojection (e.g. xy!)
        public List<String> getAxisNames(String crsUri) {
            List<String> names = new ArrayList<String>();
            Iterator it = outputAxisCrsMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, String> entry = (Map.Entry<String, String>)it.next();
                if (entry.getValue().equals(crsUri)) {
                    names.add(entry.getKey());
                }
            }
            return names;
        }

        // Returns the subsettingCrs URI associated to a certain axis
        public String getCrsUriReprojection(String axisName) {
            return outputAxisCrsMap.get(axisName);
        }

        // NOTE(campalani): initial null values (when no CrsExt values as specified
        //  need to be replaced with default values, relative to requested coverage.
        public void setSubsettingCrs(String value) {
            subsettingCrs = value;
        }

        public void setOutputCrs(String value) {
            outputCrs = value;
        }

        // Methods
        // Add en entry in the output reprojection dictionary
        public void addAxisOutputReprojection(String axisName, String crsUri) {
            outputAxisCrsMap.put(axisName, crsUri);
        }
    }

    public static class Scaling {

        private boolean set;
        private int type;
        private float factor;
        private HashMap<String, Float> fact;
        private HashMap<String, Long> sz;
        private HashMap<String, Pair<Long, Long>> extent;

        public Scaling() {
            set = false;
            type = 0;
            factor = (float) 1.0;
            fact = new HashMap<String, Float>();
            sz = new HashMap<String, Long>();
            extent = new HashMap<String, Pair<Long, Long>>();
        }

        public boolean isSet() {
            return set;
        }

        public int getType() {
            return type;
        }

        public float getFactor() {
            return factor;
        }

        public boolean isPresentFactor(String axis) {
            return fact.containsKey(axis);
        }

        public float getFactor(String axis) {
            return fact.get(axis);
        }

        public boolean isPresentSize(String axis) {
            return sz.containsKey(axis);
        }

        public boolean isPresentExtent(String axis) {
            return extent.containsKey(axis);
        }

        public long getSize(String axis) {
            return sz.get(axis);
        }

        public Pair<Long, Long> getExtent(String axis) {
            return extent.get(axis);
        }

        public void setFactor(float f) {
            this.factor = f;
            this.set = true;
        }

        public void addFactor(String axis, float f) {
            this.fact.put(axis, f);
            this.set = true;
        }

        public void addSize(String axis, long sz) {
            this.sz.put(axis, sz);
            this.set = true;
        }

        public void addExtent(String axis, Pair<Long, Long> ex) {
            this.extent.put(axis, ex);
            this.set = true;
        }

        public void setType(int t) {
            this.type = t;
        }

        public int getAxesNumber() {
            switch (this.type) {
                case 1: return 0;
                case 2: return this.fact.size();
                case 3: return this.sz.size();
                case 4: return this.extent.size();
                default: return 0;
            }
        }
    }
}
