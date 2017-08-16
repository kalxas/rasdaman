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
package org.rasdaman.migration.domain.legacy;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Array;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.rasdaman.migration.domain.legacy.LegacyConfigManager.*;
import static org.rasdaman.migration.domain.legacy.LegacyRasConstants.*;
import petascope.core.CrsDefinition;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCSException;
import petascope.util.CrsUtil;
import static petascope.exceptions.ExceptionCode.WCSTDuplicatedCoverageId;

/**
 * The DbMetadataSource is a IMetadataSource that uses a relational database. It
 * keeps a global connection which is reused on future requests, as well as
 * between threads. Before each read, the connection is verified to be valid,
 * and recreated if necessary. This IMetadataSource is not particularly
 * efficient, because it accesses the database at least once for every read. To
 * increase efficiency, wrap a CachedMetadataSource around this one.
 *
 */
public class LegacyDbMetadataSource implements LegacyIMetadataSource {

    //private static Logger log = LoggerFactory.getLogger(LegacyDbMetadataSource.class);

    /*
     *  Petascopedb Tables and Fields
     */
    public static final String TABLES_PREFIX = "ps_";
    public static final int MIN_TABLES_NUMBER = 30; // NB: Keep it up-to-date

    public static final String POSTGRE_PREFIX = "pg_";
    public static final String CURRENT_SCHEMA = "CURRENT_SCHEMA";

    /* PostgreSQL tables */
    public static final String TABLE_TABLES_CATALOG = POSTGRE_PREFIX + "catalog." + POSTGRE_PREFIX + "tables";
    public static final String TABLES_CATALOG_SCHEMANAME = "schemaname";
    public static final String TABLES_CATALOG_TABLENAME = "tablename";

    /* Dictionary tables (coverage-independent) */
    // TABLE_EXTRAMETADATA_TYPE : types of extra-metadata for a coverage (eg GMLCOV, OWS, etc.)
    public static final String TABLE_EXTRAMETADATA_TYPE = TABLES_PREFIX + "extra_metadata_type";
    public static final String EXTRAMETADATA_TYPE_ID = "id";
    public static final String EXTRAMETADATA_TYPE_TYPE = "type";
    public static final String EXTRAMETADATA_TYPE_OWS = "ows"; // need to know how OWS Metadata is recognized to enable/disable it
    public static final String EXTRAMETADATA_TYPE_GMLCOV = "gmlcov";
    // TABLE_CRS : list of /single/ Coordinate Reference Systems (no compound CRS)
    public static final String TABLE_CRS = TABLES_PREFIX + "crs";
    public static final String CRS_ID = "id";
    public static final String CRS_URI = "uri";
    // TABLE_FORMAT : list of formats abbreviations
    public static final String TABLE_FORMAT = TABLES_PREFIX + "format";
    public static final String FORMAT_ID = "id";
    public static final String FORMAT_NAME = "name";
    public static final String FORMAT_MIME_ID = "mime_type_id";
    public static final String FORMAT_GDAL_ID = "gdal_id";
    // TABLE_GDAL_FORMAT : list of GDAL-understandable formats (see $ gdal_translate --formats)
    public static final String TABLE_GDAL_FORMAT = TABLES_PREFIX + "gdal_format";
    public static final String GDAL_FORMAT_ID = "id";
    public static final String GDAL_FORMAT_GDAL_ID = "gdal_id";
    public static final String GDAL_FORMAT_DESCRIPTION = "description";
    // TABLE_MIME_TYPE : list of MIME types (WCS requests use this)
    public static final String TABLE_MIME_TYPE = TABLES_PREFIX + "mime_type";
    public static final String MIME_TYPE_ID = "id";
    public static final String MIME_TYPE_MIME = "mime_type";
    // TABLE_GML_SUBTYPE : list of GML coverage types (eg Coverage, RectifiedGridCoverage, MultiPointCoverage, etc)
    public static final String TABLE_GML_SUBTYPE = TABLES_PREFIX + "gml_subtype";
    public static final String GML_SUBTYPE_ID = "id";
    public static final String GML_SUBTYPE_SUBTYPE = "subtype";
    public static final String GML_SUBTYPE_PARENT = "subtype_parent";
    // TABLE_INTERVAL : to determine allowed values in a coverage attribute space (range-type)
    public static final String TABLE_INTERVAL = TABLES_PREFIX + "interval";
    public static final String INTERVAL_ID = "id";
    public static final String INTERVAL_MIN = "min";
    public static final String INTERVAL_MAX = "max";
    // TABLE_UOM : Units of Measure (UoM)
    public static final String TABLE_UOM = TABLES_PREFIX + "uom";
    public static final String UOM_ID = "id";
    public static final String UOM_CODE = "code";
    // TABLE_QUANTITY : quantities (see OGC SWE)
    public static final String TABLE_QUANTITY = TABLES_PREFIX + "quantity";
    public static final String QUANTITY_ID = "id";
    public static final String QUANTITY_UOM_ID = "uom_id";
    public static final String QUANTITY_LABEL = "label";
    public static final String QUANTITY_DESCRIPTION = "description";
    public static final String QUANTITY_DEFINITION = "definition_uri";
    public static final String QUANTITY_SIGNIFICANT_FIGURES = "significant_figures";
    public static final String QUANTITY_NIL_IDS = "nil_ids";
    // TABLE_NIL_VALUE : catalog of NIL value/reason mappings
    public static final String TABLE_NIL_VALUE = TABLES_PREFIX + "nil_value";
    public static final String NIL_VALUE_ID = "id";
    public static final String NIL_VALUE_VALUE = "value";
    public static final String NIL_VALUE_REASON = "reason";
    // TABLE_QUANTITY_INTERVAL : association table between TABLE_INTERVAL and TABLE_QUANTITY
    public static final String TABLE_QUANTITY_INTERVAL = TABLES_PREFIX + "quantity_interval";
    public static final String QUANTITY_INTERVAL_IID = "interval_id";
    public static final String QUANTITY_INTERVAL_QID = "quantity_id";
    // TABLE_MULTIPOINT : store geometry and rangeset of multipoint coverages
    public static final String TABLE_MULTIPOINT = TABLES_PREFIX + "multipoint";
    public static final String MULTIPOINT_ID = "id";
    public static final String MULTIPOINT_COVERAGE_ID = "coverage_id";
    public static final String MULTIPOINT_COORDINATE = "coordinate";
    public static final String MULTIPOINT_VALUE = "value";
    // TABLE_RANGE_DATATYPE : WCPS range types [OGC 08-068r2, Tab.2]
    public static final String TABLE_RANGE_DATATYPE = TABLES_PREFIX + "range_data_type";
    public static final String RANGE_DATATYPE_ID = "id";
    public static final String RANGE_DATATYPE_NAME = "name";
    public static final String RANGE_DATATYPE_MEANING = "meaning";

    /* Transversal OWS-related tables */
    // TABLE_DESCRIPTION : ows:Description used in coverage summaries and service identification
    public static final String TABLE_DESCRIPTION = TABLES_PREFIX + "description";
    public static final String DESCRIPTION_ID = "id";
    public static final String DESCRIPTION_TITLES = "titles";
    public static final String DESCRIPTION_ABSTRACTS = "abstracts";
    public static final String DESCRIPTION_KEYWORD_GROUP_IDS = "keyword_group_ids";
    // TABLE_KEYWORD : keywords for the WCS service identification
    public static final String TABLE_KEYWORD = TABLES_PREFIX + "keyword";
    public static final String KEYWORD_ID = "id";
    public static final String KEYWORD_VALUE = "value";
    public static final String KEYWORD_LANGUAGE = "language";
    // TABLE_KEYWORD_GROUP
    public static final String TABLE_KEYWORD_GROUP = TABLES_PREFIX + "keyword_group";
    public static final String KEYWORD_GROUP_ID = "id";
    public static final String KEYWORD_GROUP_KEYWORD_IDS = "keyword_ids";
    public static final String KEYWORD_GROUP_TYPE = "type";
    public static final String KEYWORD_GROUP_TYPE_CODESPACE = "type_codespace";

    /* WCS Service-related tables */
    // TABLE_SERVICE_IDENTIFICATION : metadata for the WCS service
    public static final String TABLE_SERVICE_IDENTIFICATION = TABLES_PREFIX + "service_identification";
    public static final String SERVICE_IDENTIFICATION_ID = "id";
    public static final String SERVICE_IDENTIFICATION_TYPE = "type";
    public static final String SERVICE_IDENTIFICATION_TYPE_CODESPACE = "type_codespace";
    public static final String SERVICE_IDENTIFICATION_TYPE_VERSIONS = "type_versions";
    public static final String SERVICE_IDENTIFICATION_DESCRIPTION_ID = "description_id";
    public static final String SERVICE_IDENTIFICATION_FEES = "fees";
    public static final String SERVICE_IDENTIFICATION_CONSTRAINTS = "access_constraints";
    // TABLE_SERVICE_PROVIDER : metadata for the WCS service provider
    public static final String TABLE_SERVICE_PROVIDER = TABLES_PREFIX + "service_provider";
    public static final String SERVICE_PROVIDER_ID = "id";
    public static final String SERVICE_PROVIDER_NAME = "name";
    public static final String SERVICE_PROVIDER_SITE = "site";
    public static final String SERVICE_PROVIDER_CONTACT_NAME = "contact_individual_name";
    public static final String SERVICE_PROVIDER_CONTACT_POSITION = "contact_position_name";
    public static final String SERVICE_PROVIDER_CONTACT_PHONE_ID = "contact_phone_id";
    public static final String SERVICE_PROVIDER_CONTACT_DELIVERY = "contact_delivery_points";
    public static final String SERVICE_PROVIDER_CONTACT_CITY = "contact_city";
    public static final String SERVICE_PROVIDER_CONTACT_AREA = "contact_administrative_area";
    public static final String SERVICE_PROVIDER_CONTACT_PCODE = "contact_postal_code";
    public static final String SERVICE_PROVIDER_CONTACT_COUNTRY = "contact_country";
    public static final String SERVICE_PROVIDER_CONTACT_EMAIL = "contact_email_addresses";
    public static final String SERVICE_PROVIDER_CONTACT_HOURS = "contact_hours_of_service";
    public static final String SERVICE_PROVIDER_CONTACT_INSTRUCTIONS = "contact_instructions";
    public static final String SERVICE_PROVIDER_CONTACT_ROLE_ID = "contact_role_id";
    // TABLE_TELEPHONE : voice+facsimile tuples
    public static final String TABLE_TELEPHONE = TABLES_PREFIX + "telephone";
    public static final String TELEPHONE_ID = "id";
    public static final String TELEPHONE_VOICE = "voice";
    public static final String TELEPHONE_FACSIMILE = "facsimile";
    // TABLE_ROLE_CODE
    public static final String TABLE_ROLE_CODE = TABLES_PREFIX + "role_code";
    public static final String ROLE_CODE_ID = "id";
    public static final String ROLE_CODE_VALUE = "value";


    /* Coverage-related tables */
    // TABLE_COVERAGE : root table of a gml:*Coverage
    public static final String TABLE_COVERAGE = TABLES_PREFIX + "coverage";
    public static final String COVERAGE_ID = "id";
    public static final String COVERAGE_NAME = "name";
    public static final String COVERAGE_GML_TYPE_ID = "gml_type_id";
    public static final String COVERAGE_NATIVE_FORMAT_ID = "native_format_id";
    public static final String COVERAGE_DESCRIPTION_ID = "description_id";
    // TABLE_EXTRAMETADATA : descriptive metadata
    public static final String TABLE_EXTRAMETADATA = TABLES_PREFIX + "extra_metadata";
    public static final String EXTRAMETADATA_ID = "id";
    public static final String EXTRAMETADATA_COVERAGE_ID = "coverage_id";
    public static final String EXTRAMETADATA_METADATA_TYPE_ID = "metadata_type_id";
    public static final String EXTRAMETADATA_VALUE = "value";
    // Domain-set //
    // TABLE_DOMAINSET : common geometric information for any type of coverage
    public static final String TABLE_DOMAINSET = TABLES_PREFIX + "domain_set";
    public static final String DOMAINSET_COVERAGE_ID = "coverage_id";
    public static final String DOMAINSET_NATIVE_CRS_IDS = "native_crs_ids";
    // TABLE_GRIDDED_DOMAINSET : geometry information specific to gridded coverages
    public static final String TABLE_GRIDDED_DOMAINSET = TABLES_PREFIX + "gridded_domain_set";
    public static final String GRIDDED_DOMAINSET_COVERAGE_ID = "coverage_id";
    public static final String GRIDDED_DOMAINSET_ORIGIN = "grid_origin";
    // TABLE_GRID_AXIS : axis-specific information, regardless of its regularity and rectilinearity
    public static final String TABLE_GRID_AXIS = TABLES_PREFIX + "grid_axis";
    public static final String GRID_AXIS_ID = "id";
    public static final String GRID_AXIS_COVERAGE_ID = "gridded_coverage_id";
    public static final String GRID_AXIS_RASDAMAN_ORDER = "rasdaman_order";
    // TABLE_RECTILINEAR_AXIS : if an axis is rectilinear, then we can define it with offset vectors
    public static final String TABLE_RECTILINEAR_AXIS = TABLES_PREFIX + "rectilinear_axis";
    public static final String RECTILINEAR_AXIS_ID = "grid_axis_id";
    public static final String RECTILINEAR_AXIS_OFFSET_VECTOR = "offset_vector";
    // TABLE_VECTOR_COEFFICIENTS : /long/ table, storing the coefficients (c*offset_vector) of an irregularly-spaced rectilinear axis
    public static final String TABLE_VECTOR_COEFFICIENTS = TABLES_PREFIX + "vector_coefficients";
    public static final String VECTOR_COEFFICIENTS_AXIS_ID = "grid_axis_id";
    public static final String VECTOR_COEFFICIENTS_COEFFICIENT = "coefficient";
    public static final String VECTOR_COEFFICIENTS_ORDER = "coefficient_order";
    // Range-set //
    // TABLE_RANGE_SET : hub for different range-set options for a coverage: rasdaman (-> TABLE_RASDAMAN_COLLECTION), PostGIS, etc.
    public static final String TABLE_RANGESET = TABLES_PREFIX + "range_set";
    public static final String RANGESET_ID = "id";
    public static final String RANGESET_COVERAGE_ID = "coverage_id";
    public static final String RANGESET_STORAGE_TABLE = "storage_table";
    public static final String RANGESET_STORAGE_ID = "storage_id";
    // TABLE_RASDAMAN_COLLECTION : list of rasdaman collections (1 coverage = 1 MDD // 1 collection = 1+ MDDs)
    public static final String TABLE_RASDAMAN_COLLECTION = TABLES_PREFIX + "rasdaman_collection";
    public static final String RASDAMAN_COLLECTION_ID = "id";
    public static final String RASDAMAN_COLLECTION_NAME = "name";
    public static final String RASDAMAN_COLLECTION_OID = "oid";
    public static final String RASDAMAN_COLLECTION_BASE_TYPE = "base_type";

    // Range-type //
    // TABLE_RANGETYPE_COMPONENT : components (aka bands, or channels) of a coverage
    public static final String TABLE_RANGETYPE_COMPONENT = TABLES_PREFIX + "range_type_component";
    public static final String RANGETYPE_COMPONENT_ID = "id";
    public static final String RANGETYPE_COMPONENT_COVERAGE_ID = "coverage_id";
    public static final String RANGETYPE_COMPONENT_NAME = "name";
    public static final String RANGETYPE_COMPONENT_TYPE_ID = "data_type_id";
    public static final String RANGETYPE_COMPONENT_ORDER = "component_order";
    public static final String RANGETYPE_COMPONENT_FIELD_TABLE = "field_table";
    public static final String RANGETYPE_COMPONENT_FIELD_ID = "field_id";

    // Bounding Box//
    // TABLE_BOUNDING_BOX
    public static final String TABLE_BOUNDING_BOX = TABLES_PREFIX + "bounding_box";
    public static final String LOWER_LEFT = "lower_left";
    public static final String UPPER_RIGHT = "upper_right";
    /* ~end TABLES */

 /* Stored procedures */
    private static String PROCEDURE_IDX = "idx";

    /* SQL aliases */
    private static String NIL_VALUES_ALIAS = "nil_values";
    private static String NIL_REASONS_ALIAS = "nil_reasons";

    // postgres-style
    private static String CASE_INSENSITIVE_LIKE = "ILIKE";

    // parsing of sdom output
    public static final String DIMENSION_SEPARATOR = ",";
    public static final String DIMENSION_BOUND_SEPARATOR = ":";

    /* Status variables */
    private boolean initializing;
    private boolean checkAtInit;

    /* Contents of (static) dictionary-tables */
    // TODO: map DB tables to dedicated classes instead of Map objects
    private LegacyServiceMetadata sMeta;
    private Map<Integer, String> extraMetadataTypes;
    private Map<Integer, String> gmlSubTypes;
    private Map<String, String> gmlChildParent; // GML type -> parent type
    private Map<Integer, String> mimeTypes;
    private Map<String, String> supportedFormats; // format -> MIME type
    private Map<String, String> gdalFormatsIds; // GDAL code -> format name
    private Map<Integer, String> rangeDataTypes;

    /* Contents of (static) dictionary-tables (reversed) */
    private Map<String, Integer> revExtraMetadataTypes;
    private Map<String, Integer> revGmlSubTypes;
    private Map<String, Integer> revMimeTypes;
    private Map<String, String> revSupportedFormats; // MIME type -> format
    private Map<String, String> revGdalFormatsIds; // format name -> GDAL code
    private Map<String, Integer> revRangeDataTypes;

    /* Database access info */
    private static final String pgDriver = "org.postgresql.Driver";
    private String driver;
    private String pass;
    private String url;
    private String user;

