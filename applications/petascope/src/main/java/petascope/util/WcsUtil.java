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
 * GNU General Public License for more details.
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

import java.io.StringWriter;
import java.util.Arrays;
import java.util.Set;
import javax.xml.bind.JAXBException;
import net.opengis.ows.v_1_0_0.ExceptionReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petascope.PetascopeXmlNamespaceMapper;
import petascope.core.CoverageMetadata;
import petascope.core.DbMetadataSource;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.WCSException;
import petascope.wcps.server.core.Bbox;
import petascope.wcps.server.core.DomainElement;
import petascope.wcs2.parsers.GetCoverageMetadata;
import petascope.wcs2.parsers.GetCoverageMetadata.RangeField;
import petascope.wcs2.templates.Templates;

/**
 * WCS utility methods.
 *
 * @author <a href="mailto:d.misev@jacobs-university.de">Dimitar Misev</a>
 */
public class WcsUtil {

    private static final Logger log = LoggerFactory.getLogger(WcsUtil.class);
    
    /* Constants */
    public static final String KEY_CHAR   = "char";
    public static final String KEY_UCHAR  = "unsigned char";
    public static final String KEY_SHORT  = "short";
    public static final String KEY_USHORT = "unsigned short";
    public static final String KEY_INT    = "int";
    public static final String KEY_UINT   = "unsigned int";
    public static final String KEY_LONG   = "long";
    public static final String KEY_ULONG  = "unsigned long";
    public static final String KEY_FLOAT  = "float";
    public static final String KEY_DOUBLE = "double";
    
    public static final String CHAR_MIN   = "-128";
    public static final String CHAR_MAX   = "127";
    public static final String UCHAR_MIN  = "0";
    public static final String UCHAR_MAX  = "255";
    public static final String SHORT_MIN  = "-32768";
    public static final String SHORT_MAX  = "32767";
    public static final String USHORT_MIN = "0";
    public static final String USHORT_MAX = "65535";
    public static final String INT_MIN    = "-2147483648";
    public static final String INT_MAX    = "2147483647";
    public static final String UINT_MIN   = "0";
    public static final String UINT_MAX   = "4294967295";
    public static final String LONG_MIN   = "-9223372036854775808";
    public static final String LONG_MAX   = "9223372036854775807";
    public static final String ULONG_MIN  = "0";
    public static final String ULONG_MAX  = "18446744073709551615";
    public static final String FLOAT_MIN  = "+/-3.4e-38";
    public static final String FLOAT_MAX  = "+/-3.4e+38";
    public static final String DOUBLE_MIN = "+/-1.7e-308";
    public static final String DOUBLE_MAX = "+/-1.7e+308";

    /**
     * Utility method to read coverage's metadata
     */
    public static CoverageMetadata getMetadata(DbMetadataSource meta, String coverageId) throws WCSException {
        try {
            return meta.read(coverageId);
        } catch (Exception e) {
            e.printStackTrace();
            throw new WCSException(ExceptionCode.NoApplicableCode.locator(coverageId),
                    "Metadata for coverage '" + coverageId + "' is not valid.");
        }
    }

    /**
     * Transforms a csv output returned by rasdaman server into a csv format
     * accepted by the gml:tupleList according to section 19.3.8 of the
     * OGC GML standard version 3.2.1
     * @param csv - a csv input like {b1 b2 ... bn, b1 b2 ... bn, ...}, {...}
     * where each {...} represents a dimension and each sequence b1 ... bn n bands
     * @return csv string of form b1 b2 .. bn, b1 b2 ... bn, ...
     */
    protected static String rasCsvToTupleList(String csv) {
        return csv.replace("{", "").replace("}","").replace("\"", "");
    }

    /**
     * Convert csv format from rasdaman into a tupleList format, for including
     * in a gml:DataBlock
     *
     * @param csv coverage in csv format
     * @return tupleList representation
     */
    public static String csv2tupleList(String csv) {
        return rasCsvToTupleList(csv); // FIXME
    }

