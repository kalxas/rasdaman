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

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petascope.wms2.metadata.dao.PersistentMetadataObjectProvider;

import java.sql.SQLException;
import java.util.Properties;

/**
 * Constructs the default metadata objects. This is needed as some default values for
 * some fields should be presented in the capabilities document even if no data was entered.
 * We provide the following default objects
 * - GetCapabilities formats read from  GET_CAPABILITIES_FORMATS local array
 * - GetMap formats read from GET_MAP_FORMATS local array
 * - ExceptionFormats read form EXCEPTION_FORMATS local array
 * - Service read from the service properties file in the conf dir of rasdaman
 * - ContactInformation read from the service properties file in the conf dir of rasdaman
 *
 * @author <a href="mailto:dumitru@rasdaman.com">Alex Dumitru</a>
 */
public class DefaultMetadataConstructor {

    /**
     * Constructor for the class
     *
     * @param properties                       the properties from which to extract the default values
     * @param persistentMetadataObjectProvider a persistence provider
     */
    public DefaultMetadataConstructor(@NotNull Properties properties, @NotNull PersistentMetadataObjectProvider persistentMetadataObjectProvider) {
        this.persistentMetadataObjectProvider = persistentMetadataObjectProvider;
        this.properties = properties;
    }

    /**
     * Construct all the metadata objects that have defaults
     */
    public void construct() throws SQLException {
        ContactInformation information = constructContactInformation(properties, persistentMetadataObjectProvider);
        Service service = constructService(properties, persistentMetadataObjectProvider, information);
        constructKeywords(properties, persistentMetadataObjectProvider, service);
        constructGetCapabilitiesFormats(persistentMetadataObjectProvider);
        constructGetMapFormats(persistentMetadataObjectProvider);
        constructExceptionFormats(persistentMetadataObjectProvider);
    }

    /**
     * Constructs the default service parameters from the service properties
     *
     * @param properties                       the service properties
     * @param persistentMetadataObjectProvider the persistence provide
     * @return the constructed service
     */
    private static Service constructService(@NotNull Properties properties,
                                            @NotNull PersistentMetadataObjectProvider persistentMetadataObjectProvider,
                                            ContactInformation information) throws SQLException {
        Service service;
        if (persistentMetadataObjectProvider.getService().countOf() == 0) {
            logger.info("Constructing the default service");
            service = new Service(
                readProperty(properties, "service.name"),
                readProperty(properties, "service.title"),
                readProperty(properties, "service.abstract"),
                readProperty(properties, "service.onlineResource"),
                readProperty(properties, "service.fees"),
                information,
                readProperty(properties, "service.accessConstraints")
            );
            service = persistentMetadataObjectProvider.getService().createIfNotExists(service);
        } else {
            service = persistentMetadataObjectProvider.getService().iterator().next();
        }
        return service;
    }

    /**
     * Constructs the default keywords for the service based on the service properties
     *
     * @param properties                       the service properties
     * @param persistentMetadataObjectProvider the persistence provider
     * @param service                          the service to which to assign the keywords
     * @throws SQLException
     */
    private static void constructKeywords(Properties properties, PersistentMetadataObjectProvider persistentMetadataObjectProvider, Service service) throws SQLException {
        if (service.getKeywords() != null && service.getKeywords().size() == 0) {
            logger.info("Constructing the default service keywords");
            String[] keywordStrings = readProperty(properties, "service.keywords").split(",");
            for (String keywordString : keywordStrings) {
                ServiceKeyword keyword = new ServiceKeyword(keywordString.trim(), service);
                persistentMetadataObjectProvider.getServiceKeyword().createIfNotExists(keyword);
            }
        }
    }

