/*
 * This file is part of rasdaman community.
 *
 * Rasdaman community is free software: you can redistribute it and/or modify
 * it under the terms of the GNU  General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Rasdaman community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU  General Public License for more details.
 *
 * You should have received a copy of the GNU  General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003 - 2010 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import nu.xom.*;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.geotools.referencing.CRS;
import org.geotools.geometry.jts.JTS;
import petascope.ConfigManager;
import petascope.core.CrsDefinition;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCSException;

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
 * @author <a href="mailto:cmppri@unife.it">Piero Campalani</a>
 */
public class CrsUtil {
    
    // NOTE: accept any URI format, but ask to SECORE: flattened definitions, less recursion.
    public static final  String OPENGIS_URI_PREFIX = "http://www.opengis.net";
    private static final String HTTP_URL_PATTERN   = "http://.*/"; 
    private static final String HTTP_PREFIX        = "http://";
    
    // SECORE keywords (URL is set in the ConfigManager)
    public static final String KEY_RESOLVER_CRS   = "crs";
    public static final String KEY_RESOLVER_CCRS  = "crs-compound";
    public static final String KEY_RESOLVER_EQUAL = "equal";
    
    // NOTE: "CRS:1" axes to have a GML definition that will be parsed.
    public static final String GRID_CRS = "CRS:1";
    public static final String PIXEL_UOM = "pixels";
    
    public static final String CRS_DEFAULT_VERSION = "0";
    //public static final String CRS_DEFAULT_FORMAT  = "application/gml+xml";
    
    // TODO: define a URL to let SECORE return the supported authorities?
    public static final String EPSG_AUTH = "EPSG";
    public static final String ISO_AUTH  = "ISO";
    public static final String AUTO_AUTH = "AUTO";
    public static final String OGC_AUTH  = "OGC";
    //public static final String IAU_AUTH  = "IAU2000";
    //public static final String UMC_AUTH  = "UMC";
    public static final List<String> SUPPORTED_AUTHS = Arrays.asList(EPSG_AUTH, ISO_AUTH, AUTO_AUTH, OGC_AUTH); // IAU_AUTH, UMC_AUTH);
    
    // WGS84
    public static final String WGS84_EPSG_CODE = "4326";
    
    /* caches: avoid EPSG db and SECORE redundant access */
    private static Map<List<String>, MathTransform> loadedTransforms = new HashMap<List<String>, MathTransform>();  // CRS reprojections
    private static Map<String, CrsDefinition>       parsedCRSs       = new HashMap<String, CrsDefinition>();        // CRS definitions
    private static Map<List<String>, Boolean>       crsComparisons   = new HashMap<List<String>, Boolean>();        // CRS equality tests
    
    private List<String> crssMap;
    private CoordinateReferenceSystem sCrsID, tCrsID;
    private static final Logger log = LoggerFactory.getLogger(CrsUtil.class);
    
    // Constructor
    public CrsUtil(String sCrs, String tCrs) throws WCSException {
        sCrsID=null;
        tCrsID=null;
        
        try {
            // TODO: allow non-EPSG CRSs.
            crssMap = new ArrayList<String>(2);
            crssMap.addAll(Arrays.asList(sCrs, tCrs));
            
            if (CrsUtil.loadedTransforms.containsKey(crssMap)) {
                log.info("CRS transform already loaded in memory.");
            } else {
                log.info("Previously unused CRS transform: create and load in memory.");
                sCrsID = CRS.decode(EPSG_AUTH + ":" + CrsUri.getCode(sCrs));
                tCrsID = CRS.decode(EPSG_AUTH + ":" + CrsUri.getCode(tCrs));
                MathTransform transform = CRS.findMathTransform(sCrsID, tCrsID);
                CrsUtil.loadedTransforms.put(crssMap, transform);                
            }
        } catch (NoSuchAuthorityCodeException e) {
            log.error("Could not find CRS " + (sCrsID==null?sCrs:tCrs) + " on the EPSG db: CRS transform impossible.");
            throw new WCSException(ExceptionCode.InvalidMetadata, "Unsopported or invalid CRS \"" + (sCrsID==null?sCrs:tCrs) + "\".");
        } catch (FactoryException e) {
            log.error("Error while decoding CRS " + (sCrsID==null?sCrs:tCrs) + "\n" + e.getMessage());
            throw new WCSException(ExceptionCode.InternalComponentError, "Error while instanciating new CrsUtil object.\".");
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new WCSException(ExceptionCode.InternalComponentError, "Error while instanciating new CrsUtil object.\".");
        }
    }
    
