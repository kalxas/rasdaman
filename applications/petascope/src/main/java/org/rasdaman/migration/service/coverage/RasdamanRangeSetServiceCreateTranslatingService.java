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

import java.math.BigInteger;
import org.rasdaman.domain.cis.RasdamanRangeSet;
import org.rasdaman.migration.domain.legacy.LegacyCoverageMetadata;
import org.springframework.stereotype.Service;

/**
 * Create a RasdamanRangeSet object from legacy CoverageMetadata object
 *
 @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
@Service
public class RasdamanRangeSetServiceCreateTranslatingService {

    public RasdamanRangeSet create(LegacyCoverageMetadata coverageMetadata) {
        RasdamanRangeSet rasdamanRangeSet = new RasdamanRangeSet();
        // oid -> collection_name
        BigInteger collectionOid = new BigInteger(coverageMetadata.getRasdamanCollection().fst.toString());
        String collectionName = coverageMetadata.getRasdamanCollection().snd.toString();
        // mddType:collectionType
        String type = coverageMetadata.getRasdamanCollectionType();
        String mddType = type.split(":")[0];
        String collectionType = type.split(":")[1];

        rasdamanRangeSet.setOid(collectionOid.longValue());
        rasdamanRangeSet.setCollectionName(collectionName);
        rasdamanRangeSet.setCollectionType(collectionType);
        rasdamanRangeSet.setMddType(mddType);

        return rasdamanRangeSet;
    }
}
