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
package petascope.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.ParsingException;
import nu.xom.ValidityException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.gdal.osr.SpatialReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.rasdaman.config.ConfigManager;
import static org.rasdaman.config.ConfigManager.SECORE_INTERNAL;
import org.rasdaman.secore.Resolver;
import org.rasdaman.secore.db.DbManager;
import org.rasdaman.secore.db.DbSecoreVersion;
import org.rasdaman.secore.req.ResolveRequest;
import org.rasdaman.secore.req.ResolveResponse;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import petascope.core.CrsDefinition;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.core.AxisTypes;
import petascope.core.XMLSymbols;
import static petascope.util.StringUtil.ENCODING_UTF8;
import static petascope.core.CrsDefinition.LONGITUDE_AXIS_LABEL_EPGS_VERSION_0;
import static petascope.core.CrsDefinition.LONGITUDE_AXIS_LABEL_EPGS_VERSION_85;



/**
 * Coordinates transformation utility in case a spatial reprojection is needed
 * before translating to WCPS/RASQL queries. * NOTE: each instance of this class
 * should decode the source and target CRS at construction time, to a
 * one-for-all read into the EPSG db. It also should not be static since
 * different requests will involve different CRS. However keeping a static
 * dictionary of the requested mathematical transformations avoids the need of
 * redundant read onto the EPSG db.
 *
 * @author <a href="mailto:p.campalani@jacobs-university.de">Piero Campalani</a>
 */
public class CrsUtil {

    // NOTE: accept any URI format, but ask to SECORE: flattened definitions, less recursion.
    public static final String OPENGIS_URI_PREFIX = "http://www.opengis.net";
    public static final String DEFAULT_EPSG_VERSION = "8.5";
    public static final String SECORE_CONTEXT_PATH = "def";
    public static final String OPENGIS_INDEX_URI = OPENGIS_URI_PREFIX + "/def/crs/OGC/0/Index$ND";
    public static final String OPENGIS_EPSG_URI = OPENGIS_URI_PREFIX + "/def/crs/EPSG/0/";
    public static final String OPENGIS_EPSG_URI_DEFAULT_VERSION = OPENGIS_URI_PREFIX + "/def/crs/EPSG/" + DEFAULT_EPSG_VERSION + "/";
    private static final String HTTP_URL_PATTERN = "(http|https)://.*/";
    private static final String HTTP_PREFIX = "http";
    private static final String LOCALHOST = "localhost";

    // SECORE keywords (URL is set in the ConfigManager)
    public static final String KEY_RESOLVER_CRS = "crs";
    public static final String KEY_RESOLVER_CCRS = "crs-compound";
    public static final String KEY_RESOLVER_EQUAL = "equal";
    public static final char SLICED_AXIS_SEPARATOR = '@';

    // NOTE: "CRS:1" axes to have a GML definition that will be parsed.
    // NOTE: IndexND is different with CRS:1 (consider IndexND is a geo-referenced axis type)
    // while CRS:1 is Rasql grid axis type, it is not interchangeable.
    public static final String GRID_CRS = "CRS:1";
    public static final String INDEX_CRS_PATTERN = "Index\\d+D";
    public static final String INDEX_CRS_PATTERN_NUMBER = "\\d+";
    // Replace \\d+ with the number of axis (e.g: 2 (mr), 3 (irr_cube_1))
    public static final String OPENGIS_INDEX_ND_PATTERN = OPENGIS_URI_PREFIX + "/def/crs/OGC/0/" + INDEX_CRS_PATTERN;
    public static final String INDEX_CRS_PREFIX = "Index";
    public static final String INDEX_UOM = "GridSpacing"; // See Uom in Index[1-9]D CRS defs
    public static final BigDecimal INDEX_SCALAR_RESOLUTION = BigDecimal.ONE; // Use for RectifiedGrid coverage which uses IndexND
    public static final String PURE_UOM = "10^0";

    public static final String CRS_DEFAULT_VERSION = "0";
    //public static final String CRS_DEFAULT_FORMAT  = "application/gml+xml";

    // TODO: do not rely on a static set of auths, but ask SECORE to return the supported authorities: ./def/crs/
    public static final String EPSG_AUTH = "EPSG";
    public static final String EPSG_4326_AUTHORITY_CODE = EPSG_AUTH + ":4326";
    public static final String ISO_AUTH = "ISO";
    public static final String AUTO_AUTH = "AUTO";
    public static final String OGC_AUTH = "OGC";
    // rotated-CRS netCDF
    public static final String COSMO_AUTH = "COSMO";
    // COSMO 101 CRS
    public static final String COSMO_101_AUTHORITY_CODE = COSMO_AUTH + ":101";
    //public static final String IAU_AUTH  = "IAU2000";
    //public static final String UMC_AUTH  = "UMC";
    public static final List<String> SUPPORTED_AUTHS = Arrays.asList(EPSG_AUTH, ISO_AUTH, AUTO_AUTH, OGC_AUTH); // IAU_AUTH, UMC_AUTH);

    // WGS84
    public static final String WGS84_EPSG_CODE = "4326";

    // e.g: <identifier>http://localhost:8080/def/crs/EPSG/0/4326</identifier>
    public static final String SECORE_IDENTIFIER_PATTERN = "<identifier>(.+?)</identifier>";
    
    private static final String REGEX = "localhost(:[0-9]+)?/def";
    private static final Pattern localhostPattern = Pattern.compile(REGEX, Pattern.CASE_INSENSITIVE);
    

    /* CACHES: avoid EPSG db and SECORE redundant access */
    private static Map<String, CrsDefinition> parsedCRSs = new HashMap<String, CrsDefinition>();        // CRS definitions
    private static Map<List<String>, Boolean> crsComparisons = new HashMap<List<String>, Boolean>();        // CRS equality tests
    private static Map<List<String>, Long[]> gridIndexConversions = new HashMap<List<String>, Long[]>();         // subset2gridIndex conversions
    
    private static Map<String, String> crsWKTCache = new HashMap<>();

    private static final Logger log = LoggerFactory.getLogger(CrsUtil.class);
    
    
    private static final String APPLICATION_PROPERTIES_FILE = "application.properties";
    private static final String KEY_SECORE_CONF_DIR = "secore.confDir";
    
    // NOTE: this is the hard-coded WKT of rorated CRS from https://github.com/Geomatys/MetOceanDWG/blob/main/MetOceanDWG%20Projects/Authority%20Codes%20for%20CRS/RotatedPole.xml
    private static final String COSMO_CRS_101_WKT = "GEODCRS[\"COSMO-DWD rotated pole grid\",\n" +
                                                    "  BASEGEODCRS[\"COSMO DWD base geodetic CRS (Spherical)\",\n" +
                                                    "    DATUM[\"COSMO DWD geodetic datum (Spherical)\",\n" +
                                                    "      ELLIPSOID[\"DWD Models Sphere\", 6371229.0, 0.0, LENGTHUNIT[\"metre\", 1]]],\n" +
                                                    "      PRIMEM[\"Greenwich\", 0.0, ANGLEUNIT[\"degree\", 0.017453292519943295]]],\n" +
                                                    "  DERIVINGCONVERSION[\"COSMO-DE pole rotation\",\n" +
                                                    "    METHOD[\"Pole rotation (netCDF CF convention)\"],\n" +
                                                    "    PARAMETER[\"Grid north pole latitude (netCDF CF convention)\", 40.0, ANGLEUNIT[\"degree\", 0.017453292519943295]],\n" +
                                                    "    PARAMETER[\"Grid north pole longitude (netCDF CF convention)\", -170.0, ANGLEUNIT[\"degree\", 0.017453292519943295]],\n" +
                                                    "    PARAMETER[\"North pole grid longitude (netCDF CF convention)\", 0.0, ANGLEUNIT[\"degree\", 0.017453292519943295]]],\n" +
                                                    "  CS[ellipsoidal, 2],\n" +
                                                    "    AXIS[\"Rotated latitude (B)\", north, ORDER[1]],\n" +
                                                    "    AXIS[\"Rotated longitude (L)\", east, ORDER[2]],\n" +
                                                    "    ANGLEUNIT[\"degree\", 0.017453292519943295],\n" +
                                                    "  SCOPE[\"Atmospheric model data.\"],\n" +
                                                    "  AREA[\"Central Germany.\"],\n" +
                                                    "  BBOX[47.00, 5.00, 55.00, 15.00],\n" +
                                                    "  ID[\"COSMO\", 101],\n" +
                                                    "  REMARK[\"Used with grid spacing of 0.025 degree in rotated coordinates.\"]]";
    
    private static boolean isSECOREloaded = false;
    
    /**
     * Invoke embedded SECORE in petascope before petascope starts to migrate 
     * 
     */
    public static void loadInternalSecore(boolean embedded, String webappsDir) throws IOException, org.rasdaman.secore.util.SecoreException, SecoreException {
        
        if (isSECOREloaded == false) {

            Properties properties = new Properties();
            InputStream resourceStream = new ClassPathResource(APPLICATION_PROPERTIES_FILE).getInputStream();
            properties.load(resourceStream);

            PropertySourcesPlaceholderConfigurer propertyResourcePlaceHolderConfigurer = new PropertySourcesPlaceholderConfigurer();
            File initialFile = new File(properties.getProperty(KEY_SECORE_CONF_DIR) + "/" + org.rasdaman.secore.ConfigManager.SECORE_PROPERTIES_FILE);
            propertyResourcePlaceHolderConfigurer.setLocation(new FileSystemResource(initialFile));

            String confDir = properties.getProperty(KEY_SECORE_CONF_DIR);
            try {
                org.rasdaman.secore.ConfigManager.initInstance(confDir, embedded, webappsDir);
                //  Create (first time load) or Get the BaseX database from caches.
                DbManager dbManager = DbManager.getInstance();

                // NOTE: we need to check current version of Secoredb first, if it is not latest, then run the update definition files with the current version to the newest versionNumber from files.
                // in $RMANHOME/share/rasdaman/secore.
                // if current version of Secoredb is empty then add SecoreVersion element to BaseX database and run all the db_updates files.
                DbSecoreVersion dbSecoreVersion = new DbSecoreVersion(dbManager.getDb());
                dbSecoreVersion.handle();
                log.debug("Initialzed BaseX dbs successfully.");
                
                isSECOREloaded = true;
            } catch (Exception ex) {
                throw new SecoreException(ExceptionCode.InternalComponentError, "Cannot initialize internal SECORE database manager. Reason: " + ex.getMessage(), ex);
            }
            
        }
    }
    
