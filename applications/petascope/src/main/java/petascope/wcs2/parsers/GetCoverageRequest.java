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
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import petascope.util.ListUtil;
import petascope.util.Pair;
import petascope.wcs2.extensions.FormatExtension;

/**
 * A GetCoverage request object, populated by a parser.
 *
   <element name="GetCoverage" type="wcs:GetCoverageType">
        <annotation>
            <documentation>Request to a WCS to perform the GetCoverage operation. This operation allows a client to retrieve a subset of one coverage. </documentation>
        </annotation>
    </element>
    <complexType name="GetCoverageType">
        <complexContent>
            <extension base="wcs:RequestBaseType">
                <sequence>
                    <element ref="wcs:CoverageId">
                        <annotation>
                            <documentation>Identifier of the coverage that this GetCoverage operation request shall draw from. </documentation>
                        </annotation>
                    </element>
                    <element ref="wcs:DimensionSubset" minOccurs="0" maxOccurs="unbounded"/>
                </sequence>
            </extension>
        </complexContent>
    </complexType>

    <!-- ======================================================= -->
    <!-- Domain subset types and elements                        -->
    <!-- ======================================================= -->
    <element name="DimensionSubset" type="wcs:DimensionSubsetType" abstract="true">
        <annotation>
            <documentation>Definition of the desired subset of the domain of the coverage. This is either a Trim operation, or a Slice operation.</documentation>
        </annotation>
    </element>
    <complexType name="DimensionSubsetType" abstract="true">
        <sequence>
            <element name="Dimension" type="NCName"/>
        </sequence>
    </complexType>
    <!-- ======================================================= -->
    <element name="DimensionTrim" type="wcs:DimensionTrimType" substitutionGroup="wcs:DimensionSubset">
        <annotation>
            <documentation>Describes the trimming of a coverage's domain axis, between two values.</documentation>
        </annotation>
    </element>
    <complexType name="DimensionTrimType">
        <complexContent>
            <extension base="wcs:DimensionSubsetType">
                <sequence>
                    <element name="TrimLow" type="double" minOccurs="0"/>
                    <element name="TrimHigh" type="double" minOccurs="0"/>
                </sequence>
            </extension>
        </complexContent>
    </complexType>
    <!-- ======================================================= -->
    <element name="DimensionSlice" type="wcs:DimensionSliceType" substitutionGroup="wcs:DimensionSubset">
        <annotation>
            <documentation>Describes the slicing of a coverage's domain axis at a particular point.</documentation>
        </annotation>
    </element>
    <complexType name="DimensionSliceType">
        <complexContent>
            <extension base="wcs:DimensionSubsetType">
                <sequence>
                    <element name="SlicePoint" type="double"/>
                </sequence>
            </extension>
        </complexContent>
    </complexType>
 *
 * @author <a href="mailto:d.misev@jacobs-university.de">Dimitar Misev</a>
 */
public class GetCoverageRequest extends BaseRequest {
    
    public static final String GRID_COVERAGE = "GridCoverage";
    public static final String RECTIFIED_GRID_COVERAGE = "RectifiedGridCoverage";
    public static final String REFERENCEABLE_GRID_COVERAGE = "ReferenceableGridCoverage";
    public static final String MULTIPOINT_COVERAGE = "MultiPointCoverage";

    private final String coverageId;
    private final String format;
    private final boolean multipart;
    private final List<DimensionSubset> subsets;
    private final List<CRS> crsExt;    // use List to allow lazy init of final CRS
    private final Scaling scale;

    public GetCoverageRequest(String coverageId) {
        this(coverageId, FormatExtension.MIME_GML, false);
    }

    public GetCoverageRequest(String coverageId, String format, boolean multipart) {
        this.coverageId = coverageId;
        this.format = format;
        this.multipart = multipart;
        this.subsets = new ArrayList<DimensionSubset>();
        this.crsExt = new ArrayList<CRS>();
        this.scale = new Scaling();
    }

    public String getCoverageId() {
        return coverageId;
    }

