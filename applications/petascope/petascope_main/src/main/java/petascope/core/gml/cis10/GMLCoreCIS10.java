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
 * Copyright 2003 - 2018 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.core.gml.cis10;

import petascope.core.gml.cis10.model.boundedby.BoundedBy;
import petascope.core.gml.cis.model.coveragefunction.CoverageFunction;
import petascope.core.gml.cis10.model.domainset.DomainSet;
import petascope.core.gml.cis10.model.metadata.Metadata;
import petascope.core.gml.cis.model.rangetype.RangeType;

/**
 * Build the core which contains all necessary elements for WCS DescribeCoverage/GetCoverage request for CIS 1.0 coverages.
 * Then, this core is decorated with something else according to WCS DescribeCoverage/GetCoverage requests.
 */
public class GMLCoreCIS10 {
    
    private BoundedBy boundedBy;
    private CoverageFunction coverageFunction;    
    private DomainSet domainSet;
    private RangeType rangeType;
    private Metadata metadata;

    public GMLCoreCIS10(BoundedBy boundedBy, CoverageFunction coverageFunction, Metadata metadata, DomainSet domainSet, RangeType rangeType) {
        this.boundedBy = boundedBy;
        this.metadata = metadata;
        this.coverageFunction = coverageFunction;
        this.domainSet = domainSet;
        this.rangeType = rangeType;        
    }

    public BoundedBy getBoundedBy() {
        return boundedBy;
    }

    public void setBoundedBy(BoundedBy boundedBy) {
        this.boundedBy = boundedBy;
    }

    public CoverageFunction getCoverageFunction() {
        return coverageFunction;
    }

    public void setCoverageFunction(CoverageFunction coverageFunction) {
        this.coverageFunction = coverageFunction;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public DomainSet getDomainSet() {
        return domainSet;
    }

    public void setDomainSet(DomainSet domainSet) {
        this.domainSet = domainSet;
    }

    public RangeType getRangeType() {
        return rangeType;
    }

    public void setRangeType(RangeType rangeType) {
        this.rangeType = rangeType;
    }
    
}
