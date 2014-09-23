package petascope.wcps2.translator;

import org.apache.commons.lang3.math.NumberUtils;
import petascope.exceptions.PetascopeException;
import petascope.wcps2.error.managed.processing.RangeFieldNotFound;

/**
 * Translation node from wcps to rasql for range subsetting.
 * Example:
 * <code>
 * $c1.red
 * </code>
 * translates to
 * <code>
 * c1.red
 * </code>
 * select encode(scale( ((c[*:*,*:*,0:0]).1) [*:*,*:*,0], [0:2,0:1] ), "csv") from irr_cube_2 AS c
 * SELECT encode(SCALE( ((c[*:*,*:*,0:0]).1) [*:*,*:*,0:0], [0:2,0:1]), "csv" ) FROM irr_cube_2 AS c
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class RangeSubsetting extends CoverageExpression {

    public RangeSubsetting(String rangeType, CoverageExpression coverageExp) {
        this.rangeType = rangeType;
        this.coverageExp = coverageExp;
        addChild(coverageExp);
        setCoverage(coverageExp.getCoverage());
    }

    @Override
    public String toRasql() {
        String rangeField = this.rangeType.trim();
        if (!NumberUtils.isNumber(rangeField)) {
            try {
                rangeField = getCoverage().getCoverageMetadata().getRangeIndexByName(this.rangeType.trim()).toString();
            } catch (PetascopeException e) {
                throw new RangeFieldNotFound(this.rangeType.trim());
            }
        }
        String template = TEMPLATE.replace("$coverageExp", this.coverageExp.toRasql().trim()).replace("$rangeType", rangeField);
        return template;
    }

    private String rangeType;
    private IParseTreeNode coverageExp;
    private final String TEMPLATE = "$coverageExp.$rangeType";
}
