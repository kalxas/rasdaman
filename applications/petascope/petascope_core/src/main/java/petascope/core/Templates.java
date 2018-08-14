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
package petascope.core;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;

/**
 * Load templates, etc.
 *
 * @author <a href="mailto:d.misev@jacobs-university.de">Dimitar Misev</a>
 * @author <a href="mailto:v.bajpai@jacobs-university.de">Vaibhav Bajpai</a>
 */
public class Templates {

    private static final Logger log = LoggerFactory.getLogger(Templates.class);

    // ******* General files *******
    // miscellanea
    public static final String GENERAL_WCS_EXCEPTION_REPORT = "WCS_ExceptionReport";
    public static final String GENERAL_WMS_EXCEPTION_REPORT = "WMS_ExceptionReport";
    public static final String GENERAL_SOAP_FAULT = "SOAPFault";
    public static final String GENERAL_SOAP_MESSAGE = "SOAPMessage";

    // Replacement Strings in the template files (for ExceptionReport)
    public static final String GENERAL_EXCEPTION_CODE_REPLACEMENT = "%exceptionCode%";
    public static final String GENERAL_EXCEPTION_TEXT_REPLACEMENT = "%exceptionText%";
    public static final String PETASCOPE_URL = "%PetascopeURL%";

    // ******* WCS 2.0.1 files *******
    // Get Capabilities (it is duplicated with WMS Getcapabilities no need to add prefix for this case).
    public static final String WCS2_GET_CAPABILITIES_FILE = "WCS2_GetCapabilities";
    public static final String WCS2_GET_CAPABILITIES_OPERATIONS_METADATA_FILE = "WCS2_GetCapabilities_OperationsMetadata";

    // Describe Coverage
    public static final String WCS2_DESCRIBE_COVERAGE_FILE = "DescribeCoverage";
    public static final String WCS2_DESCRIBE_COVERAGE_COVERAGE_DESCRIPTION_FILE = "DescribeCoverage_CoverageDescription";
    public static final String WCS2_DESCRIBE_COVERAGE_CONTENT = "%DescribeCoverageContent%";

    // Get Coverage
    public static final String WCS2_GET_COVERAGE_FILE = "GetCoverage";
    public static final String WCS2_GRID_FUNCTION = "GridFunction";
    public static final String WCS2_RANGE_FIELD = "RangeField";

    // domainSets
    public static final String WCS2_GENERAL_GRID_AXIS = "GeneralGridAxis";
    public static final String WCS2_GRID = "Grid";
    public static final String WCS2_OFFSET_VECTOR = "OffsetVector";
    public static final String WCS2_SIMPLE_MULTIPOINT = "SimpleMultiPoint";
    public static final String WCS2_RECTIFIED_GRID = "RectifiedGrid";
    public static final String WCS2_REFERENCEABLE_GRID_BY_VECTORS = "ReferenceableGridByVectors";

    //wcs-t
    public static final String WCS2_WCST_INSERT_COVERAGE_RESPONSE = "InsertCoverageResponse";

    // Replacement Strings in the template files
    public static final String WCS2_GET_CAPABILITIES_SERVICE_IDENTIFICATION_ELEMENT = "%ServiceIdentificationElement%";
    public static final String WCS2_GET_CAPABILITIES_SERVICE_PROVIDER_ELEMENT = "%ServiceProviderElement%";
    public static final String WCS2_GET_CAPABILITIES_OPERATIONS_METADATA_ELEMENT = "%OperationsMetadataElement%";
    public static final String WCS2_GET_CAPABILITIES_OPERATIONS_METADATA_ELEMENT_URL = "%URL%";
    public static final String WCS2_GET_CAPABILITIES_SERVICE_METADATA_ELEMENT = "%ServiceMetadataElement%";
    public static final String WCS2_GET_CAPABILITIES_CONTENTS_ELEMENT = "%ContentsElement%";
    public static final String WCS2_WCST_INSERT_COVERAGE_COVERAGE_ID = "%coverageId%";

    // ******* WMS 1.3 files *******
    public static final String WMS_GET_CAPABILITIES = "WMS_GetCapabilities";
    public static final String WMS_GET_CAPABILITIES_CAPABILITY = "WMS_GetCapabilities_Capability";
    public static final String WMS_GET_CAPABILITIES_CAPABILITY_DCPTYPE = "WMS_GetCapabilities_Capability_DCPType";
    // Replacement Strings in the template files    
    public static final String WMS_GET_CAPABILITIES_SERVICE_ELEMENT = "%ServiceElement%";
    public static final String WMS_GET_CAPABILITIES_CAPABILITY_ELEMENT = "%CapabilityElement%";
    public static final String WMS_GET_CAPABILITIES_CAPABILITY_GET_MAP_ELEMENT = "%GetMapElement%";
    public static final String WMS_GET_CAPABILITIES_CAPABILITY_EXCEPTION_ELEMENT = "%ExceptionElement%";
    public static final String WMS_GET_CAPABILITIES_CAPABILITY_LAYER_ELEMENTS = "%LayerElements%";

    /**
     * template name -> (template as string, template as XOM element)
     */
    private static final Map<String, String> templates = new HashMap<>();