    /* Global database key variables */
    private Connection conn;
    private Savepoint savepoint;
    private String query;

    /* Cache */
    private Map<String, LegacyCoverageMetadata> cache = new HashMap<String, LegacyCoverageMetadata>();

    // this is done for backwards compatibility: before version 9.3.2 the origin and coefficients
    // resulting from time conversion were computed as BigDecimals, but written as doubles, which
    // adds random decimals to match the precision of double
    // this means that in order to support databases created pre 9.3.2, a small epsilon (10^-10)around the
    // computed coefficient must be considered
    BigDecimal EPSILON = new BigDecimal("0.0000000001");

    /*------------------------------------------------*/
    /**
     * Main constructor with coverage metadata consistency check at startup.
     *
     * @param driver JDBC driver
     * @param url database connection string
     * @param user database connection username
     * @param pass database connection password
     * @throws PetascopeException
     * @throws SecoreException
     */
    public LegacyDbMetadataSource(String driver, String url, String user, String pass)
            throws Exception {
        this(driver, url, user, pass, true);
    }

    /**
     * Main constructor.
     *
     * @param driver JDBC driver
     * @param url database connection string
     * @param user database connection username
     * @param pass database connection password
     * @param checkAtInit if true then check coverage metadata consistency at
     * startup.
     * @throws PetascopeException
     * @throws SecoreException
     */
    public LegacyDbMetadataSource(String driver, String url, String user, String pass, boolean checkAtInit)
            throws Exception {
        try {
            this.driver = driver;
            Class.forName(driver).newInstance();
        } catch (ClassNotFoundException e) {
            throw new Exception("Metadata database error: Could not find JDBC driver: " + driver, e);
        } catch (InstantiationException e) {
            throw new Exception("Metadata database error: Could not instantiate JDBC driver: " + driver, e);
        } catch (IllegalAccessException e) {
            throw new Exception("Metadata database error: Access denied to JDBC driver: " + driver, e);
        }

        if (LegacyConfigManager.METADATA_SQLITE) {
            // SQLite's LIKE is case insensitive by default
            CASE_INSENSITIVE_LIKE = "LIKE";
        }

        this.driver = driver;
        this.url = url;
        this.user = user;
        this.pass = pass;
        this.checkAtInit = checkAtInit;

        readStaticTables();
    }

    /**
     * Read and cache static information from the database.
     *
     * @throws PetascopeException
     * @throws SecoreException
     */
    private void readStaticTables() throws Exception {
        Statement s = null;

        try {

            /*
             *  Read contents of static metadata tables
             */
            ensureConnection();
            s = conn.createStatement();
            String sqlQuery; // buffer for SQL queries

            /* TABLE_DESCRIPTION */
            // titles/abstracts/keywords
            // NOTE: do not read them now: there might be unused descriptions in PS_DESCRIPTIONS.
            // Service metadata: init
            sMeta = new LegacyServiceMetadata();

            // Check PS_ tables habe been created
            int detectedTables = countTables(TABLES_PREFIX + "%");
            if (detectedTables < MIN_TABLES_NUMBER) {
                //log.error("Missing " + TABLES_PREFIX + "* tables in the database.");
                throw new Exception("There are " + detectedTables + " out of " + MIN_TABLES_NUMBER + " tables with prefix " + TABLES_PREFIX + " in " + METADATA_URL + ".\n"
                        + "Petascope cannot be started: please update the database to version 9 by running `update_petascopedb.sh`");
            }

            /* TABLE_SERVICE_IDENTIFICATION */
            if (LegacyConfigManager.METADATA_HSQLDB) {
                sqlQuery
                        = " SELECT " + SERVICE_IDENTIFICATION_ID + ", "
                        + SERVICE_IDENTIFICATION_TYPE + ", "
                        + SERVICE_IDENTIFICATION_TYPE_CODESPACE + ", "
                        + SERVICE_IDENTIFICATION_TYPE_VERSIONS + ", "
                        + SERVICE_IDENTIFICATION_DESCRIPTION_ID + ", "
                        + SERVICE_IDENTIFICATION_FEES + ", "
                        + SERVICE_IDENTIFICATION_CONSTRAINTS
                        + " FROM " + TABLE_SERVICE_IDENTIFICATION
                        + " WHERE LCASE(" + SERVICE_IDENTIFICATION_TYPE + ") LIKE '%" + LegacyBaseRequest.SERVICE.toLowerCase() + "%';";
            } else {
                sqlQuery
                        = " SELECT " + SERVICE_IDENTIFICATION_ID + ", "
                        + SERVICE_IDENTIFICATION_TYPE + ", "
                        + SERVICE_IDENTIFICATION_TYPE_CODESPACE + ", "
                        + SERVICE_IDENTIFICATION_TYPE_VERSIONS + ", "
                        + SERVICE_IDENTIFICATION_DESCRIPTION_ID + ", "
                        + SERVICE_IDENTIFICATION_FEES + ", "
                        + SERVICE_IDENTIFICATION_CONSTRAINTS
                        + " FROM " + TABLE_SERVICE_IDENTIFICATION
                        + " WHERE " + SERVICE_IDENTIFICATION_TYPE + " " + CASE_INSENSITIVE_LIKE + " '%" + LegacyBaseRequest.SERVICE + "%';";
            }
            //log.debug("SQL query: " + sqlQuery);
            ResultSet r = s.executeQuery(sqlQuery);
            if (!r.next()) {
                throw new Exception("Missing service configuration in the database.");
            } else {
                do {
                    if (r.getRow() > 1) {
                        throw new Exception("Duplicate service configuration in the database.");
                    }
                    String serviceType = r.getString(SERVICE_IDENTIFICATION_TYPE);
                    String typeCodespace = r.getString(SERVICE_IDENTIFICATION_TYPE_CODESPACE);
                    List<String> typeVersions = null;
                    if (LegacyConfigManager.METADATA_SQLITE || LegacyConfigManager.METADATA_HSQLDB) {
                        typeVersions = LegacyListUtil.toList(r.getString(SERVICE_IDENTIFICATION_TYPE_VERSIONS));
                    } else {
                        typeVersions = sqlArray2StringList(r.getArray(SERVICE_IDENTIFICATION_TYPE_VERSIONS));
                    }
                    sMeta.addServiceIdentification(serviceType, typeCodespace, typeVersions);
                    // Additional optional elements
                    Integer serviceIdentId = r.getInt(SERVICE_IDENTIFICATION_ID); // to be effectively used in case multiple services will be allowed.
                    Integer descriptionId = r.getInt(SERVICE_IDENTIFICATION_DESCRIPTION_ID);
                    String fees = r.getString(SERVICE_IDENTIFICATION_FEES);
                    Array constraints = null;
                    if (!LegacyConfigManager.METADATA_SQLITE && !LegacyConfigManager.METADATA_HSQLDB) {
                        constraints = r.getArray(SERVICE_IDENTIFICATION_CONSTRAINTS);
                    }
                    // Add to ServiceMetadata
                    if (descriptionId != 0) {
                        // add method for reading a description
                        LegacyDescription serviceDescription = readDescription(descriptionId);
                        sMeta.getIdentification().setDescription(serviceDescription);
                    }
                    if (null != fees) {
                        sMeta.getIdentification().setFees(fees);
                    }
                    if (null != constraints) {
                        ResultSet constraintsRs = constraints.getResultSet();
                        while (constraintsRs.next()) {
                            sMeta.getIdentification().addAccessConstraint(constraintsRs.getString(2));
                        }
                    }
                } while (r.next());
            }


            /* TABLE_SERVICE_PROVIDER */
            sqlQuery
                    = " SELECT " + SERVICE_PROVIDER_NAME + ", "
                    + SERVICE_PROVIDER_SITE + ", "
                    + SERVICE_PROVIDER_CONTACT_NAME + ", "
                    + SERVICE_PROVIDER_CONTACT_POSITION + ", "
                    + TELEPHONE_VOICE + ", "
                    + TELEPHONE_FACSIMILE + ", "
                    + SERVICE_PROVIDER_CONTACT_DELIVERY + ", "
                    + SERVICE_PROVIDER_CONTACT_CITY + ", "
                    + SERVICE_PROVIDER_CONTACT_AREA + ", "
                    + SERVICE_PROVIDER_CONTACT_PCODE + ", "
                    + SERVICE_PROVIDER_CONTACT_COUNTRY + ", "
                    + SERVICE_PROVIDER_CONTACT_EMAIL + ", "
                    + SERVICE_PROVIDER_CONTACT_HOURS + ", "
                    + SERVICE_PROVIDER_CONTACT_INSTRUCTIONS + ", "
                    + ROLE_CODE_VALUE
                    + " FROM " + TABLE_SERVICE_PROVIDER
                    + " LEFT OUTER JOIN " + TABLE_TELEPHONE
                    + " ON " + TABLE_SERVICE_PROVIDER + "." + SERVICE_PROVIDER_CONTACT_PHONE_ID + "="
                    + TABLE_TELEPHONE + "." + TELEPHONE_ID
                    + " LEFT OUTER JOIN " + TABLE_ROLE_CODE
                    + " ON " + TABLE_SERVICE_PROVIDER + "." + SERVICE_PROVIDER_CONTACT_ROLE_ID + "="
                    + TABLE_ROLE_CODE + "." + ROLE_CODE_ID;
            //log.debug("SQL query: " + sqlQuery);
            r = s.executeQuery(sqlQuery);
            if (r.next()) {
                String providerName = r.getString(SERVICE_PROVIDER_NAME); // mandatory
                sMeta.addServiceProvider(providerName);
                // Optional fields
                String providerSite = r.getString(SERVICE_PROVIDER_SITE);
                if (null != providerSite) {
                    sMeta.getProvider().setSite(providerSite);
                }
                //
                LegacyServiceProvider sProvider = sMeta.getProvider();
                String contactName = r.getString(SERVICE_PROVIDER_CONTACT_NAME);
                if (null != contactName) {
                    sProvider.getContact().setIndividualName(contactName);
                }
                //
                String contactPosition = r.getString(SERVICE_PROVIDER_CONTACT_POSITION);
                if (null != contactPosition) {
                    sProvider.getContact().setPositionName(contactPosition);
                }
                //
                Array contactVoicePhones = r.getArray(TELEPHONE_VOICE);
                if (null != contactVoicePhones) {
                    ResultSet voicePhonesRs = contactVoicePhones.getResultSet();
                    while (voicePhonesRs.next()) {
                        sProvider.getContact().getContactInfo().addVoicePhone(voicePhonesRs.getString(2));
                    }
                }
                Array contactFacsimilePhones = r.getArray(TELEPHONE_FACSIMILE);
                if (null != contactFacsimilePhones) {
                    ResultSet facsimilePhonesRs = contactFacsimilePhones.getResultSet();
                    while (facsimilePhonesRs.next()) {
                        sProvider.getContact().getContactInfo().addFacsimilePhone(facsimilePhonesRs.getString(2));
                    }
                }
                //

                if (!LegacyConfigManager.METADATA_SQLITE && !LegacyConfigManager.METADATA_HSQLDB) {
                    Array deliveries = r.getArray(SERVICE_PROVIDER_CONTACT_DELIVERY);
                    if (null != deliveries) {
                        ResultSet deliveriesRs = deliveries.getResultSet();
                        while (deliveriesRs.next()) {
                            sProvider.getContact().getContactInfo().getAddress().addDeliveryPoint(deliveriesRs.getString(2));
                        }
                    }
                }
                //
                String contactCity = r.getString(SERVICE_PROVIDER_CONTACT_CITY);
                if (null != contactCity) {
                    sProvider.getContact().getContactInfo().getAddress().setCity(contactCity);
                }
                //
                String contactArea = r.getString(SERVICE_PROVIDER_CONTACT_AREA);
                if (null != contactArea) {
                    sProvider.getContact().getContactInfo().getAddress().setAdministrativeArea(contactArea);
                }
                //
                String contactPostalCode = r.getString(SERVICE_PROVIDER_CONTACT_PCODE);
                if (null != contactPostalCode) {
                    sProvider.getContact().getContactInfo().getAddress().setPostalCode(contactPostalCode);
                }
                //
                String contactCountry = r.getString(SERVICE_PROVIDER_CONTACT_COUNTRY);
                if (null != contactCountry) {
                    sProvider.getContact().getContactInfo().getAddress().setCountry(contactCountry);
                }
                //
                if (!LegacyConfigManager.METADATA_SQLITE && !LegacyConfigManager.METADATA_HSQLDB) {
                    Array emails = r.getArray(SERVICE_PROVIDER_CONTACT_EMAIL);
                    if (null != emails) {
                        ResultSet emailsRs = emails.getResultSet();
                        while (emailsRs.next()) {
                            sProvider.getContact().getContactInfo().getAddress().addEmailAddress(emailsRs.getString(2));
                        }
                    }
                }
                //
                String contactHours = r.getString(SERVICE_PROVIDER_CONTACT_HOURS);
                if (null != contactHours) {
                    sProvider.getContact().getContactInfo().setHoursOfService(contactHours);
                }
                //
                String contactInstructions = r.getString(SERVICE_PROVIDER_CONTACT_INSTRUCTIONS);
                if (null != contactInstructions) {
                    sProvider.getContact().getContactInfo().setInstructions(contactInstructions);
                }
                //
                String contactRole = r.getString(ROLE_CODE_VALUE);
                if (null != contactRole) {
                    sProvider.getContact().setRole(contactRole);
                }
            }

            /* TABLE_EXTRAMETADATA_TYPE */
            extraMetadataTypes = new HashMap<Integer, String>();
            revExtraMetadataTypes = new HashMap<String, Integer>();
            sqlQuery
                    = " SELECT " + EXTRAMETADATA_TYPE_ID + ", "
                    + EXTRAMETADATA_TYPE_TYPE
                    + " FROM " + TABLE_EXTRAMETADATA_TYPE;
            //log.debug("SQL query: " + sqlQuery);
            r = s.executeQuery(sqlQuery);
            while (r.next()) {
                extraMetadataTypes.put(r.getInt(EXTRAMETADATA_TYPE_ID), r.getString(EXTRAMETADATA_TYPE_TYPE));
                revExtraMetadataTypes.put(r.getString(EXTRAMETADATA_TYPE_TYPE), r.getInt(EXTRAMETADATA_TYPE_ID));
            }

            /* TABLE_GML_SUBTYPE */
            gmlSubTypes = new HashMap<Integer, String>();
            revGmlSubTypes = new HashMap<String, Integer>();
            gmlChildParent = new HashMap<String, String>();
            sqlQuery
                    = " SELECT a." + GML_SUBTYPE_ID + ", "
                    + " a." + GML_SUBTYPE_SUBTYPE + ", "
                    + " b." + GML_SUBTYPE_SUBTYPE + " AS " + GML_SUBTYPE_PARENT
                    + " FROM " + TABLE_GML_SUBTYPE + " AS a "
                    + " LEFT OUTER JOIN " + TABLE_GML_SUBTYPE + " AS b "
                    + " ON a." + GML_SUBTYPE_PARENT + " = b." + GML_SUBTYPE_ID;
            //log.debug("SQL query: " + sqlQuery);
            r = s.executeQuery(sqlQuery);
            while (r.next()) {
                gmlSubTypes.put(r.getInt(GML_SUBTYPE_ID), r.getString(GML_SUBTYPE_SUBTYPE));
                revGmlSubTypes.put(r.getString(GML_SUBTYPE_SUBTYPE), r.getInt(GML_SUBTYPE_ID));
                gmlChildParent.put(r.getString(GML_SUBTYPE_SUBTYPE),
                        null == r.getString(GML_SUBTYPE_PARENT) ? "" : r.getString(GML_SUBTYPE_PARENT));
            }

            /* FORMAT <-> GDAL <-> MIME */
            mimeTypes = new HashMap<Integer, String>();
            revMimeTypes = new HashMap<String, Integer>();
            gdalFormatsIds = new HashMap<String, String>();
            revGdalFormatsIds = new HashMap<String, String>();
            supportedFormats = new HashMap<String, String>();
            revSupportedFormats = new HashMap<String, String>();
            // Aliases (more robust against fields of same name over different tables)
            String formatNameAlias = "f" + FORMAT_NAME;
            String mimeIdAlias = "m" + MIME_TYPE_ID;
            String mimeTypeAlias = "m" + MIME_TYPE_MIME;
            String gdalIdAlias = "g" + GDAL_FORMAT_GDAL_ID;
            sqlQuery
                    = " SELECT " + TABLE_FORMAT + "." + FORMAT_NAME + " AS " + formatNameAlias + ","
                    + TABLE_MIME_TYPE + "." + MIME_TYPE_ID + " AS " + mimeIdAlias + ","
                    + TABLE_MIME_TYPE + "." + MIME_TYPE_MIME + " AS " + mimeTypeAlias + ","
                    + TABLE_GDAL_FORMAT + "." + GDAL_FORMAT_GDAL_ID + " AS " + gdalIdAlias
                    + " FROM " + TABLE_MIME_TYPE
                    + " LEFT OUTER JOIN " + TABLE_FORMAT + " ON "
                    + TABLE_MIME_TYPE + "." + MIME_TYPE_ID + "="
                    + TABLE_FORMAT + "." + FORMAT_MIME_ID
                    + " LEFT OUTER JOIN " + TABLE_GDAL_FORMAT + " ON "
                    + TABLE_GDAL_FORMAT + "." + GDAL_FORMAT_ID + "="
                    + TABLE_FORMAT + "." + FORMAT_GDAL_ID;
            //log.debug("SQL query: " + sqlQuery);
            r = s.executeQuery(sqlQuery);
            while (r.next()) {
                mimeTypes.put(r.getInt(mimeIdAlias), r.getString(mimeTypeAlias));
                revMimeTypes.put(r.getString(mimeTypeAlias), r.getInt(mimeIdAlias));
                supportedFormats.put(r.getString(formatNameAlias), r.getString(mimeTypeAlias));
                revSupportedFormats.put(r.getString(mimeTypeAlias), r.getString(formatNameAlias));
                gdalFormatsIds.put(r.getString(gdalIdAlias), r.getString(formatNameAlias));
                revGdalFormatsIds.put(r.getString(formatNameAlias), r.getString(gdalIdAlias));
            }

            // Range data types (WCPS rangeType())
            rangeDataTypes = new HashMap<Integer, String>();
            revRangeDataTypes = new HashMap<String, Integer>();
            sqlQuery
                    = "SELECT " + RANGE_DATATYPE_ID + ","
                    + RANGE_DATATYPE_NAME
                    + " FROM " + TABLE_RANGE_DATATYPE;
            //log.debug("SQL query: " + sqlQuery);
            r = s.executeQuery(sqlQuery);
            while (r.next()) {
                rangeDataTypes.put(r.getInt(RANGE_DATATYPE_ID), r.getString(RANGE_DATATYPE_NAME));
                revRangeDataTypes.put(r.getString(RANGE_DATATYPE_NAME), r.getInt(RANGE_DATATYPE_ID));
            }

            // End of session
            s.close();
            s = null;

            if (checkAtInit) {
                initializing = true;
                boolean coveragesOk = false;

                while (!coveragesOk) {
                    Iterator<String> coverages = coverages().iterator();
                    while (coverages.hasNext()) {
                        read(coverages.next());
                    }
                    coveragesOk = true;
                }
                initializing = false;
            }

        } catch (SQLException sqle) {
            throw new Exception("Metadata database error while reading static database information.", sqle);
        } finally {
            closeStatement(s);
        }
    }