    /**
     * Constructs the contact information for the service properties
     *
     * @param properties                       the service properties
     * @param persistentMetadataObjectProvider the persistence provider
     * @throws SQLException
     */
    private static ContactInformation constructContactInformation(Properties properties, PersistentMetadataObjectProvider persistentMetadataObjectProvider) throws SQLException {
        ContactInformation information;
        if (persistentMetadataObjectProvider.getContactInformation().countOf() == 0) {
            logger.info("Constructing the default contact information");
            information = new ContactInformation(
                readProperty(properties, "contact.person"),
                readProperty(properties, "contact.organization"),
                readProperty(properties, "contact.position"),
                readProperty(properties, "contact.addressType"),
                readProperty(properties, "contact.address"),
                readProperty(properties, "contact.city"),
                readProperty(properties, "contact.stateOrProvince"),
                readProperty(properties, "contact.postcode"),
                readProperty(properties, "contact.country"),
                readProperty(properties, "contact.voiceTelephone"),
                readProperty(properties, "contact.FacsimileTelephone"),
                readProperty(properties, "contact.electronicMailAddress")
            );
            return information;
        } else {
            information = persistentMetadataObjectProvider.getContactInformation().iterator().next();
        }
        return information;
    }

    /**
     * Constructs the capabilities formats metadata objects
     *
     * @param persistentMetadataObjectProvider the persistence provider
     * @throws SQLException
     */
    private static void constructGetCapabilitiesFormats(PersistentMetadataObjectProvider persistentMetadataObjectProvider) throws SQLException {
        if (persistentMetadataObjectProvider.getGetCapabilitiesFormat().countOf() == 0) {
            logger.info("Constructing get capabilities formats");
            for (String format : GET_CAPABILITIES_FORMATS) {
                persistentMetadataObjectProvider.getGetCapabilitiesFormat().createIfNotExists(new GetCapabilitiesFormat(format));
            }
        }
    }

    /**
     * Constructs the get map formats metadata objects
     *
     * @param persistentMetadataObjectProvider the persistence provider
     * @throws SQLException
     */
    private static void constructGetMapFormats(PersistentMetadataObjectProvider persistentMetadataObjectProvider) throws SQLException {
        if (persistentMetadataObjectProvider.getGetMapFormat().countOf() == 0) {
            logger.info("Constructing default get map formats");
            int index = 0;
            for (String format : GET_MAP_FORMATS) {
                persistentMetadataObjectProvider.getGetMapFormat().createIfNotExists(new GetMapFormat(format, GET_MAP_RASDAMAN_FORMATS[index]));
                index += 1;
            }
        }
    }

    /**
     * Constructs the exception formats metadata objects
     *
     * @param persistentMetadataObjectProvider the persistence provider
     * @throws SQLException
     */
    private static void constructExceptionFormats(PersistentMetadataObjectProvider persistentMetadataObjectProvider) throws SQLException {
        if (persistentMetadataObjectProvider.getExceptionFormat().countOf() == 0) {
            logger.info("Constructing default exception formats");
            for (String format : EXCEPTION_FORMATS) {
                persistentMetadataObjectProvider.getExceptionFormat().createIfNotExists(new ExceptionFormat(format));
            }
        }
    }

    /**
     * Reads a property as a trimmed string. If the property is null, an empty string is returned
     *
     * @param properties   the properties object to get the property from
     * @param propertyName the name of the property
     * @return the property value
     */
    @NotNull
    private static String readProperty(@NotNull Properties properties, @NotNull String propertyName) {
        String prop = properties.getProperty(propertyName);
        if (prop == null) {
            prop = "";
        }
        return prop.trim();
    }


    @NotNull
    private final Properties properties;
    @NotNull
    private final PersistentMetadataObjectProvider persistentMetadataObjectProvider;
    private static final String[] GET_CAPABILITIES_FORMATS = new String[] {"text/xml"};
    private static final String[] GET_MAP_FORMATS = new String[] {"image/png", "image/jpeg", "image/tiff"};
    private static final String[] GET_MAP_RASDAMAN_FORMATS = new String[] {"png", "jpeg", "GTiff"};
    private static final String[] EXCEPTION_FORMATS = new String[] {"application/vnd.ogc.se_inimage", "application/vnd.ogc.se_xml", "application/vnd.ogc.se_blank"};
    @NotNull
    private static final Logger logger = LoggerFactory.getLogger(DefaultMetadataConstructor.class);
}