    // Interface (unuseful now)
    /**
     * @return Indicates whether the CrsUtil object has already been created: only DomainInterval/Points 
     *  read the WCPS CRS specification, so CrsUtil is called multiple times to transform e.g. a whole BBOX specification.
     */
    /*public CoordinateReferenceSystem getSourceCrs() {
        return sCrsID;
    }
    public CoordinateReferenceSystem getTargetCrs() {
        return tCrsID;
    }*/
    
    // Methods
    /** Overloaded transform methods:
     * @param   srcCoords       Array of input coordinates: min values first, max values next. E.g. [xMin,yMin,xMax,yMax].
     * @return  List<Double>    Locations transformed to targetCRS (defined at construction time).
     * @throws  WCSException
     */
    public List<Double> transform (double[] srcCoords) throws WCSException {
        
        try {
            double[] trasfCoords = new double[srcCoords.length];
            
            // Transform
            JTS.xform(CrsUtil.loadedTransforms.get(crssMap), srcCoords, trasfCoords);

            /* Re-order transformed coordinates: warping between different projections 
             * might change the ordering of the points. Eg with a small horizontal subsets
             * and a very wide vertical subset:
             *   
             *   EPSG:32634 (UTM 34N) |  EPSG:4326 (pure WGS84)
             *   230000x   3800000y   =  18.07lon   34.31lat  
             *   231000x   4500000y   =  17.82lon   40.61lat
             */
            double buf;
            for (int i = 0; i < trasfCoords.length/2; i++) {
                if (trasfCoords[i] > trasfCoords[i+trasfCoords.length/2]) {
                    // Need to swap the coordinates
                    buf = trasfCoords[i];
                    trasfCoords[i] = trasfCoords[i+trasfCoords.length/2];
                    trasfCoords[i+trasfCoords.length/2] = buf;
                }
            }
            
            // Format output
            List<Double> out = new ArrayList<Double>(srcCoords.length);            
            for (int i = 0; i < trasfCoords.length; i++)
                out.add(trasfCoords[i]);
            
            return out;
                        
        } catch (TransformException e) {
            log.error("Error while transforming coordinates.\n" + e.getMessage());
            throw new WCSException(ExceptionCode.InternalComponentError, "Error while transforming point.\".");
        } catch (ClassCastException e) {
            log.error("Inappropriate key when accessing CrsUtil.loadedTransforms.\n" + e.getMessage());
            throw new WCSException(ExceptionCode.InternalComponentError, "Error while transforming point.\".");
        } catch (NullPointerException e) {
            log.error("Null key when accessing CrsUtil.loadedTransforms.\n" + e.getMessage());
            throw new WCSException(ExceptionCode.InternalComponentError, "Error while transforming point.\".");
        } catch (Exception e) {
            log.error("Error while transforming point." + e.getMessage());
            throw new WCSException(ExceptionCode.InternalComponentError, "Error while transforming point.\".");
        }
    }
    // Dummy overload
    public List<Double> transform (String[] coords) throws WCSException {
        double[] doubleCoords = new double[coords.length];
        int i = 0;
        try {
            for (i = 0; i < coords.length; i++)
                doubleCoords[i] = Double.parseDouble(coords[i]);
        } catch (NumberFormatException ex) {
            throw new WCSException (ExceptionCode.InvalidParameterValue,
                    "Coordinate " + coords[i] + " seems wrong: could not parse to number.", ex);
        }
        // Call the real transform method:
        return transform(doubleCoords);
    }
    
    /**
     * @param   String crsID    OGC identification URL of CRS.
     * @return  boolean         True if a supported CRS (currently EPSG codes only).
     */
    public static boolean isSupportedCrsCode(String crsID) {
        try {
            // read from List e return
            return crsID.equals(GRID_CRS)
                    || (CrsUri.getAuthority(crsID).equals(EPSG_AUTH) && CrsUri.getVersion(crsID).equals(CRS_DEFAULT_VERSION)); //&& SUPPORTED_EPSG.contains(Integer.parseInt(CrsUri.getCode(crsID))));
            //      || CrsUri.getAuthority(crsID).equals(IAU_AUTH) && ....
            //      ...
        } catch (Exception e) {
            log.warn(e.getMessage());
            return false;
        }
    }
    