    /**
     * Convert spatial domain of the form [band1][band2]..., where band1 is of
     * the form [low:high, low:high,...]
     *
     * @param sdom spatial domain as retreived from rasdaman with sdom(coverage)
     * @return (low, high) bound
     */
    public static Pair<String, String> sdom2bounds(String sdom) {
        sdom = sdom.replaceAll("\\[", "");
        sdom = sdom.substring(0, sdom.length() - 1);
        String[] bands = sdom.split("\\]");

        int n = bands[0].split(",").length;
        Double[] low = new Double[n];
        Arrays.fill(low, Double.POSITIVE_INFINITY);
        Double[] high = new Double[n];
        Arrays.fill(high, Double.NEGATIVE_INFINITY);

        for (String band : bands) {
            String[] dims = band.split(",");
            for (int i = 0; i < dims.length; i++) {
                String[] bounds = dims[i].split(":");
                try {
                    Double l = Double.parseDouble(bounds[0]);
                    low[i] = Math.min(low[i], l);
                } catch (NumberFormatException ex) {
                    log.warn("Error parsing " + bounds[0], ex);
                }
                try {
                    Double h = Double.parseDouble(bounds[1]);
                    high[i] = Math.max(high[i], h);
                } catch (NumberFormatException ex) {
                    log.warn("Error parsing " + bounds[1], ex);
                }
            }
        }
        String l = "", h = "";
        for (Double t : low) {
            l += StringUtil.d2s(t) + " ";
        }
        for (Double t : high) {
            h += StringUtil.d2s(t) + " ";
        }
        return Pair.of(l.trim(), h.trim());
    }

