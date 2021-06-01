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
import javax.persistence.*;

/**
 * CIS 1.1
 *
 * The optional CIS::Envelope component helps applications in gaining a quick
 * overview on the coverage’s location. The location information does not need
 * to use the same CRS as the domain set, therefore the bounding box may not
 * always be the minimal.
 *
 * If present, the envelope of a coverage instantiating class coverage shall
 * consist of a CIS::EnvelopeByAxis which contains list of AxisExtent.
 */
@Entity
@Table(name = Envelope.TABLE_NAME)
public class Envelope implements Serializable {

    public static final String TABLE_NAME = "envelope";
    public static final String COLUMN_ID = TABLE_NAME + "_id";

    @Id
    @JsonIgnore
    @Column(name = COLUMN_ID)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = EnvelopeByAxis.COLUMN_ID)
    private EnvelopeByAxis envelopeByAxis;
    
    public Envelope() {

    }

    public Envelope(EnvelopeByAxis envelopeByAxis) {
        this.envelopeByAxis = envelopeByAxis;
    }

    public EnvelopeByAxis getEnvelopeByAxis() {
        return envelopeByAxis;
    }

    public void setEnvelopeByAxis(EnvelopeByAxis envelopeByAxis) {
        this.envelopeByAxis = envelopeByAxis;
    }
}
