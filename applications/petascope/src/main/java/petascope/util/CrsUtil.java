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

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import nu.xom.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petascope.ConfigManager;
import petascope.core.CoverageMetadata;
import petascope.core.CrsDefinition;
import static petascope.core.CrsDefinition.ELEV_ALIASES;
import static petascope.core.CrsDefinition.X_ALIASES;
import static petascope.core.CrsDefinition.Y_ALIASES;
import petascope.core.DbMetadataSource;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCPSException;
import static petascope.util.StringUtil.ENCODING_UTF8;
import petascope.wcps.server.core.CellDomainElement;
import petascope.wcps.server.core.CoverageExpr;
import petascope.wcps.server.core.CoverageInfo;
import petascope.wcps.server.core.DomainElement;
import petascope.wcps.server.core.ExtendCoverageExpr;
import petascope.wcps.server.core.IRasNode;
import petascope.wcps.server.core.TrimCoverageExpr;

/**
 * Coordinates transformation utility in case a spatial reprojection
 *  is needed before translating to WCPS/RASQL queries. *
 * NOTE: each instance of this class should decode the source and
 *  target CRS at construction time, to a one-for-all read into the EPSG db.
 *  It also should not be static since different requests
 *  will involve different CRS. However keeping a static dictionary
 *  of the requested mathematical transformations avoids the need of
 *  redundant read onto the EPSG db.
 *
 * @author <a href="mailto:p.campalani@jacobs-university.de">Piero Campalani</a>
 */
public class CrsUtil {

    // NOTE: accept any URI format, but ask to SECORE: flattened definitions, less recursion.
    public static final  String OPENGIS_URI_PREFIX = "http://www.opengis.net";
    private static final String HTTP_URL_PATTERN   = "http://.*/";
    private static final String HTTP_PREFIX        = "http://";

    // SECORE keywords (URL is set in the ConfigManager)
    public static final String KEY_RESOLVER_CRS    = "crs";
    public static final String KEY_RESOLVER_CCRS   = "crs-compound";
    public static final String KEY_RESOLVER_EQUAL  = "equal";
    public static final char SLICED_AXIS_SEPARATOR = '@';

    // NOTE: "CRS:1" axes to have a GML definition that will be parsed.
    public static final String GRID_CRS  = "CRS:1";
    public static final String GRID_UOM = "GridSpacing"; // See Uom in Index[1-9]D CRS defs
    public static final String PURE_UOM  = "10^0";

    public static final String CRS_DEFAULT_VERSION = "0";
    //public static final String CRS_DEFAULT_FORMAT  = "application/gml+xml";

    // TODO: do not rely on a static set of auths, but ask SECORE to return the supported authorities: ./def/crs/
    public static final String EPSG_AUTH = "EPSG";
    //public static final String ISO_AUTH  = "ISO";
    //public static final String AUTO_AUTH = "AUTO";
    //public static final String OGC_AUTH  = "OGC";
    //public static final String IAU_AUTH  = "IAU2000";
    //public static final String UMC_AUTH  = "UMC";
    //public static final List<String> SUPPORTED_AUTHS = Arrays.asList(EPSG_AUTH, ISO_AUTH, AUTO_AUTH, OGC_AUTH); // IAU_AUTH, UMC_AUTH);

    // WGS84
    public static final String WGS84_EPSG_CODE = "4326";

    /* CACHES: avoid EPSG db and SECORE redundant access */
    private static Map<String, CrsDefinition>       parsedCRSs       = new HashMap<String, CrsDefinition>();        // CRS definitions
    private static Map<List<String>, Boolean>       crsComparisons   = new HashMap<List<String>, Boolean>();        // CRS equality tests
    private static Map<List<String>, long[]>  gridIndexConversions   = new HashMap<List<String>, long[]>();         // subset2gridIndex conversions

    private List<String> crssMap;
    private static final Logger log = LoggerFactory.getLogger(CrsUtil.class);