    public static String exceptionReportToXml(ExceptionReport report) {
        log.info(report.getException().get(0).getLocator());
        log.info(report.getException().get(0).getExceptionCode());
        String output = null;
        try {
            javax.xml.bind.JAXBContext jaxbCtx = javax.xml.bind.JAXBContext.
                    newInstance(report.getClass().getPackage().getName());
            javax.xml.bind.Marshaller marshaller = jaxbCtx.createMarshaller();
            marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_ENCODING, "UTF-8"); //NOI18N
            marshaller.setProperty("jaxb.formatted.output", true);
            marshaller.setProperty("jaxb.schemaLocation",
                    "http://www.opengis.net/ows http://schemas.opengis.net/ows/2.0/owsExceptionReport.xsd");
            marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new PetascopeXmlNamespaceMapper());
            StringWriter strWriter = new StringWriter();
            marshaller.marshal(report, strWriter);
            output = strWriter.toString();
            String sub = output.substring(output.indexOf("<ows:Exception "), output.
                    indexOf("</ows:ExceptionReport>"));
            log.debug(output);
            log.debug(sub);
            try {
                output = Templates.getTemplate(Templates.EXCEPTION_REPORT, Pair.
                        of("\\{exception\\}", sub));
            } catch (Exception ex) {
                log.warn("Error handling exception report template");
            }
            log.debug("Done marshalling Error Report.");
            log.debug(output);
        } catch (JAXBException e2) {
            log.error("Stack trace: {}", e2);
            log.error("Error stack trace: " + e2);
        }
        return output;
    }

    public static String exceptionToXml(PetascopeException e) {
        return exceptionReportToXml(e.getReport());
    }

    public static String getGML(GetCoverageMetadata m, String template, boolean replaceBounds) {
        
        // TODO
        // Automatize the creation of the header: namespaces, schema locations, etc. (Mind XMLSymbols).
        
        // Range type
        String rangeFields = "";
        for (RangeField range : m.getRangeFields()) {
            rangeFields += Templates.getTemplate(Templates.RANGE_FIELD,
                    Pair.of("\\{" + Templates.KEY_FIELDNAME     + "\\}", range.getFieldName()),
                    Pair.of("\\{" + Templates.KEY_COMPONENTNAME + "\\}", range.getComponentName()),
                    Pair.of("\\{" + Templates.KEY_DATATYPE      + "\\}", range.getDatatype()),
                    Pair.of("\\{" + Templates.KEY_NILVALUES     + "\\}", range.getNilValues()),
                    Pair.of("\\{" + Templates.KEY_FIELDDESCR    + "\\}", range.getDescription()),
                    Pair.of("\\{" + Templates.KEY_ALLOWEDVALUES + "\\}", range.getAllowedValues()),
                    Pair.of("\\{" + Templates.KEY_CODE          + "\\}", range.getUomCode())
                    );
        }

        // GMLCOV metadata
        Set<String> gmlcovMetadata = m.getMetadata().getExtraMetadata(XMLSymbols.PREFIX_GMLCOV);
        String gmlcovFormattedMetadata = "";
        for (String metadataValue : gmlcovMetadata) {
            gmlcovFormattedMetadata += "<" + XMLSymbols.PREFIX_GMLCOV + ":" + XMLSymbols.LABEL_METADATA + ">" + metadataValue 
                    + "</" + XMLSymbols.PREFIX_GMLCOV + ":" + XMLSymbols.LABEL_METADATA + ">";
        }
        
        // Whole document
        String ret = Templates.getTemplate(template,
                Pair.of("\\{" + Templates.KEY_DOMAINSET_ADDITIONS + "\\}", getDomainSetAdditions(m)), // need to be first: it adds other shared "{%}" keys (eg `uomLabels')
                Pair.of("\\{" + Templates.KEY_COVERAGEID          + "\\}", m.getCoverageId()),
                Pair.of("\\{" + Templates.KEY_COVERAGETYPE        + "\\}", m.getCoverageType()),
                Pair.of("\\{" + Templates.KEY_GRIDID              + "\\}", m.getGridId()),
                Pair.of("\\{" + Templates.KEY_MPID                + "\\}", Templates.PREFIX_MP + m.getGridId()),
                Pair.of("\\{" + Templates.KEY_GRIDDIMENSION       + "\\}", String.valueOf(m.getGridDimension())),
                Pair.of("\\{" + Templates.KEY_UOMLABELS           + "\\}", m.getUomLabels()),
                Pair.of("\\{" + Templates.KEY_RANGEFIELDS         + "\\}", rangeFields),
                Pair.of("\\{" + Templates.KEY_COVERAGESUBTYPE     + "\\}", m.getCoverageType()),
                Pair.of("\\{" + Templates.KEY_AXISLABELS          + "\\}", m.getAxisLabels()),
                Pair.of("\\{" + Templates.KEY_GRIDTYPE            + "\\}", m.getGridType()),
                Pair.of("\\{" + Templates.KEY_SRSGROUP            + "\\}", getSrsGroup(m)),
                Pair.of("\\{" + Templates.KEY_SRSNAME             + "\\}", getSrsName(m)),                
                Pair.of("\\{" + Templates.KEY_METADATA            + "\\}", gmlcovFormattedMetadata));

        // RGBV need to replace bounds after knowledge of the retrieved coefficients
        if (replaceBounds && !m.getCoverageType().equals(XMLSymbols.LABEL_REFERENCEABLE_GRID_COVERAGE)) {
            ret = getBounds(ret, m);
        }
        
        return ret;
    }

    private static String getSrsGroup(GetCoverageMetadata m) {
        Bbox bbox = m.getBbox();
        if (bbox != null) {
            return " " + Templates.KEY_SRSNAME + "=\"" + bbox.getCrsName() + "\" " +
                    Templates.KEY_SRSDIMENSION + "=\"" + m.getGridDimension() + "\"";
        } else {
            return "";
        }
    }

    private static String getSrsName(GetCoverageMetadata m) {
        if (m.getCrs() != null) {
            // Need to encode the '&' that are in CCRS
            return StringUtil.escapeXmlPredefinedEntities(m.getCrs());
        } else {
            return CrsUtil.GRID_CRS;
        }
    }
    
    /**
     * Replaces the bounds of the grid
     * @param gml  The GML response
     * @param m    The metadata specific to the WCS GetCoverage request
     * @return 
     */
    public static String getBounds(String gml, GetCoverageMetadata m) {
        gml = gml.replaceAll("\\{" + Templates.KEY_LOW         + "\\}", m.getLow())
                 .replaceAll("\\{" + Templates.KEY_HIGH        + "\\}", m.getHigh())
                 .replaceAll("\\{" + Templates.KEY_AXISLABELS  + "\\}", m.getAxisLabels())
                 .replaceAll("\\{" + Templates.KEY_LOWERCORNER + "\\}", m.getDomLow())
                 .replaceAll("\\{" + Templates.KEY_UPPERCORNER + "\\}", m.getDomHigh());
        return gml;
    }
    
    /**
     * Replaces the bounds of the grid
     * @param gml
     * @param m
     * @return 
     */
    /*public static String getBounds(String gml, GetCoverageMetadata m) {
        gml = gml.replaceAll("\\{" + Templates.KEY_LOW        + "\\}", m.getLow())
                 .replaceAll("\\{" + Templates.KEY_HIGH       + "\\}", m.getHigh())
                 .replaceAll("\\{" + Templates.KEY_AXISLABELS + "\\}", m.getAxisLabels());
        return gml;
    }*/
    
    // NOTE1: rotated ReferenceableGridCoverage are not supported:
    // need to yield the offsets anyway for GML response: unity vectors.   
    // NOTE2: an ad-hoc templates was needed since dimensionality of the coverage is
    // not fixed and each offset vector needs a GML row.
    private static String getOffsetVectors(GetCoverageMetadata m) {
        String output = "";
        String[] axisNames = m.getAxisLabels().split(" ");
        // Loop through the N dimensions
        for (int i = 0; i < axisNames.length; i++) {
            if (i>0) {
                output += "\n";
            }
            output += getOffsetVectors(m, axisNames[i]);
        }
        return output;
    }
    // Overload
    //for offset-vectors of a single grid axis:
    private static String getOffsetVectors(GetCoverageMetadata m, String axisName) {
        String output = Templates.getTemplate(Templates.OFFSET_VECTOR,
                    Pair.of("\\{" + Templates.KEY_OFFSETS + "\\}", getOffsets(m, axisName)));
        return output;    
    }
    
    // Function the builds the content of offsets vector GML elements for a specified dimension.
    // Note still it is assumed that grid dimensions are exactly aligned with CRS axes.
    private static String getOffsets(GetCoverageMetadata m, String axisName) {
        String output = "";
        String[] axisNames = m.getAxisLabels().split(" ");
        // Loop through the N dimensions
        for (int i = 0; i < axisNames.length; i++) {
            if (i>0) {
                output += " ";
            }
            output += (axisNames[i].equals(axisName)) ? m.getMetadata().getDomainByName(axisNames[i]).getOffsetVector() : "0";
        }
        return output;
    }
    
    // Builds the XML for the coefficients of an irregular axis in a gmlrgrid:ReferenceableGridByVectors
    // If the axis is regular, an empty string is returned.
    // NOTE: change request to GML to let the coefficients be /optional/ (Campalani et al., EnviroInfo2013).
    public static String getCoefficients(GetCoverageMetadata m, String axisName) {
        // init
        String coefficients = "";
        CoverageMetadata meta = m.getMetadata();
        DomainElement domEl = meta.getDomainByName(axisName);
        
        // "optional" coefficients
        if (domEl.isIrregular()) {
            coefficients = "\n          <" + XMLSymbols.LABEL_COEFFICIENTS  + ">" +
                    StringUtil.listToTuple(domEl.getCoefficients(), ' ') +
                    "</" + XMLSymbols.LABEL_COEFFICIENTS + ">"
                    ;
        }
        return coefficients;
    }
        
    /**
     * Add the coefficients in a gmlrgrid:ReferenceableGridByVectors.
     * They are not known at the time of initializing the GML output, but only after processing
     * the coverage data (see petascope.wcps.server.core.crs and DbMetadataSource.getCoefficientsOfInterval()).
     * @param gml  The GML output already filled with data and metadata.
     * @param m    The metadata specific to the WCS GetCoverage request
     * @return GML where {coefficients} have been replaced with real values.
     * @throws WCSException
     * @throws PetascopeException 
     */
    public static String addCoefficients(String gml, GetCoverageMetadata m) 
            throws WCSException, PetascopeException {
        String[] axisNames = m.getAxisLabels().split(" ");
        // Loop through the N dimensions (rely on order)
        for (int i = 0; i < axisNames.length; i++) {
            gml = gml.replaceFirst("\\{" + Templates.KEY_COEFFICIENTS + "\\}", WcsUtil.getCoefficients(m, axisNames[i]));
        }
        return gml;
    }    

    // Builds the XML for the gmlrgrid:RefernceableGridByVectors domain-set
    private static String getGeneralGridAxes(GetCoverageMetadata m) {
        Bbox bbox = m.getBbox();
        String output = "";
        String[] axisNames = m.getAxisLabels().split(" ");
        // Loop through the N dimensions
        for (int i = 0; i < axisNames.length; i++) {
            if (i>0) {
                output += "\n";
            }
            output += Templates.getTemplate(Templates.GENERAL_GRID_AXIS,
                    Pair.of("\\{" + Templates.KEY_GRIDAXISSPANNED + "\\}", axisNames[i]),
                    Pair.of("\\{" + Templates.KEY_OFFSET_VECTOR   + "\\}", getOffsetVectors(m, axisNames[i]))
                    // coefficients are visible /after/ WCPS processing
                    );
        }
        return output;
    }
        
    private static String getDomainSetAdditions(GetCoverageMetadata m) {
        String ret = "";
        Bbox bbox = m.getBbox();
        if (bbox != null) {
            // Fill with origin and offset-vector(s)
            String outGml = Templates.getTemplate(Templates.GRID_ORIGIN,
                    Pair.of("\\{" + Templates.KEY_POINTID   + "\\}", m.getCoverageId() + Templates.SUFFIX_ORIGIN),
                    Pair.of("\\{" + Templates.KEY_ORIGINPOS + "\\}", bbox.getLowerCorner(m.getAxisLabels().split(" ")))
                    );
            if (m.getCoverageType().equals(XMLSymbols.LABEL_RECTIFIED_GRID_COVERAGE)) {
                // Fill with offset-vector(s)
                String offsetsGml = getOffsetVectors(m);
                outGml += "\n" + offsetsGml;
            } else if (m.getCoverageType().equals(XMLSymbols.LABEL_REFERENCEABLE_GRID_COVERAGE)) {
                // Fill with origin and GeneralGridAxis element(s)
                outGml += "\n" + getGeneralGridAxes(m);
            }
            
            return outGml;
        } else {
            log.warn("Bbox object is missing for coverage " + m.getMetadata().getCoverageName());
        }
        return ret;
    }
   
    public static Pair<String, String> toInterval(String type) {
        if (type.equals(KEY_CHAR))   { return Pair.of(CHAR_MIN,   CHAR_MAX);   } else 
        if (type.equals(KEY_UCHAR))  { return Pair.of(UCHAR_MIN,  UCHAR_MAX);  } else 
        if (type.equals(KEY_SHORT))  { return Pair.of(SHORT_MIN,  SHORT_MAX);  } else 
        if (type.equals(KEY_USHORT)) { return Pair.of(USHORT_MIN, USHORT_MAX); } else 
        if (type.equals(KEY_INT))    { return Pair.of(INT_MIN,    INT_MAX);    } else 
        if (type.equals(KEY_UINT))   { return Pair.of(UINT_MIN,   UINT_MAX);   } else 
        if (type.equals(KEY_LONG))   { return Pair.of(LONG_MIN,   LONG_MAX);   } else 
        if (type.equals(KEY_ULONG))  { return Pair.of(ULONG_MIN,  ULONG_MAX);  } else 
        if (type.equals(KEY_FLOAT))  { return Pair.of(FLOAT_MIN,  FLOAT_MAX);  } else 
        if (type.equals(KEY_DOUBLE)) { return Pair.of(DOUBLE_MIN, DOUBLE_MAX); }
        return Pair.of("", "");
    }

    /**
     * @return the minimum interval from a and b
     */
    public static String min(String a, String b) {
        String[] as = a.split(":");
        String[] bs = b.split(":");
        if (as.length < bs.length) {
            return a;
        } else if (as.length > bs.length) {
            return b;
        }
        Integer al = toInt(as, 0);
        Integer bl = toInt(bs, 0);
        if (as.length == 1) {
            if (al < bl) {
                return a;
            } else {
                return b;
            }
        }
        Integer ah = toInt(as, 1);
        Integer bh = toInt(bs, 1);

        Integer rl = al;
        if (rl > bl) {
            rl = bl;
        }
        Integer rh = ah;
        if (rh > bh) {
            rh = bh;
        }

        return toStr(rl) + ":" + toStr(rh);
    }

    private static String toStr(Integer i) {
        if (i == Integer.MAX_VALUE) {
            return "*";
        } else {
            return i.toString();
        }
    }

    private static Integer toInt(String[] s, int i) {
        if (s[i].equals("*")) {
            return Integer.MAX_VALUE;
        } else {
            return Integer.parseInt(s[i]);
        }
    }
}
