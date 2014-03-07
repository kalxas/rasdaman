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

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Serializer;
import static petascope.util.XMLSymbols.ATT_DEFINITION;
import static petascope.util.XMLSymbols.ATT_UOMCODE;
import static petascope.util.XMLSymbols.LABEL_CONSTRAINT;
import static petascope.util.XMLSymbols.LABEL_DESCRIPTION;
import static petascope.util.XMLSymbols.LABEL_LABEL;
import static petascope.util.XMLSymbols.LABEL_NILVALUES;
import static petascope.util.XMLSymbols.LABEL_NILVALUES_ASSOCIATION_ROLE;
import static petascope.util.XMLSymbols.LABEL_QUANTITY;
import static petascope.util.XMLSymbols.LABEL_UOM;
import static petascope.util.XMLSymbols.NAMESPACE_SWE;
import static petascope.util.XMLSymbols.PREFIX_SWE;

/**
 * Scalar component with decimal representation and a unit of measure used to store value of a continuous quantity.
 * Other concrete types of AbstractSimpleComponents are `Category', `Count' and `Time'.
 *
 * @author <a href="mailto:p.campalani@jacobs-university.de">Piero Campalani</a>
 */
public class Quantity extends AbstractSimpleComponent {

    // fields
    /**
     * Unit of measure of this quantity.
     * This unit is essential for the correct interpretation of data represented as decimal numbers and is thus mandatory.
     * Quantities with no physical unit still have a scale (such as unity, percent, perthousands, etc.) that must be specified with this property.
     * This type specifies a character string of length at least one,
     * and restricted such that it must not contain any of the following characters:
     * ":" (colon), " " (space), (newline), (carriage return), (tab).
     * This allows values corresponding to familiar abbreviations, such as "kg", "m/s", etc.
     * It is also required that the symbol be an identifier for a unit of measure as specified in the
     * "Unified Code of Units of Measure" (UCUM) (http://aurora.regenstrief.org/UCUM).
     * swe:Quantity : [1..1] uom ((type="swe:UnitReference"))
     */
    private final String uom;
    /**
     * The "constraint" attribute is used to restrict the range of possible values to a list of inclusive intervals and/or single values.
     * Intervals are currently supported.
     * swe:Quantity : [1..1] constraint ((type="swe:AllowedValuesPropertyType"))
     */
    private final AllowedValues constraint;
    /**
     * The "value" attribute (or the corresponding value in out-of-band data) is a real value that
     * is within one of the constraint intervals or exactly one of the enumerated values, and most
     * importantly is expressed in the unit specified.
     * Not supported.
     * swe:Quantity : [1..1] constraint ((type="double"))
     */
    private final double value = 0.0D;

    // constructors
    /**
     * Constructor with mandatory UoM.
     * @param unitOfMeasure
     */
    public Quantity(String unitOfMeasure) {
        super();
        uom = unitOfMeasure;
        constraint = null;
    }
    /**
     * Constructor with mandatory UoM and additional constraint(s).
     * @param unitOfMeasure
     * @param allowedValues
     */
    public Quantity(String unitOfMeasure, AllowedValues allowedValues) {
        super();
        uom = unitOfMeasure;
        constraint = allowedValues;
    }
    /**
     * Full attributes constructor.
     * @param label
     * @param description
     * @param definition
     * @param nils
     * @param unitOfMeasure
     * @param allowedValues
     */
    public Quantity(String label, String description, String definition, List<NilValue> nils, String unitOfMeasure, AllowedValues allowedValues) {
        super(label, description, definition, nils);
        uom = unitOfMeasure;
        constraint = allowedValues;
    }

    // access
    /**
     * Getter method for the UoM.
     * @return The Unit of Measure of this SWE Quantity.
     */
    public String getUom() {
        return uom;
    }
    /**
     * Getter method for the constraints.
     * @return The constraints that apply on this SWE Quantity.
     */
    public AllowedValues getAllowedValues() {
        return constraint;
    }

    // methods
    public String toGML() {
        // Example with both allowed values and NILs:
        //
        // <swe:Quantity definition="http://sweet.jpl.nasa.gov/2.0/physRadiation.owl#IonizingRadiation">
        //   <swe:label>Radiation Dose</swe:label>
        //   <swe:description>Radiation dose measured by Gamma detector</swe:description>
        //   <swe:nilValues>
        //     <swe:NilValues>
        //       <swe:nilValue reason="http://www.opengis.net/def/nil/OGC/0/BelowDetectionRange">-INF</swe:nilValue>
        //       <swe:nilValue reason="http://www.opengis.net/def/nil/OGC/0/AboveDetectionRange">INF</swe:nilValue>
        //     </swe:NilValues>
        //   </swe:nilValues>
        //   <swe:uom code="uR"/>
        //   <swe:constraint>
        //     <swe:AllowedValues>
        //       <swe:interval>-180 0</swe:interval>
        //       <swe:interval>1 180</swe:interval>
        //     </swe:AllowedValues>
        //   </swe:constraint>
        // </swe:Quantity>
        Element quantity = new Element(PREFIX_SWE + ":" + LABEL_QUANTITY, NAMESPACE_SWE);
        Element nilValues;
        Element nilValuesRole;
        Element child;
        // label
        if (null != getLabel() && !getLabel().isEmpty()) {
            child = new Element(PREFIX_SWE + ":" + LABEL_LABEL, NAMESPACE_SWE);
            child.appendChild(getLabel());
            quantity.appendChild(child);
        }
        // description
        if (null != getDescription() && !getDescription().isEmpty()) {
            child = new Element(PREFIX_SWE + ":" + LABEL_DESCRIPTION, NAMESPACE_SWE);
            child.appendChild(getDescription());
            quantity.appendChild(child);
        }
        // definition URI
        if (null != getDefinition() && !getDefinition().isEmpty()) {
            quantity.addAttribute(new Attribute(ATT_DEFINITION, getDefinition()));
        }
        // NIL values
        Iterator<NilValue> nilIt = getNilValuesIterator();
        if (nilIt.hasNext()) {
            nilValuesRole = new Element(PREFIX_SWE + ":" + LABEL_NILVALUES_ASSOCIATION_ROLE, NAMESPACE_SWE);
            nilValues = new Element(PREFIX_SWE + ":" + LABEL_NILVALUES, NAMESPACE_SWE);
            while (nilIt.hasNext()) {
                NilValue nil = nilIt.next();
                if (null != nil.getValue() && !nil.getValue().isEmpty()) {
                    nilValues.appendChild(nil.toGML());
                }
            }
            nilValuesRole.appendChild(nilValues);
            quantity.appendChild(nilValuesRole);
        }
        // UoM
        child = new Element(PREFIX_SWE + ":" + LABEL_UOM, NAMESPACE_SWE);
        child.addAttribute(new Attribute(ATT_UOMCODE, getUom()));
        quantity.appendChild(child);
        // constraint
        Iterator<RealPair> constraintIt = getAllowedValues().getIntervalIterator();
        Element constraintEl;
        if (constraintIt.hasNext()) {
            constraintEl = new Element(PREFIX_SWE + ":" + LABEL_CONSTRAINT, NAMESPACE_SWE);
            constraintEl.appendChild(getAllowedValues().toGML());
            quantity.appendChild(constraintEl);
        }

        Document indentedQuantity = new Document(quantity);
        try {
            Serializer serializer = new Serializer(System.out);
            serializer.setIndent(2);
            serializer.write(indentedQuantity);
        } catch (IOException ex) {

        }
        return indentedQuantity.getRootElement().toXML();
    }
}
