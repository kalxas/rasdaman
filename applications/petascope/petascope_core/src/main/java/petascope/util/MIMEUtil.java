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
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
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
    public static final String MIME_DTED = "application/x-ogc-dted";
    public static final String MIME_EHDR = "application/x-ogc-ehdr";
    public static final String MIME_ELAS = "application/x-ogc-elas";
    public static final String MIME_ENVI = "application/x-ogc-envi";
    public static final String MIME_ERS = "application/x-ogc-ers";
    public static final String MIME_FIT = "application/x-ogc-fit";
    public static final String MIME_FITS = "application/x-ogc-fits";
    public static final String MIME_GIF = "image/gif";
    public static final String MIME_GMT = "application/x-netcdf-gmt";
    public static final String MIME_GS7BG = "application/x-ogc-gs7bg";
    public static final String MIME_GSAG = "application/x-ogc-gsag";
    public static final String MIME_GSBG = "application/x-ogc-gsbg";
    public static final String MIME_GTA = "application/x-ogc-gta";
    public static final String MIME_HF2 = "application/x-ogc-hf2";
    public static final String MIME_HFA = "application/x-erdas-hfa";
    public static final String MIME_IDA = "application/x-ogc-ida";
    public static final String MIME_INGR = "application/x-ogc-ingr";
    public static final String MIME_ISIS2 = "application/x-ogc-isis2";
    public static final String MIME_LAN = "application/x-erdas-lan";
    public static final String MIME_MFF2 = "application/x-ogc-mff2";
    public static final String MIME_NITF = "application/x-ogc-nitf";
    public static final String MIME_PAUX = "application/x-ogc-paux";
    public static final String MIME_PCIDSK = "application/x-ogc-pcidsk";
    public static final String MIME_PCRASTER = "application/x-ogc-pcraster";
    public static final String MIME_PDF = "application/x-ogc-pdf";
    public static final String MIME_PNM = "application/x-ogc-pnm";
    public static final String MIME_R = "text/x-r";
    public static final String MIME_RMF = "application/x-ogc-rmf";
    public static final String MIME_SGI = "image/x-sgi";
    public static final String MIME_VRT = "application/x-ogc-vrt";
    public static final String MIME_XPM = "image/xpm";
    public static final String MIME_ZMAP = "application/x-ogc-zmap";

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
    public static final String ENCODE_DTED = "dted";
    public static final String ENCODE_EHDR = "ehdr";
    public static final String ENCODE_ELAS = "elas";
    public static final String ENCODE_ENVI = "envi";
    public static final String ENCODE_ERS = "ers";
    public static final String ENCODE_FIT = "fit";
    public static final String ENCODE_FITS = "fits";
    public static final String ENCODE_GIF = "gif";
    public static final String ENCODE_GMT = "gmt";
    public static final String ENCODE_GS7BG = "gs7bg";
    public static final String ENCODE_GSAG = "gsag";
    public static final String ENCODE_GSBG = "gsbg";
    public static final String ENCODE_GTA = "gta";
    public static final String ENCODE_HF2 = "hf2";
    public static final String ENCODE_HFA = "hfa";
    public static final String ENCODE_IDA = "ida";
    public static final String ENCODE_INGR = "ingr";
    public static final String ENCODE_ISIS2 = "isis2";
    public static final String ENCODE_LAN = "lan";
    public static final String ENCODE_MFF2 = "mff2";
    public static final String ENCODE_NITF = "nitf";
    public static final String ENCODE_PAUX = "paux";
    public static final String ENCODE_PCIDSK = "pcidsk";    
    public static final String ENCODE_PCRASTER = "pcraster";
    public static final String ENCODE_PDF = "pdf";
    public static final String ENCODE_PNM = "pnm";
    public static final String ENCODE_R = "r";
    public static final String ENCODE_RMF = "rmf";
    public static final String ENCODE_SGI = "sgi";
    public static final String ENCODE_VRT = "vrt";
    public static final String ENCODE_XPM = "xpm";
    public static final String ENCODE_ZMAP = "zmap";    

    // supporting codecs
    public static final String CODEC = "codec";
    public static final String FORMAT_ID_JP2 = "jpeg2000";
    public static final String FORMAT_ID_OPENJP2 = "jp2openjpeg";

    // Rasql must use Jp2OpenJpeg with "codec":"jp2" to have geo-reference metadata in encoding    
    public static final String CODEC_JP2 = "jp2";

    // Metadata for JPEG2000 (jp2openjpeg driver)
    public static final String FORMAT_ID_OPENJP2_CODEC = "codec=jp2";

    private static Map<String, String> mimeTypesMap;

    private static void init() {
        // Add the mime type and rasdaman data type
        mimeTypesMap = new LinkedHashMap<>();
        // No encode gml in rasql, only in WCS/WCPS
        mimeTypesMap.put(MIME_GML, ENCODE_GML);
        mimeTypesMap.put(MIME_JPEG, ENCODE_JPEG);
        mimeTypesMap.put(MIME_PNG, ENCODE_PNG);
        mimeTypesMap.put(MIME_TIFF, ENCODE_TIFF);
        mimeTypesMap.put(MIME_BMP, ENCODE_BMP);
        mimeTypesMap.put(MIME_JP2, ENCODE_JPEG2000);
        mimeTypesMap.put(MIME_NETCDF, ENCODE_NETCDF);
        mimeTypesMap.put(MIME_CSV, ENCODE_CSV);
        mimeTypesMap.put(MIME_JSON, ENCODE_JSON);
        mimeTypesMap.put(MIME_DEM, ENCODE_DEM);
        mimeTypesMap.put(MIME_DTED, ENCODE_DTED);
        mimeTypesMap.put(MIME_EHDR, ENCODE_EHDR);
        mimeTypesMap.put(MIME_ELAS, ENCODE_ELAS);
        mimeTypesMap.put(MIME_ENVI, ENCODE_ENVI);
        mimeTypesMap.put(MIME_ERS, ENCODE_ERS);
        mimeTypesMap.put(MIME_FIT, ENCODE_FIT);
        mimeTypesMap.put(MIME_FITS, ENCODE_FITS);
        mimeTypesMap.put(MIME_GIF, ENCODE_GIF);
        mimeTypesMap.put(MIME_GMT, ENCODE_GMT);
        mimeTypesMap.put(MIME_GS7BG, ENCODE_GS7BG);
        mimeTypesMap.put(MIME_GSAG, ENCODE_GSAG);
        mimeTypesMap.put(MIME_GSBG, ENCODE_GSBG);
        mimeTypesMap.put(MIME_GTA, ENCODE_GTA);
        mimeTypesMap.put(MIME_HF2, ENCODE_HF2);
        mimeTypesMap.put(MIME_HFA, ENCODE_HFA);
        mimeTypesMap.put(MIME_IDA, ENCODE_IDA);
        mimeTypesMap.put(MIME_INGR, ENCODE_INGR);
        mimeTypesMap.put(MIME_ISIS2, ENCODE_ISIS2);
        mimeTypesMap.put(MIME_LAN, ENCODE_LAN);
        mimeTypesMap.put(MIME_MFF2, ENCODE_MFF2);
        mimeTypesMap.put(MIME_NITF, ENCODE_NITF);
        mimeTypesMap.put(MIME_PAUX, ENCODE_PAUX);
        mimeTypesMap.put(MIME_PCIDSK, ENCODE_PCIDSK);
        mimeTypesMap.put(MIME_PCRASTER, ENCODE_PCRASTER);
        mimeTypesMap.put(MIME_PDF, ENCODE_PDF);
        mimeTypesMap.put(MIME_PNM, ENCODE_PNM);
        mimeTypesMap.put(MIME_R, ENCODE_R);
        mimeTypesMap.put(MIME_RMF, ENCODE_RMF);
        mimeTypesMap.put(MIME_SGI, ENCODE_SGI);
        mimeTypesMap.put(MIME_VRT, ENCODE_VRT);
        mimeTypesMap.put(MIME_XPM, ENCODE_XPM);
        mimeTypesMap.put(MIME_ZMAP, ENCODE_ZMAP);
    }

    /**
     * Return the maps which only can read keys, values
     *
     * @return
     */
    private static Map<String, String> getInstance() {
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
        for (Map.Entry<String, String> entry : getInstance().entrySet()) {
            String mimeType = entry.getKey();
            // e.g: mime is image/png, type is png
            if (mimeType.contains(encodingType)) {
                return mimeType;
            }
        }

        // if encoding does not exist, mean it is not supported
        throw new PetascopeException(ExceptionCode.UnsupportedEncodingFormat, "Encoding format '" + encodingType + "' is not supported.");
    }

    /**
     * Return the encoding type of a mimeType (e.g: image/jpeg -> [jpg
     * (not supported), jpeg])
     *
     * @param mimeType
     * @return
     * @throws petascope.exceptions.PetascopeException
     */
    public static String getFormatType(String mimeType) throws PetascopeException {
        String formatType = getInstance().get(mimeType);
        if (formatType == null) {
            throw new PetascopeException(ExceptionCode.InvalidRequest, "MIME type is not supported, given '" + mimeType + "'.");
        }
        return formatType;
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
        } else {
            // just return the extension as format type when the mimeType is not well-known
            return getFormatType(mimeType);
        }
    }
    
    /**
     * Check if the MIME output is an 2D image which can be displayable.
     */
    public static boolean displayableMIME(String encode) {
        if (encode.equalsIgnoreCase(ENCODE_PNG)
            || encode.equalsIgnoreCase(ENCODE_JPEG)
            || encode.equalsIgnoreCase(ENCODE_BMP)
            || encode.equalsIgnoreCase(ENCODE_TIFF)
            || encode.equalsIgnoreCase(ENCODE_GTIFF)
            || encode.equalsIgnoreCase(ENCODE_GIF)
            || encode.equalsIgnoreCase(ENCODE_JPEG2000)) {
            return true;
        }
        return false;
    }
}
