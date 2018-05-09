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
import nu.xom.Attribute;
import nu.xom.Element;
import static petascope.core.XMLSymbols.ATT_REASON;
import static petascope.core.XMLSymbols.LABEL_NILVALUE;
import static petascope.core.XMLSymbols.NAMESPACE_SWE;
import static petascope.core.XMLSymbols.PREFIX_SWE;

/**
 * NilValue allows the specification of one or more reserved values that may be
 * included in a data stream when the normal measurement value is not available.
 *
 * An instance of the “NilValues” class is composed of one to many “NilValue”
 * objects, each of which specifies a mapping between a reserved value and a
 * reason.
 *
 * <swe:Quantity definition="urn:ogc:def:property:OGC::RadiationDose">
 * <gml:description>Radiation dose measured by Gamma detector</gml:description>
 * <gml:name>Radiation Dose</gml:name>
 * <swe:nilValues>
 * <swe:NilValues>
 * <swe:nilValue reason="urn:ogc:def:nil:OGC::BelowDetectionLimit">-INF</swe:nilValue>
 * <swe:nilValue reason="urn:ogc:def:nil:OGC::AboveDetectionLimit">+INF</swe:nilValue>
 * </swe:NilValues>
 * </swe:nilValues>
 * <swe:uom code="uR"/>
 * </swe:Quantity>
 */
@Entity
@Table(name = NilValue.TABLE_NAME)
public class NilValue implements Serializable {

    public static final String TABLE_NAME = "nil_value";
    public static final String COLUMN_ID = TABLE_NAME + "_id";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = COLUMN_ID)
    private long id;

    @Column(name = "value")
    private String value;

    @Column(name = "reason")
    @Lob
    // NOTE: As this could be long text, so varchar(255) is not enough
    private String reason;

    public NilValue() {

    }

    public NilValue(String value, String reason) {
        this.value = value;
        this.reason = reason;
    }

    @JsonIgnore
    // no serialize this value as JSON extra parameters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Element toGML() {
        //<swe:nilValue reason="http://www.opengis.net/def/nil/OGC/0/BelowDetectionRange">-INF</swe:nilValue>
        Element nilValue = new Element(PREFIX_SWE + ":" + LABEL_NILVALUE, NAMESPACE_SWE);
        nilValue.addAttribute(new Attribute(ATT_REASON, getReason()));
        nilValue.appendChild(value);

        return nilValue;
    }
}