    public List<DimensionSubset> getSubsets() {
        return subsets;
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
    
    public List<CRS> getCRS() {
        return crsExt;
    }
    
    public Scaling getScaling() {
        return scale;
    }
    
    public boolean isScaled() {
        return scale.isSet();
    }

    @Override
    public String toString() {
        return "Coverage: " + coverageId + ", Subsets: " + ListUtil.ltos(subsets, ", ");
    }

    public static class DimensionSubset {

        protected final String dimension;
        //protected final String crs;
        protected String crs;
        protected boolean transformed; // on subsettingCrs specification (campalani)

        public DimensionSubset(String dimension) {
            this(dimension, null);
        }

        public DimensionSubset(String dimension, String crs) {
            this.dimension = dimension;
            this.crs = crs;
            this.transformed = false;
        }

        public String getDimension() {
            return dimension;
        }

        public String getCrs() {
            return crs;
        }

        /** NOTE(campalani): to avoid second wrong CRS transformsetBounds
         *  (both setBounds and addCoverageData loop through AbstractFormatExtension)         * 
         */
        public boolean isCrsTransformed() {
            return transformed;
        }
        
        public void isCrsTransformed(boolean value) {
            transformed = value;
        }
        // When transforming a subset, change crs accordingly
        public void setCrs(String value) {
            crs = value;
        }
        
        @Override
        public String toString() {
            return dimension + ((crs != null) ? "," + crs : "");
        }
    }

    public static class DimensionTrim extends DimensionSubset {

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
        }

        public String getTrimHigh() {
            return trimHigh;
        }

        public String getTrimLow() {
            return trimLow;
        }
        /**
         * @param value Set new lower bound to 1D domain (due to a CRS transformation).
         */
        public void setTrimLow(String value) {
            trimLow = value;
        }
        public void setTrimLow(Double value){
            setTrimLow(value.toString());
        }
                
        /**
         * @param value Set new upper bound to 1D domain (due to a CRS transformation).
         */
        public void setTrimHigh(String value) {
            trimHigh = value;
        }
        public void setTrimHigh(Double value) {
            setTrimHigh(value.toString());
        }
        
        @Override
        public String toString() {
            return super.toString() + "(" + trimLow + "," + trimHigh + ")";
        }
    }

    public static class DimensionSlice extends DimensionSubset {

        //private final String slicePoint;
        private String slicePoint;

        public DimensionSlice(String dimension, String slicePoint) {
            this(dimension, null, slicePoint);
        }

        public DimensionSlice(String dimension, String crs, String slicePoint) {
            super(dimension, crs);
            this.slicePoint = slicePoint;
        }

        public String getSlicePoint() {
            return slicePoint;
        }

        public void setSlicePoint(String value) {
            slicePoint = value;
        }
        public void setSlicePoint(Double value){
            setSlicePoint(value.toString());
        }
        
        @Override
        public String toString() {
            return super.toString() + "(" + slicePoint + ")";
        }
    }
    
    // CRS-extension additional (optional) parameters
    public static class CRS {
        private String subsettingCrs;
        private String outputCrs;

        // Constructor
        public CRS(String subset, String out) {
            subsettingCrs = subset;
            outputCrs = out;
        }
        
        // Interface
        public String getSubsettingCrs() {
            return subsettingCrs;
        }            

        public String getOutputCrs() {                
            return outputCrs;
        }
        
        // NOTE(campalani): initial null values (when no CRS values as specified
        //  need to be replaced with default values, relative to requested coverage.
        public void setSubsettingCrs(String value) {
            subsettingCrs = value;
        }
        
        public void setOutputCrs(String value) {
            outputCrs = value;
        }
    }
    
    public static class Scaling {
    
        private boolean set;
        private int type;
        private float factor;
        private HashMap<String, Float> fact;
        private HashMap<String, Integer> sz;
        private HashMap<String, Pair<Integer, Integer>> extent;
        
        public Scaling() {
            set = false;
            type = 0;
            factor = (float) 1.0;
            fact = new HashMap<String, Float>();
            sz = new HashMap<String, Integer>();
            extent = new HashMap<String, Pair<Integer, Integer>>();
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
        
        public int getSize(String axis) {
            return sz.get(axis);
        }
        
        public Pair<Integer, Integer> getExtent(String axis) {
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
        
        public void addSize(String axis, int sz) {
            this.sz.put(axis, sz);
            this.set = true;
        }
        
        public void addExtent(String axis, Pair<Integer, Integer> ex) {
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
