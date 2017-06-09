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

import javax.persistence.*;
import java.util.List;

/**
 * 6.5.3 (CIS 1.1) Constraints on the interpolation methods meaningfully
 * applicable to this coverage
 *
 * If no interpolationRestriction element is present (null), then any interpolation
 * method is applicable to the coverage on hand.
 *
 * In presence of an interpolationRestriction element, only those interpola-
 * tion methods may be meaningfully applied whose identifiers appear in an
 * allow- edInterpolation element; 
 * 
 * in case of an empty list this means that no interpolation is applicable at all.
 * 
 * <interpolationRestriction>
 * <allowedInterpolation>
 * http://www.opengis.net/def/interpolation/OGC/1/nearest-neighbor
 * </allowedInterpolation>
 * <allowedInterpolation>
 * http://www.opengis.net/def/interpolation/OGC/1/linear
 * </allowedInterpolation>
 * </interpolationRestriction>
 * 
 */
@Entity
@Table(name = InterpolationRestriction.TABLE_NAME)
public class InterpolationRestriction {

    public static final String TABLE_NAME = "interpolation_restriction";
    public static final String COLUMN_ID = TABLE_NAME + "_id";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = COLUMN_ID)
    private long id;

    @ElementCollection(fetch = FetchType.EAGER)
    @OrderColumn
    // List of interpolation URI (null means all possible, empty means none)
    private List<String> allowedInterpolations;

    public InterpolationRestriction() {

    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List<String> getAllowedInterpolations() {
        return allowedInterpolations;
    }

    public void setAllowedInterpolations(List<String> allowedInterpolations) {
        this.allowedInterpolations = allowedInterpolations;
    }
}
