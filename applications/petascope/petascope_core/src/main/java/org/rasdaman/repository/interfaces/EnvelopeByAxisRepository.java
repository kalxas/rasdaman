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
import org.springframework.data.repository.CrudRepository;
import org.rasdaman.domain.cis.EnvelopeByAxis;
import org.rasdaman.domain.cis.Wgs84BoundingBox;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository to store the EnvelopeByAxis object to database
 */
public interface EnvelopeByAxisRepository extends CrudRepository<EnvelopeByAxis, Long> {
    
    @Query("FROM EnvelopeByAxis a JOIN FETCH a.axisExtents b JOIN FETCH a.wgs84BBox")
    public List<EnvelopeByAxis> findAllEnvelopeByAxis();
    
    @Modifying
    @Query("update EnvelopeByAxis set wgs84BBox = :wgs84BBox where id = :envelopByAxisAutoId")
    void saveWgs84BBox(@Param("envelopByAxisAutoId") long envelopByAxisAutoId, @Param("wgs84BBox") Wgs84BoundingBox wgs84BBox);
    
}
