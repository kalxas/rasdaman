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

import nu.xom.Element;

/**
 *
 * @author <a href="mailto:p.campalani@jacobs-university.de">Piero Campalani</a>
 */
public abstract class AbstractSWEIdentifiable {

    // fields
    /**
     * The optional "identifier" attribute allows assigning a unique identifier to the component, so that it can be referenced later on.
     * It can be used, for example, when defining the unique identifier of a universal constant.
     * swe:AbstractSWEIdentifiableType : [0..1] identifier ((type="anyURI"))
     */
    private final String identifierURI = null;
    /**
     * The "label" is meant to hold a short descriptive name describing what property the component represents.
     * swe:AbstractSWEIdentifiableType : [0..1] label ((type="string"))
     */
    private final String label;
    /**
     * The "description" can carry any length of plain text describing what property the component represents.
     * swe:AbstractSWEIdentifiableType : [0..1] description ((type="string"))
     */
    private final String description;

    // constructors
    /**
     * Constructor with empty initialization.
     */
    protected AbstractSWEIdentifiable () {
        label         = "";
        description   = "";
    }
    /**
     * Constructor for the class, specifying the 2 elements currently supported.
     * @param lab   label
     * @param descr description
     */
    protected AbstractSWEIdentifiable (String lab, String descr) {
        label         = lab;
        description   = descr;
    }

    // access
    /**
     * Getter method for the identifier element.
     * @return The identifier of this SWE object.
     */
    public String getIdentifier() {
        return (null==identifierURI ? "" : identifierURI);
    }
    /**
     * Getter method for the label element.
     * @return The label of this SWE object.
     */
    public String getLabel() {
        return (null==label ? "" : label);
    }
    /**
     * Getter method for the description element.
     * @return The description of this SWE object.
     */
    public String getDescription() {
        return (null==description ? "" : description);
    }

    // methods
    public abstract String toGML();
}
