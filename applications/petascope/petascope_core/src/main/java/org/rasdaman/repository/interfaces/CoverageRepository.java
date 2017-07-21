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
package org.rasdaman.repository.interfaces;

import java.util.List;
import org.rasdaman.domain.cis.Coverage;
import org.rasdaman.domain.cis.EnvelopeByAxis;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 * Repository to store the Coverage object to database
 */
public interface CoverageRepository extends CrudRepository<Coverage, String> {
    Coverage findOneByCoverageId(String coverageId);
    
    @Query("Select coverageId from Coverage")
    List<String> readAllCoverageIds();
    
    @Query("Select coverageId, coverageType from Coverage")
    List<Object[]> readAllCoverageIdsAndTypes();
    
    @Query("select b.envelopeByAxis From Coverage c inner join c.envelope b where c.coverageId = :coverageId")
    EnvelopeByAxis readEnvelopeByAxisByCoverageId(@Param("coverageId") String coverageId);
}