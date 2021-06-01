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
 * Copyright 2003 - 2017 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package org.rasdaman.domain.cis;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.Transient;
import petascope.util.ListUtil;
import petascope.util.BigDecimalUtil;

/**
 * CIS 1.1
 *
 * In summary, CIS 1.1 is a backwards compatible extension of GMLCOV 1.0, also
 * merging in GML 3.3 grid types. Note that irregular grid types in both GMLCOV
 * and GML in future may get deprecated in favour of the general grid type in
 * CIS 1.1 which is more concise, better to analyze by applications, and support
 * cases not addressed by the previous grid approaches.
 *
 *
 * Like in GML, all coverage types in CIS 1.1 (as in GMLCOV 1.0) are derived
 * from a com- mon Coverage type.
 *
 * This unifies OGC’s coverage implementation model. It does so by super- seding
 * and extending CIS 1.0 (also known as GMLCOV 1.0) with further ways to model
 * and represent coverages, and by integrating the GML 3.3 grid types.
 *
 * This class does not allow creating coverage instances, but rather provides
 * the fun- dament for the further classes which define various specializations
 * of which instance documents can be created.
 *
 */
@Entity
@Table(name = Coverage.TABLE_NAME)
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Coverage implements Serializable {

    public static final String TABLE_NAME = "coverage";
    public static final String COLUMN_ID = "id";
    
    // Used by Hibernate HSQ, Criteria
    public static final String COVERAGE_CLASS_NAME = "Coverage";
    public static final String COVERAGE_ID_PROPERTY = "coverageId";
    
    public static final String FK_COVERAGE_ID = "fk_id";

    @Id
    @JsonIgnore
    @Column(name = COLUMN_ID)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name = "coverage_id")
    // this is the id of coverage (or coverage name)
    protected String coverageId;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = CoverageFunction.COLUMN_ID)
    private CoverageFunction coverageFunction;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = Envelope.COLUMN_ID)
    // persist this object before persist the container object (i.e: it needs the PK of the cascading to make the FK)    
    protected Envelope envelope;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = DomainSet.COLUMN_ID)
    private DomainSet domainSet;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = RasdamanRangeSet.COLUMN_ID)
    private RasdamanRangeSet rasdamanRangeSet;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = RangeType.COLUMN_ID)
    private RangeType rangeType;

    @Column(name = "metadata")
    @Lob
    // NOTE: As this could be long text, so varchar(255) is not enough
    // Hibernate will detect suitable datatype for target database (e.g: in Postgreql is text for String)
    private String metadata;

    @Column(name = "coverage_type")
    // To determine coverage is: GridCoverage, RectifiedGridCoverage, ReferenceableGridCoverage
    protected String coverageType;
    
    @Transient
    // Store the calculated size of coverage in bytes for overview
    protected long coverageSizeInBytes;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = FK_COVERAGE_ID)
    @OrderColumn
    protected List<CoveragePyramid> pyramid = new ArrayList<>();

    public Coverage() {

    }

    public Coverage(String coverageId, String coverageType, long coverageSizeInBytes, 
                    Envelope envelope, DomainSet domainSet, RangeType rangeType, RasdamanRangeSet rasdamanRangeSet, String metadata) {
        this.coverageId = coverageId;
        this.envelope = envelope;
        this.domainSet = domainSet;
        this.rasdamanRangeSet = rasdamanRangeSet;
        this.rangeType = rangeType;
        this.metadata = metadata;
        this.coverageType = coverageType;
        this.coverageSizeInBytes = coverageSizeInBytes;
    }
    
    public Coverage(String coverageId, String coverageType, long coverageSizeInBytes, Envelope envelope, RasdamanRangeSet rasdamanRangeSet) {
        this.coverageId = coverageId;
        this.coverageType = coverageType;
        this.coverageSizeInBytes = coverageSizeInBytes;
        this.envelope = envelope;
        this.rasdamanRangeSet = rasdamanRangeSet;        
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCoverageId() {
        return coverageId;
    }

    public void setCoverageId(String coverageId) {
        this.coverageId = coverageId;
    }

    public CoverageFunction getCoverageFunction() {
        return coverageFunction;
    }

    public void setCoverageFunction(CoverageFunction coverageFunction) {
        this.coverageFunction = coverageFunction;
    }

    public Envelope getEnvelope() {
        return envelope;
    }

    public void setEnvelope(Envelope envelope) {
        this.envelope = envelope;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public RangeType getRangeType() {
        return rangeType;
    }

    public void setRangeType(RangeType rangeType) {
        this.rangeType = rangeType;
    }

    public RasdamanRangeSet getRasdamanRangeSet() {
        return rasdamanRangeSet;
    }

    public void setRasdamanRangeSet(RasdamanRangeSet rasdamanRangeSet) {
        this.rasdamanRangeSet = rasdamanRangeSet;
    }

    // Depend on coverage type to define the method
    public DomainSet getDomainSet() {
        return this.domainSet;
    }

    public void setDomainSet(DomainSet domainSet) {
        this.domainSet = domainSet;
    }

    public String getCoverageType() {
        return coverageType;
    }

    public void setCoverageType(String coverageType) {
        this.coverageType = coverageType;
    }

    public long getCoverageSizeInBytes() {
        return coverageSizeInBytes;
    }

    public void setCoverageSizeInBytes(long coverageSizeInBytes) {
        this.coverageSizeInBytes = coverageSizeInBytes;
    }

    public List<CoveragePyramid> getPyramid() {
        return pyramid;
    }

    public void setPyramid(List<CoveragePyramid> pyramid) {
        this.pyramid = pyramid;
    }
    
    /**
     * Check if a coverage id already exists in this coverage's pyramid set
     */
    public boolean hasPyramidMember(String coverageId) {
        for (CoveragePyramid pyramidMember : this.pyramid) {
            if (pyramidMember.getPyramidMemberCoverageId().equals(coverageId)) {
                return true;
            }
        }
        
        return false;
    }

    // Helper methods
    /**
     * Gets an array list containing all null values, on all bands and removes
     * the duplicates. This is used for passing the values further to rasdaman
     * (1 null value mean all bands have same null value, list of null values
     * mean each value is null value for the corresponding band only)
     *
     * @return
     */
    public List<NilValue> getAllUniqueNullValues() {
        List<NilValue> uniqueNilValues = new ArrayList<NilValue>();

        List<Field> fields = this.getRangeType().getDataRecord().getFields();

        for (Field field : fields) {
            Quantity quantity = field.getQuantity();
            
            if (quantity.getNilValuesList() != null) {
                for (NilValue nilValue : quantity.getNilValuesList()) {
                    if (!uniqueNilValues.contains(nilValue)) {
                        uniqueNilValues.add(nilValue);
                    }
                }
            }
        }

        return uniqueNilValues;
    }
    
    public List<NilValue> getAllNullValues() {
        List<NilValue> result = new ArrayList<>();

        List<Field> fields = this.getRangeType().getDataRecord().getFields();

        for (Field field : fields) {
            Quantity quantity = field.getQuantity();
            result.addAll(quantity.getNilValuesList());            
        }

        return result;
    }

    /**
     *
     * Returns the number of bands (quantities) in a coverage
     *
     * @return
     */
    @JsonIgnore
    public int getNumberOfBands() {
        // Each field contains 1 quantity
        int numberOfBands = this.getRangeType().getDataRecord().getFields().size();

        return numberOfBands;
    }
    
      
    /**
     * Return the concatenated string of distinct band types, e.g: Float32,Byte,Int16
     */
    @JsonIgnore
    public String getPixelDataType() {
        List<String> list = new ArrayList<>();
        
        for (Field field : this.getRangeType().getDataRecord().getFields()) {
            String bandDataType = field.getQuantity().getDataType();
            if (bandDataType != null && !list.contains(bandDataType)) {
                list.add(bandDataType);
            }
        }
        
        String result = ListUtil.join(list, ",");
        return result;
    }

    /**
     *
     * Returns the number of dimensions in a coverage
     *
     * @return
     */
    @JsonIgnore
    public int getNumberOfDimensions() {
        int numberOfDimensions = this.getEnvelope().getEnvelopeByAxis().getSrsDimension();

        return numberOfDimensions;
    }
}
