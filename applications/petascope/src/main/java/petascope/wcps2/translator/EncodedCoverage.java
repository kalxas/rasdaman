package petascope.wcps2.translator;

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
    public EncodedCoverage(IParseTreeNode coverageExpression, String format, String otherParams) {
        this.format = format;
        this.coverageExpression = coverageExpression;
        this.otherParams = otherParams;
        addChild(coverageExpression);
    }

    @Override
    public String toRasql() {
        String template, otherParamsString = "";

        //strip first part of the mime type because rasdaman encode function does not support proper mime types yet
        //e.g. image/png -> png; text/csv -> csv and so on.
        String adaptedFormat = format.split("/").length > 1 ? ('"' + format.split("/")[1]) : format;

        if (otherParams != null && otherParams != "") {
            otherParamsString = "," + otherParams;
        }
        template = TEMPLATE.replace("$arrayOps", coverageExpression.toRasql())
            .replace("$format", adaptedFormat)
            .replace("$otherParams", otherParamsString);
        return template;
    }

    @Override
    protected String nodeInformation() {
        return new StringBuilder("(").append(format).append(", ").append(otherParams).append(")").toString();
    }

    private final String format;
    private final IParseTreeNode coverageExpression;
    private final String otherParams;
    private final static String TEMPLATE = "encode($arrayOps, $format $otherParams)";
}
