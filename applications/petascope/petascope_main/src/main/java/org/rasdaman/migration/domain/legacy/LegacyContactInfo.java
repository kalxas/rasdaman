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
 * Copyright 2003 - 2018 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package org.rasdaman.migration.domain.legacy;

import java.util.ArrayList;
import java.util.List;

/**
 * Java class for ows:ContactInfo elements.
 * @author <a href="mailto:p.campalani@jacobs-university.de">Piero Campalani</a>
 */
public class LegacyContactInfo {

    private List<String> voicePhone;
    private List<String> facsimilePhone;
    private LegacyAddress contactAddress;
    private String hoursOfService;
    private String contactInstructions;

    // Constructor
    LegacyContactInfo() {
        contactAddress = new LegacyAddress();
        voicePhone = new ArrayList<String>();
        facsimilePhone = new ArrayList<String>();
    }

    // Getters/Setters
    public List<String> getVoicePhones() {
        return new ArrayList<String>(voicePhone);
    }
    public void addVoicePhone(String phone) {
        voicePhone.add(phone);
    }
    //
    public List<String> getFacsimilePhones() {
        return new ArrayList<String>(facsimilePhone);
    }
    public void addFacsimilePhone(String facsimile) {
        facsimilePhone.add(facsimile);
    }
    //
    public LegacyAddress getAddress() {
        return contactAddress;
    }
    //
    public String getHoursOfService() {
        return hoursOfService;
    }
    public void setHoursOfService(String hours) {
        hoursOfService = hours;
    }
    //
    public String getInstructions() {
        return contactInstructions;
    }
    public void setInstructions(String instructions) {
        contactInstructions = instructions;
    }

    // Methods
    public boolean isEmpty() {
        return getVoicePhones().isEmpty() &&
               getFacsimilePhones().isEmpty() &&
               getAddress().getAddressMetadata().isEmpty() &&
               getHoursOfService().isEmpty() &&
               getInstructions().isEmpty();
    }
}
