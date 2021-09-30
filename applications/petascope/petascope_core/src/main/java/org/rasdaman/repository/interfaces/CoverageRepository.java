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
import org.rasdaman.domain.cis.BaseLocalCoverage;
import org.rasdaman.domain.cis.Coverage;
import org.rasdaman.domain.cis.CoveragePyramid;
import org.rasdaman.domain.cis.EnvelopeByAxis;
import org.rasdaman.domain.cis.RasdamanRangeSet;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/**
 * Repository to store the Coverage object to database
 */

// NOTE: LOB object from Postgresql will throw exception without it is fetched in method annoted by @Transactional.

// Any data model object which is read in transaction and later is updated values in Petascope without an explicit save() to database
// will be actually updated internally by Hibernate by the auto commit mode.
// Hence, use this @Transactional annotation at this ***lowest level*** for SELECT queries when it is needed only which expects change temporarily in java objects.
// And annote the upper level methods with @Transactional for UPDATE, DELETE queries when these method are invoked explicitly
// (e.g: from WCS UpdateCoverage request, DeleteCoverage request)

// A typical example for SELECT query is: the CRS of axis when it is saved to petascopedb should be the template format 
// (e.g: $SECORE_URL$/crs/EPSG/0/4326 not http://localhost:8080/def/crs/EPSG/0/4326).
// As later, Petascope will replace the prefix $SECORE_URL$ by the value configured in petascope.properties (e.g: http://opengis.net/def/crs/EPSG/0/4326).
// However, Hibernate shouldn't persist this change to petascopedb and the template CRS with should be intact.
@Transactional
public interface CoverageRepository extends CrudRepository<Coverage, String> {
    Coverage findOneByCoverageId(String coverageId);
    
    @Query("Select coverageId from Coverage ORDER by coverageId")
    List<String> readAllCoverageIds();
    
    @Query("Select id, coverageId, coverageType from Coverage")
    List<Object[]> readAllCoverageIdsAndTypes();
    
    @Query("select distinct a \n"
        + "from BaseLocalCoverage a \n"     
        + "JOIN FETCH a.envelope as b \n"
        + "JOIN FETCH b.envelopeByAxis as c \n"            
        + "JOIN FETCH c.axisExtents as d \n"        
        + "LEFT JOIN FETCH c.wgs84BBox as e \n"
        + "LEFT JOIN FETCH a.pyramid as a2 \n")
    List<BaseLocalCoverage> readAllBasicCoverageMetadatas();
    
    @Query("Select coverageId, coverageType from Coverage c where c.coverageId = :coverageId")
    String readCoverageTypeByCoverageId(@Param("coverageId") String coverageId);
    
    @Query("select b.envelopeByAxis From Coverage c inner join c.envelope b where c.coverageId = :coverageId")
    EnvelopeByAxis readEnvelopeByAxisByCoverageId(@Param("coverageId") String coverageId);
    
    @Query("select e.lowerBound, e.upperBound \n"
    + "FROM Coverage as a \n"
    + "INNER JOIN a.domainSet as b \n"
    + "INNER JOIN b.generalGrid as c \n"
    + "INNER JOIN c.gridLimits as d \n"
    + "INNER JOIN d.indexAxes as e \n"
    + "where a.coverageId = :coverageId")
    List<Object[]> readGridBoundsByCoverageId(@Param("coverageId") String coverageId);
    
    @Query("select b FROM Coverage as a \n"
           + "INNER JOIN a.rasdamanRangeSet as b \n"           
           + "WHERE a.coverageId = :coverageId")
    RasdamanRangeSet readRasdamanRangeSet(@Param("coverageId") String coverageId);
    
    @Query("select a.coverageId, b.id, b.collectionName, b.collectionType, b.mddType, b.tiling \n"
            + "FROM Coverage as a \n"
           + "INNER JOIN a.rasdamanRangeSet as b \n")
    List<Object[]> readAllRasdamanRangeSets();

    
    @Query("select b from Coverage as a \n"
          + " INNER JOIN a.pyramid as b \n"
          + " WHERE a.coverageId = :baseCoverageId")
    List<CoveragePyramid> readAllCoveragePyramidsByBaseCoverageId(@Param("baseCoverageId") String baseCoverageId);
    
    @Transactional
    @Modifying
    @Query("update Coverage set coverageSizeInBytes = :coverageSizeInBytes where id = :coverageAutoId")
    void saveCoverageSizeInBytes(@Param("coverageAutoId") long coverageAutoId, @Param("coverageSizeInBytes") long coverageSizeInBytes);
}

