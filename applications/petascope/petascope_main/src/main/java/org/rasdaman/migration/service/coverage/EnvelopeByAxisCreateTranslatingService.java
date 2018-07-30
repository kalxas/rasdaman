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
package org.rasdaman.migration.service.coverage;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.rasdaman.domain.cis.AxisExtent;
import org.rasdaman.domain.cis.EnvelopeByAxis;
import org.rasdaman.migration.domain.legacy.LegacyCoverageMetadata;
import org.rasdaman.migration.domain.legacy.LegacyDomainElement;
import org.springframework.stereotype.Service;
import petascope.util.TimeUtil;
import org.rasdaman.migration.domain.legacy.LegacyAxisTypes;

/**
 * Create a EnvelopeByAxis object from legacy CoverageMetadata object
 *
 @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
@Service
public class EnvelopeByAxisCreateTranslatingService {

    /**
     * Create new EnvelopeByAxis object for non-existing coverage in database
     * from legacy coverage metadata
     *
     * @param coverageMetadata
     * @return
     * @throws Exception
     */
    public EnvelopeByAxis create(LegacyCoverageMetadata coverageMetadata) throws Exception {
        EnvelopeByAxis envelopeByAxis = new EnvelopeByAxis();
        envelopeByAxis.setSrsName(coverageMetadata.getCompoundCrs());
        envelopeByAxis.setSrsDimension(coverageMetadata.getDimension());
        envelopeByAxis.setAxisLabels(coverageMetadata.getAxisLabelsRepresentation());

        List<AxisExtent> axisExtents = new ArrayList<>();

        for (LegacyDomainElement domainElement : coverageMetadata.getDomainList()) {
            AxisExtent axisExtent = new AxisExtent();
            axisExtent.setSrsName(domainElement.getNativeCrs());
            axisExtent.setAxisLabel(domainElement.getLabel());
            axisExtent.setUomLabel(domainElement.getUom());

            String lowerBound;
            String upperBound;

            if (domainElement.getType().equals(LegacyAxisTypes.T_AXIS)) {
                lowerBound = TimeUtil.valueToISODateTime(BigDecimal.ZERO, domainElement.getMinValue(), domainElement.getCrsDef());
                upperBound = TimeUtil.valueToISODateTime(BigDecimal.ZERO, domainElement.getMaxValue(), domainElement.getCrsDef());
            } else {
                lowerBound = domainElement.getMinValue().toPlainString();
                upperBound = domainElement.getMaxValue().toPlainString();
            }
            
            axisExtent.setLowerBound(lowerBound);
            axisExtent.setUpperBound(upperBound);
            axisExtent.setResolution(domainElement.getDirectionalResolution());

            axisExtents.add(axisExtent);
        }

        envelopeByAxis.setAxisExtents(axisExtents);

        return envelopeByAxis;
    }
}