    private static final String TEMPLATE_FOLDER = "templates";
    private static final String GENERAL_PREFIX = "general";
    private static final String WCS2_PREFIX = "wcs2";
    private static final String WMS_PREFIX = "wms";

    private static final String GENERAL_TEMPLATE_FOLDER = TEMPLATE_FOLDER + "/" + GENERAL_PREFIX;
    private static final String WCS2_TEMPLATE_FOLDER = TEMPLATE_FOLDER + "/" + WCS2_PREFIX;
    private static final String WMS_TEMPLATE_FOLDER = TEMPLATE_FOLDER + "/" + WMS_PREFIX;
    private static final String TEMPLATE_FILE_EXTENSION = ".templ";

    // load templates
    static {
        // miscellanea
        loadGeneralTemplate(GENERAL_SOAP_MESSAGE);
        loadGeneralTemplate(GENERAL_SOAP_FAULT);
        loadGeneralTemplate(GENERAL_WCS_EXCEPTION_REPORT);
        loadGeneralTemplate(GENERAL_WMS_EXCEPTION_REPORT);

        // WCS 2.0.1 services
        // Get Capabilities
        loadWCS2Template(WCS2_GET_CAPABILITIES_FILE);
        loadWCS2Template(WCS2_GET_CAPABILITIES_OPERATIONS_METADATA_FILE);

        // coverage
        // Describe Coverage
        loadWCS2Template(WCS2_DESCRIBE_COVERAGE_FILE);
        loadWCS2Template(WCS2_DESCRIBE_COVERAGE_COVERAGE_DESCRIPTION_FILE);

        // Get Coverage
        loadWCS2Template(WCS2_GET_COVERAGE_FILE);
        loadWCS2Template(WCS2_DESCRIBE_COVERAGE_FILE);
        loadWCS2Template(WCS2_GRID_FUNCTION);
        loadWCS2Template(WCS2_RANGE_FIELD);
        // domainSets
        loadWCS2Template(WCS2_GENERAL_GRID_AXIS);
        loadWCS2Template(WCS2_GRID);
        loadWCS2Template(WCS2_OFFSET_VECTOR);
        loadWCS2Template(WCS2_RECTIFIED_GRID);
        loadWCS2Template(WCS2_REFERENCEABLE_GRID_BY_VECTORS);
        loadWCS2Template(WCS2_SIMPLE_MULTIPOINT);

        //wcs-t
        loadWCS2Template(WCS2_WCST_INSERT_COVERAGE_RESPONSE);

        // WMS 1.3
        loadWMSTemplate(WMS_GET_CAPABILITIES);
        loadWMSTemplate(WMS_GET_CAPABILITIES_CAPABILITY);
        loadWMSTemplate(WMS_GET_CAPABILITIES_CAPABILITY_DCPTYPE);
    }

    /**
     * Load a General template to map
     *
     * @param templateFileName
     */
    private static void loadGeneralTemplate(String templateFileName) {
        loadTemplate(templateFileName, GENERAL_PREFIX);
    }

    /**
     * Load a WCS2 template to map
     *
     * @param templateFileName
     */
    private static void loadWCS2Template(String templateFileName) {
        loadTemplate(templateFileName, WCS2_PREFIX);
    }

    /**
     * Load a WMS template to map
     *
     * @param templateFileName
     */
    private static void loadWMSTemplate(String templateFileName) {
        loadTemplate(templateFileName, WMS_PREFIX);
    }

    private static void loadTemplate(String templateFileName, String prefix) {
        try {
            // Get file from wcs2 resources folder
            ClassLoader classLoader = Templates.class.getClassLoader();
            String filePath = getTemplateFolder(prefix) + "/" + templateFileName + TEMPLATE_FILE_EXTENSION;
            String contents = IOUtils.toString(classLoader.getResourceAsStream(filePath));
            templates.put(templateFileName, contents);
        } catch (Exception ex) {
            log.error("Error loading template file: " + templateFileName, ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    /**
     * Return the correspondent folder from the prefix of file name
     *
     * @param templateFileName
     * @return
     */
    private static String getTemplateFolder(String prefix) throws PetascopeException {
        if (prefix.equals(GENERAL_PREFIX)) {
            return GENERAL_TEMPLATE_FOLDER;
        } else if (prefix.equals(WCS2_PREFIX)) {
            return WCS2_TEMPLATE_FOLDER;
        } else if (prefix.equals(WMS_PREFIX)) {
            return WMS_TEMPLATE_FOLDER;
        }
        throw new PetascopeException(ExceptionCode.InternalComponentError, "Prefix is not supported, given: " + prefix);
    }

    /**
     * Load a template file from this package.
     *
     * @param template name of template
     * @return the template
     */
    public static String getTemplate(String template) {
        return templates.get(template);
    }

    /**
     * Load a template file from this package.
     *
     * @param template name of template
     * @param replacements
     * @return the template
     */
    public static String getTemplate(String template, Pair<String, String>... replacements) {
        String ret = getTemplate(template);
        if (ret != null) {
            for (Pair<String, String> p : replacements) {
                ret = ret.replace(p.fst, p.snd);
            }
        }
        return ret;
    }
}
