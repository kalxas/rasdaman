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
package org.rasdaman.migration.legacy;

import java.util.ArrayList;
import java.util.List;

/**
 * Java class for ows:ServiceIdentification elements.
 * @author <a href="mailto:p.campalani@jacobs-university.de">Piero Campalani</a>
 */
public class LegacyServiceIdentification {

    private LegacyDescription serviceDescription;
    private String serviceType;
    private String serviceTypeCodespace;
    private List<String> serviceTypeVersions;
    private String serviceFees;
    private List<String> accessConstraints;

    // Constructors
    // Title and abstract are optional: ServiceType and ServiceTypeVersion are mandatory
    public LegacyServiceIdentification(String type, List<String> typeVersions) {
        serviceType = type;
        serviceTypeVersions = typeVersions;
        serviceDescription = new LegacyDescription();
        accessConstraints = new ArrayList<String>();
    }
    public LegacyServiceIdentification(String type, String codespace, List<String> typeVersions) {
        this(type, typeVersions);
        serviceTypeCodespace = codespace;
    }

    // Getters/Setterss
    public LegacyDescription getDescription() {
        return serviceDescription;
    }
    public void setDescription(LegacyDescription descr) {
        serviceDescription = descr;
    }
    //
    public String getType() {
        return serviceType;
    }
    //
    public String getTypeCodeSpace() {
        return serviceTypeCodespace;
    }
    //
    public List<String> getTypeVersions() {
        return serviceTypeVersions;
    }
    //
    public String getFees() {
        return serviceFees;
    }
    public void setFees(String fees) {
        serviceFees = fees;
    }
    //
    public List<String> getAccessConstraints() {
        return accessConstraints;
    }
    public void addAccessConstraint(String constraint) {
        accessConstraints.add(constraint);
    }
} //~ ServiceIdentification