    /**
     * Returns a list of all available coverages in petascopedb
     *
     * @return Unordered list of coverage names
     * @throws PetascopeException
     */
    @Override
    public List<String> coverages() throws Exception {
        Statement s = null;
        List<String> coverages;

        try {
            ensureConnection();
            s = conn.createStatement();

            String sqlQuery
                    = " SELECT " + COVERAGE_NAME
                    + " FROM " + TABLE_COVERAGE
                    + " ORDER BY " + COVERAGE_NAME;
            //log.debug("SQL query: " + sqlQuery);
            ResultSet r = s.executeQuery(sqlQuery);
            coverages = new ArrayList<String>(r.getFetchSize());
            while (r.next()) {
                coverages.add(r.getString(COVERAGE_NAME));
            }
            s.close();
            s = null;

            return coverages;
        } catch (SQLException sqle) {
            throw new Exception("Metadata database error while getting a list of the coverages.", sqle);
        } finally {
            closeStatement(s);
        }
    }

    /**
     * Given a coverage id; returns the corresponding type
     *
     * @param coverageId
     * @return The type of the coverage
     * @throws PetascopeException
     */
    public String coverageType(Integer coverageId) throws Exception {
        Statement s = null;
        try {
            ensureConnection();
            s = conn.createStatement();
            String sqlQuery
                    = " SELECT " + COVERAGE_GML_TYPE_ID
                    + " FROM " + TABLE_COVERAGE
                    + " WHERE " + COVERAGE_ID + "=" + coverageId;
            //log.debug("SQL query: " + sqlQuery);
            ResultSet r = s.executeQuery(sqlQuery);
            if (r.next()) {
                return gmlSubTypes.get(r.getInt(COVERAGE_GML_TYPE_ID));
            }
            throw new Exception("Error getting coverage type.");
        } catch (SQLException sqle) {
            throw new Exception("Error retrieving type for coverage " + coverageId, sqle);
        } finally {
            closeStatement(s);
        }
    }

    /**
     * Given a coverage name; returns the corresponding id
     *
     * @param coverageName
     * @return The ID (TABLE_COVERAGE) of this coverage
     * @throws PetascopeException
     */
    public Integer coverageID(String coverageName) throws Exception {
        Integer ret = null;
        Statement s = null;
        try {
            ensureConnection();
            s = conn.createStatement();
            String sqlQuery
                    = " SELECT " + COVERAGE_ID
                    + " FROM " + TABLE_COVERAGE
                    + " WHERE " + COVERAGE_NAME + "='" + coverageName + "'";
            //log.debug("SQL query: " + sqlQuery);
            ResultSet r = s.executeQuery(sqlQuery);

            if (r.next()) {
                ret = r.getInt(COVERAGE_ID);
            }
            s.close();
            s = null;
        } catch (SQLException sqle) {
            throw new Exception("Error retrieving ID for coverage " + coverageName, sqle);
        } finally {
            closeStatement(s);
        }
        if (ret == null) {
            throw new Exception("Error getting coverageID.");
        }
        return ret;
    }

    /**
     * Given a format name; returns the corresponding MIME type
     *
     * @param format
     * @return The associated MIME type
     */
    @Override
    public String formatToMimetype(String format) {
        return supportedFormats.get(format);
    }

    /**
     * select index and uri from r where ps_domain_set contains ps_crs.id
     *
     * @param r
     * @param coverageName
     * @return TreeMap with index as key and list of uris as values
     * @throws PetascopeException
     */
    private TreeMap idx(ResultSet r, String coverageName) throws Exception {
        TreeMap<Integer, List<String>> ret = new TreeMap<Integer, List<String>>();
        try {

            ResultSet rLocal = r;
            if (!rLocal.next()) {
                throw new Exception("Coverage '" + coverageName + "' is missing the domain-set information.");
            } else {
                do {
                    Array crsIds = rLocal.getArray(DOMAINSET_NATIVE_CRS_IDS);
                    Integer[] ids = (Integer[]) crsIds.getArray();

                    List<Integer> idSeries = new ArrayList<Integer>();
                    for (int i = 0; i < ids.length; i++) {
                        idSeries.add(ids[i]);
                    }
                    //check if id is in the series
                    Integer index = idSeries.indexOf(rLocal.getInt(CRS_ID));
                    if (index != -1) {
                        //index+1 because we need one based index

                        if (ret.containsKey(index + 1)) {
                            ret.get(index + 1).add(rLocal.getString(CRS_URI));
                        } else {
                            List<String> uriList = new ArrayList<String>();
                            uriList.add(rLocal.getString(CRS_URI));
                            ret.put(index + 1, uriList);
                        }
                    }

                } while (rLocal.next());
            }

        } catch (SQLException sqle) {
            //log.error("Failed retrieving CRS uris", sqle);
            throw new Exception("Metadata database error", sqle);
        }

        return ret;
    }

