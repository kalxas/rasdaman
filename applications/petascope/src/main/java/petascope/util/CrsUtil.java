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

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.geotools.referencing.CRS;
import org.geotools.geometry.jts.JTS;
import petascope.exceptions.ExceptionCode;
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
    public static final String OPENGIS_URI_PREFIX  = "http://www.opengis.net";
    public static final String ANY_URI_PREFIX      = "http://.*/";
    public static final String RESOLVER_CCRS_PATH  = "def/crs-compound";
    public static final String RESOLVER_CRS_PATH   = "def/crs";
    
    // Parsing the GML
    public static final String CRS_GMLSUFFIX = "CRS";
    public static final String CS_GMLSUFFIX = "CS";
    
    // NOTE: "CRS:1" axes to have a GML definition that will be parsed.
    public static final String GRID_CRS = "CRS:1";
    public static final String PIXEL_UOM = "pixels";
    
    public static final String CRS_DEFAULT_VERSION = "0";
    //public static final String CRS_DEFAULT_FORMAT  = "application/gml+xml";
    
    // TODO: define a URL to let SECORE return the supported authorities?
    public static final String EPSG_AUTH = "EPSG";
    public static final String ISO_AUTH  = "ISO";
    public static final String AUTO_AUTH = "AUTO";
    //public static final String IAU_AUTH  = "IAU2000";
    //public static final String UMC_AUTH  = "UMC";
    public static final List<String> SUPPORTED_AUTHS = Arrays.asList(EPSG_AUTH, ISO_AUTH, AUTO_AUTH); // IAU_AUTH, UMC_AUTH);
    
    // WGS84
    public static final String WGS84_EPSG_CODE = "4326";    
    public static final String WGS84_URI       = OPENGIS_URI_PREFIX + "/" + RESOLVER_CRS_PATH + "/" + EPSG_AUTH + "/" + CRS_DEFAULT_VERSION + "/" + WGS84_EPSG_CODE;
    
    /* caches: avoid EPSG db and SECORE redundant access */
    private static Map<List<String>,MathTransform> loadedTransforms = new HashMap<List<String>,MathTransform>();
    
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
     * @param   String committee    The committee defining the CRS (e.g. EPSG).
     * @param   int code            Code of the CRS.
     * @return  String              The URI of the CRS.
     */
    public static String CrsUri(String committee, int code ) {
        return OPENGIS_URI_PREFIX + "/" + RESOLVER_CRS_PATH + "/" + committee + "/" + CRS_DEFAULT_VERSION + "/" + code;
    }
    // @Override: shortcut ".../auth/0/"
    public static String CrsUri(String committee) {
        return OPENGIS_URI_PREFIX + "/" + RESOLVER_CRS_PATH + "/" + committee + "/" + CRS_DEFAULT_VERSION + "/";
    }
        
    /**
     * Nested class to offer utilities for CRS URI handling.
     */
    public static class CrsUri {
        
        private static final String COMPOUND_SPLIT   = "(\\?|&)\\d+=";
        private static final String COMPOUND_PATTERN = "^" + ANY_URI_PREFIX + RESOLVER_CCRS_PATH;
        
        private static final String AUTHORITY_KEY = "authority";    // Case-insensitivity is added in the pattern
        private static final String VERSION_KEY   = "version";
        private static final String CODE_KEY      = "code";
        //private static final String FORMAT_KEY    = "format";

        private static final String KV_PAIR = "(((?i)" + AUTHORITY_KEY  + ")=[^&]+|"  +
                                               "((?i)" + CODE_KEY       + ")=(\\d+)|" + 
                                             //"((?i)" + FORMAT_KEY     + ")=[^&]+|" +  // Add 1 KV_PAIR here below when enabled.        
                                               "((?i)" + VERSION_KEY    + ")=(\\d+))";
        private static final String KVP_CRS_PATTERN = "^" + 
                ANY_URI_PREFIX + RESOLVER_CRS_PATH + "\\?" +
                KV_PAIR + "&" + KV_PAIR + "&" + KV_PAIR + "$";

        private static final String REST_CRS_PATTERN = "^" + 
                ANY_URI_PREFIX + RESOLVER_CRS_PATH + "/[^/]+/\\d+/\\d+$";

        // In case the URI represents a CCRS, it return the list of atomic CRS it represents. 
        // NOTE: consistency of the URI should be first evaluated with isValid().
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
        
        // Soft test on prefix only: for overall validity of an URI use isValid().
        public static boolean isCompound(String uri) {            
            Pattern p = Pattern.compile(COMPOUND_PATTERN);
            Matcher m = p.matcher(uri);
            while (m.find()) {
                return true;
            }
            return false;
        }
        
        // isKvp() and isRest() work on atomic CRS
        public static boolean isKvp(String uri) {
            Pattern pKvp  = Pattern.compile(KVP_CRS_PATTERN);
            Matcher m     = pKvp.matcher(uri);
            while (m.find()) { return true; }
            return false;  
        }
        public static boolean isRest(String uri) {
            Pattern pRest = Pattern.compile(REST_CRS_PATTERN);            
            Matcher m     = pRest.matcher(uri);
            while (m.find()) { return true; }
            return false;
        }
        
        // Checks if an URI, compound or not, is consistent (assumption: need to be pointing to KAHLUA resolver)
        // NOTE: not completely free from leackages, but almost.
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
        
        // Tells whether 2 (different?) CRS URIs point to the same CRS definition.
        public static boolean areEquivalent(String uri1, String uri2) {            
            return getAuthority(uri1).equals(getAuthority(uri2))
                    && getVersion(uri1).equals(getVersion(uri2))
                    &&    getCode(uri1).equals(getCode(uri2));
        }
        
        // Getters (decomposeUri() first: they work on atomic CRS URIs, check validity as well first)
        public static String getAuthority(String uri) {
            Pattern p;
            Matcher m;
            // KVP
            if (isKvp(uri)) {
                p = Pattern.compile("^" + ANY_URI_PREFIX + RESOLVER_CRS_PATH + "\\?.*((?i)" + AUTHORITY_KEY  + ")=([^&]+).*$");
                m = p.matcher(uri);
                while (m.find()) {
                    if (m.groupCount() == 2)
                        return m.group(2);
                    else log.warn(uri + " seems to be invalid.");
                }
            }
            // REST
            if (isRest(uri)) {
                p = Pattern.compile("^" + ANY_URI_PREFIX + RESOLVER_CRS_PATH + "/([^/]+)/\\d+/\\d+$");
                m = p.matcher(uri);
                while (m.find()) {
                    if (m.groupCount() == 1)
                        return m.group(1);
                    else log.warn(uri + " seems to be invalid.");
                }
            }
            return "";
        }
        public static String getVersion(String uri) {
            Pattern p;
            Matcher m;
            // KVP
            if (isKvp(uri)) {
                p = Pattern.compile("^" + ANY_URI_PREFIX + RESOLVER_CRS_PATH + "\\?.*((?i)" + VERSION_KEY  + ")=(\\d+).*$");
                m = p.matcher(uri);
                while (m.find()) {
                    if (m.groupCount() == 2)
                        return m.group(2);
                    else log.warn(uri + " seems to be invalid.");
                }
            }
            // REST
            if (isRest(uri)) {
                p = Pattern.compile("^" + ANY_URI_PREFIX + RESOLVER_CRS_PATH + "/[^/]+/(\\d+)/\\d+$");
                m = p.matcher(uri);
                while (m.find()) {
                    if (m.groupCount() == 1)
                        return m.group(1);
                    else log.warn(uri + " seems to be invalid.");
                }
            }
            return "";
        }
        // NOTE: `code' is generally a String, not integer (eg <resolver>/def/crs/OGC/0/Image1D)
        public static String getCode(String uri) {
            Pattern p;
            Matcher m;
            // KVP 
            if (isKvp(uri)) {
                p = Pattern.compile("^" + ANY_URI_PREFIX + RESOLVER_CRS_PATH + "\\?.*((?i)" + CODE_KEY  + ")=(\\d+).*$");
                m = p.matcher(uri);
                while (m.find()) {
                    if (m.groupCount() == 2)
                        return m.group(2);
                    else log.warn(uri + " seems to be invalid.");
                }
            }
            // REST
            if (isRest(uri)) {
                p = Pattern.compile("^" + ANY_URI_PREFIX + RESOLVER_CRS_PATH + "/[^/]+/\\d+/(\\d+)$");
                m = p.matcher(uri);
                while (m.find()) {
                    if (m.groupCount() == 1)
                        return m.group(1);
                }
            }
            return "";
        }
        
        //public static String getFormat(String uri) {
        // String format = CRS_DEFAULT_FORMAT;
        // /parse format in URI/
        // return format;
        //}
        
        public static String createCompound(LinkedHashSet<String> crsUris) {
            String ccrsOut = OPENGIS_URI_PREFIX + "/" +  RESOLVER_CCRS_PATH + "?";
            Iterator it = crsUris.iterator();
            for (int i = 0; i < crsUris.size(); i++) {
                ccrsOut += (i+1) + "=" + it.next();
                if (it.hasNext()) ccrsOut += "&";
            }
            return ccrsOut;
        }
    }
}