    /**
     * Build a CRS HTTP URI.
     * @param   resolver     The prefix of the resolver URL (http://<host>/def/)
     * @param   committee    The committee defining the CRS (e.g. EPSG)
     * @param   version      Version of the CRS
     * @param   code         Code of the CRS
     * @return  The URI of the CRS (based on SECORE URL by default)
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

    /**
     * Parser of GML definitions of Coordinate Reference Systems:
     * parse the specified (resolver) URI recursively and creates the CrsDefinition object(s) along.
     * Sliced axes list (e.g. <uri>@<not_sliced_axis>) is dropped from the URI to keep it actionable
     * (SECORE does not understand this notation, nor it is standardized).
     * @param  givenCrsUri   The URI of the /atomic/ CRS to be parsed (URI need to be decomposed first).
     * @return The parsed CRS definition
     * @throws PetascopeException
     * @throws SecoreException
     */
    // (!!) Always use decomposeUri() output to feed this method: it currently understands single CRSs.
    public static CrsDefinition getGmlDefinition(String givenCrsUri) throws PetascopeException, SecoreException {
        CrsDefinition crs = null;
        List<List<String>> axes = new ArrayList<List<String>>();

        // Remove any possible slicing suffixes:
        givenCrsUri = givenCrsUri.replaceAll(SLICED_AXIS_SEPARATOR + ".*$", "");

        // Check first if the definition is already in cache:
        if (CrsUri.isCached(givenCrsUri)) {
            log.info(givenCrsUri + " definition is already in cache: do not need to fetch GML definition.");
            return CrsUri.getCachedDefinition(givenCrsUri);
        }

        // Check if the URI syntax is valid
        if (!CrsUri.isValid(givenCrsUri)) {
            log.info(givenCrsUri + " definition seems not valid.");
            throw new PetascopeException(ExceptionCode.InvalidMetadata, givenCrsUri + " definition seems not valid.");
        }

        // Need to parse the XML
        log.info(givenCrsUri + " definition needs to be parsed from resolver.");
        String uom = "";
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
                URL url = new URL(crsUri);
                URLConnection con = url.openConnection();
                con.setConnectTimeout(ConfigManager.CRSRESOLVER_CONN_TIMEOUT);
                con.setReadTimeout(ConfigManager.CRSRESOLVER_READ_TIMEOUT);
                InputStream inStream = con.getInputStream();

                // Build the document
                Document doc = XMLUtil.buildDocument(null, inStream);
                Element root = doc.getRootElement();

                // Catch some exception in the GML
                Element exEl = XMLUtil.firstChildRecursivePattern(root, ".*" + XMLSymbols.LABEL_EXCEPTION_TEXT);
                if (exEl != null) {
                    log.error(crsUri + ": " + exEl.getValue());
                    throw new SecoreException(ExceptionCode.ResolverError, exEl.getValue());
                }

                // Check if it exists:
                if (!root.getLocalName().matches(".*" + XMLSymbols.CRS_GMLSUFFIX)) {
                    log.error(crsUri + " does not seem to be a CRS definition");
                    throw new PetascopeException(ExceptionCode.InvalidMetadata, "Invalid CRS URI: " + crsUri);
                }

                // This value will be then stored in the CrsDefinition
                String crsType = root.getLocalName();
                log.debug("CRS element found: '" + crsType + "'.");

                // Get the *CS element: **don't** look recursive otherwise you can getinto the underlying geodetic CRS of a projected one (eg EPSG:32634)
                Element csEl = XMLUtil.firstChildPattern(root, ".*" + XMLSymbols.CS_GMLSUFFIX);
                // Check if it exists
                if (csEl == null) {
                    log.error(crsUri + ": missing the Coordinate System element.");
                    throw new PetascopeException(ExceptionCode.InvalidMetadata, "Invalid CRS definition: " + crsUri);
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

                List<Element> axesList = XMLUtil.ch(csEl, XMLSymbols.LABEL_CRSAXIS);

                // Check if there is at least one axis definition
                if (axesList.isEmpty()) {
                    log.error(crsUri + ": missing the axis element(s).");
                    throw new PetascopeException(ExceptionCode.InvalidMetadata, "Invalid CRS definition: " + crsUri);
                }

                for (Element axisEl : axesList) {

                    // Get CoordinateSystemAxis mandatory element
                    Element csaEl = XMLUtil.firstChildRecursive(axisEl, XMLSymbols.LABEL_CSAXIS);
                    if (csaEl == null) {
                        log.error(crsUri + ": missing the CoordinateSystemAxis element.");
                        throw new PetascopeException(ExceptionCode.InvalidMetadata, "Invalid CRS definition: " + crsUri);
                    }

                    // Get abbreviation
                    Element axisAbbrevEl = XMLUtil.firstChildRecursive(csaEl, XMLSymbols.LABEL_AXISABBREV);
                    Element axisDirEl    = XMLUtil.firstChildRecursive(csaEl, XMLSymbols.LABEL_AXISDIRECTION);

                    // Check if they are defined: otherwise exception must be thrown
                    if (axisAbbrevEl == null | axisDirEl == null) {
                        log.error(crsUri + ": axis definition misses abbreviation and/or direction.");
                        throw new PetascopeException(ExceptionCode.InvalidMetadata, "Invalid CRS definition: " + crsUri);
                    }
                    String axisAbbrev = axisAbbrevEl.getValue();
                    String axisDir    = axisDirEl.getValue();

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

                        // UoM attribute can be either a String or as well a dereferenced definition (URL)
                        if (!uomAtt.getValue().contains(HTTP_PREFIX)) {
                            uomName = uomAtt.getValue().split(" ")[0]; // UoM is meant as one word only
                        } else {
                            // Need to parse a new XML definition
                            uomUrl = new URL(uomAtt.getValue());
                            try {
                                URLConnection uomCon = uomUrl.openConnection();
                                uomCon.setConnectTimeout(ConfigManager.CRSRESOLVER_CONN_TIMEOUT);
                                uomCon.setReadTimeout(ConfigManager.CRSRESOLVER_READ_TIMEOUT);
                                InputStream uomInStream = uomCon.getInputStream();

                                // Build the document
                                Document uomDoc =   XMLUtil.buildDocument(null, uomInStream);
                                Element uomRoot = uomDoc.getRootElement();

                                // Catch some exception in the GML
                                Element uomExEl = XMLUtil.firstChildRecursive(uomRoot, XMLSymbols.LABEL_EXCEPTION_TEXT);
                                if (uomExEl != null) {
                                    log.error(crsUri + ": " + uomExEl.getValue());
                                    throw new SecoreException(ExceptionCode.ResolverError, uomExEl.getValue());
                                }

                                // Get the UoM value
                                Element uomNameEl = XMLUtil.firstChildRecursive(uomRoot, XMLSymbols.LABEL_NAME);
                                if (uomNameEl == null) {
                                    log.error(uom + ": UoM definition misses name.");
                                    throw new PetascopeException(ExceptionCode.InvalidMetadata, "Invalid UoM definition: " + uom);
                                }
                                uomName = uomNameEl.getValue().split(" ")[0]; // Some UoM might have further comments after actual UoM (eg EPSG:4326)
                            } catch (ParsingException pEx) {
                                uomName = extractUomNameFromUri(uomUrl);
                            } catch (IOException ioEx) {
                                // The UoM is not a resolvable URI: use its last field as name (FS='/')
                                uomName = extractUomNameFromUri(uomUrl);
                            } // NOTE: with Java 7 you can put exception types in OR in a single catch clause.
                        }
                    }

                    log.debug("Axis element found: " + axisAbbrev + "[" + uomName + "]");

                    // Add axis to the definition (temporarily first, then force XY order)
                    List<String> tmp = new ArrayList<String>();
                    tmp.addAll(Arrays.asList(axisDir, axisAbbrev, uomName));
                    axes.add(tmp);

                } // END axes loop

                // If this is a TemporalCRS definition, read the TemporalDatum's origin
                if (crsType.equals(XMLSymbols.LABEL_TEMPORALCRS)) {

                    Element datumEl = XMLUtil.firstChildRecursivePattern(root, ".*" + XMLSymbols.DATUM_GMLSUFFIX);
                    if (datumEl == null) {
                        log.warn(crsUri + ": missing the datum element.");
                        throw new PetascopeException(ExceptionCode.InvalidMetadata, "Invalid CRS definition: " + crsUri);
                    }

                    log.debug("Datum element found: '" + datumEl.getLocalName() + "'.");

                    // Get the origin of the datum
                    Element datumOriginEl = XMLUtil.firstChildRecursive(datumEl, XMLSymbols.LABEL_ORIGIN);
                    if (datumOriginEl == null) {
                        log.warn(crsUri + ": missing the origin of the datum.");
                        throw new PetascopeException(ExceptionCode.InvalidMetadata, "Invalid CRS definition: " + crsUri);
                    }
                    datumOrigin = datumOriginEl.getValue();

                    // Add datum origin to the definition object
                    crs.setDatumOrigin(datumOrigin);
                    log.debug("Found datum origin: " + datumOrigin);

                } // else: no need to parse the datum
                break; // fallback only on IO problems
            } catch (MalformedURLException ex) {
                log.error("Malformed URI: " + ex.getMessage());
                throw new SecoreException(ExceptionCode.InvalidMetadata, ex);
            } catch (ValidityException ex) {
                throw new SecoreException(ExceptionCode.InternalComponentError,
                        (null==uomUrl ? crsUri : uomUrl) + " definition is not valid.", ex);
            } catch (ParsingException ex) {
                log.error(ex.getMessage() + "\n at line " + ex.getLineNumber() + ", column " + ex.getColumnNumber());
                throw new SecoreException(ExceptionCode.InternalComponentError,
                        (null==uomUrl ? crsUri : uomUrl) + " definition is malformed.", ex);
            } catch (IOException ex) {
                if (crsUri.equals(lastUri) || null != uomUrl) {
                    throw new SecoreException(ExceptionCode.InternalComponentError,
                            (null==uomUrl ? crsUri : uomUrl) + ": resolver exception: " + ex.getMessage(), ex);
                } else {
                    log.info("Connection problem with " + (null==uomUrl ? crsUri : uomUrl) + ": " + ex.getMessage());
                    log.info("Attempting to fetch the CRS definition via fallback resolver.");
                }
            } catch (SecoreException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new PetascopeException(ExceptionCode.InternalComponentError,
                        (null==uomUrl ? crsUri : uomUrl) + ": general exception while parsing definition.", ex);
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

        // Cache the definition
        parsedCRSs.put(givenCrsUri, crs);
        log.info(givenCrsUri + " into cache for future (inter-requests) use.");

        return crs;
    }