    /**
     * @param   String committee    The committee defining the CRS (e.g. EPSG)
     * @param   String version      Version of the CRS
     * @param   String code         Code of the CRS
     * @return  String              The URI of the CRS (based on SECORE URL by default)
     */
    public static String CrsUri(String committee, String version, String code) {
        return ConfigManager.SECORE_URL + "/" + KEY_RESOLVER_CRS + "/" + committee + "/" + version + "/" + code;
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
        return CrsUriDir(ConfigManager.SECORE_URL, committee, CRS_DEFAULT_VERSION);
    }  
    /**
     * Parser of GML definitions of Coordinate Reference Systems:
     * parse the specified (resolver) URI recursively and creates the CrsDefinition object(s) along.
     * @param  String URI   The URI of the /atomic/ CRS to be parsed (URI need to be decomposed first).
     */
    // (!!) Always use decomposeUri() output to feed this method: it currently understands single CRSs.
    public static CrsDefinition parseGmlDefinition(String crsUri) throws PetascopeException {        
        CrsDefinition crs = null;
        List<List<String>> axes = new ArrayList<List<String>>();
           
        // Check first if the definition is already in cache:
        if (CrsUri.isCached(crsUri)) {
            log.info(crsUri + " definition is already in cache: do not need to fetch GML definition.");
            return CrsUri.getCachedDefinition(crsUri);
        }

        // Check if the URI syntax is valid
        if (!CrsUri.isValid(crsUri)) {
            log.info(crsUri + " definition seems not valid.");
            throw new PetascopeException(ExceptionCode.InvalidMetadata, crsUri + " definition seems not valid.");
        }        
        
        // Need to parse the XML
        log.info(crsUri + " definition needs to be parsed from resolver.");
        String uom = "";
        String datumOrigin = ""; // for TemporalCRSs
        
        // TODO: allow retry to a resolver mirror in case the server is temporarily down.
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
            Element exEl = XMLUtil.firstChildRecursive(root, ".*" + XMLSymbols.LABEL_EXCEPTION_TEXT);                    
            if (exEl != null) {
                log.error(crsUri + ": " + exEl.getValue());
                throw new SecoreException(exEl.getValue());
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
                        URL uomUrl = new URL(uomAtt.getValue());
                        URLConnection uomCon = uomUrl.openConnection();
                        uomCon.setConnectTimeout(ConfigManager.CRSRESOLVER_CONN_TIMEOUT);
                        uomCon.setReadTimeout(ConfigManager.CRSRESOLVER_READ_TIMEOUT);
                        InputStream uomInStream = uomCon.getInputStream();
                        
                        // Build the document
                        Document uomDoc = XMLUtil.buildDocument(null, uomInStream);
                        Element uomRoot = uomDoc.getRootElement();
                        
                        // Catch some exception in the GML
                        Element uomExEl = XMLUtil.firstChildRecursive(root, XMLSymbols.LABEL_EXCEPTION_TEXT);
                        if (uomExEl != null) {
                            log.error(crsUri + ": " + uomExEl.getValue());
                            throw new SecoreException(uomExEl.getValue());
                        }
                        
                        // Get the UoM value
                        Element uomNameEl = XMLUtil.firstChildRecursive(uomRoot, XMLSymbols.LABEL_NAME);
                        if (uomNameEl == null) {
                            log.error(uom + ": UoM definition misses name.");
                            throw new PetascopeException(ExceptionCode.InvalidMetadata, "Invalid UoM definition: " + uom);
                        }
                        uomName = uomNameEl.getValue().split(" ")[0]; // Some UoM might have further comments after actual UoM (eg EPSG:4326)
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
                
            } // else: no need to parse the datum
        } catch (ValidityException ex) {
            throw new PetascopeException(ExceptionCode.InternalComponentError,
                    (uom.isEmpty() ? crsUri : uom) + " definition is not valid.", ex);
        } catch (ParsingException ex) {
            log.debug(ex.getMessage() + "\n at line " + ex.getLineNumber() + ", column " + ex.getColumnNumber());
            throw new PetascopeException(ExceptionCode.InternalComponentError,
                    (uom.isEmpty() ? crsUri : uom) + " definition is malformed.", ex);
        } catch (IOException ex) {
            throw new PetascopeException(ExceptionCode.InternalComponentError,
                    (uom.isEmpty() ? crsUri : uom) + ": could not connect to resolver. The site may be down.", ex);
            // Retry to connect to internal resolver? Extract WHAT(crs,uom...), AUTH, CODE, VERSION and ask Kahlua?
        } catch (Exception ex) {
            throw new PetascopeException(ExceptionCode.InternalComponentError,
                    (uom.isEmpty() ? crsUri : uom) + ": general exception while parsing definition.", ex);
        }
        
