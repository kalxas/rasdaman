package petascope.wcps2.translator;

import org.apache.commons.lang3.StringUtils;
import petascope.util.CrsUtil;
import petascope.wcps.metadata.CoverageInfo;
import petascope.wcps2.metadata.Coverage;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to translate an encoded coverage
 * <p/>
 * <code>
 * encode($c, "image/png", "NODATA=0")
 * </code>
 * translates to
 * <code>
 * encode(c, "png", "NODATA=0")
 * </code>
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class EncodedCoverage extends IParseTreeNode {

    /**
     * Constructor for the class
     *
     * @param coverageExpression the coverage expression that is encoded
     * @param format             the format in which to encode it
     * @param otherParams        parameters to be supplied to the encoder
     */
    public EncodedCoverage(IParseTreeNode coverageExpression, String format, List<String> otherParams) {
        this.format = format;
        this.coverageExpression = coverageExpression;
        this.otherParams = otherParams;
        addChild(coverageExpression);
    }

    @Override
    public String toRasql() {
        String result, otherParamsString = "";

        //strip first part of the mime type because rasdaman encode function does not support proper mime types yet
        //e.g. image/png -> png; text/csv -> csv and so on.
        String adaptedFormat = format.split("/").length > 1 ? ('"' + format.split("/")[1]) : format;
        //tiff format goes to rasdaman GTiff
        if(adaptedFormat.toLowerCase().contains(WCPS_TIFF)){
            adaptedFormat = RASDAMAN_TIFF;
            addTiffExtraParams();
        }

        if (otherParams != null && otherParams.size() > 0) {
            otherParamsString = ", \"" + StringUtils.join(otherParams, ";").replace("\"", "") + "\"";
        }
        //get the right template
        String template = getTemplate(adaptedFormat);
        result = template.replace("$arrayOps", coverageExpression.toRasql())
            .replace("$format", adaptedFormat)
            .replace("$otherParams", otherParamsString);
        return result;
    }

    /**
     * Adds the required extra params in case the result is tiff encoded.
     */
    private void addTiffExtraParams(){
        CoverageInfo coverageInfo = ((CoverageExpression) this.coverageExpression).getCoverage().getCoverageInfo();
        String xMin = String.valueOf(coverageInfo.getBbox().getMinX());
        String xMax = String.valueOf(coverageInfo.getBbox().getMaxX());
        String yMin = String.valueOf(coverageInfo.getBbox().getMinY());
        String yMax = String.valueOf(coverageInfo.getBbox().getMaxY());
        String uriCrs = coverageInfo.getCoverageCrs();
        String crs = CrsUtil.CrsUri.getAuthority(uriCrs) + ":" + CrsUtil.CrsUri.getCode(uriCrs);
        for(String param: TIFF_EXTRA_PARAMS){
            this.otherParams.add(param.replace("$xmin", xMin).replace("$xmax", xMax).replace("$ymin", yMin).
                    replace("$ymax", yMax).replace("$crs", crs));
        }
    }

    @Override
    protected String nodeInformation() {
        return new StringBuilder("(").append(format).append(", ").append(otherParams).append(")").toString();
    }

    /**
     * Rerturns the right template, depending on the operation that ahs been executed.
     * @param operation
     * @return
     */
    private static String getTemplate(String operation){
        if(operation.toLowerCase().replaceAll("\"", "").equals(DEM_OPERATION)){
            return NON_GDAL_OPERATION_TEMPLATE.replace("$operation", DEM_OPERATION);
        }
        return TEMPLATE;
    }

    private final String format;
    private final IParseTreeNode coverageExpression;
    private final List<String> otherParams;
    private final static String TEMPLATE = "encode($arrayOps, $format $otherParams)";
    private final static String NON_GDAL_OPERATION_TEMPLATE = "$operation($arrayOps $otherParams)";
    private final static String DEM_OPERATION = "dem";
    private final static String WCPS_TIFF = "tiff";
    private final static String RASDAMAN_TIFF = "\"GTiff\"";
    private final static List<String> TIFF_EXTRA_PARAMS = new ArrayList<String>(5);
    static {
        TIFF_EXTRA_PARAMS.add("xmin=$xmin");
        TIFF_EXTRA_PARAMS.add("xmax=$xmax");
        TIFF_EXTRA_PARAMS.add("ymin=$ymin");
        TIFF_EXTRA_PARAMS.add("ymax=$ymax");
        TIFF_EXTRA_PARAMS.add("crs=$crs");
    }
}
