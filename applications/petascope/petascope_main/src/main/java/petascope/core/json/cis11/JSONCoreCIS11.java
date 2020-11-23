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
 * Copyright 2003 - 2020 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.core.json.cis11;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import petascope.core.json.cis11.model.domainset.DomainSet;
import petascope.core.json.cis11.model.envelope.Envelope;
import static petascope.core.json.cis11.model.envelope.Envelope.ENVELOPE_NAME;
import petascope.core.json.cis11.model.metadata.Metadata;
import petascope.core.json.cis11.model.rangetype.RangeType;


/**
 * Build the core which contains all necessary elements for WCS DescribeCoverage/GetCoverage request for JSON CIS 1.1 coverages.
 * Then, this core is decorated with something else according to WCS DescribeCoverage/GetCoverage requests.
 * 
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@JsonPropertyOrder({ENVELOPE_NAME})
public class JSONCoreCIS11 {
    
    private Envelope envelope;
    private DomainSet domainSet;
    private RangeType rangeType;
    private Metadata metadata;

    public JSONCoreCIS11(Envelope envelope, DomainSet domainSet, RangeType rangeType, Metadata metadata) {
        this.envelope = envelope;
        this.domainSet = domainSet;
        this.rangeType = rangeType;
        this.metadata = metadata;
    }

    public Envelope getEnvelope() {
        return envelope;
    }

    public void setEnvelope(Envelope envelope) {
        this.envelope = envelope;
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

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

}
