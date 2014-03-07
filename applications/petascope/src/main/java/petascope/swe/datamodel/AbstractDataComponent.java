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
 * Abstract base class for all data components.
 *
 * @author <a href="mailto:p.campalani@jacobs-university.de">Piero Campalani</a>
 */
public abstract class AbstractDataComponent extends AbstractSWEIdentifiable {

    // fields
    /**
     * The "updatable" attribute is an optional flag indicating if the component value is fixed or can be updated.
     * It is only applicable if the data component is used to define the input of a process.
     * swe:AbstractSWEIdentifiable : [0..1] @updatable ((type="boolean"))
     */
    private final boolean updatable = false;
    /**
     * The "optional" attribute is an optional flag indicating if the component value can be omitted in the data stream.
     * It is only meaningful if the component is used as a schema descriptor (i.e. not for a component containing an inline value).
     * It is ‘false’ by default.
     * swe:AbstractSWEIdentifiable : [0..1] @optional ((type="boolean" default="false"))
     */
    private final boolean optional = false;
    /**
     * The "definition" attribute identifies the property that the data component represents by using a scoped name.
     * It should map to a controlled term defined in an (web accessible) dictionary, registry or ontology.
     * Such terms provide the formal textual definition agreed upon by one or more communities,
     * eventually illustrated by pictures and diagrams as well as additional semantic information
     * such as relationships to units and other concepts, ontological mappings, etc.
     * swe:AbstractSWEIdentifiable : [0..1] @definition ((type="anyURI"))
     */
    private final String definitionURI;

    // constructors
    /**
     * Constructor with empty initialization.
     */
    protected AbstractDataComponent() {
        super();
        definitionURI = "";
    }
    /**
     * Constructor specifying a definition URI.
     * @param definition
     */
    protected AbstractDataComponent(String definition) {
        super();
        definitionURI = definition;
    }
    /**
     * Full attributes constructor (currently supported elements).
     * @param label
     * @param description
     * @param definition
     */
    protected AbstractDataComponent(String label, String description, String definition) {
        super(label, description);
        definitionURI = definition;
    }

    // access
    /**
     * Getter method for the description element.
     * @return The description of this SWE data component.
     */
    public String getDefinition() {
        return (null==definitionURI ? "" : definitionURI);
    }

    // methods
    public abstract String toGML();
}