    public static void handleSecoreController(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        org.rasdaman.secore.controller.SecoreController controller = new org.rasdaman.secore.controller.SecoreController();
        controller.handleRequest(req, resp);
    }

    // Interface (unuseful now)
    /**
     * @return Indicates whether the CrsUtil object has already been created:
     * only DomainInterval/Points read the WCPS CRS specification, so CrsUtil is
     * called multiple times to transform e.g. a whole BBOX specification.
     */
    /*public CoordinateReferenceSystem getSourceCrs() {
        return sCrsID;
    }
    public CoordinateReferenceSystem getTargetCrs() {
        return tCrsID;
    }*/
    /**
     * Build a CRS HTTP URI.
     *
     * @param resolver The prefix of the resolver URL (http://<host>/def/)
     * @param committee The committee defining the CRS (e.g. EPSG)
     * @param version Version of the CRS
     * @param code Code of the CRS
     * @return The URI of the CRS (based on SECORE URL by default)
     */
    public static String CrsUri(String resolver, String committee, String version, String code) {
        return resolver + "/" + KEY_RESOLVER_CRS + "/" + committee + "/" + version + "/" + code;
    }

    public static String CrsUri(String committee, String version, String code) {
        return getResolverUri() + "/" + KEY_RESOLVER_CRS + "/" + committee + "/" + version + "/" + code;
    }

    public static String CrsUri(String committee, String code) {
        return CrsUri(committee, CRS_DEFAULT_VERSION, code);
    }

    // @Override: shortcut ".../auth/0/" for e.g. supportedCrs in WCS GetCapabilities response
    public static String CrsUriDir(String prefix, String committee, String version) {
        return prefix + "/" + KEY_RESOLVER_CRS + "/" + committee + "/" + version + "/";
    }

    public static String CrsUriDir(String prefix, String committee) {
        return CrsUriDir(prefix, committee, CRS_DEFAULT_VERSION);
    }

    public static String CrsUriDir(String committee) {
        return CrsUriDir(getResolverUri(), committee, CRS_DEFAULT_VERSION);
    }
    
    public static final String getEPSGVersion0CRS() {
        // used in WCS GetCapabilities to list all the possible CRSs
        String result = CrsUtil.getResolverUri() + "/crs/EPSG/0";
        return result;
    }

    /**
     * WCS GetCapabilities must contain list of EPSG CRSs as it is required in
     * OGC CRS Extension
     *
     * @return
     * @throws java.net.MalformedURLException
     */
    public static List<String> getAllEPSGCrss() throws Exception {
        String uri = CrsUtil.getEPSGVersion0CRS();        
        InputStream inStream = getInputStreamByInternalOrExternalSECORE(uri);

        String crssTmp = IOUtils.toString(inStream, ENCODING_UTF8);
        Matcher matcher = Pattern.compile(SECORE_IDENTIFIER_PATTERN).matcher(crssTmp);

        List<String> crss = new ArrayList<>();
        while (matcher.find()) {
            crss.add(matcher.group(1));
        }

        return crss;
    }

    /**
     * Return the datumOrigin from CRS definition by CRS uri
     *
     * @return
     */
    public static String getDatumOrigin(String crs) throws PetascopeException {
        String datumOrigin = getCrsDefinition(crs).getDatumOrigin();

        return datumOrigin;
    }
    
    /**
     * Check if the crs should go to the internal SECORE or not
     */
    public static boolean isInternalSecoreURL(String url) {
        if (url.contains(LOCALHOST + ":" + ConfigManager.EMBEDDED_PETASCOPE_PORT)) {
            // If SECORE url is localhost, then check it should be resolved internally by petascope or not
            String tmpCRS = url.replace(ConfigManager.DEFAULT_PETASCOPE_PORT, ConfigManager.EMBEDDED_PETASCOPE_PORT);
            return url.startsWith(ConfigManager.DEFAULT_SECORE_INTERNAL_URL) || url.startsWith(tmpCRS);
        }
        
        return false;
    }
    
    /**
     * Return the input stream by internal SECORE or external SECORE
     */
    private static InputStream getInputStreamByInternalOrExternalSECORE(String crsUri) throws Exception {
        InputStream result;
        crsUri = crsUri.replaceAll("\"", "%22");
        
        if (isInternalSecoreURL(crsUri)) {
            // If secore_url=interal -> query from embedded secore
            ResolveRequest request = new ResolveRequest(crsUri);
            ResolveResponse res = Resolver.resolve(request);
            String text = res.getData();
            result = IOUtils.toInputStream(text);
        } else {
            log.debug("# Checking external SECORE with url " + crsUri);
            // external secore, send requests normally
            result = HttpUtil.getInputStream(crsUri);
        }
        
        return result;
    }