        /* ISSUE: order in rasdaman in always easting then northing, but
         * a CRS definition (e.g. EPSG:4326) might define Latitude as first axis.
         * In case of spatial axis: force easting first, always. 
         * See also System.setProperty() in petascope.PetascopeInterface.java.
         * Trace of true order remains in the CrsDefinition.
         */   
        forceXYorder(crs, axes);        
        for (List<String> axisMetadata : axes) {
            // All metadata of this axis is parsed: add it to CrsDefinition:
            crs.addAxis(axisMetadata.get(0), axisMetadata.get(1), axisMetadata.get(2));
        }
        
        if (crs == null) {
            throw new PetascopeException(ExceptionCode.InternalComponentError,
                    crsUri + ": could not parse the definition.");
        }
        
        // Cache the definition
        parsedCRSs.put(crsUri, crs);
        log.info(crsUri + " into cache for future (inter-requests) use.");
        
        return crs;
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
     * Nested class to offer utilities for CRS URI handling.
     */
    public static class CrsUri {
        
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
            List<String> crss = new ArrayList<String>();
            
            if (isCompound(uri)) {
                String[] splitted = uri.split(COMPOUND_SPLIT);
                log.debug(Arrays.toString(splitted));
                if (splitted.length <= 1) log.warn(uri + " seems invalid: check consitency first.");
                if (splitted.length == 2) log.warn(uri + " seems compound but only one CRS is listed.");
                // The first element of the splitted String is the definition prefix: ignore it.
                for (int i=0; i<splitted.length; i++) {
                    if (i>0) {
                        crss.add(splitted[i]);
                        log.debug("Found atomic CRS from compound:" + splitted[i]);
                    }
                }
            } else {
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
            Matcher m = p.matcher(uri);
            while (m.find()) {
                return true;
            }
            return false;
        }

        /**
         * Checks if a specified URI (or an equivalent one) has already been cached.
         * @param uri
         * @return True if uri's definition has already been parsed and cached.
         */
        public static boolean isCached(String uri) throws PetascopeException {
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
            Matcher m     = pKvp.matcher(uri);
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
            Matcher m     = pRest.matcher(uri);
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

            if (crss.isEmpty()) return false;
            for (String current : crss) {
                if (current.equals(GRID_CRS)) return true; // TODO: if CRS:1 is replaces by URI, need 'areEquivalent() instead
                if (isKvp(current) || isRest(current)) {
                    // Check if authority is supported (SECORE is now bottleneck) as well:
                    // --> http://kahlua.eecs.jacobs-university.de:8080/def/crs/browse.jsp <--
                    if (!SUPPORTED_AUTHS.contains(getAuthority(current)))
                        return false;
                } else return false;
            }
            return true;
        }
        
        /**
         * Return true whether 2 CRS URLs are equivalent definitions.
         * It exploits SECORE's equality handling capabilities and caches new comparisons.
         * @param uri1
         * @param uri2
         * @return true if uri1 and uri2 point to the same GML definition
         */
        public static boolean areEquivalent(String uri1, String uri2) throws PetascopeException {            
            //return getAuthority(uri1).equals(getAuthority(uri2))
            //        && getVersion(uri1).equals(getVersion(uri2))
            //        &&    getCode(uri1).equals(getCode(uri2));
            
            // Test if Strings are exactly equal, then no need to ask SECORE
            if (uri1.equals(uri2)) {
                return true;
            }          
            
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
                // New comparison: need to ask SECORE
                log.info(getAuthority(uri1) + "(" + getVersion(uri1) + "):" + getCode(uri1) + "/" +
                         getAuthority(uri2) + "(" + getVersion(uri2) + "):" + getCode(uri2) + " " + 
                        "comparison is *not* cached: need to ask SECORE.");
                String equalityUri = ConfigManager.SECORE_URL + "/" + KEY_RESOLVER_EQUAL + "?" +
                        "1=" + URLs.get(0) + "&" +
                        "2=" + URLs.get(1);
                Boolean equal;
                
                try {
                    // Create InputStream and set the timeouts
                    URL url = new URL(equalityUri);
                    URLConnection con = url.openConnection();
                    con.setConnectTimeout(ConfigManager.CRSRESOLVER_CONN_TIMEOUT);
                    con.setReadTimeout(ConfigManager.CRSRESOLVER_READ_TIMEOUT);
                    InputStream inStream = con.getInputStream();
                    
                    // Build the document
                    Document doc = XMLUtil.buildDocument(null, inStream);
                    Element root = doc.getRootElement();
                    
                    // Catch some exception
                    Element exEl = XMLUtil.firstChildRecursive(root, XMLSymbols.LABEL_EXCEPTION_TEXT);
                    if (exEl != null) {
                        log.error("Error while comparing " +getAuthority(uri1) + "(" + getVersion(uri1) + "):" + 
                                getCode(uri1) + "/" + getAuthority(uri2) + "(" + getVersion(uri2) + "):" + 
                                getCode(uri2) + ": " + exEl.getValue());
                        throw new SecoreException(exEl.getValue());
                    } else {   
                        // Cache this new comparison
                        Element eqEl = XMLUtil.firstChildRecursive(root, XMLSymbols.LABEL_EQUAL);
                        equal = Boolean.parseBoolean(eqEl.getValue());
                    }
                    
                } catch (ParsingException ex) {
                    throw new PetascopeException(ExceptionCode.XmlNotValid.locator(
                            "line: " + ex.getLineNumber() + ", column:" + ex.getColumnNumber()),
                            ex.getMessage(), ex);
                } catch (SecoreException ex) {
                    throw ex;
                } catch (Exception ex) {
                    throw new PetascopeException(ExceptionCode.XmlNotValid, ex.getMessage(), ex);
                }
                
                // cache comparison
                crsComparisons.put(URLs, equal);
                return equal;
            }
        }
        
