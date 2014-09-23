package petascope.wcps2.translator;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Translation class fo the range constructor expressions
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class RangeConstructorExpression extends CoverageExpression {
    /**
     * Constructor for the class
     *
     * @param fieldStructure the structure of the range fields
     */
    public RangeConstructorExpression(Map<String, CoverageExpression> fieldStructure) {
        this.fieldStructure = fieldStructure;
        setCoverage(fieldStructure.values().iterator().next().getCoverage());
    }

    @Override
    public String toRasql() {
        List<String> translatedFields = new ArrayList<String>();
        int index = 0;
        for (Map.Entry<String, CoverageExpression> entry : fieldStructure.entrySet()) {
            translatedFields.add(entry.getValue().toRasql() + " * " + generateIdentityStruct(index, fieldStructure.entrySet().size()));
            index++;
        }
        return TEMPLATE.replace("$fieldDefinitions", StringUtils.join(translatedFields, " + "));
    }

    private String generateIdentityStruct(int position, int size) {
        List<String> parts = new ArrayList<String>(size);
        for (int j = 0; j < size; j++) {
            if (j == position) {
                parts.add("1c");
            } else {
                parts.add("0c");
            }
        }
        return "{" + StringUtils.join(parts, ",") + "}";
    }

    private final Map<String, CoverageExpression> fieldStructure;
    private final String TEMPLATE = "($fieldDefinitions)";
}