    /**
     * Parser of GML definitions of Coordinate Reference Systems: parse the
     * specified (resolver) URI recursively and creates the CrsDefinition
     * object(s) along. Sliced axes list (e.g. <uri>@<not_sliced_axis>) is
     * dropped from the URI to keep it actionable (SECORE does not understand
     * this notation, nor it is standardized).
     *
     * @param givenCrsUri The URI of the /atomic/ CRS to be parsed (URI need to
     * be decomposed first).
     * @return The parsed CRS definition
     * @throws PetascopeException
     * @throws SecoreException
     */
    // (!!) Always use decomposeUri() output to feed this method: it currently understands single CRSs.
    public static CrsDefinition getCrsDefinition(String givenCrsUri) throws PetascopeException {
        CrsDefinition crs = null;
        List<List<String>> axes = new ArrayList<>();
        
        // replace any $SECORE_URL$ for some cases it still exists when running multiple DescribeCoverage in parallel
        givenCrsUri = CrsUtil.CrsUri.fromDbRepresentation(givenCrsUri);

        // Remove any possible slicing suffixes:
        givenCrsUri = givenCrsUri.replaceAll(SLICED_AXIS_SEPARATOR + ".*$", "").trim();
        // NOTE: as opengis.net/def/crs/EPSG/0 now is not SECORE anymore (without fully resolved CRS definition),
        // it will use the default version instead
        givenCrsUri = givenCrsUri.replace(OPENGIS_EPSG_URI, OPENGIS_EPSG_URI_DEFAULT_VERSION);

        // Check first if the definition is already in cache:
        if (CrsUri.isCached(givenCrsUri)) {
            return CrsUri.getCachedDefinition(givenCrsUri);
        }

        // Check if the URI syntax is valid
        if (!CrsUri.isValid(givenCrsUri)) {
            throw new PetascopeException(ExceptionCode.InvalidMetadata, "CRS '" + givenCrsUri + "' definition seems not valid.");
        }

        // Need to parse the XML
        String datumOrigin = ""; // for TemporalCRSs

        // Prepare fallback URIs in case of service unavailablilty of given resolver
        List<String> crsUris = new ArrayList<String>();
        crsUris.add(givenCrsUri);
        String lastUri = givenCrsUri;
        for (String resolverUri : ConfigManager.SECORE_URLS) {
            String fullUri = CrsUri(resolverUri,
                    CrsUri.getAuthority(givenCrsUri),
                    CrsUri.getVersion(givenCrsUri),
                    CrsUri.getCode(givenCrsUri));
            if (!crsUris.contains(fullUri)) {
                lastUri = fullUri;
                crsUris.add(lastUri);
            }
        }

        // Start parsing
        for (String crsUri : crsUris) {
            URL uomUrl = null;
            try {
                InputStream inStream = getInputStreamByInternalOrExternalSECORE(crsUri);

                // Build the document
                Document doc = XMLUtil.buildDocument(null, inStream);
                Element root = doc.getRootElement();

                // Catch some exception in the GML
                Element exEl = XMLUtil.firstChildRecursivePattern(root, ".*" + XMLSymbols.LABEL_EXCEPTION_TEXT);
                if (exEl != null) {
                    throw new SecoreException(ExceptionCode.ResolverError, "Failed to get CRS definition from URL '" + crsUri + "'. Given error message: " + exEl.getValue());
                }

                // Check if it exists:
                if (!root.getLocalName().matches(".*" + XMLSymbols.CRS_GMLSUFFIX)) {
                    throw new PetascopeException(ExceptionCode.InvalidMetadata, "CRS '" + crsUri + "' does not seem to be a CRS definition.");
                }

                // This value will be then stored in the CrsDefinition
                String crsType = root.getLocalName();
                log.debug("CRS element found: '" + crsType + "'.");

                // Get the *CS element: **don't** look recursive otherwise you can getinto the underlying geodetic CRS of a projected one (eg EPSG:32634)
                // e.g. ellipsoidalCS or CartesianCS element
                Element csEl = XMLUtil.firstChildPattern(root, ".*" + XMLSymbols.CS_GMLSUFFIX);
                // Check if it exists
                if (csEl == null) {
                    // NOTE: in case, e.g. ellipsoidalCS element doesn't exist as the first child of root element, but deep down in root's child elements,
                    // then try this recursive method before throwing error
                    // for COSMO 101 CRS, the real CRS gml:EllipsoidalCS inside gml:coordinateSystem element
                    Element coordinateSystemElement = XMLUtil.firstChildRecursive(root, XMLSymbols.LABEL_COORDINATE_SYSTEM);
                    csEl = XMLUtil.firstChildRecursive(coordinateSystemElement, XMLSymbols.LABEL_ELLIPSOIDALS_CS);
                    
                    if (csEl == null) {
                        throw new PetascopeException(ExceptionCode.InvalidMetadata, "CRS '" + crsUri + "' missing the Coordinate System element.");
                    }
                }
                log.debug("CS element found: " + csEl.getLocalName());

                // Skip optional association role [eg cartesianCS(CartesianCS)]
                if (XMLUtil.firstChildPattern(csEl, ".*" + XMLSymbols.CS_GMLSUFFIX) != null) {
                    csEl = XMLUtil.firstChildPattern(csEl, ".*" + XMLSymbols.CS_GMLSUFFIX);
                    log.debug("CS element found: " + csEl.getLocalName());
                }

                // Init CrsDefinition, then add axes later on
                crs = new CrsDefinition(
                        CrsUri.getAuthority(crsUri),
                        CrsUri.getVersion(crsUri),
                        CrsUri.getCode(crsUri),
                        crsType);

                List<Element> axesList = XMLUtil.getChildElements(csEl, XMLSymbols.LABEL_CRSAXIS);

                // Check if there is at least one axis definition
                if (axesList.isEmpty()) {
                    throw new PetascopeException(ExceptionCode.InvalidMetadata, "CRS '" + crsUri + "' missing the axis element(s).");
                }

                for (Element axisEl : axesList) {

                    // Get CoordinateSystemAxis mandatory element
                    Element csaEl = XMLUtil.firstChildRecursive(axisEl, XMLSymbols.LABEL_CSAXIS);
                    if (csaEl == null) {
                        throw new PetascopeException(ExceptionCode.InvalidMetadata, "CRS '" + crsUri + "' missing the CoordinateSystemAxis element.");
                    }

                    // Get abbreviation
                    Element axisAbbrevEl = XMLUtil.firstChildRecursive(csaEl, XMLSymbols.LABEL_AXISABBREV);
                    Element axisDirEl = XMLUtil.firstChildRecursive(csaEl, XMLSymbols.LABEL_AXISDIRECTION);

                    // Check if they are defined: otherwise exception must be thrown
                    if (axisAbbrevEl == null | axisDirEl == null) {
                        throw new PetascopeException(ExceptionCode.InvalidMetadata, "CRS '" + crsUri + "' misses  abbreviation and/or direction in axis definition.");
                    }
                    String axisAbbrev = axisAbbrevEl.getValue();
                    String axisDir = axisDirEl.getValue();

                    // Get the UoM of this axis
                    String uomName;
                    Attribute uomAtt = null;
                    for (int l = 0; l < csaEl.getAttributeCount(); l++) {
                        uomAtt = csaEl.getAttribute(l);
                        if (uomAtt.getLocalName().equals(XMLSymbols.ATT_UOM)) {
                            break;
                        }
                    }

                    // Check if it exists, otherwise set an empty UoM and throw a warning message
                    if (uomAtt == null) {
                        log.warn(crsUri + ": missing unit of measure in " + axisAbbrev + " axis definition: setting empty UoM.");
                        uomName = "";
                    } else {
                        // e.g. http://ows.rasdaman.org/def//uom/UCUM/0/d
                        String uomCrsUrlTmp = uomAtt.getValue().trim();

                        // UoM attribute can be either a String or as well a dereferenced definition (URL)
                        if (!uomCrsUrlTmp.startsWith(HTTP_PREFIX)) {
                            uomName = uomAtt.getValue().split(" ")[0]; // UoM is meant as one word only
                        } else {
                            // Need to parse a new XML definition
                            uomUrl = new URL(uomCrsUrlTmp);
                            try {
                                Element uomRoot = crsDefUrlToXml(uomCrsUrlTmp);
                                if (uomRoot != null) {

                                    // Catch some exception in the GML
                                    Element uomExEl = XMLUtil.firstChildRecursive(uomRoot, XMLSymbols.LABEL_EXCEPTION_TEXT);
                                    if (uomExEl != null) {
                                        throw new SecoreException(ExceptionCode.ResolverError, "UoM of CRS '" + crsUri + "' is not valid. Given: " + uomExEl.getValue());
                                    }

                                    // Get the UoM value
                                    Element uomNameEl = XMLUtil.firstChildRecursive(uomRoot, XMLSymbols.LABEL_NAME);
                                    if (uomNameEl == null) {
                                        throw new PetascopeException(ExceptionCode.InvalidMetadata, "UoM definition of CRS '" + crsUri + "' missses name element.");
                                    }
                                    uomName = uomNameEl.getValue().split(" ")[0]; // Some UoM might have further comments after actual UoM (eg EPSG:4326)
                                } else {
                                    uomName = extractUomNameFromUri(uomUrl);
                                }
                            } catch (Exception ex) {
                                // In case UOM CRS doesn't exist, just extract the uom label from the last part of the crs
                                uomName = extractUomNameFromUri(uomUrl);
                            }
                        }
                    }

                    log.debug("Axis element found: " + axisAbbrev + "[" + uomName + "]");
                    
                    // e.g: /def/uom/UCUM/0/s which doesn't exist in SECORE, only get the unit character: s - seconds
                    if (uomName.contains("UCUM/0/")) {
                        String[] tmps = uomName.split("/");
                        
                        // return s (seconds) only
                        uomName = tmps[tmps.length - 1];
                    }

                    // Add axis to the definition (temporarily first, then force XY order)
                    List<String> tmp = new ArrayList<String>();
                    tmp.addAll(Arrays.asList(axisDir, axisAbbrev, uomName));
                    axes.add(tmp);

                } // END axes loop

                // If this is a TemporalCRS definition, read the TemporalDatum's origin
                if (crsType.equals(XMLSymbols.LABEL_TEMPORALCRS)) {

                    Element datumEl = XMLUtil.firstChildRecursivePattern(root, ".*" + XMLSymbols.DATUM_GMLSUFFIX);
                    if (datumEl == null) {
                        throw new PetascopeException(ExceptionCode.InvalidMetadata, "CRS '" + crsUri + "' missing datum element.");
                    }

                    log.debug("Datum element found: '" + datumEl.getLocalName() + "'.");

                    // Get the origin of the datum
                    Element datumOriginEl = XMLUtil.firstChildRecursive(datumEl, XMLSymbols.LABEL_ORIGIN);
                    if (datumOriginEl == null) {
                        throw new PetascopeException(ExceptionCode.InvalidMetadata, "CRS '" + crsUri + "' missing the origin of the datum.");
                    }
                    datumOrigin = datumOriginEl.getValue();

                    // Add datum origin to the definition object
                    crs.setDatumOrigin(datumOrigin);
                    log.debug("Found datum origin: " + datumOrigin);

                } // else: no need to parse the datum
                break; // fallback only on IO problems
            } catch (MalformedURLException ex) {
                log.error("Malformed URI: " + ex.getMessage());
                throw new PetascopeException(ExceptionCode.IOConnectionError, ex);
            } catch (ValidityException ex) {
                throw new PetascopeException(ExceptionCode.InternalComponentError,
                        (null == uomUrl ? crsUri : uomUrl) + " definition is not valid. Reason: " + ex.getMessage(), ex);
            } catch (ParsingException ex) {
                String errorMessage = ex.getMessage() + "\n at line " + ex.getLineNumber() + ", column " + ex.getColumnNumber();
                throw new PetascopeException(ExceptionCode.InternalComponentError,
                        (null == uomUrl ? crsUri : uomUrl) + " definition is malformed. Reason: " + errorMessage, ex);
            } catch (IOException ex) {
                if (crsUri.equals(lastUri) || null != uomUrl) {
                    throw new PetascopeException(ExceptionCode.InternalComponentError,
                            (null == uomUrl ? crsUri : uomUrl) + " has IO error. Reason: " + ex.getMessage(), ex);
                } else {
                    log.warn("Connection problem with " + (null == uomUrl ? crsUri : uomUrl) + ": " + ex.getMessage());
                    log.warn("Attempting to fetch the CRS definition via fallback resolver.");
                }
            } catch (Exception ex) {
                throw new PetascopeException(ExceptionCode.InternalComponentError,
                        (null == uomUrl ? crsUri : uomUrl) + " has general exception while parsing definition. Reason: " + ex.getMessage(), ex);
            }
        }

        /* ISSUE: order in rasdaman in always easting then northing, but
         * a CRS definition (e.g. EPSG:4326) might define Latitude as first axis.
         * In case of spatial axis: force easting first, always.
         * See also System.setProperty() in petascope.PetascopeInterface.java.
         * Trace of true order remains in the CrsDefinition.
         */
        //forceXYorder(crs, axes);
        for (List<String> axisMetadata : axes) {
            // All metadata of this axis is parsed: add it to CrsDefinition:
            crs.addAxis(axisMetadata.get(0), axisMetadata.get(1), axisMetadata.get(2));
        }

        // Cache the crs definition by the CRS URI
        parsedCRSs.put(givenCrsUri, crs);
        log.trace("CRS URI {} is added into cache for future (inter-requests) use.", givenCrsUri);

        return crs;
    }

    /**
     * Try to build XML elements tree from CRS URI (try with all provided SECORE
     * configurations in petascope.properties if it still returns NULL for
     * elements tree)
     *
     * @param url
     * @return Element
     * @throws MalformedURLException
     * @throws IOException
     * @throws ParsingException
     */
    public static Element crsDefUrlToXml(final String url) throws Exception {
        Element ret = crsDefUrlToDocument(url);
        if (ret == null) {
            for (String configuredResolver : ConfigManager.SECORE_URLS) {
                String newUrl = CrsUri.replaceResolverInUrl(url, configuredResolver);
                ret = crsDefUrlToDocument(newUrl);
                if (ret != null) {
                    break;
                }
            }
        }
        return ret;
    }