    /**
     * Given a coverage name; scans its metadata information thoroughly (range,
     * domain, extra) and stores it in a dedicated class instance.
     *
     * @param coverageName
     * @return The CoverageMetadata object for this coverage
     * @throws PetascopeException
     * @throws SecoreException
     */
    @Override
    public LegacyCoverageMetadata read(String coverageName) throws Exception {

        // output object
        LegacyCoverageMetadata covMeta;

        //log.debug("Reading metadata for coverage '{}'", coverageName);

        if (coverageName == null || coverageName.equals("")) {
            throw new Exception("Cannot retrieve coverage with null or empty name");
        }

        // If coverage has been read already, no need to do it again
        if (cache.containsKey(coverageName)) {
            //log.trace("Returning cached coverage metadata.");
            return cache.get(coverageName);
        }

        // Init
        Statement s = null;
        String sqlQuery; // buffer for SQL queries

        try {
            ensureConnection();
            s = conn.createStatement();

            /* TABLE_COVERAGE */
            sqlQuery
                    = " SELECT " + COVERAGE_ID + ", "
                    + COVERAGE_NAME + ", "
                    + COVERAGE_GML_TYPE_ID + ", "
                    + COVERAGE_NATIVE_FORMAT_ID + ", "
                    + COVERAGE_DESCRIPTION_ID
                    + " FROM " + TABLE_COVERAGE
                    + " WHERE " + COVERAGE_NAME + "='" + coverageName + "'";
            //log.debug("SQL query: " + sqlQuery);
            ResultSet r = s.executeQuery(sqlQuery);
            if (!r.next()) {
                throw new Exception("Coverage '" + coverageName + "' is not served by this server");
            }

            // Store fetched data
            int coverageId = r.getInt(COVERAGE_ID);
            String coverageType = gmlSubTypes.get(r.getInt(COVERAGE_GML_TYPE_ID));
            String coverageNativeFormat = mimeTypes.get(r.getInt(COVERAGE_NATIVE_FORMAT_ID));
            int descriptionId = r.getInt(COVERAGE_DESCRIPTION_ID);

            /* EXTRA METADATA */
            // Each coverage can have 1+ additional descriptive metadata (1+ OWS:Metadata, 1+ GMLCOV:Metadata, etc)
            sqlQuery
                    = " SELECT " + EXTRAMETADATA_METADATA_TYPE_ID + ", "
                    + EXTRAMETADATA_VALUE
                    + " FROM " + TABLE_EXTRAMETADATA
                    + " WHERE " + EXTRAMETADATA_COVERAGE_ID + "=" + coverageId;
            //log.debug("SQL query: " + sqlQuery);
            r = s.executeQuery(sqlQuery);
            // Store fetched data
            Set<LegacyPair<String, String>> extraMetadata = new HashSet<LegacyPair<String, String>>();
            while (r.next()) {
                LegacyPair<String, String> typeValue = LegacyPair.of(
                        extraMetadataTypes.get(r.getInt(EXTRAMETADATA_METADATA_TYPE_ID)),
                        r.getString(EXTRAMETADATA_VALUE)
                );
                if (typeValue.fst.equals(EXTRAMETADATA_TYPE_OWS) && !METADATA_IN_COVSUMMARY) {
                    //log.info("OWS Metadata elements have been disabled and will not be shown in the capabilities document.");
                    //log.info("To enable it, change the " + KEY_METADATA_IN_COVSUMMARY + " parameter in " + SETTINGS_FILE + ".");
                } else {
                    // Ok to add this extra metadata:
                    extraMetadata.add(typeValue);
                }
            }

            // TABLE_DOMAINSET : read the CRS, as array of single-CRS URIs
            // IMPORTANT: keep the same order of foreign keys in the array native_crs_ids.
            LegacyCellDomainElement cde;
            List<LegacyCellDomainElement> cellDomainElements = new ArrayList<LegacyCellDomainElement>(r.getFetchSize());
            List<LegacyPair<CrsDefinition.Axis, String>> crsAxes = new ArrayList<LegacyPair<CrsDefinition.Axis, String>>();

            if (!coverageType.equals(LegacyXMLSymbols.LABEL_GRID_COVERAGE)) {

                sqlQuery
                        = " SELECT " + TABLE_DOMAINSET + "." + DOMAINSET_NATIVE_CRS_IDS + " , "
                        + TABLE_CRS + "." + CRS_ID + " ," + TABLE_CRS + "." + CRS_URI
                        + " FROM " + TABLE_DOMAINSET + "," + TABLE_CRS
                        + " WHERE " + TABLE_DOMAINSET + "." + DOMAINSET_COVERAGE_ID + "=" + coverageId;

                //log.debug("SQL query: " + sqlQuery);
                r = s.executeQuery(sqlQuery);

                TreeMap<Integer, List<String>> idUriMap = idx(r, coverageName);

                if (0 == idUriMap.size()) {
                    throw new Exception("Coverage '" + coverageName + "' is missing the domain-set information.");
                } else {
                    for (Map.Entry<Integer, List<String>> entry : idUriMap.entrySet()) {
                        List<String> uriList = entry.getValue();
                        for (String each : uriList) {
                            // Store fetched data
                            // Replace possible %SECORE_URL% prefixes with resolvable configured SECORE URLs:
                            String uri = each.replace(SECORE_URL_KEYWORD, SECORE_URLS.get(0));
                            if (null == uri) {
                                //log.error("No native CRS found for this coverage.");
                                throw new Exception("No native CRS found for this coverage.");
                            }
                            // If not cached, parse the SECORE-resolved definition ot this CRS
                            CrsDefinition crsDef = CrsUtil.getCrsDefinition(uri);
                            for (CrsDefinition.Axis axis : crsDef.getAxes()) {
                                crsAxes.add(LegacyPair.of(axis, uri));
                            }
                        }
                    }

                }
                //log.trace("Coverage {} with CRS decoded has {} axes.", coverageName, crsAxes.size());
                // Check CRS
                if (crsAxes.isEmpty()) {
                    throw new Exception("Coverage '" + coverageName + "' has no external (native) CRS.");
                }
            } // else: DomainSet for simple grid is handled afterwards.

            /* RANGE-TYPE : coverage feature-space description (SWE-based) */
            // TABLE_RANGETYPE_COMPONENT
            // Ordered mapping of band name and its UoM
            List<LegacyRangeElement> rangeElements = new ArrayList<LegacyRangeElement>();
            List<LegacyPair<LegacyRangeElement, LegacyQuantity>> rangeElementsQuantities = new ArrayList<LegacyPair<LegacyRangeElement, LegacyQuantity>>();

            sqlQuery
                    = " SELECT " + RANGETYPE_COMPONENT_NAME + ", "
                    + RANGETYPE_COMPONENT_TYPE_ID + ", "
                    + RANGETYPE_COMPONENT_FIELD_TABLE + ", "
                    + RANGETYPE_COMPONENT_FIELD_ID
                    + " FROM " + TABLE_RANGETYPE_COMPONENT
                    + " WHERE " + RANGETYPE_COMPONENT_COVERAGE_ID + "=" + coverageId
                    + " ORDER BY " + RANGETYPE_COMPONENT_ORDER + " ASC ";
            //log.debug("SQL query: " + sqlQuery);
            r = s.executeQuery(sqlQuery);
            if (!r.next()) {
                throw new Exception("Coverage '" + coverageName + "' is missing the range-type metadata.");
            }
            do {
                // Check if it is a SWE:quantity (currently the only supported SWE field)
                if (!r.getString(RANGETYPE_COMPONENT_FIELD_TABLE).equals(TABLE_QUANTITY)) {
                    throw new Exception("Band " + r.getString(RANGETYPE_COMPONENT_NAME) + " of coverage '"
                            + coverageName + "' is not a continuous quantity.");
                }

                LegacyQuantity quantity = readSWEQuantity(r.getInt(RANGETYPE_COMPONENT_FIELD_ID));

                // Create the RangeElement (WCPS): {name, type, UoM}
                LegacyRangeElement rEl = new LegacyRangeElement(
                        r.getString(RANGETYPE_COMPONENT_NAME),
                        rangeDataTypes.get(r.getInt(RANGETYPE_COMPONENT_TYPE_ID)),
                        quantity.getUom());
                rangeElements.add(rEl);
                //log.debug("Added range element: " + rangeElements.get(rangeElements.size() - 1));

                // CoverageMetadata input to ensure RangeElements and Quantities have same cardinality
                rangeElementsQuantities.add(new LegacyPair(rEl, quantity));

            } while (r.next());

            /**
             * * DOMAINSET SWITCH: grids Vs multipoints **
             */
            // Now read the coverage-type-specific domain-set information
            // Here is where different coverage types differ.
            if (LegacyWcsUtil.isGrid(coverageType)) {

                // Variables for metadata object creation of gridded coverage
                LinkedHashMap<List<BigDecimal>, BigDecimal> gridAxes;   // Offset-vector -> greatest-coefficient (null if no coeffs)
                List<BigDecimal> gridOrigin;  // each BD is a coordinate's component
                LegacyPair<BigInteger, String> rasdamanColl;// collName -> OID
                String rasdamanCollectionType; // mddType:colType
                // This is a gridded coverage //

                /* RANGE-SET : coverage values (aka features, attributes, etc.) */
                // TABLE_RANGESET
                sqlQuery
                        = " SELECT " + RANGESET_STORAGE_TABLE + ", "
                        + RANGESET_STORAGE_ID
                        + " FROM " + TABLE_RANGESET
                        + " WHERE " + RANGESET_COVERAGE_ID + "=" + coverageId;
                //log.debug("SQL query: " + sqlQuery);
                r = s.executeQuery(sqlQuery);
                // Check that range-set is not missing
                if (!r.next()) {
                    throw new Exception("Missing range-set for coverage '" + coverageName + "'");
                }
                // Currently only coverages stored in `rasdaman' are supported
                String storageTable = r.getString(RANGESET_STORAGE_TABLE);
                if (!storageTable.equals(TABLE_RASDAMAN_COLLECTION)) {
                    throw new Exception("Storage table '" + storageTable + "' is not valid."
                            + "Only coverages stored in `rasdaman' are currently supported.");
                } else {
                    // TABLE_RASDAMAN_COLLECTION
                    // Read the rasdaman collection:OID which stores the values of this coverage
                    int storageId = r.getInt(RANGESET_STORAGE_ID);
                    sqlQuery
                            = " SELECT " + RASDAMAN_COLLECTION_NAME + ", "
                            + RASDAMAN_COLLECTION_OID + ","
                            + RASDAMAN_COLLECTION_BASE_TYPE
                            + " FROM " + TABLE_RASDAMAN_COLLECTION
                            + " WHERE " + RASDAMAN_COLLECTION_ID + "=" + storageId;
                    //log.debug("SQL query: " + sqlQuery);
                    r = s.executeQuery(sqlQuery);
                    if (!r.next()) {
                        throw new Exception("Coverage '" + coverageName + "' is missing the range-set metadata.");
                    }
                    // Store fetched data: OID -> collName
                    rasdamanColl = LegacyPair.of(
                            r.getBigDecimal(RASDAMAN_COLLECTION_OID).toBigInteger(),
                            r.getString(RASDAMAN_COLLECTION_NAME)
                    );
                    rasdamanCollectionType = r.getString(RASDAMAN_COLLECTION_BASE_TYPE);
                    //log.trace("Coverage '" + coverageName + "' has range-set data in " + r.getString(RASDAMAN_COLLECTION_NAME) + ":" + r.getBigDecimal(RASDAMAN_COLLECTION_OID) + ".");
                }

                List<LegacyPair<String, String>> pixelBboxes = getCollectionDomain(rasdamanColl.snd, rasdamanColl.fst);
                Iterator<LegacyPair<String, String>> pixelBboxesIt = pixelBboxes.iterator();
                //log.debug("done");

                /* DOMAIN-SET : geometry, origin, axes... */
                if (!coverageType.equals(LegacyXMLSymbols.LABEL_GRID_COVERAGE)) {
                    sqlQuery
                            = " SELECT " + GRIDDED_DOMAINSET_ORIGIN
                            + " FROM " + TABLE_GRIDDED_DOMAINSET
                            + " WHERE " + GRIDDED_DOMAINSET_COVERAGE_ID + "=" + coverageId;
                    //log.debug("SQL query: " + sqlQuery);
                    r = s.executeQuery(sqlQuery);
                    if (!r.next()) {
                        throw new Exception("Gridded coverage '" + coverageName + "' is missing the origin.");
                    }
                    // Store fetched data
                    gridOrigin = new ArrayList<BigDecimal>();
                    // The result-set of a SQL Array is a set of results, each of which is an array of 2 columns {id,attribute},
                    // of indexes 1 and 2 respectively:
                    ResultSet rs = r.getArray(GRIDDED_DOMAINSET_ORIGIN).getResultSet();
                    while (rs.next()) {
                        //log.debug("Grid origin component: " + rs.getBigDecimal(2));
                        gridOrigin.add(rs.getBigDecimal(2));
                    }
                    //log.trace("Gridded coverage '{}' has origin '{}'", coverageName, gridOrigin);
                    // Check origin is not empty
                    if (gridOrigin.isEmpty()) {
                        throw new Exception("Gridded coverage '" + coverageName + "' has an empty origin.");
                    }
                    // check if origin is compatible with the native CRS
                    // (then offset-vectors are checked against origin)
                    if (gridOrigin.size() != crsAxes.size()) {
                        throw new Exception("Native CRS of coverage '" + coverageName + " is not compatible with given origin: "
                                + crsAxes.size() + " CRS dimensions against " + gridOrigin.size() + " origin components.");
                    }

                    // Read axis-specific information, independently of its nature (rectilinear, regularly-spaced, etc.)
                    // Axis id -> {offset-vector, isIrregular}
                    // NOTE: use LinkedHashMap to preserve insertion order.
                    gridAxes = new LinkedHashMap<List<BigDecimal>, BigDecimal>();
                    sqlQuery
                            = " SELECT " + GRID_AXIS_ID + ", "
                            + GRID_AXIS_RASDAMAN_ORDER
                            + " FROM " + TABLE_GRID_AXIS
                            + " WHERE " + GRID_AXIS_COVERAGE_ID + "=" + coverageId
                            + " ORDER BY " + GRID_AXIS_RASDAMAN_ORDER;
                    //log.debug("SQL query: " + sqlQuery);
                    r = s.executeQuery(sqlQuery);
                    if (!r.next()) {
                        throw new Exception("Gridded coverage '" + coverageName + "' has no axes.");
                    }
                    // Cannot put r.next() in a loop since an other SQL query is sent and "r" gets closed.
                    // Store my data a priori then run the loop
                    List<Integer> axisIds = new ArrayList<Integer>(r.getFetchSize());
                    do {
                        // Store fetched data
                        axisIds.add(r.getInt(GRID_AXIS_ID));
                    } while (r.next());

                    // Proceed with axis examination
                    for (int axisId : axisIds) {
                        // pixel bbox for the current dimension
                        LegacyPair<String, String> pixelBbox = pixelBboxesIt.next();

                        //log.debug("Found axis with id {}", axisId);
                        // Read the offset vector
                        sqlQuery
                                = " SELECT " + RECTILINEAR_AXIS_OFFSET_VECTOR
                                + " FROM " + TABLE_RECTILINEAR_AXIS
                                + " WHERE " + RECTILINEAR_AXIS_ID + "=" + axisId;
                        //log.debug("SQL query: " + sqlQuery);
                        ResultSet rAxis = s.executeQuery(sqlQuery);
                        if (rAxis.next()) {
                            // Store fetched data
                            List<BigDecimal> offsetVector = new ArrayList<BigDecimal>();
                            // The result-set of a SQL Array is a set of results, each of which is an array of 2 columns {id,attribute},
                            // of indexes 1 and 2 respectively:
                            rs = rAxis.getArray(RECTILINEAR_AXIS_OFFSET_VECTOR).getResultSet();
                            while (rs.next()) {
                                offsetVector.add(rs.getBigDecimal(2));
                            }
                            //log.trace("Axis {} has offset-vector {}", axisId, offsetVector);
                            // Check offset vector is not empty
                            if (offsetVector.isEmpty()) {
                                throw new Exception("Axis " + axisId + " of '" + coverageName + "' has an empty offset vector.");
                            }

                            // Check if offset vector is aligned with a CRS axis
                            if (LegacyVectors.nonZeroComponentsIndices(offsetVector.toArray(new BigDecimal[offsetVector.size()])).size() > 1) {
                                throw new Exception("Offset vector " + offsetVector + " of coverage '" + coverageName + " is forbidden."
                                        + "Only aligned offset vectors are currently allowed (1 non-zero component).");
                            }

                            // Check consistency origin/offset-vector
                            if (offsetVector.size() != gridOrigin.size()) {
                                throw new Exception("Incompatible dimensionality of grid origin (" + gridOrigin.size()
                                        + ") and offset vector of axis " + gridAxes.size() + " (" + offsetVector.size()
                                        + ") for coverage '" + coverageName + "'.");
                            }

                            // Check if it has coefficients -> is irregular
                            // At the same time check that the number of coefficients is consistent with the `sdom' of the collection:
                            sqlQuery
                                    = " SELECT COUNT(*), MAX(" + VECTOR_COEFFICIENTS_COEFFICIENT + ") "
                                    + "FROM " + TABLE_VECTOR_COEFFICIENTS
                                    + " WHERE " + VECTOR_COEFFICIENTS_AXIS_ID + "=" + axisId;
                            //log.debug("SQL query: " + sqlQuery);
                            rAxis = s.executeQuery(sqlQuery);
                            BigDecimal maxCoeff = null;
                            boolean isIrregular = false;
                            if (rAxis.next()) {
                                int coeffNumber = rAxis.getInt(1);
                                if (coeffNumber > 0) {
                                    // this axis has coefficients
                                    isIrregular = true;
                                    maxCoeff = rAxis.getBigDecimal(2);
                                    // check consistency with `sdom'
                                    int sdomCount = new Integer(LegacyStringUtil.getCount(pixelBbox.fst, pixelBbox.snd));
                                    if (coeffNumber != sdomCount) {
                                        throw new Exception("Coverage '" + coverageName + " has a wrong number of coefficients for axis " + axisId
                                                + " (" + coeffNumber + ") and is not consistent with its rasdaman `sdom' (" + sdomCount + ").");
                                    }
                                }
                            }
                            //log.trace("Axis at index: " + gridAxes.size() + " of coverage '" + coverageName + "' is " + (isIrregular ? "" : "not ") + "irregular.");

                            // Build up the axis map
                            gridAxes.put(offsetVector, maxCoeff);

                            /* Create CellDomainElement and DomainElement for this axis: */
                            // CellDomain
                            cde = new LegacyCellDomainElement(
                                    pixelBbox.fst,
                                    pixelBbox.snd,
                                    gridAxes.size() - 1
                            );
                            cellDomainElements.add(cde);
                            //log.debug("Added WCPS `cellDomain' element: " + cde);

                        } else {
                            throw new Exception("Axis " + axisId + " of '" + coverageName + "' has no offset vector.");
                        }
                    } // ~end read grid axes

                    // Check if the coverage can fit into the CRS (dimensionality)
                    if (gridAxes.size() > crsAxes.size()) {
                        throw new Exception(gridAxes.size() + "D coverage '" + coverageName + "' cannot fit into its "
                                + crsAxes.size() + "D native CRS.");
                    }
                    // Currently accept only N-D coverages in N-D CRS
                    if (gridAxes.size() != crsAxes.size()) {
                        throw new Exception(gridAxes.size() + "D coverage '" + coverageName + "' must have the same dimensions as its native CRS.");
                    }

                    // Check if offset-vectors are orthogonal
                    if (!LegacyVectors.areOrthogonal(new ArrayList(gridAxes.keySet()))) {
                        throw new Exception("Offset-vectors for coverage '" + coverageName + "' are not orthogonal.");
                    }

                    // Check if rasdaman domain has the same dimensions
                    if (gridAxes.keySet().size() != cellDomainElements.size()) {
                        throw new Exception(gridAxes.keySet().size() + "D coverage '" + coverageName + "' is not compatible with "
                                + cellDomainElements.size() + " range-set.");
                    }
                } else {
                    // Simple GridCoverage type: geometry is not defined:
                    // we need to set on our own following WCS uniform coverage handling policy
                    gridOrigin = new ArrayList<BigDecimal>();
                    gridAxes = new LinkedHashMap<List<BigDecimal>, BigDecimal>();
                    int dimensionNo = pixelBboxes.size();
                    // [#760] Assign IndexCrs and versor-vectors to a GridCoverage by default (needed for BBOX, which is WCS Req.1)
                    String uri = LegacyConfigManager.SECORE_URLS.get(0) + '/'
                            + CrsUtil.KEY_RESOLVER_CRS + '/'
                            + CrsUtil.OGC_AUTH + '/'
                            + CrsUtil.CRS_DEFAULT_VERSION + '/'
                            + CrsUtil.INDEX_CRS_PATTERN.replace(CrsUtil.INDEX_CRS_PATTERN_NUMBER, "" + dimensionNo);
                    //log.debug("Assigning " + uri + " CRS to " + coverageName + "by default.");
                    CrsDefinition crsDef = CrsUtil.getCrsDefinition(uri);
                    for (CrsDefinition.Axis axis : crsDef.getAxes()) {
                        crsAxes.add(LegacyPair.of(axis, uri));
                    }
                    // CellDomain elements and Grid geometry
                    for (int i = 0; i < dimensionNo; i++) {
                        LegacyPair<String, String> pixelBbox = pixelBboxesIt.next();
                        cde = new LegacyCellDomainElement(pixelBbox.fst, pixelBbox.snd, i);
                        cellDomainElements.add(cde);
                        //log.debug("cell domain for " + i + ": " + cde);
                        gridOrigin.add(new BigDecimal(cde.getLo()));
                        BigDecimal[] uv = LegacyVectors.unitVector(dimensionNo, i);
                        gridAxes.put(Arrays.asList(uv), null);
                    }
                }

                /* Build the complete metadata object */
                // NOTE: create a Bbox object with list of axis/extents or just let CoverageMetadata have a getBbox method?
                // `domain' object has the required metadata to deduce the Bbox of the coverage.
                // TODO : general constructor for *Coverages, then overloads in the CoverageMetadata.java
                covMeta = new LegacyCoverageMetadata(
                        coverageName,
                        coverageType,
                        coverageNativeFormat,
                        extraMetadata,
                        crsAxes,
                        cellDomainElements,
                        gridOrigin,
                        gridAxes,
                        rasdamanColl,
                        rangeElementsQuantities
                );
                // non-dynamic coverage:
                covMeta.setCoverageId(coverageId);
                covMeta.setRasdamanCollectionType(rasdamanCollectionType);
            } else if (LegacyWcsUtil.isMultiPoint(coverageType)) {
                // Fetch the bbox for Multi* Coverages
                ArrayList<BigDecimal> lowerLeft;
                ArrayList<BigDecimal> upperRight;
                sqlQuery
                        = " SELECT " + LOWER_LEFT + "," + UPPER_RIGHT
                        + " FROM " + TABLE_BOUNDING_BOX
                        + " WHERE " + GRIDDED_DOMAINSET_COVERAGE_ID + "=" + coverageId;
                lowerLeft = new ArrayList<BigDecimal>();
                upperRight = new ArrayList<BigDecimal>();
                //log.debug("SQL query: " + sqlQuery);
                r = s.executeQuery(sqlQuery);
                if (!r.next()) {
                    throw new Exception("Multi* coverage '" + coverageName + "' is missing the bounding box.");
                } else {
                    ResultSet rs = r.getArray(LOWER_LEFT).getResultSet();
                    while (rs.next()) {
                        lowerLeft.add(rs.getBigDecimal(2));
                    }
                    rs = r.getArray(UPPER_RIGHT).getResultSet();
                    while (rs.next()) {
                        upperRight.add(rs.getBigDecimal(2));
                    }
                }
                cellDomainElements = new ArrayList<LegacyCellDomainElement>(1);
                covMeta = new LegacyCoverageMetadata(coverageName, coverageType,
                        coverageNativeFormat, extraMetadata, crsAxes, cellDomainElements,
                        rangeElementsQuantities, lowerLeft, upperRight);
            } else {
                // TODO manage Multi*Coverage alternatives
                throw new Exception("Coverages of type '" + coverageType + "' are not supported.");
            }

            // Add possible OWS description
            if (descriptionId != 0 && DESCRIPTION_IN_COVSUMMARY) {
                LegacyDescription covDescription = readDescription(descriptionId);
                covMeta.setDescription(covDescription);
            }

            // Don't cache as it is only read for once time to migrate
            // //log.trace("Caching coverage metadata for coverage: {}", coverageName);
            // cache.put(coverageName, covMeta);

            /* Done with SQL statements */
            s.close();
            s = null;

            return covMeta;

        } catch (Exception sEx) {
            throw sEx;
        } finally {
            closeStatement(s);
        }
    }

    /**
     * Write a coverage's metadata to the database. This function can be used
     * for both inserting new coverages and updating coverage metadata.
     *
     * @param meta CoverageMetadata container for the information to be stored
     * in the metadata database
     * @param commit Boolean value, specifying if we want to commit immediately
     * or not
     * @throws PetascopeException
     */
    private void write(LegacyCoverageMetadata meta, boolean commit) throws Exception {
        String coverageName = meta.getCoverageName();
        if (existsCoverageName(coverageName)) {
            updateCoverageMetadata(meta, commit);
        } else {
            insertNewCoverageMetadata(meta, commit);
        }
    }

    /**
     * Update metadata for an existing coverage. All information may change
     * (including name), but the ID of the tuple in PS_Coverage will stay the
     * same.
     *
     * @param meta CoverageMetadata object, container of the new information.
     * @param commit True if we want to commit immediately, false to delay
     * commit indefinitely
     */
    public void updateCoverageMetadata(LegacyCoverageMetadata meta, boolean commit) throws Exception {
        throw new UnsupportedOperationException("Update is not yet supported");
    }

    /**
     * Insert metadata for a new coverage.
     *
     * @param meta CoverageMetadata object, container of information
     * @param commit Boolean value, specifying if we want to commit immediately
     * or not
     * @throws petascope.exceptions.PetascopeException
     */
    public void insertNewCoverageMetadata(LegacyCoverageMetadata meta, boolean commit) throws Exception {
        Statement s = null;
        try {
            //initialize connection
            s = conn.createStatement();
            //get the GML Type id
            int gmlTypeId = getGMLTypeId(s, meta.getCoverageType());
            //get the native format id
            int nativeFormatId = getNativeFormatId(s, meta.getNativeFormat());
            //get the description id
            //@TODO implement parser for description first

            //insert into ps_coverage table
            int coverageId = insertIntoPsCoverage(s, meta.getCoverageName(), gmlTypeId, nativeFormatId);

            //insert information about the rangeset
            String rasdamanCollectionType = meta.getRasdamanCollectionType();
            insertRangeSet(s, coverageId, meta.getRasdamanCollection(), rasdamanCollectionType);

            //insert the quantities together with all dependencies (e.g: nill values of ranges)
            insertRangeTypes(s, coverageId, meta.getRangeIterator(), meta.getSweComponentsIterator());

            //insert the domain set
            insertDomainSet(s, coverageId, meta.getCrsUris());

            //insert the gridded domain set
            insertGriddedDomainSet(s, coverageId, meta.getGridOrigin());

            //insert the axes
            insertGridAxes(s, coverageId, meta.getGridAxes());

            //insert the extra metadata
            Set<String> extraMetadata = meta.getExtraMetadata(EXTRAMETADATA_TYPE_GMLCOV);
            if (extraMetadata != null) {
                insertExtraMetadata(coverageId, meta.getExtraMetadata(EXTRAMETADATA_TYPE_GMLCOV));
            }

            //close
            s.close();
            s = null;
            if (commit) {
                commitAndClose();
            }
        } catch (SQLException e) {
            throw new Exception("Failed inserting coverage with id " + meta.getCoverageName());
        } finally {
            closeStatement(s);
        }

    }

    /**
     * Updates an axis coefficient for an irregular axis. The transaction is NOT
     * commited. It has to be manually commited after the method is called, to
     * allow other actions to be grouped.
     *
     * @param gridAxisId
     * @param coefficient
     * @param coefficientOrder
     * @return the connection object.
     * @throws SQLException
     */
    public void updateAxisCoefficient(int gridAxisId, BigDecimal coefficient, int coefficientOrder) throws SQLException {
        PreparedStatement s = null;
        Savepoint savePoint = conn.setSavepoint();
        try {
            s = conn.prepareStatement("SELECT COUNT(*) FROM " + TABLE_VECTOR_COEFFICIENTS + " WHERE " + RECTILINEAR_AXIS_ID + "= ? AND "
                    + VECTOR_COEFFICIENTS_ORDER + "=?");
            //check if a coefficient already exist
            s.setInt(1, gridAxisId);
            s.setInt(2, coefficientOrder);
            //log.info("Executing prepared statement: " + s.toString());
            ResultSet result = s.executeQuery();
            boolean coefficientAlreadyExists = false;
            while (result.next()) {
                coefficientAlreadyExists = result.getInt(1) > 0;
            }
            if (!coefficientAlreadyExists) {
                //add it
                PreparedStatement insertCoeff = conn.prepareStatement("INSERT INTO " + TABLE_VECTOR_COEFFICIENTS + " VALUES (?, ?, ?)");
                insertCoeff.setInt(1, gridAxisId);
                insertCoeff.setBigDecimal(2, coefficient);
                insertCoeff.setInt(3, coefficientOrder);
                //log.info("Executing prepared statement: " + insertCoeff.toString());
                insertCoeff.executeUpdate();
            }
        } catch (SQLException e) {
            conn.rollback(savePoint);
            throw e;
        } finally {
            closeStatement(s);
        }
    }

