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

package petascope.wms2.metadata;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Representation of the contact information of the organization responsible for this service.
 * No clear definition is given by the standard, the below class was based on the example response in the annex
 *
 * @author <a href="mailto:dumitru@rasdaman.com">Alex Dumitru</a>
 */
@DatabaseTable(tableName = IPersistentMetadataObject.TABLE_PREFIX + "contact_information")
public class ContactInformation implements ISerializableMetadataObject, IPersistentMetadataObject {

    /**
     * Constructor for the class
     *
     * @param contactPerson                the contact person responsible for the service
     * @param contactOrganization          the contact organization responsible for the service
     * @param contactPosition              the contact position responsible for the service
     * @param addressType                  the type of the address below
     * @param address                      the address of the organization responsible for the service
     * @param city                         the city of the organization responsible for the service
     * @param stateOrProvince              the state or province of the organization responsible for the service
     * @param postCode                     the postcode of the of the organization responsible for the service
     * @param country                      the country of the organization responsible for the service
     * @param contactVoiceTelephone        the telephone number of the organization responsible for the service
     * @param contactFacsimileTelephone    the fax of the organization responsible for the service
     * @param contactElectronicMailAddress the email of the organization responsible for the service
     */
    public ContactInformation(@Nullable String contactPerson, @Nullable String contactOrganization, @Nullable String contactPosition,
                              @Nullable String addressType, @Nullable String address, @Nullable String city,
                              @Nullable String stateOrProvince, @Nullable String postCode, @Nullable String country,
                              @Nullable String contactVoiceTelephone, @Nullable String contactFacsimileTelephone, @Nullable String contactElectronicMailAddress) {
        this.contactPerson = contactPerson;
        this.contactOrganization = contactOrganization;
        this.contactPosition = contactPosition;
        this.addressType = addressType;
        this.address = address;
        this.city = city;
        this.stateOrProvince = stateOrProvince;
        this.postCode = postCode;
        this.country = country;
        this.contactVoiceTelephone = contactVoiceTelephone;
        this.contactFacsimileTelephone = contactFacsimileTelephone;
        this.contactElectronicMailAddress = contactElectronicMailAddress;
    }

    /**
     * Empty constructor to be used by the persistence provider
     */
    protected ContactInformation() {
    }

    /**
     * Returns the path to the template corresponding to this metadata object
     *
     * @return the path to the template
     */
    @NotNull
    @Override
    public InputStream getStreamToTemplate() {
        return this.getClass().getResourceAsStream(PATH_TO_TEMPLATES + "ContactInformation.tpl.xml");
    }

    /**
     * Returns for each variable allowed in the template a corresponding values. The variable name should NOT include the $ prefix
     *
     * @return a map of form (variableKey -> variableValue)
     */
    @NotNull
    @Override
    public Map<String, String> getTemplateVariables() {
        Map<String, String> ret = new HashMap<String, String>();
        ret.put("contactPerson", contactPerson);
        ret.put("contactOrganization", contactOrganization);
        ret.put("contactPosition", contactPosition);
        ret.put("addressType", addressType);
        ret.put("address", address);
        ret.put("city", city);
        ret.put("stateOrProvince", stateOrProvince);
        ret.put("postCode", postCode);
        ret.put("country", country);
        ret.put("contactVoiceTelephone", contactVoiceTelephone);
        ret.put("contactFacsimileTelephone", contactFacsimileTelephone);
        ret.put("contactElectronicMailAddress", contactElectronicMailAddress);
        return ret;
    }

    @DatabaseField(generatedId = true)
    private int id;

    @Nullable
    @DatabaseField(canBeNull = true)
    private String contactPerson;

    @Nullable
    @DatabaseField(canBeNull = true)
    private String contactOrganization;

    @Nullable
    @DatabaseField(canBeNull = true)
    private String contactPosition;

    @Nullable
    @DatabaseField(canBeNull = true)
    private String addressType;

    @Nullable
    @DatabaseField(canBeNull = true)
    private String address;

    @Nullable
    @DatabaseField(canBeNull = true)
    private String city;

    @Nullable
    @DatabaseField(canBeNull = true)
    private String stateOrProvince;

    @Nullable
    @DatabaseField(canBeNull = true)
    private String postCode;

    @Nullable
    @DatabaseField(canBeNull = true)
    private String country;

    @Nullable
    @DatabaseField(canBeNull = true)
    private String contactVoiceTelephone;

    @Nullable
    @DatabaseField(canBeNull = true)
    private String contactFacsimileTelephone;

    @Nullable
    @DatabaseField(canBeNull = true)
    private String contactElectronicMailAddress;

}