    /**
     * Check if axisCrs of coverage is gridCrs (CRS:1)
     *
     * @param axisCrs
     * @return
     */
    public static boolean isGridCrs(String axisCrs) {
        if (axisCrs.equals(CrsUtil.GRID_CRS)) {
            return true;
        }
        return false;
    }

    /**
     * Check if axisCrs of coverage is IndexCRS (IndexND)
     *
     * @param axisCrs
     * @return
     */
    public static boolean isIndexCrs(String axisCrs) {
        // strip the crsID (e.g: 4326, Index3D from a CRS URL)
        String crsID = axisCrs.substring(axisCrs.lastIndexOf("/") + 1);
        if (crsID.contains(INDEX_CRS_PREFIX)) {
            return true;
        }
        return false;
    }
    
    /**
     * e.g: http://localhost:8080/def/crs/EPSG/0/4326
     * -> http://opengis.net/def/crs/EPSG/0/4326
     */
    public static String createOpenGisUrl(String crs) {
        String tmp = crs.split("/" + SECORE_CONTEXT_PATH)[1];
        String result = OPENGIS_URI_PREFIX + "/" + SECORE_CONTEXT_PATH + tmp;
        
        return result;
    }

    /**
     * Building a XML elements tree from a CRS URI
     *
     * @param url
     * @return Element
     * @throws MalformedURLException
     */
    private static Element crsDefUrlToDocument(final String url) throws Exception {
        Element ret = null;
        try {                                  
            // Get the CRS URI definition from SECORE to build Element
            InputStream inStream = getInputStreamByInternalOrExternalSECORE(url);               
            Document doc = XMLUtil.buildDocument(null, inStream);
            ret = doc.getRootElement();
        } catch (IOException | ParsingException | PetascopeException ex) {
            log.warn("Error while building the document from URL '" + url + "'", ex);
            ret = null;
        }
        return ret;
    }

    /**
     * Extracts the UoM name from the URI as last field in the RESTful . 10^0 as
     * UoM for pure numbers is otherwise returned.
     *
     * @param uomUrl
     * @return The UoM label for the URI.
     */
    private static String extractUomNameFromUri(URL uomUrl) {
        String uomName = PURE_UOM;
        String uomPath = uomUrl.getPath();
        if (null == uomPath) {
            log.warn(uomUrl + " does not have a query part: fallback to dimensionless axis.");
        } else {
            Pattern p = Pattern.compile(CrsUri.LAST_PATH_PATTERN);
            Matcher m = p.matcher(uomPath);
            while (m.find()) {
                if (m.groupCount() == 1) {
                    uomName = m.group(1);
                    log.debug("Extracted name of the UoM from " + uomUrl + " is " + uomName + ".");
                } else {
                    log.warn("Cannot extract name of UoM from " + uomUrl + " with pattern \'" + CrsUri.LAST_PATH_PATTERN + "\'.");
                    log.warn("Will use whole URL as UoM name.");
                    uomName = uomUrl.toString();
                }
            }
        }
        return uomName;
    }

    /**
     * Discover which is the order of the specified (CRS) axis in the specified
     * ordered list of CRSs (compound CRS). If the axis is not present in the
     * (C)CRS, then -1 is returned. NOTE: use CrsUri.decomposeUri() to get a
     * list of single CRS URIs from a compound CRS.
     *
     * @param crsUris An ordered list of single CRS URIs
     * @param axisAbbrev The CRS axis label (//CoordinateSystemAxis/axisAbbrev)
     * @return The order of `axisAbbrev' axis in the (C)CRS [0 is first], -1
     * otherwise.
     * @throws PetascopeException
     * @throws SecoreException
     */
    public static Integer getCrsAxisOrder(List<String> crsUris, String axisAbbrev) throws PetascopeException, SecoreException {

        // init
        CrsDefinition crsDef;
        Integer counter = 0;

        // scan the CRS axes
        for (String singleCrs : crsUris) {
            crsDef = CrsUtil.getCrsDefinition(singleCrs); // cache is used, no SECORE access here
            for (CrsDefinition.Axis crsAxis : crsDef.getAxes()) {
                if (!CrsUri.isSliced(singleCrs, crsAxis.getAbbreviation())) {
                    if (crsAxis.getAbbreviation().equals(axisAbbrev)) {
                        return counter;
                    }
                    counter += 1;
                }
            }
        }

        return -1;
    }

    /**
     * Discover which is the label of the specified (CRS) axis in the specified
     * ordered list of CRSs (compound CRS). If the axis is not present in the
     * (C)CRS, then an empty String is returned. NOTE: use CrsUri.decomposeUri()
     * to get a list of single CRS URIs from a compound CRS.
     *
     * @param crsUris An ordered list of single CRS URIs
     * @param axisOrder The order of the axis (//CoordinateSystemAxis) in the
     * (C)CRS [0 is first]
     * @return The label (//CoordinateSystemAxis/axisAbbrev) of the
     * "axisOrder"-th axis in the (C)CRS, an empty String otherwise.
     * @throws PetascopeException
     * @throws SecoreException
     */
    public static String getAxisLabel(List<String> crsUris, Integer axisOrder) throws PetascopeException, SecoreException {

        // init
        Integer counter = 0;
        CrsDefinition crsDef;

        // scan the CRS axes
        for (String singleCrs : crsUris) {
            crsDef = CrsUtil.getCrsDefinition(singleCrs);
            for (CrsDefinition.Axis crsAxis : crsDef.getAxes()) {
                if (!CrsUri.isSliced(singleCrs, crsAxis.getAbbreviation())) {
                    if (counter == axisOrder) {
                        return crsAxis.getAbbreviation();
                    }
                    counter += 1;
                }
            }
        }

        // Axis was not found: return empty String
        return "";
    }

    // Overload for a single URI
    public static String getAxisLabel(String singleCrsUri, Integer axisOrder) throws PetascopeException, SecoreException {
        return getAxisLabel(new ArrayList<String>(Arrays.asList(new String[]{singleCrsUri})), axisOrder);
    }

    /**
     * Get an ordered list of axis labels for a CCRS.
     *
     * @param crsUris An ordered list of single CRS URIs
     * @return The ordered list of labels (//CoordinateSystemAxis/axisAbbrev) of
     * the (C)CRS.
     * @throws PetascopeException
     * @throws SecoreException
     */
    public static List<String> getAxesLabels(List<String> crsUris) throws PetascopeException, SecoreException {

        // init
        List<String> axesLabels = new ArrayList<String>(getTotalDimensionality(crsUris));
        CrsDefinition crsDef;

        // scan the CRS axes
        for (String singleCrs : crsUris) {
            crsDef = CrsUtil.getCrsDefinition(singleCrs);
            for (CrsDefinition.Axis crsAxis : crsDef.getAxes()) {
                if (!CrsUri.isSliced(singleCrs, crsAxis.getAbbreviation())) {
                    axesLabels.add(crsAxis.getAbbreviation());
                }
            }
        }

        return axesLabels;
    }
    
    
    /**
     * Return a map of axis labels and their types
     * e.g: Lat -> Y, Long -> X
     */
    public static Map<String, String> getAxisLabelsTypesMap(String singleCrsURI) throws PetascopeException, SecoreException {
        CrsDefinition crsDef = CrsUtil.getCrsDefinition(singleCrsURI);
        
        Map<String, String> map = new HashMap<>();
        for (CrsDefinition.Axis crsAxis : crsDef.getAxes()) { 
            map.put(crsAxis.getAbbreviation(), crsAxis.getType());
        }
        
        return map;
    }

    // Overload for single URI
    public static List<String> getAxesLabels(String singleCrsUri) throws PetascopeException, SecoreException {
        return getAxesLabels(new ArrayList<String>(Arrays.asList(new String[]{singleCrsUri})));
    }
    
    /**
     * From coverage's CRS and axis's index in coverage, return axis type.
     * e.g: CRS is AnsiDate&EPSG:4326 (3 Axes: time, Lat, Long)
     * and index is 2 (axis Long) then axis type will be X (from EPSG:4326 CRS definition).
     */
    public static String getAxisTypeByIndex(String coverageCRS, int axisIndex) throws PetascopeException {
        List<String> crss = CrsUri.decomposeUri(coverageCRS);
        
        String axisType = AxisTypes.UNKNOWN;
        
        int i = 0;
        
        for (String crs : crss) {
            CrsDefinition crsDefinition = CrsUtil.getCrsDefinition(crs);
            
            if (axisIndex < i + crsDefinition.getAxes().size()) {
                int normalizedIndex = axisIndex - i;
                axisType = crsDefinition.getAxes().get(normalizedIndex).getType();
                break;
            }
            i = i + crsDefinition.getAxes().size();
        }
        
        return axisType;
    }

    /**
     * Get an ordered list of axis UoMs for a CCRS.
     *
     * @param crsUris An ordered list of single CRS URIs
     * @return The ordered list of UoM (//CoordinateSystemAxis/@uom) of the
     * (C)CRS.
     * @throws PetascopeException
     * @throws SecoreException
     */
    public static List<String> getAxesUoMs(List<String> crsUris) throws PetascopeException, SecoreException {

        // init
        List<String> axesUoMs = new ArrayList<>(getTotalDimensionality(crsUris));
        CrsDefinition crsDef;

        // scan the CRS axes
        for (String singleCrs : crsUris) {
            crsDef = CrsUtil.getCrsDefinition(singleCrs);
            for (CrsDefinition.Axis crsAxis : crsDef.getAxes()) {
                if (!CrsUri.isSliced(singleCrs, crsAxis.getAbbreviation())) {
                    axesUoMs.add(crsAxis.getUoM());
                }
            }
        }

        return axesUoMs;
    }

