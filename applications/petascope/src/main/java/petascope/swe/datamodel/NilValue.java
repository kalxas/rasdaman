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
 * Copyright 2003 - 2014 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */

package petascope.swe.datamodel;

import nu.xom.Attribute;
import nu.xom.Element;
import static petascope.util.XMLSymbols.ATT_REASON;
import static petascope.util.XMLSymbols.LABEL_NILVALUE;
import static petascope.util.XMLSymbols.NAMESPACE_SWE;
import static petascope.util.XMLSymbols.PREFIX_SWE;

/**
 * NIL values are used to indicate that the actual value of a property cannot be given in the data stream.
 * The reason for which the value is not included is essential for a good interpretation of the data,
 * so each reserved value is associated to a well-defined reason.
 * In that sense, a NIL value definition is essentially a mapping between a reserved value and a reason.
 * (OGC 08-094r1)
 *
 * @author <a href="mailto:p.campalani@jacobs-university.de">Piero Campalani</a>
 */
public class NilValue {

    /*
     * XSD ANNOTATIONS:
     *
     * NilValuesPropertyType :
     *   [0..1] swe:NilValues
     *   [1..*] @swe:AssociationAttributeGroup : attributeGroup ref="xlink:simpleAttrs"
     *
     * NilValues :
     *   substitutionGroup="swe:AbstractSWE" type="swe:NilValuesType"
     *   [1..*] nilValue         type="swe:NilValue
     */

    // fields
    /**
     * The mandatory "value" attribute specifies the data value that would be found in the
     * stream to indicate that a measurement value is missing for the corresponding reason.
     * swe:NilValue : [1..1] value type="string"
     */
    private final String value;
    /**
     * The mandatory ``reason'' attribute indicates the reason why a measurement value is not
     * available. It is a resolvable reference to a controlled term that provides the formal textual
     * definition of this reason (usually agreed upon by one or more communities).
     * swe:NilValue : [1..1] reason type="anyURI"
     * Example: http://www.opengis.net/def/nil/OGC/0/BelowDetectionRange
     */
    private final String reason;

    // constructor
    /**
     * Constructor of a NIL value, with specified value and reaons URI.
     * @param thisValue
     * @param thisReason
     */
    public NilValue(String thisValue, String thisReason) {
        value  = thisValue;
        reason = thisReason;
    }

    // access
    /**
     * Getter method for the value of this SWE NIL value.
     * @return
     */
    public String getValue() {
        return value;
    }
    /**
     * Getter method for the reason of this SWE NIL value.
     * @return The URI defining the reason for this NIL.
     */
    public String getReason() {
        return reason;
    }

    // methods
    public Element toGML() {
        //<swe:nilValue reason="http://www.opengis.net/def/nil/OGC/0/BelowDetectionRange">-INF</swe:nilValue>
        Element nilValue = new Element(PREFIX_SWE + ":" + LABEL_NILVALUE, NAMESPACE_SWE);
        nilValue.addAttribute(new Attribute(ATT_REASON, getReason()));
        nilValue.appendChild(value);

        return nilValue;
    }

}
