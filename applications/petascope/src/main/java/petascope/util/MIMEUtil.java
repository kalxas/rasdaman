package petascope.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.WCSException;

/**
 * Class stores the mime type and rasdaman data type
 *
 @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
public class MIMEUtil {

    // mime types (gml+xml needs to be the first wcs:formatSupported in GetCapabilities result for OGC CITE test)
    public static final String MIME_GML = "application/gml+xml";
    public static final String MIME_JPEG = "image/jpeg";
    public static final String MIME_PNG = "image/png";
    public static final String MIME_TIFF = "image/tiff";
    public static final String MIME_BMP = "image/bmp";
    public static final String MIME_JP2 = "image/jp2";

    public static final String MIME_NETCDF = "application/netcdf";
    public static final String MIME_CSV = "text/csv";
    public static final String MIME_JSON = "application/json";
    // This does not exist in the MIME standard list
    public static final String MIME_DEM = "application/dem";
    // Only used for response mime type as Browser will download the file instead of displaying it when encoding format is gml
    public static final String MIME_XML = "text/xml";

    // rasdaman types
    public static final String ENCODE_JPEG = "jpeg";
    public static final String ENCODE_PNG = "png";
    public static final String ENCODE_GTIFF = "GTiff";
    public static final String ENCODE_TIFF = "tiff";
    public static final String ENCODE_BMP = "bmp";
    public static final String ENCODE_JPEG2000 = "jpeg2000";

    public static final String ENCODE_NETCDF = "netcdf";
    public static final String ENCODE_CSV = "csv";
    public static final String ENCODE_JSON = "json";
    // Only supported when it is specified in WCPS format
    public static final String ENCODE_DEM = "dem";
    // this encoding only exists in Petascope to return the GML for a coverage
    public static final String ENCODE_GML = "gml";

    // supporting codecs
    public static final String CODEC = "codec";
    public static final String FORMAT_ID_JP2 = "jpeg2000";
    public static final String FORMAT_ID_OPENJP2 = "jp2openjpeg";

    // Rasql must use Jp2OpenJpeg with "codec":"jp2" to have geo-reference metadata in encoding    
    public static final String CODEC_JP2 = "jp2";

    // Metadata for JPEG2000 (jp2openjpeg driver)
    public static final String FORMAT_ID_OPENJP2_CODEC = "codec=jp2";

    private static Map<String, List<String>> mimeTypesMap;

    private static void init() {
        // Add the mime type and rasdaman data type
        mimeTypesMap = new LinkedHashMap<>();
        // No encode gml in rasdaman, only in WCPS
        mimeTypesMap.put(MIME_GML, new ArrayList<>(Arrays.asList(ENCODE_GML)));
        mimeTypesMap.put(MIME_JPEG, new ArrayList<>(Arrays.asList(ENCODE_JPEG)));
        mimeTypesMap.put(MIME_PNG, new ArrayList<>(Arrays.asList(ENCODE_PNG)));
        mimeTypesMap.put(MIME_TIFF, new ArrayList<>(Arrays.asList(ENCODE_GTIFF, ENCODE_TIFF)));
        mimeTypesMap.put(MIME_BMP, new ArrayList<>(Arrays.asList(ENCODE_BMP)));
        mimeTypesMap.put(MIME_JP2, new ArrayList<>(Arrays.asList(ENCODE_JPEG2000)));
        mimeTypesMap.put(MIME_NETCDF, new ArrayList<>(Arrays.asList(ENCODE_NETCDF)));
        mimeTypesMap.put(MIME_CSV, new ArrayList<>(Arrays.asList(ENCODE_CSV)));
        mimeTypesMap.put(MIME_JSON, new ArrayList<>(Arrays.asList(ENCODE_JSON)));
        mimeTypesMap.put(MIME_DEM, new ArrayList<>(Arrays.asList(ENCODE_DEM)));
    }

    /**
     * Return the maps which only can read keys, values
     *
     * @return
     */
    private static Map<String, List<String>> getInstance() {
        if (mimeTypesMap == null) {
            init();
        }

        return mimeTypesMap;
    }

    /**
     * Return the MIME type of a rasdaman encoding type (e.g: png -> image/png)
     *
     * @param encodingType
     * @return
     * @throws petascope.exceptions.PetascopeException
     */
    public static String getMimeType(String encodingType) throws PetascopeException {
        for (Map.Entry<String, List<String>> entry : getInstance().entrySet()) {
            String mimeType = entry.getKey();
            List<String> encodingTypes = entry.getValue();
            if (encodingTypes.contains(encodingType)) {
                return mimeType;
            }
        }

        // if encoding does not exist, mean it is not supported
        throw new PetascopeException(ExceptionCode.UnsupportedEncodingFormat, "Encoding format: " + encodingType + " is not supported.");
    }

    /**
     * Return the list of encoding types of a mimeType (e.g: image/jpeg -> [jpg
     * (not supported), jpeg])
     *
     * @param mimeType
     * @return
     * @throws petascope.exceptions.PetascopeException
     */
    public static List<String> getEncodingType(String mimeType) throws PetascopeException {
        List<String> formatTypes = getInstance().get(mimeType);
        if (formatTypes == null) {
            throw new PetascopeException(ExceptionCode.InvalidRequest, "MIME type is not supported, given: " + mimeType);
        }
        return formatTypes;
    }

    /**
     * Return the list of MimeTypes
     *
     * @return
     */
    public static List<String> getAllMimeTypes() {
        Set<String> keySet = getInstance().keySet();

        return new ArrayList<>(keySet);
    }

    /**
     * Return the file name extension for a MIME type e.g: image/png -> .png,
     * application/netcdf -> .nc, application/gml+xml -> .gml
     *
     * @param mimeType
     * @return
     * @throws petascope.exceptions.PetascopeException
     */
    public static String getFileNameExtension(String mimeType) throws PetascopeException {
        if (mimeType.equals(MIME_GML)) {
            return ENCODE_GML;
        } else if (mimeType.equals(MIME_JPEG)) {
            return ENCODE_JPEG;
        } else if (mimeType.equals(MIME_PNG)) {
            return ENCODE_PNG;
        } else if (mimeType.equals(MIME_TIFF)) {
            return ENCODE_TIFF;
        } else if (mimeType.equals(MIME_BMP)) {
            return ENCODE_BMP;
        } else if (mimeType.equals(MIME_JP2)) {
            return "jp2";
        } else if (mimeType.equals(MIME_NETCDF)) {
            return "nc";
        } else if (mimeType.equals(MIME_CSV)) {
            return "csv";
        } else if (mimeType.equals(MIME_JSON)) {
            return "json";
        } else if (mimeType.equals(MIME_DEM)) {
            return ".data";
        } else if (mimeType.equals("")) {
            // It is used in case result returns a number, e.g: return avg(c)
            // MIME XML so Browser will display result instead of downloading as a file if set to MIME csv 
            return MIME_XML;
        }

        throw new WCSException(ExceptionCode.InvalidRequest, "MIME type is not supported, given: " + mimeType);
    }
}