        // Getters (decomposeUri() first: they work on atomic CRS URIs, check validity as well first)
        /**
         * Extracts the authority from a CRS URL.
         * @param uri   The URL of a CRS
         * @return      The authority of the CRS definition
         */
        public static String getAuthority(String uri) {
            Pattern p;
            Matcher m;
            // KVP
            if (isKvp(uri)) {
                p = Pattern.compile("^" + HTTP_URL_PATTERN + KEY_RESOLVER_CRS + "\\?.*((?i)" + AUTHORITY_KEY  + ")=([^&]+).*$");
                m = p.matcher(uri);
                while (m.find()) {
                    if (m.groupCount() == 2)
                        return m.group(2);
                    else log.warn(uri + " seems to be invalid.");
                }
            }
            // REST
            if (isRest(uri)) {
                p = Pattern.compile("^" + HTTP_URL_PATTERN + KEY_RESOLVER_CRS + "/([^/]+)/.+/.+$");
                m = p.matcher(uri);
                while (m.find()) {
                    if (m.groupCount() == 1)
                        return m.group(1);
                    else log.warn(uri + " seems to be invalid.");
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
            Pattern p;
            Matcher m;
            // KVP
            if (isKvp(uri)) {
                p = Pattern.compile("^" + HTTP_URL_PATTERN + KEY_RESOLVER_CRS + "\\?.*((?i)" + VERSION_KEY  + ")=(.+).*$");
                m = p.matcher(uri);
                while (m.find()) {
                    if (m.groupCount() == 2)
                        return m.group(2);
                    else log.warn(uri + " seems to be invalid.");
                }
            }
            // REST
            if (isRest(uri)) {
                p = Pattern.compile("^" + HTTP_URL_PATTERN + KEY_RESOLVER_CRS + "/[^/]+/(.+)/.+$");
                m = p.matcher(uri);
                while (m.find()) {
                    if (m.groupCount() == 1)
                        return m.group(1);
                    else log.warn(uri + " seems to be invalid.");
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
            Pattern p;
            Matcher m;
            // KVP 
            if (isKvp(uri)) {
                p = Pattern.compile("^" + HTTP_URL_PATTERN + KEY_RESOLVER_CRS + "\\?.*((?i)" + CODE_KEY  + ")=(.+).*$");
                m = p.matcher(uri);
                while (m.find()) {
                    if (m.groupCount() == 2)
                        return m.group(2);
                    else log.warn(uri + " seems to be invalid.");
                }
            }
            // REST
            if (isRest(uri)) {
                p = Pattern.compile("^" + HTTP_URL_PATTERN + KEY_RESOLVER_CRS + "/[^/]+/.+/(.+)$");
                m = p.matcher(uri);
                while (m.find()) {
                    if (m.groupCount() == 1)
                        return m.group(1);
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
         */
        public static CrsDefinition getCachedDefinition(String uri) throws PetascopeException {
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
        public static String createCompound(LinkedHashSet<String> crsUris) {
            if (crsUris.size() == 1) {
                // Only one CRS: no need to compound
                return (crsUris.iterator().next());
            } else {
                // By default, use SECORE host in the CCRS URL
                String ccrsOut = ConfigManager.SECORE_URL + "/" +  KEY_RESOLVER_CCRS + "?";
                Iterator it = crsUris.iterator();
                for (int i = 0; i < crsUris.size(); i++) {
                    ccrsOut += (i+1) + "=" + it.next();
                    if (it.hasNext()) ccrsOut += "&";
                }
                return ccrsOut;
            }
        }
    }
}