    /**
     * Extracts the UoM name from the URI as last field in the RESTful .
     * 10^0 as UoM for pure numbers is otherwise returned.
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
            while(m.find()) {
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
     * In case northing is first, force easting/northing axis order.
     * @param axesMetadata The axis metadata as parsed from GML definition (Dir, Abbrev, UoM).
     */
    private static void forceXYorder(CrsDefinition crs, List<List<String>> axesMetadata) {
        int Xind = -1;
        int Yind = -1;

        for (int i = 0; i < axesMetadata.size(); i++) {
            if (crs.X_ALIASES.contains(axesMetadata.get(i).get(1))) {
                Xind = i;
            } else if (crs.Y_ALIASES.contains(axesMetadata.get(i).get(1))) {
                Yind = i;
            }
        }

        if ((Xind > 0) && (Yind >= 0) && (Yind < Xind)) {
            // Northing is first in the CRS definition: swap metadata.
            List<String> tmp = axesMetadata.get(Yind);
            axesMetadata.set(Yind, axesMetadata.get(Xind));
            axesMetadata.set(Xind, tmp);
        }
    }

    /**
     * Discover which is the order of the specified (CRS) axis in the specified ordered list of CRSs (compound CRS).
     * If the axis is not present in the (C)CRS, then -1 is returned.
     * NOTE: use CrsUri.decomposeUri() to get a list of single CRS URIs from a compound CRS.
     * @param crsUris     An ordered list of single CRS URIs
     * @param axisAbbrev  The CRS axis label (//CoordinateSystemAxis/axisAbbrev)
     * @return The order of `axisAbbrev' axis in the (C)CRS [0 is first], -1 otherwise.
     * @throws PetascopeException
     * @throws SecoreException
     */
    public static Integer getCrsAxisOrder(List<String> crsUris, String axisAbbrev) throws PetascopeException, SecoreException {

        // init
        CrsDefinition crsDef;
        Integer counter = 0;

        // scan the CRS axes
        for (String singleCrs : crsUris) {
            crsDef = CrsUtil.getGmlDefinition(singleCrs); // cache is used, no SECORE access here
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
     * Discover which is the label of the specified (CRS) axis in the specified ordered list of CRSs (compound CRS).
     * If the axis is not present in the (C)CRS, then an empty String is returned.
     * NOTE: use CrsUri.decomposeUri() to get a list of single CRS URIs from a compound CRS.
     * @param crsUris   An ordered list of single CRS URIs
     * @param axisOrder The order of the axis (//CoordinateSystemAxis) in the (C)CRS [0 is first]
     * @return The label (//CoordinateSystemAxis/axisAbbrev) of the "axisOrder"-th axis in the (C)CRS, an empty String otherwise.
     * @throws PetascopeException
     * @throws SecoreException
     */
    public static String getAxisLabel(List<String> crsUris, Integer axisOrder) throws PetascopeException, SecoreException {

        // init
        Integer counter = 0;
        CrsDefinition crsDef;

        // scan the CRS axes
        for (String singleCrs : crsUris) {
            crsDef = CrsUtil.getGmlDefinition(singleCrs);
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
     * @param crsUris  An ordered list of single CRS URIs
     * @return The ordered list of labels (//CoordinateSystemAxis/axisAbbrev) of the (C)CRS.
     * @throws PetascopeException
     * @throws SecoreException
     */
    public static List<String> getAxesLabels(List<String> crsUris) throws PetascopeException, SecoreException {

        // init
        List<String> axesLabels = new ArrayList<String>(getTotalDimensionality(crsUris));
        CrsDefinition crsDef;

        // scan the CRS axes
        for (String singleCrs : crsUris) {
            crsDef = CrsUtil.getGmlDefinition(singleCrs);
            for (CrsDefinition.Axis crsAxis : crsDef.getAxes()) {
                if (!CrsUri.isSliced(singleCrs, crsAxis.getAbbreviation())) {
                    axesLabels.add(crsAxis.getAbbreviation());
                }
            }
        }

        return axesLabels;
    }
    // Overload for single URI
    public static List<String> getAxesLabels(String singleCrsUri) throws PetascopeException, SecoreException {
        return getAxesLabels(new ArrayList<String>(Arrays.asList(new String[]{singleCrsUri})));
    }

    /**
     * Discover which is the type of the specified (CRS) axis.
     * @param crs   An ordered list of single CRS URIs
     * @param axisName The order of the axis (//CoordinateSystemAxis) in the (C)CRS [0 is first]
     * @return The type of the specified axis
     */
    public static String getAxisType(CrsDefinition crs, String axisName) {

        String type;

        // init
        if (X_ALIASES.contains(axisName)) {
            type = AxisTypes.X_AXIS;
        } else if (Y_ALIASES.contains(axisName)) {
            type = AxisTypes.Y_AXIS;
        } else if (ELEV_ALIASES.contains(axisName)) {
            type = AxisTypes.ELEV_AXIS;
            // A TemporalCRS has just one axis:
        } else if (crs.getType().equals(XMLSymbols.LABEL_TEMPORALCRS)) {
            type = AxisTypes.T_AXIS;
        } else {
            type = AxisTypes.OTHER;
        }

        return type;
    }

    /**
     * Get an ordered list of axis UoMs for a CCRS.
     * @param crsUris  An ordered list of single CRS URIs
     * @return The ordered list of UoM (//CoordinateSystemAxis/@uom) of the (C)CRS.
     * @throws PetascopeException
     * @throws SecoreException
     */
    public static List<String> getAxesUoMs(List<String> crsUris) throws PetascopeException, SecoreException {

        // init
        List<String> axesUoMs = new ArrayList<String>(getTotalDimensionality(crsUris));
        CrsDefinition crsDef;

        // scan the CRS axes
        for (String singleCrs : crsUris) {
            crsDef = CrsUtil.getGmlDefinition(singleCrs);
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
     * @param crsUris  An ordered list of single CRS URIs
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
            crsDef = CrsUtil.getGmlDefinition(singleCrs);
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
     * Deduces the updated (C)CRS URI from a given (C)CRS where 1+ axes have to be removed (sliced out).
     * @param crsUris     An ordered list of single CRS URIs
     * @param slicedAxes  The unordered list of axes to be sliced
     * @return An updated (C)CRS URI where CRSs have been either removed (all its axes were sliced), partially sliced, or left as-is.
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
                List<String> labels = new ArrayList<String>(getGmlDefinition(singleCrs).getDimensions());
                // Get the list of axis labels of this URI and update the Map
                labels.addAll(getGmlDefinition(singleCrs).getAxesLabels());
                crsAxes.put(singleCrs, labels);
            }

            // Now decrease the axes number at every slice:
            for (Map.Entry orderLabel : orderedSlicedAxes.entrySet()) {
                for (String singleCrs : crsUris) {
                    List<String> crsAxesLabels = getAxesLabels(singleCrs);
                    String thisLabel = (String)orderLabel.getValue();
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
                String uri = (String)uriLabels.getKey();
                List<String> axesLabels = (List<String>)uriLabels.getValue();

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
     * @return
     */
    private static String getResolverUri() {
        return ConfigManager.SECORE_URLS.get(0);
    }

    /**
     * Utility to convert CRS subsets to grid indexes.
     * Each grid axis is aligned with an axis of the native (compound) CRS.
     * Conversions are cached, so that multiple calls do not duplicate computations.
     * For WCS each request indeed you convert to build the RasQL query to fetch the data,
     * and additionally you need the same indexes to update the coverage description.
     *
     * Grid indexes are not trimmer to fit sdom, since operations like extend or scale neet to
     * go over the bounds. It is up to the caller to trim them afterwards.
     *
     * @param covMeta
     * @param dbMeta
     * @param axisName
     * @param stringLo
     * @param loIsNumeric
     * @param stringHi
     * @param hiIsNumeric
     * @return
     * @throws PetascopeException
     */
    public static long[] convertToInternalGridIndices(CoverageMetadata covMeta, DbMetadataSource dbMeta, String axisName,
            String stringLo, boolean loIsNumeric, String stringHi, boolean hiIsNumeric)
            throws PetascopeException {

        // out indexes
        long[] subsetGridIndexes = new long[2];

        // Check the cache: signature is {coverage name, axis name, lo, hi}
        List<String> cacheKey = new ArrayList<String>(Arrays.asList(new String[] {
            covMeta.getCoverageName(),
            axisName,
            stringLo,
            stringHi
        }));
        // query the cache (for irregular axes: need to fetch the coefficients and set them in the DomainElement: no cache)
        if (gridIndexConversions.containsKey(cacheKey)
                &&  !covMeta.getDomainByName(axisName).isIrregular()) {
            return gridIndexConversions.get(cacheKey);

        } else {
            // Conversion is not cached: compute it
            DomainElement      dom = covMeta.getDomainByName(axisName);
            CellDomainElement cdom = covMeta.getCellDomainByName(axisName);

            // null-pointers check
            if (null == cdom || null == dom) {
                log.error("Could not find the \"" + axisName + "\" axis for coverage:" + covMeta.getCoverageName());
                throw new PetascopeException(ExceptionCode.NoApplicableCode, "Could not find the \"" + axisName + "\" axis for coverage:" + covMeta.getCoverageName());
            }

            // Get datum origin can also be null if !temporal axis: use axisType to guard)
            String datumOrigin = dom.getAxisDef().getCrsDefinition().getDatumOrigin();

            // Get Unit of Measure of this axis:
            String axisUoM = dom.getUom();

            // Get cellDomain extremes
            long indexMin = cdom.getLoInt();
            long indexMax = cdom.getHiInt();
            log.trace("CellDomain extremes values: LOW: " + indexMin + ", HIGH:" + indexMax);

            // Get Domain extremes (real sdom)
            BigDecimal domMin = dom.getMinValue();
            BigDecimal domMax = dom.getMaxValue();
            log.trace("Domain extremes coordinates: (" + domMin + ", " + domMax + ")");
            log.trace("Subset cooordinates: (" + stringLo + ", " + stringHi + ")");

            /*---------------------------------*/
            /*         VALIDITY CHECKS         */
            /*---------------------------------*/
            log.trace("Checking order, format and bounds of axis {} ...", axisName);

            // Requires homogeneity in the bounds of a subset
            if (loIsNumeric ^ hiIsNumeric) { // XOR
                log.error("(" + stringLo + "," + stringHi + ") subset is invalid.");
                throw new PetascopeException(ExceptionCode.InvalidRequest,
                        "(" + stringLo + "," + stringHi + ") subset requires bounds of the same domain.");
            }
            //~(From now on, some tests can be made on a single bound)
            boolean subsetWithTimestamps = !loIsNumeric; // = !hiIsNumeric

            // if subsets are not numeric, /now/ they other choice is that they are timestamps: check the format is valid
            if (subsetWithTimestamps && !TimeUtil.isValidTimestamp(stringLo)) {
                log.error("Invalid temporal subset: " + stringLo);
                throw new WCPSException(ExceptionCode.InvalidRequest,
                        "Subset '" + stringLo + "' is not valid nor as a number, nor as a supported time description. "
                        + "See Date4J javadoc for supported formats.");
            }
            if (subsetWithTimestamps && !TimeUtil.isValidTimestamp(stringHi)) {
                log.error("Invalid temporal subset: " + stringHi);
                throw new WCPSException(ExceptionCode.InvalidRequest,
                        "Subset '" + stringHi + "' is not valid nor as a number, nor as a supported time description. "
                        + "See Date4J javadoc for supported formats.");
            }

            // Check order of subset: separate treatment for temporal axis with timestamps subsets
            if (subsetWithTimestamps) {
                // Check order
                if (!TimeUtil.isOrderedTimeSubset(stringLo, stringHi)) {
                    log.error("Temporal subset is not ordered: [" + stringLo + ", " + stringHi + "]");
                    throw new PetascopeException(ExceptionCode.InvalidSubsetting,
                            axisName + " axis: lower bound " + stringLo + " is greater then the upper bound " + stringHi);
                }
            } else {

                // Numerical axis:
                double coordLo = Double.parseDouble(stringLo);
                double coordHi = Double.parseDouble(stringHi);

                // Check order
                if (coordHi < coordLo) {
                    log.error("Subset is not ordered: [" + coordLo + ", " + coordHi + "]");
                    throw new PetascopeException(ExceptionCode.InvalidSubsetting,
                            axisName + " axis: lower bound " + coordLo + " is greater the upper bound " + coordHi);
                }

                // Check intersection with extents
                if (coordLo > domMax.doubleValue() || coordHi < domMin.doubleValue()) {
                    log.error("Subset " + "[" + coordLo + ", " + coordHi + "] is out of coverage bounds [" + domMin + ", " + domMax + "]");
                    throw new PetascopeException(ExceptionCode.InvalidSubsetting,
                            axisName + " axis: subset (" + coordLo + ":" + coordHi + ") is out of bounds.");
                }
            }

            /*---------------------------------*/
            /*             CONVERT             */
            /*---------------------------------*/
            log.trace("Converting axis {} interval to pixel indices ...", axisName);
            // There can be several different cases depending on spacing and type of this axis:
            if (dom.isIrregular()) {

                // Consistency check
                // TODO need to find a way to solve `static` issue of dbMeta
                if (dbMeta == null) {
                    throw new PetascopeException(ExceptionCode.InternalComponentError,
                            "Axis " + axisName + " is irregular but is not linked to DbMetadataSource.");
                }

                // Need to query the database (IRRSERIES table) to get the extents
                try {
                    //
                    String numLo = stringLo;
                    String numHi = stringHi;
                    BigDecimal normalizedNumLo;
                    BigDecimal normalizedNumHi;

                    if (subsetWithTimestamps) {
                        // Need to convert timestamps to TemporalCRS numeric coordinates (normalized: "how many vectors" between subset/datum)
                        normalizedNumLo = new BigDecimal(TimeUtil.countOffsets(datumOrigin, stringLo, axisUoM, dom.getScalarResolution().doubleValue()));
                        normalizedNumHi = new BigDecimal(TimeUtil.countOffsets(datumOrigin, stringHi, axisUoM, dom.getScalarResolution().doubleValue()));
                    } else {
                        // Coefficients refer to how many offset-vectors of distance is a coordinate
                        normalizedNumLo = BigDecimalUtil.divide(new BigDecimal(numLo), dom.getScalarResolution());
                        normalizedNumHi = BigDecimalUtil.divide(new BigDecimal(numHi), dom.getScalarResolution());
                    }

                    // Retrieve correspondent cell indexes (unique method for numerical/timestamp values)
                    // TODO: I need to extract all the values, not just the extremes
                    BigDecimal normalizedDomMin = BigDecimalUtil.divide(domMin, dom.getScalarResolution());
                    subsetGridIndexes = dbMeta.getIndexesFromIrregularRectilinearAxis(
                            covMeta.getCoverageName(),
                            covMeta.getDomainIndexByName(axisName), // i-order of axis
                            normalizedNumLo.subtract(normalizedDomMin),  // coefficients are relative to the origin, but subsets are not.
                            normalizedNumHi.subtract(normalizedDomMin),  //
                            indexMin, indexMax);

                    // Retrieve the coefficients values and store them in the DomainElement
                    dom.setCoefficients(dbMeta.getCoefficientsOfInterval(
                            covMeta.getCoverageName(),
                            covMeta.getDomainIndexByName(axisName), // i-order of axis
                            normalizedNumLo.subtract(normalizedDomMin),
                            normalizedNumHi.subtract(normalizedDomMin)
                            ));

                    // Add sdom lower bound
                    subsetGridIndexes[0] = subsetGridIndexes[0] + indexMin;

                } catch (PetascopeException e) {
                    throw new PetascopeException(ExceptionCode.InternalComponentError,
                            "Error while fetching cell boundaries of irregular axis '" +
                            axisName + "' of coverage " + covMeta.getCoverageName() + ": " + e.getMessage(), e);
                }

            } else {
                // The axis is regular: need to differentiate between numeric and timestamps
                // TODO: enable negative directions in time axis too (dom.isPositiveForwards())
                if (subsetWithTimestamps) {
                    // Need to convert timestamps to TemporalCRS numeric coordinates
                    Double numLo = TimeUtil.countOffsets(datumOrigin, stringLo, axisUoM, dom.getScalarResolution().doubleValue());
                    Double numHi = TimeUtil.countOffsets(datumOrigin, stringHi, axisUoM, dom.getScalarResolution().doubleValue());

                    // Consistency check
                    if (numHi < domMin.doubleValue() || numLo > domMax.doubleValue()) {
                        throw new PetascopeException(ExceptionCode.InternalComponentError,
                                "Translated pixel indixes of regular temporal axis (" +
                                numLo + ":" + numHi +") exceed the allowed values.");
                    }

                    // Replace timestamps with numeric subsets
                    stringLo = "" + numLo;
                    stringHi = "" + numHi;
                    // Now subsets can be tranlsated to pixels with normal numeric proportion
                }

                // Indexed CRSs (integer "GridSpacing" UoM): no need for math proportion, coords are just int offsets.
                // This is not the same as CRS:1 which access direct grid coordinates.
                // Index CRSs have indexed coordinates, but still offset vectors can determine a different reference (eg origin is UL corner).
                if (axisUoM.equals(CrsUtil.GRID_UOM)) {
                    int count = 0;
                    // S = subset value, px_s = subset grid index
                    // m = min(grid index), M = max(grid index)
                    // {isPositiveForwards / isNegativeForwards}
                    // Formula : px_s = grid_origin + [{S/M} - {m/S}]
                    for (String subset : (Arrays.asList(new String[] {stringLo, stringHi}))) {
                        // NOTE: on subsets.lo the /next/ integer needs to be taken : trunc(stringLo) + 1 (if it is not exact integer)
                        boolean roundUp = subset.equals(stringLo) && ((double)Double.parseDouble(subset) != (long)Double.parseDouble(subset));
                        long hiBound = dom.isPositiveForwards() ? (long)Double.parseDouble(subset) + (roundUp ? 1 : 0): indexMax;
                        long loBound = dom.isPositiveForwards() ? indexMin : (long)Double.parseDouble(subset) + (roundUp ? 1 : 0);
                        subsetGridIndexes[count] =  domMin.longValue() + (hiBound - loBound);
                        count += 1;
                    }
                    log.debug("Subset grid indexes for input Index CRS subsets: (" +
                            subsetGridIndexes[0] + "," + subsetGridIndexes[1] + ")");
                } else {

                    /* Loop to get the pixel subset values.
                    * Different cases (0%-overlap subsets are excluded by the checks above)
                    *        MIN                                                     MAX
                    *         |-------------+-------------+-------------+-------------|
                    *              cell0         cell1         cell2         cell3
                    * i)   |_____________________|
                    * ii)                              |__________________|
                    * iii)                                                         |_________________|
                    */
                    // Numeric interval: simple mathematical proportion
                    double coordLo = Double.parseDouble(stringLo);
                    double coordHi = Double.parseDouble(stringHi);

                    // Get cell dimension -- Use BigDecimals to avoid finite arithmetic rounding issues of Doubles
                    //double cellWidth = dom.getResolution().doubleValue();
                    double cellWidth = BigDecimalUtil.divide(
                            (domMax.subtract(domMin)),
                            (BigDecimal.valueOf(indexMax+1)).subtract(BigDecimal.valueOf(indexMin)))
                            .doubleValue();
                    //      = (dDomHi-dDomLo)/(double)((pxHi-pxLo)+1);

                    // Open interval on the right: take away epsilon from upper bound:
                    //double coordHiEps = coordHi - cellWidth/10000;    // [a,b) subsets
                    double coordHiEps = coordHi;                        // [a,b] subsets

                    // Conversion to pixel domain
                    /*
                    * Policy = minimum encompassing BoundingBox returned
                    *
                    *     9°       10°       11°       12°       13°
                    *     o---------o---------o---------o---------o
                    *     |  cell0  |  cell1  |  cell2  |  cell3  |
                    *     [=== s1 ==]
                    *          [=== s2 ==]
                    *           [===== s3 ====]
                    *      [= s4 =]
                    *
                    * -- [a,b] closed intervals:
                    * s1(9°  ,10°  ) -->  [cell0:cell1]
                    * s2(9.5°,10.5°) -->  [cell0:cell1]
                    * s3(9.7°,11°  ) -->  [cell0:cell2]
                    * s4(9.2°,9.8° ) -->  [cell0:cell0]
                    *
                    * -- [a,b) open intervals:
                    * s1(9°  ,10°  ) -->  [cell0:cell0]
                    * s2(9.5°,10.5°) -->  [cell0:cell1]
                    * s3(9.7°,11°  ) -->  [cell0:cell1]
                    * s4(9.2°,9.8° ) -->  [cell0:cell0]
                    */
                    if (dom.isPositiveForwards()) {
                        // Normal linear numerical axis
                        subsetGridIndexes = new long[] {
                            (long)Math.floor((coordLo    - domMin.doubleValue()) / cellWidth) + indexMin,
                            (long)Math.floor((coordHiEps - domMin.doubleValue()) / cellWidth) + indexMin
                        };
                        // NOTE: the if a slice equals the upper bound of a coverage, out[0]=pxHi+1 but still it is a valid subset.
                        if (coordLo == coordHi && coordHi == domMax.doubleValue()) {
                            subsetGridIndexes[0] -= 1;
                        }
                    } else {
                        // Linear negative axis (eg northing of georeferenced images)
                        subsetGridIndexes = new long[] {
                            // First coordHi, so that left-hand index is the lower one
                            (long)Math.floor((domMax.doubleValue() - coordHiEps) / cellWidth) + indexMin,
                            (long)Math.floor((domMax.doubleValue() - coordLo)    / cellWidth) + indexMin
                        };
                        // NOTE: the if a slice equals the lower bound of a coverage, out[0]=pxHi+1 but still it is a valid subset.
                        if (coordLo == coordHi && coordHi == domMin.doubleValue()) {
                            subsetGridIndexes[0] -= 1;
                        }
                    }
                    log.debug("Transformed coords indices (" + subsetGridIndexes[0] + "," + subsetGridIndexes[1] + ")");
                }
            }

            // Check lo<hi integrity (subsetHi becomes lower grid bound if the axis is positive backwards):
            if (subsetGridIndexes[0] > subsetGridIndexes[1]) { // = (dom.isPositiveForwards())
                subsetGridIndexes = new long[] {subsetGridIndexes[1], subsetGridIndexes[0]}; // swap
                log.debug("Fix grid indexes order: (" + subsetGridIndexes[0] + "," + subsetGridIndexes[1] + ")");
            }
        }

        // Add to cache
        gridIndexConversions.put(cacheKey, subsetGridIndexes);

        return subsetGridIndexes;
    }
    // Overload (for slices)
    // [!] NOTE: do not use this method in case of trims: no lo<hi guard can be applied if interval elements are converted separately.
    public static long convertToInternalGridIndices(CoverageMetadata meta, DbMetadataSource dbMeta, String axisName, String value, boolean isNumeric) throws PetascopeException {
        return convertToInternalGridIndices(meta, dbMeta, axisName, value, isNumeric, value, isNumeric)[0];
    }


    /**
     * Nested class to offer utilities for CRS *URI* handling.
     */
    public static class CrsUri {

        public static final String LAST_PATH_PATTERN = ".*/(.*)$";

        private static final String COMPOUND_SPLIT   = "(\\?|&)\\d+=";
        private static final String COMPOUND_PATTERN = "^" + HTTP_URL_PATTERN + KEY_RESOLVER_CCRS;

        private static final String AUTHORITY_KEY = "authority";    // Case-insensitivity is added in the pattern
        private static final String VERSION_KEY   = "version";
        private static final String CODE_KEY      = "code";
        //private static final String FORMAT_KEY    = "format";

        private static final String KV_PAIR = "(((?i)" + AUTHORITY_KEY  + ")=[^&]+|"  +
                                               "((?i)" + CODE_KEY       + ")=(.+)|" +
                                             //"((?i)" + FORMAT_KEY     + ")=[^&]+|" +  // Add 1 KV_PAIR here below when enabled.
                                               "((?i)" + VERSION_KEY    + ")=(.+))";
        private static final String KVP_CRS_PATTERN = "^" +
                HTTP_URL_PATTERN + KEY_RESOLVER_CRS + "\\?" +
                KV_PAIR + "&" + KV_PAIR + "&" + KV_PAIR + "$";

        private static final String REST_CRS_PATTERN = "^" +
                HTTP_URL_PATTERN + KEY_RESOLVER_CRS + "/[^/]+/.+/.+$";

        // In case the URI represents a CCRS, it return the list of atomic CRS it represents.
        // NOTE: consistency of the URI should be first evaluated with isValid().
        /**
         * In case the URI represents a CCRS, it returns the list of atomic CRS it represents.
         * @param uri
         * @return The list of atomic URIs that form the CCRS, with one element in case uri is already atomic.
         */
        public static List<String> decomposeUri(String uri) {
            String decUri = StringUtil.urldecode(uri, null);
            List<String> crss = new ArrayList<String>();

            if (isCompound(decUri)) {
                String[] splitted = decUri.split(COMPOUND_SPLIT);
                log.debug(Arrays.toString(splitted));
                if (splitted.length <= 1) {
                    log.warn(decUri + " seems invalid: check consitency first.");
                }
                if (splitted.length == 2) {
                    log.warn(decUri + " seems compound but only one CRS is listed.");
                }
                // The first element of the splitted String is the definition prefix: ignore it.
                for (int i=0; i<splitted.length; i++) {
                    if (i>0) {
                        crss.add(splitted[i]);
                        log.debug("Found atomic CRS from compound:" + splitted[i]);
                    }
                }
            } else if (!uri.isEmpty()) {
                crss.add(uri);
            }

            return crss;
        }

        /**
         * Checks if a URI is compound.
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
         * Checks if a specified URI (or an equivalent one) has already been cached.
         * @param uri
         * @return True if uri's definition has already been parsed and cached.
         * @throws PetascopeException
         * @throws SecoreException
         */
        public static boolean isCached(String uri) throws PetascopeException, SecoreException {
            for (String cachedUri : parsedCRSs.keySet()) {
                if (areEquivalent(cachedUri, uri)) {
                    log.debug(uri + " CRS is already decoded in cache.");
                    return true;
                }
            }
            log.debug(uri + " CRS needs to be decoded via resolver.");
            return false;
        }

        /**
         * Returns true if a specified URI is key-value paired.
         * It works with atomic CRSs (use decomposeUri first).
         * @param uri
         * @return True if uri is key-value paired
         */
        public static boolean isKvp(String uri) {
            Pattern pKvp  = Pattern.compile(KVP_CRS_PATTERN);
            Matcher m     = pKvp.matcher(StringUtil.urldecode(uri, null));
            while (m.find()) { return true; }
            return false;
        }
        /**
         * Returns true if a specified URI is RESTful.
         * It works with atomic CRSs (use decomposeUri first).
         * @param uri
         * @return True if uri is RESTful.
         */
        public static boolean isRest(String uri) {
            Pattern pRest = Pattern.compile(REST_CRS_PATTERN);
            Matcher m     = pRest.matcher(StringUtil.urldecode(uri, null));
            while (m.find()) { return true; }
            return false;
        }

        /**
         * Checks if an URI, compound or not, is consistent.
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
         * Return true whether 2 CRS URLs are equivalent definitions.
         * It exploits SECORE's equality handling capabilities and caches new comparisons.
         * If configured SECORE is not available, further configured fallback URIs are queried.
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
                log.info(getAuthority(uri1) + "(" + getVersion(uri1) + "):" + getCode(uri1) + "/" +
                         getAuthority(uri2) + "(" + getVersion(uri2) + "):" + getCode(uri2) + " " +
                        "comparison is cached: *no* need to ask SECORE.");
                return crsComparisons.get(URLs);
            } else {
                // New comparison: need to ask SECORE(s)
                log.info(getAuthority(uri1) + "(" + getVersion(uri1) + "):" + getCode(uri1) + "/" +
                         getAuthority(uri2) + "(" + getVersion(uri2) + "):" + getCode(uri2) + " " +
                        "comparison is *not* cached: need to ask SECORE.");
                Boolean equal = null;
                for (String resolverUri : ConfigManager.SECORE_URLS) {
                    try {
                        log.debug("Checking equivalence of CRSs via " + resolverUri + "...");
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
                    throw new SecoreException(ExceptionCode.InternalComponentError,
                            "None of the configured CRS URIs resolvers seems available: please check network or add further fallback endpoints. Please see server logs for further info.");
                }

                // cache the comparison
                crsComparisons.put(URLs, equal);
                return equal;
            }
        }

        /**
         * Check equivalence of two CRS URIs through a single resolver.
         * @param resolverUri
         * @param uri1
         * @param uri2
         * @return
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

            String equalityUri_given = resolverUri + "/" + KEY_RESOLVER_EQUAL + "?" +
                        "1=" + uri1 + "&" +
                        "2=" + uri2;
            String equalityUri_atResolver = resolverUri + "/" + KEY_RESOLVER_EQUAL + "?" +
                        "1=" + CrsUtil.CrsUri(resolverUri, getAuthority(uri1), getVersion(uri1), getCode(uri1)) + "&" +
                        "2=" + CrsUtil.CrsUri(resolverUri, getAuthority(uri2), getVersion(uri2), getCode(uri2));
            Boolean equal = false;

            for (String equalityUri : new String[]{equalityUri_given, equalityUri_atResolver}) {
                try {
                    // Create InputStream and set the timeouts
                    URL url = new URL(equalityUri);
                    URLConnection con = url.openConnection();
                    con.setConnectTimeout(ConfigManager.CRSRESOLVER_CONN_TIMEOUT);
                    con.setReadTimeout(ConfigManager.CRSRESOLVER_READ_TIMEOUT);
                    InputStream inStream = con.getInputStream();
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
                            equalityUri + " returned an invalid document.", ex);
                } catch (ParsingException ex) {
                    throw new PetascopeException(ExceptionCode.XmlNotValid.locator(
                            "line: " + ex.getLineNumber() + ", column:" + ex.getColumnNumber()),
                            ex.getMessage(), ex);
                } catch (IOException ex) {
                    throw new SecoreException(ExceptionCode.InternalComponentError,
                            equalityUri + ": resolver exception: " + ex.getMessage(), ex);
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
         * @param uri   The URL of a CRS
         * @return      The authority of the CRS definition
         */
        public static String getAuthority(String uri) {
            String decUri = StringUtil.urldecode(uri, null);
            Pattern p;
            Matcher m;
            // KVP
            if (isKvp(decUri)) {
                p = Pattern.compile("^" + HTTP_URL_PATTERN + KEY_RESOLVER_CRS + "\\?.*((?i)" + AUTHORITY_KEY  + ")=([^&]+).*$");
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
                    if (m.groupCount() == 1) {
                        return m.group(1);
                    } else {
                        log.warn(decUri + " seems to be invalid.");
                    }
                }
            }
            return "";
        }
        /**
         * Extracts the version from a CRS URL.
         * @param uri   The URL of a CRS
         * @return      The version of the CRS definition
         */
        public static String getVersion(String uri) {
            String decUri = StringUtil.urldecode(uri, null);
            Pattern p;
            Matcher m;
            // KVP
            if (isKvp(decUri)) {
                p = Pattern.compile("^" + HTTP_URL_PATTERN + KEY_RESOLVER_CRS + "\\?.*((?i)" + VERSION_KEY  + ")=(.+).*$");
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
                    if (m.groupCount() == 1) {
                        return m.group(1);
                    } else {
                        log.warn(decUri + " seems to be invalid.");
                    }
                }
            }
            return "";
        }
        /**
         * Extracts the code from a CRS URL.
         * @param uri   The URL of a CRS
         * @return      The code of the CRS definition
         */
        public static String getCode(String uri) {
            // NOTE: `code' is generally a String, not integer (eg <resolver>/def/crs/OGC/0/Image1D)
            String decUri = StringUtil.urldecode(uri, null);
            Pattern p;
            Matcher m;
            // KVP
            if (isKvp(decUri)) {
                p = Pattern.compile("^" + HTTP_URL_PATTERN + KEY_RESOLVER_CRS + "\\?.*((?i)" + CODE_KEY  + ")=(.+).*$");
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
                    if (m.groupCount() == 1) {
                        return m.group(1);
                    }
                }
            }
            return "";
        }

        // Generalize the simple HashMap.get() method to include non-identical
        // but equivalent CRS URI (e.g. KVP/SOAP or KVP pairs order).
        /**
         * Generalization of HashMap.get() method to include non-identical
         * but equivalent CRS URI (eg KVP/SOAP or KVP pairs order).
         * @param uri   The URI which needs to be parsed
         * @return      The cached CrsDefinition, null otherwise
         * @throws PetascopeException
         * @throws SecoreException
         */
        public static CrsDefinition getCachedDefinition(String uri) throws PetascopeException, SecoreException {
            for (String cachedUri : parsedCRSs.keySet()) {
                if (areEquivalent(cachedUri, uri)) {
                    return parsedCRSs.get(cachedUri);
                }
            }
            log.warn(uri + " is not in cache as supposed: check with isCached() before calling this method.");
            return null;
        }

        /**
         * Builds a compound CRS from the input atomic CRS URIs.
         * @param crsUris
         * @return  The compounding of the listed CRS URIs.
         */
        public static String createCompound(List<String> crsUris) {
            String ccrsOut = "";
            switch (crsUris.size()) {
                case 0: // no URIs: return empty string
                    break;
                case 1: // Only one CRS: no need to compound
                    ccrsOut = crsUris.iterator().next();
                    break;
                default : // By default, use SECORE host in the CCRS URL
                    ccrsOut = getResolverUri() + "/" +  KEY_RESOLVER_CCRS + "?";
                    Iterator it = crsUris.iterator();
                    for (int i = 0; i < crsUris.size(); i++) {
                        ccrsOut += (i+1) + "=" + it.next();
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
         * @param singleCrsUri
         * @param leftAxesLabels
         * @throws PetascopeException
         * @throws SecoreException
         * @return buildSlicedUri("http://www.opengis.net/def/crs/EPSG/0/4327",{Lat,Long}) -> "http://www.opengis.net/def/crs/EPSG/0/4327@Lat,Long"
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
         * @param crsUris
         * @param axisLabel
         * @return True if axis is in the list of sliced axes. Example:
         * isSliced("http://www.opengis.net/def/crs/EPSG/0/4327@Long,h", "Lat") --> TRUE {only Long and h are left in th CRS}.
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
    }

    /**
     * Inner class which gathers the required geo-parameters for GTiff/JPEG2000 encoding.
     */
    public static class CrsProperties {
        /* Encoding parameters */
        private static final String CRS_PARAM  = "crs";
        private static final String XMAX_PARAM = "xmax";
        private static final String XMIN_PARAM = "xmin";
        private static final String YMAX_PARAM = "ymax";
        private static final String YMIN_PARAM = "ymin";
        private static final char PS = ';'; // parameter separator
        private static final char KVS = '='; // key-value separator

        /* Members */
        private double lowX;
        private double highX;
        private double lowY;
        private double highY;
        private String crs;

        /* Constructors */
        // Unreferenced gml:Grid
        public CrsProperties() {
            lowX  = 0.0D;
            highX = 0.0D;
            lowY  = 0.0D;
            highY = 0.0D;
            crs   = "";
        }
        // Georeferenced gml:RectifiedGrid
        public CrsProperties(double xMin, double xMax, double yMin, double yMax, String crs) {
            lowX  = xMin;
            highX = xMax;
            lowY  = yMin;
            highY = yMax;
            this.crs = crs;
        }
        public CrsProperties(String xMin, String xMax, String yMin, String yMax, String crs) {
            this(Double.parseDouble(xMin), Double.parseDouble(xMax),
                    Double.parseDouble(yMin), Double.parseDouble(yMax), crs);
        }

        /**
         * Returns the bounds of the requested coverage with trim-updates and
         * letting out sliced dimensions. Dimensionality of the bounds is
         * checked against the DIM argument.
         *
         * @param queryRoot The root node of the XML query, used to fetch trims
         * and slices
         * @param expectedDim expected dimensionality of queryRoot
         * @throws WCPSException
         */
        public CrsProperties(CoverageExpr queryRoot, Integer expectedDim) throws WCPSException {
            CoverageInfo info = queryRoot.getCoverageInfo();

            if (info != null) {
                // (order of subset) not necessarily = (order of coverage axes)
                Map<Integer, String> orderToName = new HashMap<Integer, String>();
                // axis name -> geo bounds
                Map<String, Double[]> nameToBounds = new HashMap<String, Double[]>();

                // Fetch the operations which change the geo bounding box (trim/extend)
                List<IRasNode> subsets = MiscUtil.childrenOfTypes(queryRoot, TrimCoverageExpr.class, ExtendCoverageExpr.class);

                // Check each dimension: slice->discard, trim/extend->setBounds, otherwise set bbox bounds
                for (int i = 0; i < info.getNumDimensions(); i++) {
                    String dimName = info.getDomainElement(i).getLabel();

                    // The dimension is surely in the output
                    if (!queryRoot.slicedAxis(dimName)) {
                        orderToName.put(info.getDomainIndexByName(dimName), dimName);

                        // Set the bounds of this dimension: total bbox first, then update in case of trims in the request
                        nameToBounds.put(dimName, new Double[] {
                            info.getDomainElement(info.getDomainIndexByName(dimName)).getMinValue().doubleValue(),
                            info.getDomainElement(info.getDomainIndexByName(dimName)).getMaxValue().doubleValue()
                        });

                        // reduce or extend the bbox according to the subset ops applied to the coverage
                        for (IRasNode subset : subsets) {
                            Double[] subsetBounds = null;
                            if (subset instanceof TrimCoverageExpr) {
                                if (((TrimCoverageExpr)subset).trimsDimension(dimName)) {
                                    // Set bounds specified in the trim (themselves trimmed by bbox values)
                                    subsetBounds = ((TrimCoverageExpr)subset).trimmingValues(dimName);
                                }
                            }
                            if (subset instanceof ExtendCoverageExpr) {
                                if (((ExtendCoverageExpr)subset).extendsDimension(dimName)) {
                                    // Set bounds specified in the trim (themselves trimmed by bbox values)
                                    subsetBounds = ((ExtendCoverageExpr)subset).extendingValues(dimName);
                                }
                            }
                            if (subsetBounds != null && subsetBounds.length > 0) {
                                nameToBounds.remove(dimName);
                                nameToBounds.put(dimName, subsetBounds);
                            }
                        }
                    }
                }

                // Check dimensions is exactly 2:
                if (orderToName.size() != expectedDim) {
                    String message = "The number of output dimensions " + orderToName.size()
                            + " does not match the expected dimensionality: " + expectedDim;
                    log.error(message);
                    throw new WCPSException(ExceptionCode.InvalidRequest, message);
                }

                // Set the bounds in the proper order (according to the order of the axes in the coverage
                Double[] dom1 = nameToBounds.get(orderToName.get(Collections.min(orderToName.keySet())));
                Double[] dom2 = nameToBounds.get(orderToName.get(Collections.max(orderToName.keySet())));

                // Output: min1, max1, min2, max2
                lowX = dom1[0];
                highX = dom1[1];
                lowY = dom2[0];
                highY = dom2[1];
            }
        }

        // Interface
        public double getXmin() {
            return lowX;
        }
        public double getXmax() {
            return highX;
        }
        public double getYmin() {
            return lowY;
        }
        public double getYmax() {
            return highY;
        }
        public String getCrs() {
            return crs;
        }
        public void setCrs(String crs) {
            this.crs = crs;
        }

        @Override
        public String toString() {
            return toString(null);
        }

        public String toString(String extraParams) {
            String ret = null;
            if (crs != null && !CrsUtil.GRID_CRS.equals(crs)) {
                ret = appendToExtraParams(
                        appendToExtraParams(
                        appendToExtraParams(
                        appendToExtraParams(
                        appendToExtraParams(extraParams, XMIN_PARAM, lowX + ""),
                                                         XMAX_PARAM, highX + ""),
                                                         YMIN_PARAM, lowY + ""),
                                                         YMAX_PARAM, highY + ""),
                                                         CRS_PARAM, CrsUtil.CrsUri.getAuthority(crs) + ":" + CrsUtil.CrsUri.getCode(crs));
            } else {
                // return empty in case of CRS:1
                ret = appendToExtraParams(extraParams, null, null);
            }
            return ret;
        }

        /**
         * Append key-value pair to existing extra parameters. Takes into account
         * if any argument is null.
         *
         * @return extraParams with key-value accordingly appended.
         */
        private String appendToExtraParams(String extraParams, String key, String value) {
            String param = (key == null || value == null) ? "" : key + KVS + value;
            String ret = null;

            if (extraParams == null || extraParams.length() == 0) {
                ret = param;
            } else {
                if (extraParams.toLowerCase().contains(PS + key + KVS)) {
                    ret = extraParams; // don't override if key is already supplied by user
                } else {
                    ret = extraParams + PS + param;
                }
            }

            return ret;
        }
    }
}