    // Overload for single URI
    public static List<String> getAxesUoMs(String singleCrsUri) throws PetascopeException, SecoreException {
        return getAxesUoMs(new ArrayList<String>(Arrays.asList(new String[]{singleCrsUri})));
    }

    /**
     * Counts the number of axes involved in a CCRS composition.
     *
     * @param crsUris An ordered list of single CRS URIs
     * @return The sum of dimensionalities of each single CRS.
     * @throws PetascopeException
     * @throws SecoreException
     */
    public static Integer getTotalDimensionality(List<String> crsUris) throws PetascopeException, SecoreException {

        // init
        Integer counter = 0;
        CrsDefinition crsDef;

        // scan the CRS axes
        for (String singleCrs : crsUris) {
            crsDef = CrsUtil.getCrsDefinition(singleCrs);
            for (String axisLabel : crsDef.getAxesLabels()) {
                counter += CrsUri.isSliced(singleCrs, axisLabel) ? 0 : 1;
            }
        }

        return counter;
    }

    // Overload for single URI
    public static Integer getTotalDimensionality(String singleCrsUri) throws PetascopeException, SecoreException {
        return getTotalDimensionality(new ArrayList<String>(Arrays.asList(new String[]{singleCrsUri})));
    }

    /**
     * Deduces the updated (C)CRS URI from a given (C)CRS where 1+ axes have to
     * be removed (sliced out).
     *
     * @param crsUris An ordered list of single CRS URIs
     * @param slicedAxes The unordered list of axes to be sliced
     * @return An updated (C)CRS URI where CRSs have been either removed (all
     * its axes were sliced), partially sliced, or left as-is.
     * @throws PetascopeException
     * @throws SecoreException
     */
    public static String sliceAxesOut(List<String> crsUris, Set<String> slicedAxes)
            throws PetascopeException, SecoreException {

        // init
        Map<Integer, String> orderedSlicedAxes = new TreeMap<Integer, String>(); // {Axis Order in Coverage --> Axis Abbreviation}
        Integer order;
        String slicedCrsUri = "";

        // order the axes as they are in the CRS definition (subsets in a W*S request are an unordered set)
        for (String axisLabel : slicedAxes) {
            order = getCrsAxisOrder(crsUris, axisLabel); // axis order
            if (order >= 0) {
                orderedSlicedAxes.put(order, axisLabel);
            } else {
                log.warn("Axis '" + axisLabel + "' is not defined by '" + CrsUri.createCompound(crsUris) + "'.");
            }
        }

        if (orderedSlicedAxes.isEmpty()) {
            // No axis needs tobe sliced out: just compose the CCRS out of the input uris
            slicedCrsUri = CrsUri.createCompound(crsUris);

        } else {

            // Prepare a map of CRS uris along with the ordered list of axis labels, that will be updated at every slice subset:
            // a CRS must be either sliced (%/<axisLabel1>[/<axisLabel2>...])or removed from `gml:srsName' in case all its axes are sliced out.
            Map<String, List<String>> crsAxes = new LinkedHashMap<String, List<String>>(crsUris.size()); // keep insertion order
            for (String singleCrs : crsUris) {
                List<String> labels = new ArrayList<String>(getCrsDefinition(singleCrs).getDimensions());
                // Get the list of axis labels of this URI and update the Map
                labels.addAll(getCrsDefinition(singleCrs).getAxesLabels());
                crsAxes.put(singleCrs, labels);
            }

            // Now decrease the axes number at every slice:
            for (Map.Entry orderLabel : orderedSlicedAxes.entrySet()) {
                for (String singleCrs : crsUris) {
                    List<String> crsAxesLabels = getAxesLabels(singleCrs);
                    String thisLabel = (String) orderLabel.getValue();
                    if (crsAxesLabels.contains(thisLabel)) {
                        // This CRS contains the sliced axis: remove it from the Map
                        List<String> updatedLabels = crsAxes.get(singleCrs);
                        updatedLabels.remove(thisLabel);
                        crsAxes.put(singleCrs, updatedLabels); // old value is just overwritten
                    }
                }
            }

            // Build-up the new (C)CRS uri:
            List<String> updatedSingleUris = new ArrayList<String>();
            for (Map.Entry uriLabels : crsAxes.entrySet()) {
                String uri = (String) uriLabels.getKey();
                List<String> axesLabels = (List<String>) uriLabels.getValue();

                if (axesLabels.isEmpty()) {
                    log.debug(uri + " CRS will be removed from the output URI.");
                } else if ((axesLabels.size() > 0) && (axesLabels.size() < getTotalDimensionality(uri))) {
                    log.debug(uri + " has been sliced.");
                    updatedSingleUris.add(CrsUri.buildSlicedUri(uri, axesLabels));
                } else if (axesLabels.size() == getTotalDimensionality(uri)) {
                    log.debug(uri + " has not been sliced at all: keep it as-is.");
                    updatedSingleUris.add(uri);
                } else if (axesLabels.size() > getTotalDimensionality(uri)) {
                    log.error(uri + " is left with a non-positive number of axis: something went wrong.");
                }
            }

            // Create the updated (C)CRS:
            slicedCrsUri = CrsUri.createCompound(updatedSingleUris);
        }

        return slicedCrsUri;
    }

    /**
     * Method to return the default SECORE URI (first in the configuration list)
     */
    public static String getResolverUri() {
        return ConfigManager.SECORE_URLS.get(0);
    }
    
    public static String getEPSG4326FullURL() {
        return getResolverUri() + "/crs/EPSG/0/4326";
    }

    /**
     * Return the opengis full uri for EPSG code, e.g: EPSG:4326 ->
     * http://www.opengis.net/def/crs/EPSG/0/4326
     */
    public static String getFullCRSURLByAuthorityCode(String authorityCode) {
        // e.g. EPSG:4326
        String[] tmps = authorityCode.split(":");
        // e.g. EPSG
        String authortiy = tmps[0];
        // e.g. 4326
        String code = tmps[1];
        
        // e.g. http://localhost:8080/def/crs/EPSG/0/4326
        return getResolverUri() + "/crs/" + authortiy + "/0/" + code;
    }
    
    /**
     * e.g: 4326 -> localhost:8080/def/crs/EPSG/0/
     */
    public static String getEPSGFullUriByCode(String code) {
        return getEPSGVersion0CRS() + "/" + code;
    }
 
    /**
     * Ultility to get the code from CRS (e.g: EPSG:4326 -> 4326)
     */
    public static String getCode(String crs) {
        if (crs.contains(":")) {
            return crs.split(":")[1];
        }
        return CrsUri.getCode(crs);
    }

    /**
     * Given a CRS URL or Authority:Code, return authority:code
     * e.g: http://localhost:8080/def/crs/EPSG/0/4326 -> EPSG:4326
     */
    public static String getAuthorityCode(String crs) {
        crs = crs.trim();
        
        if (isAuthorityCode(crs)) {
            return crs;
        }
        
        String prefix = "/crs/";
        String[] values = crs.substring(crs.indexOf(prefix), crs.length()).replace(prefix, "").split("/");
        String result = values[0] + ":" + values[2];
        return result;
    }    
    
    /**
     * Given a CRS URL, return the WKT content of this CRS
     * 
     * @param crs a CRS URL or AuthorityCode e.g. EPSG:4326
     */
    public static String getWKT(String crs) throws PetascopeException {
        if (!CrsProjectionUtil.isValidTransform(crs)) {
            return null;
        }
        
         // OGRSpatialReference is expensive, cache CRS
        if (crsWKTCache.containsKey(crs)) {
            return crsWKTCache.get(crs);
        }
        
        String wkt = null;
        SpatialReference spatialReference = new SpatialReference();
        
        // e.g. EPSG:4326 or COSMO:101
        String authorityCode = getAuthorityCode(crs);
        if (authorityCode.contains(EPSG_AUTH)) {
            // e.g. EPSG:4326 -> 4326
            int epsgCode = Integer.valueOf(authorityCode.split(":")[1]);
            spatialReference.ImportFromEPSG(epsgCode);
            wkt = spatialReference.ExportToWkt();
        } else if (authorityCode.contains(COSMO_101_AUTHORITY_CODE)) {
            // COSMO:101
            wkt = getWKTCOSMO101CRS();
        } else {
            throw new PetascopeException(ExceptionCode.NoApplicableCode, "Getting WKT from CRS '" + crs + "' is not supported.");
        }
        
        String result = wkt.trim();
        crsWKTCache.put(crs, result);
        
        return result;
    }
    
    /**
     * Check if two input CRSs (AuthorityCode) or fullCRS have the same WKT string
     */
    public static boolean equalsWKT(String sourceWKT, String targetWKT) throws PetascopeException {
        return StringUtils.normalizeSpace(sourceWKT).equalsIgnoreCase(targetWKT);
    }
    
    /**
     * Get the WKT for rotated CRS COSMO 101 (Juelich) CRS.
     * @TODO: it is hard-coded for now as no possibility for converting GML to
     */
    private static String getWKTCOSMO101CRS() {
        return COSMO_CRS_101_WKT;
    }
    
    /**
     * Given a CRS URL, return a EPSG:code (e.g. EPSG:4326) or a WKT of the CRS (for non-EPSG CRS)
     * if possible of the input CRS.
     */
    public static String getAuthorityEPSGCodeOrWKT(String crs) throws PetascopeException {
        // e.g. EPSG:4326 or COSMO:101
        String authorityCode = getAuthorityCode(crs);
        String result = null;
        if (authorityCode.contains(EPSG_AUTH)) {
            result = authorityCode;
        } else if (authorityCode.contains(COSMO_AUTH)) {
            result = getWKTCOSMO101CRS();
        } else {
            throw new PetascopeException(ExceptionCode.NoApplicableCode, "Not supported for getting EPSG authority code or WKT from CRS '" + crs + "'.");
        }
        
        return result;
    }