    private void insertExtraMetadata(int coverageId, Set<String> extraMetadataSet) throws SQLException, Exception {
        PreparedStatement s = null;
        try {
            //get the type id of gmlcov metadata type
            int typeId = 0;
            for (int key : extraMetadataTypes.keySet()) {
                if (extraMetadataTypes.get(key).equals(EXTRAMETADATA_TYPE_GMLCOV)) {
                    typeId = key;
                    break;
                }
            }
            //check if the type id has been successfully initialized
            if (typeId == 0) {
                throw new Exception();
            }
            //do the insertion, for each extra metadata entry
            for (String extraMetadata : extraMetadataSet) {
                s = conn.prepareStatement("INSERT INTO " + TABLE_EXTRAMETADATA
                        + "(" + EXTRAMETADATA_COVERAGE_ID + ", " + EXTRAMETADATA_METADATA_TYPE_ID + ", " + EXTRAMETADATA_VALUE + ") "
                        + "VALUES (?, ?, ?)");
                s.setInt(1, coverageId);
                s.setInt(2, typeId);
                s.setString(3, extraMetadata);
                //log.info("Executing prepared statement: " + s.toString());
                s.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        } finally {
            closeStatement(s);
        }
    }

    private void insertGridAxes(Statement s, int coverageId, LinkedHashMap<List<BigDecimal>, BigDecimal> gridAxes) throws SQLException {
        //for each axis insert the axis # and offset vector
        int axisNumber = 0;
        for (List<BigDecimal> offsetVector : gridAxes.keySet()) {
            String vector = "{";
            for (BigDecimal component : offsetVector) {
                vector += component.toString() + ",";
            }
            if (vector.length() > 1) {
                vector = vector.substring(0, vector.length() - 1) + "}";
            } else {
                vector = "NULL";
            }
            //insert the axis number
            String sqlQuery = "INSERT INTO " + TABLE_GRID_AXIS
                    + " (" + GRID_AXIS_COVERAGE_ID + ","
                    + GRID_AXIS_RASDAMAN_ORDER + ") VALUES"
                    + " (" + coverageId + "," + axisNumber + ")";
            //log.debug("SQL Query : " + sqlQuery);
            s.executeUpdate(sqlQuery);
            //get the newly inserted id
            String idQuery = "SELECT " + GRID_AXIS_ID
                    + " FROM " + TABLE_GRID_AXIS
                    + " WHERE " + GRID_AXIS_COVERAGE_ID + "=" + coverageId
                    + " AND " + GRID_AXIS_RASDAMAN_ORDER + "=" + axisNumber;
            //log.debug("SQL Query : " + idQuery);
            int gridId = -1;
            ResultSet r = s.executeQuery(idQuery);
            while (r.next()) {
                gridId = r.getInt(GRID_AXIS_ID);
            }
            axisNumber++;
            //insert the offset vector
            String offsetVectorQuery = "INSERT INTO " + TABLE_RECTILINEAR_AXIS
                    + " (" + RECTILINEAR_AXIS_ID + "," + RECTILINEAR_AXIS_OFFSET_VECTOR
                    + ") VALUES (" + gridId + ",'" + vector + "')";
            //log.debug("SQL Query : " + offsetVectorQuery);
            s.executeUpdate(offsetVectorQuery);
            //insert the coefficients
            //@TODO now  we are just adding one coefficient, add all
            if (gridAxes.get(offsetVector) != null) {
                updateAxisCoefficient(gridId, gridAxes.get(offsetVector), 0);
            }
        }
    }

    private void insertGriddedDomainSet(Statement s, int coverageId, List<BigDecimal> gridOrigin) throws SQLException {
        String origin = "{";
        for (BigDecimal point : gridOrigin) {
            origin += point.toString() + ",";
        }
        if (origin.length() > 1) {
            origin = origin.substring(0, origin.length() - 1) + "}";
        } else {
            origin = "NULL";
        }
        String sqlQuery = "INSERT INTO " + TABLE_GRIDDED_DOMAINSET
                + " (" + GRIDDED_DOMAINSET_COVERAGE_ID + ","
                + GRIDDED_DOMAINSET_ORIGIN + ") VALUES"
                + " (" + coverageId + ",'" + origin + "')";
        s.executeUpdate(sqlQuery);
    }

    private void insertDomainSet(Statement s, int coverageId, List<String> crses) throws SQLException {
        //check if the crs exists already
        String crsIds = "{";

        for (String rawCrs : crses) {
            String crs = CrsUtil.CrsUri.toDbRepresentation(rawCrs);
            String sqlQuery = "SELECT " + CRS_ID
                    + " FROM " + TABLE_CRS
                    + " WHERE " + CRS_URI + "='" + crs + "'";
            //log.debug("SQL Query : " + sqlQuery);
            ResultSet r = s.executeQuery(sqlQuery);
            if (r.next()) {
                crsIds += r.getInt(CRS_ID) + ",";
            } else {
                //insert the new crs
                String insertQuery = "INSERT INTO " + TABLE_CRS
                        + " (" + CRS_URI + ") VALUES"
                        + " ('" + crs + "')";
                //log.debug("SQL Query : " + insertQuery);
                s.executeUpdate(insertQuery);
                //get the id
                r = s.executeQuery(sqlQuery);
                while (r.next()) {
                    crsIds += r.getInt(CRS_ID) + ",";
                }
            }
        }
        //remove the last , and add }
        if (crsIds.length() > 1) {
            crsIds = crsIds.substring(0, crsIds.length() - 1) + "}";
        } else {
            crsIds = "NULL";
        }
        //add the entry to the table
        String insertIntoDomain = "INSERT INTO " + TABLE_DOMAINSET
                + " (" + DOMAINSET_COVERAGE_ID + ","
                + DOMAINSET_NATIVE_CRS_IDS + ") VALUES "
                + " (" + coverageId + ",'" + crsIds + "')";
        //log.debug("SQL Query : " + insertIntoDomain);
        s.executeUpdate(insertIntoDomain);
    }

    private void insertRangeTypes(Statement s, int coverageId, Iterator<LegacyRangeElement> rangeInterator, Iterator<LegacyAbstractSimpleComponent> quantityIterator) throws SQLException {
        int order = 0;
        //insert the fields
        List<Integer> fieldIds = insertQuantity(s, quantityIterator);
        while (rangeInterator.hasNext()) {
            LegacyRangeElement range = rangeInterator.next();
            //get the dataType id
            int dataTypeId = getRangeDataType(s, range.getType());
            String sqlQuery = "INSERT INTO " + TABLE_RANGETYPE_COMPONENT
                    + " (" + RANGETYPE_COMPONENT_COVERAGE_ID + ","
                    + RANGETYPE_COMPONENT_NAME + ","
                    + RANGETYPE_COMPONENT_TYPE_ID + ","
                    + RANGETYPE_COMPONENT_ORDER + ","
                    + RANGETYPE_COMPONENT_FIELD_ID + ") VALUES"
                    + " (" + coverageId + ",'" + range.getName() + "',"
                    + dataTypeId + "," + order + "," + fieldIds.get(order) + ")";
            //log.debug("SQL Query : ", sqlQuery);
            s.executeUpdate(sqlQuery);
            order++;
        }
    }

    private List<Integer> insertQuantity(Statement s, Iterator<LegacyAbstractSimpleComponent> quantityIterator) throws SQLException {
        List<Integer> ret = new ArrayList<Integer>();
        while (quantityIterator.hasNext()) {
            LegacyQuantity quantity = (LegacyQuantity) quantityIterator.next();
            //get the uom id
            int uomId = insertUom(s, quantity.getUom());
            //get the nil value ids
            String nilsID = insertNils(s, quantity.getNilValuesIterator());
            String nilsVal = "NULL";
            if (!nilsID.equals("NULL")) {
                nilsVal = "'" + nilsID + "'";
            } else {
                // And if nilsID is NULL then in select query need to change to "{}"
                nilsID = "{}";
            }
            //check if the quantity already exists
            // NOTE: there is a constraint in ps_quantity  (uom_id, label), then if label is '' or label is a random string then
            // when insert a new Quantity with same uom_id, label ('') and nil_ids, it will use this existing row instead of
            // insert new Quantity row.

            String sqlIdQuery = "SELECT " + QUANTITY_ID + "," + QUANTITY_LABEL
                    + " FROM " + TABLE_QUANTITY
                    + " WHERE " + QUANTITY_UOM_ID + "='" + uomId + "'"
                    + " AND " + QUANTITY_DESCRIPTION + "='" + quantity.getDescription() + "'"
                    + " AND " + QUANTITY_NIL_IDS + "='" + nilsID + "'";
            ResultSet r = s.executeQuery(sqlIdQuery);
            if (r.next()) {
                String label = r.getString(QUANTITY_LABEL);
                // An exisiting row is used for the new Quantity if it's label value is '' or random string
                if (label.isEmpty() || LegacyStringUtil.isRandomString(QUANTITY_LABEL, label)) {
                    ret.add(r.getInt(QUANTITY_ID));
                }
            } else {
                // There is a constraint in ps_quantity which does not allow duplicate (uom_id, label)
                // then if label is not defined, make a random string to insert.
                String quantityLabel = quantity.getLabel();
                if (quantityLabel.isEmpty()) {
                    quantityLabel = LegacyStringUtil.createRandomString(QUANTITY_LABEL);
                }
                String sqlQuery = "INSERT INTO " + TABLE_QUANTITY
                        + " (" + QUANTITY_UOM_ID + "," + QUANTITY_LABEL + ","
                        + QUANTITY_DESCRIPTION + "," + QUANTITY_DEFINITION + ","
                        + QUANTITY_NIL_IDS
                        + ") VALUES ( " + uomId + ",'" + quantityLabel + "','"
                        + quantity.getDescription() + "','" + quantity.getDefinition()
                        + "'," + nilsVal + ")";
                //log.debug("SQL Query : " + sqlQuery);
                s.executeUpdate(sqlQuery, Statement.RETURN_GENERATED_KEYS);

                // Get the inserted ID
                int idKey = 0;
                ResultSet rs = s.getGeneratedKeys();
                if (rs.next()) {
                    idKey = rs.getInt(1);
                }
                ret.add(idKey);
            }
        }
        return ret;
    }

    private String insertNils(Statement s, Iterator<LegacyNilValue> nilIterator) throws SQLException {
        String ret = "{";
        while (nilIterator.hasNext()) {
            LegacyNilValue nil = nilIterator.next();
            //check if the value exists already
            String sqlIdQuery = "SELECT " + NIL_VALUE_ID
                    + " FROM " + TABLE_NIL_VALUE
                    + " WHERE " + NIL_VALUE_VALUE + "='" + nil.getValue()
                    + "' AND " + NIL_VALUE_REASON + "='" + nil.getReason() + "'";
            //log.debug("SQL Query : " + sqlIdQuery);
            ResultSet r = s.executeQuery(sqlIdQuery);
            if (r.next()) {
                ret += r.getInt(NIL_VALUE_ID) + ",";
            } else {
                //insert it in the table
                String sqlQuery = "INSERT INTO " + TABLE_NIL_VALUE
                        + " (" + NIL_VALUE_REASON + ","
                        + NIL_VALUE_VALUE + ") VALUES"
                        + " ('" + nil.getReason() + "','" + nil.getValue() + "')";
                //log.debug("SQL Query : " + sqlQuery);
                s.executeUpdate(sqlQuery);
                //get the id of the newly inserted value
                //log.debug("SQL Query : " + sqlIdQuery);
                r = s.executeQuery(sqlIdQuery);
                while (r.next()) {
                    ret += r.getInt(NIL_VALUE_ID) + ",";
                }
            }
        }
        //remove the last , form the result and add a }
        if (ret.length() > 1) {
            ret = ret.substring(0, ret.length() - 1) + "}";
        } else {
            ret = "NULL";
        }

        return ret;
    }

    /**
     * Inserts a new uom, if it doesn't exist already.
     *
     * @param s
     * @param uomCode
     * @return the uom id
     * @throws SQLException
     */
    private int insertUom(Statement s, String uomCode) throws SQLException {
        int ret = -1;
        //if no uom is given, return the first id
        if (uomCode.isEmpty()) {
            return 1;
        }
        //check if the uom exists already, if not insert it
        String sqlQuery = "SELECT " + UOM_ID
                + " FROM " + TABLE_UOM
                + " WHERE " + UOM_CODE + "='" + uomCode + "'";
        //log.debug("SQL Query : " + sqlQuery);
        ResultSet r = s.executeQuery(sqlQuery);
        if (r.next()) {
            //code already exists
            ret = r.getInt(UOM_ID);
        } else {
            //insert it and re-do the query
            String insertQuery = "INSERT INTO " + TABLE_UOM
                    + " (" + UOM_CODE + ") VALUES"
                    + " ('" + uomCode + "')";
            //log.debug("SQL Query : " + insertQuery);
            s.executeUpdate(insertQuery);
            r = s.executeQuery(sqlQuery);
            while (r.next()) {
                ret = r.getInt(UOM_ID);
            }
        }
        return ret;
    }

    private int getRangeDataType(Statement s, String typeName) throws SQLException {
        //get the id
        int ret = -1;
        String sqlQuery = "SELECT " + RANGE_DATATYPE_ID
                + " FROM " + TABLE_RANGE_DATATYPE
                + " WHERE " + RANGE_DATATYPE_NAME + "='" + typeName + "'";
        //log.debug("SQL Query : " + sqlQuery);
        ResultSet r = s.executeQuery(sqlQuery);
        while (r.next()) {
            ret = r.getInt(RANGE_DATATYPE_ID);
        }
        return ret;
    }

    /**
     * Inserts the range set information of the coverage
     *
     * @param s
     * @param coverageId
     * @param rasdamanCollection
     * @param rasdamanCollectionType
     * @throws SQLException
     */
    private void insertRangeSet(Statement s, int coverageId, LegacyPair<BigInteger, String> rasdamanCollection, String rasdamanCollectionType) throws SQLException {
        //create the rasdaman collection entry in petascopedb
        String sqlQuery = "INSERT INTO " + TABLE_RASDAMAN_COLLECTION
                + "(" + RASDAMAN_COLLECTION_NAME + "," + RASDAMAN_COLLECTION_OID
                + "," + RASDAMAN_COLLECTION_BASE_TYPE + ") VALUES ("
                + "'" + rasdamanCollection.snd + "'," + rasdamanCollection.fst
                + ",'" + rasdamanCollectionType + "')";
        //log.debug("SQL Query : " + sqlQuery);
        s.executeUpdate(sqlQuery);
        //get the id
        int id = -1;
        String sqlIdQuery = "SELECT " + RASDAMAN_COLLECTION_ID
                + " FROM " + TABLE_RASDAMAN_COLLECTION
                + " WHERE " + RASDAMAN_COLLECTION_OID + "=" + rasdamanCollection.fst;
        ResultSet result = s.executeQuery(sqlIdQuery);
        while (result.next()) {
            id = result.getInt(RASDAMAN_COLLECTION_ID);
        }
        //create the entry in the range_set table
        String sqlRangeSetQuery = "INSERT INTO " + TABLE_RANGESET
                + "(" + RANGESET_COVERAGE_ID + ","
                + RANGESET_STORAGE_ID + ") VALUES "
                + "(" + coverageId + "," + id + ")";
        s.executeUpdate(sqlRangeSetQuery);
    }

    /**
     * Creates an entry in the coverages table.
     *
     * @param s
     * @param coverageName
     * @param gmlTypeId
     * @param nativeFormatId
     * @return the id of the newly inserted coverage
     * @throws WCSException
     * @throws WCSTDuplicatedCoverageName
     * @throws SQLException
     * @throws PetascopeException
     */
    private int insertIntoPsCoverage(Statement s, String coverageName, int gmlTypeId, int nativeFormatId)
            throws Exception {
        //check if another coverage with the given name doesn't already exist
        if (this.existsCoverageName(coverageName)) {
            throw new Exception();
        }
        String sqlQuery = "INSERT INTO " + TABLE_COVERAGE
                + " (" + COVERAGE_NAME + "," + COVERAGE_GML_TYPE_ID + "," + COVERAGE_NATIVE_FORMAT_ID + ") VALUES "
                + " ('" + coverageName + "'," + gmlTypeId + "," + nativeFormatId + ")";
        //log.debug("SQL query : " + sqlQuery);
        s.executeUpdate(sqlQuery);
        //return the coverage id
        return this.coverageID(coverageName);
    }

    /**
     * Returns the id of a given native format (mime type).
     *
     * @param s
     * @param nativeFormat
     * @return
     * @throws WCSException
     * @throws SQLException
     */
    private int getNativeFormatId(Statement s, String nativeFormat) throws Exception {
        int ret = -1;
        String sqlQuery = "SELECT " + MIME_TYPE_ID
                + " FROM " + TABLE_MIME_TYPE
                + " WHERE " + MIME_TYPE_MIME + "='" + nativeFormat + "'";
        //log.debug("SQL query : " + sqlQuery);
        ResultSet r = s.executeQuery(sqlQuery);
        while (r.next()) {
            ret = r.getInt(MIME_TYPE_ID);
        }
        if (ret == -1) {
            throw new Exception();
        }
        return ret;
    }

    /**
     * Returns the id of a given gmlType.
     *
     * @param s
     * @param gmlType
     * @return
     * @throws SQLException
     * @throws WCSException
     */
    private int getGMLTypeId(Statement s, String gmlType) throws SQLException, Exception {
        int ret = -1;
        String sqlQuery = "SELECT " + GML_SUBTYPE_ID
                + " FROM " + TABLE_GML_SUBTYPE
                + " WHERE " + GML_SUBTYPE_SUBTYPE + "='" + gmlType + "'";
        //log.debug("SQL query : " + sqlQuery);
        ResultSet r = s.executeQuery(sqlQuery);
        while (r.next()) {
            ret = r.getInt(GML_SUBTYPE_ID);
        }
        if (ret == -1) {
            throw new Exception();
        }
        return ret;
    }

    /**
     * Delete a coverage from the database. The transaction is not committed.
     *
     * @param coverageName
     * @throws PetascopeException
     * @throws java.sql.SQLException
     */
    public void delete(String coverageName) throws Exception, SQLException {
        ensureConnection();
        if (existsCoverageName(coverageName) == false) {
            throw new Exception("Cannot delete inexistent coverage: " + coverageName);
        }

        /* Delete main coverage entry from "PS_Coverage". Auxiliary metadata are
         * automatically deleted by the DB (via CASCADING) on
         * deletion of the main entry in ps_coverage */
        Statement s = null;
        try {
            s = conn.createStatement();
            String sqlQuery
                    = "DELETE FROM " + TABLE_COVERAGE
                    + " WHERE " + COVERAGE_NAME + "='" + coverageName + "'";
            //log.debug("SQL query : " + sqlQuery);
            int count = s.executeUpdate(sqlQuery);
            //log.trace("Affected rows: " + count);
            s.close();
            s = null;
        } catch (SQLException e) {
            throw new Exception("Failed deleting coverage with id " + coverageName);
        } finally {
            closeStatement(s);
        }
    }

    /**
     * Check if there is metadata available for a given coverage name
     *
     * @param name coverage name
     * @return true is coverage already exists
     */
    public boolean existsCoverageName(String name) {
        boolean result = false;
        Statement s = null;
        try {
            ensureConnection();
            s = conn.createStatement();
            String sqlQuery
                    = "SELECT * FROM " + TABLE_COVERAGE
                    + " WHERE " + COVERAGE_NAME + "='" + name + "'";
            //log.debug("Check if metadata is available for given coverage name: {}, SQL query: {}", name, sqlQuery);
            ResultSet r = s.executeQuery(sqlQuery);
            if (r.next()) {
                result = true;
            } else {
                result = false;
            }
            s.close();
            s = null;
        } catch (SQLException e) {
            //log.error("Database error while checking if coverage " + name + " exists.", e);
        } finally {
            closeStatement(s);
        }
        return result;
    }

    /* Returns the available formatToMimetype formats, as stored in the metadata database */
    public String[] getMimetypesList() {
        return supportedFormats.values().toArray(new String[1]);
    }

    /* Translate a mime-type to a format name, if known to rasdaman. */
    @Override
    public String mimetypeToFormat(String mime) {
        return revSupportedFormats.get(mime);
    }

    @Override
    public String formatToGdalid(String format) {
        return revGdalFormatsIds.get(format);
    }

    @Override
    public String gdalidToFormat(String gdalid) {
        return gdalFormatsIds.get(gdalid);
    }

    /**
     * Clear the internal cache of coverage metadata objects.
     */
    public void clearCache() {
        cache.clear();
    }

    /**
     * @return the range data types
     */
    public Collection<String> getDataTypes() {
        return rangeDataTypes.values();
    }

    /**
     * Returns the GMLCOV parent type of a coverage. The parent->child mapping
     * is taken from petascopedb::PS_GML_SUBTYPE table.
     *
     * @param type The GMLCOV type of the child.
     * @return The GMLCOV type of the correspondent parent; an empty string
     * otherwise in case of unknown or root (eg Coverage) type.
     */
    public String getParentCoverageType(String type) {
        String parentType = "";

        if (gmlChildParent.containsKey(type)) {
            for (Map.Entry<String, String> childParent : gmlChildParent.entrySet()) {
                if (childParent.getKey().equals(type)) {
                    parentType = childParent.getValue();
                }
            }
        }

        return parentType;
    }

    /**
     * Metadata of the WCS service fetched from petascopedb tables.
     *
     * @return The object with all the user-defined service metadata
     */
    public LegacyServiceMetadata getServiceMetadata() {
        return sMeta;
    }

    /**
     * Fetches MultiPoint Coverage Domain and Range Data from MultiPoint
     * Coverage created in PetascopeDB
     *
     * @author Alireza
     * @param schemaName
     * @param coverageID
     * @param coverageName
     * @param cellDomainList
     * @throws PetascopeException
     */
    public String[] multipointDomainRangeData(String schemaName, int coverageID, String coverageName,
            List<LegacyCellDomainElement> cellDomainList) throws Exception {
        String[] members = {"", ""};
        Statement s = null;
        //int pointCount = 0;

        try {
            ensureConnection();
            s = conn.createStatement();

            String xmin = cellDomainList.get(0).getLo().toString();
            String xmax = cellDomainList.get(0).getHi().toString();
            String ymin = cellDomainList.get(1).getLo().toString();
            String ymax = cellDomainList.get(1).getHi().toString();
            String zmin = cellDomainList.get(2).getLo().toString();
            String zmax = cellDomainList.get(2).getHi().toString();

            String sqlQuery = "";
            ResultSet res = null;
            if (xmin.equals("1") && xmax.equals("1")) {
                sqlQuery
                        = "SELECT min(St_X(" + MULTIPOINT_COORDINATE + ")),"
                        + " max(St_X(" + MULTIPOINT_COORDINATE + ")) "
                        + " FROM " + TABLE_MULTIPOINT;
                res = executePostGISQuery(sqlQuery);
                while (res.next()) {
                    xmin = res.getString(1);
                    xmax = res.getString(2);
                }
            }
            if (ymin.equals("1") && ymax.equals("1")) {
                sqlQuery
                        = "SELECT min(St_Y(" + MULTIPOINT_COORDINATE + ")),"
                        + " max(St_Y(" + MULTIPOINT_COORDINATE + ")) "
                        + " FROM " + TABLE_MULTIPOINT;
                res = executePostGISQuery(sqlQuery);
                while (res.next()) {
                    ymin = res.getString(1);
                    ymax = res.getString(2);
                }
            }
            if (zmin.equals("1") && zmax.equals("1")) {
                sqlQuery
                        = "SELECT min(St_Z(" + MULTIPOINT_COORDINATE + ")),"
                        + " max(St_Z(" + MULTIPOINT_COORDINATE + ")) "
                        + " FROM " + TABLE_MULTIPOINT;
                res = executePostGISQuery(sqlQuery);
                while (res.next()) {
                    zmin = res.getString(1);
                    zmax = res.getString(2);
                }
            }

            // Check for slicing
            String genQuery = "";
            String selectClause
                    = "SELECT " + MULTIPOINT_VALUE + "[1] || ',' || "
                    + MULTIPOINT_VALUE + "[2] || ',' || "
                    + MULTIPOINT_VALUE + "[3],";
            String whereClause
                    = " WHERE " + MULTIPOINT_COVERAGE_ID + " = " + coverageID;

            if (xmin.equals(xmax)) {
                selectClause += "St_Y(" + MULTIPOINT_COORDINATE + ") || ' ' || St_Z(" + MULTIPOINT_COORDINATE + ")";
                whereClause += " AND St_X(" + MULTIPOINT_COORDINATE + ") = " + xmin;
            } else if (ymin.equals(ymax)) {
                selectClause += "St_X(" + MULTIPOINT_COORDINATE + ") || ' ' || St_Z(" + MULTIPOINT_COORDINATE + ")";
                whereClause += " AND St_Y(" + MULTIPOINT_COORDINATE + ") = " + ymin;
            } else if (zmin.equals(zmax)) {
                selectClause += "St_X(" + MULTIPOINT_COORDINATE + ") || ' ' || St_Y(" + MULTIPOINT_COORDINATE + ")";
                whereClause += " AND St_Z(" + MULTIPOINT_COORDINATE + ") = " + zmin;
            } else {
                selectClause += "St_X(" + MULTIPOINT_COORDINATE + ") || ' ' || St_Y(" + MULTIPOINT_COORDINATE + ") || ' ' || St_Z(" + MULTIPOINT_COORDINATE + ")";
                whereClause += " AND " + TABLE_MULTIPOINT + "." + MULTIPOINT_COORDINATE + " && "
                        + "'BOX3D(" + xmin + " " + ymin + " " + zmin + "," + xmax + " " + ymax + " " + zmax + ")'::box3d";
            }
            genQuery = selectClause + " FROM " + TABLE_MULTIPOINT + whereClause + " ORDER BY " + MULTIPOINT_ID;
            //log.debug("postGISQuery: " + genQuery);

            ResultSet r = s.executeQuery(genQuery);
            StringBuilder pointMembers = new StringBuilder();
            StringBuilder rangeMembers = new StringBuilder();;

            while (r.next()) {

                rangeMembers.append(r.getString(1)).append(" ");
                pointMembers.append(r.getString(2)).append(" ");
                ////log.debug("pointCount : " + pointCount);

            }

            s.close();
            s = null;

            members[0] = pointMembers.toString();
            members[1] = rangeMembers.toString();

            return members;

        } catch (SQLException sqle) {
            throw new Exception("Metadata database error", sqle);
        } finally {
            closeStatement(s);
        }
    }

    /**
     * Returns the coefficients between lo and hi, with a small epsilon number
     * (lo - epsilon, hi + epsilon) to support old imported coverage which was
     * imported when using BigDecimal(double) containing random numbers instead
     * of BigDecimal(double.toString()).
     *
     * @param covName
     * @param iOrder
     * @param lo
     * @param hi
     * @param epsilon
     * @return
     */
    private String getCoefficientsQuery(String covName, int iOrder, BigDecimal lo, BigDecimal hi, BigDecimal epsilon) {
        String sqlQuery
                = " SELECT MIN(" + TABLE_VECTOR_COEFFICIENTS + "." + VECTOR_COEFFICIENTS_ORDER + "),"
                + " MAX(" + TABLE_VECTOR_COEFFICIENTS + "." + VECTOR_COEFFICIENTS_ORDER + "),"
                + " COUNT(" + TABLE_VECTOR_COEFFICIENTS + "." + VECTOR_COEFFICIENTS_ORDER + ")"
                + " FROM " + TABLE_VECTOR_COEFFICIENTS + ", "
                + TABLE_GRID_AXIS + ", "
                + TABLE_COVERAGE
                + " WHERE " + TABLE_VECTOR_COEFFICIENTS + "." + VECTOR_COEFFICIENTS_AXIS_ID
                + " = " + TABLE_GRID_AXIS + "." + GRID_AXIS_ID
                + " AND " + TABLE_GRID_AXIS + "." + GRID_AXIS_COVERAGE_ID
                + " = " + TABLE_COVERAGE + "." + COVERAGE_ID
                + " AND " + TABLE_GRID_AXIS + "." + GRID_AXIS_RASDAMAN_ORDER + "=" + iOrder
                + " AND " + TABLE_COVERAGE + "." + COVERAGE_NAME + "='" + covName + "'"
                + " AND " + TABLE_VECTOR_COEFFICIENTS + "." + VECTOR_COEFFICIENTS_COEFFICIENT
                + " >= " + lo.subtract(epsilon).toString()
                + " AND " + TABLE_VECTOR_COEFFICIENTS + "." + VECTOR_COEFFICIENTS_COEFFICIENT
                // + " < "  + stringHi  // [a,b) subsets
                + " <= " + hi.add(epsilon).toString() // [a,b] subsets
                ;
        return sqlQuery;
    }

    /**
     * Method to retrieve subset indexes of an irregularly spaced axis, subject
     * to specified bounds. Bounds must be numeric (timestamps have already been
     * indexed before function call)
     *
     * @param covName Coverage human-readable name
     * @param iOrder Order of the axis in the CRS: to indentify it in case the
     * coverage has 2+ irregular axes.
     * @param lo The distance from origin (normalized by offset vector) of low
     * subset
     * @param hi The distance from origin (normalized by offset vector) of high
     * subset
     * @param min The lower bound
     * @param max The upper bound
     * @return Index of coefficients which enclose the interval.
     * @throws PetascopeException
     */
    public long[] getIndexesFromIrregularRectilinearAxis(String covName, int iOrder, BigDecimal lo, BigDecimal hi, long min, long max)
            throws Exception {

        long[] outCells = new long[2];
        Statement s = null;

        try {
            ensureConnection();
            s = conn.createStatement();
            // try with an epsilon

            String sqlQuery = getCoefficientsQuery(covName, iOrder, lo, hi, EPSILON);
            //log.debug("SQL query : " + sqlQuery);
            ResultSet r = s.executeQuery(sqlQuery);
            if (r.next()) {
                //check if count is not 0
                if (r.getInt(3) == 0) {
                    // no intersection on this slices
                    throw new Exception(covName + " does not contain any slices in the given subset (between coefficients '" + lo + "' and '" + hi + "')."
                    );
                }
                outCells[0] = r.getInt(1);
                outCells[1] = r.getInt(2);
            } else {
                // no intersection on this slices
                throw new Exception("Could not connect to metadata source."
                );
            }

            s.close();
            s = null;

            return outCells;

        } catch (SQLException sqle) {
            throw new Exception("Metadata database error", sqle);
        } finally {
            closeStatement(s);
        }
    }

    /**
     * Retrieves the coefficients of an irregular axis of a certain interval
     *
     * @param covName Coverage human-readable name
     * @param iOrder Order of the axis in the CRS: to identify it in case the
     * coverage has 2+ irregular axes.
     * @param lo The distance from origin (normalized by offset vector) of low
     * subset
     * @param hi The distance from origin (normalized by offset vector) of high
     * subset
     * @return The ordered list of coefficients of the grid points inside the
     * interval.
     * @throws PetascopeException
     */
    public List<BigDecimal> getCoefficientsOfInterval(String covName, int iOrder, BigDecimal lo, BigDecimal hi)
            throws Exception {

        List<BigDecimal> coefficients = new ArrayList<BigDecimal>();
        Statement s = null;

        try {
            ensureConnection();
            s = conn.createStatement();

            // Use subquery to get an ordered list of coefficient: array_agg does not order-by the array elements.
            // NOTE: ARRAY_AGG(foo by order) is available but from Postgres 9.0 on.
            String sqlQuery
                    = "SELECT " + TABLE_VECTOR_COEFFICIENTS + "." + VECTOR_COEFFICIENTS_COEFFICIENT
                    + " FROM " + TABLE_VECTOR_COEFFICIENTS + ", "
                    + TABLE_GRID_AXIS + ", "
                    + TABLE_COVERAGE
                    + " WHERE " + TABLE_VECTOR_COEFFICIENTS + "." + VECTOR_COEFFICIENTS_AXIS_ID
                    + " = " + TABLE_GRID_AXIS + "." + GRID_AXIS_ID
                    + " AND " + TABLE_GRID_AXIS + "." + GRID_AXIS_COVERAGE_ID
                    + " = " + TABLE_COVERAGE + "." + COVERAGE_ID
                    + " AND " + TABLE_GRID_AXIS + "." + GRID_AXIS_RASDAMAN_ORDER + "=" + iOrder
                    + " AND " + TABLE_COVERAGE + "." + COVERAGE_NAME + "='" + covName + "'"
                    + " AND " + TABLE_VECTOR_COEFFICIENTS + "." + VECTOR_COEFFICIENTS_COEFFICIENT
                    + " >= " + lo.subtract(EPSILON).toString()
                    + " AND " + TABLE_VECTOR_COEFFICIENTS + "." + VECTOR_COEFFICIENTS_COEFFICIENT
                    // + " < "  + stringHi  // [a,b) subsets
                    + " <= " + hi.add(EPSILON).toString()
                    + // [a,b] subsets
                    " ORDER BY " + TABLE_VECTOR_COEFFICIENTS + "." + VECTOR_COEFFICIENTS_COEFFICIENT;
            //log.debug("SQL query : " + sqlQuery);
            ResultSet r = s.executeQuery(sqlQuery);

            while (r.next()) {
                coefficients.add(r.getBigDecimal(1));
            }

            if (!coefficients.isEmpty()) {
                Arrays.sort(coefficients.toArray());
            }

            s.close();
            s = null;

            return coefficients;

        } catch (SQLException sqle) {
            throw new Exception("Metadata database error", sqle);
        } finally {
            closeStatement(s);
        }
    }

    /**
     * Retrieves the complete set of coefficients of an irregular axis.
     *
     * @param covName Coverage human-readable name
     * @param iOrder Order of the axis in the CRS: to identify it in case the
     * coverage has 2+ irregular axes.
     * @return The ordered list of coefficients of the grid points inside the
     * interval.
     * @throws PetascopeException
     */
    public List<BigDecimal> getAllCoefficients(String covName, int iOrder)
            throws Exception {

        List<BigDecimal> coefficients = new ArrayList<BigDecimal>();
        Statement s = null;

        try {
            ensureConnection();
            s = conn.createStatement();

            // Use subquery to get an ordered list of coefficient: array_agg does not order-by the array elements.
            // NOTE: ARRAY_AGG(foo by order) is available but from Postgres 9.0 on.
            String sqlQuery
                    = "SELECT " + TABLE_VECTOR_COEFFICIENTS + "." + VECTOR_COEFFICIENTS_COEFFICIENT
                    + " FROM " + TABLE_VECTOR_COEFFICIENTS + ", "
                    + TABLE_GRID_AXIS + ", "
                    + TABLE_COVERAGE
                    + " WHERE " + TABLE_VECTOR_COEFFICIENTS + "." + VECTOR_COEFFICIENTS_AXIS_ID
                    + " = " + TABLE_GRID_AXIS + "." + GRID_AXIS_ID
                    + " AND " + TABLE_GRID_AXIS + "." + GRID_AXIS_COVERAGE_ID
                    + " = " + TABLE_COVERAGE + "." + COVERAGE_ID
                    + " AND " + TABLE_GRID_AXIS + "." + GRID_AXIS_RASDAMAN_ORDER + "=" + iOrder
                    + " AND " + TABLE_COVERAGE + "." + COVERAGE_NAME + "='" + covName + "'"
                    + " ORDER BY " + TABLE_VECTOR_COEFFICIENTS + "." + VECTOR_COEFFICIENTS_COEFFICIENT;
            //log.debug("SQL query : " + sqlQuery);
            ResultSet r = s.executeQuery(sqlQuery);

            while (r.next()) {
                coefficients.add(r.getBigDecimal(1));
            }
            if (!coefficients.isEmpty()) {
                Arrays.sort(coefficients.toArray());
            }

            s.close();
            s = null;

            return coefficients;
        } catch (SQLException sqle) {
            throw new Exception("Metadata database error", sqle);
        } finally {
            closeStatement(s);
        }
    }

    /**
     * Gets the id of the axis with the given order, 0 if nothing found.
     *
     * @param coverageId
     * @param axisOrder
     * @return
     * @throws SQLException
     */
    public int getGridAxisId(int coverageId, int axisOrder) throws SQLException {
        Savepoint savePoint = conn.setSavepoint();
        PreparedStatement s = null;
        try {
            s = conn.prepareStatement("SELECT " + GRID_AXIS_ID
                    + " FROM " + TABLE_GRID_AXIS
                    + " WHERE " + GRID_AXIS_COVERAGE_ID + "= ?"
                    + "AND " + GRID_AXIS_RASDAMAN_ORDER + "= ?");
            s.setInt(1, coverageId);
            s.setInt(2, axisOrder);
            ResultSet r = s.executeQuery();
            int result = 0;
            while (r.next()) {
                result = r.getInt(GRID_AXIS_ID);
            }
            return result;
        } catch (SQLException e) {
            conn.rollback(savePoint);
            throw e;
        } finally {
            closeStatement(s);
        }
    }

    /**
     * Get the lower and upper bound of the given collection in pixel
     * coordinates.
     *
     * @param collName The collection name
     * @param collOid The OID of the collection
     * @return The minimum and maximum pixel bounds of the array.
     * @throws PetascopeException
     */
    public List<LegacyPair<String, String>> getCollectionDomain(String collName, BigInteger collOid) throws Exception {

        List<LegacyPair<String, String>> ret = new ArrayList<LegacyPair<String, String>>();

        // Run RasQL query
        Object obj = null;
        String rasQuery
                = RASQL_SELECT + " " + RASQL_SDOM + "(c) "
                + RASQL_FROM + " " + collName + " " + RASQL_AS + " c "
                + RASQL_WHERE + " " + RASQL_OID + "(c) = " + collOid;
        //log.debug("RasQL query : " + rasQuery);
        try {
            obj = LegacyRasUtil.executeRasqlQuery(rasQuery);
        } catch (Exception ex) {
            //log.error("Error while executing RasQL query", ex);
            throw new Exception("Error while executing RasQL query", ex);
        }

        // Parse the result
        if (obj != null) {
            LegacyRasQueryResult res = new LegacyRasQueryResult(obj);
            if (!res.getScalars().isEmpty()) {
                String s = res.getScalars().get(0).replaceAll("[\\[|\\]]", "");
                //log.debug("domain = " + s);
                String[] dims = s.split(DIMENSION_SEPARATOR);
                for (String dim : dims) {
                    String[] bounds = dim.split(DIMENSION_BOUND_SEPARATOR);
                    ret.add(LegacyPair.of(bounds[0], bounds[1]));
                }
            } else {
                //log.error("Marray " + collOid + " of collection " + collName + " was not found.");
                throw new Exception("Marray " + collOid + " of collection " + collName + " was not found: wrong OID in " + TABLE_RASDAMAN_COLLECTION + "?");
            }
        } else {
            //log.error("Empty response from rasdaman.");
            throw new Exception("Empty response from rasdaman.");
        }
        return ret;
    }

    public ResultSet executePostGISQuery(String postGisQuery) throws Exception {
        Statement s = null;
        ResultSet r = null;
        try {
            //log.debug("PostGIS Query: " + postGisQuery);
            s = conn.createStatement();
            r = s.executeQuery(postGisQuery);
        } catch (SQLException sqle) {
            throw new Exception("Metadata database error", sqle);
        } finally {
        }
        return r;
    }

    /**
     * Turns a SQL array to Java list (integer values case)
     *
     * @param sqlArray
     * @throws SQLException
     */
    private List<Integer> sqlArray2IntList(Array sqlArray) throws SQLException {
        List<Integer> outList = new ArrayList<Integer>();
        if (null != sqlArray) {
            ResultSet arrayRs = sqlArray.getResultSet();
            while (arrayRs.next()) {
                outList.add(arrayRs.getInt(2));
            }
        }
        return outList;
    }

    /**
     * Turns a SQL array to Java list (strings case)
     *
     * @param sqlArray
     * @throws SQLException
     */
    private List<String> sqlArray2StringList(Array sqlArray) throws SQLException {
        List<String> outList = new ArrayList<String>();
        if (null != sqlArray) {
            ResultSet arrayRs = sqlArray.getResultSet();
            while (arrayRs.next()) {
                outList.add(arrayRs.getString(2));
            }
        }
        return outList;
    }

    private LegacyDescription readDescription(Integer descriptionId) throws SQLException {

        LegacyDescription owsDescription = new LegacyDescription();
        Statement s = conn.createStatement();
        ResultSet r;
        String sqlQuery;

        /* PS_DESCRIPTION */
        sqlQuery
                = " SELECT " + DESCRIPTION_TITLES + ", "
                + DESCRIPTION_ABSTRACTS + ", "
                + DESCRIPTION_KEYWORD_GROUP_IDS
                + " FROM " + TABLE_DESCRIPTION
                + " WHERE " + DESCRIPTION_ID + "=" + descriptionId;
        //log.debug("SQL query: " + sqlQuery);
        r = s.executeQuery(sqlQuery);
        if (r.next()) {
            // Get titles and abstracts
            List<String> titles = null;
            if (LegacyConfigManager.METADATA_SQLITE) {
                titles = LegacyListUtil.toList(r.getString(DESCRIPTION_TITLES));
            } else {
                titles = sqlArray2StringList(r.getArray(DESCRIPTION_TITLES));
            }
            for (String title : titles) {
                owsDescription.addTitle(title);
            }
            List<String> abstracts = null;
            if (LegacyConfigManager.METADATA_SQLITE) {
                abstracts = LegacyListUtil.toList(r.getString(DESCRIPTION_ABSTRACTS));
            } else {
                abstracts = sqlArray2StringList(r.getArray(DESCRIPTION_ABSTRACTS));
            }
            for (String descrAbstract : abstracts) {
                owsDescription.addAbstract(descrAbstract);
            }

            // Get keywords
            if (!LegacyConfigManager.METADATA_SQLITE) {
                List<Integer> keywordGroupIds = sqlArray2IntList(r.getArray(DESCRIPTION_KEYWORD_GROUP_IDS));
                for (Integer groupId : keywordGroupIds) {

                    /* PS_KEYWORD_GROUP */
                    sqlQuery
                            = " SELECT " + KEYWORD_GROUP_KEYWORD_IDS + ", "
                            + KEYWORD_GROUP_TYPE + ", "
                            + KEYWORD_GROUP_TYPE_CODESPACE
                            + " FROM " + TABLE_KEYWORD_GROUP
                            + " WHERE " + KEYWORD_GROUP_ID + "=" + groupId;
                    //log.debug("SQL query: " + sqlQuery);
                    ResultSet rr = s.executeQuery(sqlQuery);
                    List<LegacyPair<String, String>> keysAndLangs = new ArrayList<LegacyPair<String, String>>();
                    while (rr.next()) {
                        // type and type-codespace
                        String groupType = rr.getString(KEYWORD_GROUP_TYPE);
                        String typeCodespace = rr.getString(KEYWORD_GROUP_TYPE_CODESPACE);
                        List<Integer> keywordIds = sqlArray2IntList(rr.getArray(KEYWORD_GROUP_KEYWORD_IDS));

                        for (Integer keyId : keywordIds) {
                            // keywords
                            Statement ss = conn.createStatement();

                            /* PS_KEYWORD */
                            sqlQuery
                                    = " SELECT " + KEYWORD_VALUE + ", "
                                    + KEYWORD_LANGUAGE
                                    + " FROM " + TABLE_KEYWORD
                                    + " WHERE " + KEYWORD_ID + "=" + keyId;
                            //log.debug("SQL query: " + sqlQuery);
                            ResultSet rrr = ss.executeQuery(sqlQuery);
                            while (rrr.next()) {
                                String kValue = rrr.getString(KEYWORD_VALUE);
                                String kLang = rrr.getString(KEYWORD_LANGUAGE);
                                // Add this keyword
                                keysAndLangs.add(LegacyPair.of(kValue, kLang));
                            }
                        }

                        // Add the group of keywords
                        owsDescription.addKeywordGroup(keysAndLangs, groupType, typeCodespace);
                    }
                }
            }
        } // else: no harm, Descriptions are optional

        return owsDescription;
    }

    private int countTables(String iPattern) throws Exception, SQLException {

        Statement s = conn.createStatement();
        ResultSet r;
        String sqlQuery;
        int count = 0;

        if (LegacyConfigManager.METADATA_SQLITE) {
            sqlQuery
                    = " SELECT COUNT(*) FROM sqlite_master"
                    + " WHERE type = 'table' AND name " + CASE_INSENSITIVE_LIKE + " \'" + iPattern + "\'";
        } else if (LegacyConfigManager.METADATA_HSQLDB) {
            sqlQuery
                    = " SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES";
        } else {
            sqlQuery
                    = " SELECT COUNT(*) FROM " + TABLE_TABLES_CATALOG
                    + " WHERE " + TABLES_CATALOG_SCHEMANAME + "=" + CURRENT_SCHEMA
                    + " AND " + TABLES_CATALOG_TABLENAME + " " + CASE_INSENSITIVE_LIKE + " \'" + iPattern + "\'";
        }
        //log.debug("SQL query: " + sqlQuery);
        r = s.executeQuery(sqlQuery);

        if (r.next()) {
            count = r.getInt(1);
        } else {
            closeStatement(s);
            throw new Exception("No tuples returned while counting tables with pattern " + iPattern);
        }
        closeStatement(s);

        return count;
    }

    /**
     * Creates an SWE Quantity object from the associated SQL result set and
     * associated allowed intervals.
     *
     * @param rSwe Tuple containing ps_quantity and ps_nil_value.
     * @param intervals Allowed intervals defining the constraints of the
     * quantity on this tuple.
     * @throws SQLException
     */
    private LegacyQuantity readSWEQuantity(int quantityId)
            throws SQLException, Exception {

        // Quantity fields
        LegacyQuantity sweQuantity;
        String label;
        String description;
        String definitionUri;
        String uomCode;
        List<LegacyNilValue> nils = new ArrayList<LegacyNilValue>();
        List<LegacyPair<BigDecimal, BigDecimal>> allowedIntervals = new ArrayList<LegacyPair<BigDecimal, BigDecimal>>();
        // SQL interface
        Statement s = null;
        ResultSet rSwe;
        String sqlQuery;

        // DB connection
        try {
            ensureConnection();
            s = conn.createStatement();

            /////////////////////////////////////////////////////
            // Quantity <-> Intervals of allowed values
            List<Integer> intervalIds = new ArrayList<Integer>();
            sqlQuery
                    = "SELECT " + QUANTITY_INTERVAL_QID + ","
                    + QUANTITY_INTERVAL_IID
                    + " FROM " + TABLE_QUANTITY_INTERVAL
                    + " WHERE " + QUANTITY_INTERVAL_QID + "=" + quantityId;
            //log.debug("SQL query: " + sqlQuery);
            rSwe = s.executeQuery(sqlQuery);
            while (rSwe.next()) {
                intervalIds.add(rSwe.getInt(QUANTITY_INTERVAL_IID));
            }

            // IntervalIds -> Intervals
            // NOTE: PostgreSQL Numeric <-> BigDecimal
            // (see "Mapping SQL and Java Types, §8.9.1" - http://docs.oracle.com/javase/1.3/docs/guide/jdbc/getstart/mapping.html)
            List<LegacyPair<BigDecimal, BigDecimal>> intervals = new ArrayList<LegacyPair<BigDecimal, BigDecimal>>(intervalIds.size());
            if (!intervalIds.isEmpty()) { // allowed-intervals are optional
                sqlQuery
                        = "SELECT " + INTERVAL_MIN + ", "
                        + INTERVAL_MAX
                        + " FROM " + TABLE_INTERVAL
                        + " WHERE " + INTERVAL_ID + " IN ("
                        + LegacyListUtil.printList(intervalIds, ",") + ")"
                        + " ORDER BY " + INTERVAL_MIN;
                ;
                //log.debug("SQL query: " + sqlQuery);
                rSwe = s.executeQuery(sqlQuery);
                while (rSwe.next()) {
                    LegacyPair minMax = LegacyPair.of(rSwe.getBigDecimal(INTERVAL_MIN), rSwe.getBigDecimal(INTERVAL_MAX));
                    intervals.add(minMax);
                }
            }
            LegacyAllowedValue allowedValues;
            List<LegacyRealPair> pairs = new ArrayList<LegacyRealPair>(intervals.size());
            // Create intervals
            for (LegacyPair<BigDecimal, BigDecimal> interval : intervals) {
                pairs.add(new LegacyRealPair(interval.fst, interval.snd));
            }
            allowedValues = new LegacyAllowedValue(pairs);

            String tableNilValueJoin = " LEFT OUTER JOIN " + TABLE_NIL_VALUE + " ON (";;
            sqlQuery
                    = "SELECT " + QUANTITY_NIL_IDS
                    + " FROM " + TABLE_QUANTITY
                    + " WHERE " + QUANTITY_ID + "=" + quantityId;
            //log.debug("SQL query: " + sqlQuery);
            ResultSet quantityNilIdsRS = s.executeQuery(sqlQuery);
            if (quantityNilIdsRS.next()) {
                List<Integer> quantityNilIds = sqlArray2IntList(
                        quantityNilIdsRS.getArray(QUANTITY_NIL_IDS));
                if (!quantityNilIds.isEmpty()) {
                    String tmpIds = "";
                    for (Integer quantityNilId : quantityNilIds) {
                        if (!tmpIds.isEmpty()) {
                            tmpIds += " OR ";
                        }
                        tmpIds += TABLE_NIL_VALUE + "." + NIL_VALUE_ID + "=" + quantityNilId;
                    }
                    tableNilValueJoin += tmpIds + ")";
                } else {
                    // left outer join must have some condition, so this adds a random test
                    tableNilValueJoin += "1=2)";
                }
            }

            // Quantity attributes
            String unaggregatedFields
                    = TABLE_UOM + "." + UOM_CODE + ","
                    + TABLE_QUANTITY + "." + QUANTITY_LABEL + ","
                    + TABLE_QUANTITY + "." + QUANTITY_DESCRIPTION + ","
                    + TABLE_QUANTITY + "." + QUANTITY_DEFINITION + ","
                    + TABLE_QUANTITY + "." + QUANTITY_NIL_IDS + ","
                    + TABLE_NIL_VALUE + "." + NIL_VALUE_ID;
            sqlQuery
                    = " SELECT " + unaggregatedFields + ","
                    + TABLE_NIL_VALUE + "." + NIL_VALUE_VALUE + " AS " + NIL_VALUES_ALIAS + " , "
                    + TABLE_NIL_VALUE + "." + NIL_VALUE_REASON + " AS " + NIL_REASONS_ALIAS
                    + " FROM " + TABLE_QUANTITY
                    + " INNER JOIN " + TABLE_UOM + " ON (" + TABLE_UOM + "." + UOM_ID + "=" + TABLE_QUANTITY + "." + QUANTITY_UOM_ID
                    + ") " + tableNilValueJoin
                    + " WHERE " + TABLE_QUANTITY + "." + QUANTITY_ID + "=" + quantityId
                    + " GROUP BY " + unaggregatedFields + " , " + NIL_VALUES_ALIAS + " , " + NIL_REASONS_ALIAS;

            //Example query + Response
            //SELECT ps_uom.code, ps_quantity.label, ps_quantity.description, ps_quantity.definition_uri,
            //      ps_quantity.nil_ids, ps_nil_value.id, ps_nil_value.value as nil_values,
            //      ps_nil_value.reason as nil_reasons
            //FROM ps_nil_value, ps_quantity INNER JOIN ps_uom ON (ps_quantity.uom_id=ps_uom.id)
            //WHERE ps_quantity.id = <quantityId>
            //GROUP BY ps_uom.code, ps_quantity.label, ps_quantity.description,
            //ps_quantity.definition_uri, nil_values, nil_reasons, ps_quantity.nil_ids, ps_nil_value.id
            // code   | label |   description    |                   definition_uri                    | nil_ids | id | nil_values |                 nil_reasons
            //---------+-------+------------------+-----------------------------------------------------+---------+----+------------+----------------------------------------------
            //Celsius | tg    | mean temperature | http://eca.knmi.nl/download/ensembles/ensembles.php | {1,7}   |  1 | -9999      | http://www.opengis.net/def/nil/OGC/0/missing
            //Celsius | tg    | mean temperature | http://eca.knmi.nl/download/ensembles/ensembles.php | {1,7}   |  5 | -8000      | http://___________
            //Celsius | tg    | mean temperature | http://eca.knmi.nl/download/ensembles/ensembles.php | {1,7}   |  7 | -9999      | http://___________
            //Celsius | tg    | mean temperature | http://eca.knmi.nl/download/ensembles/ensembles.php | {1,7}   |  6 | -8000      | http://___________
            //log.debug("SQL query: " + sqlQuery);
            rSwe = s.executeQuery(sqlQuery);
            if (!rSwe.next()) {
                closeStatement(s);
                throw new Exception("No SWE quantities stored in the database.");
            }

            // Parse the result set
            label = rSwe.getString(QUANTITY_LABEL);
            description = rSwe.getString(QUANTITY_DESCRIPTION);
            definitionUri = rSwe.getString(QUANTITY_DEFINITION);
            uomCode = rSwe.getString(UOM_CODE);

            List<Integer> nilIds = sqlArray2IntList(rSwe.getArray(QUANTITY_NIL_IDS));

            //for all rows
            do {
                //check if id is in the nilIds
                Integer index = nilIds.indexOf(rSwe.getInt(NIL_VALUE_ID));
                if (index != -1) {
                    String nilValue = rSwe.getString(NIL_VALUES_ALIAS);
                    String nilReason = rSwe.getString(NIL_REASONS_ALIAS);
                    // NOTE: If nilValue is empty (null or "") then don't add it to coverage metadata's range as it is invalid (we allow to add nilReason = "")
                    if (!StringUtils.isEmpty(nilValue) && null != nilReason) {
                        nils.add(new LegacyNilValue(nilValue, nilReason));
                    }
                }
            } while (rSwe.next());

            // Finally, create the Quantity component
            sweQuantity = new LegacyQuantity(
                    label,
                    description,
                    definitionUri,
                    nils,
                    uomCode,
                    allowedValues
            );

            s.close();
            s = null;

        } catch (SQLException sqle) {
            throw new Exception("Error retrieving Quantity with id " + quantityId, sqle);
        } finally {
            closeStatement(s);
        }

        return sweQuantity;
    }

    /**
     * ----------------------------- Connection management helpers
     * -----------------------------
     */
    /**
     * Open the connection if it isn't already.
     */
    public void ensureConnection() throws SQLException {
        synchronized (this) {
            if (conn == null || conn.isClosed()) {
                openConnection();
            }
        }
    }

    /**
     * Open connection; no checks are performed here.
     */
    public void openConnection() throws SQLException {
        //log.trace("Opening database connection to " + user + "@" + url);
        conn = DriverManager.getConnection(url, user, pass);
        conn.setAutoCommit(false);
        savepoint = conn.setSavepoint();
    }

    /**
     * Rollback to previous savepoint and close connection.
     */
    public void abortAndClose() throws SQLException {
        if (conn != null) {
            conn.rollback(savepoint);
            conn.close();
            conn = null;
        }
    }

    /**
     * Commit and close connection.
     */
    public void commitAndClose() throws SQLException {
        if (conn != null) {
            conn.commit();
            conn.close();
            conn = null;
        }
    }

    /**
     * Safe connection closing.
     */
    public void closeConnection() {
        if (conn != null) {
            try {
                //log.trace("Closing database connection.");
                conn.close();
            } catch (SQLException sqle) {
                //log.error("Failed closing database connection.", sqle);
            }
            conn = null;
        }
    }

    /**
     * Safe connection and statement closing.
     */
    public void closeConnection(Statement s) {
        closeStatement(s);
        closeConnection();
    }

    /**
     * Safe connection and statement closing.
     */
    public void closeStatement(Statement s) {
        if (s != null) {
            try {
                s.close();
            } catch (SQLException ex) {
            }
        }
    }

    /* ----------------- Admin update Service Identification and Provider ----------------------*/
    /**
     * @author: Bang Pham Huu
     */
    /**
     * This function will only get 1 (Primary Key) from tables (for updating
     * identification and provider) which needs 1 ID from FIRST_ROW of this
     * table Table: ps_description, ps_service_identification,
     * ps_service_provider
     *
     * @param tableName
     * @return pk key from first row
     * @throws java.sql.SQLException
     */
    public String getPrimaryKeyFromFirstRow(String tableName) throws SQLException {
        String pk = "";

        String PK_FIELD = "id";

        // Init connection to database first
        ensureConnection();

        // Then init query to database
        Statement s = conn.createStatement();

        String sqlQuery = "SELECT " + PK_FIELD + " FROM " + tableName;

        //log.debug("SQL query : " + sqlQuery);

        ResultSet r = null;

        // Execute query and return error
        try {
            r = s.executeQuery(sqlQuery);
            if (r.next()) {
                // Get only 1 value
                pk = r.getString(PK_FIELD);
            }

            if (pk.equals("")) {
                String error = tableName + " is empty.";
                throw new SQLException(error);
            }
        } catch (SQLException ex) {
            String error = "Error when query table " + LegacyDbMetadataSource.TABLE_ROLE_CODE + " with query: " + sqlQuery + ". Exception is: " + ex.toString();
            //log.error(error);
            throw new SQLException(error);
        }

        s.close();
        s = null;

        // If table is not empty then return PK key
        return pk;

    }

    /**
     * This function will update Identification including (title, abstract,
     * service type and service type version) NOTE: table description (title,
     * abstract) table service identification (service type, service type
     * version)
     *
     * @throws java.sql.SQLException
     */
    public void updateIdentication(Map<String, String> valueMap) throws SQLException {
        // Init connection to database first
        ensureConnection();

        // Then init query to database
        Statement s = conn.createStatement();

        // *********** 1. First update ps_description ***********
        // 1.1 Get first row (pk_key) from ps_description
        String pkID = getPrimaryKeyFromFirstRow(TABLE_DESCRIPTION);

        // 1.2 Generate update query
        String sqlQuery = "UPDATE " + TABLE_DESCRIPTION + " ";

        /*update ps_description
         set titles='{abc}', abstracts='{xxxxxxxxxxxxxxxXXX}'
         where id=1*/
        sqlQuery = sqlQuery + "SET " + DESCRIPTION_TITLES + "='{" + valueMap.get("serviceTitle") + "}'" + ",";
        sqlQuery = sqlQuery + DESCRIPTION_ABSTRACTS + "='{" + valueMap.get("abstract") + "}' ";
        sqlQuery = sqlQuery + "WHERE " + DESCRIPTION_ID + "='" + pkID + "'";
        //log.debug("SQL query : " + sqlQuery);

        // 1.3 Run update query (note executeUpdate)
        try {
            int r = s.executeUpdate(sqlQuery);
            // Important: Without commit although r > 0 but nothing is updated?
            conn.commit();
            if (r > 0) {
                //log.debug("Update " + TABLE_DESCRIPTION + " successful!");
            }
        } catch (Exception e) {
            //log.error(e.getMessage());
        } finally {
            s.close();
            s = null;
        }

        // *********** 2. Second update ps_service_identification ***********
        s = conn.createStatement();
        // 2.1 Get first row (pk_key) from ps_service_deintification
        pkID = getPrimaryKeyFromFirstRow(TABLE_SERVICE_IDENTIFICATION);

        // 2.2 Generate update query
        sqlQuery = "UPDATE " + TABLE_SERVICE_IDENTIFICATION + " ";

        // NOTE: Don't allow to update service_type as it have problem with load Coverage Description (error)
        sqlQuery = sqlQuery + "SET " + SERVICE_IDENTIFICATION_TYPE_VERSIONS + "='{" + valueMap.get("serviceTypeVersion") + "}' ";
        sqlQuery = sqlQuery + "WHERE " + SERVICE_IDENTIFICATION_ID + "='" + pkID + "'";
        //log.debug("SQL query : " + sqlQuery);

        // 2.3 Run update query
        try {
            int r = s.executeUpdate(sqlQuery);
            // Important: Without commit although r > 0 but nothing is updated?
            conn.commit();
            if (r > 0) {
                //log.debug("Update " + TABLE_SERVICE_IDENTIFICATION + " successful!");
            }
        } catch (SQLException ex) {
            String error = "Error when query table " + LegacyDbMetadataSource.TABLE_SERVICE_IDENTIFICATION + " with query: " + sqlQuery + ". Exception is: " + ex.toString();
            //log.error(error);
            throw new SQLException(error);
        } finally {
            // Close connection
            s.close();
            s = null;

        }

    }

    /**
     * This function will update Service Provider including (name, website,
     * contact, position, role, email, hours of service, contact instructions,
     * city, administrative area, postal code, country)
     *
     * @param valueMap: Map<String, String>
     * @throws java.sql.SQLException
     */
    public void updateProvider(Map<String, String> valueMap) throws SQLException {

        // Init connection to database first
        ensureConnection();

        // Then init query to database
        Statement s = conn.createStatement();

        // *********** 1. First update ps_service_provider ***********
        // 1.1 Get first row (pk_key) from ps_service_provider
        String pkID = getPrimaryKeyFromFirstRow(TABLE_SERVICE_PROVIDER);

        // 1.2 Generate update query
        String sqlQuery = "UPDATE " + TABLE_SERVICE_PROVIDER + " "
                + "SET name = '" + valueMap.get("providerName") + "',"
                + "site = '" + valueMap.get("providerWebsite") + "',"
                + "contact_individual_name = '" + valueMap.get("contactPerson") + "',"
                + "contact_administrative_area = '" + valueMap.get("administrativeArea") + "',"
                + "contact_position_name = '" + valueMap.get("positionName") + "',"
                + "contact_city = '" + valueMap.get("cityAddress") + "',"
                + "contact_postal_code = '" + valueMap.get("postalCode") + "',"
                + "contact_country = '" + valueMap.get("country") + "',"
                + "contact_email_addresses = '{" + valueMap.get("email") + "}',"
                + "contact_hours_of_service = '" + valueMap.get("hoursOfService") + "',"
                + "contact_instructions = '" + valueMap.get("contactInstructions") + "',"
                + "contact_role_id = '" + valueMap.get("roleID") + "' "
                + "WHERE id= '" + getPrimaryKeyFromFirstRow(TABLE_SERVICE_PROVIDER) + "'";

        //log.debug("SQL query : " + sqlQuery);

        // 1.3 Run update query (note executeUpdate)
        try {
            int r = s.executeUpdate(sqlQuery);
            // Important: Without commit although r > 0 but nothing is updated?
            conn.commit();
            if (r > 0) {
                //log.debug("Update " + TABLE_SERVICE_PROVIDER + " successful!");
            }
        } catch (Exception e) {
            String error = "Error when query table " + LegacyDbMetadataSource.TABLE_SERVICE_PROVIDER + " with query: " + sqlQuery + ". Exception is: " + e.toString();
            //log.error(error);
            throw new SQLException(error);
        } finally {
            s.close();
            s = null;
        }
    }

    /**
     * This function will get all role_code in ps_role_code and return to
     * form.jsp to display role list as select/option
     *
     * @throws java.sql.SQLException
     */
    public List<LegacyPair> getListRoleCodeFromDB() throws SQLException {
        // Init connection to database first
        ensureConnection();

        // Then init query to database
        Statement s = conn.createStatement();

        // Return list<string, string> with key, value from role table
        List<LegacyPair> roleList = new ArrayList<LegacyPair>();

        String id = ROLE_CODE_ID;
        String value = ROLE_CODE_VALUE;

        String sqlQuery = "SELECT " + id + "," + value
                + " FROM " + LegacyDbMetadataSource.TABLE_ROLE_CODE;
        //log.debug("SQL query : " + sqlQuery);

        ResultSet r = null;

        // Execute query and return error
        try {
            r = s.executeQuery(sqlQuery);
            if (!r.next()) {
                String error = "Role code table is empty";
                throw new SQLException(error);
            }
        } catch (SQLException ex) {
            String error = "Error when query table " + LegacyDbMetadataSource.TABLE_ROLE_CODE + " with query: " + sqlQuery + ". Exception is: " + ex.toString();
            //log.error(error);
            throw new SQLException(error);
        }
        // If table is not empty then get ID, Value as key, value and return as String List
        while (r.next()) {
            LegacyPair<String, String> typeValue = LegacyPair.of(r.getString("id"), r.getString("value"));
            roleList.add(typeValue);
        }

        // Close connection
        try {
            s.close();
            s = null;
        } catch (Exception e) {
            //log.error(e.getMessage());
        } finally {
            closeConnection(s);
        }

        // return role list to select/option
        return roleList;
    }
    
    
    /**
     * Returns a list of all available WMS layers in petascopedb
     *
     * @return Unordered list of coverage names
     * @throws PetascopeException
     */
    public List<String> layers() throws Exception {
        Statement s = null;
        List<String> coverages;

        try {
            ensureConnection();
            s = conn.createStatement();

            String sqlQuery
                    = " SELECT name"
                    + " FROM wms13_layer"
                    + " ORDER BY name";
            //log.debug("SQL query: " + sqlQuery);
            ResultSet r = s.executeQuery(sqlQuery);
            coverages = new ArrayList<String>(r.getFetchSize());
            while (r.next()) {
                coverages.add(r.getString("name"));
            }
            s.close();
            s = null;

            return coverages;
        } catch (SQLException sqle) {
            throw new Exception("Metadata database error while getting a list of the coverages.", sqle);
        } finally {
            closeStatement(s);
        }
    }
    

    /**
     * Read Legacy WMS 1.3 layer's metadata by layer name
     *
     * @param legacyLayerName
     * @return
     * @throws SQLException
     */
    public LegacyWMSLayer getLegacyWMSLayer(String legacyLayerName) throws SQLException {
        LegacyWMSLayer legacyWmsLayer = null;
        this.ensureConnection();
        //initialize connection
        Statement s = conn.createStatement();
        try {

            // First, check if legacy WMS layer exists in both wms layer table and wcs coverage table
            // NOTE: if a wms layer does not exist in wcs coverage table, it cannot be used to migrate as the associated coverage does not exist.
            String sqlQuery = "SELECT * from wms13_layer where name ='" + legacyLayerName 
                            + "' and name in (select name from ps_coverage where name = '" + legacyLayerName + "')";
            //log.debug("SQL Query : " + sqlQuery);
            ResultSet r = s.executeQuery(sqlQuery);
            while (r.next()) {
                legacyWmsLayer = new LegacyWMSLayer();

                int id = r.getInt("id");
                String name = r.getString("name");
                String title = r.getString("title");
                String layerAbstract = r.getString("layerAbstract");
                int exBoundingBox_id = r.getInt("exBoundingBox_id");
                LegacyWMSEXGeographicBoundingBox EXBBox = this.getLegacyWMSEXGeographicBoundingBox(exBoundingBox_id);
                List<LegacyWMSStyle> styles = this.getLegacyWMSStyle(id);

                legacyWmsLayer.setName(name);
                legacyWmsLayer.setTitle(title);
                legacyWmsLayer.setLayerAbstract(layerAbstract);
                legacyWmsLayer.setExBBox(EXBBox);
                legacyWmsLayer.setStyles(styles);
            }

        } catch (SQLException e) {
            throw e;
        } finally {
            this.closeStatement(s);
        }

        return legacyWmsLayer;
    }

    /**
     * Read the Legacy WMS EXGeographicBoundingBox object from
     * wms13_ex_geographic_bounding_box table
     *
     * @param s
     * @param layerId
     * @return
     */
    private LegacyWMSEXGeographicBoundingBox getLegacyWMSEXGeographicBoundingBox(int exBBoxId) throws SQLException {
        LegacyWMSEXGeographicBoundingBox exBBox = new LegacyWMSEXGeographicBoundingBox();

        Statement s = conn.createStatement();
        try {
            String sqlQuery = "SELECT * from wms13_ex_geographic_bounding_box where id=" + exBBoxId;
            //log.debug("SQL Query : " + sqlQuery);
            ResultSet r = s.executeQuery(sqlQuery);
            while (r.next()) {
                String westBoundLongitude = r.getString("westBoundLongitude");
                String eastBoundLongitude = r.getString("eastBoundLongitude");
                String southBoundLatitude = r.getString("southBoundLatitude");
                String northBoundLatitude = r.getString("northBoundLatitude");

                exBBox.setId(exBBoxId);
                exBBox.setWestBoundLongitude(westBoundLongitude);
                exBBox.setEastBoundLongitude(eastBoundLongitude);
                exBBox.setSouthBoundLatitude(southBoundLatitude);
                exBBox.setNorthBoundLatitude(northBoundLatitude);
            }
        } catch (SQLException e) {
            throw e;
        } finally {
            closeStatement(s);
        }

        return exBBox;
    }

    /**
     * Read the Legacy WMS Style objects from wms13_style table A layer can
     * contain multiple styles.
     *
     * @param s
     * @param layerId
     * @return
     */
    private List<LegacyWMSStyle> getLegacyWMSStyle(int layerId) throws SQLException {
        List<LegacyWMSStyle> styles = new ArrayList<>();

        Statement s = conn.createStatement();
        
        try {
            String sqlQuery = "SELECT * from wms13_style where layer_id=" + layerId;
            //log.debug("SQL Query : " + sqlQuery);
            ResultSet r = s.executeQuery(sqlQuery);
            while (r.next()) {
                LegacyWMSStyle style = new LegacyWMSStyle();
                String name = r.getString("name");
                String title = r.getString("title");
                String styleAbstract = r.getString("styleAbstract");
                String rasqlQueryTransformer = r.getString("rasqlQueryTransformer");

                style.setName(name);
                style.setTitle(title);
                style.setStyleAbstract(styleAbstract);
                style.setRasqlQueryTransformer(rasqlQueryTransformer);

                styles.add(style);
            }
        } catch (SQLException e) {
            throw e;
        } finally {
            closeStatement(s);
        }

        return styles;
    }
}
