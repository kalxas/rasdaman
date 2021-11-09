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
import java.util.ArrayList;
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
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.Transient;

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
public class BaseLocalCoverage implements Serializable {


    @Id
    @JsonIgnore
    @Column(name = Coverage.COLUMN_ID)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name = "coverage_id", unique = true)
    // this is the id of coverage (or coverage name)
    protected String coverageId;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = Envelope.COLUMN_ID)
    // persist this object before persist the container object (i.e: it needs the PK of the cascading to make the FK)    
    protected Envelope envelope;

    @Column(name = "coverage_type")
    // To determine coverage is: GridCoverage, RectifiedGridCoverage, ReferenceableGridCoverage
    protected String coverageType;
    
    @Column(name = "coverage_size_in_bytes")
    // Store the calculated size of coverage in bytes for overview
    protected Long coverageSizeInBytes = 0L;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = Coverage.FK_COVERAGE_ID)
    @OrderColumn
    protected List<CoveragePyramid> pyramid = new ArrayList<>();
    
    @Transient
    private RasdamanRangeSet rasdamanRangeSet;
    
    @Column(name = "inspire_metadata_url")
    @Lob
    protected String inspireMetadataURL;
    
    public BaseLocalCoverage() {

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

    public Envelope getEnvelope() {
        return envelope;
    }

    public void setEnvelope(Envelope envelope) {
        this.envelope = envelope;
    }

    public String getCoverageType() {
        return coverageType;
    }

    public void setCoverageType(String coverageType) {
        this.coverageType = coverageType;
    }

    public Long getCoverageSizeInBytes() {
        if (coverageSizeInBytes == null) {
            coverageSizeInBytes = 0L;
        }
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
    
    public RasdamanRangeSet getRasdamanRangeSet() {
        return rasdamanRangeSet;
    }

    public void setRasdamanRangeSet(RasdamanRangeSet rasdamanRangeSet) {
        this.rasdamanRangeSet = rasdamanRangeSet;
    }
    
    public String getInspireMetadataURL() {
        return inspireMetadataURL;
    }

    public void setInspireMetadataURL(String inspireMetadataURL) {
        this.inspireMetadataURL = inspireMetadataURL;
    }
}