    /**
     * return true if both axis labels are longitude axes
     */
    private static boolean isLongitudeAxis(String axisLabel1, String axisLabel2) {
        if (axisLabel1.equals(LONGITUDE_AXIS_LABEL_EPGS_VERSION_85) 
           || axisLabel1.equals(LONGITUDE_AXIS_LABEL_EPGS_VERSION_0)) {
            if (axisLabel2.equals(LONGITUDE_AXIS_LABEL_EPGS_VERSION_85)
                || axisLabel2.equals(LONGITUDE_AXIS_LABEL_EPGS_VERSION_0)) {
               return true;
            }
        }
                
        return false;
    }
    
    /**
     * From a map of axis labels and types (e.g: {"Lat": -> Y, "Lon" -> X}
     * return the axis type of an aixs label (e.g: "Long")
     */
    public static String getAxisTypeFromMap(Map<String, String> axisLabelsTypesMap, String axisLabel) throws PetascopeException {
        String axisType = axisLabelsTypesMap.get(axisLabel);
        
        if (axisType == null) {
            if (axisLabel.equals(LONGITUDE_AXIS_LABEL_EPGS_VERSION_85)) {
                axisType = axisLabelsTypesMap.get(LONGITUDE_AXIS_LABEL_EPGS_VERSION_0);
            }
            
            if (axisType == null) {
                throw new PetascopeException(ExceptionCode.InvalidRequest, 
                                            "Axis label '" + axisLabel + "' does not exist in the map of axis labels and types");
            }
        }
        
        return axisType;
    }
    
    /**
     * Check if 2 CRSs are matched
     * e.g: http://opengis.net/def/OGC/0/AnsiDate
     * and  http://localhost:8080/def/crs/OGC/O/AnsiDate?axis-label="d"
     * 
     * are equivalent
     */
    public static boolean crsURIsMatch(String crsURI1, String crsURI2) {
        String strippedCRS1 = CrsUtil.CrsUri.toDbRepresentation(crsURI1).split("\\?")[0];
        String strippedCRS2 = CrsUtil.CrsUri.toDbRepresentation(crsURI2).split("\\?")[0];
        
        return strippedCRS1.equals(strippedCRS2);        
    }
    
