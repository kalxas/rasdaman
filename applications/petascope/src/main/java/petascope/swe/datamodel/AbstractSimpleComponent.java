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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import nu.xom.Element;

/**
 * SWE abstract simple component.
 * From this classe derive the concrete subclasses `Quantity', `Category', `Count' and `Time'.
 *
 * @author <a href="mailto:p.campalani@jacobs-university.de">Piero Campalani</a>
 */
public abstract class AbstractSimpleComponent extends AbstractDataComponent {

    // fields
    // List<QualityProperty> quality; // not supported
    /**
     * The optional "nilValues" attribute is used to provide a list (ie one or more) of NIL values.
     * swe:AbstractSimpleComponent : [0..1] nilValues ((type="swe:NilValuesPropertyType"))
     */
    private final List<NilValue> nilValues;
    /**
     * The "referenceFrame" attribute identifies the reference frame (as defined by the "SC_CRS" object)
     * relative to which the coordinate value is given.
     * Not supported.
     */
    private final String referenceFrame = null;
    /**
     * The "axisID" attribute takes a string that uniquely identifies one of the
     * reference frame’s axes along which the coordinate value is given.
     * Not supported.
     */
    private final String axisID = null;

    // constructors
    /**
     * Constructor with empty initialization.
     */
    protected AbstractSimpleComponent() {
        super();
        nilValues = new ArrayList<NilValue>(0);
    }
    /**
     * Full attributes constructor (currently supported elements).
     * @param label
     * @param description
     * @param definition
     * @param nils
     */
    protected AbstractSimpleComponent(String label, String description, String definition, List<NilValue> nils) {
        super(label, description, definition);
        nilValues = new ArrayList<NilValue>(nils.size());
        for (NilValue nil : nils) {
            nilValues.add(nil);
        }
    }

    // access
    /**
     * Getter method for the list of NIL values.
     * @return The iterator of the list of NIL values for this SWE component.
     */
    public Iterator<NilValue> getNilValuesIterator() {
        return nilValues.iterator();
    }

    // methods
    public abstract String toGML();
}