    /**
     * Return true if aixs labels have same name ("Long" or "Lon" is also accepted).
     */
    public static boolean axisLabelsMatch(String axisLabel1, String axisLabel2) {
        if (axisLabel1.equals(axisLabel2)
           || isLongitudeAxis(axisLabel1, axisLabel2)) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Check if input string contains Authority:CODE pattern (e.g: EPSG:4326)
     */
    public static boolean isAuthorityCode(String input) {
        return input.contains(":") && !input.contains("/");
    }
    
    /**
     * Check if CRS definition is XY axes order (e.g: EPSG:3857) or YX axes order (e.g: EPSG:4326)
     * @param uri URL to CRS definition from SECORE
     */
    public static boolean isXYAxesOrder(String uri) throws PetascopeException {
        if (isAuthorityCode(uri)) {
            // e.g: EPSG:4326 -> http://localhost:8080/def/crs/EPSG/0/4326
            uri = getFullCRSURLByAuthorityCode(uri);
        }
        List<CrsDefinition.Axis> axes = new ArrayList<>();
        axes = CrsUtil.getCrsDefinition(uri).getAxes();
        if (axes.isEmpty()) {
            throw new PetascopeException(ExceptionCode.InvalidMetadata, "CRS does not contain any axis. Given: '" + uri + "'.");
        } else if (axes.get(0).getType().equals(AxisTypes.Y_AXIS)) {
            // YX order
            return false;
        }
        
        // XY order
        return true;
    }
    
    /**
     * Check if axis is type X
     */
    public static boolean isXAxis(String axisType) {
        return axisType.equals(AxisTypes.X_AXIS);
    }
    
    /**
     * Check if axis is type Y
     */
    public static boolean isYAxis(String axisType) {
        return axisType.equals(AxisTypes.Y_AXIS);
    }
    
    /**
     * Check if axis is type X or Y
     */
    public static boolean isXYAxis(String axisType) {
        return isXAxis(axisType) || isYAxis(axisType);
    }
    
    public static boolean isTimeAxis(String axisType) {
        return axisType.equals(AxisTypes.T_AXIS);
    }
    
    /**
     * e.g. if input crsURL is http://localhost:8080/def/crs/EPSG/0/4326 -> http://localhost:8080/rasdaman/def/crs/EPSG/0/4326
     */
    public static String replaceOldURLWithNewURL(String crsURL) {
        String result = crsURL;
        
        if (result != null) {
            Matcher m = localhostPattern.matcher(crsURL);
            if (m.find()) {
                result = result.replace("/def", "/rasdaman/def");
            }
        }
        
        return result;
    }
    
    /**
     * Return X and Y crs axes from a CRS URI.
     * NOTE: The axes order is dependent from the CRS definition.
     * e.g: EPSG:4326 returns Lat, Long axes (YX order)
     *      EPSG:32632 returns E, N (XY order)
     */
    public static List<CrsDefinition.Axis> getXYAxes(String crsURI) throws PetascopeException, SecoreException {
        List<CrsDefinition.Axis> axes = CrsUtil.getCrsDefinition(crsURI).getAxes();
        List<CrsDefinition.Axis> results = Arrays.asList(null, null);
        
        for (CrsDefinition.Axis axis : axes) {
            if (isXAxis(axis.getType())) {
                results.set(0, axis);
            } else if (isYAxis(axis.getType())) {
                results.set(1, axis);
            }
        }
        
        return results;        
    }
    
    /**
     * Check if user has secore_urls=http://localhost:8080/def in petascope.properties
     * to migrate to secore_urls=internal
     * 
     */
    public static void checkSECOREURLsForInternalMigration() throws PetascopeException {
        
        
        // e.g. http://localhost:8080/rasdaman/def
        final String internalSECOREURL = ConfigManager.getInstance(ConfigManager.CONF_DIR).getInternalSecoreURL();
        
        for (int i = 0; i < ConfigManager.SECORE_URLS.size(); i++) {
            String url = ConfigManager.SECORE_URLS.get(i);
            
            if (url.trim().equals(SECORE_INTERNAL)) {
                ConfigManager.SECORE_URLS.set(i, internalSECOREURL);
            } else {
                Matcher m = localhostPattern.matcher(url);
                if (m.find()) {
                    ConfigManager.SECORE_URLS.set(i, internalSECOREURL);
                    log.warn("Please set secore_urls=internal in petascope.properties, as http://localhost:8080/def is not valid anymore.");
                }
            }
        }
    }

    /**
     * Nested class to offer utilities for CRS *URI* handling.
     */
    public static class CrsUri {

        /**
         * SECORE keyword used in legacy PS_CRS table to be replaces with the
         * first configured resolver. NOTE: Don't store the whole URI with fixed
         * SECORE endpoint (e.g: http://localhost:8080/def/crs/epsg/0/4326) as
         * if one does not host SECORE in localhost:8080 and changes in
         * petascope.propeties with SECORE http://abc.com/def/crs/epsg/0/4326,
         * the stored URI with localhost will be not found.
         */
        public static final String SECORE_URL_PREFIX = "$SECORE_URL$";
        public static final String LAST_PATH_PATTERN = ".*/(.*)$";

        private static final String COMPOUND_SPLIT = "(\\?|&)\\d+=";
        private static final String COMPOUND_PATTERN = "^" + HTTP_URL_PATTERN + KEY_RESOLVER_CCRS;

        private static final String AUTHORITY_KEY = "authority";    // Case-insensitivity is added in the pattern
        private static final String VERSION_KEY = "version";
        private static final String CODE_KEY = "code";
        //private static final String FORMAT_KEY    = "format";

        private static final String KV_PAIR = "(((?i)" + AUTHORITY_KEY + ")=[^&]+|"
                + "((?i)" + CODE_KEY + ")=(.+)|"
                + //"((?i)" + FORMAT_KEY     + ")=[^&]+|" +  // Add 1 KV_PAIR here below when enabled.
                "((?i)" + VERSION_KEY + ")=(.+))";
        private static final String KVP_CRS_PATTERN = "^"
                + HTTP_URL_PATTERN + KEY_RESOLVER_CRS + "\\?"
                + KV_PAIR + "&" + KV_PAIR + "&" + KV_PAIR + "$";

        private static final String REST_CRS_PATTERN = "^"
                + HTTP_URL_PATTERN + KEY_RESOLVER_CRS + "/[^/]+/.+/.+$";

        public static final String RESOLVER_PREFIX_END = "/def";

        // In case the URI represents a CCRS, it return the list of atomic CRS it represents.
        // NOTE: consistency of the URI should be first evaluated with isValid().
        /**
         * In case the URI represents a CCRS, it returns the list of atomic CRS
         * it represents.
         *
         * @param uri
         * @return The list of atomic URIs that form the CCRS, with one element
         * in case uri is already atomic.
         */
        public static List<String> decomposeUri(String uri) {
            String decUri = StringUtil.urldecode(uri, null);
            List<String> crss = new ArrayList<>();

            if (isCompound(decUri)) {
                String[] splitted = decUri.split(COMPOUND_SPLIT);
                if (splitted.length <= 1) {
                    log.warn(decUri + " seems invalid: check consitency first.");
                }
                if (splitted.length == 2) {
                    log.warn(decUri + " seems compound but only one CRS is listed.");
                }
                // The first element of the splitted String is the definition prefix: ignore it.
                for (int i = 0; i < splitted.length; i++) {
                    if (i > 0) {
                        crss.add(splitted[i]);
                    }
                }
            } else if (!uri.isEmpty()) {
                crss.add(uri);
            }

            return crss;
        }

        private static String replaceResolverInUrl(String url, String newResolver) {
            String ret = url;
            int resolverPrefixEndIndex = url.indexOf(RESOLVER_PREFIX_END);
            if (resolverPrefixEndIndex >= 0) {
                String oldResolver = url.substring(0, resolverPrefixEndIndex + RESOLVER_PREFIX_END.length());
                ret = url.replaceAll(oldResolver, newResolver);
            }
            return ret;
        }

        /**
         * Checks if a URI is compound.
         *
         * @param uri
         * @return True if uri is compound
         */
        public static boolean isCompound(String uri) {
            Pattern p = Pattern.compile(COMPOUND_PATTERN);
            Matcher m = p.matcher(StringUtil.urldecode(uri, null));
            while (m.find()) {
                return true;
            }
            return false;
        }

        /**
         * Checks if a specified URI (or an equivalent one) has already been
         * cached.
         *
         * @param uri
         * @return True if uri's definition has already been parsed and cached.
         * @throws PetascopeException
         * @throws SecoreException
         */
        public static boolean isCached(String uri) throws PetascopeException {
            if (parsedCRSs.containsKey(uri)) {
                return true;
            }
            log.trace(uri + " CRS needs to be parsed via resolver.");
            return false;
        }

        /**
         * Returns true if a specified URI is key-value paired. It works with
         * atomic CRSs (use decomposeUri first).
         *
         * @param uri
         * @return True if uri is key-value paired
         */
        public static boolean isKvp(String uri) {
            Pattern pKvp = Pattern.compile(KVP_CRS_PATTERN);
            Matcher m = pKvp.matcher(StringUtil.urldecode(uri, null));
            while (m.find()) {
                return true;
            }
            return false;
        }

        /**
         * Returns true if a specified URI is RESTful. It works with atomic CRSs
         * (use decomposeUri first).
         *
         * @param uri
         * @return True if uri is RESTful.
         */
        public static boolean isRest(String uri) {
            Pattern pRest = Pattern.compile(REST_CRS_PATTERN);
            Matcher m = pRest.matcher(StringUtil.urldecode(uri, null));
            while (m.find()) {
                return true;
            }
            return false;
        }

        /**
         * Checks if an URI, compound or not, is consistent.
         *
         * @param uri
         * @return true if it is a valid CRS URI.
         */
        // TODO: use SECORE.
        public static boolean isValid(String uri) {
            List<String> crss = decomposeUri(uri);

            if (crss.isEmpty()) {
                return false;
            }
            for (String current : crss) {
                if (current.equals(GRID_CRS)) {
                    return true;
                } // TODO: if CRS:1 is replaces by URI, need 'areEquivalent() instead
                if (isKvp(current) || isRest(current)) {
                    // Check if authority is supported (SECORE is now bottleneck) as well:
                    // --> http://kahlua.eecs.jacobs-university.de:8080/def/crs/browse.jsp <--
                    //if (!SUPPORTED_AUTHS.contains(getAuthority(current))) {
                    //    return false;
                    //}
                } else {
                    return false;
                }
            }
            return true;
        }

        /**
         * Return true whether 2 CRS URLs are equivalent definitions. It
         * exploits SECORE's equality handling capabilities and caches new
         * comparisons. If configured SECORE is not available, further
         * configured fallback URIs are queried.
         *
         * @param uri1
         * @param uri2
         * @throws PetascopeException
         * @throws SecoreException
         * @return true if uri1 and uri2 point to the same GML definition
         */
        public static boolean areEquivalent(String uri1, String uri2) throws PetascopeException, SecoreException {
            //return getAuthority(uri1).equals(getAuthority(uri2))
            //        && getVersion(uri1).equals(getVersion(uri2))
            //        &&    getCode(uri1).equals(getCode(uri2));

            // Test if Strings are exactly equal, then no need to ask SECORE
            if (uri1.equals(uri2)) {
                return true;
            }

            // CRS:1 workaround (temporary)
            if (uri1.equals(GRID_CRS) || uri2.equals(GRID_CRS)) {
                return false; // they are not equal (see check above)
            }
            // Otherwise, use SECORE (and cached comparisons)
            List<String> URLs = new ArrayList<String>(2);
            URLs.addAll(Arrays.asList(uri1, uri2));

            if (crsComparisons.containsKey(URLs)) {
                // Comparison is cached                
                return crsComparisons.get(URLs);
            } else {
                // New comparison: need to ask SECORE(s)
                log.trace(getAuthority(uri1) + "(" + getVersion(uri1) + "):" + getCode(uri1) + "/"
                        + getAuthority(uri2) + "(" + getVersion(uri2) + "):" + getCode(uri2) + " "
                        + "comparison is *not* cached: need to ask SECORE.");
                Boolean equal = null;
                for (String resolverUri : ConfigManager.SECORE_URLS) {
                    try {
                        equal = checkEquivalence(resolverUri, uri1, uri2);
                        break; // No need to check against any resolver
                    } catch (SecoreException ex) {
                        // Skip to next loop cycle: try with an other configured resolver URI.
                        log.warn(ex.getMessage());
                    } catch (PetascopeException ex) {
                        throw ex;
                    }
                }

                if (null == equal) {
                    throw new SecoreException(ExceptionCode.ResolverError,
                            "None of the configured CRS URIs resolvers seems available: please check network or add further fallback endpoints. Please see server logs for further info.");
                }

                // cache the comparison
                crsComparisons.put(URLs, equal);
                return equal;
            }
        }

        /**
         * Check equivalence of two CRS URIs through a single resolver.
         *
         * @param resolverUri
         * @param uri1
         * @param uri2
         * @throws PetascopeException
         * @throws SecoreException
         */
        private static boolean checkEquivalence(String resolverUri, String uri1, String uri2)
                throws PetascopeException, SecoreException {

            // Escape key entities: parametrized CRS with KV pairs can clash otherwise
            try {
                uri1 = URLEncoder.encode(uri1, ENCODING_UTF8);
                uri2 = URLEncoder.encode(uri2, ENCODING_UTF8);
            } catch (UnsupportedEncodingException e) {
                log.warn(e.getLocalizedMessage());
                log.warn("URIs will not be URL-encoded.");
            }

            /* Tentative 1: comarison of given URIs
             * Tentative 2: both URIs at resolver
             */
            String equalityUri_given = resolverUri + "/" + KEY_RESOLVER_EQUAL + "?"
                    + "1=" + uri1 + "&"
                    + "2=" + uri2;
            String equalityUri_atResolver = resolverUri + "/" + KEY_RESOLVER_EQUAL + "?"
                    + "1=" + CrsUtil.CrsUri(resolverUri, getAuthority(uri1), getVersion(uri1), getCode(uri1)) + "&"
                    + "2=" + CrsUtil.CrsUri(resolverUri, getAuthority(uri2), getVersion(uri2), getCode(uri2));
            Boolean equal = false;

            for (String equalityUri : new String[]{equalityUri_given, equalityUri_atResolver}) {
                try {
                    // Create InputStream and set the timeouts
                    InputStream inStream = getInputStreamByInternalOrExternalSECORE(equalityUri);
                    log.debug(equalityUri);

                    // Build the document
                    Document doc = XMLUtil.buildDocument(null, inStream);
                    Element root = doc.getRootElement();

                    // Catch some exception
                    Element exEl = XMLUtil.firstChildRecursive(root, XMLSymbols.LABEL_EXCEPTION_TEXT);
                    if (exEl != null) {
                        log.error("Exception returned: " + exEl.getValue());
                        if (equalityUri.equals(equalityUri_atResolver)) {
                            throw new SecoreException(ExceptionCode.ResolverError, exEl.getValue());
                        } // else try with URIs at resolver
                    } else {
                        // Cache this new comparison
                        Element eqEl = XMLUtil.firstChildRecursive(root, XMLSymbols.LABEL_EQUAL);
                        equal = Boolean.parseBoolean(eqEl.getValue());
                    }

                } catch (ValidityException ex) {
                    throw new SecoreException(ExceptionCode.InternalComponentError,
                            "CRS '" + equalityUri + "' returned an invalid document. Reason: " + ex.getMessage(), ex);
                } catch (ParsingException ex) {
                    throw new PetascopeException(ExceptionCode.XmlNotValid.locator(
                            "line: " + ex.getLineNumber() + ", column:" + ex.getColumnNumber()),
                            ex.getMessage(), ex);
                } catch (IOException ex) {
                    throw new SecoreException(ExceptionCode.InternalComponentError,
                            "CRS '" + equalityUri + "' has IO error. Reason: " + ex.getMessage(), ex);
                } catch (SecoreException ex) {
                    throw ex;
                } catch (Exception ex) {
                    throw new PetascopeException(ExceptionCode.XmlNotValid, ex.getMessage(), ex);
                }
                return equal;
            }
            return false;
        }

        // Getters (decomposeUri() first: they work on atomic CRS URIs, check validity as well first)
        /**
         * Extracts the authority from a CRS URL.
         *
         * @param uri The URL of a CRS
         * @return The authority of the CRS definition
         */
        public static String getAuthority(String uri) {
            String decUri = StringUtil.urldecode(uri, null);
            Pattern p;
            Matcher m;
            // KVP
            if (isKvp(decUri)) {
                p = Pattern.compile("^" + HTTP_URL_PATTERN + KEY_RESOLVER_CRS + "\\?.*((?i)" + AUTHORITY_KEY + ")=([^&]+).*$");
                m = p.matcher(decUri);
                while (m.find()) {
                    if (m.groupCount() == 2) {
                        return m.group(2);
                    } else {
                        log.warn(decUri + " seems to be invalid.");
                    }
                }
            }
            // REST
            if (isRest(decUri)) {
                p = Pattern.compile("^" + HTTP_URL_PATTERN + KEY_RESOLVER_CRS + "/([^/]+)/.+/.+$");
                m = p.matcher(decUri);
                while (m.find()) {
                    if (m.groupCount() == 2) {
                        return m.group(2);
                    } else {
                        log.warn(decUri + " seems to be invalid.");
                    }
                }
            }
            return "";
        }

        /**
         * Extracts the version from a CRS URL.
         *
         * @param uri The URL of a CRS
         * @return The version of the CRS definition
         */
        public static String getVersion(String uri) {
            String decUri = StringUtil.urldecode(uri, null);
            Pattern p;
            Matcher m;
            // KVP
            if (isKvp(decUri)) {
                p = Pattern.compile("^" + HTTP_URL_PATTERN + KEY_RESOLVER_CRS + "\\?.*((?i)" + VERSION_KEY + ")=(.+).*$");
                m = p.matcher(decUri);
                while (m.find()) {
                    if (m.groupCount() == 2) {
                        return m.group(2);
                    } else {
                        log.warn(decUri + " seems to be invalid.");
                    }
                }
            }
            // REST
            if (isRest(decUri)) {
                p = Pattern.compile("^" + HTTP_URL_PATTERN + KEY_RESOLVER_CRS + "/[^/]+/(.+)/.+$");
                m = p.matcher(decUri);
                while (m.find()) {
                    if (m.groupCount() == 2) {
                        return m.group(2);
                    } else {
                        log.warn(decUri + " seems to be invalid.");
                    }
                }
            }
            return "";
        }

        /**
         * Extracts the code from a CRS URL.
         *
         * @param uri The URL of a CRS
         * @return The code of the CRS definition
         */
        public static String getCode(String uri) {
            // NOTE: `code' is generally a String, not integer (eg <resolver>/def/crs/OGC/0/Image1D)
            String decUri = StringUtil.urldecode(uri, null);
            Pattern p;
            Matcher m;
            // KVP
            if (isKvp(decUri)) {
                p = Pattern.compile("^" + HTTP_URL_PATTERN + KEY_RESOLVER_CRS + "\\?.*((?i)" + CODE_KEY + ")=(.+).*$");
                m = p.matcher(decUri);
                while (m.find()) {
                    if (m.groupCount() == 2) {
                        return m.group(2);
                    } else {
                        log.warn(decUri + " seems to be invalid.");
                    }
                }
            }
            // REST
            if (isRest(decUri)) {
                p = Pattern.compile("^" + HTTP_URL_PATTERN + KEY_RESOLVER_CRS + "/[^/]+/.+/(.+)$");
                m = p.matcher(decUri);
                while (m.find()) {
                    if (m.groupCount() == 2) {
                        return m.group(2);
                    }
                }
            }
            return "";
        }

        // Generalize the simple HashMap.get() method to include non-identical
        // but equivalent CRS URI (e.g. KVP/SOAP or KVP pairs order).
        /**
         * Generalization of HashMap.get() method to include non-identical but
         * equivalent CRS URI (eg KVP/SOAP or KVP pairs order).
         *
         * @param uri The URI which needs to be parsed
         * @return The cached CrsDefinition, null otherwise
         * @throws PetascopeException
         * @throws SecoreException
         */
        public static CrsDefinition getCachedDefinition(String uri) throws PetascopeException {
            CrsDefinition crsDefinition = parsedCRSs.get(uri);
            return crsDefinition;
        }

        /**
         * Builds a compound CRS from the input atomic CRS URIs.
         *
         * @param crsUris
         * @return The compounding of the listed CRS URIs.
         */
        public static String createCompound(List<String> crsUris) {
            Set<String> crsSet = new LinkedHashSet<>(crsUris);
            String ccrsOut = "";
            switch (crsSet.size()) {
                case 0: // no URIs: return empty string
                    break;
                case 1: // Only one CRS: no need to compound
                    ccrsOut = crsSet.iterator().next();
                    break;
                default: // By default, use SECORE host in the CCRS URL
                    ccrsOut = getResolverUri() + "/" + KEY_RESOLVER_CCRS + "?";
                    Iterator it = crsSet.iterator();
                    for (int i = 0; i < crsSet.size(); i++) {
                        ccrsOut += (i + 1) + "=" + it.next();
                        if (it.hasNext()) {
                            ccrsOut += "&";
                        }
                    }
                    break;
            }
            return ccrsOut;
        }

        /**
         * Appends a CSV list of axes which have not been sliced by a 0D subset.
         * NOTE: this notation is *not* standard.
         *
         * @param singleCrsUri
         * @param leftAxesLabels
         * @throws PetascopeException
         * @throws SecoreException
         * @return
         * buildSlicedUri("http://www.opengis.net/def/crs/EPSG/0/4327",{Lat,Long})
         * -> "http://www.opengis.net/def/crs/EPSG/0/4327@Lat,Long"
         */
        public static String buildSlicedUri(String singleCrsUri, List<String> leftAxesLabels)
                throws PetascopeException, SecoreException {
            String slicedUri = singleCrsUri;

            if (leftAxesLabels.containsAll(getAxesLabels(singleCrsUri))) {
                log.debug("Trying to build a sliced CRS URI where all axes are kept and none is sliced.");
                return singleCrsUri;
            } else {

                // Build the URI: <singleUri>@<leftAxisLabel_1>,<leftAxisLabel_2>/...
                for (String axisLabel : leftAxesLabels) {
                    if (!getAxesLabels(singleCrsUri).contains(axisLabel)) {
                        log.warn("CRS " + singleCrsUri + " does not contain axis '" + axisLabel + "'.");
                    } else {
                        slicedUri += (slicedUri.equals(singleCrsUri) ? SLICED_AXIS_SEPARATOR : ",") + axisLabel;
                    }
                }

                return slicedUri;
            }
        }

        /**
         * Determines if an axis has been sliced from the CRS URI.
         *
         * @param crsUris
         * @param axisLabel
         * @return True if axis is in the list of sliced axes. Example:
         * isSliced("http://www.opengis.net/def/crs/EPSG/0/4327@Long,h", "Lat")
         * --> TRUE {only Long and h are left in th CRS}.
         */
        public static Boolean isSliced(List<String> crsUris, String axisLabel) {
            Boolean isSliced = true;
            for (String singleCrsUri : crsUris) {
                String slicePath = singleCrsUri.replaceAll("^.*" + SLICED_AXIS_SEPARATOR, "");
                if (slicePath.equals(singleCrsUri)) {
                    // there was no @<slicePath> after the URI: no axes are sliced
                    isSliced = false;
                } else {
                    // There is a CSV list of axes: those are the ones who are left (not sliced)
                    List<String> slicedAxesList = Arrays.asList(slicePath.split(","));
                    for (String slicedAxis : slicedAxesList) {
                        if (slicedAxis.equals(axisLabel)) {
                            isSliced = false;
                        }
                    }
                }
            }
            return isSliced;
        }

        // Overload
        public static Boolean isSliced(String singleCrsUri, String axisLabel) {
            return isSliced(new ArrayList<String>(Arrays.asList(new String[]{singleCrsUri})), axisLabel);
        }

        /**
         * Replace the SECORE URI prefix (e.g: http://localhost:8080) to a
         * placeholder before persisting to database. Then when loading
         * persisted coverage's CRS with new SECORE endpoint in
         * petascope.properties, placeholder can be replaced with new endpoint.
         *
         * @param crsUri
         * @return
         */
        public static String toDbRepresentation(String crsUri) {
            //simple
            if (!CrsUri.isCompound(crsUri)) {
                return CrsUri.simpleUriToDbRepresentation(crsUri);
            } //compound
            else {
                List<String> uris = CrsUri.decomposeUri(crsUri);
                String result = SECORE_URL_PREFIX + "/" + CrsUtil.KEY_RESOLVER_CCRS + "?";
                int counter = 1;
                for (String uri : uris) {
                    result += String.valueOf(counter) + "=" + simpleUriToDbRepresentation(uri);
                    if (counter < uris.size()) {
                        result += "&";
                        counter++;
                    }
                }
                return result;
            }
        }

        /**
         * When reading coverage's CRS from database, the URI contains SECORE
         * prefix as string placeholder. So, replace it with SECORE endpoint
         * configured in petascope.properties. e.g:
         * %SECORE_URL%/def/crs/epsg/0/4326 to
         * http://localhost:8080/def/crs/epsg/0/4326
         *
         * @param crsUri
         * @return
         */
        public static String fromDbRepresentation(String crsUri) {
            String secoreURL = ConfigManager.SECORE_URLS.get(0);
            crsUri = crsUri.replace(SECORE_URL_PREFIX, secoreURL);
            return crsUri;
        }

        /**
         * Return a string with authority:code (e.g: EPSG:4326)
         *
         * @return
         */
        public static String getAuthorityCode(String crsUri) {
            String authorityName = getAuthority(crsUri);
            String codeName = getCode(crsUri);
            String authorityCode = authorityName + ":" + codeName;
            return authorityCode;
        }
        

        /**
         * Converts a simple (not composed) crs uri to the form that is stored
         * into the database.
         *
         * @param crsUri the uri of the crs.
         * @return the db form of the uri. E.g.
         * "http://www.opengis.net/def/crs/EPSG/0/4326" ->
         * "$SECORE_URL$/crs/EPSG/0/4326"
         */
        private static String simpleUriToDbRepresentation(String crsUri) {
            String result = crsUri;
            
            // Don't try to parse CRS when it is already the abstract URL
            if (!crsUri.contains(SECORE_URL_PREFIX)) {
                String authority = CrsUri.getAuthority(crsUri);
                String code = CrsUri.getCode(crsUri);
                String version = CrsUri.getVersion(crsUri);
                result = SECORE_URL_PREFIX + "/" + CrsUtil.KEY_RESOLVER_CRS + "/" + authority + "/" + version + "/" + code;
            }

            return result;
        }
    }
}
